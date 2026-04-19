package com.hound.api;

import java.nio.file.Path;

/**
 * Source of SQL text for a Hound parse operation.
 *
 * <p>Two variants:
 * <ul>
 *   <li>{@link FromFile} — path on local disk (legacy: Dali upload / directory flows)</li>
 *   <li>{@link FromText} — in-memory SQL text (SKADI/ULLR harvest — zero disk I/O)</li>
 * </ul>
 *
 * <p>Usage in SKADI flow:
 * <pre>{@code
 * List<SqlSource> sources = fetchResult.files().stream()
 *         .map(f -> new SqlSource.FromText(f.sqlText(), f.suggestedFilename()))
 *         .toList();
 * houndParser.parseSources(sources, config, listener);
 * }</pre>
 *
 * @since SI-03
 */
public sealed interface SqlSource permits SqlSource.FromFile, SqlSource.FromText {

    /** Logical name of this source — shown in parse results, logs, and HEIMDALL events. */
    String sourceName();

    /**
     * SQL sourced from a file on local disk.
     *
     * <p>Used by the legacy Dali upload / directory parse flow.
     *
     * @param path absolute path to the SQL file
     */
    record FromFile(Path path) implements SqlSource {
        @Override
        public String sourceName() { return path.getFileName().toString(); }
    }

    /**
     * SQL text provided in-memory (no disk I/O).
     *
     * <p>Used by {@link studio.seer.dali.skadi.SourceArchiveService} to forward SKADI harvest
     * results directly to HoundParser without writing temporary files to disk.
     *
     * @param sql        complete SQL DDL text (e.g. CREATE OR REPLACE FUNCTION ...)
     * @param sourceName logical name used as file identifier in results
     *                   (e.g. {@code "public__fn_add.function.sql"})
     */
    record FromText(String sql, String sourceName) implements SqlSource {}
}
