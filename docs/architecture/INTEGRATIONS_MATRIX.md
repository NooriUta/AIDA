# AIDA — Матрица интеграций (Integration Matrix)

**Документ:** `INTEGRATIONS_MATRIX`
**Версия:** 1.0
**Дата:** 11.04.2026
**Статус:** Working document — партнёр `MODULES_TECH_STACK.md`

Этот документ описывает **контракты** между модулями AIDA: кто с кем взаимодействует, через какой протокол, какого типа, с каким смыслом. Детали events payload schemas выносятся в отдельный HEIMDALL Event Schema document когда будет готов.

---

## 1. Типы интеграций

| Тип | Значение | Примеры технологий |
|---|---|---|
| **C** | Call — синхронный RPC с возвратом значения | HTTP REST, gRPC, in-JVM call |
| **R** | Read — запрос данных из storage owner | GraphQL query, Cypher, HTTP REST |
| **W** | Write — изменение данных в storage | Native driver, embedded engine |
| **E** | Event — async эмиссия в event bus (fire-and-forget) | CDI events, Reactive Messaging, HTTP POST |
| **S** | Subscription — long-lived stream событий | WebSocket, SSE, graphql-ws |
| **Cmd** | Control command — async запрос с side-effect без возврата значения | REST POST, Reactive Messaging |

Легенда статуса: ✅ working | 🔵 new | 🔬 proposed | ⚠️ TBD

---

## 2. Матрица интеграций

### 2.1 Frontend → Backend (через Chur BFF)

| # | From | To | Type | Protocol | Tech | Status | Смысл |
|---|---|---|---|---|---|---|---|
| I1 | VERDANDI | Chur | C | HTTPS POST | fetch + Cookie | ✅ | «Залогиниться, получить data, logout» |
| I2 | VERDANDI | Chur | S | WebSocket | graphql-ws / native | 🔵 | «Подпишись на события сессии» |
| I3 | HEIMDALL frontend | Chur | C | HTTPS POST | fetch + Cookie | 🔵 | «Получить список sessions, metrics snapshot» |
| I4 | HEIMDALL frontend | Chur | S | WebSocket | graphql-ws | 🔵 | «Подпишись на all events для admin dashboard» |
| I5 | HEIMDALL frontend | Chur | Cmd | HTTPS POST | fetch + Cookie | 🔵 | «Reset / replay / cancel session» |

### 2.2 Chur → Backend services

| # | From | To | Type | Protocol | Tech | Status | Смысл |
|---|---|---|---|---|---|---|---|
| I6 | Chur | SHUTTLE | C (proxy) | HTTP POST | fetch + trusted headers | ✅ | «Проверь JWT, добавь X-Seer-User/Role, передай в SHUTTLE» |
| I7 | Chur | HEIMDALL backend | C (proxy) | HTTP POST | fetch + trusted headers | 🔵 | «Проксирование admin queries в HEIMDALL» |
| I8 | Chur | Keycloak | C | HTTP POST | jose + Direct Access Grants | ✅ | «Обменяй username/password на JWT» |
| I9 | Chur | Keycloak | C | HTTP GET | jose + JWKS | ✅ | «Проверь подпись JWT через public key» |

### 2.3 SHUTTLE → Core services

| # | From | To | Type | Protocol | Tech | Status | Смысл |
|---|---|---|---|---|---|---|---|
| I10 | SHUTTLE | HoundArcade (YGG) | R | HTTP REST | Quarkus REST client, Cypher/SQL | ✅ | Read queries по графу lineage |
| **I11** | SHUTTLE | Dali Core | Cmd | **HTTP REST (Вариант A)** | Quarkus REST client + shared Gradle module `dali-models` | 🔵 | startParseSession, control mutations. Dali и SHUTTLE — отдельные процессы в одном Gradle проекте |
| I12 | SHUTTLE | MIMIR backend | C | HTTP POST | Quarkus REST client | 🔵 | askMimir mutation |
| I13 | SHUTTLE | ANVIL backend | C | HTTP POST или in-JVM | ⚠️ TBD (см. Q26) | 🔵 | findImpact direct (если SHUTTLE напрямую, минуя MIMIR) |

**Нюанс I11 (зафиксировано):** Dali Core и SHUTTLE живут в одном Gradle multi-module проекте (shared dependencies, `dali-models` shared types), но runtime — **отдельные процессы**. Общаются через HTTP REST с Quarkus REST client. Это даёт independent failure domain, independent deployment/scaling, и единый механизм вызова Dali для всех клиентов (SHUTTLE + HEIMDALL).

**Нюанс I13:** ANVIL backend может вызываться двумя путями: через MIMIR как tool call (I22) или напрямую из SHUTTLE для UI button «Find Impact» без LLM. Оба пути могут сосуществовать. Q26 открыт.

### 2.4 Dali Core → Hound + External sources

| # | From | To | Type | Protocol | Tech | Status | Смысл |
|---|---|---|---|---|---|---|---|
| **I14** | Dali Core | Hound | C | **in-JVM method call** | Gradle `implementation project(':hound')` | 🔵 | «Распарси файл с HoundConfig, верни ParseResult» — direct Java call |
| I15 | Dali Core | External DB | R | JDBC | ⚠️ TBD driver | 🔵 PLANNED | Fetch SQL scripts из источника для UC1 harvest |
| I16 | Dali Core | file system | R | Java NIO | standard lib | 🔵 | Fetch .sql файлы из mount point |

**Критическое архитектурное решение в I14:** Dali вызывает Hound **как Java library**, не через subprocess, не через gRPC, не через HTTP. Hound — classpath зависимость в Gradle build Dali:

```gradle
// dali/build.gradle
dependencies {
    implementation project(':hound')  // Hound as Java library
    implementation 'io.quarkus:quarkus-core'
    implementation 'org.jobrunr:quarkus-jobrunr'
}
```

Это главное преимущество Quarkus+JobRunr выбора — нулевой integration cost для самого data-intensive pipeline компонента.

### 2.5 Hound → YGG (three modes policy)

Hound поддерживает **три режима записи** в YGG, управляемые через `HoundConfig.writeMode`. Ниже — три отдельные интеграции, выбор по UC.

| # | From | To | Type | Protocol | UC usage | Status |
|---|---|---|---|---|---|---|
| **I17a** | Hound | YGG (local file) | W | **embedded engine** (ArcadeDB embedded driver) | UC3 benchmark, UC4 offline bulk import, dev, UC2a preview | ✅ |
| **I17b** | Hound | HoundArcade (:2480) | W | **HTTP REST single** (ArcadeDB network adapter) | UC2b event-driven (один файл) | ✅ |
| **I17c** | Hound | HoundArcade (:2480) | W | **HTTP REST batch** (ArcadeDB network + buffering) | UC1 scheduled harvest, UC2a preview parse (default) | ✅ |

**Важно:** Hound **не пишет в YGG через Dali**. Dali оркеструет (передаёт HoundConfig с writeMode), но сам write — прямая Hound → YGG. Dali не write proxy.

### 2.6 Dali Core → FRIGG (JobRunr persistence)

| # | From | To | Type | Protocol | Tech | Status | Смысл |
|---|---|---|---|---|---|---|---|
| **I18** | Dali Core | FRIGG | W/R | **ArcadeDB driver** через custom StorageProvider | JobRunr API → custom adapter → ArcadeDB | 🔵 NEW | Persist jobs, state, history, recurring schedules |

**Это новая интеграция, зафиксирована в v2.2.** JobRunr по умолчанию не поддерживает ArcadeDB — нужен custom `StorageProvider` implementation (~200-400 LoC). Это часть Dali engineering scope.

### 2.7 MIMIR интеграции

| # | From | To | Type | Protocol | Tech | Status | Смысл |
|---|---|---|---|---|---|---|---|
| I19 | MIMIR backend | Anthropic Claude API | C | HTTPS POST | Anthropic SDK | 🔵 | LLM tier 1 — prompt → response/tool_call |
| I20 | MIMIR backend | Ollama (local) | C | HTTP POST | native HTTP | 🔵 | LLM tier 2 fallback — Qwen local |
| I21 | MIMIR backend | filesystem (cache) | R/W | standard lib | JSON file | 🔵 | Tier 3 fallback — cached response safety net |
| I22 | MIMIR backend | ANVIL backend | C (tool call) | HTTP или in-JVM | ⚠️ TBD | 🔵 | find_impact tool |
| I23 | MIMIR backend | SHUTTLE | C (tool call) | HTTP GraphQL | graphql-request | 🔵 | search_nodes tool — ищи по имени |
| I24 | MIMIR backend | HoundArcade (YGG) | R (tool call) | HTTP REST | direct Cypher | 🔵 | get_procedure_source, count_dependencies tools |

**Post-upgrade opportunity (Q29):** после ArcadeDB upgrade доступен built-in MCP Server. Потенциально можно упростить MIMIR до тонкой обёртки вокруг ArcadeDB MCP вместо custom tool framework (ADR-DA-002 пересмотреть). Re-evaluation в May-June.

### 2.8 ANVIL интеграции

| # | From | To | Type | Protocol | Tech | Status | Смысл |
|---|---|---|---|---|---|---|---|
| I25 | ANVIL backend | HoundArcade (YGG) | R | HTTP REST | Cypher traversal queries | 🔵 | Graph downstream/upstream на N hops |

**Post-upgrade opportunity (Q30):** ArcadeDB 26.3.1+ имеет 72 встроенных graph algorithms. Для `find_downstream_impact` можно использовать built-in вместо custom Cypher — упрощение ANVIL.

### 2.9 HEIMDALL event collection (cross-cutting)

**Важный нюанс:** Как именно компоненты отправляют events в HEIMDALL backend — **открытый вопрос (Q25)**. Варианты:
- **A:** HTTP POST (каждый эмиттер POST /events на HEIMDALL) — просто, надёжно, но overhead
- **B:** SmallRye Reactive Messaging in-memory channel — работает если все в одном Quarkus процессе (неприменимо для Hound как library)
- **C:** SmallRye Reactive Messaging с Kafka/Pulsar broker — production-grade, но +1 процесс
- **D:** Hybrid — CDI events in-process + HTTP POST bridge для cross-service

**Рекомендация:** начать с **A (HTTP POST)** для простоты, мигрировать на C в post-HighLoad при необходимости durability.

| # | From | To | Type | Protocol | Tech | Status | Смысл |
|---|---|---|---|---|---|---|---|
| I26 | Hound | HEIMDALL backend | E | CDI events → network (TBD bridge) | Q25 | 🔵 | parsing events: file_parsing_started, atom_extracted, resolution_completed |
| I27 | Dali Core | HEIMDALL backend | E | CDI events → network (TBD bridge) | Q25 | 🔵 | orchestration events: session_started, worker_dispatched, job_completed |
| I28 | MIMIR backend | HEIMDALL backend | E | CDI events → network (TBD bridge) | Q25 | 🔵 | LLM events: tool_call_started, tool_call_completed, llm_response_ready |
| I29 | ANVIL backend | HEIMDALL backend | E | CDI events → network (TBD bridge) | Q25 | 🔵 | algorithm events: traversal_started, traversal_progress, traversal_completed |
| I30 | SHUTTLE | HEIMDALL backend | E | CDI events → network (TBD bridge) | Q25 | 🔵 | API events: request_received, request_completed, subscription_opened |
| I31 | VERDANDI | HEIMDALL backend | E | WebSocket upstream | ⚠️ TBD | 🔬 low priority | UI events (optional): user_clicked_node, filter_changed |

### 2.10 HEIMDALL control commands (обратный канал)

| # | From | To | Type | Protocol | Tech | Status | Смысл |
|---|---|---|---|---|---|---|---|
| I32 | HEIMDALL backend | Dali Core | Cmd | HTTP POST или JobRunr API | direct REST | 🔵 | reset/replay/cancel running jobs |
| I33 | HEIMDALL backend | VERDANDI (clients) | S | WebSocket | graphql-ws через SHUTTLE subscriptions | 🔵 | Stream metrics и filtered events подписчикам (VERDANDI использует graphql-ws, не native) |
| I34 | HEIMDALL backend | HEIMDALL frontend | S | **Native WebSocket** `/ws/events` | raw JSON stream | 🔵 | Admin dashboard real-time event stream + metrics (✅ Q13 CLOSED) |

**Нюанс I33 vs I34:** SHUTTLE может быть средним узлом для VERDANDI WebSocket (VERDANDI подписывается через SHUTTLE, который проксирует от HEIMDALL). Или HEIMDALL может иметь свой WebSocket напрямую. Зависит от Q24 (HEIMDALL backend deployment).

### 2.11 Keycloak (auth infrastructure)

| # | From | To | Type | Protocol | Tech | Status | Смысл |
|---|---|---|---|---|---|---|---|
| I35 | All backend services | Keycloak | C | HTTP GET /certs | standard OIDC | ✅ | JWT validation через public keys |

---

## 3. Use case timelines

### 3.1 UC1 — Scheduled harvest

```
1. JobRunr cron trigger → Dali.triggerHarvest() @Scheduled
2. Dali → HEIMDALL: E complex_job_started {job_id, target_dbs: 12}
3. Loop для каждой БД (12 штук):
   - Dali → External DB: R fetch SQL scripts (JDBC)
   - Dali: enqueue subtask в JobRunr queue
4. JobRunr управляет 4 workers параллельно:
   - Worker: Dali → Hound C parse(files, config) in-JVM
   - Worker: Hound → YGG W write atoms + edges (REMOTE_BATCH)
   - Worker: Dali → HEIMDALL E subtask_completed {db, atoms}
5. Dali → HEIMDALL: E complex_job_progress {done: 3, total: 12}
6. HEIMDALL → frontend: S dashboard update "25%"
7. ... continue for remaining 9 DBs
8. Dali → HEIMDALL: E complex_job_completed {total_atoms: 400K}
9. HEIMDALL → frontend: S final metrics
```

### 3.2 MIMIR query — natural language question

```
1. User types question in VERDANDI
2. VERDANDI → Chur: POST /graphql askMimir(question)
3. Chur → SHUTTLE: proxy mutation
4. SHUTTLE → MIMIR: C askMimir(question, sessionId)
5. MIMIR → HEIMDALL: E mimir_query_received
6. MIMIR → Claude: C tool_use prompt with 5 tools
7. Claude → MIMIR: tool_call find_impact(node_id)
8. MIMIR → HEIMDALL: E tool_call_selected
9. MIMIR → ANVIL: C findDownstreamImpact(nodeId, maxDepth=5)
10. ANVIL → HEIMDALL: E traversal_started
11. ANVIL → YGG: R Cypher MATCH downstream
12. YGG → ANVIL: 23 affected columns
13. ANVIL → HEIMDALL: E traversal_completed
14. ANVIL → MIMIR: ImpactResult JSON
15. MIMIR → Claude: C synthesize response
16. Claude → MIMIR: NL response + highlight node_ids
17. MIMIR → HEIMDALL: E mimir_query_completed
18. MIMIR → SHUTTLE: MimirAnswer {text, highlight_ids}
19. Response chain back to VERDANDI
20. VERDANDI: LOOM highlights 23 nodes in red
```

### 3.3 HEIMDALL control command — Reset demo state

```
1. Admin clicks "Reset" button in HEIMDALL frontend
2. HEIMDALL frontend → Chur: POST /graphql resetDemo
3. Chur: scope check aida:admin:destructive
4. Chur → HEIMDALL backend: proxy command
5. HEIMDALL backend → Dali: Cmd POST /control/reset
6. Dali → JobRunr: delete all running jobs
7. Dali → JobRunr: clear job history
8. Dali → self: reset session counters
9. Dali → HEIMDALL: ResetResult {cleared: 15 jobs}
10. HEIMDALL: clear event ring buffer (10K)
11. HEIMDALL: reset metrics aggregators
12. HEIMDALL → Chur → frontend: success response
13. HEIMDALL → frontend: S broadcast "demo_reset" event
14. Frontend shows "Demo reset complete"
```

---

## 4. Открытые вопросы по матрице

| # | Вопрос | Влияет на |
|---|---|---|
| Q24 | HEIMDALL backend deployment (отдельный процесс vs embedded) | I7, I33, I34 |
| Q25 | Event bus transport (HTTP POST / Kafka / Reactive Messaging) | I26-I31 |
| Q26 | ANVIL direct из SHUTTLE или только через MIMIR | I13, I22 |
| Q27 | HoundConfig полная схема | I14 детали |
| Q31 | UC2a preview implementation (6α vs 6β) | Новые интеграции для preview lifecycle |

---

## 5. История изменений

| Дата | Версия | Что изменилось |
|---|---|---|
| 11.04.2026 | 1.0 | Initial draft матрицы. 35 known интеграций. I11 зафиксирован как HTTP REST с shared Gradle module. I14 зафиксирован как in-JVM call. I17a/b/c разделён на три моды. I18 новая интеграция для FRIGG как JobRunr persistence. Arrow Flight — strategic note в §8 архитектурного документа, не прорабатывается для October. |
