package studio.seer.anvil.heimdall;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import studio.seer.shared.HeimdallEvent;

@RegisterRestClient(configKey = "heimdall-api")
@Path("/api/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface HeimdallClient {

    @POST
    Uni<Void> ingest(HeimdallEvent event);
}
