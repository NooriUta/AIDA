package com.mimir;

import com.mimir.model.AskRequest;
import com.mimir.model.MimirAnswer;
import com.mimir.service.MimirService;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * MP-05 smoke tests — MimirService is mocked so no real LLM call is made.
 * DOD: POST /api/ask → 200, GET /api/health → 200.
 */
@QuarkusTest
class AskResourceSmokeTest {

    @InjectMock
    MimirService mimirService;

    @Test
    void postAskReturns200WithStubAnswer() {
        Mockito.when(mimirService.ask(
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString(),
            Mockito.anyString()
        )).thenReturn(new MimirAnswer(
            "The table HR_EMPLOYEES has 3 downstream procedures.",
            List.of("search_nodes", "count_dependencies"),
            List.of("node-42", "node-17"),
            0.92,
            0L
        ));

        given()
            .contentType(ContentType.JSON)
            .header("X-Seer-Tenant-Alias", "acme")
            .body(new AskRequest("What reads from HR_EMPLOYEES?", "acme:session-1", null, 5))
        .when()
            .post("/api/ask")
        .then()
            .statusCode(200)
            .body("answer",           containsString("HR_EMPLOYEES"))
            .body("toolCallsUsed",    hasSize(2))
            .body("highlightNodeIds", hasItem("node-42"))
            .body("confidence",       greaterThan(0.9f));
    }

    @Test
    void getHealthReturns200() {
        given()
        .when()
            .get("/api/health")
        .then()
            .statusCode(200)
            .body("status",  equalTo("UP"))
            .body("service", equalTo("mimir"));
    }
}
