package studio.seer.heimdall.resource;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Tests for ServicesResource — restart() auth and guard logic.
 * Does not invoke actual Docker; only verifies HTTP guard conditions.
 */
@QuarkusTest
class ServicesResourceTest {

    // ── restart() guard logic ─────────────────────────────────────────────────

    @Test
    void restart_nonAdminRole_returns403() {
        given()
            .header("X-Seer-Role", "viewer")
            .queryParam("mode", "docker")
        .when().post("/services/dali/restart")
        .then().statusCode(403);
    }

    @Test
    void restart_noRole_returns403() {
        given()
            .queryParam("mode", "docker")
        .when().post("/services/dali/restart")
        .then().statusCode(403);
    }

    @Test
    void restart_nonDockerMode_returns400() {
        given()
            .header("X-Seer-Role", "admin")
            .queryParam("mode", "dev")
        .when().post("/services/dali/restart")
        .then().statusCode(400)
            .body("error", containsString("Docker"));
    }

    @Test
    void restart_protectedService_returns403() {
        // heimdall-backend is in NO_RESTART set
        given()
            .header("X-Seer-Role", "admin")
            .queryParam("mode", "docker")
        .when().post("/services/heimdall-backend/restart")
        .then().statusCode(403)
            .body("error", equalTo("protected service"));
    }
}
