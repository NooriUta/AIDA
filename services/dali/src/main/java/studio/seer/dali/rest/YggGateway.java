package studio.seer.dali.rest;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.storage.FriggCommand;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

/**
 * Synchronous facade over {@link YggClient} for server-level operations against YGG (ArcadeDB).
 *
 * <p>Used exclusively by {@link YggSchemaInitializer} at startup to ensure the {@code hound}
 * database exists before any parse sessions try to write lineage data.
 */
@ApplicationScoped
public class YggGateway {

    private static final Logger log = LoggerFactory.getLogger(YggGateway.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Inject @RestClient YggClient client;

    @ConfigProperty(name = "ygg.db")       String db;
    @ConfigProperty(name = "ygg.user")     String user;
    @ConfigProperty(name = "ygg.password") String password;

    /**
     * Creates the YGG (hound) database if it doesn't already exist.
     *
     * <p>Uses the ArcadeDB 26.x {@code POST /api/v1/server} endpoint.
     * The old {@code /api/v1/create/{db}} endpoint was removed in 26.x.
     *
     * @return {@code true} if the database exists and is usable (created or already present);
     *         {@code false} if creation failed (auth error, network issue, etc.)
     */
    public boolean ensureDatabase() {
        return ensureDatabaseNamed(db);
    }

    /**
     * Creates a named YGG database if it doesn't already exist.
     * Used by {@link studio.seer.dali.rest.YggSchemaInitializer} to ensure the correct
     * lineage/source-archive databases ({@code hound_default}, {@code hound_src_default})
     * exist, rather than always checking the config-default {@code hound} database.
     *
     * @return {@code true} if the database exists or was created; {@code false} on error.
     */
    public boolean ensureDatabaseNamed(String dbName) {
        FriggCommand cmd = new FriggCommand(null, "create database " + dbName, null);
        try {
            client.serverCommand(basicAuth(), cmd).await().atMost(TIMEOUT);
            log.info("[YGG] database '{}' created", dbName);
            return true;
        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();
            if (status == 500 || status == 400) {
                // ArcadeDB returns 500 or 400 when the database already exists — treat as success
                log.info("[YGG] database '{}' already exists (HTTP {})", dbName, status);
                return true;
            }
            log.warn("[YGG] ensureDatabaseNamed({}) failed — HTTP {}: {}", dbName, status, e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("[YGG] ensureDatabaseNamed({}) failed (connection issue?): {}", dbName, e.getMessage());
            return false;
        }
    }

    private String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (user + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
