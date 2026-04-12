# SHUTTLE · Задача LOOM-024-BE
# Регистрация DaliApplication и DaliDatabase в GraphQL API

**Дата:** 2026-04-04
**Автор:** LOOM
**Адресат:** SHUTTLE (Quarkus lineage-api, порт 8080)
**Приоритет:** 🔴 Критический
**Блокирует:** LOOM-024 (полное завершение — переход от синтетической группировки к реальным данным)

---

## Контекст: что сделано на стороне LOOM

LOOM-024 реализован на фронте в режиме **синтетической заглушки**:

```
Текущее поведение:
  overview() → SchemaNode[]  (плоский список схем)
  LOOM делит на группы: каждые 5 схем = 1 DB ("HoundDB-N"), каждые 2 DB = 1 Application
  Все имена DB/Application придуманы, не реальные
```

Как только SHUTTLE вернёт реальную иерархию — `transformGqlOverview` на фронте
переключится на реальные ID, имена и группировку. Синтетика уйдёт.

---

## Что уже известно о данных в ArcadeDB

Из `OverviewService.java` (подтверждено на живой БД 2026-04-04):

```
Вершина DaliSchema:
  schema_name    — имя схемы
  schema_geoid   — собственный глобальный ID
  database_geoid — ← ключевое поле, ссылается на DaliDatabase

Рёбра из DaliSchema:
  CONTAINS_TABLE   → DaliTable
  CONTAINS_PACKAGE → DaliPackage
  CONTAINS_ROUTINE → DaliRoutine
```

`database_geoid` уже есть. Это значит `DaliDatabase` в графе существует (или планировался).

---

## Что нужно выяснить (разведочные запросы к ArcadeDB)

### 1. Убедиться, что DaliDatabase существует как класс

```sql
SELECT name, superClass
FROM (SELECT expand(classes) FROM metadata:schema)
WHERE name IN ['DaliDatabase', 'DaliApplication']
```

### 2. Узнать свойства DaliDatabase

```sql
SELECT properties.name, properties.type
FROM (SELECT expand(classes) FROM metadata:schema)
WHERE name = 'DaliDatabase'
```

```sql
-- Посмотреть живой пример записи
SELECT @rid, @class, *
FROM DaliDatabase
LIMIT 3
```

### 3. Узнать как Database связан с Application

```sql
-- Какие рёбра выходят из DaliDatabase?
SELECT out().@class AS targets, outE().@class AS edgeTypes
FROM DaliDatabase
LIMIT 5
```

```sql
-- Существуют ли DaliApplication вершины?
SELECT @rid, @class, *
FROM DaliApplication
LIMIT 5
```

### 4. Проверить что database_geoid в схемах рабочий

```sql
-- Найти пару Schema → Database через geoid
SELECT
    s.schema_name      AS schemaName,
    s.database_geoid   AS dbGeoid,
    db.@rid            AS dbRid
FROM DaliSchema s
LET db = (SELECT FROM DaliDatabase WHERE @rid = s.database_geoid)
LIMIT 5
```

---

## Контракт, который нужен LOOM

### Вариант A — Минимальный (рекомендуется для старта)

Добавить поля `databaseGeoid`, `databaseName`, `applicationGeoid`, `applicationName`
к существующему `SchemaNode`. Текущий `overview` query остаётся, просто возвращает больше полей.

**Новый Java record:**

```java
// studio.seer.lineage.model.SchemaNode
@Description("Schema with optional DB/Application parent refs for L1 hierarchy")
public record SchemaNode(
    String id,
    String name,
    int    tableCount,
    int    routineCount,
    int    packageCount,

    // NEW — null если не зарегистрировано в графе
    @Nullable String databaseGeoid,
    @Nullable String databaseName,
    @Nullable String databaseEngine,   // "PostgreSQL", "Oracle", etc. или пустая строка

    @Nullable String applicationGeoid,
    @Nullable String applicationName
) {}
```

**Новый ArcadeDB SQL в `OverviewService.overview()`:**

```sql
SELECT
    s.@rid                             AS rid,
    s.schema_name                      AS schema_name,
    s.out('CONTAINS_TABLE').size()     AS tableCount,
    s.out('CONTAINS_PACKAGE').size()   AS packageCount,
    s.out('CONTAINS_ROUTINE').size()   AS routineCount,
    db.@rid                            AS databaseGeoid,   -- совпадает с именем поля в SchemaNode
    db.database_name                   AS databaseName,
    db.database_engine                 AS databaseEngine,
    app.@rid                           AS applicationGeoid, -- совпадает с именем поля в SchemaNode
    app.app_name                       AS applicationName
FROM DaliSchema s
LEFT JOIN DaliDatabase    db  ON db.@rid  = s.database_geoid
LEFT JOIN DaliApplication app ON ...      -- уточнить edge-тип после разведки
ORDER BY app.app_name, db.database_name, s.schema_name
```

SQL-алиасы (`databaseGeoid`, `applicationGeoid`) намеренно совпадают с именами полей Java record —
маппер читает `str(row, "databaseGeoid")` напрямую без переименований.

> ⚠️ `LEFT JOIN DaliApplication` — JOIN-условие уточняется по результатам разведочных запросов выше.
> Возможные варианты:
> - `db` имеет `application_geoid` property → `JOIN DaliApplication app ON app.@rid = db.application_geoid`
> - Ребро `DaliDatabase -[BELONGS_TO]-> DaliApplication` → `db.in('BELONGS_TO')` или `db.out('OWNED_BY')`

**GraphQL response — что LOOM ожидает получить (пример):**

```json
{
  "overview": [
    {
      "id": "#13:0",
      "name": "public",
      "tableCount": 22,
      "routineCount": 8,
      "packageCount": 2,
      "databaseGeoid": "#12:0",
      "databaseName": "orders_db",
      "databaseEngine": "PostgreSQL",
      "applicationGeoid": "#11:0",
      "applicationName": "OrderService"
    },
    {
      "id": "#13:1",
      "name": "reporting",
      "tableCount": 5,
      "routineCount": 0,
      "packageCount": 0,
      "databaseGeoid": "#12:0",
      "databaseName": "orders_db",
      "databaseEngine": "PostgreSQL",
      "applicationGeoid": "#11:0",
      "applicationName": "OrderService"
    },
    {
      "id": "#13:2",
      "name": "analytics",
      "tableCount": 18,
      "routineCount": 3,
      "packageCount": 0,
      "databaseGeoid": null,       ← схема без БД → LOOM покажет "HoundDB"
      "databaseName": null,
      "databaseEngine": null,
      "applicationGeoid": null,    ← DB без App → LOOM покажет DB standalone
      "applicationName": null
    }
  ]
}
```

---

### Вариант B — Полноценный (после Варианта A)

Отдельный query `overviewHierarchy` с вложенной структурой.
Нужен для Phase 3 когда Application/Database станут кликабельными объектами
с собственными KNOT Inspector данными.

```
query OverviewHierarchy {
  overviewHierarchy {
    applications {
      id
      name
      databases {
        id
        name
        engine
        schemas {
          id  name  tableCount  routineCount  packageCount
        }
      }
    }
    orphanDatabases {    ← DaliDatabase без Application
      id  name  engine
      schemas { ... }
    }
  }
}
```

> Вариант B блокирует переход Application/Database на кликабельные ноды в KNOT Inspector.
> Вариант A достаточен для закрытия LOOM-024.

---

## Что LOOM сделает после получения реальных данных

Как только `SchemaNode` будет содержать `databaseGeoid`/`applicationGeoid`:

```typescript
// transformGraph.ts — вместо синтетической группировки:
// Сейчас:
//   каждые 5 схем → 1 DB ("HoundDB-N")
//   каждые 2 DB   → 1 Application ("System-N")

// После:
//   group by applicationGeoid (null → standalone/orphan)
//   group by databaseGeoid    (null → stub "HoundDB")
//   ApplicationNode.label = applicationName (реальное имя)
//   DatabaseNode.label     = databaseName   (реальное имя, не "HoundDB")
```

Синтетическая заглушка останется только для `applicationName == null && databaseName == null`.

---

## Новые типы нод, которые LOOM будет использовать

После получения реальных данных на canvas появятся:

| Тип | DaliNodeType | Описание |
|-----|-------------|----------|
| `ApplicationNode` | `'DaliApplication'` | RF group parent, dashed border |
| `DatabaseNode` | `'DaliDatabase'` | RF child inside App, или standalone |
| `L1SchemaNode` | `'DaliSchema'` | RF grandchild inside DB, скрыт до раскрытия |

При double-click:
- `DaliApplication` → L1 scope filter (остаёмся на L1, dims остальные App)
- `DaliDatabase` → drill-down L2 (переход на L2 для этой БД)
- `DaliSchema` → drill-down L2 (переход на L2 для этой схемы)

---

## Что нужно от SHUTTLE для закрытия LOOM-024

### Минимум для DONE

- [ ] Разведочные запросы выполнены, свойства DaliDatabase задокументированы
- [ ] `SchemaNode` record расширен: `databaseGeoid`, `databaseName`, `databaseEngine`, `applicationGeoid`, `applicationName`
- [ ] `OverviewService.overview()` заполняет новые поля из ArcadeDB (null если нет данных)
- [ ] GraphQL query `overview` возвращает новые поля (backward compatible — старые поля на месте)
- [ ] Проверено на живой БД: хотя бы одна схема возвращает реальный `databaseGeoid`

### Хорошо бы иметь (для LOOM-030 Impact Analysis)

- [ ] Свойства DaliDatabase задокументированы: `database_name`, `database_engine`, edge-типы
- [ ] Свойства DaliApplication задокументированы: `app_name`, edge к DaliDatabase

---

## Что мешает сделать самостоятельно

LOOM не имеет прямого доступа к ArcadeDB (только через SHUTTLE GraphQL).
Неизвестно:
- Существуют ли вершины `DaliApplication` в hound DB прямо сейчас
- Каким property/edge `DaliDatabase` связана с `DaliApplication`
- Точное имя property `database_name` в DaliDatabase (может быть `db_name`, `name`, etc.)

Ответы на эти вопросы — только у SHUTTLE через живой ArcadeDB.

---

## Acceptance Criteria для LOOM-024 (backend часть)

```gherkin
Given: SHUTTLE возвращает overview с databaseName != null для хотя бы 1 схемы
When:  LOOM загружает L1 canvas
Then:
  - DatabaseNode показывает реальное имя из databaseName (не "HoundDB")
  - Если applicationName != null — схемы сгруппированы внутри ApplicationNode
  - Если applicationName == null — DatabaseNode показывается standalone
  - Если databaseName == null — DatabaseNode показывается как "HoundDB" (заглушка)
```
