# YGG Schema Reference

**Документ:** `YGG_SCHEMA_REFERENCE`
**Версия:** 1.0
**Дата:** 16.04.2026
**Статус:** ACTIVE
**База данных:** ArcadeDB 26.3.2, имя БД: `hound`

> YGG — lineage-граф. Хранит семантическую структуру SQL-кода: таблицы, колонки,
> рутины, операторы, атомы, потоки данных. Инициализируется
> `SchemaInitializer` → `RemoteSchemaCommands.all()`.

---

## 1. Vertex-типы

### Пространство имён

| Тип | Описание | Ключевые свойства |
|-----|----------|-------------------|
| `DaliApplication` | Корневой узел приложения | `name`, `createdAt` |
| `DaliDatabase` | База данных (Oracle SID, PostgreSQL database) | `name`, `vendor` |
| `DaliSchema` | Схема внутри БД (напр. `HR`, `PUBLIC`) | `name`, `database` |

### Структурные узлы

| Тип | Описание | Ключевые свойства |
|-----|----------|-------------------|
| `DaliTable` | Таблица или VIEW | `name`, `schema`, `qualifiedName`, `isView` |
| `DaliColumn` | Колонка таблицы | `name`, `table`, `dataType`, `position`, `nullable` |
| `DaliRoutine` | Процедура, функция, триггер | `name`, `schema`, `routineType`, `qualifiedName` |
| `DaliPackage` | PL/SQL Package (extends DaliRoutine) | `name`, `schema`, `qualifiedName`, `specSource`, `bodySource` |
| `DaliParameter` | Параметр рутины | `name`, `direction` (IN/OUT/IN OUT), `dataType`, `position` |
| `DaliVariable` | Локальная переменная рутины | `name`, `dataType`, `scope` |

### Операторы и атомы

| Тип | Описание | Ключевые свойства |
|-----|----------|-------------------|
| `DaliStatement` | SQL-оператор (SELECT, INSERT, UPDATE, DELETE, MERGE…) | `stmtType`, `routineQName`, `lineStart`, `lineEnd`, `text` |
| `DaliAtom` | Минимальная семантическая единица (ссылка на колонку, таблицу) | `geoid`, `atomType`, `rawText`, `resolved`, `qualifiedName` |
| `DaliOutputColumn` | Колонка в SELECT-списке | `alias`, `expression`, `position`, `resolved` |
| `DaliJoin` | JOIN-операция | `joinType` (INNER/LEFT/RIGHT/FULL), `condition` |
| `DaliAffectedColumn` | Колонка, затронутая UPDATE/DELETE/MERGE | `name`, `table` |

### Расширенные типы (v27 / Sprint 2)

| Тип | Описание | Ключевые свойства |
|-----|----------|-------------------|
| `DaliRecord` | PL/SQL record (%ROWTYPE, TYPE … IS RECORD) | `name`, `recordType`, `scope` |
| `DaliRecordField` | Именованное поле DaliRecord | `name`, `fieldType`, `position` |
| `DaliDDLStatement` | DDL-оператор (ALTER TABLE, CREATE INDEX…) | `ddlType`, `targetObject`, `text` |

### Сессии

| Тип | Описание | Ключевые свойства |
|-----|----------|-------------------|
| `DaliSession` | Сессия парсинга | `id`, `status`, `dialect`, `source`, `startedAt`, `completedAt`, `atomCount`, `resolutionRate` |

---

## 2. Edge-типы

### 2.1 Пространство имён

| Edge | Source → Target | Описание |
|------|-----------------|----------|
| `BELONGS_TO_APP` | DaliDatabase → DaliApplication | БД принадлежит приложению |
| `CONTAINS_SCHEMA` | DaliDatabase → DaliSchema | БД содержит схему |
| `CONTAINS_TABLE` | DaliSchema → DaliTable | Схема содержит таблицу |
| `HAS_COLUMN` | DaliTable → DaliColumn | Таблица содержит колонку |
| `CONTAINS_ROUTINE` | DaliSchema → DaliRoutine | Схема содержит рутину |
| `CONTAINS_STMT` | DaliRoutine → DaliStatement | Рутина содержит оператор |

### 2.2 Структура операторов

| Edge | Source → Target | Описание |
|------|-----------------|----------|
| `HAS_OUTPUT_COL` | DaliStatement → DaliOutputColumn | SELECT → output columns |
| `HAS_ATOM` | DaliStatement → DaliAtom | Оператор содержит атом |
| `HAS_JOIN` | DaliStatement → DaliJoin | Оператор содержит JOIN |
| `HAS_PARAMETER` | DaliRoutine → DaliParameter | Рутина → параметр |
| `HAS_VARIABLE` | DaliRoutine → DaliVariable | Рутина → локальная переменная |
| `CHILD_OF` | DaliStatement → DaliStatement | Вложенный оператор (subquery) |

### 2.3 Lineage / DataFlow

| Edge | Source → Target | Описание |
|------|-----------------|----------|
| `DATA_FLOW` | DaliAtom/DaliOutputColumn → DaliAtom/DaliOutputColumn | Поток данных между колонками |
| `READS_FROM` | DaliStatement → DaliTable | Оператор читает таблицу |
| `WRITES_TO` | DaliStatement → DaliTable | Оператор пишет в таблицу |
| `FILTER_FLOW` | DaliAtom → DaliAtom | WHERE-условие участвует в lineage |
| `JOIN_FLOW` | DaliAtom → DaliAtom | JOIN-предикат как источник данных |
| `UNION_FLOW` | DaliOutputColumn → DaliOutputColumn | UNION-ветка → итоговая колонка |
| `USES_SUBQUERY` | DaliStatement → DaliStatement | Оператор использует подзапрос |
| `NESTED_IN` | DaliStatement → DaliStatement | Subquery вложен в родительский |
| `ROUTINE_USES_TABLE` | DaliRoutine → DaliTable | Агрегированная связь рутина → таблица (для L2 LOOM) |

### 2.4 Вызовы

| Edge | Source → Target | Описание |
|------|-----------------|----------|
| `CALLS` | DaliStatement → DaliRoutine | EXEC / функция-вызов |

### 2.5 Разрешение атомов

| Edge | Source → Target | Описание |
|------|-----------------|----------|
| `ATOM_REF_TABLE` | DaliAtom → DaliTable | Атом ссылается на таблицу |
| `ATOM_REF_COLUMN` | DaliAtom → DaliColumn | Атом ссылается на колонку |
| `ATOM_REF_STMT` | DaliAtom → DaliStatement | Атом ссылается на подзапрос |
| `ATOM_REF_OUTPUT_COL` | DaliAtom → DaliOutputColumn | Атом ссылается на output column |
| `ATOM_PRODUCES` | DaliAtom → DaliOutputColumn | Атом является источником для output column |
| `ATOM_REF_DDL` | DaliAtom → DaliDDLStatement | Атом ссылается на DDL |

### 2.6 Records (Sprint 2 / v27)

| Edge | Source → Target | Описание |
|------|-----------------|----------|
| `BULK_COLLECTS_INTO` | DaliStatement → DaliRecord | BULK COLLECT INTO запись |
| `RECORD_USED_IN` | DaliRecord → DaliStatement | Запись используется в операторе |
| `HAS_RECORD_FIELD` | DaliRecord → DaliRecordField | Запись содержит поле |
| `RETURNS_INTO` | DaliStatement → DaliRecord | RETURNING INTO запись |

### 2.7 DDL (Sprint 2 / v27)

| Edge | Source → Target | Описание |
|------|-----------------|----------|
| `DaliDDLModifiesTable` | DaliDDLStatement → DaliTable | DDL изменяет таблицу |
| `DaliDDLModifiesColumn` | DaliDDLStatement → DaliColumn | DDL изменяет колонку |

### 2.8 Affected columns (UPDATE/DELETE)

| Edge | Source → Target | Описание |
|------|-----------------|----------|
| `HAS_AFFECTED_COL` | DaliStatement → DaliAffectedColumn | UPDATE SET col=... |
| `AFFECTED_COL_REF_TABLE` | DaliAffectedColumn → DaliTable | Затронутая колонка → таблица |

### 2.9 Joins

| Edge | Source → Target | Описание |
|------|-----------------|----------|
| `JOIN_SOURCE_TABLE` | DaliJoin → DaliTable | Левая сторона JOIN |
| `JOIN_TARGET_TABLE` | DaliJoin → DaliTable | Правая сторона JOIN |

### 2.10 Pipelined functions

| Edge | Source → Target | Описание |
|------|-----------------|----------|
| `PIPES_FROM` | DaliStatement → DaliRoutine | TABLE(pipelined_func()) → рутина |

---

## 3. Итого: статистика типов

| Категория | Кол-во типов |
|-----------|-------------|
| Vertex-типов | 16 |
| Edge-типов | 28 |
| Всего | **44** |

---

## 4. Индексы

Создаются `SchemaInitializer` при старте Hound. Ключевые:

| Индекс | Тип | Поля | Описание |
|--------|-----|------|----------|
| `DaliAtom_geoid` | UNIQUE | `geoid` | Стабильный детерминированный id атома |
| `DaliAtom_qualifiedName` | NOTUNIQUE | `qualifiedName` | Поиск атома по имени |
| `DaliTable_schema` | NOTUNIQUE | `schema` | Поиск таблиц по схеме |
| `DaliTable_qualifiedName` | NOTUNIQUE | `qualifiedName` | Поиск таблицы |
| `DaliRoutine_schema` | NOTUNIQUE | `schema` | Поиск рутин по схеме |
| `DaliRoutine_qualifiedName` | NOTUNIQUE | `qualifiedName` | Поиск рутины |
| `DaliColumn_table` | NOTUNIQUE | `table` | Колонки таблицы |
| `DaliStatement_routineQName` | NOTUNIQUE | `routineQName` | Операторы рутины |
| `DaliOutputColumn_resolved` | NOTUNIQUE | `resolved` | Поиск нерезолвленных output columns |

---

## 5. Геоид (geoid)

**Определение:** Детерминированный стабильный идентификатор атома. Не меняется при пересоздании базы данных.

**Формат:**
```
{schema}.{routine_qualified_name}.{sequential_index}
```

**Пример:**
```
HR.PKG_ORDERS.PROC_CREATE_ORDER.42
```

**Свойства:**
- Уникален в рамках одной БД + dialect
- Позволяет инкрементальный пересчёт: при изменении одного файла пересчитываются только его атомы
- Используется для де-дупликации при повторном парсинге (`clearBeforeWrite=false`)

---

## 6. Быстрые SQL-запросы к YGG

```sql
-- Подсчёт вершин по типу
SELECT @type, count(*) as cnt FROM (SELECT FROM V) GROUP BY @type ORDER BY cnt DESC

-- Все атомы схемы HR
SELECT geoid, atomType, resolved FROM DaliAtom WHERE qualifiedName LIKE 'HR.%' LIMIT 100

-- Нерезолвленные атомы
SELECT geoid, rawText FROM DaliAtom WHERE resolved = false

-- Таблицы схемы PUBLIC
SELECT name, qualifiedName FROM DaliTable WHERE schema = 'PUBLIC'

-- Рутины пакета PKG_ORDERS
SELECT name, routineType FROM DaliRoutine WHERE qualifiedName LIKE 'HR.PKG_ORDERS.%'

-- Lineage: что пишет в таблицу ORDERS
SELECT expand(in('WRITES_TO')) FROM DaliTable WHERE name = 'ORDERS'

-- Output columns конкретного statement
SELECT expand(out('HAS_OUTPUT_COL')) FROM DaliStatement WHERE @rid = #12:34

-- Проверить наличие Sprint 2 edge типов
SELECT name FROM (SELECT expand(classes) FROM schema:types) WHERE name IN [
  'HAS_RECORD_FIELD', 'RETURNS_INTO', 'DaliDDLModifiesTable', 'DaliDDLModifiesColumn'
]
```

---

## 7. Известные ограничения

| Ограничение | Детали | Sprint |
|-------------|--------|--------|
| `HAS_RECORD_FIELD`, `RETURNS_INTO`, `DaliDDLModifiesTable`, `DaliDDLModifiesColumn` | Могут отсутствовать в существующих YGG-экземплярах (Sprint 1 не включал эти типы) | Sprint 2 |
| JSON_TABLE | Не парсится — атом остаётся PENDING | Sprint 3 (KI-JSON-1) |
| XMLTABLE | Аналогично JSON_TABLE | Sprint 3 (KI-XML-1) |
| Dynamic SQL (`EXECUTE IMMEDIATE`) | Stub — атомы не разрешаются | Deferred |
| Nested records (record of record) | Только первый уровень | Sprint 3 (KI-NEST-1) |

> При обновлении существующего YGG без полного пересоздания БД — выполнить
> Фикс-ET из `docs/guides/STARTUP_SEQUENCE.md §3.1`.

---

## 8. Подключение к YGG

```
URL:      http://localhost:2480
Database: hound
User:     root
Password: playwithdata   (dev; в prod — через Vault/k8s secrets)
```

ArcadeDB Studio: http://localhost:2480/studio/index.html

---

## История изменений

| Дата | Версия | Что |
|------|--------|-----|
| 16.04.2026 | 1.0 | Создан. 16 vertex-типов, 28 edge-типов, индексы, геоид, SQL-примеры. |
