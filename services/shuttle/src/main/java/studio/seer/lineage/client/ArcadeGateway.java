package studio.seer.lineage.client;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.lineage.heimdall.HeimdallEmitter;
import studio.seer.lineage.heimdall.model.EventLevel;
import studio.seer.lineage.heimdall.model.EventType;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Application-scoped facade over the raw ArcadeDB REST client.
 * Handles auth header construction and provides typed query methods.
 *
 * <p>EV-04: Queries exceeding {@value SLOW_QUERY_THRESHOLD_MS}ms emit {@code CYPHER_QUERY_SLOW}
 * to HEIMDALL for performance monitoring.
 * <p>EV-05: Connection failures emit {@code DB_CONNECTION_ERROR} to HEIMDALL.
 */
@ApplicationScoped
public class ArcadeGateway {

    private static final Logger log = LoggerFactory.getLogger(ArcadeGateway.class);

    /** EV-04: Queries slower than this threshold emit CYPHER_QUERY_SLOW. */
    private static final long SLOW_QUERY_THRESHOLD_MS = 500;

    @Inject
    @RestClient
    ArcadeDbClient client;

    @Inject
    HeimdallEmitter heimdallEmitter;

    @ConfigProperty(name = "arcade.db")
    String db;

    @ConfigProperty(name = "arcade.user")
    String user;

    @ConfigProperty(name = "arcade.password")
    String password;

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * @deprecated MTN-02: Tenant-unaware overload. Routes all queries to the default
     * database regardless of the caller's tenant context. New code MUST use
     * {@link #sqlIn(String, String, Map)} with an explicit db name resolved via
     * {@code YggLineageRegistry.resourceFor(identity.tenantAlias()).databaseName()}.
     * Existing usages are tracked via ArchUnit freeze; do not add more.
     */
    @Deprecated(forRemoval = true, since = "MTN-02")
    public Uni<List<Map<String, Object>>> sql(String query) {
        return sqlIn(db, query, null);
    }

    /** @deprecated MTN-02 — see {@link #sql(String)}. */
    @Deprecated(forRemoval = true, since = "MTN-02")
    public Uni<List<Map<String, Object>>> sql(String query, Map<String, Object> params) {
        return sqlIn(db, query, params);
    }

    /** @deprecated MTN-02 — see {@link #sql(String)}. */
    @Deprecated(forRemoval = true, since = "MTN-02")
    public Uni<List<Map<String, Object>>> cypher(String query) {
        return cypherIn(db, query, null);
    }

    /** @deprecated MTN-02 — see {@link #sql(String)}. */
    @Deprecated(forRemoval = true, since = "MTN-02")
    public Uni<List<Map<String, Object>>> cypher(String query, Map<String, Object> params) {
        return cypherIn(db, query, params);
    }

    /** SHT-04: Tenant-routed SQL — queries the specified ArcadeDB database. */
    public Uni<List<Map<String, Object>>> sqlIn(String database, String query, Map<String, Object> params) {
        log.debug("[ArcadeDB SQL db={}] {}", database, query);
        long start = System.currentTimeMillis();
        return client.command(database, basicAuth(), new ArcadeCommand("sql", query, params))
            .map(ArcadeResponse::result)
            .invoke(__ -> emitSlowQueryIfNeeded("sql", database, System.currentTimeMillis() - start))
            .onFailure().invoke(ex -> {
                log.error("[ArcadeDB SQL FAILED db={}] {}: {}", database, query.lines().findFirst().orElse("?"), ex.getMessage());
                emitDbConnectionError(database, ex.getMessage()); // EV-05
            });
    }

    /** SHT-04: Tenant-routed Cypher — queries the specified ArcadeDB database. */
    public Uni<List<Map<String, Object>>> cypherIn(String database, String query, Map<String, Object> params) {
        log.debug("[ArcadeDB Cypher db={}] {}", database, query);
        long start = System.currentTimeMillis();
        return client.command(database, basicAuth(), new ArcadeCommand("cypher", query, params))
            .map(ArcadeResponse::result)
            .invoke(__ -> emitSlowQueryIfNeeded("cypher", database, System.currentTimeMillis() - start))
            .onFailure().invoke(ex -> {
                log.error("[ArcadeDB Cypher FAILED db={}] {}: {}", database, query.lines().findFirst().orElse("?"), ex.getMessage());
                emitDbConnectionError(database, ex.getMessage()); // EV-05
            });
    }

    // ── HEIMDALL helpers ──────────────────────────────────────────────────────

    /** EV-04: Emit CYPHER_QUERY_SLOW if duration exceeds threshold. */
    private void emitSlowQueryIfNeeded(String queryType, String database, long durationMs) {
        if (durationMs > SLOW_QUERY_THRESHOLD_MS) {
            log.warn("[ArcadeDB SLOW db={} type={}] {}ms > {}ms threshold", database, queryType, durationMs, SLOW_QUERY_THRESHOLD_MS);
            heimdallEmitter.emit(EventType.CYPHER_QUERY_SLOW, EventLevel.WARN, null, null, durationMs, Map.of(
                    "query_type",    queryType,
                    "duration_ms",   durationMs,
                    "threshold_ms",  SLOW_QUERY_THRESHOLD_MS,
                    "db",            database));
        }
    }

    /** EV-05: Emit DB_CONNECTION_ERROR on ArcadeDB connectivity failure. */
    private void emitDbConnectionError(String database, String error) {
        heimdallEmitter.emit(EventType.DB_CONNECTION_ERROR, EventLevel.ERROR, null, null, 0, Map.of(
                "db",    "ygg",
                "host",  database,
                "error", error != null ? error : "unknown"));
    }

    /** Returns the default configured database name. */
    public String defaultDb() { return db; }

    // ── Internal ──────────────────────────────────────────────────────────────

    private String basicAuth() {
        String credentials = user + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(
            credentials.getBytes(StandardCharsets.UTF_8)
        );
    }
}
