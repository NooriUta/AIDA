package com.mimir.heimdall;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import studio.seer.shared.HeimdallEvent;

/**
 * Fire-and-forget client to HEIMDALL Backend :9093.
 * Same contract as services/anvil/heimdall/HeimdallClient.
 */
@Path("/api/events")
@RegisterRestClient(configKey = "heimdall-api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface HeimdallClient {

    @POST
    Uni<Void> ingest(HeimdallEvent event);
}
