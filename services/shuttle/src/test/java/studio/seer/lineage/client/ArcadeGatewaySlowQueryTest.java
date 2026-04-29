package studio.seer.lineage.client;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import studio.seer.lineage.heimdall.HeimdallEmitter;
import studio.seer.lineage.heimdall.model.EventLevel;
import studio.seer.lineage.heimdall.model.EventType;

import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * EV-04: Unit tests for {@link ArcadeGateway#emitSlowIfNeeded}.
 *
 * Covers the threshold logic, payload content, query truncation, and
 * fire-and-forget safety guarantee (emitter exceptions must not propagate).
 *
 * The method is private so it is exercised via reflection — this keeps the
 * tests focused on the exact behaviour without introducing test-only visibility
 * on the production class.
 */
@QuarkusTest
class ArcadeGatewaySlowQueryTest {

    @InjectMock
    HeimdallEmitter heimdall;

    @Inject
    ArcadeGateway gateway;

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Reflectively invoke the private {@code emitSlowIfNeeded} method.
     */
    private void emitSlowIfNeeded(String database, String query,
                                   String language, long durationMs) throws Exception {
        Method m = ArcadeGateway.class.getDeclaredMethod(
                "emitSlowIfNeeded", String.class, String.class, String.class, long.class);
        m.setAccessible(true);
        m.invoke(gateway, database, query, language, durationMs);
    }

    // ── tests ─────────────────────────────────────────────────────────────────

    /**
     * EV-04-T1: duration strictly below 500 ms → heimdall.emit must never be called.
     */
    @Test
    void emitSlowIfNeeded_belowThreshold_doesNotEmit() throws Exception {
        emitSlowIfNeeded("hound_default", "SELECT FROM DaliTable", "sql", 499L);

        verify(heimdall, never()).emit(any(), any(), any(), any(), anyLong(), anyMap());
    }

    /**
     * EV-04-T2: duration exactly at the 500 ms threshold → emit called once
     * with event type CYPHER_QUERY_SLOW and level WARN.
     */
    @Test
    void emitSlowIfNeeded_atThreshold_emitsWarn() throws Exception {
        emitSlowIfNeeded("hound_default", "SELECT FROM DaliTable", "sql", 500L);

        verify(heimdall, times(1)).emit(
                eq(EventType.CYPHER_QUERY_SLOW),
                eq(EventLevel.WARN),
                isNull(),
                isNull(),
                eq(500L),
                anyMap());
    }

    /**
     * EV-04-T3: duration 1200 ms, short query → payload map contains the full
     * query text under the "query" key.
     */
    @SuppressWarnings("unchecked")
    @Test
    void emitSlowIfNeeded_aboveThreshold_includesQueryInPayload() throws Exception {
        String sql = "SELECT FROM DaliTable";
        emitSlowIfNeeded("hound_default", sql, "sql", 1200L);

        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(heimdall).emit(
                eq(EventType.CYPHER_QUERY_SLOW),
                eq(EventLevel.WARN),
                isNull(), isNull(),
                eq(1200L),
                captor.capture());

        Map<String, Object> payload = captor.getValue();
        assertEquals(sql, payload.get("query"), "Payload must contain the original query text");
    }

    /**
     * EV-04-T4: query length = 3000 chars → emitted payload "query" value is
     * truncated to MAX_QUERY_CHARS (2000) chars + "…" sentinel = 2001 chars total.
     */
    @SuppressWarnings("unchecked")
    @Test
    void emitSlowIfNeeded_longQuery_truncatesToMaxChars() throws Exception {
        String longQuery = "X".repeat(3000);
        emitSlowIfNeeded("hound_default", longQuery, "cypher", 600L);

        var captor = org.mockito.ArgumentCaptor.forClass(Map.class);
        verify(heimdall).emit(
                eq(EventType.CYPHER_QUERY_SLOW),
                eq(EventLevel.WARN),
                isNull(), isNull(),
                eq(600L),
                captor.capture());

        String emittedQuery = (String) captor.getValue().get("query");
        assertNotNull(emittedQuery, "query key must be present in payload");
        assertTrue(emittedQuery.length() <= ArcadeGateway.MAX_QUERY_CHARS + 1,
                "Truncated query must be at most MAX_QUERY_CHARS + 1 char (the ellipsis), got " + emittedQuery.length());
        assertTrue(emittedQuery.endsWith("…"),
                "Truncated query must end with the '…' sentinel");
    }

    /**
     * EV-04-T5: emitter throws a RuntimeException → exception must NOT propagate
     * to the caller (fire-and-forget guarantee).
     */
    @Test
    void emitSlowIfNeeded_emitterThrows_doesNotPropagate() throws Exception {
        doThrow(new RuntimeException("HEIMDALL down"))
                .when(heimdall).emit(any(), any(), any(), any(), anyLong(), anyMap());

        assertDoesNotThrow(() -> emitSlowIfNeeded(
                "hound_default", "SELECT FROM DaliTable", "sql", 999L),
                "emitSlowIfNeeded must never propagate emitter exceptions");
    }
}
