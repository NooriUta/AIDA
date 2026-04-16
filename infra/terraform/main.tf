###############################################################################
# AIDA — Yandex Cloud Infrastructure
# Terraform >= 1.5, provider yandex >= 0.100
#
# Usage:
#   cp terraform.tfvars.example terraform.tfvars   # fill in values
#   terraform init
#   terraform plan
#   terraform apply
#   ../scripts/set-github-secrets.sh               # sets GH secrets from outputs
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
  name               = "aida-prod"
  platform_id        = "standard-v3"
  zone               = var.yc_zone
  service_account_id = yandex_iam_service_account.aida_vm.id   # VM SA: Lockbox + S3

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
      ssh_public_key    = file(var.ssh_public_key_path)
      domain            = var.domain
      lockbox_secret_id = yandex_lockbox_secret.aida_secrets.id
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

# ── IAM: Service Account for CI (GitHub Actions) ─────────────────────────────
resource "yandex_iam_service_account" "aida_ci" {
  name        = "aida-ci"
  description = "GitHub Actions CD — push to YCR, pull on VM via JSON key"
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

# JSON key — used as YC_SA_KEY GitHub secret; supports `docker login cr.yandex -u json_key`
resource "yandex_iam_service_account_key" "aida_ci_json_key" {
  service_account_id = yandex_iam_service_account.aida_ci.id
  description        = "JSON RSA key for GitHub Actions — docker login to YCR"
  key_algorithm      = "RSA_4096"
}

# ── IAM: Service Account for VM ───────────────────────────────────────────────
# Attached to the compute instance — allows yc CLI (via metadata service) to
# access Lockbox and Object Storage without hardcoded credentials on the VM.
resource "yandex_iam_service_account" "aida_vm" {
  name        = "aida-vm"
  description = "VM runtime SA — Lockbox reader + Object Storage for backups"
}

resource "yandex_resourcemanager_folder_iam_member" "aida_vm_lockbox" {
  folder_id = var.yc_folder_id
  role      = "lockbox.payloadViewer"
  member    = "serviceAccount:${yandex_iam_service_account.aida_vm.id}"
}

resource "yandex_resourcemanager_folder_iam_member" "aida_vm_storage" {
  folder_id = var.yc_folder_id
  role      = "storage.editor"
  member    = "serviceAccount:${yandex_iam_service_account.aida_vm.id}"
}

# HMAC key for S3-compatible Object Storage (used by backup-arcadedb.sh aws CLI)
# Values go into Lockbox so the VM reads them via .env.prod
resource "yandex_iam_service_account_static_access_key" "aida_vm_s3_key" {
  service_account_id = yandex_iam_service_account.aida_vm.id
  description        = "HMAC key for ArcadeDB backup → Object Storage"
}

# ── Object Storage (ArcadeDB backups) ────────────────────────────────────────
resource "yandex_storage_bucket" "aida_backups" {
  bucket     = "aida-arcadedb-backups"
  folder_id  = var.yc_folder_id
  access_key = yandex_iam_service_account_static_access_key.aida_vm_s3_key.access_key
  secret_key = yandex_iam_service_account_static_access_key.aida_vm_s3_key.secret_key

  versioning {
    enabled = true
  }

  lifecycle_rule {
    enabled = true
    expiration {
      days = 30
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
# After `terraform apply`, run set-github-secrets.sh which prints the lockbox command.
#
# Required Lockbox keys (all go into .env.prod on the VM):
#   ARCADEDB_ADMIN_PASSWORD   — YGG ArcadeDB root password
#   ARCADEDB_PASS             — same value (used by shuttle/chur clients)
#   FRIGG_PASSWORD            — FRIGG ArcadeDB root password (also used by Dali)
#   KEYCLOAK_CLIENT_SECRET    — Keycloak client secret for Chur BFF
#   COOKIE_SECRET             — session cookie signing secret (≥32 random chars)
#   ANTHROPIC_API_KEY         — Claude API key (sk-ant-...)
#   AWS_ACCESS_KEY_ID         — S3 HMAC key (from terraform output s3_access_key)
#   AWS_SECRET_ACCESS_KEY     — S3 HMAC secret (from terraform output s3_secret_key)

# ── DNS ───────────────────────────────────────────────────────────────────────
resource "yandex_dns_zone" "aida" {
  name   = "aida-zone"
  zone   = "${var.domain}."
  public = true
}

resource "yandex_dns_recordset" "aida_a" {
  zone_id = yandex_dns_zone.aida.id
  name    = "@"
  type    = "A"
  ttl     = 300
  data    = [yandex_compute_instance.aida_vm.network_interface[0].nat_ip_address]
}
