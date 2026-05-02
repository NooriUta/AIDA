package com.mimir;

import com.mimir.model.AskRequest;
import com.mimir.orchestration.MimirOrchestrator;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

/**
 * Error path tests — ensure MIMIR не валит запрос при upstream failure.
 *
 * Tests:
 * - 429 Too Many Requests → MimirAnswer.unavailable() (not 500)
 * - timeout → MimirAnswer.unavailable() (not 500)
 * - generic exception → MimirAnswer.unavailable()
 */
@QuarkusTest
class AskResourceErrorTest {

    @InjectMock MimirOrchestrator orchestrator;

    @Test
    void deepSeek429ReturnsUnavailableNotError() {
        Mockito.when(orchestrator.ask(
                Mockito.any(AskRequest.class),
                Mockito.anyString(),
                Mockito.anyString()
        )).thenThrow(new RuntimeException("429 Too Many Requests"));

        given()
            .contentType(ContentType.JSON)
            .header("X-Seer-Tenant-Alias", "acme")
            .body(new AskRequest("test", "test-1", null, 5))
        .when()
            .post("/api/ask")
        .then()
            .statusCode(200)
            .body("answer", containsString("temporarily unavailable"));
    }

    @Test
    void timeoutReturnsUnavailable() {
        Mockito.when(orchestrator.ask(
                Mockito.any(AskRequest.class),
                Mockito.anyString(),
                Mockito.anyString()
        )).thenThrow(new RuntimeException("Request timed out after 30000ms"));

        given()
            .contentType(ContentType.JSON)
            .header("X-Seer-Tenant-Alias", "acme")
            .body(new AskRequest("test", "test-2", null, 5))
        .when()
            .post("/api/ask")
        .then()
            .statusCode(200)
            .body("answer", containsString("temporarily unavailable"));
    }

    @Test
    void genericExceptionReturnsUnavailable() {
        Mockito.when(orchestrator.ask(
                Mockito.any(AskRequest.class),
                Mockito.anyString(),
                Mockito.anyString()
        )).thenThrow(new RuntimeException("Connection refused"));

        given()
            .contentType(ContentType.JSON)
            .header("X-Seer-Tenant-Alias", "acme")
            .body(new AskRequest("test", "test-3", null, 5))
        .when()
            .post("/api/ask")
        .then()
            .statusCode(200);
    }
}
