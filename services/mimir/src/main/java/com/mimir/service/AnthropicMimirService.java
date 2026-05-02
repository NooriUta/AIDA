package com.mimir.service;

import com.mimir.model.MimirAnswer;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;

/**
 * Anthropic Claude MIMIR service — stub pending MIMIR Foundation sprint.
 *
 * ADR-MIMIR-001 Tier 1: Anthropic wired in MIMIR Foundation once
 * the correct Quarkiverse chatLanguageModelSupplier API is confirmed.
 *
 * Current behaviour: returns a graceful "not yet configured" response
 * so routing works end-to-end without a real LLM key.
 *
 * TODO MIMIR Foundation: replace with @RegisterAiService + proper supplier
 * once Anthropic integration is validated against Quarkiverse 1.9.x API.
 */
@ApplicationScoped
public class AnthropicMimirService implements MimirAiPort {

    private static final Logger LOG = Logger.getLogger(AnthropicMimirService.class);

    @Override
    public MimirAnswer ask(String sessionId, String question, String dbName, String tenantAlias) {
        LOG.warnf("Anthropic provider stub called (session=%s). Real integration in MIMIR Foundation.", sessionId);
        return new MimirAnswer(
            "Anthropic Claude integration is coming in MIMIR Foundation sprint. " +
            "Please use model='deepseek' for now.",
            List.of(),
            List.of(),
            0.0,
            0L
        );
    }
}
