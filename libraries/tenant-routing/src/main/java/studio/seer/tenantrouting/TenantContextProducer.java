package studio.seer.tenantrouting;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

/**
 * CDI producer that creates the immutable {@link TenantContext} from the
 * request-scoped {@link TenantContextHolder} populated by {@link TenantContextFilter}.
 */
@ApplicationScoped
public class TenantContextProducer {

    @Inject
    TenantContextHolder holder;

    @Produces
    @RequestScoped
    public TenantContext produce() {
        return new TenantContext(
                holder.tenantAlias(),
                holder.userId(),
                holder.userEmail(),
                holder.scopes(),
                holder.roles(),
                holder.correlationId()
        );
    }
}
