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
 * AV-11: Bridge detection job.
 *
 * <p>Runs weekly (Sunday). Marks edges that are bridges (cut-edges) —
 * removing them disconnects the graph. These represent critical dependencies
 * in the schema lineage.
 *
 * <p>YIELD: {@code edge} (edge RID of a bridge).
 *
 * <p>Strategy: first reset all {@code is_on_bridge=false} on DaliRelation,
 * then mark bridges found by the algorithm as {@code true}.
 */
@ApplicationScoped
public class BridgesJob {

    private static final Logger LOG = LoggerFactory.getLogger(BridgesJob.class);

    @Inject
    AnalyticsClient analytics;

    private volatile String lastRun = null;
    public String lastRunTimestamp() { return lastRun; }

    @Scheduled(cron = "{analytics.topology.cron}", identity = "bridges-job")
    public void run() {
        LOG.info("[Bridges] Starting bridge detection on db={}", analytics.dbName());
        long startMs = System.currentTimeMillis();

        try {
            // Reset all edges before re-marking
            analytics.command("UPDATE DaliRelation SET is_on_bridge = false");

            List<Map<String, Object>> rows = analytics.command(
                    "CALL algo.bridges() YIELD edge"
            );

            String nowIso = Instant.now().toString();
            int bridgeCount = 0;

            for (Map<String, Object> row : rows) {
                Object rid = row.get("@rid") != null ? row.get("@rid") : row.get("edge");
                if (rid == null) continue;

                analytics.command(String.format(
                        "UPDATE %s SET is_on_bridge = true WHERE @rid = %s", rid, rid
                ));
                bridgeCount++;
            }

            lastRun = nowIso;
            LOG.info("[Bridges] Done: found {} bridge edges in {}ms",
                    bridgeCount, System.currentTimeMillis() - startMs);

        } catch (Exception e) {
            LOG.error("[Bridges] Job failed: {}", e.getMessage(), e);
        }
    }
}
