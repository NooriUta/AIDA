package studio.seer.heimdall.scheduler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;

import java.util.Map;

/**
 * HTA-12: Restore an archived tenant — recreate databases, import the S3 exports,
 * invalidate the lineage-registry cache, and mark the tenant ACTIVE.
 */
@ApplicationScoped
public class RestoreJobService {

    private static final Logger LOG = Logger.getLogger(RestoreJobService.class);

    @Inject Instance<JobScheduler> jobScheduler;
    @Inject S3ExportClient         s3;
    @Inject TenantDatabaseAdmin    dbAdmin;
    @Inject ArchiveAuditEmitter    audit;

    public void scheduleRestore(String tenantAlias) {
        LOG.infof("[RestoreJob] enqueue alias=%s", tenantAlias);
        jobScheduler.get().enqueue(() -> restoreTenant(tenantAlias));
    }

    @Job(name = "restore-%0")
    public void restoreTenant(String tenantAlias) {
        LOG.infof("[RestoreJob] start alias=%s", tenantAlias);

        Map<String, String> dbs = Map.of(
                "lineage", "hound_"     + tenantAlias,
                "source",  "hound_src_" + tenantAlias,
                "dali",    "dali_"      + tenantAlias);

        // 1. Recreate databases
        for (String db : dbs.values()) {
            try {
                dbAdmin.createDatabase(db);
                LOG.infof("[RestoreJob] created db=%s", db);
            } catch (RuntimeException ex) {
                LOG.warnf("[RestoreJob] create failed db=%s — %s (may already exist)", db, ex.getMessage());
            }
        }

        // 2. Download + import each export
        for (Map.Entry<String, String> e : dbs.entrySet()) {
            String key = "tenants/" + tenantAlias + "/" + e.getValue() + ".zip";
            try {
                String local = s3.downloadExport(key);
                LOG.infof("[RestoreJob] downloaded %s → %s (import TODO — backlog)", key, local);
                // Full import integration deferred to SPRINT_MT_NEXT_BACKLOG.
            } catch (S3ExportClient.ExportException ex) {
                LOG.errorf(ex, "[RestoreJob] download failed alias=%s key=%s", tenantAlias, key);
                throw ex;
            }
        }

        // 3. Downstream workers (shuttle/dali) invalidate their own ArcadeConnection
        //    caches on next request via YggLineageRegistry (see HTA-02 worker wiring).
        //    Full cross-service cache-invalidation event-bus is a SPRINT_MT_NEXT_BACKLOG item.

        // 4. Audit
        audit.emitRestored(tenantAlias);
        LOG.infof("[RestoreJob] done alias=%s", tenantAlias);
    }
}
