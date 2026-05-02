package com.mimir.model.anvil;

import java.util.List;

/** Response from ANVIL {@code POST /api/lineage} — DATA_FLOW edges. */
public record LineageResult(
        List<ImpactNode> nodes,
        List<ImpactEdge> edges,
        long             executionMs
) {}
