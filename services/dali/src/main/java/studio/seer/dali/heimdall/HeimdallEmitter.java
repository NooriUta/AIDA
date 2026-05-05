package studio.seer.dali.heimdall;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.shared.EventLevel;
import studio.seer.shared.EventType;
import studio.seer.shared.HeimdallEvent;

import java.util.Map;

/**
 * Fire-and-forget HEIMDALL event emitter for Dali.
 *
 * <p>Three API layers — use the most specific one available:
 * <ol>
 *   <li>{@link #emit(HeimdallEvent)} — raw, full control</li>
 *   <li>{@link #info}/{@link #warn}/{@link #error} — level + type + payload</li>
 *   <li>Typed methods ({@link #sessionStarted}, {@link #atomExtracted}, ...) — no Map.of() boilerplate</li>
 * </ol>
 *
 * <p>All methods are non-blocking. Failures are logged at DEBUG and silently swallowed —
 * HEIMDALL observability must never affect Dali parse correctness.
 */
@ApplicationScoped
public class HeimdallEmitter {

    private static final Logger log = LoggerFactory.getLogger(HeimdallEmitter.class);

    @Inject
    @RestClient
    HeimdallClient client;

    // ── Layer 1: raw ──────────────────────────────────────────────────────────

    /** Send a pre-built event. Fire-and-forget — returns immediately. */
    public void emit(HeimdallEvent event) {
        client.ingest(event)
              .onFailure().invoke(e ->
                      log.debug("HeimdallEmitter: failed to send {} — {}", event.eventType(), e.getMessage()))
              .onFailure().recoverWithNull()
              .subscribe().with(__ -> {});
    }

    // ── Layer 2: generic ──────────────────────────────────────────────────────

    public void info(EventType type, String sessionId, Map<String, Object> payload) {
        emit(build("dali",  type, EventLevel.INFO,  sessionId, 0, payload));
    }

    public void warn(EventType type, String sessionId, Map<String, Object> payload) {
        emit(build("dali",  type, EventLevel.WARN,  sessionId, 0, payload));
    }

    public void error(EventType type, String sessionId, Map<String, Object> payload) {
        emit(build("dali",  type, EventLevel.ERROR, sessionId, 0, payload));
    }

    private void houndInfo(EventType type, String sessionId, Map<String, Object> payload) {
        emit(build("hound", type, EventLevel.INFO,  sessionId, 0, payload));
    }

    private void houndWarn(EventType type, String sessionId, Map<String, Object> payload) {
        emit(build("hound", type, EventLevel.WARN,  sessionId, 0, payload));
    }

    /**
     * Returns a new payload with {@code tenantAlias} added (no-op if tenant is null/blank or
     * key already present). HEIMDALL EventLog UI reads {@code payload.tenantAlias} to populate
     * the Tenant column — without it the column shows "—".
     */
    private static Map<String, Object> withTenant(Map<String, Object> payload, String tenantAlias) {
        if (tenantAlias == null || tenantAlias.isBlank()) return payload;
        Map<String, Object> enriched = new java.util.LinkedHashMap<>(payload);
        enriched.putIfAbsent("tenantAlias", tenantAlias);
        return enriched;
    }

    // ── Layer 3: typed Dali events ────────────────────────────────────────────

    /** Emitted by SessionService when a new session is accepted into the queue. */
    public void jobEnqueued(String sessionId, String tenantAlias, String source, String dialect) {
        info(EventType.JOB_ENQUEUED, sessionId, withTenant(Map.of(
                "source",  source,
                "dialect", dialect), tenantAlias));
    }

    /** Emitted by ParseJob when the job worker picks up the session. */
    public void sessionStarted(String sessionId, String tenantAlias, String source, String dialect,
                               boolean preview, boolean clearBeforeWrite, int threads) {
        info(EventType.SESSION_STARTED, sessionId, withTenant(Map.of(
                "source",           source,
                "dialect",          dialect,
                "preview",          preview,
                "clearBeforeWrite", clearBeforeWrite,
                "threads",          threads), tenantAlias));
    }

    /** Emitted by ParseJob on successful completion. */
    public void sessionCompleted(String sessionId, String tenantAlias, int atomCount,
                                 double resolutionRate, long durationMs, int files) {
        emit(build("dali", EventType.SESSION_COMPLETED, EventLevel.INFO, sessionId, durationMs, withTenant(Map.of(
                "atomCount",      atomCount,
                "resolutionRate", resolutionRate,
                "files",          files), tenantAlias)));
    }

    /** Emitted by SessionService when a session is cancelled by user request. */
    public void sessionCancelled(String sessionId, String tenantAlias) {
        warn(EventType.SESSION_FAILED, sessionId,
                withTenant(Map.of("reason", "CANCELLED_BY_USER"), tenantAlias));
    }

    /** Emitted by ParseJob when an unrecoverable error aborts the session. */
    public void sessionFailed(String sessionId, String tenantAlias, String error, long durationMs) {
        emit(build("dali", EventType.SESSION_FAILED, EventLevel.ERROR, sessionId, durationMs, withTenant(Map.of(
                "error", error != null ? error : "unknown"), tenantAlias)));
    }

    /** Emitted when Hound begins parsing a single SQL file. */
    public void fileParsingStarted(String sessionId, String tenantAlias, String file, String dialect) {
        houndInfo(EventType.FILE_PARSING_STARTED, sessionId, withTenant(Map.of(
                "file",    file,
                "dialect", dialect), tenantAlias));
    }

    /**
     * Emitted after a file parse completes — carries the atom count for that file.
     * HEIMDALL metrics service aggregates these to compute {@code atomsExtracted}.
     */
    public void atomExtracted(String sessionId, String tenantAlias, String file, int atomCount) {
        houndInfo(EventType.ATOM_EXTRACTED, sessionId, withTenant(Map.of(
                "file",      file,
                "atomCount", atomCount), tenantAlias));
    }

    /**
     * Emitted when ANTLR4 reports a hard syntax error inside a file
     * (no viable alternative, token recognition error, EOF mismatch).
     * Level is ERROR — name and level aligned: requires attention even though
     * session continues with partial parse.
     */
    public void parseError(String sessionId, String tenantAlias, String file, int line, int col, String msg) {
        emit(build("hound", EventType.PARSE_ERROR, EventLevel.ERROR, sessionId, 0, withTenant(Map.of(
                "file", file,
                "line", line,
                "col",  col,
                "msg",  msg != null ? msg : ""), tenantAlias)));
    }

    /**
     * Emitted when ANTLR4 reports a recoverable grammar limitation
     * (mismatched input, extraneous input, missing token).
     * Level is WARN — visible in logs but does not affect {@code isSuccess()}.
     */
    public void parseWarning(String sessionId, String tenantAlias, String file, int line, int col, String msg) {
        houndWarn(EventType.PARSE_WARNING, sessionId, withTenant(Map.of(
                "file", file,
                "line", line,
                "col",  col,
                "msg",  msg != null ? msg : ""), tenantAlias));
    }

    /**
     * Emitted for semantic-level warnings detected during the AST walk
     * (e.g. unresolved atoms, orphan scopes, suspicious table names).
     */
    public void semanticWarning(String sessionId, String tenantAlias, String file,
                                String category, String message) {
        houndWarn(EventType.SEMANTIC_WARNING, sessionId, withTenant(Map.of(
                "file",     file,
                "category", category,
                "msg",      message != null ? message : ""), tenantAlias));
    }

    /**
     * Emitted for semantic-level errors detected during the AST walk
     * (e.g. depth overflow, critical builder failures).
     */
    public void semanticError(String sessionId, String tenantAlias, String file,
                              String category, String message) {
        emit(build("hound", EventType.SEMANTIC_ERROR, EventLevel.ERROR, sessionId, 0, withTenant(Map.of(
                "file",     file,
                "category", category,
                "msg",      message != null ? message : ""), tenantAlias)));
    }

    /** Emitted when Hound encounters an error parsing a file. */
    public void fileParsingFailed(String sessionId, String tenantAlias, String file, String error) {
        emit(build("hound", EventType.FILE_PARSING_FAILED, EventLevel.ERROR, sessionId, 0, withTenant(Map.of(
                "file",  file,
                "error", error != null ? error : "unknown"), tenantAlias)));
    }

    // ── Layer 3: YGG write events (EV-02 / EV-03 / EV-05 / EV-06) ───────────

    /**
     * EV-02: Emitted when Hound successfully writes a session's graph to YGG.
     * Fired once per session after all files are written (non-preview only).
     */
    public void yggWriteCompleted(String sessionId, String tenantAlias, int vertices, int edges, long durationMs) {
        emit(build("hound", EventType.YGG_WRITE_COMPLETED, EventLevel.INFO, sessionId, durationMs,
                withTenant(Map.of("verticesWritten", vertices, "edgesWritten", edges), tenantAlias)));
    }

    /**
     * EV-02: Emitted when one or more YGG writes fail after all retries are exhausted.
     * In batch mode: fired if any file permanently failed (others may have succeeded).
     */
    public void yggWriteFailed(String sessionId, String tenantAlias, String error) {
        emit(build("hound", EventType.YGG_WRITE_FAILED, EventLevel.ERROR, sessionId, 0,
                withTenant(Map.of("error", error != null ? error : "unknown"), tenantAlias)));
    }

    /**
     * EV-03: Emitted after ParseJob truncates the lineage graph before a new parse
     * ({@code clearBeforeWrite=true}).
     */
    public void yggClearCompleted(String sessionId, String tenantAlias, long durationMs) {
        emit(build("dali", EventType.YGG_CLEAR_COMPLETED, EventLevel.INFO, sessionId, durationMs,
                withTenant(Map.of("durationMs", durationMs), tenantAlias)));
    }

    /**
     * EV-05: Emitted when ArcadeDB (YGG) is unreachable — connection refused / timed out.
     * Level ERROR: indicates infrastructure problem, not a parse issue.
     */
    public void dbConnectionError(String sessionId, String tenantAlias, String db, String error) {
        emit(build("dali", EventType.DB_CONNECTION_ERROR, EventLevel.ERROR, sessionId, 0,
                withTenant(Map.of("db", db != null ? db : "ygg",
                                  "error", error != null ? error : "unknown"), tenantAlias)));
    }

    /**
     * EV-06: Emitted when an admin creates a new JDBC harvest source.
     * {@code sourceId} may be blank for transient (pre-persist) creates — always non-null.
     */
    public void sourceCreated(String tenantAlias, String sourceId, String dialect) {
        emit(build("dali", EventType.SOURCE_CREATED, EventLevel.INFO, null, 0,
                Map.of("sourceId",    sourceId != null ? sourceId : "",
                       "dialect",     dialect  != null ? dialect  : "",
                       "tenantAlias", tenantAlias != null ? tenantAlias : "")));
    }

    /**
     * EV-06: Emitted when an admin deletes a JDBC harvest source.
     */
    public void sourceDeleted(String tenantAlias, String sourceId, String dialect) {
        emit(build("dali", EventType.SOURCE_DELETED, EventLevel.INFO, null, 0,
                Map.of("sourceId",    sourceId != null ? sourceId : "",
                       "dialect",     dialect  != null ? dialect  : "",
                       "tenantAlias", tenantAlias != null ? tenantAlias : "")));
    }

    // ── Internal builder ──────────────────────────────────────────────────────

    private static HeimdallEvent build(String sourceComponent, EventType type, EventLevel level,
                                       String sessionId, long durationMs,
                                       Map<String, Object> payload) {
        return new HeimdallEvent(
                System.currentTimeMillis(),
                sourceComponent,
                type.name(),
                level,
                sessionId,
                null,          // userId — not available in background job context
                null,          // correlationId
                durationMs,
                payload);
    }
}
