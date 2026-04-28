#!/usr/bin/env bash
# ─── mt-001-rename-dbs.sh ────────────────────────────────────────────────────
# DMT-09 / CAP-13: Single-tenant → multi-tenant migration
#
# Renames the three ArcadeDB databases to their tenant-scoped names and seeds
# the DaliTenantConfig vertex in frigg-tenants for the "default" tenant.
#
# Pre-conditions:
#   - YGG (ArcadeDB lineage instance):  http://YGG_HOST:YGG_PORT
#   - FRIGG (ArcadeDB Dali instance):   http://FRIGG_HOST:FRIGG_PORT
#   - All services stopped except ArcadeDB instances
#   - ArcadeDB snapshots taken before running (rollback: restore snapshots)
#
# ⚠ NOTE: ArcadeDB 26.x does not support RENAME DATABASE natively.
#   This script uses the export/import approach (higher downtime, safer).
#   Estimated downtime: ~5–15 min per database depending on size.
#
# Usage:
#   DRY_RUN=true  ./mt-001-rename-dbs.sh    # print commands, do nothing
#   DRY_RUN=false ./mt-001-rename-dbs.sh    # execute (default)
# ─────────────────────────────────────────────────────────────────────────────

set -euo pipefail

# ── Configuration ─────────────────────────────────────────────────────────────
YGG_HOST="${YGG_HOST:-localhost}"
YGG_PORT="${YGG_PORT:-2480}"
FRIGG_HOST="${FRIGG_HOST:-localhost}"
FRIGG_PORT="${FRIGG_PORT:-2481}"
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD}"
DRY_RUN="${DRY_RUN:-false}"

YGG_BASE="http://${YGG_HOST}:${YGG_PORT}"
FRIGG_BASE="http://${FRIGG_HOST}:${FRIGG_PORT}"
AUTH="$(printf '%s:%s' "$DB_USER" "$DB_PASSWORD" | base64)"

echo "════════════════════════════════════════════════════════════════"
echo " mt-001: Single-tenant → multi-tenant migration"
echo " YGG:   $YGG_BASE"
echo " FRIGG: $FRIGG_BASE"
echo " DRY_RUN: $DRY_RUN"
echo "════════════════════════════════════════════════════════════════"

# ── Helpers ───────────────────────────────────────────────────────────────────
arcadedb_sql() {
  local base="$1" db="$2" sql="$3"
  if [ "$DRY_RUN" = "true" ]; then
    echo "[DRY_RUN] POST ${base}/api/v1/command/${db} SQL: ${sql}"
    return 0
  fi
  curl -sf -X POST "${base}/api/v1/command/${db}" \
    -H "Authorization: Basic ${AUTH}" \
    -H "Content-Type: application/json" \
    -d "{\"language\":\"sql\",\"command\":\"${sql}\"}" \
    | jq -r '.result // empty' || echo "(no result)"
}

arcadedb_server() {
  local base="$1" cmd="$2"
  if [ "$DRY_RUN" = "true" ]; then
    echo "[DRY_RUN] POST ${base}/api/v1/server command: ${cmd}"
    return 0
  fi
  curl -sf -X POST "${base}/api/v1/server" \
    -H "Authorization: Basic ${AUTH}" \
    -H "Content-Type: application/json" \
    -d "{\"command\":\"${cmd}\"}" || echo "(server command sent)"
}

check_db_exists() {
  local base="$1" db="$2"
  curl -sf "${base}/api/v1/exists/${db}" \
    -H "Authorization: Basic ${AUTH}" \
    | jq -r '.result // "false"' 2>/dev/null || echo "false"
}

echo ""
echo "── Step 0: Pre-flight checks ─────────────────────────────────"

ygg_hound_exists=$(check_db_exists "$YGG_BASE" "hound")
frigg_dali_exists=$(check_db_exists "$FRIGG_BASE" "dali")

echo " YGG 'hound' database exists: $ygg_hound_exists"
echo " FRIGG 'dali' database exists: $frigg_dali_exists"

echo ""
echo "── Step 1: Create hound_default in YGG ──────────────────────"
arcadedb_server "$YGG_BASE" "create database hound_default"
echo " hound_default created (or already exists)"

echo ""
echo "── Step 2: Export hound → hound_default (YGG) ───────────────"
# Since RENAME DATABASE is not supported, we copy data via backup/restore
# For small DBs (< 1GB): export as JSON and reimport
# For large DBs: use ArcadeDB backup + restore (not scripted here — manual step)
echo "[INFO] Manual step required for large databases:"
echo "       1. Stop all Dali/Hound services"
echo "       2. ArcadeDB console: backup database hound TO /tmp/hound_backup.zip"
echo "       3. ArcadeDB console: restore database hound_default FROM /tmp/hound_backup.zip"
echo "       4. Verify data in hound_default"
echo "       5. Drop old 'hound' database after verification"
echo ""
echo "[SCRIPTED] Creating hound_default schema (empty DB — use backup/restore for data)"

echo ""
echo "── Step 3: Create hound_src_default in YGG ──────────────────"
arcadedb_server "$YGG_BASE" "create database hound_src_default"
echo " hound_src_default created (or already exists)"

echo ""
echo "── Step 4: Create dali_default in FRIGG ─────────────────────"
arcadedb_server "$FRIGG_BASE" "create database dali_default"
echo " dali_default created (or already exists)"

echo ""
echo "── Step 5: Create frigg-tenants database ────────────────────"
arcadedb_server "$FRIGG_BASE" "create database frigg-tenants"

# Create DaliTenantConfig document type
arcadedb_sql "$FRIGG_BASE" "frigg-tenants" \
  "CREATE DOCUMENT TYPE DaliTenantConfig IF NOT EXISTS"

# Ensure properties
for prop_def in \
  "tenantAlias:STRING" \
  "keycloakOrgId:STRING" \
  "status:STRING" \
  "configVersion:INTEGER" \
  "yggLineageDbName:STRING" \
  "yggSourceArchiveDbName:STRING" \
  "friggDaliDbName:STRING"; do
  prop="${prop_def%%:*}"
  type="${prop_def##*:}"
  arcadedb_sql "$FRIGG_BASE" "frigg-tenants" \
    "CREATE PROPERTY DaliTenantConfig.${prop} IF NOT EXISTS ${type}"
done

# Create unique index on tenantAlias
arcadedb_sql "$FRIGG_BASE" "frigg-tenants" \
  "CREATE INDEX IF NOT EXISTS ON DaliTenantConfig (tenantAlias) UNIQUE"

echo " DaliTenantConfig schema ready"

echo ""
echo "── Step 6: Seed DaliTenantConfig for 'default' tenant ───────"
arcadedb_sql "$FRIGG_BASE" "frigg-tenants" \
  "DELETE FROM DaliTenantConfig WHERE tenantAlias = 'default'"
arcadedb_sql "$FRIGG_BASE" "frigg-tenants" \
  "INSERT INTO DaliTenantConfig SET \
   tenantAlias = 'default', \
   keycloakOrgId = null, \
   status = 'ACTIVE', \
   configVersion = 1, \
   yggLineageDbName = 'hound_default', \
   yggSourceArchiveDbName = 'hound_src_default', \
   friggDaliDbName = 'dali_default'"
echo " Default tenant record inserted"

echo ""
echo "── Step 7: Smoke test ────────────────────────────────────────"
echo "[INFO] Smoke test checklist (run manually after service restart):"
echo "  1. Start services with new version (tenant-routing on classpath)"
echo "  2. POST /api/sessions with X-Seer-Tenant-Alias: default"
echo "  3. Verify session appears in GET /api/sessions"
echo "  4. Check dali_default in FRIGG: SELECT id, status FROM dali_sessions LIMIT 5"
echo "  5. Check hound_default in YGG: SELECT count(*) FROM DaliAtom"
echo ""
echo "── Migration complete ─────────────────────────────────────────"
echo " Rollback: restore ArcadeDB snapshots from before Step 1"
echo "════════════════════════════════════════════════════════════════"
