# VERDANDI — Архитектурный обзор системы

**Дата:** 09.04.2026
**Версия:** 2.0 (обновление после Sprint 6 refactor + Sprint 7 scaffold)
**Предыдущая версия:** 1.0 от 05.04.2026

---

## 1. Назначение

**VERDANDI** — часть экосистемы **SEER Studio**, IDE-подобного инструмента для визуализации Data Lineage. VERDANDI реализует две ипостаси:
- **LOOM** — интерактивный граф-визуализатор (3-уровневый: L1 Overview → L2 Explore → L3 Lineage)
- **KNOT** — инспектор и отчёт по сессиям Data Lineage

**Пользователи:** Data Engineer, Data Analyst, DBA / Architect.

---

## 2. Компоненты

### 2.1 verdandi (frontend)

| Параметр | Значение |
|----------|----------|
| Фреймворк | React 19 + TypeScript 5.9 |
| Сборка | Vite 8 |
| Graph rendering | @xyflow/react (React Flow) |
| Layout engine | ELK.js (Web Worker) |
| State | Zustand (loomStore + authStore) |
| Data fetching | TanStack Query (React Query) + graphql-request |
| Styling | Tailwind CSS + CSS Variables (SEER Design System) |
| i18n | react-i18next (EN, RU) |
| UI kit | shadcn/ui + Lucide icons |
| Testing | Vitest |
| Порт | 5173 |

**Файловая структура (76 файлов, ~14,730 LOC):**

```
verdandi/src/
├── App.tsx                          — Router + ErrorBoundary
├── main.tsx                         — Entry point
├── components/
│   ├── ErrorBoundary.tsx            (93)  — Catch render errors + fallback UI
│   ├── Toast.tsx                          — Toast notifications
│   ├── auth/
│   │   ├── LoginPage.tsx            (192)
│   │   └── ProtectedRoute.tsx
│   ├── canvas/
│   │   ├── LoomCanvas.tsx           (346)  — Main canvas (refactored)
│   │   ├── Breadcrumb.tsx           (139)
│   │   ├── ExportPanel.tsx                — PNG/SVG export
│   │   ├── NodeContextMenu.tsx            — Right-click menu
│   │   ├── ZoomLevelContext.tsx            — LOD compact rendering
│   │   └── nodes/
│   │       ├── ApplicationNode.tsx  (137)
│   │       ├── DatabaseNode.tsx     (169)
│   │       ├── L1SchemaNode.tsx     (117)
│   │       ├── ServiceNode.tsx      (116)
│   │       ├── TableNode.tsx        (285)
│   │       ├── StatementNode.tsx    (183+)
│   │       ├── RoutineNode.tsx / RoutineGroupNode.tsx
│   │       ├── SchemaNode.tsx / SchemaGroupNode.tsx
│   │       ├── ColumnNode.tsx / PackageNode.tsx
│   │       └── NodeExpandButtons.tsx (98)
│   ├── inspector/                         — KNOT Inspector panel
│   │   ├── InspectorPanel.tsx       (97)
│   │   ├── InspectorSchema.tsx      (82)
│   │   ├── InspectorTable.tsx       (73)
│   │   ├── InspectorRoutine.tsx     (23)
│   │   ├── InspectorStatement.tsx   (84)
│   │   ├── InspectorSection.tsx     (62)
│   │   └── InspectorEmpty.tsx       (15)
│   ├── knot/                              — KNOT Report page
│   │   ├── KnotPage.tsx
│   │   ├── KnotSummary.tsx / KnotStructure.tsx
│   │   ├── KnotStatements.tsx / KnotRoutines.tsx
│   │   └── KnotAtoms.tsx
│   ├── layout/
│   │   ├── Shell.tsx                (69)
│   │   ├── Header.tsx               (350)
│   │   ├── FilterToolbar.tsx        (345)
│   │   ├── FilterToolbarL1.tsx      (346)
│   │   ├── ResizablePanel.tsx       (143)
│   │   ├── StatusBar.tsx
│   │   ├── LanguageSwitcher.tsx     (93)
│   │   ├── LegendButton.tsx
│   │   └── proto/ (3 dev-only prototypes)
│   └── panels/
│       └── SearchPanel.tsx          (470)
├── hooks/canvas/                          — Extracted from LoomCanvas
│   ├── useGraphData.ts              (110)  — Query orchestration
│   ├── useExpansion.ts              (88)   — LOOM-027 expansion merge
│   ├── useDisplayGraph.ts           (217)  — Transform + filter pipeline
│   ├── useLoomLayout.ts             (231)  — ELK layout + Worker
│   ├── useFitView.ts                (45)   — Viewport management
│   └── useFilterSync.ts             (129)  — Toolbar ↔ Canvas sync
├── stores/
│   ├── loomStore.ts                 (647)  — Main Zustand store
│   └── authStore.ts                 (93)
├── services/
│   ├── lineage.ts                   (169)  — GraphQL fetch functions
│   ├── hooks.ts                     (178)  — React Query hooks (12 hooks)
│   └── mockData.ts                        — Dev-only mock data
├── utils/
│   ├── transformGraph.ts            (42)   — Re-export facade
│   ├── transformExplore.ts          (467)  — L2/L3 transforms
│   ├── transformOverview.ts         (281)  — L1 transforms
│   ├── transformHelpers.ts          (146)  — Shared utilities
│   ├── transformColumns.ts          (127)  — Column enrichment
│   ├── layoutGraph.ts               (183)  — ELK layout + cache + Worker
│   ├── layoutL1.ts                  (204)  — L1 positions
│   ├── filterGraph.ts                     — Node filtering utils
│   ├── cn.ts                              — className utility
│   ├── transformHelpers.test.ts     (193)  — Tests
│   └── transformColumns.test.ts     (176)  — Tests
├── workers/
│   └── elkWorker.ts                 (23)   — ELK in Web Worker
├── types/
│   ├── domain.ts                    (134)
│   ├── graph.ts
│   └── api.ts
└── i18n/
    ├── config.ts
    └── locales/{en,ru}/common.json
```

**Три уровня визуализации:**

| Уровень | Что показывает | Hook | Transform |
|---------|---------------|------|-----------|
| L1 — Overview | Applications → Services → Databases → Schemas | `useOverview()` | `transformGqlOverview()` |
| L2 — Explore | Tables, Routines, Statements, Packages внутри Schema | `useExplore(scope)` | `transformGqlExplore()` |
| L3 — Lineage | Column-level lineage, subqueries, atoms | `useLineage(nodeId)` | `transformGqlExplore()` |

**Pipeline фильтрации (useDisplayGraph — 6 фаз):**

1. L1 system-level + depth filtering
2. Hide nodes (red button — LOOM-026)
3. Table-level view (suppress column edges)
4. Direction filtering (upstream/downstream toggle)
5. Field-level filtering (dim unrelated columns)
6. L1 hierarchy filtering (App → DB → Schema cascade)

### 2.2 SHUTTLE (backend GraphQL API)

| Параметр | Значение |
|----------|----------|
| Фреймворк | Quarkus 3.34.2 + Java 21 |
| API | SmallRye GraphQL |
| БД клиент | REST client → ArcadeDB HTTP API |
| Сборка | Gradle 9 |
| Порт | 8080 |

**Файловая структура (30 файлов, ~2,553 LOC):**

```
SHUTTLE/src/main/java/studio/seer/lineage/
├── client/
│   ├── ArcadeGateway.java           — HTTP facade (sql/cypher with error logging)
│   ├── ArcadeDbClient.java          — REST client interface
│   ├── ArcadeCommand.java           — Command DTO
│   └── ArcadeResponse.java          — Response parser
├── model/
│   ├── ExploreResult.java           — GraphQL response (nodes + edges)
│   ├── GraphNode.java / GraphEdge.java / SchemaNode.java / SearchResult.java
│   └── Knot*.java (8 records)       — KnotSession, KnotReport, KnotStatement, etc.
├── resource/
│   ├── LineageResource.java         — GraphQL endpoint (9 queries)
│   └── KnotResource.java           — GraphQL endpoint (KNOT)
├── security/
│   └── SeerIdentity.java           — Extract X-Seer-Role/User headers
└── service/
    ├── ExploreService.java          (~570) — L2 scope explore (schema/pkg/db/rid)
    ├── LineageService.java          (~147) — L3 lineage + expandDeep
    ├── OverviewService.java               — L1 overview
    ├── SearchService.java           (~113) — Full-text search (12 types)
    └── KnotService.java             (~600) — KNOT report (sessions, structure, flow)
```

**GraphQL Queries (9):**

| Query | Service | Описание |
|-------|---------|----------|
| `overview` | OverviewService | Агрегированный список DaliSchema с counts |
| `explore(scope)` | ExploreService | Граф внутри schema/package/rid |
| `stmtColumns(ids)` | ExploreService | Column enrichment for statements |
| `lineage(nodeId)` | LineageService | Bidirectional 1-hop lineage |
| `upstream(nodeId)` | LineageService | Incoming edges only |
| `downstream(nodeId)` | LineageService | Outgoing edges only |
| `expandDeep(nodeId, depth)` | LineageService | Multi-hop (1–10) expansion |
| `search(query, limit)` | SearchService | Full-text search (12 Dali types) |
| `me` | SeerIdentity | Username + role |

**+ KNOT GraphQL Queries:**

| Query | Service | Описание |
|-------|---------|----------|
| `knotSessions` | KnotService | All parsing sessions |
| `knotReport(sessionId)` | KnotService | Full report (structure + flow + atoms) |

**Scope format (ExploreService):**
- `"schema-DWH"` → DaliSchema by name
- `"schema-DWH|DatabaseName"` → DaliSchema with DB filter
- `"pkg-MY_PKG"` → DaliPackage by name
- `"db-DatabaseName"` → All schemas in a database
- `"#10:0"` → Raw @rid (generic bidirectional explore)

### 2.3 Chur (auth gateway)

| Параметр | Значение |
|----------|----------|
| Фреймворк | Fastify 4 + TypeScript |
| Auth | @fastify/jwt (JWT в httpOnly cookies) |
| Passwords | bcryptjs |
| Rate limiting | In-memory (5 req / 15 min per IP in prod) |
| Порт | 3000 |

**Файловая структура (9 файлов, ~505 LOC):**

```
Chur/src/
├── server.ts          — Entry: Fastify setup, CORS allowlist, plugins, routes
├── config.ts          — Env-based config (port, JWT, ArcadeDB, CORS)
├── users.ts           — User verification (bcrypt)
├── arcade.ts          — ArcadeDB HTTP client
├── types.ts           — JWT payload augmentation
├── plugins/
│   └── rbac.ts        — authenticate + authorizeQuery decorators
└── routes/
    ├── auth.ts        — /login (rate-limited), /me, /refresh, /logout
    ├── graphql.ts     — Proxy → SHUTTLE with X-Seer headers
    └── query.ts       — Direct SQL/Cypher → ArcadeDB (admin only)
```

**Эндпоинты:**

| Route | Method | Auth | Описание |
|-------|--------|------|----------|
| `/auth/login` | POST | — | Login + JWT cookie (rate-limited) |
| `/auth/me` | GET | JWT | Current user |
| `/auth/refresh` | POST | JWT | Silent token renewal |
| `/auth/logout` | POST | — | Clear JWT cookie |
| `/graphql` | POST/GET | JWT | Proxy → SHUTTLE |
| `/api/query` | POST | JWT+admin | Direct SQL/Cypher → ArcadeDB |
| `/health` | GET | — | Health check |

**Security model:**
- JWT stored in httpOnly cookie (XSS-safe)
- CORS allowlist (no wildcard)
- Rate limiting on /auth/login (5/15min prod)
- Write operations require `admin` role (regex detection)
- GraphQL proxied with trusted headers (private network)

### 2.4 ArcadeDB (Hound)

Graph database, порт 2480. Схема определяется проектом **Hound** (Java-парсер Data Lineage).

**Основные типы вершин (15):**
DaliApplication, DaliService, DaliDatabase, DaliSchema, DaliTable, DaliColumn, DaliPackage, DaliRoutine, DaliStatement, DaliSession, DaliOutputColumn, DaliParameter, DaliVariable, DaliAtom, DaliJoin.

**Основные типы рёбер (12):**
CONTAINS_TABLE, CONTAINS_COLUMN, CONTAINS_ROUTINE, CONTAINS_SCHEMA, CONTAINS_STMT, HAS_COLUMN, HAS_OUTPUT_COL, READS_FROM, WRITES_TO, CALLS, HAS_SERVICE, HAS_DATABASE.

---

## 3. Поток данных

```
[Hound Parser]
    │  Парсит SQL/DDL → создаёт вершины и рёбра в ArcadeDB
    ▼
[ArcadeDB :2480]
    │  Хранит граф Data Lineage
    ▼
[SHUTTLE :8080]  ← Cypher + SQL (parameterized)
    │  SmallRye GraphQL API (9 queries + 2 KNOT queries)
    │  .onFailure().recoverWithItem(List.of()) on all Uni chains
    ▼
[Chur :3000]  ← HTTP proxy + JWT auth
    │  CORS allowlist + rate limiting
    │  X-Seer-Role/X-Seer-User headers
    ▼
[verdandi :5173]
    │  graphql-request → React Query hooks (retry:2, staleTime:30s)
    │  transform pipeline → ELK.js layout (Web Worker)
    │  ErrorBoundary + status UI
    ▼
[Браузер — Interactive Canvas]
    │  LOOM (3-level visualization) + KNOT (report inspector)
```

---

## 4. State Management (Zustand)

### loomStore — 647 LOC, 11 категорий

| Категория | Ключевые поля | Описание |
|-----------|--------------|----------|
| View Navigation | viewLevel, currentScope, navigationStack | L1/L2/L3 переключение |
| L1 Scope Filter | l1ScopeStack, expandedDbs | App/Service scope narrowing |
| L1 Toolbar | l1Filter, l1HierarchyFilter | Depth, direction, system-level |
| L1 Data Lists | availableApps, availableDbs, availableSchemas | Populate filter dropdowns |
| Node Selection | selectedNodeId, highlightedNodes/Edges | Click → select, path tracing |
| Filter Toolbar | filter (startObjectId, fieldFilter, depth, etc.) | L2/L3 field-level filtering |
| Theme | theme, palette | Dark/light + 5 palettes |
| Graph Stats | nodeCount, edgeCount, zoom | StatusBar display |
| Node Expansion | nodeExpansionState, hiddenNodeIds | LOOM-026 (collapsed/partial/expanded) |
| Upstream/Downstream | expandRequest, expandedIds, expansionGqlNodes/Edges | LOOM-027 |
| Viewport | fitViewRequest | Focus on node or fit all |

### authStore — 93 LOC

| Поле | Описание |
|------|----------|
| user | { username, role } |
| isAuthenticated | boolean |
| login()/logout() | JWT cookie management |
| checkSession() | GET /auth/me |

---

## 5. Ключевые решения (ADR)

### ADR-001: React Flow + ELK.js
- @xyflow/react для rendering, ELK.js для layout
- ELK bundle ~2MB, lazy loaded через Web Worker (Sprint 6)
- Fallback на grid layout при ошибке

### ADR-002: Zustand (не Redux, не Context)
- Минимальный boilerplate, селекторы, no provider wrapper

### ADR-003: Chur как auth-gateway
- Fastify обслуживает auth + proxy (не в Quarkus)
- Двойной hop: Browser → Chur → SHUTTLE → ArcadeDB

### ADR-004b: GraphQL через SHUTTLE
- Frontend не шлёт Cypher — использует typed GraphQL API

### ADR-005: Parallel queries вместо Cypher UNION
- ArcadeDB UNION дедуплицирует labels()[0]
- Отдельные параллельные запросы + merge в Java

### ADR-006: Named parameter binding (Sprint 6)
- SQL queries используют `:q` binding вместо String.format()
- Cypher queries используют `$param` binding
- Integer LIMIT — единственное место String.format() (validated)

### ADR-007: Canvas hook extraction (Sprint 6)
- LoomCanvas → 6 focused hooks
- Каждый hook имеет единственную ответственность
- Hooks compose в LoomCanvas (346 LOC orchestrator)

---

## 6. Порты и сервисы

| Сервис | Порт | Протокол | Зависит от |
|--------|------|----------|-----------|
| ArcadeDB | 2480 | HTTP REST | — |
| SHUTTLE | 8080 | HTTP (GraphQL) | ArcadeDB |
| Chur | 3000 | HTTP | SHUTTLE, ArcadeDB |
| verdandi | 5173 | HTTP (Vite dev) | Chur |

Запуск: `./gradlew devAll` — открывает все 3 сервиса в отдельных окнах (Windows) или parallel processes (Unix).

---

## 7. Известные ограничения (09.04.2026)

1. **Docker Compose не реализован** — `devAll` работает, но ArcadeDB запускается вручную
2. **Тестовое покрытие ~5%** — 2 test-файла, нет integration/e2e
3. **CI pipeline отсутствует** — PR не проверяются автоматически
4. **ColumnInfo.type не заполняется** — требует Hound fix
5. **HOUND-DB-001 не начат** — orphan DaliDatabase невидимы
6. **loomStore — 647 LOC** — нужна slice-декомпозиция
7. **JWT secret default** — `dev-secret-change-in-prod` в config.ts

### Закрытые ограничения (vs 05.04)

- ~~SQL injection в SearchService~~ → named params ✅
- ~~Нет error handling~~ → ErrorBoundary + .onFailure() ✅
- ~~KNOT Inspector не реализован~~ → InspectorPanel + 5 sub ✅
- ~~Тесты отсутствуют~~ → 2 тестовых файла (начало) ✅
- ~~CORS wildcard~~ → allowlist ✅
- ~~Нет rate limiting~~ → auth.ts rate limiter ✅
