# VERDANDI — Полное ревью кодовой базы

**Дата:** 09.04.2026
**Автор:** Claude Code (автоматическое ревью)
**Коммит:** `2cd57bd` (master)
**Предыдущее ревью:** 05.04.2026 (коммит `f8e31b8`)

---

## 1. Статус проекта

### Завершённые фазы

| Фаза | Статус | Дата завершения |
|------|--------|-----------------|
| Phase 1 — Skeleton + Mock Data | ✅ 100% | 03.04.2026 |
| Phase 1.5 — Auth + i18n | ✅ 100% | 04.04.2026 |
| Phase 2 — Quarkus + RBAC + Real Data | ✅ 100% | 04.04.2026 |
| Phase 3 — Core Features | ✅ 100% | 07.04.2026 |
| Sprint 6 — Refactor + Tests + ELK Worker | ✅ 100% | 08.04.2026 |
| Sprint 7 — Scaffold (Error Boundary, devAll) | 🔄 Начат | 08.04.2026 |

### Phase 3 — все задачи закрыты

| Задача | Статус |
|--------|--------|
| LOOM-023: Canvas read-only mode | ✅ |
| LOOM-023b: Filter Toolbar L2/L3 | ✅ |
| LOOM-024: L1 ApplicationNode + 3-level canvas | ✅ |
| LOOM-024b: FilterToolbarL1 | ✅ |
| LOOM-025: Column Mapping L2 + jumpTo | ✅ |
| LOOM-026: TableNode три состояния | ✅ |
| LOOM-027: Expand buttons upstream/downstream | ✅ |
| LOOM-028: KNOT Inspector (right panel) | ✅ |
| LOOM-029: Context menu (right-click) | ✅ |
| LOOM-030: Export PNG/SVG | ✅ |
| LOOM-032: SearchPanel | ✅ |
| KNOT-001: KNOT Report inspector | ✅ |

### Sprint 6 — рефакторинг + тесты

| Задача | Результат |
|--------|-----------|
| S6-T1: Удалить L1NodesProto | ✅ 618 LOC удалено |
| S6-T2: Split transformGraph.ts (1086 → 42 LOC facade) | ✅ → transformExplore, transformOverview, transformHelpers, transformColumns |
| S6-T3: Split LoomCanvas.tsx (970 → 346 LOC) | ✅ → 6 hooks (useGraphData, useExpansion, useDisplayGraph, useLoomLayout, useFitView, useFilterSync) |
| S6-T4: Vitest + unit tests | ✅ transformHelpers.test.ts, transformColumns.test.ts |
| S6-T5: ELK Web Worker spike | ✅ elkWorker.ts + layoutGraph.ts Worker integration |

### Sprint 7 — начало (scaffold)

| Задача | Результат |
|--------|-----------|
| R-01: Error Boundary | ✅ ErrorBoundary.tsx — обёртка App + resetKey на pathname |
| R-03: CORS hardening | ✅ Chur: allowlist вместо fallback `*` |
| R-04: Rate limiting /auth/login | ✅ 5/15 мин в prod, 50/1 мин в dev |
| R-05: JWT refresh | ✅ POST /auth/refresh — silent token renewal |
| devAll gradle task | ✅ Запуск SHUTTLE + Chur + verdandi одной командой |

---

## 2. Кодовая база — метрики

| Модуль | Файлов | LOC | Язык | Δ vs 05.04 |
|--------|--------|-----|------|------------|
| verdandi (frontend) | 76 | ~14,730 | TypeScript + React | +67% |
| SHUTTLE (backend) | 30 | ~2,553 | Java 21 + Quarkus | +131% |
| Chur (auth gateway) | 9 | ~505 | TypeScript + Fastify | -16% |
| **ИТОГО** | **115** | **~17,788** | — | **+69%** |

### Изменения в структуре (Sprint 6 рефакторинг)

```
Было (05.04):                          Стало (09.04):
transformGraph.ts      1086 LOC   →   transformGraph.ts       42  (re-export facade)
                                      transformExplore.ts    467
                                      transformOverview.ts   281
                                      transformHelpers.ts    146
                                      transformColumns.ts    127

LoomCanvas.tsx          970 LOC   →   LoomCanvas.tsx         346
                                      hooks/canvas/useGraphData.ts      110
                                      hooks/canvas/useExpansion.ts       88
                                      hooks/canvas/useDisplayGraph.ts   217
                                      hooks/canvas/useLoomLayout.ts     231
                                      hooks/canvas/useFitView.ts         45
                                      hooks/canvas/useFilterSync.ts     129
```

---

## 3. Ревью: закрытие проблем из ревью 05.04

### Критические (P1–P3) — все закрыты ✅

| # | Проблема (05.04) | Статус | Как решено |
|---|-------------------|--------|-----------|
| P1 | LoomCanvas.tsx — монолит 663 LOC | ✅ | Извлечены 6 custom hooks, LoomCanvas = 346 LOC |
| P2 | transformGraph.ts — монолит 789 LOC | ✅ | Split на 4 модуля + facade (42 LOC) |
| P3 | Нет обработки ошибок | ✅ | ErrorBoundary, statusKey UI, `.onFailure()` в SHUTTLE |

### Высокий приоритет (H1–H5)

| # | Проблема (05.04) | Статус | Как решено |
|---|-------------------|--------|-----------|
| H1 | console.log() в production | ✅ | Удалены. Остался 1 `console.warn` в layoutGraph (fallback — корректно) |
| H2 | Нет loading state для expansion | ✅ | Spinner overlay + large-graph hint |
| H3 | Нет защиты от спама expand | ✅ | stmtColsReady guard, `staleTime: 30_000` |
| H4 | Нет retry в React Query | ✅ | `retry: 2` на всех hooks |
| H5 | Дублирование FilterToolbar/L1 | 🟡 | Не решено — UI-примитивы всё ещё дублируются |

### Backend (SHUTTLE)

| # | Проблема (05.04) | Статус | Как решено |
|---|-------------------|--------|-----------|
| P1-BE | SQL injection в SearchService | ✅ | Named param binding: `Map.of("q", like)` |
| P2-BE | Нет `.onFailure()` | ✅ | `.onFailure().recoverWithItem(List.of())` на всех Uni-цепочках |
| P3-BE | ExploreService монолит | 🟡 | Остаётся крупным (~570 LOC), но внутри structured |
| H1-BE | Hardcoded LIMIT | 🟡 | 200–500 в Cypher, клиент не знает |
| H5-BE | SeerIdentity не валидирует роли | 🟡 | Доверяет Chur headers |

### Chur (auth gateway)

| # | Проблема (05.04) | Статус | Как решено |
|---|-------------------|--------|-----------|
| C1 | Пароли ArcadeDB в plaintext | 🟡 | Env fallback, но default `playwithdata` в config.ts |
| C2 | Нет refresh token | ✅ | POST /auth/refresh реализован |
| C3 | Нет rate limiting | ✅ | In-memory rate limiter, 5/15 мин, sweep каждые 5 мин |
| CORS | fallback `*` | ✅ | Allow-list из `CORS_ORIGIN` env |

---

## 4. Новые компоненты с 05.04

### Frontend — новые файлы

| Компонент | LOC | Назначение |
|-----------|-----|-----------|
| `ErrorBoundary.tsx` | 93 | Catch render errors + i18n fallback UI |
| `Toast.tsx` | — | Toast уведомления (export errors и т.д.) |
| `NodeContextMenu.tsx` | — | LOOM-029: right-click context menu |
| `ExportPanel.tsx` | — | LOOM-030: PNG/SVG export |
| `ZoomLevelContext.tsx` | — | LOD: compact rendering at low zoom |
| `InspectorPanel.tsx` + 5 sub | 337 | KNOT Inspector (schema/table/routine/stmt) |
| `KnotPage.tsx` + 5 sub | — | KNOT Report (sessions, routines, atoms) |
| `LegendButton.tsx` | — | Colour legend popup |
| `elkWorker.ts` | 23 | ELK layout in Web Worker |
| `transformColumns.ts` | 127 | Column enrichment transform |
| hooks/canvas/* (6 files) | 820 | Extracted from LoomCanvas |
| 2 test files | 369 | transformHelpers + transformColumns |

### Backend — новые файлы

| Файл | LOC | Назначение |
|------|-----|-----------|
| `KnotService.java` | ~600 | KNOT Report (sessions, structure, flow) |
| `KnotResource.java` | — | GraphQL endpoint for KNOT |
| 8 Knot model records | — | KnotSession, KnotReport, KnotStatement, etc. |

---

## 5. Текущие проблемы

### 🔴 Critical — нет

Все критические проблемы из ревью 05.04 закрыты.

### 🟡 High — 5 открытых

| # | Проблема | Файл | Рекомендация |
|---|----------|------|-------------|
| H1 | **FilterToolbar + FilterToolbarL1 дублируют UI** | layout/ | Извлечь shared toggle/select/pill компоненты |
| H2 | **SearchPanel.tsx — 470 LOC** | panels/ | Split на SearchInput, SearchResults, HiddenNodesPanel |
| H3 | **loomStore.ts — 647 LOC** | stores/ | Разделить на slices (navigation, filter, expansion, theme) |
| H4 | **ExploreService.java — ~570 LOC** | SHUTTLE | Один метод exploreSchema — декомпозировать по фазам |
| H5 | **Hardcoded LIMIT 200–500 в Cypher** | SHUTTLE | Клиент не знает о truncation — добавить `hasMore` флаг |
| H6 | **Нет `@RolesAllowed` на GraphQL resources** | SHUTTLE | Документация заявляет viewer+, но нет enforcement — relies on Chur |
| H7 | **Нет query timeout на ArcadeDB** | SHUTTLE | REST client без timeout — медленные запросы могут зависнуть |
| H8 | **1 failing test** | SHUTTLE | KnotService.deriveName() — strip logic bug (множественные расширения) |

### 🟢 Medium — 6 открытых

| # | Проблема | Детали |
|---|----------|--------|
| M1 | **ColumnInfo.type пустой** | Требует Hound fix |
| M2 | **Нет accessibility (ARIA)** | Кнопки без label, цвет без текста |
| M3 | **Magic numbers** | L2_MAX_COLS, MAX_PARTIAL_COLS — вынести в config |
| M4 | **3 proto-файла** в `layout/proto/` | Удалить или перенести в storybook |
| M5 | **ArcadeDB credentials default** | `playwithdata` в config.ts — убрать default |
| M6 | **Нет E2E тестов** | Playwright не настроен |

### ⚪ Low — 4

| # | Проблема | Детали |
|---|----------|--------|
| L1 | `mockData.ts` в src/services | Не используется в production — удалить или перенести |
| L2 | Header.tsx 350 LOC | Можно split на HeaderNavigation, HeaderToolbar |
| L3 | lineage.ts 455 LOC | Крупнейший service file — можно split на lineage + knot |
| L4 | Нет `unhandledrejection` handler | Promise rejections без ErrorBoundary → console error |

---

## 6. Тестирование

### Текущее состояние

| Метрика | Было (05.04) | Сейчас | Цель (Sprint 7) |
|---------|-------------|--------|-----------------|
| Тестовые файлы | 0 | 4 (2 FE + 2 BE) | 12+ |
| Unit тесты | 0 | ~55 assertions | 60+ |
| Покрытие | 0% | ~8% | ≥ 30% |
| E2E сценариев | 0 | 0 | 1 |
| Тестовый фреймворк | — | Vitest + JUnit 5 | + Playwright |

### Что протестировано

**Frontend (Vitest):**
- `transformHelpers.test.ts` — parseStmtLabel, extractStatementType, getEdgeStyle, makeNodeId
- `transformColumns.test.ts` — column enrichment transform pipeline

**Backend (JUnit 5):**
- `KnotServiceTest.java` (183 LOC, 33 tests) — static helper methods: parseStmtType, parseLineNumber, deriveName, atomLine/atomPos
- `SearchServiceTest.java` (60 LOC, 2 tests) — SQL template structure guards (no `%s` in templates)

### Известные проблемы в тестах

- `KnotServiceTest.deriveName_fromFilePath_windows()` — FAILING: ожидает `MY_PROC`, получает `MY_PROC.pck` (logic bug в KnotService.deriveName() — стрипает только последнее расширение)

### Что нужно протестировать (приоритет)

1. **SHUTTLE unit tests** — ExploreService, LineageService, SearchService, KnotService
2. **Chur unit tests** — auth flow, RBAC guard, rate limiter
3. **Canvas hooks** — useGraphData, useExpansion, useDisplayGraph
4. **E2E smoke** — login → L1 → L2 → inspect → export

---

## 7. Безопасность

### Закрытые уязвимости

| # | Уязвимость | Severity | Статус |
|---|-----------|----------|--------|
| SEC-01 | SQL injection в SearchService | 🔴 High | ✅ Закрыт — named params |
| SEC-02 | Stack trace leak (SHUTTLE) | 🟡 Medium | ✅ Закрыт — `.onFailure().recoverWithItem()` |
| SEC-03 | CORS wildcard fallback | 🟡 Medium | ✅ Закрыт — allowlist |
| SEC-04 | Brute-force /auth/login | 🟡 Medium | ✅ Закрыт — rate limiter |
| SEC-05 | JWT не обновляется | 🟡 Medium | ✅ Закрыт — /auth/refresh |

### Открытые замечания

| # | Замечание | Severity | Рекомендация |
|---|----------|----------|-------------|
| SEC-06 | JWT secret default `dev-secret-change-in-prod` | 🟡 Medium | Убрать default, требовать env |
| SEC-07 | ArcadeDB pass default `playwithdata` | 🟢 Low | Убрать default из config.ts |
| SEC-08 | SeerIdentity доверяет headers | 🟢 Low | Приемлемо для private network |
| SEC-09 | Depth в expandDeep — interpolation | 🟢 Info | Безопасно: `Math.max(1, Math.min(depth, 10))` |

---

## 8. Архитектура — текущая оценка

### Сильные стороны

1. **Чистая 3-tier архитектура:** verdandi → Chur → SHUTTLE → ArcadeDB
2. **Рефакторинг LoomCanvas в hooks** — отличная декомпозиция, каждый hook ≤231 LOC
3. **Transform pipeline разделён** — transformExplore/Overview/Helpers/Columns
4. **ELK Web Worker** — layout не блокирует UI (spike ready)
5. **Error handling на всех уровнях** — ErrorBoundary, statusKey, `.onFailure()`
6. **KNOT Report** — полностью новая подсистема за 1 спринт
7. **i18n на 100%** — все UI-строки в common.json (EN/RU)
8. **React Query** — retry:2, staleTime, 401 auto-logout
9. **Gradle monorepo** — `devAll` запускает все 3 сервиса

### Области для улучшения

1. **Тестовое покрытие ~5%** — критически мало для рефакторинга
2. **Нет Docker Compose** — ручной запуск ArcadeDB
3. **Нет CI pipeline** — PR не проверяются автоматически
4. **loomStore — 647 LOC** — нужна slice-декомпозиция
5. **Proto-файлы** — 3 прототипа в layout/proto/, не нужны в production

---

## 9. Прогресс по Sprint 7 Plan

| # | Задача | Статус | Заметки |
|---|--------|--------|---------|
| **Блок 1: Надёжность** | | |
| R-01 | Error Boundary | ✅ | ErrorBoundary.tsx с retry + resetKey |
| R-02 | Export error UX — toast | ✅ | Toast.tsx + ExportPanel интеграция |
| R-03 | CORS hardening | ✅ | Allowlist в server.ts |
| R-04 | Rate limiting | ✅ | In-memory, prod 5/15m, dev 50/1m |
| R-05 | JWT refresh | ✅ | /auth/refresh endpoint |
| **Блок 2: Тестирование** | | |
| T-01 | SHUTTLE unit tests | ⬜ | Запланировано 11.04 |
| T-02 | Chur unit tests | ⬜ | Запланировано 14.04 |
| T-03 | verdandi hook tests | ⬜ | Запланировано 14.04 |
| T-04 | E2E Playwright | ⬜ | Запланировано 15.04 |
| **Блок 3: Performance** | | |
| P-01 | ELK Web Worker | 🔄 | Spike готов (Sprint 6), production TBD |
| P-02 | displayGraph memoization | ⬜ | |
| P-03 | Load test 500+ | ⬜ | |
| P-04 | React Query retry + staleTime | ✅ | Уже `retry: 2, staleTime: 30_000` |
| **Блок 4: DevOps** | | |
| D-01 | Docker Compose | ⬜ | Запланировано 17.04 |
| D-02 | GitHub Actions CI | ⬜ | Запланировано 18.04 |
| D-03 | Environment profiles | ⬜ | |

**Блок 1 — 100% завершён.** Блоки 2–4 по плану.

---

## 10. Сравнение с целями Sprint 7

| Метрика | Было (08.04) | Сейчас (09.04) | Цель (18.04) |
|---------|-------------|----------------|-------------|
| Unit тест-файлов | 4 | 4 (2 FE + 2 BE) | 12+ |
| Тестовое покрытие | ~8% | ~8% | ≥ 30% |
| E2E сценариев | 0 | 0 | 1 |
| Error Boundary | нет | ✅ да | да |
| Rate limiting | нет | ✅ да | да |
| JWT refresh | нет | ✅ да | да |
| CORS hardening | нет | ✅ да | да |
| Docker Compose | нет | нет | да |
| CI pipeline | нет | нет | да |
| ELK на UI thread | да | spike Worker | нет (Worker) |

---

## 11. Рекомендации на следующую неделю (10–18 апреля)

### Приоритет 1 — Тесты (10–15 апреля)

1. **T-01: SHUTTLE unit tests** — ExploreService, LineageService, SearchService
   - Testcontainers + ArcadeDB test profile
   - Приоритет: buildResult(), ScopeRef.parse(), search merge/sort
2. **T-02: Chur tests** — fastify.inject() для auth, RBAC, rate limiter
3. **T-03: verdandi hook tests** — useGraphData, useExpansion (мокировать React Query)
4. **T-04: E2E Playwright** — login → overview → explore → inspect → export

### Приоритет 2 — Performance (16 апреля)

1. **P-01: ELK Web Worker → production** — spike в elkWorker.ts готов, нужен production path
2. **P-02: displayGraph memoization** — useMemo с deep equality на filter state

### Приоритет 3 — DevOps (17–18 апреля)

1. **D-01: Docker Compose** — ArcadeDB + SHUTTLE + Chur + verdandi
2. **D-02: GitHub Actions** — lint + test + build on PR
3. **D-03: `.env.example`** — документированные env-переменные

### Quick wins (можно сделать параллельно)

- Удалить 3 proto-файла из `layout/proto/` (или перенести в storybook)
- Убрать `mockData.ts` из services/
- Заменить default JWT secret на обязательный env
- Добавить `hasMore` флаг в ExploreResult для truncated results

---

## 12. Выводы

**Проект в отличном состоянии.** За 4 дня (05–08.04) закрыты все критические проблемы из ревью 05.04:

- ✅ SQL injection → named params
- ✅ Error handling → ErrorBoundary + `.onFailure()` 
- ✅ Монолиты → LoomCanvas 346 LOC + 6 hooks, transformGraph split на 4 модуля
- ✅ Security → CORS allowlist, rate limiting, JWT refresh
- ✅ console.log cleanup
- ✅ React Query retry

**Главный риск:** тестовое покрытие (~5%). Sprint 7 Блок 2 (тестирование — 20ч) — must have перед любыми дальнейшими изменениями. Без тестов рефакторинг loomStore или ExploreService рискованный.

**Архитектурный долг управляем:** H1–H5 — это "should have" улучшения, не блокеры. Система стабильна и готова к тестированию и контейнеризации.
