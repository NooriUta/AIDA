package studio.seer.tenantrouting;

/**
 * Generic registry that maps a tenant alias to a pooled resource T.
 * Implementations cache resources by (tenantAlias, configVersion) and
 * rebuild on invalidation (triggered by config change or reconnect call).
 */
public interface TenantResourceRegistry<T> {

    /**
     * Return the resource for the given tenant alias.
     * Creates and caches the resource on first access.
     */
    T resourceFor(String tenantAlias);

    /** Invalidate the cached resource for one tenant (e.g. after config change). */
    void invalidate(String tenantAlias);

    /** Invalidate all cached resources (e.g. on bulk config reload). */
    void invalidateAll();
}
