package studio.seer.heimdall.scheduler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;

import java.time.Instant;

/**
 * HTA-13: Permanently delete a tenant's S3 archives after the retention window expires.
 *
 * Triggered either immediately via {@link #schedulePurge(String)} (admin override)
 * or at {@code archiveRetentionUntil} timestamp via {@link #scheduleAt(String, long)}.
 */
@ApplicationScoped
public class PurgeJobService {

    private static final Logger LOG = Logger.getLogger(PurgeJobService.class);

    @Inject Instance<JobScheduler> jobScheduler;
    @Inject S3ExportClient         s3;
    @Inject ArchiveAuditEmitter    audit;

    /** Enqueue an immediate purge. */
    public void schedulePurge(String tenantAlias) {
        LOG.infof("[PurgeJob] enqueue now alias=%s", tenantAlias);
        jobScheduler.get().enqueue(() -> purgeTenant(tenantAlias));
    }

    /** Schedule a one-shot purge at the given epoch-ms retention boundary. */
    public void scheduleAt(String tenantAlias, long retainUntilEpochMs) {
        Instant at = Instant.ofEpochMilli(retainUntilEpochMs);
        LOG.infof("[PurgeJob] schedule alias=%s at=%s", tenantAlias, at);
        jobScheduler.get().schedule(at, () -> purgeTenant(tenantAlias));
    }

    @Job(name = "purge-%0")
    public void purgeTenant(String tenantAlias) {
        LOG.infof("[PurgeJob] start alias=%s", tenantAlias);
        try {
            s3.purgeTenantPrefix(tenantAlias);
        } catch (S3ExportClient.ExportException ex) {
            LOG.errorf(ex, "[PurgeJob] purge failed alias=%s", tenantAlias);
            throw ex;
        }
        audit.emitPurged(tenantAlias);
        LOG.infof("[PurgeJob] done alias=%s", tenantAlias);
    }
}
