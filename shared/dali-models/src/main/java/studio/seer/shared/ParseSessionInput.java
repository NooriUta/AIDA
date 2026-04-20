package studio.seer.shared;

/**
 * Input parameters for a Dali parse session.
 *
 * <p>Two source types are supported:
 * <ol>
 *   <li><b>FILE</b> — {@code source} is a local path (file or directory). Fields
 *       {@code jdbcUser}, {@code jdbcPassword}, {@code jdbcSchema} are {@code null}.</li>
 *   <li><b>JDBC</b> — {@code source} is a JDBC URL (starts with {@code "jdbc:"}).
 *       Credentials are provided via {@code jdbcUser}, {@code jdbcPassword}, {@code jdbcSchema}.</li>
 * </ol>
 *
 * @param dialect          SQL dialect — "plsql" | "postgresql" | "clickhouse" | ...
 * @param source           Local path/directory (FILE) or JDBC URL (JDBC)
 * @param preview          If true, run parse without persisting to YGG (dry-run)
 * @param clearBeforeWrite If true, all Dali YGG data is truncated before this session writes
 * @param uploaded         If true, {@code source} is a temp dir created by the upload endpoint
 *                         and must be deleted after the job completes or fails
 * @param jdbcUser         JDBC username (JDBC source only; null for FILE)
 * @param jdbcPassword     JDBC password (JDBC source only; null for FILE)
 * @param jdbcSchema       Schema/owner to harvest (JDBC source only; null → adapter default)
 * @param dbName           Database display name — when non-null, Hound creates a DaliDatabase
 *                         vertex and attaches CONTAINS_SCHEMA edges so schemas are navigable
 *                         in the graph (fixes the "HoundDB" stub problem for ad-hoc uploads)
 * @param appName          Optional application name — when non-null, Hound also creates a
 *                         DaliApplication vertex linked via BELONGS_TO_APP
 */
public record ParseSessionInput(
        String  dialect,
        String  source,
        boolean preview,
        boolean clearBeforeWrite,
        boolean uploaded,
        String  jdbcUser,
        String  jdbcPassword,
        String  jdbcSchema,
        String  dbName,
        String  appName
) {
    /**
     * Backward-compatible constructor for FILE-based sources (no JDBC credentials, no db context).
     */
    public ParseSessionInput(String dialect, String source, boolean preview,
                             boolean clearBeforeWrite, boolean uploaded) {
        this(dialect, source, preview, clearBeforeWrite, uploaded, null, null, null, null, null);
    }
}
