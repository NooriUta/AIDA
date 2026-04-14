# G6 / G6-EXT / G8 — Cursor → INSERT VALUES Lineage

**Компонент:** HOUND Semantic Engine  
**Файлы:** `PlSqlSemanticListener.java`, `BaseSemanticListener.java`, `AtomProcessor.java`,
`RecordInfo.java`, `StatementInfo.java`, `ScopeManager.java`,
`StructureAndLineageBuilder.java`, `Structure.java`,
`RemoteWriter.java`, `EmbeddedWriter.java`, `SchemaInitializer.java`,
`JsonlBatchBuilder.java`, `ArcadeDBSemanticWriter.java`  
**Дата:** 2026-04-13

---

## 1. Постановка задачи

Oracle PL/SQL допускает INSERT без явного списка колонок, когда VALUES-выражения
берутся из записей коллекции, заполненной через BULK COLLECT:

```sql
-- 1. Курсор: BULK COLLECT → коллекция
SELECT order_id, line_num, customer_id
BULK COLLECT INTO l_tab FROM orders_stg;

-- 2. INSERT без column list — поля берутся из записей коллекции
FORALL i IN 1..l_tab.COUNT
  INSERT INTO DWH.FACT_SALES_STG
  VALUES (
    SEQ_DWH_SALES.NEXTVAL,    -- последовательность, пропускается
    l_tab(i).order_id,        -- ← G6: фиксируем ORDER_ID
    l_tab(i).line_num,        -- ← G6: фиксируем LINE_NUM
    l_tab(i).customer_id      -- ← G6: фиксируем CUSTOMER_ID
  );
```

**Цель**: зафиксировать `AffectedColumns` INSERT-стейтмента и материализовать
lineage-цепочку через промежуточный узел `DaliRecord`:

```
DaliStatement(SELECT BULK COLLECT)
        │
  BULK_COLLECTS_INTO
        │
        ▼
DaliRecord(l_tab)
  ├── HAS_RECORD_FIELD → DaliRecordField(order_id)
  ├── HAS_RECORD_FIELD → DaliRecordField(line_num)
  └── HAS_RECORD_FIELD → DaliRecordField(customer_id)
        │
  RECORD_USED_IN
        │
        ▼
DaliStatement(FORALL INSERT)
```

---

## 2. Архитектура решения — три пути

### 2.1 G6: SELECT BULK COLLECT → VALUES без column list (исходный путь)

```
ANTLR parse tree
       │
       ▼
enterInto_clause (parent = Query_blockContext, ctx.BULK() != null)
       │ varName = ctx.general_element(0)
       │ rec = builder.ensureRecord(varName, routineGeoid)
       │ rec.setSourceStatementGeoid(currentStmt)
       │ scopeManager.registerCursorRecord(varName, stmtGeoid, inline=true)
       ▼
processAtomsOnStatementExit (INSERT stmt, нет column list)
       │
       │  buildInsertValuesAffectedColumns()
       │    слот 1: SEQ.NEXTVAL → пропуск (не collection field)
       │    слот 2: l_tab(i).order_id → collection_name=L_TAB, field=ORDER_ID
       │    слот 3: l_tab(i).line_num  → field=LINE_NUM
       │
       │  populateRecordFields(rec, cursorStmtGeoid)
       │    → rec.addField("ORDER_ID"), "LINE_NUM", "CUSTOMER_ID"
       ▼
StatementInfo.addAffectedColumn(
    columnRef, targetColName, targetGeoid,
    dataset_alias="L_TAB", "INSERT", "target", slot)
```

### 2.2 G6-EXT: FORALL INSERT с явным column list (расширение)

Реальный ERP-паттерн — FORALL INSERT **с явным column list** (G5-совместимым),
но VALUES содержит `l_buffer(i).field`. Старый guard прерывался досрочно.

```
ANTLR parse tree
       │
       ▼
buildInsertValuesAffectedColumns
       │
       │  si.getInsertTargetColumns() НЕ пустой → вход в G6-EXT ветку
       │
       │  Сканируем stmtAtoms:
       │    для каждого атома с is_collection_field_access=true
       │      parent_context ∈ {VALUES, INSERT_VALUES}
       │      → si.addBulkCollectSource(collection_name)
       │      → collsFound.add(collection_name)
       │
       │  Для каждой коллекции в collsFound:
       │    cursorStmtGeoid = scopeManager.getCursorRecordStmt(collName)
       │    rec = builder.getRecords() по varName
       │    если rec.getFields().isEmpty():
       │      populateRecordFields(rec, cursorStmtGeoid)
       │
       ▼
return (AffectedColumns заполнены через G5, RECORD_USED_IN будет построен
        на основе bulkCollectSources в RemoteWriter/EmbeddedWriter)
```

**Ключевое отличие**: G6-EXT не дублирует AffectedColumns (G5 уже сделал это),
но записывает имена коллекций в `StatementInfo.bulkCollectSources` — чтобы
`RemoteWriter` мог построить ребро `RECORD_USED_IN`.

### 2.3 G8: FETCH cursor BULK COLLECT INTO (новый путь)

ERP-паттерн с явным cursor FETCH, а не SELECT BULK COLLECT:

```sql
CURSOR cur_data IS SELECT contact_id, amount FROM crm_stage;

LOOP
  FETCH cur_data BULK COLLECT INTO l_buffer LIMIT 500;
  EXIT WHEN l_buffer.COUNT = 0;
  FORALL i IN 1..l_buffer.COUNT
    INSERT INTO DWH.STG_CRM (batch_date, contact_id, amount)
    VALUES (p_batch_date, l_buffer(i).contact_id, l_buffer(i).amount);
END LOOP;
```

```
ANTLR parse tree
       │
       ▼
exitFetch_statement (PlSqlSemanticListener)
       │
       │  ctx.BULK() != null?  → да
       │  ctx.variable_or_collection(0) → varName = "L_BUFFER"
       │  ctx.cursor_name() → cursorName = "CUR_DATA"
       │  cursorSelectGeoid = scopeManager.getCursorStmtGeoid("CUR_DATA")
       │                    ← ScopeManager.cursorRegistry
       │
       │  rec = builder.ensureRecord("L_BUFFER", routineGeoid)
       │  rec.setSourceStatementGeoid(cursorSelectGeoid)
       │  scopeManager.registerCursorRecord("L_BUFFER", cursorSelectGeoid, inline=true)
       │
       ▼
processAtomsOnStatementExit (FORALL INSERT)
       │
       │  G6-EXT путь (явный column list):
       │    si.addBulkCollectSource("L_BUFFER")
       │    populateRecordFields(rec, cursorSelectGeoid)
       │      → rec.addField("CONTACT_ID"), "AMOUNT"
       ▼
RemoteWriter:
  DaliRecord(L_BUFFER) + DaliRecordField(CONTACT_ID) + DaliRecordField(AMOUNT)
  BULK_COLLECTS_INTO: cur_data SELECT → DaliRecord(L_BUFFER)
  RECORD_USED_IN:     DaliRecord(L_BUFFER) → INSERT stmt
```

---

## 3. Узел DaliRecord

`DaliRecord` — тип вершины ArcadeDB, представляющий PL/SQL коллекцию,
заполняемую через `BULK COLLECT INTO`.

**Геоид:** `routineGeoid + ":RECORD:" + VAR_NAME_UPPER`

Пример: `PROCEDURE:PKG_ETL_01_CRM_STAGING:LOAD:RECORD:L_BUFFER`

**Свойства вершины:**

| Поле | Тип | Значение |
|------|-----|----------|
| `record_geoid` | STRING | геоид (уникальный ключ) |
| `record_name` | STRING | имя переменной (`L_BUFFER`) |
| `routine_geoid` | STRING | routine-владелец |
| `source_stmt_geoid` | STRING | geoid cursor-SELECT или FETCH-курсора (nullable) |
| `fields` | STRING | список полей через запятую (`CONTACT_ID,AMOUNT`) |
| `session_id` | STRING | сессия анализа |

---

## 4. Узел DaliRecordField

`DaliRecordField` — тип вершины ArcadeDB, представляющий **одно поле** коллекции DaliRecord.
Необходим для построения column-level mapping: `DaliRecordField → FIELD_MAPS_TO → DaliColumn`.

**Геоид:** `rec.getGeoid() + ":FIELD:" + FIELD_NAME_UPPER`

Пример: `PROCEDURE:LOAD:RECORD:L_BUFFER:FIELD:CONTACT_ID`

**Свойства вершины:**

| Поле | Тип | Значение |
|------|-----|----------|
| `field_geoid` | STRING | геоид поля (уникальный ключ) |
| `field_name` | STRING | имя поля (`CONTACT_ID`) |
| `field_order` | INTEGER | позиция в cursor SELECT (1-based) |
| `record_geoid` | STRING | геоид родительского DaliRecord |
| `session_id` | STRING | сессия анализа |

**Рёбра:**

| Ребро | Откуда | Куда | Статус |
|-------|--------|------|--------|
| `HAS_RECORD_FIELD` | DaliRecord | DaliRecordField | ✅ реализовано |
| `FIELD_MAPS_TO` | DaliRecordField | DaliColumn | ⏳ G11, не реализовано |

---

## 5. `populateRecordFields` — алгоритм

Заполняет `RecordInfo.fields` из `columnsOutput` cursor SELECT-стейтмента.
Вызывается в обоих путях G6 и G6-EXT.

```java
private void populateRecordFields(RecordInfo rec, String cursorStmtGeoid) {
    if (builder == null || cursorStmtGeoid == null) return;
    StatementInfo cursorSi = builder.getStatements().get(cursorStmtGeoid);
    if (cursorSi == null) return;

    // Сортируем columnsOutput по полю "order" (position in SELECT list)
    cursorSi.getColumnsOutput().entrySet().stream()
            .sorted(Comparator.comparingInt(e -> {
                Object ord = e.getValue().get("order");
                return ord instanceof Number n ? n.intValue() : 0;
            }))
            .forEach(e -> {
                String name = (String) e.getValue().get("name");
                // Исключаем wildcard — SELECT * не даёт имён полей (G7 gap)
                if (name != null && !"*".equals(name)) rec.addField(name);
            });
}
```

**Когда fields остаётся пустым:**
- `SELECT * BULK COLLECT INTO l_tab` — wildcard → G7 (не реализовано)
- Cursor SELECT не найден в builder (cross-package scenario)
- `columnsOutput` пуст (пустой стейтмент)

---

## 6. `StatementInfo.bulkCollectSources` (G6-EXT)

Хранит имена коллекций, чьи поля встречаются в VALUES-выражениях INSERT,
когда INSERT имеет явный column list (G5). Используется `RemoteWriter` и
`EmbeddedWriter` для построения `RECORD_USED_IN` без дублирования AffectedColumns.

```java
// StatementInfo.java
private final Set<String> bulkCollectSources = new LinkedHashSet<>();

public void addBulkCollectSource(String collectionAlias) {
    if (collectionAlias != null) bulkCollectSources.add(collectionAlias.toUpperCase());
}

public Set<String> getBulkCollectSources() {
    return Collections.unmodifiableSet(bulkCollectSources);
}
```

**Использование в RemoteWriter/EmbeddedWriter:**

```java
// Было (G6 only):
boolean usesRecord = si.getAffectedColumns().stream()
        .anyMatch(ac -> rec.getVarName().equals(ac.get("dataset_alias")));

// Стало (G6 + G6-EXT):
boolean usesRecord = si.getAffectedColumns().stream()
        .anyMatch(ac -> rec.getVarName().equals(ac.get("dataset_alias")))
    || si.getBulkCollectSources().contains(rec.getVarName().toUpperCase());
```

---

## 7. `exitFetch_statement` — алгоритм G8

```java
// PlSqlSemanticListener.java
@Override
public void exitFetch_statement(PlSqlParser.Fetch_statementContext ctx) {
    if (ctx == null || ctx.BULK() == null) return;
    if (ctx.variable_or_collection() == null || ctx.variable_or_collection().isEmpty()) return;

    // 1. Имя коллекции-приёмника (первый элемент INTO)
    String varName = BaseSemanticListener.cleanIdentifier(
            ctx.variable_or_collection(0).getText()).toUpperCase();

    // 2. Разрешаем cursor SELECT geoid через cursorRegistry
    String cursorSelectGeoid = null;
    if (ctx.cursor_name() != null) {
        String rawCursor = ctx.cursor_name().getText();
        int paren = rawCursor.indexOf('(');
        String cursorName = BaseSemanticListener.cleanIdentifier(
                paren >= 0 ? rawCursor.substring(0, paren) : rawCursor);
        cursorSelectGeoid = base.engine.getScopeManager().getCursorStmtGeoid(cursorName);
    }

    // 3. Создаём/обновляем RecordInfo
    String routineGeoid = base.currentRoutine();
    var rec = base.engine.getBuilder().ensureRecord(varName, routineGeoid);
    if (cursorSelectGeoid != null && rec.getSourceStatementGeoid() == null)
        rec.setSourceStatementGeoid(cursorSelectGeoid);

    // 4. Регистрируем varName → cursor stmtGeoid в ScopeManager
    //    (inline=true: прямой geoid, без поиска через cursorRegistry)
    String stmtGeoid = cursorSelectGeoid != null ? cursorSelectGeoid
            : base.engine.getScopeManager().currentStatement();
    if (stmtGeoid != null)
        base.engine.getScopeManager().registerCursorRecord(varName, stmtGeoid, true);

    logger.debug("G8 FETCH BULK COLLECT: {} → cursor stmt {}", varName, cursorSelectGeoid);
}
```

**Связка с cursorRegistry:**

`ScopeManager.registerCursor(cursorName, stmtGeoid)` вызывается при
`exitCursor_declaration`. `getCursorStmtGeoid(cursorName)` возвращает
geoid SELECT-стейтмента курсора для последующего `rec.setSourceStatementGeoid`.

---

## 8. Паттерн распознавания атомов (`isCollectionFieldAccess`)

```java
private static boolean isCollectionFieldAccess(List<String> tokens,
                                                List<Map<String, String>> tokenDetails) {
    int n = tokens.size();
    if (n < 6) return false;
    if (!getCanonical(tokenDetails.get(0)).isIdentifier()) return false;  // IDENT
    if (getCanonical(tokenDetails.get(1)) != LEFT_PAREN)   return false;  // (
    if (getCanonical(tokenDetails.get(n-3)) != RIGHT_PAREN) return false; // )
    if (getCanonical(tokenDetails.get(n-2)) != PERIOD)      return false; // .
    if (!getCanonical(tokenDetails.get(n-1)).isIdentifier()) return false; // IDENT
    return true;
}
```

| Выражение              | Совпадение | collection_name | collection_field_name |
|------------------------|------------|-----------------|-----------------------|
| `L_TAB(I).ORDER_ID`    | ✓          | `L_TAB`         | `ORDER_ID`            |
| `L_BUFFER(I).AMOUNT`   | ✓          | `L_BUFFER`      | `AMOUNT`              |
| `L_TAB(I+1).AMOUNT`    | ✓          | `L_TAB`         | `AMOUNT`              |
| `SEQ_DWH.NEXTVAL`      | ✗          | —               | —                     |
| `L_TAB(I)`             | ✗          | —               | —                     |
| `NULL`, `100`          | ✗          | —               | —                     |

---

## 9. Позиционный счётчик (output_column_sequence)

Позиция в VALUES (1-based) соответствует порядку выражений в `expressions_()`.
Не совпавшие выражения (последовательности, константы) **увеличивают счётчик**,
но не создают запись в `AffectedColumns`:

```
VALUES (SEQ.NEXTVAL,      l_tab(i).order_id, l_tab(i).line_num)
        slot=1 (skip)     slot=2             slot=3
```

---

## 10. Определение целевой колонки (targetColName)

1. **DDL ordinal**: `findColumnByOrdinal(targetGeoid, slot)` → `ColumnInfo.ordinalPosition`
2. **Fallback**: если DDL не загружен и в слоте ровно одно поле → `targetColName = fields[0]`
3. **Multi-field slot без DDL**: пропуск (нельзя назвать целевую колонку)

---

## 11. Запись DaliRecord + DaliRecordField в RemoteWriter

```java
// --- Phase 1: вставка вершин DaliRecord + DaliRecordField ---

for (var e : str.getRecords().entrySet()) {
    RecordInfo rec = e.getValue();
    String fieldsJson = String.join(",", rec.getFields());

    // 1. DaliRecord vertex
    rcmd("INSERT INTO DaliRecord SET session_id=?, record_geoid=?, record_name=?, " +
            "routine_geoid=?, source_stmt_geoid=?, fields=?",
        sid, rec.getGeoid(), rec.getVarName(),
        rec.getRoutineGeoid(), rec.getSourceStatementGeoid(), fieldsJson);

    // 2. DaliRecordField vertices (один на каждое именованное поле)
    List<String> fields = rec.getFields();
    for (int fi = 0; fi < fields.size(); fi++) {
        String fieldName = fields.get(fi);
        String fieldGeoid = rec.getGeoid() + ":FIELD:" + fieldName;  // геоид схема
        rcmd("INSERT INTO DaliRecordField SET session_id=?, field_geoid=?, field_name=?, " +
                "field_order=?, record_geoid=?",
            sid, fieldGeoid, fieldName, fi + 1, rec.getGeoid());
    }
}

// --- Phase 2: рёбра ---

for (var e : str.getRecords().entrySet()) {
    RecordInfo rec = e.getValue();
    String recRid = rid.records.get(rec.getGeoid());
    if (recRid == null) continue;

    // BULK_COLLECTS_INTO: cursor SELECT → DaliRecord
    if (rec.getSourceStatementGeoid() != null) {
        String srcRid = rid.statements.get(rec.getSourceStatementGeoid());
        if (srcRid != null) edgeByRid("BULK_COLLECTS_INTO", srcRid, recRid, sid);
    }

    // HAS_RECORD_FIELD: DaliRecord → DaliRecordField
    for (String fieldName : rec.getFields()) {
        String fieldGeoid = rec.getGeoid() + ":FIELD:" + fieldName;
        String fieldRid = rid.recordFields.get(fieldGeoid);
        if (fieldRid != null) edgeByRid("HAS_RECORD_FIELD", recRid, fieldRid, sid);
    }

    // RECORD_USED_IN: DaliRecord → INSERT stmt
    //   G6: dataset_alias check (VALUES без column list)
    //   G6-EXT: bulkCollectSources check (VALUES с явным column list)
    for (var stmtEntry : str.getStatements().entrySet()) {
        StatementInfo si = stmtEntry.getValue();
        if (!"INSERT".equals(si.getType())) continue;

        boolean usesRecord = si.getAffectedColumns().stream()
                .anyMatch(ac -> rec.getVarName().equals(ac.get("dataset_alias")))
            || si.getBulkCollectSources().contains(rec.getVarName().toUpperCase());

        if (usesRecord) {
            String stmtRid = rid.statements.get(stmtEntry.getKey());
            if (stmtRid != null) edgeByRid("RECORD_USED_IN", recRid, stmtRid, sid);
        }
    }
}
```

---

## 12. REMOTE_BATCH: порядок flush и гарантия отсутствия dangling edges

### 12.1 Проблема

В REMOTE_BATCH всё буферизируется и отправляется одним HTTP POST к ArcadeDB
`/api/v1/batch`. ArcadeDB обрабатывает строки NDJSON последовательно и **бросает
ошибку на dangling edge**, если вершина ещё не вставлена на момент создания ребра.

### 12.2 Гарантия

`JsonlBatchBuilder` хранит два отдельных буфера:

```java
private final StringBuilder vertices = new StringBuilder(64 * 1024); // Phase 1
private final StringBuilder edges    = new StringBuilder(32 * 1024); // Phase 2

public String build() {
    return vertices.toString() + edges.toString(); // vertices ВСЕГДА первые
}
```

| Шаг | Тип | Вершины/рёбра |
|-----|-----|---------------|
| 7b / 9b | vertex | DaliAffectedColumn |
| 7c / 9c | vertex | **DaliRecord** |
| 7d / 9d | vertex | **DaliRecordField** |
| 8 / 10  | vertex | DaliAtom |
| Phase 2 | edge | BULK_COLLECTS_INTO, HAS_RECORD_FIELD, RECORD_USED_IN |

`DaliRecord` и `DaliRecordField` вставляются **перед** DaliAtom и **перед** рёбрами
в обоих вариантах (`buildFromResult(sid, result)` и `buildFromResult(sid, result, tableRids, ...)`).

---

## 13. Поток данных по файлам

### `PlSqlSemanticListener.java`

```java
// G6: SELECT BULK COLLECT INTO
@Override
public void enterInto_clause(PlSqlParser.Into_clauseContext ctx) {
    if (ctx == null || ctx.parent == null) return;
    if (!(ctx.parent instanceof PlSqlParser.Query_blockContext)) return;
    if (ctx.BULK() != null) {
        // → ensureRecord + registerCursorRecord
    }
}

// G8: FETCH cursor BULK COLLECT INTO
@Override
public void exitFetch_statement(PlSqlParser.Fetch_statementContext ctx) {
    if (ctx == null || ctx.BULK() == null) return;
    // → getCursorStmtGeoid → ensureRecord + registerCursorRecord
}
```

### `AtomProcessor.java`

```java
// G6-EXT guard (column list присутствует)
if (!si.getInsertTargetColumns().isEmpty()) {
    Set<String> collsFound = new LinkedHashSet<>();
    for (Map<String, Object> a : stmtAtoms.values()) {
        if (!Boolean.TRUE.equals(a.get("is_collection_field_access"))) continue;
        String pCtx = (String) a.get("parent_context");
        if (!"VALUES".equals(pCtx) && !"INSERT_VALUES".equals(pCtx)) continue;
        String collName = (String) a.get("collection_name");
        if (collName != null) { si.addBulkCollectSource(collName); collsFound.add(collName); }
    }
    if (builder != null && scopeManager != null) {
        for (String collName : collsFound) {
            String cursorStmtGeoid = scopeManager.getCursorRecordStmt(collName);
            if (cursorStmtGeoid == null) continue;
            RecordInfo rec = builder.getRecords().values().stream()
                    .filter(r -> collName.equals(r.getVarName())).findFirst().orElse(null);
            if (rec != null && rec.getFields().isEmpty())
                populateRecordFields(rec, cursorStmtGeoid);
        }
    }
    return; // AffectedColumns уже заполнены G5
}
```

### `ScopeManager.java`

```java
/** Возвращает geoid SELECT-стейтмента для именованного курсора. */
public String getCursorStmtGeoid(String cursorName) {
    return cursorName != null ? cursorRegistry.get(cursorName.toUpperCase()) : null;
}

/** Возвращает geoid cursor SELECT для record-переменной (из registerCursorRecord). */
public String getCursorRecordStmt(String recordVar) {
    return recordVar != null ? cursorRecordAliases.get(recordVar.toUpperCase()) : null;
}
```

---

## 14. Результаты корпусного прогона

Корпус: **190 файлов** из `C:/Dali_tests/test_plsql`

| Метрика | До G6 | После G6 | После G8+G6-EXT |
|---------|-------|----------|-----------------|
| DaliRecord | 0 | 9 | **54** |
| BULK_COLLECTS_INTO | 0 | 9 | **42** |
| RECORD_USED_IN | 0 | 0 | **3** |
| DaliRecordField | — | — | ✅ (новый тип) |

Файлы с DaliRecord:
- `DWH2/.../DM_LOADER_RMS_FCT_CUSTOMER_DEFAULT_EVENT_UL.pck` — 5 records, FOR loop
- `ERP_CORE/PKG_ETL_01_CRM_STAGING.sql` — 2 records, FETCH+FORALL
- `ERP_CORE/PKG_ETL_05_FACT_FINANCE.sql` — 1 record
- `ERP_CORE/PKG_ETL_08_TREASURY.sql` — 1 record

---

## 15. Ограничения и известные расширения

| Ситуация | Статус |
|----------|--------|
| `collection(i).field` — один уровень вложенности | ✅ поддержано |
| `SELECT col1, col2 BULK COLLECT INTO` — поля заполнены | ✅ G6/G8 |
| FETCH cursor BULK COLLECT INTO → RecordInfo | ✅ G8 |
| FORALL INSERT с явным column list → RECORD_USED_IN | ✅ G6-EXT |
| DaliRecordField vertices (per field) | ✅ HAS_RECORD_FIELD |
| `SELECT * BULK COLLECT INTO` — wildcard | ❌ G7: поля из DDL не восстанавливаются |
| `FOR rec IN (SELECT ...) LOOP` — inline cursor loop | ❌ G10: RecordInfo не создаётся |
| FIELD_MAPS_TO: DaliRecordField → DaliColumn | ❌ G11: не реализовано |
| Курсор с JOIN → findCursorSourceTable | ❌ G12: только первая таблица |
| `collection(i).sub_record.field` — два уровня | ❌ не поддержано |
| Несколько коллекций в одном INSERT VALUES | ✅ slotToCollection per-slot |

---

## 16. Беклог следующего спринта

| ID | Описание | Приоритет |
|----|----------|-----------|
| G7 | `SELECT * BULK COLLECT` → восстановить поля из DDL таблицы-источника | HIGH |
| G10 | `FOR rec IN cursor LOOP` → DaliRecord vertex | HIGH |
| G11 | `FIELD_MAPS_TO`: DaliRecordField → DaliColumn для полного column mapping | MEDIUM |
| G12 | `findCursorSourceTable` для курсоров с JOIN (сейчас только первая таблица) | LOW |
| G13 | Unit tests: G6-EXT, G8, G10, G11 | MEDIUM |

---

## 17. Тесты

### `com.hound.semantic.LowGapsTest`

| Метод | Что проверяет |
|-------|---------------|
| `g6_insertValuesCollectionFields_registersAffectedColumns` | ORDER_ID, LINE_NUM, CUSTOMER_ID попадают в AffectedColumns; NEXTVAL исключается |
| `g6_insertValuesWithExplicitColumnList_notDuplicated` | При явном column list ORDER_ID фигурирует ровно 1 раз (G5 guard) |
| `g6ext_forallInsert_explicitColList_createsRecordUsedIn` | ⏳ G13 backlog |
| `g8_fetchBulkCollect_createsRecordInfo` | ⏳ G13 backlog |

```bash
./gradlew :libraries:hound:test --tests "*LowGapsTest.g6*"
# полный прогон:
./gradlew :libraries:hound:test
# корпус (требует ArcadeDB + corpus dir):
./gradlew :libraries:hound:test --tests "com.hound.storage.CorpusBatchIT" \
  -Dcorpus=true -Dintegration=true
```

---

## 19. G9 — RETURNING INTO lineage

> Добавлено в Sprint 2 (Apr 2026, KI-RETURN-1)

### 19.1 Постановка задачи

Oracle PL/SQL позволяет возвращать значения из DML-операторов в переменные:

```sql
-- Вариант 1: скалярная переменная
UPDATE hr.orders SET status = 'CLOSED'
WHERE order_id = p_id
RETURNING status INTO v_status;

-- Вариант 2: поле записи
UPDATE hr.orders SET status = 'CLOSED'
WHERE order_id = p_id
RETURNING status INTO l_rec.status;

-- Вариант 3: OUT-параметр
UPDATE hr.orders SET status = 'CLOSED'
WHERE order_id = p_id
RETURNING status INTO p_out_status;

-- Вариант 4: BULK COLLECT в коллекцию
UPDATE hr.orders SET status = 'CLOSED'
WHERE order_id IN (SELECT order_id FROM pending)
RETURNING order_id, status BULK COLLECT INTO l_ids, l_statuses;
```

**Цель**: построить рёбра `RETURNS_INTO` от `DaliStatement` к целевой вершине.

### 19.2 4 варианта target и соответствующие vertex types

| Вариант | Синтаксис | Target Vertex | Geoid формат |
|---------|-----------|--------------|-------------|
| 1 | `RETURNING col INTO :v_scalar` | `DaliVariable` | `ROUTINE:VAR:V_SCALAR` |
| 2 | `RETURNING col INTO l_rec.field` | `DaliRecordField` | `ROUTINE:RECORD:L_REC:FIELD:FIELD` |
| 3 | `RETURNING col INTO p_out` | `DaliParameter` | `ROUTINE:PARAM:P_OUT` |
| 4 | `RETURNING col BULK COLLECT INTO l_tab` | `DaliRecord` | `ROUTINE:RECORD:L_TAB` |

### 19.3 Диаграмма

```
Вариант 1: скалярная переменная
─────────────────────────────────────────────────────────────────
DaliStatement(UPDATE)
        │
  RETURNS_INTO  [returning_exprs="STATUS"]
        │
        ▼
DaliVariable(V_STATUS)
  ├── var_geoid = "HR.PROC_CLOSE:VAR:V_STATUS"
  └── data_type = "VARCHAR2" (если известен из declaration)


Вариант 2: поле записи
─────────────────────────────────────────────────────────────────
DaliStatement(UPDATE)
        │
  RETURNS_INTO  [returning_exprs="STATUS"]
        │
        ▼
DaliRecordField(L_REC.STATUS)
  ├── field_geoid = "HR.PROC_CLOSE:RECORD:L_REC:FIELD:STATUS"
  └── record_geoid = "HR.PROC_CLOSE:RECORD:L_REC"


Вариант 3: OUT-параметр
─────────────────────────────────────────────────────────────────
DaliStatement(UPDATE)
        │
  RETURNS_INTO  [returning_exprs="STATUS"]
        │
        ▼
DaliParameter(P_OUT_STATUS)
  ├── param_geoid = "HR.PROC_CLOSE:PARAM:P_OUT_STATUS"
  └── param_mode = "OUT"


Вариант 4: BULK COLLECT → DaliRecord
─────────────────────────────────────────────────────────────────
DaliStatement(UPDATE BULK COLLECT)
        │
  RETURNS_INTO  [returning_exprs="ORDER_ID,STATUS"]
        │
        ▼
DaliRecord(L_TAB)
  ├── record_geoid = "HR.PROC_CLOSE:RECORD:L_TAB"
  └── HAS_RECORD_FIELD ──► DaliRecordField(ORDER_ID)
                      └──► DaliRecordField(STATUS)
```

### 19.4 Алгоритм классификации target

```java
// В BaseSemanticListener.classifyReturningTarget():
ReturningTargetKind classify(String varName, String routineGeoid) {

    // 1. IDENT.IDENT → поле записи
    if (varName.contains("."))
        return RECORD_FIELD;

    // 2. Есть параметр с таким именем?
    RoutineInfo ri = engine.getBuilder().getRoutines().get(routineGeoid);
    if (ri != null && ri.hasParameter(varName))
        return PARAMETER;

    // 3. Есть DaliRecord с таким именем?
    if (engine.getBuilder().getRecords()
               .containsKey(routineGeoid + ":RECORD:" + varName))
        return RECORD;

    // 4. По умолчанию — DaliVariable
    return VARIABLE;
}
```

### 19.5 Атрибуты ребра RETURNS_INTO

| Атрибут | Тип | Описание |
|---------|-----|---------|
| `session_id` | String | ID сессии парсинга |
| `returning_exprs` | String | Запятая-разделённый список выражений из `RETURNING expr1, expr2` |

### 19.6 Файлы (Sprint 2)

| Файл | Изменение |
|------|-----------|
| `PlSqlSemanticListener.java` | `enterReturning_clause()` — парсинг targets |
| `BaseSemanticListener.java` | `onReturningTarget()`, `classifyReturningTarget()` |
| `StatementInfo.java` | `ReturningTarget record`, `addReturningTarget()` |
| `RemoteWriter.java` | Loop RETURNS_INTO edges после write statements |
| `JsonlBatchBuilder.java` | `appendEdge("RETURNS_INTO", ...)` после STMT vertices |
| `RemoteSchemaCommands.java` | `CREATE EDGE TYPE RETURNS_INTO IF NOT EXISTS` |

### 19.7 Связь с G6/G8

| Feature | Механизм | Результат |
|---------|---------|---------|
| G6: BULK COLLECT INTO | `BULK_COLLECTS_INTO` ребро | DaliStatement(SELECT) → DaliRecord |
| G8: FETCH BULK COLLECT | `cursorRecordAliases` | DaliRecord заполняется из cursor columns |
| **G9: RETURNING INTO** | `RETURNS_INTO` ребро | DaliStatement(DML) → DaliVariable/RecordField/Parameter/Record |

---

## 18. Связанные компоненты

| Компонент | Роль |
|-----------|------|
| G5 (`exitInsert_into_clause`) | Регистрирует явный column list; `getInsertTargetColumns()` |
| G3-MERGE (`onMergeInsertColumns`) | Аналогичная логика для MERGE INSERT VALUES positional binding |
| `StatementInfo.addAffectedColumn` | Хранение; `dataset_alias` = collection var name |
| `StatementInfo.bulkCollectSources` | G6-EXT: имена коллекций в VALUES при наличии column list |
| `ScopeManager.cursorRegistry` | cursor name → cursor SELECT geoid |
| `ScopeManager.cursorRecordAliases` | record var → cursor SELECT geoid (G6/G8) |
| `RecordInfo.fields` | Список полей (из cursor SELECT columnsOutput, без `*`) |
| `DaliRecord` (ArcadeDB vertex) | Коллекция PL/SQL; BULK_COLLECTS_INTO + RECORD_USED_IN |
| `DaliRecordField` (ArcadeDB vertex) | Поле коллекции; HAS_RECORD_FIELD; FIELD_MAPS_TO (G11) |
| `SchemaInitializer` | DDL для DaliRecord, DaliRecordField, новых edge-типов |
| `RemoteWriter` | REMOTE path: вставляет DaliRecord+DaliRecordField, рёбра через `edgeByRid` |
| `EmbeddedWriter` | EMBEDDED path: аналогичная логика |
| `JsonlBatchBuilder` | REMOTE_BATCH path: vertex-before-edge гарантия |
| `ArcadeDBSemanticWriter.cleanAll` | Удаляет DaliRecord, DaliRecordField и все связанные рёбра |
