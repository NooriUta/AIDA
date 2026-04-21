package studio.seer.anvil.rest;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import studio.seer.anvil.model.YggCommand;
import studio.seer.anvil.model.YggResponse;

@RegisterRestClient(configKey = "ygg-api")
@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface YggQueryClient {

    @POST
    @Path("/query/{db}")
    Uni<YggResponse> query(
            @PathParam("db")              String db,
            @HeaderParam("Authorization") String authorization,
            YggCommand                    body
    );

    @POST
    @Path("/command/{db}")
    Uni<YggResponse> command(
            @PathParam("db")              String db,
            @HeaderParam("Authorization") String authorization,
            YggCommand                    body
    );
}
