# MIMIR — Service Specification

**Документ:** `MIMIR_SPEC`
**Версия:** 1.0
**Дата:** 16.04.2026
**Статус:** DRAFT — не реализован (C.2.3 pending)
**Зависимость:** Q29 (ArcadeDB MCP Server re-eval май 2026)

---

## 1. Назначение

**MIMIR** — LLM tool-calling оркестратор. Отвечает за:
- Приём natural language вопросов от пользователя
- Выбор и вызов инструментов (YGG, ANVIL, SHUTTLE)
- Формирование ответа на естественном языке

**Девиз:** «Спроси граф на человеческом языке»

---

## 2. Место в архитектуре

```
VERDANDI (Chat UI)
    │  HTTP / GraphQL
    ▼
SHUTTLE (GraphQL mutation: askMimir)
    │  HTTP POST
    ▼
MIMIR  ──── Tool: find_impact ──────► ANVIL
       ──── Tool: search_nodes ─────► SHUTTLE GraphQL
       ──── Tool: get_source ────────► YGG (ArcadeDB)
       ──── Tool: count_deps ─────────► YGG (ArcadeDB)
    │
    ├── Tier 1: Anthropic Claude Sonnet (HTTPS)
    ├── Tier 2: Ollama + Qwen 2.5 (local HTTP)
    └── Tier 3: JSON file cache (safety net)
```

---

## 3. LLM Tiers (приоритет исполнения)

| Tier | Провайдер | URL | Условие использования |
|------|-----------|-----|----------------------|
| 1 | Anthropic Claude Sonnet 3.5 | `https://api.anthropic.com` | `ANTHROPIC_API_KEY` задан |
| 2 | Ollama + Qwen 2.5 14B | `http://localhost:11434` | Tier 1 недоступен / privacy mode |
| 3 | JSON cache | `./cache/mimir_responses.json` | Tier 1+2 недоступны |

Конфигурация через env:
```env
MIMIR_LLM_TIER=auto          # auto | anthropic | ollama | cache
ANTHROPIC_API_KEY=sk-ant-...
OLLAMA_URL=http://localhost:11434
OLLAMA_MODEL=qwen2.5:14b
```

---

## 4. Инструменты LLM (tool_calling)

### Tool 1: `find_impact`

```json
{
  "name": "find_impact",
  "description": "Find all objects (tables, routines, statements) downstream from a given column or table",
  "parameters": {
    "node_id": "string",
    "direction": "downstream | upstream",
    "max_hops": "integer (default: 5)"
  }
}
```

Реализация: HTTP POST → ANVIL backend `/api/impact`.

### Tool 2: `search_nodes`

```json
{
  "name": "search_nodes",
  "description": "Search YGG for tables, columns, or routines by name",
  "parameters": {
    "name": "string (supports wildcard %)",
    "node_type": "table | column | routine | all"
  }
}
```

Реализация: SHUTTLE GraphQL → `searchNodes(name, type)`.

### Tool 3: `get_procedure_source`

```json
{
  "name": "get_procedure_source",
  "description": "Get the source SQL of a routine from YGG",
  "parameters": {
    "qualified_name": "string (e.g. HR.PKG_ORDERS.PROC_CREATE_ORDER)"
  }
}
```

Реализация: YGG Cypher → `MATCH (r:DaliRoutine {qualifiedName: $qname}) RETURN r.bodySource`.

### Tool 4: `count_dependencies`

```json
{
  "name": "count_dependencies",
  "description": "Count how many routines read from or write to a given table",
  "parameters": {
    "table_name": "string",
    "schema": "string"
  }
}
```

Реализация: YGG SQL → `SELECT count(*) FROM (TRAVERSE in('READS_FROM') FROM ...)`.

### Tool 5 (TBD)

Кандидаты:
- `explain_statement(stmt_id)` — объяснить конкретный DaliStatement
- `find_orphan_tables()` — таблицы без references
- `compare_sessions(session1, session2)` — diff между двумя парсинг-сессиями

---

## 5. API

### Endpoint: `POST /api/ask`

Запрос:
```json
{
  "question": "Какие процедуры читают из таблицы ORDERS?",
  "context": {
    "schema": "HR",
    "session_id": "optional"
  },
  "max_tool_calls": 5
}
```

Ответ:
```json
{
  "answer": "Таблицу ORDERS читают 7 процедур: PKG_ORDERS.GET_ORDER, ...",
  "tool_calls_used": ["search_nodes", "count_dependencies"],
  "confidence": "high | medium | low",
  "sources": [{"node_id": "...", "type": "DaliRoutine"}]
}
```

---

## 6. Интеграция с SHUTTLE

`MutationResource.askMimir()` — сейчас stub (`GraphQLException`).

После реализации MIMIR:
```java
// services/shuttle/src/main/java/studio/seer/lineage/resource/MutationResource.java
@Mutation("askMimir")
public Uni<String> askMimir(@Name("question") String question) {
    return Uni.createFrom().item(() -> mimirClient.ask(question, null))
        .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
        .onFailure().recoverWithItem(ex -> "MIMIR unavailable: " + ex.getMessage());
}
```

---

## 7. Технический стек

| Компонент | Технология | Причина |
|-----------|-----------|---------|
| Runtime | Quarkus 3.x + Java 21 | Единообразие с DALI/SHUTTLE |
| LLM SDK | Anthropic Java SDK (или HTTP client) | tool_calling support |
| Ollama клиент | REST HTTP POST /api/generate | Fallback |
| Port | 9094 | Следующий за HEIMDALL |

---

## 8. Открытые вопросы

| Q | Вопрос | Deadline |
|---|--------|---------|
| Q29 | MIMIR через ArcadeDB MCP Server? (встроенный MCP в ArcadeDB 26.x+) | Май 2026 |
| Q-M1 | Какой язык для MIMIR: Java (Quarkus) или Python (LangChain)? | Q29 зависит |
| Q-M2 | Stateful conversation (история чата) или stateless per-request? | До начала реализации |
| Q-M3 | Контроль доступа: MIMIR видит весь YGG или только схему пользователя? | C.3 scopes |

---

## 9. Зависимости

| Зависимость | Статус |
|-------------|--------|
| ANVIL service | DRAFT spec (не реализован) |
| SHUTTLE `askMimir` mutation | ✅ stub есть (GraphQLException) |
| Anthropic API ключ | Нужен |
| C.2.3 MIMIR spec | Этот документ |
| Q29 ArcadeDB MCP | Re-eval май 2026 |

---

## 10. Roadmap

| Milestone | Задача | Оценка |
|-----------|--------|--------|
| M3 prep | Q29 решён + выбор языка | 1 день |
| M3 | MIMIR skeleton + Tier 1 (Anthropic) | 3 дня |
| M3 | Tool 1: `find_impact` | 2 дня |
| M3 | Tool 2: `search_nodes` | 1 день |
| M3 | VERDANDI Chat UI (базовый) | 3 дня |
| M3 | Tier 2 (Ollama) fallback | 2 дня |

---

## История изменений

| Дата | Версия | Что |
|------|--------|-----|
| 16.04.2026 | 1.0 | DRAFT. 5 инструментов. 3 LLM tiers. API контракт. Открытые вопросы Q29/Q-M1/Q-M2/Q-M3. |
