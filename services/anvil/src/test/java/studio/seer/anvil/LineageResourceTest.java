package studio.seer.anvil;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import studio.seer.anvil.model.*;
import studio.seer.anvil.service.LineageService;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class LineageResourceTest {

    @InjectMock
    LineageService lineageService;

    @BeforeEach
    void setupMocks() {
        LineageResult result = new LineageResult(
                List.of(new ImpactNode("geoid:src:1", "DaliTable", "HR.RAW_SALARIES", 1)),
                List.of(),
                28L
        );
        Mockito.when(lineageService.queryLineage(Mockito.any(LineageRequest.class)))
               .thenReturn(result);
    }

    @Test
    void postLineage_upstreamSalary_returnsSourceNode() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"nodeId":"HR.EMPLOYEES.SALARY","direction":"upstream","dbName":"hound_default","maxHops":10}
                """)
        .when()
            .post("/api/lineage")
        .then()
            .statusCode(200)
            .body("nodes",       hasSize(greaterThan(0)))
            .body("executionMs", greaterThanOrEqualTo(0));
    }

    @Test
    void postLineage_downstream_returns200() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"nodeId":"LOAD_FX_RATES","direction":"downstream","dbName":"hound_default"}
                """)
        .when()
            .post("/api/lineage")
        .then()
            .statusCode(200);
    }

    @Test
    void postLineage_both_callsServiceAndReturns200() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"nodeId":"HR.EMPLOYEES.SALARY","direction":"both","dbName":"hound_default"}
                """)
        .when()
            .post("/api/lineage")
        .then()
            .statusCode(200)
            .body("nodes", notNullValue());
    }

    @Test
    void postLineage_invalidDirection_returns400_beforeService() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"nodeId":"LOAD_FX_RATES","direction":"sideways"}
                """)
        .when()
            .post("/api/lineage")
        .then()
            .statusCode(400);

        // Service must NOT be called for invalid direction
        Mockito.verify(lineageService, Mockito.never()).queryLineage(Mockito.any());
    }

    @Test
    void postLineage_missingNodeId_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"direction\":\"upstream\"}")
        .when()
            .post("/api/lineage")
        .then()
            .statusCode(400);
    }
}
