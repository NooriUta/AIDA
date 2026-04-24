package studio.seer.anvil.model;

import java.util.List;

public record LineageResult(
        List<ImpactNode> nodes,
        List<ImpactEdge> edges,
        long             executionMs
) {}
