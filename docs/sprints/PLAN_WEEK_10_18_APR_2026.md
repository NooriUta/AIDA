# План на неделю 10–18 апреля 2026

**Обновлено:** 09.04.2026
**Sprint:** 7 — Phase 4: Polish + Performance + DevOps
**Статус Sprint 7:** Блок 1 (Надёжность) — 100% завершён

---

## Итоги 08–09 апреля

Блок 1 (Надёжность) Sprint 7 завершён полностью:
- ✅ R-01: ErrorBoundary с resetKey + i18n fallback
- ✅ R-02: Toast для ошибок экспорта
- ✅ R-03: CORS allowlist (Chur)
- ✅ R-04: Rate limiting /auth/login (5/15m prod)
- ✅ R-05: JWT refresh — /auth/refresh endpoint

---

## Блок 2: Тестирование — 🔴 Must Have (~20 ч)

### Четверг 10 апреля

| # | Задача | Оценка | Детали |
|---|--------|--------|--------|
| T-01a | **Setup SHUTTLE test infra** | 2 ч | Добавить JUnit 5 + Quarkus test deps в build.gradle |
| T-01b | **Unit-тесты SearchService** | 2 ч | search() merge/sort, toResult(), edge cases (empty query, special chars) |
| T-01c | **Unit-тесты ExploreService** | 2 ч | ScopeRef.parse(), buildResult() — mock ArcadeGateway |

### Пятница 11 апреля

| # | Задача | Оценка | Детали |
|---|--------|--------|--------|
| T-01d | **Unit-тесты LineageService** | 2 ч | expandDeep depth capping, buildResult merge |
| T-01e | **Unit-тесты KnotService** | 2 ч | Session list, report assembly |

### Понедельник 14 апреля

| # | Задача | Оценка | Детали |
|---|--------|--------|--------|
| T-02 | **Chur unit tests** | 4 ч | fastify.inject: auth flow, RBAC guard (isWriteQuery), rate limiter logic |
| T-03 | **verdandi hook tests** | 4 ч | useGraphData, useExpansion, useDisplayGraph (mock RQ + store) |

### Вторник 15 апреля

| # | Задача | Оценка | Детали |
|---|--------|--------|--------|
| T-04 | **E2E Playwright smoke** | 6 ч | Setup + 1 scenario: login → L1 overview → double-click DB → L2 explore → select node → inspect → export PNG |

---

## Блок 3: Performance — 🟡 Should Have (~9 ч)

### Среда 16 апреля

| # | Задача | Оценка | Детали |
|---|--------|--------|--------|
| P-01 | **ELK Web Worker — production** | 4 ч | Spike в elkWorker.ts → production path: postMessage/onmessage, fallback on error |
| P-02 | **displayGraph memoization** | 2 ч | useMemo с shallow compare filter state в useDisplayGraph |

### Четверг 17 апреля (утро)

| # | Задача | Оценка | Детали |
|---|--------|--------|--------|
| P-03 | **Load test 500+ нод** | 2 ч | Ручной тест: найти schema с 500+ нод, замерить time-to-layout |
| P-04 | **React Query staleTime tuning** | 1 ч | ✅ Уже сделано (retry:2, staleTime:30s) — только проверить |

---

## Блок 4: DevOps — 🟡 Should Have (~8 ч)

### Четверг 17 апреля (после обеда) + Пятница 18 апреля

| # | Задача | Оценка | Детали |
|---|--------|--------|--------|
| D-01 | **Docker Compose** | 4 ч | `docker-compose.yml`: ArcadeDB + SHUTTLE + Chur + verdandi |
| D-02 | **GitHub Actions CI** | 3 ч | `.github/workflows/ci.yml`: lint + vitest + gradle test + build on PR |
| D-03 | **Environment profiles** | 1 ч | `.env.example` с документацией всех env vars |

---

## Quick Wins (по мере возможности)

| # | Задача | Оценка | Приоритет |
|---|--------|--------|-----------|
| QW-01 | Удалить 3 proto-файла из `layout/proto/` | 15 мин | Можно в любой момент |
| QW-02 | Удалить/перенести `mockData.ts` | 10 мин | Можно в любой момент |
| QW-03 | Убрать default JWT secret | 10 мин | До Docker Compose |
| QW-04 | Убрать default ArcadeDB password | 10 мин | До Docker Compose |
| QW-05 | Добавить `hasMore` в ExploreResult | 1 ч | При работе с SHUTTLE |

---

## Календарь

```
Чт  10.04:  T-01a/b/c  SHUTTLE test setup + SearchService + ExploreService tests
Пт  11.04:  T-01d/e    LineageService + KnotService tests
Пн  14.04:  T-02/T-03  Chur tests + verdandi hook tests
Вт  15.04:  T-04       E2E Playwright smoke test
Ср  16.04:  P-01/P-02  ELK Web Worker production + memoization
Чт  17.04:  P-03 + D-01 Load test + Docker Compose
Пт  18.04:  D-02/D-03  GitHub Actions CI + env profiles
```

---

## Метрики (цели к 18.04)

| Метрика | Сейчас | Цель |
|---------|--------|------|
| Unit тест-файлов | 2 | 12+ |
| Тестовое покрытие | ~5% | ≥ 30% |
| E2E сценариев | 0 | 1 |
| Error Boundary | ✅ да | да |
| Rate limiting | ✅ да | да |
| JWT refresh | ✅ да | да |
| Docker Compose | нет | да |
| CI pipeline | нет | да |
| ELK на Worker | spike | production |

---

## Риски

| Риск | Mitigation |
|------|-----------|
| ArcadeDB в тестах — нет Testcontainers image | Mock ArcadeGateway; integration tests позже |
| Playwright setup на Windows + JetBrains Node | Использовать system Node; npx playwright install |
| Docker Compose: SHUTTLE native image долго собирается | Использовать JVM mode для dev, native для CI |
| 500+ нод load test — нет подходящей схемы | Использовать реальные данные Hound; если нет — synthetic generator |
