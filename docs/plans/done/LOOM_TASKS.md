# VERDANDI LOOM — Project Plan

**Последнее обновление:** 04.04.2026  
**Статус Phase 1:** ✅ Завершён (11/11 задач)  
**Статус Phase 1.5:** ✅ Завершён (3/3 задачи) — коммит 04.04.2026
**Статус Phase 2:** ✅ Завершён — Quarkus :8080 + rbac-proxy :3000 + Vite :5173  
**Текущий этап:** Phase 3 — Core Features  
**Принцип:** каждая задача — завершённый результат, который можно увидеть в браузере.

---

## Phase 1 — Skeleton + Mock Data ✅ DONE

**Цель:** работающий каркас с mock-данными, React Flow canvas, drill-down между уровнями.  
**Срок:** ~2 недели → **Выполнен 03.04.2026**

### LOOM-001: Scaffold проекта ✅
**Приоритет:** 🔴 Критический | **Оценка:** 2–3 ч | **Зависимости:** нет

**Что сделано:**
1. `npm create vite@latest loom -- --template react-ts`
2. Зависимости: `@xyflow/react`, `zustand`, `tailwindcss @tailwindcss/vite`, `lucide-react`, `elkjs`
3. shadcn/ui инициализирован
4. Структура каталогов:

```
loom/
├── src/
│   ├── components/
│   │   ├── canvas/          ← React Flow, ноды, рёбра
│   │   ├── panels/          ← Search, KNOT inspector
│   │   ├── layout/          ← Header, StatusBar, Shell
│   │   └── ui/              ← shadcn/ui компоненты
│   ├── stores/              ← Zustand stores
│   ├── hooks/               ← Custom hooks
│   ├── services/            ← API, mock data
│   ├── types/               ← TypeScript типы (Dali domain)
│   ├── utils/               ← Transformers, helpers
│   └── styles/              ← CSS variables, theme
├── public/
│   └── mock/                ← Mock JSON data
└── ...
```

5. CSS variables для SEER дизайн-системы (тёмная тема default)

---

### LOOM-002: TypeScript типы доменной модели ✅
**Приоритет:** 🔴 Критический | **Оценка:** 1–2 ч | **Зависимости:** LOOM-001

**Что сделано:** `src/types/domain.ts` + `src/types/graph.ts` — все типы нод, рёбер, ViewLevel, LoomNode, LoomEdge.

---

### LOOM-003: Mock data ✅
**Приоритет:** 🔴 Критический | **Оценка:** 1–2 ч | **Зависимости:** LOOM-002

**Что сделано:**
- `public/mock/overview.json` — L1 (~10 нод)
- `public/mock/explore-public.json` — L2 (~15 нод)
- `public/mock/column-orders.json` — L3 (~10 нод)
- `src/services/mockData.ts` — `fetchMockOverview()`, `fetchMockExplore()`, `fetchMockColumnLineage()`

---

### LOOM-004: Zustand store ✅
**Приоритет:** 🔴 Критический | **Оценка:** 1–2 ч | **Зависимости:** LOOM-002

**Что сделано:** `src/stores/loomStore.ts` — viewLevel, currentScope, navigationStack, selectedNodeId, highlightedNodes/Edges, actions: drillDown, navigateBack, navigateToLevel, selectNode, clearHighlight.

---

### LOOM-005: Shell layout — Header + Panels + StatusBar ✅
**Приоритет:** 🟡 Важный | **Оценка:** 3–4 ч | **Зависимости:** LOOM-001

**Что сделано:**
- `Shell.tsx` — CSS Grid layout (Header 48px / Left 240px / Canvas flex / Right 320px / StatusBar 28px)
- `Header.tsx` — логотип SEER, табы ипостасей (LOOM active), command palette placeholder, user badge
- `StatusBar.tsx` — число нод/рёбер, уровень, zoom, роль
- `ResizablePanel.tsx` — drag handle, collapsible

---

### LOOM-006: React Flow canvas — базовый ✅
**Приоритет:** 🔴 Критический | **Оценка:** 2–3 ч | **Зависимости:** LOOM-003, LOOM-004, LOOM-005

**Что сделано:**
- `LoomCanvas.tsx` — React Flow + MiniMap + Controls + Background, тёмная тема
- `src/utils/transformGraph.ts` — `toReactFlow(apiResponse)`
- `src/utils/layoutGraph.ts` — ELK.js Layered, direction RIGHT
- Начальное состояние: L1 Overview из mock data

---

### LOOM-007: SchemaNode — кастомная нода L1 ✅
**Приоритет:** 🟡 Важный | **Оценка:** 2–3 ч | **Зависимости:** LOOM-006

**Что сделано:** `src/components/canvas/nodes/SchemaNode.tsx` — иконка FolderTree, имя, «X tables, Y routines», hover state, double-click → drillDown.

---

### LOOM-008: TableNode — кастомная нода L2 ✅
**Приоритет:** 🟡 Важный | **Оценка:** 3–4 ч | **Зависимости:** LOOM-006

**Что сделано:** `src/components/canvas/nodes/TableNode.tsx` — header с purple border, список колонок (первые 5), PK индикаторы, «+N more», handles на колонки, click → selectNode, double-click → drillDown.

---

### LOOM-009: Breadcrumb навигация ✅
**Приоритет:** 🟡 Важный | **Оценка:** 1–2 ч | **Зависимости:** LOOM-004, LOOM-006

**Что сделано:** `src/components/canvas/Breadcrumb.tsx` — путь `Overview > public > orders`, кликабельные сегменты → navigateBack, текущий сегмент bold/некликабельный, поверх canvas top-left.

---

### LOOM-010: Drill-down — переключение уровней ✅
**Приоритет:** 🟡 Важный | **Оценка:** 2–3 ч | **Зависимости:** LOOM-007, LOOM-008, LOOM-009

**Что сделано:** полный цикл L1 → L2 → L3 → back через double-click и breadcrumb, ELK.js пересчёт при каждом переключении, StatusBar обновляется.

---

### LOOM-011: Dark/Light theme toggle ✅
**Приоритет:** 🟢 Желательный | **Оценка:** 1 ч | **Зависимости:** LOOM-005

**Что сделано:** CSS variables для light theme, toggle Sun/Moon в header, `prefers-color-scheme` media query, localStorage, React Flow `colorMode` prop.

---

## Phase 1.5 — Auth + i18n ✅ DONE

**Цель:** заложить инфраструктуру аутентификации и интернационализации до Phase 2.  
**Срок:** ~1 день → **Выполнен 04.04.2026**

### LOOM-012: Auth Store + Login Page ✅
**Приоритет:** 🔴 Критический | **Оценка:** 3–4 ч | **Зависимости:** LOOM-004, LOOM-005

**Что сделано:**
- `authStore.ts` — Zustand store: user, isAuthenticated, isLoading, error; mock login (admin/editor/viewer); Phase 2 меняет только тело `login()`
- `LoginPage.tsx` — React Hook Form + Zod, SEER DS styling, auto-redirect при уже аутентифицированном пользователе
- `ProtectedRoute.tsx` — redirect неавторизованных на `/login`
- `App.tsx` — BrowserRouter: публичный `/login` + защищённый `/*`
- `Header.tsx` — user badge из authStore (username + role dot), dropdown с logout
- `StatusBar.tsx` — роль и все лейблы из authStore + i18n

⚠️ **Догон перед Phase 2:** добавить `status.*` ключи в локали (loading, error, empty, unauthorized) — понадобятся при первом подключении ArcadeDB.

---

### LOOM-013: i18n Setup ✅
**Приоритет:** 🟡 Важный | **Оценка:** 2–3 ч | **Зависимости:** LOOM-001

**Что сделано:**
- `react-i18next` + `i18next-browser-languagedetector` установлены
- `i18n/config.ts` — bundled EN/RU ресурсы, LanguageDetector читает `seer-lang` из localStorage, затем `navigator.language`
- `locales/en/common.json` + `locales/ru/common.json` — ~30 ключей, покрывают все Phase 1 UI строки

---

### LOOM-014: Language Switcher + String Replacement ✅
**Приоритет:** 🟡 Важный | **Оценка:** 1–2 ч | **Зависимости:** LOOM-013, LOOM-005

**Что сделано:**
- `LanguageSwitcher.tsx` — Globe icon + EN/RU dropdown, закрывается при outside blur, добавлен в Header между theme toggle и user badge
- Hardcoded строки заменены на `t()` в: Header, StatusBar, Breadcrumb, LoomCanvas, SchemaNode, TableNode

---

## Phase 2 — Quarkus Middleware + RBAC ✅ DONE

**Цель:** заменить тонкий RBAC-прокси на Quarkus middleware с GraphQL, RLS и intent-based query builder. Подключить реальные данные из ArcadeDB.  
**Выполнен:** 04.04.2026 — все сервисы запущены (Quarkus :8080, rbac-proxy :3000, Vite :5173)

> **ADR-004 → ADR-004b:** вместо тонкого Fastify-прокси — Quarkus middleware с GraphQL схемой, compile-time RBAC (`@RolesAllowed`) и RLS фильтрами. Фронтенд переходит с raw Cypher на GraphQL клиент (`graphql-request`).

### Архитектура Phase 2

```
Браузер (LOOM SPA)
    │  POST /graphql  { query: "{ overview { schemas { name tableCount } } }" }
    │  Authorization: Bearer <JWT>
    ▼
Quarkus (8080)
    │  SmallRye JWT → проверка токена
    │  @RolesAllowed("viewer") → compile-time RBAC
    │  Intent-based query builder → Cypher/SQL с RLS фильтрами
    ▼
ArcadeDB (2480)  — HTTP REST API
```

Fastify rbac-proxy остаётся **только** для `/auth/login`, `/auth/me`, `/auth/logout` — либо выпиливается если Quarkus берёт auth на себя через `quarkus-smallrye-jwt-build`.

**Зависимости Quarkus (pom.xml):**
```xml
quarkus-smallrye-graphql
quarkus-smallrye-jwt
quarkus-rest-client-reactive
quarkus-hibernate-validator
quarkus-container-image-docker
```

---

### Что реально сделано в Phase 2

**Фронтенд (LOOM SPA):**
- `DS-01` — SEER DS v1.1: логотип (dot + SEER), тонкий активный таб, круглый бейдж пользователя → `Header.tsx`
- `DS-02` — Palette switcher: 5 палитр (Amber Forest, Lichen, Slate, Juniper, Warm Dark) + light/dark → `globals.css`, `loomStore.ts`
- `LOOM-014` — Фикс хардкода `" routines"` → `t('nodes.routines')` в PackageNode
- `LOOM-017` — GraphQL-клиент `lineage.ts`: 6 функций (fetchOverview, fetchExplore, fetchLineage, fetchUpstream, fetchDownstream, fetchSearch) → `services/lineage.ts`
- `LOOM-018` — Реальная авторизация: `POST /auth/login`, `checkSession()` вместо mock → `authStore.ts`, `App.tsx`
- `LOOM-019` — 6 TanStack Query хуков с auto-logout на 401 → `services/hooks.ts`
- `LOOM-020` — `transformGqlOverview` + `transformGqlExplore`: GraphQL → LoomNode/LoomEdge → `utils/transformGraph.ts`
- `LOOM-021` — LoomCanvas подключён к реальным хукам, mock-данные удалены, drill-down по двойному клику → `LoomCanvas.tsx`

**Backend (lineage-api):**
- `BE-01` — Quarkus-проект `lineage-api`: ArcadeDB HTTP-клиент, SmallRye GraphQL, SeerIdentity (trusted headers)
- `BE-02` — Верификация всех запросов по живой БД через curl, исправлены имена полей (`schema_name`, `table_name` и др.)
- `BE-03` — `OverviewService`, `ExploreService`, `LineageService`, `SearchService` — все запросы
- `BE-04` — Добавлен `toExploreResult()` в ExploreService (фикс compile error)
- `BE-05` — Фикс `@rid` → ключ `"rid"` в OverviewService
- `BE-06` — Миграция с Maven → Gradle 9 + Quarkus 3.34.2

**rbac-proxy:**
- `PROXY-01` — Фикс `arcadeDb: 'hound'` (было `'lineage'`)
- `PROXY-02` — Маршрут `POST /graphql` с X-Seer-Role/X-Seer-User заголовками
- `PROXY-03` — Замена `@fastify/cors` на ручной CORS-хук (несовместимость Fastify 4/5)
- `PROXY-04` — Патч проверки версий плагинов в Fastify 4 (dev-режим)

**Инфраструктура:**
- `INFRA-01` — `User` тип в ArcadeDB, пользователь `admin/admin`
- `INFRA-02` — Все три сервиса запущены: Quarkus :8080, rbac-proxy :3000, Vite :5173

**Отличия от плана:**
- LOOM-015 (i18n status keys) — выполнен попутно, не отдельным коммитом
- LOOM-016: вместо `@RolesAllowed` использован `SeerIdentity` (trusted headers от proxy) — более простая схема для MVP
- LOOM-022 (Docker Compose) — отложен, сервисы запускаются локально напрямую
- Gradle вместо Maven — Gradle доступен из HOUND окружения

---

### LOOM-015: i18n догон — status/error ключи ✅
**Приоритет:** 🔴 Критический | **Оценка:** 30 мин | **Зависимости:** LOOM-013

```json
"status": {
  "loading": "Loading...",
  "error": "Failed to load data",
  "empty": "No nodes found",
  "unauthorized": "Session expired. Please sign in again.",
  "forbidden": "Access denied"
}
```

**Готово когда:** `t('status.error')` рендерится корректно на EN и RU.

---

### LOOM-016: Quarkus middleware — GraphQL + RBAC + RLS
**Приоритет:** 🔴 Критический | **Оценка:** ~1 неделя | **Зависимости:** нет

```java
@GraphQLApi
public class LineageResource {

    @Query
    @RolesAllowed({"viewer", "editor", "admin"})
    public List<SchemaNode> overview(@Context SecurityContext ctx) {
        Set<String> allowed = permissionService.allowedSchemas(ctx);
        return arcadeClient.query(buildOverviewQuery(allowed));
    }

    @Query
    @RolesAllowed({"viewer", "editor", "admin"})
    public ExploreResult explore(String scope, int depth,
                                 @Context SecurityContext ctx) { ... }

    @Query
    @RolesAllowed({"viewer", "editor", "admin"})
    public LineageResult columnLineage(String tableId, String column,
                                       @Context SecurityContext ctx) { ... }

    @Mutation
    @RolesAllowed("admin")
    public boolean dropLineage(String nodeId) { ... }
}
```

**RLS в query builder:**
```java
// viewer → WHERE schema.name IN ['allowed1', 'allowed2']
// editor → полный доступ на чтение
// admin  → всё включая мутации
private String buildOverviewQuery(Set<String> allowedSchemas) {
    if (allowedSchemas.isEmpty()) return QUERY_ALL;
    return QUERY_FILTERED.replace(":schemas",
        allowedSchemas.stream()
            .map(s -> "'" + s + "'")
            .collect(Collectors.joining(",")));
}
```

> **⚠️ Перед стартом LOOM-016** — нужно понять какие поля реально есть у DaliTable, DaliAtom, DaliStatement в Hound/ArcadeDB. GraphQL schema строится под реальную модель, не под предположения.

**Готово когда:** `POST /graphql` с JWT возвращает данные, `@RolesAllowed` блокирует неавторизованные запросы.

---

### LOOM-017: GraphQL клиент на фронтенде
**Приоритет:** 🔴 Критический | **Оценка:** 2–3 ч | **Зависимости:** LOOM-016

```bash
npm install graphql-request graphql
```

```typescript
// src/services/graphql.ts
import { GraphQLClient } from 'graphql-request';

export const gqlClient = new GraphQLClient('/graphql', {
  credentials: 'include',
  errorPolicy: 'all',
});
```

Заменяет `queryArcade(language, command)` — raw Cypher из фронтенда уходит, запросы переходят в Quarkus resolvers.

**Готово когда:** `gqlClient.request(OVERVIEW_QUERY)` возвращает типизированный ответ.

---

### LOOM-018: authStore — подключить реальный login
**Приоритет:** 🔴 Критический | **Оценка:** 30 мин | **Зависимости:** LOOM-016

Заменить mock `login()` на `POST /auth/login`. Удалить `MOCK_USERS`.

---

### LOOM-019: GraphQL хуки — Overview, Explore, Lineage
**Приоритет:** 🔴 Критический | **Оценка:** 2–3 ч | **Зависимости:** LOOM-017

```typescript
// hooks/useOverview.ts
const OVERVIEW_QUERY = gql`
  query Overview {
    overview {
      schemas { id name tablesCount routinesCount childrenAvailable }
      packages { id name routinesCount }
    }
  }
`;

// hooks/useExplore.ts
const EXPLORE_QUERY = gql`
  query Explore($scope: String!, $depth: Int) {
    explore(scope: $scope, depth: $depth) {
      nodes { id type label metadata childrenAvailable }
      edges { id source target type }
      stats { totalNodes totalEdges truncated }
    }
  }
`;

// hooks/useColumnLineage.ts
const LINEAGE_QUERY = gql`
  query ColumnLineage($tableId: String!, $column: String) {
    columnLineage(tableId: $tableId, column: $column) {
      nodes { id type label metadata }
      edges { id source target type sourceHandle targetHandle }
    }
  }
`;
```

**Готово когда:** canvas показывает живые данные на каждом уровне.

---

### LOOM-020: transformGraph — GraphQL response → React Flow
**Приоритет:** 🔴 Критический | **Оценка:** 1–2 ч | **Зависимости:** LOOM-019

Обновить `transformGraph.ts` — принимает типизированный GraphQL response, строит `LoomNode[]` + `LoomEdge[]`. Удалить зависимость от `mockData.ts` в `LoomCanvas.tsx`.

---

### LOOM-021: Search — GraphQL + Fuse.js
**Приоритет:** 🟡 Важный | **Оценка:** 2–3 ч | **Зависимости:** LOOM-017

```graphql
query Search($q: String!, $types: [String], $limit: Int) {
  search(q: $q, types: $types, limit: $limit) {
    results { id type label path score }
    total
  }
}
```

Fuse.js — клиентский fuzzy по уже загруженным нодам текущего view.

---

### LOOM-022: Docker Compose — полный стек
**Приоритет:** 🟡 Важный | **Оценка:** 2–3 ч | **Зависимости:** LOOM-016

```yaml
services:
  loom-ui:
    image: nginx:alpine
    volumes:
      - ./loom/dist:/usr/share/nginx/html
      - ./nginx.conf:/etc/nginx/conf.d/default.conf
    ports: ["80:80"]

  quarkus-middleware:
    image: quarkus-middleware:latest   # native image ~30MB RAM, старт ~50ms
    environment:
      ARCADEDB_URL: http://arcadedb:2480
      DB_NAME: lineage
      MP_JWT_VERIFY_PUBLICKEY_LOCATION: /keys/public.pem
    ports: ["8080:8080"]

  arcadedb:
    image: arcadedata/arcadedb:26.3.1
    volumes:
      - arcadedb-data:/home/arcadedb/databases
    ports: ["2480:2480"]
    environment:
      JAVA_OPTS: "-Xmx4g"

volumes:
  arcadedb-data:
```

```nginx
location /graphql { proxy_pass http://quarkus-middleware:8080; }
location /auth/   { proxy_pass http://quarkus-middleware:8080; }
```

**Готово когда:** `docker compose up` → login → GraphQL → canvas с живыми данными.

---

### Порядок выполнения Phase 2

```
LOOM-015 (i18n догон)            ← 30 мин, первым делом
    │
    ├── LOOM-016 (Quarkus)       ← бэкенд, параллельно с фронтендом
    │       │
    │       └── (нужна реальная схема Hound/ArcadeDB перед стартом)
    │
    └── LOOM-017 (GraphQL клиент)
            │
            ├── LOOM-018 (authStore реальный login)
            │
            └── LOOM-019 (GraphQL хуки)
                    │
                    └── LOOM-020 (transformGraph ← GraphQL response)
                            │
                            ├── LOOM-021 (Search)
                            └── LOOM-022 (Docker Compose)
```

---

## Phase 3 — Core Features ⬜ TODO

**Цель:** ключевые фичи для production-использования.  
**Срок:** ~4–5 недель · 12 задач · ~40–52 ч  
**Детальный документ:** `LOOM_PHASE3_TASKS.md` — User Stories, Use Cases, таблицы выбора для каждой задачи

---

### LOOM-023: Canvas read-only mode
**Приоритет:** 🔴 Критический | **Оценка:** 30 мин | **Зависимости:** LOOM-006

По умолчанию граф — документ для чтения, не whiteboard.

```tsx
// LoomCanvas.tsx
<ReactFlow
  nodesDraggable={false}      // граф read-only по умолчанию
  nodesConnectable={false}    // нельзя создавать рёбра вручную
  elementsSelectable={true}   // клик для KNOT inspector — оставить
  panOnDrag={true}            // pan по пустому месту — оставить
  zoomOnScroll={true}         // zoom — оставить
/>
```

Когда включать `nodesDraggable=true` — решаем отдельно при появлении конкретного use case (возможный сценарий: режим аннотирования для editor/admin перед экспортом). До тех пор — всегда `false`.

**Готово когда:** ноды не перетаскиваются, pan/zoom работает, клик выделяет.

---

### LOOM-024: L1 расширение — ApplicationNode + ServiceNode
**Приоритет:** 🔴 Критический | **Оценка:** 3–4 ч | **Зависимости:** LOOM-007, LOOM-020

Два новых типа нод верхнего уровня L1. Добавить в `domain.ts`:

```typescript
// Новые типы нод:
| 'DaliApplication'   // верхний L1
| 'DaliService'       // второй L1

// Новые типы рёбер:
| 'HAS_SERVICE'       // Application → Service
| 'HAS_DATABASE'      // Service → DaliDatabase
| 'USES_DATABASE'     // Service → DaliDatabase (many-to-many)
```

**Иерархия на L1 canvas** (ELK layered TOP→DOWN):
```
[DaliApplication] → [DaliService] → [DaliDatabase] → [DaliSchema/DaliPackage]
```

**Поведение double-click:**
- `DaliApplication` / `DaliService` → scope filter в L1, уровень не меняется, breadcrumb: `Overview > MyApp > OrderService`
- `DaliDatabase` / `DaliSchema` → drill-down на L2 (как сейчас)

**Новый action в loomStore:**
```typescript
setScopeFilter: (nodeId: string, nodeType: 'DaliApplication' | 'DaliService') => void;
```

**Готово когда:** L1 показывает 4 уровня иерархии, scope filter работает, drill-down на Database/Schema не сломан.

---

### LOOM-025: TableNode — collapse / expand / inline filter
**Приоритет:** 🔴 Критический | **Оценка:** 3–4 ч | **Зависимости:** LOOM-008

Три состояния вместо «первые 5 + +N more». Добавить в `domain.ts`:

```typescript
export type NodeDisplayState = 'collapsed' | 'partial' | 'expanded';

// Добавить в TableNodeData:
displayState: NodeDisplayState;   // default: 'partial'
visibleColumnsCount: number;      // default: 7
```

**Кнопки окна** (top-right угол header, как в ОС):
- 🔴 — убрать ноду с canvas (скрыть, не удалять из БД)
- 🟡 — переключить в `partial`
- 🟢 — переключить в `expanded`

В `expanded` state: inline filter (`filter columns...`) через Fuse.js, вертикальный скроллбар если > 15 колонок, клик на колонку → drill-down L3 по этой колонке.

**Готово когда:** три состояния переключаются кнопками, filter работает, клик на колонку → L3.

---

### LOOM-026: Expand buttons — upstream / downstream
**Приоритет:** 🟡 Важный | **Оценка:** 3–4 ч | **Зависимости:** LOOM-019, LOOM-020

На каждой ноде при hover — кнопки за границами:

```
[↑ +N]   [ ИМЯ НОДЫ ]   [↓ +N]
```

- `↑` upstream — ноды которые пишут в эту ноду (depth=1 по умолчанию)
- `↓` downstream — ноды которые читают из этой ноды
- `+N` — prefetch при hover, показывает число соседей
- Если N > 20 → `↓ +20 / 47` (20 загружено, 47 всего)
- Новые ноды добавляются к графу, ELK пересчитывает layout
- Глубина в KNOT inspector: ползунок `1 / 2 / 3 / ∞` (default: 1)

```typescript
// Новые actions в loomStore:
expandNeighbors: (nodeId: string, direction: 'upstream' | 'downstream', depth: number) => void;
collapseNeighbors: (nodeId: string, direction: 'upstream' | 'downstream') => void;
```

**Готово когда:** hover → кнопки с числом соседей, клик → ноды появляются на canvas.

---

### LOOM-027: StatementNode + StatementGroupNode (L2/L3)
**Приоритет:** 🟡 Важный | **Оценка:** 4–5 ч | **Зависимости:** LOOM-020

**StatementNode** — корневые Statement на L2 (без входящего `NESTED_IN` / `USES_SUBQUERY`):
- Header: тип (Query/Cursor/RefCursor/Insert/...) + имя
- Список читаемых/записываемых таблиц (первые 3)
- Expand button → показывает дочерние Statement рядом

**StatementGroupNode** — визуальная рамка группировки для L3 (Вариант C):
- React Flow GroupNode **без** `extent: 'parent'` — рамка только визуальная
- Collapsed → одна нода с бейджем `N nested`
- Expanded → dashed border рамка с дочерними Statement внутри, каждый со своим lineage (Column → Atom → OutputColumn)
- Рёбра `DATA_FLOW` пересекают границу рамки наружу — React Flow поддерживает
- ELK считает ноды внутри как обычные, не subgraph

**Готово когда:** L3 показывает StatementGroupNode с полным lineage внутри, DATA_FLOW выходит за границу рамки.

---

### LOOM-028: Impact analysis + KNOT inspector
**Приоритет:** 🟡 Важный | **Оценка:** 3–4 ч | **Зависимости:** LOOM-026

- Cypher traversal: upstream / downstream с настраиваемой глубиной
- KNOT inspector (правая панель): дерево upstream/downstream, affected count, stats
- Highlight нод и рёбер: `--wrn` (#D4922A) для downstream, `--inf` (#88B8A8) для upstream
- Depth control: ползунок `1 / 2 / 3 / ∞` в KNOT inspector

---

### LOOM-029: Оставшиеся типы нод L2/L3
**Приоритет:** 🟡 Важный | **Оценка:** 3–4 ч | **Зависимости:** LOOM-027

- `RoutineNode` — имя, язык, число корневых Statement, expand button для вложенных рутин (`CALLS`)
- `AtomNode` — тип операции (SUM/RANK/FILTER/EXTRACT/...), expression, badge для window functions (`PARTITION BY region`)
- `ColumnNode` — отдельная нода для L3 column lineage
- `OutputColumnNode` — результирующая колонка, nullable badge если NULL в части UNION веток

---

### LOOM-030: Прочие Phase 3 задачи
**Приоритет:** 🟢 Желательный | **Оценка:** 2–3 ч | **Зависимости:** LOOM-028

- Filter panel (левая панель): по типу ноды, типу ребра
- Виртуализация колонок в TableNode (> 20) через `@tanstack/react-virtual`
- Prefetch при hover на ноду (`TanStack Query prefetchQuery`)
- Role-based UI: скрывать edit-элементы для роли `viewer`
- `fitView({ padding: 0.1, duration: 300 })` после каждого ELK layout пересчёта

---

## Phase 4 — Polish ⬜ TODO

**Цель:** production-качество UX и надёжность.  
**Срок:** ~2–3 недели

- [ ] Framer Motion: transition при drill-down (fade + scale)
- [ ] Keyboard navigation: Escape = back, Enter = drill-down, ⌘K = command palette
- [ ] Export: PNG/SVG снимок текущего графа
- [ ] E2E тесты: Playwright (login, drill-down L1→L3, breadcrumb, theme switch)
- [ ] Unit тесты: Vitest для transformGraph, authStore, loomStore
- [ ] Performance profiling: React DevTools, React Flow node count benchmarks

---

## Phase 5 — Расширение ⬜ TODO

**Цель:** расширение после реализации версионирования в Dali/Hound.  
**Срок:** по готовности бэкенда

- [ ] Version diff (когда Dali/Hound реализует versioning)
- [ ] WebSocket live updates (ArcadeDB change events)
- [ ] Расширение RBAC proxy до GraphQL middleware (если нужно)
- [ ] Начало разработки ANVIL / SHUTTLE

---

## Общая дорожная карта

```
Phase 1   [████████████] ✅ DONE        ~2 нед   (LOOM-001 – LOOM-011)
Phase 1.5 [████████████] ✅ DONE        ~1 день  (LOOM-012 – LOOM-014)
Phase 2   [████████████] ✅ DONE        ~3 нед   (LOOM-015 – LOOM-022) · Quarkus + GraphQL
Phase 3   [░░░░░░░░░░░░] 🚧 CURRENT     ~4–5 нед (LOOM-023 – LOOM-034) · 12 задач
Phase 4   [░░░░░░░░░░░░] ⬜ TODO         ~2–3 нед (Polish + Tests)
Phase 5   [░░░░░░░░░░░░] ⬜ TODO         по бэкенду

Итого MVP (Phase 1–3): ~9–10 недель • Solo-dev + Claude AI
```

## Решённые архитектурные решения

| ADR | Решение | Статус |
|-----|---------|--------|
| ADR-001 | Lazy exploration (L1/L2/L3) — не рендерить 627K нод целиком | ✅ Принято |
| ADR-002 | React Flow (@xyflow/react) для canvas | ✅ Принято |
| ADR-003 | Стек: Vite 6 + React 19 + TS + ELK.js + Zustand + TanStack Query + shadcn/ui | ✅ Принято |
| ADR-004 | API MVP: фронтенд → ArcadeDB через тонкий RBAC proxy (Fastify + JWT) | ✅ Принято |
| ADR-005 | Auth mock-first: authStore с абстрактным login(), Phase 2 меняет только impl | ✅ Принято |
| ADR-006 | i18n bundled resources (не lazy HTTP): < 100 ключей, нет flickering | ✅ Принято |
| ADR-004b | Quarkus вместо тонкого Fastify-прокси: SmallRye GraphQL + `@RolesAllowed` (compile-time RBAC) + RLS в query builder. Фронтенд переходит с raw Cypher на GraphQL клиент. Native image: ~30MB RAM, старт ~50ms | ✅ Принято |
| ADR-011 | GraphQL клиент на фронтенде — `graphql-request` (минимальный, без кеша — кеш в TanStack Query). Альтернативы: urql (сложнее), Apollo (избыточен для solo-dev) | ✅ Принято |
| ADR-007 | `nodesDraggable=false` по умолчанию — граф документ для чтения, не whiteboard. Включение — отдельное решение при появлении use case | ✅ Принято |
| ADR-008 | StatementGroupNode — Вариант C: React Flow GroupNode без `extent:'parent'`, рамка только визуальная, ноды внутри физически независимы, ELK считает их как обычные. DATA_FLOW пересекает границу рамки наружу | ✅ Принято |
| ADR-009 | L1 Application/Service scope filter — double-click на Application/Service не меняет уровень, только сужает scope через `setScopeFilter()`. Переход на L2 только через Database/Schema | ✅ Принято |
| ADR-010 | TableNode три состояния (collapsed/partial/expanded) вместо «5 строк + +N more». Кнопки окна ОС (🔴🟡🟢) как триггеры переключения | ✅ Принято |
