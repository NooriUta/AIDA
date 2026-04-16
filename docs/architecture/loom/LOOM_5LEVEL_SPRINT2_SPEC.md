# LOOM 5-Level — Sprint 2 Execution Spec

**Документ:** `LOOM_5LEVEL_SPRINT2_SPEC`
**Версия:** 1.0
**Дата:** 16.04.2026
**Статус:** 📋 SPEC — реализация в `feature/loom-5level-s2-apr2026`
**Опирается на:** `LOOM_5LEVEL_ARCHITECTURE.md` v1.2, `PLAN_LOOM_5LEVEL_S2_APR2026.md`

> Этот документ — **execution spec**: что именно делает каждый компонент, контракты,
> структуры данных, граничные случаи. Plan-файл (`PLAN_LOOM_5LEVEL_S2_APR2026.md`)
> описывает "что" и "когда", этот документ описывает "как".

---

## 1. `exploreRoutineAggregate` — backend resolver

### 1.1 Назначение

Возвращает агрегированный граф для **нового L2**: вместо отдельных `DaliStatement`-узлов
клиент получает `DaliRoutine` → `DaliTable` рёбра с весом `stmt_count`.

Ответ на вопрос: *«Какие рутины в этом пакете/схеме читают/пишут какие таблицы, и как интенсивно?»*

### 1.2 GraphQL contract

```graphql
type Query {
  """
  Aggregated routine→table edges for L2 view.
  scope: schema_geoid OR package_geoid (e.g. "HR" or "HR.PKG_ORDERS")
  """
  exploreRoutineAggregate(scope: String!): ExploreResult!
}
```

Возвращает существующий тип `ExploreResult` — чтобы frontend pipeline `transformGqlExplore`
не нуждался в изменении.

### 1.3 Cypher запрос

```cypher
-- READS_FROM aggregation
MATCH (r:DaliRoutine)-[:CONTAINS_STMT]->(s:DaliStatement)-[:READS_FROM]->(t:DaliTable)
WHERE r.schema_geoid = $scope OR r.package_geoid = $scope
RETURN r.routine_geoid AS src,
       t.table_geoid   AS tgt,
       'READS_FROM'    AS type,
       count(s)        AS stmt_count

UNION ALL

-- WRITES_TO aggregation
MATCH (r:DaliRoutine)-[:CONTAINS_STMT]->(s:DaliStatement)-[:WRITES_TO]->(t:DaliTable)
WHERE r.schema_geoid = $scope OR r.package_geoid = $scope
RETURN r.routine_geoid AS src,
       t.table_geoid   AS tgt,
       'WRITES_TO'     AS type,
       count(s)        AS stmt_count
```

**ArcadeDB SQL вариант** (если Cypher не доступен):

```sql
SELECT
  r.routine_geoid AS src,
  t.table_geoid   AS tgt,
  'READS_FROM'    AS type,
  count(*)        AS stmt_count
FROM DaliRoutine r
JOIN DaliStatement s ON (s IN r.out('CONTAINS_STMT'))
JOIN DaliTable t ON (t IN s.out('READS_FROM'))
WHERE r.schema_geoid = :scope OR r.package_geoid = :scope
GROUP BY r.routine_geoid, t.table_geoid
```

### 1.4 ExploreResult mapping

```java
// ExploreService.java — метод exploreRoutineAggregate(String scope)
ExploreResult result = new ExploreResult();

// Nodes: DaliRoutine + DaliTable (no DaliStatement!)
// Edges: aggregated READS_FROM / WRITES_TO с meta.stmtCount

// Edge meta extension:
public record EdgeMeta(String type, Integer stmtCount) {}
// stmtCount передаётся в поле edge.meta.stmt_count
// Frontend: RoutineNode показывает weight на ребре (badge)
```

### 1.5 Граничные случаи

| Случай | Поведение |
|---|---|
| Рутина без CONTAINS_STMT | Включается как изолированный узел (без рёбер) |
| Таблица только из внешней схемы | Включается как `DaliTable` с `external=true` |
| scope = package_geoid с вложенными пакетами | Рекурсивно не раскрывается — только прямые рутины пакета |
| UNION ALL с одинаковым (routine, table, READS_FROM) | Суммируется → один агрегированный edge |

### 1.6 Файлы реализации

| Файл | Изменение |
|---|---|
| `services/shuttle/.../resource/LineageResource.java` | Новый `@Query("exploreRoutineAggregate")` |
| `services/shuttle/.../service/ExploreService.java` | `exploreRoutineAggregate(String scope)` |
| `services/shuttle/.../model/ExploreEdge.java` | Добавить поле `stmtCount` (nullable) |

---

## 2. L4 — Statement drill-down с DaliOutputColumn как first-class node

### 2.1 Назначение

Ответ на вопрос: *«Откуда берётся каждый output column этого сложного SELECT — через какие subquery?»*

На L3 `DaliOutputColumn` — handle внутри карточки `StatementNode`.
На L4 `DaliOutputColumn` — **самостоятельный узел** с входящими/исходящими рёбрами.

### 2.2 L4 граф — структура

```
DaliStatement (root)
  │
  ├─[CHILD_OF {kind: "CTE"}]─► DaliStatement (CTE_1)
  │     └─[HAS_OUTPUT_COL]─► DaliOutputColumn("total_amount")
  │           └─[DATA_FLOW]─► DaliOutputColumn("total") из основного SELECT
  │
  ├─[CHILD_OF {kind: "SQ"}]─► DaliStatement (SQ_subquery)
  │     └─[HAS_OUTPUT_COL]─► DaliOutputColumn("dept_id")
  │           └─[DATA_FLOW]─► DaliOutputColumn("dept_id") из основного SELECT
  │
  └─[READS_FROM]─► DaliTable("departments")
                        └─[HAS_COLUMN]─► DaliColumn("dept_budget")
```

### 2.3 DaliOutputColumn как first-class node на L4

**На L3** (существующее поведение):
```
StatementNode [card]
  ├── out: col "total_amount" [handle]
  └── out: col "dept_id"     [handle]
```

**На L4** (новое):
```
OutputColumnNode [card] id=oc:total_amount
  properties: { name, expr, sourceType, resolvedGeoid }
  handles: in (source columns) + out (consumer columns)

OutputColumnNode [card] id=oc:dept_id
  properties: { name, expr }
```

Ребро `DATA_FLOW` на L4 всегда имеет handle-to-handle — то есть соединяет конкретные
`OutputColumnNode`, а не `StatementNode`-header.

### 2.4 `exploreStatementTree` — backend

```graphql
type Query {
  """
  Full subquery tree + output column flow for a single DaliStatement.
  stmtGeoid: geoid of the root DaliStatement (e.g. "HR.PROC_INIT.3")
  """
  exploreStatementTree(stmtGeoid: String!): StatementTreeResult!
}

type StatementTreeResult {
  root:     StatementNode!
  children: [StatementChild!]!   # CTE / SQ / INLINE_VIEW children
  columns:  [OutputColumnNode!]! # ALL output columns (root + children)
  sources:  [DaliTable!]!        # direct READS_FROM tables
  flows:    [ColumnFlow!]!       # DATA_FLOW edges between OutputColumns
}

type StatementChild {
  stmt: StatementNode!
  kind: String!    # "CTE" | "SQ" | "INLINE_VIEW"
  alias: String
}

type OutputColumnNode {
  id:            String!
  stmtGeoid:     String!
  name:          String!
  expr:          String
  sourceType:    String   # "column_ref" | "literal" | "expression" | "pending"
  resolvedGeoid: String   # null если не resolved
}

type ColumnFlow {
  srcId:  String!    # OutputColumnNode.id
  tgtId:  String!    # OutputColumnNode.id
  weight: Int        # 1 для прямой ссылки, 2+ для fan-in
}
```

### 2.5 ArcadeDB query — Statement tree

```sql
-- Шаг 1: получить корень
SELECT @rid, geoid, stmt_text, stmt_type
FROM DaliStatement WHERE geoid = :stmtGeoid

-- Шаг 2: получить детей (CTE, SQ, INLINE_VIEW)
SELECT child.@rid, child.geoid, e.kind, e.alias
FROM (TRAVERSE out('CHILD_OF') FROM (SELECT FROM DaliStatement WHERE geoid = :stmtGeoid))
WHERE @class = 'DaliStatement' AND @rid != :rootRid

-- Шаг 3: все OutputColumns корня и детей
SELECT oc.@rid AS id, oc.name, oc.expr, oc.source_type, oc.resolved_geoid,
       oc.in('HAS_OUTPUT_COL')[0].geoid AS stmtGeoid
FROM DaliOutputColumn oc
WHERE oc.in('HAS_OUTPUT_COL')[0].geoid IN :allStmtGeoids

-- Шаг 4: DATA_FLOW рёбра между собранными OutputColumns
SELECT out.@rid AS srcId, in.@rid AS tgtId
FROM DATA_FLOW
WHERE out IN :outputColumnRids AND in IN :outputColumnRids
```

### 2.6 Frontend: OutputColumnNode

Новый компонент `OutputColumnNode.tsx` (или вынести из StatementNode):

```tsx
// frontends/verdandi/src/components/canvas/nodes/OutputColumnNode.tsx

interface OutputColumnNodeData {
  id:            string;
  name:          string;
  expr?:         string;
  sourceType:    'column_ref' | 'literal' | 'expression' | 'pending';
  resolvedGeoid?: string;
}

// Рендер: маленькая карточка, цвет по sourceType:
//   column_ref  → синий   (resolved reference)
//   literal     → серый   (constant)
//   expression  → жёлтый  (computed)
//   pending     → красный (unresolved)
// Handle IN  = левая сторона (источники)
// Handle OUT = правая сторона (потребители)
```

### 2.7 Переход L3 → L4

Триггер: клик на `StatementNode` header (существующий узел на L3).

```typescript
// frontends/verdandi/src/stores/slices/navigationSlice.ts
case 'drillToL4':
  state.viewLevel = 'L4';
  state.selectedStmtGeoid = action.payload.stmtGeoid;
  state.breadcrumb.push({ level: 'L4', label: truncate(stmtGeoid, 30) });
```

Breadcrumb: `HR / PKG_ORDERS / proc_init / stmt_3`

### 2.8 Граничные случаи L4

| Случай | Поведение |
|---|---|
| Statement без subquery-детей | L4 показывает только root + его OutputColumns + source tables |
| OutputColumn `pending` | Отображается красным, tooltip: "Unresolved — run S3 pass" |
| Рекурсивный CTE | Максимальная глубина рекурсии = 5 (limit в TRAVERSE) |
| Более 50 OutputColumns | Пагинация: показывать первые 20, кнопка "show all" |

---

## 3. Зависимости между фазами

```
S2.0 (fixes)  ─────────────────────────► S2.1 (exploreRoutineAggregate)
                                              │
                                              ▼
S2.2 (level renumber) ──────────────────► S2.3 (new L2 view, uses exploreRoutineAggregate)
                                              │
S2.4 (DaliRecord L3) ─────────────────────┤
                                              ▼
                                         S2.5 (L4 stmt drill, DaliOutputColumn node)
                                              │
                                              ▼
                                         S2.6 (StatementNode click → L2 back)
```

---

## 4. Known issues из Sprint 1 (фиксируются в S2.0)

### Issue A: DATA_FLOW target handle not mapped

**Симптом:** `DATA_FLOW` ребро приземляется на header StatementNode вместо конкретной
output-column row.

**Причина:** backend эмитит `targetHandle = 'tgt-' + id(oc)` где `id(oc) = #31:X` (ArcadeDB rid),
но frontend создаёт handle с `id = 'tgt-' + col.id` где `col.id` — синтетический id из `applyStmtColumns`.

**Фикс:**

```typescript
// frontends/verdandi/src/utils/transformColumns.ts
// Строка ~58 в applyStmtColumns:
// БЫЛО:
stmtCols.push({ id: col.id, ... })
// СТАЛО:
stmtCols.push({ id: col.backendRid ?? col.id, ... })
// backendRid приходит из GraphQL как oc.id (ArcadeDB @rid)
```

```java
// ExploreService.java — OutputColumn mapping:
// БЫЛО: oc.setId(syntheticId)
// СТАЛО: oc.setId(arcadeRid) // e.g. "#31:5"
```

### Issue B: edge palette mismatch

```typescript
// frontends/verdandi/src/utils/transformColumns.ts lines 108-142
// БЫЛО:
{ stroke: '#D4922A', strokeDasharray: '3 2' }  // WRITES_TO
// СТАЛО:
getEdgeStyle('WRITES_TO')  // из transformHelpers.ts
```

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 16.04.2026 | 1.0 | Initial execution spec. `exploreRoutineAggregate` contract (Cypher + ArcadeDB SQL). L4 StatementTree: `DaliOutputColumn` as first-class node, GraphQL schema, `OutputColumnNode.tsx`. Issue A+B fixes. |
