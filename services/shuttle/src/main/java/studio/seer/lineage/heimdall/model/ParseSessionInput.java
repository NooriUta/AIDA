package studio.seer.lineage.heimdall.model;

import org.eclipse.microprofile.graphql.Input;

/**
 * GraphQL input type for the {@code startParseSession} mutation.
 *
 * @param dialect          SQL dialect: "PLSQL" | "GENERIC_SQL" | "POSTGRESQL"
 * @param source           Path to file or directory (resolved on the Dali host)
 * @param preview          true → parse without writing to YGG (dry-run)
 *                         null → Dali defaults to false
 * @param clearBeforeWrite null → Dali defaults to true (truncate YGG before writing)
 * @param filePattern      Glob pattern, e.g. "*.sql".  null → "*.sql"
 * @param maxFiles         Maximum files to parse.  null → no limit
 */
@Input("ParseSessionInput")
public record ParseSessionInput(
        String  dialect,
        String  source,
        Boolean preview,
        Boolean clearBeforeWrite,
        String  filePattern,
        Integer maxFiles
) {}
