package studio.seer.dali.storage;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     */
    public List<Map<String, Object>> sql(String query) {
        log.debug("[FRIGG] {}", query);
        return client.command(db, basicAuth(), new FriggCommand("sql", query, null))
                .map(FriggResponse::result)
                .onFailure().invoke(ex -> log.error("[FRIGG FAILED] {} — {}", query, ex.getMessage()))
                .await().atMost(TIMEOUT);
    }

    /**
     * Executes a SQL query with named parameters.
     */
    public List<Map<String, Object>> sql(String query, Map<String, Object> params) {
        log.debug("[FRIGG] {}", query);
        return client.command(db, basicAuth(), new FriggCommand("sql", query, params))
                .map(FriggResponse::result)
                .onFailure().invoke(ex -> log.error("[FRIGG FAILED] {} — {}", query, ex.getMessage()))
                .await().atMost(TIMEOUT);
    }

    /**
     * Creates the FRIGG database if it doesn't already exist.
     * Ignores errors (e.g. database already exists).
     */
    public void ensureDatabase() {
        try {
            client.createDatabase(db, basicAuth()).await().atMost(TIMEOUT);
            log.info("[FRIGG] database '{}' created (or already existed)", db);
        } catch (Exception e) {
            log.debug("[FRIGG] ensureDatabase — ignored: {}", e.getMessage());
        }
    }

    private String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (user + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
