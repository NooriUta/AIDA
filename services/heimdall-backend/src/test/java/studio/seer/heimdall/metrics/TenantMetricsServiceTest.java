package studio.seer.heimdall.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.heimdall.metrics.TenantMetricsSummary.TenantCounter;
import studio.seer.shared.EventLevel;
import studio.seer.shared.HeimdallEvent;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TenantMetricsService — per-tenant in-memory event counters.
 *
 * Uses direct instantiation (no CDI needed — no @Inject dependencies).
 *
 * HB-P3C-1
 */
class TenantMetricsServiceTest {

    private TenantMetricsService service;

    @BeforeEach
    void setUp() {
        service = new TenantMetricsService();
    }

    // ── record() — basic routing ──────────────────────────────────────────────

    @Test
    void record_nullEvent_noNpe() {
        assertDoesNotThrow(() -> service.record(null));
        assertTrue(service.raw().isEmpty());
    }

    @Test
    void record_withTenantAlias_routesToTenant() {
        service.record(event("SESSION_STARTED", EventLevel.INFO, "acme"));

        assertThat(service.raw()).containsKey("acme");
        TenantCounter c = service.raw().get("acme");
        assertEquals(1, c.totalEvents());
        assertEquals(1, c.parseSessions());
    }

    @Test
    void record_withTenantField_routesToTenant() {
        // payload with "tenant" key (alternate field) should also route correctly
        HeimdallEvent ev = new HeimdallEvent(
                System.currentTimeMillis(), "dali", "SESSION_STARTED",
                EventLevel.INFO, null, null, null, 0,
                Map.of("tenant", "beta"));
        service.record(ev);

        assertThat(service.raw()).containsKey("beta");
    }

    @Test
    void record_noTenantAlias_routesToPlatformKey() {
        HeimdallEvent ev = new HeimdallEvent(
                System.currentTimeMillis(), "dali", "SESSION_STARTED",
                EventLevel.INFO, null, null, null, 0, Map.of());
        service.record(ev);

        assertThat(service.raw()).containsKey(TenantMetricsService.PLATFORM_KEY);
    }

    @Test
    void record_nullPayload_routesToPlatformKey() {
        HeimdallEvent ev = new HeimdallEvent(
                System.currentTimeMillis(), "dali", "SESSION_STARTED",
                EventLevel.INFO, null, null, null, 0, Map.of());
        service.record(ev);

        assertThat(service.raw()).containsKey(TenantMetricsService.PLATFORM_KEY);
    }

    // ── record() — counter increment logic ───────────────────────────────────

    @Test
    void record_sessionStarted_incrementsParseSessions() {
        service.record(event("SESSION_STARTED", EventLevel.INFO, "acme"));
        assertEquals(1, service.raw().get("acme").parseSessions());
    }

    @Test
    void record_atomExtracted_incrementsAtoms() {
        service.record(event("ATOM_EXTRACTED", EventLevel.INFO, "acme"));
        assertEquals(1, service.raw().get("acme").atoms());
    }

    @Test
    void record_errorLevel_incrementsErrors() {
        service.record(event("FILE_PARSING_FAILED", EventLevel.ERROR, "acme"));
        assertEquals(1, service.raw().get("acme").errors());
    }

    @Test
    void record_infoLevel_noErrorIncrement() {
        service.record(event("SESSION_STARTED", EventLevel.INFO, "acme"));
        assertEquals(0, service.raw().get("acme").errors());
    }

    @Test
    void record_jobStarted_incrementsActiveJobs() {
        service.record(event("JOB_STARTED", EventLevel.INFO, "acme"));
        assertEquals(1, service.raw().get("acme").activeJobs());
    }

    @Test
    void record_jobCompleted_decrementsActiveJobs() {
        service.record(event("JOB_STARTED",    EventLevel.INFO, "acme"));
        service.record(event("JOB_COMPLETED",  EventLevel.INFO, "acme"));
        assertEquals(0, service.raw().get("acme").activeJobs());
    }

    @Test
    void record_jobFailed_decrementsActiveJobs() {
        service.record(event("JOB_STARTED", EventLevel.INFO, "acme"));
        service.record(event("JOB_FAILED",  EventLevel.INFO, "acme"));
        assertEquals(0, service.raw().get("acme").activeJobs());
    }

    @Test
    void record_activeJobsFloorAtZero() {
        // JOB_COMPLETED without prior JOB_STARTED must not go negative
        service.record(event("JOB_COMPLETED", EventLevel.INFO, "acme"));
        assertEquals(0, service.raw().get("acme").activeJobs());
    }

    @Test
    void record_multipleTenants_isolatedCounters() {
        service.record(event("SESSION_STARTED", EventLevel.INFO, "acme"));
        service.record(event("SESSION_STARTED", EventLevel.INFO, "acme"));
        service.record(event("SESSION_STARTED", EventLevel.INFO, "beta"));

        assertEquals(2, service.raw().get("acme").parseSessions());
        assertEquals(1, service.raw().get("beta").parseSessions());
    }

    // ── summary() ─────────────────────────────────────────────────────────────

    @Test
    void summary_empty_returnsZeroTotals() {
        TenantMetricsSummary s = service.summary();

        assertEquals(0, s.tenantCount());
        assertEquals(0, s.totals().totalEvents());
        assertTrue(s.top20().isEmpty());
    }

    @Test
    void summary_singleTenant_appearsInTop20() {
        service.record(event("SESSION_STARTED", EventLevel.INFO, "acme"));
        service.record(event("SESSION_STARTED", EventLevel.INFO, "acme"));

        TenantMetricsSummary s = service.summary();

        assertEquals(1, s.tenantCount());
        assertEquals(1, s.top20().size());
        assertEquals("acme", s.top20().get(0).tenantAlias());
        assertEquals(2, s.top20().get(0).totalEvents());
        assertEquals(2, s.totals().totalEvents());
    }

    @Test
    void summary_totals_sumAllTenants() {
        service.record(event("ATOM_EXTRACTED", EventLevel.INFO, "acme"));
        service.record(event("ATOM_EXTRACTED", EventLevel.INFO, "beta"));
        service.record(event("FILE_PARSING_FAILED", EventLevel.ERROR, "acme"));

        TenantMetricsSummary s = service.summary();

        assertEquals(3, s.totals().totalEvents());
        assertEquals(2, s.totals().atoms());
        assertEquals(1, s.totals().errors());
    }

    // ── raw() ─────────────────────────────────────────────────────────────────

    @Test
    void raw_returnsUnmodifiableSnapshot() {
        service.record(event("SESSION_STARTED", EventLevel.INFO, "acme"));
        Map<String, TenantCounter> raw = service.raw();

        assertNotNull(raw);
        assertEquals(1, raw.size());
        assertThrows(UnsupportedOperationException.class, () -> raw.put("x", null));
    }

    // ── helper ────────────────────────────────────────────────────────────────

    private static HeimdallEvent event(String type, EventLevel level, String tenantAlias) {
        return new HeimdallEvent(
                System.currentTimeMillis(), "dali", type, level,
                null, null, null, 0,
                Map.of("tenantAlias", tenantAlias));
    }
}
