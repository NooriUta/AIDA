package studio.seer.heimdall.analytics;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

/**
 * UA-04: REST endpoint exposing UX analytics derived from the 24-hour event window.
 *
 * <pre>
 * GET /analytics/ux   — UX summary (hot nodes, level distribution, slow renders, active users)
 * </pre>
 *
 * No authentication guard here — HEIMDALL admin routes are secured at the
 * Chur proxy layer ({@code requireAdmin} middleware). HEIMDALL itself runs
 * inside the private network (not exposed to the internet).
 */
@Path("/analytics")
@Produces(MediaType.APPLICATION_JSON)
public class AnalyticsResource {

    private static final Logger LOG = Logger.getLogger(AnalyticsResource.class);

    @Inject
    UxAggregator uxAggregator;

    /**
     * Returns a UX analytics summary: hot nodes, level distribution, slow renders,
     * active session count, and window metadata.
     *
     * @return 200 with {@link UxAggregator.UxSummary} JSON
     */
    @GET
    @Path("/ux")
    public Response getUxSummary() {
        try {
            UxAggregator.UxSummary summary = uxAggregator.getUxSummary();
            return Response.ok(summary).build();
        } catch (Exception e) {
            LOG.errorf("Failed to compute UX summary: %s", e.getMessage());
            return Response.serverError()
                    .entity("{\"error\":\"analytics_unavailable\"}")
                    .build();
        }
    }
}
