package studio.seer.dali.archive;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.rest.YggClient;
import studio.seer.dali.storage.FriggCommand;
import studio.seer.dali.storage.FriggGateway;
import studio.seer.tenantrouting.TenantNotAvailableException;
import studio.seer.tenantrouting.YggSourceArchiveRegistry;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ensures the hound_src_{tenant} schema (DaliParseSession, DaliSourceFile, DaliParseError)
 * exists in YGG for all known tenants at Dali startup.
 *
 * Runs at Priority(4) — after YggSchemaInitializer(3) which already ensured
 * the hound_default lineage DB, before FriggSchemaInitializer(5).
 *
 * Schema is idempotent (IF NOT EXISTS guards everywhere) and safe to call multiple times.
 * Called again by SourceArchiveWriter on first write for any newly provisioned tenant.
 */
@ApplicationScoped
public class SourceArchiveSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(SourceArchiveSchemaInitializer.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    @Inject @RestClient YggClient yggClient;
    @Inject YggSourceArchiveRegistry sourceRegistry;
    @Inject FriggGateway frigg;

    @ConfigProperty(name = "ygg.user")     String yggUser;
    @ConfigProperty(name = "ygg.password") String yggPassword;

    /** Tracks DBs already initialised in this JVM lifetime — avoids redundant DDL. */
    private final Set<String> initialised = ConcurrentHashMap.newKeySet();

    void onStart(@Observes @Priority(4) StartupEvent ev) {
        log.info("SourceArchiveSchemaInitializer: ensuring hound_src_* schemas in YGG...");
        ensureForAlias("default");
        try {
            List<Map<String, Object>> rows = frigg.sqlIn(
                    "frigg-tenants",
                    "SELECT tenantAlias FROM DaliTenantConfig " +
                    "WHERE status IN ['ACTIVE', 'PROVISIONING', 'SUSPENDED']");
            for (Map<String, Object> row : rows) {
                Object a = row.get("tenantAlias");
                if (a != null && !"default".equals(a.toString())) ensureForAlias(a.toString());
            }
        } catch (Exception e) {
            log.warn("SourceArchiveSchemaInitializer: could not enumerate tenants ({})", e.getMessage());
        }
    }

    /**
     * Ensures schema for a given tenant alias. Idempotent — safe to call from provisioning.
     * Called lazily by SourceArchiveWriter on first write for a tenant.
     */
    public void ensureForAlias(String tenantAlias) {
        String db = resolveDb(tenantAlias);
        if (db == null || initialised.contains(db)) return;
        log.info("SourceArchiveSchemaInitializer: initialising schema in {}", db);
        try {
            createDb(db);
            for (String type : SourceArchiveSchemaCommands.TYPES) {
                ddl(db, "CREATE DOCUMENT TYPE `" + type + "` IF NOT EXISTS");
            }
            for (String[] p : SourceArchiveSchemaCommands.PROPERTIES) {
                ddl(db, String.format("CREATE PROPERTY `%s`.`%s` IF NOT EXISTS %s", p[0], p[1], p[2]));
            }
            for (Object[] idx : SourceArchiveSchemaCommands.INDEXES) {
                String type    = (String)  idx[0];
                String prop    = (String)  idx[1];
                boolean unique = (Boolean) idx[2];
                ddl(db, String.format("CREATE INDEX IF NOT EXISTS ON `%s` (`%s`) %s",
                        type, prop, unique ? "UNIQUE" : "NOTUNIQUE"));
            }
            initialised.add(db);
            log.info("SourceArchiveSchemaInitializer: schema ready in {}", db);
        } catch (Exception e) {
            log.warn("SourceArchiveSchemaInitializer: schema init failed for {} ({}): {}",
                    tenantAlias, db, e.getMessage());
        }
    }

    private String resolveDb(String tenantAlias) {
        try {
            return sourceRegistry.resourceFor(tenantAlias).databaseName();
        } catch (TenantNotAvailableException e) {
            log.debug("SourceArchiveSchemaInitializer: tenant '{}' not in FRIGG yet ({})", tenantAlias, e.reason());
            return "hound_src_" + tenantAlias;
        }
    }

    private void createDb(String dbName) {
        try {
            yggClient.serverCommand(auth(), new FriggCommand(null, "create database " + dbName, null))
                    .await().atMost(TIMEOUT);
            log.info("SourceArchiveSchemaInitializer: created DB {}", dbName);
        } catch (Exception e) {
            // ArcadeDB returns 500/400 when DB already exists — treat as success
            log.debug("SourceArchiveSchemaInitializer: create DB {} — {} (likely already exists)", dbName, e.getMessage());
        }
    }

    private void ddl(String db, String sql) {
        try {
            yggClient.command(db, auth(), new FriggCommand("sql", sql, null))
                    .await().atMost(TIMEOUT);
        } catch (Exception e) {
            log.warn("SourceArchiveSchemaInitializer: DDL failed in {} — {} : {}", db, sql, e.getMessage());
        }
    }

    String auth() {
        return "Basic " + Base64.getEncoder().encodeToString(
                (yggUser + ":" + yggPassword).getBytes(StandardCharsets.UTF_8));
    }
}
