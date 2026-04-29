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
 * AV-12: HITS (Hyperlink-Induced Topic Search) job.
 *
 * <p>Runs daily. Computes hub and authority scores for each vertex.
 * In schema lineage:
 * <ul>
 *   <li><b>Hub score</b> — table that references many other tables (e.g. a fact table)</li>
 *   <li><b>Authority score</b> — table referenced by many others (e.g. a central dimension)</li>
 * </ul>
 *
 * <p>YIELD: {@code @rid}, {@code hub}, {@code authority}.
 */
@ApplicationScoped
public class HitsJob {

    private static final Logger LOG = LoggerFactory.getLogger(HitsJob.class);

    @Inject
    AnalyticsClient analytics;

    private volatile String lastRun = null;
    public String lastRunTimestamp() { return lastRun; }

    @Scheduled(cron = "{analytics.hits.cron}", identity = "hits-job")
    public void run() {
        LOG.info("[HITS] Starting HITS computation on db={}", analytics.dbName());
        long startMs = System.currentTimeMillis();

        try {
            List<Map<String, Object>> rows = analytics.command(
                    "CALL algo.hits() YIELD node, hub, authority"
            );

            if (rows.isEmpty()) {
                LOG.warn("[HITS] No results returned — graph may be empty");
                return;
            }

            String nowIso = Instant.now().toString();
            int updated = 0;

            for (Map<String, Object> row : rows) {
                Object rid       = row.get("@rid");
                Object hub       = row.get("hub");
                Object authority = row.get("authority");
                if (rid == null) continue;

                double hubVal = hub       instanceof Number n ? n.doubleValue() : 0.0;
                double authVal= authority instanceof Number n ? n.doubleValue() : 0.0;

                analytics.command(String.format(
                        "UPDATE %s SET hub_score = %s, authority_score = %s, analytics_updated_at = date('%s') WHERE @rid = %s",
                        rid, hubVal, authVal, nowIso, rid
                ));
                updated++;
            }

            lastRun = nowIso;
            LOG.info("[HITS] Done: updated {} vertices in {}ms",
                    updated, System.currentTimeMillis() - startMs);

        } catch (Exception e) {
            LOG.error("[HITS] Job failed: {}", e.getMessage(), e);
        }
    }
}
