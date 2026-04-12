# AIDA — Decisions Log (quick reference)

**Документ:** `DECISIONS_LOG`
**Версия:** 1.4
**Дата:** 11.04.2026
**Статус:** Working document — quick reference для навигации

Это **краткий** snapshot того что зафиксировано, что открыто, и что отложено. Для деталей смотри соответствующие документы.

---

## 📋 Quick status

**Зафиксированных решений:** 17
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
| ✅ **Q27** | **HoundConfig полная схема — ЗАКРЫТ** (13.04.2026, C.1.2) | |

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
| 12.04.2026 | 1.4 | **HEIMDALL Sprint 2 DONE.** Решение #15 добавлено. R1/R2 закрыты. EventFilter 4 типа, FriggGateway, Chur proxy, HeimdallEmitter в SHUTTLE. |
| 12.04.2026 | 1.3 | **Q12 и Q13 закрыты.** Q12: Recharts via shadcn/charts (shadcn/ui уже в стеке → zero-config). Q13: Native WebSocket для HEIMDALL frontend (I34), graphql-ws остаётся в VERDANDI (I33). INTEGRATIONS_MATRIX I34 обновлён. |
| 12.04.2026 | 1.2 | **Frontend architecture зафиксирована.** ADR-DA-012: single domain + path routing (`seer.studio/verdandi`, `/urd`, `/skuld`, `/heimdall`). ADR-DA-013: URL-based context passing (ArcadeDB geoid как canonical ID, `navigateTo` + `useAppContext` в `aida-shared`). `aida-shared` scope L2. Решения #13 и #14 добавлены. B1 для demo, B2 post-HighLoad. |
