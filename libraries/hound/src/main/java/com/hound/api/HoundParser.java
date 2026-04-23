package com.hound.api;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Public API for parsing SQL files and writing semantic results to YGG.
 *
 * <p>All implementations must be thread-safe — Dali calls {@link #parseSources}
 * (or the deprecated {@link #parseBatch}) from a JobRunr worker thread.
 *
 * <p>Default implementation: {@code com.hound.HoundParserImpl}.
 *
 * <p><b>SKADI / in-memory flow:</b> use {@link #parseSources(List, HoundConfig, HoundEventListener)}
 * with {@link SqlSource.FromText} to avoid writing temporary files to disk.
 */
public interface HoundParser {

    /**
     * Parse a single SQL file with the given config.
     *
     * @param file   path to the SQL file
     * @param config typed Hound configuration
     * @return parse result with stats and any warnings/errors
     */
    ParseResult parse(Path file, HoundConfig config);

    /**
     * Parse a single SQL file, emitting events to {@code listener}.
     *
     * @param file     path to the SQL file
     * @param config   typed Hound configuration
     * @param listener event listener; use {@link NoOpHoundEventListener#INSTANCE} if not needed
     * @return parse result
     */
    ParseResult parse(Path file, HoundConfig config, HoundEventListener listener);

    /**
     * Parse a single SQL file with database context (creates DaliDatabase + CONTAINS_SCHEMA).
     *
     * <p>When {@code dbName} is non-null the implementation calls
     * {@code writer.ensureCanonicalPool(dbName, appName, appName)} so that every
     * DaliSchema produced from this file is linked to a named DaliDatabase vertex.
     * This is the preferred overload for Dali FILE-upload sessions where the user
     * explicitly names the target database.
     *
     * @param file     path to the SQL file
     * @param config   typed Hound configuration
     * @param dbName   database name for CONTAINS_SCHEMA grouping; null = ad-hoc mode
     * @param appName  optional application name for BELONGS_TO_APP grouping; null = skip
     * @param listener event listener; use {@link NoOpHoundEventListener#INSTANCE} if not needed
     * @return parse result
     */
    ParseResult parse(Path file, HoundConfig config, String dbName, String appName,
                      HoundEventListener listener);

    /**
     * Parse multiple SQL sources in parallel (preferred API since SI-03).
     *
     * <p>Supports both on-disk ({@link SqlSource.FromFile}) and in-memory
     * ({@link SqlSource.FromText}) sources. Use {@code FromText} for SKADI harvests
     * to avoid writing temporary files.
     *
     * <p>Thread count is taken from {@link HoundConfig#workerThreads()}.
     *
     * @param sources  list of SQL sources to parse; may mix FromFile and FromText
     * @param config   typed Hound configuration
     * @param listener event listener; use {@link NoOpHoundEventListener#INSTANCE} if not needed
     * @return one {@link ParseResult} per source, in the same order as {@code sources}
     */
    List<ParseResult> parseSources(List<SqlSource> sources, HoundConfig config, HoundEventListener listener);

    /**
     * Parse multiple files in parallel (thread count from {@link HoundConfig#workerThreads()}).
     *
     * @param files  list of SQL files to parse
     * @param config typed Hound configuration
     * @return one {@link ParseResult} per file, in the same order as {@code files}
     * @deprecated Prefer {@link #parseSources(List, HoundConfig, HoundEventListener)} with
     *             {@link SqlSource.FromFile} — avoids unnecessary Path→SqlSource conversion.
     */
    @Deprecated
    List<ParseResult> parseBatch(List<Path> files, HoundConfig config);

    /**
     * Parse multiple files in parallel, emitting events to {@code listener}.
     *
     * @param files    list of SQL files to parse
     * @param config   typed Hound configuration
     * @param listener event listener
     * @return one {@link ParseResult} per file, in the same order as {@code files}
     * @deprecated Prefer {@link #parseSources(List, HoundConfig, HoundEventListener)}.
     */
    @Deprecated
    List<ParseResult> parseBatch(List<Path> files, HoundConfig config, HoundEventListener listener);

    /**
     * Truncates all Dali vertex/edge types in YGG (TRUNCATE TYPE … UNSAFE).
     *
     * <p>Call this before the first {@link #parse} / {@link #parseSources} call when you want
     * a clean slate (i.e. {@code clearBeforeWrite = true} in the UI). No-op when
     * {@code config.writeMode()} is {@link ArcadeWriteMode#DISABLED}.
     *
     * @param config must have a valid {@code arcadeUrl} if writeMode is REMOTE or REMOTE_BATCH
     */
    void cleanAll(HoundConfig config);
}
