package studio.seer.heimdall.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import studio.seer.heimdall.reconcile.OrgDriftReconciler;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ReconcileResource — POST /api/admin/reconcile-orgs.
 *
 * HB-P3C-4
 */
@QuarkusTest
class ReconcileResourceTest {

    @InjectMock OrgDriftReconciler reconciler;

    @Inject ReconcileResource resource;

    @Test
    void reconcileOrgs_cleanReport_returns200() {
        OrgDriftReconciler.Report clean = new OrgDriftReconciler.Report(
                "run-1", List.of(), List.of(), List.of(), null);
        when(reconciler.run()).thenReturn(clean);

        given().accept(ContentType.JSON)
        .when().post("/api/admin/reconcile-orgs")
        .then().statusCode(200)
               .body("runId", equalTo("run-1"))
               .body("friggOrphan", empty());

        verify(reconciler).run();
    }

    @Test
    void reconcileOrgs_withDrifts_returns200AndShowsDrifts() {
        OrgDriftReconciler.Report dirty = new OrgDriftReconciler.Report(
                "run-2",
                List.of("orphan-frigg"),
                List.of("orphan-kc"),
                List.of(),
                null);
        when(reconciler.run()).thenReturn(dirty);

        given().accept(ContentType.JSON)
        .when().post("/api/admin/reconcile-orgs")
        .then().statusCode(200)
               .body("friggOrphan", hasItem("orphan-frigg"))
               .body("kcOrphan",    hasItem("orphan-kc"));
    }

    @Test
    void reconcileOrgs_dryRun_stillCallsReconciler() {
        // Current implementation ignores dryRun flag in delegate — verify it passes through
        OrgDriftReconciler.Report report = new OrgDriftReconciler.Report(
                "run-3", List.of(), List.of(), List.of(), null);
        when(reconciler.run()).thenReturn(report);

        given().accept(ContentType.JSON)
        .when().post("/api/admin/reconcile-orgs?dry-run=true")
        .then().statusCode(200);

        verify(reconciler).run();
    }

    @Test
    void reconcileOrgs_withError_returns200WithErrorField() {
        OrgDriftReconciler.Report errReport = new OrgDriftReconciler.Report(
                "run-4", List.of(), List.of(), List.of(), "KC connection refused");
        when(reconciler.run()).thenReturn(errReport);

        given().accept(ContentType.JSON)
        .when().post("/api/admin/reconcile-orgs")
        .then().statusCode(200)
               .body("error", equalTo("KC connection refused"));
    }
}
