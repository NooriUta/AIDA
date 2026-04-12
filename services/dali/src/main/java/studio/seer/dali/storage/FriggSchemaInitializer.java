package studio.seer.dali.storage;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ensures the JobRunr schema exists in FRIGG (ArcadeDB) before the
 * background job server starts processing jobs.
 *
 * <p>Creates four document types if they don't already exist:
 * <ul>
 *   <li>{@code jobrunr_jobs}           — persisted job records</li>
 *   <li>{@code jobrunr_recurring_jobs} — recurring job definitions</li>
 *   <li>{@code jobrunr_servers}        — background server heartbeats</li>
 *   <li>{@code jobrunr_metadata}       — cluster-wide metadata</li>
 * </ul>
 *
 * <p>If FRIGG is not reachable at startup (e.g., container starting up),
 * errors are logged as warnings rather than failing the service.
 *
 * <p><b>Prerequisites:</b> the {@code dali} database must exist in FRIGG.
 * Create it manually via the ArcadeDB console or use the {@link FriggGateway#ensureDatabase()}
 * call (enabled by default here).
 */
@ApplicationScoped
public class FriggSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(FriggSchemaInitializer.class);

    private static final String[] DOCUMENT_TYPES = {
        "jobrunr_jobs",
        "jobrunr_recurring_jobs",
        "jobrunr_servers",
        "jobrunr_metadata"
    };

    @Inject FriggGateway frigg;

    void onStart(@Observes StartupEvent ev) {
        log.info("FriggSchemaInitializer: initialising JobRunr schema in FRIGG...");
        try {
            frigg.ensureDatabase();
            for (String type : DOCUMENT_TYPES) {
                createDocumentType(type);
            }
            createIndexes();
            log.info("FriggSchemaInitializer: JobRunr schema ready (4 document types)");
        } catch (Exception e) {
            log.warn("FriggSchemaInitializer: could not initialise FRIGG schema (FRIGG may be unavailable): {}",
                    e.getMessage());
        }
    }

    private void createDocumentType(String typeName) {
        try {
            frigg.sql("CREATE DOCUMENT TYPE `" + typeName + "` IF NOT EXISTS");
            log.debug("FriggSchemaInitializer: type '{}' ensured", typeName);
        } catch (Exception e) {
            log.warn("FriggSchemaInitializer: could not create type '{}': {}", typeName, e.getMessage());
        }
    }

    private void createIndexes() {
        createIndex("jobrunr_jobs",           "id",    true);
        createIndex("jobrunr_jobs",           "state", false);
        createIndex("jobrunr_recurring_jobs", "id",    true);
        createIndex("jobrunr_servers",        "id",    true);
        createIndex("jobrunr_metadata",       "id",    true);
    }

    private void createIndex(String type, String property, boolean unique) {
        String indexType = unique ? "UNIQUE" : "NOTUNIQUE";
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
