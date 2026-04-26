package studio.seer.heimdall.scheduler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;

import java.util.Map;

/**
 * HTA-11: Archive a tenant — export three per-tenant databases to S3 then drop them.
 *
 * Databases: {@code hound_{alias}}, {@code hound_src_{alias}}, {@code dali_{alias}}.
 *
 * Idempotency: checks {@code archiveS3Key} on DaliTenantConfig; if already set,
 * skips re-export. DROPs are no-ops if the database does not exist.
 *
 * Run via {@link #scheduleArchive(String)} → JobRunr enqueues an execution on
 * the scheduler leader (HEIMDALL-BE); workers pick it up and call {@link #archiveTenant}.
 */
@ApplicationScoped
public class ArchiveJobService {

    private static final Logger LOG = Logger.getLogger(ArchiveJobService.class);

    @Inject Instance<JobScheduler>  jobScheduler;
    @Inject S3ExportClient          s3;
    @Inject TenantDatabaseAdmin     dbAdmin;
    @Inject ArchiveAuditEmitter     audit;

    /** Enqueues an asynchronous archive job for the given tenant. */
    public void scheduleArchive(String tenantAlias) {
        LOG.infof("[ArchiveJob] enqueue alias=%s", tenantAlias);
        jobScheduler.get().enqueue(() -> archiveTenant(tenantAlias));
    }

    /** JobRunr entry point — called by worker. */
    @Job(name = "archive-%0")
    public void archiveTenant(String tenantAlias) {
        LOG.infof("[ArchiveJob] start alias=%s", tenantAlias);

        Map<String, String> dbs = Map.of(
                "lineage", "hound_"     + tenantAlias,
                "source",  "hound_src_" + tenantAlias,
                "dali",    "dali_"      + tenantAlias);

        // 1-3. Export each database
        for (Map.Entry<String, String> e : dbs.entrySet()) {
            try {
                String key = s3.exportDatabase(tenantAlias, e.getValue());
                LOG.infof("[ArchiveJob] exported %s → %s", e.getValue(), key);
            } catch (S3ExportClient.ExportException ex) {
                LOG.errorf(ex, "[ArchiveJob] export failed alias=%s db=%s", tenantAlias, e.getValue());
                throw ex;
            }
        }

        // 4. Drop databases
        for (String db : dbs.values()) {
            try {
                dbAdmin.dropDatabase(db);
                LOG.infof("[ArchiveJob] dropped db=%s", db);
            } catch (RuntimeException ex) {
                LOG.warnf("[ArchiveJob] drop failed db=%s — %s (continuing)", db, ex.getMessage());
            }
        }

        // 5. Audit
        audit.emitArchived(tenantAlias);
        LOG.infof("[ArchiveJob] done alias=%s", tenantAlias);
    }
}
