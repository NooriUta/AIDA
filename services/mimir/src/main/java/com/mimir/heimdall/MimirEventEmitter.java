package com.mimir.heimdall;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import studio.seer.shared.EventLevel;
import studio.seer.shared.EventType;
import studio.seer.shared.HeimdallEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Fire-and-forget HEIMDALL event emitter for MIMIR.
 *
 * <p>Same pattern as ANVIL's {@code AnvilEventEmitter}: failures logged at DEBUG and silently swallowed —
 * observability must never affect MIMIR correctness.
 *
 * <p>Layers:
 * <ul>
 *   <li>Layer 1 (raw): {@link #emit(HeimdallEvent)} dispatches single event</li>
 *   <li>Layer 2 (typed): domain-specific methods (queryReceived, modelSelected, ...)
 *       map parameters → {@link HeimdallEvent} record</li>
 * </ul>
 *
 * <p>Events emitted (MC-05 scope):
 * <ol>
 *   <li>QUERY_RECEIVED — at {@code /api/ask} entry</li>
 *   <li>MODEL_SELECTED — after ModelRouter resolution (Decision #70)</li>
 *   <li>LLM_REQUEST_STARTED — before LLM call</li>
 *   <li>LLM_RESPONSE_RECEIVED — after LLM call with tokens</li>
 *   <li>TOOL_CALL_STARTED — wrapper in tools</li>
 *   <li>TOOL_CALL_COMPLETED — wrapper in tools</li>
 *   <li>RESPONSE_SYNTHESIZED — final answer ready</li>
 *   <li>FALLBACK_ACTIVATED — Tier1→Tier3 / 429 / timeout</li>
 *   <li>TIMEOUT — request exceeded threshold</li>
 * </ol>
 *
 * <p>BYOK + Quota events (MT-06/07/08) are added in TIER2 sprint:
 * LLM_CREDENTIAL_USED, QUOTA_EXCEEDED, TOKEN_USAGE_RECORDED, HIL_*.
 */
@ApplicationScoped
public class MimirEventEmitter {

    private static final Logger LOG = Logger.getLogger(MimirEventEmitter.class);
    private static final String SOURCE = "mimir";

    @Inject
    @RestClient
    HeimdallClient client;

    // ── Layer 1: raw ─────────────────────────────────────────────────────────

    public void emit(HeimdallEvent event) {
        client.ingest(event)
              .onFailure().invoke(e ->
                      LOG.debugf("MimirEventEmitter: failed to send %s — %s",
                              event.eventType(), e.getMessage()))
              .onFailure().recoverWithNull()
              .subscribe().with(__ -> {});
    }

    // ── Layer 2: typed MIMIR events ──────────────────────────────────────────

    public void queryReceived(String sessionId, String tenantAlias, int questionLength, String model) {
        emit(build(EventType.QUERY_RECEIVED, EventLevel.INFO, 0, sessionId, mapOf(
                "tenant_alias",      tenantAlias,
                "question_length",   questionLength,
                "model",             model)));
    }

    public void modelSelected(String sessionId, String model, String reason) {
        emit(build(EventType.MODEL_SELECTED, EventLevel.INFO, 0, sessionId, mapOf(
                "model",  model,
                "reason", reason)));
    }

    public void llmRequestStarted(String sessionId, String model, int promptTokens) {
        emit(build(EventType.LLM_REQUEST_STARTED, EventLevel.INFO, 0, sessionId, mapOf(
                "model",         model,
                "prompt_tokens", promptTokens)));
    }

    public void llmResponseReceived(String sessionId, String model, long durationMs,
                                    int promptTokens, int completionTokens) {
        emit(build(EventType.LLM_RESPONSE_RECEIVED, EventLevel.INFO, durationMs, sessionId, mapOf(
                "model",             model,
                "duration_ms",       durationMs,
                "prompt_tokens",     promptTokens,
                "completion_tokens", completionTokens)));
    }

    public void toolCallStarted(String sessionId, String toolName, Map<String, Object> args) {
        Map<String, Object> payload = new HashMap<>(args);
        payload.put("tool_name", toolName);
        emit(build(EventType.TOOL_CALL_STARTED, EventLevel.INFO, 0, sessionId, payload));
    }

    public void toolCallCompleted(String sessionId, String toolName, long durationMs, int resultSize) {
        emit(build(EventType.TOOL_CALL_COMPLETED, EventLevel.INFO, durationMs, sessionId, mapOf(
                "tool_name",   toolName,
                "duration_ms", durationMs,
                "result_size", resultSize)));
    }

    public void responseSynthesized(String sessionId, long totalDurationMs, int totalTokens, int affectedNodes) {
        emit(build(EventType.RESPONSE_SYNTHESIZED, EventLevel.INFO, totalDurationMs, sessionId, mapOf(
                "total_duration_ms", totalDurationMs,
                "total_tokens",      totalTokens,
                "affected_nodes",    affectedNodes)));
    }

    public void fallbackActivated(String sessionId, String reason, String newTier) {
        emit(build(EventType.FALLBACK_ACTIVATED, EventLevel.WARN, 0, sessionId, mapOf(
                "reason",    reason,
                "new_tier",  newTier)));
    }

    public void timeout(String sessionId, long elapsedMs, long thresholdMs) {
        emit(build(EventType.TIMEOUT, EventLevel.WARN, elapsedMs, sessionId, mapOf(
                "elapsed_ms",   elapsedMs,
                "threshold_ms", thresholdMs)));
    }

    /** Tier-3 demo cache served the response (TIER2 MT-02). */
    public void cacheHit(String sessionId) {
        emit(build(EventType.CACHE_HIT, EventLevel.INFO, 0, sessionId, mapOf(
                "tier", "demo-cache")));
    }

    // ── Internal builder ─────────────────────────────────────────────────────

    private static HeimdallEvent build(EventType type, EventLevel level, long durationMs,
                                       String sessionId, Map<String, Object> payload) {
        return new HeimdallEvent(
                System.currentTimeMillis(),
                SOURCE,
                type.name(),
                level,
                sessionId,
                null,            // userId — not propagated through tools yet
                null,            // correlationId — future feature
                durationMs,
                payload);
    }

    /** Mutable HashMap to allow downstream payload enrichment without conflicts. */
    private static Map<String, Object> mapOf(Object... kvs) {
        if (kvs.length % 2 != 0) {
            throw new IllegalArgumentException("mapOf requires even number of args");
        }
        Map<String, Object> m = new HashMap<>(kvs.length / 2);
        for (int i = 0; i < kvs.length; i += 2) {
            if (kvs[i + 1] != null) {
                m.put((String) kvs[i], kvs[i + 1]);
            }
        }
        return m;
    }
}
