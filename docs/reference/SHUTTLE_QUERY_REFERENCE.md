# SHUTTLE — Query Reference
> Актуально: 2026-04-15
> Источник: `SHUTTLE/src/main/java/studio/seer/lineage/service/`

Все запросы к ArcadeDB (порт 2480) проходят через `ArcadeGateway`.
Язык: **SQL** (агрегация, поиск) или **Cypher** (обход графа, lineage).
Слияние результатов параллельных запросов выполняется **в Java** — ArcadeDB не поддерживает UNION ALL через несколько вызовов, а Cypher UNION имеет баг с `labels()[0]` (возвращает `List<String>`).

---

## 1. OverviewService — L1 агрегация

**Файл:** `OverviewService.java`
**Язык:** SQL
**Вызов:** `overview()` → GraphQL `query { overview { ... } }`
**Параллелизм:** 1 запрос

### Запрос

```sql
SELECT
    @rid                                                          AS rid,
    schema_name,
    out('CONTAINS_TABLE').size()                                  AS tableCount,
    out('CONTAINS_ROUTINE')[@type = 'DaliRoutine'].size()         AS routineCount,
    out('CONTAINS_ROUTINE')[@type = 'DaliPackage'].size()         AS packageCount,
    in('CONTAINS_SCHEMA')[0].db_geoid                             AS databaseGeoid,
    in('CONTAINS_SCHEMA')[0].db_name                              AS databaseName
FROM DaliSchema
ORDER BY schema_name
```

### Что возвращает

| Поле | Тип | Описание |
|------|-----|----------|
| `rid` | String | `@rid` вершины DaliSchema |
| `schema_name` | String | Имя схемы |
| `tableCount` | int | `out('CONTAINS_TABLE').size()` |
| `routineCount` | int | Прямые рутины в схеме (сейчас всегда 0) |
| `packageCount` | int | Пакеты в схеме (via `CONTAINS_ROUTINE[@type=DaliPackage]`) |
| `databaseGeoid` | String? | `@rid` родительского DaliDatabase |
| `databaseName` | String? | `db_name` родительского DaliDatabase |

### Особенности
- `CONTAINS_ROUTINE` из DaliSchema ведёт **только в DaliPackage** (в текущих данных).
  `routineCount` = 0 корректно — прямых рутин нет.
- `databaseGeoid` / `databaseName` = null для схем без ребра `CONTAINS_SCHEMA`.
- `databaseEngine`, `applicationGeoid`, `applicationName` — не заполняются (зарезервировано для HOUND-DB-001).

---

## 2. ExploreService — L2 обход

**Файл:** `ExploreService.java`
**Язык:** Cypher
**Вызов:** `explore(scope)` → GraphQL `query { explore(scope: "...") { nodes edges } }`  
**Вызов (AGG):** `exploreRoutineAggregate(scope)` → GraphQL `query { exploreRoutineAggregate(scope: "...") { nodes edges } }`
**Параллелизм:** 2 (schema, через `Uni.combine`) или 1 большой UNION ALL (pkg/db) или 8 параллельных (rid) или 3 (routineAggregate) или 4 (routineScope)

Scope-формат парсится в `ScopeRef`:

| Формат | Метод | Пример |
|--------|-------|--------|
| `schema-NAME` | `exploreSchema(name, null, false)` | `schema-DWH` |
| `schema-NAME\|DB` | `exploreSchema(name, db, false)` | `schema-DWH\|DWH` |
| `pkg-NAME` | `explorePackage(name)` | `pkg-CALC_PKL_CRED` |
| `db-NAME` | `exploreByDatabase(name)` | `db-CRM` |
| `routine-RID` | `exploreRoutineScope(rid)` | `routine-#10:5` |
| `#10:5` | `exploreByRid(rid)` | `#10:5` |

Метод `explore(scope, includeExternal)` — перегрузка с флагом внешних рёбер (только для schema scope).

---

### 2.1 exploreSchema — 10 веток UNION ALL

Параметры: `$schema` (schema_name / schema_geoid), `$dbName` (db_name, `''` = не фильтровать).

**Фильтр дубликатов:** `WHERE $dbName = '' OR s.db_name = $dbName`
*Нужен потому что два DaliSchema могут иметь одинаковое `schema_name` в разных БД.*

#### Фаза 1 — структурная принадлежность

| # | Паттерн | Edge | LIMIT |
|---|---------|------|-------|
| 1 | `schema → table` | `CONTAINS_TABLE` | 300 |
| 2 | `schema → package/routine` | `CONTAINS_ROUTINE` | 200 |
| 3 | `schema → routine → rootStmt` | `CONTAINS_STMT` | 300 |
| 4 | `schema → pkg → routine` | `CONTAINS_ROUTINE` | 200 |
| 5 | `schema → pkg → routine → rootStmt` | `CONTAINS_STMT` | 300 |

Фильтр rootStmt: `WHERE coalesce(stmt.parent_statement, '') = ''`
*Исключает вложенные подзапросы — только «корневые» операторы.*

#### Фаза 2 — источник / приёмник данных

| # | Паттерн | Edge | LIMIT |
|---|---------|------|-------|
| 6 | `rootStmt → table` (прямая запись) | `WRITES_TO` | 3000 |
| 7 | `rootStmt → table` (прямое чтение) | `READS_FROM` | 3000 |
| 8 | `subStmt -[:CHILD_OF*]-> rootStmt → table` (hoisted чтение) | `READS_FROM` | 200 |
| 9 | `subStmt -[:CHILD_OF*]-> rootStmt → table` (hoisted запись) | `WRITES_TO` | 200 |
| 10 | `rootStmt reads-FROM-schema-table → WRITES_TO → cross-schema table` | `WRITES_TO` | 200 |

*«Hoisting»: подзапросы поднимаются к корневому оператору через `CHILD_OF*1..30` — сам subStmt в граф не добавляется.*

#### Фаза 3 — колонки (вынесена в stmtColumns)

`HAS_COLUMN`, `HAS_OUTPUT_COL`, `HAS_AFFECTED_COL` **не** включены в этот запрос.
Колонки загружаются отдельным вызовом `stmtColumns(ids)` — см. раздел 2.5.

**Итого:** 10 веток, 1 запрос к ArcadeDB.

#### Параллельный запрос: columnFlowCypher (DATA_FLOW / FILTER_FLOW)

Запускается **параллельно** с основным UNION ALL через `Uni.combine().all().unis(baseUni, colUni).asTuple()`.
Возвращает рёбра с `sourceHandle` / `targetHandle` — route-поля для React Flow, адресующие конкретную колонку.

Добавлен потому, что Cypher UNION ALL требует одинаковый набор колонок от всех веток, а добавление
`sourceHandle`/`targetHandle` к 10 базовым веткам раздуло бы запрос без выгоды.

```cypher
-- DATA_FLOW: DaliTable.column → DATA_FLOW → DaliOutputColumn → (вверх по CHILD_OF*0..30) → rootStmt
MATCH (s:DaliSchema)
WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
MATCH (s)-[:CONTAINS_TABLE]->(srcTbl:DaliTable)-[:HAS_COLUMN]->(srcCol:DaliColumn)
MATCH (srcCol)-[:DATA_FLOW]->(oc:DaliOutputColumn)<-[:HAS_OUTPUT_COL]-(sub:DaliStatement)
MATCH (sub)-[:CHILD_OF*0..30]->(root:DaliStatement)
WHERE coalesce(root.parent_statement, '') = ''
OPTIONAL MATCH (root)-[:HAS_AFFECTED_COL]->(rootAff:DaliAffectedColumn)
    WHERE toUpper(coalesce(rootAff.column_name, '')) = toUpper(coalesce(oc.name, oc.col_key, ''))
OPTIONAL MATCH (root)-[:HAS_OUTPUT_COL]->(rootOc:DaliOutputColumn)
    WHERE toUpper(coalesce(rootOc.name, rootOc.col_key, '')) = toUpper(coalesce(oc.name, oc.col_key, ''))
RETURN DISTINCT id(srcTbl) AS srcId, ... , 'DATA_FLOW' AS edgeType,
       'src-' + id(srcCol) AS sourceHandle,
       CASE WHEN rootAff IS NOT NULL THEN 'tgt-' + id(rootAff)
            WHEN rootOc  IS NOT NULL THEN 'tgt-' + id(rootOc)
            ELSE '' END AS targetHandle

UNION  -- (не UNION ALL, чтобы дедуплицировать)

-- FILTER_FLOW: DaliColumn → FILTER_FLOW → rootDaliStatement (WHERE-условие)
MATCH (s:DaliSchema) ...
MATCH (s)-[:CONTAINS_TABLE]->(srcTbl:DaliTable)-[:HAS_COLUMN]->(srcCol:DaliColumn)
MATCH (srcCol)-[:FILTER_FLOW]->(stmt:DaliStatement)
WHERE coalesce(stmt.parent_statement, '') = ''
RETURN ... , 'FILTER_FLOW' AS edgeType,
       'src-' + id(srcCol) AS sourceHandle, '' AS targetHandle
LIMIT 4000
```

`targetHandle = 'tgt-' + id(column_node)` — соответствует handle-идентификатору рядка колонки
в `StatementNode.tsx`. Если совпадение по имени не найдено — `targetHandle = ''` (ребро к default handle ноды).

Старые сегменты phase 2.3/2.4 (таблица → стейтмент без handle) **удалены** — `columnFlowCypher` заменяет их.

#### EXTERNAL_EXTENSION (includeExternal=true)

Добавляется в конец UNION ALL при `explore(scope, true)`.  
Возвращает рёбра READS_FROM/WRITES_TO к таблицам **из другой схемы** (cross-schema lineage).

```cypher
UNION ALL
-- EXT-READS_FROM: любой stmt схемы ($schema), читающий таблицу из другой схемы
MATCH (s:DaliSchema)
WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
MATCH (s)-[:CONTAINS_ROUTINE]->(r1)-[:CONTAINS_ROUTINE*0..1]->(rr:DaliRoutine)
      -[:CONTAINS_STMT]->(rootStmt:DaliStatement)
WHERE coalesce(rootStmt.parent_statement, '') = ''
MATCH (rootStmt)<-[:CHILD_OF*0..30]-(sub:DaliStatement)-[:READS_FROM]->(t:DaliTable)
WHERE t.schema_geoid IS NOT NULL AND t.schema_geoid <> $schema
RETURN DISTINCT id(rootStmt) AS srcId, ... , 'READS_FROM' AS edgeType
LIMIT 2000

UNION ALL
-- EXT-WRITES_TO (аналогично)
...
LIMIT 1000
```

**Почему property filter вместо subgraph check:**  
Первая попытка использовала `NOT (s)-[:CONTAINS_TABLE]->(t)` (отрицательный обход подграфа).
В сочетании с двумя паттернами переменной длины это тайм-аутилось при 30 с на реальных схемах.
Замена на `t.schema_geoid <> $schema` — per-row проверка свойства, без subgraph probe.

**Итого:** базовый cypher → 10-12 веток + columnFlowCypher → 2 ветки = **2 параллельных запроса к ArcadeDB**.

---

### 2.2 explorePackage — 6 веток UNION ALL

Параметр: `$pkg` (package_name).

| # | Паттерн | Edge | LIMIT |
|---|---------|------|-------|
| 1 | `pkg → routine` | `CONTAINS_ROUTINE` | 200 |
| 2 | `routine → rootStmt` | `CONTAINS_STMT` | 300 |
| 3 | `rootStmt → table` (прямое чтение) | `READS_FROM` | 200 |
| 4 | `rootStmt ← CHILD_OF ← subStmt → table` (подзапрос, поднятие) | `READS_FROM` (hoisted) | 200 |
| 5 | `rootStmt → table` (запись) | `WRITES_TO` | 200 |
| 6 | `rootStmt → outputCol` | `HAS_OUTPUT_COL` | 500 |

*`ROUTINE_USES_TABLE` = 0 рёбер в данных, не используется.*

---

### 2.3 exploreByRid — 8 параллельных запросов

Параметр: `$rid` (id вершины в ArcadeDB Cypher, не `@rid`).

| # | Запрос | Описание | LIMIT |
|---|--------|----------|-------|
| 1 | `outQ` | Все исходящие рёбра от `$rid` (с фильтром constraint-нод) | 300 |
| 2 | `inQ` | Все входящие рёбра к `$rid` (с фильтром constraint-нод) | 300 |
| 3 | `outColQ` | `HAS_OUTPUT_COL` для DaliStatement-детей `$rid` | 200 |
| 4 | `stmtOutColQ` | `HAS_OUTPUT_COL` если `$rid` — сам DaliStatement | 100 |
| 5 | `sibColQ` | Если `$rid` — DaliColumn: родительская таблица + все её колонки | 100 |
| 6 | `sibOutColQ` | Если `$rid` — DaliOutputColumn: родительский stmt + все его output-колонки | 100 |
| 7 | `hoistReadsQ` | READS_FROM subStmt → hoisted rootStmt (через `CHILD_OF*1..30`) | 200 |
| 8 | `hoistWritesQ` | WRITES_TO subStmt → hoisted rootStmt (через `CHILD_OF*1..30`) | 200 |

Все 8 запросов запускаются параллельно через `Uni.combine().all().unis(...)`.
Каждый обёрнут в `.onFailure().recoverWithItem(List.of())` — один падающий запрос не блокирует остальные.

**Фильтрация constraint-нод и DaliAtom (BUG-VC-001, BUG-VC-007):** `outQ` и `inQ` содержат:
```cypher
AND NOT (m:DaliConstraint OR m:DaliPrimaryKey OR m:DaliForeignKey OR m:DaliAtom)
```
Без этого фильтра клик на таблицу с PK/FK возвращает неизвестные фронтенду типы нод (crash canvas).

Порядок `coalesce` для меток: `schema_name → table_name → package_name → routine_name → stmt_geoid → column_name → name → col_key`

---

### 2.4 exploreByDatabase — 9 веток UNION ALL

Параметр: `$dbName` (db_name). Scope: `db-NAME`.

Аналог `exploreSchema` но для всей БД сразу — обходит все `DaliSchema` где `db_name = $dbName`.
Колонки вынесены в `stmtColumns` (Phase 3 не включена). LIMITs увеличены (500–3000) из-за большего объёма данных.

| # | Паттерн | Edge | LIMIT |
|---|---------|------|-------|
| 1 | `schema → table` | `CONTAINS_TABLE` | 500 |
| 2 | `schema → package/routine` | `CONTAINS_ROUTINE` | 300 |
| 3 | `schema → routine → rootStmt` | `CONTAINS_STMT` | 500 |
| 4 | `schema → pkg → routine` | `CONTAINS_ROUTINE` | 300 |
| 5 | `schema → pkg → routine → rootStmt` | `CONTAINS_STMT` | 500 |
| 6 | `rootStmt → table` (прямая запись) | `WRITES_TO` | 3000 |
| 7 | `rootStmt → table` (прямое чтение) | `READS_FROM` | 3000 |
| 8 | hoisted READS_FROM (subStmt → CHILD_OF → root) | `READS_FROM` | 500 |
| 9 | hoisted WRITES_TO (subStmt → CHILD_OF → root) | `WRITES_TO` | 500 |

---

### 2.5 exploreRoutineAggregate — L2 AGG (рутины × таблицы)

**GraphQL:** `query { exploreRoutineAggregate(scope: "schema-DWH") { nodes edges } }`  
**Параллелизм:** 3 параллельных Uni (mainQuery + extQuery + callsQuery)  
**Scope:** `schema-NAME` или `pkg-NAME` (разбирается через `ScopeRef.parse()`)

**Назначение:** агрегированный вид — для каждой рутины коллапсирует все её стейтменты в одно
ребро `routine → table` (READS_FROM или WRITES_TO). Canvas показывает рутины и таблицы,
без стейтмент-узлов. Позволяет увидеть межпакетные/межсхемные потоки без шума statement-уровня.

#### mainQuery — агрегация (package или schema scope)

**Package scope** (`pkg-NAME`):
```cypher
MATCH (p:DaliPackage {package_name: $scope})-[:CONTAINS_ROUTINE]->(r:DaliRoutine)
OPTIONAL MATCH (r)-[:CONTAINS_STMT]->(stmt:DaliStatement)
OPTIONAL MATCH (stmt)-[:READS_FROM]->(tR:DaliTable)
OPTIONAL MATCH (stmt)-[:WRITES_TO]->(tW:DaliTable)
WITH p, r, collect(DISTINCT tR) AS reads, collect(DISTINCT tW) AS writes
RETURN id(p) AS pkgId, p.package_name AS pkgName,
       coalesce(p.schema_geoid, '') AS pkgSchema,
       id(r) AS src, r.routine_name AS srcLabel,
       coalesce(r.schema_geoid, '') AS srcSchema,
       coalesce(r.package_geoid, '') AS srcPackage,
       coalesce(r.routine_type, '') AS srcKind,
       reads, writes
LIMIT 300
```

**Schema scope** (`schema-NAME`):
```cypher
MATCH (s:DaliSchema) WHERE s.schema_geoid = $scope
MATCH (s)-[:CONTAINS_ROUTINE]->(n1)
OPTIONAL MATCH (n1)-[:CONTAINS_ROUTINE]->(nested:DaliRoutine)
WITH s, CASE WHEN n1:DaliRoutine THEN n1 ELSE nested END AS r
WHERE r IS NOT NULL
OPTIONAL MATCH (r)-[:CONTAINS_STMT]->(stmt:DaliStatement)
OPTIONAL MATCH (stmt)-[:READS_FROM]->(tR:DaliTable)
OPTIONAL MATCH (stmt)-[:WRITES_TO]->(tW:DaliTable)
WITH r, collect(DISTINCT tR) AS reads, collect(DISTINCT tW) AS writes
RETURN id(r) AS src, r.routine_name AS srcLabel, ...
LIMIT 300
```

*Примечание:* `CONTAINS_STMT` охватывает **все** стейтменты рутины (root + sub-queries).
`parent_statement` **не** фильтруется — цель — все таблицы, которые хотя бы один стейтмент рутины читает/пишет.
`collect(DISTINCT tR/tW)` дедуплицирует на уровне рутины.

#### extQuery — cross-schema рутины (только schema scope)

Рутины из **других схем**, которые читают или пишут таблицы в `$scope`:
```cypher
MATCH (extR:DaliRoutine)-[:CONTAINS_STMT]->(stmt:DaliStatement)-[:READS_FROM]->(tR:DaliTable)
WHERE extR.schema_geoid <> $scope
  AND tR.schema_geoid = $scope
WITH extR, collect(DISTINCT tR) AS reads
OPTIONAL MATCH (extR)-[:CONTAINS_STMT]->(stmt2:DaliStatement)-[:WRITES_TO]->(tW:DaliTable)
WHERE tW.schema_geoid = $scope
WITH extR, reads, collect(DISTINCT tW) AS writes
RETURN id(extR) AS src, extR.routine_name AS srcLabel,
       coalesce(extR.schema_geoid, '') AS srcSchema, ...
LIMIT 100
```

Для package scope = `Uni.createFrom().item(List.of())` (не актуально).

#### callsQuery — routine → routine CALLS

```cypher
-- package scope:
MATCH (p:DaliPackage {package_name: $scope})-[:CONTAINS_ROUTINE]->(r1:DaliRoutine)
MATCH (r1)-[:CALLS]->(r2:DaliRoutine)
RETURN id(r1) AS srcId, r1.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
       coalesce(r1.schema_geoid, '') AS srcScope, ...
       id(r2) AS tgtId, r2.routine_name AS tgtLabel, 'DaliRoutine' AS tgtType,
       'CALLS' AS edgeType, '' AS sourceHandle, '' AS targetHandle
LIMIT 200

-- schema scope: аналогично через CONTAINS_ROUTINE → DaliRoutine
LIMIT 200
```

Вызывающий (r1) и вызываемый (r2) могут быть из разных схем — r2 рендерится как external RoutineNode.

#### Post-processing в Java

Строки из mainQuery и extQuery приходят в формате `(routine, [reads], [writes])`.
Java разворачивает их в flat-rows `buildResult`-формата:
1. Для package scope: один `DaliPackage` self-node (NODE_ONLY) + CONTAINS_ROUTINE рёбра pkg→routine
2. Для каждой рутины: один self-node (NODE_ONLY) — чтобы рутины без чтений/записей тоже рендерились
3. Для каждой таблицы из `reads`: ребро routine→table с edgeType=`READS_FROM`
4. Для каждой таблицы из `writes`: ребро routine→table с edgeType=`WRITES_TO`
5. callRows добавляются напрямую (уже в flat-формате)

`buildResult()` дедуплицирует ноды, `enrichDataSource()` добавляет `dataSource`-бейдж таблицам.

---

### 2.6 exploreRoutineScope — L3 (drill из L2 AGG)

**GraphQL:** scope `routine-#10:5` → `explore("routine-#10:5")` → `exploreRoutineScope("#10:5")`  
**Параллелизм:** 6 параллельных Uni (Q1–Q6), объединяемых через `Uni.combine().all().unis(...)`

**Назначение:** детализированный вид одной DaliRoutine после клика на RoutineNode в L2 AGG.
Canvas показывает root-стейтменты, таблицы, record-узлы. Без sub-statement узлов и DaliAtom.

| Запрос | Паттерн | Edge | LIMIT |
|--------|---------|------|-------|
| Q1 stmtsQ | root-стейтменты рутины (self-loop NODE_ONLY) | — | 300 |
| Q2 directReadsQ | rootStmt → table | `READS_FROM` | 500 |
| Q3 directWritesQ | rootStmt → table | `WRITES_TO` | 500 |
| Q4 hoistReadsQ | rootStmt ← CHILD_OF*1..20 ← sub → table | `READS_FROM` hoisted | 1000 |
| Q5a recordSelfQ | DaliRecord узлы (NODE_ONLY, по `routine_geoid`) | — | 200 |
| Q5b recordFieldsQ | rec → DaliRecordField (HAS_RECORD_FIELD) | `HAS_RECORD_FIELD` | 1000 |
| Q6 bulkCollectsQ | stmt → DaliRecord через `BULK_COLLECTS_INTO` | `BULK_COLLECTS_INTO` | 200 |

**Фильтр root-стейтментов:** `WHERE coalesce(s.parent_statement, '') = ''`  
**Hoist-паттерн (Q4):** `CHILD_OF*1..20` — sub-стейтменты поднимаются к root, сами в граф не попадают.

**Исключения (intentional):** `DaliParameter`, `DaliVariable`, `DaliOutputColumn`,
`DaliAffectedColumn`, `DaliAtom`, non-root sub-statements.  
Детализация колонок приходит через отдельный `stmtColumns` (раздел 2.7).

---

### 2.7 stmtColumns — второй проход колонок

**Файл:** `ExploreService.exploreStmtColumns(List<String> ids)`
**GraphQL:** `query { stmtColumns(ids: [...]) { nodes edges } }`
**Параллелизм:** 3 параллельных Uni-запроса с UNWIND

Второй проход загрузки колонок для уже отрендеренных нод. Вызывается фронтендом после
`explore` — передаёт все `@rid` таблиц и стейтментов из первого прохода.

| Запрос | Паттерн | Edge |
|--------|---------|------|
| `hasColQ` | `DaliTable → DaliColumn` (via HAS_COLUMN рёбра, созданные Hound-парсером) | `HAS_COLUMN` |
| `hasOutColQ` | `DaliStatement → DaliOutputColumn` | `HAS_OUTPUT_COL` |
| `hasAffColQ` | `DaliStatement → DaliAffectedColumn` | `HAS_AFFECTED_COL` |

`hasColQ` использует обход рёбер (O(degree) — быстрее, чем property-scan по всему `DaliColumn`):

```cypher
UNWIND $ids AS rid
MATCH (t:DaliTable)
WHERE id(t) = rid
WITH t
MATCH (t)-[:HAS_COLUMN]->(col:DaliColumn)
RETURN id(t) AS srcId, t.table_name AS srcLabel, 'DaliTable' AS srcType,
       id(col) AS tgtId, coalesce(col.column_name,'') AS tgtLabel, '' AS tgtScope,
       'DaliColumn' AS tgtType, 'HAS_COLUMN' AS edgeType,
       toString(coalesce(col.is_pk, false)) AS tgtPk,
       toString(coalesce(col.is_fk, false)) AS tgtFk,
       toString(coalesce(col.is_required, false)) AS tgtReq,
       coalesce(col.data_type, '') AS tgtDataType
```

**⚠ ArcadeDB Cypher chained MATCH**: `MATCH (t) WHERE... / MATCH (t)-[...]-(m)` возвращает
пустой результат. Обязательно разделять через `WITH t` между двумя MATCH-клаузами.

**Ключевое:** Использует `UNWIND $ids AS rid` + `WHERE id(t) = rid` вместо `id(t) IN $ids`.
ArcadeDB Cypher: `id()` возвращает LINK-тип, несовместимый с `IN` над `List<String>`.

Каждый запрос обёрнут в `.onFailure().recoverWithItem(List.of())`.
Результат завершается через `.flatMap(this::enrichDataSource)` (BUG-VC-004) —
DaliTable-ноды получают `dataSource` для отображения бейджа в `TableNode.tsx`.

---

## 3. LineageService — L3 линейный обход

**Файл:** `LineageService.java`
**Язык:** Cypher
**Вызов:** `lineage(nodeId)` / `upstream(nodeId)` / `downstream(nodeId)` / `expandDeep(nodeId, depth)`
**Параллелизм:** 2 параллельных (lineage) или 1 (upstream/downstream/expandDeep)

**Фильтрация constraint-нод (BUG-VC-002):** Все четыре метода содержат:
```cypher
AND NOT (m:DaliConstraint OR m:DaliPrimaryKey OR m:DaliForeignKey)
```
Без этого фильтра IS_PK_COLUMN / IS_FK_COLUMN рёбра от колонок с PK/FK возвращали
constraint-ноды неизвестных типов в lineage-граф.

### 3.1 lineage — двунаправленный (2 параллельных запроса)

```cypher
-- outQ: исходящие
MATCH (n)-[r]->(m) WHERE id(n) = $nodeId
  AND (NOT m:DaliStatement OR coalesce(m.parent_statement, '') = '')
  AND NOT (m:DaliConstraint OR m:DaliPrimaryKey OR m:DaliForeignKey OR m:DaliAtom)
RETURN id(n) AS srcId, labels(n)[0] AS srcType, <label_n> AS srcLabel,
       id(m) AS tgtId, labels(m)[0] AS tgtType, <label_m> AS tgtLabel,
       m.schema_geoid AS tgtScope, type(r) AS edgeType
LIMIT 200

-- inQ: входящие (переменные переставлены)
MATCH (m)-[r]->(n) WHERE id(n) = $nodeId
  AND (NOT m:DaliStatement OR coalesce(m.parent_statement, '') = '')
  AND NOT (m:DaliConstraint OR m:DaliPrimaryKey OR m:DaliForeignKey OR m:DaliAtom)
RETURN id(m) AS srcId, labels(m)[0] AS srcType, <label_m> AS srcLabel,
       id(n) AS tgtId, labels(n)[0] AS tgtType, <label_n> AS tgtLabel,
       n.schema_geoid AS tgtScope, type(r) AS edgeType
LIMIT 200
```

### 3.2 upstream — только входящие (1 запрос)

Используется кнопкой `◄` (LOOM-027) на нодах графа.
Паттерн `MATCH (m)-[r]->(n) WHERE id(n) = $nodeId` — тот же, что `inQ` в lineage (с constraint-фильтром).

### 3.3 downstream — только исходящие (1 запрос)

Используется кнопкой `►` (LOOM-027) на нодах графа.
Паттерн `MATCH (n)-[r]->(m) WHERE id(n) = $nodeId` — тот же, что `outQ` (с constraint-фильтром).

### 3.4 expandDeep — многоуровневый обход (1 запрос)

**GraphQL:** `query { expandDeep(nodeId: "...", depth: 5) { nodes edges } }`
**Глубина:** 1–10 (cap через `Math.max(1, Math.min(depth, 10))`).

Ищет все рёбра `READS_FROM | WRITES_TO` в обоих направлениях на `depth` переходов от `nodeId`.
Интерполирует `depth` в строку запроса (не параметр) — ArcadeDB не поддерживает `$param` внутри `*min..max`.

Безопасен от constraint-нод: фильтр `all(n IN nodes(path) WHERE n:DaliTable OR (n:DaliStatement AND ...))`
гарантирует, что в путях только известные типы.

Возвращает все отдельные рёбра пути через `UNWIND relationships(path)` + `WITH DISTINCT`.
Фронтенд мёржит их в существующий L2-граф без перезапуска ELK-layout. LIMIT 500.

### Порядок `coalesce` для меток в LineageService

```
table_name → column_name → routine_name → package_name → stmt_geoid → app_name → schema_name
```

*Отличается от ExploreService: `app_name` включён, `column_name` стоит раньше `routine_name`.*

---

## 4. SearchService — полнотекстовый поиск

**Файл:** `SearchService.java`
**Язык:** SQL
**Вызов:** `search(query, limit)` → GraphQL `query { search(q: "...", limit: 20) { ... } }`
**Параллелизм:** 12 параллельных SQL-запросов, слияние в Java по убыванию score

Механизм: `LIKE '%query%'` (не Lucene full-text — ArcadeDB не токенизирует `_`).

| # | Тип | Поле поиска | Поле scope | Score |
|---|-----|-------------|------------|-------|
| 1 | `DaliTable` | `table_name` | `in('CONTAINS_TABLE')[0].schema_name` | 1.0 |
| 2 | `DaliColumn` | `column_name` | `in('HAS_COLUMN')[0].in('CONTAINS_TABLE')[0].schema_name` | 0.9 |
| 3 | `DaliOutputColumn` | `name` | `session_id` | 0.9 |
| 4 | `DaliPackage` | `package_name` | `package_name` | 0.8 |
| 5 | `DaliRoutine` | `routine_name` | `in('CONTAINS_ROUTINE')[0].package_name` | 0.8 |
| 6 | `DaliParameter` | `param_name` | `session_id` | 0.75 |
| 7 | `DaliVariable` | `var_name` | `session_id` | 0.75 |
| 8 | `DaliSchema` | `schema_name` | `db_name` | 0.7 |
| 9 | `DaliDatabase` | `db_name` | `db_name` | 0.7 |
| 10 | `DaliApplication` | `app_name` | `''` | 0.7 |
| 11 | `DaliStatement` | `stmt_geoid` | `in('CONTAINS_STMT')[0].in('CONTAINS_ROUTINE')[0].package_name` или `session_id` | 0.6 |
| 12 | `DaliSession` | `file_path` | `db_name` | 0.5 |

### Навигация из поиска (scope → уровень)

| scope значение | Уровень перехода | Формат scope |
|----------------|-----------------|--------------|
| `schema_name` (Table, Column, Schema) | L2 Schema | `schema-<scope>` |
| `package_name` (Package, Routine, Statement) | L2 Package | `pkg-<scope>` |
| `session_id` / `db_name` / `''` | Только отображение | — |

### Известные проблемы

- **~40% таблиц** не имеют ребра `CONTAINS_TABLE` → `in('CONTAINS_TABLE')[0]` = null → scope пустой → переход из поиска не работает.
- `DaliOutputColumn`, `DaliParameter`, `DaliVariable` — scope = `session_id` → нет прямой навигации на L2.
- SQL injection: `esc()` экранирует только `'` → `''.replace("'", "''")`. Допустимо для внутреннего использования, недостаточно для публичного API.

---

## 5. Общие паттерны и ограничения ArcadeDB

### Почему нет UNION между запросами в Java

ArcadeDB Cypher имеет баг: при `UNION ALL` колонки, созданные через `labels(n)[0]`, возвращаются как `List<String>` вместо `String`, что ломает маппинг. Обходной путь — параллельные запросы через `Uni.combine().all().unis(...)` с ручным слиянием в Java.

### Параметризация

Cypher: `$paramName` (Map передаётся как второй аргумент `arcade.cypher(query, params)`).
SQL: форматирование через `String.format()` (SearchService) — не параметризованные запросы.

### `id()` vs `@rid` в Cypher

`@rid` не работает в Cypher WHERE-условиях — использовать `id(n) = $rid`.
`@rid` работает в SQL-запросах (`SELECT @rid FROM ...`).

### Constraint-ноды и DaliAtom не попадают в LOOM canvas

После рефакторинга Hound (commits 3849730/ef248cd) в ArcadeDB созданы vertex-типы:
`DaliConstraint`, `DaliPrimaryKey`, `DaliForeignKey` с рёбрами:
`HAS_PRIMARY_KEY`, `HAS_FOREIGN_KEY`, `IS_PK_COLUMN`, `IS_FK_COLUMN`, `REFERENCES_TABLE`, `REFERENCES_COLUMN`.

Также: `DaliAtom` (50,858 вершин) — ANTLR4 atom analysis, связаны через `HAS_ATOM`, `ATOM_REF_COLUMN` и т.д.

Все эти типы **не включены** в `DaliNodeType` (frontend `domain.ts`) и не рендеримы в LOOM.
`exploreByRid`, `lineage`, `upstream`, `downstream` содержат фильтр:
```cypher
AND NOT (m:DaliConstraint OR m:DaliPrimaryKey OR m:DaliForeignKey OR m:DaliAtom)
```

Frontend `transformExplore.ts`: `SUPPRESSED_EDGES` содержит `HAS_ATOM`, `ATOM_REF_STMT`, `ATOM_REF_OUTPUT_COL`.
`transformGqlExplore` фильтрует ноды с `type === 'DaliAtom'`.

### Лимиты

| Запрос | Лимит |
|--------|-------|
| CONTAINS_TABLE (schema) | 300 |
| CONTAINS_TABLE (database) | 500 |
| CONTAINS_ROUTINE / CONTAINS_STMT | 200–500 |
| READS_FROM / WRITES_TO (schema) | 200–3000 |
| READS_FROM / WRITES_TO hoisted | 200–500 |
| EXTERNAL_EXTENSION READS_FROM | 2000 |
| EXTERNAL_EXTENSION WRITES_TO | 1000 |
| columnFlowCypher DATA_FLOW + FILTER_FLOW | 4000 |
| HAS_OUTPUT_COL / HAS_AFFECTED_COL | 500 |
| HAS_COLUMN | 500 |
| exploreByRid out/in | 300 |
| exploreByRid hoistReadsQ / hoistWritesQ | 200 |
| exploreRoutineAggregate mainQuery | 300 |
| exploreRoutineAggregate extQuery (cross-schema) | 100 |
| exploreRoutineAggregate callsQuery | 200 |
| exploreRoutineScope stmts / reads / writes | 300 / 500 / 500 |
| exploreRoutineScope hoistReads | 1000 |
| exploreRoutineScope records / fields | 200 / 1000 |
| lineage out/in | 200 |
| expandDeep paths | 500 |
| search per type | `max(limit/2, 10)` |

---

## 6. Карта: уровень → сервис → метод

```
L1 Overview
  └── OverviewService.overview()
        └── SQL: SELECT FROM DaliSchema (1 запрос)

L2 Explore (классический: schema → stmt → table)
  └── ExploreService.explore(scope [, includeExternal])
        ├── schema-NAME[|DB]  → exploreSchema()      — 10 веток UNION ALL
        │                                               + columnFlowCypher (параллельно)
        │                                               + EXTERNAL_EXTENSION (если includeExternal)
        ├── pkg-NAME          → explorePackage()     — 6 веток UNION ALL
        ├── db-NAME           → exploreByDatabase()  — 9 веток UNION ALL
        ├── routine-RID       → exploreRoutineScope() — 6 параллельных Cypher (L3 drill из AGG)
        └── #rid              → exploreByRid()        — 8 параллельных Cypher

L2 AGG (агрегированный: routine × table, без стейтментов)
  └── ExploreService.exploreRoutineAggregate(scope)
        ├── schema-NAME  → mainQuery (schema scope) + extQuery (cross-schema) + callsQuery
        └── pkg-NAME     → mainQuery (pkg scope) + callsQuery
              (3 параллельных Uni, post-processing flatten в Java)

L2+ Second pass (колонки)
  └── ExploreService.exploreStmtColumns(ids)
        └── 3 параллельных Cypher с UNWIND (HAS_COLUMN + HAS_OUTPUT_COL + HAS_AFFECTED_COL)

L3 Lineage (full)
  └── LineageService.lineage(nodeId)
        └── 2 параллельных Cypher (out + in, с constraint-фильтром)

L3 Expand кнопки ◄►  (LOOM-027)
  ├── LineageService.upstream(nodeId)    — 1 Cypher (incoming, с constraint-фильтром)
  └── LineageService.downstream(nodeId) — 1 Cypher (outgoing, с constraint-фильтром)

L2/L3 Multi-hop expand
  └── LineageService.expandDeep(nodeId, depth)
        └── 1 Cypher path-query (READS_FROM|WRITES_TO *1..depth, depth cap 10)

Search
  └── SearchService.search(query, limit)
        └── 12 параллельных SQL (LIKE '%query%')
```
