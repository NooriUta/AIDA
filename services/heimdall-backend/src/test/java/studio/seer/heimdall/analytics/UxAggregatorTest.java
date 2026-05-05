package studio.seer.heimdall.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.shared.EventLevel;
import studio.seer.shared.HeimdallEvent;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UA-07: Unit tests for {@link UxAggregator}.
 *
 * <p>Tests run without a CDI container — {@link UxAggregator} is instantiated directly.
 */
class UxAggregatorTest {

    private UxAggregator aggregator;

    @BeforeEach
    void setUp() {
        aggregator = new UxAggregator();
    }

    // ── Empty state ────────────────────────────────────────────────────────────

    @Test
    void emptySummary_hasZeroCounts() {
        UxAggregator.UxSummary s = aggregator.getUxSummary();

        assertThat(s.hotNodes()).isEmpty();
        assertThat(s.slowRenders()).isEmpty();
        assertThat(s.activeSessionCount()).isZero();
        assertThat(s.totalEventsInWindow()).isZero();
        assertThat(s.levelDistribution().info()).isZero();
        assertThat(s.levelDistribution().warn()).isZero();
        assertThat(s.levelDistribution().error()).isZero();
    }

    // ── Hot nodes ─────────────────────────────────────────────────────────────

    @Test
    void hotNodes_countsByNodeId() {
        aggregator.record(loomNodeSelected("node-A", "DaliTable",  "s1"));
        aggregator.record(loomNodeSelected("node-A", "DaliTable",  "s2"));
        aggregator.record(loomNodeSelected("node-B", "DaliSchema", "s3"));

        UxAggregator.UxSummary s = aggregator.getUxSummary();
        assertThat(s.hotNodes()).hasSize(2);
        assertThat(s.hotNodes().get(0).nodeId()).isEqualTo("node-A");
        assertThat(s.hotNodes().get(0).clicks()).isEqualTo(2);
        assertThat(s.hotNodes().get(1).nodeId()).isEqualTo("node-B");
        assertThat(s.hotNodes().get(1).clicks()).isEqualTo(1);
    }

    @Test
    void hotNodes_cappedAtTopN() {
        // Insert more than HOT_NODES_TOP_N distinct nodes
        for (int i = 0; i < UxAggregator.HOT_NODES_TOP_N + 5; i++) {
            aggregator.record(loomNodeSelected("node-" + i, "DaliTable", "s" + i));
        }
        UxAggregator.UxSummary s = aggregator.getUxSummary();
        assertThat(s.hotNodes().size()).isLessThanOrEqualTo(UxAggregator.HOT_NODES_TOP_N);
    }

    // ── Level distribution ────────────────────────────────────────────────────

    @Test
    void levelDistribution_countsAllLevels() {
        aggregator.record(event("LOOM_NODE_SELECTED", EventLevel.INFO,  null, null));
        aggregator.record(event("LOOM_NODE_SELECTED", EventLevel.INFO,  null, null));
        aggregator.record(event("CYPHER_QUERY_SLOW",  EventLevel.WARN,  null, null));
        aggregator.record(event("DB_CONNECTION_ERROR", EventLevel.ERROR, null, null));

        UxAggregator.UxSummary s = aggregator.getUxSummary();
        assertThat(s.levelDistribution().info()).isEqualTo(2);
        assertThat(s.levelDistribution().warn()).isEqualTo(1);
        assertThat(s.levelDistribution().error()).isEqualTo(1);
    }

    // ── Slow renders ──────────────────────────────────────────────────────────

    @Test
    void slowRenders_recordedAndReturnedMostRecentFirst() throws InterruptedException {
        aggregator.record(loomViewSlow(100, 800,  "s1"));
        Thread.sleep(2); // ensure different timestamps
        aggregator.record(loomViewSlow(200, 1200, "s2"));

        UxAggregator.UxSummary s = aggregator.getUxSummary();
        assertThat(s.slowRenders()).hasSize(2);
        // Most recent first (highest timestamp → renderMs=1200 was recorded later)
        assertThat(s.slowRenders().get(0).renderMs()).isEqualTo(1200);
    }

    @Test
    void slowRenders_cappedAtMax() {
        for (int i = 0; i < UxAggregator.SLOW_RENDER_MAX + 5; i++) {
            aggregator.record(loomViewSlow(i + 1, 600 + i, "s" + i));
        }
        UxAggregator.UxSummary s = aggregator.getUxSummary();
        assertThat(s.slowRenders().size()).isLessThanOrEqualTo(UxAggregator.SLOW_RENDER_MAX);
    }

    // ── Event type counts ──────────────────────────────────────────────────────

    @Test
    void eventTypeCounts_tracksVerdandiEventsByType() {
        aggregator.record(loomNodeSelected("n1", "DaliTable", "s1"));
        aggregator.record(loomNodeSelected("n2", "DaliTable", "s1"));
        aggregator.record(loomNodeSelected("n3", "DaliTable", "s1"));
        aggregator.record(event("LOOM_FILTER_APPLIED", EventLevel.INFO, "s1", null));
        aggregator.record(event("LOOM_DRILL_DOWN",     EventLevel.INFO, "s1", null));
        // non-verdandi event — should NOT appear in counts
        aggregator.record(new HeimdallEvent(
                System.currentTimeMillis(), "dali", "HOUND_PARSE_STARTED",
                EventLevel.INFO, "s1", null, null, 0, Map.of()));

        UxAggregator.UxSummary s = aggregator.getUxSummary();
        assertThat(s.eventTypeCounts()).hasSize(3);
        assertThat(s.eventTypeCounts().get(0).eventType()).isEqualTo("LOOM_NODE_SELECTED");
        assertThat(s.eventTypeCounts().get(0).count()).isEqualTo(3);
    }

    // ── Active sessions ───────────────────────────────────────────────────────

    @Test
    void activeSessionCount_distinctSessionIds() {
        aggregator.record(event("LOOM_NODE_SELECTED", EventLevel.INFO,  "session-1", null));
        aggregator.record(event("LOOM_NODE_SELECTED", EventLevel.INFO,  "session-1", null)); // duplicate
        aggregator.record(event("LOOM_VIEW_SLOW",     EventLevel.WARN,  "session-2", null));

        UxAggregator.UxSummary s = aggregator.getUxSummary();
        assertThat(s.activeSessionCount()).isEqualTo(2);
    }

    @Test
    void nullRecord_isIgnoredGracefully() {
        aggregator.record(null);
        UxAggregator.UxSummary s = aggregator.getUxSummary();
        assertThat(s.totalEventsInWindow()).isZero();
    }

    @Test
    void maxEvents_oldestDropped() {
        for (int i = 0; i < UxAggregator.MAX_EVENTS + 10; i++) {
            aggregator.record(event("LOOM_NODE_SELECTED", EventLevel.INFO, "s" + i, null));
        }
        UxAggregator.UxSummary s = aggregator.getUxSummary();
        assertThat(s.totalEventsInWindow()).isLessThanOrEqualTo(UxAggregator.MAX_EVENTS);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private HeimdallEvent event(String type, EventLevel level, String sessionId, String userId) {
        return new HeimdallEvent(
                System.currentTimeMillis(), "verdandi", type,
                level, sessionId, userId, null, 0, Map.of());
    }

    private HeimdallEvent loomNodeSelected(String nodeId, String nodeType, String sessionId) {
        return new HeimdallEvent(
                System.currentTimeMillis(), "verdandi", "LOOM_NODE_SELECTED",
                EventLevel.INFO, sessionId, null, null, 0,
                Map.of("nodeId", nodeId, "nodeType", nodeType));
    }

    private HeimdallEvent loomViewSlow(int nodesCount, long renderMs, String sessionId) {
        return new HeimdallEvent(
                System.currentTimeMillis(), "verdandi", "LOOM_VIEW_SLOW",
                EventLevel.WARN, sessionId, null, null, renderMs,
                Map.of("nodeCount", nodesCount));
    }
}
