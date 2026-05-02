package com.mimir.tools;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;
import java.util.Map;

/**
 * Tools backed by ANVIL :9095 — lineage impact analysis.
 * Stubs in MP-04 (SPRINT_MIMIR_PREP). Full implementation in MIMIR Foundation.
 */
@ApplicationScoped
public class AnvilTools {

    @ConfigProperty(name = "mimir.anvil.url", defaultValue = "http://localhost:9095")
    String anvilUrl;

    @Tool("Find downstream and upstream objects affected by changes to the given node. " +
          "Returns list of dependent node IDs and names.")
    public List<Map<String, String>> find_impact(
        @P("The node ID to analyze impact for") String nodeId,
        @P("Direction: DOWNSTREAM or UPSTREAM") String direction,
        @P("Maximum hops to traverse, default 3") int maxHops
    ) {
        // TODO(MIMIR-Foundation): POST anvilUrl/api/impact
        return List.of(Map.of(
            "stub", "find_impact not yet implemented",
            "nodeId", nodeId,
            "direction", direction
        ));
    }

    @Tool("Query the lineage path between two nodes in the graph. " +
          "Returns the traversal path with intermediate nodes.")
    public Map<String, Object> query_lineage(
        @P("Source node ID") String sourceId,
        @P("Target node ID") String targetId,
        @P("Maximum path length") int maxHops
    ) {
        // TODO(MIMIR-Foundation): POST anvilUrl/api/lineage
        return Map.of(
            "stub", "query_lineage not yet implemented",
            "sourceId", sourceId,
            "targetId", targetId
        );
    }
}
