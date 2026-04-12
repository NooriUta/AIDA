package studio.seer.lineage.heimdall;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import studio.seer.lineage.heimdall.model.HeimdallEvent;

/**
 * In-process broadcast bus: every HEIMDALL event emitted by HeimdallEmitter
 * is duplicated here so GraphQL subscriptions can deliver it in real time.
 *
 * Architecture (I33 — INTEGRATIONS_MATRIX):
 *   HeimdallEmitter --fire&forget--> HEIMDALL backend (HTTP)
 *   HeimdallEmitter.emit() also calls HeimdallEventBus.publish()
 *   BroadcastProcessor fans events out to all active WS subscribers
 *   SubscriptionResource.heimdallEvents() / sessionProgress() subscribe this Multi
 *
 * No SmallRye Reactive Messaging needed — pure Mutiny BroadcastProcessor.
 */
@ApplicationScoped
public class HeimdallEventBus {

    private static final Logger LOG = Logger.getLogger(HeimdallEventBus.class);

    /**
     * Hot, fan-out stream. Each subscriber receives every event published after
     * it subscribes. Late-joiners do NOT get backlog (intentional — live feed only).
     */
    private final BroadcastProcessor<HeimdallEvent> processor = BroadcastProcessor.create();

    /**
     * Called by HeimdallEmitter after each successful event dispatch.
     * Fires the event into the in-process broadcast channel.
     */
    public void publish(HeimdallEvent event) {
        processor.onNext(event);
        LOG.debugf("HeimdallEventBus: broadcast %s/%s",
                event.sourceComponent(), event.eventType());
    }

    /**
     * Returns a Multi that all active GraphQL subscription websockets can subscribe to.
     * Each call returns the same hot stream — new subscriber picks up from 'now'.
     */
    public Multi<HeimdallEvent> stream() {
        return processor;
    }
}
