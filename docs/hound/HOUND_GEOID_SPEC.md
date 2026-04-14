# Hound Geoid Specification

> Единый справочник geoid-схемы для всех vertex types в ArcadeDB (YGG/FRIGG).
>
> Актуально на: 2026-04-14 | Sprint 2

---

## Содержание

1. [Принципы](#1-принципы)
2. [Таблица geoid по типам вершин](#2-таблица-geoid-по-типам-вершин)
3. [Детальные форматы](#3-детальные-форматы)
4. [Коллизии и дедупликация](#4-коллизии-и-дедупликация)
5. [Специальные конструкции](#5-специальные-конструкции)
6. [Связь geoid → RID в ArcadeDB](#6-связь-geoid--rid-в-arcadedb)
7. [Уникальные индексы](#7-уникальные-индексы)

---

## 1. Принципы

| Принцип | Правило |
|---------|---------|
| **Детерминированность** | Одни и те же входные данные → всегда один и тот же geoid |
| **Уникальность** | Geoid уникален в пределах одной БД-namespace (или сессии в ad-hoc режиме) |
| **Регистр** | `UPPER_CASE` для всех компонентов: имён схем, таблиц, колонок, процедур |
| **Разделитель namespace** | `.` (точка) — для иерархии SCHEMA.TABLE.COLUMN |
| **Разделитель типа** | `:` (двоеточие) — для уточнения типа внутри объекта: `ROUTINE:RECORD:VAR` |
| **Dedup-механизм** | `UNIQUE_HASH_INDEX` в ArcadeDB по полю `*_geoid` + `RidCache` в RemoteWriter |
| **Immutability** | Geoid не меняется после первой записи; повторная запись = UPSERT / skip |

---

## 2. Таблица geoid по типам вершин

| Vertex Type | Формат geoid | Пример |
|-------------|-------------|--------|
| `DaliDatabase` | `DB_NAME` | `PROD` |
| `DaliSchema` | `DB_NAME.SCHEMA_NAME` | `PROD.HR` |
| `DaliTable` | `SCHEMA_NAME.TABLE_NAME` | `HR.ORDERS` |
| `DaliColumn` | `SCHEMA_NAME.TABLE_NAME.COLUMN_NAME` | `HR.ORDERS.STATUS` |
| `DaliRoutine` | `SCHEMA.PACKAGE.ROUTINE_NAME` или `SCHEMA.ROUTINE_NAME` | `HR.PKG1.PROC_A` / `HR.PROC_B` |
| `DaliPackage` | `SCHEMA.PACKAGE_NAME` | `HR.PKG_ORDERS` |
| `DaliStatement` | `ROUTINE_GEOID.SEQ` (DML/DCL) | `HR.PKG1.PROC_A.1` |
| `DaliDDLStatement` | `DDL_` + `STMT_GEOID` или `TABLE_GEOID.SEQ` | `DDL_HR.ORDERS.3` |
| `DaliOutputColumn` | `STMT_GEOID:OUT:SEQ` | `HR.PROC_A.1:OUT:2` |
| `DaliAffectedColumn` | `STMT_GEOID:AFF:COLUMN_NAME` | `HR.PROC_A.1:AFF:STATUS` |
| `DaliPrimaryKey` | `TABLE_GEOID:PK:NAME_OR_HASH` | `HR.ORDERS:PK:PK_ORDERS` |
| `DaliForeignKey` | `TABLE_GEOID:FK:NAME_OR_HASH` | `HR.ORDERS:FK:FK_ORDERS_CUST` |
| `DaliUniqueConstraint` | `TABLE_GEOID:UNIQUE:NAME_OR_HASH` | `HR.ORDERS:UNIQUE:UQ_ORDERS_CODE` |
| `DaliCheckConstraint` | `TABLE_GEOID:CHECK:NAME_OR_HASH` | `HR.ORDERS:CHECK:CHK_STATUS` |
| `DaliRecord` | `ROUTINE_GEOID:RECORD:VAR_NAME` | `HR.PKG1.PROC_A:RECORD:L_TAB` |
| `DaliRecordField` | `RECORD_GEOID:FIELD:FIELD_NAME` | `HR.PKG1.PROC_A:RECORD:L_TAB:FIELD:ORDER_ID` |
| `DaliVariable` | `ROUTINE_GEOID:VAR:VAR_NAME` | `HR.PKG1.PROC_A:VAR:V_COUNT` |
| `DaliParameter` | `ROUTINE_GEOID:PARAM:PARAM_NAME` | `HR.PKG1.PROC_A:PARAM:P_ID` |
| `DaliAtom` | `STMT_GEOID:ATOM:SEQ` | `HR.PROC_A.1:ATOM:3` |
| `DaliJoin` | `STMT_GEOID:JOIN:SEQ` | `HR.PROC_A.1:JOIN:1` |
| `DaliSnippet` | `STMT_GEOID:SNIPPET` | `HR.PROC_A.1:SNIPPET` |

---

## 3. Детальные форматы

### 3.1 DaliSchema

```
DB_NAME.SCHEMA_NAME
```

- Namespace mode: `DB_NAME` = фактическое имя БД из HoundConfig (`db_name`)
- Ad-hoc mode: prefix по умолчанию не используется; geoid = `SCHEMA_NAME`
- Пример: `PROD.HR`, `PROD.SYS`, `DEV.DWH`

### 3.2 DaliTable

```
SCHEMA_NAME.TABLE_NAME
```

- Не включает `DB_NAME` — таблицы привязываются к схеме, а не к БД напрямую
- `DB_NAME` передаётся как отдельное поле `db_name` на вершине
- Пример: `HR.ORDERS`, `DWH.FACT_SALES`

### 3.3 DaliColumn

```
SCHEMA_NAME.TABLE_NAME.COLUMN_NAME
```

- Глубина всегда 3 уровня через `.`
- Пример: `HR.ORDERS.ORDER_ID`, `HR.ORDERS.STATUS`

### 3.4 DaliRoutine

```
Standalone:  SCHEMA.ROUTINE_NAME
In package:  SCHEMA.PACKAGE_NAME.ROUTINE_NAME
```

- Package-level: `HR.PKG_ORDERS.PLACE_ORDER`
- Standalone:    `HR.PROC_CLEANUP`
- Анонимный блок: `SCHEMA.ANONYMOUS_BLOCK_SEQ` (счётчик на сессию)

### 3.5 DaliStatement

```
ROUTINE_GEOID.SEQ
```

- `SEQ` — последовательный номер оператора внутри routine (1-based)
- Пример: `HR.PKG_ORDERS.PLACE_ORDER.1`, `HR.PKG_ORDERS.PLACE_ORDER.2`
- Для top-level (вне routine): `SCHEMA.FILE_NAME.SEQ`

### 3.6 DaliDDLStatement

```
DDL_ + STMT_GEOID
```

- `STMT_GEOID` формируется по тем же правилам что и DaliStatement
- Пример: `DDL_HR.ORDERS.3`
- DDL-оператор ALTER TABLE → отдельный geoid по той же таблице с SEQ

### 3.7 DaliRecord / DaliRecordField

```
DaliRecord:      ROUTINE_GEOID:RECORD:VAR_NAME
DaliRecordField: RECORD_GEOID:FIELD:FIELD_NAME
```

Пример цепочки:
```
HR.PKG1.PROC_A:RECORD:L_TAB              ← DaliRecord
HR.PKG1.PROC_A:RECORD:L_TAB:FIELD:ORDER_ID   ← DaliRecordField
HR.PKG1.PROC_A:RECORD:L_TAB:FIELD:STATUS     ← DaliRecordField
```

Источник полей:
- **BULK COLLECT INTO** → поля из cursor SELECT output columns
- **%ROWTYPE** → поля из DaliColumn той же сессии по той же таблице
- **RETURNING BULK COLLECT INTO** → поля из RETURNING clause expressions

### 3.8 DaliVariable / DaliParameter

```
DaliVariable:  ROUTINE_GEOID:VAR:VAR_NAME
DaliParameter: ROUTINE_GEOID:PARAM:PARAM_NAME
```

Пример:
```
HR.PROC_A:VAR:V_COUNT        ← DaliVariable
HR.PROC_A:PARAM:P_ORDER_ID   ← DaliParameter
```

### 3.9 Constraints

```
PK:     TABLE_GEOID:PK:CONSTRAINT_NAME
        TABLE_GEOID:PK:PK_HASH         (если имя не указано)

FK:     TABLE_GEOID:FK:CONSTRAINT_NAME
        TABLE_GEOID:FK:FK_HASH

UNIQUE: TABLE_GEOID:UNIQUE:CONSTRAINT_NAME
        TABLE_GEOID:UNIQUE:UQ_HASH_OF_COLUMNS

CHECK:  TABLE_GEOID:CHECK:CONSTRAINT_NAME
        TABLE_GEOID:CHECK:HEX_OF_EXPR_HASHCODE
```

Для UNIQUE без имени: hash = `Integer.toHexString(String.join(",", sortedCols).hashCode())`
Для CHECK без имени: hash = `Integer.toHexString(checkExpression.hashCode())`

---

## 4. Коллизии и дедупликация

### 4.1 Одна колонка в нескольких сессиях

```
Session 1 → INSERT DaliColumn(geoid="HR.ORDERS.STATUS") → RID=#22:15
Session 2 → INSERT DaliColumn(geoid="HR.ORDERS.STATUS") → DuplicatedKeyException
                    → catch → pool.putColRid("HR.ORDERS.STATUS", "#22:15")
                    → reuse RID
```

Механизм `CanonicalPool` обеспечивает переиспользование RID между сессиями.

### 4.2 Upgrade data_source

При коллизии DaliColumn из DDL vs DML:
```java
// Если колонка уже существует как "reconstructed" (из SELECT),
// но теперь встречается в CREATE TABLE → upgrade:
UPDATE DaliColumn SET data_source='master', is_pk=?, is_fk=?, ...
WHERE db_name=? AND column_geoid=?
```

### 4.3 DaliRecordField — dedup

```java
// В RemoteWriter:
String fieldGeoid = recGeoid + ":FIELD:" + fieldName.toUpperCase();
if (ridCache.recordFields.containsKey(fieldGeoid)) continue; // skip duplicate
// INSERT + ridCache.recordFields.put(fieldGeoid, newRid)

// В JsonlBatchBuilder:
if (vertexIds.contains(fieldGeoid)) { writeStats.markDuplicate("DaliRecordField"); return; }
vertexIds.add(fieldGeoid); // register
```

### 4.4 In-batch dedup

`JsonlBatchBuilder.vertexIds: Set<String>` — хранит все geoid'ы текущего батча.
При повторном `appendVertex()` с тем же geoid → `markDuplicate()` + skip.

---

## 5. Специальные конструкции

### 5.1 CTE (Common Table Expression)

```
CTE alias: PARENT_STMT_GEOID:CTE:CTE_NAME
```

CTE не создаёт отдельную DaliTable — резолвится в scope как виртуальная таблица.
Geoid используется только внутри NameResolver для scope resolution.

### 5.2 Subquery

```
Subquery: PARENT_STMT_GEOID:SUB:SEQ
```

Создаётся как отдельный DaliStatement с `parent_statement_geoid = PARENT_STMT_GEOID`
и ребром `CHILD_OF`.

### 5.3 Inline function (WITH FUNCTION)

```
Inline function: OUTER_STMT_GEOID:INLINE_FUNC:FUNC_NAME
```

Создаётся как временный DaliRoutine, видимый только внутри outer statement scope.

### 5.4 Remote table (Database Link)

```
Table с dblink: SCHEMA.TABLE@DBLINK_NAME
Пример: HR.ORDERS@PROD_DB
```

`@` — часть geoid; поле `dblink` также записывается отдельно на вершину DaliTable.

### 5.5 JSON / XML virtual sources

```
DaliJsonSource: STMT_GEOID:JSON:ALIAS
DaliXmlSource:  STMT_GEOID:XML:ALIAS
```

### 5.6 LATERAL / APPLY subquery

```
Lateral scope: STMT_GEOID помечается в ScopeManager как lateral,
               outer stmt geoid хранится в lateralOuter map.
```

Не меняет формат geoid; меняет стратегию резолюции имён в NameResolver.

---

## 6. Связь geoid → RID в ArcadeDB

### 6.1 RidCache в RemoteWriter

```java
static class RidCache {
    Map<String, String> schemas     = new HashMap<>(); // geoid → #cluster:pos
    Map<String, String> tables      = new HashMap<>();
    Map<String, String> columns     = new HashMap<>();
    Map<String, String> routines    = new HashMap<>();
    Map<String, String> statements  = new HashMap<>();
    Map<String, String> ddlStatements = new HashMap<>();
    Map<String, String> records     = new HashMap<>();
    Map<String, String> recordFields = new HashMap<>(); // Sprint 2
    Map<String, String> variables   = new HashMap<>();  // Sprint 2
    Map<String, String> parameters  = new HashMap<>();  // Sprint 2
    Map<String, String> constraints = new HashMap<>();
    Map<String, String> outputColumns = new HashMap<>();
    Map<String, String> atoms       = new HashMap<>();
    Map<String, String> joins       = new HashMap<>();
}
```

Каждый INSERT возвращает RID (`@rid`) → сохраняется в соответствующую map.
При создании рёбер: `edgeByRid(type, fromRid, toRid, sid)`.

### 6.2 CanonicalPool (namespace mode)

Аналогичная структура, но персистентная между файлами одной сессии:
```java
pool.hasSchemaRid(geoid)          // проверка перед INSERT
pool.putSchemaRid(geoid, rid)     // регистрация после INSERT
pool.getSchemaRid(geoid)          // получение для edge creation
```

### 6.3 JsonlBatchBuilder — временные ID

В REMOTE_BATCH режиме RID'ы неизвестны до отправки батча.
`@id` в NDJSON = geoid вершины (временный идентификатор внутри батча).
`@from` / `@to` рёбер:
- Если endpoint в текущем батче → `@from = geoid`
- Если endpoint уже в БД (canonical) → `@from = "#cluster:pos"` (реальный RID)
- Если endpoint не найден → ребро dropped, `writeStats.droppedEdges++`

---

## 7. Уникальные индексы

Все geoid-поля защищены `UNIQUE_HASH_INDEX` в ArcadeDB:

| Vertex Type | Индекс | Поля |
|-------------|--------|------|
| `DaliSchema` | `DaliSchema[db_name,schema_geoid]` | `(db_name, schema_geoid)` |
| `DaliTable` | `DaliTable[db_name,table_geoid]` | `(db_name, table_geoid)` |
| `DaliColumn` | `DaliColumn[db_name,column_geoid]` | `(db_name, column_geoid)` |
| `DaliRecord` | `DaliRecord[record_geoid]` | `(record_geoid)` |
| `DaliRecordField` | `DaliRecordField[field_geoid]` | `(field_geoid)` |
| `DaliRoutine` | `DaliRoutine[routine_geoid]` | `(routine_geoid)` |
| `DaliVariable` | `DaliVariable[var_geoid]` | `(var_geoid)` |
| `DaliParameter` | `DaliParameter[param_geoid]` | `(param_geoid)` |

Инициализация: `RemoteSchemaCommands.java` → `SchemaInitializer.remoteSchemaCommands()`.

---

## История изменений

| Дата | Что |
|------|-----|
| 2026-04-14 | v1.0 — начальная версия, Sprint 2 (PL/SQL lineage gaps) |
