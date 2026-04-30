package studio.seer.heimdall.control;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import studio.seer.heimdall.snapshot.FriggGateway;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Phase 3-C: TenantStatusReconciler unit tests.
 * Tests transition detection, baseline seeding, and deletion handling.
 */
@QuarkusTest
class TenantStatusReconcilerTest {

    @Inject
    TenantStatusReconciler reconciler;

    @InjectMock
    FriggGateway frigg;

    @InjectMock
    ControlEventEmitter emitter;

    @BeforeEach
    void resetBaseline() {
        reconciler.resetBaselineForTests();
        Mockito.reset(emitter);
    }

    // ── baseline seed (first tick — no events) ───────────────────────────────

    @Test
    void firstTick_seedsBaseline_noEventsEmitted() {
        stubFrigg(List.of(
                row("acme",  "ACTIVE"),
                row("beta",  "ACTIVE")
        ));

        reconciler.tick();

        verifyNoInteractions(emitter);
    }

    // ── status transition detection ───────────────────────────────────────────

    @Test
    void secondTick_statusUnchanged_noEvent() {
        stubFrigg(List.of(row("acme", "ACTIVE")));
        reconciler.tick();  // baseline

        stubFrigg(List.of(row("acme", "ACTIVE")));
        reconciler.tick();  // no change

        verifyNoInteractions(emitter);
    }

    @Test
    void transition_activeToSuspended_emitsSuspended() {
        stubFrigg(List.of(row("acme", "ACTIVE")));
        reconciler.tick();  // baseline

        stubFrigg(List.of(row("acme", "SUSPENDED")));
        reconciler.tick();

        verify(emitter).emitTenantSuspended("acme");
        verifyNoMoreInteractions(emitter);
    }

    @Test
    void transition_activeToArchived_emitsArchived() {
        stubFrigg(List.of(row("acme", "ACTIVE")));
        reconciler.tick();  // baseline

        stubFrigg(List.of(row("acme", "ARCHIVED")));
        reconciler.tick();

        verify(emitter).emitTenantArchived("acme");
    }

    @Test
    void transition_suspendedToActive_emitsRestored() {
        stubFrigg(List.of(row("acme", "SUSPENDED")));
        reconciler.tick();  // baseline

        stubFrigg(List.of(row("acme", "ACTIVE")));
        reconciler.tick();

        verify(emitter).emitTenantRestored("acme");
    }

    @Test
    void transition_archivedToActive_emitsRestored() {
        stubFrigg(List.of(row("acme", "ARCHIVED")));
        reconciler.tick();  // baseline

        stubFrigg(List.of(row("acme", "ACTIVE")));
        reconciler.tick();

        verify(emitter).emitTenantRestored("acme");
    }

    @Test
    void newTenant_appeared_emitsInvalidated() {
        stubFrigg(List.of(row("acme", "ACTIVE")));
        reconciler.tick();  // baseline

        stubFrigg(List.of(row("acme", "ACTIVE"), row("beta", "ACTIVE")));
        reconciler.tick();

        verify(emitter).emitTenantInvalidated("beta", "new_tenant");
        verifyNoMoreInteractions(emitter);
    }

    @Test
    void tenantRemoved_emitsPurged() {
        stubFrigg(List.of(row("acme", "ACTIVE"), row("beta", "ACTIVE")));
        reconciler.tick();  // baseline

        stubFrigg(List.of(row("acme", "ACTIVE")));  // beta gone
        reconciler.tick();

        verify(emitter).emitTenantPurged("beta");
    }

    @Test
    void emptyRows_noException() {
        stubFrigg(List.of());
        reconciler.tick();  // should return early, no NPE

        verifyNoInteractions(emitter);
    }

    @Test
    void unknownNewStatus_emitsInvalidated() {
        stubFrigg(List.of(row("acme", "ACTIVE")));
        reconciler.tick();  // baseline

        stubFrigg(List.of(row("acme", "UNKNOWN_STATUS")));
        reconciler.tick();

        verify(emitter).emitTenantInvalidated("acme", "status_UNKNOWN_STATUS");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void stubFrigg(List<Map<String, Object>> rows) {
        when(frigg.sqlTenants(anyString(), any())).thenReturn(Uni.createFrom().item(rows));
    }

    private static Map<String, Object> row(String alias, String status) {
        return Map.of("tenantAlias", alias, "status", status);
    }
}
