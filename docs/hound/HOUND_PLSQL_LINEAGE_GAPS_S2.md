# PL/SQL Lineage Gaps — Sprint 2 Implementation Spec

> Детальная спецификация реализуемых конструкций PL/SQL парсинга.
> Формат: алгоритмы парсинга, geoid-вычисление, vertex/edge схема, алгоритм RemoteWriter/JsonlBatchBuilder.
>
> **Статус документа:** ACTIVE | Apr 2026
> **Исключения:** EXECUTE IMMEDIATE (Dynamic SQL), MERGE USING SOURCE.* (S1.BUG-4) — не в этом спринте.

---

## Содержание

| # | ID | Приоритет | Конструкция |
|---|----|-----------|-------------|
| 1 | KI-DDL-1 | 🔴 Critical | ALTER TABLE DDL edges |
| 2 | KI-RETURN-1 | 🔴 Critical | RETURNING INTO + DaliRecordField |
| 3 | KI-005 | 🟠 High | UNIQUE / CHECK constraints |
| 4 | KI-ROWTYPE-1 | 🟠 High | %ROWTYPE / %TYPE variable resolution |
| 5 | KI-PIPE-1 | 🟠 High | PIPE ROW (pipelined functions) |
| 6 | KI-PENDING-1 | 🟠 High | Pending column resolution — pass 3 + pass 4 |
| 7 | KI-INSALL-1 | 🟡 Medium | INSERT ALL / INSERT FIRST |
| 8 | KI-DBMSSQL-1 | 🟡 Medium | DBMS_SQL stub |
| 9 | KI-DBLINK-1 | 🟡 Medium | Database links (@dblink) |
| 10 | KI-JSON-1 | 🟡 Medium | JSON_TABLE / JSON_VALUE / JSON_QUERY |
| 11 | KI-XML-1 | 🟡 Medium | XMLTABLE / XMLQUERY |
| 12 | KI-LATERAL-1 | 🟡 Medium | LATERAL joins |
| 13 | KI-APPLY-1 | 🟡 Medium | CROSS APPLY / OUTER APPLY |
| 14 | KI-WITHFUNC-1 | 🟡 Medium | WITH FUNCTION (inline SQL functions) |
| 15 | KI-FLASHBACK-1 | 🟡 Medium | AS OF TIMESTAMP / SCN |
| 16 | KI-PRAGMA-1 | 🟡 Medium | PRAGMA AUTONOMOUS_TRANSACTION |
| 17 | KI-VARRAY-1 | 🟡 Medium | VARRAY(i) indexed access |
| 18 | KI-CONDCOMP-1 | 🟡 Medium | $IF / $ELSIF conditional compilation |
| 19 | KI-NESTREC-1 | ⚪ Stub | Nested records (multi-level field access) |

---

## 1. KI-DDL-1 — ALTER TABLE DDL edges 🔴

### 1.1 Постановка задачи

**Проблема:** `DaliDDLStatement` создаётся при ALTER TABLE, но рёбра
`DaliDDLModifiesTable` и `DaliDDLModifiesColumn` не создаются ни в
`RemoteWriter` (строки 735–756), ни в `JsonlBatchBuilder` (строки 340–354).

**Цель:**
```
DaliDDLStatement --[DaliDDLModifiesTable]--> DaliTable
DaliDDLStatement --[DaliDDLModifiesColumn]--> DaliColumn  (на каждую изменяемую колонку)
```

### 1.2 Parsing

**Файл:** `PlSqlSemanticListener.java`

```java
@Override
public void enterAlter_table(PlSqlParser.Alter_tableContext ctx) {
    // Существующая логика: base.onDdlStatementEnter(...)
    // Добавить: фиксация изменяемых колонок
}

@Override
public void enterAlter_table_properties(PlSqlParser.Alter_table_propertiesContext ctx) {
    // ADD COLUMN
    if (ctx.add_column_clause() != null) {
        for (var colDef : ctx.add_column_clause().column_definition())
            base.onDdlColumnChange(
                cleanColumnName(colDef.column_name().getText()), "ADD");
    }
    // MODIFY COLUMN
    if (ctx.modify_column_clauses() != null) {
        for (var modCol : ctx.modify_column_clauses().modify_col_properties())
            base.onDdlColumnChange(
                cleanColumnName(modCol.column_name().getText()), "MODIFY");
    }
    // DROP COLUMN
    if (ctx.drop_column_clause() != null) {
        for (var cn : ctx.drop_column_clause().column_name())
            base.onDdlColumnChange(cleanColumnName(cn.getText()), "DROP");
    }
}
```

### 1.3 BaseSemanticListener — накопление

```java
public void onDdlColumnChange(String colName, String operation) {
    var si = currentDdlStatement();
    if (si == null) return;
    String tableGeoid = si.getTargetTableGeoid();
    if (tableGeoid == null) return;
    String colGeoid = tableGeoid + "." + colName.toUpperCase();
    si.addAffectedColumnGeoid(colGeoid, operation);
}
```

**StatementInfo** — новые поля:
```java
// Список (geoid, operation) для DDL-изменений
private final List<Pair<String,String>> affectedColumnGeoids = new ArrayList<>();
public void addAffectedColumnGeoid(String geoid, String op) {
    affectedColumnGeoids.add(new Pair<>(geoid, op));
}
public List<Pair<String,String>> getAffectedColumnGeoids() {
    return Collections.unmodifiableList(affectedColumnGeoids);
}
```

### 1.4 Geoid вычисление

```
DaliDDLStatement geoid: "DDL_" + ROUTINE_GEOID + "." + SEQ

Пример ALTER TABLE HR.ORDERS ADD COLUMN NOTE VARCHAR2(500):
  → DaliDDLStatement geoid = "DDL_HR.PROC_INIT.3"
  → DaliDDLModifiesTable: DDL_HR.PROC_INIT.3 → HR.ORDERS
  → DaliDDLModifiesColumn: DDL_HR.PROC_INIT.3 → HR.ORDERS.NOTE (op=ADD)
```

### 1.5 Новые типы схемы (RemoteSchemaCommands.java)

```java
"CREATE EDGE TYPE DaliDDLModifiesTable IF NOT EXISTS EXTENDS E",
"CREATE EDGE TYPE DaliDDLModifiesColumn IF NOT EXISTS EXTENDS E",
```

### 1.6 Алгоритм RemoteWriter (после строки 756)

```java
// После написания DaliDDLStatement:
String ddlRid = ridCache.ddlStatements.get(ddlGeoid);
if (ddlRid != null) {
    // Ребро к таблице
    for (String tGeoid : s.getTargetTables().keySet()) {
        String tRid = ridCache.tables.get(tGeoid);
        if (tRid != null)
            edgeByRid("DaliDDLModifiesTable", ddlRid, tRid, sid,
                      "operation", s.getDdlOperation());
    }
    // Рёбра к колонкам
    for (var pair : s.getAffectedColumnGeoids()) {
        String cRid = ridCache.columns.get(pair.key());
        if (cRid != null)
            edgeByRid("DaliDDLModifiesColumn", ddlRid, cRid, sid,
                      "operation", pair.value());
    }
}
```

RidCache нуждается в новом map:
```java
Map<String, String> ddlStatements = new HashMap<>(); // ddlGeoid → RID
```

### 1.7 Алгоритм JsonlBatchBuilder (после строки 354)

```java
// После appendVertex DaliDDLStatement:
for (String tGeoid : si.getTargetTables().keySet()) {
    b.appendEdge("DaliDDLModifiesTable", ddlGeoid, tGeoid,
                 mapOf("session_id", sid, "operation", si.getDdlOperation()));
}
for (var pair : si.getAffectedColumnGeoids()) {
    b.appendEdge("DaliDDLModifiesColumn", ddlGeoid, pair.key(),
                 mapOf("session_id", sid, "operation", pair.value()));
}
```

---

## 2. KI-RETURN-1 — RETURNING INTO + DaliRecordField 🔴

### 2.1 Постановка задачи

**Проблема:**
- `DaliRecordField` пишется в `RemoteWriter` (строки 810–827), но:
  - **Отсутствует** в `JsonlBatchBuilder` (строки 435–446)
  - **Отсутствует** как тип в `RemoteSchemaCommands`
  - **Отсутствует** dedup-проверка (нет `ridCache.recordFields.containsKey()`)
- `RETURNING INTO` clause не обрабатывается вовсе — нет listener'а, нет рёбер

**Цель:**
```
UPDATE t RETURNING col INTO :v_scalar    → STMT --[RETURNS_INTO]--> DaliVariable
UPDATE t RETURNING col INTO l_rec.fld   → STMT --[RETURNS_INTO]--> DaliRecordField
UPDATE t RETURNING col INTO p_out       → STMT --[RETURNS_INTO]--> DaliParameter
UPDATE t RETURNING col BULK COLLECT INTO l_tab
                                        → STMT --[RETURNS_INTO]--> DaliRecord
                    DaliRecord --[HAS_RECORD_FIELD]--> DaliRecordField (на каждое поле)
```

### 2.2 Шаг 1 — RecordInfo.java — обогатить FieldInfo

**Текущее состояние:** `List<String> fields` (только имена)

**Новая модель:**
```java
public record FieldInfo(
    String name,                // UPPER_CASE
    String dataType,            // nullable (VARCHAR2, NUMBER, ...)
    int ordinalPosition,        // 1-based
    String sourceColumnGeoid    // nullable — для %TYPE / %ROWTYPE (HR.ORDERS.STATUS)
) {}

// В RecordInfo:
private final List<FieldInfo> fieldInfos = new ArrayList<>();

// Новый метод:
public void addField(String name, String dataType, int ordinal, String srcColGeoid) {
    fieldInfos.add(new FieldInfo(name.toUpperCase(), dataType, ordinal, srcColGeoid));
}

// Backward-compat (для G6/G8 которые вызывают addField(String)):
public void addField(String name) {
    fieldInfos.add(new FieldInfo(name.toUpperCase(), null,
                                 fieldInfos.size() + 1, null));
}

public List<FieldInfo> getFieldInfos() {
    return Collections.unmodifiableList(fieldInfos);
}
```

### 2.3 Шаг 2 — RemoteSchemaCommands.java

```java
"CREATE VERTEX TYPE DaliRecordField IF NOT EXISTS",
"CREATE INDEX ON DaliRecordField (field_geoid) UNIQUE_HASH_INDEX",
"CREATE EDGE TYPE HAS_RECORD_FIELD IF NOT EXISTS EXTENDS E",
"CREATE EDGE TYPE RETURNS_INTO IF NOT EXISTS EXTENDS E",
```

### 2.4 Шаг 3 — PlSqlSemanticListener.java — listener

```java
@Override
public void enterReturning_clause(PlSqlParser.Returning_clauseContext ctx) {
    if (ctx == null) return;
    String stmtGeoid = base.engine.getScopeManager().currentStatement();
    if (stmtGeoid == null) return;

    // Собираем выражения из RETURNING expr1, expr2
    List<String> retExprs = new ArrayList<>();
    if (ctx.expressions() != null)
        for (var e : ctx.expressions().expression())
            retExprs.add(cleanColumnName(e.getText()).toUpperCase());

    // Обрабатываем INTO targets
    if (ctx.bind_variables() != null) {
        for (var bv : ctx.bind_variables().bind_variable()) {
            String varName = bv.getText().replace(":", "").toUpperCase();
            base.onReturningTarget(varName, retExprs, stmtGeoid);
        }
    }
}
```

### 2.5 Шаг 4 — BaseSemanticListener.java — классификация target

```java
public enum ReturningTargetKind { VARIABLE, PARAMETER, RECORD_FIELD, RECORD }

public void onReturningTarget(String varName, List<String> retExprs, String stmtGeoid) {
    String routineGeoid = currentRoutine();
    ReturningTargetKind kind = classifyReturningTarget(varName, routineGeoid);

    String targetGeoid = switch (kind) {
        case RECORD_FIELD -> {
            // "L_REC.FIELD_NAME" → split on '.'
            String[] p = varName.split("\\.", 2);
            yield routineGeoid + ":RECORD:" + p[0] + ":FIELD:" + p[1];
        }
        case PARAMETER -> routineGeoid + ":PARAM:" + varName;
        case RECORD    -> routineGeoid + ":RECORD:" + varName;
        case VARIABLE  -> routineGeoid + ":VAR:"   + varName;
    };

    engine.getBuilder().addReturningTarget(stmtGeoid, kind.name(), targetGeoid, retExprs);
}

private ReturningTargetKind classifyReturningTarget(String varName, String routineGeoid) {
    // 1. Содержит '.' → поле записи: L_REC.FIELD
    if (varName.contains(".")) return ReturningTargetKind.RECORD_FIELD;

    // 2. Проверяем — это параметр routine?
    var ri = engine.getBuilder().getRoutines().get(routineGeoid);
    if (ri != null && ri.hasParameter(varName)) return ReturningTargetKind.PARAMETER;

    // 3. Это record-переменная?
    String recGeoid = routineGeoid + ":RECORD:" + varName;
    if (engine.getBuilder().getRecords().containsKey(recGeoid))
        return ReturningTargetKind.RECORD;

    // 4. По умолчанию — обычная переменная
    return ReturningTargetKind.VARIABLE;
}
```

### 2.6 StatementInfo — новые поля

```java
// ReturningTarget: kind = VARIABLE/PARAMETER/RECORD_FIELD/RECORD
public record ReturningTarget(
    String kind,          // "VARIABLE", "PARAMETER", "RECORD_FIELD", "RECORD"
    String targetGeoid,   // geoid целевой вершины
    List<String> expressions  // список выражений из RETURNING clause
) {}

private boolean hasReturning = false;
private final List<ReturningTarget> returningTargets = new ArrayList<>();

public void addReturningTarget(String kind, String geoid, List<String> exprs) {
    hasReturning = true;
    returningTargets.add(new ReturningTarget(kind, geoid, List.copyOf(exprs)));
}

public List<ReturningTarget> getReturningTargets() {
    return Collections.unmodifiableList(returningTargets);
}
```

### 2.7 Шаг 5 — JsonlBatchBuilder.java — DaliRecordField

После строки 446 (DaliRecord vertex):
```java
// Поля записи
for (FieldInfo fi : rec.getFieldInfos()) {
    String fieldGeoid = rec.getGeoid() + ":FIELD:" + fi.name();
    if (vertexIds.contains(fieldGeoid)) {
        writeStats.markDuplicate("DaliRecordField");
        continue;
    }
    b.appendVertex("DaliRecordField", fieldGeoid, mapOf(
        "session_id",          sid,
        "field_geoid",         fieldGeoid,
        "field_name",          fi.name(),
        "data_type",           fi.dataType(),
        "ordinal_position",    fi.ordinalPosition(),
        "record_geoid",        rec.getGeoid(),
        "source_column_geoid", fi.sourceColumnGeoid()  // nullable
    ));
}
// Ребра HAS_RECORD_FIELD
for (FieldInfo fi : rec.getFieldInfos()) {
    String fieldGeoid = rec.getGeoid() + ":FIELD:" + fi.name();
    b.appendEdge("HAS_RECORD_FIELD", rec.getGeoid(), fieldGeoid,
                 mapOf("session_id", sid));
}
```

Рёбра RETURNS_INTO (после STMT vertices):
```java
for (var rt : si.getReturningTargets()) {
    b.appendEdge("RETURNS_INTO", stmtGeoid, rt.targetGeoid(), mapOf(
        "session_id",       sid,
        "returning_exprs",  String.join(",", rt.expressions())
    ));
}
```

### 2.8 Шаг 6 — RemoteWriter.java — dedup DaliRecordField

Заменить существующий loop (строки 810–827):
```java
for (FieldInfo fi : rec.getFieldInfos()) {
    String fieldGeoid = rec.getGeoid() + ":FIELD:" + fi.name();
    if (ridCache.recordFields.containsKey(fieldGeoid)) continue; // dedup

    String rfRid = rcmd("""
        INSERT INTO DaliRecordField SET
          session_id=?, field_geoid=?, field_name=?,
          data_type=?, ordinal_position=?, record_geoid=?,
          source_column_geoid=?
        """,
        sid, fieldGeoid, fi.name(),
        fi.dataType(), fi.ordinalPosition(), rec.getGeoid(),
        fi.sourceColumnGeoid());

    ridCache.recordFields.put(fieldGeoid, rfRid);
    edgeByRid("HAS_RECORD_FIELD", recRid, rfRid, sid);
}
```

Рёбра RETURNS_INTO (добавить после записи statements):
```java
for (var rt : s.getReturningTargets()) {
    String targetRid = switch (rt.kind()) {
        case "VARIABLE"     -> ridCache.variables.get(rt.targetGeoid());
        case "PARAMETER"    -> ridCache.parameters.get(rt.targetGeoid());
        case "RECORD_FIELD" -> ridCache.recordFields.get(rt.targetGeoid());
        case "RECORD"       -> ridCache.records.get(rt.targetGeoid());
        default -> null;
    };
    if (targetRid != null)
        edgeByRid("RETURNS_INTO", stmtRid, targetRid, sid,
                  "returning_exprs", String.join(",", rt.expressions()));
}
```

RidCache — новые maps:
```java
Map<String, String> variables  = new HashMap<>();  // varGeoid → RID
Map<String, String> parameters = new HashMap<>();  // paramGeoid → RID
Map<String, String> ddlStatements = new HashMap<>(); // ddlGeoid → RID
```

### 2.9 Порядок вершин в JsonlBatchBuilder (обновлённая таблица)

| # | Тип | Примечание |
|---|-----|------------|
| 14 | `DaliRecord` | G6: BULK COLLECT |
| **14a** | **`DaliRecordField`** | **Sprint 2: поля DaliRecord** |
| 15 | `DaliAtom` | |

---

## 3. KI-005 — UNIQUE / CHECK constraints 🟠

### 3.1 Parsing

**Файл:** `PlSqlSemanticListener.java`, строка 1604

```java
// enterOut_of_line_constraint — добавить ветки:
if (ctx.UNIQUE() != null) {
    List<String> cols = ctx.column_name().stream()
        .map(cn -> cleanColumnName(cn.getText()).toUpperCase())
        .toList();
    String name = ctx.constraint_name() != null
        ? cleanIdentifier(ctx.constraint_name().getText()) : null;
    base.onUniqueConstraint(name, cols);
}
if (ctx.CHECK() != null && ctx.condition() != null) {
    String name = ctx.constraint_name() != null
        ? cleanIdentifier(ctx.constraint_name().getText()) : null;
    base.onCheckConstraint(name, ctx.condition().getText());
}
```

Аналогично — `enterInline_constraint` для inline UNIQUE/CHECK в column definition.

### 3.2 Geoid вычисление

```
UNIQUE (с именем):  TABLE_GEOID + ":UNIQUE:" + NAME
UNIQUE (без имени): TABLE_GEOID + ":UNIQUE:" + toHex(hash(join(",", sortedCols)))

CHECK (с именем):  TABLE_GEOID + ":CHECK:" + NAME
CHECK (без имени): TABLE_GEOID + ":CHECK:" + toHex(hash(checkExpression))
```

```java
public static String buildUniqueGeoid(String tableGeoid, String name, List<String> cols) {
    if (name != null && !name.isBlank()) return tableGeoid + ":UNIQUE:" + name.toUpperCase();
    String colHash = Integer.toHexString(String.join(",",
        cols.stream().sorted().toList()).hashCode());
    return tableGeoid + ":UNIQUE:" + colHash;
}

public static String buildCheckGeoid(String tableGeoid, String name, String expr) {
    if (name != null && !name.isBlank()) return tableGeoid + ":CHECK:" + name.toUpperCase();
    return tableGeoid + ":CHECK:" + Integer.toHexString(expr.hashCode());
}
```

### 3.3 Vertex / Edge схема

```
DaliTable --[HAS_UNIQUE_KEY]--> DaliUniqueConstraint
DaliUniqueConstraint --[IS_UNIQUE_COLUMN]--> DaliColumn  (на каждую колонку в UNIQUE)

DaliTable --[HAS_CHECK]--> DaliCheckConstraint
DaliCheckConstraint.check_expression = "salary > 0"
```

Новые типы в `RemoteSchemaCommands`:
```java
"CREATE VERTEX TYPE DaliUniqueConstraint IF NOT EXISTS",
"CREATE VERTEX TYPE DaliCheckConstraint IF NOT EXISTS",
"CREATE EDGE TYPE HAS_UNIQUE_KEY IF NOT EXISTS EXTENDS E",
"CREATE EDGE TYPE IS_UNIQUE_COLUMN IF NOT EXISTS EXTENDS E",
"CREATE EDGE TYPE HAS_CHECK IF NOT EXISTS EXTENDS E",
```

### 3.4 BaseSemanticListener

```java
public void onUniqueConstraint(String name, List<String> cols) {
    String tableGeoid = currentDdlTableGeoid();
    if (tableGeoid == null) return;
    String geoid = ConstraintInfo.buildUniqueGeoid(tableGeoid, name, cols);
    engine.getBuilder().addUniqueConstraint(geoid, name, tableGeoid, cols);
}

public void onCheckConstraint(String name, String expr) {
    String tableGeoid = currentDdlTableGeoid();
    if (tableGeoid == null) return;
    String geoid = ConstraintInfo.buildCheckGeoid(tableGeoid, name, expr);
    engine.getBuilder().addCheckConstraint(geoid, name, tableGeoid, expr);
}
```

---

## 4. KI-ROWTYPE-1 — %ROWTYPE / %TYPE 🟠

### 4.1 Постановка задачи

```sql
DECLARE
  l_row HR.ORDERS%ROWTYPE;     -- все колонки таблицы ORDERS
  l_id  HR.ORDERS.ORDER_ID%TYPE; -- тип одной колонки
BEGIN
  SELECT * INTO l_row FROM HR.ORDERS WHERE order_id = p_id;
  RETURNING col INTO l_id;
END;
```

**Цель:**
- `l_row` → `DaliRecord` с `DaliRecordField` для каждой колонки ORDERS
- `l_id` → `DaliVariable` с `source_column_geoid = HR.ORDERS.ORDER_ID`

### 4.2 Parsing

```java
// В enterVariable_declaration:
String rawType = /* тип из grammar */;
String upper = rawType != null ? rawType.toUpperCase() : "";

if (upper.endsWith("%ROWTYPE")) {
    String tableRef = upper.replace("%ROWTYPE", "").trim();
    base.onRowtypeVariable(varName, tableRef);
} else if (upper.endsWith("%TYPE")) {
    String colRef = upper.replace("%TYPE", "").trim();
    base.onPercentTypeVariable(varName, colRef);
} else {
    base.onRoutineVariable(varName, rawType);
}
```

### 4.3 BaseSemanticListener — обработчики

```java
public void onRowtypeVariable(String varName, String tableRef) {
    String routineGeoid = currentRoutine();
    String recGeoid = routineGeoid + ":RECORD:" + varName.toUpperCase();
    String tableGeoid = resolveTableGeoid(tableRef); // через NameResolver

    RecordInfo rec = new RecordInfo(recGeoid, varName.toUpperCase());
    // Поля будут заполнены в post-walk Phase (pendingRowtypes)
    engine.getBuilder().addRecord(recGeoid, rec);
    engine.addPendingRowtype(recGeoid, tableGeoid);
}

public void onPercentTypeVariable(String varName, String colRef) {
    String routineGeoid = currentRoutine();
    String varGeoid = routineGeoid + ":VAR:" + varName.toUpperCase();
    String colGeoid = resolveColumnGeoid(colRef); // SCHEMA.TABLE.COL

    VariableInfo vi = new VariableInfo(varGeoid, varName.toUpperCase(),
                                       null, colGeoid /* sourceColumnGeoid */);
    engine.getBuilder().addVariable(routineGeoid, vi);
}
```

### 4.4 Post-walk resolution (UniversalSemanticEngine)

После pass 2 (Level 2 resolvePendingColumns), выполняется:

```java
// Шаг: заполнение %ROWTYPE records полями из DaliColumn
for (PendingRowtype pr : pendingRowtypes) {
    RecordInfo rec = builder.getRecords().get(pr.recGeoid());
    if (rec == null) continue;
    String tableGeoid = pr.tableGeoid();

    builder.getColumns().values().stream()
        .filter(c -> tableGeoid.equals(c.getTableGeoid()))
        .sorted(Comparator.comparingInt(ColumnInfo::getOrdinalPosition))
        .forEach(col -> rec.addField(
            col.getName(),
            col.getDataType(),
            col.getOrdinalPosition(),
            col.getGeoid() // sourceColumnGeoid
        ));
}
```

---

## 5. KI-PIPE-1 — PIPE ROW 🟠

### 5.1 Постановка задачи

```sql
CREATE OR REPLACE FUNCTION get_orders RETURN SYS_REFCURSOR PIPELINED IS
BEGIN
  FOR r IN (SELECT * FROM orders) LOOP
    PIPE ROW(r);  -- ← PIPE ROW statement
  END LOOP;
END;

-- Caller:
SELECT * FROM TABLE(get_orders()); -- ← pipelined function call
```

**Цель:**
```
DaliRoutine(get_orders, pipelined=true)
  --[PIPES_FROM]--> DaliStatement(SELECT FROM orders)

DaliStatement(SELECT FROM TABLE(get_orders()))
  --[READS_PIPELINED]--> DaliRoutine(get_orders)
```

### 5.2 Parsing PIPE ROW

```java
@Override
public void enterPipe_row_statement(PlSqlParser.Pipe_row_statementContext ctx) {
    if (ctx == null) return;
    String routineGeoid = base.currentRoutine();
    if (routineGeoid == null) return;

    // Помечаем routine как pipelined
    var ri = base.engine.getBuilder().getRoutines().get(routineGeoid);
    if (ri != null) ri.setPipelined(true);

    // Фиксируем аргумент (имя записи/коллекции)
    if (ctx.expression() != null) {
        String exprText = ctx.expression().getText().toUpperCase();
        base.onPipeRowExpression(exprText, routineGeoid);
    }
}
```

### 5.3 Parsing TABLE() cast (caller side)

```java
@Override
public void enterTable_collection_expression(
        PlSqlParser.Table_collection_expressionContext ctx) {
    if (ctx == null || ctx.expression() == null) return;
    String funcRef = ctx.expression().getText().toUpperCase();
    String funcName = extractFunctionName(funcRef); // без параметров

    // Пытаемся резолвить funcName → RoutineInfo
    var ri = engine.getBuilder().findRoutineByName(funcName);
    if (ri != null && ri.isPipelined()) {
        String stmtGeoid = base.engine.getScopeManager().currentStatement();
        engine.getBuilder().addLineageEdge(
            stmtGeoid, ri.getGeoid(), "READS_PIPELINED");
    } else {
        // Неизвестная функция — ребро READS_FROM_TABLE_FUNC
        engine.getBuilder().addLineageEdge(
            stmtGeoid, funcName, "READS_FROM_TABLE_FUNC");
    }
}
```

### 5.4 RoutineInfo — новое поле

```java
private boolean pipelined = false;
public boolean isPipelined() { return pipelined; }
public void setPipelined(boolean p) { pipelined = p; }
```

### 5.5 Новые edge types в RemoteSchemaCommands

```java
"CREATE EDGE TYPE PIPES_FROM IF NOT EXISTS EXTENDS E",
"CREATE EDGE TYPE READS_PIPELINED IF NOT EXISTS EXTENDS E",
"CREATE EDGE TYPE READS_FROM_TABLE_FUNC IF NOT EXISTS EXTENDS E",
```

---

## 6. KI-PENDING-1 — Pending column resolution 🟠

### 6.1 Текущее состояние

После parse-walk ~20–30% column references остаются в `pendingColumns`:
- Level 1: `resolvePartialPendingForStatement()` — разрешает в открытом scope
- Level 2: `resolvePendingColumns()` — post-walk, exact match по geoid

### 6.2 Pass 3 — Parent chain, depth = 1

Назначение: разрешить correlated subquery references и CTE column aliases.

```java
private String resolveViaParent(String columnRef, String stmtGeoid) {
    var si = builder.getStatements().get(stmtGeoid);
    if (si == null || si.getParentStatementGeoid() == null) return null;

    String parentGeoid = si.getParentStatementGeoid();
    var parentSi = builder.getStatements().get(parentGeoid);
    if (parentSi == null) return null;

    // 1. Проверяем output column aliases родителя (для CTE)
    for (var outCol : parentSi.getOutputColumns()) {
        if (columnRef.equalsIgnoreCase(outCol.getAlias()))
            return parentGeoid; // stmtGeoid источника
    }

    // 2. Проверяем source tables родителя (correlated subquery)
    for (String srcGeoid : parentSi.getSourceTableGeoids()) {
        if (nameResolver.tableHasColumn(srcGeoid, columnRef))
            return srcGeoid;
    }

    return null; // depth = 1, не рекурсируем выше
}
```

Правило **depth = 1**: SQL стандарт — correlated subquery может обращаться
только к прямому родителю, не к дедушке.

### 6.3 Pass 4 — Single-table fuzzy (quality = LOW)

Назначение: при единственном источнике в FROM — атрибуируем любую ref к нему.

```java
pendingColumns.removeIf(p -> {
    var si = builder.getStatements().get(p.get("stmt"));
    if (si == null) return false;
    List<String> sources = si.getSourceTableGeoids();
    if (sources.size() != 1) return false; // только при одном источнике

    String srcGeoid = sources.get(0);
    String colName = p.get("ref").contains(".")
        ? p.get("ref").substring(p.get("ref").lastIndexOf('.') + 1)
        : p.get("ref");

    // Создаём колонку как reconstructed если не существует
    builder.ensureColumn(srcGeoid, colName, null);

    // Помечаем как low quality в resolution log
    atomProcessor.logResolution(p.get("ref"), srcGeoid, "FUZZY_SINGLE_TABLE", "LOW");
    return true;
});
```

### 6.4 Итоговый порядок pass

```
Level 1:  resolvePartialPendingForStatement()
          ↑ выполняется в открытом scope во время walk

Level 2:  resolvePendingColumns() — post-walk, exact match
          Стратегии 1-8 NameResolver (exact geoid, alias, table name, CTE, subquery, ...)

Pass 3:   resolveViaParent() — parent chain, depth = 1
          Цель: correlated subqueries, CTE aliases

Pass 4:   single-table fuzzy — quality=LOW
          Цель: оставшиеся refs при единственном source table
```

---

## 7. KI-INSALL-1 — INSERT ALL / INSERT FIRST 🟡

### 7.1 Parsing

```java
// Actual implementation — simpler than planned.
// Insert_statementContext already has multi_table_insert() method.
@Override
public void enterInsert_statement(PlSqlParser.Insert_statementContext ctx) {
    base.onDmlTargetEnter();
    // KI-INSALL-1: detect INSERT ALL / INSERT FIRST via multi_table_insert child
    String stmtType = ctx.multi_table_insert() != null ? "INSERT_MULTI" : "INSERT";
    base.onStatementEnter(stmtType, extract(ctx), getStartLine(ctx), getEndLine(ctx));
}
```

### 7.2 Структура YGG

```
DaliStatement(INSERT_MULTI)
  ├──[READS_FROM]──> DaliTable (FROM source)
  ├──[WRITES_TO]──> DaliTable(t1)  ← зарегистрирована через general_table_ref
  └──[WRITES_TO]──> DaliTable(t2)  ← зарегистрирована через general_table_ref

Примечание: child INSERT statements НЕ создаются.
Все target tables INSERT ALL регистрируются на один общий DaliStatement через
механизм general_table_ref + in_dml_target=true (стандартный путь).
```

> ⚠ Решение Q31: child INSERT statements отклонены — существующий механизм general_table_ref уже регистрирует все target tables на текущий statement.

---

## 8. KI-DBMSSQL-1 — DBMS_SQL stub 🟡

```java
// В classifyAtom() AtomProcessor:
if (text.startsWith("DBMS_SQL.")) {
    atomData.put("is_dbms_sql_call", true);
    atomData.put("dbms_sql_method", text.substring(9));
    si.setContainsDynamicSql(true);
}
```

`DaliStatement.contains_dynamic_sql = true` — маркер для аудита.
Дальнейшая обработка (EXECUTE IMMEDIATE) — вне scope Sprint 2.

---

## 9. KI-DBLINK-1 — Database links (@dblink) 🟡

### 9.1 Parsing

```java
// Actual implementation — @DBLINK stripped transparently in cleanIdentifier().
// ParsedTableRef record NOT implemented.
public static String cleanIdentifier(String name) {
    if (name == null) return null;
    String clean = name.replaceAll("[\"'`\\[\\]]", "").trim().toUpperCase();
    int atIdx = clean.indexOf('@');
    if (atIdx > 0) clean = clean.substring(0, atIdx); // strip @DBLINK suffix
    return clean;
}
```

### 9.2 Geoid

```
DaliTable с dblink: SCHEMA.TABLE@DBLINK_NAME
Пример: HR.ORDERS@PROD_DB
```

### 9.3 Vertex / Edge схема

```
DaliTable.dblink = "PROD_DB"       (новое nullable поле)
DaliTable.data_source = "remote"
Рёбра: стандартные READS_FROM / WRITES_TO — dblink хранится как атрибут вершины
```

### 9.4 Изменения в коде (факт)

- `BaseSemanticListener.cleanIdentifier()`: добавлена обрезка `@DBLINK` суффикса
- `TableInfo.dblink` — **НЕ добавлено** (deferred Sprint 3)
- `StructureAndLineageBuilder.ensureRemoteTable()` — **НЕ добавлено**
- `DaliTable.dblink` свойство в схеме — **НЕ добавлено**

Geoid таблицы с dblink: `SCHEMA.TABLE` (dblink отбрасывается, не хранится).
Полная поддержка remote table с сохранением dblink — Sprint 3.

---

## 10. KI-JSON-1 — JSON_TABLE / JSON_VALUE / JSON_QUERY 🟡

### 10.1 Parsing

```java
@Override
public void enterJson_table_clause(PlSqlParser.Json_table_clauseContext ctx) {
    String alias = base.currentTableAlias();
    base.onJsonTableEnter(alias);
    // Создаём DaliJsonSource: STMT_GEOID:JSON:ALIAS
}

@Override
public void enterJson_column_definition(PlSqlParser.Json_column_definitionContext ctx) {
    String colName = cleanColumnName(ctx.column_name().getText());
    String path = ctx.json_path() != null ? ctx.json_path().getText() : null;
    base.onJsonTableColumn(colName, path);
}
```

### 10.2 Geoid

```
DaliJsonSource: STMT_GEOID:JSON:ALIAS
```

### 10.3 Vertex / Edge схема

```
DaliStatement --[READS_FROM]--> DaliJsonSource
DaliColumn    --[EXTRACTED_FROM_JSON]--> DaliJsonSource  (json_path как свойство)
```

---

## 11. KI-XML-1 — XMLTABLE / XMLQUERY 🟡

Аналогичен KI-JSON-1. Разница: `enterXmltable_clause` / `enterXml_column_definition`.

```
DaliXmlSource: STMT_GEOID:XML:ALIAS
DaliColumn --[EXTRACTED_FROM_XML]--> DaliXmlSource  (xpath_expr как свойство ребра)
```

Новые типы в RemoteSchemaCommands:
```java
"CREATE VERTEX TYPE DaliJsonSource IF NOT EXISTS",
"CREATE VERTEX TYPE DaliXmlSource IF NOT EXISTS",
"CREATE EDGE TYPE EXTRACTED_FROM_JSON IF NOT EXISTS EXTENDS E",
"CREATE EDGE TYPE EXTRACTED_FROM_XML IF NOT EXISTS EXTENDS E",
```

---

## 12. KI-LATERAL-1 — LATERAL joins 🟡

### 12.1 Parsing

```java
// Actual implementation.
// Table_ref_auxContext has no LATERAL() method — LATERAL lives in
// Dml_table_expression_clauseContext inside Table_ref_aux_internal_oneContext.
@Override
public void enterTable_ref_aux_internal_one(
        PlSqlParser.Table_ref_aux_internal_oneContext ctx) {
    if (ctx == null) return;
    PlSqlParser.Dml_table_expression_clauseContext dml =
            ctx.dml_table_expression_clause();
    if (dml != null && dml.LATERAL() != null) {
        String stmtGeoid = base.engine.getScopeManager().currentStatement();
        if (stmtGeoid != null)
            base.engine.getScopeManager().markHasLateral(stmtGeoid);
    }
}
```

### 12.2 ScopeManager — новые методы (факт)

```java
// Actual ScopeManager additions (simplified vs. planned):
private final java.util.Set<String> lateralStatements = new java.util.HashSet<>();

/** Marks statement as containing LATERAL subquery or APPLY join. */
public void markHasLateral(String stmtGeoid) {
    if (stmtGeoid != null) lateralStatements.add(stmtGeoid);
}

/** Returns true if statement has a LATERAL / APPLY join. */
public boolean hasLateral(String stmtGeoid) {
    return stmtGeoid != null && lateralStatements.contains(stmtGeoid);
}

// NOT implemented: registerLateralScope(inner, outer), isLateralScope(), getLateralOuter()
// These require knowing the inner subquery geoid BEFORE it is created — deferred Sprint 3.
```

### 12.3 NameResolver — Стратегия S9 (LATERAL) — DEFERRED Sprint 3

Метаданные `ScopeManager.hasLateral(stmtGeoid)` доступны для NameResolver.
Фактическая интеграция (look up outer scope для unresolved refs) — Sprint 3.
Решение Q32: `markHasLateral` вместо `registerLateralScope` — нельзя получить
inner stmt geoid в момент, когда LATERAL ещё не создал subquery scope.

---

## 13. KI-APPLY-1 — CROSS APPLY / OUTER APPLY 🟡

Текущий `extractJoinType()` возвращает строку, семантика не обрабатывается.

```java
// Actual implementation (in exitJoin_clause, after onJoinComplete):
if (joinType.contains("APPLY")) {
    // KI-APPLY-1: CROSS APPLY / OUTER APPLY — lateral-like correlated join.
    // Mark enclosing statement for future NameResolver lateral strategy.
    String stmtGeoid = base.engine.getScopeManager().currentStatement();
    if (stmtGeoid != null)
        base.engine.getScopeManager().markHasLateral(stmtGeoid);
}
```

---

## 14. KI-WITHFUNC-1 — WITH FUNCTION 🟡

### 14.1 Parsing

```java
// Actual implementation.
// Grammar has no enterWith_function_definition — WITH FUNCTION goes through
// enterFunction_body because With_clauseContext.function_body() holds inline functions.
@Override
public void enterFunction_body(PlSqlParser.Function_bodyContext ctx) {
    String name = ctx.identifier() != null
            ? BaseSemanticListener.cleanIdentifier(ctx.identifier().getText())
            : "UNKNOWN";
    // KI-WITHFUNC-1: detect inline WITH FUNCTION via parent context
    boolean isInlineFunc = ctx.parent instanceof PlSqlParser.With_clauseContext;
    String routineKind = isInlineFunc ? "INLINE_FUNCTION" : "FUNCTION";
    base.onRoutineEnter(name, routineKind, base.currentSchema(), base.currentPackage(), getStartLine(ctx));
    extractParameters(ctx.parameter());
    if (ctx.type_spec() != null) base.onRoutineReturnType(ctx.type_spec().getText());
}
```

### 14.2 Geoid (факт)

`DaliRoutine.geoid = SCHEMA.FUNC_NAME` — стандартный геоид рутины.
`DaliRoutine.routine_type = "INLINE_FUNCTION"` — признак inline.

Планировалось: `OUTER_STMT_GEOID:INLINE_FUNC:FUNC_NAME` — **deferred Sprint 3**.
Причина: сложность создания уникального геоида в момент enterFunction_body,
когда outer statement scope ещё не определён однозначно.

### 14.3 Resolve (факт)

Inline function регистрируется как обычная `DaliRoutine` с `routine_type="INLINE_FUNCTION"`.
При вызове `func()` в SELECT резолвится через стандартный lookup по рутинам.
Приоритизация inline перед глобальными рутинами — **deferred Sprint 3**.
Решение Q33: parent-context check вместо отдельного grammar rule.

---

## 15. KI-FLASHBACK-1 — AS OF TIMESTAMP / SCN 🟡

### 15.1 Parsing

```java
@Override
public void enterFlashback_query_clause(
        PlSqlParser.Flashback_query_clauseContext ctx) {
    String type = ctx.TIMESTAMP() != null ? "TIMESTAMP" : "SCN";
    String expr = ctx.expression() != null ? ctx.expression().getText() : null;
    String stmtGeoid = base.engine.getScopeManager().currentStatement();
    var si = base.engine.getBuilder().getStatements().get(stmtGeoid);
    if (si != null) {
        si.setFlashbackType(type);
        si.setFlashbackExpr(expr);
    }
}
```

### 15.2 YGG

`DaliStatement.flashback_type` ("TIMESTAMP"/"SCN"), `DaliStatement.flashback_expr`.
Ребро `READS_FROM` получает дополнительный атрибут `is_historical = true`.

---

## 16. KI-PRAGMA-1 — PRAGMA AUTONOMOUS_TRANSACTION 🟡

```java
@Override
public void enterPragma_declaration(PlSqlParser.Pragma_declarationContext ctx) {
    if (ctx.AUTONOMOUS_TRANSACTION() != null) {
        var ri = base.engine.getBuilder().getRoutines().get(base.currentRoutine());
        if (ri != null) ri.setAutonomousTransaction(true);
    }
}
```

`DaliRoutine.autonomous_transaction = true` — мета-флаг.
Не меняет топологию рёбер; используется для аудита и визуализации в YGG.

---

## 17. KI-VARRAY-1 — VARRAY(i) indexed access 🟡

### 17.1 Проблема

```sql
-- VARRAY indexed access: COLLECTION(i).FIELD
-- vs function call: FUNC(arg).FIELD
-- vs cursor field:  CURSOR_REC.FIELD
```

Текущий `classifyAtom()` не различает эти паттерны при `IDENT(expr)` без дополнительного контекста.

### 17.2 Parsing — расширение classifyAtom()

```java
// Actual implementation — stub tag inside existing function_call branch.
// Cannot distinguish VARRAY access from function call at token level without
// checking variable registry (not available in classifyAtom context).

// Inside the function_call branch (IDENT LEFT_PAREN ... ):
atomData.put("is_function_call", true);
atomData.put("function_name", funcName);
// KI-VARRAY-1: tag as candidate — downstream resolution when variable registry available
atomData.put("is_varray_access_candidate", true);
atomData.put("collection_name_candidate", funcName);
```

> ⚠ isCollectionIndexedAccess() с проверкой RoutineInfo.hasRecord() — **deferred Sprint 3**. Sprint 2 ограничивается тегом-кандидатом для диагностики.

---

## 18. KI-CONDCOMP-1 — $IF / $ELSIF conditional compilation 🟡

### 18.1 Проблема

Oracle PL/SQL поддерживает условную компиляцию:
```sql
$IF $$DEBUG $THEN
    DBMS_OUTPUT.PUT_LINE('debug');
$ELSE
    NULL;
$END
```

ANTLR4 lexer не обрабатывает `$IF/$THEN/$END` → parse error или пропуск кода.

### 18.2 Препроцессор

**Новый класс:** `ConditionalCompilationPreprocessor.java`

```java
public class ConditionalCompilationPreprocessor {
    private static final Pattern CC_PATTERN = Pattern.compile(
        "\\$IF\\b.*?\\$THEN|\\$ELSIF\\b.*?\\$THEN|\\$ELSE\\b|\\$END\\b",
        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    /**
     * Стратегия: conservative — включаем ВСЕ ветки (overcapture > undercapture).
     * Директивы $IF/$THEN/$END удаляются, код всех веток сохраняется.
     */
    public String expand(String source) {
        return CC_PATTERN.matcher(source).replaceAll("");
    }
}
```

### 18.3 Интеграция в HoundParserImpl

```java
// Перед ANTLR4 parse:
String preprocessed = new ConditionalCompilationPreprocessor().expand(sourceCode);
lexer.setInputStream(CharStreams.fromString(preprocessed));
```

---

## 19. KI-NESTREC-1 — Nested records ⚪ STUB

**Статус:** Отложено на Sprint 3. Минимальный маркер для отчётности.

### 19.1 Пример проблемы

```sql
DECLARE
  TYPE t_inner IS RECORD (x NUMBER, y VARCHAR2(100));
  TYPE t_outer IS RECORD (id NUMBER, inner_rec t_inner);
  l_data t_outer;
BEGIN
  -- Многоуровневый доступ: l_data.inner_rec.x
  -- chain length = 3 > 1 (IDENT.IDENT.IDENT)
  v_result := l_data.inner_rec.x;  -- ← НЕ обрабатывается
END;
```

`l_data.inner_rec` — `DaliRecordField` с типом `t_inner` (другой record type).
`l_data.inner_rec.x` — поле вложенной записи.
Текущий AtomProcessor обрабатывает только `IDENT.IDENT` (depth = 1).

### 19.2 Stub marker

```java
// В classifyAtom() при chain.size() > 2:
atomData.put("nested_field_stub", true);
atomProcessor.logResolution(text, "UNRESOLVED", "nested_record_depth_gt_1");
// Не создаём ребро — логируем для диагностики
```

---

## Критические файлы (сводка)

| Файл | Изменения Sprint 2 (факт) |
|------|--------------------------|
| `PlSqlSemanticListener.java` | +15 enter/exit handlers (KI-005, KI-ROWTYPE-1, KI-PIPE-1, KI-PRAGMA-1, KI-FLASHBACK-1, KI-INSALL-1, KI-LATERAL-1, KI-APPLY-1, KI-WITHFUNC-1) |
| `BaseSemanticListener.java` | `onUniqueConstraint`, `onCheckConstraint`, `onRowtypeVariable`, `cleanIdentifier` @dblink strip |
| `AtomProcessor.java` | DBMS_SQL stub, VARRAY candidate tag |
| `ScopeManager.java` | `markHasLateral`, `hasLateral` |
| `RecordInfo.java` | `FieldInfo record(name, dataType, ordinalPosition, sourceColumnGeoid)`, `getFieldInfos()` |
| `StatementInfo.java` | `ReturningTarget`, `flashbackType`, `flashbackExpr`, `containsDynamicSql` |
| `RoutineInfo.java` | `isPipelined`, `autonomousTransaction` |
| `UniversalSemanticEngine.java` | Pass 3–5, `pendingRowtypes`, `PendingRowtype` record |
| `RemoteWriter.java` | DDL edges, RETURNS_INTO, DaliRecordField dedup, UQ/CH constraints, is_pipelined |
| `JsonlBatchBuilder.java` | DaliRecordField, DaliUniqueConstraint, DaliCheckConstraint, is_pipelined |
| `RemoteSchemaCommands.java` | DaliUniqueConstraint, DaliCheckConstraint, HAS_UNIQUE_KEY, IS_UNIQUE_COLUMN, HAS_CHECK, HAS_RECORD_FIELD, RETURNS_INTO, DaliDDLModifiesTable, DaliDDLModifiesColumn, PIPES_FROM, READS_PIPELINED + свойства |
| `ConditionalCompilationPreprocessor.java` | **Новый класс** — $IF/$ELSIF preprocessing |
| `HoundParserImpl.java` | Интеграция ConditionalCompilationPreprocessor |
| `YggStatsResource.java` (Dali) | `countAtoms(where)` — bugfix resolved/unresolved stats |

Примечание: `TableInfo.dblink`, `StructureAndLineageBuilder.ensureRemoteTable()` — **NOT implemented** (dblink stripped in cleanIdentifier instead).

---

## Верификация

| # | Тест | Критерий |
|---|------|---------|
| V1 | `ResolutionLogDiagnosticTest` | % resolved ≥ 90% |
| V2 | `SELECT FROM DaliDDLStatement` в YGG | Рёбра `DaliDDLModifiesTable` присутствуют |
| V3 | `SELECT FROM DaliRecordField` | `field_geoid` уникален, нет дублей |
| V4 | `UPDATE t RETURNING col INTO :v` | `DaliVariable` + ребро `RETURNS_INTO` |
| V5 | `UPDATE t RETURNING col INTO l_rec.field` | `DaliRecordField` + ребро `RETURNS_INTO` |
| V6 | `UPDATE t RETURNING col INTO p_out` | `DaliParameter` + ребро `RETURNS_INTO` |
| V7 | `CREATE TABLE t (col1 NUMBER, UNIQUE(col1))` | `DaliUniqueConstraint` + `IS_UNIQUE_COLUMN` |
| V8 | `INSERT ALL INTO t1 ... INTO t2 ...` | statement_type='INSERT_MULTI' + multiple WRITES_TO edges on single DaliStatement |
| V9 | `PIPE ROW` + `TABLE(func())` | `DaliRoutine.pipelined=true` + `READS_PIPELINED` ребро |
| V10 | `SELECT col FROM orders@prod_db` | table registered as ORDERS (dblink stripped in cleanIdentifier, not stored in DaliTable) |

---

## История изменений

| Дата | Что |
|------|-----|
| 2026-04-14 | v1.0 — начальная версия Sprint 2 |
| 2026-04-14 | v2.0 — Sprint 2 РЕАЛИЗОВАНО. Все 14 items закоммичены (c9fa242). Расхождения план↔факт задокументированы в §7,9,12,13,14,17. Bugfix YGG stats (Dali). Sprint 3 backlog: KI-JSON-1, KI-XML-1, KI-NESTREC-1, полная LATERAL/VARRAY резолюция, dblink хранение. |
