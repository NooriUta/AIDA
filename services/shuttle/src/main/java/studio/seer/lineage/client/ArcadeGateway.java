package studio.seer.lineage.client;

import io.smallrye.mutiny.Uni;
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
 * Application-scoped facade over the raw ArcadeDB REST client.
 * Handles auth header construction and provides typed query methods.
 */
@ApplicationScoped
public class ArcadeGateway {

    private static final Logger log = LoggerFactory.getLogger(ArcadeGateway.class);

    @Inject
    @RestClient
    ArcadeDbClient client;

    @ConfigProperty(name = "arcade.db")
    String db;

    @ConfigProperty(name = "arcade.user")
    String user;

    @ConfigProperty(name = "arcade.password")
    String password;

    // ── Public API ────────────────────────────────────────────────────────────

    public Uni<List<Map<String, Object>>> sql(String query) {
        return sqlIn(db, query, null);
    }

    public Uni<List<Map<String, Object>>> sql(String query, Map<String, Object> params) {
        return sqlIn(db, query, params);
    }

    public Uni<List<Map<String, Object>>> cypher(String query) {
        return cypherIn(db, query, null);
    }

    public Uni<List<Map<String, Object>>> cypher(String query, Map<String, Object> params) {
        return cypherIn(db, query, params);
    }

    /** SHT-04: Tenant-routed SQL — queries the specified ArcadeDB database. */
    public Uni<List<Map<String, Object>>> sqlIn(String database, String query, Map<String, Object> params) {
        log.debug("[ArcadeDB SQL db={}] {}", database, query);
        return client.command(database, basicAuth(), new ArcadeCommand("sql", query, params))
            .map(ArcadeResponse::result)
            .onFailure().invoke(ex -> log.error("[ArcadeDB SQL FAILED db={}] {}: {}", database, query.lines().findFirst().orElse("?"), ex.getMessage()));
    }

    /** SHT-04: Tenant-routed Cypher — queries the specified ArcadeDB database. */
    public Uni<List<Map<String, Object>>> cypherIn(String database, String query, Map<String, Object> params) {
        log.debug("[ArcadeDB Cypher db={}] {}", database, query);
        return client.command(database, basicAuth(), new ArcadeCommand("cypher", query, params))
            .map(ArcadeResponse::result)
            .onFailure().invoke(ex -> log.error("[ArcadeDB Cypher FAILED db={}] {}: {}", database, query.lines().findFirst().orElse("?"), ex.getMessage()));
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
