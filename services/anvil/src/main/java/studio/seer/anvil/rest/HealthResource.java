package studio.seer.anvil.rest;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import studio.seer.anvil.model.YggCommand;

import java.util.Map;

/**
 * GET /api/health — YGG connectivity check + basic service info.
 */
@Path("/api/health")
@Produces(MediaType.APPLICATION_JSON)
public class HealthResource {

    @Inject
    @RestClient
    YggQueryClient ygg;

    @ConfigProperty(name = "ygg.db", defaultValue = "hound_default")
    String defaultDb;

    @ConfigProperty(name = "ygg.user", defaultValue = "root")
    String yggUser;

    @ConfigProperty(name = "ygg.password", defaultValue = "playwithdata")
    String yggPassword;

    @GET
    public Response health() {
        String yggStatus = "ok";
        try {
            String auth = "Basic " + java.util.Base64.getEncoder()
                    .encodeToString((yggUser + ":" + yggPassword).getBytes());
            ygg.query(defaultDb, auth, new YggCommand("sql", "SELECT count(*) FROM DaliTable LIMIT 1", Map.of()))
               .await().indefinitely();
        } catch (Exception e) {
            yggStatus = "error: " + e.getMessage();
        }
        return Response.ok(Map.of(
                "status",    "ok".equals(yggStatus) ? "ok" : "degraded",
                "ygg",       yggStatus,
                "cacheSize", 0
        )).build();
    }
}
