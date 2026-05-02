package com.mimir.orchestration;

import com.mimir.cache.DemoCacheService;
import com.mimir.heimdall.MimirEventEmitter;
import com.mimir.model.AskRequest;
import com.mimir.model.MimirAnswer;
import com.mimir.persistence.MimirSession;
import com.mimir.persistence.MimirSessionRepository;
import com.mimir.routing.ModelRouter;
import com.mimir.tenant.DbNameResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.Optional;

/**
 * Multi-agent reasoning orchestrator — ADR-MIMIR-001 Tier 2 stub.
 *
 * <p>Current behavior (FOUNDATION): single-agent — delegates to {@link ModelRouter#resolveForTenant}.
 *
 * <p>Wires in:
 * <ul>
 *     <li>{@link DbNameResolver} — tenantAlias → dbName mapping (Q-MC1 closed)</li>
 *     <li>{@link MimirEventEmitter} — query_received + model_selected events</li>
 *     <li>Graceful fallback on LLM exceptions → {@link MimirAnswer#unavailable()}</li>
 * </ul>
 *
 * <p>Future (TIER2): producer-critic, ensemble, delegation patterns when {@code request.agents()} is non-empty.
 */
@ApplicationScoped
public class MimirOrchestrator {

    private static final Logger LOG = Logger.getLogger(MimirOrchestrator.class);

    @Inject ModelRouter             router;
    @Inject DbNameResolver          dbNameResolver;
    @Inject MimirEventEmitter       eventEmitter;
    @Inject MimirSessionRepository  sessionRepo;
    @Inject DemoCacheService        demoCache;

    /**
     * Single-agent ask (current) — delegates to ModelRouter.
     * Will become multi-agent in MIMIR Foundation (request.agents() list).
     */
    public MimirAnswer ask(AskRequest request, String sessionId, String tenantAlias) {
        long start = System.currentTimeMillis();
        String questionPreview = request.question() == null ? "" : request.question();
        String requestedModel = request.model();

        eventEmitter.queryReceived(sessionId, tenantAlias, questionPreview.length(), requestedModel);

        if (request.agents() != null && !request.agents().isEmpty()) {
            LOG.warnf("Multi-agent reasoning requested (agents=%s) but not yet implemented — " +
                    "falling back to primary model. Implement in TIER2 sprint.", request.agents());
        }

        String dbName = request.dbName() != null && !request.dbName().isBlank()
                ? request.dbName()
                : dbNameResolver.forTenant(tenantAlias);

        // TIER2 MT-02: forced demo mode — answer from cache, skip live model entirely
        if (demoCache.isDemoMode()) {
            Optional<MimirAnswer> hit = demoCache.tryCache(questionPreview);
            if (hit.isPresent()) {
                MimirAnswer answer = hit.get();
                long durationMs = System.currentTimeMillis() - start;
                eventEmitter.modelSelected(sessionId, "demo-cache", "demo-mode");
                eventEmitter.cacheHit(sessionId);
                eventEmitter.responseSynthesized(sessionId, durationMs, 0,
                        answer.highlightNodeIds() != null ? answer.highlightNodeIds().size() : 0);
                sessionRepo.save(MimirSession.completed(sessionId, tenantAlias,
                        answer.toolCallsUsed(), answer.highlightNodeIds()));
                return answer;
            }
            LOG.debugf("Demo mode active but no cache pattern matched for tenant=%s session=%s — falling through to live model",
                    tenantAlias, sessionId);
        }

        // Resolve model (per-tenant aware — scaffold for TIER2 BYOK)
        var port = router.resolveForTenant(requestedModel, tenantAlias);
        String resolvedModel = requestedModel != null && !requestedModel.isBlank()
                ? requestedModel
                : "default";
        eventEmitter.modelSelected(sessionId, resolvedModel,
                requestedModel != null ? "request" : "default");

        try {
            MimirAnswer answer = port.ask(sessionId, request.question(), dbName, tenantAlias);
            long durationMs = System.currentTimeMillis() - start;
            eventEmitter.responseSynthesized(sessionId, durationMs,
                    0,  // total tokens — populated in TIER2 MT-07
                    answer.highlightNodeIds() != null ? answer.highlightNodeIds().size() : 0);

            // MC-08: persist session in FRIGG (fire-and-forget — failure doesn't break response)
            sessionRepo.save(MimirSession.completed(sessionId, tenantAlias,
                    answer.toolCallsUsed(), answer.highlightNodeIds()));

            return answer;
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - start;
            String reason = classifyFailure(e);
            LOG.warnf(e, "MIMIR LLM call failed (tenant=%s session=%s reason=%s) — falling back to unavailable",
                    tenantAlias, sessionId, reason);
            if ("timeout".equals(reason)) {
                eventEmitter.timeout(sessionId, durationMs, 30_000L);
            }

            // TIER2 MT-02: Tier-1 failed → try Tier-3 demo cache (DeepSeek-only fallback path,
            // ADR-MIMIR-002 in effect until 2026-06-02 Ollama re-eval)
            Optional<MimirAnswer> cached = demoCache.tryCache(questionPreview);
            if (cached.isPresent()) {
                eventEmitter.fallbackActivated(sessionId, reason, "demo-cache");
                eventEmitter.cacheHit(sessionId);
                MimirAnswer answer = cached.get();
                sessionRepo.save(MimirSession.completed(sessionId, tenantAlias,
                        answer.toolCallsUsed(), answer.highlightNodeIds()));
                return answer;
            }

            eventEmitter.fallbackActivated(sessionId, reason, "unavailable");
            // MC-08: persist failed state too — useful for HEIMDALL forensics
            sessionRepo.save(MimirSession.failed(sessionId, tenantAlias));
            return MimirAnswer.unavailable();
        }
    }

    /**
     * Classifies LLM failure for audit:
     * <ul>
     *     <li>"429" — upstream rate limit</li>
     *     <li>"timeout" — request exceeded threshold</li>
     *     <li>"error" — generic</li>
     * </ul>
     */
    private static String classifyFailure(Exception e) {
        String msg = String.valueOf(e.getMessage()).toLowerCase();
        if (msg.contains("429") || msg.contains("rate limit") || msg.contains("too many requests")) {
            return "429";
        }
        if (msg.contains("timeout") || msg.contains("timed out") || e instanceof java.util.concurrent.TimeoutException) {
            return "timeout";
        }
        return "error";
    }
}
