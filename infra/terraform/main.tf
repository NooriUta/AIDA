###############################################################################
# AIDA — Yandex Cloud Infrastructure
# Terraform >= 1.5, provider yandex >= 0.100
#
# Usage:
#   cp terraform.tfvars.example terraform.tfvars   # fill in values
#   terraform init
#   terraform plan
#   terraform apply
###############################################################################

terraform {
  required_version = ">= 1.5"
  required_providers {
    yandex = {
      source  = "yandex-cloud/yandex"
      version = ">= 0.100"
    }
  }
}

provider "yandex" {
  token     = var.yc_token
  cloud_id  = var.yc_cloud_id
  folder_id = var.yc_folder_id
  zone      = var.yc_zone
}

# ── Network ──────────────────────────────────────────────────────────────────
resource "yandex_vpc_network" "aida" {
  name = "aida-network"
}

resource "yandex_vpc_subnet" "aida" {
  name           = "aida-subnet"
  zone           = var.yc_zone
  network_id     = yandex_vpc_network.aida.id
  v4_cidr_blocks = ["10.0.1.0/24"]
}

# ── Security Group ────────────────────────────────────────────────────────────
resource "yandex_vpc_security_group" "aida" {
  name       = "aida-sg"
  network_id = yandex_vpc_network.aida.id

  ingress {
    protocol       = "TCP"
    port           = 22
    v4_cidr_blocks = ["0.0.0.0/0"]
    description    = "SSH"
  }

  ingress {
    protocol       = "TCP"
    port           = 80
    v4_cidr_blocks = ["0.0.0.0/0"]
    description    = "HTTP (redirect to HTTPS)"
  }

  ingress {
    protocol       = "TCP"
    port           = 443
    v4_cidr_blocks = ["0.0.0.0/0"]
    description    = "HTTPS"
  }

  egress {
    protocol       = "ANY"
    v4_cidr_blocks = ["0.0.0.0/0"]
    description    = "All outbound"
  }
}

# ── Compute VM ────────────────────────────────────────────────────────────────
data "yandex_compute_image" "ubuntu" {
  family = "ubuntu-2204-lts"
}

resource "yandex_compute_instance" "aida_vm" {
  name        = "aida-prod"
  platform_id = "standard-v3"
  zone        = var.yc_zone

  resources {
    cores  = var.vm_cores
    memory = var.vm_memory_gb
  }

  boot_disk {
    initialize_params {
      image_id = data.yandex_compute_image.ubuntu.id
      size     = var.vm_disk_gb
      type     = "network-ssd"
    }
  }

  network_interface {
    subnet_id          = yandex_vpc_subnet.aida.id
    security_group_ids = [yandex_vpc_security_group.aida.id]
    nat                = true   # external IP
  }

  metadata = {
    user-data = templatefile("${path.module}/../cloud-init.yml", {
      ssh_public_key = file(var.ssh_public_key_path)
      domain         = var.domain
    })
  }

  scheduling_policy {
    preemptible = false
  }
}

# ── Container Registry (YCR mirror) ──────────────────────────────────────────
resource "yandex_container_registry" "aida_ycr" {
  name      = "aida-registry"
  folder_id = var.yc_folder_id
}

# ── IAM: Service Account for CI ──────────────────────────────────────────────
resource "yandex_iam_service_account" "aida_ci" {
  name        = "aida-ci"
  description = "Used by GitHub Actions CD pipeline to push to YCR and pull on VM"
}

resource "yandex_container_registry_iam_binding" "aida_ci_pusher" {
  registry_id = yandex_container_registry.aida_ycr.id
  role        = "container-registry.images.pusher"
  members     = ["serviceAccount:${yandex_iam_service_account.aida_ci.id}"]
}

resource "yandex_container_registry_iam_binding" "aida_ci_puller" {
  registry_id = yandex_container_registry.aida_ycr.id
  role        = "container-registry.images.puller"
  members     = ["serviceAccount:${yandex_iam_service_account.aida_ci.id}"]
}

resource "yandex_iam_service_account_static_access_key" "aida_ci_key" {
  service_account_id = yandex_iam_service_account.aida_ci.id
  description        = "Key for GitHub Actions (YC_SA_KEY secret)"
}

# ── Object Storage (ArcadeDB backups) ────────────────────────────────────────
resource "yandex_storage_bucket" "aida_backups" {
  bucket    = "aida-arcadedb-backups"
  folder_id = var.yc_folder_id

  versioning {
    enabled = true
  }

  lifecycle_rule {
    enabled = true
    expiration {
      days = 30   # retain 30 days of backups
    }
  }
}

# ── Lockbox (secrets management) ─────────────────────────────────────────────
resource "yandex_lockbox_secret" "aida_secrets" {
  name        = "aida-prod"
  description = "Production secrets for AIDA stack"
  folder_id   = var.yc_folder_id
}

# NOTE: Secret values are NOT managed by Terraform to avoid storing them in state.
# Add values manually via YC Console or:
#   yc lockbox secret add-version \
#     --id <secret-id> \
#     --payload '[{"key":"ARCADEDB_ADMIN_PASSWORD","text_value":"..."},...]'
#
# Required keys:
#   ARCADEDB_ADMIN_PASSWORD
#   ARCADEDB_PASS
#   KEYCLOAK_CLIENT_SECRET
#   COOKIE_SECRET
#   ANTHROPIC_API_KEY

# ── DNS ───────────────────────────────────────────────────────────────────────
resource "yandex_dns_zone" "aida" {
  name        = "aida-zone"
  zone        = "${var.domain}."
  public      = true
}

resource "yandex_dns_recordset" "aida_a" {
  zone_id = yandex_dns_zone.aida.id
  name    = "@"
  type    = "A"
  ttl     = 300
  data    = [yandex_compute_instance.aida_vm.network_interface[0].nat_ip_address]
}
