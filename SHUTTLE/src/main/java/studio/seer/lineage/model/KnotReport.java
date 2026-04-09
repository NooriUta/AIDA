package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;
import java.util.List;

@Description("KNOT — full report for one session: summary + tables + routines + statements + snippets + atoms + output columns + affected columns + calls + params + vars")
public record KnotReport(
    KnotSession                  session,
    List<KnotTable>              tables,
    List<KnotRoutine>            routines,
    List<KnotStatement>          statements,
    List<KnotSnippet>            snippets,
    List<KnotAtom>               atoms,
    List<KnotOutputColumn>       outputColumns,
    List<KnotAffectedColumn>     affectedColumns,
    List<KnotCall>               calls,
    List<KnotParameter>          parameters,
    List<KnotVariable>           variables
) {}
