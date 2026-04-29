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
 * AV-11: Weakly Connected Components (WCC) job.
 *
 * <p>Runs daily. Groups vertices by connected component.
 * Useful for identifying isolated sub-graphs in the schema lineage.
 *
 * <p>YIELD: {@code @rid}, {@code component} (component id string).
 */
@ApplicationScoped
public class WccJob {

    private static final Logger LOG = LoggerFactory.getLogger(WccJob.class);

    @Inject
    AnalyticsClient analytics;

    private volatile String lastRun = null;
    public String lastRunTimestamp() { return lastRun; }

    @Scheduled(cron = "{analytics.wcc.cron}", identity = "wcc-job")
    public void run() {
        LOG.info("[WCC] Starting WCC computation on db={}", analytics.dbName());
        long startMs = System.currentTimeMillis();

        try {
            List<Map<String, Object>> rows = analytics.command(
                    "CALL algo.wcc() YIELD node, component"
            );

            if (rows.isEmpty()) {
                LOG.warn("[WCC] No results returned — graph may be empty");
                return;
            }

            String nowIso = Instant.now().toString();
            int updated = 0;

            for (Map<String, Object> row : rows) {
                Object rid       = row.get("@rid");
                Object component = row.get("component");
                if (rid == null || component == null) continue;

                analytics.command(String.format(
                        "UPDATE %s SET wcc_component_id = '%s', analytics_updated_at = date('%s') WHERE @rid = %s",
                        rid, component.toString().replace("'", "\\'"), nowIso, rid
                ));
                updated++;
            }

            lastRun = nowIso;
            LOG.info("[WCC] Done: assigned {} vertices to components in {}ms",
                    updated, System.currentTimeMillis() - startMs);

        } catch (Exception e) {
            LOG.error("[WCC] Job failed: {}", e.getMessage(), e);
        }
    }
}
