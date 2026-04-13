package studio.seer.dali.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import studio.seer.dali.service.SessionService;
import studio.seer.dali.storage.SessionRepository;
import studio.seer.shared.ParseSessionInput;

import java.util.Map;

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

    @Inject SessionService    sessionService;
    @Inject SessionRepository sessionRepository;

    @GET
    public Response list(@QueryParam("limit") @DefaultValue("50") int limit) {
        return Response.ok(sessionService.listRecent(limit)).build();
    }

    @POST
    public Response create(ParseSessionInput input) {
        if (input == null || input.dialect() == null || input.source() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"dialect and source are required\"}")
                    .build();
        }
        try {
            return Response.accepted(sessionService.enqueue(input)).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"" + e.getMessage() + "\"}")
                    .build();
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
