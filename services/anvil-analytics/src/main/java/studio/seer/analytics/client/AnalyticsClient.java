package studio.seer.analytics.client;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Application-scoped facade over {@link ArcadeAnalyticsClient}.
 *
 * <p>Handles Basic-Auth header construction and provides
 * convenience methods for the analytics jobs.
 *
 * <p>All methods are synchronous (blocking) — called from @Scheduled jobs
 * running on dedicated threads, not on Vert.x event loop.
 *
 * @see PageRankJob
 * @see BridgesJob
 */
@ApplicationScoped
public class AnalyticsClient {

    private static final Logger LOG = LoggerFactory.getLogger(AnalyticsClient.class);

    @Inject
    @RestClient
    ArcadeAnalyticsClient client;

    @ConfigProperty(name = "ygg.db")
    String db;

    @ConfigProperty(name = "ygg.user")
    String user;

    @ConfigProperty(name = "ygg.password")
    String password;

    // ── Cached Basic-Auth header ──────────────────────────────────────────────

    private volatile String authHeader;

    private String auth() {
        if (authHeader == null) {
            authHeader = "Basic " + Base64.getEncoder()
                    .encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
        }
        return authHeader;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Execute a Cypher write command (UPDATE, CALL algo.*, ALTER TYPE, etc.)
     * and return the result rows (may be empty for DDL).
     *
     * @param cypher Cypher statement
     * @return result rows (empty list for DDL / no YIELD)
     */
    public List<Map<String, Object>> command(String cypher) {
        LOG.debug("[analytics] command: {}", cypher.length() > 120 ? cypher.substring(0, 120) + "…" : cypher);
        ArcadeResponse resp = client.command(db, auth(), new ArcadeCommand("cypher", cypher))
                .await().indefinitely();
        return resp != null && resp.result() != null ? resp.result() : List.of();
    }

    /**
     * Execute a Cypher read query and return result rows.
     *
     * @param cypher SELECT/MATCH Cypher
     * @return result rows
     */
    public List<Map<String, Object>> query(String cypher) {
        LOG.debug("[analytics] query: {}", cypher.length() > 120 ? cypher.substring(0, 120) + "…" : cypher);
        ArcadeResponse resp = client.query(db, auth(), new ArcadeCommand("cypher", cypher))
                .await().indefinitely();
        return resp != null && resp.result() != null ? resp.result() : List.of();
    }

    /**
     * Execute a DDL statement (ALTER TYPE). Logs the statement at INFO level
     * since schema migrations are significant operational events.
     *
     * @param ddl ALTER TYPE statement
     */
    public void ddl(String ddl) {
        LOG.info("[analytics] DDL: {}", ddl);
        command(ddl);
    }

    /** Returns the configured ArcadeDB database name. */
    public String dbName() {
        return db;
    }
}
