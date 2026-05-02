package com.mimir.service;

import com.mimir.model.MimirAnswer;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

/**
 * Ollama MIMIR service — stub until provider is decided (local vs. cloud).
 *
 * ADR-MIMIR-001: Tier 3 — Ollama provider TBD (local or cloud).
 * Returns MimirAnswer.unavailable() so the API responds gracefully.
 * Replace with real @RegisterAiService once provider is selected (MIMIR Tier2 sprint).
 */
@ApplicationScoped
public class OllamaMimirService implements MimirAiPort {

    private static final Logger LOG = Logger.getLogger(OllamaMimirService.class);

    @Override
    public MimirAnswer ask(String sessionId, String question, String dbName, String tenantAlias) {
        LOG.warnf("Ollama provider not yet wired (session=%s) — returning stub. See ADR-MIMIR-001 Tier3.", sessionId);
        return new MimirAnswer(
            "Ollama is not yet configured. Please use 'deepseek' or 'anthropic' as the model.",
            java.util.List.of(),
            java.util.List.of(),
            0.0,
            0L
        );
    }
}
