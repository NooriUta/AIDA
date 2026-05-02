package com.mimir.model.anvil;

/** Request body for ANVIL {@code POST /api/lineage}. */
public record LineageRequest(
        String nodeId,
        String direction,   // "upstream" | "downstream" | "both"
        String dbName,
        int    maxHops
) {}
