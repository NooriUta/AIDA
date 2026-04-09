package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;
import java.util.List;

@Description("KNOT — output column of a SELECT statement")
public record KnotOutputColumn(
    String stmtGeoid,
    String name,
    String expression,
    String alias,
    int    colOrder,
    String sourceType,
    String tableRef,
    List<KnotOutputColumnAtom> atoms  // source atoms via ATOM_PRODUCES edge
) {}
