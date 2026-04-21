package studio.seer.lineage.client.anvil;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import studio.seer.lineage.client.anvil.model.AnvilQueryResponse;
import studio.seer.lineage.client.anvil.model.ImpactRequest;
import studio.seer.lineage.client.anvil.model.ImpactResult;
import studio.seer.lineage.client.anvil.model.QueryRequest;

import jakarta.ws.rs.ProcessingException;
import java.time.temporal.ChronoUnit;

/**
 * MicroProfile REST client for ANVIL's {@code /api} endpoints.
 *
 * <p>Configured via {@code quarkus.rest-client.anvil-client.url} (default: http://localhost:9095).
 * Fault tolerance: {@code @Timeout} and {@code @Retry} on methods; fallback handling done
 * reactively in {@link studio.seer.lineage.resource.MutationResource} via Mutiny
 * {@code .onFailure().recoverWithItem()}.
 */
@RegisterRestClient(configKey = "anvil-client")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AnvilClient {

    @POST
    @Path("/impact")
    @Timeout(value = 15, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 2, delay = 500, delayUnit = ChronoUnit.MILLIS,
           retryOn = ProcessingException.class)
    ImpactResult findImpact(
            @HeaderParam("X-Seer-Tenant-Alias") String tenantAlias,
            ImpactRequest request);

    @POST
    @Path("/query")
    @Timeout(value = 10, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 1, delay = 300, delayUnit = ChronoUnit.MILLIS,
           retryOn = ProcessingException.class)
    AnvilQueryResponse executeQuery(
            @HeaderParam("X-Seer-Tenant-Alias") String tenantAlias,
            QueryRequest request);
}
