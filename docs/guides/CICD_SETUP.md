# Sprint 10 — CI/CD Setup: Инструкция

> Всё ниже делается **один раз** перед началом имплементации Sprint 10.
> После — пайплайн работает автоматически при каждом merge в master.

---

## Содержание

1. [GitHub Container Registry (GHCR)](#1-github-container-registry)
2. [Сервер: подготовка](#2-сервер-подготовка)
3. [SSH-ключ для деплоя](#3-ssh-ключ-для-деплоя)
4. [GitHub Secrets](#4-github-secrets)
5. [GitHub Environments](#5-github-environments)
6. [Telegram-бот (опционально)](#6-telegram-бот)
7. [Первый деплой: проверка](#7-первый-деплой)
8. [Rollback](#8-rollback)

---

## 1. GitHub Container Registry

GHCR уже доступен для репозитория NooriUta/Verdandi — дополнительной регистрации не нужно.

### Что нужно сделать

**1.1** Создать Personal Access Token (PAT) для pull образов на сервере:

```
GitHub → Settings → Developer settings
→ Personal access tokens → Tokens (classic) → Generate new token

Scopes:
  ✓ write:packages
  ✓ read:packages
  ✓ delete:packages

Name: seer-ghcr-deploy
Expiration: No expiration (или ротировать раз в год)
```

Сохрани токен — он понадобится в шаге 4 (`GHCR_TOKEN`).

**1.2** Видимость образов — по умолчанию приватные. Если сервер под другим аккаунтом,
убедись что пакеты доступны:

```
GitHub → Packages → (после первого push) → Package settings
→ Manage access → Add repository: NooriUta/Verdandi
```

---

## 2. Сервер: подготовка

### Требования к серверу

| Ресурс | Минимум | Рекомендовано |
|--------|---------|---------------|
| RAM | 2 GB | 4 GB |
| CPU | 1 vCPU | 2 vCPU |
| Disk | 20 GB | 40 GB |
| OS | Ubuntu 22.04 | Ubuntu 22.04 |
| Порты открыты | 22 (SSH), 80, 443, 15173, 13000 | |

### Установка Docker и Docker Compose

```bash
# Подключиться к серверу
ssh user@YOUR_SERVER_IP

# Установить Docker
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker $USER
newgrp docker

# Проверить
docker --version        # Docker 24+
docker compose version  # Docker Compose v2.x
```

### Создать рабочую директорию

```bash
sudo mkdir -p /opt/seer-studio
sudo chown $USER:$USER /opt/seer-studio
cd /opt/seer-studio

# Скопировать prod-compose файл (будет создан в Sprint 10)
# scp docker-compose.prod.yml user@server:/opt/seer-studio/docker-compose.yml
```

### Войти в GHCR на сервере

```bash
echo "ВАШ_GHCR_TOKEN" | docker login ghcr.io -u nooriuta --password-stdin
```

---

## 3. SSH-ключ для деплоя

Генерируется **один раз на сервере** (или локально — загружается на сервер).

```bash
# На сервере
ssh-keygen -t ed25519 -C "seer-studio-deploy" -f ~/.ssh/seer_deploy -N ""

# Добавить публичный ключ в authorized_keys
cat ~/.ssh/seer_deploy.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

# Вывести приватный ключ — скопировать целиком в GitHub Secret
cat ~/.ssh/seer_deploy
```

Приватный ключ (весь блок от `-----BEGIN...` до `...END-----`) пойдёт в `DEPLOY_KEY`.

---

## 4. GitHub Secrets

```
GitHub → репозиторий NooriUta/Verdandi
→ Settings → Secrets and variables → Actions → New repository secret
```

Добавить по одному:

| Secret | Значение | Где взять |
|--------|----------|-----------|
| `DEPLOY_HOST` | IP или hostname сервера, например `95.163.100.42` | VPS-провайдер |
| `DEPLOY_USER` | Имя пользователя на сервере, например `ubuntu` | VPS-провайдер |
| `DEPLOY_KEY` | Содержимое `~/.ssh/seer_deploy` (приватный ключ) | Шаг 3 |
| `GHCR_TOKEN` | PAT с `read/write:packages` | Шаг 1.1 |
| `TELEGRAM_CHAT_ID` | ID чата/канала (см. шаг 6) | Шаг 6 |
| `TELEGRAM_TOKEN` | Токен бота | Шаг 6 |

> `TELEGRAM_CHAT_ID` и `TELEGRAM_TOKEN` — опционально, пайплайн работает без них.

### Проверить Secrets

```
Settings → Secrets → Actions

Должно быть видно (значения скрыты):
  DEPLOY_HOST  ••••
  DEPLOY_USER  ••••
  DEPLOY_KEY   ••••
  GHCR_TOKEN   ••••
```

---

## 5. GitHub Environments

Настройка `production` environment — ручной апрув перед деплоем (опционально).

```
GitHub → Settings → Environments → New environment

Name: production

Required reviewers:
  ✓ NooriUta          ← сам себя, для подтверждения

Wait timer: 0 минут   (или 5 минут как safety net)

Deployment branches: master only
```

Если ручной апрув не нужен — оставь environment без Required reviewers,
деплой будет полностью автоматическим.

---

## 6. Telegram-бот

### Создать бота

```
1. Написать @BotFather в Telegram
2. /newbot
3. Имя: SEER Studio Deploy
4. Username: seer_studio_deploy_bot (или любой свободный)
5. Сохранить TOKEN — вида 7234567890:AAFxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

### Получить Chat ID

```
1. Написать боту любое сообщение (или добавить в канал)
2. Открыть в браузере:
   https://api.telegram.org/bot{TOKEN}/getUpdates

3. В ответе найти:
   "chat": { "id": -1001234567890 }
   Это и есть TELEGRAM_CHAT_ID
```

### Проверить работу

```bash
curl -X POST "https://api.telegram.org/bot{TOKEN}/sendMessage" \
  -d "chat_id={CHAT_ID}&text=Test+from+SEER+Studio"
```

---

## 7. Первый деплой: проверка

После того как Sprint 10 имплементирован и слит в master:

**7.1** Убедиться что образы появились в GHCR:

```
GitHub → Packages

Должно быть:
  ghcr.io/nooriuta/verdandi/verdandi:latest
  ghcr.io/nooriuta/verdandi/chur:latest
  ghcr.io/nooriuta/verdandi/shuttle:latest
```

**7.2** Проверить деплой вручную (первый раз):

```bash
ssh user@YOUR_SERVER_IP
cd /opt/seer-studio

# Скачать образы
docker compose pull

# Запустить стек
docker compose up -d

# Проверить статус
docker compose ps
docker compose logs --tail=20
```

**7.3** Health-check:

```bash
# Chur
curl http://localhost:13000/health

# SHUTTLE
curl http://localhost:18080/q/health

# verdandi (nginx)
curl http://localhost:15173
```

**7.4** Следующие деплои — полностью автоматически при merge в master.

---

## 8. Rollback

### Через GitHub Actions UI

```
GitHub → Actions → CI/CD
→ Найти последний успешный деплой
→ Re-run jobs
```

### Вручную на сервере по SHA

```bash
ssh user@YOUR_SERVER_IP
cd /opt/seer-studio

# Список последних тегов
docker images ghcr.io/nooriuta/verdandi/verdandi --format "{{.Tag}}"

# Откатиться на конкретный sha
export TARGET_SHA=abc1234

docker compose stop
docker pull ghcr.io/nooriuta/verdandi/verdandi:${TARGET_SHA}
docker pull ghcr.io/nooriuta/verdandi/chur:${TARGET_SHA}
docker pull ghcr.io/nooriuta/verdandi/shuttle:${TARGET_SHA}

# Обновить docker-compose.yml тег и поднять
sed -i "s|:latest|:${TARGET_SHA}|g" docker-compose.yml
docker compose up -d
```

### Скрипт rollback.sh (положить на сервер)

```bash
#!/bin/bash
# Использование: ./rollback.sh abc1234
set -e

SHA=${1:?Usage: rollback.sh <sha>}
cd /opt/seer-studio

echo "Rolling back to $SHA..."
for svc in verdandi chur shuttle; do
  docker pull ghcr.io/nooriuta/verdandi/${svc}:${SHA}
done

# Заменить latest на target sha в compose
cp docker-compose.yml docker-compose.yml.bak
sed -i "s|:latest|:${SHA}|g" docker-compose.yml

docker compose up -d
echo "Done. Rolled back to $SHA"
```

```bash
chmod +x rollback.sh
```

---

## Чеклист перед имплементацией Sprint 10

- [ ] GHCR PAT создан, сохранён
- [ ] Сервер: Docker + Compose установлены
- [ ] Сервер: `docker login ghcr.io` выполнен
- [ ] SSH-ключ сгенерирован, pub добавлен в authorized_keys
- [ ] GitHub Secret `DEPLOY_HOST` добавлен
- [ ] GitHub Secret `DEPLOY_USER` добавлен
- [ ] GitHub Secret `DEPLOY_KEY` добавлен (приватный ключ)
- [ ] GitHub Secret `GHCR_TOKEN` добавлен
- [ ] GitHub Environment `production` создан
- [ ] (opt) Telegram бот создан, `TELEGRAM_TOKEN` + `TELEGRAM_CHAT_ID` добавлены
- [ ] `/opt/seer-studio` директория создана на сервере
