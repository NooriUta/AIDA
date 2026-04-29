package studio.seer.dali.rest;

import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.tenantrouting.TenantNotAvailableException;
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
        // Resolve the actual database names from the registry so we ensure the RIGHT databases
        // (hound_default, hound_src_default) rather than the legacy config-default 'hound'.
        // resolveDbNameQuietly tolerates FRIGG being unavailable during tests / cold boot.
        String lineageDb = resolveDbNameQuietly("default", lineageRegistry::resourceFor, "hound_default");
        String sourceDb  = resolveDbNameQuietly("default", sourceRegistry::resourceFor,  "hound_src_default");
        log.info("YggSchemaInitializer: ensuring YGG databases exist — lineage={}, source={}", lineageDb, sourceDb);
        // Retry until the lineage DB is reachable (YGG may need a moment after its health check passes).
        boolean ready = false;
        for (int attempt = 1; attempt <= 12 && !ready; attempt++) {
            ready = ygg.ensureDatabaseNamed(lineageDb);
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
        // Also ensure the source-archive database exists (best-effort; failure is non-fatal).
        ygg.ensureDatabaseNamed(sourceDb);
    }

    private static String resolveDbNameQuietly(String alias,
                                               java.util.function.Function<String, ? extends studio.seer.tenantrouting.ArcadeConnection> resolver,
                                               String fallback) {
        try {
            return resolver.apply(alias).databaseName();
        } catch (TenantNotAvailableException e) {
            log.debug("YggSchemaInitializer: '{}' not in FRIGG yet ({}); logging fallback name '{}'",
                     alias, e.reason(), fallback);
            return fallback;
        }
    }
}
