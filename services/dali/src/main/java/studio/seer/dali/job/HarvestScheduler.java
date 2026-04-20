package studio.seer.dali.job;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * Cron trigger for the nightly JDBC harvest (DS-06).
 *
 * <p>Fires daily at 02:00 UTC in production; every 5 minutes in {@code %dev} profile
 * for manual testing; disabled ({@code off}) in {@code %test} to prevent scheduled
 * interference with unit tests.
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

    @ConfigProperty(name = "dali.harvest.cron", defaultValue = "0 0 2 * * ?")
    String cron;

    /** Cron format: second minute hour day month weekday */
    @Scheduled(cron = "${dali.harvest.cron:0 0 2 * * ?}")
    void triggerNightlyHarvest() {
        String harvestId = "scheduled-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        log.info("HarvestScheduler: triggering nightly harvest (harvestId={})", harvestId);
        jobScheduler.get().<HarvestJob>enqueue(j -> j.execute(harvestId));
    }
}
