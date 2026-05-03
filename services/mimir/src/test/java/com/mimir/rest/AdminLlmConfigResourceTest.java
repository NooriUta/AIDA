package com.mimir.rest;

import com.mimir.byok.TenantLlmConfig;
import com.mimir.byok.TenantLlmConfigStore;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@QuarkusTest
class AdminLlmConfigResourceTest {

    @InjectMock TenantLlmConfigStore store;

    @Test
    void postRequiresAdminRole() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"tenantAlias\":\"acme\",\"provider\":\"deepseek\",\"apiKey\":\"sk-test-123456\"}")
                .when().post("/api/admin/llm-config")
                .then().statusCode(403);
    }

    @Test
    void getRequiresAdminRole() {
        given()
                .when().get("/api/admin/llm-config/acme")
                .then().statusCode(403);
    }

    @Test
    void deleteRequiresAdminRole() {
        given()
                .when().delete("/api/admin/llm-config/acme")
                .then().statusCode(403);
    }

    @Test
    void postWithAdminPersistsAndReturnsMask() {
        doNothing().when(store).save(eq("acme"), eq("deepseek"),
                eq("sk-prod-abc123def456"), isNull(), isNull());

        given()
                .contentType(ContentType.JSON)
                .header("X-Seer-Role", "admin")
                .body("{\"tenantAlias\":\"acme\",\"provider\":\"deepseek\",\"apiKey\":\"sk-prod-abc123def456\"}")
                .when().post("/api/admin/llm-config")
                .then()
                .statusCode(200)
                .body("tenantAlias", is("acme"))
                .body("provider",    is("deepseek"))
                .body("keyMask",     is("sk-***-f456"))
                .body(not(containsString("abc123")));

        verify(store).save("acme", "deepseek", "sk-prod-abc123def456", null, null);
    }

    @Test
    void getMissingTenantReturns404() {
        when(store.findByTenant("ghost")).thenReturn(Optional.empty());

        given()
                .header("X-Seer-Role", "admin")
                .when().get("/api/admin/llm-config/ghost")
                .then().statusCode(404);
    }

    @Test
    void getReturnsSanitizedConfig() {
        when(store.findByTenant("acme")).thenReturn(Optional.of(new TenantLlmConfig(
                "acme", "anthropic", "envelope-base64-blob", "https://api.anthropic.com",
                "claude-sonnet-4-6", "2026-05-02T20:00:00Z")));

        given()
                .header("X-Seer-Role", "admin")
                .when().get("/api/admin/llm-config/acme")
                .then()
                .statusCode(200)
                .body("provider",  is("anthropic"))
                .body("modelName", is("claude-sonnet-4-6"))
                .body("keyMask",   notNullValue())
                .body("$",         not(hasKey("encryptedApiKey")))
                .body("$",         not(hasKey("apiKey")));
    }

    @Test
    void deleteWithAdminInvokesStore() {
        when(store.delete("acme")).thenReturn(true);

        given()
                .header("X-Seer-Role", "admin")
                .when().delete("/api/admin/llm-config/acme")
                .then().statusCode(204);

        verify(store).delete("acme");
    }
}
