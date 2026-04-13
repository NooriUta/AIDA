# Dali — Architecture Overview

> Status: **C.2 SKELETON** — Sprint C.2 (13.04.2026)  
> Module: `services/dali` (Quarkus 3.34.2, port 9090)

---

## Purpose

Dali is an **async PL/SQL parse service** that accepts parse sessions via REST,
executes them as background jobs in-JVM via Hound, and persists job state in FRIGG
(ArcadeDB).

Goals:
- No subprocess calls to Hound — parse runs in the same JVM via `HoundParserImpl`
- Async execution with retry (JobRunr)
- Observable: job progress visible via REST + (future) Heimdall events
- Persistent: job state survives service restarts via FRIGG

---

## Component Map

```
┌────────────────────────────────────────────────────────┐
│ Dali (port 9090)                                       │
│                                                         │
│  REST Layer                                             │
│  ┌─────────────────────────────────────────────────┐  │
│  │ SessionResource  POST /api/sessions              │  │
│  │                  GET  /api/sessions/{id}         │  │
│  └─────────────────────┬───────────────────────────┘  │
│                         │ enqueue()                     │
│  Service Layer          ▼                               │
│  ┌──────────────────────────────────────────────────┐  │
│  │ SessionService   ConcurrentHashMap<id, Session>  │  │
│  │                  JobScheduler.enqueue(ParseJob)  │  │
│  └──────────────────────┬───────────────────────────┘  │
│                          │ async (JobRunr queue)         │
│  Job Layer               ▼                              │
│  ┌──────────────────────────────────────────────────┐  │
│  │ ParseJob         @Job(retries=3)                 │  │
│  │                  HoundParser.parse(file, config)  │  │
│  │                  DaliHoundListener → SLF4J       │  │
│  └──────────────────────┬───────────────────────────┘  │
│                          │ parse result                  │
│                          ▼                               │
│  ┌──────────────────────────────────────────────────┐  │
│  │ HoundParserImpl  in-JVM (no subprocess)          │  │
│  └──────────────────────────────────────────────────┘  │
│                                                         │
│  Infrastructure                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │ JobRunrLifecycle  produces StorageProvider        │  │
│  │                   produces JobScheduler           │  │
│  │                   starts BackgroundJobServer      │  │
│  └──────────────────────────────────────────────────┘  │
│  ┌──────────────────────────────────────────────────┐  │
│  │ FriggSchemaInitializer  creates 4 ArcadeDB types │  │
│  │ FriggGateway            SQL over HTTP to FRIGG   │  │
│  └──────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────┘
              │
              ▼
   FRIGG (ArcadeDB :2481)  ← jobrunr_jobs, _servers, etc.
```

---

## REST API

### `POST /api/sessions`

```json
// Request
{ "dialect": "plsql", "source": "/path/to/file.pck", "preview": false }

// Response 202 Accepted
{ "id": "uuid", "status": "QUEUED", "dialect": "plsql",
  "progress": 0, "total": 0, "startedAt": "...", "updatedAt": "..." }
```

### `GET /api/sessions/{id}`

```json
// Response 200 OK (while running)
{ "id": "uuid", "status": "RUNNING", ... }

// Response 200 OK (when done)
{ "id": "uuid", "status": "COMPLETED", ... }

// Response 404 Not Found (unknown id)
```

**Session lifecycle:** `QUEUED → RUNNING → COMPLETED | FAILED`

---

## JobRunr Integration

Dali uses **JobRunr OSS 7.3.0** (not a Quarkus extension) configured via CDI:

```
JobRunrLifecycle (StartupEvent)
  → JobRunr.configure()
      .useStorageProvider(InMemoryStorageProvider)   ← TODO: ArcadeDbStorageProvider
      .useJobActivator(CDI.current()::select)
      .useBackgroundJobServer()
      .initialize()
  → returns JobRunrConfigurationResult.getJobScheduler()
  → JobScheduler produced as @Singleton CDI bean
```

The `JobActivator` uses the CDI container to resolve `ParseJob`, enabling
`@Inject HoundParser` and `@Inject SessionService` inside the job.

**Retry policy:** 3 attempts (configured via `@Job(retries = 3)` on `ParseJob.execute`).

---

## Storage: FRIGG (ArcadeDB)

### Schema (4 document types, created by `FriggSchemaInitializer` at startup)

| Type                       | Purpose                            | Indexes               |
|----------------------------|------------------------------------|----------------------|
| `jobrunr_jobs`             | Persisted job records              | `id` UNIQUE, `state` |
| `jobrunr_recurring_jobs`   | Recurring job definitions          | `id` UNIQUE          |
| `jobrunr_servers`          | Background server heartbeats       | `id` UNIQUE          |
| `jobrunr_metadata`         | Cluster-wide metadata              | `id` UNIQUE          |

### `ArcadeDbStorageProvider`

Implements `StorageProvider` (extends `AbstractStorageProvider`) with all required methods.
Uses `JobMapper.serializeJob(job)` for `Job` serialisation and `JacksonJsonMapper` for
`BackgroundJobServerStatus`.

**Current status:** Class is implemented but **not yet wired** as the active StorageProvider.
The `InMemoryStorageProvider` is active until integration testing with FRIGG is done.

To activate:
```java
// In JobRunrLifecycle.storageProvider():
@Inject FriggGateway frigg;

@Produces @ApplicationScoped
public StorageProvider storageProvider() {
    return new ArcadeDbStorageProvider(frigg);
}
```

---

## Configuration (`application.properties`)

```properties
quarkus.http.port=9090
quarkus.rest-client.dali-frigg.url=${FRIGG_URL:http://localhost:2481}
frigg.db=${FRIGG_DB:dali}
frigg.user=${FRIGG_USER:root}
frigg.password=${FRIGG_PASSWORD:playwithdata}
```

**Environment variables for Docker:**

| Variable        | Default               | Description               |
|-----------------|-----------------------|---------------------------|
| `FRIGG_URL`     | `http://localhost:2481` | ArcadeDB endpoint        |
| `FRIGG_DB`      | `dali`                | ArcadeDB database name    |
| `FRIGG_USER`    | `root`                | ArcadeDB username         |
| `FRIGG_PASSWORD`| `playwithdata`        | ArcadeDB password         |

---

## Outstanding Work

| Item                        | Priority | Notes                                    |
|-----------------------------|----------|------------------------------------------|
| Wire `ArcadeDbStorageProvider` | High  | Replace `InMemoryStorageProvider`        |
| Heimdall event forwarding   | Medium   | `DaliHoundListener` → HEIMDALL pipeline  |
| Directory parse support     | Medium   | `ParseJob` currently handles single file |
| `GET /api/sessions` list    | Low      | Paginated session list endpoint          |
| Integration tests (Д10)     | High     | Start FRIGG container, run end-to-end    |
