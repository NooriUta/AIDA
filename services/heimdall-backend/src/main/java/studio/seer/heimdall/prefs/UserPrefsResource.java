package studio.seer.heimdall.prefs;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.logging.Logger;

import java.util.Map;

/**
 * REST API for per-user UI preferences stored in FRIGG.
 *
 * Routes (proxied via Chur):
 *   GET  /api/prefs/{sub}  → fetch prefs (returns defaults if no record)
 *   PUT  /api/prefs/{sub}  → upsert prefs
 *
 * Security: this endpoint is on the internal Docker network.
 * Chur authenticates the user and only forwards requests for the
 * session owner's own sub. No admin token needed here.
 */
@Path("/api/prefs")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserPrefsResource {

    private static final Logger LOG = Logger.getLogger(UserPrefsResource.class);

    @Inject UserPrefsRepository repo;

    @GET
    @Path("/{sub}")
    public Uni<Response> getPrefs(@PathParam("sub") String sub) {
        return repo.findBySub(sub)
                .map(prefs -> Response.ok(
                        prefs != null ? prefs : UserPrefsRecord.defaults(sub)
                ).build())
                .onFailure().recoverWithItem(ex -> {
                    LOG.warnf("[PREFS] GET failed for sub=%s: %s", sub, ex.getMessage());
                    // Return defaults on FRIGG unavailability — graceful degradation
                    return Response.ok(UserPrefsRecord.defaults(sub)).build();
                });
    }

    @PUT
    @Path("/{sub}")
    public Uni<Response> putPrefs(
            @PathParam("sub") String sub,
            UserPrefsRecord body
    ) {
        // Always use the path sub (ignore any sub in body to prevent spoofing)
        UserPrefsRecord toSave = new UserPrefsRecord(
                sub,
                body.theme(),
                body.palette(),
                body.density(),
                body.uiFont(),
                body.monoFont(),
                body.fontSize()
        );
        return repo.upsert(toSave)
                .map(__ -> Response.ok(Map.of("ok", true)).build())
                .onFailure().recoverWithItem(ex -> {
                    LOG.errorf("[PREFS] PUT failed for sub=%s: %s", sub, ex.getMessage());
                    return Response.serverError()
                            .entity(Map.of("error", ex.getMessage()))
                            .build();
                });
    }
}
