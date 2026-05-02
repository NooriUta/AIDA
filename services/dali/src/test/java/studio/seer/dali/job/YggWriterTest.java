package studio.seer.dali.job;

import com.hound.api.HoundConfig;
import com.hound.api.HoundEventListener;
import com.hound.api.HoundParser;
import com.hound.api.ParseResult;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import studio.seer.dali.heimdall.HeimdallEmitter;
import studio.seer.dali.service.SessionService;
import studio.seer.shared.ParseSessionInput;
import studio.seer.tenantrouting.ArcadeConnection;
import studio.seer.tenantrouting.YggLineageRegistry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * EV-08 deferred — 5-A: Verifies that {@link ParseJob} emits the correct
 * {@code YGG_WRITE_COMPLETED} and {@code YGG_WRITE_FAILED} Heimdall events.
 *
 * <p>Uses {@code @InjectMock} to replace live deps:
 * <ul>
 *   <li>{@link HeimdallEmitter} — prevents HTTP calls to Heimdall, allows verify()
 *   <li>{@link HoundParser} — returns controlled ParseResult without real YGG writes
 *   <li>{@link SessionService} — avoids live FRIGG for session-state updates
 *   <li>{@link YggLineageRegistry} — returns a stub resource (no network lookup)
 * </ul>
 */
@QuarkusTest
class YggWriterTest {

    @InjectMock HeimdallEmitter    emitter;
    @InjectMock HoundParser        houndParser;
    @InjectMock SessionService     sessionService;
    @InjectMock YggLineageRegistry yggLineageRegistry;

    @Inject ParseJob parseJob;

    // ── Temp file fixtures ────────────────────────────────────────────────────

    private static Path tempFile; // single SQL file
    private static Path tempDir;  // directory with 2 SQL files for batch tests

    @BeforeAll
    static void createFixtures() throws IOException {
        tempFile = Files.createTempFile("ygg-test-single-", ".sql");
        Files.writeString(tempFile, "-- test\nSELECT id FROM orders;");

        tempDir = Files.createTempDirectory("ygg-test-batch-");
        Files.writeString(tempDir.resolve("a.sql"), "SELECT id FROM orders;");
        Files.writeString(tempDir.resolve("b.sql"), "SELECT name FROM customers;");
    }

    @AfterAll
    static void deleteFixtures() throws IOException {
        if (tempFile != null) Files.deleteIfExists(tempFile);
        if (tempDir != null) {
            try (var stream = Files.walk(tempDir)) {
                stream.sorted(Comparator.reverseOrder())
                        .forEach(p -> { try { Files.deleteIfExists(p); } catch (IOException ignored) {} });
            }
        }
    }

    @BeforeEach
    void setupYggRegistry() {
        // YggLineageRegistry.resourceFor() just needs to return a non-null ArcadeConnection.
        // No network call is made here — HoundParser is mocked and ignores the config URL.
        ArcadeConnection stub = Mockito.mock(ArcadeConnection.class);
        Mockito.when(stub.databaseName()).thenReturn("hound_test");
        Mockito.when(yggLineageRegistry.resourceFor(anyString())).thenReturn(stub);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Non-preview single-file input pointing to tempFile. */
    private static ParseSessionInput singleInput(boolean preview) {
        return new ParseSessionInput(
                "plsql",
                tempFile.toAbsolutePath().toString().replace('\\', '/'),
                preview, false, false,
                null, null, null, null, null,
                preview ? null : "default");
    }

    /** Non-preview batch input pointing to tempDir. */
    private static ParseSessionInput batchInput() {
        return new ParseSessionInput(
                "plsql",
                tempDir.toAbsolutePath().toString().replace('\\', '/'),
                false, false, false,
                null, null, null, null, null, "default");
    }

    /** Successful ParseResult: 10 vertices, 3 edges. */
    private static ParseResult okResult(String file) {
        return new ParseResult(file, 5, 10, 3, 0, Map.of(), 1.0, 5, 0, List.of(), List.of(), 100L);
    }

    /**
     * ParseResult with non-empty errors.
     * {@code errors().isEmpty() == false} → {@link ParseJob#toFileResult} sets {@code success=false}
     * → in runBatch {@code failedCount > 0} → {@code yggWriteFailed} is emitted.
     * No retry is triggered (retries only fire on thrown exceptions, not on error-result returns).
     */
    private static ParseResult errorResult(String file) {
        return new ParseResult(file, 0, 0, 0, 0, Map.of(), 0.0, 0, 0,
                List.of(), List.of("PARSE_ERROR: unexpected token near ';'"), 50L);
    }

    // ── Tests ─────────────────────────────────────────────────────────────────

    /**
     * EV-02-T1: single-file, non-preview → {@code yggWriteCompleted} emitted once with
     * the vertex/edge counts from ParseResult; {@code yggWriteFailed} never emitted.
     */
    @Test
    @DisplayName("EV-02-T1: runSingle non-preview → yggWriteCompleted(sid, 10, 3, …) emitted")
    void runSingle_nonPreview_emitsYggWriteCompleted() throws Exception {
        String sid = "sid-single-ok-" + UUID.randomUUID();
        Mockito.when(houndParser.parse(any(Path.class), any(HoundConfig.class), any(HoundEventListener.class)))
               .thenReturn(okResult(tempFile.toString()));

        parseJob.execute(sid, singleInput(false));

        verify(emitter).yggWriteCompleted(eq(sid), any(), eq(10), eq(3), anyLong());
        verify(emitter, never()).yggWriteFailed(any(), any(), any());
    }

    /**
     * EV-02-T2: single-file, {@code preview=true} → {@code yggWriteCompleted} must NOT be
     * emitted (preview runs skip all YGG writes by design).
     */
    @Test
    @DisplayName("EV-02-T2: runSingle preview=true → yggWriteCompleted NOT emitted")
    void runSingle_preview_doesNotEmitYggWriteCompleted() throws Exception {
        String sid = "sid-single-preview-" + UUID.randomUUID();
        Mockito.when(houndParser.parse(any(Path.class), any(HoundConfig.class), any(HoundEventListener.class)))
               .thenReturn(okResult(tempFile.toString()));

        parseJob.execute(sid, singleInput(true));

        verify(emitter, never()).yggWriteCompleted(any(), any(), anyInt(), anyInt(), anyLong());
        verify(emitter, never()).yggWriteFailed(any(), any(), any());
    }

    /**
     * EV-02-T3: batch directory, all files succeed → {@code yggWriteCompleted} emitted
     * exactly once; {@code yggWriteFailed} never emitted.
     */
    @Test
    @DisplayName("EV-02-T3: runBatch all success → yggWriteCompleted×1, yggWriteFailed×0")
    void runBatch_allSuccess_emitsYggWriteCompleted() throws Exception {
        String sid = "sid-batch-ok-" + UUID.randomUUID();
        Mockito.when(houndParser.parse(any(Path.class), any(HoundConfig.class), any(HoundEventListener.class)))
               .thenReturn(okResult("any.sql"));

        parseJob.execute(sid, batchInput());

        verify(emitter, times(1)).yggWriteCompleted(eq(sid), any(), anyInt(), anyInt(), anyLong());
        verify(emitter, never()).yggWriteFailed(any(), any(), any());
    }

    /**
     * EV-02-T4: batch directory, all files return ParseResult with non-empty errors
     * → {@code success=false} → {@code failedCount > 0}
     * → both {@code yggWriteFailed} AND {@code yggWriteCompleted} are emitted
     * (yggWriteCompleted is always emitted after runBatch regardless of failures).
     *
     * <p>Using an error-returning mock (not a throwing mock) avoids the 6s retry delay
     * ({@code FILE_RETRY_BASE_MS=2000}): retries only fire on thrown exceptions.
     */
    @Test
    @DisplayName("EV-02-T4: runBatch file errors → yggWriteFailed + yggWriteCompleted both emitted")
    void runBatch_fileErrors_emitsBothFailedAndCompleted() throws Exception {
        String sid = "sid-batch-fail-" + UUID.randomUUID();
        Mockito.when(houndParser.parse(any(Path.class), any(HoundConfig.class), any(HoundEventListener.class)))
               .thenReturn(errorResult("test.sql"));

        parseJob.execute(sid, batchInput());

        verify(emitter).yggWriteFailed(eq(sid), any(), contains("file(s) failed"));
        verify(emitter).yggWriteCompleted(eq(sid), any(), anyInt(), anyInt(), anyLong());
    }
}
