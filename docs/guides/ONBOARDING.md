# AIDA — Onboarding Guide

**Документ:** `ONBOARDING`
**Версия:** 1.0
**Дата:** 16.04.2026
**Статус:** ACTIVE
**Аудитория:** Новый разработчик, DevOps

---

## 1. Что такое AIDA

**AIDA** (AI-Driven Impact Analysis) — система автоматического column lineage для imperative PL/SQL.

Отвечает на вопрос: **«Если я изменю колонку `orders.status` — что сломается?»**

### Ключевые числа

| Метрика | Значение |
|---------|----------|
| Resolution rate | 98.8% |
| Корпус тестирования | 203 PL/SQL файла, ~70K LOC |
| Атомов в графе | 143 000 |
| Время полного парсинга | 261 сек (4.4 мин) |

---

## 2. Архитектура (один взгляд)

```
VERDANDI ──► Chur (BFF) ──► SHUTTLE (GraphQL) ──► DALI (parse) ──► YGG (ArcadeDB)
                                                         │
                                              HEIMDALL (observability)
                                                         │
                                              FRIGG (ArcadeDB, snapshots)
```

---

## 3. Сервисы и порты

| Сервис | Порт (локально) | Технология | Назначение |
|--------|-----------------|------------|------------|
| **verdandi** | http://localhost:15173 | React + Vite | Главный UI (LOOM 5-level граф) |
| **Shell** | http://localhost:25175 | Vite Module Federation | MFE-хост |
| **heimdall-frontend** | http://localhost:25174 | React + Vite | Admin / observability UI |
| **Chur** | http://localhost:13000 | Fastify + Node 24 | BFF: auth (Keycloak JWT), proxy к SHUTTLE |
| **SHUTTLE** | http://localhost:18080 | Quarkus + Java 21 | GraphQL API + REST-клиенты |
| **DALI** | http://localhost:19090 | Quarkus + Java 21 | Parse orchestrator (JobRunr + Hound) |
| **HEIMDALL backend** | http://localhost:19093 | Quarkus + Java 21 | SSE event stream, metrics ring buffer |
| **Keycloak** | http://localhost:18180 | Keycloak 24 | IAM (realm: `seer`, client: `aida-bff`) |
| **YGG (HoundArcade)** | http://localhost:2480 | ArcadeDB 26.3.2 | Lineage-граф (БД `hound`) |
| **FRIGG** | http://localhost:2481 | ArcadeDB 26.3.2 | JobRunr state + session snapshots |

Полный список сервисов: `docker-compose.yml` в корне репо.

---

## 4. Быстрый старт

### 4.1 Предварительные условия

```bash
# Минимальные требования:
docker --version   # >= 24
docker compose version  # >= 2.20
java --version     # 21 (для локальной разработки без Docker)
node --version     # >= 20 (frontend)
```

### 4.2 Запуск всего стека

```bash
git clone https://github.com/NooriUta/AIDA.git
cd aida-root

# Запустить все сервисы
docker compose up -d

# Проверить статус (все должны быть healthy, не просто running)
docker compose ps --format "table {{.Name}}\t{{.Status}}"
```

**⚠️ Порядок важен!** FRIGG должен стать `healthy` до Dali. Если Dali стартовал раньше — см. §6.3.

### 4.3 Проверка готовности

```bash
# Dali API health
curl http://localhost:19090/api/sessions/health
# → {"frigg":"ok","ygg":"ok","sessions":0}

# SHUTTLE GraphQL UI
open http://localhost:18080/q/graphql-ui

# VERDANDI (граф открывается)
open http://localhost:15173

# HEIMDALL admin
open http://localhost:25174
```

### 4.4 Запуск первой сессии парсинга

```bash
# Запустить preview-парсинг (не пишет в YGG)
curl -X POST http://localhost:19090/api/sessions \
  -H "Content-Type: application/json" \
  -d '{"dialect":"PLSQL","source":"/opt/sql/corpus","preview":true}'
# → {"id":"<uuid>","status":"QUEUED",...}

# Отслеживать прогресс
watch -n 3 'curl -s http://localhost:19090/api/sessions/<uuid> | python3 -m json.tool'
```

---

## 5. Структура репозитория

```
aida-root/
├── libraries/
│   └── hound/              # ANTLR4 PL/SQL → YGG parser (Java library)
├── shared/
│   └── dali-models/        # Shared data models (Java records)
├── services/
│   ├── dali/               # Parse orchestrator (Quarkus)
│   ├── shuttle/            # GraphQL API (Quarkus)
│   └── heimdall-backend/   # Observability backend (Quarkus)
├── bff/
│   └── chur/               # BFF / auth gateway (Fastify)
├── frontends/
│   ├── verdandi/           # Main SPA (React + Vite + React Flow)
│   ├── heimdall-frontend/  # Admin UI (React + Vite)
│   └── shell/              # MFE host (Vite Module Federation)
├── docs/                   # Документация
├── docker-compose.yml
└── settings.gradle
```

---

## 6. Часто встречаемые проблемы

### 6.1 YGG не отвечает (`Connection refused: localhost:2480`)

```bash
docker compose logs houndarcade --tail=30
docker compose up -d houndarcade
# Подождать healthy:
docker compose ps houndarcade
```

### 6.2 Dali не видит FRIGG (`FRIGG connection error`)

Убедиться, что FRIGG healthy ДО старта Dali:
```bash
docker compose up -d frigg
# Подождать healthy (30-60 сек)
docker compose ps frigg
# Только потом:
docker compose up -d dali
```

### 6.3 Dali завис в `schemaReady deadlock`

Симптом: Dali стартовал до FRIGG, logs показывают `Waiting for schema...` бесконечно.  
**Не помогает** `docker compose restart dali`. Нужно:
```bash
docker compose rm -f dali
docker compose up -d dali
```

### 6.4 Отсутствуют edge-типы Sprint 2 (`HAS_RECORD_FIELD`, `RETURNS_INTO`, `DaliDDLModifiesTable`, `DaliDDLModifiesColumn`)

Симптом: L3/L4 LOOM не отображает записи (%ROWTYPE) и DDL-связи.  
Запустить Фикс-ET из `docs/guides/STARTUP_SEQUENCE.md §3.1`.

### 6.5 verdandi `npm run build` падает с TypeScript-ошибками

```bash
cd frontends/verdandi
npm ci
npx tsc --noEmit   # посмотреть ошибки
npm run build
```

Если ошибки в generated типах — пересгенерить GraphQL types:
```bash
npm run codegen
```

---

## 7. Разработка без Docker

### Backend (Quarkus dev mode)

```bash
# Запустить только инфраструктуру
docker compose up -d frigg houndarcade keycloak

# Dev mode с hot reload
cd services/dali
./gradlew :services:dali:quarkusDev

# В отдельном терминале
cd services/shuttle
./gradlew :services:shuttle:quarkusDev
```

### Frontend (Vite dev mode)

```bash
cd frontends/verdandi
npm ci
npm run dev
# → http://localhost:15173

cd frontends/heimdall-frontend
npm ci
npm run dev
# → http://localhost:25174
```

---

## 8. CI/CD

Конфигурация: `.github/workflows/ci.yml`

| Job | Что проверяет |
|-----|---------------|
| `hound` | ANTLR library: test + build |
| `dali-models` | Shared models: build |
| `shuttle` | GraphQL API: test + build |
| `dali` | Parse service: test + build (+ ArcadeDB service container) |
| `chur` | BFF: typecheck + test + build |
| `verdandi` | Frontend: lint + test + coverage + build |
| `shell` | MFE host: typecheck + build |
| `heimdall-backend` | Observability: test + build |
| `heimdall-frontend` | Admin UI: test + typecheck + build |

**Правило:** Merge только при зелёном CI. `--admin` bypass — запрещён.

---

## 9. Полезные ссылки

| Ресурс | URL |
|--------|-----|
| Архитектура (модули) | `docs/architecture/MODULES_TECH_STACK.md` |
| GraphQL schema | `services/shuttle/src/main/resources/META-INF/graphql/` |
| YGG schema | `docs/architecture/YGG_SCHEMA_REFERENCE.md` |
| Startup guide | `docs/guides/STARTUP_SEQUENCE.md` |
| Demo script | `docs/guides/DEMO_SCRIPT.md` |
| QG checklist | `docs/quality-gates/QG-DALI-ygg-write.md` |
| Sprint backlog | `docs/plans/REFACTORING_REMAINING.md` |
| LOOM spec | `docs/architecture/loom/LOOM_5LEVEL_SPRINT2_SPEC.md` |

---

## История изменений

| Дата | Версия | Что |
|------|--------|-----|
| 16.04.2026 | 1.0 | Создан: порты, быстрый старт, troubleshooting, структура репо, CI. |
