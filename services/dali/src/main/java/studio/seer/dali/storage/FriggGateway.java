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
     * Executes a SQL query in the default configured FRIGG database.
     * BUG-SS-041: retries once on IOException (stale ArcadeDB connection).
     */
    public List<Map<String, Object>> sql(String query) {
        return sqlIn(db, query, null);
    }

    /** Executes a SQL query with named parameters in the default FRIGG database. */
    public List<Map<String, Object>> sql(String query, Map<String, Object> params) {
        return sqlIn(db, query, params);
    }

    /** Executes a SQL query in an explicitly named FRIGG database (multi-tenant). */
    public List<Map<String, Object>> sqlIn(String database, String query) {
        return sqlIn(database, query, null);
    }

    /** Executes a SQL query with named parameters in an explicitly named FRIGG database. */
    public List<Map<String, Object>> sqlIn(String database, String query, Map<String, Object> params) {
        log.debug("[FRIGG:{}] {}", database, query);
        try {
            var result = client.command(database, basicAuth(), new FriggCommand("sql", query, params))
                    .map(FriggResponse::result)
                    .onFailure(java.io.IOException.class).retry().atMost(1)
                    .await().atMost(TIMEOUT);
            return result != null ? result : List.of();
        } catch (Exception ex) {
            log.error("[FRIGG:{}] FAILED {} — {}", database, query, ex.getMessage());
            throw ex;
        }
    }

    /** Quick health check — returns true if FRIGG responds to a simple query. */
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
     * Creates the default FRIGG database (from config) if it doesn't already exist.
     * Returns true if usable, false on failure.
     */
    public boolean ensureDatabase() {
        return ensureDatabaseNamed(db);
    }

    /**
     * Creates a named FRIGG database if it doesn't already exist.
     * Used by FriggSchemaInitializer.ensureSchema(tenantAlias) for tenant DBs.
     */
    public boolean ensureDatabaseNamed(String dbName) {
        FriggCommand cmd = new FriggCommand(null, "create database " + dbName, null);
        try {
            client.serverCommand(basicAuth(), cmd).await().atMost(TIMEOUT);
            log.info("[FRIGG] database '{}' created", dbName);
            return true;
        } catch (WebApplicationException e) {
            int status = e.getResponse().getStatus();
            if (status == 500 || status == 400) {
                log.info("[FRIGG] database '{}' already exists (HTTP {})", dbName, status);
                return true;
            }
            log.warn("[FRIGG] ensureDatabaseNamed({}) failed — HTTP {}: {}", dbName, status, e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("[FRIGG] ensureDatabaseNamed({}) failed: {}", dbName, e.getMessage());
            return false;
        }
    }

    /** Computes the FRIGG database name for a given tenant alias. */
    public static String tenantDb(String tenantAlias) {
        return "dali_" + tenantAlias;
    }

    private String basicAuth() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (user + ":" + password).getBytes(StandardCharsets.UTF_8));
    }
}
