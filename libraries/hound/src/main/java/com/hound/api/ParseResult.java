package com.hound.api;

import java.util.List;

/**
 * Result of parsing a single SQL file.
 *
 * <p>Returned by {@link HoundParser#parse} and {@link HoundParser#parseBatch}.
 * Downstream consumers (Dali) use this to track progress and aggregate stats.
 */
public record ParseResult(
        /** Absolute or relative path of the parsed file. */
        String file,

        /** Number of semantic atoms extracted (column references, function calls, etc.). */
        int atomCount,

        /** Number of graph vertices written to YGG. */
        int vertexCount,

        /** Number of graph edges written to YGG. */
        int edgeCount,

        /** Fraction of column atoms successfully resolved (0.0–1.0). */
        double resolutionRate,

        /** Non-fatal warnings (e.g. unresolved refs in soft-fail mode). */
        List<String> warnings,

        /** Errors encountered during parsing (does not include exceptions — those throw). */
        List<String> errors,

        /** Wall-clock time for parse + walk + resolve + write, in milliseconds. */
        long durationMs
) {
    public ParseResult {
        if (warnings == null) warnings = List.of();
        if (errors   == null) errors   = List.of();
    }

    /** Convenience: true if no errors were recorded. */
    public boolean isSuccess() {
        return errors.isEmpty();
    }
}
