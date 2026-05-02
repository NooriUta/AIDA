package com.mimir.memory;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * MIMIR chat memory configuration.
 *
 * Uses an internal ConcurrentHashMap-backed ChatMemoryStore so that:
 *   - Each sessionId gets an isolated MessageWindowChatMemory (20 messages)
 *   - DELETE /api/sessions/{id} can evict a specific session's history
 *
 * Note: InMemoryChatMemoryStore moved to dev.langchain4j.store.memory.chat in LangChain4j 1.x.
 * We use ChatMemoryStore interface directly to avoid version-specific class names.
 */
@ApplicationScoped
public class MimirMemoryConfiguration {

    @ConfigProperty(name = "mimir.memory.max-messages", defaultValue = "20")
    int maxMessages;

    /** Thread-safe in-memory store keyed by session id. */
    private final ChatMemoryStore store = new InMemoryStore();

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

    /** Clears chat history for a session (called on DELETE /api/sessions/:id). */
    public void evict(String sessionId) {
        store.deleteMessages(sessionId);
    }

    // ── Simple ConcurrentHashMap implementation of ChatMemoryStore ────────────

    private static final class InMemoryStore implements ChatMemoryStore {

        private final Map<Object, List<ChatMessage>> data = new ConcurrentHashMap<>();

        @Override
        public List<ChatMessage> getMessages(Object memoryId) {
            return data.getOrDefault(memoryId, new ArrayList<>());
        }

        @Override
        public void updateMessages(Object memoryId, List<ChatMessage> messages) {
            data.put(memoryId, new ArrayList<>(messages));
        }

        @Override
        public void deleteMessages(Object memoryId) {
            data.remove(memoryId);
        }
    }
}
