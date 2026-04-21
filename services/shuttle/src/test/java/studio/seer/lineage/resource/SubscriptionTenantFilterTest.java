package studio.seer.lineage.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.lineage.heimdall.HeimdallEventBus;
import studio.seer.lineage.heimdall.model.EventLevel;
import studio.seer.lineage.heimdall.model.HeimdallEvent;
import studio.seer.lineage.heimdall.model.HeimdallEventView;
import studio.seer.lineage.security.SeerIdentity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * SHT-14: Subscription tenant filter E2E tests.
 *
 * Verifies that heimdallEvents() and sessionProgress() subscriptions
 * only deliver events matching the subscriber's tenant alias.
 * Super-admin receives all events regardless of tenantAlias.
 */
@QuarkusTest
class SubscriptionTenantFilterTest {

    @InjectMock HeimdallEventBus eventBus;
    @InjectMock SeerIdentity     identity;

    @Inject SubscriptionResource resource;

    private BroadcastProcessor<HeimdallEvent> processor;

    @BeforeEach
    void setUp() {
        processor = BroadcastProcessor.create();
        when(eventBus.stream()).thenReturn(processor);
    }

    // ── heimdallEvents ────────────────────────────────────────────────────────

    @Test
    void heimdallEvents_tenantA_receivesOwnEvents() {
        when(identity.tenantAlias()).thenReturn("tenant-a");
        when(identity.role()).thenReturn("admin");

        List<HeimdallEventView> received = collectEvents(resource.heimdallEvents(), () -> {
            processor.onNext(event("tenant-a", "SESSION_STARTED", "sess-1"));
            processor.onNext(event("tenant-b", "SESSION_STARTED", "sess-2")); // must be filtered
            processor.onComplete();
        });

        assertEquals(1, received.size(), "tenant-a subscriber must receive only tenant-a events");
        assertEquals("sess-1", received.get(0).sessionId());
    }

    @Test
    void heimdallEvents_tenantB_receivesOwnEvents() {
        when(identity.tenantAlias()).thenReturn("tenant-b");
        when(identity.role()).thenReturn("admin");

        List<HeimdallEventView> received = collectEvents(resource.heimdallEvents(), () -> {
            processor.onNext(event("tenant-a", "SESSION_STARTED", "sess-1"));
            processor.onNext(event("tenant-b", "SESSION_STARTED", "sess-2"));
            processor.onComplete();
        });

        assertEquals(1, received.size());
        assertEquals("sess-2", received.get(0).sessionId());
    }

    @Test
    void heimdallEvents_superadmin_receivesAllEvents() {
        when(identity.tenantAlias()).thenReturn("default");
        when(identity.role()).thenReturn("super-admin");

        List<HeimdallEventView> received = collectEvents(resource.heimdallEvents(), () -> {
            processor.onNext(event("tenant-a", "SESSION_STARTED", "sess-1"));
            processor.onNext(event("tenant-b", "SESSION_STARTED", "sess-2"));
            processor.onNext(event("tenant-c", "SESSION_STARTED", "sess-3"));
            processor.onComplete();
        });

        assertEquals(3, received.size(), "super-admin must receive all events");
    }

    @Test
    void heimdallEvents_legacyEventNoTenantAlias_deliveredToAll() {
        when(identity.tenantAlias()).thenReturn("tenant-a");
        when(identity.role()).thenReturn("admin");

        List<HeimdallEventView> received = collectEvents(resource.heimdallEvents(), () -> {
            // Legacy event: no tenantAlias — null alias means global, delivered to all
            processor.onNext(legacyEvent("SESSION_STARTED", "sess-legacy"));
            processor.onComplete();
        });

        assertEquals(1, received.size(), "events with no tenantAlias must pass to all tenants");
    }

    // ── sessionProgress ───────────────────────────────────────────────────────

    @Test
    void sessionProgress_filtersBySessionIdAndTenant() {
        when(identity.tenantAlias()).thenReturn("tenant-a");
        when(identity.role()).thenReturn("viewer");

        List<HeimdallEventView> received = collectEvents(
                resource.sessionProgress("sess-target"), () -> {
                    processor.onNext(event("tenant-a", "STEP_DONE", "sess-target"));   // ✓ pass
                    processor.onNext(event("tenant-a", "STEP_DONE", "sess-other"));    // ✗ wrong session
                    processor.onNext(event("tenant-b", "STEP_DONE", "sess-target"));   // ✗ wrong tenant
                    processor.onComplete();
                });

        assertEquals(1, received.size());
        assertEquals("sess-target", received.get(0).sessionId());
    }

    @Test
    void sessionProgress_superadmin_receivesMatchingSessionFromAnyTenant() {
        when(identity.tenantAlias()).thenReturn("default");
        when(identity.role()).thenReturn("super-admin");

        List<HeimdallEventView> received = collectEvents(
                resource.sessionProgress("sess-x"), () -> {
                    processor.onNext(event("tenant-a", "STEP_DONE", "sess-x")); // ✓ superadmin bypass
                    processor.onNext(event("tenant-b", "STEP_DONE", "sess-x")); // ✓ superadmin bypass
                    processor.onNext(event("tenant-a", "STEP_DONE", "sess-y")); // ✗ wrong session
                    processor.onComplete();
                });

        assertEquals(2, received.size());
    }

    @Test
    void sessionProgress_nullSessionId_receivesNothing() {
        when(identity.tenantAlias()).thenReturn("tenant-a");
        when(identity.role()).thenReturn("viewer");

        List<HeimdallEventView> received = collectEvents(
                resource.sessionProgress(null), () -> {
                    processor.onNext(event("tenant-a", "STEP_DONE", "sess-1"));
                    processor.onComplete();
                });

        assertTrue(received.isEmpty(), "null sessionId filter must block all events");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static HeimdallEvent event(String tenantAlias, String type, String sessionId) {
        return new HeimdallEvent(
                System.currentTimeMillis(),
                "test",
                type,
                EventLevel.INFO,
                sessionId,
                "user-1",
                "corr-1",
                0L,
                Map.of("tenantAlias", tenantAlias),
                tenantAlias);
    }

    private static HeimdallEvent legacyEvent(String type, String sessionId) {
        // 9-arg backward-compat constructor, no tenantAlias field
        return new HeimdallEvent(
                System.currentTimeMillis(),
                "test",
                type,
                EventLevel.INFO,
                sessionId,
                "user-1",
                "corr-1",
                0L,
                Map.of());   // no tenantAlias key → null alias
    }

    private List<HeimdallEventView> collectEvents(Multi<HeimdallEventView> stream,
                                                  Runnable publish) {
        List<HeimdallEventView> collected = new java.util.concurrent.CopyOnWriteArrayList<>();
        var sub = stream.subscribe().with(collected::add, ex -> {}, () -> {});
        publish.run();
        // Give the reactive pipeline a tick to drain
        try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        sub.cancel();
        return collected;
    }
}
