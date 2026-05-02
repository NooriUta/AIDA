package com.mimir.routing;

import com.mimir.byok.DynamicMimirService;
import com.mimir.byok.DynamicModelFactory;
import com.mimir.byok.LlmCredentialResolver;
import com.mimir.byok.ResolvedKey;
import com.mimir.heimdall.MimirEventEmitter;
import com.mimir.service.AnthropicMimirService;
import com.mimir.service.MimirAiPort;
import com.mimir.service.MimirService;
import com.mimir.service.OllamaMimirService;
import com.mimir.tools.AnvilTools;
import com.mimir.tools.ShuttleTools;
import com.mimir.tools.YggTools;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.util.Optional;

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

    @Inject MimirService           deepSeek;
    @Inject AnthropicMimirService  anthropic;
    @Inject OllamaMimirService     ollama;

    @Inject LlmCredentialResolver  credentialResolver;
    @Inject DynamicModelFactory    modelFactory;
    @Inject ChatMemoryProvider     chatMemoryProvider;
    @Inject AnvilTools             anvilTools;
    @Inject ShuttleTools           shuttleTools;
    @Inject YggTools               yggTools;
    @Inject MimirEventEmitter      eventEmitter;

    @ConfigProperty(name = "mimir.default-model", defaultValue = "deepseek")
    String defaultModel;

    @ConfigProperty(name = "mimir.byok.enabled", defaultValue = "true")
    boolean byokEnabled;

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
        if (byokEnabled && tenantAlias != null && !tenantAlias.isBlank()) {
            try {
                Optional<ResolvedKey> byok = credentialResolver.resolveForTenant(tenantAlias);
                if (byok.isPresent()) {
                    ResolvedKey rk = byok.get();
                    LOG.debugf("BYOK active for tenant=%s provider=%s key=%s",
                            tenantAlias, rk.provider(), rk.keyMask());
                    eventEmitter.llmCredentialUsed(tenantAlias, rk.provider(), rk.keyMask());
                    return new DynamicMimirService(
                            modelFactory.buildFor(rk),
                            chatMemoryProvider,
                            anvilTools, shuttleTools, yggTools);
                }
            } catch (Exception e) {
                LOG.warnf(e, "BYOK build failed for tenant=%s — falling back to shared default", tenantAlias);
            }
        }
        return resolve(requestedModel);
    }
}
