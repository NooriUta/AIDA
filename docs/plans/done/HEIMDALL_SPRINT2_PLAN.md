# HEIMDALL — Детальный план Sprint 2: Метрики + Фильтрация + Контроль

**Ветка:** `feature/heimdall-sprint2`  
**Дата:** 2026-04-12  
**Основан на:** `docs/plans/HEIMDALL_IMPL_PLAN.md` + прочитанный код Sprint 1

---

## Наблюдения из кода Sprint 1

**RingBuffer.java**: `clear()` сам вызывает `push(HeimdallEvent.internal(DEMO_RESET, ...))` — подписчики WebSocket получат DEMO_RESET автоматически. ControlResource не должен делать отдельный push.

**EventStreamEndpoint.java**: Cold-start replay берёт `snapshot().stream().limit(200)` — **первые** 200 из буфера, не последние. Это баг Sprint 1; при добавлении фильтра учитываем этот же порядок (не меняем семантику).

**HeimdallEvent**: поле `eventType` — это `String`, не `EventType`. В `EventFilter.matches()` сравниваем строки (`e.eventType().equals("ATOM_EXTRACTED")`).

**ArcadeGateway паттерн (SHUTTLE)**: `@RestClient` inject, `basicAuth()` через Base64, Uni-возвраты. FriggGateway аналогичен.

**Fastify + WebSocket (Chur)**: В `package.json` нет `@fastify/websocket`. WebSocket-прокси в шаге 2.6 требует установки пакета.

**RBAC в Chur**: нет понятия scope (`aida:admin`), только `UserRole` (viewer/editor/admin). Маппинг: `aida:admin` → role == "admin".

---

## Порядок создания файлов (по зависимостям)

```
Шаг 1:  MetricsSnapshot.java              новый record
Шаг 2:  MetricsCollector.java             зависит от MetricsSnapshot + EventType
Шаг 3:  EventResource.java               ИЗМЕНЕНИЕ: inject MetricsCollector
Шаг 4:  MetricsResource.java             зависит от MetricsCollector
Шаг 5:  EventFilter.java                 зависит от HeimdallEvent/EventLevel
Шаг 6:  EventStreamEndpoint.java         ИЗМЕНЕНИЕ: filter из query param
Шаг 7:  SnapshotInfo.java                новый record
Шаг 8:  FriggCommand.java               вспомогательный record
Шаг 9:  FriggResponse.java              вспомогательный record
Шаг 10: FriggClient.java                @RegisterRestClient
Шаг 11: FriggGateway.java               зависит от FriggClient
Шаг 12: SnapshotManager.java            зависит от FriggGateway + SnapshotInfo
Шаг 13: ControlResource.java            зависит от RingBuffer + MetricsCollector + SnapshotManager
Шаг 14: build.gradle heimdall-backend   ИЗМЕНЕНИЕ: quarkus-rest-client-jackson
Шаг 15: application.properties heimdall ИЗМЕНЕНИЕ: frigg config
─── SHUTTLE ────────────────────────────────────────────────────────────────────
Шаг 16: studio.seer.lineage.heimdall.model.HeimdallEvent  копия (временно)
Шаг 17: studio.seer.lineage.heimdall.model.EventType      копия (временно)
Шаг 18: studio.seer.lineage.heimdall.model.EventLevel     копия (временно)
Шаг 19: HeimdallClient.java             @RegisterRestClient
Шаг 20: HeimdallEmitter.java            fire-and-forget
Шаг 21: LineageResource.java            ИЗМЕНЕНИЕ: inject HeimdallEmitter
Шаг 22: application.properties SHUTTLE  ИЗМЕНЕНИЕ: heimdall-api config
─── Chur ───────────────────────────────────────────────────────────────────────
Шаг 23: bff/chur/src/routes/heimdall.ts  новый файл
Шаг 24: bff/chur/src/server.ts           ИЗМЕНЕНИЕ: register routes
```

---

## Шаг 1: MetricsSnapshot.java

`services/heimdall-backend/src/main/java/studio/seer/heimdall/metrics/MetricsSnapshot.java`

```java
package studio.seer.heimdall.metrics;

/**
 * Immutable snapshot of aggregated HEIMDALL metrics.
 *
 * @param atomsExtracted  число событий ATOM_EXTRACTED
 * @param filesParsed     FILE_PARSING_COMPLETED
 * @param toolCallsTotal  TOOL_CALL_COMPLETED
 * @param activeWorkers   WORKER_ASSIGNED (монотонный счётчик в Sprint 2)
 * @param queueDepth      JOB_ENQUEUED - JOB_COMPLETED (gauge)
 * @param resolutionRate  resolutions/filesParsed * 100.0, NaN если filesParsed==0
 */
public record MetricsSnapshot(
        long   atomsExtracted,
        long   filesParsed,
        long   toolCallsTotal,
        long   activeWorkers,
        long   queueDepth,
        double resolutionRate
) {}
```

---

## Шаг 2: MetricsCollector.java

`services/heimdall-backend/src/main/java/studio/seer/heimdall/metrics/MetricsCollector.java`

**Решение**: `AtomicLong` вместо Micrometer Counter — Counter#count() возвращает double с накопленной погрешностью, AtomicLong даёт точные значения. MeterRegistry инъектируется для будущего Prometheus scrape (Gauge регистрируем там же).

```java
package studio.seer.heimdall.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import studio.seer.shared.HeimdallEvent;

import java.util.concurrent.atomic.AtomicLong;

@ApplicationScoped
public class MetricsCollector {

    private static final Logger LOG = Logger.getLogger(MetricsCollector.class);

    @Inject MeterRegistry meterRegistry;

    private final AtomicLong atomsExtracted   = new AtomicLong(0);
    private final AtomicLong filesParsed      = new AtomicLong(0);
    private final AtomicLong toolCallsTotal   = new AtomicLong(0);
    private final AtomicLong resolutionsTotal = new AtomicLong(0);
    private final AtomicLong activeWorkers    = new AtomicLong(0);
    private final AtomicLong queueDepth       = new AtomicLong(0);

    public void record(HeimdallEvent event) {
        if (event == null || event.eventType() == null) return;
        switch (event.eventType()) {
            case "ATOM_EXTRACTED"         -> atomsExtracted.incrementAndGet();
            case "FILE_PARSING_COMPLETED" -> filesParsed.incrementAndGet();
            case "TOOL_CALL_COMPLETED"    -> toolCallsTotal.incrementAndGet();
            case "RESOLUTION_COMPLETED"   -> resolutionsTotal.incrementAndGet();
            case "WORKER_ASSIGNED"        -> activeWorkers.incrementAndGet();
            case "JOB_ENQUEUED"           -> queueDepth.incrementAndGet();
            case "JOB_COMPLETED"          -> { long d = queueDepth.decrementAndGet(); if (d < 0) queueDepth.set(0); }
            case "DEMO_RESET"             -> reset();
            default                       -> { /* прочие типы не агрегируются */ }
        }
    }

    public MetricsSnapshot snapshot() {
        long parsed   = filesParsed.get();
        long resolved = resolutionsTotal.get();
        double rate   = parsed == 0 ? Double.NaN : (resolved * 100.0) / parsed;
        return new MetricsSnapshot(
                atomsExtracted.get(), parsed, toolCallsTotal.get(),
                activeWorkers.get(), queueDepth.get(), rate
        );
    }

    public void reset() {
        atomsExtracted.set(0); filesParsed.set(0); toolCallsTotal.set(0);
        resolutionsTotal.set(0); activeWorkers.set(0); queueDepth.set(0);
        LOG.info("MetricsCollector reset");
    }
}
```

---

## Шаг 3: EventResource.java (изменение)

```diff
 import studio.seer.heimdall.RingBuffer;
+import studio.seer.heimdall.metrics.MetricsCollector;

 public class EventResource {
     @Inject RingBuffer ringBuffer;
+    @Inject MetricsCollector metricsCollector;

     @POST
     public Response ingest(HeimdallEvent event) {
         // ...enrichment...
         ringBuffer.push(enriched);
+        metricsCollector.record(enriched);
         return Response.accepted().build();
     }

     @POST @Path("/batch")
     public Response ingestBatch(List<HeimdallEvent> events) {
         events.stream()
               .filter(e -> e != null && e.sourceComponent() != null && e.eventType() != null)
-              .peek(ringBuffer::push)
+              .peek(e -> { ringBuffer.push(e); metricsCollector.record(e); })
               .count();
         return Response.accepted().build();
     }
 }
```

---

## Шаг 4: MetricsResource.java

`services/heimdall-backend/src/main/java/studio/seer/heimdall/resource/MetricsResource.java`

```java
package studio.seer.heimdall.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import studio.seer.heimdall.metrics.MetricsCollector;
import studio.seer.heimdall.metrics.MetricsSnapshot;

@Path("/metrics")
@Produces(MediaType.APPLICATION_JSON)
public class MetricsResource {

    @Inject MetricsCollector metricsCollector;

    @GET @Path("/snapshot")
    public MetricsSnapshot snapshot() {
        return metricsCollector.snapshot();
    }
}
```

---

## Шаг 5: EventFilter.java

`services/heimdall-backend/src/main/java/studio/seer/heimdall/ws/EventFilter.java`

**Решение**: Sprint 2 — один key:value (комбинированные фильтры в Sprint 3). `minLevel` — ordinal comparison (INFO=0 < WARN=1 < ERROR=2).

```java
package studio.seer.heimdall.ws;

import org.jboss.logging.Logger;
import studio.seer.shared.EventLevel;
import studio.seer.shared.HeimdallEvent;

/**
 * Фильтр WebSocket-стрима. Все ненулевые поля — AND-логика.
 *
 * Форматы:
 *   component:mimir     → sourceComponent == "mimir"
 *   level:ERROR         → level.ordinal >= ERROR.ordinal
 *   session_id:abc123   → sessionId == "abc123"
 *   type:ATOM_EXTRACTED → eventType == "ATOM_EXTRACTED"
 */
public record EventFilter(String sessionId, String component, EventLevel minLevel, String eventType) {

    private static final Logger LOG = Logger.getLogger(EventFilter.class);

    public static EventFilter empty() {
        return new EventFilter(null, null, null, null);
    }

    public static EventFilter parse(String raw) {
        if (raw == null || raw.isBlank()) return empty();
        int colon = raw.indexOf(':');
        if (colon <= 0 || colon == raw.length() - 1) {
            LOG.warnf("EventFilter: malformed '%s'", raw);
            return empty();
        }
        String key   = raw.substring(0, colon).trim().toLowerCase();
        String value = raw.substring(colon + 1).trim();

        return switch (key) {
            case "component"  -> new EventFilter(null, value, null, null);
            case "session_id" -> new EventFilter(value, null, null, null);
            case "type"       -> new EventFilter(null, null, null, value.toUpperCase());
            case "level"      -> {
                try { yield new EventFilter(null, null, EventLevel.valueOf(value.toUpperCase()), null); }
                catch (IllegalArgumentException e) {
                    LOG.warnf("EventFilter: unknown level '%s'", value);
                    yield empty();
                }
            }
            default -> { LOG.warnf("EventFilter: unknown key '%s'", key); yield empty(); }
        };
    }

    public boolean matches(HeimdallEvent e) {
        if (sessionId != null && !sessionId.equals(e.sessionId()))         return false;
        if (component  != null && !component.equals(e.sourceComponent()))   return false;
        if (eventType  != null && !eventType.equals(e.eventType()))         return false;
        if (minLevel   != null) {
            EventLevel lv = e.level();
            if (lv == null || lv.ordinal() < minLevel.ordinal())            return false;
        }
        return true;
    }

    public boolean isEmpty() {
        return sessionId == null && component == null && minLevel == null && eventType == null;
    }
}
```

---

## Шаг 6: EventStreamEndpoint.java (изменение)

**Риск #1**: `connection.handshakeRequest().queryParam("filter")` — может вернуть `String` или `Optional<String>` в зависимости от версии Quarkus 3.34.x. Проверить при первой компиляции.

```diff
+import studio.seer.heimdall.ws.EventFilter;

 @OnOpen
 public Uni<Void> onOpen(WebSocketConnection connection) {
+    String rawFilter = connection.handshakeRequest().queryParam("filter"); // или .orElse(null) если Optional
+    EventFilter filter = EventFilter.parse(rawFilter);

     Consumer<HeimdallEvent> subscriber = event -> {
+        if (!filter.matches(event)) return;
         if (connection.isOpen()) {
             connection.sendText(toJson(event))...
         }
     };

     subscribers.put(connection.id(), subscriber);
     ringBuffer.subscribe(subscriber);

-    var replay = ringBuffer.snapshot().stream().limit(COLD_START_REPLAY_SIZE).toList();
+    var replay = ringBuffer.snapshot().stream()
+            .filter(filter::matches)
+            .limit(COLD_START_REPLAY_SIZE)
+            .toList();
     // ... остальное без изменений
```

---

## Шаг 7–9: SnapshotInfo + FriggCommand + FriggResponse

`services/heimdall-backend/src/main/java/studio/seer/heimdall/snapshot/`

```java
// SnapshotInfo.java
public record SnapshotInfo(String id, String name, long timestamp, int eventCount) {}

// FriggCommand.java
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FriggCommand(String language, String command, Map<String, Object> params) {}

// FriggResponse.java
public record FriggResponse(List<Map<String, Object>> result) {}
```

---

## Шаг 10: FriggClient.java

```java
package studio.seer.heimdall.snapshot;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client для FRIGG — ArcadeDB :2481 (не HoundArcade :2480).
 */
@RegisterRestClient(configKey = "frigg-api")
@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface FriggClient {

    @POST
    @Path("/command/{db}")
    Uni<FriggResponse> command(
            @PathParam("db")                  String db,
            @HeaderParam("Authorization")     String authorization,
            FriggCommand                      body
    );
}
```

---

## Шаг 11: FriggGateway.java

По паттерну `services/shuttle/src/main/java/studio/seer/lineage/client/ArcadeGateway.java`:

```java
package studio.seer.heimdall.snapshot;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class FriggGateway {

    private static final Logger LOG = Logger.getLogger(FriggGateway.class);

    @Inject @RestClient FriggClient client;

    @ConfigProperty(name = "frigg.db")       String db;
    @ConfigProperty(name = "frigg.user")     String user;
    @ConfigProperty(name = "frigg.password") String password;

    public Uni<List<Map<String, Object>>> sql(String query, Map<String, Object> params) {
        LOG.debugf("[FRIGG] %s", query);
        return client.command(db, basicAuth(), new FriggCommand("sql", query, params))
                .map(FriggResponse::result)
                .onFailure().invoke(ex -> LOG.errorf("[FRIGG FAILED] %s: %s", query, ex.getMessage()));
    }

    private String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (user + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
```

---

## Шаг 12: SnapshotManager.java

**Решение по ArcadeDB DDL**: `CREATE CLASS HeimdallSnapshot IF NOT EXISTS EXTENDS V` — синтаксис ArcadeDB v24. Если FRIGG недоступен — recoverWithItem, DoD-шаг 4 упадёт с 500 (это Риск #2, см. ниже).

Events сериализуются в JSON-строку и хранятся как одно поле `eventsJson` (не отдельные вертексы). Список snapshots — SELECT без eventsJson.

```java
package studio.seer.heimdall.snapshot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import studio.seer.shared.HeimdallEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class SnapshotManager {

    private static final Logger LOG = Logger.getLogger(SnapshotManager.class);

    @Inject FriggGateway frigg;
    @Inject ObjectMapper mapper;

    private volatile boolean schemaEnsured = false;

    public Uni<String> save(String name, List<HeimdallEvent> events) {
        String id       = UUID.randomUUID().toString();
        long   ts       = System.currentTimeMillis();
        String safeName = (name == null || name.isBlank()) ? "unnamed" : name;

        String eventsJson;
        try {
            eventsJson = mapper.writeValueAsString(events);
        } catch (JsonProcessingException e) {
            return Uni.createFrom().failure(e);
        }

        String sql = "INSERT INTO HeimdallSnapshot SET " +
                "snapshotId = :id, name = :name, ts = :ts, eventCount = :count, eventsJson = :json";

        return ensureSchema()
                .chain(() -> frigg.sql(sql, Map.of(
                        "id", id, "name", safeName, "ts", ts,
                        "count", events.size(), "json", eventsJson)))
                .map(__ -> { LOG.infof("Snapshot '%s' saved id=%s events=%d", safeName, id, events.size()); return id; })
                .onFailure().invoke(ex -> LOG.errorf("Save snapshot failed: %s", ex.getMessage()));
    }

    public Uni<List<SnapshotInfo>> list() {
        return ensureSchema()
                .chain(() -> frigg.sql(
                        "SELECT snapshotId, name, ts, eventCount FROM HeimdallSnapshot ORDER BY ts DESC LIMIT 100",
                        null))
                .map(rows -> rows.stream().map(r -> new SnapshotInfo(
                        (String) r.get("snapshotId"),
                        (String) r.get("name"),
                        toLong(r.get("ts")),
                        toInt(r.get("eventCount"))
                )).toList());
    }

    private Uni<Void> ensureSchema() {
        if (schemaEnsured) return Uni.createFrom().voidItem();
        return frigg.sql("CREATE CLASS HeimdallSnapshot IF NOT EXISTS EXTENDS V", null)
                .invoke(__ -> schemaEnsured = true)
                .replaceWithVoid();
    }

    private static long toLong(Object v) { return v instanceof Number n ? n.longValue() : 0L; }
    private static int  toInt(Object v)  { return v instanceof Number n ? n.intValue()  : 0;  }
}
```

---

## Шаг 13: ControlResource.java

**Auth**: X-Seer-Role из forwarded заголовка (Chur проксирует). Только "admin" допускается.  
**DEMO_RESET**: `ringBuffer.clear()` сам пушит событие в WebSocket. Вызываем `metricsCollector.reset()` следом явно.

```java
package studio.seer.heimdall.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import studio.seer.heimdall.RingBuffer;
import studio.seer.heimdall.metrics.MetricsCollector;
import studio.seer.heimdall.snapshot.SnapshotManager;

import java.util.Map;

@Path("/control")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ControlResource {

    private static final Logger LOG = Logger.getLogger(ControlResource.class);

    @Inject RingBuffer       ringBuffer;
    @Inject MetricsCollector metricsCollector;
    @Inject SnapshotManager  snapshots;

    @POST @Path("/reset")
    public Response reset(@HeaderParam("X-Seer-Role") String role) {
        if (!isAdmin(role)) return forbidden();
        ringBuffer.clear();           // уже пушит DEMO_RESET в WebSocket
        metricsCollector.reset();     // сбрасываем счётчики явно
        LOG.infof("Demo reset by role=%s", role);
        return Response.ok(Map.of("status", "reset")).build();
    }

    @POST @Path("/snapshot")
    public Uni<Response> saveSnapshot(
            @HeaderParam("X-Seer-Role") String role,
            @QueryParam("name")         String name) {
        if (!isAdmin(role)) return Uni.createFrom().item(forbidden());
        var events = ringBuffer.snapshot();
        return snapshots.save(name, events)
                .map(id -> Response.ok(Map.of(
                        "snapshotId", id,
                        "eventCount", events.size(),
                        "name",       name != null ? name : "unnamed")).build())
                .onFailure().recoverWithItem(ex ->
                        Response.serverError().entity(Map.of("error", ex.getMessage())).build());
    }

    @POST @Path("/cancel/{sessionId}")
    public Response cancelSession(
            @HeaderParam("X-Seer-Role") String role,
            @PathParam("sessionId")     String sessionId) {
        if (!isAdmin(role)) return forbidden();
        // Заглушка — реальная отмена через Dali API (Sprint 3)
        return Response.ok(Map.of("sessionId", sessionId, "status", "cancel_requested",
                "note", "stub, Dali integration pending")).build();
    }

    @GET @Path("/snapshots")
    public Uni<Response> listSnapshots(@HeaderParam("X-Seer-Role") String role) {
        if (!isAdmin(role)) return Uni.createFrom().item(forbidden());
        return snapshots.list()
                .map(list -> Response.ok(list).build())
                .onFailure().recoverWithItem(ex ->
                        Response.serverError().entity(Map.of("error", ex.getMessage())).build());
    }

    private boolean isAdmin(String role) { return "admin".equalsIgnoreCase(role); }
    private Response forbidden() {
        return Response.status(403).entity(Map.of("error", "admin role required")).build();
    }
}
```

---

## Шаг 14: build.gradle heimdall-backend (изменение)

```diff
+    // REST Client — для FRIGG (ArcadeDB :2481)
+    implementation 'io.quarkus:quarkus-rest-client'
+    implementation 'io.quarkus:quarkus-rest-client-jackson'
```

---

## Шаг 15: application.properties heimdall-backend (изменение)

```diff
+# ── FRIGG — ArcadeDB :2481 (отдельный от HoundArcade :2480) ─────────────────
+quarkus.rest-client.frigg-api.url=http://localhost:2481
+quarkus.rest-client.frigg-api.scope=Singleton
+frigg.db=heimdall
+frigg.user=root
+frigg.password=playwithdata
+%prod.quarkus.rest-client.frigg-api.url=http://frigg:2481
```

---

## Шаги 16–22: SHUTTLE HeimdallEmitter

**Временное решение**: копируем модели в SHUTTLE (Docker multi-module build SHUTTLE → отдельный рефакторинг-спринт).

**Шаги 16–18**: Создать package `studio.seer.lineage.heimdall.model` в SHUTTLE:
- `HeimdallEvent.java` — точная копия из `shared/dali-models`
- `EventType.java` — точная копия
- `EventLevel.java` — точная копия

**Шаг 19: HeimdallClient.java**

`services/shuttle/src/main/java/studio/seer/lineage/heimdall/HeimdallClient.java`

```java
package studio.seer.lineage.heimdall;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import studio.seer.lineage.heimdall.model.HeimdallEvent;

@RegisterRestClient(configKey = "heimdall-api")
@Path("/events")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface HeimdallClient {

    @POST
    Uni<Response> send(HeimdallEvent event);
}
```

**Шаг 20: HeimdallEmitter.java**

`services/shuttle/src/main/java/studio/seer/lineage/heimdall/HeimdallEmitter.java`

```java
package studio.seer.lineage.heimdall;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import studio.seer.lineage.heimdall.model.EventLevel;
import studio.seer.lineage.heimdall.model.EventType;
import studio.seer.lineage.heimdall.model.HeimdallEvent;

import java.util.Map;

/**
 * Fire-and-forget отправка событий в HEIMDALL.
 * Ошибки логируются WARN — никогда не пробрасываются в вызывающий код.
 * Падение HEIMDALL не роняет SHUTTLE.
 */
@ApplicationScoped
public class HeimdallEmitter {

    private static final Logger LOG = Logger.getLogger(HeimdallEmitter.class);

    @Inject @RestClient HeimdallClient client;

    public void emit(EventType type, EventLevel level,
                     String sessionId, String correlationId,
                     long durationMs, Map<String, Object> payload) {
        var event = new HeimdallEvent(
                System.currentTimeMillis(), "shuttle", type.name(), level,
                sessionId, null, correlationId, durationMs, payload
        );
        client.send(event)
                .subscribe().with(
                        __ -> LOG.debugf("Emitted %s to HEIMDALL", type),
                        ex -> LOG.warnf("HEIMDALL emit failed (%s): %s", type, ex.getMessage())
                );
    }
}
```

**Шаг 21: LineageResource.java (изменение)**

```diff
+import studio.seer.lineage.heimdall.HeimdallEmitter;
+import studio.seer.lineage.heimdall.model.EventLevel;
+import studio.seer.lineage.heimdall.model.EventType;

 public class LineageResource {
+    @Inject HeimdallEmitter heimdall;

     @Query("lineage")
     public Uni<LineageResult> getLineage(...) {
+        long start = System.currentTimeMillis();
+        heimdall.emit(EventType.REQUEST_RECEIVED, EventLevel.INFO, null, null, 0, Map.of("query", "lineage"));
         return service.getLineage(...)
+            .invoke(result -> heimdall.emit(EventType.REQUEST_COMPLETED, EventLevel.INFO,
+                    null, null, System.currentTimeMillis() - start, Map.of("query", "lineage")));
     }
 }
```

**Шаг 22: application.properties SHUTTLE (изменение)**

```diff
+quarkus.rest-client.heimdall-api.url=http://localhost:9093
+quarkus.rest-client.heimdall-api.connect-timeout=500
+quarkus.rest-client.heimdall-api.read-timeout=1000
+%prod.quarkus.rest-client.heimdall-api.url=http://heimdall-backend:9093
```

---

## Шаги 23–24: Chur proxy routes

**Шаг 23: bff/chur/src/routes/heimdall.ts**

**Зависимость**: `@fastify/http-proxy` уже должен быть в package.json (проверить). WebSocket-прокси требует `@fastify/websocket`.

```typescript
import { FastifyInstance } from 'fastify';

const HEIMDALL_ORIGIN = process.env.HEIMDALL_URL ?? 'http://localhost:9093';

export async function heimdallRoutes(fastify: FastifyInstance) {

  // GET /heimdall/health — без аутентификации
  fastify.get('/heimdall/health', async (req, reply) => {
    const res = await fetch(`${HEIMDALL_ORIGIN}/q/health`);
    const body = await res.json();
    reply.status(res.status).send(body);
  });

  // GET /heimdall/metrics/snapshot — scope: admin
  fastify.get('/heimdall/metrics/snapshot', {
    preHandler: [fastify.authenticate, fastify.requireRole('admin')],
  }, async (req, reply) => {
    const res = await fetch(`${HEIMDALL_ORIGIN}/metrics/snapshot`, {
      headers: { 'X-Seer-Role': (req as any).user?.role ?? '' },
    });
    reply.status(res.status).send(await res.json());
  });

  // POST /heimdall/control/:action — scope: admin (destructive)
  fastify.post('/heimdall/control/:action', {
    preHandler: [fastify.authenticate, fastify.requireRole('admin')],
  }, async (req, reply) => {
    const { action } = req.params as { action: string };
    const url = new URL(`/control/${action}`, HEIMDALL_ORIGIN);
    if (req.query) {
      Object.entries(req.query as Record<string, string>).forEach(([k, v]) =>
        url.searchParams.set(k, v)
      );
    }
    const res = await fetch(url.toString(), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Seer-Role': (req as any).user?.role ?? '',
      },
      body: req.body ? JSON.stringify(req.body) : undefined,
    });
    reply.status(res.status).send(await res.json());
  });

  // GET /heimdall/control/snapshots
  fastify.get('/heimdall/control/snapshots', {
    preHandler: [fastify.authenticate, fastify.requireRole('admin')],
  }, async (req, reply) => {
    const res = await fetch(`${HEIMDALL_ORIGIN}/control/snapshots`, {
      headers: { 'X-Seer-Role': (req as any).user?.role ?? '' },
    });
    reply.status(res.status).send(await res.json());
  });
}
```

> WebSocket-прокси `/heimdall/ws/events` — отдельный шаг после установки `@fastify/websocket` (Риск #7).

**Шаг 24: server.ts (изменение)**

```diff
+import { heimdallRoutes } from './routes/heimdall.js';

+fastify.register(heimdallRoutes);
```

---

## Риски и решения

| # | Уровень | Риск | Решение |
|---|---------|------|---------|
| **R1** | 🔴 ВЫСОКИЙ | `handshakeRequest().queryParam()` — возвращает `String` или `Optional<String>` в Quarkus 3.34.x | Проверить при первой компиляции, поправить сигнатуру |
| **R2** | 🟡 СРЕДНИЙ | FRIGG (:2481) может не существовать в dev | SnapshotManager уже имеет `onFailure().recoverWithItem` — graceful degradation. Альтернатива: запустить второй ArcadeDB контейнер в docker-compose |
| **R3** | 🟡 СРЕДНИЙ | `CREATE CLASS ... EXTENDS V` vs `CREATE VERTEX TYPE ... IF NOT EXISTS` (ArcadeDB v24+) | Проверить версию FRIGG; при ошибке DDL поправить SQL |
| **R4** | 🟢 НИЗКИЙ | `activeWorkers` в Sprint 2 — монотонный счётчик WORKER_ASSIGNED, не реальный gauge | Задокументировано; реальный gauge (учёт SESSION_COMPLETED) — Sprint 3 |
| **R5** | 🟡 СРЕДНИЙ | EventFilter поддерживает только один key:value | Задокументировать; Chur может валидировать формат на входе |
| **R6** | 🟡 СРЕДНИЙ | `@fastify/websocket` не установлен в Chur | WebSocket-прокси оставить на конец Sprint 2 или начало Sprint 3; HTTP-маршруты работают без него |
| **R7** | 🟢 НИЗКИЙ | `DEMO_RESET` попадает в `metricsCollector.record()` → вызывает `reset()` внутри, но ControlResource вызывает `reset()` явно → двойной сброс | Допустимо (reset идемпотентен); при необходимости убрать case "DEMO_RESET" из MetricsCollector.record() |

---

## Definition of Done — проверочные сценарии

```bash
# 1. Метрики
curl http://localhost:9093/metrics/snapshot
# → {"atomsExtracted":0,"filesParsed":0,"toolCallsTotal":0,"activeWorkers":0,"queueDepth":0,"resolutionRate":"NaN"}

# 2. Фильтрованный WebSocket (только SHUTTLE-события)
npx wscat -c "ws://localhost:9093/ws/events?filter=component:shuttle"

# 3. Reset (через Chur → HEIMDALL)
curl -X POST http://localhost:3000/heimdall/control/reset
# → {"status":"reset"}, WebSocket-клиенты получают DEMO_RESET-событие

# 4. Snapshot
curl -X POST "http://localhost:9093/control/snapshot?name=baseline" -H "X-Seer-Role: admin"
# → {"snapshotId":"<uuid>","eventCount":N,"name":"baseline"}

# 5. SHUTTLE эмитит события → в HEIMDALL ≤100мс
# Запустить SHUTTLE + HEIMDALL, выполнить GraphQL-запрос, wscat показывает REQUEST_RECEIVED/REQUEST_COMPLETED

# 6. Fire-and-forget
# Остановить HEIMDALL, сделать GraphQL-запрос в SHUTTLE
# SHUTTLE отвечает нормально, в логах: WARN "HEIMDALL emit failed"

# 7. Chur health proxy
curl http://localhost:3000/heimdall/health
# → proxied /q/health от HEIMDALL
```

---

*Sprint 2 план v1.0 — 2026-04-12*
