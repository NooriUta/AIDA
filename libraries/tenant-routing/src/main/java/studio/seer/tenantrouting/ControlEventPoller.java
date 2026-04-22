package studio.seer.tenantrouting;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;

/**
 * MTN-51 — Pull-based consumer for {@code heimdall.ControlEvent}.
 *
 * <p>Each consumer service (SHUTTLE, ANVIL, DALI, MIMIR) wires a
 * {@code @Scheduled(every="60s")} bean that delegates to an instance of this
 * class. Every tick it reads events with
 * {@code createdAt > lastAppliedCreatedAt ORDER BY createdAt ASC LIMIT 100},
 * runs each through the caller-supplied handler, then advances
 * {@code lastAppliedCreatedAt}.
 *
 * <p>The {@link FenceGate} guards against out-of-order delivery inside a
 * single batch: the store is append-only with monotonic {@code createdAt} from
 * a single emitter (HEIMDALL), but restarts and clock skew mean we treat
 * ordering as best-effort and let the gate drop stale tokens.
 *
 * <p>State is process-local: on restart lastApplied resets to 0 and we replay
 * the whole store. Idempotent handlers make that safe (e.g. registry
 * invalidation is idempotent). Persistent offset per-service is a Round 5
 * improvement once the event volume justifies it.
 */
public final class ControlEventPoller {

    private static final Logger log = LoggerFactory.getLogger(ControlEventPoller.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient http;
    private final String baseUrl;
    private final String authHeader;
    private final FenceGate fenceGate;
    private final BiConsumer<String, Map<String, Object>> handler;

    /** Highest {@code createdAt} successfully processed. */
    private final AtomicLong lastApplied = new AtomicLong(0L);

    /**
     * @param http       reusable HttpClient
     * @param friggUrl   FRIGG base URL (http://host:port)
     * @param username   FRIGG basic-auth user
     * @param password   FRIGG basic-auth password
     * @param fenceGate  per-tenant lastSeen token gate (see {@link FenceGate})
     * @param handler    user callback — receives (eventType, payloadMap).
     *                   Must be idempotent. Caller decides whether to invalidate
     *                   per-tenant caches, disconnect sockets, etc.
     */
    public ControlEventPoller(
            HttpClient http,
            String friggUrl,
            String username,
            String password,
            FenceGate fenceGate,
            BiConsumer<String, Map<String, Object>> handler
    ) {
        this.http       = http;
        this.baseUrl    = friggUrl.endsWith("/") ? friggUrl.substring(0, friggUrl.length() - 1) : friggUrl;
        this.authHeader = "Basic " + Base64.getEncoder()
                .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        this.fenceGate  = fenceGate;
        this.handler    = handler;
    }

    /**
     * Called by the consumer's {@code @Scheduled} tick. Returns the number of
     * events applied (post-gate). Exceptions are logged and swallowed so one
     * bad event doesn't break the polling loop.
     */
    public int tick() {
        long since = lastApplied.get();
        try {
            var body = MAPPER.writeValueAsString(Map.of(
                    "language", "sql",
                    "command",  "SELECT id, tenantAlias, eventType, fenceToken, createdAt, payload " +
                                "FROM ControlEvent WHERE createdAt > :since " +
                                "ORDER BY createdAt ASC LIMIT 100",
                    "params",   Map.of("since", since)));
            var req = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/v1/query/heimdall"))
                    .header("Content-Type",  "application/json")
                    .header("Authorization", authHeader)
                    .timeout(Duration.ofSeconds(10))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            var res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() < 200 || res.statusCode() >= 300) {
                log.warn("ControlEventPoller: HTTP {}: {}", res.statusCode(), res.body());
                return 0;
            }
            var parsed = MAPPER.readValue(res.body(), Map.class);
            var result = parsed.get("result");
            if (!(result instanceof List<?> rows) || rows.isEmpty()) return 0;

            int applied = 0;
            long maxCreatedAt = since;
            for (Object raw : rows) {
                if (!(raw instanceof Map<?, ?> rowRaw)) continue;
                @SuppressWarnings("unchecked")
                Map<String, Object> row = (Map<String, Object>) rowRaw;
                String eventType    = String.valueOf(row.getOrDefault("eventType", ""));
                String tenantAlias  = String.valueOf(row.getOrDefault("tenantAlias", ""));
                long   fenceToken   = ((Number) row.getOrDefault("fenceToken", 0L)).longValue();
                long   createdAt    = ((Number) row.getOrDefault("createdAt", 0L)).longValue();
                Object payloadRaw   = row.get("payload");
                Map<String, Object> payload;
                if (payloadRaw instanceof String s) {
                    payload = MAPPER.readValue(s, Map.class);
                } else if (payloadRaw instanceof Map<?, ?> m) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> p2 = (Map<String, Object>) m;
                    payload = p2;
                } else {
                    payload = Map.of();
                }

                FenceGate.Decision decision = fenceGate.admit(tenantAlias, fenceToken);
                if (decision == FenceGate.Decision.STALE) {
                    log.warn("ControlEventPoller: stale fence rejected tenant={} fence={}", tenantAlias, fenceToken);
                    continue;
                }
                try {
                    handler.accept(eventType, Map.of(
                            "tenantAlias", tenantAlias,
                            "fenceToken",  fenceToken,
                            "payload",     payload));
                    applied++;
                } catch (Exception e) {
                    log.error("ControlEventPoller handler failure for event {}/{}",
                            eventType, tenantAlias, e);
                }
                if (createdAt > maxCreatedAt) maxCreatedAt = createdAt;
            }
            lastApplied.set(maxCreatedAt);
            if (applied > 0) {
                log.info("ControlEventPoller: applied={} since={}-> {}", applied, since, maxCreatedAt);
            }
            return applied;
        } catch (Exception e) {
            log.warn("ControlEventPoller tick failed: {}", e.getMessage());
            return 0;
        }
    }

    /** @return last successfully applied {@code createdAt}. */
    public long lastAppliedCreatedAt() {
        return lastApplied.get();
    }

    /** @internal test hook — reset applied cursor. */
    public void resetForTests() {
        lastApplied.set(0L);
    }
}
