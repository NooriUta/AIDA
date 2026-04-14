# Hound — Алгоритм обработки CREATE TABLE

> Актуально для: Sprint Stabilizing Apr 13 2026 (T14-FIX rev.2)  
> Статус: **исправлено + расширено** (isRequired, defaultValue, atom suppression)

---

## 1. Участвующие классы

| Класс | Путь | Роль |
|---|---|---|
| `PlSqlSemanticListener` | `hound/…/dialect/plsql/PlSqlSemanticListener.java` | ANTLR4-листенер; точки входа в parse tree |
| `BaseSemanticListener` | `hound/…/semantic/listener/BaseSemanticListener.java` | Семантическая логика; управляет контекстным словарём `current` |
| `StructureAndLineageBuilder` | `hound/…/semantic/engine/StructureAndLineageBuilder.java` | Строит `Structure`: таблицы, колонки, стейтменты; хранит `ddlTableGeoids` |
| `TableInfo` | `hound/…/semantic/model/TableInfo.java` | Неизменяемая модель таблицы |
| `ColumnInfo` | `hound/…/semantic/model/ColumnInfo.java` | Модель колонки: geoid, dataType, isRequired, defaultValue, ordinalPosition |
| `Structure` | `hound/…/semantic/model/Structure.java` | Результат парсинга: `getTables()`, `getColumns()`, `getDdlTableGeoids()` |
| `WriteHelpers` | `hound/…/storage/WriteHelpers.java` | Статические хелперы: `isMasterTable()`, `isViewTable()`, `isDdl()` |
| `RemoteWriter` | `hound/…/storage/RemoteWriter.java` | Запись в ArcadeDB через `rcmd()` (saveObjects и writeBatch пути) |
| `JsonlBatchBuilder` | `hound/…/storage/JsonlBatchBuilder.java` | Строит NDJSON payload для HTTP Batch endpoint (ad-hoc режим) |

---

## 2. Контекстный словарь `current` — ключи DDL

`current` — это `Map<String, Object>` в `BaseSemanticListener`, живущий на время разбора одного файла.  
Следующие ключи используются эксклюзивно для DDL:

| Ключ | Тип | Устанавливается | Очищается | Назначение |
|---|---|---|---|---|
| `ddl_table_geoid` | `String` | `onCreateTableEnter` / `onAlterTableEnter` | `onCreateTableExit` / `onAlterTableExit` | geoid таблицы-цели DDL; **также служит флагом подавления атомов** в `addAtom()` |
| `ddl_col_ordinal` | `Integer` | `onCreateTableEnter` → начальное значение `0` | `onCreateTableExit` | счётчик позиций колонок для CREATE TABLE; инкрементируется на каждом `column_definition` |
| `statement` | `String` | `initStatement()` внутри `onStatementEnter` | `exitStatement()` | geoid текущего активного SQL-стейтмента; `currentStatement()` читает этот ключ |

> **Примечание для ALTER TABLE**: `ddl_col_ordinal` **не устанавливается** — `addColumn()` использует auto-increment через `t.columnCount()`.

---

## 3. Регистрация таблицы как DDL-defined (ddlTableGeoids)

Вместо сканирования `str.getStatements()` используется выделенный `Set<String> ddlTableGeoids` в `StructureAndLineageBuilder`.

### Путь аналогии с DML

```
DML:  SELECT … FROM CRM.ORDERS  →  processTableReference()
        → builder.ensureTable("CRM.ORDERS")     // попадает в str.getTables()
        → isMasterTable() = false               // нет в ddlTableGeoids
        → data_source = 'reconstructed'         // ✓

DDL:  CREATE TABLE CRM.ORDERS  →  initDdlTable()
        → builder.ensureTableWithType(geoid, …) // попадает в str.getTables()
        → builder.markDdlTable(geoid)           // попадает в ddlTableGeoids
        → isMasterTable() = true                // O(1) Set.contains()
        → data_source = 'master'                // ✓
```

### `initDdlTable()` — единая точка регистрации DDL-таблиц

`BaseSemanticListener.java`:

```java
private String initDdlTable(String tableName, String schema) {
    String geoid = initTable(tableName, null, schema, "TABLE");
    engine.getBuilder().ensureTableWithType(geoid, null, "TABLE");
    engine.getBuilder().markDdlTable(geoid);
    return geoid;
}
```

Вызывается из:
- `onCreateTableEnter(schemaName, tableName, snippet, lineStart, lineEnd)`
- `onAlterTableEnter(tableRef)`

### `isMasterTable()` — O(1) lookup

`WriteHelpers.java`:

```java
static boolean isMasterTable(String tableGeoid, Structure str) {
    if (str == null || tableGeoid == null) return false;
    return str.getDdlTableGeoids().contains(tableGeoid);   // O(1)
}
```

---

## 4. Полный алгоритм CREATE TABLE

### 4.1 Точка входа — ANTLR4

**`PlSqlSemanticListener.enterCreate_table`**

```
ANTLR: enterCreate_table(ctx)
  → schemaName из ctx.schema_name()
  → tableName  из ctx.table_name()
  → base.onCreateTableEnter(schemaName, tableName, snippet, lineStart, lineEnd)
```

### 4.2 Регистрация таблицы

**`BaseSemanticListener.onCreateTableEnter`**

```
onCreateTableEnter(schemaName, tableName, snippet, lineStart, lineEnd)

  1. schema = schemaName ?? currentSchema()

  2. geoid = initDdlTable(tableName, schema)
     ↳ initTable()                             → BaseListener.tables (локальный map; не попадает в str!)
     ↳ builder.ensureTableWithType(geoid, …)   → str.getTables() ✓  (TableInfo добавлена)
     ↳ builder.markDdlTable(geoid)             → ddlTableGeoids.add(geoid) ✓

  3. current.put("ddl_table_geoid", geoid)      // также служит флагом подавления атомов
  4. current.put("ddl_col_ordinal", 0)          // счётчик порядка колонок

  5. onStatementEnter("CREATE_TABLE", snippet, lineStart, lineEnd)
     ↳ builder.addStatement(stmtGeoid, "CREATE_TABLE", …)  → str.getStatements()
     ↳ current.put("statement", stmtGeoid)
```

### 4.3 Регистрация колонок

**`PlSqlSemanticListener.enterColumn_definition`**

```
enterColumn_definition(ctx)

  1. colName   = ctx.column_name().getText()  → cleanColumnName()
  2. dataType  = ctx.datatype()?.getText() ?? ctx.type_name()?.getText()  → toUpperCase()

  3. defaultValue = null
     if ctx.DEFAULT() != null && ctx.expression() != null:
         defaultValue = ctx.expression().getText().toUpperCase()
         // примеры: "'N'", "SYSTIMESTAMP", "USER", "SYS_GUID()"

  4. isRequired = false
     for ic in ctx.inline_constraint():
         if ic.NOT() != null && ic.NULL_() != null:
             isRequired = true; break
     // NOT NULL_ в грамматике → ic.NOT() и ic.NULL_() непусты

  5. base.onDdlColumnDefinition(colName, dataType, isRequired, defaultValue)
```

**`BaseSemanticListener.onDdlColumnDefinition`**

```
onDdlColumnDefinition(columnName, dataType, isRequired, defaultValue)

  1. tableGeoid = current.get("ddl_table_geoid")
     → если null — выход (вызван вне DDL-контекста)

  2. ordinalObj = current.get("ddl_col_ordinal")

  3. if ordinalObj != null:  // CREATE TABLE — явный порядок
         ordinal = ordinalObj + 1
         current.put("ddl_col_ordinal", ordinal)
         builder.addColumnWithOrdinal(tableGeoid, columnName, null, null, ordinal,
                                      dataType, isRequired, defaultValue)
           ↳ columns.computeIfAbsent(geoid, …)
           ↳ t.incrementColumnCount()
           ↳ → ColumnInfo(…, ordinalPosition=ordinal, dataType, isRequired, defaultValue)

  4. else:  // ALTER TABLE — auto-increment
         builder.addColumn(tableGeoid, columnName, null, null, dataType, isRequired, defaultValue)
           ↳ ordinal = t.columnCount() после increment
```

### 4.4 Подавление атомов из DEFAULT-выражений

**`BaseSemanticListener.addAtom`**

```java
// T14: suppress atom processing inside CREATE TABLE / ALTER TABLE column definitions.
// DEFAULT expressions, constraints and other DDL sub-trees must not generate DaliAtom records.
if (current.get("ddl_table_geoid") != null) return;
```

Атомы из `DEFAULT 'N'`, `DEFAULT USER`, `DEFAULT SYSTIMESTAMP` **никогда** не доходят до `engine.onAtom()` → нет ложных `DaliAtom` → нет ребёр к `DaliTable`/`DaliColumn`.

`DaliDDLStatement` является **только регистрационной нодой** (CREATE/ALTER), без атомов в атрибуте.

### 4.5 Выход из контекста

**`PlSqlSemanticListener.exitCreate_table`** → `base.onCreateTableExit()`

```
onCreateTableExit()
  1. current.put("ddl_table_geoid", null)   // снять флаг DDL-контекста
  2. current.put("ddl_col_ordinal", null)
  3. onStatementExit()
```

---

## 5. Алгоритм ALTER TABLE

### 5.1 Особенности порядка вызовов

В `PlSqlSemanticListener.enterAlter_table` вызовы идут в порядке:
```
base.onAlterTableEnter(fullName)          // ДО создания стейтмента
base.onStatementEnter("ALTER_TABLE", ...) // ПОСЛЕ
```

### 5.2 Enter

```
onAlterTableEnter(tableRef)
  1. geoid = initDdlTable(tableRef, currentSchema())
     ↳ ensureTableWithType + markDdlTable   // str.getTables() + ddlTableGeoids
  2. current.put("ddl_table_geoid", geoid)
```

### 5.3 Колонки

Аналогично CREATE TABLE (§4.3), ветка `else` в `onDdlColumnDefinition`.

### 5.4 Exit

```
PlSqlSemanticListener.exitAlter_table:
  base.onAlterTableExit()   → current.put("ddl_table_geoid", null)
  base.onStatementExit()
```

---

## 6. Поля ColumnInfo (T14)

| Поле | Тип | Источник | ArcadeDB свойство |
|---|---|---|---|
| `columnName` | `String` | `ctx.column_name().getText()` | `column_name` |
| `ordinalPosition` | `int` | счётчик `ddl_col_ordinal` (1-based) | `ordinal_position` |
| `dataType` | `String` | `ctx.datatype().getText()` (uppercase) | `data_type` |
| `isRequired` | `boolean` | `ic.NOT() != null && ic.NULL_() != null` | `is_required` |
| `defaultValue` | `String` | `ctx.expression().getText()` (uppercase) | `default_value` |
| `data_source` | `String` | `isMasterTable(tableGeoid, str)` | `data_source` |

**Пример для `CRM.COUNTRIES`:**

| Колонка | dataType | ordinalPosition | isRequired | defaultValue |
|---|---|---|---|---|
| COUNTRY_ID | NUMBER(19) | 1 | true | null |
| COUNTRY_CODE | VARCHAR2(3BYTE) | 2 | true | null |
| IS_EU_MEMBER | VARCHAR2(1BYTE) | 8 | true | `'N'` |
| IS_ACTIVE | VARCHAR2(1BYTE) | 9 | true | `'Y'` |
| CREATED_BY | VARCHAR2(100BYTE) | 10 | true | `USER` |
| CREATED_DATE | TIMESTAMP(6) | 11 | true | `SYSTIMESTAMP` |
| UPDATED_BY | VARCHAR2(100BYTE) | 12 | false | null |
| UPDATED_DATE | TIMESTAMP(6) | 13 | false | null |

---

## 7. Запись в ArcadeDB

### 7.1 DaliColumn INSERT (все 3 пути)

```sql
INSERT INTO DaliColumn SET
  session_id/db_name = ?,
  column_geoid      = ?,
  table_geoid       = ?,
  column_name       = ?,
  expression        = ?,
  alias             = ?,
  is_output         = ?,
  col_order         = ?,
  ordinal_position  = ?,
  used_in_statements= ?,
  data_source       = ?,       -- 'master' / 'reconstructed'
  data_type         = ?,       -- "VARCHAR2(100BYTE)", "NUMBER(19)", null
  is_required       = ?,       -- true/false
  default_value     = ?        -- "'N'", "SYSTIMESTAMP", null
```

### 7.2 DaliTable — определение data_source

```
isMasterTable(geoid, str) = str.getDdlTableGeoids().contains(geoid)
  → true   → data_source = 'master'
  → false  → data_source = 'reconstructed'
```

### 7.3 DaliStatement vs DaliDDLStatement

`CREATE_TABLE` и `ALTER_TABLE` **исключены** из DaliStatement (оба overload JsonlBatchBuilder):

```java
if (isDdl(s.getType())) continue;  // DDL → только DaliDDLStatement
```

`DaliDDLStatement` — только регистрационная нода: тип, snippet, строки. Без атомов.

---

## 8. История дефектов (T14-FIX)

### 8.1 T14-FIX rev.1 — data_source='master' не работал

**Причина**: `onCreateTableEnter()` вызывал только `initTable()` → таблица попадала только в локальный `BaseSemanticListener.tables`, но **не** в `StructureAndLineageBuilder.tables` → нет `TableInfo` → нет `DaliTable`.

**Исправление**: введён `initDdlTable()` — единая точка, вызывает `initTable()` + `ensureTableWithType()` + `markDdlTable()`.

### 8.2 T14-FIX rev.1a — ошибочный подход addTargetTable

Первоначальный фикс добавлял `stmt.addTargetTable(geoid, null)` в `onCreateTableEnter` и `onAlterTableExit`, а `isMasterTable()` сканировал `str.getStatements()` через `.stream().anyMatch()`.

**Отклонено** пользователем: «вся ветка лишняя» — `targetTables` семантически предназначен для DML, не для DDL-регистрации.

**Исправление**: `ddlTableGeoids` — выделенный `Set<String>` в `StructureAndLineageBuilder`. `isMasterTable()` использует `O(1) Set.contains()`.

### 8.3 T14-FIX rev.2 — isRequired, defaultValue, atom suppression

**Добавлено**:
- Поля `isRequired` и `defaultValue` в `ColumnInfo`
- Извлечение из parse tree в `enterColumn_definition` (NotNull через `ic.NOT()/ic.NULL_()`, DEFAULT через `ctx.expression().getText()`)
- Подавление атомов в `addAtom()` при `ddl_table_geoid != null` — DaliDDLStatement остаётся чистой регистрационной нодой

---

## 9. Известные ограничения (KI)

| ID | Описание | Статус |
|---|---|---|
| KI-001 | Рёбра `DaliDDLStatement → DaliTable` и `DaliDDLStatement → DaliColumn` не создаются | Deferred |
| KI-002 | `CREATE VIEW`: разбор внутреннего SELECT и перенос output columns в DaliColumn VIEW не реализован | Deferred |
| KI-003 | `ALTER TABLE` не обновляет существующие DaliColumn (только добавляет новые) | Deferred |
| KI-004 | `dataType` содержит форму без пробелов (e.g. `VARCHAR2(100BYTE)` вместо `VARCHAR2(100 BYTE)`) | KI |

---

## 10. Диаграмма потока данных

```
SQL-файл
  │
  ▼
PlSqlSemanticListener.enterCreate_table()
  │  schemaName, tableName, snippet, lines
  ▼
BaseSemanticListener.onCreateTableEnter()
  ├─ initDdlTable(tableName, schema)
  │    ├─ initTable()                  → BaseListener.tables (local, NOT in str)
  │    ├─ builder.ensureTableWithType() → str.getTables() ✓
  │    └─ builder.markDdlTable()        → ddlTableGeoids ✓
  ├─ current["ddl_table_geoid"] = geoid  ← флаг подавления атомов
  ├─ current["ddl_col_ordinal"] = 0
  └─ onStatementEnter("CREATE_TABLE")    → str.getStatements()
  │
  ▼ (для каждой колонки)
PlSqlSemanticListener.enterColumn_definition()
  │  colName, dataType, defaultValue, isRequired
  ├─ [atoms from DEFAULT expr → addAtom() → early return: ddl_table_geoid != null]
  ▼
BaseSemanticListener.onDdlColumnDefinition()
  ├─ ordinal = ++ddl_col_ordinal
  └─ builder.addColumnWithOrdinal(…, dataType, isRequired, defaultValue)
       → str.getColumns() ✓  (ColumnInfo с полными метаданными)
  │
  ▼
PlSqlSemanticListener.exitCreate_table()
  │
  ▼
BaseSemanticListener.onCreateTableExit()
  ├─ current["ddl_table_geoid"] = null  ← снять флаг
  ├─ current["ddl_col_ordinal"] = null
  └─ onStatementExit()
  │
  ▼
Structure (str)
  ├─ getTables()        → TableInfo(tableName, schema, "TABLE")
  ├─ getColumns()       → ColumnInfo(col, ordinal, dataType, isRequired, defaultValue)
  ├─ getDdlTableGeoids()→ {geoid}   ← для isMasterTable()
  └─ getStatements()    → {CREATE_TABLE:N: StatementInfo(...)}
  │
  ▼
WriteHelpers.isMasterTable(geoid, str)
  └─ str.getDdlTableGeoids().contains(geoid)  → true
  │
  ▼
RemoteWriter / JsonlBatchBuilder
  └─ INSERT DaliTable  (data_source='master', table_type='TABLE')
  └─ INSERT DaliColumn (data_source='master', ordinal=N, data_type=T,
                        is_required=B, default_value=D)
  └─ INSERT DaliDDLStatement (type='CREATE_TABLE')  — только регистрация
```
