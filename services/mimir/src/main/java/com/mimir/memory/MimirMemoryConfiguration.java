package com.mimir.memory;

import dev.langchain4j.memory.chat.ChatMemoryProvider;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

/**
 * MIMIR session memory manager.
 *
 * Delegates to Quarkiverse's synthetic ChatMemoryProvider — generated automatically
 * for MimirService because of @RegisterAiService + @MemoryId. We do NOT produce our
 * own ChatMemoryProvider: in Quarkiverse 1.9.x the synthetic bean conflicts with any
 * @DefaultBean producer, causing AmbiguousResolutionException even when @DefaultBean
 * is specified (synthetic beans bypass the DefaultBean suppression mechanism).
 *
 * Max-messages window is configured via quarkus.langchain4j.chat-memory.max-messages
 * (Quarkiverse default: 10; set to 20 in application.properties).
 *
 * Evict usage: DELETE /api/sessions/{id} → AskResource → evict(sessionId).
 */
@ApplicationScoped
public class MimirMemoryConfiguration {

    private static final Logger LOG = Logger.getLogger(MimirMemoryConfiguration.class);

    @Inject
    ChatMemoryProvider chatMemoryProvider;

    /** Clears chat history for a session (called on DELETE /api/sessions/:id). */
    public void evict(String sessionId) {
        LOG.debugf("Evicting chat memory for session: %s", sessionId);
        chatMemoryProvider.get(sessionId).clear();
    }
}
