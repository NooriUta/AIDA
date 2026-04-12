package studio.seer.heimdall.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import studio.seer.heimdall.metrics.MetricsCollector;
import studio.seer.heimdall.metrics.MetricsSnapshot;

/**
 * Read-only metrics endpoint.
 * GET /metrics/snapshot → live aggregated counters.
 */
@Path("/metrics")
@Produces(MediaType.APPLICATION_JSON)
public class MetricsResource {

    @Inject MetricsCollector metricsCollector;

    @GET
    @Path("/snapshot")
    public MetricsSnapshot snapshot() {
        return metricsCollector.snapshot();
    }
}
