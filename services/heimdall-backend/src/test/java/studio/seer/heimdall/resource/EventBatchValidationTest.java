package studio.seer.heimdall.resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.shared.EventLevel;
import studio.seer.shared.HeimdallEvent;

import jakarta.ws.rs.core.Response;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TC-HB-03 — HB-batch-valid: {@code POST /events/batch} must validate each event
 * individually and return structured per-index errors.
 *
 * <p>Tests the batch ingest logic in {@link EventResource} directly (without
 * Quarkus test runner) by:
 * <ul>
 *   <li>Injecting no-op stubs for {@code RingBuffer}, {@code MetricsCollector},
 *       and {@code TenantMetricsService} via reflection so no CDI container is needed.</li>
 *   <li>Calling {@link EventResource#ingestBatch} directly.</li>
 * </ul>
 *
 * <p>Also covers the static helper methods {@code requiresTenantTag()} and
 * {@code hasTenantTag()} as pure unit tests.
 */
class EventBatchValidationTest {

    private EventResource resource;

    @BeforeEach
    void setUp() throws Exception {
        resource = new EventResource();

        // Inject no-op stubs so no CDI/Quarkus container is needed
        setField(resource, "ringBuffer",     new NoOpRingBuffer());
        setField(resource, "metricsCollector", new NoOpMetricsCollector());
        setField(resource, "tenantMetrics",  new NoOpTenantMetrics());
    }

    // ── requiresTenantTag() helper ────────────────────────────────────────────

    @Test
    void requiresTenantTag_platformEvent_doesNotRequireTag() {
        assertThat(EventResource.requiresTenantTag("seer.platform.heartbeat")).isFalse();
        assertThat(EventResource.requiresTenantTag("seer.platform.startup")).isFalse();
    }

    @Test
    void requiresTenantTag_auditEvent_doesNotRequireTag() {
        assertThat(EventResource.requiresTenantTag("seer.audit.login")).isFalse();
    }

    @Test
    void requiresTenantTag_businessEvent_requiresTag() {
        assertThat(EventResource.requiresTenantTag("seer.dali.parse_complete")).isTrue();
        assertThat(EventResource.requiresTenantTag("seer.hound.query")).isTrue();
    }

    @Test
    void requiresTenantTag_nullEventType_doesNotRequireTag() {
        assertThat(EventResource.requiresTenantTag(null)).isFalse();
    }

    // ── hasTenantTag() helper ─────────────────────────────────────────────────

    @Test
    void hasTenantTag_presentAndNonBlank_returnsTrue() {
        HeimdallEvent e = event("seer.dali.parse", Map.of("tenantAlias", "acme"));
        assertThat(EventResource.hasTenantTag(e)).isTrue();
    }

    @Test
    void hasTenantTag_missing_returnsFalse() {
        HeimdallEvent e = event("seer.dali.parse", Map.of());
        assertThat(EventResource.hasTenantTag(e)).isFalse();
    }

    @Test
    void hasTenantTag_blank_returnsFalse() {
        HeimdallEvent e = event("seer.dali.parse", Map.of("tenantAlias", "  "));
        assertThat(EventResource.hasTenantTag(e)).isFalse();
    }

    @Test
    void hasTenantTag_nullPayload_returnsFalse() {
        HeimdallEvent e = new HeimdallEvent(
                System.currentTimeMillis(), "src", "seer.dali.x",
                EventLevel.INFO, null, null, null, 0, null);
        assertThat(EventResource.hasTenantTag(e)).isFalse();
    }

    // ── TC-HB-03: ingestBatch() per-index validation ──────────────────────────

    @Test
    void ingestBatch_validBatch_returns202() {
        List<HeimdallEvent> batch = List.of(
                event("seer.platform.heartbeat", Map.of()),
                event("seer.audit.login",        Map.of())
        );

        Response r = resource.ingestBatch(batch);

        assertThat(r.getStatus()).isEqualTo(202);
    }

    @Test
    void ingestBatch_nullSourceComponent_returns400WithIndex() {
        List<HeimdallEvent> batch = List.of(
                event("seer.platform.ok", Map.of()),
                eventWithNullSource("seer.platform.bad"),  // index 1 is malformed
                event("seer.platform.ok", Map.of())
        );

        Response r = resource.ingestBatch(batch);

        assertThat(r.getStatus()).isEqualTo(400);
        String body = r.getEntity().toString();
        assertThat(body).contains("malformed_event_in_batch");
        assertThat(body).contains("\"index\":1");
    }

    @Test
    void ingestBatch_nullEventType_returns400WithIndex() {
        List<HeimdallEvent> batch = new ArrayList<>();
        batch.add(event("seer.platform.ok", Map.of()));
        batch.add(eventWithNullType());          // index 1

        Response r = resource.ingestBatch(batch);

        assertThat(r.getStatus()).isEqualTo(400);
        assertThat(r.getEntity().toString()).contains("\"index\":1");
    }

    @Test
    void ingestBatch_firstEventMalformed_returns400WithIndex0() {
        List<HeimdallEvent> batch = List.of(eventWithNullSource("seer.platform.x"));

        Response r = resource.ingestBatch(batch);

        assertThat(r.getStatus()).isEqualTo(400);
        assertThat(r.getEntity().toString()).contains("\"index\":0");
    }

    @Test
    void ingestBatch_oversizedBatch_returns413() {
        List<HeimdallEvent> batch = new ArrayList<>();
        for (int i = 0; i <= EventResource.BATCH_MAX_SIZE; i++) {
            batch.add(event("seer.platform.heartbeat", Map.of()));
        }

        Response r = resource.ingestBatch(batch);

        assertThat(r.getStatus()).isEqualTo(413);
        assertThat(r.getEntity().toString())
                .contains("batch_too_large")
                .contains("\"max\":" + EventResource.BATCH_MAX_SIZE);
    }

    @Test
    void ingestBatch_exactlyMaxSize_returns202() {
        List<HeimdallEvent> batch = new ArrayList<>();
        for (int i = 0; i < EventResource.BATCH_MAX_SIZE; i++) {
            batch.add(event("seer.platform.heartbeat", Map.of()));
        }

        Response r = resource.ingestBatch(batch);

        assertThat(r.getStatus()).isEqualTo(202);
    }

    @Test
    void ingestBatch_missingTenantTag_skipsEventButAcceptsBatch() {
        // HTA-14: batch skips untagged business events but does NOT reject the whole batch
        List<HeimdallEvent> batch = List.of(
                event("seer.dali.parse_complete", Map.of()),            // no tenantAlias → skipped
                event("seer.dali.parse_complete", Map.of("tenantAlias", "acme"))  // valid
        );

        Response r = resource.ingestBatch(batch);

        assertThat(r.getStatus()).isEqualTo(202);
    }

    @Test
    void ingestBatch_emptyBatch_returns202() {
        Response r = resource.ingestBatch(List.of());
        assertThat(r.getStatus()).isEqualTo(202);
    }

    // ── helpers ────────────────────────────────────────────────────────────────

    private static HeimdallEvent event(String type, Map<String, Object> payload) {
        return new HeimdallEvent(
                System.currentTimeMillis(), "test-source", type,
                EventLevel.INFO, null, null, null, 0, payload);
    }

    private static HeimdallEvent eventWithNullSource(String type) {
        return new HeimdallEvent(
                System.currentTimeMillis(), null /* sourceComponent */, type,
                EventLevel.INFO, null, null, null, 0, Map.of());
    }

    private static HeimdallEvent eventWithNullType() {
        return new HeimdallEvent(
                System.currentTimeMillis(), "test-source", null /* eventType */,
                EventLevel.INFO, null, null, null, 0, Map.of());
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }

    // ── no-op stubs for injected dependencies ─────────────────────────────────

    private static class NoOpRingBuffer extends studio.seer.heimdall.RingBuffer {
        @Override public void push(HeimdallEvent e) {}
        @Override public void clear() {}
    }

    private static class NoOpMetricsCollector extends studio.seer.heimdall.metrics.MetricsCollector {
        @Override public void record(HeimdallEvent e) {}
    }

    private static class NoOpTenantMetrics extends studio.seer.heimdall.metrics.TenantMetricsService {
        @Override public void record(HeimdallEvent e) {}
    }
}
