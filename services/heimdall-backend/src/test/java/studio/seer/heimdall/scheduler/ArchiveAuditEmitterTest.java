package studio.seer.heimdall.scheduler;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import studio.seer.heimdall.RingBuffer;
import studio.seer.shared.HeimdallEvent;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ArchiveAuditEmitter — verifies all three typed methods emit correct event types.
 */
@QuarkusTest
class ArchiveAuditEmitterTest {

    @InjectMock RingBuffer ringBuffer;
    @Inject     ArchiveAuditEmitter emitter;

    @Test
    void emitArchived_pushesCorrectEventType() {
        emitter.emitArchived("acme");

        verify(ringBuffer).push(argThat(e ->
                "seer.audit.tenant_archived".equals(e.eventType()) &&
                "acme".equals(e.payload().get("tenantAlias"))));
    }

    @Test
    void emitRestored_pushesCorrectEventType() {
        emitter.emitRestored("demo-co");

        verify(ringBuffer).push(argThat(e ->
                "seer.audit.tenant_restored".equals(e.eventType()) &&
                "demo-co".equals(e.payload().get("tenantAlias"))));
    }

    @Test
    void emitPurged_pushesCorrectEventType() {
        emitter.emitPurged("test-tenant");

        verify(ringBuffer).push(argThat(e ->
                "seer.audit.tenant_purged".equals(e.eventType()) &&
                "test-tenant".equals(e.payload().get("tenantAlias"))));
    }

    @Test
    void emitArchived_eventHasHeimdallSource() {
        emitter.emitArchived("acme");

        verify(ringBuffer).push(argThat((HeimdallEvent e) ->
                "heimdall".equals(e.sourceComponent())));
    }

    @Test
    void emitPurged_timestampIsPositive() {
        emitter.emitPurged("acme");

        verify(ringBuffer).push(argThat((HeimdallEvent e) -> e.timestamp() > 0));
    }
}
