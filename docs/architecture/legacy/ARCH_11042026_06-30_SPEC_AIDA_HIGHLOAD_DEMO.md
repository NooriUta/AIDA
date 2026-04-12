> ⚠️ **УСТАРЕВШИЙ ДОКУМЕНТ (deprecated, 12.04.2026)**
> Терминология в этом документе устарела после сессии v2.2 (11.04.2026):
> — везде, где написано «AIDA architecture» или «AIDA components», имеется в виду **SEER Studio** (первый продукт внутри AIDA платформы)
> — HEIMDALL в этом документе описан как «in-process Dali module» (ADR-DA-001) — это решение **REVOKED**, заменено на ADR-DA-010 (отдельный сервис)
> — актуальные решения: `MODULES_TECH_STACK.md` v2.3, `REFACTORING_PLAN.md` v1.1, `DECISIONS_LOG.md` v1.1
> Документ сохранён как исторический источник demo narrative, Track A/B split (§8), и demo safety patterns (§6-7).

# AIDA — Техническая спецификация архитектуры (HighLoad demo October 2026)

**Документ:** `ARCH_11042026_06-30_SPEC_AIDA_HIGHLOAD_DEMO`
**Тип:** Architecture specification (детальная)
**Дата:** 11.04.2026
**Версия:** 1.0
**Companion к:** `ARCH_11042026_06-30_REVIEW_AIDA_HIGHLOAD_DEMO.md`
**Source of truth:** `active/AIDA_TARGET_ARCH_HIGHLOAD_2026.md`

---

## 0. Цель и scope

`ARCH-review` отвечает на вопросы **«в каком состоянии архитектура»** и **«куда двигаться»**: ревью, векторы, SWOT, gap-анализ, рекомендации.

`ARCH-spec` (этот документ) отвечает на вопрос **«как именно»**: компоненты, интерфейсы, события, данные, deployment topology, ADR, performance budgets. Это **daily engineering reference** для команды.

Два документа работают вместе. ARCH-review — для еженедельного planning и решений по scope. ARCH-spec — для разработки.

**Философия архитектуры:**

> Архитектура — не теория, а **контракт между компонентами**. Каждый компонент имеет explicit interface, который позволяет parallel development без постоянной координации между co-founder.

**Принципы (наследуются из v4 + v2 update):**

1. **Algorithms-as-tools, не vector RAG** (ADR-DA-007 ← унаследован из v4 ADR-014)
2. **Observability-first** — каждый компонент эмитит структурированные события в HEIMDALL
3. **Deterministic where it matters** — ANVIL — pure graph algorithm, MIMIR только для NL-интерфейса
4. **Scope discipline** — явный defer list блокирует feature creep
5. **Parallel development по двум трекам** с чёткими интерфейсами
6. **Demo safety built-in** — fallback'и, кэши, recovery на каждом уровне

**Out of scope этого документа:**
- Production-ready архитектура (post-HighLoad work)
- Multi-tenancy, billing, SSO, SOC2 (commercial post-MVP)
- Cloud deployment (post-pilot)
- Native интеграции с dbt/Airflow/Snowflake (H3+)

---

## 1. Шесть слоёв архитектуры

Система организована в 6 слоёв. Слои 1–5 — стандартный layered подход, слой 6 (HEIMDALL) — cross-cutting (получает события от всех ниже и от себя самого).

### 1.1 Слой 1 — Data Layer

**Назначение:** хранение графа lineage, метаданных, пользовательских настроек, исходных файлов.

**Компоненты:**
- **YGG** — ArcadeDB multi-model database (graph + document + key-value), single source of truth для lineage data
- **FRIGG** — saved LOOM views (October scope только)
- **File System** — исходные SQL-файлы для парсинга

### 1.2 Слой 2 — Platform Services + Algorithms

**Назначение:** базовые алгоритмы (парсинг, traversal, impact) и платформенные сервисы (auth, write API, models).

**Компоненты:**
- **Hound** — SQL/PL-SQL парсер, извлекает atoms и lineage в YGG
- **ANVIL** — pure graph algorithm для impact analysis (single use case `find_downstream_impact` для October)
- **Dali Core** — единый source of truth для data models, write API к YGG
- **Chur** — JWT validator (в demo режиме — single admin)

### 1.3 Слой 3 — MIMIR (AI Orchestration)

**Назначение:** orchestrator LLM-вызовов с tool calling pattern. Не владеет бизнес-логикой — вся логика в ANVIL/Hound, MIMIR только маршрутизирует.

**Компоненты MIMIR:**
- LLM client (Claude primary, OpenAI fallback, local Qwen ultimate fallback)
- Tool registry с 5 tools для October
- Intent parser
- Response synthesizer
- Conversation state (in-memory для demo)
- Demo query cache (для live safety)
- Event emitter в HEIMDALL

### 1.4 Слой 4 — SHUTTLE (API Gateway)

**Назначение:** единая точка входа для UI к backend. GraphQL для queries/mutations, WebSocket для real-time событий.

**Endpoints October scope:**
- GraphQL Query (sessions, nodes, lineage, search, KNOT data, FRIGG views)
- GraphQL Mutation (startParseSession, saveView, deleteView)
- GraphQL Subscription (heimdallEvents, sessionProgress)
- HTTP routing + Chur JWT validation + rate limiting

### 1.5 Слой 5 — SEER Studio (User Applications)

**Назначение:** пользовательские приложения, UI слой.

**Компоненты:**
- **LOOM** — graph canvas, главный визуальный UI для демо (60% screen time)
- **KNOT** — inspector с детальным structure breakdown
- **ANVIL UI** — view для impact analysis результатов
- **MIMIR Chat** — natural language interface

### 1.6 Слой 6 — HEIMDALL (Cross-cutting Demo Observability)

**Назначение:** делает работу всех компонентов видимой в real time. Это **главный differentiator** демо для HighLoad audience.

**Scope ровно:** demo observability. Не production observability.

**Компоненты HEIMDALL:**
- Event bus (in-process ring buffer 10K events)
- Metrics aggregator (counters, gauges, histograms)
- Real-time dashboard (WebSocket streaming)
- Event stream viewer (filtered, color-coded)
- Demo controls (recording, replay, reset)

**Cross-cutting означает:** все компоненты слоёв 2–5 эмитят события → HEIMDALL event bus → metrics + stream → dashboard. HEIMDALL не вызывает другие компоненты, только пассивно собирает события.

---

## 2. Спецификации компонентов

### 2.1 Hound

**Слой:** 2 (Algorithms)
**Текущее состояние:** PL/SQL парсер production-ready (9920 atoms, 78.5% resolution)
**Целевое к октябрю:** PL/SQL + PostgreSQL, ≥85% resolution rate, событийная эмиссия

**Интерфейс — parse request:**
```
parse_session(
  session_id: string,
  files: [FileRef],
  dialect: enum(PLSQL, POSTGRESQL, CLICKHOUSE),
  options: {
    resolve_dynamic_sql: bool,
    emit_events: bool,
    parallel_workers: int
  }
) -> ParseResult {
  session_id,
  atoms_extracted: int,
  resolution_rate: float,
  unresolved_categories: {category: count},
  graph_nodes_created: int,
  graph_edges_created: int,
  duration_ms: int,
  errors: [ErrorRef]
}
```

**События в HEIMDALL:**
- `hound.session_started` — `{session_id, file_count}`
- `hound.file_parsing_started` — `{session_id, file_name, size_bytes}`
- `hound.atom_extracted` — `{session_id, atom_type, resolved}` (батчами по 100, чтобы не задавить bus)
- `hound.file_parsing_completed` — `{session_id, file_name, atoms, duration_ms}`
- `hound.session_completed` — `{session_id, total_atoms, resolution_rate, duration_ms}`
- `hound.dynamic_sql_detected` — `{file, location}` (для Correctness demo)
- `hound.nested_cte_resolved` — `{depth, columns}`
- `hound.window_function_parsed` — `{partition_by_count, order_by_count}`
- `hound.error` — `{session_id, file_name, error_type, message}`

**Внутренняя структура:**
```
Hound/
├── dialects/
│   ├── plsql/          ← production ready (ANTLR-based)
│   │   ├── lexer
│   │   ├── parser
│   │   ├── atom_extractor
│   │   └── resolver
│   ├── postgresql/     ← Apr-Jun work
│   └── clickhouse/     ← May-Jul (droppable, ADR-DA-005)
├── core/
│   ├── session_manager
│   ├── graph_writer (→ YGG)
│   └── event_emitter (→ HEIMDALL)
└── benchmark/
    └── comparison_runner (sqlglot, sqllineage)
```

**Performance targets:**
- 50K LoC файл: <5 сек
- Batch 500K LoC (10 файлов × 50K): <30 сек с 4 parallel workers
- Memory peak: <2 GB RSS
- Throughput: >10K atoms/sec на single файл
- Resolution rate: ≥85% на demo dataset

**Зависимости:** YGG (writes), File System (reads), HEIMDALL (events, optional)

### 2.2 YGG

**Слой:** 1 (Data Layer)
**Реализация:** ArcadeDB multi-model
**Текущее состояние:** Working

**Почему ArcadeDB:**
- Native graph + document в одном engine
- Cypher + SQL + GraphQL
- Embedded или server modes (важно для local demo)
- Open source, no vendor lock
- TimeSeries type (был под MUNINN, deferred)

**Schema v1 (October scope):**

**Vertex types:**
- `Session` — метаданные парсинг-сессии
- `Schema` — DB schema
- `Table` — определение таблицы
- `Column` — определение колонки
- `Procedure` — stored procedure
- `Query` — SQL query (SELECT/INSERT/UPDATE/DELETE)
- `Atom` — атомарное выражение (column ref, function call, literal)

**Edge types:**
- `CONTAINS` — Schema→Table, Table→Column, Procedure→Query
- `READS_FROM` — Query→Table, Query→Column
- `WRITES_TO` — Query→Table, Query→Column
- `DEPENDS_ON` — Column→Column (transformation dependency)
- `CALLS` — Procedure→Procedure
- `PARSED_IN` — any_node→Session

**Ключевые свойства Column:**
```
Column {
  id: string (unique)
  schema_name: string
  table_name: string
  column_name: string
  data_type: string
  nullable: bool
  created_in_session: string
  atom_references: [Atom.id]
}
```

**Индексы:**
- Unique: `Column.id`, `Table.id`, `Procedure.id`
- Full-text: `Column.column_name`, `Table.table_name`, `Procedure.name`
- Composite: `(schema_name, table_name, column_name)`

**Demo data volumes:**
- ~50K vertices, ~100K edges (после парсинга 500K LoC)
- Memory footprint: <2 GB
- Performance: <100 ms на 5-hop traversal, <500 ms на 10-hop worst case

**События:** YGG не эмитит события напрямую — это делают компоненты, которые её вызывают (Hound, ANVIL, SHUTTLE, Dali Core).

### 2.3 SHUTTLE

**Слой:** 4 (API Gateway)
**Текущее состояние:** GraphQL read API working
**Целевое:** + mutations, + WebSocket events, + rate limiting

**GraphQL schema (October scope):**

```graphql
type Query {
  sessions(limit: Int, offset: Int): [Session!]!
  session(id: ID!): Session
  node(id: ID!): GraphNode
  nodeByName(schema: String!, table: String!, column: String): GraphNode
  searchNodes(query: String!, types: [NodeType!]): [GraphNode!]!
  upstream(nodeId: ID!, maxDepth: Int = 5): LineageSubgraph!
  downstream(nodeId: ID!, maxDepth: Int = 5): LineageSubgraph!
  sessionStructure(sessionId: ID!): SessionStructure!
  userViews(userId: ID!): [SavedView!]!
}

type Mutation {
  startParseSession(input: ParseSessionInput!): Session!
  saveView(input: SaveViewInput!): SavedView!
  deleteView(id: ID!): Boolean!
  askMimir(question: String!, sessionId: ID!): MimirAnswer!
}

type Subscription {
  heimdallEvents(filter: EventFilter): HeimdallEvent!
  sessionProgress(sessionId: ID!): SessionProgress!
}
```

**WebSocket protocol:**
- Client subscribes на `heimdallEvents` с optional filter
- Server стримит events as they arrive
- Server-side batching: до 50 events за 100ms chunk (anti-overwhelm)
- Reconnect: client получает последние 100 events

**Authentication:**
- JWT в `Authorization: Bearer <token>`
- Chur валидирует, извлекает user_id
- Demo режим: single admin user, pre-generated токен

**Rate limiting:**
- 100 req/sec per client
- WebSocket: 1000 events/sec на клиента

**События в HEIMDALL:**
- `shuttle.request_received` — `{method, path, user_id}`
- `shuttle.request_completed` — `{method, path, status, duration_ms}`
- `shuttle.subscription_opened` — `{type, user_id}`
- `shuttle.subscription_closed` — `{type, user_id, duration_sec}`

### 2.4 MIMIR

**Слой:** 3 (AI Orchestration)
**Текущее состояние:** Не начато
**Целевое:** Отвечать на 5 demo queries reliably + полная событийная прозрачность + 3-tier fallback

**ADR reference:** ADR-DA-007 (algorithms-as-tools, унаследован из v4 ADR-014).

> «MIMIR не владеет бизнес-логикой lineage. Вся логика — в ANVIL / URD / SKULD / Dali Core. MIMIR только маршрутизирует запросы и формирует ответ.»

**Архитектура MIMIR:**

```
MIMIR/
├── llm_client/
│   ├── claude_adapter (primary)
│   ├── openai_adapter (fallback tier 2)
│   └── local_adapter (Qwen via Ollama, ultimate fallback)
├── tool_registry/
│   ├── schema_definitions.json
│   └── implementations/
│       ├── query_lineage.py     → ANVIL/YGG
│       ├── find_impact.py       → ANVIL
│       ├── search_nodes.py      → SHUTTLE GraphQL
│       ├── get_procedure_source → YGG
│       └── count_dependencies   → YGG
├── intent_parser/
│   └── prompt_templates.md
├── response_synthesizer/
│   └── response_builder.py
├── conversation_state/
│   └── session_memory.py (in-memory для demo)
├── cache/
│   └── demo_query_cache.json (live demo safety)
└── event_emitter/
    └── heimdall_events.py
```

**Tool registry — October scope (5 tools):**

```json
{
  "tools": [
    {
      "name": "query_lineage",
      "description": "Find upstream or downstream lineage from a node",
      "input_schema": {
        "type": "object",
        "properties": {
          "node_id": {"type": "string"},
          "direction": {"type": "string", "enum": ["upstream", "downstream"]},
          "max_depth": {"type": "integer", "default": 5}
        },
        "required": ["node_id", "direction"]
      }
    },
    {
      "name": "find_impact",
      "description": "Find what will break if a column is modified or removed",
      "input_schema": {
        "type": "object",
        "properties": {
          "node_id": {"type": "string"},
          "max_depth": {"type": "integer", "default": 5}
        },
        "required": ["node_id"]
      }
    },
    {
      "name": "search_nodes",
      "description": "Find nodes by name or partial match",
      "input_schema": {
        "type": "object",
        "properties": {
          "query": {"type": "string"},
          "types": {"type": "array", "items": {"type": "string"}}
        },
        "required": ["query"]
      }
    },
    {
      "name": "get_procedure_source",
      "description": "Fetch source code of a stored procedure",
      "input_schema": {
        "type": "object",
        "properties": {"procedure_id": {"type": "string"}},
        "required": ["procedure_id"]
      }
    },
    {
      "name": "count_dependencies",
      "description": "Count how many nodes depend on a given node",
      "input_schema": {
        "type": "object",
        "properties": {"node_id": {"type": "string"}},
        "required": ["node_id"]
      }
    }
  ]
}
```

**События в HEIMDALL (критично для demo transparency):**
- `mimir.query_received` — `{session_id, question_text_sample, tokens}`
- `mimir.llm_request_started` — `{model, prompt_tokens}`
- `mimir.llm_response_received` — `{latency_ms, completion_tokens, tool_calls_count}`
- `mimir.tool_call_started` — `{tool_name, input_preview}`
- `mimir.tool_call_completed` — `{tool_name, duration_ms, result_count}`
- `mimir.response_synthesized` — `{total_duration_ms, total_tokens, affected_nodes}`

**Demo safety features:**
- **Query cache:** последние 5 demo queries закэшированы с их tool calls и responses. Если LLM падает, fallback на cached response с искусственной задержкой (выглядит как real LLM call).
- **Whitelist questions:** в live demo доступны только вопросы из whitelist. Free-form input скрыт за hotkey, доступен только в rehearsal.
- **Hard timeout:** 30 сек на полный запрос. После — automatic fallback.

**LLM cost:**
- ~5 queries × ~3000 tokens × ~10 rehearsals = ~150K tokens
- Claude Sonnet: ~$0.45 input + ~$2.25 output на 150K
- Total dev cost: ~$3-5 за rehearsal цикл
- Budgeted: 150K ₽ (~$1.5K)

### 2.5 ANVIL

**Слой:** 2 (Algorithms)
**Текущее состояние:** Не начато
**Целевое:** Один use case реализован — `find_downstream_impact`

**Полная спецификация:** см. `DISCOVERY_ANVIL.md`. Краткая версия здесь.

**October scope:**
- `find_downstream_impact(node_id, max_depth)` — что сломается при изменении node
- `find_upstream_sources(node_id, max_depth)` — откуда приходят данные
- Pure Cypher queries на YGG, без state

**Ключевое свойство:** **deterministic and fast**. Это и есть differentiation от LLM hallucination. ANVIL — это «вот пример того, что LLM не должен делать сам», и MIMIR явно показывает audience: «теперь я вызываю ANVIL — это не LLM, это алгоритм».

**Cypher query example для `find_downstream_impact`:**
```cypher
MATCH (start:Column {id: $node_id})
MATCH (start)<-[:DEPENDS_ON*1..$max_depth]-(affected)
RETURN DISTINCT affected
```

**События в HEIMDALL:**
- `anvil.impact_analysis_started` — `{node_id, direction, max_depth}`
- `anvil.traversal_progress` — `{nodes_visited, edges_traversed}`
- `anvil.impact_analysis_completed` — `{duration_ms, results_count, truncated}`

### 2.6 LOOM

**Слой:** 5 (SEER Studio)
**Текущее состояние:** Production для маленьких графов (10 узлов tested)
**Целевое:** Smooth render 5–10K узлов + presentation mode + split-screen с HEIMDALL

**Технологический стек:**
- Frontend: React + TypeScript
- Graph rendering: Sigma.js / Cytoscape / WebGL — **decision до конца апреля (ADR-DA-004)**
- State: Zustand или Redux (existing codebase)
- Communication: GraphQL Apollo client + WebSocket для HEIMDALL events

**Стратегия рендеринга для scale:**

```
По уровням zoom:
- Far zoom (overview): aggregate nodes по schema/table, простые формы
- Mid zoom: показываем tables, скрываем individual columns
- Close zoom: показываем columns с full detail
- Виртуализация: рендерим только visible viewport
- Прогрессивная загрузка: загружаем граф chunks по viewport
```

**Presentation mode:**
- Font size 1.5x
- Contrast ratio ≥7:1 (WCAG AAA)
- Скрытые toolbars (минимизированы)
- Larger hit targets для keyboard navigation
- Color palette оптимизирован для projector

**Split-screen с HEIMDALL:**
- Layout: LOOM 65% width, HEIMDALL 35% width
- Shared highlight state: когда ANVIL находит affected nodes, LOOM подсвечивает + HEIMDALL показывает event stream
- Coordinated reset button

**События в HEIMDALL:**
- `loom.view_loaded` — `{nodes_count, edges_count, render_time_ms}`
- `loom.node_selected` — `{node_id, node_type}`
- `loom.zoom_changed` — `{zoom_level, visible_nodes}`
- `loom.search_performed` — `{query, results_count}`

**Performance targets:**
- 1K узлов: <1 сек first paint
- 5K узлов: <3 сек time to interactive
- 10K узлов: <5 сек с виртуализацией
- Pan/zoom: 60 fps

### 2.7 KNOT

**Слой:** 5 (SEER Studio)
**Текущее состояние:** Production working — sessions, structure, queries, atoms, resolution rate
**Целевое:** Polish + cross-dialect support + export

**Роль в demo:** secondary к LOOM. Показывается коротко в Technical Foundation секции для credibility — «вот 9920 atoms, вот 78.5% resolution, вот session metadata». Главная демо-роль — drill-down с LOOM в моменте «расскажи подробнее».

### 2.8 HEIMDALL

**Слой:** 6 (Cross-cutting)
**Текущее состояние:** Не существует
**Целевое:** Полностью построен — event bus + metrics + dashboard + stream viewer

**Архитектура:**

```
HEIMDALL/
├── event_bus/
│   ├── emitter.py (thin client для всех компонентов)
│   ├── collector.py (receives events)
│   └── ring_buffer.py (in-memory, последние 10K events)
├── metrics_aggregator/
│   ├── counters.py (atoms, files, tool_calls)
│   ├── gauges.py (memory, workers, queue_depth)
│   ├── histograms.py (latencies)
│   └── resolution_tracker.py (для Correctness demo)
├── dashboard/
│   ├── server.py (WebSocket server)
│   ├── frontend/ (React UI, separate route в SEER Studio)
│   └── presentation_mode.py
├── stream_viewer/
│   └── event_stream.py (filtered real-time event log)
└── demo_controls/
    ├── recording.py
    ├── replay.py
    └── reset.py
```

**Канонический event schema (стабильный контракт от day 1):**

```typescript
interface HeimdallEvent {
  timestamp: number;          // unix ms
  source_component: string;   // "hound" | "mimir" | "anvil" | ...
  event_type: string;         // "parse_started" | "tool_call" | ...
  level: "info" | "warn" | "error";
  session_id?: string;
  user_id?: string;
  payload: Record<string, any>;
  duration_ms?: number;
  correlation_id?: string;    // tie related events together
}
```

**Зачем стабильный контракт:** см. ARCH-review § 6 — это решение ТРИЗ-противоречия «demo vs production observability». Schema проектируется один раз и **не меняется** при переходе к production observability. Меняются только collectors и UIs.

**Pipeline метрик:**

```
Component эмитит event
    ↓
HEIMDALL event_bus collector receives
    ↓
Event сохраняется в ring buffer (последние 10K)
    ↓
Metrics aggregator обновляет counters/gauges/histograms
    ↓
Dashboard server пушит к WebSocket clients:
    - Aggregated metrics (каждые 100 ms)
    - Individual events (если matched filter)
    ↓
Dashboard UI рендерит в real time
```

**Dashboard layout (presentation mode):**

```
┌─────────────────────────────────────────────┐
│  HEIMDALL — Demo Observability              │
├─────────────────────────────────────────────┤
│  [SCALE METRICS]              [LATENCY]     │
│  Atoms: 9,847                 Parse: 23ms   │
│  Files: 487                   Tool: 14ms    │
│  Workers: 4 active            LLM: 1.2s     │
│                                             │
│  [EVENT STREAM]                             │
│  [12:34:56] hound.file_parsing_started      │
│  [12:34:57] hound.atom_extracted (×100)     │
│  [12:34:58] mimir.tool_call_started         │
│  [12:34:58] anvil.traversal_progress        │
│  [12:34:58] mimir.tool_call_completed       │
│                                             │
│  [RESOLUTION]                               │
│  Resolved: 78.5% ████████░░                 │
│  Categories:                                │
│    unknown_function: 12%                    │
│    external_reference: 6.5%                 │
│    dynamic_sql: 3%                          │
└─────────────────────────────────────────────┘
```

**Технологические выборы:**
- In-process Python/Go module (не отдельный сервис — ADR-DA-001)
- Ring buffer in-memory (не persistent — demo mode)
- WebSocket server: FastAPI или Node.js (matching stack)
- Dashboard frontend: React, отдельный route `/heimdall` в SEER Studio

**Out of scope (deferred к post-HighLoad):**
- Persistent storage
- Prometheus exposition
- Grafana integration
- Distributed tracing (Jaeger)
- Alert manager
- Multi-tenancy
- Retention policies

### 2.9 FRIGG

**Слой:** 1 (Data Layer)
**Текущее состояние:** Не начато
**Целевое:** Saved LOOM views (cheap win, ~2-3 дня работы)

Подробная спецификация: `DISCOVERY_FRIGG_SCHEMA.md`.

**Schema:**
```
SavedView {
  id: string (uuid)
  user_id: string
  name: string
  created_at: timestamp
  view_state: {
    center_node_id: string
    zoom_level: float
    depth: int
    direction: "upstream" | "downstream" | "both"
    filters: {...}
  }
}
```

**Storage decision:** отдельный YGG instance vs JSON file — **open question**, закрыть до конца апреля.

**Use case в demo:** co-founder готовит несколько demo views заранее («overview», «scale moment», «intelligence query result», «correctness comparison») и переключается между ними одним кликом. Это **demo safety** — известные working states вместо ad-hoc navigation на сцене.

### 2.10 Chur

**Слой:** 2 (Platform)
**Текущее состояние:** Working для legacy
**Целевое:** Demo mode — single admin user, pre-generated токен

Post-HighLoad: миграция к Keycloak — deferred.

### 2.11 Dali Core

**Слой:** 2 (Platform)
**Текущее состояние:** Data models существуют, нужна проверка
**Целевое:** Verify schema consistency через всю цепочку Hound → ANVIL → MIMIR → LOOM

**Action item для апреля:** провести consistency audit data models. Это блокирующая работа для M1 milestone, поскольку без согласованных моделей parallel development в Track A (Hound, ANVIL) и Track B (MIMIR, LOOM) приведёт к integration кошмару в августе.

---

## 3. Архитектурные решения (ADR)

### ADR-DA-001 — HEIMDALL как in-process модуль

**Статус:** Принято
**Контекст:** HEIMDALL может быть отдельным сервисом или встроен в-процессе. Demo running на одном ноутбуке, нет network boundary.
**Решение:** **in-process модуль**.
**Альтернативы отклонены:** Отдельный HEIMDALL сервис — добавляет deployment complexity ради marginal demo benefit.
**Последствия:**
- Проще deployment (один процесс меньше)
- Ниже latency event collection
- Post-HighLoad: можно вынести в отдельный сервис, не меняя emitters

### ADR-DA-002 — MIMIR tool calling: custom framework, не LangChain

**Статус:** Принято
**Контекст:** Можно использовать LangChain или построить минимальный custom framework для tool calling.
**Решение:** **Custom minimal framework, ~300 строк Python.**
**Альтернативы отклонены:** LangChain — слишком тяжёлый, слишком много moving parts для live demo safety. Зависимость от framework upgrades — недопустимо за месяц до сцены.
**Последствия:**
- Полный контроль над поведением
- Нет dependency upgrades, ломающих demo
- Explicit event emission в HEIMDALL без обходных путей
- Меньше features из коробки (но нам не нужны)

### ADR-DA-003 — Event bus: in-process ring buffer, без persistence

**Статус:** Принято
**Контекст:** Event bus может быть Redis Streams, Kafka, in-memory queue или ring buffer. Demo нужен real-time, не история.
**Решение:** **in-process ring buffer на 10K events.**
**Альтернативы отклонены:** Redis Streams — overkill для single-process demo; Kafka — абсурд для local laptop.
**Последствия:**
- Покрывает всю длительность demo (40 минут × ~250 events/min = ~10K events)
- Нулевой operational overhead
- Post-HighLoad: можно заменить на persistent backend, не трогая emitters (благодаря стабильной schema — см. ARCH-review § 6)

### ADR-DA-004 — Graph rendering library для LOOM

**Статус:** **PENDING — закрыть до конца апреля**
**Контекст:** LOOM протестирован на 10 узлах, нужна работа на 5–10K. Разные библиотеки имеют разный performance ceiling.

**Кандидаты:**
- **Sigma.js** — proven at scale, WebGL backend, хорош для 10K+ nodes
- **Cytoscape.js** — feature-rich, canvas-based, хорошие layout алгоритмы
- **React Flow** — React-native, ниже performance ceiling
- **Custom WebGL** — максимум performance, максимум сложности

**Trigger для решения:** end of April, на основе текущего LOOM codebase + первых profiling measurements.

### ADR-DA-005 — ClickHouse dialect: stretch, droppable

**Статус:** Принято
**Контекст:** PL/SQL + PostgreSQL — минимум для demo. ClickHouse — nice to have, поскольку HighLoad audience содержит много ClickHouse users.
**Решение:** **Stretch goal — drop если slipping.**
**Trigger для drop:** конец июня, если <50% complete.
**Последствия:**
- Rock-solid 2 dialects лучше, чем wobbly 3
- Если получится — три-dialect demo стронгер
- Если нет — нет потери критического scope

### ADR-DA-006 — MIMIR fallback: 3-tier strategy

**Статус:** Принято
**Контекст:** Live demo не может упасть из-за network hiccup или Claude API outage.
**Решение:** **Three-tier fallback.**
- Tier 1: Claude API (primary)
- Tier 2: OpenAI GPT-4 (если Claude down)
- Tier 3: Cached responses для demo queries (если network down полностью)

**Все три tier эмитят идентичные HEIMDALL события — audience не отличает.**

**Последствия:**
- Demo safety обеспечена на network/API failure
- Дополнительная разработка: cache layer + два LLM adapters
- Explicit cost: разработка ~1 неделя

### ADR-DA-007 — Algorithms-as-tools (унаследован из v4 ADR-014)

**Статус:** Принято (наследуется)
**Контекст:** GraphRAG — ключевое архитектурное направление. Вопрос — как интегрировать с существующими алгоритмами ANVIL/Hound. Naive подход (vector RAG + stuff в prompt) плох — hallucinations, нет explainability. Правильный подход — tool-using AI.

**Решение:**
1. **MIMIR** — компонент в слое AI Orchestration
2. MIMIR **не владеет** бизнес-логикой lineage. Вся логика остаётся в ANVIL/Hound/Dali Core
3. **Каждый алгоритм должен быть tool-friendly:** возвращать структурированный JSON с metadata (что было queried, что найдено, confidence, references к graph nodes), а не только визуальное представление
4. **MIMIR функции:**
   - Intent parsing
   - Tool selection
   - Parameter extraction
   - Tool calling (LLM function calling API)
   - Response building
   - Conversation memory
   - Trace & explainability
5. **Explainability first:** каждый ответ MIMIR должен быть прослеживаемым до конкретных tool calls и subgraph nodes

**Альтернативы отклонены:**
- Vector RAG + stuff в prompt → hallucinations и плохая accuracy
- Fine-tuned LLM на lineage данных → дорого и данных мало
- Rule-based query parser без LLM → теряем NL interface

**Последствия:**
- Новый компонент в архитектуре (MIMIR)
- Новые требования к ANVIL API: tool-friendly с day 1
- Зависимость от external LLM provider (см. risk mitigation в ARCH-review)
- LLM costs становятся частью operational cost model
- Explainability как **competitive advantage** vs vector RAG tools — это ровно то, что HighLoad audience оценит

---

## 4. Performance budgets

Количественные таргеты для October demo. Это **targets, а не guarantees**. При промахе — документировать и работать. Приоритеты при конфликте: correctness > LLM latency (audience perception) > graph render > event throughput (degradable).

### 4.1 Hound

| Метрика | Target |
|---|---|
| Single file 50K LoC | <5 сек |
| Batch 500K LoC (10 × 50K) | <30 сек (4 workers) |
| Memory peak | <2 GB RSS |
| Throughput | >10K atoms/sec single file |
| Resolution rate | ≥85% demo dataset |

### 4.2 YGG + ANVIL (graph operations)

| Метрика | Target |
|---|---|
| Single node lookup | <10 ms p95 |
| 5-hop traversal | <100 ms typical |
| 10-hop worst case | <500 ms |
| Full graph load для LOOM | <2 сек |

### 4.3 MIMIR (LLM operations)

| Метрика | Target |
|---|---|
| LLM latency | <3 сек p95 (Claude Sonnet) |
| Tool call overhead | <50 ms (MIMIR internal) |
| End-to-end query | <5 сек (question → answer) |
| Cached fallback | <1 сек full pipeline simulation |

### 4.4 LOOM (UI rendering)

| Метрика | Target |
|---|---|
| Initial render 1K узлов | <1 сек first paint |
| Initial render 5K узлов | <3 сек time to interactive |
| Initial render 10K узлов | <5 сек с виртуализацией |
| Pan/zoom responsiveness | 60 fps |
| Search highlight | <100 ms после query |

### 4.5 HEIMDALL (observability overhead)

| Метрика | Target |
|---|---|
| Event emission overhead | <1% CPU на источнике |
| Dashboard update rate | 10 Hz aggregated metrics |
| Event stream throughput | 1000 events/sec без drops |
| UI render rate | 60 fps presentation mode |

---

## 5. Component interfaces matrix

Quick reference: who talks to whom.

| From ↓ / To → | Hound | YGG | Dali | ANVIL | MIMIR | SHUTTLE | LOOM | KNOT | HEIMDALL |
|---|---|---|---|---|---|---|---|---|---|
| **Hound** | — | W | R | — | — | — | — | — | E |
| **YGG** | — | — | — | — | — | — | — | — | — |
| **Dali** | — | W | — | — | — | — | — | — | E |
| **ANVIL** | — | R | — | — | — | — | — | — | E |
| **MIMIR** | — | — | — | C | — | — | — | — | E |
| **SHUTTLE** | C | R | C | — | C | — | — | — | E |
| **LOOM** | — | — | — | — | — | C | — | — | E |
| **KNOT** | — | — | — | — | — | C | — | — | E |
| **HEIMDALL** | — | — | — | — | — | — | — | — | — |

**Legend:**
- **W** = Writes
- **R** = Reads
- **C** = Calls (RPC/API)
- **E** = Emits events

**Ключевые наблюдения:**
- YGG — pure storage, не инициирует вызовы
- HEIMDALL — pure receiver, не эмитит (meta-design: observability сама не наблюдается)
- SHUTTLE — hub для UI → backend
- MIMIR в October scope вызывает только ANVIL (через tool calling, не напрямую)
- Все компоненты эмитят события в HEIMDALL — это и есть cross-cutting

---

## 6. Data flows — ключевые сценарии

### 6.1 Сценарий: Live parsing demo (Scale story)

Это первый live момент демо: пользователь нажимает «Load dataset», и аудитория видит, как 500K LoC парсится в реальном времени со всеми метриками на split-screen.

(см. sequence-диаграмму ниже)

### 6.2 Сценарий: Intelligence query (Intelligence demo, money shot)

Это **главный момент демо**: пользователь задаёт вопрос на естественном языке, MIMIR делает tool calls к ANVIL, audience видит весь pipeline transparently.

**Money shot фраза для talk:**
> «Это не vector RAG. Это не retrieval. Это **algorithms as tools**. LLM не догадывается — он вызывает real graph traversal. Каждый шаг visible. Каждый результат verifiable. Это anti-hallucination by architecture.»

(см. sequence-диаграмму ниже)

### 6.3 Сценарий: Correctness comparison (Correctness story)

Пользователь загружает «nightmare SQL» с edge cases. AIDA парсит правильно. Demo runner запускает sqlglot и sqllineage на том же файле — они падают или дают неправильный результат. Side-by-side display.

```
User clicks: "Load nightmare SQL — CTE nested with window functions"
    ↓
Hound parses → emits markers:
    hound.dynamic_sql_detected {file, location}
    hound.nested_cte_resolved {depth: 4, columns: 12}
    hound.window_function_parsed {partition_by: 3, order_by: 2}
    HEIMDALL event stream показывает parser decisions в real time
    ↓
AIDA correctly extracts column-level lineage
    ↓
Demo runner scripts:
    sqlglot parse same file → crash или wrong output
    sqllineage parse same file → silent wrong
    HEIMDALL shows: sqlglot_result = "CRASH", sqllineage_result = "WRONG"
    ↓
Side-by-side display:
    - sqlglot: ✗ crashed
    - sqllineage: ✗ got X, correct is Y
    - AIDA: ✓ correct
    ↓
HEIMDALL resolution panel shows:
    Resolved: 78.5%
    Unresolved categories с counts
    ↓
Speaker: «Вот где мы знаем что работает, и вот где мы знаем что не работает. Честно.»
```

---

## 7. Deployment topology

### 7.1 October demo deployment

**Target environment:**
- Single ноутбук (co-founder)
- macOS или Linux
- 16 GB+ RAM, 8+ cores
- External monitor для split-screen (LOOM + HEIMDALL)

**Process topology:**

```
┌─────────────────────────────────────────┐
│  Single laptop — demo environment       │
│                                         │
│  Process 1: ArcadeDB (YGG)              │
│  - localhost:2480                       │
│  - In-memory mode                       │
│  - Pre-loaded demo dataset              │
│                                         │
│  Process 2: Hound worker                │
│  - Receives parse jobs via queue        │
│  - Writes к YGG                         │
│  - Эмитит events к HEIMDALL             │
│                                         │
│  Process 3: SEER Studio backend         │
│  - FastAPI / Node.js                    │
│  - SHUTTLE GraphQL endpoint             │
│  - MIMIR tool calling                   │
│  - HEIMDALL event collector             │
│  - WebSocket server                     │
│  - localhost:8000                       │
│                                         │
│  Process 4: SEER Studio frontend        │
│  - Dev server или pre-built static      │
│  - LOOM, KNOT, MIMIR chat, HEIMDALL UI  │
│  - localhost:3000                       │
│                                         │
│  External: Claude API (HTTPS)           │
│  - MIMIR LLM calls                      │
│  - Fallback: OpenAI, потом local Qwen   │
└─────────────────────────────────────────┘
```

**Startup sequence:**
1. `make demo-start` (single command)
2. Скрипт запускает ArcadeDB
3. Ждёт пока YGG healthy
4. Запускает backend (SHUTTLE + MIMIR + HEIMDALL collector)
5. Запускает frontend dev server
6. Открывает браузер на localhost:3000
7. Готов за ~15 секунд

**Reset между rehearsals:**
- `make demo-reset` — очищает session state, восстанавливает baseline YGG snapshot
- ~5 секунд

**Backup strategy:**
- Primary laptop — main demo
- Backup laptop — identical setup, синхронизация раз в неделю
- Pre-recorded demo video — ultimate fallback

### 7.2 Post-conference deployment (November+)

**Target:** Docker Compose для первого pilot customer.

```yaml
# docker-compose.yml (sketch)
services:
  arcadedb:
    image: arcadedb/arcadedb:latest
    volumes:
      - ./data:/arcadedb/databases

  seer-backend:
    build: ./backend
    depends_on: [arcadedb]
    environment:
      - CLAUDE_API_KEY=${CLAUDE_API_KEY}

  seer-frontend:
    build: ./frontend
    depends_on: [seer-backend]
```

**Изменения от demo topology:**
- Persistent ArcadeDB volume
- Real auth (не single admin)
- External config для customer credentials
- Simpler HEIMDALL (production metrics, не demo theater)

---

## 8. Parallel development strategy

С учётом 2 co-founder + variable velocity до июля, parallel работа критична.

### 8.1 Track A — Data & Algorithms (Co-founder 1)

**Ответственность:** Hound, YGG, ANVIL, Dali Core, benchmarks

**Apr–Jun focus:**
- PostgreSQL dialect в Hound
- Performance baseline measurements
- YGG schema consistency
- ANVIL basic implementation

**Jul–Sep focus:**
- Hound polish, edge cases
- ANVIL integration testing
- Performance optimization
- Benchmark runner для Correctness demo

### 8.2 Track B — UI & Orchestration (Co-founder 2)

**Ответственность:** LOOM, KNOT, MIMIR, HEIMDALL, SHUTTLE, integration

**Apr–Jun focus:**
- LOOM virtualization
- HEIMDALL event bus + dashboard
- MIMIR tool calling framework
- SHUTTLE WebSocket для events

**Jul–Sep focus:**
- MIMIR integration с ANVIL
- Split-screen LOOM + HEIMDALL layout
- Demo rehearsal
- Polish, bug fixes

### 8.3 Shared

Both co-founder касаются:
- Integration testing
- Demo rehearsal
- Performance profiling
- Blog posts (alternating)
- Demo script

**Synchronization:**
- Weekly 30-min sync
- Shared task tracker (GitHub Issues или Notion)
- Weekly integration merge на main

> **Открытый вопрос (см. ARCH-review § 5.4):** кто из двух co-founder ведёт Track A, кто Track B — пока не зафиксировано на уровне людей. Решить за неделю.

---

## 9. Open questions для April resolution

Items требующие decision до начала heavy engineering:

1. **LOOM rendering library** (ADR-DA-004): Sigma.js vs Cytoscape vs custom WebGL?
2. **MIMIR LLM provider:** Claude primary подтверждён, но OpenAI vs local Qwen для tier 2 fallback?
3. **FRIGG storage:** отдельный YGG instance vs JSON file для demo simplicity?
4. **Dialect scope:** ClickHouse — держим как stretch или drop now?
5. **Event bus transport:** in-process pub/sub vs local Redis pub/sub?
6. **HighLoad CFP deadline:** даты, speaker slot, booth availability?
7. **Co-founder track assignment:** кто Track A, кто Track B?
8. **Benchmark dataset:** что именно показываем как 500K LoC?

**Дедлайн:** конец апреля. Decision документировать как новые ADR-DA-008..015.

---

## 10. Что НЕ в этой архитектуре (явный defer list)

Чтобы избежать scope creep — explicit список того, что **намеренно** не делается:

**Deferred к post-HighLoad:**
- Multi-tenancy (FRIGG user isolation beyond demo)
- SSO / Keycloak integration
- Billing integration
- Full HEIMDALL (Prometheus, Grafana, Loki, Jaeger)
- URD (history, time-travel)
- SKULD (validation, proposals)
- MUNINN (persistent metrics)
- HUGINN (отдельный metrics collector)
- BIFROST (production infrastructure stack)
- Multi-graph support в YGG
- dbt/Airflow/Snowflake native integrations
- Cloud multi-tenant deployment
- Open source release strategy

**Никогда (out of AIDA scope):**
- Full-text semantic search (vector RAG — отклонено в ADR-DA-007)
- Generic SQL query builder
- Database administration features
- Generic data catalog features

---

## 11. Summary

Это техническая спецификация AIDA архитектуры для October 2026 HighLoad demo. Шесть слоёв с HEIMDALL как cross-cutting observability layer. Одиннадцать компонентов с explicit interfaces, event schemas, performance budgets.

**Ключевые принципы:**
1. **Algorithms-as-tools, не vector RAG** (ADR-DA-007)
2. **Observability first** — всё видимо через HEIMDALL события
3. **Deterministic where it matters** — ANVIL pure algorithm, MIMIR только для NL
4. **Scope discipline** — explicit defer list блокирует feature creep
5. **Parallel development possible** — два трека с чёткими интерфейсами
6. **Demo safety built-in** — fallback'и, кэши, recovery на каждом уровне

**Что это даёт:**
- Co-founder знают точно за что отвечают
- Daily engineering decisions имеют reference document
- Weekly planning (TARGET_ARCH) использует это как detail lookup
- New contributor может onboard от этого документа
- Post-HighLoad pivot к production — clear starting point

**Что это не делает:**
- Не заменяет TARGET_ARCH (там priorities и roadmap)
- Не диктует implementation details (классы, функции)
- Не предсказывает всё (8 open questions остаются)
- Не покрывает post-HighLoad production architecture

---

*Этот документ — детальный companion к ARCH-review для October 2026 focus. Обновляется при принятии major architectural decisions. После HighLoad — пересматривается для post-conference production architecture.*
