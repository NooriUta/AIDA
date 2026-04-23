package studio.seer.dali.rest;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jobrunr.scheduling.JobScheduler;
import studio.seer.dali.job.HarvestJob;
import studio.seer.dali.service.CancelResult;
import studio.seer.dali.service.SessionService;
import studio.seer.dali.storage.SessionRepository;
import studio.seer.shared.ParseSessionInput;
import studio.seer.tenantrouting.TenantContext;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST API for Dali parse sessions.
 *
 * <pre>
 * GET   /api/sessions             — list recent sessions (newest first) → 200 OK + Session[]
 * POST  /api/sessions             — enqueue a new parse session         → 202 Accepted + Session
 * GET   /api/sessions/{id}        — poll session status                 → 200 OK + Session | 404
 * POST  /api/sessions/{id}/cancel — cancel session (tenant-scoped)      → 202 | 403 | 404 | 409
 * </pre>
 */
@Path("/api/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SessionResource {

    @Inject SessionService          sessionService;
    @Inject SessionRepository       sessionRepository;
    @Inject Instance<JobScheduler>  jobScheduler;
    @Inject TenantContext           tenantCtx;

    @GET
    public Response list(@QueryParam("limit") @DefaultValue("50") int limit) {
        return Response.ok(sessionService.listRecent(tenantCtx.tenantAlias(), limit)).build();
    }

    @POST
    public Response create(SessionRequest body) {
        if (body == null || body.dialect() == null || body.source() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"dialect and source are required\"}")
                    .build();
        }
        try {
            return Response.accepted(sessionService.enqueue(body.toInput(tenantCtx.tenantAlias()))).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /** JSON body for POST /api/sessions. */
    private record SessionRequest(String dialect, String source, boolean preview, boolean clearBeforeWrite,
                                  String dbName, String appName) {
        ParseSessionInput toInput(String tenantAlias) {
            return new ParseSessionInput(
                    dialect, source, preview, clearBeforeWrite, false,
                    null, null, null,
                    dbName  != null && !dbName.isBlank()  ? dbName.strip()  : null,
                    appName != null && !appName.isBlank() ? appName.strip() : null,
                    tenantAlias);
        }
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        return sessionService.findForTenant(id, tenantCtx.tenantAlias())
                .map(s -> Response.ok(s).build())
                .orElseGet(() -> {
                    // Check if session exists at all (might belong to different tenant → 403)
                    if (sessionService.find(id).isPresent()) {
                        return Response.status(Response.Status.FORBIDDEN)
                                .entity("{\"error\":\"session belongs to a different tenant\"}")
                                .build();
                    }
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"error\":\"session not found\"}")
                            .build();
                });
    }

    @GET
    @Path("/archive")
    public Response archive(
            @QueryParam("limit") @DefaultValue("200") int limit,
            @QueryParam("allTenants") @DefaultValue("false") boolean allTenants) {
        List<studio.seer.shared.Session> sessions = (allTenants && tenantCtx.isSuperadmin())
                ? sessionRepository.findAllTenants(limit)
                : sessionRepository.findAll(tenantCtx.tenantAlias(), limit);
        return Response.ok(sessions).build();
    }

    /**
     * Cancel a session. DMT-08: enforces tenant isolation — 403 if session belongs to another tenant.
     * Superadmin (scope aida:superadmin) can cancel any session.
     *
     * <pre>
     * 202 Accepted  — cancellation requested
     * 403 Forbidden — session belongs to different tenant
     * 404 Not Found — session not found
     * 409 Conflict  — session already terminal
     * </pre>
     */
    @POST
    @Path("/{id}/cancel")
    @Consumes(MediaType.WILDCARD)
    public Response cancel(@PathParam("id") String id) {
        String alias = tenantCtx.isSuperadmin() ? null : tenantCtx.tenantAlias();
        CancelResult result = sessionService.cancelSession(id, alias);
        return switch (result.status()) {
            case "NOT_FOUND"   -> Response.status(Response.Status.NOT_FOUND).entity(result).build();
            case "FORBIDDEN"   -> Response.status(Response.Status.FORBIDDEN).entity(result).build();
            case "ALREADY_DONE"-> Response.status(Response.Status.CONFLICT).entity(result).build();
            default            -> Response.accepted(result).build();
        };
    }

    /**
     * Trigger a JDBC harvest for a specific tenant (used by Heimdall cron and admin UI).
     * Tenant alias comes from the X-Seer-Tenant-Alias header set by Chur, defaulting to "default".
     */
    @POST
    @Path("/harvest")
    @Consumes(MediaType.WILDCARD)
    public Response harvest(
            @HeaderParam("X-Seer-Tenant-Alias") @DefaultValue("default") String tenantAlias) {
        if (!jobScheduler.isResolvable()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"error\":\"JobScheduler not available — Dali may still be starting\"}")
                    .build();
        }
        String effectiveTenant = (tenantAlias != null && !tenantAlias.isBlank()) ? tenantAlias : "default";
        String harvestId = "harvest-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        jobScheduler.get().<HarvestJob>enqueue(j -> j.execute(harvestId, effectiveTenant));
        return Response.accepted(Map.of("harvestId", harvestId, "tenantAlias", effectiveTenant, "status", "enqueued")).build();
    }

    @GET
    @Path("/health")
    public Response health() {
        boolean friggOk = sessionService.isFriggHealthy();
        int sessionCount = sessionService.listRecent(tenantCtx.tenantAlias(), 200).size();
        return Response.ok(Map.of(
                "frigg",    friggOk ? "ok" : "error",
                "sessions", sessionCount,
                "tenant",   tenantCtx.tenantAlias()
        )).build();
    }
}
