package studio.seer.analytics.jobs;

import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.analytics.client.AnalyticsClient;

import java.time.Instant;

/**
 * AV-12: Community assignment job (Phase 0 — schema grouping).
 *
 * <p>Runs daily. Assigns {@code community_id} to each vertex based on
 * the schema it belongs to (identified via {@code schema_geoid} property).
 *
 * <p><b>Phase 0 (current):</b> community = schema group.
 * {@code UPDATE DaliTable SET community_id = schema_geoid} where schema_geoid is not null.
 *
 * <p><b>Phase 1 (future):</b> Replace with Louvain modularity algorithm
 * ({@code CALL algo.louvain()}) when ArcadeDB supports it natively.
 * Tracked in Sprint LOOM-5K backlog.
 *
 * <p>Phase 0 is intentionally simple — community_id enables grouping in LOOM LOD view
 * even without a proper clustering algorithm.
 */
@ApplicationScoped
public class CommunityJob {

    private static final Logger LOG = LoggerFactory.getLogger(CommunityJob.class);

    @Inject
    studio.seer.analytics.client.AnalyticsClient analytics;

    private volatile String lastRun = null;
    public String lastRunTimestamp() { return lastRun; }

    @Scheduled(cron = "{analytics.community.cron}", identity = "community-job")
    public void run() {
        LOG.info("[Community] Starting community assignment (Phase 0) on db={}", analytics.dbName());
        long startMs = System.currentTimeMillis();

        try {
            // Phase 0: community = schema_geoid (group tables by schema)
            analytics.command(
                    "UPDATE DaliTable SET community_id = schema_geoid WHERE schema_geoid IS NOT NULL"
            );

            String nowIso = Instant.now().toString();
            lastRun = nowIso;
            LOG.info("[Community] Done (Phase 0 — schema grouping) in {}ms",
                    System.currentTimeMillis() - startMs);

        } catch (Exception e) {
            LOG.error("[Community] Job failed: {}", e.getMessage(), e);
        }
    }
}
