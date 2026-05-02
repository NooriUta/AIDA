package com.mimir.memory;

import dev.langchain4j.memory.ChatMemory;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import io.quarkus.arc.DefaultBean;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.ConcurrentHashMap;

/**
 * MIMIR chat memory configuration.
 *
 * Keeps a ConcurrentHashMap<Object, ChatMemory> per session:
 *   - Each sessionId gets an isolated MessageWindowChatMemory (configurable size)
 *   - DELETE /api/sessions/{id} evicts and clears a specific session's history
 *
 * Deliberately avoids ChatMemoryStore — its package changed across LangChain4j
 * versions (dev.langchain4j.store.memory → dev.langchain4j.store.memory.chat),
 * which causes NoClassDefFoundError in Quarkus test JVM even when it compiles.
 * Storing ChatMemory instances directly is simpler and version-agnostic.
 */
@ApplicationScoped
public class MimirMemoryConfiguration {

    @ConfigProperty(name = "mimir.memory.max-messages", defaultValue = "20")
    int maxMessages;

    /** Thread-safe per-session memory store. */
    private final ConcurrentHashMap<Object, ChatMemory> memories = new ConcurrentHashMap<>();

    @Produces
    @DefaultBean
    @ApplicationScoped
    public ChatMemoryProvider chatMemoryProvider() {
        return sessionId -> memories.computeIfAbsent(sessionId,
            id -> MessageWindowChatMemory.withMaxMessages(maxMessages));
    }

    /** Clears chat history for a session (called on DELETE /api/sessions/:id). */
    public void evict(String sessionId) {
        ChatMemory memory = memories.remove(sessionId);
        if (memory != null) {
            memory.clear();
        }
    }
}
