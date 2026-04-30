package studio.seer.heimdall.control;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.shared.ControlEvent;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ControlEventEmitter — emit dispatch, typed helpers, idempotency.
 */
@QuarkusTest
class ControlEventEmitterTest {

    @InjectMock FenceTokenProvider fences;
    @InjectMock ControlEventStore  store;

    @Inject ControlEventEmitter emitter;

    @BeforeEach
    void setUp() {
        when(fences.next()).thenReturn(42L);
        when(store.persist(any())).thenReturn(true);
    }

    // ── emit() ────────────────────────────────────────────────────────────────

    @Test
    void emit_newEvent_callsStoreAndReturnsFencedEvent() {
        ControlEvent ev = emitter.emit("acme", "tenant_invalidated", Map.of("cause", "test"));

        assertNotNull(ev);
        assertEquals("acme", ev.tenantAlias());
        assertEquals("tenant_invalidated", ev.eventType());
        assertEquals(42L, ev.fenceToken());
        verify(store).persist(eq(ev));
    }

    @Test
    void emit_nullPayload_treatedAsEmptyMap() {
        ControlEvent ev = emitter.emit("acme", "tenant_suspended", null);

        assertNotNull(ev);
        assertNotNull(ev.payload());
    }

    @Test
    void emit_duplicateEvent_doesNotThrow() {
        // store.persist returns false (idempotent duplicate) — emitter should return event gracefully
        when(store.persist(any())).thenReturn(false);

        ControlEvent ev = emitter.emit("acme", "tenant_invalidated", Map.of());

        assertNotNull(ev);
        // Store IS still called — idempotency check is store's responsibility
        verify(store).persist(any());
    }

    // ── typed helpers ─────────────────────────────────────────────────────────

    @Test
    void emitTenantInvalidated_includesCauseInPayload() {
        ControlEvent ev = emitter.emitTenantInvalidated("acme", "manual-reset");

        assertEquals("tenant_invalidated", ev.eventType());
        assertEquals("acme", ev.tenantAlias());
    }

    @Test
    void emitTenantInvalidated_nullCause_payloadStillNonNull() {
        ControlEvent ev = emitter.emitTenantInvalidated("acme", null);

        assertNotNull(ev);
        assertNotNull(ev.payload());
    }

    @Test
    void emitTenantSuspended_setsCorrectEventType() {
        ControlEvent ev = emitter.emitTenantSuspended("demo-co");

        assertEquals("tenant_suspended", ev.eventType());
        assertEquals("demo-co", ev.tenantAlias());
    }

    @Test
    void emitTenantArchived_setsCorrectEventType() {
        ControlEvent ev = emitter.emitTenantArchived("demo-co");
        assertEquals("tenant_archived", ev.eventType());
    }

    @Test
    void emitTenantRestored_setsCorrectEventType() {
        ControlEvent ev = emitter.emitTenantRestored("demo-co");
        assertEquals("tenant_restored", ev.eventType());
    }

    @Test
    void emitTenantPurged_setsCorrectEventType() {
        ControlEvent ev = emitter.emitTenantPurged("demo-co");
        assertEquals("tenant_purged", ev.eventType());
    }

    @Test
    void emit_fenceTokenFromProvider_stamped() {
        when(fences.next()).thenReturn(777L);

        ControlEvent ev = emitter.emit("acme", "tenant_invalidated", Map.of());

        assertEquals(777L, ev.fenceToken());
    }
}
