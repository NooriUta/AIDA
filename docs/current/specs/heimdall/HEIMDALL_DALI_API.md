# HEIMDALL ↔ Dali — API Reference

**Version:** 1.0  
**Date:** 2026-04-20  
**Status:** ✅ Released (v1.2.0)

---

## Base URL

| Environment | URL |
|-------------|-----|
| Dev | `http://localhost:9090` |
| Docker | `http://dali:9090` |
| Prod | `${DALI_URL}` |

---

## Sessions API

### POST /sessions

Trigger a SKADI Harvest (JDBC) session.

**Request:**
```json
{ "sourceFilter": null }
```
- `sourceFilter`: `null` = all sources; `"source-name"` = specific source only.

**Response:**
```json
{ "sessionId": "abc123", "status": "enqueued" }
```

---

### GET /sessions

List all harvest sessions.

**Response:**
```json
[
  {
    "sessionId": "abc123",
    "status": "PROCESSING",
    "trigger": "api",
    "startedAt": "2026-04-20T02:00:00Z"
  }
]
```

`status` values: `ENQUEUED` · `PROCESSING` · `COMPLETED` · `FAILED`  
`trigger` values: `api` · `cron` · `upload`

---

### POST /sessions/{id}/cancel

Cancel an in-progress session.

**Response:** `204 No Content`

---

### GET /sessions/{id}/events

SSE stream of session events.

```
Content-Type: text/event-stream

data: {"event":"SESSION_STARTED","level":"INFO","sessionId":"abc123","payload":{"sources":3}}
data: {"event":"WORKER_ASSIGNED","level":"INFO","sessionId":"abc123","payload":{"worker_id":0,"source":"PROD_DWH"}}
data: {"event":"JOB_COMPLETED","level":"INFO","sessionId":"abc123","payload":{"source":"PROD_DWH","atoms":1842,"files":47,"dur_ms":3210}}
data: {"event":"SESSION_COMPLETED","level":"INFO","sessionId":"abc123","payload":{"total_atoms":4711}}
```

---

## File Upload API

### POST /api/sessions/upload

Upload SQL/ZIP files for one-shot parse (UC-1b).

**Request:** `multipart/form-data`

| Field | Type | Required | Default |
|-------|------|----------|---------|
| `file` | binary | ✅ | — |
| `dialect` | string | — | `plsql` |
| `preview` | boolean | — | `false` |
| `clearBeforeWrite` | boolean | — | `true` |

Accepted extensions: `.sql` `.pck` `.prc` `.pkb` `.pks` `.fnc` `.trg` `.vw` `.zip` `.rar`

**Response:**
```json
{ "sessionId": "upload-1713567890123", "status": "enqueued" }
```

---

## Sources CRUD API

### GET /api/sources

```json
[
  {
    "id": "src-001",
    "name": "PROD_DWH",
    "dialect": "oracle",
    "jdbcUrl": "jdbc:oracle:thin:@ora-host:1521:ORCL",
    "lastHarvest": "2026-04-20T02:03:42Z",
    "atomCount": 4711,
    "schemaFilter": {
      "include": [],
      "exclude": ["SYS","SYSTEM","DBSNMP"]
    }
  }
]
```

---

### POST /api/sources

```json
{
  "name": "PROD_DWH",
  "dialect": "oracle",
  "jdbcUrl": "jdbc:oracle:thin:@host:1521:SID",
  "username": "dali_harvest",
  "password": "secret",
  "schemaFilter": {
    "include": ["HR", "FINANCE"],
    "exclude": ["SYS", "SYSTEM"]
  }
}
```

**Response:** `201 Created` with created source object.

---

### PUT /api/sources/{id}

Same body as POST. Partial update: omitted fields retain existing values.

**Response:** `200 OK` with updated source object.

---

### DELETE /api/sources/{id}

**Response:** `204 No Content`

---

### POST /api/sources/test

Test a JDBC connection without saving.

**Request:**
```json
{
  "jdbcUrl": "jdbc:oracle:thin:@host:1521:SID",
  "dialect": "oracle",
  "username": "dali_harvest",
  "password": "secret"
}
```

**Response (success):**
```json
{ "ok": true, "latencyMs": 42 }
```

**Response (failure):**
```json
{ "ok": false, "error": "ORA-01017: invalid username/password" }
```

---

## WebSocket Events (HEIMDALL backend)

Emitted by `DaliHeimdallEmitter` to `ws://localhost:9093/ws/events`.

| Event | Level | Trigger | Payload fields |
|-------|-------|---------|---------------|
| `SESSION_STARTED` | INFO | `HarvestJob.execute()` start | `sessionId`, `sources`, `mode` |
| `WORKER_ASSIGNED` | INFO | `FileParseJob.execute()` start | `worker_id`, `source` |
| `JOB_COMPLETED` | INFO | `FileParseJob` success | `source`, `atoms`, `files`, `dur_ms` |
| `JOB_FAILED` | ERROR | `FileParseJob` exception | `source`, `error` |
| `COMPLEX_JOB_PROGRESS` | INFO | After enqueuing all subtasks | `done`, `total`, `atoms_so_far` |
| `SESSION_COMPLETED` | INFO | All subtasks finished | `total_atoms` |

---

## JobRunr Dashboard

Embedded at `/jobrunr` in HEIMDALL frontend.  
Backend: `http://localhost:29091` (JobRunr built-in HTTP server).  
Enabled via: `quarkus.jobrunr.dashboard.enabled=true`, `quarkus.jobrunr.background-job-server.dashboard-port=29091`.

Shows: HarvestJob queue · FileParseJob subtasks · retry state · failed jobs.
