package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

/**
 * A directed edge in the lineage graph.
 *
 * <p>For column-level DATA_FLOW / FILTER_FLOW, {@code source} and {@code target}
 * still point at the parent DaliTable / DaliStatement rids (so React Flow
 * finds the mounted nodes), and {@code sourceHandle} / {@code targetHandle}
 * carry the specific column handle id (e.g. {@code "src-#25:7"} / {@code "tgt-#25:9"})
 * so the edge routes into the column row inside the parent card instead of
 * the node default handle. Both fields are nullable — table-level edges
 * (READS_FROM, WRITES_TO, plus the aggregated DATA_FLOW / FILTER_FLOW
 * fallbacks) leave them null and render to the node default handle.</p>
 */
@Description("A directed edge in the lineage graph")
public record GraphEdge(
    String id,
    String source,
    String target,
    String type,
    String sourceHandle,
    String targetHandle
) {
    /** Convenience constructor for edges without column-handle routing. */
    public GraphEdge(String id, String source, String target, String type) {
        this(id, source, target, type, null, null);
    }
}
