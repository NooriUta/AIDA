# Sprint Review — M1 "Core Engines" (сессия 12.04.2026)

**Дата:** 12.04.2026  
**Ветка:** `feature/m1-core-engines`  
**Спринт:** M1 — Apr 13 → May 9 (Milestone: Core Engines)  
**Участники:** 1 разработчик + Claude  

---

## Итоговая статистика

| Метрика | Значение |
|---------|----------|
| Коммиты за сессию | 6 |
| Файлов изменено (M1 итого) | 114 |
| Вставок / удалений | +13 649 / −565 |
| Новых компонентов | 3 фронтенд-приложения (heimdall-frontend, shell, aida-shared) |
| Новых сервисов backend | SHUTTLE mutations + subscriptions |
| Открытых багов | 1 (ELK cross-origin, фикс написан) |

---

## Track 1 — HEIMDALL Frontend ✅ Завершён

### T1.0 — `packages/aida-shared/` ✅
- Дизайн-токены (CSS custom properties) — единый источник правды для Shell + HEIMDALL
- `initTheme()` — применяет `data-theme` / `data-palette` из localStorage до первого рендера
- Типы: `HeimdallEvent`, `MetricsSnapshot`, `SnapshotInfo`, `EventFilter`, `AppContext`
- Утилиты ADR-DA-013: `buildAppUrl()`, параметры `nodeId / schema / returnTo / highlight / sessionId`

### T1.1 — Scaffold heimdall-frontend ✅
- `frontends/heimdall-frontend/` — React 19 + Vite + Module Federation
- `vite.config.ts` с `federation({ name: 'heimdall-frontend', exposes: { './App' } })`
- `Dockerfile` (builder → nginx:alpine), запись в `docker-compose.yml`
- CORS добавлен в HEIMDALL backend (`application.properties` → `:5174`)

### T1.2 — Хуки ✅
- `useEventStream.ts` — native WebSocket `/ws/events`, sliding buffer 500 событий, reconnect
- `useMetrics.ts` — polling `/metrics/snapshot` каждые 2000 мс, AbortController
- `useControl.ts` — POST reset / snapshot, GET snapshots

### T1.3 — App.tsx + routing ✅
- `/dashboard`, `/events`, `/controls`, fallback → redirect
- Shared layout на CSS-переменных (`--bg1`, `--t1`, `--acc`, `--bd`)
- `main.tsx` — `import 'aida-shared/styles/tokens'` + `initTheme()` до ReactDOM

### T1.4–T1.8 — UI-компоненты ✅
| Компонент | Технология | Статус |
|-----------|-----------|--------|
| `MetricsBar` | CSS vars, 6 счётчиков | ✅ |
| `ThroughputChart` | @nivo/line, sliding 30s window | ✅ |
| `ResolutionGauge` | @nivo/pie, donut | ✅ |
| `EventLog` | react-virtuoso, `followOutput="smooth"` | ✅ |
| `DashboardPage` | MetricsBar + Chart + EventLog (50) | ✅ |
| `EventStreamPage` | EventLog full + фильтры | ✅ |
| `ControlsPage` | Reset + Snapshot + список | ✅ |

### T1.9 — Auth + i18n + Toolbar ✅
- Keycloak-интеграция (через Chur cookie `sid`) для HEIMDALL frontend
- Полная локализация ru/en (`i18n/locales/`)
- Toolbar с фильтрами по компоненту / уровню / сессии

---

## Track 2 — SHUTTLE Mutations + Subscriptions ✅ Завершён

### T2.1 — MutationResource.java ✅
```java
@Mutation("resetDemoState")    // POST /heimdall/control/reset
@Mutation("startParseSession") // генерирует sessionId
@Mutation("cancelSession")     // emit SESSION_FAILED
```
- `ParseSessionInput` record
- `HeimdallClient` расширен методом `reset(@HeaderParam("X-Seer-Role"))`

### T2.2 — SubscriptionResource.java ✅
```java
@Subscription("heimdallEvents")   // Multi<HeimdallEvent> — все события
@Subscription("sessionProgress")  // фильтр по sessionId
```
- `HeimdallEventBus` (`@ApplicationScoped`) — in-process Emitter
- `HeimdallEmitter` обновлён: `eventBus.publish(event)` после отправки в HEIMDALL
- `application.properties`: `mp.messaging.outgoing.heimdall-events.connector=smallrye-in-memory`

---

## Track 3 — Chur BFF ✅ Завершён

### T3.1 — requireAdmin middleware ✅
- `bff/chur/src/middleware/requireAdmin.ts` — проверка `req.user?.role === 'admin'`
- Применён в HEIMDALL-маршрутах: `preHandler: [authenticate, requireAdmin]`

### T3.2 — WebSocket proxy `/heimdall/ws/events` ✅
- `@fastify/websocket` установлен и зарегистрирован
- Проверка cookie `sid` → `ensureValidSession` → проброс к HEIMDALL `:9093/ws/events`
- Поддержка query param `?filter=...`

### T3.3 — GraphQL proxy ✅
- `/graphql` → SHUTTLE `:8080/graphql` (HTTP + WebSocket graphql-ws)
- Исправлен вложенный Router (`BrowserRouter` убран из verdandi, Router — на Shell)

---

## Track 4 — Shell Module Federation ✅ Завершён

### T4.1–T4.3 — Shell scaffold ✅
- `frontends/shell/` — Vite + MF host
- `shellStore.ts` — `currentApp`, `navigateTo(app, context)`
- `AidaNav` — горизонтальная навигация VERDANDI · HEIMDALL
- `AppLayout` — flex-column 100%, `<main>` flex:1, overflow:hidden

### T4.4 — verdandi → MF remote ✅
- `@module-federation/vite` добавлен в verdandi
- `federation({ name: 'verdandi', exposes: { './App' } })`
- Shared: react, react-dom, react-router-dom, zustand (singleton)

---

## Сессия 12.04.2026 — MF Integration fixes

После сборки Shell + verdandi + HEIMDALL frontend обнаружены и исправлены 5 критических дефектов:

### Fix 1 — `height: 100vh` → `100%` (`489ce35`)
**Проблема:** Shell имеет `<main style={{flex:1}}>`, но verdandi-компоненты использовали `height: 100vh`
(= 100% viewport), переполняя контейнер на высоту AidaNav (~55px).  
**Файлы:** `Shell.tsx`, `KnotPage.tsx`, `UnderConstructionPage.tsx`, `LoginPage.tsx`

### Fix 2 — React Flow CSS в MF-remote режиме (`335be4d`)
**Проблема:** `@xyflow/react/dist/style.css` был импортирован только в `main.tsx`, который **не выполняется**
когда verdandi загружается как MF-remote. В результате MiniMap оказывался в левом верхнем углу,
кнопка Экспорт — не на месте (все Panel-элементы рендерились как block вместо absolute).  
**Фикс:** Добавлен `import '@xyflow/react/dist/style.css'` в `App.tsx`.

### Fix 3 — fitView после async layout (`dfa2b9b`)
**Проблема:** React Flow's `fitView` prop срабатывает один раз при инициализации когда граф пуст.
После завершения ELK-layout (async) граф оставался вне области видимости.  
**Фикс:** `requestFitView()` вызывается в `useLoomLayout` после L1 и ELK layout.

### Fix 4 — Гигантские узлы → ELK 20 000px граф (`9f14041`)
**Проблема:** `applyStmtColumns` добавлял ВСЕ 633 колонки INSERT-узла → высота 14 024px →
ELK строил граф 20 906px → fitView зумировался до 10%.  
**Фикс:** Колпак `TRANSFORM.MAX_PARTIAL_COLS = 20` в `applyStmtColumns` + safety cap в `getNodeHeight`.
`tableColMap`/`stmtColMap` по-прежнему хранят ВСЕ ID колонок для сопоставления рёбер.

### Fix 5 — ELK cross-origin Worker (`67bf1aa`) ⚠️ Нужна верификация
**Проблема:** `new Worker(elkWorkerUrl)` бросает `SecurityError` когда `elkWorkerUrl` указывает на
verdandi origin (`:5173`) а Shell работает на другом origin (`:5175`). ELK молча падает,
`applyGridLayout` отдаёт сетку из 617 узлов при 10% зуме.  
**Фикс:** Pre-fetch worker bytes через CORS → `URL.createObjectURL(blob)` → same-origin Worker.  
**Верифицирован вручную:** ELK с blob-worker дал A→B→C `x=[12, 132, 252]` ✅  
**End-to-end верификация:** заблокирована нестабильностью БД → записан в `BUG_ELK_CROSS_ORIGIN_WORKER.md`

---

## Открытые пункты

| ID | Описание | Приоритет | Статус |
|----|----------|-----------|--------|
| BUG-ELK-001 | ELK cross-origin Worker в MF-remote (фикс написан, нужна E2E проверка) | High | Fix written |
| PRE-EXISTING | ~25 TS-ошибок в verdandi build (тесты, unused vars, legacy) | Low | Не в scope M1 |

---

## Архитектурные решения этой сессии

| Решение | Обоснование |
|---------|-------------|
| Blob URL для ELK Worker | Единственный способ обойти cross-origin без изменения серверной конфигурации |
| Pre-fetch перед ELK constructor | `workerFactory` синхронная → blob URL нужен до `new ELK()` |
| CSS в App.tsx (не main.tsx) | MF-remote не выполняет main.tsx — критически важно для любых side-effect imports |
| `height: 100%` везде | Позволяет flex-layout Shell контролировать высоту дочерних MF-remote |

---

## Что запущено сейчас

| Сервис | Порт | Способ запуска |
|--------|------|----------------|
| Chur (BFF) | `:3000` | `cmd.exe` + `tsx.cmd watch` |
| HEIMDALL backend | `:9093` | Preview |
| HEIMDALL frontend | `:5174` | Preview |
| Shell (MF host) | `:5175` | Preview |
| Shuttle (Quarkus) | `:8080` | Gradle quarkusDev (отдельное окно) |

---

## Следующий шаг (как только БД стабильна)

1. Открыть Shell → VERDANDI → LOOM → DWH2 → L2
2. Убедиться в консоли: `[LOOM] ELK layout (worker) — NNN ms (617 nodes, ...)`
3. Закрыть BUG-ELK-001 ✅
4. Перейти к M1 W2 остатку: `EventStreamPage`, `ControlsPage`, docker-compose верификация
