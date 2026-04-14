package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

/**
 * One source-table reference for a DaliStatement. Distinguishes "DIRECT"
 * reads — where the root statement itself has a READS_FROM edge to the table —
 * from "SUBQUERY" reads — where a descendant statement (via CHILD_OF*) reads
 * the table and the edge is conceptually hoisted to the root.
 *
 * <p>{@code viaStmtGeoid} is populated for SUBQUERY rows to show <em>which</em>
 * sub-statement actually performed the read, so the user can jump to it in
 * the descendants list.</p>
 *
 * <p>{@code schemaGeoid} lets the frontend render a badge when the table
 * lives outside the scope being explored — answers the cross-schema case
 * that prompted this feature (INSERT #25:8333 reads from BMRT.* while the
 * user is exploring BUDM_RMS).</p>
 */
@Description("KNOT — source table reference with direct/subquery indicator")
public record SourceTableRef(
    String rid,            // table @rid
    String tableGeoid,
    String tableName,
    String schemaGeoid,    // table's owning schema (may differ from stmt's)
    String sourceKind,     // "DIRECT" | "SUBQUERY"
    String viaStmtGeoid    // populated when sourceKind = "SUBQUERY"
) {}
