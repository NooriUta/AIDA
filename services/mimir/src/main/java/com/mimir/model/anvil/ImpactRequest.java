package com.mimir.model.anvil;

import java.util.List;

/** Request body for ANVIL {@code POST /api/impact}. Mirrored from {@code studio.seer.anvil.model.ImpactRequest}. */
public record ImpactRequest(
        String       nodeId,
        String       direction,    // "downstream" | "upstream"
        int          maxHops,      // 1..N
        String       dbName,       // hound_{tenant} via DbNameResolver
        List<String> includeTypes  // optional, default [DaliRoutine, DaliStatement, DaliTable, DaliColumn]
) {}
