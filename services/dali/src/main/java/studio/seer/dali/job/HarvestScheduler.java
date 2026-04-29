package studio.seer.dali.job;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.config.DaliConfig;

import java.util.UUID;

/**
 * Cron trigger for the nightly JDBC harvest (DS-06).
 *
 * <p>Fires daily at 02:00 UTC in production; every 5 minutes in {@code %dev} profile
 * for manual testing; disabled ({@code off}) in {@code %test} to prevent scheduled
 * interference with unit tests.
 *
 * <p>DMT-07: when {@code dali.jobrunr.worker-only=true} the cron fires but
 * immediately returns without enqueuing — only the designated scheduler pod
 * (worker-only=false) submits harvest jobs. This prevents duplicate nightly
 * harvests when multiple Dali replicas share one JobRunr store.
 *
 * <p>Configure via application.properties (see DS-06 in SPRINT_DALI_CORE_S1.md).
 *
 * @see HarvestJob
 */
@ApplicationScoped
public class HarvestScheduler {

    private static final Logger log = LoggerFactory.getLogger(HarvestScheduler.class);

    // Instance<> defers resolution — JobScheduler is produced during StartupEvent (Priority 10).
    // @Scheduled fires only after startup completes, so get() is always safe here.
    @Inject Instance<JobScheduler> jobScheduler;

    @Inject DaliConfig config;

    @ConfigProperty(name = "dali.harvest.cron", defaultValue = "0 0 2 * * ?")
    String cron;

    /** Cron format: second minute hour day month weekday */
    @Scheduled(cron = "${dali.harvest.cron:0 0 2 * * ?}")
    void triggerNightlyHarvestCron() {
        tryEnqueueHarvest();
    }

    /**
     * Enqueues the nightly harvest job unless this node is in worker-only mode.
     *
     * @return {@code true} if the job was enqueued, {@code false} if skipped (DMT-07 guard).
     *         Package-private for unit testing.
     */
    boolean tryEnqueueHarvest() {
        if (config.jobrunr().workerOnly()) {
            log.debug("HarvestScheduler: worker-only mode — skipping scheduled harvest (DMT-07)");
            return false;
        }
        String harvestId = "scheduled-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("HarvestScheduler: triggering nightly harvest (harvestId={})", harvestId);
        jobScheduler.get().<HarvestJob>enqueue(j -> j.execute(harvestId, "default"));
        return true;
    }
}
