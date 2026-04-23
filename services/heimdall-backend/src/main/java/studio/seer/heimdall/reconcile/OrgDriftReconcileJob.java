package studio.seer.heimdall.reconcile;

import io.quarkus.arc.Unremovable;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * MTN-26 — Daily scheduled wrapper around {@link OrgDriftReconciler#run()}.
 *
 * <p>Runs at 03:00 UTC via Quarkus Scheduler. Emits
 * {@code seer.audit.tenant_kc_drift_detected} through standard HEIMDALL event
 * pipeline when the report is not clean.
 *
 * <p>Manual on-demand invocation goes through
 * {@code POST /api/admin/reconcile-orgs} — see
 * {@link studio.seer.heimdall.resource.ReconcileResource}.
 */
@Unremovable
@ApplicationScoped
public class OrgDriftReconcileJob {

    private static final Logger LOG = Logger.getLogger(OrgDriftReconcileJob.class);

    @Inject OrgDriftReconciler reconciler;

    /** "0 0 3 * * ?" — every day at 03:00 UTC. */
    @Scheduled(cron = "0 0 3 * * ?", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void runDaily() {
        LOG.info("[MTN-26] daily org-drift reconcile tick");
        OrgDriftReconciler.Report report = reconciler.run();
        if (!report.clean()) {
            LOG.warnf("[MTN-26] drift detected runId=%s friggOrphan=%d kcOrphan=%d aliasMismatch=%d",
                    report.runId(),
                    report.friggOrphan().size(),
                    report.kcOrphan().size(),
                    report.aliasMismatch().size());
        }
    }
}
