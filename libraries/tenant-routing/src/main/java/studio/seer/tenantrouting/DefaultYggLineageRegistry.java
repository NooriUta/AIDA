package studio.seer.tenantrouting;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Single-tenant (HighLoad demo) implementation of {@link YggLineageRegistry}.
 *
 * Routes ALL tenant aliases to the same {@code hound_default} database.
 * Post-HL: replaced by {@link FriggYggLineageRegistry} which reads routing
 * from {@code DaliTenantConfig} in {@code frigg-tenants}.
 *
 * Config properties:
 *   ygg.lineage.url  — ArcadeDB base URL (default: ${ygg.url:http://localhost:2480})
 *   ygg.lineage.db   — database name    (default: hound_default)
 *   ygg.user         — username         (default: root)
 *   ygg.password     — password
 */
@ApplicationScoped
public class DefaultYggLineageRegistry implements YggLineageRegistry {

    private static final Logger log = LoggerFactory.getLogger(DefaultYggLineageRegistry.class);

    @ConfigProperty(name = "ygg.lineage.url", defaultValue = "http://localhost:2480")
    String yggLineageUrl;

    @ConfigProperty(name = "ygg.lineage.db", defaultValue = "hound_default")
    String yggLineageDb;

    @ConfigProperty(name = "ygg.user", defaultValue = "root")
    String yggUser;

    @ConfigProperty(name = "ygg.password", defaultValue = "playwithdata")
    String yggPassword;

    private HttpArcadeConnection connection;

    @PostConstruct
    void init() {
        var client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        connection = new HttpArcadeConnection(client, yggLineageUrl, yggLineageDb,
                yggUser, yggPassword);
        log.info("YggLineageRegistry → {}  db={}", yggLineageUrl, yggLineageDb);
    }

    @Override
    public ArcadeConnection resourceFor(String tenantAlias) {
        // Single-pool: all tenants → hound_default (single-tenant HighLoad demo)
        return connection;
    }

    @Override
    public void invalidate(String tenantAlias) {
        // No-op: single shared connection, no per-tenant state
    }

    @Override
    public void invalidateAll() {
        // No-op
    }
}
