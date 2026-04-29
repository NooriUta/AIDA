package studio.seer.analytics;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.analytics.client.AnalyticsClient;
import studio.seer.analytics.jobs.*;
import studio.seer.analytics.rest.AnalyticsHealthResource;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * AV-12: Unit tests for AnalyticsHealthResource.
 *
 * Tests GET /api/analytics/health response shape.
 */
class AnalyticsHealthResourceTest {

    private AnalyticsHealthResource resource;
    private StubAnalyticsClient      stub;

    @BeforeEach
    void setUp() throws Exception {
        resource        = new AnalyticsHealthResource();
        stub            = new StubAnalyticsClient();

        setField(resource, "analytics",       stub);
        setField(resource, "pageRankJob",     new PageRankJob());
        setField(resource, "wccJob",          new WccJob());
        setField(resource, "hitsJob",         new HitsJob());
        setField(resource, "bridgesJob",      new BridgesJob());
        setField(resource, "articulationJob", new ArticulationJob());
        setField(resource, "sccJob",          new SccJob());
        setField(resource, "communityJob",    new CommunityJob());

        // Inject stub into each job so they don't throw NPE on dbName()
        injectStubIntoJob(resource, "pageRankJob",     stub);
        injectStubIntoJob(resource, "wccJob",          stub);
        injectStubIntoJob(resource, "hitsJob",         stub);
        injectStubIntoJob(resource, "bridgesJob",      stub);
        injectStubIntoJob(resource, "articulationJob", stub);
        injectStubIntoJob(resource, "sccJob",          stub);
        injectStubIntoJob(resource, "communityJob",    stub);
    }

    @Test
    void health_dbReachable_returns200() {
        stub.shouldThrow = false;

        Response r = resource.health();

        assertThat(r.getStatus()).isEqualTo(200);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) r.getEntity();
        assertThat(body.get("status")).isEqualTo("ok");
        assertThat(body.get("db")).isEqualTo("hound_test");
        assertThat(body.get("phase")).isEqualTo(1);
        assertThat(body).containsKey("lastRun");
        assertThat(body).containsKey("jobs");
    }

    @Test
    void health_dbUnreachable_returns503() {
        stub.shouldThrow = true;

        Response r = resource.health();

        assertThat(r.getStatus()).isEqualTo(503);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) r.getEntity();
        assertThat(body.get("status")).isEqualTo("degraded");
        assertThat(body).containsKey("dbError");
    }

    @Test
    void health_lastRunAllNull_whenNoJobsExecuted() {
        Response r = resource.health();

        @SuppressWarnings("unchecked")
        Map<String, Object> lastRun = (Map<String, Object>)
                ((Map<String, Object>) r.getEntity()).get("lastRun");

        assertThat(lastRun).containsKeys(
                "pagerank", "wcc", "hits", "bridges", "articulation", "scc", "community"
        );
        // All null before any job runs
        lastRun.values().forEach(v -> assertThat(v).isNull());
    }

    @Test
    void health_jobsListContainsAllExpected() {
        Response r = resource.health();

        @SuppressWarnings("unchecked")
        List<String> jobs = (List<String>)
                ((Map<String, Object>) r.getEntity()).get("jobs");

        assertThat(jobs).containsExactlyInAnyOrder(
                "pagerank", "wcc", "hits", "bridges", "articulation", "scc", "community"
        );
    }

    // ── Stub ────────────────────────────────────────────────────────────────────

    static class StubAnalyticsClient extends AnalyticsClient {
        boolean shouldThrow = false;

        @Override
        public List<Map<String, Object>> query(String cypher) {
            if (shouldThrow) throw new RuntimeException("DB unreachable");
            return List.of();
        }

        @Override
        public List<Map<String, Object>> command(String cypher) {
            return List.of();
        }

        @Override
        public void ddl(String ddl) { /* no-op */ }

        @Override
        public String dbName() { return "hound_test"; }
    }

    private void injectStubIntoJob(Object resource, String jobFieldName, StubAnalyticsClient stub)
            throws Exception {
        Field jobField = resource.getClass().getDeclaredField(jobFieldName);
        jobField.setAccessible(true);
        Object job = jobField.get(resource);
        try {
            Field analyticsField = job.getClass().getDeclaredField("analytics");
            analyticsField.setAccessible(true);
            analyticsField.set(job, stub);
        } catch (NoSuchFieldException ignored) {
            // Jobs that don't have analytics field (shouldn't happen)
        }
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
