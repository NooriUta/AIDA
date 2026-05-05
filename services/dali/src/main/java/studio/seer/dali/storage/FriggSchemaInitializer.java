package studio.seer.dali.storage;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Ensures the JobRunr schema exists in FRIGG (ArcadeDB) before the
 * background job server starts processing jobs.
 *
 * <p>Creates five document types if they don't already exist:
 * <ul>
 *   <li>{@code jobrunr_jobs}           — persisted job records</li>
 *   <li>{@code jobrunr_recurring_jobs} — recurring job definitions</li>
 *   <li>{@code jobrunr_servers}        — background server heartbeats</li>
 *   <li>{@code jobrunr_metadata}       — cluster-wide metadata</li>
 *   <li>{@code dali_sessions}          — Dali parse session records</li>
 * </ul>
 *
 * <p>If FRIGG is not reachable at startup (e.g., container starting up),
 * errors are logged as warnings rather than failing the service.
 * {@link #isSchemaReady()} returns {@code false} in that case, and
 * {@code SessionService.enqueue()} refuses new jobs until the schema is repaired.
 *
 * <p><b>Prerequisites:</b> the {@code dali} database must exist in FRIGG.
 * Create it manually via the ArcadeDB console or use the {@link FriggGateway#ensureDatabase()}
 * call (enabled by default here).
 *
 * <p><b>Observer priority:</b> {@code @Priority(5)} — Quarkus fires lower numbers first, so
 * this fires before {@code JobRunrLifecycle} ({@code @Priority(10)}) and
 * {@code SessionService} ({@code @Priority(20)}), ensuring schema and server-table cleanup
 * happen before the BackgroundJobServer starts.
 */
@ApplicationScoped
public class FriggSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(FriggSchemaInitializer.class);

    private static final String[] DOCUMENT_TYPES = {
        "jobrunr_jobs",
        "jobrunr_recurring_jobs",
        "jobrunr_servers",
        "jobrunr_metadata",
        "dali_sessions",
        "dali_sources"
    };

    /**
     * Properties to ensure before creating indexes.
     * ArcadeDB requires a declared property before an index can be created on it.
     * Format: { typeName, propertyName, arcadeDbType }
     */
    private static final String[][] INDEXED_PROPERTIES = {
        { "jobrunr_jobs",           "id",              "STRING"   },
        { "jobrunr_jobs",           "state",           "STRING"   },
        { "jobrunr_jobs",           "scheduledAt",     "DATETIME" },   // SCHEDULED-poll uses `scheduledAt < cutoff`
        { "jobrunr_jobs",           "updatedAt",       "DATETIME" },   // PROCESSING-orphan poll uses `updatedAt < cutoff`
        { "jobrunr_recurring_jobs", "id",              "STRING"   },
        { "jobrunr_servers",        "id",              "STRING"   },
        { "jobrunr_servers",        "lastHeartbeat",   "LONG"     },
        { "jobrunr_metadata",       "id",              "STRING"   },
        { "dali_sessions",          "id",              "STRING"   },
        { "dali_sessions",          "startedAt",       "DATETIME" },   // BUG-SS-014: was STRING
        // ── Perf-analysis properties (queryable top-level, mirrors sessionJson content) ─
        { "dali_sessions",          "finishedAt",      "DATETIME" },
        { "dali_sessions",          "status",          "STRING"   },
        { "dali_sessions",          "dialect",         "STRING"   },
        { "dali_sessions",          "instanceId",      "STRING"   },
        { "dali_sessions",          "durationMs",      "LONG"     },
        { "dali_sessions",          "atomCount",       "INTEGER"  },
        { "dali_sessions",          "vertexCount",     "INTEGER"  },
        { "dali_sessions",          "edgeCount",       "INTEGER"  },
        { "dali_sessions",          "droppedEdgeCount","INTEGER"  },
        { "dali_sessions",          "resolutionRate",  "DOUBLE"   },
        // ── JDBC source registry ──────────────────────────────────────────────────
        { "dali_sources",           "id",              "STRING"   },
        { "dali_sources",           "name",            "STRING"   },
        { "dali_sources",           "dialect",         "STRING"   },
        { "dali_sources",           "jdbcUrl",         "STRING"   },
        { "dali_sources",           "username",        "STRING"   },
        { "dali_sources",           "password",        "STRING"   },
        { "dali_sources",           "atomCount",       "INTEGER"  },
        { "dali_sources",           "lastHarvest",     "STRING"   },
        { "dali_sources",           "schemaInclude",   "STRING"   },
        { "dali_sources",           "schemaExclude",   "STRING"   },
        { "dali_sources",           "createdAt",       "STRING"   },
    };

    @Inject FriggGateway frigg;

    /** True only when all five document types were successfully created/verified. */
    private volatile boolean schemaReady = false;

    /** Returns {@code true} if the FRIGG schema initialised correctly at startup. */
    public boolean isSchemaReady() {
        return schemaReady;
    }

    void onStart(@Observes @Priority(5) StartupEvent ev) {
        log.info("FriggSchemaInitializer: initialising JobRunr schema in FRIGG...");
        try {
            // Retry until the database is ready — FRIGG may need a moment after its
            // health check passes (race condition observed with ArcadeDB 26.3.2 in CI).
            boolean dbReady = false;
            for (int attempt = 1; attempt <= 12 && !dbReady; attempt++) {
                dbReady = frigg.ensureDatabase();
                if (dbReady) {
                    log.info("FriggSchemaInitializer: FRIGG database ready (attempt {})", attempt);
                } else {
                    log.warn("FriggSchemaInitializer: FRIGG not ready on attempt {}/12, retrying in 3 s…",
                             attempt);
                    try { Thread.sleep(3_000); } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw ie;
                    }
                }
            }
            if (!dbReady) {
                log.warn("FriggSchemaInitializer: database still unavailable after 12 attempts — schema init may fail");
            }
            boolean allTypesOk = true;
            for (String type : DOCUMENT_TYPES) {
                if (!createDocumentType(type)) allTypesOk = false;
            }
            // Migrate lastHeartbeat DATETIME → LONG before ensureProperties recreates it
            migrateLastHeartbeatToLong();
            ensureProperties();
            createIndexes();
            // Clear stale server registrations from previous runs so that master
            // election starts clean and getLongestRunningBackgroundJobServerId()
            // never returns a phantom UUID from a prior container lifecycle.
            clearStaleServers();
            if (allTypesOk) {
                schemaReady = true;
                log.info("FriggSchemaInitializer: schema ready (6 document types, perf-stat properties indexed)");
            } else {
                log.warn("FriggSchemaInitializer: schema partially initialised — one or more document " +
                         "types could not be created. Session enqueueing will be refused until Dali restarts.");
            }
            // Ensure tenant-specific DB for default tenant and all known tenants in FRIGG
            ensureSchema("default");
            ensureAllTenantSchemas();
        } catch (Exception e) {
            log.warn("FriggSchemaInitializer: could not initialise FRIGG schema (FRIGG may be unavailable): {}",
                    e.getMessage());
        }
    }

    /**
     * Queries frigg-tenants for all DaliTenantConfig records and ensures
     * dali_{alias} schema exists for each. Idempotent — safe to call at startup.
     */
    public void ensureAllTenantSchemas() {
        try {
            List<Map<String, Object>> rows = frigg.sqlIn(
                    "frigg-tenants",
                    "SELECT tenantAlias FROM DaliTenantConfig " +
                    "WHERE status IN ['ACTIVE', 'PROVISIONING', 'SUSPENDED']");
            for (Map<String, Object> row : rows) {
                Object aliasObj = row.get("tenantAlias");
                if (aliasObj == null) continue;
                String alias = aliasObj.toString();
                if (!"default".equals(alias)) { // MTN-04-EXEMPT: skip bootstrap tenant during multi-tenant schema init
                    ensureSchema(alias);
                }
            }
        } catch (Exception e) {
            log.warn("FriggSchemaInitializer: could not enumerate tenants from frigg-tenants: {}", e.getMessage());
        }
    }

    /**
     * Ensures the dali_{alias} database and schema exist for a given tenant.
     * Idempotent — safe to call multiple times.
     * Called at startup for "default" and by provisioning for new tenants.
     */
    public void ensureSchema(String tenantAlias) {
        String dbName = frigg.tenantDb(tenantAlias);
        log.info("FriggSchemaInitializer: ensuring tenant schema in {}", dbName);
        try {
            frigg.ensureDatabaseNamed(dbName);
            for (String type : DOCUMENT_TYPES) {
                createDocumentTypeIn(dbName, type);
            }
            ensurePropertiesIn(dbName);
            createIndexesIn(dbName);
            log.info("FriggSchemaInitializer: tenant schema ready in {}", dbName);
        } catch (Exception e) {
            log.warn("FriggSchemaInitializer: could not ensure tenant schema in {}: {}", dbName, e.getMessage());
        }
    }

    /**
     * Migrates {@code lastHeartbeat} from DATETIME to LONG so that epoch-ms comparisons
     * in {@link ArcadeDbStorageProvider#removeTimedOutBackgroundJobServers} work correctly.
     *
     * <p>ArcadeDB 26.3.2 refuses {@code DROP PROPERTY FORCE} when the property has an index
     * (returns HTTP 500). The workaround is to drop the index first, then the property.
     * Both steps are wrapped independently — they are no-ops on a fresh install.
     * {@link #ensureProperties()} recreates the property as LONG, and {@link #createIndexes()}
     * recreates the NOTUNIQUE index.
     */
    private void migrateLastHeartbeatToLong() {
        // Step 1: drop the index (ArcadeDB won't drop an indexed property, even with FORCE)
        try {
            frigg.sql("DROP INDEX `jobrunr_servers[lastHeartbeat]`");
            log.info("FriggSchemaInitializer: dropped index jobrunr_servers[lastHeartbeat]");
        } catch (Exception e) {
            // No-op on fresh install (index not yet created) or already dropped.
            log.debug("FriggSchemaInitializer: index drop skipped ({})", e.getMessage());
        }
        // Step 2: drop the property — FORCE not needed once the index is gone
        try {
            frigg.sql("DROP PROPERTY `jobrunr_servers`.`lastHeartbeat`");
            log.info("FriggSchemaInitializer: dropped lastHeartbeat property — will recreate as LONG");
        } catch (Exception e) {
            // No-op on fresh install.
            log.debug("FriggSchemaInitializer: property drop skipped ({})", e.getMessage());
        }
    }

    /**
     * Removes all records from {@code jobrunr_servers}.
     *
     * <p>Called at startup so that orphaned server registrations from a previous
     * container lifecycle never cause {@link org.jobrunr.server.ServerZooKeeper}
     * to elect a phantom master, leaving {@code masterId} null and triggering an NPE.
     */
    private void clearStaleServers() {
        try {
            frigg.sql("DELETE FROM `jobrunr_servers`");
            log.info("FriggSchemaInitializer: cleared stale jobrunr_servers");
        } catch (Exception e) {
            log.warn("FriggSchemaInitializer: could not clear jobrunr_servers: {}", e.getMessage());
        }
        // BUG-SS-STUCK: After clearStaleServers removes all server records, JobRunr's
        // removeTimedOutBackgroundJobServers() finds nothing to requeue — leaving any
        // PROCESSING jobs from a crashed previous run stuck forever.
        // Fix: reset them to FAILED on startup so they appear in the dashboard and
        // can be retried manually.
        try {
            frigg.sql("UPDATE `jobrunr_jobs` SET state = 'FAILED' WHERE state = 'PROCESSING'");
            log.info("FriggSchemaInitializer: reset stale PROCESSING jobs to FAILED");
        } catch (Exception e) {
            log.warn("FriggSchemaInitializer: could not reset stale PROCESSING jobs: {}", e.getMessage());
        }
        // BUG-SS-ENQUEUED-MISMATCH: Jobs can have state='ENQUEUED' in the column but a
        // terminal state (SUCCEEDED/FAILED) in jobAsJson — caused by a previous crash while
        // JobRunr was updating state. At startup BackgroundJobServer picks them up, tries
        // ENQUEUED→PROCESSING, and throws IllegalJobStateChangeException. After 5 occurrences
        // it declares FATAL and shuts down, making Dali return HTTP 409 for all new uploads.
        // Fix: delete all ENQUEUED rows at startup. Any legitimately queued work must be
        // resubmitted (client receives HTTP 202 on re-upload).
        try {
            frigg.sql("DELETE FROM `jobrunr_jobs` WHERE state = 'ENQUEUED'");
            log.info("FriggSchemaInitializer: cleared stale ENQUEUED jobs");
        } catch (Exception e) {
            log.warn("FriggSchemaInitializer: could not clear stale ENQUEUED jobs: {}", e.getMessage());
        }
        // BUG-SS-SCHEDULED-STALE: Two failure modes for SCHEDULED jobs:
        //   1. scheduledAt IS NULL  — invisible to the poll, stay stuck forever.
        //   2. scheduledAt is valid but jobAsJson has a terminal state (FAILED/SUCCEEDED)
        //      from a prior crash — when the retry fires, BackgroundJobServer tries
        //      SCHEDULED→ENQUEUED→PROCESSING but jobAsJson disagrees → IllegalJobStateChangeException.
        //      After 5 occurrences the server declares FATAL.  Typical source: probe sessions
        //      that failed mid-run with an old/bad dialect (e.g. 'sql' instead of 'plsql') had
        //      their retries scheduled; those retry jobs carry stale JSON on the next Dali start.
        // Fix: delete ALL SCHEDULED jobs at startup.  Any legitimately scheduled work
        // (harvest crons, etc.) will be rescheduled on its next cron tick; one-off sessions
        // must be resubmitted by the client.
        try {
            frigg.sql("DELETE FROM `jobrunr_jobs` WHERE state = 'SCHEDULED'");
            log.info("FriggSchemaInitializer: cleared stale SCHEDULED jobs");
        } catch (Exception e) {
            log.warn("FriggSchemaInitializer: could not clear stale SCHEDULED jobs: {}", e.getMessage());
        }
    }

    private boolean createDocumentType(String typeName) {
        return createDocumentTypeIn(null, typeName);
    }

    private boolean createDocumentTypeIn(String database, String typeName) {
        String sql = "CREATE DOCUMENT TYPE `" + typeName + "` IF NOT EXISTS";
        try {
            if (database != null) frigg.sqlIn(database, sql);
            else frigg.sql(sql);
            log.debug("FriggSchemaInitializer: type '{}' ensured (db={})", typeName, database);
            return true;
        } catch (Exception e) {
            log.warn("FriggSchemaInitializer: could not create type '{}' in {}: {}", typeName, database, e.getMessage());
            return false;
        }
    }

    private void ensureProperties() {
        ensurePropertiesIn(null);
    }

    private void ensurePropertiesIn(String database) {
        for (String[] p : INDEXED_PROPERTIES) {
            String sql = String.format("CREATE PROPERTY `%s`.`%s` IF NOT EXISTS %s", p[0], p[1], p[2]);
            try {
                if (database != null) frigg.sqlIn(database, sql);
                else frigg.sql(sql);
                log.debug("FriggSchemaInitializer: property '{}.{}' ensured (db={})", p[0], p[1], database);
            } catch (Exception e) {
                log.warn("FriggSchemaInitializer: could not create property {}.{}: {}", p[0], p[1], e.getMessage());
            }
        }
    }

    private void createIndexes() {
        createIndexesIn(null);
    }

    private void createIndexesIn(String database) {
        createIndexIn(database, "jobrunr_jobs",           "id",            true);
        createIndexIn(database, "jobrunr_jobs",           "state",         false);
        createIndexIn(database, "jobrunr_jobs",           "scheduledAt",   false);
        createIndexIn(database, "jobrunr_jobs",           "updatedAt",     false);
        createIndexIn(database, "jobrunr_recurring_jobs", "id",            true);
        createIndexIn(database, "jobrunr_servers",        "id",            true);
        createIndexIn(database, "jobrunr_servers",        "lastHeartbeat", false);
        createIndexIn(database, "jobrunr_metadata",       "id",            true);
        createIndexIn(database, "dali_sessions",          "id",            true);
        createIndexIn(database, "dali_sessions",          "startedAt",     false);
        createIndexIn(database, "dali_sessions",          "finishedAt",    false);
        createIndexIn(database, "dali_sessions",          "status",        false);
        createIndexIn(database, "dali_sessions",          "dialect",       false);
        createIndexIn(database, "dali_sessions",          "instanceId",    false);
        createIndexIn(database, "dali_sources",           "id",            true);
        createIndexIn(database, "dali_sources",           "dialect",       false);
    }

    private void createIndex(String type, String property, boolean unique) {
        createIndexIn(null, type, property, unique);
    }

    private void createIndexIn(String database, String type, String property, boolean unique) {
        String indexType = unique ? "UNIQUE" : "NOTUNIQUE";
        String sql = String.format(
                "CREATE INDEX IF NOT EXISTS ON `%s` (`%s`) %s", type, property, indexType);
        try {
            if (database != null) frigg.sqlIn(database, sql);
            else frigg.sql(sql);
            log.debug("FriggSchemaInitializer: index ensured — {}.{} ({})", type, property, indexType);
        } catch (Exception e) {
            log.warn("FriggSchemaInitializer: could not create index on {}.{}: {}", type, property, e.getMessage());
        }
    }
}
