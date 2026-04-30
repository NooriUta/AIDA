package studio.seer.dali.job;

import com.hound.api.ParseResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.dali.heimdall.HeimdallEmitter;

import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DaliHoundListener}.
 *
 * <p>DaliHoundListener is not a CDI bean — it is constructed per-session by ParseJob.
 * Plain Mockito mocking is used; no Quarkus context needed.
 *
 * <p>Validates that each HoundEventListener callback delegates to the correct
 * {@link HeimdallEmitter} typed method. Fire-and-forget resilience is guaranteed
 * by {@link HeimdallEmitter} itself via Mutiny reactive (not by DaliHoundListener).
 */
class DaliHoundListenerTest {

    private HeimdallEmitter emitter;
    private DaliHoundListener listener;

    @BeforeEach
    void setUp() {
        emitter  = mock(HeimdallEmitter.class);
        listener = new DaliHoundListener("sess-001", "plsql", emitter);
    }

    // ── onFileParseStarted ────────────────────────────────────────────────────

    @Test
    void onFileParseStarted_delegatesToFileParsingStarted() {
        listener.onFileParseStarted("/sql/ORDERS.sql", "plsql");

        verify(emitter).fileParsingStarted("sess-001", "/sql/ORDERS.sql", "plsql");
        verifyNoMoreInteractions(emitter);
    }

    // ── onAtomExtracted ───────────────────────────────────────────────────────

    @Test
    void onAtomExtracted_doesNotEmitDirectly() {
        // DaliHoundListener.onAtomExtracted does NOT call emitter directly —
        // it defers to onFileParseCompleted which carries the total atom count.
        listener.onAtomExtracted("/sql/ORDERS.sql", 42, "DATA_FLOW");

        verifyNoInteractions(emitter);
    }

    // ── onFileParseCompleted ──────────────────────────────────────────────────

    @Test
    void onFileParseCompleted_delegatesToAtomExtracted() {
        ParseResult result = buildParseResult(120, 75, 890L);

        listener.onFileParseCompleted("/sql/PKG_ETL.sql", result);

        verify(emitter).atomExtracted("sess-001", "/sql/PKG_ETL.sql", 120);
        verifyNoMoreInteractions(emitter);
    }

    @Test
    void onFileParseCompleted_zeroAtoms_stillEmitsAtomExtracted() {
        ParseResult result = buildParseResult(0, 0, 12L);

        listener.onFileParseCompleted("/sql/empty.sql", result);

        verify(emitter).atomExtracted("sess-001", "/sql/empty.sql", 0);
    }

    // ── onParseError ──────────────────────────────────────────────────────────

    @Test
    void onParseError_delegatesToParseError_withFileNameOnly() {
        listener.onParseError("/path/to/ORDERS.sql", 42, 7, "mismatched input");

        // emitter receives only the filename, not the full path (Paths.get(...).getFileName())
        verify(emitter).parseError("sess-001", "ORDERS.sql", 42, 7, "mismatched input");
        verifyNoMoreInteractions(emitter);
    }

    @Test
    void onParseError_flatFileName_passedThrough() {
        listener.onParseError("PACKAGE.sql", 1, 0, "no viable alternative");

        verify(emitter).parseError("sess-001", "PACKAGE.sql", 1, 0, "no viable alternative");
    }

    // ── onParseWarning ────────────────────────────────────────────────────────

    @Test
    void onParseWarning_delegatesToParseWarning() {
        listener.onParseWarning("/data/PROCS.sql", 100, 3, "rule mismatch");

        verify(emitter).parseWarning("sess-001", "PROCS.sql", 100, 3, "rule mismatch");
        verifyNoMoreInteractions(emitter);
    }

    // ── onError ───────────────────────────────────────────────────────────────

    @Test
    void onError_delegatesToFileParsingFailed_withErrorMessage() {
        RuntimeException cause = new RuntimeException("stack overflow in grammar");

        listener.onError("/sql/HUGE.sql", cause);

        verify(emitter).fileParsingFailed("sess-001", "/sql/HUGE.sql", "stack overflow in grammar");
        verifyNoMoreInteractions(emitter);
    }

    @Test
    void onError_nullMessageException_passesNullToEmitter() {
        // NullPointerException has no message — emitter must receive null;
        // HeimdallEmitter.fileParsingFailed() null-guards it internally.
        NullPointerException npe = new NullPointerException();

        listener.onError("/sql/BUGGY.sql", npe);

        verify(emitter).fileParsingFailed("sess-001", "/sql/BUGGY.sql", null);
    }

    // ── helper ────────────────────────────────────────────────────────────────

    /** Build a minimal ParseResult with the given atom/vertex counts and duration. */
    private static ParseResult buildParseResult(int atomCount, int vertexCount, long durationMs) {
        return new ParseResult(
                "dummy.sql",
                atomCount, vertexCount,
                /* edgeCount */        0,
                /* droppedEdgeCount */ 0,
                /* vertexStats */      java.util.Map.of(),
                /* resolutionRate */   1.0,
                /* atomsResolved */    atomCount,
                /* atomsUnresolved */  0,
                /* warnings */         java.util.List.of(),
                /* errors */           java.util.List.of(),
                durationMs);
    }
}
