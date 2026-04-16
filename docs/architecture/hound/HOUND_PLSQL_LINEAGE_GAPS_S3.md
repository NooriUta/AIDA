# PL/SQL Lineage Gaps — Sprint 3 Spec

**Документ:** `HOUND_PLSQL_LINEAGE_GAPS_S3`
**Версия:** 1.0
**Дата:** 16.04.2026
**Статус:** 📋 BACKLOG — Sprint 3 не начат
**Зависит от:** Sprint 2 ✅ DONE (HOUND_PLSQL_LINEAGE_GAPS_S2.md v2.0)
**Predecessor:** `HOUND_PLSQL_LINEAGE_GAPS_S2.md` — 14 из 19 KI items реализованы

---

## Что осталось из Sprint 2

Перенесено из S2 результатов:

| ID | Конструкция | Причина переноса |
|----|-------------|-----------------|
| KI-JSON-1 | JSON_TABLE / JSON_VALUE / JSON_QUERY | Нет parser grammar для JSON path expressions |
| KI-XML-1 | XMLTABLE / XMLQUERY | Низкий приоритет в корпусе; XML deprecated в новых схемах |
| KI-LATERAL-1 | LATERAL joins + NameResolver S9 | S9 resolver не реализован — нужен отдельный pass |
| KI-WITHFUNC-1 | WITH FUNCTION geoid нормализация | Geoid для inline SQL functions ещё не детерминирован |
| KI-NESTREC-1 | Nested records (multi-level) | `DaliRecordField` не поддерживает вложенность > 1 уровня |
| KI-DBLINK-1 | Database links @dblink (частичный) | Partial: ссылка распознаётся, но remote schema не резолвится |
| KI-VARRAY-1 | VARRAY stub-тег | Stub-тег есть, VARRAY-индексный доступ не трекается |

**Итого S3 scope: 7 KI items** (3 новых + 4 перенесённых из S2).

---

## Sprint 3 — Новые KI items

| ID | Конструкция | Приоритет |
|----|-------------|-----------|
| KI-COLLECT-1 | MULTISET + COLLECT aggregate (Oracle 12c+) | Средний |
| KI-PIVOT-1 | PIVOT / UNPIVOT — column derivation | Средний |
| KI-MERGE-2 | MERGE USING SOURCE.* (S1.BUG-4, отложено) | Высокий |

---

## Детальные спецификации

---

### KI-JSON-1 — JSON_TABLE / JSON_VALUE / JSON_QUERY

**Приоритет:** ⭐⭐⭐ (встречается в 40+ файлах корпуса)

**Проблема:**

```sql
SELECT j.order_id, j.amount
FROM orders o,
     JSON_TABLE(o.payload, '$.items[*]'
       COLUMNS (order_id NUMBER PATH '$.id',
                amount    NUMBER PATH '$.amount')
     ) j
WHERE j.amount > 1000
```

Текущее поведение: `JSON_TABLE` не распознаётся как source. Колонки `j.order_id`, `j.amount` остаются `PENDING`.

**Требуемое поведение:**

1. `JSON_TABLE(col_expr, path COLUMNS ...)` → виртуальная `DaliTable`-like вершина `JsonTable_<alias>` с `DaliColumn`-детьми согласно `COLUMNS` clause
2. Рёбра `READS_FROM` от Statement к `JsonTable_<alias>`
3. `DATA_FLOW` от JsonTable-колонок к выходным SELECT-колонкам

**YGG топология:**

```
DaliStatement --[READS_FROM]--> DaliJsonTable("JSON_TABLE_j")
DaliJsonTable --[HAS_COLUMN]--> DaliColumn("j.order_id")
DaliJsonTable --[HAS_COLUMN]--> DaliColumn("j.amount")
DaliStatement --[DATA_FLOW]--> DaliOutputColumn (order_id) source: j.order_id
```

**Новые типы в YGG:**

```java
// RemoteSchemaCommands.java — добавить
"CREATE VERTEX TYPE DaliJsonTable IF NOT EXISTS"
"CREATE EDGE TYPE JSON_TABLE_HAS_COLUMN IF NOT EXISTS"
```

**Алгоритм:**

```
Фаза 1 (Grammar):
  PlSqlLexer.g4 / PlSqlParser.g4 — проверить наличие json_table_clause правила
  Если нет → добавить в grammar (риск: parser regeneration, ~2ч)

Фаза 2 (Listener):
  PlSqlSemanticListener.enterJson_table_clause():
    1. Создать виртуальный алиас (или использовать J)
    2. Для каждой COLUMNS (col_name TYPE PATH '...')→ зарегистрировать в localAliasMap
    3. emitJsonTableVertex(alias, columns)

Фаза 3 (ResolutionPass):
  Pass 3 → при встрече col_ref типа alias.col_name → check localAliasMap → JsonTable match
```

**Estimate:** 3–4 дня. Риск: grammar изменение может сломать другие тесты.

---

### KI-NESTREC-1 — Nested records (multi-level)

**Приоритет:** ⭐⭐⭐ (геоид-чистота для сложных типов)

**Проблема:**

```sql
TYPE address_t IS RECORD (
  street VARCHAR2(100),
  city   VARCHAR2(50),
  zip    address_zip_t   -- ← nested record
);
TYPE address_zip_t IS RECORD (
  code     VARCHAR2(10),
  country  VARCHAR2(2)
);
```

Текущее поведение: `address_t.zip` создаётся как `DaliRecordField` с типом `address_zip_t`, но поля второго уровня (`zip.code`, `zip.country`) не создаются.

**Требуемое поведение:**

```
DaliRecord("address_t")
  └─[HAS_RECORD_FIELD]─► DaliRecordField("street")
  └─[HAS_RECORD_FIELD]─► DaliRecordField("city")
  └─[HAS_RECORD_FIELD]─► DaliRecordField("zip") ← тип=address_zip_t
      └─[HAS_NESTED_RECORD]─► DaliRecord("address_zip_t")
          └─[HAS_RECORD_FIELD]─► DaliRecordField("code")
          └─[HAS_RECORD_FIELD]─► DaliRecordField("country")
```

**Новые типы в YGG:**

```java
"CREATE EDGE TYPE HAS_NESTED_RECORD IF NOT EXISTS"
```

**Алгоритм:**

```
RecordTypeListener.enterRecord_type_def():
  1. Для каждого поля — проверить isRecordType(fieldType)
  2. Если да → рекурсивно registerRecordType(fieldType)
     (защита от циклов: Set<String> resolved в scope)
  3. Эмитировать HAS_NESTED_RECORD ребро при записи в YGG
```

**Estimate:** 1.5 дня. Хорошо покрывается тестами.

---

### KI-LATERAL-1 — LATERAL joins + NameResolver S9

**Приоритет:** ⭐⭐ (редкий, но важен для CTE-тяжёлых процедур)

**Проблема:**

```sql
SELECT e.name, d.dept_budget
FROM employees e,
     LATERAL (
       SELECT SUM(budget) AS dept_budget
       FROM departments
       WHERE mgr_id = e.emp_id   -- ← ссылка на внешний alias e
     ) d
```

Pass 3 не имеет `NameResolver S9` — механизма корреляции ссылки `e.emp_id` во вложенном SELECT с внешним FROM alias `e`.

**Требуемое поведение:**

1. `LATERAL(subquery)` создаёт виртуальный scope, который наследует внешние алиасы
2. `e.emp_id` во вложенном SELECT резолвится в `employees.emp_id` из внешнего FROM
3. `DATA_FLOW` ребро: `employees.emp_id` → фильтр → `LATERAL` subquery output

**NameResolver S9 spec:**

```
S9 = LATERAL correlation resolver
Входные данные: список внешних алиасов (из enclosing FROM)
Область действия: только WHERE/JOIN ON в subquery
Ограничения:
  - не применяется к SELECT list вложенного запроса (это S8)
  - циклические корреляции (LATERAL в LATERAL) — stub-тег на первой итерации
```

**Estimate:** 2–3 дня. Требует рефакторинга ScopeStack.

---

### KI-WITHFUNC-1 — WITH FUNCTION geoid нормализация

**Приоритет:** ⭐⭐

**Проблема:**

```sql
WITH FUNCTION discount(p NUMBER) RETURN NUMBER IS
  BEGIN RETURN p * 0.9; END;
SELECT discount(price) AS discounted_price FROM products
```

Функция `discount` объявлена inline в WITH clause. Текущее поведение: функция получает временный geoid вида `WITHFUNC_<hash>`, который меняется при каждом пересчёте — де-дупликация не работает.

**Требуемое поведение:**

Geoid = `WITHFUNC_{parent_stmt_geoid}.discount` — детерминирован от геоида родительского
оператора и имени функции. Уникален в рамках одного SQL statement.

**Пример:**

```
родительский Statement geoid = "HR.PROC_INIT.3"
WITH FUNCTION discount → geoid = "WITHFUNC.HR.PROC_INIT.3.discount"
```

**Estimate:** 0.5 дня.

---

### KI-DBLINK-1 — Database links @dblink (partial → full)

**Приоритет:** ⭐

**Текущее состояние (S2 partial):**

`HR.EMPLOYEES@REMOTE_DB` распознаётся, алиас `REMOTE_DB` логируется как `EXTERNAL_SOURCE`.
Но `REMOTE_DB.HR.EMPLOYEES` как отдельная DaliTable-вершина не создаётся, рёбра не строятся.

**Требуемое поведение:**

```
DaliStatement --[READS_FROM]--> DaliExternalTable("HR.EMPLOYEES@REMOTE_DB")
DaliExternalTable.source_type = "dblink"
DaliExternalTable.dblink_name = "REMOTE_DB"
```

**Estimate:** 1 день.

---

### KI-VARRAY-1 — VARRAY indexed access

**Приоритет:** ⭐

**Проблема:** `my_array(i).column_name` — индексный доступ к VARRAY элементу с разыменованием поля.
Текущее: stub-тег `VARRAY_FIELD_ACCESS`.

**Требуемое поведение:** разыменование через `%TYPE` элемента VARRAY → реальная колонка.

**Estimate:** 1.5 дня. Требует расширения `VarRefResolver`.

---

### KI-MERGE-2 — MERGE USING SOURCE.* (S1.BUG-4)

**Приоритет:** ⭐⭐⭐ (bug, не feature)

**Проблема:**

```sql
MERGE INTO target t
USING source s ON (t.id = s.id)
WHEN MATCHED THEN UPDATE SET t.val = s.val   -- ← s.val не резолвится
WHEN NOT MATCHED THEN INSERT (id, val) VALUES (s.id, s.val)
```

`s.val` в WHEN MATCHED и INSERT ... VALUES не получает `DATA_FLOW` ребро от source table.

**Причина:** `MergeStatementListener` не передаёт source alias в resolver для WHEN MATCHED/NOT MATCHED ветки.

**Файл:** `PlSqlSemanticListener.enterMerge_statement()` → передать `sourceAlias` в контекст.

**Estimate:** 0.5 дня. Хорошо изолировано.

---

## Приоритизированный порядок реализации S3

```
Неделя 1:
  1. KI-MERGE-2   — 0.5 дня  (bug, блокирует чистоту корпуса)
  2. KI-WITHFUNC-1 — 0.5 дня (geoid fix, быстро)
  3. KI-DBLINK-1  — 1 день
  4. KI-VARRAY-1  — 1.5 дня

Неделя 2:
  5. KI-NESTREC-1 — 1.5 дня
  6. KI-LATERAL-1 — 3 дня   (требует S9 resolver rework)

Неделя 3:
  7. KI-JSON-1    — 4 дня   (grammar + listener + new vertex types)
  8. KI-XML-1     — 2 дня   (если время позволяет)
```

**Target resolution rate после S3:** 99.5%+

---

## QG после Sprint 3

| QG | Условие |
|---|---------|
| QG-DALI-ygg-write | KI-MERGE-2 fix → `MERGE s.*` атомы резолвятся |
| QG-HOUND-listener-chain | JSON_TABLE → `DaliJsonTable` вершины в YGG |
| QG-DALI-resolution-rate | 143K atoms → 99.5% resolved |

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 16.04.2026 | 1.0 | Initial S3 spec. 7 перенесённых KI items + 3 новых. Детальные алгоритмы для JSON_TABLE, nested records, LATERAL S9. Estimate: 10–12 дней. |
