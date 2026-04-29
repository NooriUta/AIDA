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

    // ── Layer 3: typed Dali events ────────────────────────────────────────────

    /** Emitted by SessionService when a new session is accepted into the queue. */
    public void jobEnqueued(String sessionId, String source, String dialect) {
        info(EventType.JOB_ENQUEUED, sessionId, Map.of(
                "source",  source,
                "dialect", dialect));
    }

    /** Emitted by ParseJob when the job worker picks up the session. */
    public void sessionStarted(String sessionId, String source, String dialect,
                               boolean preview, boolean clearBeforeWrite, int threads) {
        info(EventType.SESSION_STARTED, sessionId, Map.of(
                "source",           source,
                "dialect",          dialect,
                "preview",          preview,
                "clearBeforeWrite", clearBeforeWrite,
                "threads",          threads));
    }

    /** Emitted by ParseJob on successful completion. */
    public void sessionCompleted(String sessionId, int atomCount,
                                 double resolutionRate, long durationMs, int files) {
        emit(build("dali", EventType.SESSION_COMPLETED, EventLevel.INFO, sessionId, durationMs, Map.of(
                "atomCount",      atomCount,
                "resolutionRate", resolutionRate,
                "files",          files)));
    }

    /** Emitted by SessionService when a session is cancelled by user request. */
    public void sessionCancelled(String sessionId) {
        warn(EventType.SESSION_FAILED, sessionId,
                Map.of("reason", "CANCELLED_BY_USER"));
    }

    /** Emitted by ParseJob when an unrecoverable error aborts the session. */
    public void sessionFailed(String sessionId, String error, long durationMs) {
        emit(build("dali", EventType.SESSION_FAILED, EventLevel.ERROR, sessionId, durationMs, Map.of(
                "error", error != null ? error : "unknown")));
    }

    /** Emitted when Hound begins parsing a single SQL file. */
    public void fileParsingStarted(String sessionId, String file, String dialect) {
        houndInfo(EventType.FILE_PARSING_STARTED, sessionId, Map.of(
                "file",    file,
                "dialect", dialect));
    }

    /**
     * Emitted after a file parse completes — carries the atom count for that file.
     * HEIMDALL metrics service aggregates these to compute {@code atomsExtracted}.
     */
    public void atomExtracted(String sessionId, String file, int atomCount) {
        houndInfo(EventType.ATOM_EXTRACTED, sessionId, Map.of(
                "file",      file,
                "atomCount", atomCount));
    }

    /**
     * Emitted when ANTLR4 reports a genuine syntax error inside a file.
     * Level is WARN — the file is still (partially) parsed; the session continues.
     */
    public void parseError(String sessionId, String file, int line, int col, String msg) {
        houndWarn(EventType.PARSE_ERROR, sessionId, Map.of(
                "file", file,
                "line", line,
                "col",  col,
                "msg",  msg != null ? msg : ""));
    }

    /**
     * Emitted when ANTLR4 reports a known grammar limitation (not a bug in the source file).
     * Level is INFO — informational only; does not affect {@code isSuccess()} or the ✗ indicator.
     */
    public void parseWarning(String sessionId, String file, int line, int col, String msg) {
        houndInfo(EventType.PARSE_WARNING, sessionId, Map.of(
                "file", file,
                "line", line,
                "col",  col,
                "msg",  msg != null ? msg : ""));
    }

    /** Emitted when Hound encounters an error parsing a file. */
    public void fileParsingFailed(String sessionId, String file, String error) {
        emit(build("hound", EventType.FILE_PARSING_FAILED, EventLevel.ERROR, sessionId, 0, Map.of(
                "file",  file,
                "error", error != null ? error : "unknown")));
    }

    // ── Layer 3: YGG write events (EV-02 / EV-03 / EV-05 / EV-06) ───────────

    /**
     * EV-02: Emitted when Hound successfully writes a session's graph to YGG.
     * Fired once per session after all files are written (non-preview only).
     */
    public void yggWriteCompleted(String sessionId, int vertices, int edges, long durationMs) {
        emit(build("hound", EventType.YGG_WRITE_COMPLETED, EventLevel.INFO, sessionId, durationMs,
                Map.of("verticesWritten", vertices, "edgesWritten", edges)));
    }

    /**
     * EV-02: Emitted when one or more YGG writes fail after all retries are exhausted.
     * In batch mode: fired if any file permanently failed (others may have succeeded).
     */
    public void yggWriteFailed(String sessionId, String error) {
        emit(build("hound", EventType.YGG_WRITE_FAILED, EventLevel.ERROR, sessionId, 0,
                Map.of("error", error != null ? error : "unknown")));
    }

    /**
     * EV-03: Emitted after ParseJob truncates the lineage graph before a new parse
     * ({@code clearBeforeWrite=true}).
     */
    public void yggClearCompleted(String sessionId, long durationMs) {
        emit(build("dali", EventType.YGG_CLEAR_COMPLETED, EventLevel.INFO, sessionId, durationMs,
                Map.of("durationMs", durationMs)));
    }

    /**
     * EV-05: Emitted when ArcadeDB (YGG) is unreachable — connection refused / timed out.
     * Level ERROR: indicates infrastructure problem, not a parse issue.
     */
    public void dbConnectionError(String sessionId, String db, String error) {
        emit(build("dali", EventType.DB_CONNECTION_ERROR, EventLevel.ERROR, sessionId, 0,
                Map.of("db", db != null ? db : "ygg",
                       "error", error != null ? error : "unknown")));
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
