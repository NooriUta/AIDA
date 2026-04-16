# Plan: Data Lineage Gaps — PL/SQL + документация (без Dynamic SQL и SOURCE.*)

> **Sprint:** S2 | **Дата:** Apr 2026 | **Статус:** IN PROGRESS
>
> Закрыть все открытые пробелы Data Lineage в Hound/Dali PL/SQL парсере.
> Исключены: EXECUTE IMMEDIATE (Dynamic SQL) и S1.BUG-4 (MERGE USING SOURCE.*).
>
> **Обязательное условие:** каждое изменение сопровождается обновлением/созданием документации
> по алгоритмам парсинга, geoid-схеме, структуре хранения и вычислению рёбер.

---

## Стартовые шаги

1. `git checkout -b feature/lineage-gaps-s2`
2. Сохранить этот план в `docs/sprints/PLAN_LINEAGE_GAPS_S2_APR2026.md` ✅

---

## Документация (выполнено 14.04.2026)

| Файл | Тип | Статус |
|------|-----|--------|
| `docs/hound/HOUND_GEOID_SPEC.md` | НОВЫЙ | ✅ Создан |
| `docs/hound/HOUND_PLSQL_LINEAGE_GAPS_S2.md` | НОВЫЙ | ✅ Создан |
| `docs/hound/REMOTE_BATCH_WRITE_FLOW.md` | ОБНОВЛЁН | ✅ Разделы 15–17 добавлены |
| `libraries/hound/docs/g6-cursor-insert-values-lineage.md` | ОБНОВЛЁН | ✅ Раздел G9 RETURNING INTO добавлен |
| `docs/architecture/DECISIONS_LOG.md` | ОБНОВЛЁН | ✅ Q28, Q29, Q30 добавлены |
| `docs/sprints/PLAN_LINEAGE_GAPS_S2_APR2026.md` | НОВЫЙ | ✅ Этот файл |

---

## Реализация — приоритеты

### 🔴 Критические (делать первыми)

#### KI-DDL-1 — ALTER TABLE DDL edges

**Файлы:** `RemoteWriter.java:735`, `JsonlBatchBuilder.java:341`

**Проблема:** `DaliDDLStatement` создаётся, но рёбра `DaliDDLModifiesTable` и
`DaliDDLModifiesColumn` не создаются.

**Алгоритм:**

В `PlSqlSemanticListener.enterAlter_table_properties`:
- `ADD COLUMN` → `base.onDdlColumnChange(colName, "ADD")`
- `MODIFY COLUMN` → `base.onDdlColumnChange(colName, "MODIFY")`
- `DROP COLUMN` → `base.onDdlColumnChange(colName, "DROP")`

`StatementInfo` — новый `List<Pair<String,String>> affectedColumnGeoids`.

`RemoteSchemaCommands`:
```java
"CREATE EDGE TYPE DaliDDLModifiesTable IF NOT EXISTS EXTENDS E",
"CREATE EDGE TYPE DaliDDLModifiesColumn IF NOT EXISTS EXTENDS E",
```

Geoid:
```
DaliDDLStatement: "DDL_" + ROUTINE_GEOID + "." + SEQ
```

Подробный алгоритм: `HOUND_PLSQL_LINEAGE_GAPS_S2.md §1`

---

#### KI-RETURN-1 — RETURNING INTO + DaliRecordField

**Файлы:** `PlSqlSemanticListener.java`, `BaseSemanticListener.java`,
`RecordInfo.java`, `StatementInfo.java`, `RemoteWriter.java:810`, `JsonlBatchBuilder.java:435`,
`RemoteSchemaCommands.java`

**Проблема:**
- `DaliRecordField` пишется в RemoteWriter без dedup, отсутствует в JsonlBatchBuilder
- Схемный тип `DaliRecordField` отсутствует в RemoteSchemaCommands
- `RETURNING INTO` clause не обрабатывается

**4 варианта target:**
```
RETURNING col INTO :v_scalar      → STMT --[RETURNS_INTO]--> DaliVariable
RETURNING col INTO l_rec.field    → STMT --[RETURNS_INTO]--> DaliRecordField
RETURNING col INTO p_out          → STMT --[RETURNS_INTO]--> DaliParameter
RETURNING col BULK COLLECT INTO l → STMT --[RETURNS_INTO]--> DaliRecord
```

**RecordInfo.FieldInfo** (обогащение):
```java
public record FieldInfo(String name, String dataType, int ordinalPosition, String sourceColumnGeoid) {}
```

**RidCache — новые maps:**
```java
Map<String, String> variables    = new HashMap<>();
Map<String, String> parameters   = new HashMap<>();
Map<String, String> ddlStatements = new HashMap<>();
```

Подробный алгоритм (7 шагов): `HOUND_PLSQL_LINEAGE_GAPS_S2.md §2`

---

### 🟠 Высокий приоритет

#### KI-005 — UNIQUE / CHECK constraints

`PlSqlSemanticListener.java` строка 1604 — дополнить `enterOut_of_line_constraint`.

Geoid:
```
UNIQUE: TABLE_GEOID + ":UNIQUE:" + (NAME ?? toHex(hash(sortedCols)))
CHECK:  TABLE_GEOID + ":CHECK:"  + (NAME ?? toHex(hash(expr)))
```

Рёбра:
```
DaliTable --[HAS_UNIQUE_KEY]--> DaliUniqueConstraint
DaliUniqueConstraint --[IS_UNIQUE_COLUMN]--> DaliColumn
DaliTable --[HAS_CHECK]--> DaliCheckConstraint
```

Подробный алгоритм: `HOUND_PLSQL_LINEAGE_GAPS_S2.md §3`

---

#### KI-ROWTYPE-1 — %ROWTYPE / %TYPE

`enterVariable_declaration` — детектировать `%ROWTYPE` / `%TYPE` суффикс.

Post-walk resolution: заполнить `DaliRecord.fieldInfos` из `DaliColumn` той же сессии
(фильтр по `tableGeoid`, сортировка по `ordinalPosition`).

Подробный алгоритм: `HOUND_PLSQL_LINEAGE_GAPS_S2.md §4`

---

#### KI-PIPE-1 — PIPE ROW

`enterPipe_row_statement` → `ri.setPipelined(true)`.
`enterTable_collection_expression` → ребро `READS_PIPELINED`.

Новые edges: `PIPES_FROM`, `READS_PIPELINED`, `READS_FROM_TABLE_FUNC`.

Подробный алгоритм: `HOUND_PLSQL_LINEAGE_GAPS_S2.md §5`

---

#### KI-PENDING-1 — Pending column resolution

Pass 3: `resolveViaParent()` — parent chain, depth = 1.
Pass 4: single-table fuzzy, quality = LOW.

Подробный алгоритм: `HOUND_PLSQL_LINEAGE_GAPS_S2.md §6`

---

### 🟡 Средний приоритет

| ID | Конструкция | Файл алгоритма |
|----|-------------|---------------|
| KI-INSALL-1 | INSERT ALL / INSERT FIRST | `§7` |
| KI-DBMSSQL-1 | DBMS_SQL stub | `§8` |
| KI-DBLINK-1 | Database links (@dblink) | `§9` |
| KI-JSON-1 | JSON_TABLE / JSON_VALUE | `§10` |
| KI-XML-1 | XMLTABLE / XMLQUERY | `§11` |
| KI-LATERAL-1 | LATERAL joins | `§12` |
| KI-APPLY-1 | CROSS / OUTER APPLY | `§13` |
| KI-WITHFUNC-1 | WITH FUNCTION | `§14` |
| KI-FLASHBACK-1 | AS OF TIMESTAMP / SCN | `§15` |
| KI-PRAGMA-1 | PRAGMA AUTONOMOUS_TRANSACTION | `§16` |
| KI-VARRAY-1 | VARRAY(i) indexed access | `§17` |
| KI-CONDCOMP-1 | $IF / $ELSIF preprocessing | `§18` |

---

### ⚪ Stub (Sprint 3)

| ID | Конструкция | Статус |
|----|-------------|--------|
| KI-NESTREC-1 | Nested records (depth > 1) | Маркер + logging, не реализуется |

---

## Критические файлы

| Файл | Изменения |
|------|-----------|
| `PlSqlSemanticListener.java` | +13 enter/exit handlers |
| `BaseSemanticListener.java` | +15 on*() методов |
| `AtomProcessor.java` | DBMS_SQL, TABLE(), nested stub |
| `RecordInfo.java` | FieldInfo record, getFieldInfos() |
| `StatementInfo.java` | ReturningTarget, affectedColumnGeoids |
| `RoutineInfo.java` | isPipelined, autonomousTransaction |
| `TableInfo.java` | dblink поле |
| `StructureAndLineageBuilder.java` | addReturningTarget(), ensureRemoteTable() |
| `UniversalSemanticEngine.java` | Pass 3–4, pendingRowtypes |
| `ScopeManager.java` | lateralScopes, multiInsertParent |
| `RemoteWriter.java` | DDL edges, RETURNS_INTO, dedup, RidCache +3 maps |
| `JsonlBatchBuilder.java` | DaliRecordField vertices + edges, RETURNS_INTO |
| `RemoteSchemaCommands.java` | 12+ новых типов |
| `ConditionalCompilationPreprocessor.java` | Новый класс |

---

## Верификация

| # | Тест | Критерий |
|---|------|---------|
| V1 | `ResolutionLogDiagnosticTest` | % resolved ≥ 90% |
| V2 | DDL edges | `DaliDDLModifiesTable` присутствуют в YGG |
| V3 | DaliRecordField | `field_geoid` уникален, нет дублей |
| V4 | `RETURNING col INTO :v` | `DaliVariable` + `RETURNS_INTO` |
| V5 | `RETURNING col INTO l_rec.field` | `DaliRecordField` + `RETURNS_INTO` |
| V6 | `RETURNING col INTO p_out` | `DaliParameter` + `RETURNS_INTO` |
| V7 | `UNIQUE(col1)` | `DaliUniqueConstraint` + `IS_UNIQUE_COLUMN` |
| V8 | `INSERT ALL INTO t1 INTO t2` | 2 child `DaliStatement` |
| V9 | `PIPE ROW` + `TABLE(func())` | `pipelined=true` + `READS_PIPELINED` |
| V10 | `orders@prod_db` | `DaliTable.dblink="PROD_DB"` |

---

## История изменений

| Дата | Что |
|------|-----|
| 2026-04-14 | v1.0 — план создан, документация выполнена |
