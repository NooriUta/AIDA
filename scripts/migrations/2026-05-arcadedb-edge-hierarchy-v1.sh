#!/bin/bash
# ─────────────────────────────────────────────────────────────────────────────
# Sprint 1.1 EDGE_TAXONOMY_V1 — ABSTRACT hierarchy migration (per-tenant)
# Reference: docs/current/specs/hound/EDGE_TAXONOMY_ANALYSIS.md §14, ADR-HND-009
#
# Round 9 finding: ArcadeDB 26.4.2 не поддерживает 'EXTENDS E ABSTRACT'.
# Pattern: CREATE EDGE TYPE parent + ALTER CUSTOM abstract=true; ALTER child SUPERTYPE +parent.
#
# Creates:
# - 8 top-level logical-abstract types: ATOM_REF, NAMESPACE, STMT_HAS, LINEAGE_FLOW,
#   DDL_OP, JOIN_REF, PLTYPE_REF, CONSTRAINT_REF
# - 4 nested under LINEAGE_FLOW: FLOW, TABLE_DATA_FLOW, RECORD_FLOW, WRITE_SIDE
# - 4 new ATOM_REF subtypes: ATOM_REF_VARIABLE/PARAMETER/FUNCTION/SEQUENCE
# - 28 ALTER TYPE SUPERTYPE statements (existing types ⇒ parents)
#
# Usage:
#   AB_USER=root AB_PASS=playwithdata AB_HOST=localhost:2480 ./2026-05-arcadedb-edge-hierarchy-v1.sh <db_name>
#
# Idempotent — safe to re-run.
# ─────────────────────────────────────────────────────────────────────────────

set -euo pipefail

DB_NAME="${1:-}"
if [ -z "$DB_NAME" ]; then echo "Usage: $0 <db_name>"; exit 1; fi

AB_USER="${AB_USER:-root}"
AB_PASS="${AB_PASS:-playwithdata}"
AB_HOST="${AB_HOST:-localhost:2480}"

run_sql() {
  local sql="$1"
  local result
  result=$(curl -s -u "${AB_USER}:${AB_PASS}" -H "Content-Type: application/json" -X POST \
    "http://${AB_HOST}/api/v1/command/${DB_NAME}" \
    -d "{\"language\":\"sql\",\"command\":\"${sql}\"}" 2>&1)
  if echo "$result" | grep -q '"error"'; then
    if echo "$result" | grep -qiE "already exists|already a super"; then
      echo "  ⊘ skip (idempotent): ${sql}"
    else
      echo "  ✗ ${sql}"
      echo "    $result"
    fi
  else
    echo "  ✓ ${sql}"
  fi
}

echo "═══════════════════════════════════════════════════════════════════════"
echo " Sprint 1.1 EDGE_TAXONOMY_V1 — migration for tenant: ${DB_NAME}"
echo "═══════════════════════════════════════════════════════════════════════"

echo
echo "▸ Step 1: Create 8 top-level logical-abstract types"
for t in ATOM_REF NAMESPACE STMT_HAS LINEAGE_FLOW DDL_OP JOIN_REF PLTYPE_REF CONSTRAINT_REF; do
  run_sql "CREATE EDGE TYPE ${t} IF NOT EXISTS"
  run_sql "ALTER TYPE ${t} CUSTOM abstract = true"
done

echo
echo "▸ Step 2: Create 4 nested under LINEAGE_FLOW"
for t in FLOW TABLE_DATA_FLOW RECORD_FLOW WRITE_SIDE; do
  run_sql "CREATE EDGE TYPE ${t} IF NOT EXISTS EXTENDS LINEAGE_FLOW"
  run_sql "ALTER TYPE ${t} CUSTOM abstract = true"
done

echo
echo "▸ Step 3: ALTER existing types — assign supertype"
# ATOM_REF (5)
for t in ATOM_REF_TABLE ATOM_REF_COLUMN ATOM_REF_STMT ATOM_REF_OUTPUT_COL ATOM_REF_PLTYPE_FIELD; do
  run_sql "ALTER TYPE ${t} SUPERTYPE +ATOM_REF"
done
# NAMESPACE (9)
for t in BELONGS_TO_APP CONTAINS_SCHEMA CONTAINS_TABLE HAS_COLUMN CONTAINS_ROUTINE \
         BELONGS_TO_SESSION CONTAINS_STMT HAS_PARAMETER HAS_VARIABLE; do
  run_sql "ALTER TYPE ${t} SUPERTYPE +NAMESPACE"
done
# STMT_HAS (3)
for t in HAS_OUTPUT_COL HAS_JOIN HAS_AFFECTED_COL; do
  run_sql "ALTER TYPE ${t} SUPERTYPE +STMT_HAS"
done
# FLOW (2 — JOIN_FLOW, UNION_FLOW removed in Sprint 0.1)
for t in DATA_FLOW FILTER_FLOW; do
  run_sql "ALTER TYPE ${t} SUPERTYPE +FLOW"
done
# TABLE_DATA_FLOW (2; READS_FROM direction inverted in Sprint 1.2 separately)
for t in READS_FROM WRITES_TO; do
  run_sql "ALTER TYPE ${t} SUPERTYPE +TABLE_DATA_FLOW"
done
# RECORD_FLOW (2 — RECORD_USED_IN/HAS_RECORD_FIELD remain singletons)
for t in BULK_COLLECTS_INTO RETURNS_INTO; do
  run_sql "ALTER TYPE ${t} SUPERTYPE +RECORD_FLOW"
done
# JOIN_REF (2)
for t in JOIN_SOURCE_TABLE JOIN_TARGET_TABLE; do
  run_sql "ALTER TYPE ${t} SUPERTYPE +JOIN_REF"
done
# PLTYPE_REF (3 — MULTISET_INTO остаётся singleton lineage-skip)
for t in DECLARES_TYPE OF_TYPE INSTANTIATES_TYPE; do
  run_sql "ALTER TYPE ${t} SUPERTYPE +PLTYPE_REF"
done
# DDL_OP (1 — DDL_MODIFIES после Sprint 0.1 F-2 folding)
run_sql "ALTER TYPE DDL_MODIFIES SUPERTYPE +DDL_OP"

echo
echo "▸ Step 4: Create 4 new ATOM_REF subtypes (DDL only — writers in Phase 2)"
for t in ATOM_REF_VARIABLE ATOM_REF_PARAMETER ATOM_REF_FUNCTION ATOM_REF_SEQUENCE; do
  run_sql "CREATE EDGE TYPE ${t} IF NOT EXISTS EXTENDS ATOM_REF"
done
run_sql "CREATE PROPERTY ATOM_REF_PARAMETER.param_mode IF NOT EXISTS STRING"

echo
echo "▸ Step 5: Verify hierarchy"
echo "  Total edge types:"
curl -s -u "${AB_USER}:${AB_PASS}" -H "Content-Type: application/json" -X POST \
  "http://${AB_HOST}/api/v1/command/${DB_NAME}" \
  -d "{\"language\":\"sql\",\"command\":\"SELECT COUNT(*) AS cnt FROM schema:types WHERE type='edge'\"}" \
  | grep -oE '"cnt":[0-9]+' | grep -oE '[0-9]+'
echo "  Logical-abstract groups (CUSTOM abstract=true):"
curl -s -u "${AB_USER}:${AB_PASS}" -H "Content-Type: application/json" -X POST \
  "http://${AB_HOST}/api/v1/command/${DB_NAME}" \
  -d "{\"language\":\"sql\",\"command\":\"SELECT name FROM schema:types WHERE customFields.abstract = 'true' ORDER BY name\"}"
echo

echo "✅ Sprint 1.1 EDGE_TAXONOMY_V1 migration complete for ${DB_NAME}"
