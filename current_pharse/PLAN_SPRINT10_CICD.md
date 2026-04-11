# Sprint 10 — CI/CD Automation
Branch: `sprint10/cicd-automation`
Date: 11–18 апреля 2026

---

## Статус реализации (11 апр 2026)

### Сделано
- `.github/workflows/cd.yml` — build + push to GHCR + SSH deploy + health-check + Telegram notify
- `docker-compose.prod.yml` — prod compose с `image:` refs, healthcheck на всех сервисах
- `scripts/rollback.sh` — откат по SHA
- `verdandi/vite.config.ts` — coverage thresholds (lines 70%, functions 70%, branches 60%)
- `verdandi/package.json` — скрипт `test:coverage`
- `.github/workflows/ci.yml` — добавлен шаг `test:coverage`
- GitHub Environment `production` создан
- `@vitest/coverage-v8` установлен
- Coverage локально: Lines 77.9%, Functions 71.6%, Branches 65.7% — всё зелёно

### Отложено → Sprint 10.5 / Sprint 11
- **Поднятие хостинга** — нужны GitHub Secrets (`DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_KEY`, `GHCR_TOKEN`)
  и настройка сервера (Docker, SSH-ключ). Инструкция готова: `internal_docs/instructions/CICD_SETUP.md`
- **E2E в CI** — требует mock ArcadeDB (WireMock/json-server) без внешнего HoundArcade
- **Telegram notify** — опционально, после появления сервера

### Что нужно от тебя для активации деплоя
1. Сервер с Docker (Ubuntu 22.04, 2+ GB RAM)
2. Добавить 4 секрета в GitHub → Settings → Secrets:
   `DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_KEY`, `GHCR_TOKEN`
3. Инструкция: `internal_docs/instructions/CICD_SETUP.md`

---

## Контекст

Текущий CI (ci.yml): lint + test + build на 3 сервиса параллельно, все зелёные.
Текущий CD: **нет** — деплой только вручную через `docker compose up --build`.
ArcadeDB — внешний проект (HoundArcade), в пайплайн не входит.

**Цель спринта:** автоматизировать доставку от merge в master до работающего стека
без ручного ssh на сервер. E2E в CI без ArcadeDB — через mock или заглушку.

---

## Phase 1 — Docker Image Registry

### 1-A: Dockerfile audit
Проверить Dockerfile каждого сервиса:
- `verdandi/Dockerfile` — nginx + Vite build
- `Chur/Dockerfile` — Node + Fastify
- `SHUTTLE/Dockerfile` — Quarkus (JVM или native?)

Убедиться, что все образы детерминированы (pinned base images).

### 1-B: Build + push в GitHub Container Registry
Файл: `.github/workflows/cd.yml`

```yaml
on:
  push:
    branches: [master]

jobs:
  build-push:
    strategy:
      matrix:
        service: [verdandi, Chur, SHUTTLE]
    steps:
      - uses: docker/login-action (ghcr.io)
      - uses: docker/build-push-action
        with:
          context: ./${{ matrix.service }}
          tags: ghcr.io/nooriuta/verdandi/${{ matrix.service }}:${{ github.sha }}
          tags: ghcr.io/nooriuta/verdandi/${{ matrix.service }}:latest
```

Теги: `sha` (иммутабельный) + `latest` (rolling).

---

## Phase 2 — E2E в CI без ArcadeDB

### 2-A: E2E mock-режим
Проблема: Playwright тесты требуют ArcadeDB (внешний контейнер).
Решение: добавить `E2E_MOCK=true` режим в SHUTTLE — возвращает fixture-данные
без обращения к ArcadeDB.

Файл: `SHUTTLE/src/test/resources/application-e2e.properties`
```
quarkus.profile=e2e
arcade.mock=true
```

### 2-B: E2E job в ci.yml
```yaml
e2e:
  needs: [verdandi, chur, shuttle]
  runs-on: ubuntu-latest
  steps:
    - docker compose -f docker-compose.yml -f docker-compose.e2e.yml up -d
    - cd verdandi && npm run e2e
    - docker compose down
```

Файл: `docker-compose.e2e.yml` — override для SHUTTLE с mock-профилем,
без volumes на ArcadeDB.

---

## Phase 3 — Автодеплой на сервер

### 3-A: Deploy job
После успешного build-push — деплой через SSH.

```yaml
deploy:
  needs: build-push
  environment: production
  steps:
    - uses: appleboy/ssh-action
      with:
        host: ${{ secrets.DEPLOY_HOST }}
        key:  ${{ secrets.DEPLOY_KEY }}
        script: |
          cd /opt/seer-studio
          docker compose pull
          docker compose up -d --no-build
```

**Secrets нужны:**
- `DEPLOY_HOST` — IP/hostname сервера
- `DEPLOY_KEY` — SSH private key
- `GHCR_TOKEN` — для pull образов на сервере

### 3-B: docker-compose.prod.yml
Продакшн-compose использует image: вместо build:
```yaml
verdandi:
  image: ghcr.io/nooriuta/verdandi/verdandi:latest
  # убрать: build: context/dockerfile
```

### 3-C: Health-check gate
После `docker compose up` — ждать healthy всех сервисов:
```bash
docker compose exec chur curl -f http://localhost:3000/health
docker compose exec shuttle curl -f http://localhost:8080/q/health
```

---

## Phase 4 — Notifications & Observability

### 4-A: Telegram / Slack notify
При успешном деплое или провале — уведомление в чат.
```yaml
- uses: appleboy/telegram-action
  with:
    to: ${{ secrets.TELEGRAM_CHAT_ID }}
    token: ${{ secrets.TELEGRAM_TOKEN }}
    message: |
      ✅ Deployed ${{ github.sha }} to production
      PR: ${{ github.event.pull_request.html_url }}
```

### 4-B: GitHub Environments
Настроить environment `production` в GitHub Settings:
- Required reviewers (manual approval gate)
- Wait timer (опционально)
- Deployment history + rollback UI

### 4-C: Rollback
При провале health-check:
```bash
docker compose pull ghcr.io/nooriuta/verdandi/*:previous-sha
docker compose up -d
```

---

## Phase 5 — Coverage Gate

### 5-A: Vitest coverage threshold
В `vite.config.ts`:
```ts
coverage: {
  provider: 'v8',
  thresholds: {
    lines: 70,
    functions: 70,
    branches: 60,
  }
}
```

### 5-B: Coverage report в PR
```yaml
- run: npm run test:coverage
- uses: davelosert/vitest-coverage-report-action
```

Показывает diff покрытия прямо в PR-комментарии.

---

## Execution Order

```
1-A Dockerfile audit
  ↓
1-B Build+push (cd.yml)
  ↓
2-A SHUTTLE mock mode
2-B E2E job в ci.yml   ← параллельно с 1-B
  ↓
3-A Deploy job
3-B docker-compose.prod.yml
3-C Health-check gate
  ↓
4-A Notifications
4-B GitHub Environments
4-C Rollback script
  ↓
5-A Coverage threshold
5-B PR coverage report
```

---

## Итоговый пайплайн после спринта

```
push / PR → master
    │
    ├── [CI] verdandi: lint + test + build    ─┐
    ├── [CI] Chur: test + build               ─┤ параллельно
    ├── [CI] SHUTTLE: test + build            ─┤
    └── [CI] E2E: docker compose e2e-mock     ─┘
                     │ all green
                     ▼
             [CD] Build Docker images
             ghcr.io/nooriuta/verdandi/*:sha
                     │
                     ▼
             [CD] Push to GHCR
                     │
                     ▼
             [Gate] GitHub Environment
             (manual approval — опционально)
                     │
                     ▼
             [CD] SSH deploy → сервер
             docker compose pull + up
                     │
                     ▼
             [CD] Health-check
                     │
              ✓ healthy        ✗ failed
                     │              │
                     ▼              ▼
             Telegram notify   Rollback + notify
```

---

## Файлы к созданию / изменению

| Файл | Действие |
|------|---------|
| `.github/workflows/cd.yml` | NEW — build+push+deploy |
| `docker-compose.prod.yml` | NEW — image: refs вместо build: |
| `docker-compose.e2e.yml` | NEW — mock override |
| `.github/workflows/ci.yml` | MOD — добавить E2E job |
| `SHUTTLE/src/test/.../application-e2e.properties` | NEW — mock profile |
| `verdandi/vite.config.ts` | MOD — coverage thresholds |
| `verdandi/package.json` | MOD — test:coverage script |

**GitHub Secrets нужно настроить вручную:**
`DEPLOY_HOST`, `DEPLOY_KEY`, `GHCR_TOKEN`, `TELEGRAM_CHAT_ID`, `TELEGRAM_TOKEN`
