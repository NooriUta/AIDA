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
 * Fire-and-forget event emitter from SHUTTLE → HEIMDALL.
 *
 * Two delivery paths (both non-blocking):
 *  1. HTTP POST /events → HEIMDALL backend (persistence + ring buffer)
 *  2. HeimdallEventBus.publish() → in-process BroadcastProcessor
 *     → GraphQL subscriptions (I33: SHUTTLE as event relay for verdandi)
 *
 * Key guarantee: errors on either path are logged at WARN and never propagated.
 * HEIMDALL being down must NOT affect SHUTTLE operation.
 */
@ApplicationScoped
public class HeimdallEmitter {

    private static final Logger LOG = Logger.getLogger(HeimdallEmitter.class);

    @Inject @RestClient HeimdallClient client;
    @Inject             HeimdallEventBus eventBus;

    /**
     * Emit a single event asynchronously. Non-blocking, errors swallowed at WARN.
     */
    public void emit(EventType type, EventLevel level,
                     String sessionId, String correlationId,
                     long durationMs, Map<String, Object> payload) {

        var event = new HeimdallEvent(
                System.currentTimeMillis(),
                "shuttle",
                type.name(),
                level,
                sessionId,
                null,
                correlationId,
                durationMs,
                payload
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
