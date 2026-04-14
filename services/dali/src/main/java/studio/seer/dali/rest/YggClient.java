package studio.seer.dali.rest;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import studio.seer.dali.storage.FriggCommand;
import studio.seer.dali.storage.FriggResponse;

/**
 * MicroProfile REST client for YGG — ArcadeDB instance used for lineage storage (hound schema).
 *
 * <p>Config key {@code ygg-api} maps to
 * {@code quarkus.rest-client.ygg-api.url} in application.properties.
 */
@RegisterRestClient(configKey = "ygg-api")
@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface YggClient {

    @POST
    @Path("/command/{db}")
    Uni<FriggResponse> command(
            @PathParam("db")              String db,
            @HeaderParam("Authorization") String authorization,
            FriggCommand                  body
    );
}
