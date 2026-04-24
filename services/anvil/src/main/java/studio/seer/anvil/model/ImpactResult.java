package studio.seer.anvil.model;

import java.util.List;

public record ImpactResult(
        ImpactNode       rootNode,
        List<ImpactNode> nodes,
        List<ImpactEdge> edges,
        int              totalAffected,
        boolean          hasMore,
        boolean          cached,
        long             executionMs
) {}
