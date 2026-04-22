package studio.seer.heimdall.snapshot;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * MicroProfile REST client for FRIGG — ArcadeDB :2481 dev / frigg:2480 Docker.
 * Intentionally NOT connected to HoundArcade (:2480).
 */
@RegisterRestClient(configKey = "frigg-api")
@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface FriggClient {

    @POST
    @Path("/command/{db}")
    Uni<FriggResponse> command(
            @PathParam("db")              String db,
            @HeaderParam("Authorization") String authorization,
            FriggCommand                  body
    );

    /**
     * Creates an ArcadeDB database.
     * Returns 200 {"result":"ok"} if created, 500 if it already exists — callers should
     * swallow errors from this endpoint and treat both outcomes as success.
     */
    @POST
    @Path("/create/{db}")
    Uni<String> createDatabase(
            @PathParam("db")              String db,
            @HeaderParam("Authorization") String authorization
    );
}
