package com.mimir.integration;

import com.mimir.model.AskRequest;
import com.mimir.model.MimirAnswer;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

/**
 * MC-07 — Integration tests for 3 demo queries against real DeepSeek API.
 *
 * <p>Conditional: skipped unless {@code DEEPSEEK_API_KEY} is set in env. Locally:
 * <pre>{@code
 * DEEPSEEK_API_KEY=sk-... ./gradlew :services:mimir:test --tests com.mimir.integration.DemoQueriesIT
 * }</pre>
 *
 * <p>Validation deliverable: {@code docs/current/specs/mimir/MIMIR_DEEPSEEK_VALIDATION_REPORT.md}
 * — fill p50/p95 latency + token usage from these runs over the validation period
 * (May 2 → 02.06.2026 re-eval).
 *
 * <p>Disabled in unit-test path (no env var) so CI passes without exposing API keys.
 *
 * <p>Note: real ANVIL + ArcadeDB are NOT required for these tests — tools internally
 * gracefully degrade (executionMs=-1 fallback) when upstreams unreachable.
 * The LLM still gets back valid (empty) data from each tool and synthesizes an answer
 * acknowledging "no data available". Test verifies the *flow*, not data correctness.
 */
@QuarkusTest
@EnabledIfEnvironmentVariable(named = "DEEPSEEK_API_KEY", matches = ".+")
class DemoQueriesIT {

    @Test
    void ordersReadingProcedures() {
        AskRequest req = new AskRequest(
                "Какие процедуры читают из таблицы HR.ORDERS?",
                "it-orders-1", "hound_default", null, null, 5);

        MimirAnswer answer = given()
                .contentType(ContentType.JSON)
                .header("X-Seer-Tenant-Alias", "default")
                .body(req)
            .when()
                .post("/api/ask")
            .then()
                .statusCode(200)
                .extract()
                .as(MimirAnswer.class);

        assertNotEmpty(answer);
    }

    @Test
    void salaryImpact() {
        AskRequest req = new AskRequest(
                "Что сломается если изменить колонку HR.EMPLOYEES.SALARY?",
                "it-salary-2", "hound_default", null, null, 5);

        MimirAnswer answer = given()
                .contentType(ContentType.JSON)
                .header("X-Seer-Tenant-Alias", "default")
                .body(req)
            .when()
                .post("/api/ask")
            .then()
                .statusCode(200)
                .extract()
                .as(MimirAnswer.class);

        assertNotEmpty(answer);
    }

    @Test
    void exportDateLineage() {
        AskRequest req = new AskRequest(
                "Откуда берётся значение колонки CRM.CA_DATA_EXPORTS.EXPORT_DATE?",
                "it-export-3", "hound_default", null, null, 5);

        MimirAnswer answer = given()
                .contentType(ContentType.JSON)
                .header("X-Seer-Tenant-Alias", "default")
                .body(req)
            .when()
                .post("/api/ask")
            .then()
                .statusCode(200)
                .extract()
                .as(MimirAnswer.class);

        assertNotEmpty(answer);
    }

    @Test
    void healthEndpointWorks() {
        // Health smoke covered everywhere, but here ensures IT setup loads service correctly.
        given()
            .when()
                .get("/api/health")
            .then()
                .statusCode(200)
                .body("status", equalTo("UP"))
                .body("service", equalTo("mimir"))
                .body("models", hasItems("deepseek", "anthropic", "ollama"));
    }

    private static void assertNotEmpty(MimirAnswer answer) {
        org.assertj.core.api.Assertions.assertThat(answer).isNotNull();
        org.assertj.core.api.Assertions.assertThat(answer.answer()).isNotBlank();
        // Tool calls + highlights are best-effort — depend on LLM choosing right tools.
        // Validation report tracks rates over time.
    }
}
