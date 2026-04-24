package studio.seer.heimdall.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import studio.seer.shared.ControlEvent;
import studio.seer.shared.SchemaVersion;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * MTN-51 — Convenience API for emitting {@code seer.control.*} events.
 *
 * <p>Responsibilities per emit:
 *   <ol>
 *     <li>Stamp a fresh {@code fenceToken} via {@link FenceTokenProvider}.</li>
 *     <li>Stamp {@code schemaVersion = CURRENT} via {@link SchemaVersion}.</li>
 *     <li>Persist to FRIGG via {@link ControlEventStore}.</li>
 *     <li>Fire a CDI event of type {@link ControlEventPublished} so in-process
 *         observers (e.g. {@code ControlEventWsBridge} for MTN-36 WS disconnect)
 *         react synchronously without polling the store.</li>
 *   </ol>
 *
 * <p>HTTP fan-out to consumer services (SHUTTLE / ANVIL / DALI / MIMIR) is
 * performed by each consumer's periodic {@code ControlEventPoller} — pull-based,
 * not push — which gives exactly-once delivery with resumable offset and does
 * not require HEIMDALL to know every consumer's URL at emit time.
 */
@ApplicationScoped
public class ControlEventEmitter {

    private static final Logger LOG = Logger.getLogger(ControlEventEmitter.class);

    @Inject FenceTokenProvider  fences;
    @Inject ControlEventStore   store;
    @Inject Event<ControlEventPublished> cdi;

    public ControlEvent emitTenantInvalidated(String tenantAlias, String cause) {
        return emit(tenantAlias, "tenant_invalidated", map("cause", cause));
    }

    public ControlEvent emitTenantSuspended(String tenantAlias) {
        return emit(tenantAlias, "tenant_suspended", Map.of());
    }

    public ControlEvent emitTenantArchived(String tenantAlias) {
        return emit(tenantAlias, "tenant_archived", Map.of());
    }

    public ControlEvent emitTenantRestored(String tenantAlias) {
        return emit(tenantAlias, "tenant_restored", Map.of());
    }

    public ControlEvent emitTenantPurged(String tenantAlias) {
        return emit(tenantAlias, "tenant_purged", Map.of());
    }

    /**
     * Generic entry point. Typed helpers are thin wrappers; callers who need
     * arbitrary {@code eventType} values (tests, custom downstream) use this.
     */
    public ControlEvent emit(String tenantAlias, String eventType, Map<String, Object> payload) {
        long token = fences.next();
        Map<String, Object> stamped = SchemaVersion.withCurrentSchemaVersion(
                payload == null ? Map.of() : payload);

        ControlEvent event = ControlEvent.newEvent(
                tenantAlias, eventType, token, SchemaVersion.CURRENT, stamped);

        boolean persisted = store.persist(event);
        if (!persisted) {
            // Idempotent duplicate — don't re-fire CDI event, caller already reacted.
            LOG.debugf("[MTN-51] emit %s/%s — idempotent duplicate id=%s",
                    eventType, tenantAlias, event.id());
            return event;
        }

        LOG.infof("[MTN-51] emit %s tenant=%s fence=%d id=%s",
                eventType, tenantAlias, token, event.id());
        cdi.fire(new ControlEventPublished(event));
        return event;
    }

    private static Map<String, Object> map(String k, Object v) {
        if (v == null) return Map.of();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put(k, v);
        return m;
    }

    /** CDI event fired after successful persist. Observers react in-process. */
    public record ControlEventPublished(ControlEvent event) {}
}
