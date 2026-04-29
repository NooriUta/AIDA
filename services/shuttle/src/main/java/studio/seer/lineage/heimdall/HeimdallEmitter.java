package studio.seer.lineage.heimdall;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import studio.seer.lineage.heimdall.model.EventLevel;
import studio.seer.lineage.heimdall.model.EventType;
import studio.seer.lineage.heimdall.model.HeimdallEvent;
import studio.seer.lineage.security.SeerIdentity;
import studio.seer.tenantrouting.YggLineageRegistry;

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
 * Automatic payload enrichment per request context:
 *   tenantAlias — from X-Seer-Tenant-Alias (SeerIdentity)
 *   db          — ArcadeDB database name for this tenant (e.g. "hound_acme")
 *
 * Outside a request scope (background jobs) enrichment is silently skipped;
 * shuttle events are HTA-14-exempt so the missing tenantAlias is fine.
 */
@ApplicationScoped
public class HeimdallEmitter {

    private static final Logger LOG = Logger.getLogger(HeimdallEmitter.class);

    @Inject @RestClient HeimdallClient    client;
    @Inject             HeimdallEventBus  eventBus;
    @Inject             SeerIdentity      identity;
    @Inject             YggLineageRegistry lineageRegistry;

    /**
     * Emit a single event asynchronously. Non-blocking, errors swallowed at WARN.
     * tenantAlias + db are injected from the current request context automatically.
     */
    public void emit(EventType type, EventLevel level,
                     String sessionId, String correlationId,
                     long durationMs, Map<String, Object> payload) {

        // Enrich payload with tenant context from the current request.
        // Map.of(...) is immutable, so we always copy into a mutable map first.
        Map<String, Object> enriched = payload != null ? new HashMap<>(payload) : new HashMap<>();
        try {
            String tenant = identity.tenantAlias();
            if (tenant != null && !tenant.isBlank()) {
                enriched.putIfAbsent("tenantAlias", tenant);
                // Resolve the actual ArcadeDB database name for this tenant
                // (e.g. "hound_acme", "hound_default") so operators can identify
                // which DB a slow query hit without looking up the registry manually.
                try {
                    String db = lineageRegistry.resourceFor(tenant).databaseName();
                    if (db != null && !db.isBlank()) {
                        enriched.putIfAbsent("db", db);
                    }
                } catch (Exception dbEx) {
                    LOG.tracef("db enrichment skipped (registry miss for '%s'): %s", tenant, dbEx.getMessage());
                }
            }
        } catch (Exception ex) {
            // SeerIdentity is @RequestScoped — outside a request (e.g. background jobs)
            // this proxy call throws. Silently skip enrichment in that case.
            LOG.tracef("tenant/db enrichment skipped (no request context): %s", ex.getMessage());
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
