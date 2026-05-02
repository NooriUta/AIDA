package com.mimir.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import io.quarkiverse.langchain4j.ModelName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.function.Supplier;

/**
 * CDI supplier that provides the Anthropic ChatLanguageModel instance.
 * Used by AnthropicMimirService via @RegisterAiService(chatLanguageModelSupplier=...).
 *
 * The @ModelName("anthropic") qualifier selects the Anthropic-configured bean registered
 * by quarkus-langchain4j-anthropic extension (quarkus.langchain4j.anthropic.*).
 */
@ApplicationScoped
public class AnthropicModelSupplier implements Supplier<ChatLanguageModel> {

    @Inject
    @ModelName("anthropic")
    ChatLanguageModel model;

    @Override
    public ChatLanguageModel get() {
        return model;
    }
}
