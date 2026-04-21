package studio.seer.dali.rest;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.tenantrouting.YggLineageRegistry;
import studio.seer.tenantrouting.YggSourceArchiveRegistry;

/**
 * Ensures the YGG lineage and source-archive databases exist in ArcadeDB before any
 * parse session tries to write lineage data.
 *
 * <p>DMT-05: uses {@link YggLineageRegistry} and {@link YggSourceArchiveRegistry} instead
 * of the old {@link YggGateway} which hardcoded the database name from config.
 *
 * <p>Runs at {@code @Priority(3)} — before FriggSchemaInitializer (5) and JobRunrLifecycle (10).
 */
@ApplicationScoped
public class YggSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(YggSchemaInitializer.class);

    @Inject YggLineageRegistry       lineageRegistry;
    @Inject YggSourceArchiveRegistry sourceRegistry;
    @Inject YggGateway               ygg;   // kept for ensureDatabase() ping

    void onStart(@Observes @Priority(3) StartupEvent ev) {
        String lineageDb = lineageRegistry.resourceFor("default").databaseName();
        String sourceDb  = sourceRegistry.resourceFor("default").databaseName();
        log.info("YggSchemaInitializer: ensuring YGG databases exist — lineage={}, source={}", lineageDb, sourceDb);
        boolean ready = false;
        for (int attempt = 1; attempt <= 12 && !ready; attempt++) {
            ready = ygg.ensureDatabase();
            if (ready) {
                log.info("YggSchemaInitializer: YGG database ready (attempt {})", attempt);
            } else {
                log.warn("YggSchemaInitializer: YGG not ready on attempt {}/12, retrying in 3 s…", attempt);
                try { Thread.sleep(3_000); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        if (!ready) {
            log.warn("YggSchemaInitializer: YGG database still unavailable — parse sessions will fail");
        }
    }
}
