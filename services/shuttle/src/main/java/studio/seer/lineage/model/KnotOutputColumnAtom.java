package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("KNOT — source atom linked to an output column via ATOM_PRODUCES edge")
public record KnotOutputColumnAtom(
    String text,    // atom_text (may contain ~line:pos suffix)
    String col,     // column_name — referenced column
    String tbl,     // table_name / source alias
    String status   // RESOLVED | UNRESOLVED | CONSTANT | FUNCTION_CALL (ADR-HND-002 primary_status)
) {}
