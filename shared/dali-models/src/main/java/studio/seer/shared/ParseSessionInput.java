package studio.seer.shared;

/**
 * Input parameters for a Dali parse session.
 *
 * @param dialect          SQL dialect — "plsql" | "postgresql" | "clickhouse" | ...
 * @param source           Path or identifier of the file/directory to parse
 * @param preview          If true, run parse without persisting to YGG (dry-run)
 * @param clearBeforeWrite If true, all Dali YGG data is truncated before this session writes. Default: true.
 * @param uploaded         If true, {@code source} is a temp dir created by the upload endpoint
 *                         and must be deleted after the job completes or fails.
 */
public record ParseSessionInput(
        String  dialect,
        String  source,
        boolean preview,
        boolean clearBeforeWrite,
        boolean uploaded
) {}
