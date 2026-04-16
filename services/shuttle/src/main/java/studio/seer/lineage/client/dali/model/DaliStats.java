package studio.seer.lineage.client.dali.model;

/**
 * YGG graph statistics as returned by {@code GET /api/stats}.
 */
public record DaliStats(
        long daliTables,
        long daliColumns,
        long daliRoutines,
        long daliStatements,
        long daliAtoms,
        long lastUpdated
) {
    public static DaliStats empty() {
        return new DaliStats(0, 0, 0, 0, 0, 0);
    }
}
