package studio.seer.lineage.heimdall.model;

import org.eclipse.microprofile.graphql.Input;

/**
 * GraphQL input type for startParseSession mutation.
 * path  — source file or directory to parse
 * dbType — dialect hint (e.g. "oracle", "postgres")
 */
@Input("ParseSessionInput")
public record ParseSessionInput(String path, String dbType) {}
