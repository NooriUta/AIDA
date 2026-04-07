package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("KNOT — output column of a SELECT statement")
public record KnotOutputColumn(
    String stmtGeoid,
    String name,
    String expression,
    String alias,
    int    colOrder,
    String sourceType,
    String tableRef
) {}
