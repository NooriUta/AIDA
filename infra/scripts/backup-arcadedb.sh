#!/usr/bin/env bash
# backup-arcadedb.sh — trigger ArcadeDB backup for all AIDA databases
#                       and upload to Yandex Object Storage.
#
# Multi-tenant layout (MT-001):
#   FRIGG (port 2481): dali_default, frigg-tenants, frigg-users, frigg-sessions, heimdall
#   YGG   (port 2480): hound_default, hound_src_default
#   Note: per-tenant DBs (dali_acme, hound_acme, …) are also backed up if present.
#
# Usage:
#   ./infra/scripts/backup-arcadedb.sh
#
# Scheduled via cron:
#   0 3 * * * /opt/seer-studio/infra/scripts/backup-arcadedb.sh >> /var/log/aida-backup.log 2>&1
#
# Requires:
#   - aws CLI (S3-compatible, used with Yandex Object Storage endpoint)
#   - FRIGG_PASSWORD and ARCADEDB_ADMIN_PASSWORD in .env.prod
#   - Object Storage bucket "aida-arcadedb-backups" created via Terraform
#
set -euo pipefail

DEST_DIR="/opt/seer-studio"
ENV_FILE="${DEST_DIR}/.env.prod"
BUCKET="s3://aida-arcadedb-backups"
YOS_ENDPOINT="https://storage.yandexcloud.net"

FRIGG_API="http://localhost:2481/api/v1"
YGG_API="http://localhost:2480/api/v1"

BACKUP_LOCAL_DIR_FRIGG="/var/lib/docker/volumes/seer-studio_frigg_data/_data/backup"
BACKUP_LOCAL_DIR_YGG="/var/lib/docker/volumes/seer-studio_hound_databases/_data/backup"

# Load secrets
# shellcheck source=/dev/null
source "${ENV_FILE}"

FRIGG_PASS="${FRIGG_PASSWORD:-playwithdata}"
YGG_PASS="${ARCADEDB_ADMIN_PASSWORD:-playwithdata}"

DATE=$(date +%Y%m%d-%H%M%S)

echo "[backup] === ArcadeDB backup started at ${DATE} ==="

# ── Helper: trigger backup for one database ──────────────────────────────────
backup_db() {
  local api="$1" pass="$2" db="$3" label="$4"
  echo "[backup] triggering backup for ${label}/${db}..."
  local resp
  resp=$(curl -sf --max-time 30 -u "root:${pass}" \
    -X POST "${api}/server" \
    -H 'Content-Type: application/json' \
    -d "{\"command\":\"backup database ${db}\"}" 2>&1 || echo "error")
  echo "[backup]   response: ${resp}"
}

# ── Helper: list known per-tenant DBs from FRIGG ─────────────────────────────
list_tenant_aliases() {
  curl -sf --max-time 10 -u "root:${FRIGG_PASS}" \
    -X POST "${FRIGG_API}/query/frigg-tenants" \
    -H 'Content-Type: application/json' \
    -d '{"language":"sql","command":"SELECT tenantAlias FROM DaliTenantConfig WHERE status = '\''ACTIVE'\''"}' \
    2>/dev/null \
  | python3 -c 'import json,sys; [print(r["tenantAlias"]) for r in json.load(sys.stdin).get("result",[])]' \
  2>/dev/null || echo ""
}

# ── Step 1: FRIGG backups ────────────────────────────────────────────────────
echo "[backup] --- FRIGG databases ---"
# Core FRIGG databases
for db in frigg-sessions frigg-tenants frigg-users heimdall dali; do
  backup_db "${FRIGG_API}" "${FRIGG_PASS}" "${db}" "FRIGG"
done

# Per-tenant dali_* databases (including dali_default)
TENANTS=$(list_tenant_aliases)
for alias in ${TENANTS}; do
  backup_db "${FRIGG_API}" "${FRIGG_PASS}" "dali_${alias}" "FRIGG"
done

# ── Step 2: YGG backups ──────────────────────────────────────────────────────
echo "[backup] --- YGG databases ---"
for alias in ${TENANTS}; do
  backup_db "${YGG_API}" "${YGG_PASS}" "hound_${alias}"     "YGG"
  backup_db "${YGG_API}" "${YGG_PASS}" "hound_src_${alias}" "YGG"
done

# ── Step 3: wait for backup files to appear (ArcadeDB writes async) ──────────
echo "[backup] waiting for backup files..."
sleep 15
echo "[backup] backup files ready"

# ── Step 4: upload to Yandex Object Storage ──────────────────────────────────
echo "[backup] uploading FRIGG backups to ${BUCKET}/${DATE}/frigg/..."
if [ -d "${BACKUP_LOCAL_DIR_FRIGG}" ]; then
  aws s3 sync "${BACKUP_LOCAL_DIR_FRIGG}" \
    "${BUCKET}/${DATE}/frigg/" \
    --endpoint-url "${YOS_ENDPOINT}" \
    --storage-class STANDARD
else
  echo "[backup] WARN: FRIGG backup dir not found at ${BACKUP_LOCAL_DIR_FRIGG}"
fi

echo "[backup] uploading YGG backups to ${BUCKET}/${DATE}/ygg/..."
if [ -d "${BACKUP_LOCAL_DIR_YGG}" ]; then
  aws s3 sync "${BACKUP_LOCAL_DIR_YGG}" \
    "${BUCKET}/${DATE}/ygg/" \
    --endpoint-url "${YOS_ENDPOINT}" \
    --storage-class STANDARD
else
  echo "[backup] WARN: YGG backup dir not found at ${BACKUP_LOCAL_DIR_YGG}"
fi

echo "[backup] upload complete"

# ── Step 5: prune old backups (retain last 30 backup sets) ───────────────────
echo "[backup] pruning old backup sets (keeping last 30)..."

PREFIXES=$(aws s3 ls "${BUCKET}/" \
  --endpoint-url "${YOS_ENDPOINT}" \
  | awk '{print $2}' \
  | sort)

TOTAL=$(echo "${PREFIXES}" | grep -c . || true)
TO_DELETE=$((TOTAL - 30))

if [ "${TO_DELETE}" -gt 0 ]; then
  OLD=$(echo "${PREFIXES}" | head -n "${TO_DELETE}")
  while IFS= read -r prefix; do
    [ -z "${prefix}" ] && continue
    echo "[backup] removing old backup: ${prefix}"
    aws s3 rm "${BUCKET}/${prefix}" \
      --recursive \
      --endpoint-url "${YOS_ENDPOINT}"
  done <<< "${OLD}"
else
  echo "[backup] no pruning needed (${TOTAL} backup sets retained)"
fi

echo "[backup] === done at $(date +%Y%m%d-%H%M%S) ==="
