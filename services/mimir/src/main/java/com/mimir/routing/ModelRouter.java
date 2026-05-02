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

    /**
     * Per-tenant routing scaffold (Q-MF2 partial — TIER2 MT-06 BYOK extends this).
     *
     * <p>FOUNDATION scope (this method): same as {@link #resolve(String)} but accepts tenantAlias
     * for future BYOK + per-tenant routing. Currently delegates to model-based resolution.
     *
     * <p>TIER2 MT-06 will add:
     * <pre>{@code
     * Optional<ResolvedKey> byok = credentialResolver.resolveForTenant(tenantAlias);
     * if (byok.isPresent()) {
     *     return new DynamicMimirService(modelFactory.buildFor(byok.get()), ...);
     * }
     * }</pre>
     *
     * @param requestedModel nullable model from AskRequest
     * @param tenantAlias    nullable tenant identifier (for future BYOK)
     * @return the appropriate MimirAiPort implementation
     */
    public MimirAiPort resolveForTenant(String requestedModel, String tenantAlias) {
        // FOUNDATION: tenantAlias не используется (scaffold для TIER2)
        // TIER2 MT-06 заменит этот метод на BYOK-aware logic
        return resolve(requestedModel);
    }
}
