output "vm_external_ip" {
  description = "External IP — paste as DEPLOY_HOST GitHub Secret"
  value       = yandex_compute_instance.aida_vm.network_interface[0].nat_ip_address
}

output "vm_internal_ip" {
  description = "Internal IP of the AIDA VM within VPC"
  value       = yandex_compute_instance.aida_vm.network_interface[0].ip_address
}

output "registry_id" {
  description = "YCR Registry ID — paste as YC_REGISTRY_ID GitHub Secret"
  value       = yandex_container_registry.aida_ycr.id
}

output "registry_endpoint" {
  description = "YCR endpoint for docker push/pull"
  value       = "cr.yandex/${yandex_container_registry.aida_ycr.id}"
}

# ── GitHub Actions secrets (sensitive) ───────────────────────────────────────
output "ci_sa_key_json" {
  description = "JSON RSA key for GitHub Actions — paste as YC_SA_KEY GitHub Secret"
  value = jsonencode({
    id                 = yandex_iam_service_account_key.aida_ci_json_key.id
    service_account_id = yandex_iam_service_account.aida_ci.id
    created_at         = yandex_iam_service_account_key.aida_ci_json_key.created_at
    key_algorithm      = yandex_iam_service_account_key.aida_ci_json_key.key_algorithm
    public_key         = yandex_iam_service_account_key.aida_ci_json_key.public_key
    private_key        = yandex_iam_service_account_key.aida_ci_json_key.private_key
  })
  sensitive = true
}

# ── Lockbox payload values (sensitive) ───────────────────────────────────────
output "s3_access_key" {
  description = "S3 HMAC access key — add to Lockbox as AWS_ACCESS_KEY_ID"
  value       = yandex_iam_service_account_static_access_key.aida_vm_s3_key.access_key
  sensitive   = true
}

output "s3_secret_key" {
  description = "S3 HMAC secret key — add to Lockbox as AWS_SECRET_ACCESS_KEY"
  value       = yandex_iam_service_account_static_access_key.aida_vm_s3_key.secret_key
  sensitive   = true
}

output "lockbox_secret_id" {
  description = "Lockbox secret ID — used by set-github-secrets.sh to print the fill command"
  value       = yandex_lockbox_secret.aida_secrets.id
}

output "backup_bucket" {
  description = "Object Storage bucket for ArcadeDB backups"
  value       = yandex_storage_bucket.aida_backups.bucket
}

output "service_account_ci_id" {
  description = "CI service account ID (aida-ci)"
  value       = yandex_iam_service_account.aida_ci.id
}

output "service_account_vm_id" {
  description = "VM service account ID (aida-vm) — attached to compute instance"
  value       = yandex_iam_service_account.aida_vm.id
}
