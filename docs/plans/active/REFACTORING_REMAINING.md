# Refactoring Plan — Незакрытые задачи

**Документ:** `REFACTORING_REMAINING`
**Версия:** 1.0
**Дата:** 16.04.2026
**Статус:** ACTIVE — замена удалённого REFACTORING_PLAN.md
**Источник:** Sprint M2 plan + фактический прогресс

> Оригинальный `REFACTORING_PLAN.md` был удалён как "completed", но ряд задач
> C.3–C.5 остался незакрытым. Этот документ фиксирует остаток.

---

## Статус C.* задач

### Track A — C.0 + C.1: Hound library (M2 prerequisite)

| ID | Задача | Статус |
|----|--------|--------|
| C.0.1 | Hound тесты → network mode (ArcadeDB 26.x) | ✅ DONE |
| C.0.2 | `arcadedb-engine` обновить в build.gradle | ✅ DONE |
| C.0.3 | Regression тесты Cypher queries | ✅ DONE |
| C.1.0 | Bug fixes Hound (до изменений) | ✅ DONE |
| C.1.1 | `HoundSession` / `HoundResult` publish-ready API | ✅ DONE |
| C.1.2 | `HoundConfig` typed POJO → `shared/dali-models/` | ✅ DONE |
| C.1.3 | `HoundEventListener` interface | ✅ DONE |
| C.1.4 | Gradle `java-library` plugin | ✅ DONE |
| C.1.5 | `REMOTE_BATCH` как internal detail | ✅ DONE |

### Track B — Dali Core

| ID | Задача | Статус |
|----|--------|--------|
| DA-01 | Quarkus проект `services/dali` | ✅ DONE |
| DA-02 | JobRunr 7.3.0 setup | ✅ DONE |
| DA-03 | `ArcadeDbStorageProvider` (FRIGG persistence) | ✅ DONE |
| DA-04 | UC1 — scheduled harvest | ✅ DONE |
| DA-05 | UC2b — event-driven parse (REST + SHUTTLE integration) | ✅ DONE (PR #14, Apr 16) |
| DA-06 | docker-compose entry + settings.gradle | ✅ DONE |

### Track C — SHUTTLE REST clients

| ID | Задача | Статус | PR |
|----|--------|--------|-----|
| C.2.2 | SHUTTLE mutations (startParseSession, cancelSession, stubs) | ✅ DONE | PR #14 |
| C.2.4 | DaliClient (REST client к Dali) | ✅ DONE | PR #14 |
| C.2.5 | MimirClient (REST client к MIMIR) | ⏳ stub → C.2.3 pending | — |

### Track C — Chur scopes (C.3)

| ID | Задача | Статус | Блокирует |
|----|--------|--------|-----------|
| C.3.1 | `requireScope()` по JWT scopes (сейчас role-only) | ❌ НЕ НАЧАТ | RBAC production |
| C.3.2 | Scope-based routing для `aida:harvest`, `seer:write` | ❌ НЕ НАЧАТ | Dali access control |
| C.3.3 | Token refresh flow (когда истекает KC session) | ⏳ частичный | Session stability |

**Описание:** Chur сейчас проверяет `role === 'admin'` (M1 упрощение). Для production
нужна полная проверка JWT scopes через `requireScope('aida:harvest')` и т.д.

### Track C — VERDANDI saved views (C.4)

| ID | Задача | Статус | Блокирует |
|----|--------|--------|-----------|
| C.4.1 | `saveView` GraphQL mutation → FRIGG storage | ❌ stub (GraphQLException) | LOOM bookmarks |
| C.4.2 | `deleteView` mutation | ❌ stub | — |
| C.4.3 | GET /views endpoint в Chur + SHUTTLE | ❌ НЕ НАЧАТ | View list UI |
| C.4.4 | ViewsPanel в VERDANDI (список сохранённых views) | ❌ НЕ НАЧАТ | — |

**Зависит от:** FRIGG UserPrefs schema (уже есть) + SHUTTLE `saveView` реализации.

### Track C — Infrastructure (C.5)

| ID | Задача | Статус | Блокирует |
|----|--------|--------|-----------|
| C.5.1 | k8s manifests (базовые) | ❌ НЕ НАЧАТ | production deploy |
| C.5.2 | CI pipeline (GitHub Actions) | ⏳ частичный (docker-compose CI есть) | automated QG |
| C.5.3 | Secrets management (Vault / k8s secrets) | ❌ НЕ НАЧАТ | production security |
| C.5.4 | Monitoring (Prometheus + Grafana или LGTM) | ❌ НЕ НАЧАТ | SLA |

---

## Приоритеты до M3 (31 июля 2026)

| Задача | Срок | Причина |
|--------|------|---------|
| C.3.1 — Chur full scopes | до M3 | Security: admin-only сейчас пустой контроль |
| C.4.1 — saveView | до M3 | LOOM bookmarks — обещали в demo |
| C.2.5 — MimirClient stub → spec | до M3 | C.2.3 зависимость |
| C.5.2 — CI pipeline полный | до M3 | автоматические QG проверки |

---

## Открытые решения (из REFACTORING_PLAN)

| Q | Вопрос | Текущий статус |
|---|--------|----------------|
| Q29 | MIMIR через ArcadeDB MCP? | Re-eval май 2026. MIMIR_SPEC.md написан. |
| Q30 | ANVIL через ArcadeDB 72 алгоритма? | Re-eval май 2026. ANVIL_SPEC.md написан. |
| Q31 | UC2a — batch preview с embedded ArcadeDB? | Отложено post-M3. |

---

## Документация (статус)

| Документ | Статус |
|----------|--------|
| `docs/architecture/YGG_SCHEMA_REFERENCE.md` | ✅ Создан 16.04.2026 |
| `docs/guides/ONBOARDING.md` | ✅ Создан 16.04.2026 |
| `docs/architecture/hound/HOUND_POSTGRESQL_SPEC.md` | ✅ Создан 16.04.2026 (DRAFT) |
| `docs/quality-gates/QG-PERFORMANCE-weekly.md` | ✅ Создан 16.04.2026 |
| `docs/research/RAG_VS_PARSE_HYPOTHESIS.md` | ✅ Создан 16.04.2026 |
| `docs/plans/MIMIR_SPEC.md` | ✅ Создан 16.04.2026 (DRAFT) |
| `docs/plans/ANVIL_SPEC.md` | ✅ Создан 16.04.2026 (DRAFT) |
| `docs/plans/SKADI_SPEC.md` | ✅ Создан 16.04.2026 (CONCEPT) |
| `docs/guides/STARTUP_SEQUENCE.md` | ✅ Создан 15.04.2026 |
| `docs/guides/DEMO_SCRIPT.md` | ✅ Создан 16.04.2026 |
| `docs/ABSTRACT_HIGHLOAD_2026.md` | ✅ Создан 16.04.2026 |

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 16.04.2026 | 1.0 | Создан как замена удалённого REFACTORING_PLAN.md. C.0–C.2 статусы зафиксированы. C.3 (Chur scopes), C.4 (VERDANDI views), C.5 (infra) — незакрытые задачи описаны. |
| 16.04.2026 | 1.1 | Добавлена таблица документации — 11 файлов созданы. Q30/Q29 статусы обновлены. |
