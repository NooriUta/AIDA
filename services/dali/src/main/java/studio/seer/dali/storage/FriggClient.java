package studio.seer.dali.storage;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * MicroProfile REST client for FRIGG — ArcadeDB instance used by Dali.
 *
 * <p>Config key {@code dali-frigg} maps to
 * {@code quarkus.rest-client.dali-frigg.url} in application.properties.
 */
@RegisterRestClient(configKey = "dali-frigg")
@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface FriggClient {

    /** Execute a SQL/Gremlin/Cypher command within a database. */
    @POST
    @Path("/command/{db}")
    Uni<FriggResponse> command(
            @PathParam("db")              String db,
            @HeaderParam("Authorization") String authorization,
            FriggCommand                  body
    );

    /**
     * Create a database (server-level operation; ignores 500 if already exists).
     * Returns HTTP 200 on success.
     */
    @POST
    @Path("/create/{db}")
    Uni<String> createDatabase(
            @PathParam("db")              String db,
            @HeaderParam("Authorization") String authorization
    );
}
