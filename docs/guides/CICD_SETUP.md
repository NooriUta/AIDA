# AIDA — CI/CD и Production Setup

> Всё ниже делается **один раз** перед первым деплоем.
> После — пайплайн работает автоматически при каждом merge в master.

---

## Содержание

1. [GitHub Container Registry (GHCR)](#1-github-container-registry)
2. [Yandex Container Registry (YCR)](#2-yandex-container-registry)
3. [Сервер: подготовка](#3-сервер-подготовка)
4. [SSH-ключ для деплоя](#4-ssh-ключ-для-деплоя)
5. [GitHub Secrets](#5-github-secrets)
6. [Файл .env.prod на сервере](#6-файл-envprod-на-сервере)
7. [HTTPS / TLS — домен seidrstudio.pro](#7-https--tls)
8. [Первый деплой](#8-первый-деплой)
9. [Rollback](#9-rollback)
10. [Чеклист](#10-чеклист)

---

## 1. GitHub Container Registry

GHCR доступен для репозитория NooriUta/AIDA — дополнительной регистрации не нужно.

### 1.1 Создать Personal Access Token (PAT)

```
GitHub → Settings → Developer settings
→ Personal access tokens → Tokens (classic) → Generate new token

Scopes:
  ✓ write:packages
  ✓ read:packages
  ✓ delete:packages

Name: aida-ghcr-deploy
Expiration: No expiration (или ротировать раз в год)
```

Сохрани токен — пойдёт в `GHCR_TOKEN` (шаг 5).

---

## 2. Yandex Container Registry

Образы зеркалируются из GHCR → YCR для быстрых pull'ов на VM в YC.

### 2.1 Создать сервис-аккаунт и ключ

```bash
# Создать SA (если нет)
yc iam service-account create --name aida-ci

# Выдать роль container-registry.images.pusher
yc container registry add-access-binding \
  --registry-name aida \
  --service-account-name aida-ci \
  --role container-registry.images.pusher

# Создать JSON-ключ
yc iam key create --service-account-name aida-ci \
  --output ci-key.json
```

Содержимое `ci-key.json` → GitHub Secret `YC_SA_JSON_KEY`.  
ID реестра → `YC_REGISTRY_ID`.

---

## 3. Сервер: подготовка

### Требования

| Ресурс | Минимум | Используемый |
|--------|---------|-------------|
| RAM    | 4 GB    | 8 GB        |
| CPU    | 2 vCPU  | 4 vCPU      |
| Disk   | 40 GB   | 80 GB       |
| OS     | Ubuntu 22.04 | Ubuntu 22.04 |

### Установить Docker

```bash
ssh user@95.163.244.138

curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
newgrp docker

docker --version          # Docker 24+
docker compose version    # v2.x
```

### Создать рабочую директорию

```bash
sudo mkdir -p /opt/seer-studio
sudo chown $USER:$USER /opt/seer-studio
```

### Установить nginx и certbot

```bash
sudo apt update
sudo apt install -y nginx certbot python3-certbot-nginx
```

### Войти в GHCR и YCR

```bash
# GHCR
echo "ВАШ_GHCR_TOKEN" | docker login ghcr.io -u nooriuta --password-stdin

# YCR (после получения ключа SA)
cat ci-key.json | docker login \
  --username json_key \
  --password-stdin \
  cr.yandex
```

---

## 4. SSH-ключ для деплоя

```bash
# На сервере
ssh-keygen -t ed25519 -C "aida-deploy" -f ~/.ssh/aida_deploy -N ""

# Добавить публичный ключ в authorized_keys
cat ~/.ssh/aida_deploy.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

# Вывести приватный ключ — скопировать целиком в GitHub Secret DEPLOY_KEY
cat ~/.ssh/aida_deploy
```

---

## 5. GitHub Secrets

```
GitHub → NooriUta/AIDA → Settings → Secrets and variables → Actions
→ New repository secret
```

| Secret | Значение | Пример |
|--------|----------|--------|
| `DEPLOY_HOST` | IP сервера | `95.163.244.138` |
| `DEPLOY_USER` | Пользователь SSH | `ubuntu` |
| `DEPLOY_KEY` | Приватный SSH-ключ | `-----BEGIN OPENSSH...` |
| `GHCR_TOKEN` | PAT с `read/write:packages` | шаг 1.1 |
| `YC_REGISTRY_ID` | ID реестра YCR | `crp1234abcd5678` |
| `YC_SA_JSON_KEY` | JSON SA-ключ для YCR | `{"id":"..."}` |
| `DOMAIN` | Домен продакшена | `seidrstudio.pro` |
| `KEYCLOAK_CLIENT_SECRET` | KC client secret | _(генерировать)_ |
| `COOKIE_SECRET` | Секрет сессий Chur | _(минимум 32 символа)_ |
| `FRIGG_PASSWORD` | Пароль ArcadeDB FRIGG | _(минимум 16 символов)_ |

---

## 6. Файл .env.prod на сервере

Создать `/opt/seer-studio/.env.prod` — этот файл читается `docker-compose.yc.yml` через `env_file`.

```bash
cat > /opt/seer-studio/.env.prod << 'EOF'
# Домен
DOMAIN=seidrstudio.pro

# Keycloak
KEYCLOAK_CLIENT_SECRET=<из GitHub Secret>
KC_ADMIN_USER=admin
KC_ADMIN_PASS=<сильный пароль>

# Cookie-сессии Chur
COOKIE_SECRET=<минимум 32 случайных символа>

# ArcadeDB — Ygg (lineage graph)
ARCADEDB_ROOT_PASSWORD=<пароль>
ARCADEDB_USER=root
ARCADEDB_DB=hound

# ArcadeDB — Frigg (HEIMDALL + Dali)
FRIGG_PASSWORD=<пароль>

# Опционально: количество дней хранения сессий Dali
DALI_SESSION_RETENTION_DAYS=30
EOF

chmod 600 /opt/seer-studio/.env.prod
```

> ⚠️ Никогда не коммить `.env.prod` в репозиторий — он в `.gitignore`.

---

## 7. HTTPS / TLS

**Домен:** `seidrstudio.pro` → `95.163.244.138`

DNS уже настроен в Рег.ру:
```
A  @    → 95.163.244.138
A  www  → 95.163.244.138
```

### 7.1 Получить сертификат Let's Encrypt

Выполнить **один раз** на сервере:

```bash
# Убедиться что порт 80 открыт (нужен для ACME challenge)
sudo ufw allow 80
sudo ufw allow 443

# Получить сертификат
sudo certbot --nginx -d seidrstudio.pro -d www.seidrstudio.pro

# Проверить автообновление
sudo certbot renew --dry-run
```

Certbot автоматически:
- Создаст `/etc/letsencrypt/live/seidrstudio.pro/fullchain.pem`
- Настроит cron/systemd timer для обновления каждые 60 дней

### 7.2 После certbot — перезапустить CD

После получения сертификата запустить следующий деплой (или вручную):

```bash
cd /opt/seer-studio
sudo bash -c "sed 's/\${DOMAIN}/seidrstudio.pro/g' infra/nginx/nginx.conf \
  > /etc/nginx/sites-available/aida"
sudo ln -sf /etc/nginx/sites-available/aida /etc/nginx/sites-enabled/aida
sudo nginx -t && sudo systemctl reload nginx
```

### 7.3 Итоговые URL

| Сервис | URL |
|--------|-----|
| Verdandi (Seiðr Studio) | `https://seidrstudio.pro/` |
| Heimdallr (Control Panel) | `https://seidrstudio.pro/heimdall/` |
| Keycloak Admin | `https://seidrstudio.pro/kc/admin/` |

### 7.4 Пока нет сертификата

Сервисы доступны напрямую по IP (только для тестирования):

| Сервис | URL |
|--------|-----|
| Verdandi | `http://95.163.244.138:15173/` |
| Heimdallr | `http://95.163.244.138:25174/` |
| Chur BFF | `http://95.163.244.138:13000/` |

> ⚠️ Логин работать **не будет** — `COOKIE_SECURE=true` требует HTTPS.  
> Для теста можно временно установить `COOKIE_SECURE=false` в `.env.prod`.

---

## 8. Первый деплой

После настройки всех секретов и `.env.prod`:

```bash
ssh user@95.163.244.138
cd /opt/seer-studio

# Pull образов (первый раз вручную)
docker compose \
  -f docker-compose.prod.yml \
  -f docker-compose.yc.yml \
  pull

# Запуск стека
docker compose \
  -f docker-compose.prod.yml \
  -f docker-compose.yc.yml \
  up -d --remove-orphans

# Проверить статус
docker compose -f docker-compose.prod.yml -f docker-compose.yc.yml ps
docker compose -f docker-compose.prod.yml -f docker-compose.yc.yml logs --tail=30
```

### Health checks

```bash
# Chur
curl http://localhost:13000/health

# Heimdall backend
curl http://localhost:19093/q/health

# Verdandi
curl -I http://localhost:15173/

# Heimdallr
curl -I http://localhost:25174/
```

### Следующие деплои

Полностью автоматически при каждом merge в `master` через GitHub Actions.

---

## 9. Rollback

### Через GitHub Actions UI

```
GitHub → Actions → CD
→ Найти последний успешный деплой
→ Re-run jobs
```

### Вручную по SHA

```bash
ssh user@95.163.244.138
cd /opt/seer-studio

TARGET_SHA=abc1234

# Обновить IMAGE_TAG и перезапустить
IMAGE_TAG=${TARGET_SHA} docker compose \
  -f docker-compose.prod.yml \
  -f docker-compose.yc.yml \
  up -d --remove-orphans
```

---

## 10. Чеклист

### Разово (инфраструктура)

- [ ] GHCR PAT создан → `GHCR_TOKEN`
- [ ] YCR реестр создан, SA-ключ получен → `YC_REGISTRY_ID`, `YC_SA_JSON_KEY`
- [ ] Docker установлен на сервере
- [ ] `docker login ghcr.io` выполнен на сервере
- [ ] SSH-ключ сгенерирован, pub → `authorized_keys`
- [ ] `/opt/seer-studio` создана, права выставлены

### GitHub Secrets

- [ ] `DEPLOY_HOST` = `95.163.244.138`
- [ ] `DEPLOY_USER` = имя пользователя
- [ ] `DEPLOY_KEY` = приватный SSH-ключ
- [ ] `GHCR_TOKEN`
- [ ] `YC_REGISTRY_ID`
- [ ] `YC_SA_JSON_KEY`
- [ ] `DOMAIN` = `seidrstudio.pro`
- [ ] `KEYCLOAK_CLIENT_SECRET`
- [ ] `COOKIE_SECRET`
- [ ] `FRIGG_PASSWORD`

### .env.prod на сервере

- [ ] Файл создан в `/opt/seer-studio/.env.prod`
- [ ] `DOMAIN=seidrstudio.pro` прописан
- [ ] Все пароли заполнены, файл `chmod 600`

### HTTPS

- [ ] nginx установлен (`sudo apt install nginx`)
- [ ] certbot установлен (`sudo apt install certbot python3-certbot-nginx`)
- [ ] Порты 80 и 443 открыты в YC Security Group
- [ ] `certbot --nginx -d seidrstudio.pro -d www.seidrstudio.pro` выполнен
- [ ] `certbot renew --dry-run` — OK
- [ ] CD запущен после certbot → nginx перезагружен автоматически
- [ ] `https://seidrstudio.pro/` открывается с зелёным замком
- [ ] `https://seidrstudio.pro/heimdall/` открывается
- [ ] Логин на Verdandi работает, cookie `Secure=true` в DevTools
