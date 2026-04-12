package studio.seer.lineage.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "arcade")
@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ArcadeDbClient {

    @POST
    @Path("/command/{db}")
    Uni<ArcadeResponse> command(
        @PathParam("db") String db,
        @HeaderParam("Authorization") String authorization,
        ArcadeCommand body
    );

    @GET
    @Path("/database/{db}")
    Uni<Object> databaseInfo(
        @PathParam("db") String db,
        @HeaderParam("Authorization") String authorization
    );
}
