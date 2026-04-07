# VERDANDI — Архитектурный обзор системы

**Дата:** 05.04.2026
**Версия:** 1.0 (автоматически сгенерировано по результатам code review)

---

## 1. Назначение

**VERDANDI** — часть экосистемы **SEER Studio**, IDE-подобного инструмента для визуализации Data Lineage. VERDANDI реализует ипостась **LOOM** — интерактивный граф-визуализатор, отображающий потоки данных между базами, схемами, таблицами, колонками, рутинами и SQL-statements.

**Пользователи:** Data Engineer, Data Analyst, DBA / Architect.

---

## 2. Компоненты

### 2.1 verdandi (frontend)

| Параметр | Значение |
|----------|----------|
| Фреймворк | React 19 + TypeScript 5.9 |
| Сборка | Vite 8 |
| Graph rendering | @xyflow/react (React Flow) |
| Layout engine | ELK.js (elkjs/lib/elk.bundled.js) |
| State | Zustand (loomStore + authStore) |
| Data fetching | TanStack Query (React Query) + graphql-request |
| Styling | Tailwind CSS + CSS Variables (SEER Design System) |
| i18n | react-i18next (EN, RU) |
| UI kit | shadcn/ui + Lucide icons |
| Порт | 5173 |

**Три уровня визуализации:**

| Уровень | Что показывает | Данные |
|---------|---------------|--------|
| L1 — Overview | Applications → Services → Databases → Schemas | `useOverview()` → `transformGqlOverview()` |
| L2 — Explore | Tables, Routines, Statements, Packages внутри Schema | `useExplore(scope)` → `transformGqlExplore()` |
| L3 — Lineage | Column-level lineage, subqueries, atoms | `useLineage(nodeId)` → `transformGqlExplore()` |

**Pipeline фильтрации (6 фаз в LoomCanvas):**

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

**GraphQL Queries:**

| Query | Service | Описание |
|-------|---------|----------|
| `overview` | OverviewService | Агрегированный список DaliSchema с counts |
| `explore(scope)` | ExploreService | Граф внутри schema/package/rid |
| `lineage(nodeId)` | LineageService | Upstream/downstream для одной ноды |
| `upstream(nodeId)` | LineageService | Только upstream edges |
| `downstream(nodeId)` | LineageService | Только downstream edges |
| `search(query, limit)` | SearchService | Полнотекстовый поиск по 12 типам Dali |
| `me` | SeerIdentity | Username + role текущего пользователя |

**Scope format (ExploreService):**
- `"schema-DWH"` → DaliSchema по имени
- `"schema-DWH|DatabaseName"` → DaliSchema с фильтром по БД
- `"pkg-MY_PKG"` → DaliPackage по имени
- `"#10:0"` → Raw @rid (generic bidirectional explore)

### 2.3 Chur (auth gateway)

| Параметр | Значение |
|----------|----------|
| Фреймворк | Fastify 4 + TypeScript |
| Auth | @fastify/jwt (JWT в httpOnly cookies) |
| Passwords | bcryptjs |
| Порт | 3000 |

**Эндпоинты:**

| Route | Method | Описание |
|-------|--------|----------|
| `/auth/login` | POST | Аутентификация, выдача JWT cookie |
| `/auth/me` | GET | Текущий пользователь |
| `/auth/logout` | POST | Удаление JWT cookie |
| `/graphql` | POST/GET | Proxy → SHUTTLE с X-Seer-Role/X-Seer-User |
| `/api/query` | POST | Direct SQL/Cypher → ArcadeDB (admin only) |
| `/health` | GET | Health check |

**Security model:**
- JWT stored in httpOnly cookie (XSS-safe)
- Write operations (INSERT, UPDATE, DELETE, CREATE, DROP, ALTER) require `admin` role
- GraphQL proxied with trusted headers (private network assumption)

### 2.4 ArcadeDB (Hound)

Graph database, порт 2480. Схема определяется проектом **Hound** (Java-парсер Data Lineage).

**Основные типы вершин:**
DaliApplication, DaliService, DaliDatabase, DaliSchema, DaliTable, DaliColumn, DaliPackage, DaliRoutine, DaliStatement, DaliSession, DaliOutputColumn, DaliParameter, DaliVariable, DaliAtom, DaliJoin.

**Основные типы рёбер:**
CONTAINS_TABLE, CONTAINS_COLUMN, CONTAINS_ROUTINE, CONTAINS_SCHEMA, HAS_COLUMN, READS_FROM, WRITES_TO, CALLS, DEPENDS_ON, HAS_SERVICE, HAS_DATABASE, USES_DATABASE, BELONGS_TO_APP.

---

## 3. Поток данных

```
[Hound Parser]
    │  Парсит SQL/DDL → создаёт вершины и рёбра в ArcadeDB
    ▼
[ArcadeDB :2480]
    │  Хранит граф Data Lineage
    ▼
[SHUTTLE :8080]  ← Cypher + SQL queries
    │  Трансформирует в GraphQL schema (GraphNode, GraphEdge, SchemaNode, SearchResult)
    ▼
[Chur :3000]  ← HTTP proxy + JWT auth
    │  Добавляет X-Seer-Role/X-Seer-User headers
    ▼
[verdandi :5173]
    │  graphql-request → React Query hooks
    │  transformGqlOverview/Explore → LoomNode/LoomEdge
    │  ELK.js layout → React Flow rendering
    ▼
[Браузер — Interactive Canvas]
```

---

## 4. State Management (Zustand)

### loomStore — 11 категорий

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

### authStore

| Поле | Описание |
|------|----------|
| user | { username, role } |
| isAuthenticated | boolean |
| login()/logout() | JWT cookie management |
| checkSession() | GET /auth/me |

---

## 5. Ключевые решения (ADR)

### ADR-001: React Flow + ELK.js

**Решение:** @xyflow/react для rendering, ELK.js для автоматического layout.
**Причина:** React Flow — зрелая библиотека с compound nodes, minimap, controls. ELK — единственный JS layout engine с поддержкой иерархических графов.
**Следствие:** ELK.js bundle ~2MB (lazy loaded). Fallback на grid layout при ошибке.

### ADR-002: Zustand (не Redux, не Context)

**Решение:** Zustand для обоих stores.
**Причина:** Минимальный boilerplate, отсутствие provider-обёртки, поддержка селекторов.

### ADR-003: Chur остаётся как auth-gateway

**Решение:** Chur (Fastify) обслуживает auth + proxy вместо переноса auth в Quarkus.
**Причина:** Быстрее для MVP. Quarkus SmallRye JWT требует дополнительной настройки token issuance. Chur уже работает.
**Следствие:** Двойной hop (Browser → Chur → SHUTTLE → ArcadeDB). Допустимо для MVP.

### ADR-004b: Quarkus GraphQL вместо raw Cypher

**Решение:** Frontend не шлёт Cypher напрямую — использует GraphQL API через SHUTTLE.
**Причина:** Безопасность (нет arbitrary query execution), типизация, intent-based queries.
**Следствие:** Новые запросы требуют изменений в SHUTTLE, а не только на фронте.

### ADR-005: Workaround — parallel queries вместо Cypher UNION

**Решение:** В ExploreService и LineageService отдельные параллельные запросы вместо UNION ALL.
**Причина:** ArcadeDB Cypher UNION дедуплицирует labels()[0] (List<String>), теряя type-информацию.
**Следствие:** Больше HTTP-запросов к ArcadeDB, но корректные данные.

---

## 6. Порты и сервисы

| Сервис | Порт | Протокол | Зависит от |
|--------|------|----------|-----------|
| ArcadeDB | 2480 | HTTP REST | — |
| SHUTTLE | 8080 | HTTP (GraphQL) | ArcadeDB |
| Chur | 3000 | HTTP | SHUTTLE, ArcadeDB |
| verdandi | 5173 | HTTP (Vite dev) | Chur |

Порядок запуска: ArcadeDB → SHUTTLE → Chur → verdandi.

---

## 7. Известные ограничения (05.04.2026)

1. **Docker Compose не реализован** — сервисы запускаются вручную
2. **Тесты отсутствуют** — ни unit, ни integration, ни e2e
3. **SQL injection в SearchService** — String.format() с user input
4. **Нет error handling** — ни на фронте (пустые/ошибочные ответы), ни на бэке (Uni без .onFailure())
5. **Column type не заполняется** — ColumnInfo.type всегда пустая строка
6. **KNOT Inspector не реализован** — правая панель отсутствует
7. **HOUND-DB-001 не начат** — ad-hoc DaliDatabase-вершины невидимы для SHUTTLE
