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
 * DeepSeek MIMIR service — default model (openai-compatible, DeepSeek API).
 *
 * Uses quarkus.langchain4j.default-config=openai → DeepSeek via api.deepseek.com/v1.
 * ADR-MIMIR-001: Tier 1 — active default provider.
 *
 * Tool routing (Decision #63 — Variant B @Tool only):
 *   AnvilTools   → ANVIL :9095 (find_impact, query_lineage)
 *   ShuttleTools → SHUTTLE :8080/graphql (search_nodes)
 *   YggTools     → YGG ArcadeDB :2480 (get_procedure_source, count_dependencies)
 */
@RegisterAiService(tools = {AnvilTools.class, ShuttleTools.class, YggTools.class})
@ApplicationScoped
public interface MimirService extends MimirAiPort {

    @SystemMessage(fromResource = "prompts/mimir-system.txt")
    @UserMessage("{question}")
    @Override
    MimirAnswer ask(
        @MemoryId  String sessionId,
        @V("question")    String question,
        @V("dbName")      String dbName,
        @V("tenantAlias") String tenantAlias
    );
}
