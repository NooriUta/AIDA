package studio.seer.dali.storage;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.ws.rs.WebApplicationException;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Synchronous facade over {@link FriggClient}.
 *
 * <p>Job code and startup initializers call blocking methods here.
 * All calls timeout after 10 seconds.
 */
@ApplicationScoped
public class FriggGateway {

    private static final Logger log = LoggerFactory.getLogger(FriggGateway.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Inject @RestClient FriggClient client;

    @ConfigProperty(name = "frigg.db")       String db;
    @ConfigProperty(name = "frigg.user")     String user;
    @ConfigProperty(name = "frigg.password") String password;

    /**
     * Executes a SQL query and returns the result rows.
     *
     * <p>BUG-SS-041: automatically retries once on {@link java.io.IOException}
     * ("Connection was closed") which occurs when ArcadeDB drops an idle connection
     * from the pool before the client-side TTL expires.
     */
    public List<Map<String, Object>> sql(String query) {
        log.debug("[FRIGG] {}", query);
        try {
            var result = client.command(db, basicAuth(), new FriggCommand("sql", query, null))
                    .map(FriggResponse::result)
                    .onFailure(java.io.IOException.class).retry().atMost(1)
                    .await().atMost(TIMEOUT);
            return result != null ? result : List.of();
        } catch (Exception ex) {
            log.error("[FRIGG FAILED] {} — {}", query, ex.getMessage());
            throw ex;
        }
    }

    /**
     * Executes a SQL query with named parameters.
     *
     * <p>BUG-SS-041: automatically retries once on {@link java.io.IOException}.
     */
    public List<Map<String, Object>> sql(String query, Map<String, Object> params) {
        log.debug("[FRIGG] {}", query);
        try {
            var result = client.command(db, basicAuth(), new FriggCommand("sql", query, params))
                    .map(FriggResponse::result)
                    .onFailure(java.io.IOException.class).retry().atMost(1)
                    .await().atMost(TIMEOUT);
            return result != null ? result : List.of();
        } catch (Exception ex) {
            log.error("[FRIGG FAILED] {} params={} — {}", query, params.keySet(), ex.getMessage());
            throw ex;
        }
    }

    /**
     * Quick health check — returns true if FRIGG responds to a simple query.
     */
    public boolean ping() {
        try {
            sql("SELECT 1");
            return true;
        } catch (Exception e) {
            log.debug("[FRIGG] ping failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Creates the FRIGG database if it doesn't already exist.
     *
     * @return {@code true} if the database exists and is usable (created or already present);
     *         {@code false} if creation failed (auth error, network issue, etc.)
     */
    public boolean ensureDatabase() {
        // ArcadeDB 26.x uses POST /api/v1/server with a command body.
        // The old /api/v1/create/{db} endpoint was removed in 26.x.
        FriggCommand cmd = new FriggCommand(null, "create database " + db, null);
        try {
            client.serverCommand(basicAuth(), cmd).await().atMost(TIMEOUT);
            log.info("[FRIGG] database '{}' created", db);
            return true;
        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();
            if (status == 500) {
                // ArcadeDB returns 500 when the database already exists — treat as success
                log.info("[FRIGG] database '{}' already exists (HTTP 500 from /server)", db);
                return true;
            }
            log.warn("[FRIGG] ensureDatabase failed — HTTP {}: {}", status, e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("[FRIGG] ensureDatabase failed (connection issue?): {}", e.getMessage());
            return false;
        }
    }

    private String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (user + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
