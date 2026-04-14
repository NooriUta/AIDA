package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

/**
 * DaliAtom count for one {@code parent_context} bucket (JOIN / SELECT / WHERE /
 * SUBQUERY / CTE / INSERT / UPDATE / VALUES / CURSOR / MERGE / ALTER_TABLE …)
 * scoped to a single DaliStatement. Rendered as rows in the KNOT Inspector
 * "Дополнительно" tab so the user can see at a glance how many filter /
 * projection / join atoms belong to the selected statement.
 */
@Description("KNOT — DaliAtom count grouped by parent_context for one stmt")
public record AtomContextCount(
    String context,
    int    count
) {}
