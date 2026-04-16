# Hound — PostgreSQL Dialect Spec

**Документ:** `HOUND_POSTGRESQL_SPEC`
**Версия:** 1.0
**Дата:** 16.04.2026
**Статус:** DRAFT — PostgreSQL semantic listener не реализован
**Автор:** Sprint 3 candidate

---

## 1. Текущий статус

| Компонент | Статус |
|-----------|--------|
| ANTLR grammar (`PostgreSQLLexer.g4` + `PostgreSQLParser.g4`) | ✅ Есть в `libraries/hound/src/main/resources/grammars/sql/postgresql/` |
| Lexer / Parser Java-генерация | ✅ `PostgreSQLLexer.java`, `PostgreSQLLexerBase.java` |
| Semantic Listener (lineage extraction) | ❌ Не реализован |
| `HoundConfig.dialect = "POSTGRESQL"` | ⚠️ Парсится, но lineage не строится |
| Тесты | ❌ Нет |

**Итог:** PostgreSQL можно указать как dialect, но lineage-семантика не реализована.
Это блокер Sprint 3 для задачи «PostgreSQL dialect».

---

## 2. Отличия PostgreSQL от PL/SQL

### 2.1 Ключевые языковые различия

| Конструкция | PL/SQL (Oracle) | PostgreSQL (PL/pgSQL) |
|------------|------------------|-----------------------|
| Блок программы | `BEGIN ... END;` | `BEGIN ... END;` ✓ |
| Процедура | `CREATE OR REPLACE PROCEDURE` | `CREATE OR REPLACE PROCEDURE` ✓ |
| Функция | `CREATE OR REPLACE FUNCTION ... RETURN type` | `CREATE OR REPLACE FUNCTION ... RETURNS type` |
| OUT-параметры | `OUT param type` | `OUT param type` ✓ |
| BULK COLLECT | `BULK COLLECT INTO coll` | ❌ нет (используется `ARRAY_AGG` / `SELECT INTO arr`) |
| RETURNING INTO | `RETURNING col INTO var` | `RETURNING col INTO var` ✓ |
| %ROWTYPE | `table%ROWTYPE` | `table%ROWTYPE` ✓ |
| %TYPE | `col%TYPE` | `col%TYPE` ✓ |
| Record type | `TYPE rec IS RECORD(...)` | Нет (используются composite types + `RECORD`) |
| Packages | `CREATE PACKAGE` / `CREATE PACKAGE BODY` | ❌ нет |
| Exception handling | `EXCEPTION WHEN ... THEN` | `EXCEPTION WHEN ... THEN` ✓ |
| Dynamic SQL | `EXECUTE IMMEDIATE` | `EXECUTE format(...)` |
| PIPE ROW | `PIPE ROW(val)` | ❌ нет (используются `SETOF` + `RETURN NEXT`) |
| Pipelined functions | `PIPELINED` | `RETURNS SETOF type` / `RETURNS TABLE(...)` |
| Cursor FOR loop | `FOR rec IN cursor LOOP` | `FOR rec IN query LOOP` ✓ |
| JSONB | ❌ | ✅ специфично для PostgreSQL |
| CTE (WITH) | ✓ | ✓ |
| Window functions | ✓ | ✓ |

### 2.2 Специфика PostgreSQL, требующая отдельной логики

| Конструкция | Сложность | Приоритет |
|------------|-----------|-----------|
| `RETURNS TABLE(col1 type, col2 type)` | Средняя | Высокий |
| `RETURN NEXT` / `RETURN QUERY` | Средняя | Высокий |
| Composite types (`CREATE TYPE AS`) | Высокая | Средний |
| `PERFORM statement` (discard result) | Низкая | Высокий |
| `FOUND` / `ROW_COUNT` special variables | Низкая | Средний |
| Dollar-quoted strings `$$...$$ ` | Низкая (уже в grammar) | Высокий |
| `RAISE NOTICE/WARNING/EXCEPTION` | Низкая | Низкий |
| Extensions (`CREATE EXTENSION`) | Средняя | Низкий |
| `JSONB` operators (`->`, `->>`, `@>`) | Средняя | Средний |
| Lateral subqueries | Высокая | Средний |

---

## 3. Архитектура реализации

### 3.1 Как устроен PL/SQL listener (эталон)

```
PlSqlParser.g4
    │
    ▼
AstListener.java          ← dispatcher, вызывает правильный dialect listener
    │
    ▼
PlSqlSemanticListener.java  ← 4-pass implementation
    ├── Pass 1: type declarations + tables
    ├── Pass 2: routine signatures (без тел)
    ├── Pass 3: routine bodies — основной resolve
    └── Pass 4: PENDING atoms — batch LOOKUP в YGG
```

### 3.2 Требуемая реализация PostgreSQL listener

Создать по аналогии с `PlSqlSemanticListener`:

```
PostgreSQLParser.g4 (уже есть)
    │
    ▼
AstListener.java — добавить ветку:
    if (dialect == "POSTGRESQL") return new PostgreSqlSemanticListener(...)

    ▼
PostgreSqlSemanticListener.java  ← новый класс
    ├── Pass 1: CREATE TABLE, CREATE TYPE, type aliases
    ├── Pass 2: функции и процедуры — сигнатуры
    ├── Pass 3: тела — SELECT/INSERT/UPDATE/DELETE lineage
    └── Pass 4: PENDING atoms
```

**Ключевые входные точки grammar:**

| Grammar rule | Что обрабатывать |
|-------------|-----------------|
| `createfunction` | Функция/процедура → DaliRoutine |
| `selectstmt` | SELECT → DaliStatement + output columns |
| `insertstmt` | INSERT → DaliStatement |
| `updatestmt` | UPDATE → DaliStatement + affected columns |
| `deletestmt` | DELETE → DaliStatement |
| `returnstmt` | RETURN / RETURN NEXT / RETURN QUERY |
| `plpgsql_block` | BEGIN..END → scan for statements |
| `forstmt` | FOR rec IN cursor LOOP |
| `execsql` | EXECUTE (dynamic SQL) → stub |

### 3.3 Shared компоненты (не нужно переписывать)

| Компонент | Переиспользуется? |
|-----------|-------------------|
| `HoundConfig` | ✅ dialect = "POSTGRESQL" |
| `DaliAtom`, `DaliOutputColumn` и все vertex-типы | ✅ |
| `JsonlBatchBuilder` (REMOTE_BATCH write) | ✅ |
| `RemoteSchemaCommands` (YGG schema) | ✅ |
| Pass 4 PENDING resolution | ✅ с минимальными изменениями |
| `HeimdallEmitter` events | ✅ |

---

## 4. Оценка трудозатрат

| Задача | Оценка | Зависимость |
|--------|--------|-------------|
| `PostgreSqlSemanticListener` — каркас + Pass 1/2 | 3 дня | — |
| Pass 3: SELECT/INSERT/UPDATE/DELETE | 4 дня | Pass 1/2 |
| Pass 4: PENDING atoms | 1 день | Pass 3 |
| RETURNS TABLE / RETURN NEXT | 2 дня | Pass 2/3 |
| Composite types | 2 дня | Pass 1 |
| Тесты (корпус 10+ PostgreSQL файлов) | 2 дня | Pass 1-4 |
| **ИТОГО** | **~14 дней** | |

---

## 5. Тестовый корпус

Для Sprint 3 подготовить минимальный корпус PostgreSQL:

```
test/resources/postgresql/
├── basic_functions.sql    # простые функции, RETURNS type
├── setof_functions.sql    # RETURNS SETOF + RETURN NEXT
├── returns_table.sql      # RETURNS TABLE(col type,...)
├── composite_types.sql    # CREATE TYPE AS + %ROWTYPE
├── dml_lineage.sql        # INSERT SELECT FROM, UPDATE ... RETURNING
├── cte_functions.sql      # WITH ... AS (...) SELECT
└── jsonb_ops.sql          # JSONB операторы (stub)
```

Минимальный порог принятия: **resolution rate ≥ 90%** на базовых конструкциях.

---

## 6. Открытые вопросы

| Q | Вопрос | Статус |
|---|--------|--------|
| Q-PG-1 | Нужен ли отдельный HoundConfig.vendor = "postgresql" или хватает dialect? | Открыт |
| Q-PG-2 | Как обрабатывать extensions (PostGIS, TimescaleDB)? | Deferred |
| Q-PG-3 | Dollar-quoting в grammar уже обрабатывается корректно? | Требует проверки |
| Q-PG-4 | Composite types: вершина DaliRecord или новый тип? | Открыт |

---

## История изменений

| Дата | Версия | Что |
|------|--------|-----|
| 16.04.2026 | 1.0 | DRAFT. Таблица отличий PL/SQL vs PL/pgSQL. Архитектура listener. Оценка 14 дней. |
