# Dali — Architecture Overview

> Status: **PRODUCTION-READY** — актуализировано 15.04.2026  
> Module: `services/dali` (Quarkus 3.34.2, port 9090)  
> Предыдущий статус: C.2 SKELETON (13.04.2026) — все "outstanding work" завершены

---

## Purpose

Dali — **async PL/SQL parse service**: принимает parse-сессии через REST,
выполняет их фоново через JobRunr (in-JVM via `HoundParserImpl`), персистирует
состояние заданий в FRIGG (ArcadeDB), и транслирует события в HEIMDALL pipeline.

Ключевые свойства:
- **No subprocess** — Hound запускается в том же JVM через `HoundParserImpl`
- **Async + retry** — JobRunr OSS 7.3.0, 4 worker threads, `@Job(retries=3)`
- **Observable** — `HeimdallEmitter` отправляет события в HEIMDALL (session start/complete/fail, file events, atom counts)
- **Persistent** — `ArcadeDbStorageProvider` (FRIGG); при рестарте QUEUED/RUNNING сессии → FAILED
- **Directory support** — `ParseJob.execute()` рекурсивно обходит директории, 7 расширений SQL-файлов

---

## Component Map

```
┌───────────────────────────────────────────────────────────────┐
│ Dali (port 9090)                                              │
│                                                               │
│  REST Layer                                                   │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ SessionResource  POST   /api/sessions                    │ │
│  │                  GET    /api/sessions/{id}               │ │
│  │                  GET    /api/sessions          (list)    │ │
│  │                  GET    /api/sessions/archive  (FRIGG)   │ │
│  │                  GET    /api/sessions/health             │ │
│  └───────────────────────────┬────────────────────────────── │
│  ┌────────────────────────── │ ─────────────────────────────┐ │
│  │ YggStatsResource  GET /api/stats              (YGG agg.) │ │
│  └──────────────────────────────────────────────────────────┘ │
│                              │ enqueue()                      │
│  Service Layer               ▼                               │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ SessionService   ConcurrentHashMap<id, Session>          │ │
│  │                  SessionRepository (FRIGG persistence)   │ │
│  │                  listRecent(limit) / isFriggHealthy()    │ │
│  │                  JobScheduler.enqueue(ParseJob)          │ │
│  └───────────────────────────┬────────────────────────────── │
│                              │ async (JobRunr queue)         │
│  Job Layer                   ▼                              │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ ParseJob         @Job(retries=3)                         │ │
│  │                  file / directory (recursive, 7 ext.)    │ │
│  │                  HeimdallEmitter.sessionStarted/Complete  │ │
│  │                  DaliHoundListener → HeimdallEmitter      │ │
│  └───────────────────────────┬────────────────────────────── │
│                              │ parse result                  │
│                              ▼                               │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ HoundParserImpl  in-JVM (no subprocess)                  │ │
│  └──────────────────────────────────────────────────────────┘ │
│                                                               │
│  Infrastructure                                               │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ JobRunrLifecycle       produces StorageProvider          │ │
│  │                        produces JobScheduler             │ │
│  │                        starts BackgroundJobServer        │ │
│  └──────────────────────────────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ FriggSchemaInitializer  creates 4 ArcadeDB types         │ │
│  │ FriggGateway            SQL over HTTP to FRIGG           │ │
│  │ ArcadeDbStorageProvider ← АКТИВЕН (заменил In-Memory)    │ │
│  └──────────────────────────────────────────────────────────┘ │
│  ┌──────────────────────────────────────────────────────────┐ │
│  │ HeimdallEmitter  10+ event types → HEIMDALL pipeline     │ │
│  │ HeimdallClient   RestClient → heimdall-backend           │ │
│  │ YggClient        RestClient → SHUTTLE /api/stats         │ │
│  └──────────────────────────────────────────────────────────┘ │
└───────────────────────────────────────────────────────────────┘
              │                          │
              ▼                          ▼
   FRIGG (ArcadeDB :2481)      HEIMDALL backend (:9093)
   jobrunr_jobs / sessions      events, metrics
              │
              ▼ (YggStatsResource)
   YGG/SHUTTLE (:2480)
   aggregate counts
```

---

## REST API

### `POST /api/sessions`

```json
// Request
{ "dialect": "plsql", "source": "/path/to/file.pck", "preview": false }
// Или директория:
{ "dialect": "plsql", "source": "/data/packages/", "preview": false }

// Response 202 Accepted
{ "id": "uuid", "status": "QUEUED", "dialect": "plsql",
  "progress": 0, "total": 0, "startedAt": "...", "updatedAt": "..." }
```

### `GET /api/sessions/{id}`

```json
{ "id": "uuid", "status": "RUNNING", "progress": 12, "total": 45, ... }
{ "id": "uuid", "status": "COMPLETED", ... }
// 404 если id неизвестен
```

**Session lifecycle:** `QUEUED → RUNNING → COMPLETED | FAILED`

### `GET /api/sessions?limit=50`

Возвращает список последних `limit` сессий из in-memory кэша (sorted by `startedAt` DESC).  
Default limit: 50.

### `GET /api/sessions/archive?limit=200`

Список исторических сессий из FRIGG (персистентный архив). Default limit: 200.  
Отдельно от in-memory кэша — переживает рестарты сервиса.

### `GET /api/sessions/health`

```json
{ "frigg": "UP", "sessions": 12 }
// или
{ "frigg": "DOWN", "sessions": 0 }
```

### `GET /api/stats`

Агрегированные счётчики из YGG (ArcadeDB :2480, SHUTTLE-база):

```json
{
  "tables": 2841,
  "columns": 47203,
  "sessions": 18,
  "statements": 104832,
  "routines": 1204,
  "atoms": {
    "total": 50858,
    "resolved": 34201,
    "constant": 8412,
    "pending": 4100,
    "unresolved": 4145
  }
}
```

---

## ParseJob — Directory Support

`ParseJob.execute()` определяет тип источника:

```
source → File?  → parse single file
       → Dir?   → Files.walk(source)
                   filter: .sql .pck .prc .pkb .pks .fnc .trg .vw
                   → for each file: HoundParser.parse(file, config)
                                    progress++ after each file
```

`DaliHoundListener` мостит Hound события в HEIMDALL:
- `fileParsingStarted` / `fileParsingComplete` / `fileParsingFailed`
- `atomExtracted` (count)
- `parseError` / `parseWarning`

Все события — fire-and-forget (`onFailure().recoverWithNull()`) — не блокируют parse.

---

## HeimdallEmitter — события в HEIMDALL pipeline

Полный typed API (10+ методов):

| Метод | Когда |
|-------|-------|
| `jobEnqueued(sessionId)` | ParseJob поставлен в очередь |
| `sessionStarted(session)` | ParseJob начинает выполнение |
| `sessionCompleted(session, stats)` | Parse завершён успешно |
| `sessionFailed(session, error)` | Parse завершился с ошибкой |
| `fileParsingStarted(sessionId, filePath)` | Начало обработки файла |
| `fileParsingComplete(sessionId, filePath)` | Файл обработан |
| `fileParsingFailed(sessionId, filePath, error)` | Файл не удалось распарсить |
| `atomExtracted(sessionId, count)` | Hound извлёк N атомов |
| `parseError(sessionId, msg)` | Синтаксическая ошибка |
| `parseWarning(sessionId, msg)` | Предупреждение парсера |

Транспорт: HTTP POST → `HeimdallClient` → `heimdall-backend:9093/events`  
Аутентификации нет — закрытая docker-сеть.

---

## JobRunr Integration

**JobRunr OSS 7.3.0** (не Quarkus extension) — конфигурируется через CDI:

```
JobRunrLifecycle (@Priority ordering):
  1. FriggSchemaInitializer  → создаёт 4 типа в FRIGG
  2. storageProvider()       → ArcadeDbStorageProvider(frigg)  ← АКТИВЕН
  3. JobScheduler produced as @Singleton
  4. BackgroundJobServer.start()  [4 worker threads]
  5. SessionService.loadFromFrigg() → QUEUED/RUNNING → FAILED
```

**Retry policy:** 3 попытки (`@Job(retries = 3)`).  
**Worker threads:** 4 (конфигурируется через `dali.jobrunr.worker-threads=4`).

---

## Storage: FRIGG (ArcadeDB)

### Schema (4 document types, `FriggSchemaInitializer`)

| Type | Purpose | Indexes |
|------|---------|---------|
| `jobrunr_jobs` | Persisted job records | `id` UNIQUE, `state` |
| `jobrunr_recurring_jobs` | Recurring job definitions | `id` UNIQUE |
| `jobrunr_servers` | Background server heartbeats | `id` UNIQUE |
| `jobrunr_metadata` | Cluster-wide metadata | `id` UNIQUE |

### ArcadeDbStorageProvider

**Статус: АКТИВЕН** — заменил `InMemoryStorageProvider`.

Реализует `StorageProvider` (extends `AbstractStorageProvider`):
- Job CRUD: `save`, `getJobById`, `getJobsByState`, `delete`
- Server heartbeat: `announceBackgroundJobServer`, `removeTimedOutBackgroundJobServers`
- Metadata: `getMetadata`, `saveMetadata`
- Serialization: `JobMapper.serializeJob(job)`, `JacksonJsonMapper`

ArcadeDB REST connection TTL 30s (`quarkus.rest-client.dali-frigg.connection-ttl=30000`) —
фикс BUG-SS-041 (idle connection drop).

---

## Configuration (`application.properties`)

```properties
# Server
quarkus.http.port=9090
quarkus.http.host=0.0.0.0

# CORS
quarkus.http.cors.origins=http://localhost:3000,http://localhost:13000,http://localhost:5174,http://localhost:5175,http://localhost:25174

# FRIGG (JobRunr storage)
quarkus.rest-client.dali-frigg.url=${FRIGG_URL:http://localhost:2481}
quarkus.rest-client.dali-frigg.connection-ttl=30000
frigg.url=${FRIGG_URL:http://localhost:2481}
frigg.db=${FRIGG_DB:dali}
frigg.user=${FRIGG_USER:root}
frigg.password=${FRIGG_PASSWORD:playwithdata}

# YGG (for /api/stats)
ygg.url=${ARCADEDB_URL:http://localhost:2480}
ygg.db=${ARCADEDB_DB:hound}
ygg.user=${ARCADEDB_USER:root}
ygg.password=${ARCADEDB_PASS:playwithdata}

# HEIMDALL
quarkus.rest-client.heimdall-api.url=${HEIMDALL_URL:http://localhost:9093}
quarkus.rest-client.ygg-api.url=${YGG_URL:http://localhost:8080}

# JobRunr
dali.jobrunr.background-job-server.enabled=true
dali.jobrunr.worker-threads=4

# Logging
quarkus.log.level=INFO
quarkus.log.category."studio.seer".level=DEBUG
```

**Docker environment variables:**

| Variable | Default | Description |
|----------|---------|-------------|
| `FRIGG_URL` | `http://localhost:2481` | ArcadeDB FRIGG endpoint |
| `FRIGG_DB` | `dali` | ArcadeDB database (JobRunr) |
| `ARCADEDB_URL` | `http://localhost:2480` | YGG (lineage graph) endpoint |
| `ARCADEDB_DB` | `hound` | YGG database name |
| `HEIMDALL_URL` | `http://localhost:9093` | HEIMDALL backend |

---

## Статус завершённых задач

Все пункты из раздела "Outstanding Work" (статус C.2 SKELETON) реализованы:

| Задача (была) | Статус | Примечание |
|---------------|--------|------------|
| Wire `ArcadeDbStorageProvider` | ✅ DONE | Активен, FRIGG-backed |
| Heimdall event forwarding | ✅ DONE | `HeimdallEmitter` + 10+ event types |
| Directory parse support | ✅ DONE | `ParseJob`, 7 расширений, прогресс per-file |
| `GET /api/sessions` list | ✅ DONE | in-memory + FRIGG archive |
| Integration tests (Д10) | 🟡 PARTIAL | FRIGG container тест нужен в CI |
