package studio.seer.tenantrouting;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * STUB — post-HighLoad implementation that will look up {@code DaliTenantConfig}
 * from {@code frigg-tenants} to resolve per-tenant YGG lineage DB name and URL.
 *
 * For HighLoad demo, use {@link DefaultYggLineageRegistry} (single shared pool).
 * This class exists as a placeholder to avoid refactoring when the multi-tenant
 * registry is wired in.
 *
 * TODO (post-HL): implement lookup via FRIGG DaliTenantConfig, per-tenant HikariCP pools,
 *                 and configVersion-based invalidation per SHUTTLE_TENANT_ISOLATION.md §2.
 */
@ApplicationScoped
public class FriggYggLineageRegistry implements YggLineageRegistry {

    @Override
    public ArcadeConnection resourceFor(String tenantAlias) {
        throw new UnsupportedOperationException(
                "FriggYggLineageRegistry not yet implemented — use DefaultYggLineageRegistry for HL demo");
    }

    @Override
    public void invalidate(String tenantAlias) {
        throw new UnsupportedOperationException("FriggYggLineageRegistry not yet implemented");
    }

    @Override
    public void invalidateAll() {
        throw new UnsupportedOperationException("FriggYggLineageRegistry not yet implemented");
    }
}
