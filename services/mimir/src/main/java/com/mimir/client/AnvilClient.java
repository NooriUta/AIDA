package com.mimir.client;

import com.mimir.model.anvil.ImpactRequest;
import com.mimir.model.anvil.ImpactResult;
import com.mimir.model.anvil.LineageRequest;
import com.mimir.model.anvil.LineageResult;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.temporal.ChronoUnit;

/**
 * REST client to ANVIL :9095 — impact + lineage tools used by {@link com.mimir.tools.AnvilTools}.
 *
 * <p>Endpoints (defined in ANVIL_SERVICE.md):
 * <ul>
 *     <li>{@code POST /api/impact}  — downstream/upstream impact traversal</li>
 *     <li>{@code POST /api/lineage} — DATA_FLOW path traversal</li>
 * </ul>
 *
 * <p>Tenant header {@code X-Seer-Tenant-Alias} forwarded by tools.
 *
 * <p>Q-MC7 deferred: current ANVIL API uses single {@code nodeId+direction}.
 * Path-between (source→target) lineage requires new ANVIL endpoint — design session
 * after Hound work. Until then, MIMIR LLM tool prompts use direction-based traversal.
 */
@Path("/api")
@RegisterRestClient(configKey = "anvil-client")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AnvilClient {

    @POST
    @Path("/impact")
    @Timeout(value = 10, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 2, delay = 500)
    @Fallback(AnvilClientFallback.Impact.class)
    ImpactResult impact(@HeaderParam("X-Seer-Tenant-Alias") String tenant, ImpactRequest body);

    @POST
    @Path("/lineage")
    @Timeout(value = 10, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 2, delay = 500)
    @Fallback(AnvilClientFallback.Lineage.class)
    LineageResult lineage(@HeaderParam("X-Seer-Tenant-Alias") String tenant, LineageRequest body);
}
