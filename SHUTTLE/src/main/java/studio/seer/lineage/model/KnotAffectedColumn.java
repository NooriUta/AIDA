package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("KNOT — column affected (written to) by a statement via HAS_AFFECTED_COL")
public record KnotAffectedColumn(
    String stmtGeoid,
    String columnName,
    String tableName,
    int    position
) {}
