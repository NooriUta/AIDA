package studio.seer.dali.rest;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Ensures the YGG (hound) database exists in ArcadeDB before any parse session
 * tries to write lineage data.
 *
 * <p>Runs at {@code @Priority(3)} — before {@code FriggSchemaInitializer} (5)
 * and {@code JobRunrLifecycle} (10).
 *
 * <p>Retries up to 12 times with 3 s delay if ArcadeDB is still starting.
 */
@ApplicationScoped
@Priority(3)
public class YggSchemaInitializer {

    private static final Logger log = LoggerFactory.getLogger(YggSchemaInitializer.class);

    @Inject YggGateway ygg;

    void onStart(@Observes StartupEvent ev) {
        log.info("YggSchemaInitializer: ensuring YGG database exists…");
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
            log.warn("YggSchemaInitializer: YGG database still unavailable after 12 attempts — " +
                     "parse sessions will fail until YGG is reachable and 'hound' database exists");
        }
    }
}
