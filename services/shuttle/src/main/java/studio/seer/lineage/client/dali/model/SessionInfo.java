package studio.seer.lineage.client.dali.model;

import java.util.List;

/**
 * Dali parse session as returned by {@code GET /api/sessions/{id}} and
 * {@code POST /api/sessions}. Also exposed as a GraphQL output type by
 * {@link studio.seer.lineage.resource.MutationResource#startParseSession}.
 */
public record SessionInfo(
        String       id,
        String       status,          // QUEUED | RUNNING | COMPLETED | FAILED | CANCELLED | UNAVAILABLE
        String       dialect,
        String       source,
        boolean      preview,
        String       startedAt,       // ISO-8601 Instant string from Dali
        String       updatedAt,
        Integer      atomCount,       // null until COMPLETED
        Integer      vertexCount,
        Integer      edgeCount,
        Double       resolutionRate,  // null until COMPLETED
        Long         durationMs,      // null until COMPLETED
        Integer      progress,
        Integer      total,
        Boolean      friggPersisted,
        List<String> errors
) {
    /** Returned by the fallback when Dali is unreachable. */
    public static SessionInfo unavailable() {
        return new SessionInfo(
                null, "UNAVAILABLE", null, null, false,
                null, null, null, null, null, null, null,
                null, null, null,
                List.of("Dali service unavailable"));
    }

    /** True when the session has reached a terminal state. */
    public boolean isDone() {
        return "COMPLETED".equals(status)
                || "FAILED".equals(status)
                || "CANCELLED".equals(status);
    }
}
