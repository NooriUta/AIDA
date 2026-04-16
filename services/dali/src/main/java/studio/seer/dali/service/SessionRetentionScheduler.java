package studio.seer.dali.service;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Purges {@code dali_sessions} records older than {@code dali.session.retention-days} (default 30)
 * from FRIGG and the in-memory cache every day at 03:00 server time.
 *
 * <p>Safe in multi-instance deployments: each instance runs its own purge; the
 * DELETE is idempotent so concurrent purges produce no side-effects.
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

    @Inject
    SessionService sessionService;

    /** Runs daily at 03:00. Cron format: second minute hour day month weekday */
    @Scheduled(cron = "0 0 3 * * ?")
    void purgeExpiredSessions() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        log.info("SessionRetention: purging sessions older than {} days (cutoff={})", retentionDays, cutoff);
        sessionService.purgeExpired(cutoff);
    }
}
