package studio.seer.anvil.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import studio.seer.anvil.service.AnvilCache;
import studio.seer.shared.HeimdallEvent;

import java.util.Map;

/**
 * AV-03 — Cache management endpoints.
 *
 * <pre>
 * POST /api/cache/invalidate   — HEIMDALL webhook on dali.session_completed
 * GET  /api/cache/stats        — current cache size (used by /api/health)
 * </pre>
 */
@Path("/api/cache")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CacheResource {

    @Inject
    AnvilCache cache;

    /** Called by HEIMDALL when a Dali harvest completes — clears cached results for that DB. */
    @POST
    @Path("/invalidate")
    public Response invalidate(HeimdallEvent event) {
        if (event == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"event body required\"}")
                    .build();
        }
        Object dbName = event.payload().get("dbName");
        if (dbName != null) {
            cache.invalidateDb(dbName.toString());
        } else {
            // No dbName → full invalidation (fallback for unknown events)
            cache.invalidateDb("");
        }
        return Response.ok(Map.of("invalidated", dbName != null ? dbName.toString() : "all")).build();
    }

    @GET
    @Path("/stats")
    public Response stats() {
        return Response.ok(Map.of("cacheSize", cache.size())).build();
    }
}
