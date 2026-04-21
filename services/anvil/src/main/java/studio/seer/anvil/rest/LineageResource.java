package studio.seer.anvil.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import studio.seer.anvil.model.LineageRequest;
import studio.seer.anvil.service.LineageService;

/**
 * AV-02 — Lineage query endpoint.
 *
 * <pre>
 * POST /api/lineage   — traverse DATA_FLOW lineage upstream / downstream / both
 * </pre>
 */
@Path("/api/lineage")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LineageResource {

    @Inject
    LineageService lineageService;

    @POST
    public Response queryLineage(LineageRequest req) {
        if (req == null || req.nodeId() == null || req.nodeId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"nodeId is required\"}")
                    .build();
        }
        String dir = req.direction();
        if (dir != null && !dir.equals("upstream") && !dir.equals("downstream") && !dir.equals("both")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"direction must be upstream | downstream | both\"}")
                    .build();
        }
        return Response.ok(lineageService.queryLineage(req)).build();
    }
}
