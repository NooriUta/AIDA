package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("KNOT — statement that references a specific column")
public record KnotColumnUsage(
    String stmtGeoid,
    String stmtType,
    String routineName,
    String routineGeoid,
    String atomType       // COLUMN, OUTPUT_COL, …
) {}
