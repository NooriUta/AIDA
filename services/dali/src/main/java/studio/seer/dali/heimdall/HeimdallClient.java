package studio.seer.dali.heimdall;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import studio.seer.shared.HeimdallEvent;

/**
 * MicroProfile REST client for HEIMDALL backend event ingestion.
 *
 * <p>Config key {@code heimdall-api} maps to
 * {@code quarkus.rest-client.heimdall-api.url} in {@code application.properties}.
 */
@RegisterRestClient(configKey = "heimdall-api")
@Path("/api")
@Consumes(MediaType.APPLICATION_JSON)
public interface HeimdallClient {

    @POST
    @Path("/events")
    Uni<Void> ingest(HeimdallEvent event);
}
