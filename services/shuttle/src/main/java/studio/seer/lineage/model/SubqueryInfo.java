package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

/**
 * One descendant statement in the recursive CHILD_OF traversal from a root
 * DaliStatement. {@code parentStmtGeoid} points at the immediate parent
 * (nullable if backend couldn't resolve), letting the frontend rebuild the
 * nesting tree without a separate depth column.
 */
@Description("KNOT — one descendant statement under a root stmt's CHILD_OF tree")
public record SubqueryInfo(
    String rid,
    String stmtGeoid,
    String stmtType,
    String parentStmtGeoid
) {}
