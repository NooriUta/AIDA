package studio.seer.dali.rest;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import studio.seer.dali.storage.SessionRepository;
import studio.seer.shared.SessionStatus;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for {@link SessionResource}.
 *
 * <p>Runs with {@code @QuarkusTest} — Quarkus starts in test mode and connects to
 * FRIGG (ArcadeDB) via {@code ArcadeDbStorageProvider}.
 * JobRunr background server is started via {@link studio.seer.dali.infrastructure.JobRunrLifecycle}.
 */
@QuarkusTest
class SessionResourceTest {

    private static final String SESSIONS_URL = "/api/sessions";

    @Inject
    SessionRepository repository;

    /** BUG-SS-025: clean up FRIGG after each test to prevent cross-run accumulation. */
    @AfterEach
    void cleanup() {
        repository.deleteAll();
    }

    @Test
    void post_validInput_returns202WithQueuedSession() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                  { "dialect": "plsql", "source": "/dev/null", "preview": true }
                  """)
        .when()
            .post(SESSIONS_URL)
        .then()
            .statusCode(202)
            .contentType(ContentType.JSON)
            .body("id",      notNullValue())
            .body("status",  equalTo(SessionStatus.QUEUED.name()))
            .body("dialect", equalTo("plsql"));
    }

    @Test
    void post_missingDialect_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                  { "source": "/some/path", "preview": false }
                  """)
        .when()
            .post(SESSIONS_URL)
        .then()
            .statusCode(400);
    }

    @Test
    void get_existingSession_returns200() {
        // Create a session first
        String id = given()
            .contentType(ContentType.JSON)
            .body("""
                  { "dialect": "plsql", "source": "/dev/null", "preview": true }
                  """)
        .when()
            .post(SESSIONS_URL)
        .then()
            .statusCode(202)
            .extract().path("id");

        // Then retrieve it
        given()
        .when()
            .get(SESSIONS_URL + "/" + id)
        .then()
            .statusCode(200)
            .body("id",     equalTo(id))
            .body("status", notNullValue());
    }

    @Test
    void get_unknownSession_returns404() {
        given()
        .when()
            .get(SESSIONS_URL + "/00000000-0000-0000-0000-000000000000")
        .then()
            .statusCode(404);
    }
}
