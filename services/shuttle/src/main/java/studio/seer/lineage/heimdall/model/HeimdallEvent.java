package studio.seer.lineage.heimdall.model;

import java.util.Map;

/**
 * Local copy of studio.seer.shared.HeimdallEvent for SHUTTLE.
 * Temporary until Docker multi-module build for SHUTTLE is implemented.
 * Keep in sync with shared/dali-models/HeimdallEvent.java.
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
        Map<String, Object> payload
) {}
