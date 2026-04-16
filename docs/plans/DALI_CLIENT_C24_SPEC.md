# Dali Client — C.2.4 Implementation Spec

**Документ:** `DALI_CLIENT_C24_SPEC`
**Версия:** 1.1
**Дата:** 16.04.2026
**Статус:** ✅ Реализован (sprint Apr 16, 2026, PR #14)
**Трек:** Track B (SHUTTLE)
**Фактически затрачено:** ~3 ч
**PR:** https://github.com/NooriUta/AIDA/pull/14
**Разблокировал:** C.2.2 mutations (startParseSession, cancelSession) + UC2b end-to-end

---

## Контекст

SHUTTLE содержит `MutationResource` с `startParseSession` и `cancelSession` мутациями — они сейчас возвращают заглушки. Нужен HTTP REST клиент к Dali (`:9090`) реализованный через MicroProfile REST Client (Quarkus).

Контракт API описан в `docs/architecture/Q7_HEIMDALL_DALI_API.md`.

---

## Структура файлов

```
services/shuttle/src/main/java/com/aida/shuttle/
└── client/
    └── dali/
        ├── DaliClient.java              ← @RegisterRestClient interface
        ├── DaliClientConfig.java        ← @ConfigProperties
        ├── DaliClientFallback.java      ← @Fallback implementation
        └── model/
            ├── ParseSessionInput.java   ← shared с dali-models (если есть)
            ├── SessionInfo.java         ← response model
            ├── SessionStatus.java       ← enum
            └── DaliStats.java           ← YGG stats response
```

> Если `dali-models` shared module доступен — использовать типы оттуда, не дублировать.

---

## DaliClient.java

```java
package com.aida.shuttle.client.dali;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.eclipse.microprofile.faulttolerance.*;

import java.util.List;
import java.util.UUID;

@RegisterRestClient(configKey = "dali-client")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DaliClient {

    /**
     * Создать и поставить в очередь сессию парсинга.
     * → 202 Accepted + SessionInfo{id, status=QUEUED}
     */
    @POST
    @Path("/sessions")
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 2, delay = 500, delayUnit = ChronoUnit.MILLIS,
           retryOn = {jakarta.ws.rs.ProcessingException.class})
    @Fallback(DaliClientFallback.class)
    SessionInfo createSession(ParseSessionInput input);

    /**
     * Получить статус сессии.
     * → 200 + Session | 404
     */
    @GET
    @Path("/sessions/{id}")
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 3, delay = 200, delayUnit = ChronoUnit.MILLIS,
           retryOn = {jakarta.ws.rs.ProcessingException.class})
    SessionInfo getSession(@PathParam("id") UUID id);

    /**
     * Список последних сессий.
     */
    @GET
    @Path("/sessions")
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    List<SessionInfo> listSessions(@QueryParam("limit") @DefaultValue("50") int limit,
                                   @QueryParam("status") String status);

    /**
     * Запросить отмену сессии.
     * → 202 + { status: "CANCELLING" }
     * После реализации cancel в Dali: убрать @Fallback или оставить для network error.
     */
    @POST
    @Path("/sessions/{id}/cancel")
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 1)
    @Fallback(DaliClientFallback.class)
    CancelResponse cancelSession(@PathParam("id") UUID id);

    /**
     * YGG graph stats.
     */
    @GET
    @Path("/stats")
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    DaliStats getStats();

    /**
     * Health check.
     */
    @GET
    @Path("/sessions/health")
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    DaliHealth getHealth();
}
```

---

## DaliClientFallback.java

```java
package com.aida.shuttle.client.dali;

import io.quarkus.logging.Log;
import org.eclipse.microprofile.faulttolerance.ExecutionContext;
import org.eclipse.microprofile.faulttolerance.FallbackHandler;

import java.util.List;

public class DaliClientFallback implements FallbackHandler<Object> {

    @Override
    public Object handle(ExecutionContext context) {
        Log.warnf("[DaliClient] Fallback activated for %s: %s",
            context.getMethod().getName(),
            context.getFailure().getMessage());

        return switch (context.getMethod().getName()) {
            case "createSession" -> SessionInfo.unavailable();
            case "getSession"    -> SessionInfo.unavailable();
            case "listSessions"  -> List.of();
            case "cancelSession" -> new CancelResponse("UNAVAILABLE",
                                        "Dali service unreachable");
            case "getStats"      -> DaliStats.empty();
            case "getHealth"     -> DaliHealth.degraded("Dali unreachable");
            default              -> null;
        };
    }
}
```

---

## Модели

### ParseSessionInput.java

```java
public record ParseSessionInput(
    String dialect,               // "PLSQL" | "GENERIC_SQL" | "POSTGRESQL"
    String source,                // путь к файлу/директории
    boolean preview,              // false → пишет в YGG
    Boolean clearBeforeWrite,     // BOXED: null → default true (решение #INV-DALI-03)
    String filePattern,           // "*.sql"
    Integer maxFiles,             // null = без лимита
    List<String> tags             // опционально
) {
    // Convenient factory
    public static ParseSessionInput forSource(String dialect, String source) {
        return new ParseSessionInput(dialect, source, false, null, "*.sql", null, List.of());
    }
}
```

### SessionInfo.java

```java
public record SessionInfo(
    UUID     id,
    String   status,           // QUEUED | PROCESSING | COMPLETED | FAILED | CANCELLED
    String   dialect,
    String   source,
    boolean  preview,
    Long     createdAt,
    Long     startedAt,
    Long     completedAt,
    Long     durationMs,
    Integer  atomCount,
    Integer  atomsResolved,
    Integer  atomsUnresolved,
    Integer  fileCount,
    Integer  filesProcessed,
    Integer  filesFailed,
    Double   resolutionRate,
    Boolean  friggPersisted,
    String   errorMessage      // null при success
) {
    public static SessionInfo unavailable() {
        return new SessionInfo(null, "UNAVAILABLE", null, null,
            false, null, null, null, null,
            null, null, null, null, null, null, null, null,
            "Dali service unavailable");
    }

    public boolean isDone() {
        return "COMPLETED".equals(status)
            || "FAILED".equals(status)
            || "CANCELLED".equals(status);
    }
}
```

### CancelResponse.java

```java
public record CancelResponse(String status, String message) {}
```

### DaliStats.java

```java
public record DaliStats(
    long daliTables,
    long daliColumns,
    long daliRoutines,
    long daliStatements,
    long daliAtoms,
    long lastUpdated
) {
    public static DaliStats empty() {
        return new DaliStats(0, 0, 0, 0, 0, 0);
    }
}
```

### DaliHealth.java

```java
public record DaliHealth(String frigg, String ygg, int sessions, int workers, int queued) {
    public static DaliHealth degraded(String reason) {
        return new DaliHealth("error", "unknown", 0, 0, 0);
    }
}
```

---

## Конфигурация

### application.properties (SHUTTLE)

```properties
# Dali REST client
quarkus.rest-client.dali-client.url=${DALI_URL:http://localhost:9090}
quarkus.rest-client.dali-client.connect-timeout=3000
quarkus.rest-client.dali-client.read-timeout=10000

# Fault tolerance
mp.fault-tolerance.enabled=true
```

### docker-compose.yml (env)

```yaml
shuttle:
  environment:
    DALI_URL: http://dali:9090
    HEIMDALL_URL: http://heimdall-backend:9093
```

---

## Интеграция в MutationResource

```java
// services/shuttle/src/main/java/**/MutationResource.java

@Inject
@RestClient
DaliClient daliClient;

// Мутация startParseSession (GraphQL)
public SessionInfo startParseSession(ParseSessionInput input) {
    return daliClient.createSession(input);
    // → SHUTTLE эмитит REQUEST_RECEIVED в HEIMDALL через HeimdallEmitter
    // → Dali принимает задачу, эмитит JOB_ENQUEUED → SESSION_STARTED
}

// Мутация cancelSession
public Boolean cancelSession(String sessionId) {
    CancelResponse resp = daliClient.cancelSession(UUID.fromString(sessionId));
    return !"UNAVAILABLE".equals(resp.status());
}
```

---

## Порядок реализации

```
1. Создать dali/model/ — 5 record-классов (~30 мин)
2. Создать DaliClient interface (~20 мин)
3. Создать DaliClientFallback (~20 мин)
4. Добавить конфигурацию в application.properties (~10 мин)
5. Интегрировать в MutationResource (~30 мин)
6. QuarkusTest: DaliClientMockTest через @InjectMock (~60 мин)
7. E2E: запустить Dali + SHUTTLE, отправить мутацию, проверить session в YGG
```

**Total estimate:** 3–4 ч до working + test.

---

## Блокирует

- **C.2.2** — `startParseSession` mutation не может возвращать реальный ID без DaliClient
- **cancel flow** — `POST /control/cancel` в HEIMDALL ControlResource → нужен DaliControlClient (отдельный client или метод в DaliClient)
- **UC2b trigger** — event-driven path SHUTTLE→Dali, описан в `UC2B_TRIGGER_SPEC.md`

---

## История изменений

| Дата | Версия | Что |
|---|---|---|
| 14.04.2026 | 1.0 | Initial spec. API contract из Q7_HEIMDALL_DALI_API.md v1.0. |
