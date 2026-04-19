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

import java.util.Map;
import java.util.UUID;

/**
 * REST API for Dali parse sessions.
 *
 * <pre>
 * GET   /api/sessions             — list recent sessions (newest first) → 200 OK + Session[]
 * POST  /api/sessions             — enqueue a new parse session         → 202 Accepted + Session
 * GET   /api/sessions/{id}        — poll session status                 → 200 OK + Session | 404
 * </pre>
 */
@Path("/api/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SessionResource {

    @Inject SessionService          sessionService;
    @Inject SessionRepository       sessionRepository;
    @Inject Instance<JobScheduler>  jobScheduler;

    @GET
    public Response list(@QueryParam("limit") @DefaultValue("50") int limit) {
        return Response.ok(sessionService.listRecent(limit)).build();
    }

    @POST
    public Response create(SessionRequest body) {
        if (body == null || body.dialect() == null || body.source() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"dialect and source are required\"}")
                    .build();
        }
        try {
            return Response.accepted(sessionService.enqueue(body.toInput())).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
        }
    }

    /** JSON body for POST /api/sessions — does not expose the internal {@code uploaded} flag. */
    private record SessionRequest(String dialect, String source, boolean preview, boolean clearBeforeWrite) {
        ParseSessionInput toInput() {
            return new ParseSessionInput(dialect, source, preview, clearBeforeWrite, false);
        }
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        return sessionService.find(id)
                .map(s -> Response.ok(s).build())
                .orElse(Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"error\":\"session not found\"}")
                        .build());
    }

    /**
     * FRIGG archive — sessions stored in FRIGG, bypassing the in-memory cache.
     * Used by the UI "Archive" section to show the authoritative historical record.
     */
    @GET
    @Path("/archive")
    public Response archive(@QueryParam("limit") @DefaultValue("200") int limit) {
        return Response.ok(sessionRepository.findAll(limit)).build();
    }

    /**
     * Cancel a session.
     *
     * <pre>
     * 202 Accepted  — cancellation requested (status = CANCELLING)
     * 404 Not Found — session not found
     * 409 Conflict  — session already in terminal state
     * </pre>
     */
    @POST
    @Path("/{id}/cancel")
    @Consumes(MediaType.WILDCARD)   // no request body — accept any (or missing) Content-Type
    public Response cancel(@PathParam("id") String id) {
        CancelResult result = sessionService.cancelSession(id);
        return switch (result.status()) {
            case "NOT_FOUND"    -> Response.status(Response.Status.NOT_FOUND).entity(result).build();
            case "ALREADY_DONE" -> Response.status(Response.Status.CONFLICT).entity(result).build();
            default             -> Response.accepted(result).build();
        };
    }

    /**
     * Trigger a full JDBC harvest via {@link HarvestJob} (C.3.2 / DS-03).
     *
     * <pre>
     * 202 Accepted  — HarvestJob enqueued, harvestId in response body
     * 503 Service Unavailable — JobScheduler not yet initialised
     * </pre>
     */
    @POST
    @Path("/harvest")
    @Consumes(MediaType.WILDCARD)
    public Response harvest() {
        if (!jobScheduler.isResolvable()) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity("{\"error\":\"JobScheduler not available — Dali may still be starting\"}")
                    .build();
        }
        String harvestId = "harvest-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        jobScheduler.get().<HarvestJob>enqueue(j -> j.execute(harvestId));
        return Response.accepted(Map.of("harvestId", harvestId, "status", "enqueued")).build();
    }

    @GET
    @Path("/health")
    public Response health() {
        boolean friggOk = sessionService.isFriggHealthy();
        int sessionCount = sessionService.listRecent(200).size();
        return Response.ok(Map.of(
                "frigg",    friggOk ? "ok" : "error",
                "sessions", sessionCount
        )).build();
    }
}
