package studio.seer.lineage.heimdall;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * MicroProfile REST client for HEIMDALL control plane.
 * Separate from HeimdallClient (event ingestion) because base paths differ.
 * Configured via quarkus.rest-client.heimdall-api.url (shared base URL).
 */
@RegisterRestClient(configKey = "heimdall-api")
@Produces(MediaType.APPLICATION_JSON)
public interface HeimdallControlClient {

    @POST
    @Path("/control/reset")
    Uni<Response> reset(@HeaderParam("X-Seer-Role") String role);
}
