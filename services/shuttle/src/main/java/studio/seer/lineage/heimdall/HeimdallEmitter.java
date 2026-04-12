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
 * Key guarantee: errors are logged at WARN level and never propagated.
 * HEIMDALL being down must NOT affect SHUTTLE operation.
 */
@ApplicationScoped
public class HeimdallEmitter {

    private static final Logger LOG = Logger.getLogger(HeimdallEmitter.class);

    @Inject @RestClient HeimdallClient client;

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
        client.send(event)
                .subscribe().with(
                        __ -> LOG.debugf("Emitted %s to HEIMDALL", type),
                        ex -> LOG.warnf("HEIMDALL emit failed (%s): %s", type, ex.getMessage())
                );
    }
}
