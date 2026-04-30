package studio.seer.dali.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Tests for JobRunrStatsResource — GET /api/jobs/stats and POST /api/jobs/reset-stuck.
 *
 * <p>Integration tests against the real FRIGG instance (running in the test profile).
 * ArcadeDbStorageProvider cannot be @InjectMock'd (extends AbstractStorageProvider which
 * is a concrete class with a non-trivial constructor); the real bean is used instead.
 *
 * <p>Tests verify response shape and field presence rather than exact counter values,
 * since job counts depend on concurrent test activity.
 */
@QuarkusTest
class JobRunrStatsResourceTest {

    /** TenantContextFilter requires a valid X-Seer-Tenant-Alias on every request. */
    private static RequestSpecification withTenant() {
        return given().header("X-Seer-Tenant-Alias", "default");
    }

    // ── GET /api/jobs/stats ───────────────────────────────────────────────────

    @Test
    void stats_returns200WithAllFiveCounterFields() {
        withTenant()
        .when().get("/api/jobs/stats")
        .then()
            .statusCode(200)
            .body("enqueued",   notNullValue())
            .body("processing", notNullValue())
            .body("failed",     notNullValue())
            .body("succeeded",  notNullValue())
            .body("scheduled",  notNullValue());
    }

    @Test
    void stats_counterValuesAreNonNegative() {
        withTenant()
        .when().get("/api/jobs/stats")
        .then()
            .statusCode(200)
            .body("enqueued",   greaterThanOrEqualTo(0))
            .body("processing", greaterThanOrEqualTo(0))
            .body("failed",     greaterThanOrEqualTo(0))
            .body("succeeded",  greaterThanOrEqualTo(0))
            .body("scheduled",  greaterThanOrEqualTo(0));
    }

    // ── POST /api/jobs/reset-stuck ────────────────────────────────────────────

    @Test
    void resetStuck_returns200WithResetOkAndProcessingCount() {
        withTenant()
        .when().post("/api/jobs/reset-stuck")
        .then()
            .statusCode(200)
            .body("reset",      equalTo("ok"))
            .body("processing", greaterThanOrEqualTo(0))
            .body("failed",     greaterThanOrEqualTo(0));
    }
}
