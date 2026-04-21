package studio.seer.lineage.heimdall.model;

import java.util.Map;

/**
 * Local copy of studio.seer.shared.HeimdallEvent for SHUTTLE.
 * SHT-07: Added tenantAlias field for tenant-filtered subscriptions.
 */
public record HeimdallEvent(
        long                timestamp,
        String              sourceComponent,
        String              eventType,
        EventLevel          level,
        String              sessionId,
        String              userId,
        String              correlationId,
        long                durationMs,
        Map<String, Object> payload,
        String              tenantAlias
) {
    /** Backward-compat factory for events without tenantAlias. */
    public HeimdallEvent(long timestamp, String sourceComponent, String eventType,
                         EventLevel level, String sessionId, String userId,
                         String correlationId, long durationMs, Map<String, Object> payload) {
        this(timestamp, sourceComponent, eventType, level, sessionId, userId,
             correlationId, durationMs, payload,
             payload != null ? (String) payload.get("tenantAlias") : null);
    }
}
