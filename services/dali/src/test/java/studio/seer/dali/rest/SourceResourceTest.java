package studio.seer.dali.rest;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import studio.seer.dali.heimdall.HeimdallEmitter;
import studio.seer.dali.security.JdbcUrlValidator;
import studio.seer.dali.storage.SourceRepository;

import java.util.Optional;

import static io.restassured.RestAssured.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * EV-08 deferred — 5-B: Verifies that {@link SourceResource} emits the correct
 * {@code SOURCE_CREATED} and {@code SOURCE_DELETED} Heimdall events (EV-06).
 *
 * <p>Uses {@code @InjectMock} to:
 * <ul>
 *   <li>{@link HeimdallEmitter} — avoid HTTP to Heimdall, enable {@code verify()}
 *   <li>{@link SourceRepository} — avoid live FRIGG/ArcadeDB for CRUD operations
 *   <li>{@link JdbcUrlValidator} — control SSRF validation result per-test
 * </ul>
 *
 * <p>The URL validation guard (SSRF) is exercised separately in
 * {@link studio.seer.dali.security.JdbcUrlSsrfBlocklistTest}.
 * These tests focus on the emit contract only.
 */
@QuarkusTest
class SourceResourceTest {

    @InjectMock HeimdallEmitter  heimdall;
    @InjectMock SourceRepository sourceRepository;
    @InjectMock JdbcUrlValidator jdbcUrlValidator;

    @BeforeEach
    void setupDefaults() {
        // Default: validator allows all URLs (SSRF protection tested separately)
        Mockito.when(jdbcUrlValidator.validate(anyString()))
               .thenReturn(JdbcUrlValidator.ValidationResult.ok());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static RequestSpecification withTenant() {
        return given().header("X-Seer-Tenant-Alias", "default");
    }

    private static SourceDTO dto(String id, String dialect) {
        return new SourceDTO(id, "test-source", dialect, "jdbc:oracle:thin:@db:1521:XE",
                null, null, null, null);
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    /**
     * EV-06-T1: {@code POST /api/sources} with valid body → 201 Created and
     * {@code sourceCreated(tenantAlias, sourceId, dialect)} emitted exactly once.
     */
    @Test
    @DisplayName("EV-06-T1: POST /api/sources → 201 + sourceCreated emitted")
    void createSource_validBody_returns201AndEmitsSourceCreated() {
        Mockito.when(sourceRepository.create(
                eq("default"), eq("prod-db"), eq("oracle"), anyString(),
                any(), any(), any()))
               .thenReturn(dto("src-create-01", "oracle"));

        withTenant()
            .contentType(ContentType.JSON)
            .body("""
                  {"name":"prod-db","dialect":"oracle","jdbcUrl":"jdbc:oracle:thin:@db.corp:1521:XE"}
                  """)
        .when()
            .post("/api/sources")
        .then()
            .statusCode(201)
            .body("id", Matchers.equalTo("src-create-01"))
            .body("dialect", Matchers.equalTo("oracle"));

        verify(heimdall).sourceCreated("default", "src-create-01", "oracle");
        verify(heimdall, never()).sourceDeleted(anyString(), anyString(), anyString());
    }

    /**
     * EV-06-T2: {@code DELETE /api/sources/{id}} for an existing source → 204 No Content
     * and {@code sourceDeleted(tenantAlias, sourceId, dialect)} emitted exactly once.
     */
    @Test
    @DisplayName("EV-06-T2: DELETE /api/sources/{id} → 204 + sourceDeleted emitted")
    void deleteSource_existingSource_returns204AndEmitsSourceDeleted() {
        Mockito.when(sourceRepository.findById("default", "src-del-02"))
               .thenReturn(Optional.of(dto("src-del-02", "plsql")));

        withTenant()
        .when()
            .delete("/api/sources/src-del-02")
        .then()
            .statusCode(204);

        verify(heimdall).sourceDeleted("default", "src-del-02", "plsql");
        verify(heimdall, never()).sourceCreated(anyString(), anyString(), anyString());
    }

    /**
     * EV-06-T3: {@code POST /api/sources} with SSRF-blocked URL → 400 Bad Request
     * and {@code sourceCreated} is NEVER emitted (no event fires for rejected requests).
     */
    @Test
    @DisplayName("EV-06-T3: POST /api/sources SSRF URL → 400, sourceCreated NOT emitted")
    void createSource_ssrfUrl_returns400_noEmit() {
        Mockito.when(jdbcUrlValidator.validate("jdbc:mysql://127.0.0.1/internal"))
               .thenReturn(JdbcUrlValidator.ValidationResult.reject("loopback address"));

        withTenant()
            .contentType(ContentType.JSON)
            .body("""
                  {"name":"evil","dialect":"mysql","jdbcUrl":"jdbc:mysql://127.0.0.1/internal"}
                  """)
        .when()
            .post("/api/sources")
        .then()
            .statusCode(400)
            .body("error", Matchers.containsString("source_url_rejected"));

        verify(heimdall, never()).sourceCreated(any(), any(), any());
    }

    /**
     * EV-06-T4: {@code POST /api/sources} with missing required field {@code name} → 400
     * and {@code sourceCreated} is NEVER emitted (guard fires before create).
     */
    @Test
    @DisplayName("EV-06-T4: POST /api/sources missing name → 400, sourceCreated NOT emitted")
    void createSource_missingName_returns400_noEmit() {
        withTenant()
            .contentType(ContentType.JSON)
            .body("""
                  {"dialect":"oracle","jdbcUrl":"jdbc:oracle:thin:@db:1521:XE"}
                  """)
        .when()
            .post("/api/sources")
        .then()
            .statusCode(400);

        verify(heimdall, never()).sourceCreated(any(), any(), any());
    }
}
