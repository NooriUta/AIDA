package com.hound.api;

import java.nio.file.Path;
import java.util.List;

/**
 * Public API for parsing SQL files and writing semantic results to YGG.
 *
 * <p>All implementations must be thread-safe — Dali calls {@link #parseBatch}
 * from a JobRunr worker thread.
 *
 * <p>Default implementation: {@code com.hound.HoundParserImpl}.
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
     * Parse multiple files in parallel (thread count from {@link HoundConfig#workerThreads()}).
     *
     * @param files  list of SQL files to parse
     * @param config typed Hound configuration
     * @return one {@link ParseResult} per file, in the same order as {@code files}
     */
    List<ParseResult> parseBatch(List<Path> files, HoundConfig config);

    /**
     * Parse multiple files in parallel, emitting events to {@code listener}.
     *
     * @param files    list of SQL files to parse
     * @param config   typed Hound configuration
     * @param listener event listener
     * @return one {@link ParseResult} per file, in the same order as {@code files}
     */
    List<ParseResult> parseBatch(List<Path> files, HoundConfig config, HoundEventListener listener);

    /**
     * Truncates all Dali vertex/edge types in YGG (TRUNCATE TYPE … UNSAFE).
     *
     * <p>Call this before the first {@link #parse} / {@link #parseBatch} call when you want
     * a clean slate (i.e. {@code clearBeforeWrite = true} in the UI). No-op when
     * {@code config.writeMode()} is {@link ArcadeWriteMode#DISABLED}.
     *
     * @param config must have a valid {@code arcadeUrl} if writeMode is REMOTE or REMOTE_BATCH
     */
    void cleanAll(HoundConfig config);
}
