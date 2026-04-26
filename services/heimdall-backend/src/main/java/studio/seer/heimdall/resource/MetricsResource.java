package studio.seer.heimdall.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import studio.seer.heimdall.metrics.MetricsCollector;
import studio.seer.heimdall.metrics.MetricsSnapshot;
import studio.seer.heimdall.metrics.TenantMetricsService;
import studio.seer.heimdall.metrics.TenantMetricsSummary;

/**
 * Read-only metrics endpoint.
 * GET /metrics/snapshot → live aggregated counters.
 */
@Path("/metrics")
@Produces(MediaType.APPLICATION_JSON)
public class MetricsResource {

    @Inject MetricsCollector     metricsCollector;
    @Inject TenantMetricsService tenantMetrics;

    @GET
    @Path("/snapshot")
    public MetricsSnapshot snapshot() {
        return metricsCollector.snapshot();
    }

    /**
     * HTA-09: Per-tenant metrics — top-20 active tenants + aggregated rest.
     * Clients poll this every 10s from the Heimdall dashboard.
     */
    @GET
    @Path("/tenants")
    public TenantMetricsSummary tenants() {
        return tenantMetrics.summary();
    }
}
