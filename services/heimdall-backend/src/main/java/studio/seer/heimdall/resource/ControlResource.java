package studio.seer.heimdall.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;
import studio.seer.heimdall.RingBuffer;
import studio.seer.heimdall.metrics.MetricsCollector;
import studio.seer.heimdall.snapshot.SnapshotManager;
import studio.seer.heimdall.tenant.TenantContext;

import java.util.Map;

/**
 * Admin control plane for HEIMDALL.
 *
 * Authorization model: Chur validates Keycloak JWT + role, then proxies requests with
 * X-Seer-Role header. HEIMDALL trusts this header (same pattern as SHUTTLE + SeerIdentity).
 * Only "admin" role is accepted for all endpoints.
 *
 * Endpoints:
 *   POST /control/reset                — clear ring buffer + reset counters
 *   POST /control/snapshot?name=...   — save snapshot to FRIGG
 *   POST /control/cancel/{sessionId}  — stub (Dali integration Sprint 3)
 *   GET  /control/snapshots           — list saved snapshots
 */
@Path("/control")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ControlResource {

    private static final Logger LOG = Logger.getLogger(ControlResource.class);

    @Inject RingBuffer       ringBuffer;
    @Inject MetricsCollector metricsCollector;
    @Inject SnapshotManager  snapshots;
    @Inject TenantContext    tenantCtx;

    @POST
    @Path("/reset")
    public Response reset(@HeaderParam("X-Seer-Role") String role) {
        if (!isAdmin(role)) return forbidden();
        ringBuffer.clear();       // pushes DEMO_RESET event to all WebSocket subscribers
        metricsCollector.reset(); // explicit counter reset
        LOG.infof("Demo reset by role=%s", role);
        return Response.ok(Map.of("status", "reset")).build();
    }

    @POST
    @Path("/snapshot")
    public Uni<Response> saveSnapshot(
            @HeaderParam("X-Seer-Role") String role,
            @QueryParam("name")         String name) {
        if (!isAdmin(role)) return Uni.createFrom().item(forbidden());
        var events = ringBuffer.snapshot();
        return snapshots.save(name, events)
                .map(id -> Response.ok(Map.of(
                        "snapshotId",  id,
                        "eventCount",  events.size(),
                        "name",        name != null ? name : "unnamed")).build())
                .onFailure().recoverWithItem(ex ->
                        Response.serverError().entity(Map.of("error", ex.getMessage())).build());
    }

    @POST
    @Path("/cancel/{sessionId}")
    public Response cancelSession(
            @HeaderParam("X-Seer-Role") String role,
            @PathParam("sessionId")     String sessionId) {
        if (!isAdmin(role)) return forbidden();
        // Stub — real cancellation via Dali API (Sprint 3)
        return Response.ok(Map.of(
                "sessionId", sessionId,
                "status",    "cancel_requested",
                "note",      "stub, Dali integration pending")).build();
    }

    @GET
    @Path("/snapshots")
    public Uni<Response> listSnapshots(@HeaderParam("X-Seer-Role") String role) {
        if (!isAdmin(role)) return Uni.createFrom().item(forbidden());
        return snapshots.list()
                .map(list -> Response.ok(list).build())
                .onFailure().recoverWithItem(ex ->
                        Response.serverError().entity(Map.of("error", ex.getMessage())).build());
    }

    private boolean isAdmin(String role) {
        // Accept both legacy X-Seer-Role header value and new scope-based check
        return "admin".equalsIgnoreCase(role)
            || "super-admin".equalsIgnoreCase(role)
            || tenantCtx.isAdmin()
            || tenantCtx.isSuperAdmin();
    }

    private Response forbidden() {
        return Response.status(403).entity(Map.of("error", "admin role required")).build();
    }
}
