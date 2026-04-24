package studio.seer.anvil;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import studio.seer.anvil.model.QueryResult;
import studio.seer.anvil.service.QueryService;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class QueryResourceTest {

    @InjectMock
    QueryService queryService;

    @BeforeEach
    void setupMocks() {
        QueryResult result = new QueryResult("cypher",
                List.of(), List.of(),
                List.of(Map.of("qualifiedName", "HR.ORDERS"),
                        Map.of("qualifiedName", "HR.EMPLOYEES")),
                2, false, 12L, "test-query-id");

        Mockito.when(queryService.execute(Mockito.anyString(), Mockito.anyString(), Mockito.any()))
               .thenReturn(result);
    }

    @Test
    void postQuery_cypherSelect_returns200WithRows() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"language":"cypher","query":"MATCH (t:DaliTable) RETURN t LIMIT 50","dbName":"hound_default"}
                """)
        .when()
            .post("/api/query")
        .then()
            .statusCode(200)
            .body("language",    equalTo("cypher"))
            .body("rows",        hasSize(2))
            .body("queryId",     notNullValue())
            .body("executionMs", greaterThanOrEqualTo(0));
    }

    @Test
    void postQuery_cypherCreate_returns403_noServiceCall() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"language":"cypher","query":"CREATE (n:Evil {x:1})","dbName":"hound_default"}
                """)
        .when()
            .post("/api/query")
        .then()
            .statusCode(403)
            .body("error", containsString("CREATE"));

        Mockito.verify(queryService, Mockito.never()).execute(Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void postQuery_sqlSelect_returns200() {
        QueryResult sqlResult = new QueryResult("sql", List.of(), List.of(),
                List.of(Map.of("qualifiedName", "HR.ORDERS")), 1, false, 8L, "sql-id");
        Mockito.when(queryService.execute(Mockito.eq("sql"), Mockito.anyString(), Mockito.any()))
               .thenReturn(sqlResult);

        given()
            .contentType(ContentType.JSON)
            .body("""
                {"language":"sql","query":"SELECT qualifiedName FROM DaliTable LIMIT 50","dbName":"hound_default"}
                """)
        .when()
            .post("/api/query")
        .then()
            .statusCode(200)
            .body("language", equalTo("sql"));
    }

    @Test
    void postQuery_sqlDrop_returns403() {
        given()
            .contentType(ContentType.JSON)
            .body("""
                {"language":"sql","query":"DROP TABLE DaliTable","dbName":"hound_default"}
                """)
        .when()
            .post("/api/query")
        .then()
            .statusCode(403);
    }

    @Test
    void postQuery_graphql_returns501() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"language\":\"graphql\",\"query\":\"{ search { id } }\"}")
        .when()
            .post("/api/query")
        .then()
            .statusCode(501);
    }

    @Test
    void postQuery_missingQuery_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"language\":\"cypher\"}")
        .when()
            .post("/api/query")
        .then()
            .statusCode(400);
    }

    @Test
    void postQuery_unknownLanguage_returns400() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"language\":\"javascript\",\"query\":\"console.log(1)\"}")
        .when()
            .post("/api/query")
        .then()
            .statusCode(400);
    }
}
