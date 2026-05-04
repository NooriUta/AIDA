package com.hound.semantic.model;

/**
 * HAL3-01 (ADR-HND-010): Lightweight record for write-side lineage edges.
 * Accumulated in-memory during ParseTreeWalk, written to ArcadeDB as edges
 * in a separate pass after the main atom pipeline.
 */
public record CompensationStats(
        String statementGeoid,
        String edgeType,
        String targetGeoid,
        String targetKind,
        String sessionId
) {
    public static final String EDGE_ASSIGNS_TO_VARIABLE   = "ASSIGNS_TO_VARIABLE";
    public static final String EDGE_WRITES_TO_PARAMETER   = "WRITES_TO_PARAMETER";
    public static final String EDGE_READS_FROM_CURSOR     = "READS_FROM_CURSOR";

    public static final String KIND_VARIABLE  = "VARIABLE";
    public static final String KIND_PARAMETER = "PARAMETER";
    public static final String KIND_CURSOR    = "CURSOR";
}
