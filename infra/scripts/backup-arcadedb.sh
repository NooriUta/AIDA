#!/usr/bin/env bash
# backup-arcadedb.sh — trigger ArcadeDB backup and upload to Yandex Object Storage
#
# Usage:
#   ./infra/scripts/backup-arcadedb.sh
#
# Scheduled via cron:
#   0 3 * * * /opt/seer-studio/infra/scripts/backup-arcadedb.sh >> /var/log/aida-backup.log 2>&1
#
# Requires:
#   - aws CLI (S3-compatible, used with Yandex Object Storage endpoint)
#   - ARCADEDB_ADMIN_PASSWORD available in .env.prod
#   - Object Storage bucket "aida-arcadedb-backups" created via Terraform
#
set -euo pipefail

DEST_DIR="/opt/seer-studio"
ENV_FILE="${DEST_DIR}/.env.prod"
BUCKET="s3://aida-arcadedb-backups"
YOS_ENDPOINT="https://storage.yandexcloud.net"
ARCADEDB_API="http://localhost:2480/api/v1/server"
BACKUP_LOCAL_DIR="/var/lib/arcadedb/backup"

# Load secrets
# shellcheck source=/dev/null
source "${ENV_FILE}"

DATE=$(date +%Y%m%d-%H%M%S)

echo "[backup] === ArcadeDB backup started at ${DATE} ==="

# ── Step 1: trigger ArcadeDB server-side backup ──────────────────────────────
echo "[backup] triggering ArcadeDB backup API..."
RESPONSE=$(curl -su "root:${ARCADEDB_ADMIN_PASSWORD}" \
  -X POST "${ARCADEDB_API}" \
  -H 'Content-Type: application/json' \
  -d '{"command":"backup database hound"}' \
  --fail --silent --show-error)

echo "[backup] API response: ${RESPONSE}"

# ── Step 2: wait for backup file to appear (ArcadeDB async) ─────────────────
echo "[backup] waiting for backup file..."
for i in $(seq 1 12); do
  BACKUP_FILES=$(find "${BACKUP_LOCAL_DIR}" -name "*.zip" -newer "${ENV_FILE}" 2>/dev/null | wc -l)
  if [ "${BACKUP_FILES}" -gt 0 ]; then
    echo "[backup] backup file found"
    break
  fi
  echo "[backup] retry ${i}/12..."
  sleep 5
done

# ── Step 3: upload to Yandex Object Storage ──────────────────────────────────
echo "[backup] uploading to ${BUCKET}/${DATE}/..."
aws s3 sync "${BACKUP_LOCAL_DIR}" \
  "${BUCKET}/${DATE}/" \
  --endpoint-url "${YOS_ENDPOINT}" \
  --storage-class STANDARD

echo "[backup] upload complete"

# ── Step 4: prune old backups (retain last 30 days) ──────────────────────────
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
