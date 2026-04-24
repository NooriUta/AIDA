package studio.seer.heimdall.scheduler;

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
import org.jobrunr.server.BackgroundJobServerConfiguration;
import org.jobrunr.server.JobActivator;
import org.jobrunr.storage.StorageProvider;
import org.jboss.logging.Logger;

/**
 * Initialises and tears down JobRunr for HEIMDALL-BE.
 *
 * HEIMDALL is the designated JobRunr scheduler leader — it runs a BackgroundJobServer
 * with 1 worker (for archive/restore/purge jobs) and holds the recurring-job scheduler
 * (harvest crons per tenant).  Dali replicas run as workers connected to the same
 * frigg-jobrunr storage (DMT-07 worker-only config).
 *
 * OSS note: JobRunr 8.5.2 community does not have a scheduler-only mode — every
 * BackgroundJobServer participates in leader election and scheduling.
 * HEIMDALL's JobActivator throws for unknown job types (Dali jobs) so they remain
 * enqueued and are picked up by the next available Dali worker.
 *
 * Priority 10 — fires after JobRunrSchemaInitializer (Priority 5).
 */
@ApplicationScoped
public class JobRunrLifecycle {

    private static final Logger LOG = Logger.getLogger(JobRunrLifecycle.class);

    @Inject SchedulerConfig            config;
    @Inject ArcadeDbSchedulerStorageProvider storageProvider;

    private volatile JobRunrConfiguration.JobRunrConfigurationResult jobRunrResult;

    // ─── Produces ─────────────────────────────────────────────────────────────

    @Produces @ApplicationScoped
    public StorageProvider storageProvider() {
        return storageProvider;
    }

    @Produces @Singleton
    public JobScheduler jobScheduler() {
        if (jobRunrResult == null) {
            throw new IllegalStateException(
                "JobScheduler requested before JobRunr was initialised (StartupEvent not yet fired)");
        }
        return jobRunrResult.getJobScheduler();
    }

    // ─── Lifecycle ────────────────────────────────────────────────────────────

    void onStart(@Observes @Priority(10) StartupEvent ev) {
        LOG.info("JobRunr: initialising HEIMDALL scheduler leader...");
        JobActivator activator = new JobActivator() {
            @Override
            public <T> T activateJob(Class<T> type) {
                try {
                    return CDI.current().select(type).get();
                } catch (Exception e) {
                    // Job type belongs to another service (e.g. Dali HarvestJob).
                    // Re-throw so JobRunr puts the job back in the queue for a Dali worker.
                    throw new IllegalStateException(
                        "Job type not available on this node: " + type.getName(), e);
                }
            }
        };
        try {
            var bgConfig = BackgroundJobServerConfiguration
                    .usingStandardBackgroundJobServerConfiguration()
                    .andWorkerCount(config.jobrunr().workerThreads());
            var jrConfig = JobRunr.configure()
                    .useStorageProvider(storageProvider)
                    .useJobActivator(activator)
                    .useBackgroundJobServer(bgConfig);
            if (config.jobrunr().dashboardEnabled()) {
                jrConfig = jrConfig.useDashboard(config.jobrunr().dashboardPort());
                LOG.infof("JobRunr: dashboard enabled on :%d", config.jobrunr().dashboardPort());
            }
            jobRunrResult = jrConfig.initialize();
            LOG.info("JobRunr: ready — HEIMDALL is scheduler leader");
        } catch (Exception e) {
            LOG.errorf("JobRunr: initialisation FAILED — %s", e.getMessage(), e);
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        LOG.info("JobRunr: shutting down...");
        JobRunr.destroy();
    }
}
