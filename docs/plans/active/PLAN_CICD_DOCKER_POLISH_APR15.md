# Sprint: CI/CD + Docker Polish — APR 15 2026

## Context

Sprint 10 (`sprint10/cicd-automation`) закрыл базовый CI/CD. Этот спринт — финишная полировка:
устранить пробелы в healthcheck'ах dev-стека, добавить пропущенные сервисы в CI, обеспечить
явную проверку TypeScript во всех 3 TS-проектах, создать `.env.example` для Java-сервисов,
очистить устаревшие ссылки на `verdandi_net` в документации.

Дополнительно добавлены требования безопасности **Уровень 1** (обязательно до prod-деплоя)
и **Уровень 2** (при появлении сервера) — см. разделы I и J.

---

## Перед началом

1. `git checkout -b feature/cicd-docker-polish-apr15`
2. Сохранить этот план в `docs/sprints/PLAN_CICD_DOCKER_POLISH_APR15.md`

---

## Задачи

### A. Healthcheck туннинг (`docker-compose.yml`)

Сервисы **без** healthcheck в dev-стеке: `shuttle`, `chur`, `verdandi`, `heimdall-frontend`, `shell`.

Добавить в `docker-compose.yml`:

| Сервис | Команда | interval | retries | start_period |
|--------|---------|----------|---------|--------------|
| shuttle | `wget -q --spider http://127.0.0.1:8080/q/health \|\| exit 1` | 15s | 5 | 60s |
| chur | `wget -q --spider http://127.0.0.1:3000/health \|\| exit 1` | 10s | 5 | 30s |
| verdandi | `wget -q -O /dev/null http://127.0.0.1:5173/ \|\| exit 1` | 15s | 3 | 20s |
| heimdall-frontend | `wget -q -O /dev/null http://127.0.0.1:5174/ \|\| exit 1` | 15s | 3 | 20s |
| shell | `wget -q -O /dev/null http://127.0.0.1:5175/ \|\| exit 1` | 15s | 3 | 20s |

Timeout для всех новых: 5s. Обновить `depends_on` у зависимых сервисов там, где нужно
condition: `service_healthy` (например, `heimdall-frontend` ждёт `heimdall-backend`).

**Файл:** `docker-compose.yml`

---

### B. CI — TypeScript type checks (3 проекта)

**Файл:** `.github/workflows/ci.yml`

**B1. verdandi** — сейчас в CI стоит `npx vite build`, пропускающий tsc.
Заменить на `npm run build` (скрипт = `tsc -b && vite build`).

**B2. shell** — сервис вообще отсутствует в CI. Добавить новый job:
```yaml
shell:
  name: shell — typecheck + build
  runs-on: ubuntu-latest
  defaults:
    run:
      working-directory: frontends/shell
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-node@v4
      with:
        node-version: 24
        cache: npm
        cache-dependency-path: frontends/shell/package-lock.json
    - run: npm ci
    - run: npm run build   # = tsc && vite build
```

**B3. chur** — `npm run build` = `tsc --project tsconfig.json`, type check уже неявно есть.
Добавить явный шаг `npx tsc --noEmit` до `npm run build` для раздельного отображения ошибок в CI.

---

### C. CI — добавить dali

`services/dali` есть в docker-compose, но отсутствует в CI. Добавить job:

```yaml
dali:
  name: DALI — test + build
  runs-on: ubuntu-latest
  steps:
    - uses: actions/checkout@v4
    - uses: actions/setup-java@v4
      with:
        distribution: temurin
        java-version: 21
        cache: gradle
    - run: chmod +x gradlew
    - run: ./gradlew :services:dali:test
    - run: ./gradlew :services:dali:build -x test
```

Паттерн идентичен shuttle и heimdall-backend (уже работает).

---

### D. `.env.example` для всех сервисов

Создать файлы (переменные брать из docker-compose.yml, раздел `environment:`):

**D1. `services/shuttle/.env.example`**
```
# SHUTTLE — Quarkus GraphQL API
# Override with QUARKUS_ prefix env vars
QUARKUS_REST_CLIENT_ARCADE_URL=http://localhost:2480
ARCADEDB_DB=hound
ARCADEDB_USER=root
ARCADEDB_PASS=playwithdata    # [REQUIRED in prod]
```

**D2. `services/dali/.env.example`**
```
# DALI — PL/SQL Parser (Quarkus)
QUARKUS_HTTP_PORT=9090
FRIGG_URL=http://localhost:2481    # ArcadeDB snapshot DB
ARCADEDB_URL=http://localhost:2480
ARCADEDB_DB=hound
ARCADEDB_USER=root
ARCADEDB_PASS=playwithdata         # [REQUIRED in prod]
```

**D3. `services/heimdall-backend/.env.example`**
```
# HEIMDALL backend — Quarkus Admin API
QUARKUS_HTTP_PORT=9093
ARCADEDB_URL=http://localhost:2481    # uses frigg (snapshot DB)
ARCADEDB_DB=hound
ARCADEDB_USER=root
ARCADEDB_PASS=playwithdata           # [REQUIRED in prod]
DOCKER_HOST=unix:///var/run/docker.sock
```

**D4. `frontends/shell/.env.example`**
```
# Shell — Module Federation Host (Vite)
VITE_VERDANDI_REMOTE=http://localhost:15173/assets/remoteEntry.js
VITE_HEIMDALL_REMOTE=http://localhost:25174/assets/remoteEntry.js
```

---

### E. Очистка verdandi_net (Q18)

Сеть уже переименована в `aida_net` в обоих compose-файлах. Осталось почистить
упоминания `verdandi_net` в документации (устаревшие архитектурные описания).

Файлы с legacy-ссылками:
- `docs/architecture/MODULES_TECH_STACK.md`
- `docs/architecture/REFACTORING_PLAN.md`
- `docs/architecture/REPO_MIGRATION_PLAN.md`
- `docs/reviews/REVIEW_STRATEGY.md`
- `docs/reviews/REVIEW_TASKS_AND_PROMPTS.md`

Заменить все вхождения `verdandi_net` → `aida_net` с пометкой `(переименовано)` где нужен контекст.

---

### F. Dockerfile — проверка layer caching

Все 7 Dockerfile уже multi-stage. Проверить COPY-порядок для Node-сервисов
(зависимости должны копироваться до исходников — чтобы `npm ci` кешировался):

```dockerfile
# Правильный порядок (пример)
COPY package.json package-lock.json ./
RUN npm ci --omit=dev
COPY tsconfig.json ./
COPY src ./src
```

Сервисы для проверки: `bff/chur/Dockerfile`, `frontends/heimdall-frontend/Dockerfile`,
`frontends/shell/Dockerfile`, `frontends/verdandi/Dockerfile`.

Если порядок неверный — переставить COPY-команды (без изменения логики).

---

### G. FIX-B — INV-3: Array.isArray guard для JWT scope (✅ выполнено)

**Статус:** реализовано в рамках этого спринта, коммит отдельный.

**Проблема:** `bff/chur/src/keycloak.ts:141` — `scope` трактовался только как строка.
Keycloak в ряде конфигураций (token exchange, кастомные scope mappers) возвращает `scope`
как `string[]`. Вызов `.split(' ')` на массиве → `TypeError` в рантайме → INV-3 ❌.

**Файл:** `bff/chur/src/keycloak.ts`

```typescript
// было:
const jwtScopes = (payload as { scope?: string }).scope?.split(' ').filter(Boolean) ?? [];

// стало:
const rawScope = (payload as { scope?: string | string[] }).scope;
const jwtScopes = Array.isArray(rawScope)
  ? rawScope.filter(Boolean)
  : (rawScope?.split(' ').filter(Boolean) ?? []);
```

**Коммит:** отдельный от основного CI/CD PR — `fix(chur): Array.isArray guard for JWT scope (FIX-B / INV-3)`

**Верификация:** `npm test` в `bff/chur` — 21/21 ✅

---

### H. H3.8 — HoundHeimdallListener (конфигурация, не код)

**Статус:** код уже реализован (`libraries/hound/src/main/java/com/hound/api/`).
INV-6 (`atomsExtracted=0`) — gap конфигурации: `heimdall.url` не передаётся в JVM Dali.

**Что нужно:** добавить `-Dheimdall.url=http://heimdall-backend:9093/events`
в `JAVA_OPTS` / `quarkus.jvm.args` для сервиса `dali` в `docker-compose.yml`.

Откладывается на отдельный тикет после проверки HEIMDALL EventResource endpoint.

---

### I. Уровень 1 — базовая гигиена (обязательно до prod-деплоя)

#### I-1. `sourcemap: false` в prod-сборках Vite

Ни один из трёх Vite-конфигов не отключает source maps — JS бандл в образе
содержит читаемый исходный код.

Добавить в секцию `build:` каждого конфига:

```ts
build: {
  target: 'es2022',
  sourcemap: false,   // ← добавить
},
```

**Файлы:**
- `frontends/verdandi/vite.config.ts`
- `frontends/heimdall-frontend/vite.config.ts`
- `frontends/shell/vite.config.ts`

#### I-2. `.dockerignore` — создать отсутствующие, дополнить существующие

**Отсутствуют** (создать):

`services/dali/.dockerignore`, `frontends/heimdall-frontend/.dockerignore`,
`frontends/shell/.dockerignore` — минимальный шаблон:
```
node_modules/
dist/
.env*
*.log
*.key
*.pem
```

**Существующие — дополнить** (отсутствуют `.env*`, `*.key`, `*.pem`):

| Файл | Что добавить |
|------|-------------|
| `.dockerignore` (root) | `.env*`, `*.key`, `*.pem`, `*.p12` |
| `bff/chur/.dockerignore` | `*.key`, `*.pem`, `*.p12` (`.env*` уже есть) |
| `services/heimdall-backend/.dockerignore` | проверить наличие `.env*`, `*.key`, `*.pem` |
| `services/shuttle/.dockerignore` | то же |

#### I-3. Проверка: нет секретов в слоях образа

После сборки выполнить вручную (или в CI):
```bash
docker history <image> --no-trunc | grep -iE "(secret|password|key|token)"
```
Если что-то выводится — секрет попал в ARG-слой, необходимо убрать из `docker build`.

**Что проверить в Dockerfiles:**
- `frontends/heimdall-frontend/Dockerfile` использует `ARG VITE_HEIMDALL_API` — убедиться,
  что это не secret, а URL (ок); настоящие секреты не должны быть ARG.

---

### J. Уровень 2 — при появлении prod-сервера

#### J-1. Non-root USER во всех Dockerfile'ах

Ни один из 7 Dockerfile'ов не имеет директивы `USER`. Контейнеры запускаются как root.

Паттерн для Node-сервисов (добавить в runner-stage):
```dockerfile
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
```

Паттерн для JVM-сервисов (eclipse-temurin:21-jre-alpine):
```dockerfile
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser
```

**Файлы (все 7):**
`bff/chur/Dockerfile`, `frontends/heimdall-frontend/Dockerfile`,
`frontends/shell/Dockerfile`, `frontends/verdandi/Dockerfile`,
`services/dali/Dockerfile`, `services/heimdall-backend/Dockerfile`,
`services/shuttle/Dockerfile`

#### J-2. Image signing (cosign) — в CD pipeline

Добавить в `.github/workflows/cd.yml` после `docker/build-push-action`:
```yaml
- uses: sigstore/cosign-installer@v3
- run: cosign sign --yes ghcr.io/${{ github.repository_owner }}/verdandi/${{ matrix.service }}:${{ github.sha }}
```

Позволяет верифицировать что образ собран именно этим CI, а не подменён.

#### J-3. Read-only filesystem для stateless-сервисов

В `docker-compose.prod.yml` для сервисов без записи на диск:
```yaml
security_opt:
  - no-new-privileges:true
read_only: true
tmpfs:
  - /tmp
```

Применимо к: `shuttle`, `chur`, `verdandi`, `heimdall-frontend`, `shell`.
Не применять к: `houndarcade`, `frigg` (пишут данные), `heimdall-backend` (docker.sock).

---

---

### K. CI/CD — расширенные улучшения

| # | Проблема | Файл | Изменение |
|---|----------|------|-----------|
| K-1 | `fix/**` не в триггерах CI ❌ | `ci.yml` | добавить `'fix/**'` в push.branches |
| K-2 | Нет `permissions` в CI | `ci.yml` | `permissions: contents: read` на уровне workflow |
| K-3 | Нет `timeout-minutes` | `ci.yml` | 15 мин Gradle / 10 мин Node на каждый job |
| K-4 | Gradle без `--no-daemon --build-cache` | `ci.yml` | добавить флаги во все `./gradlew` шаги |
| K-5 | `hound` job — нет build-шага | `ci.yml` | добавить `:libraries:hound:build -x test` |
| K-6 | Нет job `shared/dali-models` | `ci.yml` | новый job `dali-models` |
| K-7 | Нет `workflow_dispatch` | `ci.yml` | добавить триггер |
| K-8 | CD `sleep 15` — хрупко | `cd.yml` | заменить на retry-loop (10×5s) |
| K-9 | Нет `npm audit` | `ci.yml` | добавить после `npm ci` (continue-on-error: true) |
| K-10 | CD matrix — 3 из 7 сервисов | `cd.yml` | отложено до J-1 (non-root USER) |

---

### L. Документация CI/CD

**Файл:** `docs/ci-cd/CICD_PIPELINE.md` (NEW)

Содержание: архитектурная схема пайплайна (mermaid), таблица CI jobs, инструкция по секретам,
гибридный dev-режим, уровни безопасности 1 и 2, rollback.

---

### M. Heimdall — временный просмотр `docs/`

**Backend:** `GET /docs` (дерево `.md`) + `GET /docs/{path}` (содержимое).
Volume mount: `./docs:/docs:ro` только в dev docker-compose.

**Frontend:** роут `/docs`, рендер Markdown (`react-markdown`), кнопка в sidebar с `[dev]`.

**Файлы:**
- `services/heimdall-backend/.../resource/DocsResource.java` (NEW)
- `frontends/heimdall-frontend/src/pages/DocsPage.tsx` (NEW)

---

### N. Deploy в Yandex Cloud

**Стек:** Compute VM + Docker Compose · GHCR primary + YCR mirror · Yandex Lockbox

| Задача | Файлы |
|--------|-------|
| N-1 Terraform (VM, VPC, SG, DNS, YCR, Object Storage, Lockbox) | `infra/terraform/main.tf`, `variables.tf`, `outputs.tf` |
| N-2 YCR mirror job + deploy из YCR | `.github/workflows/cd.yml` |
| N-3 Lockbox secrets + `lockbox-sync.sh` | `infra/scripts/lockbox-sync.sh` |
| N-4 VM bootstrap: `cloud-init.yml` + `nginx.conf` + certbot TLS | `infra/cloud-init.yml`, `infra/nginx/nginx.conf` |
| N-5 `docker-compose.yc.yml` (YCR images, resource limits, env_file, logging) | `docker-compose.yc.yml` |
| N-6 Backup ArcadeDB → Object Storage + cron | `infra/scripts/backup-arcadedb.sh` |
| N-7 Yandex Monitoring alerts (CPU >80%, disk >85%, RAM >90%) | cloud-init + YC console |
| N-8 Rollback YCR support | `scripts/rollback.sh` |

**GitHub Secrets (добавить вручную после terraform apply):**
`YC_SA_KEY`, `YC_REGISTRY_ID`, `YC_FOLDER_ID`, `DEPLOY_HOST`, `DEPLOY_USER`, `DEPLOY_KEY`

---

## Все файлы к изменению / созданию

| Файл | Действие |
|------|----------|
| `bff/chur/src/keycloak.ts` | ✅ DONE — FIX-B |
| `docker-compose.yml` | MOD — healthchecks (A) + docs volume (M) |
| `frontends/verdandi/vite.config.ts` | MOD — sourcemap: false (I-1) |
| `frontends/heimdall-frontend/vite.config.ts` | MOD — sourcemap: false (I-1) |
| `frontends/shell/vite.config.ts` | MOD — sourcemap: false (I-1) |
| `.dockerignore` (root) | MOD — .env*, *.key, *.pem (I-2) |
| `bff/chur/.dockerignore` | MOD — *.key, *.pem (I-2) |
| `services/dali/.dockerignore` | NEW (I-2) |
| `frontends/heimdall-frontend/.dockerignore` | NEW (I-2) |
| `frontends/shell/.dockerignore` | NEW (I-2) |
| `services/shuttle/.env.example` | NEW (D) |
| `services/dali/.env.example` | NEW (D) |
| `services/heimdall-backend/.env.example` | NEW (D) |
| `frontends/shell/.env.example` | NEW (D) |
| `.github/workflows/ci.yml` | MOD — K-1..K-9 + B/C |
| `.github/workflows/cd.yml` | MOD — K-8 retry + N-2 YCR mirror |
| `docs/architecture/MODULES_TECH_STACK.md` | MOD — verdandi_net→aida_net (E) |
| `docs/architecture/REFACTORING_PLAN.md` | MOD — verdandi_net→aida_net (E) |
| `docs/architecture/REPO_MIGRATION_PLAN.md` | MOD — verdandi_net→aida_net (E) |
| `docs/reviews/REVIEW_STRATEGY.md` | MOD — verdandi_net→aida_net (E) |
| `docs/reviews/REVIEW_TASKS_AND_PROMPTS.md` | MOD — verdandi_net→aida_net (E) |
| `docs/ci-cd/CICD_PIPELINE.md` | NEW (L) |
| `services/heimdall-backend/.../DocsResource.java` | NEW (M) |
| `frontends/heimdall-frontend/src/pages/DocsPage.tsx` | NEW (M) |
| `infra/terraform/main.tf` | NEW (N-1) |
| `infra/terraform/variables.tf` | NEW (N-1) |
| `infra/terraform/outputs.tf` | NEW (N-1) |
| `infra/cloud-init.yml` | NEW (N-4) |
| `infra/nginx/nginx.conf` | NEW (N-4) |
| `infra/scripts/lockbox-sync.sh` | NEW (N-3) |
| `infra/scripts/backup-arcadedb.sh` | NEW (N-6) |
| `docker-compose.yc.yml` | NEW (N-5) |
| `scripts/rollback.sh` | MOD (N-8) |
| все 7 Dockerfile | MOD — non-root USER (J-1, при сервере) |
| `docker-compose.prod.yml` | MOD — read_only (J-3, при сервере) |

---

## Порядок выполнения

```
── Готово ─────────────────────────────────────────────────
✅ Branch feature/cicd-docker-polish-apr15
✅ G — FIX-B keycloak.ts Array.isArray

── Блок 1: Docker hygiene ─────────────────────────────────
F   Dockerfile COPY-порядок audit + fix
I-1 sourcemap: false (3 vite.config.ts)
I-2 .dockerignore — создать/дополнить (5 файлов)

── Блок 2: Dev stack ──────────────────────────────────────
A   Healthchecks в docker-compose.yml (5 сервисов)
D   .env.example для 4 сервисов

── Блок 3: CI overhaul ────────────────────────────────────
K-1..K-7  triggers, permissions, timeouts, --no-daemon,
          hound build, dali-models job, workflow_dispatch
B/C       verdandi fix, shell job, dali job, chur tsc
K-9       npm audit в Node jobs

── Блок 4: CD ─────────────────────────────────────────────
K-8  retry health-check loop
N-2  mirror-to-ycr job + deploy из YCR
N-8  rollback.sh YCR support

── Блок 5: Yandex Cloud ───────────────────────────────────
N-1  Terraform
N-3  Lockbox + lockbox-sync.sh
N-4  cloud-init.yml + nginx.conf + certbot
N-5  docker-compose.yc.yml
N-6  backup-arcadedb.sh + cron
N-7  Yandex Monitoring alerts

── Блок 6: Документация ───────────────────────────────────
L   docs/ci-cd/CICD_PIPELINE.md
E   verdandi_net cleanup в docs

── Блок 7: Heimdall docs viewer ───────────────────────────
M   DocsResource.java + DocsPage.tsx

── При появлении сервера ──────────────────────────────────
J-1 non-root USER (все Dockerfile)
J-2 cosign в cd.yml
J-3 read_only в docker-compose.prod.yml
K-10 CD matrix expansion
```

---

## Верификация

- `docker compose config` — валидность compose
- Push в `fix/*` → CI триггерится (K-1)
- CI зелёные: `shell`, `dali`, `dali-models`, `verdandi` с tsc
- `permissions: contents: read` виден в GitHub Actions summary
- `npm audit` виден в логах Node-jobs
- `terraform plan` чистый (N-1)
- Grep `verdandi_net` → 0 совпадений (E)
- `npm test` bff/chur → 21/21 ✅
