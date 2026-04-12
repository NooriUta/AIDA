package com.hound.api;

/**
 * Controls how Hound writes semantic results to ArcadeDB.
 *
 * <p>Mode selection in practice:
 * <ul>
 *   <li>{@link #DISABLED}      — dry-run / preview / testing, no DB connection needed
 *   <li>{@link #REMOTE}        — single-record writes via HTTP REST (lower throughput)
 *   <li>{@link #REMOTE_BATCH}  — HTTP Batch endpoint, one POST per file (default for Dali UC1/UC2a)
 *   <li>{@link #EMBEDDED}      — in-process EmbeddedDatabase; disabled until CR1 resolved (Q31)
 * </ul>
 */
public enum ArcadeWriteMode {
    /** No storage — parse only. */
    DISABLED,
    /** ArcadeDB remote, single-record HTTP REST. */
    REMOTE,
    /** ArcadeDB remote, HTTP Batch endpoint (one POST per file). Default for Dali. */
    REMOTE_BATCH,
    /**
     * ArcadeDB embedded in-process.
     * <p><b>Currently disabled</b> — embedded API incompatible with ArcadeDB 26.x.
     * Will be restored when CR1 (Q31) is resolved.
     */
    EMBEDDED
}
