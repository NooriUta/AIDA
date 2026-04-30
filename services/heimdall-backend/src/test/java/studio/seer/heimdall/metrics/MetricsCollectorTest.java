package studio.seer.heimdall.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.shared.EventLevel;
import studio.seer.shared.HeimdallEvent;

import java.lang.reflect.Field;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 3-C: MetricsCollector unit tests — no CDI container needed.
 * MeterRegistry is injected via CDI but never called in record/snapshot/reset,
 * so we leave it null via reflection and test the AtomicLong logic directly.
 */
class MetricsCollectorTest {

    private MetricsCollector collector;

    @BeforeEach
    void setUp() throws Exception {
        collector = new MetricsCollector();
        setField(collector, "meterRegistry", null);
    }

    // ── record / snapshot ─────────────────────────────────────────────────────

    @Test
    void record_atomExtracted_incrementsAtoms() {
        collector.record(event("ATOM_EXTRACTED"));
        assertEquals(1, collector.snapshot().atomsExtracted());
    }

    @Test
    void record_fileParsed_incrementsFiles() {
        collector.record(event("FILE_PARSING_COMPLETED"));
        assertEquals(1, collector.snapshot().filesParsed());
    }

    @Test
    void record_toolCall_incrementsToolCalls() {
        collector.record(event("TOOL_CALL_COMPLETED"));
        assertEquals(1, collector.snapshot().toolCallsTotal());
    }

    @Test
    void record_workerAssigned_incrementsWorkers() {
        collector.record(event("WORKER_ASSIGNED"));
        assertEquals(1, collector.snapshot().activeWorkers());
    }

    @Test
    void record_jobEnqueued_incrementsQueue() {
        collector.record(event("JOB_ENQUEUED"));
        assertEquals(1, collector.snapshot().queueDepth());
    }

    @Test
    void record_jobCompleted_decrementsQueue() {
        collector.record(event("JOB_ENQUEUED"));
        collector.record(event("JOB_COMPLETED"));
        assertEquals(0, collector.snapshot().queueDepth());
    }

    @Test
    void record_jobCompleted_clampAtZero() {
        // Extra JOB_COMPLETED when queue already empty → clamp to 0, no negative
        collector.record(event("JOB_COMPLETED"));
        assertEquals(0, collector.snapshot().queueDepth());
    }

    @Test
    void record_unknownEventType_noChange() {
        collector.record(event("SOMETHING_ELSE"));
        MetricsSnapshot s = collector.snapshot();
        assertEquals(0, s.atomsExtracted());
        assertEquals(0, s.filesParsed());
    }

    @Test
    void record_null_noException() {
        assertDoesNotThrow(() -> collector.record(null));
    }

    // ── reset ─────────────────────────────────────────────────────────────────

    @Test
    void reset_clearsAllCounters() {
        collector.record(event("ATOM_EXTRACTED"));
        collector.record(event("FILE_PARSING_COMPLETED"));
        collector.record(event("JOB_ENQUEUED"));
        collector.reset();

        MetricsSnapshot s = collector.snapshot();
        assertEquals(0, s.atomsExtracted());
        assertEquals(0, s.filesParsed());
        assertEquals(0, s.queueDepth());
    }

    @Test
    void record_demoReset_callsReset() {
        collector.record(event("ATOM_EXTRACTED"));
        collector.record(event("DEMO_RESET"));
        assertEquals(0, collector.snapshot().atomsExtracted());
    }

    // ── resolution rate ───────────────────────────────────────────────────────

    @Test
    void snapshot_resolutionRate_NaN_whenNoFiles() {
        assertTrue(Double.isNaN(collector.snapshot().resolutionRate()));
    }

    @Test
    void snapshot_resolutionRate_computed() {
        collector.record(event("FILE_PARSING_COMPLETED"));
        collector.record(event("FILE_PARSING_COMPLETED"));
        collector.record(event("RESOLUTION_COMPLETED")); // 1 / 2 = 50%
        assertEquals(50.0, collector.snapshot().resolutionRate(), 0.001);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static HeimdallEvent event(String type) {
        return new HeimdallEvent(
                System.currentTimeMillis(),
                "test",
                type,
                EventLevel.INFO,
                null, null, null,
                0,
                Map.of()
        );
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                Field f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                f.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name + " not found in " + target.getClass());
    }
}
