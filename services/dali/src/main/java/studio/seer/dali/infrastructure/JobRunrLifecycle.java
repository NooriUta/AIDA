package studio.seer.dali.infrastructure;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.configuration.JobRunrConfiguration;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.server.JobActivator;
import org.jobrunr.storage.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.config.DaliConfig;
import studio.seer.dali.storage.ArcadeDbStorageProvider;

/**
 * Single-responsibility bean: initialises and tears down all JobRunr infrastructure.
 *
 * <p>Produces:
 * <ul>
 *   <li>{@link StorageProvider} — {@link ArcadeDbStorageProvider} backed by FRIGG
 *       so job state survives Dali restarts.</li>
 *   <li>{@link JobScheduler} — taken directly from
 *       {@link JobRunrConfiguration#initialize()} so {@code setJobMapper()}
 *       has already been called on the storage provider.</li>
 * </ul>
 *
 * <p>The {@code JobActivator} delegates to the CDI container so that
 * {@code @Inject} fields in {@link studio.seer.dali.job.ParseJob} work at
 * execution time.
 */
// Quarkus fires lower @Priority values first. Order: FriggSchemaInitializer(5) → here(10) → SessionService(20).
// This guarantees FRIGG schema and stale-server cleanup finish before the BackgroundJobServer starts.
@ApplicationScoped
public class JobRunrLifecycle {

    private static final Logger log = LoggerFactory.getLogger(JobRunrLifecycle.class);

    @Inject DaliConfig config;
    @Inject ArcadeDbStorageProvider arcadeDbStorageProvider;

    private volatile JobRunrConfiguration.JobRunrConfigurationResult jobRunrResult;

    // ─── Produces ─────────────────────────────────────────────────────────────

    @Produces
    @ApplicationScoped
    public StorageProvider storageProvider() {
        log.info("JobRunr: using ArcadeDbStorageProvider (FRIGG — state persisted across restarts)");
        return arcadeDbStorageProvider;
    }

    /**
     * Returns the {@link JobScheduler} created by {@link JobRunrConfiguration#initialize()}.
     * Must only be called after {@link #onStart(StartupEvent)} has completed.
     * Safe for HTTP-triggered code since the HTTP server starts after {@code StartupEvent}.
     */
    @Produces
    @Singleton
    public JobScheduler jobScheduler() {
        if (jobRunrResult == null) {
            throw new IllegalStateException(
                "JobScheduler requested before JobRunr was initialised (StartupEvent not yet fired)");
        }
        return jobRunrResult.getJobScheduler();
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    void onStart(@Observes @Priority(10) StartupEvent ev) {
        log.info("JobRunr: initialising...");
        JobActivator activator = new JobActivator() {
            @Override
            public <T> T activateJob(Class<T> type) {
                return CDI.current().select(type).get();
            }
        };
        try {
            var jrConfig = JobRunr.configure()
                    .useStorageProvider(arcadeDbStorageProvider)
                    .useJobActivator(activator)
                    .useBackgroundJobServer();
            if (config.jobrunr().dashboard().enabled()) {
                int port = config.jobrunr().dashboard().port();
                jrConfig = jrConfig.useDashboard(port);
                log.info("JobRunr: dashboard enabled on :{}", port);
            }
            jobRunrResult = jrConfig.initialize();
            log.info("JobRunr: ready — BackgroundJobServer started");
        } catch (Exception e) {
            log.error("JobRunr: initialisation FAILED — job scheduling is unavailable. " +
                      "Sessions can be accepted but no jobs will be enqueued until Dali restarts. " +
                      "Cause: {}", e.getMessage(), e);
            // jobRunrResult stays null; jobScheduler() producer will throw IllegalStateException
            // on first call, which SessionService.enqueue() surfaces as a 503.
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.info("JobRunr: shutting down...");
        JobRunr.destroy();
    }
}
