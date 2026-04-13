package studio.seer.shared;

import java.util.List;

/**
 * Parse result for a single file within a batch session.
 * Populated after the file completes; included in {@link Session#fileResults()}.
 */
public record FileResult(
        String       path,
        boolean      success,
        int          atomCount,
        int          vertexCount,
        int          edgeCount,
        double       resolutionRate,
        long         durationMs,
        List<String> warnings,
        List<String> errors
) {
    public FileResult {
        if (warnings == null) warnings = List.of();
        if (errors   == null) errors   = List.of();
    }
}
