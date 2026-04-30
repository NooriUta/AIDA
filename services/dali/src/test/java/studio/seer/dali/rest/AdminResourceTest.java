package studio.seer.dali.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.Test;
import studio.seer.dali.storage.FriggSchemaInitializer;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for AdminResource — POST /api/admin/schema/ensure/{alias}.
 * Verifies alias validation and delegation to FriggSchemaInitializer.
 *
 * <p>All requests include X-Seer-Tenant-Alias header (required by TenantContextFilter
 * which runs at AUTHENTICATION priority for every JAX-RS endpoint).
 */
@QuarkusTest
class AdminResourceTest {

    @InjectMock FriggSchemaInitializer schemaInit;

    /** TenantContextFilter requires a valid X-Seer-Tenant-Alias on every request. */
    private static RequestSpecification withTenant() {
        return given().header("X-Seer-Tenant-Alias", "default");
    }

    // ── valid aliases ─────────────────────────────────────────────────────────

    @Test
    void ensureSchema_validAlias_returns200AndCallsInitializer() {
        withTenant()
        .when().post("/api/admin/schema/ensure/acme-co")
        .then()
            .statusCode(200)
            .body("status",      equalTo("ok"))
            .body("tenantAlias", equalTo("acme-co"));

        verify(schemaInit).ensureSchema("acme-co");
    }

    @Test
    void ensureSchema_minLengthAlias_returns200() {
        // Minimum valid: [a-z][a-z0-9-]{2,30}[a-z0-9] → 4 chars minimum
        withTenant()
        .when().post("/api/admin/schema/ensure/abcd")
        .then()
            .statusCode(200)
            .body("tenantAlias", equalTo("abcd"));

        verify(schemaInit).ensureSchema("abcd");
    }

    @Test
    void ensureSchema_aliasWithHyphenAndDigits_returns200() {
        withTenant()
        .when().post("/api/admin/schema/ensure/tenant01")
        .then()
            .statusCode(200)
            .body("tenantAlias", equalTo("tenant01"));

        verify(schemaInit).ensureSchema("tenant01");
    }

    // ── invalid aliases ───────────────────────────────────────────────────────

    @Test
    void ensureSchema_tooShortAlias_returns400() {
        withTenant()
        .when().post("/api/admin/schema/ensure/ab")
        .then()
            .statusCode(400)
            .body("error", containsString("Invalid tenant alias"));

        verifyNoInteractions(schemaInit);
    }

    @Test
    void ensureSchema_uppercaseAlias_returns400() {
        withTenant()
        .when().post("/api/admin/schema/ensure/ACME")
        .then()
            .statusCode(400)
            .body("error", containsString("Invalid tenant alias"));

        verifyNoInteractions(schemaInit);
    }

    @Test
    void ensureSchema_startsWithDigit_returns400() {
        // Regex requires first char to be [a-z]
        withTenant()
        .when().post("/api/admin/schema/ensure/1invalid")
        .then()
            .statusCode(400)
            .body("error", containsString("Invalid tenant alias"));

        verifyNoInteractions(schemaInit);
    }
}
