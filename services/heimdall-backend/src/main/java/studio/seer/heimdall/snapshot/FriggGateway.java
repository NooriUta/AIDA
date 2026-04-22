package studio.seer.heimdall.snapshot;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Application-scoped facade over FriggClient.
 * Mirrors the ArcadeGateway pattern from SHUTTLE.
 */
@ApplicationScoped
public class FriggGateway {

    private static final Logger LOG = Logger.getLogger(FriggGateway.class);

    @Inject @RestClient FriggClient client;

    @ConfigProperty(name = "frigg.db")           String db;
    @ConfigProperty(name = "frigg.tenants-db",
                    defaultValue = "frigg-tenants") String tenantsDb;
    @ConfigProperty(name = "frigg.user")          String user;
    @ConfigProperty(name = "frigg.password")      String password;

    public Uni<List<Map<String, Object>>> sql(String query, Map<String, Object> params) {
        return sqlOn(db, query, params);
    }

    /** Query against frigg-tenants (DaliTenantConfig, ServiceAccount, ApiKey). */
    public Uni<List<Map<String, Object>>> sqlTenants(String query, Map<String, Object> params) {
        return sqlOn(tenantsDb, query, params);
    }

    private Uni<List<Map<String, Object>>> sqlOn(String database, String query, Map<String, Object> params) {
        LOG.debugf("[FRIGG:%s] %s", database, query);
        return client.command(database, basicAuth(), new FriggCommand("sql", query, params))
                .map(FriggResponse::result)
                .onFailure().invoke(ex ->
                        LOG.errorf("[FRIGG FAILED] %s: %s", query, ex.getMessage()));
    }

    private String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (user + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
