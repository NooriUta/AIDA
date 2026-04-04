# ARCH_04042026_UX_LOOM_NAVIGATION

## VERDANDI LOOM — Модель навигации и Use Cases

**Дата:** 04.04.2026  
**Статус:** Принято  
**Контекст:** Уточнение уровней L1/L2/L3, новые сущности Application/Service, поведение Statement на L2, SubQuery drill-down на L3, механики expand и collapse/expand TableNode.

---

## 1. Иерархия данных (полная)

```
Application                          ← НОВЫЙ тип — L1 верхний уровень
  └── Service                        ← НОВЫЙ тип — L1 второй уровень
        └── DaliDatabase             ← существующий
              └── DaliSchema         ← существующий
                    ├── DaliTable    ← L2
                    │     └── DaliColumn → DaliAtom → ...  ← L3
                    └── DaliPackage
                          └── DaliRoutine   ← L2
                                └── DaliStatement (корневые) ← L2
                                      └── DaliStatement (вложенные) ← SubQuery L3
```

### Новые типы нод — добавить в `domain.ts`

```typescript
export type DaliNodeType =
  // ── НОВЫЕ (L1 верхние уровни) ─────────────────────────────────────────
  | 'DaliApplication'       // Приложение — верхний уровень L1
  | 'DaliService'           // Сервис — второй уровень L1
  // ── Существующие ──────────────────────────────────────────────────────
  | 'DaliDatabase'
  | 'DaliSchema'
  | 'DaliPackage'
  | 'DaliTable'
  | 'DaliColumn'
  | 'DaliJoin'
  | 'DaliRoutine'
  | 'DaliStatement'
  | 'DaliSession'
  | 'DaliAtom'
  | 'DaliOutputColumn'
  | 'DaliParameter'
  | 'DaliVariable';
```

### Новые типы рёбер — добавить в `domain.ts`

```typescript
// Добавить к DaliEdgeType:
  | 'HAS_SERVICE'        // Application → Service
  | 'HAS_DATABASE'       // Service → DaliDatabase
  | 'USES_DATABASE'      // Service → DaliDatabase (если сервис использует несколько БД)
```

---

## 2. Уровни визуализации — уточнённое определение

### L1 — Overview (4 уровня иерархии на одном canvas)

**Цель:** Карта приложений — кто владеет какими базами, как сервисы связаны между собой.  
**Пользователи:** DBA, Architect, Tech Lead  
**Запрос:** SQL GROUP BY + агрегация  
**Масштаб:** ~10–800 нод

**Что показывается:**

| Тип ноды | Отображение | Действие |
|----------|-------------|----------|
| `DaliApplication` | ApplicationNode — крупный, синий, верхний | Click → выделить, Double-click → scope в это приложение |
| `DaliService` | ServiceNode — средний, teal | Click → выделить, Double-click → scope в этот сервис |
| `DaliDatabase` | DatabaseNode — как раньше | Double-click → drill-down на L2 |
| `DaliSchema` | SchemaNode — как раньше | Double-click → drill-down на L2 в эту схему |
| `DaliPackage` | PackageNode — как раньше | Double-click → drill-down на L2 в этот пакет |

**Иерархия на canvas (ELK layered, направление TOP → DOWN):**
```
[Application]  →  [Service]  →  [Database]  →  [Schema / Package]
```

**Фильтрация по scope в L1:**  
Если пользователь double-click на `DaliApplication` — canvas остаётся L1, но сужается scope: показывает только сервисы и БД этого приложения. Breadcrumb: `Overview > MyApp`.  
Если double-click на `DaliService` → scope: только БД и схемы этого сервиса. Breadcrumb: `Overview > MyApp > OrderService`.  
Если double-click на `DaliDatabase` или `DaliSchema` → **переход на L2**.

**Важно:** Application и Service — это L1-scope фильтры, а не переходы на другой уровень. Уровень остаётся L1, меняется только scope и набор видимых нод.

---

### L2 — Exploration (внутри схемы/пакета)

**Цель:** Таблицы и рутины схемы, их читающие/пишущие связи. Без раскрытия вложенных подзапросов.  
**Пользователи:** Data Engineer, Data Analyst  
**Запрос:** Cypher MATCH depth=2  
**Масштаб:** ~50–500 нод

**Что показывается:**

| Тип ноды | Условие появления | Отображение |
|----------|------------------|-------------|
| `DaliTable` | Все таблицы в scope | TableNode (partial, collapse/expand) |
| `DaliRoutine` | Все рутины в scope | RoutineNode |
| `DaliStatement` | **Только корневые** — у которых нет parent Statement | StatementNode |

**Определение "корневого" Statement:**  
`DaliStatement` показывается на L2 если выполняется условие:  
**НЕТ входящего ребра `NESTED_IN` или `USES_SUBQUERY` от другого `DaliStatement`**  
то есть `parentStatement == null`.

Тип Statement при этом не важен — показываются корневые любого типа (Query, Cursor, RefCursor, Insert, Update, ...).

**Вложенные DaliRoutine:**  
DaliRoutine могут быть вложенными друг в друга (ребро `CALLS`). На L2 показывается **полное дерево вызовов** рутин в пределах scope — раскрывается через expand button `↓` на RoutineNode.

**Связи на L2:**

| Ребро | Значение |
|-------|----------|
| `READS_FROM` | Table → Table или Statement → Table |
| `WRITES_TO` | Statement → Table или Routine → Table |
| `CALLS` | Routine → Routine (вложенность) |
| `CONTAINS_STMT` | Routine → Statement (корневые) |
| `CONTAINS_TABLE` | Schema → Table |
| `CONTAINS_ROUTINE` | Package → Routine |

**Что НЕ показывается на L2:**
- Вложенные Statement (SubQuery) — только через drill-down на L3
- DaliAtom — только на L3
- DaliColumn — только в TableNode (partial/expanded) или на L3

---

### L3 — Column Lineage

**Цель:** Цепочка трансформаций данных на уровне колонок и атомарных операций.  
**Пользователи:** Data Engineer, Data Analyst (глубокий анализ)  
**Запрос:** Cypher path  
**Масштаб:** ~10–200 нод

**Два способа попасть на L3:**
1. Double-click на `DaliTable` (L2) → L3 по всем колонкам таблицы
2. Click на конкретную колонку в TableNode → L3 только по этой колонке

**Что показывается:**

| Тип ноды | Отображение |
|----------|-------------|
| `DaliColumn` | ColumnNode — исходная колонка |
| `DaliAtom` | AtomNode — атомарная операция (SELECT expr, transform) |
| `DaliOutputColumn` | OutputColumnNode — результирующая колонка |
| `DaliJoin` | JoinNode — условие JOIN |
| `DaliParameter` | ParameterNode — параметр рутины |
| `DaliVariable` | VariableNode — переменная |

**Раскрытие сложных expressions:**  
DaliAtom может содержать сложное выражение (CASE WHEN, COALESCE, CAST...). На L3 это раскрывается графически — дерево AtomNode → дочерние AtomNode через ребро `HAS_ATOM` или `CHILD_OF`.

**SubQuery — отдельный drill-down:**  
Если DaliAtom содержит SubQuery (ребро `USES_SUBQUERY` → DaliStatement):
- На AtomNode появляется badge `⊂ SubQuery`
- Click на badge → **drill-down на новый L3** для этого SubQuery
- Breadcrumb: `Overview > public > orders > col: revenue > SubQuery#1`
- SubQuery L3 показывает свою цепочку Column → Atom → OutputColumn независимо
- Из SubQuery L3 можно вернуться назад через breadcrumb

**Вложенные SubQuery:**  
SubQuery внутри SubQuery — каждый уровень добавляет сегмент в breadcrumb:  
`... > SubQuery#1 > SubQuery#2`  
Глубина не ограничена — следует реальной структуре запроса.

---

## 3. Use Cases

### UC-01: Impact Check (Data Engineer)
**Сценарий:** «Что сломается если я изменю таблицу `orders`?»

```
1. Search "orders" → результаты в левой панели
2. Click на orders (L2) → canvas центрируется на TableNode
3. Click кнопки [↓ downstream] → expand на 1 слой
4. KNOT inspector показывает: 12 таблиц затронуто, 3 рутины
5. Опционально: depth slider → 2 слоя → видно транзитивные зависимости
```

**Данные:** Cypher `MATCH (t:DaliTable {name:'orders'})-[:READS_FROM|WRITES_TO*1..N]->(d) RETURN d`

---

### UC-02: Upstream Trace (Data Engineer)
**Сценарий:** «Откуда берутся данные в колонке `revenue` таблицы `fact_sales`?»

```
1. Search "fact_sales" → найти таблицу
2. Double-click → L3 (column lineage)
3. Click на колонку "revenue" в TableNode → L3 только для revenue
4. Граф показывает цепочку: source_table.amount → Atom(multiply) → revenue
5. Если в цепочке есть SubQuery → badge ⊂, click → SubQuery L3
```

---

### UC-03: Schema Explore (Data Analyst)
**Сценарий:** «Какие таблицы в схеме `reporting` и как они связаны?»

```
1. L1 → найти нужную схему (или через search)
2. Double-click SchemaNode → L2 с таблицами reporting
3. Таблицы в partial state (заголовок + N колонок)
4. READS_FROM/WRITES_TO рёбра показывают связи
5. Click на интересную таблицу → KNOT inspector показывает детали
```

---

### UC-04: Overview Map (DBA/Architect)
**Сценарий:** «Как наши приложения связаны с базами данных?»

```
1. L1 Overview — видны все Application → Service → Database
2. ELK layout: слева Application, справа Schema
3. Можно свернуть (collapse) отдельные Application
4. Double-click Application → scope filter в L1 (не переход на L2)
5. Видно какой сервис владеет какой БД, кто использует общие БД
```

---

### UC-05: Routine Inspect (DBA/Data Engineer)
**Сценарий:** «Что читает и пишет рутина `calculate_revenue`?»

```
1. Search "calculate_revenue" → RoutineNode на L2
2. Click → KNOT inspector: список READ/WRITE таблиц
3. Expand [↑↓] кнопки → показать вложенные рутины (CALLS)
4. Click на корневой Statement → StatementNode с его READ/WRITE
5. Если нужна детализация → Double-click Statement → L3 column lineage
```

---

### UC-06: SubQuery Analysis (Data Analyst / Engineer)
**Сценарий:** «Этот атом ссылается на подзапрос — что в нём?»

```
1. L3 Column Lineage → AtomNode с badge ⊂ SubQuery
2. Click на badge → новый L3 для SubQuery
3. Breadcrumb: Overview > public > orders > revenue > SubQuery#1
4. Граф SubQuery: свои Column → Atom → OutputColumn
5. Если вложен ещё SubQuery → ещё один drill-down
6. Escape / breadcrumb → вернуться на родительский L3
```

---

## 4. Механики навигации

### 4.1 Expand buttons (upstream / downstream)

На каждой ноде при hover появляются кнопки за границами ноды:

```
[↑ +N]   [ ИМЯ НОДЫ ]   [↓ +N]
```

- `↑ upstream` — добавить слой нод, которые ПИШУТ В эту ноду
- `↓ downstream` — добавить слой нод, которые ЧИТАЮТ ИЗ этой ноды
- `+N` — prefetch при hover, показывает число нод на следующем слое
- Если N > 20 — показывает `↓ +20 / 47` (20 загружено, 47 всего)
- Новые ноды добавляются к существующему графу, ELK пересчитывает layout
- Кнопка depth в KNOT inspector: `1` / `2` / `3` / `∞` (default: 1)

**Store actions:**
```typescript
expandNeighbors: (nodeId: string, direction: 'upstream' | 'downstream', depth: number) => void;
collapseNeighbors: (nodeId: string, direction: 'upstream' | 'downstream') => void;
```

### 4.2 TableNode — три состояния (collapse/expand)

| Состояние | Высота | Содержимое | Триггер |
|-----------|--------|------------|---------|
| `collapsed` | 44px | Только заголовок (имя + schema + N col) | 🟡 кнопка или drag-up |
| `partial` | авто | Заголовок + первые 7 колонок + «+N more» | По умолчанию |
| `expanded` | авто | Заголовок + ВСЕ колонки + filter input | 🟢 кнопка или drag-down |

**Кнопки окна (top-right угол header):**
- 🔴 — убрать ноду с canvas (не удалить из БД — только скрыть)
- 🟡 — `partial` state
- 🟢 — `expanded` state

**В expanded state:**
- Inline filter input (`filter columns...`) — Fuse.js по имени и типу
- Вертикальный скроллбар если колонок > 15
- Каждая строка кликабельна → drill-down на L3 по этой колонке

### 4.3 L1 Scope filter (Application / Service)

Double-click на `DaliApplication` или `DaliService` в L1 — **не меняет уровень**, меняет scope:

```typescript
// loomStore — новый action:
setScopeFilter: (nodeId: string, nodeType: 'DaliApplication' | 'DaliService') => void;
```

Breadcrumb в L1: `Overview > MyApp > OrderService`  
Каждый сегмент кликабельный — возврат к более широкому scope.  
Кнопка `[× сбросить фильтр]` в breadcrumb возвращает к полному L1.

### 4.4 SubQuery drill-down (L3 → L3)

SubQuery — отдельная L3 сессия поверх текущей L3:

```typescript
// ViewLevel расширяется:
export type ViewLevel = 'L1' | 'L2' | 'L3';

// BreadcrumbItem расширяется:
export interface BreadcrumbItem {
  level: ViewLevel;
  scope: string | null;
  label: string;
  subQueryDepth?: number;   // НОВОЕ: 0 = основной L3, 1+ = вложенные SubQuery
}
```

Breadcrumb пример: `Overview › public › orders › revenue › SubQuery#1 › SubQuery#2`

---

## 5. Изменения в domain.ts

### Новые типы нод

```typescript
export type DaliNodeType =
  | 'DaliApplication'   // НОВЫЙ — верхний L1
  | 'DaliService'       // НОВЫЙ — второй L1
  | 'DaliDatabase'
  | 'DaliSchema'
  | 'DaliPackage'
  | 'DaliTable'
  | 'DaliColumn'
  | 'DaliJoin'
  | 'DaliRoutine'
  | 'DaliStatement'
  | 'DaliSession'
  | 'DaliAtom'
  | 'DaliOutputColumn'
  | 'DaliParameter'
  | 'DaliVariable';
```

### Новые типы рёбер

```typescript
// Добавить к DaliEdgeType:
  | 'HAS_SERVICE'        // Application → Service
  | 'HAS_DATABASE'       // Service → DaliDatabase
  | 'USES_DATABASE'      // Service → DaliDatabase (многие-ко-многим)
```

### Новые node data interfaces

```typescript
export interface ApplicationNodeData extends DaliNodeData {
  nodeType: 'DaliApplication';
  servicesCount: number;
  databasesCount: number;
}

export interface ServiceNodeData extends DaliNodeData {
  nodeType: 'DaliService';
  application: string;
  databasesCount: number;
  routinesCount: number;
}

// Расширить TableNodeData:
export type NodeDisplayState = 'collapsed' | 'partial' | 'expanded';

export interface TableNodeData extends DaliNodeData {
  nodeType: 'DaliTable';
  schema: string;
  columns: ColumnInfo[];
  displayState: NodeDisplayState;        // НОВОЕ
  visibleColumnsCount: number;           // НОВОЕ — default 7 для partial
}

// Расширить DaliNodeData — флаг SubQuery:
export interface DaliNodeData {
  label: string;
  nodeType: DaliNodeType;
  childrenAvailable: boolean;
  metadata: Record<string, unknown>;
  hasSubQuery?: boolean;                 // НОВОЕ — для AtomNode badge ⊂
  subQueryStatementId?: string;          // НОВОЕ — id DaliStatement подзапроса
  isRootStatement?: boolean;             // НОВОЕ — для фильтрации на L2
  // ... остальное без изменений
}
```

---

## 6. Cypher-запросы для новых уровней

### L1 — с Application и Service

```sql
-- Агрегация верхнего уровня
SELECT 
  app.name as application,
  svc.name as service, 
  db.name as database,
  schema.name as schema,
  count(t) as tables_count
FROM DaliApplication app
  JOIN DaliService svc ON (app)-[:HAS_SERVICE]->(svc)
  JOIN DaliDatabase db ON (svc)-[:HAS_DATABASE|USES_DATABASE]->(db)
  JOIN DaliSchema schema ON (db)-[:CONTAINS_SCHEMA]->(schema)
  JOIN DaliTable t ON (schema)-[:CONTAINS_TABLE]->(t)
GROUP BY app.name, svc.name, db.name, schema.name
```

### L2 — только корневые Statement

```cypher
// Таблицы, рутины и корневые Statement в scope схемы
MATCH (schema:DaliSchema {name: $schemaName})
OPTIONAL MATCH (schema)-[:CONTAINS_TABLE]->(t:DaliTable)
OPTIONAL MATCH (schema)-[:CONTAINS_PACKAGE]->(pkg)-[:CONTAINS_ROUTINE]->(r:DaliRoutine)
OPTIONAL MATCH (r)-[:CONTAINS_STMT]->(stmt:DaliStatement)
  WHERE NOT EXISTS { MATCH (stmt)<-[:USES_SUBQUERY|NESTED_IN]-(:DaliStatement) }
OPTIONAL MATCH (t)-[rel:READS_FROM|WRITES_TO]->(t2:DaliTable)
RETURN t, r, stmt, rel, t2
LIMIT 500
```

### L3 — SubQuery раскрытие

```cypher
// Найти SubQuery внутри конкретного Statement
MATCH (atom:DaliAtom)-[:USES_SUBQUERY]->(subStmt:DaliStatement)
WHERE id(atom) = $atomId
// Затем развернуть этот subStmt как новый L3:
MATCH path = (src:DaliColumn)<-[:ATOM_REF_COLUMN]-(a:DaliAtom)
WHERE (a)-[:BELONGS_TO]->(subStmt)
RETURN path
```

---

## 7. Новые компоненты React (Phase 3)

| Компонент | Файл | Что делает |
|-----------|------|------------|
| `ApplicationNode` | `canvas/nodes/ApplicationNode.tsx` | L1 верхний уровень |
| `ServiceNode` | `canvas/nodes/ServiceNode.tsx` | L1 второй уровень |
| `ExpandButton` | `canvas/controls/ExpandButton.tsx` | hover-кнопки ↑↓ на нодах |
| `SubQueryBadge` | `canvas/nodes/AtomNode.tsx` | badge ⊂ SubQuery |
| `ColumnFilter` | `canvas/nodes/TableNode.tsx` | inline filter в expanded state |
| `DepthControl` | `panels/KnotInspector.tsx` | slider 1/2/3/∞ слоёв |

---

## 8. Что остаётся неизменным

- Жёсткие переходы L1 → L2 → L3 через double-click и breadcrumb — сохраняются
- Expand buttons работают **поверх** текущего уровня, не ломая его логику
- ELK.js layout пересчитывается после каждого expand
- `drillDown()` / `navigateBack()` в loomStore — логика без изменений, только добавляется `subQueryDepth` в BreadcrumbItem
- Application/Service scope filter в L1 — это **не** `drillDown()`, это отдельный action `setScopeFilter()`
