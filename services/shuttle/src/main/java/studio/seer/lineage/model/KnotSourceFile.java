package studio.seer.lineage.model;

import org.eclipse.microprofile.graphql.Description;

/**
 * KNOT — full source file text from the source archive (hound_src_{tenant}).
 * Backed by DaliSourceFile. Differs from KnotScript (DaliSnippetScript in
 * lineage DB): this contains the complete, unprocessed original SQL document.
 */
@Description("KNOT — full source file from the source archive (hound_src_{tenant})")
public record KnotSourceFile(
    String sessionId,
    String filePath,
    String sqlText,
    long   sizeBytes,
    String sqlTextHash
) {}
