package com.mimir.byok;

import com.mimir.model.MimirAnswer;
import com.mimir.service.MimirAiPort;
import com.mimir.tools.AnvilTools;
import com.mimir.tools.ShuttleTools;
import com.mimir.tools.YggTools;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

import java.util.List;

/**
 * BYOK runtime adapter — wraps a {@link ChatModel} built by
 * {@link DynamicModelFactory} into a {@link MimirAiPort} backed by the same
 * three tool classes the static {@code MimirService} uses.
 *
 * <p>Built per request inside {@link com.mimir.routing.ModelRouter} so memory
 * isolation per tenant is naturally maintained (each {@link DynamicMimirService}
 * holds its own AI service handle, but shares the synthetic
 * {@link ChatMemoryProvider} so {@code @MemoryId sessionId} continues to scope
 * conversation state across both static and dynamic models).
 */
public final class DynamicMimirService implements MimirAiPort {

    /**
     * Hidden internal AI service interface — same SystemMessage/UserMessage as
     * {@code com.mimir.service.MimirService} but constructed programmatically
     * instead of by Quarkiverse code generation.
     */
    interface MimirChat {
        @SystemMessage(fromResource = "prompts/mimir-system.txt")
        @UserMessage("{question}")
        MimirAnswer ask(
                @MemoryId String sessionId,
                @V("question") String question,
                @V("dbName") String dbName,
                @V("tenantAlias") String tenantAlias
        );
    }

    private final MimirChat chat;

    public DynamicMimirService(ChatModel model,
                               ChatMemoryProvider memory,
                               AnvilTools anvil,
                               ShuttleTools shuttle,
                               YggTools ygg) {
        this.chat = AiServices.builder(MimirChat.class)
                .chatModel(model)
                .chatMemoryProvider(memory)
                .tools(List.of(anvil, shuttle, ygg))
                .build();
    }

    @Override
    public MimirAnswer ask(String sessionId, String question, String dbName, String tenantAlias) {
        return chat.ask(sessionId, question, dbName, tenantAlias);
    }
}
