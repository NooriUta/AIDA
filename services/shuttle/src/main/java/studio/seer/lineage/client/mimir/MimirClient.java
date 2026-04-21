package studio.seer.lineage.client.mimir;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import studio.seer.lineage.client.mimir.model.AskInput;
import studio.seer.lineage.client.mimir.model.MimirAnswer;

import jakarta.ws.rs.ProcessingException;
import java.time.temporal.ChronoUnit;

/**
 * MicroProfile REST client for MIMIR's {@code /api} endpoints.
 *
 * <p>Configured via {@code quarkus.rest-client.mimir-client.url} (default: http://localhost:9094).
 * Fault tolerance: {@code @Timeout} and {@code @Retry} on methods; fallback handling done
 * reactively in {@link studio.seer.lineage.resource.MutationResource} via Mutiny
 * {@code .onFailure().recoverWithItem()}.
 */
@RegisterRestClient(configKey = "mimir-client")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface MimirClient {

    @POST
    @Path("/ask")
    @Timeout(value = 10, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 2, delay = 500, delayUnit = ChronoUnit.MILLIS,
           retryOn = ProcessingException.class)
    MimirAnswer ask(@HeaderParam("X-Seer-Tenant-Alias") String tenantAlias, AskInput input);

    @DELETE
    @Path("/sessions/{id}")
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    void deleteSession(@PathParam("id") String sessionId);
}
