# HEIMDALL — Sprint Plan: первые два спринта + контурная интеграция

**Документ:** `HEIMDALL_SPRINT_PLAN`
**Версия:** 1.0
**Дата:** 12.04.2026
**Статус:** Working document — Track B

Этот документ описывает детальный план первых двух спринтов HEIMDALL backend и frontend, а также схему встраивания в общий контур после завершения репо-миграции.

---

## 0. Контекст и границы

**HEIMDALL = Admin Control Panel всей AIDA (ADR-DA-010)**

Две роли одновременно:
1. **Demo observability** — event stream, metrics, engineering spectacle на сцене
2. **Admin control** — reset/replay/cancel для demo safety

**Строгие границы scope (не выходить за них до октября):**

| В scope ✅ | Out of scope ❌ |
|---|---|
| In-memory ring buffer (10K events) | Persistent event storage |
| HTTP POST event ingestion | Kafka/Pulsar broker |
| WebSocket stream к frontend | Prometheus exposition |
| Micrometer counters/gauges | Distributed tracing (Jaeger) |
| Demo reset/replay/snapshot | Production SLA observability |
| `make demo-reset` < 5 сек | Multi-tenancy |

**Стек (HB1 — выбран как sensible default, Q3 подтвердить до mid-May):**

| Слой | Технология |
|---|---|
| Backend | Java 21 + Quarkus 3.34.2 |
| WebSocket | Quarkus/Vert.x WebSocket native |
| Metrics | Micrometer (встроен в Quarkus) |
| Persistence | FRIGG (ArcadeDB) — только snapshots |
| Port | :9093 |
| Build | `services/heimdall-backend/` в `aida-root/` |

**Допущение:** Q3 (HEIMDALL backend deployment) = HB1 (отдельный Quarkus). Если решение изменится на HB2/HB3 — план пересматривается. Но начинать нужно сейчас, не ждать.

---

## 1. Канонический Event Schema (фиксируется в Sprint 1, не меняется)

> **Стабильный контракт от Day 1.** При переходе к production observability — меняются collectors и UI, schema остаётся. Это решение ТРИЗ-противоречия «demo vs production».

```typescript
// aida-root/shared/dali-models/src/.../HeimdallEvent.java
public record HeimdallEvent(
    long       timestamp,        // unix ms
    String     sourceComponent,  // "hound" | "dali" | "mimir" | "anvil" | "shuttle" | "verdandi"
    String     eventType,        // см. EventType enum ниже
    EventLevel level,            // INFO | WARN | ERROR
    String     sessionId,        // nullable — привязка к parse session
    String     userId,           // nullable — кто инициировал
    String     correlationId,    // tie related events (напр., один tool call chain)
    long       durationMs,       // nullable — для duration events
    Map<String, Object> payload  // event-specific data
) {}

public enum EventLevel { INFO, WARN, ERROR }
```

**Canonical EventType registry (минимум для Sprint 1-2):**

```java
public enum EventType {
    // Hound (I26)
    FILE_PARSING_STARTED, FILE_PARSING_COMPLETED, FILE_PARSING_FAILED,
    ATOM_EXTRACTED, RESOLUTION_COMPLETED,

    // Dali (I27)
    SESSION_STARTED, SESSION_COMPLETED, SESSION_FAILED,
    WORKER_ASSIGNED, JOB_ENQUEUED, JOB_COMPLETED,
    COMPLEX_JOB_PROGRESS,          // progress % для batch jobs

    // MIMIR (I28)
    QUERY_RECEIVED, TIER_SELECTED,
    TOOL_CALL_STARTED, TOOL_CALL_COMPLETED,
    LLM_RESPONSE_READY, CACHE_HIT,

    // ANVIL (I29)
    TRAVERSAL_STARTED, TRAVERSAL_PROGRESS, TRAVERSAL_COMPLETED,

    // SHUTTLE (I30)
    REQUEST_RECEIVED, REQUEST_COMPLETED, SUBSCRIPTION_OPENED,

    // HEIMDALL internal
    DEMO_RESET, SNAPSHOT_SAVED, REPLAY_STARTED
}
```

Схема фиксируется в `shared/dali-models/` — доступна всем JVM модулям.

---

## 2. Sprint 1 — Skeleton + Event pipeline

**Цель:** HEIMDALL принимает события, хранит в ring buffer, стримит сырой поток. Верифицируется в изоляции — без других сервисов.

**Длительность:** ~1 неделя (Week 3: 26 апреля – 2 мая)
**Предусловие:** `aida-root/` skeleton готов (REPO_MIGRATION_PLAN шаги 1-4)

---

### H1.1 Quarkus project scaffold (~2 ч)

```bash
cd aida-root/services/heimdall-backend

# Quarkus CLI scaffold
quarkus create app studio.seer.aida:heimdall-backend \
  --extension=quarkus-rest,quarkus-websockets-next,quarkus-micrometer \
  --no-code
```

**`services/heimdall-backend/build.gradle`:**

```groovy
plugins {
    id 'java'
    id 'io.quarkus'
}

dependencies {
    implementation enforcedPlatform("io.quarkus.platform:quarkus-bom:${quarkusVersion}")
    implementation 'io.quarkus:quarkus-rest'
    implementation 'io.quarkus:quarkus-rest-jackson'
    implementation 'io.quarkus:quarkus-websockets-next'
    implementation 'io.quarkus:quarkus-micrometer'
    implementation 'io.quarkus:quarkus-arc'

    implementation project(':dali-models')   // HeimdallEvent, EventType, EventLevel
}
```

**`application.properties`:**

```properties
quarkus.http.port=9093
quarkus.application.name=heimdall-backend

# Ring buffer size
heimdall.ring-buffer.capacity=10000

# CORS (Chur is the only allowed origin)
quarkus.http.cors=true
quarkus.http.cors.origins=http://localhost:3000
```

Проверка: `./gradlew :heimdall-backend:quarkusDev` → `localhost:9093/q/health` отвечает.

---

### H1.2 Event ingestion endpoint (~3 ч)

**`EventResource.java`** — принимает события от всех эмиттеров:

```java
@Path("/events")
@Consumes(MediaType.APPLICATION_JSON)
public class EventResource {

    @Inject RingBuffer ringBuffer;
    @Inject MetricsCollector metrics;

    @POST
    public Response ingest(HeimdallEvent event) {
        // Validate
        if (event.sourceComponent() == null || event.eventType() == null) {
            return Response.status(400).build();
        }
        // Enrich timestamp if not set
        var enriched = event.timestamp() > 0 ? event
            : new HeimdallEvent(System.currentTimeMillis(), /* rest */ ...);

        ringBuffer.push(enriched);
        metrics.record(enriched);
        return Response.accepted().build();   // fire-and-forget, 202
    }

    @POST @Path("/batch")
    public Response ingestBatch(List<HeimdallEvent> events) {
        events.forEach(this::processOne);
        return Response.accepted().build();
    }
}
```

Проверка: `curl -X POST localhost:9093/events -d '{"sourceComponent":"hound","eventType":"FILE_PARSING_STARTED",...}'` → 202.

---

### H1.3 Ring buffer (in-memory, ~3 ч)

```java
@ApplicationScoped
public class RingBuffer {

    @ConfigProperty(name = "heimdall.ring-buffer.capacity", defaultValue = "10000")
    int capacity;

    private final ArrayDeque<HeimdallEvent> buffer = new ArrayDeque<>();
    private final List<Consumer<HeimdallEvent>> subscribers = new CopyOnWriteArrayList<>();

    public synchronized void push(HeimdallEvent event) {
        if (buffer.size() >= capacity) {
            buffer.pollFirst();   // evict oldest
        }
        buffer.addLast(event);
        subscribers.forEach(s -> s.accept(event));   // notify WebSocket sessions
    }

    public synchronized List<HeimdallEvent> snapshot() {
        return List.copyOf(buffer);
    }

    public synchronized List<HeimdallEvent> since(long timestampMs) {
        return buffer.stream()
            .filter(e -> e.timestamp() >= timestampMs)
            .toList();
    }

    public void subscribe(Consumer<HeimdallEvent> subscriber) {
        subscribers.add(subscriber);
    }

    public void unsubscribe(Consumer<HeimdallEvent> subscriber) {
        subscribers.remove(subscriber);
    }
}
```

---

### H1.4 WebSocket server — raw event stream (~4 ч)

```java
@ServerEndpoint("/ws/events")
@ApplicationScoped
public class EventStreamEndpoint {

    @Inject RingBuffer ringBuffer;
    @Inject ObjectMapper mapper;

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("filter") String filter) {
        sessions.put(session.getId(), session);

        // Replay last N events on connect (для cold start)
        ringBuffer.snapshot().stream()
            .limit(200)
            .forEach(e -> send(session, e));

        // Subscribe to live stream
        ringBuffer.subscribe(event -> send(session, event));
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session.getId());
    }

    private void send(Session session, HeimdallEvent event) {
        try {
            session.getAsyncRemote().sendText(mapper.writeValueAsString(event));
        } catch (Exception e) {
            sessions.remove(session.getId());
        }
    }
}
```

Проверка: `wscat -c ws://localhost:9093/ws/events` → получает live события после `curl -X POST /events`.

---

### H1.5 Docker integration (~1 ч)

**`services/heimdall-backend/Dockerfile`:**

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/quarkus-app/lib/ ./lib/
COPY build/quarkus-app/*.jar ./
COPY build/quarkus-app/app/ ./app/
COPY build/quarkus-app/quarkus/ ./quarkus/
EXPOSE 9093
CMD ["java", "-jar", "quarkus-run.jar"]
```

Добавить в `docker-compose.yml`:

```yaml
heimdall-backend:
  build: ./services/heimdall-backend
  ports: ["9093:9093"]
  environment:
    - QUARKUS_HTTP_PORT=9093
  networks: [aida_net]
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:9093/q/health"]
    interval: 10s
```

---

### Sprint 1 Definition of Done

- [ ] `./gradlew :heimdall-backend:quarkusDev` стартует на :9093
- [ ] `POST /events` принимает события, возвращает 202
- [ ] `POST /events/batch` принимает массив
- [ ] `ws://localhost:9093/ws/events` стримит живые события
- [ ] Ring buffer хранит последние 10K, evicts oldest
- [ ] Cold start replay: при connect клиент получает последние 200 событий
- [ ] Docker: `docker compose up heimdall-backend` поднимается
- [ ] Unit тест: RingBuffer eviction, subscribe/unsubscribe
- [ ] `HeimdallEvent` schema зафиксирована в `dali-models`, не меняется

---

## 3. Sprint 2 — Metrics + Filtered streams + Control

**Цель:** HEIMDALL показывает live метрики, поддерживает filtered subscriptions, умеет reset/snapshot. Готов к подключению первых эмиттеров (Dali + SHUTTLE).

**Длительность:** ~1.5 недели (Week 4: 3–13 мая)
**Предусловие:** Sprint 1 Done, Dali skeleton готов (параллельно)

---

### H2.1 Metrics aggregation (~3 дня)

```java
@ApplicationScoped
public class MetricsCollector {

    // Micrometer counters — атомарные, thread-safe
    private final Counter atomsExtracted;
    private final Counter filesParsed;
    private final Counter toolCallsTotal;
    private final Counter sessionStarted;

    // Gauges — текущее состояние
    private final AtomicLong activeWorkers   = new AtomicLong(0);
    private final AtomicLong queueDepth      = new AtomicLong(0);
    private final AtomicLong ringBufferSize  = new AtomicLong(0);

    // Histograms — распределение латентностей
    private final Timer parseLatency;
    private final Timer toolCallLatency;

    // Resolution rate (для Correctness demo)
    private final AtomicLong resolvedAtoms = new AtomicLong(0);
    private final AtomicLong totalAtoms    = new AtomicLong(0);

    public MetricsCollector(MeterRegistry registry) {
        this.atomsExtracted  = Counter.builder("hound.atoms.extracted").register(registry);
        this.filesParsed     = Counter.builder("hound.files.parsed").register(registry);
        this.toolCallsTotal  = Counter.builder("mimir.tool_calls.total").register(registry);
        this.sessionStarted  = Counter.builder("dali.sessions.started").register(registry);
        this.parseLatency    = Timer.builder("hound.parse.latency").register(registry);
        this.toolCallLatency = Timer.builder("mimir.tool_call.latency").register(registry);

        Gauge.builder("dali.workers.active", activeWorkers, AtomicLong::get).register(registry);
        Gauge.builder("dali.queue.depth",    queueDepth,    AtomicLong::get).register(registry);
        Gauge.builder("heimdall.ring.size",  ringBufferSize, AtomicLong::get).register(registry);
    }

    public void record(HeimdallEvent event) {
        switch (EventType.valueOf(event.eventType())) {
            case ATOM_EXTRACTED       -> { atomsExtracted.increment(); totalAtoms.incrementAndGet(); }
            case RESOLUTION_COMPLETED -> resolvedAtoms.incrementAndGet();
            case FILE_PARSING_STARTED -> filesParsed.increment();
            case TOOL_CALL_COMPLETED  -> toolCallsTotal.increment();
            case SESSION_STARTED      -> sessionStarted.increment();
            case WORKER_ASSIGNED      -> activeWorkers.incrementAndGet();
            case JOB_COMPLETED        -> activeWorkers.decrementAndGet();
            // и т.д.
        }
    }

    public MetricsSnapshot snapshot() {
        return new MetricsSnapshot(
            atomsExtracted.count(),
            filesParsed.count(),
            toolCallsTotal.count(),
            activeWorkers.get(),
            queueDepth.get(),
            totalAtoms.get() > 0
                ? (double) resolvedAtoms.get() / totalAtoms.get()
                : 0.0
        );
    }
}
```

**Endpoint для snapshot:**

```java
@GET @Path("/metrics/snapshot")
@Produces(MediaType.APPLICATION_JSON)
public MetricsSnapshot getSnapshot() {
    return metrics.snapshot();
}
```

---

### H2.2 Filtered WebSocket subscriptions (~2 дня)

Клиенты подписываются с фильтрами — чтобы VERDANDI видел только `sessionProgress`, а HEIMDALL frontend — всё.

```
ws://localhost:9093/ws/events?filter=session_id:abc123
ws://localhost:9093/ws/events?filter=component:mimir
ws://localhost:9093/ws/events?filter=level:ERROR
ws://localhost:9093/ws/events        ← без фильтра = всё
```

```java
@OnOpen
public void onOpen(Session session,
                   @PathParam("filter") @DefaultValue("") String rawFilter) {
    var filter = EventFilter.parse(rawFilter);   // session_id, component, level, eventType
    sessions.put(session.getId(), new SessionCtx(session, filter));

    // Replay last N filtered events on connect
    ringBuffer.snapshot().stream()
        .filter(filter::matches)
        .limit(200)
        .forEach(e -> send(session, e));

    ringBuffer.subscribe(event -> {
        if (filter.matches(event)) send(session, event);
    });
}
```

```java
public record EventFilter(
    String sessionId,      // nullable
    String component,      // nullable
    EventLevel minLevel,   // default INFO
    String eventType       // nullable
) {
    public static EventFilter parse(String raw) { /* parse key:value */ }
    public boolean matches(HeimdallEvent e) {
        if (sessionId != null && !sessionId.equals(e.sessionId())) return false;
        if (component  != null && !component.equals(e.sourceComponent())) return false;
        if (eventType  != null && !eventType.equals(e.eventType())) return false;
        return e.level().ordinal() >= minLevel.ordinal();
    }
}
```

---

### H2.3 Control resource — reset / snapshot / cancel (~2 дня)

```java
@Path("/control")
public class ControlResource {

    @Inject DaliClient daliClient;      // REST client (C.2.4)
    @Inject RingBuffer ringBuffer;
    @Inject SnapshotManager snapshots;  // сохранение в FRIGG

    // Сброс demo state — главная кнопка для make demo-reset
    @POST @Path("/reset")
    @RolesAllowed("aida:admin:destructive")
    public Uni<Void> resetDemoState() {
        return daliClient.cancelAllSessions()
            .flatMap(_ -> snapshots.restoreBaseline())
            .invoke(() -> {
                ringBuffer.clear();
                ringBuffer.push(HeimdallEvent.internal(DEMO_RESET, "Demo state restored"));
            });
    }

    // Сохранить текущий snapshot
    @POST @Path("/snapshot")
    @RolesAllowed("aida:admin")
    public Uni<String> saveSnapshot(@QueryParam("name") String name) {
        var snap = ringBuffer.snapshot();
        return snapshots.save(name, snap)
            .map(id -> "{\"snapshotId\": \"" + id + "\"}");
    }

    // Отменить конкретную сессию
    @POST @Path("/cancel/{sessionId}")
    @RolesAllowed("aida:admin")
    public Uni<Void> cancelSession(@PathParam("sessionId") String sessionId) {
        return daliClient.cancelSession(sessionId);
    }

    // Список сохранённых снапшотов
    @GET @Path("/snapshots")
    @RolesAllowed("aida:admin")
    public Uni<List<SnapshotInfo>> listSnapshots() {
        return snapshots.list();
    }
}
```

---

### H2.4 Подключение первых эмиттеров: Dali + SHUTTLE (~1 день)

После этого шага события начнут реально поступать в HEIMDALL.

**В `services/dali/` — добавить HeimdallEmitter:**

```java
@ApplicationScoped
public class HeimdallEmitter {

    @RestClient HeimdallClient heimdall;

    // Вызывается из Dali при старте сессии
    public void sessionStarted(String sessionId, String userId, int fileCount) {
        emit(HeimdallEvent.builder()
            .sourceComponent("dali")
            .eventType(SESSION_STARTED.name())
            .level(EventLevel.INFO)
            .sessionId(sessionId)
            .userId(userId)
            .payload(Map.of("file_count", fileCount))
            .build());
    }

    public void workerAssigned(String sessionId, String jobId) {
        emit(HeimdallEvent.builder()
            .sourceComponent("dali")
            .eventType(WORKER_ASSIGNED.name())
            .sessionId(sessionId)
            .payload(Map.of("job_id", jobId))
            .build());
    }

    // Fire-and-forget — не блокируем основной pipeline
    private void emit(HeimdallEvent event) {
        heimdall.ingest(event)
            .subscribe().with(
                _ -> {},   // success — ignore
                e -> Log.warnf("HEIMDALL emit failed (non-critical): %s", e.getMessage())
            );
    }
}
```

**`@RegisterRestClient` для HeimdallClient:**

```java
@RegisterRestClient(configKey = "heimdall-api")
@Path("/events")
public interface HeimdallClient {
    @POST Uni<Void> ingest(HeimdallEvent event);
    @POST @Path("/batch") Uni<Void> ingestBatch(List<HeimdallEvent> events);
}
```

**`application.properties` в Dali/SHUTTLE:**

```properties
quarkus.rest-client.heimdall-api.url=http://heimdall-backend:9093
quarkus.rest-client.heimdall-api.connect-timeout=500
quarkus.rest-client.heimdall-api.read-timeout=1000
# Критично: HEIMDALL недоступен — это не ошибка, не падать
```

---

### H2.5 SnapshotManager — persistence в FRIGG (~1 день)

```java
@ApplicationScoped
public class SnapshotManager {

    @Inject ArcadeDBClient frigg;   // FRIGG instance на :2481

    public Uni<String> save(String name, List<HeimdallEvent> events) {
        var id = UUID.randomUUID().toString();
        var doc = Map.of(
            "id", id, "name", name,
            "created_at", System.currentTimeMillis(),
            "events", events
        );
        return frigg.insertDocument("HeimdallSnapshot", doc).map(_ -> id);
    }

    public Uni<Void> restoreBaseline() {
        // Найти snapshot с name="baseline" и восстановить
        return frigg.query("SELECT FROM HeimdallSnapshot WHERE name='baseline' LIMIT 1")
            .flatMap(this::loadSnapshot);
    }

    public Uni<List<SnapshotInfo>> list() {
        return frigg.query("SELECT id, name, created_at FROM HeimdallSnapshot ORDER BY created_at DESC");
    }
}
```

---

### Sprint 2 Definition of Done

- [ ] `GET /metrics/snapshot` возвращает live counters
- [ ] WebSocket фильтрация по `session_id`, `component`, `level` работает
- [ ] `POST /control/reset` — сбрасывает state, Dali получает cancel команду
- [ ] `POST /control/snapshot` — сохраняет в FRIGG
- [ ] Dali HeimdallEmitter подключён: SESSION_STARTED, WORKER_ASSIGNED, JOB_COMPLETED
- [ ] SHUTTLE HeimdallEmitter подключён: REQUEST_RECEIVED, REQUEST_COMPLETED
- [ ] Non-critical: падение HEIMDALL не роняет Dali/SHUTTLE (try-catch, fire-and-forget)
- [ ] Unit тесты: MetricsCollector, EventFilter.matches(), RingBuffer.since()
- [ ] Integration test: Dali → HEIMDALL → WebSocket client получает события

---

## 4. Встраивание в общий контур

После завершения Sprint 1-2 нужно встроить HEIMDALL в полный request path. Это происходит параллельно с другими C.x задачами.

### 4.1 Chur → HEIMDALL proxy (C.3.1, ~1-2 дня)

Chur становится единой точкой входа — добавить два маршрута:

```typescript
// bff/chur/src/routes/heimdall.ts

// Admin GraphQL (HEIMDALL frontend читает статистику)
app.post('/heimdall/graphql', {
    preHandler: [authenticate, requireScope('aida:admin')]
}, async (req, reply) => {
    return proxy(req, reply, HEIMDALL_URL + '/graphql');
});

// Admin control commands (destructive — отдельный scope)
app.post('/heimdall/control/:action', {
    preHandler: [authenticate, requireScope('aida:admin:destructive')]
}, async (req, reply) => {
    return proxy(req, reply, HEIMDALL_URL + '/control/' + req.params.action);
});

// WebSocket upgrade для HEIMDALL frontend
app.get('/heimdall/ws/events', {
    websocket: true,
    preHandler: [authenticate, requireScope('aida:admin')]
}, (socket, req) => {
    const upstream = new WebSocket(HEIMDALL_WS_URL + '/ws/events?'
        + new URLSearchParams(req.query).toString());
    socket.pipe(upstream);
    upstream.pipe(socket);
});
```

---

### 4.2 VERDANDI → HEIMDALL events (C.4.1 + C.4.4, ~2 дня)

VERDANDI подписывается на `sessionProgress` через SHUTTLE subscriptions. HEIMDALL события транслируются через SHUTTLE GraphQL subscriptions (I33):

```graphql
# SHUTTLE schema (C.2.3)
type Subscription {
  sessionProgress(sessionId: ID!): SessionProgress!
  heimdallEvents(filter: EventFilterInput): HeimdallEvent!
}
```

SHUTTLE subscribes to HEIMDALL и проксирует в GraphQL subscription:

```java
// SHUTTLE SubscriptionResource
@Channel("heimdall-events")
Multi<HeimdallEvent> heimdallStream;

@Subscription
public Multi<HeimdallEvent> heimdallEvents(EventFilterInput filter) {
    return heimdallStream
        .filter(e -> matchesFilter(e, filter))
        .onOverflow().drop();
}
```

**В VERDANDI — WebSocket client (C.4.1):**

```typescript
// frontends/verdandi/src/hooks/useSessionProgress.ts
import { createClient } from 'graphql-ws';

const wsClient = createClient({ url: 'ws://localhost:3000/graphql' });

export function useSessionProgress(sessionId: string) {
    const [progress, setProgress] = useState<SessionProgress | null>(null);

    useEffect(() => {
        const unsub = wsClient.subscribe(
            { query: SUBSCRIBE_SESSION_PROGRESS, variables: { sessionId } },
            { next: ({ data }) => setProgress(data.sessionProgress) }
        );
        return unsub;
    }, [sessionId]);

    return progress;
}
```

---

### 4.3 Все эмиттеры — подключение по очереди

После Sprint 2 (Dali + SHUTTLE уже подключены) добавляем остальные:

| Эмиттер | Когда | Интеграция | Ключевые события |
|---|---|---|---|
| **Hound** | C.1 done (~Week 2) | `HoundEventListener.onAtomExtracted` → HeimdallEmitter | ATOM_EXTRACTED, RESOLUTION_COMPLETED |
| **Dali** | Sprint 2 | HeimdallEmitter инжектится в JobRunr workers | SESSION_*, WORKER_ASSIGNED, JOB_* |
| **SHUTTLE** | Sprint 2 | CDI interceptor на GraphQL resolvers | REQUEST_RECEIVED, REQUEST_COMPLETED |
| **MIMIR** | Июнь | При каждом tool call | TOOL_CALL_STARTED/COMPLETED, TIER_SELECTED |
| **ANVIL** | Июнь | При каждом traversal | TRAVERSAL_STARTED/PROGRESS/COMPLETED |

**Паттерн для всех:** fire-and-forget, non-critical, отдельный thread pool.

---

### 4.4 HEIMDALL frontend skeleton (конец мая – июнь)

Отдельный React проект в `frontends/heimdall-frontend/`:

```
frontends/heimdall-frontend/
├── package.json
├── vite.config.ts
└── src/
    ├── main.tsx
    ├── App.tsx
    ├── pages/
    │   ├── DashboardPage.tsx    ← metrics snapshot (Sprint 2 ready)
    │   ├── EventStreamPage.tsx  ← live ring buffer view
    │   └── ControlPage.tsx      ← reset/snapshot/cancel
    ├── hooks/
    │   ├── useMetrics.ts        ← polling /metrics/snapshot
    │   └── useEventStream.ts    ← WebSocket /ws/events
    └── components/
        ├── MetricsBar.tsx       ← atoms count, resolution %, workers
        ├── EventLog.tsx         ← virtualized list (react-virtuoso)
        └── ControlPanel.tsx     ← reset button + snapshot list
```

**Минимальный Sprint 1 frontend (ещё до официального "HEIMDALL frontend sprint"):**

```typescript
// EventStreamPage.tsx — smoke test в браузере
export function EventStreamPage() {
    const [events, setEvents] = useState<HeimdallEvent[]>([]);

    useEffect(() => {
        const ws = new WebSocket('ws://localhost:9093/ws/events');
        ws.onmessage = (msg) => {
            const event = JSON.parse(msg.data) as HeimdallEvent;
            setEvents(prev => [event, ...prev.slice(0, 199)]);
        };
        return () => ws.close();
    }, []);

    return (
        <div style={{ fontFamily: 'monospace', padding: 16 }}>
            {events.map((e, i) => (
                <div key={i} style={{ color: levelColor(e.level) }}>
                    [{new Date(e.timestamp).toISOTimeString()}]
                    {e.sourceComponent} · {e.eventType}
                    {e.sessionId && ` · session:${e.sessionId}`}
                </div>
            ))}
        </div>
    );
}
```

Этот минимальный вариант живёт в `bff/chur/` как static file или в отдельном `heimdall-dev-ui/` — **не требует полного React app** для smoke testing событий во время разработки.

---

### 4.5 Итоговая схема контура (после всех подключений)

```
Browser (VERDANDI :13000)
    │  GraphQL subscriptions (C.4.1)
    ▼
Chur BFF (:3000)
    │  proxy /graphql → SHUTTLE
    │  proxy /heimdall/ws → HEIMDALL
    ▼
SHUTTLE (:8080) ──── GraphQL subscriptions → VERDANDI
    │  I30 HTTP POST
    │  I33 proxy subscription stream
    ▼
HEIMDALL backend (:9093)
    │  WebSocket /ws/events
    │
    ├── Ring buffer (10K in-memory)
    │       ↑ ingest от: Hound · Dali · MIMIR · ANVIL · SHUTTLE
    │       │  via HTTP POST /events (Вариант A, Q25)
    │
    ├── Metrics aggregation (Micrometer)
    │
    ├── Control resource
    │       └── POST /control/reset → Dali :9090
    │
    └── Snapshot persistence
            └── FRIGG (:2481, отдельный ArcadeDB)

Browser (HEIMDALL frontend :14000)
    │  via Chur /heimdall/ws
    ▼
HEIMDALL backend (:9093)
    WebSocket /ws/events (admin — без фильтра, всё)
```

---

## 5. Demo safety интеграция

Когда контур собран — добавить в `Makefile`:

```makefile
# Одна команда для всего demo
demo-start:
	docker compose up -d
	sleep 5
	curl -s http://localhost:9093/q/health | jq .status
	@echo "✓ HEIMDALL ready"
	@echo "✓ Demo stack started"

# Быстрый reset (< 5 сек)
demo-reset:
	curl -s -X POST http://localhost:3000/heimdall/control/reset \
	     -H "Cookie: sid=$$ADMIN_SID" | jq .
	@echo "✓ Demo state restored"

# Сохранить baseline snapshot
demo-snapshot:
	curl -s -X POST "http://localhost:9093/control/snapshot?name=baseline" | jq .snapshotId
```

---

## 6. Открытые вопросы, блокирующие Sprint 2

| # | Вопрос | Нужно для | Срок |
|---|---|---|---|
| **Q3** | HEIMDALL backend = HB1 Quarkus? (подтвердить) | Sprint 1 стартует с этим допущением | mid-May confirm |
| **Q6** | HEIMDALL event schema финальная | Фиксируется в Sprint 1 как working draft | mid-May |
| **Q7** | HEIMDALL ↔ Dali control API | `/control/reset` → Dali endpoint | end April |
| **Q25** | Event bus transport | HTTP POST (Вариант A) — стартуем с ним | mid-May confirm |
| **Q13** | WebSocket protocol (graphql-ws vs native) | Sprint 2 WebSocket сервер | mid-May |

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 12.04.2026 | 1.0 | Initial. Sprint 1 (event pipeline), Sprint 2 (metrics + control + первые эмиттеры). Контурная интеграция: Chur proxy, VERDANDI subscription, все эмиттеры по очереди, HEIMDALL frontend skeleton. Demo safety Makefile targets. |
