# AIDA — Decisions Log (quick reference)

**Документ:** `DECISIONS_LOG`
**Версия:** 2.2
**Дата:** 14.04.2026
**Статус:** Working document — quick reference для навигации

Это **краткий** snapshot того что зафиксировано, что открыто, и что отложено. Для деталей смотри соответствующие документы.

---

## 📋 Quick status

**Зафиксированных решений:** 16
**Открытых вопросов:** 28 (Q1, Q3-Q22, Q24-Q32)
**Сознательно отложено:** 10 (D1-D10)

---

## ✅ Зафиксированные решения (11.04.2026 session)

### Модули и tech stack (из v2.0-v2.1)

| # | Решение | Reference |
|---|---|---|
| 1 | **Иерархия:** AIDA = зонтик-платформа, SEER Studio = первый продукт, VERDANDI = главное frontend приложение | `MODULES_TECH_STACK.md §0` |
| 2 | **Backend стеки working:** Hound (Java 21 + Gradle + ANTLR 4.13.1), SHUTTLE (Quarkus 3.34.2), Chur (Fastify 4 + Node.js 22) | `MODULES_TECH_STACK.md §1` |
| 3 | **Frontend стек working:** VERDANDI — React 19.2.4 + Vite 8.0.1 + Tailwind 4 + Zustand + graphql-request + ELK.js | `MODULES_TECH_STACK.md §3.5` |
| 4 | **HEIMDALL = Admin Control Panel всей AIDA**, отдельный frontend + отдельный backend (ADR-DA-010). ADR-DA-001 "in-process module" REVOKED | `MODULES_TECH_STACK.md §3.6` |
| 5 | **Dali Core = Java 21 + Quarkus 3.34.2 + JobRunr (Open Source)** — ADR-DA-008 CONFIRMED. JobRunr покрывает все 15 требований библиотечно, ~600-800 LoC бизнес-логики | `MODULES_TECH_STACK.md §3.7`, ADR-DA-008 |

### Новые решения этой сессии (v2.2)

| # | Решение | Reference |
|---|---|---|
| 6 | **AIDA Gradle Multi-Module Project** — все JVM сервисы (Hound, SHUTTLE, Dali, MIMIR, ANVIL, HEIMDALL backend) в одном Gradle проекте, но runtime — **отдельные процессы**. Shared types через `dali-models` module. Chur/VERDANDI не в Gradle (Node/React). | `MODULES_TECH_STACK.md §2.3` |
| 7 | **SHUTTLE → Dali = HTTP REST** (Вариант A) — Quarkus REST client + shared Gradle module. Independent failure domains, independent deployment, единый механизм вызова Dali для всех клиентов | `INTEGRATIONS_MATRIX.md I11`, `MODULES_TECH_STACK.md §3.7` |
| 8 | **FRIGG = unified SEER Studio state store** — отдельный ArcadeDB instance хранит saved views + user prefs + JobRunr jobs/state/history. JobRunr через custom ArcadeDB StorageProvider (~200-400 LoC) | `MODULES_TECH_STACK.md §3.10` |
| 9 | **Hound three modes policy в HoundConfig** — REMOTE_BATCH default (UC1/UC2a), REMOTE single (UC2b), EMBEDDED (UC3/UC4/preview/dev). Dali выбирает mode по типу job | `MODULES_TECH_STACK.md §3.1`, `INTEGRATIONS_MATRIX.md §2.5` |
| 10 | **ArcadeDB: HoundArcade (network mode) уже на 26.3.2** — production и SHUTTLE уже на 26.x. Embedded используется **только в тестах Hound** (25.12.1). Новые фичи 26.x (Bolt, gRPC batch, GraphQL introspection) нужны. ADR-DA-011 CONFIRMED. | `MODULES_TECH_STACK.md §3.2`, ADR-DA-011, `REFACTORING_PLAN.md C.0` |
| 12 | **ArcadeDB version policy** — No mixed embedded/network versions на одной БД. Выбран Вариант 1: тесты Hound → network mode (26.x). Вариант 2 (ANTLR shading) отклонён — техдолг. Вариант 3 (mixed 25+26) отклонён — риск авто-апгрейда схемы. | `REFACTORING_PLAN.md C.0` |
| 13 | **ADR-DA-012: Frontend routing — single domain + path** — `seer.studio/verdandi`, `/urd`, `/skuld`, `/heimdall`. Nginx раздаёт отдельные SPA-бандлы. B2 CONFIRMED: thin JS shell + Module Federation. Shell = host, verdandi/heimdall/urd/skuld = remotes. aida-shared singleton = shared context state. Обоснование: лучше чем субдомены для передачи контекста, одна точка деплоя. | `MODULES_TECH_STACK.md §2.4` |
| 17 | **Co-founder split (Q5 CLOSED)** — **Ты → Track A** (Hound + Dali Core + ANVIL + benchmarks + YGG) · **Соавтор → Track B** (LOOM + MIMIR + HEIMDALL dashboard + demo rehearsal + SHUTTLE). Параллельная разработка июнь–октябрь разблокирована. | DECISIONS_LOG |
| 19 | **Hound C.0.1 — ArcadeDB 26.x (ADR-DA-011 CONFIRMED).** EmbeddedWriter.java удалён. Единственный режим записи: REMOTE / REMOTE_BATCH через arcadedb-network:26.3.1. No mixed embedded/network versions. | `REFACTORING_PLAN.md C.0` |
| 18 | **Demo dataset (Q CLOSED)** — 300 собственных PL/SQL файлов, 1K–20K строк каждый (~1-3M LoC). Превышает target 500K LoC с запасом. Анонимизация не требуется — собственные тестовые/учебные файлы, публично показываемы. | DECISIONS_LOG |
| 16 | **M1 архитектурные решения (зафиксированы в процессе)**: HeimdallEventBus = Mutiny BroadcastProcessor (не SmallRye Messaging) для hot fan-out. HeimdallEventView.payloadJson = Map→JSON через Jackson (GQL-совместимо). requireAdmin M1 = role===admin (scope-based Sprint 4). @fastify/websocket@^9 (Fastify 4 compat). NavigateBridge = URL↔shellStore sync. Shell i18n ключ = seer-lang. | `SPRINT_APR13_MAY9_M1.md` |
| 15 | **HEIMDALL Sprint 2 — завершён** (12.04.2026). EventFilter 4 типа фильтров. SnapshotManager Uni chain. FriggGateway (mirrors ArcadeGateway). Chur proxy /heimdall/* (admin-only). HeimdallEmitter в SHUTTLE (fire-and-forget). R1 fixed (HandshakeRequest manual parse). R2 resolved (FRIGG running, healthcheck wget). | `HEIMDALL_SPRINT_PLAN.md v1.2` |
| 14 | **ADR-DA-013: Cross-app context passing через URL params** — canonical ID = ArcadeDB geoid (`DaliTable:prod.orders`). `navigateTo(app, context)` + `useAppContext()` в `aida-shared`. Параметры: nodeId, schema, returnTo, highlight, sessionId. URL = единственный источник правды. | `MODULES_TECH_STACK.md §2.5` |
| 11 | **Arrow Flight** — strategic note в §8 архитектурного документа, связан с долгосрочным ygg.db fork. **НЕ прорабатывается для October scope**, никаких изменений в матрице D или plan C | `MODULES_TECH_STACK.md §8.1-8.2` |

---

## ❓ Открытые вопросы

### 🔴 Блокирующие

| # | Вопрос | Когда решать |
|---|---|---|
| **Q1** | Backend стратегия (JVM-first vs polyglot vs ad-hoc) | Формально открыт, решения per-component |
| **Q3** | HEIMDALL backend deployment (HB1 Quarkus / другое) | mid May |
| **Q4** | HighLoad CFP deadline | в течение 48 часов |

### 🟡 Важные

| # | Вопрос | Note |
|---|---|---|
| Q5 | Co-founder split (Track A data vs Track B UI) | в течение недели |
| Q6 | HEIMDALL event schema | mid May |
| Q7 | HEIMDALL ↔ Dali control API формат | end of April (зависит от Q1, Q3) |
| Q8 | MIMIR язык (Python vs Java) | зависит от Q1 + Q29 post-upgrade re-eval |
| Q9 | ANVIL backend язык | зависит от Q1 + Q30 post-upgrade re-eval |
| Q10 | Chur migration на Quarkus | зависит от Q1, не блокирует |
| Q11 | Список middleware modules для URD/SKULD | от owner SEER Studio, post-HighLoad |
| ✅ Q12 | HEIMDALL frontend charting library | **Recharts via shadcn/charts** — shadcn/ui в стеке (ADR-003), zero-config Tailwind. Post-HighLoad альтернативы: Nivo, ApexCharts, Unovis |
| ✅ Q13 | HEIMDALL frontend WebSocket protocol | **Native WebSocket** — HEIMDALL backend exposes raw `/ws/events`. graphql-ws только в VERDANDI (I33 через SHUTTLE). I34 обновлён. |
| Q14 | PostgreSQL semantic listener план | start of May |
| Q15 | ClickHouse semantic listener | June |
| Q24 | HEIMDALL backend deployment details | см. Q3 |
| Q25 | Event bus transport (HTTP POST / Kafka / Reactive Messaging) | mid May |
| Q26 | ANVIL direct из SHUTTLE или только через MIMIR | при MIMIR design |
| Q27 | HoundConfig полная схема | при Hound refactor (C.1.2) |

### 🟢 Post-upgrade opportunities (новые этой сессии)

| # | Вопрос | Re-evaluation |
|---|---|---|
| **Q29** | MIMIR через ArcadeDB built-in MCP Server? | May-June после upgrade |
| **Q30** | ANVIL через 72 built-in graph algorithms? | May-June после upgrade |
| **Q31** | UC2a preview implementation (6α vs 6β Embedded Server) — CR1 разблокирован | mid May (прежде чем начать preview в Dali) |
| **Q32** | Arrow Flight scope и timing — strategic note | re-eval post-HighLoad |

### 🔵 Детали

| # | Вопрос |
|---|---|
| Q16 | Quarkus версия точно (3.17.7 vs 3.34.2) |
| Q17 | Hound LOC точная цифра |
| Q18 | Docker network rename `verdandi_net` → `aida_net` |
| Q19 | MIMIR cache format |
| Q20 | Ollama deployment детали |
| Q21 | YGG: 4 document types точный список |

---

## ✅ Hound Sprint 2 решения (14.04.2026)

| # | Решение | Reference |
|---|---------|-----------|
| **Q28** | **DaliRecordField как отдельная вершина.** Решение: Да. Причина: нужен target для ребра `RETURNS_INTO` на уровне поля, а не записи. Альтернатива (свойство `DaliRecord.fields: List<String>`) отклонена — нет возможности построить ребро к конкретному полю. `RecordInfo.FieldInfo record(name, dataType, ordinalPosition, sourceColumnGeoid)` — новая модель. | `HOUND_PLSQL_LINEAGE_GAPS_S2.md §2`, `HOUND_GEOID_SPEC.md §3.7` |
| **Q29** | **RETURNING INTO target классификация — 4 типа.** Решение: classifyReturningTarget() по приоритету: (1) содержит `.` → RECORD_FIELD, (2) параметр routine → PARAMETER, (3) DaliRecord с таким именем → RECORD, (4) иначе → VARIABLE. Все 4 варианта создают ребро `RETURNS_INTO` от DaliStatement к разным target vertex types. | `HOUND_PLSQL_LINEAGE_GAPS_S2.md §2.5`, `g6-cursor-insert-values-lineage.md §19` |
| **Q30** | **Pending column resolution depth для parent chain — depth=1.** Решение: Pass 3 (`resolveViaParent`) ищет только в прямом родителе (`parentStatementGeoid`), не рекурсирует. Мотивация: SQL correlated subquery standard — только прямой родитель видим в correlated scope. LATERAL и CTE — аналогично depth=1. Pass 4 (single-table fuzzy, quality=LOW) — дополнительный проход при единственном source table. | `HOUND_PLSQL_LINEAGE_GAPS_S2.md §6` |
| **Q31** | **INSERT ALL implementation — enterInsert_statement check.** Решение: проверка `ctx.multi_table_insert() != null` в `enterInsert_statement` вместо отдельного `enterMulti_table_insert` с `onMultiTableInsertEnter()`. Причина: механизм `enterGeneral_table_ref` + `in_dml_target=true` уже регистрирует все target таблицы INSERT ALL на текущий statement. Child INSERT statements не нужны для lineage — все WRITES_TO рёбра создаются от одного `DaliStatement(INSERT_MULTI)`. | `HOUND_PLSQL_LINEAGE_GAPS_S2.md §7` |
| **Q32** | **LATERAL scope — markHasLateral vs. registerLateralScope.** Решение: `ScopeManager.markHasLateral(stmtGeoid)` (Set<String>) вместо запланированного `registerLateralScope(inner, outer)` (Map<String,String>). Причина: inner subquery geoid создаётся ПОСЛЕ обнаружения LATERAL токена (при enterTable_ref_aux_internal_one outer scope ещё активен, inner ещё не создан). NameResolver стратегия S9 — deferred Sprint 3. | `HOUND_PLSQL_LINEAGE_GAPS_S2.md §12` |
| **Q33** | **WITH FUNCTION detection via parent context.** Решение: `ctx.parent instanceof PlSqlParser.With_clauseContext` в существующем `enterFunction_body` вместо несуществующего `enterWith_function_definition`. Причина: грамматика не имеет отдельного With_function_definition правила — WITH FUNCTION body парсится через стандартный `function_body`. `routine_type="INLINE_FUNCTION"` сохраняется в DaliRoutine. Geoid `OUTER_STMT:INLINE_FUNC:NAME` — deferred Sprint 3. | `HOUND_PLSQL_LINEAGE_GAPS_S2.md §14` |
| **Q34** | **@dblink stripping — cleanIdentifier vs. ParsedTableRef.** Решение: атрибут `@DBLINK` обрезается прозрачно в `BaseSemanticListener.cleanIdentifier()`. Альтернатива (`ParsedTableRef record`, `TableInfo.dblink`, `ensureRemoteTable()`) отклонена в Sprint 2 — geoid таблицы с dblink корректен без хранения dblink. Полная поддержка (хранение dblink как атрибута DaliTable) — Sprint 3. | `HOUND_PLSQL_LINEAGE_GAPS_S2.md §9` |
| **Q35** | **Dali YGG stats — explicit SQL queries vs. grouped status map.** Решение: `countAtoms(where)` с явным WHERE вместо `atomsByStatus.getOrDefault(...)`. Причина: `statement_geoid='unattached'` не является статусом — нельзя обработать через GROUP BY status. `atomsResolved` теперь = `status IN ('Обработано','constant')`, `atomsUnresolved` = `status IS NULL OR status NOT IN [...] OR statement_geoid='unattached'`. | `YggStatsResource.java`, `DaliPage.tsx` |

---

## 🚫 Сознательно отложено

| # | Что | Когда |
|---|---|---|
| D1 | URD frontend + backend + middleware | post-HighLoad |
| D2 | SKULD frontend + backend + middleware | post-HighLoad |
| D3 | MUNINN, HUGINN, BIFROST | post-HighLoad |
| D4 | Storage Automation product (второй продукт AIDA) | post-HighLoad |
| D5 | Multi-graph в YGG (DEV/STAGE/PROD изоляция) | post-HighLoad |
| D6 | Production observability (Prometheus, Grafana, Loki, Jaeger) | post-HighLoad |
| D7 | Multi-tenancy, billing, SOC2 | commercial post-MVP |
| D8 | Native интеграции (dbt, Airflow, Snowflake) | 2027 |
| D9 | Cloud deployment | после first pilot customer |
| **D10** | **ygg.db fork с Arrow Flight native layer** | **H2 2027+ (strategic vector)** |

---

## 📁 Структура документов AIDA architecture

| Файл | Назначение | Статус |
|---|---|---|
| `MODULES_TECH_STACK.md` | **Главный архитектурный документ** — все модули, tech stacks, ADR, стратегические заметки, open questions | v2.2 |
| `INTEGRATIONS_MATRIX.md` | Матрица 35 интеграций I1-I35 с типами, протоколами, use case timelines | v1.0 |
| `REFACTORING_PLAN.md` | План C.0-C.5 — что нужно изменить в существующей кодовой базе | v1.0 |
| `DECISIONS_LOG.md` | Этот файл — quick reference | v1.0 |
| `ARCH_11042026_06-30_SPEC_AIDA_HIGHLOAD_DEMO.md` | Техническая спецификация (⚠️ устарела по терминологии) | v1.0 |
| `ARCH_11042026_06-30_REVIEW_AIDA_HIGHLOAD_DEMO.md` | Архитектурное ревью (⚠️ устарел по терминологии) | v1.0 |

---

## 🎯 Critical path summary

**Week 1 (prerequisite):**
- C.0 Hound тесты → network mode (~1-3 дня, **пересмотрен** — remote уже на 26.x) — блокирует Dali engineering
- C.2.1 SHUTTLE SQL injection fix (~2 часа) — делать немедленно

**Week 2-3:**
- C.1 Hound library refactor (~4 дня)
- C.5.2 Keycloak client rename (~полдня)

**Week 3-8:** Dali Core + HEIMDALL backend engineering (main work)

**Week 9-12:** VERDANDI views + HEIMDALL frontend + MIMIR + ANVIL

**Week 13-16:** Integration testing, demo rehearsals, polish

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 11.04.2026 | 1.0 | Initial decisions log после большой integration session (v2.2 MODULES_TECH_STACK). 11 зафиксированных решений, 28 открытых вопросов, 10 отложенных items. |
| 12.04.2026 | 1.1 | **ArcadeDB ситуация уточнена.** HoundArcade (remote/network) уже на 26.3.2 — production path актуален. Embedded ArcadeDB в Hound — **только тесты** (25.12.1), не production. C.0 переформулирован: не «upgrade с нуля», а «тесты Hound → network mode». Effort пересмотрен: 5-8 дней → 1-3 дня. Добавлено решение #12: ArcadeDB version policy (no mixed versions). Critical path обновлён. |
| 12.04.2026 | 1.9 | **Demo dataset CLOSED.** 300 собственных PL/SQL файлов (1K–20K строк, ~1-3M LoC). Превышает 500K target. Анонимизация не нужна. |
| 13.04.2026 | 2.1 | **RBAC_MULTITENANT.md v1.0** — 8 ролей в 3 тирах (user/tenant/platform). Keycloak scopes: seer:read/write, aida:harvest/audit/tenant:admin/tenant:owner/admin/superadmin. Chur trusted headers X-Seer-*. TenantContext CDI. HeimdallEvent schema v2 (tenantId) — breaking change, координировать post-HighLoad. Q-MT1..Q-MT5 открыты. До HighLoad: single-tenant + полная матрица ролей. Multi-tenant — post-HighLoad. |
| 12.04.2026 | 2.0 | **M2/Prem2 batch.** Hound C.0.1: EmbeddedWriter удалён, только REMOTE/REMOTE_BATCH, ArcadeDB 26.3.1 (ADR-DA-011 CONFIRMED). T-K0.2 done (.env.k8s.example). HEIMDALL S3+: DaliPage/UsersPage skeleton, dashboardStore, Vite 8 + MF 1.14.2. aida-shared tokens.css синхронизирован. CI jobs добавлены. |
| 12.04.2026 | 1.9 | **Hound BULK COLLECT lineage DONE** (PR #4, 99ede77). G6-EXT FORALL INSERT → RECORD_USED_IN. G8 FETCH BULK COLLECT → RecordInfo. DaliRecordField vertex + HAS_RECORD_FIELD. Corpus 190 файлов: 164 processed, DaliRecord 0→54, RECORD_USED_IN 3✅. Беклог: G7 SELECT*/DDL, G10 cursor loop, G11 FIELD_MAPS_TO. |
| 12.04.2026 | 1.8 | **Q5 Co-founder split CLOSED.** Ты → Track A (Hound+Dali+ANVIL+benchmarks). Соавтор → Track B (LOOM+MIMIR+HEIMDALL+demo rehearsal). |
| 12.04.2026 | 1.7 | **Hound C.1.0 DONE.** Все B1-B7 применены. Доп. фиксы: Fix1 orphaned DaliColumn guard (3 точки в UniversalSemanticEngine), Fix2 double-schema geoid strip (StructureAndLineageBuilder), Fix3 HAS_COLUMN для pool-cached колонок — prefix sweep по rid.columns (RemoteWriter write+writeBatch), Fix4 UNBOUND false-positive только для физических таблиц (JsonlBatchBuilder), Fix5 удалены 4 unused schema types, Fix6 TRUNCATE UNSAFE вместо DELETE loop (~45s → ~2s). |
| 12.04.2026 | 1.6 | **M1 ALL TRACKS DONE.** Track 2 (SHUTTLE): MutationResource, HeimdallControlClient, HeimdallEventBus (Mutiny BroadcastProcessor hot fan-out), SubscriptionResource + HeimdallEventView (Jackson Map→JSON). Track 3 (Chur): requireAdmin.ts, WS proxy /heimdall/ws/events @fastify/websocket@^9. Track 4 (Shell): MF host port 5175, AidaNav, shellStore+navigateTo (ADR-DA-013), RemoteErrorBoundary, NavigateBridge, verdandi MF remote, i18n, Docker. T1.9 Dockerfile docker-compose 25174:5174. |
| 12.04.2026 | 1.5 | **Track 1 HEIMDALL frontend DONE.** packages/aida-shared/ создан (tokens.css, types HeimdallEvent/MetricsSnapshot/AppContext, initTheme). heimdall-frontend полный scaffold: useEventStream/useMetrics/useControl, Nivo charts, react-virtuoso EventLog, 3 страницы, LoginPage, ProtectedRoute, toolbar (⌘K/i18n/palette/theme). Arch: requireAdmin = role===admin (scope-based Sprint 4). SHUTTLE subscriptions = SmallRye Multi<T> in-process. C.5.2 Keycloak rename done. |
| 12.04.2026 | 1.4 | **HEIMDALL Sprint 2 DONE.** Решение #15 добавлено. R1/R2 закрыты. EventFilter 4 типа, FriggGateway, Chur proxy, HeimdallEmitter в SHUTTLE. |
| 12.04.2026 | 1.3 | **Q12 и Q13 закрыты.** Q12: Recharts via shadcn/charts (shadcn/ui уже в стеке → zero-config). Q13: Native WebSocket для HEIMDALL frontend (I34), graphql-ws остаётся в VERDANDI (I33). INTEGRATIONS_MATRIX I34 обновлён. |
| 12.04.2026 | 1.2 | **Frontend architecture зафиксирована.** ADR-DA-012: single domain + path routing (`seer.studio/verdandi`, `/urd`, `/skuld`, `/heimdall`). ADR-DA-013: URL-based context passing (ArcadeDB geoid как canonical ID, `navigateTo` + `useAppContext` в `aida-shared`). `aida-shared` scope L2. Решения #13 и #14 добавлены. B1 для demo, B2 post-HighLoad. |
| 14.04.2026 | 2.2 | **Lineage Gaps Sprint 2 DONE.** Q31–Q35 добавлены. 14 KI items реализованы, 3 (JSON, XML, NESTREC) → Sprint 3 backlog. Bugfix Dali YGG stats atomsResolved/atomsUnresolved. |
