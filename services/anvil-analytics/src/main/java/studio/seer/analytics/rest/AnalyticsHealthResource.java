package studio.seer.analytics.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import studio.seer.analytics.client.AnalyticsClient;
import studio.seer.analytics.jobs.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AV-12: Analytics health and last-run status endpoint.
 *
 * <pre>
 * GET /api/analytics/health
 * → {
 *     "status": "ok",
 *     "db": "hound_default",
 *     "phase": 1,
 *     "lastRun": {
 *       "pagerank":    "2026-04-29T10:00:00Z",
 *       "wcc":         "2026-04-29T01:00:00Z",
 *       "hits":        "2026-04-29T02:00:00Z",
 *       "bridges":     "2026-04-27T03:00:00Z",
 *       "articulation":"2026-04-27T03:00:00Z",
 *       "scc":         "2026-04-27T04:00:00Z",
 *       "community":   "2026-04-29T00:30:00Z"
 *     },
 *     "jobs": ["pagerank", "wcc", "hits", "bridges", "articulation", "scc", "community"]
 *   }
 * </pre>
 */
@Path("/api/analytics")
@Produces(MediaType.APPLICATION_JSON)
public class AnalyticsHealthResource {

    @Inject AnalyticsClient  analytics;
    @Inject PageRankJob      pageRankJob;
    @Inject WccJob           wccJob;
    @Inject HitsJob          hitsJob;
    @Inject BridgesJob       bridgesJob;
    @Inject ArticulationJob  articulationJob;
    @Inject SccJob           sccJob;
    @Inject CommunityJob     communityJob;

    @GET
    @Path("/health")
    public Response health() {
        // Quick liveness probe: run a trivial query to check DB connectivity
        boolean dbReachable = true;
        String dbError      = null;
        try {
            analytics.query("SELECT 1");
        } catch (Exception e) {
            dbReachable = false;
            dbError     = e.getMessage();
        }

        Map<String, Object> lastRun = new LinkedHashMap<>();
        lastRun.put("pagerank",     pageRankJob.lastRunTimestamp());
        lastRun.put("wcc",          wccJob.lastRunTimestamp());
        lastRun.put("hits",         hitsJob.lastRunTimestamp());
        lastRun.put("bridges",      bridgesJob.lastRunTimestamp());
        lastRun.put("articulation", articulationJob.lastRunTimestamp());
        lastRun.put("scc",          sccJob.lastRunTimestamp());
        lastRun.put("community",    communityJob.lastRunTimestamp());

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("status",  dbReachable ? "ok" : "degraded");
        body.put("db",      analytics.dbName());
        body.put("phase",   1);
        body.put("lastRun", lastRun);
        body.put("jobs",    List.of("pagerank", "wcc", "hits", "bridges", "articulation", "scc", "community"));

        if (dbError != null) {
            body.put("dbError", dbError);
        }

        int status = dbReachable ? 200 : 503;
        return Response.status(status).entity(body).build();
    }
}
