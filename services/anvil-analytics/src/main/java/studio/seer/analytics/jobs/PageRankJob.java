package studio.seer.analytics.jobs;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.analytics.client.AnalyticsClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * AV-10: PageRank computation job (Phase 1 analytics).
 *
 * <p>Runs hourly. Calls {@code CALL algo.pagerank() YIELD node, score},
 * then writes each score back to the corresponding {@code DaliTable} vertex
 * and updates {@code analytics_updated_at}.
 *
 * <p>ArcadeDB 26.4.2 supports PageRank natively via {@code CALL algo.pagerank()}.
 * The YIELD output is: {@code @rid} (vertex RID), {@code score} (double).
 *
 * <h3>Expected performance</h3>
 * <ul>
 *   <li>5K nodes / 17K edges: ~2-5 seconds</li>
 *   <li>50K nodes: ~20-60 seconds (acceptable for hourly cron)</li>
 * </ul>
 */
@ApplicationScoped
public class PageRankJob {

    private static final Logger LOG = LoggerFactory.getLogger(PageRankJob.class);

    @Inject
    AnalyticsClient analytics;

    @ConfigProperty(name = "analytics.slow.threshold.ms", defaultValue = "5000")
    long slowThresholdMs;

    /** ISO timestamp of the last successful run. */
    private volatile String lastRun = null;

    /** Exposed for AnalyticsHealthResource. */
    public String lastRunTimestamp() { return lastRun; }

    /**
     * Cron expression from {@code analytics.pagerank.cron} (default: hourly).
     * Set to {@code off} in {@code %test} profile.
     */
    @Scheduled(cron = "{analytics.pagerank.cron}", identity = "pagerank-job")
    public void run() {
        LOG.info("[PageRank] Starting PageRank computation on db={}", analytics.dbName());
        long startMs = System.currentTimeMillis();

        try {
            // Step 1: Run the PageRank algorithm
            List<Map<String, Object>> rows = analytics.command(
                    "CALL algo.pagerank() YIELD node, score"
            );

            if (rows.isEmpty()) {
                LOG.warn("[PageRank] No results returned — graph may be empty");
                return;
            }

            // Step 2: Write scores back to DaliTable vertices
            String nowIso = Instant.now().toString();
            AtomicLong updated = new AtomicLong(0);

            for (Map<String, Object> row : rows) {
                Object rid   = row.get("@rid");
                Object score = row.get("score");
                if (rid == null || score == null) continue;

                analytics.command(String.format(
                        "UPDATE %s SET pagerank_score = %s, analytics_updated_at = date('%s') WHERE @rid = %s",
                        rid, score, nowIso, rid
                ));
                updated.incrementAndGet();
            }

            long elapsed = System.currentTimeMillis() - startMs;
            lastRun = nowIso;

            if (elapsed > slowThresholdMs) {
                LOG.warn("[PageRank] Slow run: updated {} vertices in {}ms (threshold={}ms)",
                        updated.get(), elapsed, slowThresholdMs);
            } else {
                LOG.info("[PageRank] Done: updated {} vertices in {}ms", updated.get(), elapsed);
            }

        } catch (Exception e) {
            LOG.error("[PageRank] Job failed: {}", e.getMessage(), e);
        }
    }
}
