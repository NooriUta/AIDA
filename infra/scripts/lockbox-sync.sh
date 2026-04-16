#!/usr/bin/env bash
# lockbox-sync.sh — pull secrets from Yandex Lockbox → /opt/seer-studio/.env.prod
#
# Auth: uses VM service account via instance metadata (no credentials needed on VM).
# The VM must have an SA with lockbox.payloadViewer role attached (done by Terraform).
#
# LOCKBOX_SECRET_ID is baked in by cloud-init from Terraform output.
# Fallback: looks up by name "aida-prod" if ID not set.
#
# Lockbox keys synced to .env.prod:
#   ARCADEDB_ADMIN_PASSWORD, ARCADEDB_PASS, FRIGG_PASSWORD,
#   KEYCLOAK_CLIENT_SECRET, COOKIE_SECRET, ANTHROPIC_API_KEY,
#   AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY
#
set -euo pipefail

DEST="/opt/seer-studio/.env.prod"

if [[ -n "${LOCKBOX_SECRET_ID:-}" ]]; then
  LOOKUP="--id ${LOCKBOX_SECRET_ID}"
else
  LOOKUP="--name aida-prod"
fi

echo "[lockbox-sync] fetching secret (${LOOKUP})..."

yc lockbox payload get \
  ${LOOKUP} \
  --format json \
  | jq -r '.entries[] | "\(.key)=\(.textValue)"' \
  > "${DEST}"

chmod 600 "${DEST}"

echo "[lockbox-sync] secrets written to ${DEST}"
echo "[lockbox-sync] keys synced: $(wc -l < "${DEST}")"
