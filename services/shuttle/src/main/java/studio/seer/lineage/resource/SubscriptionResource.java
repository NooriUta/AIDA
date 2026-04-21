package studio.seer.lineage.resource;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Name;
import studio.seer.lineage.heimdall.HeimdallEventBus;
import studio.seer.lineage.heimdall.model.HeimdallEvent;
import studio.seer.lineage.heimdall.model.HeimdallEventView;
import studio.seer.lineage.security.SeerIdentity;

/**
 * GraphQL Subscriptions for SHUTTLE.
 *
 * SHT-07: All subscriptions are tenant-filtered. Events are only delivered
 * when event.tenantAlias matches the subscriber's tenant alias, or when the
 * subscriber has superadmin role (null tenantAlias = no filter).
 */
@GraphQLApi
public class SubscriptionResource {

    @Inject HeimdallEventBus eventBus;
    @Inject SeerIdentity     identity;

    // ── heimdallEvents ────────────────────────────────────────────────────────

    @Subscription("heimdallEvents")
    @Description("Live HEIMDALL event stream via graphql-ws. Tenant-filtered. Role: admin")
    public Multi<HeimdallEventView> heimdallEvents() {
        String alias = identity.tenantAlias();
        boolean isSuperadmin = "super-admin".equals(identity.role());

        return eventBus.stream()
                .filter(e -> isSuperadmin || tenantMatches(e, alias))
                .map(HeimdallEventView::from);
    }

    // ── sessionProgress ───────────────────────────────────────────────────────

    @Subscription("sessionProgress")
    @Description("Live events filtered by sessionId and tenant. Role: viewer+")
    public Multi<HeimdallEventView> sessionProgress(
            @Name("sessionId") String sessionId) {

        String alias = identity.tenantAlias();
        boolean isSuperadmin = "super-admin".equals(identity.role());

        return eventBus.stream()
                .filter(e -> sessionId != null && sessionId.equals(e.sessionId()))
                .filter(e -> isSuperadmin || tenantMatches(e, alias))
                .map(HeimdallEventView::from);
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private static boolean tenantMatches(HeimdallEvent e, String alias) {
        String eventAlias = e.tenantAlias();
        // null tenantAlias on event = legacy/global event, allow to all
        return eventAlias == null || eventAlias.isBlank() || eventAlias.equals(alias);
    }
}
