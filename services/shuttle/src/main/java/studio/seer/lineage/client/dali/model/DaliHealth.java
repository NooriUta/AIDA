package studio.seer.lineage.client.dali.model;

/**
 * Dali health check response from {@code GET /api/sessions/health}.
 */
public record DaliHealth(
        String frigg,
        String ygg,
        int    sessions,
        int    workers,
        int    queued
) {
    public static DaliHealth degraded(String reason) {
        return new DaliHealth("error", "unknown", 0, 0, 0);
    }
}
