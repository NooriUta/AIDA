# AIDA — Review Strategy (Scheduled Architectural Reviews)

**Документ:** `REVIEW_STRATEGY`
**Версия:** 1.0
**Дата:** 12.04.2026
**Применимость:** `aida-root/` monorepo после завершения REPO_MIGRATION_PLAN

---

## 0. Проблемы текущего подхода

Текущий review (Sprint 7→8, 12.04.2026) выявил структурные проблемы, которые нужно исправить в новой стратегии:

| Проблема | Следствие | Решение |
|---|---|---|
| Review запускался на `brandbook/seer-studio`, а не на `main` | `internal_docs/` пусты, ROADMAP отсутствует, ветка не репрезентативна | **Всегда запускать с `main`** |
| Нет `node`/`npx` в среде Claude Code → TypeScript/build не проверяется | Ошибки сборки пропускаются | **Читать CI artifacts из `.github/` + build logs** |
| Покрывал только VERDANDI (один модуль) | Hound, SHUTTLE, Chur выпадают | **Per-module review templates** |
| Нет кросс-модульных проверок | API drift между модулями не виден | **L3 integration review** |
| Метрики не сохраняются между сессиями | Нет истории тренда | **Metrics log в `docs/reviews/`** |

---

## 1. Три уровня review

```
L1 — Daily Quick Scan         ~15 мин   CI статус + critical issues
L2 — Weekly Per-Module        ~45 мин   Один модуль, глубоко
L3 — Sprint-End Full Review   ~2 часа   Все модули + кросс-модульная интеграция
```

**Выбор уровня определяет prompt template (§4).**

---

## 2. Модули и их типы

После миграции в `aida-root/` — четыре активных модуля, остальные появятся позже:

| Модуль | Путь | Тип | Язык | Reviewer profile |
|---|---|---|---|---|
| **hound** | `libraries/hound/` | Java library | Java 21 + ANTLR | A — Data/Algorithms |
| **shuttle** | `services/shuttle/` | Quarkus service | Java 21 + Quarkus | B — UI/Orchestration |
| **chur** | `bff/chur/` | Node.js BFF | TypeScript + Fastify | B |
| **verdandi** | `frontends/verdandi/` | React SPA | TypeScript + React 19 | B |
| *dali* | `services/dali/` | Quarkus service | Java 21 + Quarkus | A (будущий) |
| *heimdall-backend* | `services/heimdall-backend/` | Quarkus service | Java 21 + Quarkus | B (будущий) |
| *heimdall-frontend* | `frontends/heimdall-frontend/` | React SPA | TypeScript + React | B (будущий) |

---

## 3. Расписание

```
Понедельник    L1 Daily Quick Scan (все модули)
Вторник        L2 Weekly: libraries/hound/
Среда          L2 Weekly: services/shuttle/ + bff/chur/
Четверг        L2 Weekly: frontends/verdandi/
Пятница        L1 Daily Quick Scan
Конец спринта  L3 Sprint-End Full Review (все модули + интеграция)
```

**Правило ветки:** L1 и L2 запускаются только с ветки `main`. Если ревью запрашивается на feature-ветке — добавить в отчёт предупреждение «⚠️ НЕ main — результаты неполны».

---

## 4. Prompt Templates для Claude Code

### 4.1 L1 — Daily Quick Scan

```
Запусти L1 Daily Quick Scan для aida-root/ по REVIEW_STRATEGY.md.

Ветка: main (проверить git branch, если другая — добавить ⚠️ WARNING)
Дата: [DATE]

Для каждого активного модуля (hound, shuttle, chur, verdandi) проверь:
1. Последний коммит в этом модуле (git log --oneline -3 -- <path>)
2. Наличие новых файлов с TODO/FIXME/HACK (grep -r "TODO\|FIXME\|HACK" <path>/src --include="*.java" --include="*.ts" --include="*.tsx" -l)
3. Нет ли новых файлов >500 LOC (find <path>/src -name "*.java" -o -name "*.ts" -o -name "*.tsx" | xargs wc -l | sort -rn | head -5)
4. CI artifacts: прочитать .github/workflows/ — есть ли failing jobs?

Формат вывода: таблица модуль × [last commit | new TODOs | large files | CI status]
Если нет нарушений — одна строка "✅ L1 PASS [DATE]"
Если есть — список с приоритетами 🔴/🟡/🟢
Сохранить запись в docs/reviews/L1_LOG.md (append)
```

---

### 4.2 L2 — Weekly Per-Module

#### L2-A: `libraries/hound/` (Java library)

```
Запусти L2 Weekly Review для libraries/hound/ по REVIEW_STRATEGY.md §5.A.

Ветка: main
Дата: [DATE]
Предыдущий review: [PREV_DATE или "нет"]

Разделы:
1. MODULE HEALTH — прочитать src/main/java/, src/test/java/
   - Количество test-файлов и соотношение test/prod LOC
   - Новые публичные методы без JavaDoc
   - Методы >100 строк (find . -name "*.java" | xargs grep -n "public " | ...)

2. API STABILITY — критично (hound = library, импортируется Dali)
   - Изменения в публичных интерфейсах (HoundParser, HoundConfig, HoundEventListener)
   - Breaking changes с последнего review
   - Q27 status: HoundConfig full schema — зафиксирован или ещё TBD?

3. BUG TRACKER — статус фиксов из HOUND_CODE_REVIEW.md
   - C.1.0: B1/B2 NPE в ensureTable — закрыт?
   - C.1.0: B3 NPE в ArcadeDBSemanticWriter — закрыт?
   - C.1.0: B4 бесконечная рекурсия — закрыт?
   - C.1.6: O(A×T)→O(A) в NameResolver — закрыт?
   - C.1.6: O(N²)→O(N) в addChildStatement — закрыт?

4. PERFORMANCE REGRESSION
   - Проверить BENCH_REPORT_*.md (docs/hound/) — есть новые замеры?
   - REMOTE_BATCH speedup всё ещё 13.1×?

5. DEPENDENCY DRIFT
   - build.gradle: версии ArcadeDB, ANTLR — изменились?
   - Соответствие MODULES_TECH_STACK.md §3.1

Формат: §§1-5 в структурированном отчёте. Сохранить в docs/reviews/hound/REVIEW_[DATE].md
```

---

#### L2-B: `services/shuttle/` (Quarkus GraphQL)

```
Запусти L2 Weekly Review для services/shuttle/ по REVIEW_STRATEGY.md §5.B.

Ветка: main
Дата: [DATE]

Разделы:
1. SQL INJECTION WATCH — критический риск
   - grep -rn "String.format\|+.*sql\|concat.*sql\|\"SELECT.*\"+\|\"UPDATE.*\"+" src/
   - Статус C.2.1 fix (SearchService.java:86-87) — применён?
   - Параметризованные запросы везде?

2. GRAPHQL SCHEMA HEALTH
   - Прочитать schema.graphql или GraphQL-аннотации
   - Новые Query/Mutation/Subscription добавлены?
   - C.2.2 status: Mutation resolvers (startParseSession, askMimir, saveView) — реализованы?
   - C.2.3 status: Subscription resolvers (heimdallEvents, sessionProgress) — реализованы?

3. REST CLIENTS STATUS
   - DaliClient — существует в services/shuttle/src/?
   - MimirClient — существует?
   - HeimdallClient — существует?
   (C.2.4, C.2.5, C.2.6 из REFACTORING_PLAN)

4. TEST COVERAGE
   - Количество test-файлов vs prod-файлов
   - SearchServiceTest, ExploreServiceTest, LineageServiceTest, KnotServiceTest — все есть?

5. DEPENDENCY DRIFT
   - build.gradle: Quarkus версия — совпадает с MODULES_TECH_STACK.md (3.34.2)?
   - pom.xml удалён? (D6 из REPO_MIGRATION_PLAN)

Сохранить в docs/reviews/shuttle/REVIEW_[DATE].md
```

---

#### L2-C: `bff/chur/` (TypeScript BFF)

```
Запусти L2 Weekly Review для bff/chur/ по REVIEW_STRATEGY.md §5.C.

Ветка: main
Дата: [DATE]

Разделы:
1. SECURITY CRITICAL
   - CORS: grep "cors\|allowedOrigins\|\*" src/server.ts — нет wildcard?
   - Rate limiting: grep "rateLimit\|rate-limit" src/ — на всех auth endpoints?
   - JWT: grep "verify\|jose" src/ — проверка подписи везде где нужна?
   - Keycloak client: verdandi-bff или aida-bff?
     (C.5.2 — rename pending → статус?)

2. SCOPE CHECKS STATUS
   - requireScope('aida:admin') — реализован?
   - requireScope('aida:admin:destructive') — реализован?
   - /heimdall/* proxy routes — существуют?
   (C.3.1, C.3.2 из REFACTORING_PLAN)

3. WEBSOCKET STATUS
   - @fastify/websocket — установлен в package.json?
   - /graphql WebSocket upgrade — реализован?
   (C.3.4 из REFACTORING_PLAN)

4. TEST COVERAGE
   - rbac.test.ts — существует?
   - auth.test.ts — существует?
   - Новые тест-файлы с последнего review?

5. DEPENDENCY HEALTH
   - package.json: проверить jose, fastify, @fastify/rate-limit версии
   - Нет @fastify/jwt (должен быть удалён в пользу jose)?

Сохранить в docs/reviews/chur/REVIEW_[DATE].md
```

---

#### L2-D: `frontends/verdandi/` (React SPA)

```
Запусти L2 Weekly Review для frontends/verdandi/ по REVIEW_STRATEGY.md §5.D.

Ветка: main
Дата: [DATE]

Разделы:
1. COMPONENT SIZE WATCH (цели: компоненты ≤350 LOC, hooks ≤200 LOC)
   find src -name "*.tsx" -o -name "*.ts" | xargs wc -l | sort -rn | head -10
   Статус:
   - LoomCanvas.tsx: текущий LOC (цель ≤350, сейчас 403)
   - SearchPanel.tsx: текущий LOC (WARN-05: 516 LOC, растёт)
   - loomStore.ts: текущий LOC (✅ 251 — сохраняем)

2. TEST COVERAGE
   - Количество test-файлов: find src -name "*.test.*" | wc -l
   - Пробелы из Sprint 7: useGraphData.test.ts, useDisplayGraph.test.ts, useLoomLayout.test.ts — появились?
   - e2e/smoke.spec.ts — существует?

3. ELK WORKER STATUS (WARN-01)
   - layoutGraph.ts — ELK на main thread или Worker?
   - elkWorker.ts — интегрирован в production?
   - Если Vite 7+ — Worker support доступен?

4. NEW VIEWS STATUS (C.4.x из REFACTORING_PLAN)
   - C.4.1 WebSocket client (graphql-ws) — в package.json?
   - C.4.2 ANVIL UI view — route /anvil/:nodeId существует?
   - C.4.3 MIMIR Chat view — компонент существует?

5. ADR COMPLIANCE (быстрая проверка)
   - @xyflow/react: используется в LoomCanvas?
   - graphql-request: в package.json? (не Apollo/urql)
   - nodesDraggable=false: в LoomCanvas?
   - loomStore слайсы: все 10 на месте?

6. TECH DEBT РЕЕСТР — обновить статусы:
   - TD-09 FilterToolbar дублирование — статус?
   - TD-11 Hardcoded LIMIT без hasMore — статус?
   - WARN-05 SearchPanel 516 LOC — изменился?
   - WARN-06 LoomCanvas 403 LOC — изменился?

Сохранить в docs/reviews/verdandi/REVIEW_[DATE].md
```

---

### 4.3 L3 — Sprint-End Full Review

```
Запусти L3 Sprint-End Full Review для aida-root/ по REVIEW_STRATEGY.md §6.

Ветка: main
Sprint: [SPRINT_NUMBER]
Дата: [DATE]
Предыдущий L3: [PREV_DATE]

ЧАСТЬ 1: Per-module summary (прогони L2 для каждого модуля, summary формат)

ЧАСТЬ 2: Cross-module integration check
  2.1 API contracts
      - Проверить services/shuttle/src/ на наличие DaliClient, MimirClient, HeimdallClient
      - Проверить shared/dali-models/ — HeimdallEvent, HoundConfig, ParseSessionInput совпадают между модулями?
      - Нет ли дублирования моделей между hound и shuttle?

  2.2 Integration matrix drift
      - Прочитать INTEGRATIONS_MATRIX.md — статусы I1-I35
      - Какие интеграции изменились с последнего review?
      - Какие новые интеграции появились (не в матрице)?

  2.3 Docker Compose consistency
      - docker-compose.yml: все активные сервисы объявлены?
      - Порты соответствуют MODULES_TECH_STACK.md §3.15?
      - Network aida_net везде (не verdandi_net)?
      - Новые сервисы (dali, heimdall-backend) — добавлены или нет?

ЧАСТЬ 3: Architecture drift
  3.1 Прочитать DECISIONS_LOG.md — открытые вопросы с дедлайнами:
      - Q3 HEIMDALL backend deployment: срок mid-May — решён?
      - Q5 Co-founder split: срок эта неделя — решён?
      - Q6 HEIMDALL event schema: срок mid-May — решён?
      - Q7 HEIMDALL ↔ Dali control API: срок end of April — решён?
      - Q25 Event bus transport: срок mid-May — решён?

  3.2 Critical path status (из PROJECT_ROADMAP.md):
      - Текущая неделя vs milestone — опережаем или отстаём?
      - Какие задачи на critical path?

ЧАСТЬ 4: Demo safety radar
  - make demo-reset работает? (если команда уже существует)
  - Backup laptop sync: когда последний раз?
  - Demo dataset готов? (anonymized 500K LoC)

ЧАСТЬ 5: Метрики по всем модулям (таблица)

Сохранить в docs/reviews/SPRINT_[N]_FULL_REVIEW.md
```

---

## 5. Metrics Template — стандартный формат

Каждый review сохраняет строку в `docs/reviews/METRICS_LOG.md`:

```markdown
| DATE | SPRINT | MODULE | TEST_FILES | PROD_FILES | RATIO | MAX_FILE_LOC | OPEN_BUGS | STATUS |
|------|--------|--------|------------|------------|-------|--------------|-----------|--------|
| 12.04 | S7 | verdandi | 23 | ~80 | ~0.29 | SearchPanel:516 | WARN-01,05,06 | 🟡 |
| 12.04 | S7 | shuttle | 4 | ~30 | ~0.13 | — | C.2.1 pending | 🔴 |
| 12.04 | S7 | chur | 2 | ~10 | ~0.20 | — | C.3.1-C.3.4 pending | 🟡 |
| 12.04 | S7 | hound | 24 | ~50 | ~0.48 | — | C.1.0 B1-B7 open | 🔴 |
```

**Целевые метрики:**

| Метрика | Hound | SHUTTLE | Chur | verdandi |
|---|---|---|---|---|
| Test/prod file ratio | ≥ 0.5 | ≥ 0.3 | ≥ 0.3 | ≥ 0.3 |
| Max file LOC (prod) | ≤500 | ≤400 | ≤300 | ≤350 |
| Open critical bugs | 0 | 0 | 0 | 0 |
| SQL injection checks | N/A | 0 instances | N/A | N/A |
| ADR compliance | 100% | 100% | 100% | 100% |

---

## 6. Структура директорий review output

```
aida-root/docs/reviews/
├── METRICS_LOG.md              ← единый трекер метрик, append-only
├── L1_LOG.md                   ← daily quick scan log, append-only
│
├── hound/
│   └── REVIEW_2026-04-12.md
│
├── shuttle/
│   └── REVIEW_2026-04-12.md
│
├── chur/
│   └── REVIEW_2026-04-12.md
│
├── verdandi/
│   └── REVIEW_2026-04-12.md
│
└── sprint/
    └── SPRINT_07_FULL_REVIEW.md    ← из документа, которым открыта эта сессия
```

---

## 7. Правила для Claude Code

### 7.1 Обязательные проверки перед запуском любого review

```bash
# 1. Проверить ветку
git branch --show-current
# Если НЕ main → добавить WARNING в начало отчёта

# 2. Проверить что aida-root/ существует
# (до миграции — предупредить: "монорепо ещё не мигрировано")

# 3. Найти предыдущий review этого уровня
ls docs/reviews/[module]/ | sort | tail -3
```

### 7.2 Что делать если модуль ещё не мигрирован

Если `libraries/hound/` не существует, но `Dali4/HOUND/Hound/` существует — запустить review по старому пути, добавить в начало:
> ⚠️ Модуль ещё не мигрирован в aida-root/. Review по временному пути. После REPO_MIGRATION_PLAN Phase 2 — использовать libraries/hound/.

### 7.3 Что НЕ делать

- Не запускать `npm install`, `./gradlew build` (среда Claude Code не имеет зависимостей)
- Не читать `node_modules/`, `build/`, `.gradle/` директории
- Не сравнивать файлы за пределами `aida-root/` (старые пути — только если модуль ещё не мигрирован)
- Не открывать >10 файлов за один L1 review

### 7.4 Приоритизация находок

| Уровень | Критерий | Действие |
|---|---|---|
| 🔴 CRITICAL | NPE / SQL injection / security bug / breaking API change | Заблокировать merge, исправить немедленно |
| 🟠 HIGH | Performance regression / open critical bug / missing test для нового кода | Добавить в ближайший спринт как must-have |
| 🟡 MEDIUM | LOC > target / tech debt / ADR drift | Трекать в METRICS_LOG, планировать в следующем спринте |
| 🟢 LOW | Style / docs / nice-to-have | Заметить, не блокировать |

---

## 8. Контекст для первого review после миграции

Когда REPO_MIGRATION_PLAN завершён и `aida-root/` создан — первый L3 должен зафиксировать baseline:

```
Запусти L3 POST-MIGRATION BASELINE REVIEW для aida-root/.

Это первый review после миграции. Задача — зафиксировать состояние, не найти проблемы.

1. Verification checklist из REPO_MIGRATION_PLAN §6 — все пункты проверить
2. Per-module baseline: test files count, prod files count, max LOC
3. Подтвердить что нет новых issues (только перенесённые)
4. Зафиксировать baseline в METRICS_LOG.md как "Sprint 0 / post-migration"
5. Создать docs/reviews/sprint/SPRINT_00_MIGRATION_BASELINE.md
```

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 12.04.2026 | 1.0 | Initial. Три уровня L1/L2/L3. Per-module templates (hound, shuttle, chur, verdandi). Правила для Claude Code (branch check, migration state). Metrics template. Directory structure. Baseline review template. |
