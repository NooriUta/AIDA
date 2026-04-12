package studio.seer.shared;

/**
 * Input parameters for a Dali parse session.
 *
 * @param dialect  SQL dialect — "plsql" | "postgresql" | "clickhouse" | ...
 * @param source   Path or identifier of the file/directory to parse
 * @param preview  If true, run parse without persisting to YGG (dry-run)
 */
public record ParseSessionInput(
        String  dialect,
        String  source,
        boolean preview
) {}
