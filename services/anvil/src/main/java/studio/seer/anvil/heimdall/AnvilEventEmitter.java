package studio.seer.anvil.heimdall;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.shared.EventLevel;
import studio.seer.shared.EventType;
import studio.seer.shared.HeimdallEvent;

import java.util.Map;

/**
 * Fire-and-forget HEIMDALL event emitter for ANVIL.
 * Failures are logged at DEBUG and silently swallowed — observability must never affect traversal correctness.
 */
@ApplicationScoped
public class AnvilEventEmitter {

    private static final Logger log = LoggerFactory.getLogger(AnvilEventEmitter.class);

    @Inject
    @RestClient
    HeimdallClient client;

    // ── Layer 1: raw ──────────────────────────────────────────────────────────

    public void emit(HeimdallEvent event) {
        client.ingest(event)
              .onFailure().invoke(e ->
                      log.debug("AnvilEventEmitter: failed to send {} — {}", event.eventType(), e.getMessage()))
              .onFailure().recoverWithNull()
              .subscribe().with(__ -> {});
    }

    // ── Layer 2: typed ANVIL events (AV-08) ──────────────────────────────────

    public void traversalStarted(String nodeId, String direction, int maxHops, String tenantAlias) {
        emit(build(EventType.TRAVERSAL_STARTED, EventLevel.INFO, 0, Map.of(
                "nodeId",      nodeId,
                "direction",   direction,
                "maxHops",     maxHops,
                "tenantAlias", tenantAlias)));
    }

    public void traversalCompleted(long durationMs, int nodesFound, boolean hasMore, boolean cached) {
        emit(build(EventType.TRAVERSAL_COMPLETED, EventLevel.INFO, durationMs, Map.of(
                "durationMs", durationMs,
                "nodesFound", nodesFound,
                "hasMore",    hasMore,
                "cached",     cached)));
    }

    public void queryExecuted(String language, long durationMs, int rowCount, String tenantAlias) {
        emit(build(EventType.QUERY_EXECUTED, EventLevel.INFO, durationMs, Map.of(
                "language",    language,
                "durationMs",  durationMs,
                "rowCount",    rowCount,
                "tenantAlias", tenantAlias)));
    }

    public void queryBlocked(String reason, String language) {
        emit(build(EventType.QUERY_BLOCKED, EventLevel.WARN, 0, Map.of(
                "reason",   reason,
                "language", language)));
    }

    public void cacheHit(String nodeId, String direction, String tenantAlias) {
        emit(build(EventType.CACHE_HIT, EventLevel.INFO, 0, Map.of(
                "nodeId",      nodeId,
                "direction",   direction,
                "tenantAlias", tenantAlias)));
    }

    // ── Internal builder ──────────────────────────────────────────────────────

    private static HeimdallEvent build(EventType type, EventLevel level, long durationMs,
                                       Map<String, Object> payload) {
        return new HeimdallEvent(
                System.currentTimeMillis(),
                "anvil",
                type.name(),
                level,
                null,   // sessionId — not applicable for traversal calls
                null,   // userId
                null,   // correlationId
                durationMs,
                payload);
    }
}
