package studio.seer.heimdall.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.heimdall.scheduler.HarvestCronRegistry;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for SchedulerResource — internal scheduler management API.
 * Verifies auth guard, register/unregister harvest, status query, archive scheduling.
 *
 * Uses the @WithDefault("changeme-internal-secret") value from SchedulerConfig
 * (no mock needed for @ConfigMapping interface).
 */
@QuarkusTest
class SchedulerResourceTest {

    // Must match @WithDefault in SchedulerConfig.internalSecret()
    private static final String VALID_SECRET = "changeme-internal-secret";
    private static final String VALID_ALIAS  = "acme-co";

    @InjectMock HarvestCronRegistry cronRegistry;

    @BeforeEach
    void setUp() {
        // No SchedulerConfig mock — default value from @WithDefault is used
    }

    // ── Auth guard (checkSecret) ───────────────────────────────────────────────

    @Test
    void registerHarvest_wrongSecret_returns401() {
        given()
            .header("X-Internal-Secret", "wrong-secret")
            .contentType(ContentType.JSON)
            .body("{\"tenantAlias\":\"" + VALID_ALIAS + "\",\"cronExpr\":\"0 0 * * *\"}")
        .when().post("/api/internal/scheduler/register-harvest")
        .then().statusCode(401);
    }

    @Test
    void unregisterHarvest_wrongSecret_returns401() {
        given()
            .header("X-Internal-Secret", "bad")
            .contentType(ContentType.JSON)
            .body("{\"tenantAlias\":\"" + VALID_ALIAS + "\"}")
        .when().post("/api/internal/scheduler/unregister-harvest")
        .then().statusCode(401);
    }

    // ── registerHarvest ───────────────────────────────────────────────────────

    @Test
    void registerHarvest_validRequest_returns202AndCallsRegistry() {
        given()
            .header("X-Internal-Secret", VALID_SECRET)
            .contentType(ContentType.JSON)
            .body("{\"tenantAlias\":\"" + VALID_ALIAS + "\",\"cronExpr\":\"0 0 * * *\"}")
        .when().post("/api/internal/scheduler/register-harvest")
        .then().statusCode(202);

        verify(cronRegistry).registerHarvestJob(VALID_ALIAS, "0 0 * * *");
    }

    @Test
    void registerHarvest_invalidAlias_returns400() {
        given()
            .header("X-Internal-Secret", VALID_SECRET)
            .contentType(ContentType.JSON)
            .body("{\"tenantAlias\":\"X\",\"cronExpr\":\"0 0 * * *\"}")
        .when().post("/api/internal/scheduler/register-harvest")
        .then().statusCode(400);

        verifyNoInteractions(cronRegistry);
    }

    // ── unregisterHarvest ─────────────────────────────────────────────────────

    @Test
    void unregisterHarvest_validRequest_returns202AndCallsRegistry() {
        given()
            .header("X-Internal-Secret", VALID_SECRET)
            .contentType(ContentType.JSON)
            .body("{\"tenantAlias\":\"" + VALID_ALIAS + "\"}")
        .when().post("/api/internal/scheduler/unregister-harvest")
        .then().statusCode(202);

        verify(cronRegistry).unregisterHarvestJob(VALID_ALIAS);
    }

    // ── harvestStatus ─────────────────────────────────────────────────────────

    @Test
    void harvestStatus_registeredTenant_returnsActiveTrue() {
        when(cronRegistry.registeredCron(VALID_ALIAS)).thenReturn("0 0 * * *");

        given()
            .header("X-Internal-Secret", VALID_SECRET)
            .queryParam("tenantAlias", VALID_ALIAS)
        .when().get("/api/internal/scheduler/harvest-status")
        .then().statusCode(200)
            .body("active", equalTo(true))
            .body("cronExpr", equalTo("0 0 * * *"))
            .body("tenantAlias", equalTo(VALID_ALIAS));
    }

    @Test
    void harvestStatus_unregisteredTenant_returnsActiveFalse() {
        when(cronRegistry.registeredCron(VALID_ALIAS)).thenReturn(null);

        given()
            .header("X-Internal-Secret", VALID_SECRET)
            .queryParam("tenantAlias", VALID_ALIAS)
        .when().get("/api/internal/scheduler/harvest-status")
        .then().statusCode(200)
            .body("active", equalTo(false));
    }

    @Test
    void harvestStatus_wrongSecret_returns401() {
        given()
            .header("X-Internal-Secret", "bad")
            .queryParam("tenantAlias", VALID_ALIAS)
        .when().get("/api/internal/scheduler/harvest-status")
        .then().statusCode(401);
    }

    // ── scheduleArchive ───────────────────────────────────────────────────────

    @Test
    void scheduleArchive_validRequest_returns202AndUnregistersHarvest() {
        given()
            .header("X-Internal-Secret", VALID_SECRET)
            .contentType(ContentType.JSON)
            .body("{\"tenantAlias\":\"" + VALID_ALIAS + "\",\"delayDays\":30}")
        .when().post("/api/internal/scheduler/schedule-archive")
        .then().statusCode(202)
            .body("tenantAlias", equalTo(VALID_ALIAS))
            .body("delayDays", equalTo(30));

        // Harvest must be unregistered before archive
        verify(cronRegistry).unregisterHarvestJob(VALID_ALIAS);
    }
}
