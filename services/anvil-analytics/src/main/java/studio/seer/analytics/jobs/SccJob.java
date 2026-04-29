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
 * AV-11: Strongly Connected Components (SCC) job.
 *
 * <p>Runs weekly. SCC finds groups of vertices where every vertex
 * is reachable from every other vertex (mutual dependencies). In SQL
 * lineage, this often indicates circular view dependencies or
 * tightly coupled schema clusters.
 *
 * <p>YIELD: {@code @rid}, {@code component} (component id).
 */
@ApplicationScoped
public class SccJob {

    private static final Logger LOG = LoggerFactory.getLogger(SccJob.class);

    @Inject
    AnalyticsClient analytics;

    private volatile String lastRun = null;
    public String lastRunTimestamp() { return lastRun; }

    @Scheduled(cron = "{analytics.scc.cron}", identity = "scc-job")
    public void run() {
        LOG.info("[SCC] Starting SCC computation on db={}", analytics.dbName());
        long startMs = System.currentTimeMillis();

        try {
            List<Map<String, Object>> rows = analytics.command(
                    "CALL algo.scc() YIELD node, component"
            );

            if (rows.isEmpty()) {
                LOG.warn("[SCC] No results returned — graph may be empty");
                return;
            }

            String nowIso = Instant.now().toString();
            int updated = 0;

            for (Map<String, Object> row : rows) {
                Object rid       = row.get("@rid");
                Object component = row.get("component");
                if (rid == null || component == null) continue;

                analytics.command(String.format(
                        "UPDATE %s SET scc_component_id = '%s', analytics_updated_at = date('%s') WHERE @rid = %s",
                        rid, component.toString().replace("'", "\\'"), nowIso, rid
                ));
                updated++;
            }

            lastRun = nowIso;
            LOG.info("[SCC] Done: assigned {} vertices to SCC components in {}ms",
                    updated, System.currentTimeMillis() - startMs);

        } catch (Exception e) {
            LOG.error("[SCC] Job failed: {}", e.getMessage(), e);
        }
    }
}
