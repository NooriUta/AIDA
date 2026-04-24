package studio.seer.tenantrouting;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.time.Duration;

/**
 * Single-tenant (HighLoad demo) implementation of {@link YggSourceArchiveRegistry}.
 * Routes all aliases to {@code hound_src_default}.
 *
 * Config properties:
 *   ygg.source.url  — ArcadeDB base URL (default: ${ygg.url:http://localhost:2480})
 *   ygg.source.db   — database name     (default: hound_src_default)
 *   ygg.user        — username
 *   ygg.password    — password
 */
@ApplicationScoped
public class DefaultYggSourceArchiveRegistry implements YggSourceArchiveRegistry {

    private static final Logger log = LoggerFactory.getLogger(DefaultYggSourceArchiveRegistry.class);

    @ConfigProperty(name = "ygg.source.url", defaultValue = "http://localhost:2480")
    String yggSourceUrl;

    @ConfigProperty(name = "ygg.source.db", defaultValue = "hound_src_default")
    String yggSourceDb;

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
        connection = new HttpArcadeConnection(client, yggSourceUrl, yggSourceDb,
                yggUser, yggPassword);
        log.info("YggSourceArchiveRegistry → {}  db={}", yggSourceUrl, yggSourceDb);
    }

    @Override
    public ArcadeConnection resourceFor(String tenantAlias) {
        return connection;
    }

    @Override
    public void invalidate(String tenantAlias) { }

    @Override
    public void invalidateAll() { }
}
