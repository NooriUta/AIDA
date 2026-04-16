# Sprint 7 — Phase 4: Polish + Performance + DevOps

**Создан:** 08.04.2026
**Даты:** 09–18 апреля 2026 (8 рабочих дней)
**Ёмкость:** ~48 ч

---

## Context

Phases 1–3 и Sprint 6 (рефакторинг) полностью завершены. Продукт функционально готов:
LOOM canvas, KNOT Inspector, поиск, контекстное меню, экспорт, auth, RBAC, i18n.
Критический tech debt (SQL injection, error handling) закрыт.

**Цель спринта:** Сделать проект стабильным, тестируемым и деплоимым.

---

## Блок 1: Надёжность (09–10 апреля) — 🔴 Must Have — ~11 ч

| # | Задача | Оценка | Файлы |
|---|--------|--------|-------|
| R-01 | **React Error Boundary** — обёртка App + LoomCanvas, fallback UI с retry | 3 ч | `App.tsx`, новый `ErrorBoundary.tsx` |
| R-02 | **Export error UX** — toast при ошибке PNG/SVG | 2 ч | `ExportPanel.tsx` |
| R-03 | **CORS hardening** — убрать fallback `*` в Chur | 1 ч | `Chur/src/server.ts` |
| R-04 | **Rate limiting** /auth/login — 5 / 15 мин на IP | 2 ч | `Chur/src/routes/auth.ts` |
| R-05 | **JWT refresh** — silent token renewal | 3 ч | `Chur/src/routes/auth.ts`, `authStore.ts` |

## Блок 2: Тестирование (11–14 апреля) — 🔴 Must Have — ~20 ч

| # | Задача | Оценка | Файлы |
|---|--------|--------|-------|
| T-01 | **Unit-тесты SHUTTLE** — ExploreService, LineageService, OverviewService | 6 ч | `SHUTTLE/src/test/java/` |
| T-02 | **Unit-тесты Chur** — auth, RBAC, query (fastify.inject) | 4 ч | `Chur/src/**/*.test.ts` |
| T-03 | **Unit-тесты verdandi hooks** — useGraphData, useExpansion, useLoomLayout | 4 ч | `verdandi/src/hooks/*.test.ts` |
| T-04 | **E2E smoke** (Playwright) — login → L1 → L2 → inspect → export | 6 ч | `verdandi/e2e/` |

## Блок 3: Performance (15–16 апреля) — 🟡 Should Have — ~9 ч

| # | Задача | Оценка | Файлы |
|---|--------|--------|-------|
| P-01 | **ELK Web Worker** — layout в Worker (spike есть) | 4 ч | `workers/elkWorker.ts`, `useLoomLayout.ts` |
| P-02 | **displayGraph memoization** | 2 ч | `useDisplayGraph.ts` |
| P-03 | **Load test 500+ нод** | 2 ч | ручной тест |
| P-04 | **React Query retry + staleTime** | 1 ч | `hooks.ts` |

## Блок 4: DevOps (17–18 апреля) — 🟡 Should Have — ~8 ч

| # | Задача | Оценка | Файлы |
|---|--------|--------|-------|
| D-01 | **Docker Compose** — весь стек | 4 ч | `docker-compose.yml`, Dockerfiles |
| D-02 | **GitHub Actions CI** — lint + test + build на PR | 3 ч | `.github/workflows/ci.yml` |
| D-03 | **Environment profiles** — `.env.example` | 1 ч | `.env.example` |

---

## Календарь

```
Ср  09.04:  R-01 Error Boundary + R-02 Export toast + R-03 CORS
Чт  10.04:  R-04 Rate limiting + R-05 JWT refresh
Пт  11.04:  T-01 SHUTTLE unit tests
Пн  14.04:  T-02 Chur tests + T-03 verdandi hook tests
Вт  15.04:  T-04 E2E Playwright smoke test
Ср  16.04:  P-01 ELK Web Worker + P-02 memoization
Чт  17.04:  P-03 Load test + P-04 React Query + D-01 Docker Compose
Пт  18.04:  D-02 GitHub Actions CI + D-03 env profiles
```

## Метрики (цели)

| Метрика | Сейчас | Цель |
|---------|--------|------|
| Unit тест-файлов | 4 | 12+ |
| Тестовое покрытие | ~5% | ≥ 30% |
| E2E сценариев | 0 | 1 |
| Error Boundary | нет | да |
| Rate limiting | нет | да |
| Docker Compose | нет | да |
| CI pipeline | нет | да |
| ELK на UI thread | да | нет (Worker) |
