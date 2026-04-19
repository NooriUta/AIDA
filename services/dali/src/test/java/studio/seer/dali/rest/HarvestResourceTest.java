package studio.seer.dali.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import studio.seer.dali.storage.SessionRepository;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * E2E integration test for the harvest endpoint (DS-03).
 *
 * <p>Covers the chain: POST /api/sessions/harvest → HarvestJob enqueued in JobRunr.
 * No real JDBC sources are configured in the test profile, so HarvestJob is a no-op,
 * but the REST endpoint + JobRunr enqueue path is fully exercised.
 *
 * <p>For a full end-to-end test with live JDBC sources, run with {@code -Dintegration=true}.
 */
@QuarkusTest
class HarvestResourceTest {

    @Inject
    SessionRepository repository;

    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }

    // ── POST /api/sessions/harvest ────────────────────────────────────────────

    @Test
    void postHarvest_returns202WithHarvestId() {
        given()
        .when()
            .post("/api/sessions/harvest")
        .then()
            .statusCode(202)
            .contentType(ContentType.JSON)
            .body("harvestId", notNullValue())
            .body("harvestId", startsWith("harvest-"))
            .body("status",    equalTo("enqueued"));
    }

    @Test
    void postHarvest_harvestIdIsUnique() {
        String id1 = given()
            .when().post("/api/sessions/harvest")
            .then().statusCode(202).extract().path("harvestId");

        String id2 = given()
            .when().post("/api/sessions/harvest")
            .then().statusCode(202).extract().path("harvestId");

        org.junit.jupiter.api.Assertions.assertNotEquals(id1, id2,
            "Each harvest invocation must generate a unique harvestId");
    }

    @Test
    void postHarvest_withContentTypeJson_stillAccepted() {
        given()
            .contentType(ContentType.JSON)
        .when()
            .post("/api/sessions/harvest")
        .then()
            .statusCode(202)
            .body("harvestId", notNullValue());
    }
}
