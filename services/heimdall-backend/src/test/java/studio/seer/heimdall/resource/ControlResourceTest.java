package studio.seer.heimdall.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.heimdall.RingBuffer;
import studio.seer.heimdall.metrics.MetricsCollector;
import studio.seer.heimdall.snapshot.SnapshotManager;
import studio.seer.heimdall.snapshot.SnapshotInfo;
import studio.seer.heimdall.tenant.TenantContext;
import studio.seer.shared.HeimdallEvent;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests for ControlResource — admin control plane.
 *
 * POST /control/reset
 * POST /control/snapshot
 * POST /control/cancel/{sessionId}
 * GET  /control/snapshots
 *
 * HB-P3C-5
 */
@QuarkusTest
class ControlResourceTest {

    @InjectMock RingBuffer       ringBuffer;
    @InjectMock MetricsCollector metricsCollector;
    @InjectMock SnapshotManager  snapshots;
    @InjectMock TenantContext    tenantCtx;

    @Inject ControlResource resource;

    @BeforeEach
    void stubContext() {
        // Default: not admin via TenantContext (role is verified by header in tests)
        when(tenantCtx.isAdmin()).thenReturn(false);
        when(tenantCtx.isSuperAdmin()).thenReturn(false);
    }

    // ── POST /control/reset ────────────────────────────────────────────────────

    @Test
    void reset_adminRole_clearsBufferAndReturns200() {
        given()
                .header("X-Seer-Role", "admin")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .post("/control/reset")
        .then()
                .statusCode(200)
                .body("status", equalTo("reset"));

        verify(ringBuffer).clear();
        verify(metricsCollector).reset();
    }

    @Test
    void reset_superAdminRole_returns200() {
        given()
                .header("X-Seer-Role", "super-admin")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .post("/control/reset")
        .then()
                .statusCode(200);
    }

    @Test
    void reset_viewerRole_returns403() {
        given()
                .header("X-Seer-Role", "viewer")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .post("/control/reset")
        .then()
                .statusCode(403)
                .body("error", notNullValue());

        verifyNoInteractions(ringBuffer);
        verifyNoInteractions(metricsCollector);
    }

    @Test
    void reset_noRole_returns403() {
        given().contentType(ContentType.JSON).accept(ContentType.JSON)
        .when().post("/control/reset")
        .then().statusCode(403);
    }

    // ── POST /control/cancel/{sessionId} ─────────────────────────────────────

    @Test
    void cancelSession_adminRole_returns200Stub() {
        given()
                .header("X-Seer-Role", "admin")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .post("/control/cancel/sess-abc-123")
        .then()
                .statusCode(200)
                .body("sessionId", equalTo("sess-abc-123"))
                .body("status",    equalTo("cancel_requested"));
    }

    @Test
    void cancelSession_viewerRole_returns403() {
        given()
                .header("X-Seer-Role", "viewer")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .post("/control/cancel/sess-abc-123")
        .then()
                .statusCode(403);
    }

    // ── POST /control/snapshot ────────────────────────────────────────────────

    @Test
    void saveSnapshot_adminRole_savesAndReturns200() {
        when(ringBuffer.snapshot()).thenReturn(List.of());
        when(snapshots.save(eq("my-snap"), any()))
                .thenReturn(Uni.createFrom().item("snap-uuid-001"));

        given()
                .header("X-Seer-Role", "admin")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .post("/control/snapshot?name=my-snap")
        .then()
                .statusCode(200)
                .body("snapshotId", equalTo("snap-uuid-001"))
                .body("name",       equalTo("my-snap"));

        verify(snapshots).save(eq("my-snap"), any());
    }

    @Test
    void saveSnapshot_snapshotManagerFails_returns500() {
        when(ringBuffer.snapshot()).thenReturn(List.of());
        when(snapshots.save(any(), any()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("FRIGG down")));

        given()
                .header("X-Seer-Role", "admin")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .post("/control/snapshot")
        .then()
                .statusCode(500)
                .body("error", notNullValue());
    }

    @Test
    void saveSnapshot_viewerRole_returns403() {
        given()
                .header("X-Seer-Role", "viewer")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .post("/control/snapshot")
        .then()
                .statusCode(403);

        verifyNoInteractions(snapshots);
    }

    // ── GET /control/snapshots ────────────────────────────────────────────────

    @Test
    void listSnapshots_adminRole_returnsSnapshotList() {
        SnapshotInfo info = new SnapshotInfo("snap-1", "my-snap", 1_000L, 5);
        when(snapshots.list()).thenReturn(Uni.createFrom().item(List.of(info)));

        given()
                .header("X-Seer-Role", "admin")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .get("/control/snapshots")
        .then()
                .statusCode(200)
                .body("$",        hasSize(1))
                .body("[0].id",   equalTo("snap-1"))
                .body("[0].name", equalTo("my-snap"));

        verify(snapshots).list();
    }

    @Test
    void listSnapshots_viewerRole_returns403() {
        given()
                .header("X-Seer-Role", "viewer")
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
        .when()
                .get("/control/snapshots")
        .then()
                .statusCode(403);

        verifyNoInteractions(snapshots);
    }
}
