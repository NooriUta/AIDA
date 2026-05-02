package com.mimir.model.anvil;

import java.util.List;

/** Response from ANVIL {@code POST /api/impact}. */
public record ImpactResult(
        ImpactNode       rootNode,
        List<ImpactNode> nodes,
        List<ImpactEdge> edges,
        int              totalAffected,
        boolean          hasMore,       // true when nodes.size() reached limit
        boolean          cached,        // true when result from Caffeine cache
        long             executionMs
) {}
