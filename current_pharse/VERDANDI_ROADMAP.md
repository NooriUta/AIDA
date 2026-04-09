# VERDANDI — Полный план проекта (Roadmap)

**Обновлено:** 09.04.2026
**Статус:** Phase 4 — Sprint 7 в процессе (Блок 1 завершён)
**Текущий коммит:** `2cd57bd` (master)

---

## Общий прогресс

```
Phase 1    ████████████████████  100%  Scaffold + Mock Data          03.04
Phase 1.5  ████████████████████  100%  Auth + i18n                   04.04
Phase 2    ████████████████████  100%  Quarkus + RBAC + Real Data    04.04
Phase 3    ████████████████████  100%  Core Features                 07.04
Sprint 6   ████████████████████  100%  Refactor + Tests + Worker     08.04
Sprint 7   █████░░░░░░░░░░░░░░░  25%  Polish + Perf + DevOps        ⟵ мы здесь
Phase 5    ░░░░░░░░░░░░░░░░░░░░    0%  ANVIL ипостась
```

---

## Phase 1 — Scaffold + Mock Data ✅

**Дата:** 03.04.2026 | **Задачи:** LOOM-001 – LOOM-011 (11/11)

Vite + React 19 + TypeScript, React Flow canvas, ELK.js layout, dark theme, 5 палитр, breadcrumb, minimap, node types.

---

## Phase 1.5 — Auth + i18n ✅

**Дата:** 04.04.2026 | **Задачи:** LOOM-012 – LOOM-014 (3/3)

Chur auth gateway, JWT httpOnly cookies, LoginPage, ProtectedRoute, react-i18next (EN/RU).

---

## Phase 2 — Quarkus + RBAC + Real Data ✅

**Дата:** 04.04.2026 | **Задачи:** LOOM-015 – LOOM-022 (все)

Quarkus GraphQL, ArcadeGateway, 7 services, RBAC proxy, real data pipeline.

---

## Phase 3 — Core Features ✅

**Дата:** 04–07.04.2026 | **Задачи:** 12/12 ✅

| Задача | Описание | Дата |
|--------|----------|------|
| LOOM-023 | Canvas read-only mode | 04.04 |
| LOOM-023b | Filter Toolbar L2/L3 | 05.04 |
| LOOM-024 | L1 ApplicationNode + 3-level canvas | 05.04 |
| LOOM-024b | FilterToolbarL1 (hierarchy filters) | 05.04 |
| LOOM-025 | Column Mapping L2 + jumpTo | 05.04 |
| LOOM-026 | TableNode три состояния | 05.04 |
| LOOM-027 | Expand buttons upstream/downstream | 06.04 |
| LOOM-028 | KNOT Inspector panel | 07.04 |
| LOOM-029 | Context menu (right-click) | 07.04 |
| LOOM-030 | Export PNG/SVG | 07.04 |
| LOOM-032 | SearchPanel | 05.04 |
| KNOT-001 | KNOT Report inspector | 07.04 |

---

## Sprint 6 — Refactor + Tests + ELK Worker ✅

**Дата:** 08.04.2026 | **Задачи:** 5/5 ✅

| Задача | Результат |
|--------|-----------|
| S6-T1 | Delete L1NodesProto (618 LOC) |
| S6-T2 | Split transformGraph.ts (1086 → 4 modules + 42 LOC facade) |
| S6-T3 | Split LoomCanvas.tsx (970 → 346 LOC + 6 hooks) |
| S6-T4 | Vitest + 2 test files (transformHelpers, transformColumns) |
| S6-T5 | ELK Web Worker spike (elkWorker.ts) |

---

## Sprint 7 — Phase 4: Polish + Performance + DevOps 🔄

**Даты:** 08–18 апреля 2026

### Блок 1: Надёжность ✅ (100%)

| # | Задача | Статус |
|---|--------|--------|
| R-01 | Error Boundary | ✅ |
| R-02 | Export error UX (toast) | ✅ |
| R-03 | CORS hardening | ✅ |
| R-04 | Rate limiting /auth/login | ✅ |
| R-05 | JWT refresh | ✅ |

### Блок 2: Тестирование (10–15 апреля)

| # | Задача | Статус |
|---|--------|--------|
| T-01 | SHUTTLE unit tests (4 services) | ⬜ |
| T-02 | Chur unit tests (auth, RBAC, rate limit) | ⬜ |
| T-03 | verdandi hook tests (3 hooks) | ⬜ |
| T-04 | E2E Playwright smoke test | ⬜ |

### Блок 3: Performance (16 апреля)

| # | Задача | Статус |
|---|--------|--------|
| P-01 | ELK Web Worker → production | ⬜ (spike ready) |
| P-02 | displayGraph memoization | ⬜ |
| P-03 | Load test 500+ нод | ⬜ |
| P-04 | React Query retry + staleTime | ✅ (уже сделано) |

### Блок 4: DevOps (17–18 апреля)

| # | Задача | Статус |
|---|--------|--------|
| D-01 | Docker Compose | ⬜ |
| D-02 | GitHub Actions CI | ⬜ |
| D-03 | Environment profiles (.env.example) | ⬜ |

---

## Phase 5 — ANVIL ипостась

**Ориентировочно:** май 2026+

ANVIL — вторая ипостась SEER Studio. Предполагаемое назначение: редактирование и версионирование lineage-графа.

> ⚠️ Требования к ANVIL не зафиксированы.

| Компонент | Описание | Статус |
|-----------|----------|--------|
| A-01 | ANVIL routing + navigation | Планируется |
| A-02 | Editable canvas | Планируется |
| A-03 | SHUTTLE write endpoints | Планируется |
| A-04 | Version history + diff | Планируется |
| A-05 | Approval flow | Планируется |
| A-06 | Conflict detection | Планируется |

---

## Технический долг — бэклог

### Закрытый долг (всё до 09.04)

| # | Проблема | Закрыт |
|---|----------|--------|
| TD-01 | SQL injection в SearchService | ✅ 07.04 |
| TD-02 | Нет error handling в SHUTTLE | ✅ 07.04 |
| TD-03 | Нет error states в LoomCanvas | ✅ 08.04 |
| TD-04 | console.log в production | ✅ 07.04 |
| TD-05 | Expand buttons зависают | ✅ 07.04 |
| TD-06 | LoomCanvas монолит (970 LOC) | ✅ 08.04 |
| TD-07 | transformGraph монолит (1086 LOC) | ✅ 08.04 |
| TD-08 | Нет retry в React Query | ✅ 08.04 |
| TD-16 | L1NodesProto (618 LOC прототип) | ✅ 08.04 |
| TD-10 | Нет rate limiting | ✅ 08.04 |
| TD-18 | JWT refresh не реализован | ✅ 08.04 |
| SEC-CORS | CORS wildcard fallback | ✅ 08.04 |

### Открытый долг

| # | Проблема | Приоритет | Когда |
|---|----------|-----------|-------|
| TD-09 | FilterToolbar дублирует UI-примитивы | 🟡 | Sprint 8 |
| TD-11 | Hardcoded LIMIT без `hasMore` | 🟡 | Sprint 7/8 |
| TD-12 | ColumnInfo.type пустой (Hound) | 🟢 | Hound team |
| TD-13 | Accessibility (ARIA) | 🟢 | Sprint 8 |
| TD-14 | walkTree без защиты от циклов | 🟢 | Sprint 8 |
| TD-15 | Magic numbers | 🟢 | Sprint 8 |
| TD-17 | ArcadeDB/JWT default credentials | 🟡 | Sprint 7 D-03 |
| TD-19 | SearchPanel 470 LOC | 🟡 | Sprint 8 |
| TD-20 | loomStore 647 LOC | 🟡 | Sprint 8 |
| TD-21 | Proto files в layout/ | 🟢 | Sprint 7 QW |
| TD-22 | mockData.ts в services | 🟢 | Sprint 7 QW |

---

## Блокеры и внешние зависимости

| Блокер | Влияет на | Ответственный | Статус |
|--------|----------|---------------|--------|
| **HOUND-DB-001** — orphan DaliDatabase | L1 Overview | Hound team | ⬜ Не начат |
| **ArcadeDB Cypher UNION** | ExploreService | ArcadeDB vendor | 🔄 Обходной путь |
| **ColumnInfo.type** | TableNode/Inspector | Hound team | ⬜ Не начат |
| **ANVIL requirements** | Phase 5 | Product Owner | ⬜ Не зафиксировано |

---

## Метрики по фазам

| Фаза | Файлов (FE) | LOC (FE) | LOC (BE) | Тесты | Фичи |
|------|------------|---------|---------|-------|------|
| Phase 1 | ~25 | ~2,000 | 0 | 0 | Mock canvas |
| Phase 2 | ~45 | ~4,500 | ~1,100 | 0 | Real data |
| Phase 3 | ~65 | ~12,000 | ~2,000 | 0 | Full LOOM + KNOT |
| Sprint 6 (рефакторинг) | 76 | ~14,730 | ~2,553 | 2 файла | Hooks + modules |
| Sprint 7 (текущий) | 76 | ~14,730 | ~2,553 | 2 файла | Reliability |
| Цель Sprint 7 | ~78 | ~15,000 | ~3,000 | 12+ файлов | +tests +devops |
| Цель Phase 5 | ~100 | ~20,000 | ~4,000 | 30+ файлов | +ANVIL |
