#!/usr/bin/env bash
# lockbox-sync.sh — pull secrets from Yandex Lockbox → /opt/seer-studio/.env.prod
#
# Usage:
#   ./infra/scripts/lockbox-sync.sh
#
# Requires:
#   - yc CLI authenticated (service account key via YC_SERVICE_ACCOUNT_KEY_FILE
#     or IAM token via instance metadata)
#   - Lockbox secret named "aida-prod" in the configured folder
#
# Expected Lockbox keys:
#   ARCADEDB_ADMIN_PASSWORD
#   ARCADEDB_PASS
#   KEYCLOAK_CLIENT_SECRET
#   COOKIE_SECRET
#   ANTHROPIC_API_KEY
#
set -euo pipefail

DEST="/opt/seer-studio/.env.prod"
SECRET_NAME="aida-prod"

echo "[lockbox-sync] fetching secret '${SECRET_NAME}'..."

yc lockbox payload get \
  --name "${SECRET_NAME}" \
  --format json \
  | jq -r '.entries[] | "\(.key)=\(.value)"' \
  > "${DEST}"

chmod 600 "${DEST}"

echo "[lockbox-sync] secrets written to ${DEST}"
echo "[lockbox-sync] keys synced: $(wc -l < "${DEST}")"
