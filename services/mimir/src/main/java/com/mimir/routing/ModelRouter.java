package com.mimir.routing;

import com.mimir.service.AnthropicMimirService;
import com.mimir.service.MimirAiPort;
import com.mimir.service.MimirService;
import com.mimir.service.OllamaMimirService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

/**
 * Routes an incoming AskRequest to the appropriate LLM service.
 *
 * ADR-MIMIR-001 Tier 1: model selection via request field + server-side default.
 *
 * Priority:
 *   1. request.model() — explicit client choice
 *   2. mimir.default-model config — server default (env: MIMIR_DEFAULT_MODEL)
 *   3. fallback → deepseek
 *
 * Future (MIMIR Foundation): cost-based routing, per-tenant overrides, rate-limit fallback.
 */
@ApplicationScoped
public class ModelRouter {

    private static final Logger LOG = Logger.getLogger(ModelRouter.class);

    @Inject MimirService        deepSeek;
    @Inject AnthropicMimirService anthropic;
    @Inject OllamaMimirService  ollama;

    @ConfigProperty(name = "mimir.default-model", defaultValue = "deepseek")
    String defaultModel;

    /**
     * Resolves the AI service for the given model name.
     *
     * @param requestedModel nullable model from AskRequest ("deepseek"|"anthropic"|"ollama")
     * @return the appropriate MimirAiPort implementation
     */
    public MimirAiPort resolve(String requestedModel) {
        String model = (requestedModel != null && !requestedModel.isBlank())
            ? requestedModel.toLowerCase().trim()
            : defaultModel;

        LOG.debugf("Routing request to model: %s (requested=%s, default=%s)", model, requestedModel, defaultModel);

        return switch (model) {
            case "anthropic", "claude" -> anthropic;
            case "ollama"              -> ollama;
            default                    -> deepSeek;  // "deepseek", unknown → deepseek
        };
    }
}
