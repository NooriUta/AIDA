package studio.seer.anvil.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import studio.seer.anvil.model.ImpactRequest;
import studio.seer.anvil.security.TenantContextFilter;
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
    public Response findImpact(ImpactRequest req, @Context ContainerRequestContext ctx) {
        if (req == null || req.nodeId() == null || req.nodeId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"nodeId is required\"}")
                    .build();
        }
        // MTN-06/30: tenant alias resolved by TenantContextFilter. Fallback to
        // "default" when the filter is disabled (aida.tenant.enforce=false).
        String tenantAlias = (String) ctx.getProperty(TenantContextFilter.TENANT_CONTEXT_PROPERTY);
        if (tenantAlias == null || tenantAlias.isBlank()) tenantAlias = "default";
        return Response.ok(impactService.findImpact(req, tenantAlias)).build();
    }
}
