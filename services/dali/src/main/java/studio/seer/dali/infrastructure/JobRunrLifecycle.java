package studio.seer.dali.infrastructure;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Produces;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.inject.Singleton;
import org.jobrunr.configuration.JobRunr;
import org.jobrunr.configuration.JobRunrConfiguration;
import org.jobrunr.scheduling.JobScheduler;
import org.jobrunr.server.JobActivator;
import org.jobrunr.storage.InMemoryStorageProvider;
import org.jobrunr.storage.StorageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single-responsibility bean: initialises and tears down all JobRunr infrastructure.
 *
 * <p>Produces:
 * <ul>
 *   <li>{@link StorageProvider} — {@link InMemoryStorageProvider} for dev;
 *       swap to {@code ArcadeDbStorageProvider} when FRIGG is wired.</li>
 *   <li>{@link JobScheduler} — taken directly from
 *       {@link JobRunrConfiguration#initialize()} so {@code setJobMapper()}
 *       has already been called on the storage provider.</li>
 * </ul>
 *
 * <p>The {@code JobActivator} delegates to the CDI container so that
 * {@code @Inject} fields in {@link studio.seer.dali.job.ParseJob} work at
 * execution time.
 */
@ApplicationScoped
public class JobRunrLifecycle {

    private static final Logger log = LoggerFactory.getLogger(JobRunrLifecycle.class);

    // Created eagerly — before StartupEvent — so that the @Produces method
    // can return it without null-checking.
    private final InMemoryStorageProvider storageProvider = new InMemoryStorageProvider();

    private volatile JobRunrConfiguration.JobRunrConfigurationResult jobRunrResult;

    // ─── Produces ─────────────────────────────────────────────────────────────

    @Produces
    @ApplicationScoped
    public StorageProvider storageProvider() {
        // TODO(Д10 / integration): return new ArcadeDbStorageProvider(friggGateway)
        log.info("JobRunr: using InMemoryStorageProvider (dev mode — state lost on restart)");
        return storageProvider;
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

    void onStart(@Observes StartupEvent ev) {
        log.info("JobRunr: initialising...");
        JobActivator activator = new JobActivator() {
            @Override
            public <T> T activateJob(Class<T> type) {
                return CDI.current().select(type).get();
            }
        };
        jobRunrResult = JobRunr.configure()
                .useStorageProvider(storageProvider)
                .useJobActivator(activator)
                .useBackgroundJobServer()
                .initialize();
        log.info("JobRunr: ready — BackgroundJobServer started");
    }

    void onStop(@Observes ShutdownEvent ev) {
        log.info("JobRunr: shutting down...");
        JobRunr.destroy();
    }
}
