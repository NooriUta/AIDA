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
        return sql(query, null);
    }

    public Uni<List<Map<String, Object>>> sql(String query, Map<String, Object> params) {
        log.debug("[ArcadeDB SQL] {}", query);
        return client.command(db, basicAuth(), new ArcadeCommand("sql", query, params))
            .map(ArcadeResponse::result)
            .onFailure().invoke(ex -> log.error("[ArcadeDB SQL FAILED] {}: {}", query.lines().findFirst().orElse("?"), ex.getMessage()));
    }

    public Uni<List<Map<String, Object>>> cypher(String query) {
        return cypher(query, null);
    }

    public Uni<List<Map<String, Object>>> cypher(String query, Map<String, Object> params) {
        log.debug("[ArcadeDB Cypher] {}", query);
        return client.command(db, basicAuth(), new ArcadeCommand("cypher", query, params))
            .map(ArcadeResponse::result)
            .onFailure().invoke(ex -> log.error("[ArcadeDB Cypher FAILED] {}: {}", query.lines().findFirst().orElse("?"), ex.getMessage()));
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private String basicAuth() {
        String credentials = user + ":" + password;
        return "Basic " + Base64.getEncoder().encodeToString(
            credentials.getBytes(StandardCharsets.UTF_8)
        );
    }
}
