# SEER Studio — Installation Guide

**Документ:** `INSTALLATION`
**Версия:** 1.0
**Дата:** 18.04.2026
**Статус:** ✅ ACTIVE
**Аудитория:** DevOps / первый деплой на чистый сервер

> Этот гид описывает полную установку SEER Studio на сервер Yandex Cloud с нуля.
> Читай последовательно. Одноразовые шаги помечены **(first-time only)**.
> Для последующих деплоев используй `YC_DEPLOYMENT.md §Часть 2`.

---

## Содержание

1. [Требования](#1-требования)
2. [Yandex Cloud — создать VM](#2-yandex-cloud--создать-vm)
3. [Статический IP и DNS](#3-статический-ip-и-dns)
4. [Группа безопасности](#4-группа-безопасности)
5. [Первый вход на VM](#5-первый-вход-на-vm)
6. [Установка Docker](#6-установка-docker)
7. [Установка Nginx и Certbot](#7-установка-nginx-и-certbot)
8. [Клонирование репозитория](#8-клонирование-репозитория)
9. [Настройка секретов](#9-настройка-секретов)
10. [Настройка Nginx (reverse proxy)](#10-настройка-nginx-reverse-proxy)
11. [TLS-сертификат (Let's Encrypt)](#11-tls-сертификат-lets-encrypt)
12. [Первый запуск стека](#12-первый-запуск-стека)
13. [Проверка работоспособности](#13-проверка-работоспособности)
14. [Настройка CI/CD (GitHub Actions)](#14-настройка-cicd-github-actions)
15. [Известные проблемы при первом запуске](#15-известные-проблемы-при-первом-запуске)

---

## 1. Требования

### Локально (на твоём компьютере)

| Инструмент | Зачем |
|---|---|
| `yc` CLI | управление YC из терминала |
| `ssh` | подключение к VM |
| `git` | работа с репозиторием |
| `gh` | GitHub CLI (необязательно, для секретов) |

```bash
# Установить yc CLI:
curl -sL https://storage.yandexcloud.net/yandexcloud-yc/install.sh | bash
yc init   # авторизация + выбор cloud/folder
```

### Инфраструктура

| Ресурс | Минимум | Рекомендуется |
|---|---|---|
| VM CPU | 4 vCPU | 4 vCPU |
| VM RAM | 8 GB | **16 GB** |
| VM диск | 30 GB SSD | 50 GB SSD |
| ОС | Ubuntu 22.04 LTS | Ubuntu 22.04 LTS |
| Статический IP | обязательно | — |
| Домен | обязательно (для TLS) | — |

> ⚠️ **8 GB недостаточно** при активном парсинге PL/SQL корпуса.
> ArcadeDB (YGG) требует ~4 GB, Dali + Frigg — ещё ~3 GB.
> Используй 16 GB чтобы избежать OOM-kill контейнеров.

---

## 2. Yandex Cloud — создать VM

### 2.1 Через YC Console

1. Перейди в **Compute Cloud → Виртуальные машины → Создать ВМ**
2. Настройки:
   - **Имя:** `aida-prod`
   - **Зона:** `ru-central1-b` (или ближайшая к тебе)
   - **Образ:** `Ubuntu 22.04 LTS`
   - **Тип диска:** SSD, **50 GB**
   - **vCPU:** 4, **RAM:** 16 GB, **Гарантированная доля CPU:** 100%
   - **SSH-ключ:** вставь публичный ключ `~/.ssh/id_ed25519.pub`
   - **Сеть:** выбери или создай VPC-сеть и подсеть
3. **Не создавай VM сразу** — сначала зарезервируй статический IP (шаг 3).

### 2.2 Через yc CLI

```bash
# Создать VM (замени <SUBNET_ID> и <SSH_KEY>)
yc compute instance create \
  --name aida-prod \
  --zone ru-central1-b \
  --cores 4 \
  --memory 16 \
  --core-fraction 100 \
  --create-boot-disk image-folder-id=standard-images,image-family=ubuntu-2204-lts,size=50,type=network-ssd \
  --network-interface subnet-id=<SUBNET_ID>,nat-ip-version=ipv4,nat-address=<STATIC_IP> \
  --ssh-key ~/.ssh/id_ed25519.pub \
  --preemptible false
```

---

## 3. Статический IP и DNS

### 3.1 Зарезервировать статический IP (first-time only)

> ⚠️ **Сделай это ДО создания VM.** Динамический IP меняется после каждого ресайза/перезапуска VM. Если IP изменится после выдачи TLS-сертификата — придётся обновлять DNS и пересоздавать сертификат.

**Через Console:**
1. **Virtual Private Cloud → IP-адреса → Зарезервировать адрес**
2. Выбери ту же зону доступности, что и VM
3. Запиши IP — он будет вида `111.88.xxx.xxx`

**Через yc CLI:**
```bash
yc vpc address create \
  --name aida-static-ip \
  --external-ipv4 zone=ru-central1-b
# Запиши: address: 111.88.xxx.xxx
```

### 3.2 Привязать IP к VM

```bash
yc compute instance add-one-to-one-nat aida-prod \
  --network-interface-index 0 \
  --external-ip-address <STATIC_IP>
```

### 3.3 DNS A-запись

В панели управления доменом создай A-запись:

| Запись | Тип | Значение | TTL |
|---|---|---|---|
| `seidrstudio.pro` | A | `<STATIC_IP>` | 300 |
| `seer.seidrstudio.pro` | A | `<STATIC_IP>` | 300 |
| `heimdall.seidrstudio.pro` | A | `<STATIC_IP>` | 300 |

Проверь разлёт DNS (занимает 5–60 минут):
```bash
nslookup seidrstudio.pro 1.1.1.1
# должен вернуть <STATIC_IP>
```

---

## 4. Группа безопасности

В YC создай группу безопасности `aida-sg` со следующими **входящими** правилами:

| Протокол | Порт | Источник | Назначение |
|---|---|---|---|
| TCP | 22 | `0.0.0.0/0` | SSH |
| TCP | 80 | `0.0.0.0/0` | HTTP (Certbot + redirect) |
| TCP | 443 | `0.0.0.0/0` | HTTPS |

> YC Security Groups **stateful** — исходящий трафик разрешён автоматически для входящих соединений. Отдельные правила на egress не нужны.

```bash
# Создать через yc CLI:
yc vpc security-group create \
  --name aida-sg \
  --network-name default \
  --rule direction=ingress,protocol=tcp,port=22,cidr=0.0.0.0/0 \
  --rule direction=ingress,protocol=tcp,port=80,cidr=0.0.0.0/0 \
  --rule direction=ingress,protocol=tcp,port=443,cidr=0.0.0.0/0

# Привязать к VM:
yc compute instance update aida-prod \
  --network-interface-index 0 \
  --security-group-name aida-sg
```

---

## 5. Первый вход на VM

```bash
ssh ubuntu@<STATIC_IP>

# Обновить пакеты
sudo apt-get update && sudo apt-get upgrade -y

# Убедись что hostname и timezone правильные
hostnamectl
sudo timedatectl set-timezone UTC
```

---

## 6. Установка Docker

```bash
# Установить Docker Engine (официальный способ)
curl -fsSL https://get.docker.com | sudo sh

# Добавить ubuntu в группу docker (без sudo)
sudo usermod -aG docker ubuntu

# Применить группу (или перелогиниться)
newgrp docker

# Проверка
docker --version          # >= 24
docker compose version    # >= 2.20
```

---

## 7. Установка Nginx и Certbot

```bash
sudo apt-get install -y nginx certbot python3-certbot-nginx

# Убедиться что nginx запущен
sudo systemctl enable nginx
sudo systemctl start nginx
sudo systemctl status nginx
```

---

## 8. Клонирование репозитория

```bash
# Создать директорию для приложения
sudo mkdir -p /opt/seer-studio
sudo chown ubuntu:ubuntu /opt/seer-studio

# Клонировать (используй deploy key или HTTPS с токеном)
cd /opt/seer-studio
git clone https://github.com/NooriUta/AIDA.git .

# Или через SSH (если настроен deploy key):
git clone git@github.com:NooriUta/AIDA.git .
```

---

## 9. Настройка секретов

Все секреты хранятся в `.env.prod` на VM. Этот файл **не** в git — синхронизируется из Yandex Lockbox.

### 9.1 Если Lockbox настроен

```bash
# Синхронизировать секреты из Lockbox:
bash infra/scripts/lockbox-sync.sh

# Проверить что файл создан:
ls -la /opt/seer-studio/.env.prod
```

### 9.2 Если Lockbox не настроен (ручной способ)

```bash
cat > /opt/seer-studio/.env.prod << 'EOF'
# ArcadeDB credentials
ARCADEDB_ADMIN_PASSWORD=<STRONG_PASSWORD>
ARCADEDB_PASS=<STRONG_PASSWORD>

# Keycloak
KEYCLOAK_CLIENT_SECRET=<BASE64_32_BYTES>

# Chur BFF
COOKIE_SECRET=<HEX_32_BYTES>

# AI
ANTHROPIC_API_KEY=sk-ant-...

# FRIGG (ArcadeDB state store)
FRIGG_PASSWORD=<STRONG_PASSWORD>

# YC Registry
YC_REGISTRY_ID=<YOUR_REGISTRY_ID>
IMAGE_TAG=latest

# Domain
DOMAIN=seidrstudio.pro
EOF

chmod 600 /opt/seer-studio/.env.prod
```

**Генерация случайных секретов:**
```bash
# COOKIE_SECRET (hex 32 bytes):
openssl rand -hex 32

# KEYCLOAK_CLIENT_SECRET (base64):
openssl rand -base64 32

# ARCADEDB_ADMIN_PASSWORD / FRIGG_PASSWORD — придумай сильный пароль
```

---

## 10. Настройка Nginx (reverse proxy)

Файл конфигурации nginx уже есть в репозитории: `infra/nginx/nginx.conf`.

```bash
# Создать symlink в sites-available:
sudo ln -sf /opt/seer-studio/infra/nginx/nginx.conf \
  /etc/nginx/sites-available/aida

# Активировать сайт:
sudo ln -sf /etc/nginx/sites-available/aida \
  /etc/nginx/sites-enabled/aida

# Отключить дефолтный сайт nginx:
sudo rm -f /etc/nginx/sites-enabled/default

# Заменить домен в конфиге (если нужно):
sudo sed -i 's/seidrstudio\.pro/your-domain.com/g' /etc/nginx/sites-available/aida

# Проверить конфиг:
sudo nginx -t

# Перезагрузить nginx:
sudo systemctl reload nginx
```

> Nginx должен слушать порт 80 чтобы Certbot мог пройти HTTP-challenge.

---

## 11. TLS-сертификат (Let's Encrypt)

> ⚠️ **Выполняй только после того как DNS разошёлся** (`nslookup seidrstudio.pro 1.1.1.1` возвращает твой IP).

```bash
# Выпустить сертификат для всех поддоменов:
sudo certbot --nginx \
  -d seidrstudio.pro \
  -d seer.seidrstudio.pro \
  -d heimdall.seidrstudio.pro \
  --non-interactive \
  --agree-tos \
  -m admin@seidrstudio.pro

# Проверить автопродление:
sudo certbot renew --dry-run
```

Certbot автоматически добавит блоки `ssl_certificate` в nginx.conf и настроит cron для продления.

---

## 12. Первый запуск стека

### 12.1 Логин в Container Registry

```bash
cd /opt/seer-studio

# YCR (Yandex Container Registry):
source .env.prod
echo "<SA_KEY_JSON>" | docker login cr.yandex -u json_key --password-stdin

# Или через yc:
yc container registry configure-docker
```

### 12.2 Pull образов

```bash
docker compose \
  -f docker-compose.prod.yml \
  -f docker-compose.yc.yml \
  pull
```

### 12.3 Запустить стек

```bash
docker compose \
  -f docker-compose.prod.yml \
  -f docker-compose.yc.yml \
  up -d --remove-orphans
```

### 12.4 Следить за запуском

```bash
# Статус всех контейнеров (обновляется каждые 3 сек):
watch -n 3 "docker compose \
  -f docker-compose.prod.yml \
  -f docker-compose.yc.yml \
  ps --format 'table {{.Name}}\t{{.Status}}'"
```

**Ожидаемый порядок запуска:**
```
frigg          → healthy (30-60 сек)
ygg            → healthy (30-60 сек)
keycloak       → healthy (60-120 сек)
dali           → running (после frigg healthy)
  └─ YggSchemaInitializer(3)   → создаёт БД hound в YGG
  └─ FriggSchemaInitializer(5) → создаёт схему FRIGG
  └─ JobRunrLifecycle(10)      → запускает BackgroundJobServer
  └─ SessionService(20)        → готов к сессиям
shuttle        → running
chur           → running
heimdall-backend → running
heimdall-frontend → running
verdandi       → running
shell          → running
```

---

## 13. Проверка работоспособности

```bash
DOMAIN=seidrstudio.pro

# 1. Главная страница
curl -sIL https://$DOMAIN/ | grep "HTTP/"
# → HTTP/2 200

# 2. Auth
curl -sI https://$DOMAIN/auth/profile | grep "HTTP/"
# → HTTP/1.1 401 (не авторизован — это нормально)

# 3. GraphQL
curl -s https://$DOMAIN/graphql \
  -H "Content-Type: application/json" \
  -d '{"query":"{__typename}"}' | grep -o '"__typename"'
# → "__typename"

# 4. Dali health (SSH на VM)
curl -s http://localhost:19090/api/sessions/health
# → {"frigg":"ok","ygg":"ok","sessions":0}

# 5. HEIMDALL backend (SSH на VM)
curl -s http://localhost:19093/q/health | python3 -m json.tool | grep '"status"'
# → "status": "UP"

# 6. Потребление памяти (должно быть < 80% на каждый контейнер)
docker stats --no-stream --format \
  "table {{.Name}}\t{{.MemUsage}}\t{{.MemPerc}}"
```

**Открой в браузере:**
- `https://seidrstudio.pro` — главный UI (VERDANDI)
- `https://heimdall.seidrstudio.pro` — admin UI (HEIMDALL)

---

## 14. Настройка CI/CD (GitHub Actions)

### 14.1 GitHub Secrets

В репозитории → **Settings → Secrets and variables → Actions → New repository secret**:

| Secret | Значение |
|---|---|
| `DEPLOY_HOST` | `<STATIC_IP>` |
| `DEPLOY_USER` | `ubuntu` |
| `DEPLOY_KEY` | содержимое `~/.ssh/id_ed25519` (приватный ключ) |
| `YC_SA_KEY` | JSON ключ service account для push/pull в YCR |
| `YC_REGISTRY_ID` | ID Container Registry из YC Console |

### 14.2 Создать JSON ключ service account

```bash
# На локальной машине:
yc iam key create \
  --service-account-name aida-ci \
  --output sa-key.json
cat sa-key.json   # скопируй содержимое в секрет YC_SA_KEY
rm sa-key.json    # удали файл сразу после копирования
```

### 14.3 Проверить деплой

```bash
# Запустить деплой вручную:
gh workflow run cd.yml --ref master

# Или пушни в master:
git push origin master

# Следить за деплоем:
gh run watch
```

---

## 15. Известные проблемы при первом запуске

### Dali падает с "FRIGG connection error"

FRIGG ещё не готов. Подожди пока `docker compose ps frigg` покажет `(healthy)`.
Dali стартует автоматически после того как FRIGG станет healthy.

### "Database 'hound' is not available"

База данных `hound` в YGG не была создана. **В текущей версии это обрабатывается автоматически** через `YggSchemaInitializer` при старте Dali. Если ошибка всё равно появляется:

```bash
# Создать вручную (SSH на VM):
curl -u root:${ARCADEDB_ADMIN_PASSWORD} \
  -X POST http://localhost:2480/api/v1/server \
  -H "Content-Type: application/json" \
  -d '{"command":"create database hound"}'
# → {"result":"ok"}
```

### Keycloak не стартует (OutOfMemoryError)

```bash
docker compose logs keycloak --tail=50 | grep -i "memory\|OOM\|killed"
# Увеличить лимит в docker-compose.yc.yml:
#   keycloak.deploy.resources.limits.memory: 1G → 2G
docker compose -f docker-compose.prod.yml -f docker-compose.yc.yml \
  up -d --force-recreate keycloak
```

### Сертификат выдаётся с ошибкой "DNS problem"

DNS ещё не разошёлся. Подожди 10–30 минут и повтори:
```bash
nslookup seidrstudio.pro 8.8.8.8   # проверь разлёт через Google DNS
sudo certbot --nginx -d seidrstudio.pro ...
```

### Сайт не открывается после выдачи сертификата

```bash
# Проверить nginx конфиг:
sudo nginx -t

# Проверить что порт 443 слушается:
ss -tlnp | grep 443

# Проверить Security Group в YC Console:
#   Virtual Private Cloud → Группы безопасности → aida-sg
#   Должно быть правило: TCP 443 из 0.0.0.0/0

# Тест с самой VM:
curl -k https://localhost/
```

### FRIGG WAL corruption при первом запуске

Симптом: `docker compose ps frigg` показывает бесконечные рестарты, логи содержат `"received operation on deleted file"`.

```bash
docker compose -f docker-compose.prod.yml -f docker-compose.yc.yml down
docker volume ls | grep frigg
docker volume rm seer-studio_frigg_data
docker compose -f docker-compose.prod.yml -f docker-compose.yc.yml up -d
```

---

## Что дальше

После успешной установки:

| Задача | Документ |
|---|---|
| Ежедневный деплой | `docs/guides/YC_DEPLOYMENT.md §Часть 2` |
| Запуск первой сессии парсинга | `docs/guides/ONBOARDING.md §4.4` |
| Настройка мониторинга | `docs/guides/YC_DEPLOYMENT.md §4.3` |
| Резервные копии ArcadeDB | `docs/guides/YC_DEPLOYMENT.md §4.1` |
| Локальная разработка | `docs/guides/DEVELOPMENT.md` |
