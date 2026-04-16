# Q7 — HEIMDALL ↔ Dali API Contract

**Документ:** `Q7_HEIMDALL_DALI_API`
**Версия:** 1.1
**Дата:** 16.04.2026
**Статус:** ✅ Q7 CLOSED — три канала задокументированы и реализованы (C.2.2 + UC2b)
**Зависит от:** Q3 (HEIMDALL backend deployment, mid-May) — не блокирует документ, блокирует production deployment

---

## Сводка каналов

| Канал | Направление | Статус | Gap |
|---|---|---|---|
| Event Ingestion | Dali → HEIMDALL | ✅ реализован | нет |
| Session Management | UI/SHUTTLE → Dali | ✅ реализован | нет |
| Control Plane | HEIMDALL/UI → Dali | ✅ реализован | нет |

---

## Канал 1 — Event Ingestion (Dali → HEIMDALL)

**Назначение:** Dali и Hound шлют fire-and-forget события в HEIMDALL. Не блокирует парсинг.

**Реализация:** `HeimdallEmitter.java` (Dali) → `EventResource.java` (HEIMDALL)

### Endpoint

```
POST http://{HEIMDALL_URL}/events          — одно событие
POST http://{HEIMDALL_URL}/events/batch    — пакет событий
→ 202 Accepted (всегда, даже при ошибке записи в ring buffer)
```

### HeimdallEvent schema

```json
{
  "timestamp":        1713100000000,
  "sourceComponent":  "dali",
  "eventType":        "SESSION_STARTED",
  "level":            "INFO",
  "sessionId":        "550e8400-e29b-41d4-a716-446655440000",
  "correlationId":    "corr-abc123",
  "durationMs":       0,
  "payload": {
    "dialect":        "PLSQL",
    "fileCount":      42,
    "preview":        false,
    "threads":        4
  }
}
```

**Обязательные поля:** `timestamp`, `sourceComponent`, `eventType`, `level`
**Nullable:** `sessionId`, `correlationId`, `durationMs` (0 если неприменимо)

### sourceComponent values

| Значение | Эмиттер |
|---|---|
| `"dali"` | Dali Core (сессии, воркеры) |
| `"hound"` | HoundHeimdallListener (парсинг) |
| `"mimir"` | MIMIR (tool calls) |
| `"shuttle"` | SHUTTLE (GraphQL mutations) |
| `"chur"` | Chur BFF (auth events) |

### Полный EventType registry

| EventType | sourceComponent | level | Payload ключи |
|---|---|---|---|
| `SESSION_STARTED` | dali | INFO | dialect, fileCount, preview, threads |
| `SESSION_COMPLETED` | dali | INFO | atomsTotal, resolutionRate, durationMs |
| `SESSION_FAILED` | dali | ERROR | error, cause |
| `JOB_ENQUEUED` | dali | INFO | dialect, source, preview |
| `FILE_PARSING_STARTED` | hound | INFO | fileName, fileSizeBytes |
| `FILE_PARSING_COMPLETED` | hound | INFO | fileName, atomsExtracted, durationMs |
| `ATOM_EXTRACTED` | hound | INFO | atomType, count (throttled каждые 100) |
| `PARSE_ERROR` | hound | ERROR | fileName, line, message |
| `PARSE_WARNING` | hound | WARN | fileName, line, message, classification |
| `REQUEST_RECEIVED` | shuttle | INFO | operation, userId |
| `REQUEST_COMPLETED` | shuttle | INFO | operation, durationMs |
| `AUTH_LOGIN` | chur | INFO | userId, role |
| `AUTH_LOGOUT` | chur | INFO | userId |
| `TOOL_CALL_STARTED` | mimir | INFO | tool, query |
| `TOOL_CALL_COMPLETED` | mimir | INFO | tool, durationMs, resultCount |

### Обработка ошибок (fire-and-forget)

```java
// HeimdallEmitter.java — паттерн
client.postEvent(event)
    .onFailure().invoke(err -> Log.warn("[HeimdallEmitter] emit failed (non-fatal): " + err.getMessage()))
    .subscribe().with(ignored -> {});
// НЕ бросать исключение, НЕ блокировать парсинг-поток
```

---

## Канал 2 — Session Management (UI/SHUTTLE → Dali)

**Назначение:** HEIMDALL UI (DaliPage) и SHUTTLE (MutationResource) управляют сессиями Dali.

**Base URL:** `http://localhost:9090` (dev) / `http://dali:9090` (docker)

### Endpoints

#### POST /api/sessions — создать сессию

```
POST /api/sessions
Content-Type: application/json
→ 202 Accepted
```

**Request body — ParseSessionInput:**

```json
{
  "dialect":          "PLSQL",
  "source":           "/opt/sql/corp-batch",
  "preview":          false,
  "clearBeforeWrite": true,
  "filePattern":      "*.sql",
  "maxFiles":         null,
  "tags":             ["corp", "batch-2026"]
}
```

| Поле | Тип | Default | Описание |
|---|---|---|---|
| `dialect` | String | обязательно | PLSQL / GENERIC_SQL / POSTGRESQL / CLICKHOUSE |
| `source` | String | обязательно | путь к директории или файлу |
| `preview` | boolean | false | true → ArcadeWriteMode.DISABLED, не пишет в YGG |
| `clearBeforeWrite` | Boolean | **true** | true → TRUNCATE YGG перед парсингом |
| `filePattern` | String | `"*.sql"` | glob-паттерн |
| `maxFiles` | Integer | null | null = без лимита |
| `tags` | String[] | [] | теги для фильтрации в UI |

⚠ `clearBeforeWrite` — **boxed Boolean**, дефолт `true` (не примитив). Решение #INV-DALI-03.

**Response body — SessionInfo:**

```json
{
  "id":          "550e8400-e29b-41d4-a716-446655440000",
  "status":      "QUEUED",
  "dialect":     "PLSQL",
  "source":      "/opt/sql/corp-batch",
  "preview":     false,
  "createdAt":   1713100000000,
  "queuedAt":    1713100001000
}
```

#### GET /api/sessions/{id} — статус сессии (polling)

```
GET /api/sessions/{id}
→ 200 OK | 404 Not Found
```

**Response — Session (полный):**

```json
{
  "id":             "550e8400-...",
  "status":         "COMPLETED",
  "dialect":        "PLSQL",
  "source":         "/opt/sql/corp-batch",
  "preview":        false,
  "createdAt":      1713100000000,
  "startedAt":      1713100002000,
  "completedAt":    1713100120000,
  "durationMs":     118000,
  "atomCount":      9920,
  "atomsResolved":  7780,
  "atomsUnresolved":940,
  "fileCount":      42,
  "filesProcessed": 42,
  "filesFailed":    0,
  "resolutionRate": 0.892,
  "friggPersisted": true,
  "clearBeforeWrite": true,
  "tags":           ["corp", "batch-2026"]
}
```

**Status values:** `QUEUED` → `PROCESSING` → `COMPLETED` | `FAILED` | `CANCELLED`

#### GET /api/sessions — список активных/последних

```
GET /api/sessions?limit=50&status=PROCESSING
→ 200 OK + List<SessionInfo>
```

#### POST /api/sessions/{id}/cancel — отменить сессию

```
POST /api/sessions/{id}/cancel
→ 202 Accepted | 404 Not Found | 409 Conflict
```

Тело запроса: **отсутствует** (endpoint принимает любой `Content-Type`, включая пустой).

**Response body:**

```json
{ "status": "CANCELLING", "message": "Session marked CANCELLED; JobRunr deletion requested" }
```

| HTTP-код | `status` | Условие |
|---|---|---|
| 202 Accepted | `CANCELLING` | Сессия переведена в `CANCELLED`, JobRunr job удалён (если ещё в очереди) |
| 404 Not Found | `NOT_FOUND` | Сессия с таким `id` не существует |
| 409 Conflict | `ALREADY_DONE` | Сессия уже в терминальном состоянии: `COMPLETED`, `FAILED` или `CANCELLED` |

**Механика:**
1. `SessionService.cancelSession(id)` обновляет `Session` record → `status = CANCELLED`
2. `jobRunrIdMap.remove(id)` → `jobScheduler.delete(uuid)` (если job ещё в очереди)
3. `HeimdallEmitter.sessionCancelled()` → `POST /events` c `reason: CANCELLED_BY_USER`
4. Сессия сохраняется в FRIGG через `persist(cancelled)`

**Реализовано:** C.2.2 + UC2b (sprint Apr 16, 2026)

---

#### GET /api/sessions/archive — из FRIGG

```
GET /api/sessions/archive?limit=200&offset=0
→ 200 OK + List<Session>
```

Возвращает завершённые сессии из FRIGG. Сортировка: completedAt DESC.

#### GET /api/sessions/health — health-check

```
GET /api/sessions/health
→ 200 OK
```

```json
{
  "frigg":    "ok",
  "ygg":      "ok",
  "sessions": 3,
  "workers":  4,
  "queued":   0
}
```

`frigg: "error"` — FRIGG недоступен, persistence degraded (сессии в памяти).

#### GET /api/stats — YGG graph stats

```
GET /api/stats
→ 200 OK
```

```json
{
  "daliTables":    1240,
  "daliColumns":   8900,
  "daliRoutines":  320,
  "daliStatements":15600,
  "daliAtoms":     300906,
  "lastUpdated":   1713100120000
}
```

---

## Канал 3 — Control Plane (UI → HEIMDALL → Dali)

**Назначение:** Административные операции. Только admin scopes.

**Реализация:** HEIMDALL `ControlResource.java` → прокси через Chur `/heimdall/control/*`

### Endpoints

#### POST /control/reset — сброс ring buffer

```
POST /control/reset
X-Seer-Role: admin
→ 200 OK
```

```json
{ "status": "reset", "eventsCleared": 847 }
```

HEIMDALL очищает ring buffer. Не затрагивает Dali. Синхронно.

#### POST /control/snapshot — сохранить снапшот в FRIGG

```
POST /control/snapshot?name=before-demo
X-Seer-Role: admin
→ 200 OK
```

```json
{
  "snapshotId":  "snap-20260414-143022",
  "name":        "before-demo",
  "eventCount":  847,
  "savedAt":     1713100200000
}
```

#### GET /control/snapshots — список снапшотов

```
GET /control/snapshots
→ 200 OK + List<SnapshotMeta>
```

#### DELETE /control/snapshots/{id} — удалить снапшот

```
DELETE /control/snapshots/{id}
→ 204 No Content
```

#### POST /control/cancel/{sessionId} — ✅ реализован

```
POST /control/cancel/{sessionId}
X-Seer-Role: admin
→ 202 Accepted
```

Проксирует в `POST /api/sessions/{sessionId}/cancel` на Dali напрямую через `DaliClient` (SHUTTLE C.2.4).

**Ответ:**
```json
{
  "status":    "CANCELLING",
  "sessionId": "550e8400-...",
  "message":   "Session marked CANCELLED; JobRunr deletion requested"
}
```

**JobRunr cancel mechanics:**
```java
// SessionService.java — захватывает JobId при enqueue:
org.jobrunr.jobs.JobId jrJobId = jobScheduler.get().<ParseJob>enqueue(j -> j.execute(sessionId, input));
jobRunrIdMap.put(sessionId, UUID.fromString(jrJobId.toString()));

// cancelSession() — удаляет job из очереди:
UUID jrId = jobRunrIdMap.remove(sessionId);
if (jrId != null) jobScheduler.get().delete(jrId);
```

Dali эмитит `SESSION_FAILED` с payload `{ "reason": "CANCELLED_BY_USER" }` в HEIMDALL.

**Реализовано:** C.2.2 + UC2b (sprint Apr 16, 2026)

---

## Заголовки и аутентификация

| Заголовок | Кто устанавливает | Значение |
|---|---|---|
| `X-Seer-Role` | Chur (из KC JWT) | `admin` / `editor` / etc. |
| `X-Seer-Sub` | Chur | KC subject (UUID) |
| `X-Seer-Scopes` | Chur | space-separated scopes |

Прямые вызовы от Dali в HEIMDALL (`/events`) — без аутентификации (internal network only).
UI вызовы в Dali — через Chur proxy `/dali/*` с `X-Seer-*` заголовками.

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 16.04.2026 | 1.1 | **C.2.2 + UC2b DONE.** Cancel endpoint реализован: `POST /api/sessions/{id}/cancel` (202/404/409). `JobRunrIdMap` механика. Канал 3 — stub снят, статус ✅. DaliClient C.2.4 в SHUTTLE — все мутации wire-up завершён. |
| 14.04.2026 | 1.0 | Q7 CLOSED. Три канала задокументированы. Cancel stub зафиксирован как единственный gap. |
