package studio.seer.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.analytics.client.AnalyticsClient;
import studio.seer.analytics.jobs.PageRankJob;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AV-12: Unit tests for PageRankJob.
 *
 * <p>Uses a stub {@link AnalyticsClient} to simulate ArcadeDB responses.
 * Tests that:
 * <ol>
 *   <li>PageRank runs and updates {@code lastRunTimestamp()} on success</li>
 *   <li>UPDATE commands are issued for each yielded row</li>
 *   <li>Empty result (empty graph) logs a warning but does NOT throw</li>
 *   <li>ArcadeDB failure (exception) is swallowed and does NOT propagate</li>
 * </ol>
 */
class PageRankJobTest {

    private PageRankJob job;
    private StubAnalyticsClient stub;

    @BeforeEach
    void setUp() throws Exception {
        job  = new PageRankJob();
        stub = new StubAnalyticsClient();
        setField(job, "analytics", stub);
        setField(job, "slowThresholdMs", 5000L);
    }

    @Test
    void run_withResults_updatesLastRun() {
        // Two vertices from CALL algo.pagerank()
        stub.callResult = List.of(
                Map.of("@rid", "#10:1", "score", 0.42),
                Map.of("@rid", "#10:2", "score", 0.17)
        );

        assertThat(job.lastRunTimestamp()).isNull();
        job.run();

        assertThat(job.lastRunTimestamp()).isNotNull();
        // Expect 2 UPDATE commands (one per row) + the CALL itself
        long updateCount = stub.commands.stream()
                .filter(c -> c.startsWith("UPDATE"))
                .count();
        assertThat(updateCount).isEqualTo(2);
    }

    @Test
    void run_emptyResult_doesNotThrow() {
        stub.callResult = List.of();

        // Should not throw even if result is empty
        job.run();

        // lastRun should remain null (no successful write)
        assertThat(job.lastRunTimestamp()).isNull();
    }

    @Test
    void run_arcadeException_doesNotPropagate() {
        stub.shouldThrow = true;

        // Must not throw
        job.run();

        assertThat(job.lastRunTimestamp()).isNull();
    }

    @Test
    void run_setsPageRankScore_inUpdateCommand() {
        stub.callResult = List.of(
                Map.of("@rid", "#10:5", "score", 0.99)
        );

        job.run();

        String updateCmd = stub.commands.stream()
                .filter(c -> c.startsWith("UPDATE #10:5"))
                .findFirst().orElse(null);

        assertThat(updateCmd).isNotNull();
        assertThat(updateCmd).contains("pagerank_score");
        assertThat(updateCmd).contains("0.99");
    }

    // ── Stub ────────────────────────────────────────────────────────────────────

    static class StubAnalyticsClient extends AnalyticsClient {
        List<Map<String, Object>> callResult = new ArrayList<>();
        List<String>              commands   = new ArrayList<>();
        boolean                   shouldThrow = false;

        @Override
        public List<Map<String, Object>> command(String cypher) {
            if (shouldThrow) throw new RuntimeException("Simulated ArcadeDB failure");
            commands.add(cypher);
            // Return callResult only for CALL statements; empty for UPDATE
            if (cypher.trim().toUpperCase().startsWith("CALL")) return callResult;
            return List.of();
        }

        @Override
        public List<Map<String, Object>> query(String cypher) {
            return List.of();
        }

        @Override
        public String dbName() { return "hound_test"; }
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
