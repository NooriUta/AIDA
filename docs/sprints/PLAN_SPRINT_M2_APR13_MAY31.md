# Sprint M2 — Dali Core (Apr 13 → May 31 2026)

**Ветка:** `feature/m2-dali-core`
**Milestone:** M2 — Dali функционален (31 мая 2026)
**Критерий готовности:** `startParseSession` → Dali → Hound → YGG → SHUTTLE отдаёт lineage. UC1 + UC2b работают.

---

## Статус на старт спринта (из M1)

| Компонент | Статус |
|-----------|--------|
| HEIMDALL frontend (React 19 MF remote :5174) | ✅ DONE |
| Shell MF host (:5175) + verdandi remote | ✅ DONE |
| SHUTTLE mutations + subscriptions (SmallRye) | ✅ DONE |
| Chur WS proxy + requireAdmin | ✅ DONE |
| Hound library (194 тестов, SchemaInitializer v26) | ✅ DONE |
| dali-models (shared: HeimdallEvent, EventType, EventLevel) | ✅ DONE |
| **Dali Core service** | ❌ не создан |
| **C.0 Hound тесты → network mode** | ❌ не начат |
| **C.1 Hound library refactor** | ❌ не начат |
| **FRIGG StorageProvider** | ❌ не создан |

---

## Открытые баги (из M1)

| ID | Баг | Файл |
|----|-----|------|
| BUG-ELK-001 | ELK cross-origin Worker — fix написан, E2E не верифицирован (DB нестабильна) | `docs/sprints/BUG_ELK_CROSS_ORIGIN_WORKER.md` |

---

## Треки спринта

### Track A — C.0 + C.1: Hound → сетевой режим + library refactor

**Блокирует:** Dali engineering

| ID | Задача | Effort |
|----|--------|--------|
| C.0.1 | Перевести Hound тесты на network mode (26.x ArcadeDB) | ~1 день |
| C.0.2 | Обновить `arcadedb-engine` в Hound build.gradle | ~0.5 ч |
| C.0.3 | Regression тесты Cypher queries | ~0.5 день |
| C.1.0 | Bug fixes в Hound (до любых других изменений) | ~1 день |
| C.1.1 | Publish-ready API (`HoundSession`, `HoundResult`) | ~1 день |
| C.1.2 | `HoundConfig` как typed POJO → `shared/dali-models/` | ~0.5 дня |
| C.1.3 | `HoundEventListener` interface | ~1 день |
| C.1.4 | Gradle `java-library` plugin | ~0.5 дня |
| C.1.5 | `REMOTE_BATCH` как internal detail | ~1 день |

### Track B — Dali Core skeleton

**Зависит от:** C.1 готов

| ID | Задача | Effort |
|----|--------|--------|
| DA-01 | Quarkus 3.34.2 проект `services/dali` | ~0.5 дня |
| DA-02 | JobRunr (Open Source) setup | ~1 день |
| DA-03 | FRIGG StorageProvider (~200-400 LoC) | ~3 дня |
| DA-04 | UC1 — scheduled harvest (JobRunr job → Hound → YGG) | ~3-4 дня |
| DA-05 | UC2b — event-driven parse (REST endpoint из SHUTTLE) | ~2 дня |
| DA-06 | `settings.gradle` + docker-compose entry для Dali | ~0.5 дня |

### Track C — SHUTTLE REST clients

| ID | Задача | Effort |
|----|--------|--------|
| C.2.4 | REST client to Dali Core (`DaliClient` Quarkus REST) | ~1 день |
| C.2.5 | REST client to MIMIR backend (stub) | ~0.5 дня |

### Track D — BUG-ELK-001 верификация (при стабильной БД)

| ID | Задача | Effort |
|----|--------|--------|
| BUG-ELK-001 | E2E верификация ELK cross-origin Worker fix в Shell | ~0.5 дня |

---

## M2 критерий (31 мая)

```
HTTP POST /graphql (startParseSession)
  → SHUTTLE mutation
  → DaliClient.startSession()
  → Dali UC2b job (JobRunr)
  → Hound (network mode, REMOTE_BATCH)
  → HoundArcade :2480 (YGG)
  → GraphQL subscription heimdallEvents → VERDANDI EventLog
```

---

## Open Questions (из roadmap)

| Q | Вопрос | Срок |
|---|--------|------|
| Q7 | HEIMDALL ↔ Dali control API формат | конец апреля |
| Q29 | MIMIR через ArcadeDB MCP? | май–июнь |
| Q30 | ANVIL через 72 built-in алгоритма? | май–июнь |
| Q31 | UC2a preview (6α vs 6β) | mid-May |
