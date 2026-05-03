#!/usr/bin/env bash
###############################################################################
# set-github-secrets.sh
#
# Run ONCE after `terraform apply` from infra/terraform/ directory.
# Reads Terraform outputs and sets GitHub Actions secrets automatically.
#
# Requirements:
#   - terraform applied in infra/terraform/
#   - gh CLI installed and authenticated (gh auth login)
#   - Your SSH private key ready (the key whose PUBLIC key is in terraform.tfvars)
#
# Usage:
#   cd infra/terraform
#   ../scripts/set-github-secrets.sh [path/to/ssh_private_key]
#
#   Default SSH key: ~/.ssh/id_rsa
###############################################################################

set -euo pipefail

REPO="NooriUta/AIDA"
SSH_KEY_PATH="${1:-$HOME/.ssh/id_rsa}"

# Must be run from infra/terraform/
if [[ ! -f "main.tf" ]]; then
  echo "ERROR: run this script from infra/terraform/ directory"
  exit 1
fi

echo "========================================"
echo " AIDA — GitHub Secrets Setup"
echo " Repo: ${REPO}"
echo "========================================"
echo ""

# ── Read Terraform outputs ────────────────────────────────────────────────────
echo "[1/4] Reading Terraform outputs..."

VM_IP=$(terraform output -raw vm_external_ip)
REGISTRY_ID=$(terraform output -raw registry_id)
CI_SA_KEY=$(terraform output -raw ci_sa_key_json)
S3_ACCESS_KEY=$(terraform output -raw s3_access_key)
S3_SECRET_KEY=$(terraform output -raw s3_secret_key)
LOCKBOX_ID=$(terraform output -raw lockbox_secret_id)
BUCKET=$(terraform output -raw backup_bucket)

echo "  VM IP:       ${VM_IP}"
echo "  Registry ID: ${REGISTRY_ID}"
echo "  Lockbox ID:  ${LOCKBOX_ID}"
echo "  S3 bucket:   ${BUCKET}"
echo ""

# ── Set GitHub Secrets ────────────────────────────────────────────────────────
echo "[2/4] Setting GitHub Secrets..."

gh secret set DEPLOY_HOST    --body "${VM_IP}"         --repo "${REPO}"
echo "  ✓ DEPLOY_HOST"

gh secret set YC_REGISTRY_ID --body "${REGISTRY_ID}"   --repo "${REPO}"
echo "  ✓ YC_REGISTRY_ID"

gh secret set YC_SA_KEY      --body "${CI_SA_KEY}"      --repo "${REPO}"
echo "  ✓ YC_SA_KEY"

gh secret set DEPLOY_USER    --body "ubuntu"             --repo "${REPO}"
echo "  ✓ DEPLOY_USER"

if [[ -f "${SSH_KEY_PATH}" ]]; then
  gh secret set DEPLOY_KEY --body "$(cat "${SSH_KEY_PATH}")" --repo "${REPO}"
  echo "  ✓ DEPLOY_KEY (from ${SSH_KEY_PATH})"
else
  echo "  ⚠ DEPLOY_KEY — key not found at ${SSH_KEY_PATH}"
  echo "    Set manually: gh secret set DEPLOY_KEY --repo ${REPO}"
fi

echo ""

# ── Print Lockbox fill command ─────────────────────────────────────────────────
echo "[3/4] Lockbox secret values — run this after filling in your passwords:"
echo ""
echo "  yc lockbox secret add-version \\"
echo "    --id ${LOCKBOX_ID} \\"
echo "    --payload '["
echo "      {\"key\":\"ARCADEDB_ADMIN_PASSWORD\",  \"text_value\":\"FILL_ME\"},"
echo "      {\"key\":\"ARCADEDB_PASS\",             \"text_value\":\"FILL_ME\"},"
echo "      {\"key\":\"FRIGG_PASSWORD\",            \"text_value\":\"FILL_ME\"},"
echo "      {\"key\":\"KEYCLOAK_CLIENT_SECRET\",    \"text_value\":\"FILL_ME\"},"
echo "      {\"key\":\"COOKIE_SECRET\",             \"text_value\":\"FILL_ME\"},"
echo "      {\"key\":\"ANTHROPIC_API_KEY\",         \"text_value\":\"FILL_ME\"},"
echo "      {\"key\":\"DEEPSEEK_API_KEY\",          \"text_value\":\"FILL_ME\"},"
echo "      {\"key\":\"MIMIR_KEY_ENCRYPTION_KEY\",  \"text_value\":\"FILL_ME\"},"
echo "      {\"key\":\"MIMIR_DEFAULT_MODEL\",       \"text_value\":\"deepseek\"},"
echo "      {\"key\":\"MIMIR_DEMO_MODE\",           \"text_value\":\"false\"},"
echo "      {\"key\":\"AWS_ACCESS_KEY_ID\",         \"text_value\":\"${S3_ACCESS_KEY}\"},"
echo "      {\"key\":\"AWS_SECRET_ACCESS_KEY\",     \"text_value\":\"${S3_SECRET_KEY}\"}"
echo "    ]'"
echo ""
echo "  ARCADEDB_ADMIN_PASSWORD = ARCADEDB_PASS  (YGG ArcadeDB root password)"
echo "  FRIGG_PASSWORD          (FRIGG ArcadeDB root password, also used by Dali)"
echo "  KEYCLOAK_CLIENT_SECRET  (Keycloak client secret for Chur BFF)"
echo "  COOKIE_SECRET           (≥32 random chars, e.g.: openssl rand -hex 32)"
echo "  ANTHROPIC_API_KEY       (sk-ant-...) [optional, MIMIR Anthropic Tier 1]"
echo "  DEEPSEEK_API_KEY        (sk-...)     [REQUIRED for MIMIR /api/ask — DeepSeek default]"
echo "  MIMIR_KEY_ENCRYPTION_KEY (32B b64)   [REQUIRED for TIER2 BYOK — openssl rand -base64 32]"
echo "  MIMIR_DEFAULT_MODEL     (deepseek)   [optional override: anthropic | ollama]"
echo "  MIMIR_DEMO_MODE         (false)      [true → answers from cache fixtures, no LLM]"
echo "  AWS_ACCESS_KEY_ID       (pre-filled from Terraform — S3 backup key)"
echo "  AWS_SECRET_ACCESS_KEY   (pre-filled from Terraform — S3 backup key)"
echo ""

# ── Next steps ────────────────────────────────────────────────────────────────
echo "[4/4] Next steps:"
echo ""
echo "  1. Fill in the Lockbox command above and run it"
echo "  2. SSH into the VM and verify cloud-init finished:"
echo "       ssh ubuntu@${VM_IP} 'sudo cloud-init status --wait'"
echo "  3. Merge the PR:  sprint/dali-file-upload-apr16 → master"
echo "     → CD triggers automatically: build → mirror YCR → deploy"
echo ""
echo "========================================"
echo " Done. GitHub secrets are set."
echo "========================================"
