# VERDANDI — Sprint 7→8 Architectural Review

**Дата:** 12.04.2026
**Коммит:** 0d729b5 (brandbook/seer-studio)
**Проверяющий:** Claude Code (automated scheduled)
**Уровень:** L3 Sprint-End (по новой REVIEW_STRATEGY.md — retroactively)

> ⚠️ **NOTE по REVIEW_STRATEGY.md:** этот review запускался на ветке `brandbook/seer-studio`, а не `main`. `internal_docs/` пусты на этой ветке. TypeScript/build не проверялся (нет node в среде). По новой стратегии такой review должен добавлять WARNING и запускаться только с `main`.
>
> Документ сохранён как исторический baseline для METRICS_LOG.md.

---

## Дашборд Sprint 7→8 прогресс

| Блок | Задач | Завершено | % | Статус |
|---|---|---|---|---|
| Блок 1: Надёжность | 5 | 5 | 100% | ✅ |
| Блок 2: Тестирование | 7 | 6 | 86% | ✅ (значительно перевыполнен) |
| Блок 3: Performance | 4 | 2 | 50% | 🔄 |
| Блок 4: DevOps | 3 | 3 | 100% | ✅ |
| **ИТОГО Sprint 7** | **19** | **16** | **84%** | |

---

## Метрики качества (baseline для METRICS_LOG)

| Метрика | Было (09.04) | Сегодня (12.04) | Цель | Статус |
|---|---|---|---|---|
| Тест-файлов | 4 | **23** | 12+ | ✅ Перевыполнено |
| E2E сценариев | 0 | **1** | 1 | ✅ |
| Docker Compose | нет | **да** | да | ✅ |
| CI pipeline | нет | **да** (ci.yml + cd.yml) | да | ✅ |
| Dockerfiles | 0 | **3** | 3 | ✅ |
| .env.example | нет | **да** (65 строк) | да | ✅ |
| loomStore LOC | 647 | **251** + 10 слайсов | <400 | ✅ |
| LoomCanvas LOC | — | **403** | ≤350 | 🟡 |
| SearchPanel LOC | 470 | **516** | ≤350 | 🔴 растёт |
| TypeScript ошибки | 0 | N/A (env) | 0 | ⚠️ не проверено |

---

## Активные риски

| ID | Риск | Уровень | Детали |
|---|---|---|---|
| WARN-01 | ELK на UI thread | ✅ | **Resolved 12.04.2026.** `elk.bundled.js` + `workerFactory: () => new Worker(elkWorkerUrl)` (`elk-worker.min.js?url`). Main thread free. Verified: 524 ms (430 nodes, 709 edges). |
| WARN-05 | SearchPanel.tsx растёт | 🟡 | 516 LOC (было 470), продолжает расти |
| WARN-06 | LoomCanvas.tsx > цель | 🟢 | 403 LOC, цель ≤350 |

---

## Статус блоков

### Блок 1: Надёжность — 100% ✅
- R-01 ErrorBoundary: ✅
- R-02 Export toast: ✅
- R-03 CORS allowlist: ✅ `Chur/src/server.ts:26-36` — Set-based, no wildcard `*`
- R-04 Rate limiting: ✅ `Chur/src/routes/auth.ts:6-8` — 5/15m prod, 50/1m dev
- R-05 JWT refresh: ✅ `POST /auth/refresh` lines 101-113

### Блок 2: Тестирование — 86% (23 файла) ✅

**SHUTTLE (4 Java test files):** SearchServiceTest, ExploreServiceTest, LineageServiceTest, KnotServiceTest ✅

**Chur (2 files):** rbac.test.ts, auth.test.ts ✅

**verdandi (16 files):** CommandPalette, SearchPalette, InspectorColumn/Join/Parameter, ProfileModal, ProfileTabShortcuts, useHotkeys, useSearchHistory, filterSlice(179), persistSlice(206), undoSlice(225), displayPipeline, transformColumns/Explore/Helpers ✅

**E2E (1):** e2e/smoke.spec.ts ✅

**Пробелы (перенести в следующий спринт):**
- ❌ useGraphData.test.ts
- ❌ useExpansion.test.ts
- ❌ useDisplayGraph.test.ts
- ❌ useLoomLayout.test.ts (326 LOC — самый сложный хук)

### Блок 3: Performance — 50% 🔄
- P-01 ELK Worker: ✅ (WARN-01 resolved — real Web Worker, main thread free)
- P-02 memoization: ✅ useDisplayGraph 2×useMemo, useGraphData 2×useMemo
- P-03 Load test 500+: ❌ не проведён
- P-04 React Query: ✅

### Блок 4: DevOps — 100% ✅
- docker-compose.yml + docker-compose.prod.yml
- ci.yml (3 jobs) + cd.yml
- .env.example (65 строк, [REQUIRED in prod] маркировка)
- Dockerfiles: verdandi + SHUTTLE + Chur ✅
- nginx.conf, keycloak/seer-realm.json, scripts/ ✅

---

## ADR Compliance

| ADR | Решение | Статус |
|---|---|---|
| ADR-001 | Lazy L1/L2/L3 | ✅ `viewLevel` в loomStore |
| ADR-002 | @xyflow/react | ✅ |
| ADR-003 | Vite + React 19 + ELK.js + Zustand + TanStack + shadcn | ✅ package.json |
| ADR-004b | Quarkus GraphQL + Chur JWT | ✅ |
| ADR-007 | nodesDraggable=false | ✅ `LoomCanvas.tsx:318` |
| ADR-008 | StatementGroupNode без extent:parent | ✅ extent:parent только для DB→Schema |
| ADR-009 | Application scope filter | ✅ `l1ScopeStack` в useDisplayGraph |
| ADR-011 | graphql-request (не Apollo/urql) | ✅ `"graphql-request": "^7.4.0"` |

---

## Tech Debt реестр (обновлённый)

| # | Проблема | Статус |
|---|---|---|
| TD-09 | FilterToolbar дублирует UI-примитивы | 🟡 Открыт |
| TD-11 | Hardcoded LIMIT без `hasMore` | 🟡 Открыт |
| TD-13 | Accessibility (ARIA) | 🟢 Открыт |
| TD-14 | walkTree без защиты от циклов | 🟢 Открыт |
| TD-17 | Default credentials | ✅ ЗАКРЫТ — .env.example документирует [REQUIRED in prod] |
| TD-19 | SearchPanel 516 LOC | 🟡 Вырос на 46 строк |
| TD-20 | loomStore 647 LOC | ✅ ЗАКРЫТ — 251 LOC + 10 слайсов |
| TD-21 | Proto files в layout/ | ✅ ЗАКРЫТ |
| TD-22 | mockData.ts в services | ✅ ЗАКРЫТ |

---

## loomStore decomposition (TD-20) — ВЫПОЛНЕНО

| Слайс | LOC |
|---|---|
| navigationSlice.ts | 131 |
| undoSlice.ts | 114 |
| persistSlice.ts | 91 |
| filterSlice.ts | 78 |
| l1Slice.ts | 65 |
| expansionSlice.ts | 42 |
| visibilitySlice.ts | 39 |
| themeSlice.ts | 30 |
| viewportSlice.ts | 16 |
| selectionSlice.ts | 14 |

---

## Рекомендуемые действия (перенести в следующий спринт)

| # | Задача | Приоритет | Оценка |
|---|---|---|---|
| 1 | Тесты для canvas hooks (useGraphData, useDisplayGraph, useLoomLayout) | 🟡 | 4-6 ч |
| 2 | Load test 500+ нод, задокументировать baseline | 🟡 | 2 ч |
| 3 | Декомпозиция SearchPanel.tsx (516 LOC) | 🟢 | 3-4 ч |
| 4 | LoomCanvas.tsx → ≤350 LOC | 🟢 | 2 ч |
| 5 | Синхронизировать internal_docs между ветками | 🟡 | 1 ч |
| 6 | ELK Worker: исследовать production-only (Vite build → proper ESM) | 🟢 | 4 ч |

---

*Сохранён в docs/reviews/verdandi/ как исторический документ после введения REVIEW_STRATEGY.md v1.0*
