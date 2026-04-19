package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("KNOT — routine/statement that reads from or writes to a given table")
public record KnotTableUsage(
    String routineGeoid,
    String routineName,
    String edgeType,       // READS_FROM | WRITES_TO
    String stmtGeoid,
    String stmtType        // SELECT, INSERT, UPDATE, DELETE…
) {}
