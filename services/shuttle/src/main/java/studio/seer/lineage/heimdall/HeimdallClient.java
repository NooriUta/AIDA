package studio.seer.lineage.heimdall;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import studio.seer.lineage.heimdall.model.HeimdallEvent;

/**
 * MicroProfile REST client for HEIMDALL event ingestion.
 * Configured via quarkus.rest-client.heimdall-api.url in application.properties.
 */
@RegisterRestClient(configKey = "heimdall-api")
@Path("/events")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface HeimdallClient {

    @POST
    Uni<Response> send(HeimdallEvent event);
}
