package com.hound.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Typed configuration for a Hound parse session.
 *
 * <p>Q27 CLOSED (13.04.2026): this record is the canonical field list.
 *
 * <p>Mapping from legacy {@code RunConfig}:
 * <ul>
 *   <li>{@code language}            → {@link #dialect}
 *   <li>{@code dbType + useBatch}   → {@link #writeMode}
 *   <li>{@code dbHost + dbPort}     → {@link #arcadeUrl}
 *   <li>{@code dbName}              → {@link #arcadeDbName}
 *   <li>{@code dbUser}              → {@link #arcadeUser}
 *   <li>{@code dbPassword}          → {@link #arcadePassword}
 *   <li>{@code appName}             → {@link #targetSchema}
 *   <li>{@code threads}             → {@link #workerThreads}
 *   <li>{@code clean, diag, inputPath} → NOT here (operational flags, not config)
 * </ul>
 */
public record HoundConfig(
        /** SQL dialect: "plsql" | "postgresql" | "clickhouse" | ... */
        String dialect,

        /** Namespace isolation in YGG — maps to DaliApplication vertex name. */
        String targetSchema,

        /** How to persist semantic results. Default: {@link ArcadeWriteMode#DISABLED}. */
        ArcadeWriteMode writeMode,

        /** ArcadeDB base URL, e.g. {@code "http://localhost:2480"}. Null for DISABLED/EMBEDDED. */
        String arcadeUrl,

        /** ArcadeDB database name. Default: {@code "hound"}. */
        String arcadeDbName,

        /** ArcadeDB user. Default: {@code "root"}. */
        String arcadeUser,

        /** ArcadeDB password. Default: {@code "playwithdata"}. */
        String arcadePassword,

        /** Parallel worker threads. Default: available CPU cores. */
        int workerThreads,

        /**
         * If {@code true}, unresolved column references cause a hard error.
         * If {@code false} (default), soft-fail — warnings only.
         */
        boolean strictResolution,

        /** HTTP batch size for {@link ArcadeWriteMode#REMOTE_BATCH}. Default: 5000. */
        int batchSize,

        /** Extensibility slot — dialect-specific or future options without breaking change. */
        Map<String, String> extra
) {

    // ─── Compact constructor: defensive copy of extra ─────────────

    public HoundConfig {
        if (extra == null) extra = Collections.emptyMap();
        else extra = Collections.unmodifiableMap(new HashMap<>(extra));
    }

    // ─── Factory methods ──────────────────────────────────────────

    /**
     * Parse-only mode: no DB writes, uses all CPU cores.
     *
     * <pre>{@code
     * HoundConfig cfg = HoundConfig.defaultDisabled("plsql");
     * }</pre>
     */
    public static HoundConfig defaultDisabled(String dialect) {
        return new HoundConfig(
                dialect, null, ArcadeWriteMode.DISABLED,
                null, "hound", "root", "playwithdata",
                Runtime.getRuntime().availableProcessors(),
                false, 5000, null
        );
    }

    /**
     * REMOTE_BATCH mode — default for Dali UC1/UC2a production runs.
     *
     * @param dialect  SQL dialect, e.g. {@code "plsql"}
     * @param arcadeUrl base URL, e.g. {@code "http://localhost:2480"}
     */
    public static HoundConfig defaultRemoteBatch(String dialect, String arcadeUrl) {
        return new HoundConfig(
                dialect, null, ArcadeWriteMode.REMOTE_BATCH,
                arcadeUrl, "hound", "root", "playwithdata",
                Runtime.getRuntime().availableProcessors(),
                false, 5000, null
        );
    }

    /**
     * REMOTE (single-record) mode.
     *
     * @param dialect  SQL dialect
     * @param arcadeUrl base URL, e.g. {@code "http://localhost:2480"}
     */
    public static HoundConfig defaultRemote(String dialect, String arcadeUrl) {
        return new HoundConfig(
                dialect, null, ArcadeWriteMode.REMOTE,
                arcadeUrl, "hound", "root", "playwithdata",
                Runtime.getRuntime().availableProcessors(),
                false, 5000, null
        );
    }

    // ─── Convenience builder for overrides ───────────────────────

    /** Return a copy with a different targetSchema. */
    public HoundConfig withTargetSchema(String schema) {
        return new HoundConfig(dialect, schema, writeMode, arcadeUrl,
                arcadeDbName, arcadeUser, arcadePassword,
                workerThreads, strictResolution, batchSize, extra);
    }

    /** Return a copy with different ArcadeDB credentials. */
    public HoundConfig withCredentials(String dbName, String user, String password) {
        return new HoundConfig(dialect, targetSchema, writeMode, arcadeUrl,
                dbName, user, password,
                workerThreads, strictResolution, batchSize, extra);
    }

    /** Return a copy with a different thread count. */
    public HoundConfig withWorkerThreads(int threads) {
        return new HoundConfig(dialect, targetSchema, writeMode, arcadeUrl,
                arcadeDbName, arcadeUser, arcadePassword,
                threads, strictResolution, batchSize, extra);
    }

    /**
     * Return a copy with an additional key/value in the {@link #extra} map.
     * Use {@code "hound.session.id"} to propagate a Dali session UUID into Ygg
     * so that {@code DaliSession.session_id} matches the Dali job UUID.
     */
    public HoundConfig withExtra(String key, String value) {
        Map<String, String> merged = new HashMap<>(extra);
        merged.put(key, value);
        return new HoundConfig(dialect, targetSchema, writeMode, arcadeUrl,
                arcadeDbName, arcadeUser, arcadePassword,
                workerThreads, strictResolution, batchSize, merged);
    }
}
