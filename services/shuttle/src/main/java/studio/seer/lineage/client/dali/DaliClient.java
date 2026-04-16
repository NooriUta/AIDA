package studio.seer.lineage.client.dali;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import studio.seer.lineage.client.dali.model.*;

import jakarta.ws.rs.ProcessingException;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * MicroProfile REST client for Dali's {@code /api} endpoints.
 *
 * <p>Configured via {@code quarkus.rest-client.dali-client.url} (default: http://localhost:9090).
 * All mutating methods have a {@link DaliClientFallback} so SHUTTLE degrades gracefully
 * when Dali is unreachable — mutations return an UNAVAILABLE stub instead of a 500.
 */
@RegisterRestClient(configKey = "dali-client")
@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface DaliClient {

    /**
     * Enqueue a new parse session.
     * → 202 Accepted + SessionInfo{status=QUEUED}
     */
    @POST
    @Path("/sessions")
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 2, delay = 500, delayUnit = ChronoUnit.MILLIS,
           retryOn = ProcessingException.class)
    @Fallback(DaliClientFallback.class)
    SessionInfo createSession(DaliParseSessionInput input);

    /**
     * Poll session status.
     * → 200 + SessionInfo | 404
     */
    @GET
    @Path("/sessions/{id}")
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 3, delay = 200, delayUnit = ChronoUnit.MILLIS,
           retryOn = ProcessingException.class)
    @Fallback(DaliClientFallback.class)
    SessionInfo getSession(@PathParam("id") String id);

    /**
     * Cancel a session.
     * → 202 {status: CANCELLING} | 404 | 409
     */
    @POST
    @Path("/sessions/{id}/cancel")
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 1)
    @Fallback(DaliClientFallback.class)
    CancelResponse cancelSession(@PathParam("id") String id);

    /**
     * List recent sessions.
     */
    @GET
    @Path("/sessions")
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @Fallback(DaliClientFallback.class)
    List<SessionInfo> listSessions(@QueryParam("limit") @DefaultValue("50") int limit);

    /**
     * YGG vertex/edge statistics.
     */
    @GET
    @Path("/ygg/stats")
    @Timeout(value = 3, unit = ChronoUnit.SECONDS)
    @Fallback(DaliClientFallback.class)
    DaliStats getStats();

    /**
     * Dali health check (FRIGG + sessions).
     */
    @GET
    @Path("/sessions/health")
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @Fallback(DaliClientFallback.class)
    DaliHealth getHealth();
}
