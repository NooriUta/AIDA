package studio.seer.anvil;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import studio.seer.anvil.model.SchemaResponse;
import studio.seer.anvil.service.SchemaService;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class SchemaResourceTest {

    @InjectMock
    SchemaService schemaService;

    @BeforeEach
    void setupMocks() {
        SchemaResponse response = new SchemaResponse(
                List.of("DaliTable", "DaliColumn", "DaliRoutine", "DaliStatement"),
                List.of("DATA_FLOW", "HAS_COLUMN", "READS_FROM", "WRITES_TO"),
                "hound_default",
                Map.of("vertexCount", 45230L, "edgeCount", 123450L)
        );
        Mockito.when(schemaService.getSchema(Mockito.isNull())).thenReturn(response);
        Mockito.when(schemaService.getSchema(Mockito.eq("hound_default"))).thenReturn(response);

        SchemaResponse acmeResponse = new SchemaResponse(
                List.of("DaliTable"), List.of("DATA_FLOW"), "hound_acme",
                Map.of("vertexCount", 100L, "edgeCount", 200L)
        );
        Mockito.when(schemaService.getSchema(Mockito.eq("hound_acme"))).thenReturn(acmeResponse);
    }

    @Test
    void getSchema_defaultDb_returns200WithTypes() {
        given()
        .when()
            .get("/api/schema")
        .then()
            .statusCode(200)
            .body("dbName",      equalTo("hound_default"))
            .body("vertexTypes", hasSize(greaterThan(0)))
            .body("edgeTypes",   hasSize(greaterThan(0)))
            .body("stats.vertexCount", greaterThan(0))
            .body("stats.edgeCount",   greaterThan(0));
    }

    @Test
    void getSchema_customDbName_returnsThatDb() {
        given()
            .queryParam("dbName", "hound_acme")
        .when()
            .get("/api/schema")
        .then()
            .statusCode(200)
            .body("dbName", equalTo("hound_acme"));
    }

    @Test
    void getSchema_containsKnownVertexTypes() {
        given()
        .when()
            .get("/api/schema")
        .then()
            .statusCode(200)
            .body("vertexTypes", hasItem("DaliTable"))
            .body("vertexTypes", hasItem("DaliColumn"));
    }
}
