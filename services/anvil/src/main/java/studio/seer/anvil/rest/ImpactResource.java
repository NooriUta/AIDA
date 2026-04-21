package studio.seer.anvil.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import studio.seer.anvil.model.ImpactRequest;
import studio.seer.anvil.service.ImpactService;

/**
 * AV-01 — Impact analysis endpoint.
 *
 * <pre>
 * POST /api/impact   — find downstream/upstream affected nodes for a given nodeId
 * </pre>
 */
@Path("/api/impact")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ImpactResource {

    @Inject
    ImpactService impactService;

    @POST
    public Response findImpact(ImpactRequest req) {
        if (req == null || req.nodeId() == null || req.nodeId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"nodeId is required\"}")
                    .build();
        }
        return Response.ok(impactService.findImpact(req)).build();
    }
}
