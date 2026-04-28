package studio.seer.heimdall.scheduler;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.heimdall.RingBuffer;
import studio.seer.shared.EventLevel;
import studio.seer.shared.HeimdallEvent;

import java.util.Map;

/**
 * HTA-11..13: Emits {@code seer.audit.tenant_archived}/{@code tenant_restored}/
 * {@code tenant_purged} events into the ring buffer for the audit stream.
 */
@ApplicationScoped
public class ArchiveAuditEmitter {

    @Inject RingBuffer ringBuffer;

    public void emitArchived(String tenantAlias) {
        emit("seer.audit.tenant_archived", tenantAlias);
    }

    public void emitRestored(String tenantAlias) {
        emit("seer.audit.tenant_restored", tenantAlias);
    }

    public void emitPurged(String tenantAlias) {
        emit("seer.audit.tenant_purged", tenantAlias);
    }

    private void emit(String type, String tenantAlias) {
        ringBuffer.push(new HeimdallEvent(
                System.currentTimeMillis(),
                "heimdall",
                type,
                EventLevel.INFO,
                null, null, null, 0L,
                Map.of("tenantAlias", tenantAlias)));
    }
}
