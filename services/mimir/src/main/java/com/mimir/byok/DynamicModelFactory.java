package com.mimir.byok;

import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.time.Duration;

/**
 * Builds a {@link ChatModel} from a tenant's resolved BYOK key.
 *
 * <p>Provider whitelist is enforced here — unknown providers fail closed with
 * {@link CredentialException} rather than silently routing to default. This
 * matters because a malformed FRIGG row (provider="claude" instead of
 * "anthropic") would otherwise look like a working tenant config.
 *
 * <p>Default model names mirror what shipped in CORE; the per-tenant
 * {@code ResolvedKey.modelName()} overrides them when present.
 */
@ApplicationScoped
public class DynamicModelFactory {

    private static final Logger LOG = Logger.getLogger(DynamicModelFactory.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    public ChatModel buildFor(ResolvedKey key) {
        if (key == null) throw new CredentialException("ResolvedKey is null");
        String provider = key.provider() == null ? "" : key.provider().toLowerCase();
        String plain = key.key().reveal();

        LOG.debugf("Building per-tenant ChatModel: tenant=%s provider=%s key=%s",
                key.tenantAlias(), provider, key.keyMask());

        return switch (provider) {
            case "deepseek", "openai" -> {
                OpenAiChatModel.OpenAiChatModelBuilder b = OpenAiChatModel.builder()
                        .apiKey(plain)
                        .timeout(TIMEOUT)
                        .temperature(0.1)
                        .maxTokens(2048);
                if (key.baseUrl() != null && !key.baseUrl().isBlank()) {
                    b.baseUrl(key.baseUrl());
                } else if ("deepseek".equals(provider)) {
                    b.baseUrl("https://api.deepseek.com/v1");
                }
                b.modelName(coalesce(key.modelName(),
                        "deepseek".equals(provider) ? "deepseek-chat" : "gpt-4o-mini"));
                yield b.build();
            }
            case "anthropic" -> AnthropicChatModel.builder()
                    .apiKey(plain)
                    .modelName(coalesce(key.modelName(), "claude-sonnet-4-6"))
                    .temperature(0.1)
                    .maxTokens(2048)
                    .timeout(TIMEOUT)
                    .build();
            case "ollama-cloud", "ollama" -> {
                if (key.baseUrl() == null || key.baseUrl().isBlank()) {
                    throw new CredentialException("ollama provider requires baseUrl");
                }
                yield OllamaChatModel.builder()
                        .baseUrl(key.baseUrl())
                        .modelName(coalesce(key.modelName(), "qwen2.5:14b"))
                        .temperature(0.1)
                        .timeout(TIMEOUT)
                        .build();
            }
            default -> throw new CredentialException("Unsupported provider: " + provider);
        };
    }

    private static String coalesce(String a, String b) {
        return (a == null || a.isBlank()) ? b : a;
    }
}
