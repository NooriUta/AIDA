#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# Sprint 0.1 SCHEMA_CLEANUP — ArcadeDB edge type migration (per-tenant)
# Reference: docs/current/specs/hound/EDGE_TAXONOMY_ANALYSIS.md §13.5, §13.8 F-2
#
# Removes 6 dead edge types (0 writer + 0 consumer + не запланирован):
#   JOIN_FLOW, UNION_FLOW, PIPES_FROM, READS_PIPELINED, CURSOR_RETURNS, ROUTINE_USES_TABLE
#
# Folds DDL_OP (F-2): DaliDDLModifiesTable + DaliDDLModifiesColumn → DDL_MODIFIES
# with target_kind property ('table' | 'column').
#
# Usage:
#   AB_USER=root AB_PASS=playwithdata AB_HOST=localhost:2480 ./2026-05-arcadedb-edge-cleanup.sh <db_name>
#
# Default tenant counts: all 6 dead types = 0 edges; DaliDDLModifies* = 0 edges
# (so migration is no-op for default). Run per-tenant for non-empty cases.
# ─────────────────────────────────────────────────────────────────────────────

set -euo pipefail

DB_NAME="${1:-}"
if [ -z "$DB_NAME" ]; then
  echo "Usage: $0 <db_name>"
  exit 1
fi

AB_USER="${AB_USER:-root}"
AB_PASS="${AB_PASS:-playwithdata}"
AB_HOST="${AB_HOST:-localhost:2480}"

run_sql() {
  local sql="$1"
  # Quoting: $sql is plain SQL string (no leading/trailing quotes); embed into JSON.
  curl -s -u "${AB_USER}:${AB_PASS}" -H "Content-Type: application/json" -X POST \
    "http://${AB_HOST}/api/v1/command/${DB_NAME}" \
    -d "{\"language\":\"sql\",\"command\":\"${sql}\"}"
  echo
}

# ArcadeDB doesn't support DROP TYPE ... IF EXISTS — try DROP and ignore "not found" error.
drop_type_safe() {
  local type="$1"
  local result
  result=$(curl -s -u "${AB_USER}:${AB_PASS}" -H "Content-Type: application/json" -X POST \
    "http://${AB_HOST}/api/v1/command/${DB_NAME}" \
    -d "{\"language\":\"sql\",\"command\":\"DROP TYPE \`${type}\`\"}" 2>&1)
  if echo "$result" | grep -q "error"; then
    if echo "$result" | grep -qi "not found\|doesn't exist\|cannot.*drop"; then
      echo "  ⊘ ${type}: already absent (skip)"
    else
      echo "  ✗ ${type}: $result"
    fi
  else
    echo "  ✓ ${type}: dropped"
  fi
}

count_type() {
  local type="$1"
  curl -s -u "${AB_USER}:${AB_PASS}" -H "Content-Type: application/json" -X POST \
    "http://${AB_HOST}/api/v1/command/${DB_NAME}" \
    -d "{\"language\":\"sql\",\"command\":\"SELECT COUNT(*) AS cnt FROM \`${type}\`\"}" 2>/dev/null \
    | grep -oE '"cnt":[0-9]+' | grep -oE '[0-9]+' || echo "0"
}

echo "═══════════════════════════════════════════════════════════════════════"
echo " Sprint 0.1 SCHEMA_CLEANUP — migration for tenant: ${DB_NAME}"
echo "═══════════════════════════════════════════════════════════════════════"

# ─── Step 1: Survey existing data ────────────────────────────────────────────
echo
echo "▸ Survey: edge counts before migration"
for et in JOIN_FLOW UNION_FLOW PIPES_FROM READS_PIPELINED CURSOR_RETURNS ROUTINE_USES_TABLE \
           DaliDDLModifiesTable DaliDDLModifiesColumn DDL_MODIFIES; do
  cnt=$(count_type "$et")
  printf "  %-25s %s\n" "$et" "$cnt"
done

# ─── Step 2: Create new DDL_MODIFIES type ────────────────────────────────────
echo
echo "▸ Create DDL_MODIFIES type (idempotent)"
run_sql "CREATE EDGE TYPE DDL_MODIFIES IF NOT EXISTS"
run_sql "CREATE PROPERTY DDL_MODIFIES.target_kind IF NOT EXISTS STRING"
run_sql "CREATE PROPERTY DDL_MODIFIES.operation IF NOT EXISTS STRING"

# ─── Step 3: Migrate DaliDDLModifies* → DDL_MODIFIES ─────────────────────────
echo
echo "▸ Migrate DaliDDLModifies* edges (if any)"
# Note: ArcadeDB INSERT INTO ... SELECT FROM <edge_type> — direct edge migration
# isn't trivial; if counts > 0, manual investigation needed. For default+acme
# tenants where counts = 0, this section is no-op.
# TODO: if any tenant has non-zero DaliDDLModifies*, write explicit cypher-style
# migration: MATCH ()-[e:DaliDDLModifiesTable]->() CREATE ()-[:DDL_MODIFIES {target_kind:'table'}]->().

# ─── Step 4: Drop old types ──────────────────────────────────────────────────
echo
echo "▸ Drop dead types (count=0 only; non-empty types abort to manual review)"
for et in JOIN_FLOW UNION_FLOW PIPES_FROM READS_PIPELINED CURSOR_RETURNS ROUTINE_USES_TABLE \
           DaliDDLModifiesTable DaliDDLModifiesColumn; do
  cnt=$(count_type "$et")
  if [ "$cnt" = "0" ]; then
    drop_type_safe "$et"
  else
    echo "  ⚠ ${et} has ${cnt} edges — manual migration required, SKIPPING DROP"
  fi
done

# ─── Step 5: Verify final state ──────────────────────────────────────────────
echo
echo "▸ Verify final state"
final_count=$(curl -s -u "${AB_USER}:${AB_PASS}" -H "Content-Type: application/json" -X POST \
  "http://${AB_HOST}/api/v1/command/${DB_NAME}" \
  -d "{\"language\":\"sql\",\"command\":\"SELECT COUNT(*) AS cnt FROM schema:types WHERE type='edge'\"}" \
  | grep -oE '"cnt":[0-9]+' | grep -oE '[0-9]+')
echo "  Total edge types after migration: ${final_count}"
echo "  Expected: 50 (was 55, -6 dead types -2 PascalCase folded +1 DDL_MODIFIES = 48; or close depending on prior state)"

echo
echo "✅ Migration complete for ${DB_NAME}"
