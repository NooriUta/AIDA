package studio.seer.heimdall.reconcile;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrgDriftReconcilerTest {

    @Test
    void report_clean_whenEmptyAndNoError() {
        var r = new OrgDriftReconciler.Report("run-1", List.of(), List.of(), List.of(), null);
        assertTrue(r.clean());
    }

    @Test
    void report_notClean_whenFriggOrphan() {
        var r = new OrgDriftReconciler.Report("run-1", List.of("acme"), List.of(), List.of(), null);
        assertFalse(r.clean());
    }

    @Test
    void report_notClean_whenKcOrphan() {
        var r = new OrgDriftReconciler.Report("run-1", List.of(), List.of("zeta(uuid-123)"), List.of(), null);
        assertFalse(r.clean());
    }

    @Test
    void report_notClean_whenAliasMismatch() {
        var r = new OrgDriftReconciler.Report("run-1", List.of(), List.of(), List.of("acme<>KC:acme-corp"), null);
        assertFalse(r.clean());
    }

    @Test
    void report_notClean_whenError() {
        var r = new OrgDriftReconciler.Report("run-1", List.of(), List.of(), List.of(), "data_load_failed: boom");
        assertFalse(r.clean());
    }

    /*
     * Full drift detection semantics (FRIGG_ORPHAN / KC_ORPHAN / ALIAS_MISMATCH)
     * are exercised via Quarkus integration once the KC admin client is wired
     * behind a testable interface. For the pure-unit scope here we cover the
     * Report value type. The run() correctness lives in integration tests that
     * stub the HTTP client at the Quarkus level — tracked as a follow-up in
     * RUNBOOK §12.
     */
}
