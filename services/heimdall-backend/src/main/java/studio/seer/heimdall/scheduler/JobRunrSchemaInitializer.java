package studio.seer.heimdall.scheduler;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * Ensures JobRunr schema exists in the shared "frigg-jobrunr" ArcadeDB database
 * before the BackgroundJobServer starts.
 *
 * Priority 5 → fires before JobRunrLifecycle (Priority 10).
 */
@ApplicationScoped
public class JobRunrSchemaInitializer {

    private static final Logger LOG = Logger.getLogger(JobRunrSchemaInitializer.class);

    private static final String[][] DOCUMENT_TYPES_AND_PROPS = {
        // { typeName, propertyName, arcadeDbType }
        { "jobrunr_jobs",           "id",      "STRING" },
        { "jobrunr_jobs",           "state",   "STRING" },
        { "jobrunr_recurring_jobs", "id",      "STRING" },
        { "jobrunr_servers",        "id",      "STRING" },
        { "jobrunr_metadata",       "id",      "STRING" },
    };

    @Inject JobRunrFriggGateway frigg;

    volatile boolean ready = false;

    void onStart(@Observes @Priority(5) StartupEvent ev) {
        LOG.info("JobRunrSchemaInitializer: ensuring frigg-jobrunr schema...");
        try {
            ensureTypes();
            ensureIndexes();
            ready = true;
            LOG.infof("JobRunrSchemaInitializer: schema ready (db=%s)", frigg.db());
        } catch (Exception e) {
            LOG.warnf("JobRunrSchemaInitializer: schema setup failed — %s", e.getMessage());
        }
    }

    private void ensureTypes() {
        Set<String> seen = new HashSet<>();
        for (String[] entry : DOCUMENT_TYPES_AND_PROPS) {
            String type = entry[0];
            if (!seen.add(type)) continue;
            try {
                frigg.sql("CREATE document TYPE `" + type + "` IF NOT EXISTS");
                LOG.debugf("JobRunrSchemaInitializer: type %s OK", type);
            } catch (Exception e) {
                LOG.warnf("JobRunrSchemaInitializer: type %s — %s", type, e.getMessage());
            }
        }
    }

    private void ensureIndexes() {
        for (String[] entry : DOCUMENT_TYPES_AND_PROPS) {
            String type = entry[0], prop = entry[1], arcadeType = entry[2];
            try {
                frigg.sql("CREATE PROPERTY `" + type + "`.`" + prop + "` IF NOT EXISTS " + arcadeType);
                frigg.sql("CREATE INDEX IF NOT EXISTS ON `" + type + "` (" + prop + ") UNIQUE");
            } catch (Exception e) {
                LOG.warnf("JobRunrSchemaInitializer: index %s.%s — %s", type, prop, e.getMessage());
            }
        }
    }

    public boolean isReady() { return ready; }
}
