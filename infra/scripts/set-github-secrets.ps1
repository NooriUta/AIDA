###############################################################################
# set-github-secrets.ps1
#
# Run ONCE after `terraform apply` from infra\terraform\ directory.
# Reads Terraform outputs and sets GitHub Actions secrets automatically.
#
# Requirements:
#   - terraform applied in infra\terraform\
#   - gh CLI installed and authenticated (gh auth login)
#   - SSH private key whose public key was used in terraform.tfvars
#
# Usage (from infra\terraform\):
#   ..\scripts\set-github-secrets.ps1
#   ..\scripts\set-github-secrets.ps1 -SshKeyPath "C:\Users\you\.ssh\id_rsa"
###############################################################################

param(
    [string]$SshKeyPath = "$env:USERPROFILE\.ssh\id_rsa"
)

$ErrorActionPreference = "Stop"

$REPO = "NooriUta/AIDA"

# Must be run from infra\terraform\
if (-not (Test-Path "main.tf")) {
    Write-Error "ERROR: run this script from infra\terraform\ directory"
    exit 1
}

Write-Host "========================================"
Write-Host " AIDA - GitHub Secrets Setup"
Write-Host " Repo: $REPO"
Write-Host "========================================"
Write-Host ""

# ── Read Terraform outputs ────────────────────────────────────────────────────
Write-Host "[1/4] Reading Terraform outputs..."

$VM_IP        = terraform output -raw vm_external_ip
$REGISTRY_ID  = terraform output -raw registry_id
$CI_SA_KEY    = terraform output -raw ci_sa_key_json
$S3_ACCESS    = terraform output -raw s3_access_key
$S3_SECRET    = terraform output -raw s3_secret_key
$LOCKBOX_ID   = terraform output -raw lockbox_secret_id
$BUCKET       = terraform output -raw backup_bucket

Write-Host "  VM IP:       $VM_IP"
Write-Host "  Registry ID: $REGISTRY_ID"
Write-Host "  Lockbox ID:  $LOCKBOX_ID"
Write-Host "  S3 bucket:   $BUCKET"
Write-Host ""

# ── Set GitHub Secrets ────────────────────────────────────────────────────────
Write-Host "[2/4] Setting GitHub Secrets..."

$VM_IP       | gh secret set DEPLOY_HOST    --repo $REPO
Write-Host "  v DEPLOY_HOST"

$REGISTRY_ID | gh secret set YC_REGISTRY_ID --repo $REPO
Write-Host "  v YC_REGISTRY_ID"

$CI_SA_KEY   | gh secret set YC_SA_KEY      --repo $REPO
Write-Host "  v YC_SA_KEY"

"ubuntu"     | gh secret set DEPLOY_USER    --repo $REPO
Write-Host "  v DEPLOY_USER"

if (Test-Path $SshKeyPath) {
    Get-Content $SshKeyPath -Raw | gh secret set DEPLOY_KEY --repo $REPO
    Write-Host "  v DEPLOY_KEY (from $SshKeyPath)"
} else {
    Write-Host "  ! DEPLOY_KEY - key not found at $SshKeyPath"
    Write-Host "    Set manually: Get-Content <key_path> -Raw | gh secret set DEPLOY_KEY --repo $REPO"
}

Write-Host ""

# ── Print Lockbox fill command ────────────────────────────────────────────────
Write-Host "[3/4] Lockbox secret values - fill in passwords and run in YC CLI:"
Write-Host ""
Write-Host "yc lockbox secret add-version ``"
Write-Host "  --id $LOCKBOX_ID ``"
Write-Host "  --payload '[" -NoNewline
Write-Host ""
Write-Host "    {""key"":""ARCADEDB_ADMIN_PASSWORD"",  ""text_value"":""FILL_ME""},"
Write-Host "    {""key"":""ARCADEDB_PASS"",             ""text_value"":""FILL_ME""},"
Write-Host "    {""key"":""FRIGG_PASSWORD"",            ""text_value"":""FILL_ME""},"
Write-Host "    {""key"":""KEYCLOAK_CLIENT_SECRET"",    ""text_value"":""FILL_ME""},"
Write-Host "    {""key"":""COOKIE_SECRET"",             ""text_value"":""FILL_ME""},"
Write-Host "    {""key"":""ANTHROPIC_API_KEY"",         ""text_value"":""FILL_ME""},"
Write-Host "    {""key"":""DEEPSEEK_API_KEY"",          ""text_value"":""FILL_ME""},"
Write-Host "    {""key"":""MIMIR_KEY_ENCRYPTION_KEY"",  ""text_value"":""FILL_ME""},"
Write-Host "    {""key"":""MIMIR_DEFAULT_MODEL"",       ""text_value"":""deepseek""},"
Write-Host "    {""key"":""MIMIR_DEMO_MODE"",           ""text_value"":""false""},"
Write-Host "    {""key"":""AWS_ACCESS_KEY_ID"",         ""text_value"":""$S3_ACCESS""},"
Write-Host "    {""key"":""AWS_SECRET_ACCESS_KEY"",     ""text_value"":""$S3_SECRET""}"
Write-Host "  ]'"
Write-Host ""
Write-Host "  ARCADEDB_ADMIN_PASSWORD = ARCADEDB_PASS  (YGG ArcadeDB root password)"
Write-Host "  FRIGG_PASSWORD          (FRIGG ArcadeDB root password, also used by Dali)"
Write-Host "  KEYCLOAK_CLIENT_SECRET  (Keycloak client secret for Chur BFF)"
Write-Host "  COOKIE_SECRET           (>=32 random chars)"
Write-Host ""

# Generate a random COOKIE_SECRET suggestion
$cookieBytes = New-Object byte[] 32
[System.Security.Cryptography.RandomNumberGenerator]::Fill($cookieBytes)
$cookieSuggestion = [System.BitConverter]::ToString($cookieBytes).Replace("-","").ToLower()
Write-Host "  Suggested COOKIE_SECRET: $cookieSuggestion"
Write-Host ""
Write-Host "  ANTHROPIC_API_KEY       (sk-ant-...) [optional, MIMIR Anthropic Tier 1]"
Write-Host "  DEEPSEEK_API_KEY        (sk-...)     [REQUIRED for MIMIR /api/ask — DeepSeek default]"
Write-Host "  MIMIR_KEY_ENCRYPTION_KEY (32B b64)   [REQUIRED for TIER2 BYOK — openssl rand -base64 32]"
Write-Host "  MIMIR_DEFAULT_MODEL     (deepseek)   [optional override: anthropic | ollama]"
Write-Host "  MIMIR_DEMO_MODE         (false)      [true → answers from cache fixtures, no LLM]"
Write-Host "  AWS_ACCESS_KEY_ID       (pre-filled above from Terraform)"
Write-Host "  AWS_SECRET_ACCESS_KEY   (pre-filled above from Terraform)"
Write-Host ""

# ── Next steps ────────────────────────────────────────────────────────────────
Write-Host "[4/4] Next steps:"
Write-Host ""
Write-Host "  1. Fill in FILL_ME values in the Lockbox command above and run it"
Write-Host "  2. Verify VM cloud-init finished:"
Write-Host "       ssh ubuntu@$VM_IP 'sudo cloud-init status --wait'"
Write-Host "  3. Merge PR: sprint/dali-file-upload-apr16 -> master"
Write-Host "     -> CD triggers automatically: build -> mirror YCR -> deploy"
Write-Host ""
Write-Host "========================================"
Write-Host " Done. GitHub secrets are set."
Write-Host "========================================"
