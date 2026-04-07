package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

@Description("KNOT — SQL snippet for a statement (from DaliSnippet)")
public record KnotSnippet(
    String stmtGeoid,
    String snippet
) {}
