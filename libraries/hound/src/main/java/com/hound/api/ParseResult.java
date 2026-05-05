package com.hound.api;

import java.util.List;
import java.util.Map;

/**
 * Result of parsing a single SQL file.
 *
 * <p>Returned by {@link HoundParser#parse} and {@link HoundParser#parseBatch}.
 * Downstream consumers (Dali) use this to track progress and aggregate stats.
 */
public record ParseResult(
        /** Absolute or relative path of the parsed file. */
        String file,

        /**
         * Number of DaliAtom vertices actually written to YGG.
         * In REMOTE_BATCH mode this is the actual batch insert count.
         * In DISABLED/REMOTE mode this is the semantic atom count (approximation).
         */
        int atomCount,

        /**
         * Total vertex count across all types actually inserted into YGG.
         * Derived from {@link #vertexStats} in REMOTE_BATCH mode.
         */
        int vertexCount,

        /** Number of graph edges written to YGG. */
        int edgeCount,

        /**
         * Number of edges dropped during batch serialisation because one or both endpoints
         * could not be resolved (not in the current batch and not in canonicalRids).
         * Zero when writing is disabled or in REMOTE (non-batch) mode.
         */
        int droppedEdgeCount,

        /**
         * Per-vertex-type breakdown: type → [inserted, duplicate].
         * Populated only in REMOTE_BATCH mode; empty map otherwise.
         * "inserted" = vertex sent to YGG (new); "duplicate" = vertex already existed (skipped).
         */
        Map<String, int[]> vertexStats,

        /** Fraction of column atoms semantically resolved in Hound (0.0–1.0). */
        double resolutionRate,

        /**
         * Number of column-reference atoms successfully resolved (primary_status = "RESOLVED").
         * Used for per-file resolution breakdown in the UI.
         */
        int atomsResolved,

        /**
         * Number of column-reference atoms that failed resolution (status = "unresolved").
         * Does NOT include constants / function-calls (those are excluded from the rate).
         */
        int atomsUnresolved,

        /** Non-fatal warnings (e.g. unresolved refs in soft-fail mode). */
        List<String> warnings,

        /** Errors encountered during parsing (does not include exceptions — those throw). */
        List<String> errors,

        /** Wall-clock time for parse + walk + resolve + write, in milliseconds. */
        long durationMs
) {
    public ParseResult {
        if (warnings    == null) warnings    = List.of();
        if (errors      == null) errors      = List.of();
        if (vertexStats == null) vertexStats = Map.of();
    }

    /** Convenience: true if no errors were recorded. */
    public boolean isSuccess() {
        return errors.isEmpty();
    }
}
