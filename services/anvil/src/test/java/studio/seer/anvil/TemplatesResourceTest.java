package studio.seer.anvil;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class TemplatesResourceTest {

    @Test
    void getTemplates_returns5Templates() {
        given()
        .when()
            .get("/api/templates")
        .then()
            .statusCode(200)
            .body("total",               equalTo(5))
            .body("templates",           hasSize(5))
            .body("templates[0].id",     equalTo("downstream_impact"))
            .body("templates[0].language", equalTo("cypher"))
            .body("templates[1].id",     equalTo("upstream_lineage"))
            .body("templates[2].id",     equalTo("schema_summary"))
            .body("templates[2].language", equalTo("sql"))
            .body("templates[3].id",     equalTo("orphan_tables"))
            .body("templates[4].id",     equalTo("package_routines"));
    }

    @Test
    void getTemplates_allHaveIdNameLanguageQuery() {
        given()
        .when()
            .get("/api/templates")
        .then()
            .statusCode(200)
            .body("templates.id",          everyItem(notNullValue()))
            .body("templates.name",        everyItem(notNullValue()))
            .body("templates.language",    everyItem(notNullValue()))
            .body("templates.query",       everyItem(not(emptyOrNullString())))
            .body("templates.description", everyItem(notNullValue()));
    }
}
