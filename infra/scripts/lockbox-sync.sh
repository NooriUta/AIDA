#!/usr/bin/env bash
# lockbox-sync.sh — pull secrets from Yandex Lockbox → /opt/seer-studio/.env.prod
#
# Auth: uses VM service account via instance metadata (no yc CLI config needed).
# The VM SA must have lockbox.payloadViewer role (granted by Terraform).
#
set -euo pipefail

DEST="/opt/seer-studio/.env.prod"
SECRET_ID="${LOCKBOX_SECRET_ID:-e6qk6ms2ptpfrrpcobif}"

echo "[lockbox-sync] fetching secret ${SECRET_ID}..."

# Get IAM token from instance metadata (same approach as docker login to YCR)
IAM_TOKEN=$(curl -sf -H "Metadata-Flavor: Google" \
  http://169.254.169.254/computeMetadata/v1/instance/service-accounts/default/token \
  | jq -r .access_token)

# Fetch Lockbox payload and write .env.prod
curl -sf \
  -H "Authorization: Bearer ${IAM_TOKEN}" \
  "https://payload.lockbox.api.cloud.yandex.net/lockbox/v1/secrets/${SECRET_ID}/payload" \
  | jq -r '.entries[] | "\(.key)=\(.textValue)"' \
  > "${DEST}"

chmod 600 "${DEST}"

echo "[lockbox-sync] secrets written to ${DEST}"
echo "[lockbox-sync] keys synced: $(wc -l < "${DEST}")"
