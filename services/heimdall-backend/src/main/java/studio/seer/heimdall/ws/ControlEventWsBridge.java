package studio.seer.heimdall.ws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import studio.seer.heimdall.control.ControlEventEmitter;
import studio.seer.shared.ControlEvent;

/**
 * MTN-36 — Bridge {@link ControlEventEmitter.ControlEventPublished} CDI events
 * to {@link EventStreamEndpoint#disconnectTenant}.
 *
 * <p>Closes per-tenant WebSocket subscribers within the ADR-MT-007 5-second
 * SLA for {@code SUSPENDED / ARCHIVED / PURGED} transitions. Non-superadmin
 * sessions bound to that tenant are closed with WebSocket close code 1008
 * (policy violation) and reason = event type. Superadmin streams stay open —
 * they observe cross-tenant events including the follow-up state change.
 */
@ApplicationScoped
public class ControlEventWsBridge {

    private static final Logger LOG = Logger.getLogger(ControlEventWsBridge.class);

    @Inject EventStreamEndpoint endpoint;

    public void onPublished(@Observes ControlEventEmitter.ControlEventPublished evt) {
        ControlEvent e = evt.event();
        switch (e.eventType()) {
            case "tenant_suspended":
            case "tenant_archived":
            case "tenant_purged":
            case "tenant_hibernated": {
                int closed = endpoint.disconnectTenant(e.tenantAlias(), e.eventType());
                LOG.infof("[MTN-36] disconnected %d ws for tenant=%s reason=%s fence=%d",
                        closed, e.tenantAlias(), e.eventType(), e.fenceToken());
                break;
            }
            case "tenant_invalidated":
            case "tenant_restored":
            default:
                // Other events don't force WS disconnect — consumers re-read
                // status through reconcile loops (MTN-51 pull).
                break;
        }
    }
}
