package com.mimir.persistence;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

/**
 * REST client to FRIGG :2481 — used by {@link MimirSessionRepository}.
 *
 * <p>FRIGG = ArcadeDB on :2481 (separate instance from YGG on :2480).
 * Auth: same root/playwithdata pattern via {@link com.mimir.client.ArcadeDbAuthFilter}.
 *
 * <p>Note: {@link com.mimir.client.ArcadeDbAuthFilter} reads {@code mimir.ygg.user/password}
 * — for FRIGG we'd ideally want separate {@code mimir.frigg.user/password}, но в проде
 * ARCADEDB_ADMIN_PASSWORD один на оба instance. CORE: переиспользуем same auth filter.
 * MT-06 BYOK расширит до tenant-scoped credentials.
 */
@Path("/api/v1")
@RegisterRestClient(configKey = "frigg-client")
@RegisterProvider(com.mimir.client.ArcadeDbAuthFilter.class)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface FriggClient {

    @POST
    @Path("/query/{db}")
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 1, delay = 200)
    QueryResult query(@PathParam("db") String dbName, FriggQuery body);

    @POST
    @Path("/command/{db}")
    @Timeout(value = 5, unit = ChronoUnit.SECONDS)
    @Retry(maxRetries = 1, delay = 200)
    QueryResult command(@PathParam("db") String dbName, FriggCommand body);

    /** Result envelope from ArcadeDB REST. */
    record QueryResult(List<Map<String, Object>> result) {}

    /** Read-only query body — for SELECT / MATCH / TRAVERSE. */
    record FriggQuery(String language, String command, Map<String, Object> params) {
        public FriggQuery(String language, String command) { this(language, command, Map.of()); }
    }

    /** Mutation body — for INSERT / UPDATE / UPSERT / DELETE / CREATE TYPE. */
    record FriggCommand(String language, String command, Map<String, Object> params) {
        public FriggCommand(String language, String command) { this(language, command, Map.of()); }
    }
}
