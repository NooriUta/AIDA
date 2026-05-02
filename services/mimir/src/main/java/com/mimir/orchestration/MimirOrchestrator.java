package com.mimir.orchestration;

import com.mimir.model.AskRequest;
import com.mimir.model.MimirAnswer;
import com.mimir.routing.ModelRouter;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Multi-agent reasoning orchestrator — ADR-MIMIR-001 Tier 2.
 *
 * STUB — implementation in MIMIR Foundation sprint.
 *
 * Planned patterns:
 *   producer-critic: deepseek generates draft → anthropic critiques/refines
 *   ensemble:        all agents answer in parallel → merge by confidence
 *   delegation:      route sub-questions to specialized models
 *
 * Current behavior: delegates to ModelRouter.resolve(request.model()) single-agent.
 */
@ApplicationScoped
public class MimirOrchestrator {

    private static final Logger LOG = Logger.getLogger(MimirOrchestrator.class);

    @Inject ModelRouter router;

    /**
     * Single-agent ask (current) — delegates to ModelRouter.
     * Will become multi-agent in MIMIR Foundation (request.agents() list).
     */
    public MimirAnswer ask(AskRequest request, String sessionId, String tenantAlias) {
        if (request.agents() != null && !request.agents().isEmpty()) {
            LOG.warnf("Multi-agent reasoning requested (agents=%s) but not yet implemented — " +
                "falling back to primary model. Implement in MIMIR Foundation sprint.", request.agents());
        }
        String dbName = request.dbName() != null ? request.dbName() : "hound_" + tenantAlias;
        return router.resolve(request.model()).ask(sessionId, request.question(), dbName, tenantAlias);
    }

    // TODO MIMIR Foundation: implement producer-critic pattern
    // private MimirAnswer producerCritic(AskRequest req, String sessionId, String tenantAlias) { ... }

    // TODO MIMIR Foundation: implement ensemble pattern
    // private MimirAnswer ensemble(List<String> agents, AskRequest req, String sessionId) { ... }
}
