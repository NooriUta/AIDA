variable "yc_token" {
  description = "Yandex Cloud OAuth token or service account key"
  type        = string
  sensitive   = true
}

variable "yc_cloud_id" {
  description = "Yandex Cloud ID"
  type        = string
}

variable "yc_folder_id" {
  description = "Yandex Cloud Folder ID"
  type        = string
}

variable "yc_zone" {
  description = "Availability zone"
  type        = string
  default     = "ru-central1-a"
}

variable "domain" {
  description = "Public domain name for the application (e.g. aida.example.com)"
  type        = string
}

variable "ssh_public_key_path" {
  description = "Path to SSH public key for VM access"
  type        = string
  default     = "~/.ssh/id_rsa.pub"
}

variable "vm_cores" {
  description = "VM vCPU count"
  type        = number
  default     = 4
}

variable "vm_memory_gb" {
  description = "VM RAM in GB"
  type        = number
  default     = 8
}

variable "vm_disk_gb" {
  description = "VM boot disk size in GB"
  type        = number
  default     = 50
}
