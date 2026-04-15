package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

/**
 * KNOT — full source file for a parsed session.
 * Backed by DaliSnippetScript (one document per Hound parse run).
 * script may be several MB for large PL/SQL packages (30k–50k lines);
 * it is returned as-is and paginated on the frontend.
 */
@Description("KNOT — full source file text for a parsed session (from DaliSnippetScript)")
public record KnotScript(
    String filePath,
    String script,
    int    lineCount,
    int    charCount
) {}
