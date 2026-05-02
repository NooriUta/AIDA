package com.mimir.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.InMemoryChatMemoryStore;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class MimirMemoryConfiguration {

    @ConfigProperty(name = "mimir.memory.max-messages", defaultValue = "20")
    int maxMessages;

    private final InMemoryChatMemoryStore store = new InMemoryChatMemoryStore();

    @Produces
    @DefaultBean
    @ApplicationScoped
    public ChatMemoryProvider chatMemoryProvider() {
        return sessionId -> MessageWindowChatMemory.builder()
            .id(sessionId)
            .maxMessages(maxMessages)
            .chatMemoryStore(store)
            .build();
    }

    /** Clears chat history for a session (e.g. on DELETE /api/sessions/:id). */
    public void evict(String sessionId) {
        ChatMemory mem = MessageWindowChatMemory.builder()
            .id(sessionId)
            .maxMessages(maxMessages)
            .chatMemoryStore(store)
            .build();
        mem.clear();
    }
}
