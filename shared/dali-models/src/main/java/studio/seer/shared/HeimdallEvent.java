package studio.seer.shared;

import java.util.Map;

/**
 * Canonical event record for the HEIMDALL event pipeline.
 *
 * Schema зафиксирована в Sprint 1 как стабильный контракт (Q6).
 * При переходе к production observability меняются collectors и UI,
 * но не эта schema — решение ТРИЗ-противоречия "demo vs production".
 *
 * @param timestamp       unix milliseconds; обогащается на ingestion если 0
 * @param sourceComponent "hound" | "dali" | "mimir" | "anvil" | "shuttle" | "verdandi" | "heimdall"
 * @param eventType       {@link EventType#name()} — строка, не enum, для forward-compatibility
 * @param level           severity: INFO | WARN | ERROR
 * @param sessionId       nullable — привязка к parse-сессии
 * @param userId          nullable — кто инициировал
 * @param correlationId   nullable — связывает события одного tool call chain
 * @param durationMs      0 если не применимо
 * @param payload         event-specific data (тип определяется по eventType)
 */
public record HeimdallEvent(
        long                          timestamp,
        String                        sourceComponent,
        String                        eventType,
        EventLevel                    level,
        String                        sessionId,
        String                        userId,
        String                        correlationId,
        long                          durationMs,
        Map<String, Object>           payload
) {
    /** Фабрика для внутренних событий HEIMDALL (DEMO_RESET, SNAPSHOT_SAVED, ...) */
    public static HeimdallEvent internal(EventType type, String message) {
        return new HeimdallEvent(
                System.currentTimeMillis(),
                "heimdall",
                type.name(),
                EventLevel.INFO,
                null, null, null,
                0,
                Map.of("message", message)
        );
    }
}
