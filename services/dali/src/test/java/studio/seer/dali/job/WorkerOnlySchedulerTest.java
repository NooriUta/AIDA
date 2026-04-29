package studio.seer.dali.job;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.junit.QuarkusTestProfile;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import studio.seer.dali.service.SessionRetentionScheduler;

import java.lang.reflect.Method;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DMT-07: Verifies that {@code dali.jobrunr.worker-only=true} suppresses
 * {@link HarvestScheduler} and {@link SessionRetentionScheduler} cron firings.
 *
 * <p>Before fix: {@code worker-only} only disabled the JobRunr HTTP dashboard.
 * Quarkus {@code @Scheduled} beans still fired on every replica — in a 3-replica
 * deployment nightly harvest was enqueued 3× and session purge ran 3× concurrently.
 *
 * <p>After fix: {@code tryEnqueueHarvest()} / {@code tryPurgeExpiredSessions()} return
 * {@code false} immediately; no job is enqueued, no FRIGG writes occur on worker pods.
 */
@QuarkusTest
@TestProfile(WorkerOnlySchedulerTest.WorkerOnlyProfile.class)
class WorkerOnlySchedulerTest {

    /** Overrides worker-only to true for this test class only. */
    public static class WorkerOnlyProfile implements QuarkusTestProfile {
        @Override
        public Map<String, String> getConfigOverrides() {
            return Map.of("dali.jobrunr.worker-only", "true");
        }
    }

    @Inject HarvestScheduler harvestScheduler;
    @Inject SessionRetentionScheduler sessionRetentionScheduler;

    @Test
    void harvest_skipped_when_worker_only() {
        boolean enqueued = harvestScheduler.tryEnqueueHarvest();
        assertThat(enqueued)
            .as("worker-only=true: HarvestScheduler must skip enqueue (DMT-07)")
            .isFalse();
    }

    @Test
    void session_purge_skipped_when_worker_only() throws Exception {
        // tryPurgeExpiredSessions() is package-private in studio.seer.dali.service —
        // reflective access is intentional for cross-package testing.
        Method m = SessionRetentionScheduler.class
                .getDeclaredMethod("tryPurgeExpiredSessions");
        m.setAccessible(true);
        boolean ran = (boolean) m.invoke(sessionRetentionScheduler);
        assertThat(ran)
            .as("worker-only=true: SessionRetentionScheduler must skip purge (DMT-07)")
            .isFalse();
    }
}
