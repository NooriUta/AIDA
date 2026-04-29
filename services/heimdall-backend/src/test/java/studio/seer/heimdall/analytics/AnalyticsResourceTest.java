package studio.seer.heimdall.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.shared.EventLevel;
import studio.seer.shared.HeimdallEvent;

import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UA-07: Unit tests for {@link AnalyticsResource}.
 *
 * <p>Tests the REST endpoint logic directly (without a Quarkus container)
 * by injecting a real {@link UxAggregator} via reflection.
 *
 * <p>Covers:
 * <ul>
 *   <li>Empty aggregator → 200 with zero-value summary</li>
 *   <li>Aggregator with events → 200 with correct counts</li>
 *   <li>Slow render event → appears in slowRenders list</li>
 *   <li>windowMs → 24 h constant</li>
 *   <li>Throwing aggregator → 500 with error JSON</li>
 * </ul>
 */
class AnalyticsResourceTest {

    private UxAggregator aggregator;
    private AnalyticsResource resource;

    @BeforeEach
    void setUp() throws Exception {
        aggregator = new UxAggregator();
        resource   = new AnalyticsResource();
        // Inject aggregator via reflection — no CDI container required
        Field f = AnalyticsResource.class.getDeclaredField("uxAggregator");
        f.setAccessible(true);
        f.set(resource, aggregator);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static HeimdallEvent event(String eventType, EventLevel level,
                                       String sessionId, Map<String, Object> payload) {
        return new HeimdallEvent(
                System.currentTimeMillis(), "verdandi", eventType,
                level, sessionId, null, null, 0, payload);
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    /** Empty aggregator → 200 with all-zero summary. */
    @Test
    void emptyAggregator_returns200WithZeroSummary() {
        Response resp = resource.getUxSummary();

        assertThat(resp.getStatus()).isEqualTo(200);
        UxAggregator.UxSummary summary = (UxAggregator.UxSummary) resp.getEntity();
        assertThat(summary).isNotNull();
        assertThat(summary.activeSessionCount()).isZero();
        assertThat(summary.totalEventsInWindow()).isZero();
        assertThat(summary.hotNodes()).isEmpty();
        assertThat(summary.slowRenders()).isEmpty();
        assertThat(summary.levelDistribution().info()).isZero();
        assertThat(summary.levelDistribution().warn()).isZero();
        assertThat(summary.levelDistribution().error()).isZero();
    }

    /** Events recorded → 200 with correct level counts and session count. */
    @Test
    void withEvents_returns200WithCorrectCounts() {
        // 2 LOOM_NODE_SELECTED clicks on same node
        aggregator.record(event("LOOM_NODE_SELECTED", EventLevel.INFO, "s1",
                Map.of("node_id", "table_xyz", "node_type", "DaliTable")));
        aggregator.record(event("LOOM_NODE_SELECTED", EventLevel.INFO, "s1",
                Map.of("node_id", "table_xyz", "node_type", "DaliTable")));
        // 1 WARN + 1 ERROR from different sessions
        aggregator.record(event("CYPHER_QUERY_SLOW", EventLevel.WARN, "s2",
                Map.of("duration_ms", 700L)));
        aggregator.record(event("DB_CONNECTION_ERROR", EventLevel.ERROR, "s3",
                Map.of("db", "ygg")));

        Response resp = resource.getUxSummary();

        assertThat(resp.getStatus()).isEqualTo(200);
        UxAggregator.UxSummary summary = (UxAggregator.UxSummary) resp.getEntity();
        assertThat(summary.totalEventsInWindow()).isEqualTo(4);
        assertThat(summary.activeSessionCount()).isEqualTo(3); // s1, s2, s3
        assertThat(summary.levelDistribution().info()).isEqualTo(2);
        assertThat(summary.levelDistribution().warn()).isEqualTo(1);
        assertThat(summary.levelDistribution().error()).isEqualTo(1);
        assertThat(summary.hotNodes()).hasSize(1);
        assertThat(summary.hotNodes().get(0).nodeId()).isEqualTo("table_xyz");
        assertThat(summary.hotNodes().get(0).clicks()).isEqualTo(2);
    }

    /** LOOM_VIEW_SLOW event → appears in slowRenders list. */
    @Test
    void slowRenderEvent_appearsInSummary() {
        aggregator.record(event("LOOM_VIEW_SLOW", EventLevel.WARN, "s1",
                Map.of("nodes_count", 2000, "render_time_ms", 1500L)));

        Response resp = resource.getUxSummary();

        assertThat(resp.getStatus()).isEqualTo(200);
        UxAggregator.UxSummary summary = (UxAggregator.UxSummary) resp.getEntity();
        assertThat(summary.slowRenders()).hasSize(1);
        assertThat(summary.slowRenders().get(0).nodesCount()).isEqualTo(2000);
        assertThat(summary.slowRenders().get(0).renderMs()).isEqualTo(1500);
    }

    /** windowMs is the expected 24-hour constant. */
    @Test
    void windowMs_is24Hours() {
        Response resp = resource.getUxSummary();
        UxAggregator.UxSummary summary = (UxAggregator.UxSummary) resp.getEntity();
        assertThat(summary.windowMs()).isEqualTo(UxAggregator.WINDOW_MS);
    }

    /** Throwing aggregator → 500 with error JSON body. */
    @Test
    void brokenAggregator_returns500() throws Exception {
        UxAggregator broken = new UxAggregator() {
            @Override
            public UxSummary getUxSummary() {
                throw new RuntimeException("simulated failure");
            }
        };
        Field f = AnalyticsResource.class.getDeclaredField("uxAggregator");
        f.setAccessible(true);
        f.set(resource, broken);

        Response resp = resource.getUxSummary();
        assertThat(resp.getStatus()).isEqualTo(500);
        assertThat(resp.getEntity().toString()).contains("analytics_unavailable");
    }
}
