package studio.seer.heimdall.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import studio.seer.heimdall.metrics.MetricsCollector;
import studio.seer.heimdall.metrics.MetricsSnapshot;
import studio.seer.heimdall.metrics.TenantMetricsService;
import studio.seer.heimdall.metrics.TenantMetricsSummary;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for MetricsResource — GET /metrics/snapshot and GET /metrics/tenants.
 *
 * HB-P3C-3
 */
@QuarkusTest
class MetricsResourceTest {

    @InjectMock MetricsCollector     metricsCollector;
    @InjectMock TenantMetricsService tenantMetrics;

    @Inject MetricsResource resource;

    // ── GET /metrics/snapshot ─────────────────────────────────────────────────

    @Test
    void snapshot_delegatesToMetricsCollector_returns200() {
        MetricsSnapshot snap = new MetricsSnapshot(100L, 5L, 2L, 3L, 1L, 95.5);
        when(metricsCollector.snapshot()).thenReturn(snap);

        given()
                .accept(ContentType.JSON)
        .when()
                .get("/metrics/snapshot")
        .then()
                .statusCode(200)
                .body("atomsExtracted", equalTo(100))
                .body("filesParsed",    equalTo(5));

        verify(metricsCollector).snapshot();
    }

    @Test
    void snapshot_zeroCounters_returns200() {
        when(metricsCollector.snapshot()).thenReturn(new MetricsSnapshot(0, 0, 0, 0, 0, Double.NaN));

        given().accept(ContentType.JSON)
        .when().get("/metrics/snapshot")
        .then().statusCode(200);
    }

    // ── GET /metrics/tenants ──────────────────────────────────────────────────

    @Test
    void tenants_delegatesToTenantMetricsService_returns200() {
        var counter = new TenantMetricsSummary.TenantCounter("acme", 10, 2, 50, 0, 1, 1000L);
        var totals  = new TenantMetricsSummary.TenantCounter("_total", 10, 2, 50, 0, 1, 1000L);
        var rest    = TenantMetricsSummary.TenantCounter.empty("_rest");
        when(tenantMetrics.summary())
                .thenReturn(new TenantMetricsSummary(List.of(counter), rest, totals, 1));

        given()
                .accept(ContentType.JSON)
        .when()
                .get("/metrics/tenants")
        .then()
                .statusCode(200)
                .body("tenantCount", equalTo(1))
                .body("top20[0].tenantAlias", equalTo("acme"));

        verify(tenantMetrics).summary();
    }

    @Test
    void tenants_emptyService_returns200WithZeroCounts() {
        var empty = TenantMetricsSummary.TenantCounter.empty("_total");
        when(tenantMetrics.summary())
                .thenReturn(new TenantMetricsSummary(List.of(), empty, empty, 0));

        given().accept(ContentType.JSON)
        .when().get("/metrics/tenants")
        .then().statusCode(200)
               .body("tenantCount", equalTo(0));
    }
}
