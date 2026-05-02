package com.mimir.service;

import com.mimir.model.MimirAnswer;
import com.mimir.tools.AnvilTools;
import com.mimir.tools.ShuttleTools;
import com.mimir.tools.YggTools;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Anthropic Claude MIMIR service.
 * Uses AnthropicModelSupplier to inject the @ModelName("anthropic") ChatLanguageModel.
 *
 * ADR-MIMIR-001: Tier 1 — opt-in via request.model="anthropic".
 * Activated by setting QUARKUS_LANGCHAIN4J_DEFAULT_CONFIG=anthropic or
 * passing model="anthropic" in the AskRequest.
 */
@RegisterAiService(
    tools = {AnvilTools.class, ShuttleTools.class, YggTools.class},
    chatLanguageModelSupplier = AnthropicModelSupplier.class
)
@ApplicationScoped
public interface AnthropicMimirService extends MimirAiPort {

    @SystemMessage("""
        You are MIMIR — a data lineage analysis assistant for legacy SQL codebases.
        Working database: {dbName}. Tenant: {tenantAlias}.

        RULES:
        1. ALWAYS call deterministic tools — never guess node IDs or table names.
        2. Include highlightNodeIds in your response so LOOM can mark them.
        3. Respond in the user's language (RU or EN).
        4. If unsure about a node, call search_nodes first.
        5. Never hallucinate object names, schemas, or lineage paths.
        """)
    @UserMessage("{question}")
    @Override
    MimirAnswer ask(
        @MemoryId  String sessionId,
        @V("question")    String question,
        @V("dbName")      String dbName,
        @V("tenantAlias") String tenantAlias
    );
}
