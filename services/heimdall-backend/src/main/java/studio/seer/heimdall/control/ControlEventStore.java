package studio.seer.heimdall.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import studio.seer.heimdall.snapshot.FriggGateway;
import studio.seer.shared.ControlEvent;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * MTN-51 — Persistence for control-plane events.
 *
 * <p>All {@code seer.control.*} events are written append-only to the
 * {@code ControlEvent} vertex in the FRIGG {@code heimdall} database. Schema is
 * initialised by {@code scripts/init-arcadedb.sh → ensure_heimdall_control_event_schema}:
 * UNIQUE index on {@code id} enforces idempotency, NOTUNIQUE on {@code tenantAlias}
 * / {@code createdAt} / {@code fenceToken} support pull-based consumer reconcile.
 *
 * <p>Producer-side contract:
 * <ul>
 *   <li>Caller supplies a {@link ControlEvent} with a pre-allocated UUIDv4 id
 *       (see {@link ControlEvent#newEvent}). Duplicate ids are silently
 *       ignored (append-only semantics: the first write wins).</li>
 *   <li>Append-only is additionally enforced at the DB level by a JS trigger
 *       {@code control_event_readonly} that rejects UPDATE / DELETE
 *       (install via init-arcadedb.sh → MTN-51 hook).</li>
 * </ul>
 */
@ApplicationScoped
public class ControlEventStore {

    private static final Logger LOG = Logger.getLogger(ControlEventStore.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Inject FriggGateway frigg;

    /**
     * Persist an event. Returns {@code true} on a successful fresh insert,
     * {@code false} on idempotent duplicate (UNIQUE(id) constraint violation).
     */
    public boolean persist(ControlEvent event) {
        String payloadJson;
        try {
            payloadJson = MAPPER.writeValueAsString(event.payload());
        } catch (Exception e) {
            LOG.errorf(e, "[MTN-51] ControlEvent payload serialize failed: id=%s", event.id());
            throw new RuntimeException("payload serialize failed", e);
        }

        try {
            Uni<List<Map<String, Object>>> q = frigg.sql(
                    "INSERT INTO ControlEvent SET id = :id, tenantAlias = :tenantAlias, " +
                    "eventType = :eventType, fenceToken = :fenceToken, " +
                    "schemaVersion = :schemaVersion, createdAt = :createdAt, " +
                    "payload = :payload",
                    Map.of(
                            "id",            event.id(),
                            "tenantAlias",   event.tenantAlias(),
                            "eventType",     event.eventType(),
                            "fenceToken",    event.fenceToken(),
                            "schemaVersion", event.schemaVersion(),
                            "createdAt",     event.createdAt(),
                            "payload",       payloadJson));
            q.await().atMost(Duration.ofSeconds(10));
            return true;
        } catch (Exception e) {
            // ArcadeDB reports UNIQUE violations as generic 500 on HTTP; detect by message.
            String msg = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
            if (msg.contains("duplicate") || msg.contains("unique") || msg.contains("index already")) {
                LOG.debugf("[MTN-51] ControlEvent id=%s already persisted (idempotent skip)", event.id());
                return false;
            }
            LOG.errorf(e, "[MTN-51] ControlEvent persist failed: id=%s", event.id());
            throw new RuntimeException("persist failed", e);
        }
    }
}
