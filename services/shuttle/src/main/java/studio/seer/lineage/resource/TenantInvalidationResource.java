package studio.seer.lineage.resource;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import studio.seer.tenantrouting.YggLineageRegistry;

import java.util.Map;

/**
 * MTN-01: Internal invalidation endpoint for the Frigg-backed lineage registry.
 *
 * <p>When Chur's {@code POST /api/admin/tenants/:alias/reconnect} is called
 * (after a config update), it forwards an invalidation signal here so the
 * per-tenant connection cache refreshes on next access.
 *
 * <p>Auth: internal shared secret via {@code X-Internal-Auth} header.
 * The secret is a dev-only convenience; production should use mTLS / service
 * mesh identity (tracked as MTN-19 security-hardening).
 *
 * <p>Endpoints:
 * <pre>
 *   POST /api/internal/tenant-invalidate
 *   Headers: X-Internal-Auth: {secret}
 *   Body:    {"tenantAlias": "acme"}
 *   → 204 or 202 with {ok: true}
 * </pre>
 */
@Path("/api/internal")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TenantInvalidationResource {

    @Inject YggLineageRegistry lineageRegistry;

    @ConfigProperty(name = "aida.internal.shared-secret", defaultValue = "aida-internal-dev-secret")
    String expectedSecret;

    public record InvalidateRequest(String tenantAlias, Boolean all) {}

    @POST
    @Path("/tenant-invalidate")
    public Response invalidate(@HeaderParam("X-Internal-Auth") String auth,
                                InvalidateRequest req) {
        if (auth == null || !auth.equals(expectedSecret)) {
            Log.warnf("[MTN-01] tenant-invalidate rejected — bad X-Internal-Auth");
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "invalid_internal_auth")).build();
        }
        if (req == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "body required")).build();
        }
        if (Boolean.TRUE.equals(req.all())) {
            lineageRegistry.invalidateAll();
            Log.infof("[MTN-01] invalidateAll — entire registry cache cleared");
            return Response.ok(Map.of("ok", true, "invalidated", "all")).build();
        }
        if (req.tenantAlias() == null || req.tenantAlias().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "tenantAlias or all=true required")).build();
        }
        lineageRegistry.invalidate(req.tenantAlias());
        Log.infof("[MTN-01] invalidated tenant=%s", req.tenantAlias());
        return Response.ok(Map.of("ok", true, "invalidated", req.tenantAlias())).build();
    }
}
