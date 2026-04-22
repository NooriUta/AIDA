package studio.seer.heimdall.reconcile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import studio.seer.heimdall.RingBuffer;
import studio.seer.heimdall.audit.AuditHashChain;
import studio.seer.shared.HeimdallEvent;
import studio.seer.shared.EventLevel;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MTN-60 — Poll Keycloak admin-events REST endpoint and bridge to Heimdall
 * audit stream.
 *
 * <p>KC exposes {@code /admin/realms/{realm}/admin-events} — a chronological
 * log of admin actions (USER_CREATE / UPDATE / DELETE, ROLE_GRANT / REVOKE,
 * etc.). Polling every 60 seconds is simpler than deploying a KC SPI plugin
 * and suffices for MVP audit visibility.
 *
 * <p>Each new admin event is converted to a {@link HeimdallEvent} of type
 * {@code KC_ADMIN_EVENT} and pushed onto the ring buffer — the existing
 * audit pipeline (ring → SIEM export MTN-21) picks it up from there. Hash
 * chain coverage (MTN-37) is maintained by always flowing through the ring.
 *
 * <p>Cursor is kept in memory as {@code lastSeenAt} (unix ms from KC's
 * {@code time} field). Restart loses cursor, re-scans the last 24h window
 * to minimize duplicates without flooding on long downtime.
 */
@ApplicationScoped
public class KcAdminEventListener {

    private static final Logger LOG = Logger.getLogger(KcAdminEventListener.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Duration RESTART_BACKFILL = Duration.ofHours(24);
    private static final int BATCH_LIMIT = 100;

    @Inject RingBuffer ringBuffer;

    @ConfigProperty(name = "heimdall.kc.url",      defaultValue = "http://localhost:18180/kc")
    String kcUrl;
    @ConfigProperty(name = "heimdall.kc.realm",    defaultValue = "seer")
    String kcRealm;
    @ConfigProperty(name = "heimdall.kc.admin-user", defaultValue = "admin")
    String kcAdminUser;
    @ConfigProperty(name = "heimdall.kc.admin-pass", defaultValue = "admin")
    String kcAdminPass;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final AtomicLong lastSeenAt = new AtomicLong(0L);

    @Scheduled(every = "60s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void tick() {
        try {
            if (lastSeenAt.get() == 0L) {
                lastSeenAt.set(System.currentTimeMillis() - RESTART_BACKFILL.toMillis());
            }
            String token = fetchAdminToken();
            JsonNode events = fetchAdminEvents(token, lastSeenAt.get(), BATCH_LIMIT);
            if (events == null || !events.isArray() || events.size() == 0) return;

            long maxSeen = lastSeenAt.get();
            int accepted = 0;
            // KC returns events DESC by default; iterate newest → oldest
            for (JsonNode ev : events) {
                long ts = ev.path("time").asLong(0);
                if (ts <= lastSeenAt.get()) continue;  // stale, already processed
                accepted++;
                if (ts > maxSeen) maxSeen = ts;
                pushToRing(ev);
            }
            lastSeenAt.set(maxSeen);
            if (accepted > 0) {
                LOG.infof("[MTN-60] KC admin events polled: accepted=%d lastSeen=%d", accepted, maxSeen);
            }
        } catch (Exception e) {
            LOG.warnf("[MTN-60] KC admin-events poll failed: %s", e.getMessage());
        }
    }

    private void pushToRing(JsonNode ev) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("kcOperationType", ev.path("operationType").asText(""));
        payload.put("kcResourceType",  ev.path("resourceType").asText(""));
        payload.put("kcResourcePath",  ev.path("resourcePath").asText(""));
        payload.put("kcRealmId",       ev.path("realmId").asText(""));
        payload.put("kcAuthUserId",    ev.path("authDetails").path("userId").asText(""));
        payload.put("kcAuthRealmId",   ev.path("authDetails").path("realmId").asText(""));
        payload.put("kcTime",          ev.path("time").asLong(0));
        // Representation may contain sensitive data — include only a hash for audit trail
        String repr = ev.path("representation").asText("");
        if (!repr.isEmpty()) {
            payload.put("kcReprHash", AuditHashChain.computeHash(repr, AuditHashChain.GENESIS_HASH));
        }
        HeimdallEvent hev = new HeimdallEvent(
                ev.path("time").asLong(System.currentTimeMillis()),
                "heimdall",
                "KC_ADMIN_EVENT",
                EventLevel.INFO,
                null, null, null,
                0L,
                payload
        );
        ringBuffer.push(hev);
    }

    private String fetchAdminToken() throws Exception {
        String form = "grant_type=password&client_id=admin-cli"
                + "&username=" + urlEncode(kcAdminUser)
                + "&password=" + urlEncode(kcAdminPass);
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(kcUrl + "/realms/master/protocol/openid-connect/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .timeout(Duration.ofSeconds(10))
                .POST(HttpRequest.BodyPublishers.ofString(form))
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) throw new RuntimeException("KC token HTTP " + res.statusCode());
        return MAPPER.readTree(res.body()).path("access_token").asText();
    }

    private JsonNode fetchAdminEvents(String token, long sinceMs, int max) throws Exception {
        // KC supports dateFrom (yyyy-MM-dd) and time-based filtering via query params
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(kcUrl + "/admin/realms/" + kcRealm + "/admin-events?max=" + max))
                .header("Authorization", "Bearer " + token)
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() != 200) {
            LOG.debugf("KC admin-events HTTP %d: %s", res.statusCode(), res.body());
            return null;
        }
        return MAPPER.readTree(res.body());
    }

    private static String urlEncode(String s) {
        return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    /** @internal test hook. */
    void resetForTests() {
        lastSeenAt.set(0L);
    }
}
