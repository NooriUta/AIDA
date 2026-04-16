package studio.seer.lineage.client.dali.model;

/**
 * HTTP request body for {@code POST /api/sessions} (Dali REST API).
 * Mapped from the GraphQL {@link studio.seer.lineage.heimdall.model.ParseSessionInput} input type.
 *
 * @param dialect          SQL dialect: "PLSQL" | "GENERIC_SQL" | "POSTGRESQL"
 * @param source           Path to file or directory on the Dali host
 * @param preview          true → parse only, no write to YGG; false → writes DaliAtom vertices
 * @param clearBeforeWrite null → Dali defaults to true (truncate YGG before writing)
 * @param filePattern      null → Dali defaults to "*.sql"
 * @param maxFiles         null → no limit
 */
public record DaliParseSessionInput(
        String  dialect,
        String  source,
        boolean preview,
        Boolean clearBeforeWrite,
        String  filePattern,
        Integer maxFiles
) {
    /** Convenience factory: minimal non-preview session (writes to YGG). */
    public static DaliParseSessionInput forSource(String dialect, String source) {
        return new DaliParseSessionInput(dialect, source, false, null, null, null);
    }
}
