package studio.seer.dali.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import studio.seer.dali.service.SessionService;
import studio.seer.shared.ParseSessionInput;

/**
 * REST API for Dali parse sessions.
 *
 * <pre>
 * POST  /api/sessions          — enqueue a new parse session → 202 Accepted + Session
 * GET   /api/sessions/{id}     — poll session status         → 200 OK + Session | 404
 * </pre>
 */
@Path("/api/sessions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SessionResource {

    @Inject SessionService sessionService;

    @POST
    public Response create(ParseSessionInput input) {
        if (input == null || input.dialect() == null || input.source() == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"dialect and source are required\"}")
                    .build();
        }
        return Response.accepted(sessionService.enqueue(input)).build();
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
}
