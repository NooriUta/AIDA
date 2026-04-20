package studio.seer.dali.storage;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        } catch (Exception e) {
            log.warn("FriggSchemaInitializer: could not initialise FRIGG schema (FRIGG may be unavailable): {}",
                    e.getMessage());
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
    }

    /**
     * Creates a document type in FRIGG.
     *
     * @return {@code true} if the type was created or already exists; {@code false} on failure.
     */
    private boolean createDocumentType(String typeName) {
        try {
            frigg.sql("CREATE DOCUMENT TYPE `" + typeName + "` IF NOT EXISTS");
            log.debug("FriggSchemaInitializer: type '{}' ensured", typeName);
            return true;
        } catch (Exception e) {
            log.warn("FriggSchemaInitializer: could not create type '{}': {}", typeName, e.getMessage());
            return false;
        }
    }

    private void ensureProperties() {
        for (String[] p : INDEXED_PROPERTIES) {
            try {
                frigg.sql(String.format(
                        "CREATE PROPERTY `%s`.`%s` IF NOT EXISTS %s", p[0], p[1], p[2]));
                log.debug("FriggSchemaInitializer: property '{}.{}' ensured", p[0], p[1]);
            } catch (Exception e) {
                log.warn("FriggSchemaInitializer: could not create property {}.{}: {}", p[0], p[1], e.getMessage());
            }
        }
    }

    private void createIndexes() {
        createIndex("jobrunr_jobs",           "id",        true);
        createIndex("jobrunr_jobs",           "state",     false);
        createIndex("jobrunr_recurring_jobs", "id",        true);
        createIndex("jobrunr_servers",        "id",              true);
        createIndex("jobrunr_servers",        "lastHeartbeat",   false);
        createIndex("jobrunr_metadata",       "id",        true);
        createIndex("dali_sessions",          "id",         true);
        createIndex("dali_sessions",          "startedAt",  false);
        createIndex("dali_sessions",          "finishedAt", false);
        createIndex("dali_sessions",          "status",     false);
        createIndex("dali_sessions",          "dialect",    false);
        createIndex("dali_sessions",          "instanceId", false);
        createIndex("dali_sources",           "id",         true);
        createIndex("dali_sources",           "dialect",    false);
    }

    private void createIndex(String type, String property, boolean unique) {
        String indexType = unique ? "UNIQUE" : "NOTUNIQUE";
        // ArcadeDB auto-generates the index name; IF NOT EXISTS skips if already present.
        String sql = String.format(
                "CREATE INDEX IF NOT EXISTS ON `%s` (`%s`) %s", type, property, indexType);
        try {
            frigg.sql(sql);
            log.debug("FriggSchemaInitializer: index ensured — {} ({}) on {}", property, indexType, type);
        } catch (Exception e) {
            log.warn("FriggSchemaInitializer: could not create index on {}.{}: {}", type, property, e.getMessage());
        }
    }
}
