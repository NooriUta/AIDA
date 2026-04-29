package studio.seer.dali.service;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.dali.config.DaliConfig;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Purges {@code dali_sessions} records older than {@code dali.session.retention-days} (default 30)
 * from FRIGG and the in-memory cache every day at 03:00 server time.
 *
 * <p>Safe in multi-instance deployments: each instance runs its own purge; the
 * DELETE is idempotent so concurrent purges produce no side-effects.
 *
 * <p>DMT-07: when {@code dali.jobrunr.worker-only=true} the cron fires but
 * immediately returns without purging — the designated scheduler pod handles
 * retention. Worker-only replicas skip this to avoid redundant FRIGG load.
 *
 * <p>Configure via env:
 * <pre>
 *   DALI_SESSION_RETENTION_DAYS=30   # days to keep sessions
 * </pre>
 */
@ApplicationScoped
public class SessionRetentionScheduler {

    private static final Logger log = LoggerFactory.getLogger(SessionRetentionScheduler.class);

    @ConfigProperty(name = "dali.session.retention-days", defaultValue = "30")
    int retentionDays;

    @Inject SessionService sessionService;
    @Inject DaliConfig config;

    /** Runs daily at 03:00. Cron format: second minute hour day month weekday */
    @Scheduled(cron = "0 0 3 * * ?")
    void purgeExpiredSessionsCron() {
        tryPurgeExpiredSessions();
    }

    /**
     * Purges expired sessions unless this node is in worker-only mode.
     *
     * @return {@code true} if purge ran, {@code false} if skipped (DMT-07 guard).
     *         Package-private for unit testing.
     */
    boolean tryPurgeExpiredSessions() {
        if (config.jobrunr().workerOnly()) {
            log.debug("SessionRetentionScheduler: worker-only mode — skipping purge (DMT-07)");
            return false;
        }
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        log.info("SessionRetention: purging sessions older than {} days (cutoff={})", retentionDays, cutoff);
        sessionService.purgeExpired(cutoff);
        return true;
    }
}
