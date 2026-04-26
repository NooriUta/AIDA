package studio.seer.heimdall.metrics;

import java.util.List;

/**
 * HTA-09: Per-tenant metrics snapshot (JSON-serialisable).
 *
 * top20  — most active tenants by totalEvents DESC
 * rest   — aggregated counters for all other tenants
 * totals — sum across all tenants (sanity check / dashboard headline)
 */
public record TenantMetricsSummary(
        List<TenantCounter> top20,
        TenantCounter       rest,
        TenantCounter       totals,
        int                 tenantCount
) {
    public record TenantCounter(
            String tenantAlias,
            long   totalEvents,
            long   parseSessions,
            long   atoms,
            long   activeJobs,
            long   errors,
            long   lastEventAt
    ) {
        public static TenantCounter empty(String alias) {
            return new TenantCounter(alias, 0, 0, 0, 0, 0, 0);
        }
    }
}
