package studio.seer.heimdall.prefs;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for UserPrefsResource — GET/PUT /api/prefs/{sub}.
 *
 * Covers graceful degradation on FRIGG unavailability.
 *
 * HB-P3C-6
 */
@QuarkusTest
class UserPrefsResourceTest {

    @InjectMock UserPrefsRepository repo;

    @Inject UserPrefsResource resource;

    // ── GET /api/prefs/{sub} ──────────────────────────────────────────────────

    @Test
    void getPrefs_recordFound_returns200WithPrefs() {
        UserPrefsRecord prefs = new UserPrefsRecord(
                "user-uuid", "light", "ocean", "compact", "roboto", "fira", "13");
        when(repo.findBySub("user-uuid"))
                .thenReturn(Uni.createFrom().item(prefs));

        given().accept(ContentType.JSON)
        .when().get("/api/prefs/user-uuid")
        .then()
                .statusCode(200)
                .body("sub",   equalTo("user-uuid"))
                .body("theme", equalTo("light"));

        verify(repo).findBySub("user-uuid");
    }

    @Test
    void getPrefs_noRecord_returns200WithDefaults() {
        when(repo.findBySub("new-user"))
                .thenReturn(Uni.createFrom().nullItem());

        given().accept(ContentType.JSON)
        .when().get("/api/prefs/new-user")
        .then()
                .statusCode(200)
                .body("sub",     equalTo("new-user"))
                .body("theme",   equalTo("dark"))          // default
                .body("palette", equalTo("amber-forest")); // default
    }

    @Test
    void getPrefs_friggUnavailable_returns200WithDefaults() {
        when(repo.findBySub("user-uuid"))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("FRIGG down")));

        // Should NOT return 500 — graceful degradation returns defaults
        given().accept(ContentType.JSON)
        .when().get("/api/prefs/user-uuid")
        .then()
                .statusCode(200)
                .body("sub",   equalTo("user-uuid"))
                .body("theme", equalTo("dark")); // default
    }

    // ── PUT /api/prefs/{sub} ──────────────────────────────────────────────────

    @Test
    void putPrefs_validBody_returns200Ok() {
        when(repo.upsert(any())).thenReturn(Uni.createFrom().voidItem());

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"sub\":\"ignored\",\"theme\":\"light\",\"palette\":\"ocean\"," +
                      "\"density\":\"compact\",\"uiFont\":\"roboto\",\"monoFont\":\"fira\",\"fontSize\":\"13\"}")
        .when()
                .put("/api/prefs/user-uuid")
        .then()
                .statusCode(200)
                .body("ok", equalTo(true));

        // sub in body should be ignored; path sub used instead
        verify(repo).upsert(argThat(p -> "user-uuid".equals(p.sub())));
    }

    @Test
    void putPrefs_friggFails_returns500() {
        when(repo.upsert(any()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("write error")));

        given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body("{\"sub\":\"user-uuid\",\"theme\":\"dark\",\"palette\":\"amber-forest\"," +
                      "\"density\":\"normal\",\"uiFont\":\"inter\",\"monoFont\":\"jetbrains\",\"fontSize\":\"14\"}")
        .when()
                .put("/api/prefs/user-uuid")
        .then()
                .statusCode(500)
                .body("error", notNullValue());
    }
}
