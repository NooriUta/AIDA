package com.mimir.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;

/**
 * Tools backed by SHUTTLE :8080 GraphQL — node search.
 * Stubs in MP-04 (SPRINT_MIMIR_PREP). Full implementation in MIMIR Foundation.
 */
@ApplicationScoped
public class ShuttleTools {

    @ConfigProperty(name = "mimir.shuttle.url", defaultValue = "http://localhost:8080")
    String shuttleUrl;

    @Tool("Search for nodes in the lineage graph by name pattern. Supports wildcards (*). " +
          "Returns matching node IDs and display names.")
    public List<Map<String, String>> search_nodes(
        @P("Name pattern to search for, wildcards supported") String namePattern,
        @P("Node type filter: TABLE, PROCEDURE, VIEW, or ALL") String nodeType,
        @P("Maximum results to return, default 20") int limit
    ) {
        // TODO(MIMIR-Foundation): POST shuttleUrl/graphql searchNodes query
        return List.of(Map.of(
            "stub", "search_nodes not yet implemented",
            "pattern", namePattern,
            "nodeType", nodeType
        ));
    }
}
