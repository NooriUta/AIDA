# AIDA — Review Tasks & Prompts

**Документ:** `REVIEW_TASKS_AND_PROMPTS`
**Версия:** 1.0
**Дата:** 12.04.2026
**Назначение:** Copy-paste промты для Claude Code scheduled reviews

> Все промты рассчитаны на `aida-root/` после миграции.
> До миграции — заменить пути согласно сноскам ⚠️ в каждом промте.

---

## Сводная таблица задач

| # | Задача | Уровень | Триггер | Модуль | Время |
|---|---|---|---|---|---|
| T-01 | Инициализация review-инфраструктуры | Setup | Однократно, сразу после миграции | все | ~30 мин |
| T-02 | L1 Daily Quick Scan | L1 | Пн + Пт, 09:00 | все | ~15 мин |
| T-03 | L2 Weekly — Hound | L2 | Вторник | hound | ~45 мин |
| T-04 | L2 Weekly — SHUTTLE | L2 | Среда | shuttle | ~30 мин |
| T-05 | L2 Weekly — Chur | L2 | Среда (после T-04) | chur | ~20 мин |
| T-06 | L2 Weekly — verdandi | L2 | Четверг | verdandi | ~45 мин |
| T-07 | L3 Post-Migration Baseline | L3 | Однократно, сразу после T-01 | все | ~90 мин |
| T-08 | L3 Sprint-End Full Review | L3 | Пятница последней недели спринта | все | ~2 часа |
| T-09 | L2 Weekly — Dali (будущий) | L2 | Вторник (когда появится) | dali | ~30 мин |
| T-10 | L2 Weekly — HEIMDALL backend (будущий) | L2 | Вторник (когда появится) | heimdall-backend | ~30 мин |
| T-11 | L2 Weekly — HEIMDALL frontend (будущий) | L2 | Четверг (когда появится) | heimdall-frontend | ~30 мин |

---

## T-01 — Инициализация review-инфраструктуры

**Триггер:** однократно, сразу после завершения REPO_MIGRATION_PLAN
**Что создаёт:** структуру директорий + baseline METRICS_LOG + L1_LOG

---

```
Ты — Claude Code, запускаешься в контексте aida-root/.

Задача: инициализировать review-инфраструктуру по REVIEW_STRATEGY.md.

Шаг 1 — Создать директории:
  docs/reviews/
  docs/reviews/hound/
  docs/reviews/shuttle/
  docs/reviews/chur/
  docs/reviews/verdandi/
  docs/reviews/sprint/

Шаг 2 — Создать docs/reviews/METRICS_LOG.md со следующим содержимым:
  Заголовок: "# AIDA — Review Metrics Log"
  Описание: "Append-only лог метрик. Одна строка на модуль на review."
  Таблица с колонками: DATE | SPRINT | MODULE | TEST_FILES | PROD_FILES | RATIO | MAX_FILE_LOC | OPEN_BUGS | STATUS
  Первые строки — baseline Sprint 7 (12.04.2026):
    | 12.04 | S7 | verdandi | 23 | ~80 | ~0.29 | SearchPanel:516 | WARN-01,05,06 | 🟡 |
    | 12.04 | S7 | shuttle  | 4  | ~30 | ~0.13 | —               | C.2.1 pending | 🔴 |
    | 12.04 | S7 | chur     | 2  | ~10 | ~0.20 | —               | C.3.1-C.3.4   | 🟡 |
    | 12.04 | S7 | hound    | 24 | ~50 | ~0.48 | —               | C.1.0 B1-B7   | 🔴 |

Шаг 3 — Создать docs/reviews/L1_LOG.md со следующим содержимым:
  Заголовок: "# AIDA — L1 Daily Quick Scan Log"
  Описание: "Append-only. Формат: [DATE] STATUS | Детали если есть нарушения"
  Первая запись: "| 12.04.2026 | BASELINE | Инициализация. Sprint 7 baseline зафиксирован в METRICS_LOG.md |"

Шаг 4 — Создать docs/reviews/sprint/SPRINT_07_VERDANDI_REVIEW.md
  Скопировать содержимое из SPRINT_07_VERDANDI_REVIEW.md (если существует в корне)
  Если не существует — создать заглушку с датой 12.04.2026 и пометкой "перенесён из brandbook/seer-studio branch"

Проверить: все директории и файлы созданы, таблица METRICS_LOG правильно заполнена.
Вывести список созданных файлов.
```

---

## T-02 — L1 Daily Quick Scan

**Триггер:** понедельник и пятница, 09:00
**Что проверяет:** последние коммиты, TODO/FIXME, large files, CI status — по всем модулям
**Выход:** одна строка в L1_LOG.md, либо список нарушений

---

```
Ты — Claude Code, запускаешься в контексте aida-root/.
Задача: L1 Daily Quick Scan по REVIEW_STRATEGY.md.
Дата запуска: [YYYY-MM-DD]

ШАГ 0 — ОБЯЗАТЕЛЬНО: проверь текущую ветку
  Выполни: git branch --show-current
  Если НЕ main — добавь в начало отчёта:
  "⚠️ WARNING: review запущен на ветке [BRANCH], а не main. Результаты неполны."

ШАГ 1 — Для каждого модуля выполни следующие проверки:

  МОДУЛЬ: libraries/hound/
  ⚠️ До миграции: Dali4/HOUND/Hound/
  1a. git log --oneline -3 -- libraries/hound/
  1b. grep -rl "TODO\|FIXME\|HACK" libraries/hound/src/main/java/ --include="*.java" 2>/dev/null | wc -l
  1c. find libraries/hound/src/main/java/ -name "*.java" | xargs wc -l 2>/dev/null | sort -rn | head -3

  МОДУЛЬ: services/shuttle/
  ⚠️ До миграции: SEERStudio/VERDANDI/SHUTTLE/
  2a. git log --oneline -3 -- services/shuttle/
  2b. grep -rl "TODO\|FIXME\|HACK\|String\.format.*sql\|String\.format.*SQL" services/shuttle/src/ --include="*.java" 2>/dev/null | wc -l
  2c. find services/shuttle/src/main/java/ -name "*.java" | xargs wc -l 2>/dev/null | sort -rn | head -3

  МОДУЛЬ: bff/chur/
  ⚠️ До миграции: SEERStudio/VERDANDI/Chur/
  3a. git log --oneline -3 -- bff/chur/
  3b. grep -rl "TODO\|FIXME\|HACK" bff/chur/src/ --include="*.ts" 2>/dev/null | wc -l
  3c. find bff/chur/src/ -name "*.ts" | xargs wc -l 2>/dev/null | sort -rn | head -3

  МОДУЛЬ: frontends/verdandi/
  ⚠️ До миграции: SEERStudio/VERDANDI/verdandi/
  4a. git log --oneline -3 -- frontends/verdandi/
  4b. grep -rl "TODO\|FIXME\|HACK" frontends/verdandi/src/ --include="*.ts" --include="*.tsx" 2>/dev/null | wc -l
  4c. find frontends/verdandi/src/ -name "*.tsx" -o -name "*.ts" | xargs wc -l 2>/dev/null | sort -rn | head -5

ШАГ 2 — CI Status
  Прочитать .github/workflows/ci.yml
  Есть ли jobs с известными проблемами (disabled, TODO, deprecated)?
  Прочитать последние 20 строк любого build log если доступен

ШАГ 3 — Сформировать результат

  Если нарушений нет:
    Вывести: "✅ L1 PASS [DATE] — все модули чисты"
    Добавить в docs/reviews/L1_LOG.md: "| [DATE] | ✅ PASS | Нет нарушений |"

  Если есть нарушения — для каждого:
    🔴 CRITICAL: новый SQL injection pattern в shuttle
    🟠 HIGH: файл >500 LOC добавлен без теста
    🟡 MEDIUM: >5 новых TODO без тикета
    🟢 LOW: стилевые комментарии
    
    Добавить в L1_LOG.md: "| [DATE] | ⚠️ WARN | [список нарушений] |"
    Вывести полный список нарушений с путями к файлам
```

---

## T-03 — L2 Weekly: Hound

**Триггер:** вторник
**Что проверяет:** API stability (library contract), bug tracker C.1.0-C.1.6, performance benchmarks, dependency drift
**Выход:** `docs/reviews/hound/REVIEW_[DATE].md`

---

```
Ты — Claude Code, запускаешься в контексте aida-root/.
Задача: L2 Weekly Review — libraries/hound/ по REVIEW_STRATEGY.md §L2-A.
Дата: [YYYY-MM-DD]
⚠️ До миграции: использовать путь Dali4/HOUND/Hound/ вместо libraries/hound/

ШАГ 0 — ветка
  git branch --show-current → должна быть main

ШАГ 1 — MODULE HEALTH
  Посчитать файлы:
    find libraries/hound/src/main/java/ -name "*.java" | wc -l   → PROD_FILES
    find libraries/hound/src/test/java/ -name "*.java" | wc -l   → TEST_FILES
    echo "Ratio: TEST/PROD"
  Найти методы без JavaDoc:
    grep -n "public " libraries/hound/src/main/java/com/hound/*.java | grep -v "^\s*//" | head -20
  Найти методы >100 строк:
    awk '/\{/{depth++} /\}/{depth--; if(depth==0 && len>100) print FILENAME ":" start " (" len " lines)"} depth==1 && /public /{start=NR; len=0} {len++}' \
      libraries/hound/src/main/java/**/*.java 2>/dev/null | head -10

ШАГ 2 — API STABILITY (критично — hound импортируется Dali)
  Проверить существование публичных интерфейсов:
    find libraries/hound/src/main/java/ -name "HoundParser.java" -o -name "HoundConfig.java" -o -name "HoundEventListener.java"
  Если файлы существуют — прочитать и вывести публичные сигнатуры
  Проверить Q27 (HoundConfig full schema): открыт или закрыт?
    Найти в HoundConfig.java поля: dialect, writeMode, arcadeUrl, workerThreads, batchSize
    Отметить какие поля есть, каких не хватает до C.1.2 target

ШАГ 3 — BUG TRACKER (из HOUND_CODE_REVIEW.md)
  Для каждого бага проверить статус:

  B1/B2 — NPE в ensureTable (StructureAndLineageBuilder.java строки 44, 88):
    grep -n "tableName.toUpperCase\|null.*tableName\|isBlank" \
      libraries/hound/src/main/java/com/hound/semantic/engine/StructureAndLineageBuilder.java
    Статус: OPEN если нет null-check перед строкой 44/88, CLOSED если есть

  B3 — NPE в ArcadeDBSemanticWriter (строка 122):
    grep -n "getLineage" \
      libraries/hound/src/main/java/com/hound/storage/ArcadeDBSemanticWriter.java
    Статус: OPEN если нет null-check, CLOSED если есть

  B4 — бесконечная рекурсия в resolveImplicitTableInternal:
    grep -n "MAX_RECURSION_DEPTH\|depth.*guard\|resolveImplicitTableInternal" \
      libraries/hound/src/main/java/com/hound/semantic/engine/NameResolver.java
    Статус: OPEN если нет depth counter, CLOSED если есть

  B6 — thread naming:
    grep -n "THREAD_SEQ\|AtomicInteger\|hound-worker" \
      libraries/hound/src/main/java/com/hound/processor/ThreadPoolManager.java
    Статус: OPEN если static AtomicInteger отсутствует

  P1 — O(A×T) NameResolver:
    grep -n "tablesByName\|Map.*tablesByName" \
      libraries/hound/src/main/java/com/hound/semantic/engine/StructureAndLineageBuilder.java
    Статус: OPEN если индекс отсутствует

  P2 — O(N²) addChildStatement:
    grep -n "LinkedHashSet\|childStatements" \
      libraries/hound/src/main/java/com/hound/semantic/model/StatementInfo.java
    Статус: OPEN если List вместо LinkedHashSet

ШАГ 4 — PERFORMANCE REGRESSION
  Найти bench reports:
    find docs/hound/ -name "BENCH_REPORT*.md" | sort | tail -2
  Если есть — прочитать последний, найти строку "REMOTE_BATCH speedup"
  Ожидаемое значение: 13.1×
  Если отклонение >5% — WARN

ШАГ 5 — DEPENDENCY DRIFT
  Прочитать libraries/hound/build.gradle
  Найти и вывести:
    - arcadedb-engine версия → ожидается 25.12.1 (до C.0) или 26.x (после C.0)
    - arcadedb-network версия
    - antlr4 версия → ожидается 4.13.x
  Сравнить с MODULES_TECH_STACK.md §3.1

ШАГ 6 — СФОРМИРОВАТЬ ОТЧЁТ
  Структура: ## 1. Module Health / ## 2. API Stability / ## 3. Bug Tracker / ## 4. Performance / ## 5. Dependencies
  Каждый баг: статус ✅ CLOSED / 🔴 OPEN / ⚠️ PARTIAL
  Итоговая строка: "Открытых critical bugs: N, High: M"
  
  Сохранить в docs/reviews/hound/REVIEW_[DATE].md
  Добавить строку в docs/reviews/METRICS_LOG.md:
    | [DATE] | S[N] | hound | [TEST_FILES] | [PROD_FILES] | [RATIO] | [MAX_LOC] | [OPEN_BUGS] | [STATUS] |
```

---

## T-04 — L2 Weekly: SHUTTLE

**Триггер:** среда
**Что проверяет:** SQL injection watch, GraphQL schema health, REST clients status, test coverage, dependency drift
**Выход:** `docs/reviews/shuttle/REVIEW_[DATE].md`

---

```
Ты — Claude Code, запускаешься в контексте aida-root/.
Задача: L2 Weekly Review — services/shuttle/ по REVIEW_STRATEGY.md §L2-B.
Дата: [YYYY-MM-DD]
⚠️ До миграции: использовать путь SEERStudio/VERDANDI/SHUTTLE/

ШАГ 0 — ветка
  git branch --show-current → должна быть main

ШАГ 1 — SQL INJECTION WATCH (🔴 критический риск)
  Сканирование на SQL injection patterns:
    grep -rn "String\.format" services/shuttle/src/main/java/ --include="*.java"
    grep -rn '"\s*\+.*[sS][qQ][lL]\|[sS][qQ][lL].*"\s*\+' services/shuttle/src/main/java/ --include="*.java"
    grep -rn "arcade\.sql\|\.query\|\.command" services/shuttle/src/main/java/ --include="*.java" | grep -i "format\|concat\|\+"
  
  Проверить статус C.2.1 fix (SearchService.java:86-87):
    Прочитать строки 80-95 файла services/shuttle/src/main/java/studio/seer/lineage/SearchService.java
    Ожидаемый fix: arcade.sql(template, Map.of("like", like, "n", n))
    Статус: ✅ FIXED если параметризованный вызов, 🔴 OPEN если String.format()
  
  Результат: "0 potential injection points found" или список с файлами и строками

ШАГ 2 — GRAPHQL SCHEMA HEALTH
  Найти schema definition:
    find services/shuttle/src/main/resources/ -name "*.graphql" -o -name "schema.graphql"
    Или: find services/shuttle/src/main/java/ -name "*.java" | xargs grep -l "@GraphQLApi\|@Query\|@Mutation\|@Subscription" 2>/dev/null
  
  Проверить наличие Mutation resolvers (C.2.2):
    grep -rn "startParseSession\|askMimir\|saveView\|deleteView\|resetDemoState\|cancelSession" \
      services/shuttle/src/main/java/ --include="*.java" | grep "@Mutation\|Mutation"
    Статус: ✅ если все 6 мутаций есть, ⚠️ PARTIAL если часть, 🔵 PENDING если нет
  
  Проверить Subscription resolvers (C.2.3):
    grep -rn "heimdallEvents\|sessionProgress" \
      services/shuttle/src/main/java/ --include="*.java" | grep "@Subscription\|Subscription"
    Статус аналогично

ШАГ 3 — REST CLIENTS STATUS (C.2.4-C.2.6)
  find services/shuttle/src/main/java/ -name "DaliClient.java" 2>/dev/null → C.2.4
  find services/shuttle/src/main/java/ -name "MimirClient.java" 2>/dev/null → C.2.5
  find services/shuttle/src/main/java/ -name "HeimdallClient.java" 2>/dev/null → C.2.6
  Для каждого найденного — прочитать и вывести сигнатуры методов

ШАГ 4 — TEST COVERAGE
  Найти все test-файлы:
    find services/shuttle/src/test/ -name "*Test.java" | sort
  Проверить наличие обязательных (из Sprint 7):
    SearchServiceTest, ExploreServiceTest, LineageServiceTest, KnotServiceTest
  Посчитать: TEST_FILES / PROD_FILES ratio

ШАГ 5 — DEPENDENCY DRIFT
  Прочитать services/shuttle/build.gradle
  Найти:
    - io.quarkus версия → ожидается 3.34.2 (из MODULES_TECH_STACK.md)
    - io.smallrye.graphql или quarkus-smallrye-graphql
    - arcadedb client версия
  Найти признаки удалённого pom.xml (D6):
    find services/shuttle/ -name "pom.xml" → если найден — 🔴 WARN (должен быть удалён)

ШАГ 6 — СФОРМИРОВАТЬ ОТЧЁТ
  Структура: ## 1. SQL Injection / ## 2. GraphQL Schema / ## 3. REST Clients / ## 4. Tests / ## 5. Dependencies
  Critical items первыми
  
  Сохранить в docs/reviews/shuttle/REVIEW_[DATE].md
  Добавить строку в docs/reviews/METRICS_LOG.md
```

---

## T-05 — L2 Weekly: Chur

**Триггер:** среда (после T-04)
**Что проверяет:** security (CORS/rate-limit/JWT/scopes), HEIMDALL proxy, WebSocket, зависимости
**Выход:** `docs/reviews/chur/REVIEW_[DATE].md`

---

```
Ты — Claude Code, запускаешься в контексте aida-root/.
Задача: L2 Weekly Review — bff/chur/ по REVIEW_STRATEGY.md §L2-C.
Дата: [YYYY-MM-DD]
⚠️ До миграции: использовать путь SEERStudio/VERDANDI/Chur/

ШАГ 0 — ветка
  git branch --show-current → должна быть main

ШАГ 1 — SECURITY CRITICAL
  1a. CORS — нет wildcard:
    grep -n "cors\|\*\|allowedOrigins\|origin" bff/chur/src/server.ts
    Статус: ✅ если Set-based allowlist, 🔴 если найден wildcard "*"
  
  1b. Rate limiting на auth endpoints:
    grep -rn "rateLimit\|rate-limit\|fastify-rate-limit" bff/chur/src/ --include="*.ts"
    Проверить bff/chur/src/routes/auth.ts строки 1-20
    Ожидаемое: 5 запросов / 15 минут в prod
  
  1c. JWT validation через jose (не @fastify/jwt):
    grep -rn "jose\|verify\|JWKS\|createRemoteJWKSet" bff/chur/src/ --include="*.ts"
    grep -rn "@fastify/jwt" bff/chur/package.json → если найден 🔴 (должен быть удалён)
  
  1d. Keycloak client rename (C.5.2):
    grep -rn "verdandi-bff\|aida-bff\|CLIENT_ID" bff/chur/src/ bff/chur/.env* --include="*.ts" 2>/dev/null
    Статус: ✅ если aida-bff, 🟡 PENDING если verdandi-bff

ШАГ 2 — SCOPE CHECKS STATUS (C.3.1, C.3.2)
  Проверить requireScope реализацию:
    grep -rn "requireScope\|aida:admin\|aida:admin:destructive" bff/chur/src/ --include="*.ts"
    Статус: ✅ если функция существует, 🔵 PENDING если нет
  
  Проверить /heimdall/* proxy routes (C.3.1):
    grep -rn "heimdall\|/heimdall" bff/chur/src/ --include="*.ts"
    Ожидается: POST /heimdall/graphql, POST /heimdall/control/*
    Статус: ✅ если оба маршрута есть, 🔵 PENDING если нет

ШАГ 3 — WEBSOCKET STATUS (C.3.4)
  Проверить @fastify/websocket установлен:
    grep "@fastify/websocket" bff/chur/package.json
  Проверить WebSocket upgrade handler:
    grep -rn "websocket\|WebSocket\|ws://" bff/chur/src/ --include="*.ts"
    Ожидается: обработчик для /graphql WebSocket upgrade
    Статус: ✅ если реализован, 🔵 PENDING если нет

ШАГ 4 — TEST COVERAGE
  find bff/chur/src/ -name "*.test.ts" | sort
  Проверить обязательные: rbac.test.ts, auth.test.ts
  Посчитать TEST_FILES / PROD_FILES

ШАГ 5 — DEPENDENCY HEALTH
  Прочитать bff/chur/package.json
  Найти версии:
    jose → ожидается 5.x
    fastify → ожидается 4.28.x
    @fastify/rate-limit → должен быть
    @fastify/cookie → должен быть
    @fastify/jwt → НЕ должен быть (удалён в пользу jose)
  Проверить что нет очевидно устаревших пакетов (major versions behind)

ШАГ 6 — СФОРМИРОВАТЬ ОТЧЁТ
  Security issues — первыми и жирным
  Сохранить в docs/reviews/chur/REVIEW_[DATE].md
  Добавить строку в METRICS_LOG.md
```

---

## T-06 — L2 Weekly: verdandi

**Триггер:** четверг
**Что проверяет:** LOC targets, test coverage gaps, ELK Worker, новые views, ADR compliance, tech debt
**Выход:** `docs/reviews/verdandi/REVIEW_[DATE].md`

---

```
Ты — Claude Code, запускаешься в контексте aida-root/.
Задача: L2 Weekly Review — frontends/verdandi/ по REVIEW_STRATEGY.md §L2-D.
Дата: [YYYY-MM-DD]
⚠️ До миграции: использовать путь SEERStudio/VERDANDI/verdandi/

ШАГ 0 — ветка
  git branch --show-current → должна быть main

ШАГ 1 — COMPONENT SIZE WATCH
  Найти топ-10 самых больших файлов:
    find frontends/verdandi/src -name "*.tsx" -o -name "*.ts" | \
      xargs wc -l 2>/dev/null | sort -rn | head -12
  
  Проверить watchlist (из Sprint 7):
    grep -c "" frontends/verdandi/src/components/SearchPanel.tsx 2>/dev/null → цель ≤350, сейчас 516
    grep -c "" frontends/verdandi/src/components/loom/LoomCanvas.tsx 2>/dev/null → цель ≤350, сейчас 403
    grep -c "" frontends/verdandi/src/stores/loomStore.ts 2>/dev/null → цель <400, сейчас 251 ✅
  
  Если файл вырос с прошлого review — WARN
  Если файл превысил 500 LOC → 🔴

ШАГ 2 — TEST COVERAGE
  Посчитать тест-файлы:
    find frontends/verdandi/src -name "*.test.*" | wc -l
  
  Проверить пробелы из Sprint 7 (должны закрыться):
    find frontends/verdandi/src/hooks/ -name "useGraphData.test.*" → ❌ если нет
    find frontends/verdandi/src/hooks/ -name "useDisplayGraph.test.*" → ❌ если нет
    find frontends/verdandi/src/hooks/ -name "useLoomLayout.test.*" → ❌ если нет (326 LOC, самый сложный)
    find frontends/verdandi/src/hooks/ -name "useExpansion.test.*" → ❌ если нет
  
  E2E:
    find frontends/verdandi/e2e/ -name "smoke.spec.*" → должен существовать

ШАГ 3 — ELK WORKER STATUS (WARN-01)
  Прочитать frontends/verdandi/src/utils/layoutGraph.ts строки 100-130
  Проверить: ELK запускается на main thread или через Worker?
    grep -n "new Worker\|elkWorker\|workerize\|comlink" \
      frontends/verdandi/src/utils/layoutGraph.ts
    Текущий статус из Sprint 7: main thread (elkWorker.ts существует но не используется)
  
  Проверить версию Vite (Worker fix ожидается в Vite 7+):
    grep '"vite"' frontends/verdandi/package.json
  
  Если Vite ≥ 7.0.0 → исследовать возможность активации Worker

ШАГ 4 — NEW VIEWS STATUS (из REFACTORING_PLAN C.4.x)
  C.4.1 WebSocket client:
    grep '"graphql-ws"' frontends/verdandi/package.json → статус установки
    grep -rn "createClient\|graphql-ws" frontends/verdandi/src/ --include="*.ts" --include="*.tsx"
  
  C.4.2 ANVIL UI view:
    find frontends/verdandi/src/ -name "*Anvil*" -o -name "*anvil*" | grep -i "\.tsx\|\.ts"
    grep -rn "/anvil\|route.*anvil" frontends/verdandi/src/ --include="*.tsx"
  
  C.4.3 MIMIR Chat view:
    find frontends/verdandi/src/ -name "*Mimir*" -o -name "*mimir*" -o -name "*Chat*" | grep -i "\.tsx"
    Статус: ✅/🔵 PENDING для каждого

ШАГ 5 — ADR COMPLIANCE
  ADR-002 @xyflow/react:
    grep '"@xyflow/react"' frontends/verdandi/package.json
  
  ADR-011 graphql-request (не Apollo/urql):
    grep '"graphql-request"' frontends/verdandi/package.json → должен быть
    grep '"@apollo/client"\|"urql"' frontends/verdandi/package.json → не должны быть
  
  ADR-007 nodesDraggable=false:
    grep -n "nodesDraggable" frontends/verdandi/src/components/loom/LoomCanvas.tsx
  
  loomStore слайсы (10 штук):
    find frontends/verdandi/src/stores/slices/ -name "*Slice.ts" | wc -l → должно быть 10
    Ожидаются: navigationSlice, undoSlice, persistSlice, filterSlice, l1Slice,
               expansionSlice, visibilitySlice, themeSlice, viewportSlice, selectionSlice

ШАГ 6 — TECH DEBT РЕЕСТР
  TD-09 FilterToolbar дублирование:
    find frontends/verdandi/src/components/ -name "ToolbarPrimitives*" → ✅ если есть
  
  TD-11 Hardcoded LIMIT без hasMore:
    grep -rn "LIMIT\|limit.*20\|limit.*50\|limit.*100" \
      frontends/verdandi/src/ --include="*.ts" --include="*.tsx" | grep -v test | head -5

ШАГ 7 — СФОРМИРОВАТЬ ОТЧЁТ
  Секции: LOC / Tests / ELK / New Views / ADR / Tech Debt
  Тренды: указать изменение LOC с прошлого review если есть METRICS_LOG
  
  Сохранить в docs/reviews/verdandi/REVIEW_[DATE].md
  Добавить строку в METRICS_LOG.md
```

---

## T-07 — L3 Post-Migration Baseline

**Триггер:** однократно, сразу после завершения REPO_MIGRATION_PLAN Phase 6
**Что проверяет:** verification checklist из REPO_MIGRATION_PLAN + baseline per module
**Выход:** `docs/reviews/sprint/SPRINT_00_MIGRATION_BASELINE.md`

---

```
Ты — Claude Code, запускаешься в контексте aida-root/.
Задача: L3 Post-Migration Baseline Review.
Это первый полный review после завершения REPO_MIGRATION_PLAN.
Задача — зафиксировать состояние, не найти проблемы.
Дата: [YYYY-MM-DD]

ШАГ 0 — ветка
  git branch --show-current → должна быть main
  git log --oneline -5 → показать последние коммиты миграции

ШАГ 1 — REPO_MIGRATION_PLAN Verification Checklist
  Пройти все пункты из REPO_MIGRATION_PLAN.md §6:
  
  Monorepo mode:
    ✓ ./gradlew tasks — запускается без ошибок?
    ✓ Файл libraries/hound/build.gradle существует?
    ✓ Строка 84 содержит ${projectDir}, не ${rootDir}?
    ✓ Файл services/shuttle/pom.xml удалён?
    ✓ Файл services/shuttle/settings.gradle содержит rootProject.name = 'shuttle'?
    ✓ Сеть в docker-compose.yml называется aida_net (не verdandi_net)?
    ✓ Makefile существует?
    ✓ .github/workflows/ci.yml содержит job для hound?
  
  Standalone mode:
    ✓ libraries/hound/settings.gradle содержит rootProject.name = 'hound'?
    ✓ libraries/hound/gradlew существует?
    ✓ services/shuttle/gradlew существует?
  
  Для каждого пункта: ✅ OK / 🔴 FAILED / ⚠️ NOT CHECKED

ШАГ 2 — Per-module baseline (краткий L2 для каждого)
  Для каждого модуля (hound, shuttle, chur, verdandi):
    - Количество файлов в src/
    - Количество test-файлов
    - Test/prod ratio
    - Самый большой файл и его LOC
  
  Зафиксировать как "Sprint 0 baseline" в METRICS_LOG.md

ШАГ 3 — Новые issues от миграции (если есть)
  Проверить что не появились новые проблемы в процессе миграции:
    grep -rn "TODO.*migration\|FIXME.*migration\|HACK.*migration" . --include="*.java" --include="*.ts" --include="*.gradle"
  
  Проверить что build contexts в docker-compose.yml ведут на правильные пути:
    grep -n "context:\|build:" docker-compose.yml

ШАГ 4 — СФОРМИРОВАТЬ ОТЧЁТ
  Заголовок: "Sprint 0 — Post-Migration Baseline"
  Секция 1: Checklist результаты (таблица ✅/🔴)
  Секция 2: Module baselines таблица
  Секция 3: Open issues перенесённые из pre-migration состояния
  
  Сохранить в docs/reviews/sprint/SPRINT_00_MIGRATION_BASELINE.md
  Заполнить baseline строки в METRICS_LOG.md (заменить приблизительные значения из T-01)
```

---

## T-08 — L3 Sprint-End Full Review

**Триггер:** пятница последней недели спринта
**Что проверяет:** все модули + кросс-модульная интеграция + architecture drift + demo safety
**Выход:** `docs/reviews/sprint/SPRINT_[N]_FULL_REVIEW.md`

---

```
Ты — Claude Code, запускаешься в контексте aida-root/.
Задача: L3 Sprint-End Full Review по REVIEW_STRATEGY.md.
Sprint: [N]
Дата: [YYYY-MM-DD]
Предыдущий L3: [PREV_DATE или "T-07 baseline"]

ШАГ 0 — ветка
  git branch --show-current → должна быть main
  Если нет → добавить ⚠️ WARNING и продолжить

═══════════════════════════════════════════════════
ЧАСТЬ 1 — Per-module summary
═══════════════════════════════════════════════════

Для каждого активного модуля выполнить краткий L2:
  hound: ШАГ 1-3 из T-03 (health + API + bug tracker)
  shuttle: ШАГ 1-2 из T-04 (SQL injection + GraphQL schema)
  chur: ШАГ 1 из T-05 (security only)
  verdandi: ШАГ 1-2 из T-06 (LOC + tests)

Для каждого: одна строка "MODULE: ✅/🟡/🔴 [главная находка]"

═══════════════════════════════════════════════════
ЧАСТЬ 2 — Cross-module integration check
═══════════════════════════════════════════════════

2.1 Shared models consistency:
  Проверить shared/dali-models/ (если создан):
    find shared/dali-models/src/ -name "*.java" | sort
    Найти: HoundConfig.java, HeimdallEvent.java, ParseSessionInput.java
  
  Проверить дублирование между hound и shuttle:
    grep -rn "class.*Config\|record.*Config" libraries/hound/src/main/java/ --include="*.java"
    grep -rn "class.*Config\|record.*Config" services/shuttle/src/main/java/ --include="*.java"
    Если одинаковые классы в обоих → 🟡 кандидат на вынос в dali-models

2.2 REST client contracts (SHUTTLE → other services):
  Найти все REST clients в shuttle:
    find services/shuttle/src/main/java/ -name "*Client.java" | sort
  Для каждого: проверить что endpoint URL совпадает с портами из docker-compose.yml:
    DaliClient → :9090
    MimirClient → :9091
    HeimdallClient → :9093

2.3 Integration matrix drift:
  Прочитать INTEGRATIONS_MATRIX.md
  Найти интеграции со статусом 🔵 NEW и проверить которые уже реализованы:
    I11 SHUTTLE → Dali: DaliClient в shuttle? ✅/🔵
    I18 Dali → FRIGG: services/dali существует? ✅/🔵
    I26 Hound → HEIMDALL: HeimdallEmitter в hound? ✅/🔵
    I27 Dali → HEIMDALL: HeimdallEmitter в dali? ✅/🔵
  Есть ли новые интеграции в коде, которых нет в матрице? → добавить TODO в отчёт

2.4 Docker Compose consistency:
  Прочитать docker-compose.yml
  Проверить:
    - Network называется aida_net
    - Все сервисы из MODULES_TECH_STACK.md §3.15 declared
    - Порты соответствуют: shuttle:8080, chur:3000, verdandi:13000, keycloak:8180, arcadedb:2480
    - Если dali/heimdall-backend созданы — они в compose?

═══════════════════════════════════════════════════
ЧАСТЬ 3 — Architecture drift
═══════════════════════════════════════════════════

3.1 Прочитать DECISIONS_LOG.md
  Найти открытые вопросы с прошедшими дедлайнами:
    Q3 HEIMDALL backend: mid-May → если дата > mid-May, статус?
    Q5 Co-founder split: эта неделя → решён?
    Q6 HEIMDALL event schema: mid-May → если дата > mid-May, статус?
    Q7 HEIMDALL ↔ Dali API: end of April → если дата > end of April, статус?
    Q25 Event bus transport: mid-May → если дата > mid-May, статус?
  
  Для каждого просроченного: 🔴 OVERDUE + вывести вопрос

3.2 Прочитать PROJECT_ROADMAP.md
  Найти milestone для текущей даты
  Проверить: какие задачи должны быть завершены к этой дате?
  Сравнить с реальным состоянием кода
  Вывести: "На [DATE] должно быть: X. Реально: Y. Статус: ahead/on-track/behind"

3.3 REFACTORING_PLAN.md прогресс:
  C.0 status: grep -rn "arcadedb.*26\|26\.x\|ArcadeDB.*latest" libraries/hound/build.gradle
  C.1.0 status: из T-03 ШАГ 3 (bug tracker)
  C.2.1 status: из T-04 ШАГ 1 (SQL injection)
  C.5.2 status: из T-05 ШАГ 1d (Keycloak rename)

═══════════════════════════════════════════════════
ЧАСТЬ 4 — Demo safety radar
═══════════════════════════════════════════════════

4.1 make demo-reset:
  grep -n "demo-reset\|demo-start\|demo-reset" Makefile 2>/dev/null
  Статус: ✅ если targets существуют, 🔵 PENDING если нет

4.2 Demo dataset:
  Есть ли папка с demo data?
    find . -name "demo*" -type d -not -path "*/node_modules/*" -not -path "*/.gradle/*"
  500K LoC SQL файлы готовы? (anonymized)

4.3 Backup laptop:
  Этот пункт — manual check. Добавить в отчёт:
  "⚠️ MANUAL CHECK REQUIRED: backup laptop sync. Последняя синхронизация: [указать]"

4.4 Pre-recorded fallback video:
  find . -name "demo*.mp4" -o -name "demo*.mov" 2>/dev/null | head -3

═══════════════════════════════════════════════════
ЧАСТЬ 5 — Метрики
═══════════════════════════════════════════════════

Обновить METRICS_LOG.md для всех модулей.
Прочитать предыдущие строки из METRICS_LOG.md и показать тренд:
  TEST_FILES: растёт или стагнирует?
  MAX_FILE_LOC: уменьшается или растёт?
  OPEN_BUGS: закрываются или накапливаются?

═══════════════════════════════════════════════════
ВЫХОД
═══════════════════════════════════════════════════

Сформировать docs/reviews/sprint/SPRINT_[N]_FULL_REVIEW.md:
  ## Sprint [N] Full Review — [DATE]
  ### Executive Summary (5 строк max)
  ### Part 1: Module Status
  ### Part 2: Integration Health  
  ### Part 3: Architecture Drift
  ### Part 4: Demo Safety
  ### Part 5: Metrics
  ### Action Items (приоритизированный список)

Обновить METRICS_LOG.md всеми новыми строками.
```

---

## T-09 — L2 Weekly: Dali (будущий)

**Триггер:** вторник, когда `services/dali/` создан
**Что проверяет:** JobRunr интеграция, FRIGG StorageProvider, HeimdallEmitter, тесты

---

```
Ты — Claude Code, запускаешься в контексте aida-root/.
Задача: L2 Weekly Review — services/dali/ по REVIEW_STRATEGY.md.
Дата: [YYYY-MM-DD]

ПРЕДУСЛОВИЕ: проверить что services/dali/ существует
  find services/dali/src/ -name "*.java" | wc -l
  Если 0 → вывести "🔵 Dali ещё не создан, review пропущен" и выйти

ШАГ 1 — JOBRUNR INTEGRATION
  Найти custom StorageProvider:
    find services/dali/src/ -name "*StorageProvider*" -o -name "*ArcadeDB*Storage*"
  Если не найден → 🔵 PENDING (C.1 Dali engineering)
  Если найден → прочитать и проверить:
    - implements StorageProvider?
    - Обрабатывает Job, RecurringJob, JobRunrMetadata?
    - Тест для StorageProvider?

ШАГ 2 — HEIMDALL EMITTER
  find services/dali/src/ -name "HeimdallEmitter*" -o -name "*HeimdallEmitter*"
  Если найден → проверить:
    - @ApplicationScoped?
    - sessionStarted(), workerAssigned(), jobCompleted() методы есть?
    - fire-and-forget (subscribe().with, не block())?
    - Падение HEIMDALL не роняет Dali (try-catch в emit)?

ШАГ 3 — USE CASES STATUS
  Найти реализацию UC1 (scheduled harvest):
    grep -rn "UC1\|@Scheduled\|cron\|triggerHarvest" services/dali/src/ --include="*.java"
  Найти UC2b (event-driven):
    grep -rn "UC2b\|eventDriven\|@ConsumeEvent" services/dali/src/ --include="*.java"

ШАГ 4 — DEPENDENCY CHECK
  Проверить services/dali/build.gradle:
    - quarkus версия = 3.34.2
    - jobrunr зависимость есть
    - project(':hound') или project(':libraries:hound') есть
    - project(':dali-models') или shared models есть

Сохранить в docs/reviews/dali/REVIEW_[DATE].md
```

---

## T-10 — L2 Weekly: HEIMDALL backend (будущий)

**Триггер:** вторник, когда `services/heimdall-backend/` создан
**Что проверяет:** event pipeline (Sprint 1), metrics+control (Sprint 2), эмиттеры подключены

---

```
Ты — Claude Code, запускаешься в контексте aida-root/.
Задача: L2 Weekly Review — services/heimdall-backend/ по REVIEW_STRATEGY.md.
Дата: [YYYY-MM-DD]
Источник: HEIMDALL_SPRINT_PLAN.md

ПРЕДУСЛОВИЕ:
  find services/heimdall-backend/src/ -name "*.java" | wc -l
  Если 0 → "🔵 HEIMDALL backend не создан, review пропущен" и выйти

ШАГ 1 — SPRINT 1 CHECKLIST (event pipeline)
  POST /events endpoint:
    find services/heimdall-backend/src/ -name "EventResource.java"
    → если найден: grep -n "@POST\|ingest\|batch" .../EventResource.java
  
  Ring buffer:
    find services/heimdall-backend/src/ -name "RingBuffer.java"
    → если найден: grep -n "capacity\|push\|snapshot\|subscribe" .../RingBuffer.java
  
  WebSocket endpoint:
    find services/heimdall-backend/src/ -name "*EventStream*" -o -name "*WebSocket*"

ШАГ 2 — SPRINT 2 CHECKLIST (metrics + control)
  MetricsCollector:
    find services/heimdall-backend/src/ -name "MetricsCollector.java"
  
  Control resource:
    find services/heimdall-backend/src/ -name "ControlResource.java"
    grep -n "reset\|snapshot\|cancel" .../ControlResource.java 2>/dev/null
  
  SnapshotManager (FRIGG persistence):
    find services/heimdall-backend/src/ -name "SnapshotManager.java"

ШАГ 3 — NON-CRITICAL PATTERN CHECK
  Проверить что HEIMDALL не блокирует эмиттеров:
    grep -rn "\.block()\|\.await()\|\.get()" services/heimdall-backend/src/ --include="*.java" | grep -i "emit\|ingest"
    Должно быть 0 (fire-and-forget везде)

ШАГ 4 — SCOPE IN BOUNDS (не выходить за demo scope)
  Проверить что нет Prometheus/Kafka/Jaeger:
    grep -rn "prometheus\|kafka\|jaeger\|zipkin" services/heimdall-backend/build.gradle
    Если найдены → 🟡 WARN (out of demo scope)

Сохранить в docs/reviews/heimdall-backend/REVIEW_[DATE].md
```

---

## T-11 — L2 Weekly: HEIMDALL frontend (будущий)

**Триггер:** четверг, когда `frontends/heimdall-frontend/` создан
**Что проверяет:** dashboard views, WebSocket connection, virtualized event stream, demo safety controls

---

```
Ты — Claude Code, запускаешься в контексте aida-root/.
Задача: L2 Weekly Review — frontends/heimdall-frontend/ по REVIEW_STRATEGY.md.
Дата: [YYYY-MM-DD]

ПРЕДУСЛОВИЕ:
  find frontends/heimdall-frontend/src/ -name "*.tsx" | wc -l
  Если 0 → "🔵 HEIMDALL frontend не создан, review пропущен" и выйти

ШАГ 1 — СТРАНИЦЫ / VIEWS
  find frontends/heimdall-frontend/src/pages/ -name "*.tsx" 2>/dev/null
  Ожидаются: DashboardPage, EventStreamPage, ControlPage
  Статус: ✅/🔵 для каждой

ШАГ 2 — WEBSOCKET CONNECTION
  grep -rn "useEventStream\|WebSocket\|ws://" frontends/heimdall-frontend/src/ --include="*.ts" --include="*.tsx"
  Проверить подключение к /ws/events через Chur proxy

ШАГ 3 — VIRTUALIZED EVENT LOG
  grep -rn "react-virtuoso\|react-window\|FixedSizeList\|VirtualList" \
    frontends/heimdall-frontend/src/ --include="*.tsx" frontends/heimdall-frontend/package.json
  Если нет — 🟡 WARN (без виртуализации 10K events = freeze)

ШАГ 4 — DEMO SAFETY CONTROLS
  Найти кнопку reset:
    grep -rn "reset\|demo-reset\|resetDemo" frontends/heimdall-frontend/src/ --include="*.tsx"
  
  Проверить что destructive actions требуют подтверждения:
    grep -rn "confirm\|dialog\|modal\|areYouSure" frontends/heimdall-frontend/src/ --include="*.tsx"

ШАГ 5 — SCOPE CHECK (только demo observability)
  grep -rn "prometheus\|grafana\|jaeger\|alertmanager" \
    frontends/heimdall-frontend/src/ frontends/heimdall-frontend/package.json
  Если найдено → 🟡 WARN

Сохранить в docs/reviews/heimdall-frontend/REVIEW_[DATE].md
```

---

## Использование

### Расписание запусков в Claude Code

```yaml
# .claude/scheduled_tasks.yml (пример)
tasks:
  - id: L1-monday
    prompt_file: docs/reviews/prompts/T-02.md
    schedule: "0 9 * * 1"    # каждый понедельник 09:00
    replace: { "[YYYY-MM-DD]": "TODAY" }

  - id: L1-friday
    prompt_file: docs/reviews/prompts/T-02.md
    schedule: "0 9 * * 5"    # каждую пятницу 09:00

  - id: L2-hound
    prompt_file: docs/reviews/prompts/T-03.md
    schedule: "0 10 * * 2"   # вторник

  - id: L2-shuttle-chur
    prompt_file: docs/reviews/prompts/T-04-T-05.md
    schedule: "0 10 * * 3"   # среда

  - id: L2-verdandi
    prompt_file: docs/reviews/prompts/T-06.md
    schedule: "0 10 * * 4"   # четверг

  - id: L3-sprint-end
    prompt_file: docs/reviews/prompts/T-08.md
    schedule: "0 14 * * 5/2" # каждая вторая пятница
    replace: { "[N]": "SPRINT_NUMBER" }
```

### Порядок первого запуска

```
T-01 → T-07 → начать регулярный цикл T-02/T-03/T-04/T-05/T-06
                                      ↓ каждые 2 недели
                                     T-08
T-09 — добавить когда services/dali/ появится
T-10 — добавить когда services/heimdall-backend/ появится
T-11 — добавить когда frontends/heimdall-frontend/ появится
```
