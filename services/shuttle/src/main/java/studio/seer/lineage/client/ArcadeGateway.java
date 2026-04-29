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
 * EV-04: emits {@code CYPHER_QUERY_SLOW} to HEIMDALL when any SQL or Cypher
 * query against YGG takes longer than {@value SLOW_QUERY_MS} ms. The full
 * query text (truncated to {@value MAX_QUERY_CHARS} chars) is included in the
 * payload so engineers can identify expensive traversals.
 */
@ApplicationScoped
public class ArcadeGateway {

    private static final Logger log = LoggerFactory.getLogger(ArcadeGateway.class);

    /** EV-04: threshold for CYPHER_QUERY_SLOW event (ms). */
    static final long SLOW_QUERY_MS    = 500;
    /** Max chars of SQL included in HEIMDALL event payload. */
    static final int  MAX_QUERY_CHARS  = 2_000;

    @Inject
    @RestClient
    ArcadeDbClient client;

    @Inject
    HeimdallEmitter heimdall;

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
        final long start = System.currentTimeMillis();
        return client.command(database, basicAuth(), new ArcadeCommand("sql", query, params))
            .map(ArcadeResponse::result)
            .invoke(__ -> emitSlowIfNeeded(database, query, "sql", System.currentTimeMillis() - start))
            .onFailure().invoke(ex -> log.error("[ArcadeDB SQL FAILED db={}] {}: {}", database, query.lines().findFirst().orElse("?"), ex.getMessage()));
    }

    /** SHT-04: Tenant-routed Cypher — queries the specified ArcadeDB database. */
    public Uni<List<Map<String, Object>>> cypherIn(String database, String query, Map<String, Object> params) {
        log.debug("[ArcadeDB Cypher db={}] {}", database, query);
        final long start = System.currentTimeMillis();
        return client.command(database, basicAuth(), new ArcadeCommand("cypher", query, params))
            .map(ArcadeResponse::result)
            .invoke(__ -> emitSlowIfNeeded(database, query, "cypher", System.currentTimeMillis() - start))
            .onFailure().invoke(ex -> log.error("[ArcadeDB Cypher FAILED db={}] {}: {}", database, query.lines().findFirst().orElse("?"), ex.getMessage()));
    }

    /** Returns the default configured database name. */
    public String defaultDb() { return db; }

    // ── Internal ──────────────────────────────────────────────────────────────

    /**
     * EV-04: fires CYPHER_QUERY_SLOW to HEIMDALL when {@code durationMs >= SLOW_QUERY_MS}.
     * Always fire-and-forget — never throws.
     * Protected (not private) so that the CDI proxy overrides this method and reflection-based
     * tests can reach the real injected {@code heimdall} via polymorphic dispatch.
     */
    protected void emitSlowIfNeeded(String database, String query, String language, long durationMs) {
        if (durationMs < SLOW_QUERY_MS) return;
        try {
            String truncatedSql = query.length() > MAX_QUERY_CHARS
                    ? query.substring(0, MAX_QUERY_CHARS) + "…"
                    : query;
            heimdall.emit(EventType.CYPHER_QUERY_SLOW, EventLevel.WARN,
                    null, null, durationMs,
                    Map.of("db",       database,
                           "language", language,
                           "query",    truncatedSql,
                           "durationMs", durationMs));
        } catch (Exception e) {
            log.debug("[ArcadeGateway] CYPHER_QUERY_SLOW emit failed: {}", e.getMessage());
        }
    }

    private String basicAuth() {
        String credentials = user + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(
            credentials.getBytes(StandardCharsets.UTF_8)
        );
    }
}
