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
     * Server-level command (ArcadeDB 26.x API).
     * Use body {@code {"command":"create database <name>"}} to create a database.
     * ArcadeDB 26.x removed the old {@code /api/v1/create/{db}} endpoint.
     */
    @POST
    @Path("/server")
    Uni<String> serverCommand(
            @HeaderParam("Authorization") String authorization,
            FriggCommand                  body
    );
}
