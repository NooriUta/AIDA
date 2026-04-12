package studio.seer.lineage.resource;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import studio.seer.lineage.heimdall.HeimdallEventBus;
import studio.seer.lineage.heimdall.model.HeimdallEventView;

/**
 * GraphQL Subscriptions for SHUTTLE.
 *
 * Transport: graphql-ws protocol over WebSocket on /graphql endpoint
 * (handled natively by quarkus-smallrye-graphql — no extra setup needed).
 *
 * Architecture (I33 — INTEGRATIONS_MATRIX):
 *   HEIMDALL --HTTP/WS--> heimdall-frontend (native WebSocket, direct)
 *   HEIMDALL --events--> HeimdallEmitter --bus--> SubscriptionResource
 *                     --> graphql-ws --> verdandi / shell (via Chur proxy)
 *
 * heimdallEvents    — all events (admin only in production, role check via Chur)
 * sessionProgress   — events filtered by sessionId (viewer+)
 *
 * Note: returns HeimdallEventView (not HeimdallEvent) because Map<String,Object>
 * does not map cleanly to a GraphQL scalar — payload is serialised as payloadJson String.
 */
@GraphQLApi
public class SubscriptionResource {

    @Inject
    HeimdallEventBus eventBus;

    // ── heimdallEvents ────────────────────────────────────────────────────────

    @Subscription("heimdallEvents")
    @Description("Live HEIMDALL event stream via graphql-ws. Delivers all components. Role: admin")
    public Multi<HeimdallEventView> heimdallEvents() {
        return eventBus.stream().map(HeimdallEventView::from);
    }

    // ── sessionProgress ───────────────────────────────────────────────────────

    @Subscription("sessionProgress")
    @Description("Live events filtered by sessionId. Role: viewer+")
    public Multi<HeimdallEventView> sessionProgress(
            @Name("sessionId") String sessionId) {

        return eventBus.stream()
                .filter(e -> sessionId != null && sessionId.equals(e.sessionId()))
                .map(HeimdallEventView::from);
    }
}
