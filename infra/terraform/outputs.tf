output "vm_external_ip" {
  description = "External IP address of the AIDA VM — use as DEPLOY_HOST GitHub Secret"
  value       = yandex_compute_instance.aida_vm.network_interface[0].nat_ip_address
}

output "vm_internal_ip" {
  description = "Internal IP of the AIDA VM within VPC"
  value       = yandex_compute_instance.aida_vm.network_interface[0].ip_address
}

output "registry_id" {
  description = "Yandex Container Registry ID — use as YC_REGISTRY_ID GitHub Secret"
  value       = yandex_container_registry.aida_ycr.id
}

output "registry_endpoint" {
  description = "YCR endpoint for docker push/pull"
  value       = "cr.yandex/${yandex_container_registry.aida_ycr.id}"
}

output "backup_bucket" {
  description = "Object Storage bucket name for ArcadeDB backups"
  value       = yandex_storage_bucket.aida_backups.bucket
}

output "lockbox_secret_id" {
  description = "Lockbox secret ID — reference in lockbox-sync.sh"
  value       = yandex_lockbox_secret.aida_secrets.id
}

output "service_account_id" {
  description = "CI service account ID"
  value       = yandex_iam_service_account.aida_ci.id
}
