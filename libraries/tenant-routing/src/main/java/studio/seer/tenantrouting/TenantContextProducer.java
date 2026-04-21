package studio.seer.tenantrouting;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

/**
 * CDI producer that creates the immutable {@link TenantContext} from the
 * request-scoped {@link TenantContextHolder} populated by {@link TenantContextFilter}.
 *
 * TenantContext is a record (final) — cannot use @RequestScoped (requires proxy subclass).
 * @Dependent gives per-injection-point lifecycle, scoped to the enclosing bean's lifetime.
 */
@ApplicationScoped
public class TenantContextProducer {

    @Inject
    TenantContextHolder holder;

    @Produces
    @Dependent
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
