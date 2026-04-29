package studio.seer.lineage.heimdall;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import studio.seer.lineage.heimdall.model.EventLevel;
import studio.seer.lineage.heimdall.model.EventType;
import studio.seer.lineage.heimdall.model.HeimdallEvent;
import studio.seer.lineage.security.SeerIdentity;

import java.util.HashMap;
import java.util.Map;

/**
 * Fire-and-forget event emitter from SHUTTLE → HEIMDALL.
 *
 * Two delivery paths (both non-blocking):
 *  1. HTTP POST /events → HEIMDALL backend (persistence + ring buffer)
 *  2. HeimdallEventBus.publish() → in-process BroadcastProcessor
 *     → GraphQL subscriptions (I33: SHUTTLE as event relay for verdandi)
 *
 * Key guarantee: errors on either path are logged at WARN and never propagated.
 * HEIMDALL being down must NOT affect SHUTTLE operation.
 *
 * tenantAlias is automatically added to each event payload from SeerIdentity
 * (the X-Seer-Tenant-Alias header forwarded by Chur). Events emitted outside
 * a request scope (e.g. background jobs) will get null — that's fine since
 * shuttle is exempt from HTA-14 tenant-tag enforcement.
 */
@ApplicationScoped
public class HeimdallEmitter {

    private static final Logger LOG = Logger.getLogger(HeimdallEmitter.class);

    @Inject @RestClient HeimdallClient  client;
    @Inject             HeimdallEventBus eventBus;
    @Inject             SeerIdentity     identity;

    /**
     * Emit a single event asynchronously. Non-blocking, errors swallowed at WARN.
     * tenantAlias is injected from the current request context automatically.
     */
    public void emit(EventType type, EventLevel level,
                     String sessionId, String correlationId,
                     long durationMs, Map<String, Object> payload) {

        // Enrich payload with tenantAlias from the current request context.
        // Guard against null payload from callers that pass Map.of(...) — Map.of is immutable.
        Map<String, Object> enriched = payload != null ? new HashMap<>(payload) : new HashMap<>();
        try {
            String tenant = identity.tenantAlias();
            if (tenant != null && !tenant.isBlank()) {
                enriched.putIfAbsent("tenantAlias", tenant);
            }
        } catch (Exception ex) {
            // SeerIdentity is @RequestScoped — outside a request (e.g. background jobs)
            // this proxy call throws. Silently skip tenant enrichment in that case.
            LOG.tracef("tenantAlias enrichment skipped (no request context): %s", ex.getMessage());
        }

        var event = new HeimdallEvent(
                System.currentTimeMillis(),
                "shuttle",
                type.name(),
                level,
                sessionId,
                null,
                correlationId,
                durationMs,
                enriched.isEmpty() ? null : enriched
        );

        // Path 1 — HTTP to HEIMDALL backend (fire-and-forget)
        client.send(event)
                .subscribe().with(
                        __ -> LOG.debugf("Emitted %s to HEIMDALL backend", type),
                        ex -> LOG.warnf("HEIMDALL emit failed (%s): %s", type, ex.getMessage())
                );

        // Path 2 — in-process bus for GraphQL subscriptions
        try {
            eventBus.publish(event);
        } catch (Exception ex) {
            LOG.warnf("HeimdallEventBus publish failed (%s): %s", type, ex.getMessage());
        }
    }
}
