package studio.seer.heimdall.tenant;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.heimdall.snapshot.SnapshotManager;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.when;

/**
 * Tests for TenantContextFilter — pre-matching JAX-RS filter that reads Chur-forwarded
 * headers and populates TenantContext on every request.
 *
 * Verified indirectly: the filter must run on every request without causing errors,
 * and resource methods that read the raw headers behave as expected.
 */
@QuarkusTest
class TenantContextFilterTest {

    @InjectMock SnapshotManager snapshots;

    @BeforeEach
    void setUp() {
        when(snapshots.list()).thenReturn(Uni.createFrom().item(List.of()));
    }

    /** Filter must not crash on a request with the admin role header. */
    @Test
    void filter_adminRoleHeader_requestSucceeds() {
        given()
            .header("X-Seer-Role", "admin")
        .when().get("/control/snapshots")
        .then().statusCode(200);
    }

    /** Filter must not crash on a request with no role header — resource sees null role → 403. */
    @Test
    void filter_noRoleHeader_requestHandledGracefully() {
        given()
        .when().get("/control/snapshots")
        .then().statusCode(403);
    }

    /** Filter must not crash on a request with an unknown role — resource rejects it. */
    @Test
    void filter_unknownRoleHeader_resourceCanReject() {
        given()
            .header("X-Seer-Role", "viewer")
        .when().get("/control/snapshots")
        .then().statusCode(403);
    }

    /** Filter handles X-Seer-Scopes header without NPE (space-separated). */
    @Test
    void filter_scopesHeader_parsedWithoutError() {
        given()
            .header("X-Seer-Role",   "admin")
            .header("X-Seer-Scopes", "read:tenants write:tenants")
        .when().get("/control/snapshots")
        .then().statusCode(200);
    }
}
