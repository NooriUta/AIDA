# AIDA — Yandex Cloud Deployment Guide

**Документ:** `YC_DEPLOYMENT`
**Версия:** 1.2
**Дата:** 18.04.2026
**Статус:** ✅ ACTIVE — production deployment runbook

> Это пошаговый runbook для деплоя SEER Studio в Yandex Cloud.
> Читай полностью перед первым деплоем. Одноразовые шаги помечены **(first-time only)**.

---

## Архитектура деплоя

```
Internet
    │ HTTPS 443
    ▼
┌─────────────────────────────────────────┐
│  YC VM  (Ubuntu 22.04, 4 vCPU / 16 GB) │
│                                         │
│  Nginx (reverse proxy + TLS Certbot)    │
│    /           → verdandi:15173         │
│    /auth       → chur:13000             │
│    /graphql    → chur:13000             │
│    /api/       → chur:13000             │
│    /kc/        → keycloak:18180         │
│    /heimdall   → heimdall-frontend:25174│
│                                         │
│  Docker Compose (prod + yc overrides)   │
│  ├─ keycloak      (auth)                │
│  ├─ shuttle       (GraphQL API)         │
│  ├─ chur          (BFF)                 │
│  ├─ dali          (parser worker)       │
│  ├─ heimdall-backend  (observability)   │
│  ├─ heimdall-frontend (admin UI)        │
│  ├─ verdandi      (main UI)             │
│  ├─ shell         (MF host)             │
│  ├─ frigg         (ArcadeDB state)      │
│  └─ houndarcade   (ArcadeDB lineage)    │
└─────────────────────────────────────────┘
    │                       │
    ▼                       ▼
YC Container Registry    YC Object Storage
(cr.yandex/...)          (aida-arcadedb-backups)
    ▲
    │  mirror
GitHub Actions (GHCR → YCR → SSH deploy)
```

**Secrets:** Yandex Lockbox → `lockbox-sync.sh` → `.env.prod` на VM

---

## Требования

| Инструмент | Версия | Где взять |
|---|---|---|
| `yc` CLI | latest | `curl -sL https://storage.yandexcloud.net/yandexcloud-yc/install.sh \| bash` |
| Terraform | ≥ 1.5 | `https://developer.hashicorp.com/terraform/install` |
| Docker (local) | ≥ 24 | для сборки образов локально (если нужно) |
| SSH key pair | RSA 4096 или ED25519 | `ssh-keygen -t ed25519 -C "aida-deploy"` |

---

## Часть 1 — Первоначальная настройка (first-time only)

### 1.1 Аутентификация в YC

```bash
yc init
# Выбери cloud и folder
yc config list   # убедись что cloud_id и folder_id установлены
```

### 1.2 Terraform — инфраструктура

```bash
cd infra/terraform

cp terraform.tfvars.example terraform.tfvars
# Заполни terraform.tfvars:
#   yc_token       = "<OAuth-token из yc iam create-token>"
#   yc_cloud_id    = "<cloud ID из yc config list>"
#   yc_folder_id   = "<folder ID>"
#   domain         = "seer.yourdomain.com"
#   ssh_public_key_path = "~/.ssh/id_ed25519.pub"

terraform init
terraform plan    # проверь что создаётся
terraform apply   # подтверди: yes
```

**Terraform создаст:**
- VM `aida-prod` (4 vCPU / 16 GB / 50 GB SSD, Ubuntu 22.04)
- VPC + subnet + security group (22/80/443)
- Container Registry `aida-registry`
- Service account `aida-ci` с правами push/pull
- Object Storage bucket `aida-arcadedb-backups` (lifecycle 30 дней)
- Lockbox secret `aida-prod` (значения заполняются вручную — шаг 1.4)
- DNS A-запись `<domain> → VM external IP`

**Сохрани outputs:**
```bash
terraform output   # запиши vm_external_ip, registry_id, lockbox_secret_id
```

### 1.3 GitHub Secrets

В репозитории → **Settings → Secrets and variables → Actions**:

| Secret | Значение |
|---|---|
| `DEPLOY_HOST` | **статический** IP VM из YC console |
| `DEPLOY_USER` | `ubuntu` |
| `DEPLOY_KEY` | содержимое `~/.ssh/id_ed25519` (приватный ключ) |
| `YC_SA_KEY` | JSON ключ service account (см. ниже) |
| `YC_REGISTRY_ID` | `registry_id` из terraform output |

> ⚠️ **ВАЖНО — статический IP:** YC назначает динамический IP при создании VM.
> После ресайза или перезапуска VM IP может смениться.
> Зарезервируй статический IP заранее (YC Console → Virtual Private Cloud → IP-адреса)
> и привяжи к VM. Обнови `DEPLOY_HOST` если IP изменился — иначе CD pipeline сломается.

**Создать JSON ключ для service account:**
```bash
yc iam key create \
  --service-account-name aida-ci \
  --output sa-key.json
cat sa-key.json   # скопируй в DEPLOY_KEY
rm sa-key.json    # удали локально
```

### 1.4 Yandex Lockbox — заполнить секреты

```bash
LOCKBOX_ID=$(terraform output -raw lockbox_secret_id)

yc lockbox secret add-version --id $LOCKBOX_ID \
  --payload '[
    {"key": "ARCADEDB_ADMIN_PASSWORD", "text_value": "<STRONG_PASSWORD>"},
    {"key": "ARCADEDB_PASS",           "text_value": "<STRONG_PASSWORD>"},
    {"key": "KEYCLOAK_CLIENT_SECRET",  "text_value": "<GENERATED_SECRET>"},
    {"key": "COOKIE_SECRET",           "text_value": "<RANDOM_32_BYTES_HEX>"},
    {"key": "ANTHROPIC_API_KEY",       "text_value": "sk-ant-..."},
    {"key": "FRIGG_PASSWORD",          "text_value": "<STRONG_PASSWORD>"},
    {"key": "MINIO_ACCESS_KEY",        "text_value": "aida"},
    {"key": "MINIO_SECRET_KEY",        "text_value": "<STRONG_PASSWORD>"},
    {"key": "S3_ACCESS_KEY",           "text_value": "<YC_STATIC_KEY_ID>"},
    {"key": "S3_SECRET_KEY",           "text_value": "<YC_STATIC_KEY_SECRET>"}
  ]'
```

> **Генерация COOKIE_SECRET:**
> ```bash
> openssl rand -hex 32
> ```
> **Генерация KEYCLOAK_CLIENT_SECRET:**
> ```bash
> openssl rand -base64 32
> ```

### 1.5 Дождаться готовности VM

```bash
# VM поднимается ~3-5 минут, cloud-init работает ещё ~2 минуты
ssh ubuntu@<vm_external_ip> "cloud-init status --wait"
# Ожидаемый ответ: status: done
```

**Cloud-init устанавливает:** Docker, Nginx, Certbot, yc CLI, backup cron.

### 1.6 TLS сертификат (после DNS propagation)

```bash
# Убедись что DNS resolves:
dig +short <domain>   # должен вернуть vm_external_ip

# SSH на VM и запусти certbot:
ssh ubuntu@<vm_external_ip>
sudo certbot --nginx -d <domain> \
  --non-interactive --agree-tos \
  -m admin@<domain>

# Проверь автопродление:
sudo certbot renew --dry-run
```

### 1.7 Systemd сервис для автозапуска (⚠️ пока отсутствует)

```bash
# SSH на VM:
sudo tee /etc/systemd/system/aida-compose.service > /dev/null <<'EOF'
[Unit]
Description=AIDA Docker Compose Stack
Requires=docker.service
After=docker.service network-online.target
Wants=network-online.target

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/seer-studio
EnvironmentFile=/opt/seer-studio/.env.prod
ExecStart=/opt/seer-studio/docker-compose-start.sh
ExecStop=/usr/bin/docker compose \
  -f docker-compose.prod.yml \
  -f docker-compose.yc.yml down
TimeoutStartSec=300

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable aida-compose
```

> ⚠️ **Это нужно сделать вручную** — systemd service ещё не в `cloud-init.yml`.
> Добавить в `cloud-init.yml` в следующем спринте.

### 1.8 HEIMDALL IP-whitelist (опционально, рекомендуется)

```bash
# SSH на VM, отредактировать nginx config:
sudo nano /etc/nginx/sites-available/aida
# Найди блок location /heimdall и раскомментируй:
#   allow  <YOUR_OFFICE_IP>/32;
#   deny   all;

sudo nginx -t && sudo systemctl reload nginx
```

---

## Часть 2 — Деплой (per push to master)

### 2.1 Автоматический деплой

```bash
git push origin master
```

GitHub Actions (`cd.yml`) выполнит:
1. Build + push → GHCR
2. Mirror GHCR → YCR (`cr.yandex/...`)
3. SSH на VM → `lockbox-sync.sh` → `docker compose up -d --pull always`
4. Health check: Chur, SHUTTLE, Verdandi

**Статус:** https://github.com/NooriUta/aida-root/actions

### 2.2 Ручной деплой (аварийный)

```bash
ssh ubuntu@<vm_external_ip>
cd /opt/seer-studio

# Синхронизировать секреты
./infra/scripts/lockbox-sync.sh

# Залогиниться в YCR
yc container registry configure-docker
# или:
docker login cr.yandex -u json_key --password-stdin <<< $(cat /path/to/sa-key.json)

# Pull и запуск
docker compose -f docker-compose.prod.yml -f docker-compose.yc.yml pull
docker compose -f docker-compose.prod.yml -f docker-compose.yc.yml up -d --remove-orphans
```

---

## Часть 3 — Проверка после деплоя

```bash
# Статус контейнеров
docker compose -f docker-compose.prod.yml -f docker-compose.yc.yml ps

# Все должны быть healthy:
# keycloak, shuttle, chur, dali, heimdall-backend, heimdall-frontend,
# verdandi, shell, frigg, houndarcade

# Логи если что-то не так:
docker compose logs --tail=50 <service_name>
```

**Smoke tests:**

```bash
DOMAIN=seer.yourdomain.com

# Main UI
curl -sIL https://$DOMAIN/ | grep "200 OK"

# Auth
curl -sI https://$DOMAIN/auth/profile | grep "401\|200"

# GraphQL health
curl -s https://$DOMAIN/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{__typename}"}' | grep "QueryType"

# Dali internal health (через VM)
curl -s http://localhost:19090/q/health | grep '"status":"UP"'

# HEIMDALL backend
curl -s http://localhost:19093/q/health | grep '"status":"UP"'
```

---

## Часть 4 — Обслуживание

### 4.1 Бэкапы ArcadeDB

```bash
# Запуск вручную (обычно работает по cron 03:00 UTC):
ssh ubuntu@<vm_external_ip>
sudo bash /opt/seer-studio/infra/scripts/backup-arcadedb.sh

# Проверить наличие бэкапов:
aws s3 ls s3://aida-arcadedb-backups/ \
  --endpoint-url https://storage.yandexcloud.net \
  --recursive | tail -10
```

### 4.2 Обновление Lockbox секретов

```bash
yc lockbox secret add-version \
  --id <LOCKBOX_ID> \
  --payload '[{"key": "ANTHROPIC_API_KEY", "text_value": "sk-ant-NEW..."}]'

# Перезагрузить секреты на VM:
ssh ubuntu@<vm_external_ip>
./infra/scripts/lockbox-sync.sh
docker compose -f docker-compose.prod.yml -f docker-compose.yc.yml \
  up -d shuttle dali   # перезапустить только те что читают ключ
```

### 4.3 Мониторинг дискового пространства

```bash
ssh ubuntu@<vm_external_ip>
df -h                          # общий диск
docker system df               # Docker volumes
docker volume ls               # список volumes
# Критичные volumes:
#   hound_databases   — lineage graph (основные данные)
#   frigg_databases   — state store
```

### 4.4 Масштабирование VM

```bash
# Остановить stack
docker compose -f docker-compose.prod.yml -f docker-compose.yc.yml down

# Изменить конфигурацию через YC Console:
#   Virtual Machines → aida-prod → Edit → CPU/RAM → Save
# или через terraform:
#   vm_cores  = 4
#   vm_memory = 16
#   terraform apply  — YC изменит конфигурацию (downtime ~1 мин)
```

> ⚠️ **После ресайза VM IP меняется** если IP динамический.
> Проверь после ресайза: `yc compute instance get aida-prod --format=json | jq '.network_interfaces[].primary_v4_address.one_to_one_nat.address'`
> Обнови DNS A-запись и GitHub Secret `DEPLOY_HOST` если IP изменился.

```bash
# Обновить resource limits в docker-compose.yc.yml после изменения RAM:
# Скрипт-помощник (пересчитывает лимиты):
bash scripts/upgrade-mem-16gb.sh

# Поднять stack обратно
./docker-compose-start.sh
```

---

## Часть 5 — Известные ограничения (текущий момент)

| # | Ограничение | Workaround | Когда фиксить |
|---|---|---|---|
| 1 | **4 сервиса не собираются в CD** (dali, heimdall-backend, heimdall-frontend, shell) | Собрать локально и запушить вручную в YCR | Следующий спринт |
| 2 | **Нет systemd service** — stack не стартует после reboot VM | Вручную (шаг 1.7) или `docker-compose-start.sh` по SSH | Следующий спринт |
| 3 | ~~**resource limits** не настроены~~ | ~~`docker stats`~~ | ✅ **FIXED** PR #34 — лимиты настроены для 16 GB VM |
| 4 | **Certbot** — ручная выдача после DNS | Шаг 1.6 | Добавить в cloud-init |
| 5 | ~~**S3 File Upload** (Dali)~~ | ~~Использовать локальный путь~~ | ✅ **FIXED** sprint/dali-file-upload-apr16 |
| 6 | **HEIMDALL IP whitelist** не настроен | Вручную (шаг 1.8) | Перед первым prod-деплоем |
| 7 | **ArcadeDB MVCC conflict** — редкий `Concurrent modification on page DaliColumn` при парсинге большого корпуса | Повторить сессию; batch size 5000 слишком велик | Уменьшить batch size в ParseJob до ~500 |

---

## Часть 6 — Troubleshooting

### Контейнер не поднимается

```bash
docker compose logs --tail=100 <service>
# Частые причины:
# - FRIGG не healthy → dali не запустится (depends_on)
# - Неверный пароль в .env.prod → arcadedb refuses connection
# - YCR image не найден → проверь YC_REGISTRY_ID
```

### Потерян .env.prod

```bash
# Восстановить из Lockbox:
./infra/scripts/lockbox-sync.sh
```

### ArcadeDB потерял данные

```bash
# Восстановить из бэкапа:
BACKUP_DATE=2026-04-15
aws s3 cp s3://aida-arcadedb-backups/$BACKUP_DATE/hound.zip . \
  --endpoint-url https://storage.yandexcloud.net
# Распаковать в volume:
docker volume create hound_databases_restore
docker run --rm -v hound_databases_restore:/data -v $(pwd):/backup \
  alpine sh -c "unzip /backup/hound.zip -d /data"
# Заменить volume и перезапустить houndarcade
```

### Nginx 502 Bad Gateway

```bash
sudo nginx -t              # проверить конфиг
sudo systemctl status nginx
docker compose ps          # все ли контейнеры healthy
# Проверить порты:
ss -tlnp | grep -E "13000|18180|15173|25174"
```

### FRIGG WAL corruption (RestartCount растёт, логи: "received operation on deleted file")

Повреждение WAL — требует полного сброса volume. **Данные FRIGG эфемерны** (только JobRunr state, пересоздаётся при старте Dali).

```bash
cd /opt/seer-studio
docker compose -f docker-compose.prod.yml -f docker-compose.yc.yml down
docker volume rm seer-studio_frigg_data   # имя volume: проверь через `docker volume ls`
docker compose -f docker-compose.prod.yml -f docker-compose.yc.yml up -d
```

> ⚠️ После удаления volume Dali пересоздаёт схему FRIGG автоматически через `FriggSchemaInitializer`.

### Сайт не открывается после смены IP

Симптом: браузер показывает `ERR_TIMED_OUT`, ping идёт на старый IP.

```bash
# 1. Проверить текущий IP VM
yc compute instance get aida-prod --format=json | \
  jq '.network_interfaces[].primary_v4_address.one_to_one_nat.address'

# 2. Проверить что DNS обновился
nslookup seidrstudio.pro 1.1.1.1   # должен вернуть новый IP

# 3. Проверить порт 443 на VM (должен видеть TCP handshake)
sudo tcpdump -i eth0 port 443 -c 10

# 4. Временный workaround пока DNS не разошёлся:
# Добавить в C:\Windows\System32\drivers\etc\hosts (Windows, от Administrator):
#   <NEW_IP> seidrstudio.pro www.seidrstudio.pro seer.seidrstudio.pro heimdall.seidrstudio.pro
# После разлёта DNS убрать эти строки из hosts!
```

> **Причина:** YC назначает динамический IP при ресайзе. Решение — зарезервировать статический IP заранее (до первой выдачи сертификата).

### OOM — контейнер убивает себя (exit code 137)

```bash
docker inspect <container> | grep -A5 '"State"'
# OOMKilled: true → не хватает памяти

# Проверить текущее потребление:
docker stats --no-stream --format "table {{.Name}}\t{{.MemUsage}}\t{{.MemPerc}}"

# Увеличить лимиты в docker-compose.yc.yml и пересоздать контейнер:
docker compose -f docker-compose.prod.yml -f docker-compose.yc.yml \
  up -d --force-recreate <service_name>
```

### ArcadeDB MVCC conflict ("Concurrent modification on page")

Редкая ошибка при параллельной записи в ArcadeDB. Симптом: сессия падает с `Remote cmd FAILED: Concurrent modification on page DaliColumn`.

**Немедленное решение:** повторить сессию парсинга.
**Долгосрочное:** уменьшить batch size в `ParseJob.buildConfig()` с 5000 до ~500.

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 16.04.2026 | 1.0 | Создан на основе аудита infra/ (cloud-init, terraform, nginx, cd.yml). Полный runbook first-time + per-deploy + maintenance. 6 известных ограничений. |
| 18.04.2026 | 1.1 | Обновлён VM spec 8 GB → 16 GB. Добавлен раздел про статический IP. Troubleshooting: FRIGG WAL corruption, MVCC conflict, OOM, смена IP. Ограничение #3 (resource limits) и #5 (file upload) — FIXED. |
| 18.04.2026 | 1.2 | Добавлен скрипт upgrade-mem-16gb.sh в секцию масштабирования. |
