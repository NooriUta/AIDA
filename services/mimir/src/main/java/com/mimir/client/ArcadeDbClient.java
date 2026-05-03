package com.mimir.client;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.temporal.ChronoUnit;
import java.util.Map;

/**
 * REST client to ArcadeDB :2480 — used by ShuttleTools (search) and YggTools (procedure source / count).
 *
 * <p>Authentication: HTTP Basic auth via {@link ArcadeDbReadOnlyAuthFilter}
 * — uses the {@code mimir_ro} ArcadeDB user (reader role) so even malformed
 * tool queries cannot mutate the metadata graph. Defence at the DB layer
 * complements the read-only intent of every method below.
 *
 * <p>Endpoints used:
 * <ul>
 *     <li>{@code POST /api/v1/query/{db}}  — Cypher / SQL queries (read-only)</li>
 * </ul>
 *
 * <p>Tenant isolation: dbName is per-tenant ({@code hound_{alias}}), resolved by {@link com.mimir.tenant.DbNameResolver}.
 */
@Path("/api/v1")
@RegisterRestClient(configKey = "arcadedb-client")
@RegisterProvider(ArcadeDbReadOnlyAuthFilter.class)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface ArcadeDbClient {

    @POST
    @Path("/query/{db}")
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 1, delay = 200)
    QueryResult query(@PathParam("db") String dbName, ArcadeQuery body);

    /** Result envelope from ArcadeDB REST. */
    record QueryResult(java.util.List<Map<String, Object>> result) {}

    /** Query body. {@code language} = "cypher" | "sql" | "gremlin". */
    record ArcadeQuery(String language, String command, Map<String, Object> params) {
        public ArcadeQuery(String language, String command) {
            this(language, command, Map.of());
        }
    }
}
