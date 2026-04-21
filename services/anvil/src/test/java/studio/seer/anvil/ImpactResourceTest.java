package studio.seer.anvil;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import studio.seer.anvil.model.*;
import studio.seer.anvil.service.ImpactService;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class ImpactResourceTest {

    @InjectMock
    ImpactService impactService;

    @BeforeEach
    void setupMocks() {
        ImpactNode root = new ImpactNode("LOAD_FX_RATES", "DaliRoutine", "LOAD_FX_RATES", 0);
        List<ImpactNode> nodes = List.of(
                new ImpactNode("geoid:table:1", "DaliTable",  "HR.ORDERS",         1),
                new ImpactNode("geoid:col:2",   "DaliColumn", "HR.ORDERS.STATUS",  2)
        );
        ImpactResult result = new ImpactResult(root, nodes, List.of(), 2, false, false, 45L);

        Mockito.when(impactService.findImpact(Mockito.any(ImpactRequest.class)))
               .thenReturn(result);
    }

    @Test
    void postImpact_validDownstream_returns200WithNodes() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"nodeId":"LOAD_FX_RATES","direction":"downstream","maxHops":5,"dbName":"hound_default"}
                """)
        .when()
            .post("/api/impact")
        .then()
            .statusCode(200)
            .body("totalAffected", equalTo(2))
            .body("nodes",         hasSize(2))
            .body("nodes[0].type", equalTo("DaliTable"))
            .body("cached",        equalTo(false))
            .body("executionMs",   greaterThanOrEqualTo(0));
    }

    @Test
    void postImpact_upstreamSalary_returns200() {
        ImpactNode root    = new ImpactNode("HR.EMPLOYEES.SALARY", "DaliColumn", "HR.EMPLOYEES.SALARY", 0);
        ImpactNode source  = new ImpactNode("geoid:src:10", "DaliTable", "HR.RAW_SALARIES", 1);
        ImpactResult salaryResult = new ImpactResult(root, List.of(source), List.of(), 1, false, false, 38L);
        Mockito.when(impactService.findImpact(Mockito.any(ImpactRequest.class)))
               .thenReturn(salaryResult);

        given()
            .contentType(ContentType.JSON)
            .body("""
                {"nodeId":"HR.EMPLOYEES.SALARY","direction":"upstream","maxHops":3,"dbName":"hound_default"}
                """)
        .when()
            .post("/api/impact")
        .then()
            .statusCode(200)
            .body("totalAffected", greaterThan(0))
            .body("nodes",         hasSize(greaterThan(0)));
    }

    @Test
    void postImpact_missingNodeId_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"direction\":\"downstream\"}")
        .when()
            .post("/api/impact")
        .then()
            .statusCode(400);
    }

    @Test
    void postImpact_nullBody_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{}")
        .when()
            .post("/api/impact")
        .then()
            .statusCode(400);
    }
}
