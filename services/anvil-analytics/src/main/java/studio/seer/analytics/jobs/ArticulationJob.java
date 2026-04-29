package studio.seer.analytics.jobs;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.analytics.client.AnalyticsClient;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * AV-11: Articulation point (cut-vertex) detection job.
 *
 * <p>Runs weekly (same cron as BridgesJob — Sunday topology window).
 * Marks vertices that are articulation points: removing them increases
 * the number of connected components. These are high-impact tables
 * in the schema lineage.
 *
 * <p>YIELD: {@code node} (vertex RID).
 *
 * <p>Strategy: reset all {@code is_articulation_point=false} first,
 * then mark found vertices as {@code true}.
 */
@ApplicationScoped
public class ArticulationJob {

    private static final Logger LOG = LoggerFactory.getLogger(ArticulationJob.class);

    @Inject
    AnalyticsClient analytics;

    private volatile String lastRun = null;
    public String lastRunTimestamp() { return lastRun; }

    @Scheduled(cron = "{analytics.topology.cron}", identity = "articulation-job")
    public void run() {
        LOG.info("[Articulation] Starting articulation point detection on db={}", analytics.dbName());
        long startMs = System.currentTimeMillis();

        try {
            // Reset all before re-marking
            analytics.command("UPDATE DaliTable SET is_articulation_point = false");

            List<Map<String, Object>> rows = analytics.command(
                    "CALL algo.articulationPoints() YIELD node"
            );

            String nowIso = Instant.now().toString();
            int count = 0;

            for (Map<String, Object> row : rows) {
                Object rid = row.get("@rid") != null ? row.get("@rid") : row.get("node");
                if (rid == null) continue;

                analytics.command(String.format(
                        "UPDATE %s SET is_articulation_point = true, analytics_updated_at = date('%s') WHERE @rid = %s",
                        rid, nowIso, rid
                ));
                count++;
            }

            lastRun = nowIso;
            LOG.info("[Articulation] Done: found {} articulation points in {}ms",
                    count, System.currentTimeMillis() - startMs);

        } catch (Exception e) {
            LOG.error("[Articulation] Job failed: {}", e.getMessage(), e);
        }
    }
}
