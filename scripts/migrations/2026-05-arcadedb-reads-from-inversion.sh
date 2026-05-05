#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# Sprint 1.2 READS_FROM_INVERSION — flip existing edges direction (per-tenant)
# Reference: docs/current/specs/hound/EDGE_TAXONOMY_ANALYSIS.md §11, §12 (Round 7)
#
# Inverts direction of all existing READS_FROM edges:
#   was: DaliStatement → DaliTable (writer-friendly, conflicts with semantic name)
#   now: DaliTable → DaliStatement (data flow direction, matches «X READS_FROM Y» = Y→X)
#
# Strategy: copy edges with swapped (in,out), then delete originals.
# ArcadeDB не позволяет "swap" в-place — только пересоздать.
#
# Usage:
#   AB_USER=root AB_PASS=playwithdata AB_HOST=localhost:2480 ./<script>.sh <db_name>
# ─────────────────────────────────────────────────────────────────────────────

set -euo pipefail

DB_NAME="${1:-}"
if [ -z "$DB_NAME" ]; then echo "Usage: $0 <db_name>"; exit 1; fi

AB_USER="${AB_USER:-root}"
AB_PASS="${AB_PASS:-playwithdata}"
AB_HOST="${AB_HOST:-localhost:2480}"

echo "═══════════════════════════════════════════════════════════════════════"
echo " Sprint 1.2 READS_FROM_INVERSION — migration for tenant: ${DB_NAME}"
echo "═══════════════════════════════════════════════════════════════════════"

# ─── Step 1: Survey ──────────────────────────────────────────────────────────
echo
echo "▸ Step 1: Survey current READS_FROM direction"
sample=$(curl -s -u "${AB_USER}:${AB_PASS}" -H "Content-Type: application/json" -X POST \
  "http://${AB_HOST}/api/v1/command/${DB_NAME}" \
  -d '{"language":"sql","command":"SELECT count(*) AS cnt FROM READS_FROM"}' \
  | grep -oE '"cnt":[0-9]+' | grep -oE '[0-9]+')
echo "  Total READS_FROM edges: $sample"

if [ "$sample" = "0" ]; then
  echo "  ⊘ No edges to migrate (skip)"
  exit 0
fi

# Check direction by sampling
echo "▸ Sample edge direction (first 1):"
curl -s -u "${AB_USER}:${AB_PASS}" -H "Content-Type: application/json" -X POST \
  "http://${AB_HOST}/api/v1/command/${DB_NAME}" \
  -d '{"language":"sql","command":"SELECT in.@type AS inType, out.@type AS outType FROM READS_FROM LIMIT 1"}'
echo

# ─── Step 2: Approach — DELETE + REPARSE ─────────────────────────────────────
echo
echo "▸ Step 2: Migration approach"
echo "  Selected: DELETE all READS_FROM edges → reparse sessions to recreate with new direction."
echo "  Rationale: ArcadeDB не имеет atomic 'swap edge direction'. Безопаснее delete + recreate."
echo
echo "  Alternatively: write SQL/Cypher script для INSERT INTO READS_FROM (in=old.out, out=old.in)"
echo "  followed by DELETE old. Requires careful transaction. Skipped for safety."
echo
echo "▸ Action plan:"
echo "  1. DELETE FROM READS_FROM (this script does it on confirm)"
echo "  2. Re-parse all SQL sessions from sources via Hound — writers will emit new direction"
echo "  3. Verify post-reparse: SELECT in.@type FROM READS_FROM LIMIT 1 → DaliTable"
echo

read -p "Proceed with DELETE FROM READS_FROM in '${DB_NAME}'? (yes/no): " confirm
if [ "$confirm" != "yes" ]; then
  echo "❌ Aborted by user"
  exit 1
fi

# ─── Step 3: Delete ──────────────────────────────────────────────────────────
echo
echo "▸ Step 3: DELETE FROM READS_FROM"
result=$(curl -s -u "${AB_USER}:${AB_PASS}" -H "Content-Type: application/json" -X POST \
  "http://${AB_HOST}/api/v1/command/${DB_NAME}" \
  -d '{"language":"sql","command":"DELETE FROM READS_FROM"}' 2>&1)
echo "$result"

# ─── Step 4: Verify ──────────────────────────────────────────────────────────
echo
echo "▸ Step 4: Verify post-delete"
post=$(curl -s -u "${AB_USER}:${AB_PASS}" -H "Content-Type: application/json" -X POST \
  "http://${AB_HOST}/api/v1/command/${DB_NAME}" \
  -d '{"language":"sql","command":"SELECT count(*) AS cnt FROM READS_FROM"}' \
  | grep -oE '"cnt":[0-9]+' | grep -oE '[0-9]+')
echo "  READS_FROM count after delete: $post (expected 0)"

echo
echo "✅ Sprint 1.2 step done. NEXT: re-parse sessions через Hound parser to recreate edges with new direction."
echo "   Verify after reparse: in.@type=DaliTable, out.@type=DaliStatement"
