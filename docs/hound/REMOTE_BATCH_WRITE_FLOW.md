# REMOTE_BATCH Write Flow

> Документ описывает полный путь записи данных из Hound в ArcadeDB (YGG)
> в режиме `ArcadeWriteMode.REMOTE_BATCH`.
>
> Актуально на: 2026-04-14

---

## Содержание

1. [Обзор и назначение](#1-обзор-и-назначение)
2. [Компоненты](#2-компоненты)
3. [Режимы записи](#3-режимы-записи)
4. [Точка входа: ParseJob](#4-точка-входа-parsejob)
5. [ArcadeDBSemanticWriter — маршрутизация](#5-arcadedbsemanticwriter--маршрутизация)
6. [RemoteWriter.writeBatch() — оркестрация](#6-remotewriterwritebatch--оркестрация)
   - 6.1 [Namespace / Pool mode](#61-namespace--pool-mode)
   - 6.2 [Ad-hoc mode](#62-ad-hoc-mode)
7. [JsonlBatchBuilder — сборка NDJSON](#7-jsonlbatchbuilder--сборка-ndjson)
8. [HttpBatchClient — HTTP-отправка](#8-httpbatchclient--http-отправка)
9. [CanonicalPool — дедупликация](#9-canonicalpool--дедупликация)
10. [Обработка дубликатов](#10-обработка-дубликатов)
11. [WriteStats — статистика](#11-writestats--статистика)
12. [Полная диаграмма потока](#12-полная-диаграмма-потока)
13. [Уникальные индексы ArcadeDB](#13-уникальные-индексы-arcadedb)
14. [Константы и параметры](#14-константы-и-параметры)

---

## 1. Обзор и назначение

`REMOTE_BATCH` — основной режим записи в production. Он решает две задачи:

**Скорость:** вместо N отдельных HTTP-запросов (`rcmd`) для каждой вершины/ребра,
все объекты одного файла отправляются одним POST-запросом в формате NDJSON через
ArcadeDB Batch API (`/api/v1/batch/{db}`). Для типичного SQL-файла это уменьшает
количество сетевых round-trips с тысяч до единиц.

**Корректность при повторных запусках:** канонические объекты (DaliSchema, DaliTable,
DaliColumn) могут существовать в БД от предыдущих сессий. Их создание выносится
за пределы батча (в отдельные `rcmd`), что позволяет корректно обработать
`DuplicatedKeyException` и переиспользовать существующие записи.

---

## 2. Компоненты

| Класс | Пакет | Роль |
|-------|-------|------|
| `ParseJob` | `studio.seer.dali.job` | Точка входа, запускает парсинг файлов |
| `ArcadeDBSemanticWriter` | `com.hound.storage` | Facade, маршрутизирует по режиму |
| `RemoteWriter` | `com.hound.storage` | Основная логика pre-batch + batch |
| `JsonlBatchBuilder` | `com.hound.storage` | Сборка NDJSON payload |
| `HttpBatchClient` | `com.hound.storage` | HTTP-отправка с retry |
| `CanonicalPool` | `com.hound.storage` | Кэш RID'ов для namespace-режима |
| `WriteStats` | `com.hound.storage` | Счётчики inserted/duplicate/edges |
| `HoundConfig` | `com.hound.api` | Конфигурация парсера и БД |
| `ArcadeWriteMode` | `com.hound.api` | Enum режимов записи |

---

## 3. Режимы записи

```java
public enum ArcadeWriteMode {
    DISABLED,       // только парсинг, без записи в БД (preview)
    REMOTE,         // одиночные rcmd() для каждого объекта (медленно, устарело)
    REMOTE_BATCH,   // pre-insert canonical + NDJSON batch (production)
    EMBEDDED        // отключён (несовместимость с ArcadeDB 26.x)
}
```

`REMOTE_BATCH` выбирается в `ParseJob.buildConfig()`:
```java
return new HoundConfig(
    input.dialect(),
    null,                           // targetSchema
    ArcadeWriteMode.REMOTE_BATCH,   // ← режим
    yggUrl, yggDb, yggUser, yggPassword,
    Runtime.getRuntime().availableProcessors(),
    false, 5000, null);
```

---

## 4. Точка входа: ParseJob

```
ParseJob.execute(sessionId, ParseSessionInput)
    ├─ buildConfig(input) → HoundConfig(REMOTE_BATCH)
    ├─ [clearBeforeWrite=true] houndParser.cleanAll(config)
    ├─ [single file] runSingle(sessionId, file, config)
    │       └─ houndParser.parse(file, config, listener)
    └─ [directory]  runBatch(sessionId, dir, config, input)
            └─ для каждого файла: houndParser.parse(file, config, listener)
```

`houndParser.parse()` вызывает семантические листенеры, которые в конце
вызывают `ArcadeDBSemanticWriter.saveResult()`.

**Retry-политика JobRunr:** `@Job(retries = 3)` — при unhandled exception
вся задача повторяется 3 раза. Это дополнительный уровень защиты поверх
retry-логики `HttpBatchClient`.

---

## 5. ArcadeDBSemanticWriter — маршрутизация

### Конструкторы

```java
// Конструктор 1: только REMOTE (без batch)
new ArcadeDBSemanticWriter(host, port, dbName, user, password)
// → mode = Mode.REMOTE, batchClient = null

// Конструктор 2: с флагом useBatch
new ArcadeDBSemanticWriter(host, port, dbName, user, password, useBatch=true)
// → mode = Mode.REMOTE_BATCH, batchClient = new HttpBatchClient(...)
```

Оба конструктора при старте вызывают `SchemaInitializer.remoteSchemaCommands()`
для создания типов вершин/рёбер и индексов если они ещё не существуют.

### saveResult() — точка маршрутизации

```java
public WriteStats saveResult(SemanticResult result, PipelineTimer timer,
                             CanonicalPool pool, String dbName) {
    WriteStats ws;
    if (mode == Mode.REMOTE_BATCH) {
        ws = remote.writeBatch(batchClient, sid, result, timer, pool, dbName);
    } else {
        remote.write(sid, result, timer, pool, dbName);
        ws = new WriteStats();
    }
    // ... logging
    return ws;
}
```

`CanonicalPool pool` и `String dbName`:
- `null, null` — **ad-hoc mode**: объекты привязываются к `session_id`, не к `db_name`
- `pool, "MY_DB"` — **namespace mode**: объекты привязываются к `db_name`, переиспользуются между сессиями

---

## 6. RemoteWriter.writeBatch() — оркестрация

Метод реализует **двухфазный** подход:

```
Фаза 1: Pre-insert canonical objects via individual rcmd()
         (DaliSchema, DaliTable, DaliColumn — могут существовать в БД)

Фаза 2: Build NDJSON + single HTTP POST
         (все остальные объекты: Statement, Atom, Edge, OutputColumn, ...)
```

### 6.1 Namespace / Pool mode

Используется когда `pool != null` — при подключении к именованной БД.

**Шаг 1 — Pre-insert (строки ~1349–1437):**

```java
// DaliSchema
for (var e : str.getSchemas().entrySet()) {
    String cg = pool.canonicalSchema(e.getKey());
    if (!pool.hasSchemaRid(cg)) {          // ← проверяем кэш ПЕРЕД insert
        try {
            rcmd("INSERT INTO DaliSchema SET db_name=?, db_geoid=?, schema_geoid=?, schema_name=?",
                 dbName, dbName, geoid, name);
            pool.putSchemaRid(cg, cg);
            newSchemaGeoids.add(geoid);
        } catch (RuntimeException ex) {
            if (isDupKey(ex)) {
                pool.putSchemaRid(cg, cg); // регистрируем как известный
            } else throw ex;
        }
    }
}
// аналогично DaliTable, DaliColumn
```

Ключевое отличие от ad-hoc: запись `db_name=dbName` вместо `session_id=sid`.
Это позволяет переиспользовать схемы/таблицы/колонки между разными сессиями
одной и той же БД.

**Шаг 2 — buildRidCache():**

```java
RidCache rid = buildRidCache(sid, pool, dbName);
// SQL:
// SELECT @rid AS rid, schema_geoid FROM DaliSchema WHERE db_name = :dbName
// SELECT @rid AS rid, table_geoid  FROM DaliTable  WHERE db_name = :dbName
// SELECT @rid AS rid, column_geoid FROM DaliColumn WHERE db_name = :dbName
```

Возвращает map `geoid → ArcadeDB RID (#cluster:pos)` для использования
как `@from`/`@to` endpoint'ов в рёбрах batch'а.

**Шаг 3 — Create hierarchical edges:**

Для **только что вставленных** объектов (`newXxxGeoids`) создаёт рёбра:
```java
edgeByRid("CONTAINS_SCHEMA", pool.getDatabaseRid(), schRid, sid);
edgeByRid("CONTAINS_TABLE",  schRid, tblRid, sid);
edgeByRid("HAS_COLUMN",      tblRid, colRid, sid);
```

Для уже существующих объектов рёбра не пересоздаются.

**Шаг 4 — Build & send batch:**

```java
JsonlBatchBuilder builder = JsonlBatchBuilder.buildFromResult(
    sid, result,
    rid.tables,    // tableGeoid → RID
    rid.columns,   // columnGeoid → RID
    rid.schemas    // schemaGeoid → RID
);
String payload = builder.build();
client.send(payload, sid);
```

### 6.2 Ad-hoc mode

Используется когда `pool == null` — при парсинге без указания БД-namespace.

**preInsertAdHocSchemas():**

```java
// DaliSchema
rcmd("INSERT INTO DaliSchema SET session_id=?, schema_geoid=?, schema_name=?, db_name=?, db_geoid=?",
     sid, geoid, name, null, null);  // ← db_name = NULL

// При DuplicatedKeyException → debug log + skip

// После всех insert'ов — запрашиваем RID'ы:
SELECT @rid AS rid, schema_geoid FROM DaliSchema WHERE db_name IS NULL
```

Возвращает `AdHocInsertResult` с map `geoid → RID` и множествами `newXxxGeoids`.

**Build batch:**

```java
JsonlBatchBuilder builder = JsonlBatchBuilder.buildFromResult(sid, result, adHoc.rids());
```

---

## 7. JsonlBatchBuilder — сборка NDJSON

Собирает payload в виде двух `StringBuilder`: вершины + рёбра.
При `build()` они конкатенируются: сначала все вершины, затем все рёбра.

### Формат одной строки

```json
{"@type":"vertex","@class":"DaliStatement","@id":"STMT_GEOID","session_id":"s1","stmt_geoid":"..."}
{"@type":"edge","@class":"READS_FROM","@from":"STMT_GEOID","@to":"TABLE_GEOID","session_id":"s1"}
```

`@id` — временный идентификатор внутри батча (используется как `@from`/`@to`).
Для канонических объектов (уже в БД) `@from`/`@to` содержат реальные ArcadeDB RID'ы.

### Порядок вершин

| # | Тип | Примечание |
|---|-----|------------|
| 1 | `DaliSession` | 1 строка на файл |
| 2 | `DaliDatabase` | только в ad-hoc |
| 3 | `DaliSchema` | **ПРОПУСКАЕТСЯ** если geoid в `canonicalRids` |
| 4 | `DaliPackage` | |
| 5 | `DaliTable` | **ПРОПУСКАЕТСЯ** если geoid в `canonicalRids` |
| 6 | `DaliColumn` | **ПРОПУСКАЕТСЯ** если geoid в `canonicalRids` |
| 7 | `DaliRoutine` | пропускается если это package-placeholder |
| 8 | `DaliParameter`, `DaliVariable` | |
| 9 | `DaliStatement` | обычные DML-операторы |
| 10 | `DaliDDLStatement` | CREATE/ALTER/DROP |
| 11 | `DaliPrimaryKey`, `DaliForeignKey` | из DDL |
| 12 | `DaliOutputColumn` | |
| 13 | `DaliAffectedColumn` | |
| 14 | `DaliRecord` | G6: BULK COLLECT |
| 15 | `DaliAtom` | только `statement`/`unattached` контекст |
| 16 | `DaliJoin` | |
| 17 | `DaliSnippet` | **ПРОПУСКАЕТСЯ в batch** (вставляется через post-batch `rcmd`) |

### Порядок рёбер

| # | Тип ребра | Откуда → Куда |
|---|-----------|---------------|
| 1 | `CONTAINS_TABLE` | Schema → Table |
| 2 | `CONTAINS_ROUTINE` | Schema/Package → Routine |
| 3 | `NESTED_IN` | Parent → Child Routine |
| 4 | `HAS_COLUMN` | Table → Column |
| 5 | `CONTAINS_STMT` | Routine → Statement |
| 6 | `CHILD_OF` | Statement → Parent Statement |
| 7 | `HAS_OUTPUT_COL` | Statement → OutputColumn |
| 8 | `HAS_AFFECTED_COL` | Statement → AffectedColumn |
| 9 | `AFFECTED_COL_REF_TABLE` | AffectedColumn → Table |
| 10 | `BULK_COLLECTS_INTO`, `RECORD_USED_IN` | G6 edges |
| 11 | `HAS_JOIN`, `JOIN_SOURCE_TABLE`, `JOIN_TARGET_TABLE` | |
| 12 | `HAS_PARAMETER`, `HAS_VARIABLE` | |
| 13 | `BELONGS_TO_SESSION` | Routine/Statement → Session |
| 14 | `READS_FROM`, `WRITES_TO` | Statement → Table |
| 15 | `USES_SUBQUERY` | Statement → Statement |
| 16 | `HAS_ATOM` | Statement → Atom |
| 17 | `ATOM_REF_TABLE`, `ATOM_REF_COLUMN`, `ATOM_REF_STMT` | Atom → ... |
| 18 | `DATA_FLOW`, `FILTER_FLOW`, `ATOM_PRODUCES` | Lineage edges |
| 19 | `CALLS` | Routine → Routine |

### Дедупликация внутри батча

```java
// appendVertex() — защита от одного geoid дважды в одном файле
if (extId != null && !vertexIds.add(extId)) {
    writeStats.markDuplicate(type);
    return;  // skip
}
```

### Резолюция endpoint'ов рёбер

```java
private String resolveEndpoint(String geoid) {
    if (vertexIds.contains(geoid))    return geoid;          // вершина в этом батче
    String rid = canonicalRids.get(geoid);
    return rid;  // null → edge будет dropped
}
```

Если оба endpoint'а не найдены → `writeStats.dropEdge()`, ребро не попадает в payload.

---

## 8. HttpBatchClient — HTTP-отправка

### Endpoint

```
POST http://{host}:{port}/api/v1/batch/{dbName}
     ?lightEdges=true&wal=false&parallelFlush=true&batchSize=100000

Content-Type: application/x-ndjson
Authorization: Basic <base64>
```

| Параметр | Значение | Назначение |
|----------|----------|------------|
| `lightEdges=true` | bool | Уменьшает объём ответа |
| `wal=false` | bool | Отключает Write-Ahead Log, ускоряет запись |
| `parallelFlush=true` | bool | Параллельный flush на ArcadeDB nodes |
| `batchSize=100000` | int | Размер commit-окна внутри batch |

### Retry-логика

```
attempt 1 → POST
    5xx → sleep(1s) → attempt 2
    5xx → sleep(2s) → attempt 3
    5xx → throw RuntimeException("Batch failed after 3 attempts")
    4xx → throw immediately (ошибка запроса, retry бессмысленен)
    2xx → return OK
```

Максимальное суммарное время ожидания: ~3 секунды (1 + 2).

### Опциональная gzip-компрессия

```java
byte[] body = gzip ? compress(payload) : payload.getBytes(UTF_8);
```

При gzip ставится `Content-Encoding: gzip`. По умолчанию gzip отключён.

---

## 9. CanonicalPool — дедупликация

`CanonicalPool` — in-memory кэш RID'ов для одного `dbName`.
Создаётся один раз в `RemoteWriter.ensurePool()` и передаётся во все
последующие вызовы `writeBatch()` для одной БД.

### Ключи canonical geoid

```java
canonicalSchema(geoid)  → geoid           // "CRM"
canonical(geoid)        → geoid           // "CRM.CONTRACTS"
canonicalCol(tblGeoid, colName)           // "CRM.CONTRACTS.ID"
```

### Жизненный цикл

```
ensurePool(dbName, appName, appGeoid)
    ├─ SELECT RID FROM DaliApplication WHERE app_geoid = ?
    │   └─ [not found] INSERT INTO DaliApplication ...
    ├─ SELECT RID FROM DaliDatabase WHERE db_geoid = ?
    │   └─ [not found] INSERT INTO DaliDatabase ...
    └─ [both new] CREATE EDGE BELONGS_TO_APP

→ writeBatch(client, sid, result, timer, pool, dbName)
    ├─ [!pool.hasSchemaRid] INSERT DaliSchema → pool.putSchemaRid
    ├─ [!pool.hasTableRid]  INSERT DaliTable  → pool.putTableRid
    └─ [!pool.hasColRid]    INSERT DaliColumn → pool.putColRid

→ следующий файл той же сессии → pool уже содержит RID'ы → INSERT пропускается
```

### Thread-safety

`CanonicalPool` использует `ConcurrentHashMap` для RID-кэшей — безопасен
при параллельном парсинге нескольких файлов.

---

## 10. Обработка дубликатов

Дубликаты возникают при повторном парсинге файла или при обработке нескольких
файлов одной сессии, ссылающихся на одни и те же схемы/таблицы.

### Таблица обработки по уровням

| Уровень | Где | Как |
|---------|-----|-----|
| **Pool cache** | Namespace mode, pre-batch | `pool.hasXxxRid()` перед INSERT — не делаем попытку вообще |
| **rcmd catch** | Pre-batch, все пути | `catch (RuntimeException)` + проверка сообщения на `"Duplicated key"` / `"DuplicatedKeyException"` / `"Found duplicate key"` |
| **In-batch dedup** | `JsonlBatchBuilder.appendVertex()` | `vertexIds.add(extId)` — геoid уже в Set → skip + `markDuplicate()` |
| **canonicalRids skip** | `JsonlBatchBuilder` | Если геoid в `canonicalRids` → вершина пропускается при сборке |
| **Edge resolution** | `resolveEndpoint()` | endpoint не найден → `dropEdge()` |

### Апгрейд data_source при дубликате

При повторном INSERT DaliColumn с `data_source='master'` (из DDL):
```java
// Если DuplicatedKeyException → обновляем существующую запись:
UPDATE DaliColumn SET data_source=?, is_pk=?, is_fk=?, fk_ref_table=?, fk_ref_column=?
WHERE db_name=? AND column_geoid=?
```

Это гарантирует, что если колонка сначала была создана как `reconstructed`
(из SELECT), а потом встретился CREATE TABLE — она апгрейдится до `master`.

---

## 11. WriteStats — статистика

```java
public class WriteStats {
    Map<String, int[]> vtx;  // type → [inserted, duplicate]
    int edges;
    int droppedEdges;
}
```

Логируется после каждого файла:
```
ArcadeDB REMOTE_BATCH: sid=abc db=PROD V:1243 (dup:17) E:4521 dropped:3 atoms:892 raw:187432b [1.2s]
```

Доступна в `FileResult.vertexStats()` → передаётся в Dali SessionService
→ отображается в Heimdall SessionList (таблица FILES).

---

## 12. Полная диаграмма потока

```
ParseJob.execute()
    │
    ├─ buildConfig() → HoundConfig(REMOTE_BATCH, yggUrl, yggDb, ...)
    │
    ├─ [clearBeforeWrite] houndParser.cleanAll()
    │
    └─ для каждого файла:
           houndParser.parse(file, config, listener)
                │
                └─ ArcadeDBSemanticWriter.saveResult(result, timer, pool, dbName)
                        │
                        └─ RemoteWriter.writeBatch(batchClient, sid, result, timer, pool, dbName)
                                │
                        ┌───────┴──────────────────────────────────┐
                        │ pool != null (namespace)                  │ pool == null (ad-hoc)
                        │                                           │
                        │ 1. rcmd INSERT DaliSchema                 │ 1. preInsertAdHocSchemas()
                        │    (pool check + dup catch)               │    rcmd INSERT Schema/Table/Col
                        │ 2. rcmd INSERT DaliTable                  │    + catch DuplicatedKey
                        │    (pool check + dup catch)               │    + SELECT all RIDs back
                        │ 3. rcmd INSERT DaliColumn                 │
                        │    (pool check + dup catch)               │
                        │ 4. buildRidCache()                        │
                        │    SELECT RIDs by db_name                 │
                        │ 5. edgeByRid() для новых объектов         │
                        │    CONTAINS_SCHEMA, CONTAINS_TABLE,       │
                        │    HAS_COLUMN                             │
                        │                                           │
                        └───────────────────┬───────────────────────┘
                                            │
                                JsonlBatchBuilder.buildFromResult(sid, result, rids)
                                            │
                                  Phase 1: вершины (Session, Package, Routine,
                                           Statement, Atom, OutputColumn, ...)
                                  Phase 2: рёбра (READS_FROM, HAS_ATOM,
                                           DATA_FLOW, CALLS, ...)
                                            │
                                        builder.build()
                                            │
                                  HttpBatchClient.send(payload, sid)
                                    POST /api/v1/batch/{db}?lightEdges=true&wal=false
                                    retry 3x на 5xx с exponential backoff
                                            │
                                  post-batch: rcmd INSERT DaliSnippet
                                            │
                                        WriteStats ←─────────────────────────
```

---

## 13. Уникальные индексы ArcadeDB

Созданы при старте через `SchemaInitializer.remoteSchemaCommands()`:

| Тип | Индекс | Ключи |
|-----|--------|-------|
| `DaliSchema` | `DaliSchema[db_name,schema_geoid]` | `(db_name, schema_geoid)` |
| `DaliTable` | `DaliTable[db_name,table_geoid]` | `(db_name, table_geoid)` |
| `DaliColumn` | `DaliColumn[db_name,column_geoid]` | `(db_name, column_geoid)` |

Для ad-hoc записей `db_name = NULL` — это допустимое значение в уникальном
ключе ArcadeDB (каждая `NULL` считается уникальной *если не установлен
constraint NOT NULL*). Однако при повторном парсинге того же файла без смены
`session_id` возникает `DuplicatedKeyException` с `[null, SCHEMA_GEOID]`.

---

## 14. Константы и параметры

```java
// HttpBatchClient
MAX_RETRIES       = 3
RETRY_BASE_MS     = 1000   // exponential: 1s, 2s, 4s

// Batch endpoint
lightEdges=true
wal=false
parallelFlush=true
batchSize=100000

// RemoteWriter.rcmd()
RCMD_MAX_RETRIES  = 3
RCMD_RETRY_BASE_MS = 200   // для timeout-retry внутри rcmd

// JsonlBatchBuilder
SNIPPET_MAX       = 4000   // символов (DaliSnippet.snippet truncate)

// data_source значения
MASTER            = "master"        // таблица/колонка из CREATE TABLE/VIEW
RECONSTRUCTED     = "reconstructed" // восстановлена из SELECT/DML
```
