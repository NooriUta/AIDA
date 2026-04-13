package studio.seer.shared;

/**
 * Per-vertex-type write statistics for a Dali parse session.
 *
 * @param type      ArcadeDB vertex class name (e.g. "DaliAtom", "DaliTable").
 * @param inserted  Vertices actually written to YGG (new inserts).
 * @param duplicate Vertices skipped because they already existed in YGG
 *                  (canonical pool hit, pre-existing schema/table/column, or in-batch dedup guard).
 */
public record VertexTypeStat(
        String type,
        int    inserted,
        int    duplicate
) {
    /** Total objects seen (inserted + duplicate). */
    public int total() { return inserted + duplicate; }
}
