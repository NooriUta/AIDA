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
 * MIMIR AI service — LangChain4j @RegisterAiService interface.
 *
 * Session isolation: @MemoryId = tenantId:uuid — каждая сессия получает
 * свой MessageWindowChatMemory (20 сообщений, InMemoryChatMemoryStore).
 *
 * Tool routing (Decision #63 — Variant B @Tool only, no McpToolProvider):
 *   AnvilTools  → ANVIL :9095 (find_impact, query_lineage)
 *   ShuttleTools→ SHUTTLE :8080/graphql (search_nodes)
 *   YggTools    → YGG (ArcadeDB) :2480 (get_procedure_source, count_dependencies)
 *
 * LLM tier:
 *   dev  → DeepSeek via openai-compat extension (quarkus.langchain4j.default-config=openai)
 *   prod → Anthropic Claude Sonnet (quarkus.langchain4j.default-config=anthropic)
 *   Full tier routing (Ollama Tier2 + JSON cache Tier3) in MIMIR Foundation sprint.
 */
@RegisterAiService(tools = {AnvilTools.class, ShuttleTools.class, YggTools.class})
@ApplicationScoped
public interface MimirService {

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
    MimirAnswer ask(
        @MemoryId  String sessionId,
        @V("question")    String question,
        @V("dbName")      String dbName,
        @V("tenantAlias") String tenantAlias
    );
}
