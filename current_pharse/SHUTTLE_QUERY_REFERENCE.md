# SHUTTLE — Query Reference
> Актуально: 2026-04-06
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
**Параллелизм:** 1 большой UNION ALL запрос (schema/pkg) или 6 параллельных (rid)

Scope-формат парсится в `ScopeRef`:

| Формат | Метод | Пример |
|--------|-------|--------|
| `schema-NAME` | `exploreSchema(name, null)` | `schema-DWH` |
| `schema-NAME\|DB` | `exploreSchema(name, db)` | `schema-DWH\|DWH` |
| `pkg-NAME` | `explorePackage(name)` | `pkg-CALC_PKL_CRED` |
| `#10:5` | `exploreByRid(rid)` | `#10:5` |

---

### 2.1 exploreSchema — 13 веток UNION ALL

Параметры: `$schema` (schema_name), `$dbName` (db_name, `''` = не фильтровать).

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
| 6 | `rootStmt → table` (запись) | `WRITES_TO` | 200 |
| 7 | `rootStmt → table` (прямое чтение) | `READS_FROM` | 200 |
| 8 | `rootStmt ← CHILD_OF ← subStmt → table` (подзапрос, поднятие) | `READS_FROM` (hoisted) | 200 |

*Ветка 8 («hoisting»): подзапросы не добавляются в граф сами по себе — вместо них отображается родительский rootStmt с ребром READS_FROM к таблице.*

#### Фаза 3 — наполнение колонками

| Паттерн | Edge | Веток | LIMIT |
|---------|------|-------|-------|
| `table → column` | `HAS_COLUMN` | 1 | 500 |
| `stmt → outputCol` (прямой + через пакет) | `HAS_OUTPUT_COL` | 2 | 500 |
| `stmt → affectedCol` (прямой + через пакет) | `HAS_AFFECTED_COL` | 2 | 500 |

**Итого:** 13 веток, 1 запрос к ArcadeDB.

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

### 2.3 exploreByRid — 6 параллельных запросов

Параметр: `$rid` (id вершины в ArcadeDB Cypher, не `@rid`).

| # | Запрос | Описание | LIMIT |
|---|--------|----------|-------|
| 1 | `outQ` | Все исходящие рёбра от `$rid` | 300 |
| 2 | `inQ` | Все входящие рёбра к `$rid` | 300 |
| 3 | `outColQ` | `HAS_OUTPUT_COL` для DaliStatement-детей `$rid` | 200 |
| 4 | `stmtOutColQ` | `HAS_OUTPUT_COL` если `$rid` — сам DaliStatement | 100 |
| 5 | `sibColQ` | Если `$rid` — DaliColumn: родительская таблица + все её колонки | 100 |
| 6 | `sibOutColQ` | Если `$rid` — DaliOutputColumn: родительский stmt + все его output-колонки | 100 |

Запросы 1–2 запускаются параллельно через `Uni.combine().all().unis(...)`.
Метка лейбла строится через `nodeLabel()`:
`schema_name → table_name → package_name → routine_name → column_name → stmt_geoid → snippet`

---

## 3. LineageService — L3 линейный обход

**Файл:** `LineageService.java`
**Язык:** Cypher
**Вызов:** `lineage(nodeId)` / `upstream(nodeId)` / `downstream(nodeId)`
**Параллелизм:** 2 параллельных (lineage) или 1 (upstream/downstream)

### 3.1 lineage — двунаправленный (2 параллельных запроса)

```cypher
-- outQ: исходящие
MATCH (n)-[r]->(m) WHERE id(n) = $nodeId
RETURN id(n) AS srcId, labels(n)[0] AS srcType, <label_n> AS srcLabel,
       id(m) AS tgtId, labels(m)[0] AS tgtType, <label_m> AS tgtLabel,
       m.schema_geoid AS tgtScope, type(r) AS edgeType
LIMIT 200

-- inQ: входящие (переменные переставлены)
MATCH (m)-[r]->(n) WHERE id(n) = $nodeId
RETURN id(m) AS srcId, labels(m)[0] AS srcType, <label_m> AS srcLabel,
       id(n) AS tgtId, labels(n)[0] AS tgtType, <label_n> AS tgtLabel,
       n.schema_geoid AS tgtScope, type(r) AS edgeType
LIMIT 200
```

### 3.2 upstream — только входящие (1 запрос)

Используется кнопкой `◄` (LOOM-027) на нодах графа.
Паттерн `MATCH (m)-[r]->(n) WHERE id(n) = $nodeId` — тот же, что `inQ` в lineage.

### 3.3 downstream — только исходящие (1 запрос)

Используется кнопкой `►` (LOOM-027) на нодах графа.
Паттерн `MATCH (n)-[r]->(m) WHERE id(n) = $nodeId` — тот же, что `outQ`.

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

### Лимиты

| Запрос | Лимит |
|--------|-------|
| CONTAINS_TABLE branches | 300 |
| CONTAINS_ROUTINE / CONTAINS_STMT | 200–300 |
| READS_FROM / WRITES_TO | 200 |
| HAS_OUTPUT_COL / HAS_AFFECTED_COL | 500 |
| HAS_COLUMN | 500 |
| exploreByRid out/in | 300 |
| lineage out/in | 200 |
| search per type | `max(limit/2, 10)` |

---

## 6. Карта: уровень → сервис → метод

```
L1 Overview
  └── OverviewService.overview()
        └── SQL: SELECT FROM DaliSchema (1 запрос)

L2 Explore
  └── ExploreService.explore(scope)
        ├── schema-NAME[|DB]  → exploreSchema()   — 13 веток UNION ALL
        ├── pkg-NAME          → explorePackage()  — 6 веток UNION ALL
        └── #rid              → exploreByRid()    — 6 параллельных Cypher

L3 Lineage (full)
  └── LineageService.lineage(nodeId)
        └── 2 параллельных Cypher (out + in)

L3 Expand кнопки ◄►  (LOOM-027)
  ├── LineageService.upstream(nodeId)    — 1 Cypher (incoming)
  └── LineageService.downstream(nodeId) — 1 Cypher (outgoing)

Search
  └── SearchService.search(query, limit)
        └── 12 параллельных SQL (LIKE '%query%')
```
