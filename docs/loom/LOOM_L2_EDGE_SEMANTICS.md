# LOOM L2 — Data-flow edge semantics

**Документ:** `LOOM_L2_EDGE_SEMANTICS`
**Версия:** 1.1
**Дата:** 15.04.2026
**Статус:** ACTIVE — reference для команды и будущих промотов level-логики

Объясняет как четыре основных dataflow-ребра (`READS_FROM`, `WRITES_TO`,
`DATA_FLOW`, `FILTER_FLOW`) эмитятся Hound'ом, хранятся в ArcadeDB и
возвращаются SHUTTLE'ом во фронтенд для отрисовки на L2 канвасе LOOM,
а также где граница — что L2 показывает, а что нет, и почему.

---

## 1. Edge-реестр

Все четыре типа — **graph edges** (а не документы) в схеме ArcadeDB,
определены в `libraries/hound/src/main/java/com/hound/storage/RemoteSchemaCommands.java:74–84`.
Writer — `JsonlBatchBuilder` / `RemoteWriter` (строки приведены в таблице).

| Edge | DB-endpoints (source → target) | Emitter | Свойства |
|------|-------------------------------|---------|----------|
| `READS_FROM` | `DaliStatement → DaliTable` | `RemoteWriter.java` (phase 2, stmt+tbl pair), `JsonlBatchBuilder.java` | `session_id` |
| `WRITES_TO`  | `DaliStatement → DaliTable` | same | `session_id` |
| `DATA_FLOW`  | `DaliColumn → DaliOutputColumn` | `JsonlBatchBuilder` (atom phase) | `session_id`, `via_atom` |
| `FILTER_FLOW` | `DaliColumn → DaliStatement` | `JsonlBatchBuilder` line 877 & `RemoteWriter.java:1226` | `session_id`, `filter_type` (`WHERE` / `HAVING` / `JOIN`), `statement_geoid`, `via_atom` |

**Сырые counts** в тестовой YGG на 14.04.2026:
```
READS_FROM:   7 270
WRITES_TO:    1 501
DATA_FLOW:   18 573
FILTER_FLOW:  8 688
```

---

## 2. Семантика

### 2.1 READS_FROM — «statement читает таблицу»

Для каждого statement, который содержит reference на таблицу в
`FROM` / `JOIN` / `USING` / `MERGE USING` / subquery-level FROM,
Hound пишет ребро `DaliStatement -[READS_FROM]-> DaliTable`.

**Emitted when:**
- SELECT ... FROM t
- UPDATE t (также создаёт WRITES_TO к t, плюс READS_FROM к любым join-таблицам)
- DELETE FROM t WHERE EXISTS (SELECT FROM t2) — READS_FROM t2
- MERGE INTO target USING source — READS_FROM source (плюс WRITES_TO target)
- SELECT внутри CURSOR — READS_FROM всех FROM-таблиц

**NOT emitted for:**
- Literal VALUES tuples
- Inline views, CTEs (они становятся child statements со своими READS_FROM,
  см. §4 Hoisting)

### 2.2 WRITES_TO — «statement меняет таблицу»

Эмитится когда statement производит мутацию данных или DDL:
- `INSERT INTO t ...`
- `UPDATE t SET ...`
- `DELETE FROM t WHERE ...`
- `MERGE INTO t ...`
- `TRUNCATE TABLE t`
- `CREATE TABLE t AS SELECT ...` (CTAS — одновременно WRITES_TO t)
- `CREATE VIEW v AS SELECT ...` (WRITES_TO v)
- `ALTER TABLE t ...` — `DaliDDLStatement → DaliDDLModifiesTable → DaliTable` (отдельный edge type, не обычный WRITES_TO)

### 2.3 DATA_FLOW — «колонка-источник питает output колонку»

Эмитится column-level: для каждой **resolved column reference** в
SELECT-списке statement'а, writer создаёт ребро
`DaliColumn -[DATA_FLOW]-> DaliOutputColumn`.

**Пример:**
```sql
INSERT INTO orders_summary (region, total)
SELECT o.region, SUM(o.amount) FROM orders o GROUP BY o.region;
```
→ `orders.region -[DATA_FLOW]-> orders_summary.region_oc`
→ `orders.amount -[DATA_FLOW]-> orders_summary.total_oc`

Для `SELECT *` writer раскрывает `*` в список реальных колонок и
создаёт DATA_FLOW для каждой.

Для expression columns (`a+b`, `CASE WHEN`) — writer создаёт DATA_FLOW
от КАЖДОЙ source column, участвующей в expression, к одному и тому же
output column. То есть `CASE WHEN a>b THEN c ELSE d END AS X` даёт
4 ребра DATA_FLOW → X (от `a`, `b`, `c`, `d`).

### 2.4 FILTER_FLOW — «колонка участвует в фильтре/join/having»

Column-level analogue для predicate atoms. Writer обходит каждый
ColumnRef в `WHERE`, `HAVING`, `JOIN ... ON`, `CASE WHEN condition`
(кроме SELECT-list) и эмитит
`DaliColumn -[FILTER_FLOW]-> DaliStatement` с `filter_type` = "WHERE" | "HAVING" | "JOIN".

**Пример:**
```sql
SELECT o.region FROM orders o WHERE o.status = 'PAID' AND o.year = 2026;
```
→ `orders.status -[FILTER_FLOW {filter_type:'WHERE'}]-> stmt`
→ `orders.year   -[FILTER_FLOW {filter_type:'WHERE'}]-> stmt`
→ `orders.region -[DATA_FLOW]-> region_oc` (селективный projection)

Разделение нужно для audit: «где вообще используется эта колонка?»
даёт два ответа — «в projections» (DATA_FLOW) и «в predicates» (FILTER_FLOW).

---

## 3. Как L2 query их достаёт (`ExploreService.exploreSchema`)

**Scope:** одна `DaliSchema` (например `BUDM_RMS`). Результат — плоский
список rows, каждая строка описывает один edge с
`srcId/srcLabel/srcType` + `tgtId/tgtLabel/tgtScope/tgtType` + `edgeType`.
Frontend `transformExplore.ts` собирает их в React Flow nodes + edges.

**10 UNION ALL-сегментов** (см. `ExploreService.java:78-170` + 2.3/2.4 добавленные 14.04.2026):

| # | Cypher (упрощённо) | Возвращает |
|---|-------------------|-----------|
| 1 | `(s)-[:CONTAINS_TABLE]->(t)` | все таблицы в схеме |
| 2 | `(s)-[:CONTAINS_ROUTINE]->(child)` где `child :DaliPackage \| DaliRoutine` | top-level routines+packages |
| 3 | `(s)-[:CONTAINS_ROUTINE]->(r)-[:CONTAINS_STMT]->(stmt)` где `stmt.parent_statement = ''` | root statements в schema-direct routines |
| 4 | `(s)-[:CONTAINS_ROUTINE]->(pkg)-[:CONTAINS_ROUTINE]->(r)` | routines в packages |
| 5 | same as #4 + `-[:CONTAINS_STMT]->(stmt)` | root stmts в package routines |
| 6 | `(s)-[:CONTAINS_TABLE]->(t)<-[:WRITES_TO]-(stmt)` где `stmt.parent_statement = ''` | **прямой WRITES_TO от root-stmt** |
| 7 | `(s)-[:CONTAINS_TABLE]->(t)<-[:READS_FROM]-(stmt)` где `stmt.parent_statement = ''` | **прямой READS_FROM от root-stmt** |
| 8 | `(s)-[:CONTAINS_TABLE]->(t)<-[:READS_FROM]-(:Stmt)-[:CHILD_OF*1..30]->(rootStmt)` | **hoisted READS_FROM** — subquery читает → приписываем root |
| 9 | same hoist but for WRITES_TO | **hoisted WRITES_TO** |
| 10 | `stmt-[:READS_FROM]->(:Table)<-[:WRITES_TO]-(target)` | transitive: если stmt и читает из t, и пишет в t2, эмитим WRITES_TO к t2 (для видимости output targets на канвасе) |
| **2.3** | `(s)-[:CONTAINS_TABLE]->(srcTbl)-[:HAS_COLUMN]->(col)-[:DATA_FLOW]->(oc)<-[:HAS_OUTPUT_COL]-(stmt)` | **DATA_FLOW агрегированный до `srcTable → stmt`** (колонка-источник → консьюмер-stmt) |
| **2.4** | `(s)-[:CONTAINS_TABLE]->(srcTbl)-[:HAS_COLUMN]->(col)-[:FILTER_FLOW]->(stmt)` | **FILTER_FLOW агрегированный до `srcTable → stmt`** |

### 3.1 Hoisting (сегменты 8/9)

Зачем нужен? — Oracle PL/SQL generator чаще всего пишет:
```sql
INSERT INTO target
SELECT ... FROM source_table JOIN other ON ...
```
ANTLR parser складывает это как **root INSERT statement** + **child SELECT** (через CHILD_OF). Сам INSERT не имеет READS_FROM; source-таблицы
читает SELECT-subquery. Без hoisting'а INSERT на канвасе висел бы
без стрелок.

Segment 8 прыгает по цепочке `CHILD_OF*1..30` и приписывает «sub-stmt
reads from t» к «root reads from t». То же для WRITES_TO в сегменте 9.

### 3.2 Direction flip

Backend возвращает `READS_FROM` как `stmt → table`. Канвас хочет
рисовать стрелку от **источника к потребителю** — то есть **table → stmt**.
`transformExplore.ts:261` переворачивает sourcelink в рендере:

```ts
const flip = edgeType === 'READS_FROM';
return { source: flip ? e.target : e.source, target: flip ? e.source : e.target, ... };
```

`WRITES_TO`, `DATA_FLOW`, `FILTER_FLOW` — не переворачиваются, они и в
backend, и в рендере идут в одном направлении `stmt → table` / `table → stmt`.

---

## 4. Почему у `#25:8333` INSERT нет визуальных источников

**Конкретный stmt:** `BUDM_RMS_TMD.DM_LOADER_RMS_DET_GUARANTOR:PROCEDURE:LOAD:INSERT:47`, @rid `#25:8333`.

**Direct check:**
```
SELECT count(*) FROM (SELECT expand(out('READS_FROM')) FROM DaliStatement WHERE @rid = '#25:8333')
→ 0

SELECT count(*) FROM (SELECT expand(in('CHILD_OF')) FROM DaliStatement WHERE @rid = '#25:8333')
→ 1
```

У INSERT нет direct READS_FROM. Есть 1 child subquery. Hoist (сегмент 8)
должен был подтянуть его sources. Проверка:
```cypher
MATCH (sub)-[:READS_FROM]->(t:DaliTable)
MATCH (sub)-[:CHILD_OF*1..30]->(root {stmt_geoid: 'BUDM_RMS_TMD...INSERT:47'})
RETURN DISTINCT t.table_geoid;
```
→ 6 таблиц, **все в схеме `BMRT.*`**, не в `BUDM_RMS`.

**Корень проблемы.** Сегмент 8 структурно связан со схемой scope:

```cypher
MATCH (s:DaliSchema) WHERE s.schema_geoid = $schema
MATCH (s)-[:CONTAINS_TABLE]->(t:DaliTable)   -- t ДОЛЖНА быть в $schema
      <-[:READS_FROM]-(...)
```

`BMRT.BM_DET_CURRENCY` ∉ `BUDM_RMS`, поэтому строка не матчит и hoisted
edge не возвращается. **Cross-schema reads невидимы на schema-scope L2.**

### 4.1 Возможные фиксы

| Вариант | Плюсы | Минусы |
|---------|-------|--------|
| **A.** Убрать фильтр `(s)-[:CONTAINS_TABLE]->(t)` в hoist-сегментах — возвращать любые таблицы | Видно все cross-schema reads для INSERT/UPDATE | Канвас получит узлы-таблицы, которых нет в schema, их надо рендерить с другим стилем («external»); potential bloat на схемах типа DWH |
| **B.** Показывать заглушку «N external sources» на INSERT node, с клик-through на детальный просмотр | Канвас остаётся чистым | Нужен UI-control + отдельный query |
| **C.** Автоматически расширять scope до `database` при наличии cross-schema reads | Прозрачно для пользователя | Breaks schema isolation; может взорвать DWH-сценарии |
| **D.** Документировать как expected behaviour, дать пользователю toggle «Include cross-schema» в FilterToolbar | Контролируемо | Требует nullable стейта + UI |

**Рекомендация:** вариант **D** — добавить toggle «External sources» в
FilterToolbar, по умолчанию выключен. Когда включён — снимается
фильтр `s.schema_geoid = $schema` для сегментов 8/9 (и 2.3/2.4 по
DATA_FLOW/FILTER_FLOW), добавляется новый сегмент для external tables
с пометкой `external: true` в meta, frontend рендерит их серым и
помечает badge-ом. Отдельный commit в рамках Phase 4c или 5.

---

## 5. Edge counts в L2-результате для `BUDM_RMS`

После фиксов 14.04.2026:
```
CONTAINS_TABLE:  54
READS_FROM:     219  (прямые + hoisted в пределах BUDM_RMS)
WRITES_TO:      111  (прямые + hoisted в пределах BUDM_RMS)
DATA_FLOW:       17  (агрегировано до srcTable → stmt)
FILTER_FLOW:      3  (агрегировано до srcTable → stmt)
```

`DATA_FLOW` и `FILTER_FLOW` мало потому что в BUDM_RMS большинство
INSERT'ов читают из BMRT.* — те же кросс-schema кейсы что описаны в §4.
Колонка-источник `BMRT.*.col` не проходит фильтр `(s)-[:CONTAINS_TABLE]->(srcTbl)`
в сегментах 2.3/2.4.

---

## 6. Визуальный стиль на канвасе

Все четыре edge-типа имеют уникальный (цвет × dash-pattern × animated)
ключ, см. `frontends/verdandi/src/utils/transformHelpers.ts:42-57` и
обновления 14.04.2026:

### Основные data-flow edges (L2 + L3)

| Edge | Цвет | Паттерн | Animated |
|------|------|---------|----------|
| `READS_FROM` | `#88B8A8` teal | solid | ❌ |
| `WRITES_TO` | `#D4922A` amber | **solid** *(было long-dash `8 3`, изменено 15.04.2026)* | ❌ |
| `DATA_FLOW` | `#A8B860` lime | medium-dash `5 3` | ✅ flowing |
| `FILTER_FLOW` | `#B87AA8` mauve | dots `1 4` | ✅ flowing |

### PL/SQL record edges (L3 только)

| Edge | Цвет | Паттерн | Откуда → Куда |
|------|------|---------|--------------|
| `BULK_COLLECTS_INTO` | `#D4922A` amber | dash `5 3` | `DaliStatement` → `DaliRecord` |
| `RETURNS_INTO` | `#B87AA8` mauve | solid | `DaliStatement` → `DaliRecord` |
| `RECORD_USED_IN` | `#88B8A8` teal | dash `5 3` | `DaliRecord` → `DaliStatement` |
| `HAS_RECORD_FIELD` | — | **подавлен** | `DaliRecord` → `DaliRecordField` (поля внутри ноды) |

Каждый тип различим на любом zoom level. Legend (`LegendButton.tsx`)
отражает эти же токены.

---

## История изменений

| Дата | Версия | Что |
|------|--------|-----|
| 14.04.2026 | 1.0 | Initial — объясняет все 4 edge-типа, их Cypher-источники в `ExploreService.exploreSchema`, hoisting через CHILD_OF*, и cross-schema blind-spot для INSERT `#25:8333`. |
| 15.04.2026 | 1.1 | WRITES_TO переведён на solid (убран `strokeDasharray: '8 3'`). Добавлен раздел PL/SQL record edges: BULK_COLLECTS_INTO / RETURNS_INTO / RECORD_USED_IN (Q6–Q8 в `exploreRoutineScope`). HAS_RECORD_FIELD в NESTING_EDGES — поля внутри RecordNode, не стрелки. |
