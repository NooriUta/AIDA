package studio.seer.analytics.client;

import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * MicroProfile REST Client for ArcadeDB's command + query endpoints.
 *
 * <p>Used by analytics jobs to:
 * <ol>
 *   <li>Run {@code CALL algo.*} Cypher commands and read results (pagerank scores, etc.)</li>
 *   <li>Run {@code UPDATE ... SET field = ?} to write results back into DaliTable/DaliRelation</li>
 *   <li>Run {@code ALTER TYPE ...} migrations (schema v27)</li>
 * </ol>
 *
 * <p>Base URL configured via {@code quarkus.rest-client.ygg-api.url} (shared with ANVIL).
 */
@RegisterRestClient(configKey = "ygg-api")
@Path("/api/v1")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ArcadeAnalyticsClient {

    /**
     * Execute a read-only Cypher query (e.g. SELECT, MATCH).
     * ArcadeDB 26.4.2 supports Cypher via /api/v1/query/{db}.
     */
    @POST
    @Path("/query/{db}")
    Uni<ArcadeResponse> query(
            @PathParam("db")              String db,
            @HeaderParam("Authorization") String authorization,
            ArcadeCommand                 body
    );

    /**
     * Execute a write command (UPDATE, ALTER TYPE, CALL algo.*).
     * ArcadeDB routes CALL statements to its graph algorithm engine.
     */
    @POST
    @Path("/command/{db}")
    Uni<ArcadeResponse> command(
            @PathParam("db")              String db,
            @HeaderParam("Authorization") String authorization,
            ArcadeCommand                 body
    );
}
