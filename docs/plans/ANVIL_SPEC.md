# ANVIL — Service Specification

**Документ:** `ANVIL_SPEC`
**Версия:** 1.0
**Дата:** 16.04.2026
**Статус:** DRAFT — не реализован
**Зависимость:** Q26 (прямой вызов vs через MIMIR), Q30 (ArcadeDB 72 алгоритмов)

---

## 1. Назначение

**ANVIL** — сервис анализа влияния (impact analysis) изменений.

**Ключевой вопрос, который решает ANVIL:**
> «Если я изменю колонку `orders.status` — какие таблицы, процедуры и операторы
> будут затронуты? Какой путь от источника до конечного потребителя?»

**ANVIL** — это граф-traversal engine поверх YGG.

---

## 2. Место в архитектуре

```
VERDANDI ("Find Impact" кнопка)
    │
    ▼
SHUTTLE (GraphQL query: findImpact) ──► ANVIL ──► YGG (Cypher traversal)
                                           │
                                      MIMIR tool:
                                      find_impact()
                                           │
                                      HEIMDALL (events)
```

---

## 3. Core Use Cases

### UC-A1: Downstream impact (прямой)

**Вопрос:** «Что читает или зависит от колонки `HR.ORDERS.STATUS`?»

**Traversal:**
```
START: DaliColumn {qualifiedName: 'HR.ORDERS.STATUS'}
TRAVERSE edges:
  ← ATOM_REF_COLUMN
  → из DaliAtom: следовать по DATA_FLOW, FILTER_FLOW, JOIN_FLOW
  → до DaliOutputColumn / DaliStatement / DaliRoutine
MAX depth: 10 hops
```

**Ответ:** Список `{nodeId, nodeType, label, depth, path}` — все зависимые объекты.

### UC-A2: Upstream lineage (обратный)

**Вопрос:** «Откуда берётся значение в колонке `HR.FACT_SALES.TOTAL_AMOUNT`?»

Обратный traversal по DATA_FLOW → к исходным DaliColumn источникам.

### UC-A3: Routine impact

**Вопрос:** «Что сломается, если я удалю процедуру `HR.PKG_ORDERS.PROC_CANCEL_ORDER`?»

```
START: DaliRoutine {qualifiedName: 'HR.PKG_ORDERS.PROC_CANCEL_ORDER'}
TRAVERSE: ← CALLS (кто вызывает эту рутину)
          → ROUTINE_USES_TABLE (какие таблицы затронуты)
```

### UC-A4: Table impact

**Вопрос:** «Сколько процедур читают из таблицы `HR.ORDERS`?»

```
START: DaliTable {qualifiedName: 'HR.ORDERS'}
TRAVERSE: ← READS_FROM, ← WRITES_TO, ← ROUTINE_USES_TABLE
```

---

## 4. API

### `POST /api/impact`

**Запрос:**
```json
{
  "nodeId": "HR.ORDERS.STATUS",
  "nodeType": "column",
  "direction": "downstream",
  "maxHops": 5,
  "includeTypes": ["DaliRoutine", "DaliStatement", "DaliTable"],
  "sessionId": "optional — фильтр по сессии парсинга"
}
```

**Ответ:**
```json
{
  "rootNode": {"id": "HR.ORDERS.STATUS", "type": "DaliColumn"},
  "totalAffected": 12,
  "nodes": [
    {"id": "#12:34", "type": "DaliRoutine", "label": "HR.PKG_ORDERS.PROC_CREATE_ORDER", "depth": 2},
    {"id": "#12:35", "type": "DaliStatement", "label": "SELECT ... FROM ORDERS WHERE STATUS=...", "depth": 1}
  ],
  "edges": [
    {"from": "#12:34", "to": "#12:35", "type": "CONTAINS_STMT"}
  ],
  "executionMs": 45
}
```

### `POST /api/impact/batch`

Для нескольких node_id одновременно — используется при rename refactoring analysis.

### `GET /api/impact/health`

```json
{"ygg": "ok", "algorithms": "cypher"}
```

---

## 5. Граф-алгоритм

### Вариант A: Cypher traversal (текущий план, Q30 не решён)

```cypher
MATCH path = (start:DaliColumn {qualifiedName: $qname})
             -[:ATOM_REF_COLUMN|DATA_FLOW|FILTER_FLOW|JOIN_FLOW*1..{maxHops}]-
             (end)
WHERE labels(end) IN $includeTypes
RETURN end, length(path) as depth, path
ORDER BY depth ASC
LIMIT 500
```

ArcadeDB поддерживает OpenCypher — запрос через `/api/v1/query/{db}` с `language=cypher`.

### Вариант B: ArcadeDB 72 встроенных алгоритмов (Q30 — re-eval май 2026)

ArcadeDB 26.x включает встроенные граф-алгоритмы:
- `shortestPath()`
- `dijkstra()`
- `betweenness centrality`
- и другие через `/api/v1/algorithm/{db}/{algorithm}`

**Q30:** Может ли один из 72 алгоритмов заменить кастомный Cypher traversal для impact analysis?

До решения Q30 — реализовывать Cypher traversal (проще, уже работает в Hound).

---

## 6. Интеграция с SHUTTLE

После реализации ANVIL добавить в SHUTTLE GraphQL schema:

```graphql
type ImpactNode {
  id: String!
  type: String!
  label: String!
  depth: Int!
}

type ImpactEdge {
  from: String!
  to: String!
  type: String!
}

type ImpactResult {
  rootNode: ImpactNode!
  totalAffected: Int!
  nodes: [ImpactNode!]!
  edges: [ImpactEdge!]!
  executionMs: Int!
}

type Query {
  findImpact(nodeId: String!, direction: String, maxHops: Int): ImpactResult
}
```

SHUTTLE → ANVIL: REST client `AnvilClient` (аналогично `DaliClient`).

---

## 7. Технический стек

| Компонент | Технология |
|-----------|-----------|
| Runtime | Quarkus 3.x + Java 21 |
| YGG client | ArcadeDB REST API (Cypher) |
| Port | 9095 |
| Build | Gradle, модуль `services/anvil` |

---

## 8. Открытые вопросы

| Q | Вопрос | Deadline |
|---|--------|---------|
| Q26 | SHUTTLE вызывает ANVIL напрямую или через MIMIR как tool? | До начала ANVIL |
| Q30 | ArcadeDB 72 алгоритмов — заменить кастомный traversal? | Май 2026 |
| Q-A1 | Результат ANVIL кешировать? (Invalidate при новой parse session) | Архитектура |
| Q-A2 | MAX LIMIT: 500 нод в ответе достаточно? Нужна пагинация? | API design |

---

## 9. Roadmap

| Milestone | Задача | Оценка |
|-----------|--------|--------|
| M3 prep | Q26 решён + Q30 решён | 1 день |
| M3 | ANVIL skeleton + `/api/impact` endpoint | 2 дня |
| M3 | Cypher traversal для UC-A1 downstream | 2 дня |
| M3 | UC-A2 upstream + UC-A3 routine impact | 2 дня |
| M3 | SHUTTLE `AnvilClient` + `findImpact` GraphQL | 2 дня |
| M3 | VERDANDI "Find Impact" кнопка в L3/L4 | 2 дня |

---

## История изменений

| Дата | Версия | Что |
|------|--------|-----|
| 16.04.2026 | 1.0 | DRAFT. 4 UC. REST API контракт. Cypher traversal. Q26/Q30. |
