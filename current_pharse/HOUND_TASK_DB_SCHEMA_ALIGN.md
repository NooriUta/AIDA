# HOUND-DB-001 — Устранение несогласованности схемы DaliDatabase / DaliSchema

**Компонент:** Hound (Java-парсер Data Lineage → ArcadeDB)
**Статус:** К выполнению
**Приоритет:** Высокий
**Дата создания:** 2026-04-05

---

## 1. Контекст и симптомы

### Симптомы

- SHUTTLE (`SearchService`) ищет базы данных по полю `db_name`, но часть вершин `DaliDatabase` содержит только поле `database_name` — они не находятся поиском.
- SHUTTLE (`OverviewService`) строит иерархию через рёбра `in('CONTAINS_SCHEMA')`, однако схемы, созданные в ad-hoc-режиме, не имеют этого ребра и «выпадают» из дерева объектов.
- В результате данные, записанные Hound в ad-hoc-режиме (когда `pool == null`), практически невидимы для SHUTTLE.

### Затронутые компоненты

| Компонент | Затронуто |
|-----------|-----------|
| Hound — метод `ensureCanonicalPool()` | Эталонный режим, работает корректно |
| Hound — ad-hoc-режим записи (встроенный путь, ~строка 363) | Несовместимые поля |
| Hound — ad-hoc-режим записи (удалённый путь, ~строка 924) | Несовместимые поля |
| SchemaInitializer | Объявляет лишние поля без пометки об устаревании |
| SHUTTLE `SearchService` | Уже исправлен на `db_name`, ожидает выравнивания Hound |
| SHUTTLE `OverviewService` | Использует `in('CONTAINS_SCHEMA')`, работает только с namespace-режимом |

---

## 2. Корневая причина

Hound создаёт вершины `DaliDatabase` двумя несовместимыми способами в зависимости от того, задан ли `pool`.

### Таблица сравнения режимов

| Атрибут / Поведение | Namespace-режим (`pool != null`) | Ad-hoc-режим (`pool == null`) |
|---|---|---|
| Метод записи | `ensureCanonicalPool()` | Встроенный: ~строка 363; Удалённый: ~строка 924 |
| Поле имени БД | `db_name` | `database_name` |
| Поле geo-идентификатора БД | `db_geoid` | `database_geoid` |
| Поле времени создания | `created_at` | — (отсутствует) |
| Поле сессии | — | `session_id` |
| Ребро к DaliApplication | `BELONGS_TO_APP` ✓ | — (отсутствует) |
| Ребро к DaliSchema | `CONTAINS_SCHEMA` ✓ | — (отсутствует) |
| Поле `db_name` в DaliSchema | ✓ | — (отсутствует) |
| Видимость для SHUTTLE | ✓ Полная | ✗ Не видна |

### Хронология схемы

- **SchemaInitializer v7** — добавлены поля `db_name`, `db_geoid` для `DaliDatabase` (namespace-режим).
- **SchemaInitializer v10** — дополнительно объявлены `database_name` и `database_geoid` для `DaliDatabase`, но эти поля никогда не были каноническими — они лишь фиксировали ad-hoc-практику записи.

Итог: в базе сосуществуют два несовместимых набора полей для одного и того же типа вершины.

---

## 3. Что нужно сделать

### Часть 1 — Выравнивание полей DaliDatabase в ad-hoc-режиме

Во всех путях записи ad-hoc-режима добавить поля `db_name` и `db_geoid`, чтобы они дублировали поведение namespace-режима. Старые поля `database_name` и `database_geoid` на данном этапе **сохраняются** (для обратной совместимости существующих данных), новые поля добавляются параллельно.

#### Встроенный путь (~строка 363)

Было:
```java
embeddedDb.newVertex("DaliDatabase")
    .set("session_id",     sid)
    .set("database_geoid", e.getKey())
    .set("database_name",  d.get("name"))
    .save();
```

Стало:
```java
embeddedDb.newVertex("DaliDatabase")
    .set("session_id",     sid)
    .set("database_geoid", e.getKey())   // сохраняем для совместимости
    .set("database_name",  d.get("name")) // сохраняем для совместимости
    .set("db_geoid",       e.getKey())   // канонический идентификатор
    .set("db_name",        d.get("name")) // каноническое имя
    .save();
```

#### Удалённый путь (~строка 924)

Было:
```java
rcmd("INSERT INTO DaliDatabase SET session_id=?, database_geoid=?, database_name=?",
     sid, e.getKey(), d.get("name"));
```

Стало:
```java
rcmd("INSERT INTO DaliDatabase SET session_id=?, database_geoid=?, database_name=?, db_geoid=?, db_name=?",
     sid, e.getKey(), d.get("name"), e.getKey(), d.get("name"));
```

---

### Часть 2 — Очистка SchemaInitializer

В `SchemaInitializer` поля `database_name` и `database_geoid` на типе `DaliDatabase` отметить как устаревшие в комментариях. Удалять их из объявления схемы пока **не нужно** — это сломает существующие данные.

```java
// SchemaInitializer — раздел DaliDatabase

// Канонические поля (namespace-режим, v7+)
declareStringProp("DaliDatabase", "db_name");
declareStringProp("DaliDatabase", "db_geoid");
declareLongProp("DaliDatabase",   "created_at");

// @deprecated — поля ad-hoc-режима, добавлены в v10.
// Сохраняются для backward-compatibility. Новый код должен использовать db_name / db_geoid.
declareStringProp("DaliDatabase", "database_name");
declareStringProp("DaliDatabase", "database_geoid");
```

---

### Часть 3 — SQL-миграция существующих данных

Выполнить однократно на каждой базе ArcadeDB (production и dev), чтобы подтянуть существующие ad-hoc-вершины к каноническому набору полей.

```sql
-- Backfill db_name и db_geoid для ad-hoc-вершин DaliDatabase,
-- у которых ещё нет канонических полей.

UPDATE DaliDatabase
SET db_name   = database_name,
    db_geoid  = database_geoid
WHERE db_name IS NULL
  AND database_name IS NOT NULL;
```

Проверка после выполнения:

```sql
-- Должна вернуть 0 строк
SELECT count(*) FROM DaliDatabase
WHERE db_name IS NULL AND database_name IS NOT NULL;
```

> Миграцию рекомендуется обернуть в транзакцию и выполнять в период минимальной нагрузки.

---

### Часть 4 — Поле `db_name` в DaliSchema (ad-hoc-режим)

В ad-hoc-режиме создания `DaliSchema` также выставить поле `db_name`, чтобы SHUTTLE `OverviewService` мог группировать схемы даже при отсутствии ребра `CONTAINS_SCHEMA`.

Источник значения: поле `database_geoid` родительской базы (оно же `e.getKey()` в контексте итерации) или ключ `db` из карты схемы, если он доступен.

#### Встроенный путь — создание DaliSchema в ad-hoc-режиме

Было:
```java
embeddedDb.newVertex("DaliSchema")
    .set("session_id",     sid)
    .set("schema_geoid",   schemaGeoid)
    .set("schema_name",    s.get("name"))
    .set("database_geoid", dbGeoid)
    .save();
```

Стало:
```java
embeddedDb.newVertex("DaliSchema")
    .set("session_id",     sid)
    .set("schema_geoid",   schemaGeoid)
    .set("schema_name",    s.get("name"))
    .set("database_geoid", dbGeoid)   // сохраняем для совместимости
    .set("db_name",        dbName)    // каноническое имя родительской БД
    .save();
```

Где `dbName` — значение поля `db_name` / `d.get("name")` родительской `DaliDatabase`, уже доступное в области видимости цикла обработки схем.

#### Удалённый путь — создание DaliSchema в ad-hoc-режиме

Аналогично встроенному: добавить `db_name=?` в INSERT и передать соответствующий аргумент.

```java
rcmd("INSERT INTO DaliSchema SET session_id=?, schema_geoid=?, schema_name=?, database_geoid=?, db_name=?",
     sid, schemaGeoid, s.get("name"), dbGeoid, dbName);
```

---

## 4. Критерии приёмки

1. **Поиск по `db_name`** — вершины `DaliDatabase`, созданные в ad-hoc-режиме, возвращаются запросом SHUTTLE `SearchService` (`WHERE db_name LIKE '%s'`) наравне с namespace-вершинами.

2. **Обратная совместимость полей** — вершины `DaliDatabase`, созданные в ad-hoc-режиме, по-прежнему содержат поля `database_name` и `database_geoid` (существующий код, читающий эти поля, не ломается).

3. **Миграция применена** — после выполнения SQL-миграции запрос
   `SELECT count(*) FROM DaliDatabase WHERE db_name IS NULL AND database_name IS NOT NULL`
   возвращает 0.

4. **DaliSchema группируется** — SHUTTLE `OverviewService` корректно группирует схемы ad-hoc-режима по `db_name` (даже при отсутствии ребра `CONTAINS_SCHEMA`).

5. **SchemaInitializer** — поля `database_name` и `database_geoid` объявлены в SchemaInitializer с комментарием `@deprecated`; порядок инициализации схемы не изменился.

6. **Тесты** — unit-тест или интеграционный тест на Hound, проверяющий что после записи ad-hoc-вершины `DaliDatabase` она содержит поля `db_name` и `db_geoid`.

---

## 5. Не входит в задачу

- Добавление ребра `BELONGS_TO_APP` для ad-hoc-вершин `DaliDatabase` — отдельная задача (требует наличия соответствующей `DaliApplication` в контексте сессии).
- Добавление ребра `CONTAINS_SCHEMA` для ad-hoc-вершин `DaliSchema` — отдельная задача.
- Полное удаление полей `database_name` / `database_geoid` из SchemaInitializer и из кода Hound — выполняется после миграции и стабилизации в отдельном PR.
- Изменения в SHUTTLE — SHUTTLE уже исправлен и ожидает данной задачи по Hound.
- Изменения в схеме ArcadeDB (добавление индексов на `db_name`) — оценить отдельно по результатам нагрузочного тестирования.
