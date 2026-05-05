package studio.seer.lineage.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.lineage.heimdall.HeimdallEmitter;
import studio.seer.lineage.heimdall.model.EventType;
import studio.seer.lineage.model.*;
import studio.seer.lineage.service.KnotService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for KnotResource — GraphQL KNOT analytics API.
 *
 * Verifies:
 * - Each query method delegates to KnotService and returns the result
 * - heimdall.emit() is called twice per query (REQUEST_RECEIVED + REQUEST_COMPLETED)
 *
 * SHT-P3D-3: KnotResource coverage
 */
@QuarkusTest
class KnotResourceTest {

    @InjectMock KnotService     knotService;
    @InjectMock HeimdallEmitter heimdall;

    @Inject KnotResource resource;

    @BeforeEach
    void resetMocks() {
        reset(heimdall);
    }

    // ── knotSessions ──────────────────────────────────────────────────────────

    @Test
    void knotSessions_delegatesAndReturnsResult() {
        KnotSession session = mock(KnotSession.class);
        when(knotService.knotSessions()).thenReturn(Uni.createFrom().item(List.of(session)));

        List<KnotSession> result = resource.knotSessions().await().indefinitely();

        assertEquals(1, result.size());
        verify(knotService).knotSessions();
        // REQUEST_RECEIVED + REQUEST_COMPLETED
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    @Test
    void knotSessions_emptySessions_returnsEmpty() {
        when(knotService.knotSessions()).thenReturn(Uni.createFrom().item(List.of()));

        List<KnotSession> result = resource.knotSessions().await().indefinitely();

        assertTrue(result.isEmpty());
    }

    // ── knotReport ────────────────────────────────────────────────────────────

    @Test
    void knotReport_delegatesWithSessionId() {
        KnotReport report = mock(KnotReport.class);
        when(knotService.knotReport("sess-1", null)).thenReturn(Uni.createFrom().item(report));

        KnotReport result = resource.knotReport("sess-1", "").await().indefinitely();

        assertSame(report, result);
        verify(knotService).knotReport("sess-1", null);
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    @Test
    void knotReport_nullSessionId_noNpe() {
        when(knotService.knotReport(null, null)).thenReturn(Uni.createFrom().nullItem());

        // Should not throw even with null sessionId
        assertDoesNotThrow(() -> resource.knotReport(null, "").await().indefinitely());
    }

    // ── knotSnippet ───────────────────────────────────────────────────────────

    @Test
    void knotSnippet_delegatesWithStmtGeoid() {
        when(knotService.knotSnippet("geoid-x")).thenReturn(Uni.createFrom().item("SELECT 1 FROM dual"));

        String snippet = resource.knotSnippet("geoid-x").await().indefinitely();

        assertEquals("SELECT 1 FROM dual", snippet);
        verify(knotService).knotSnippet("geoid-x");
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    // ── knotStatementExtras ───────────────────────────────────────────────────

    @Test
    void knotStatementExtras_delegatesAndReturns() {
        StatementExtras extras = mock(StatementExtras.class);
        when(extras.descendants()).thenReturn(List.of());
        when(extras.totalAtomCount()).thenReturn(0);
        when(knotService.knotStatementExtras("stmt-geoid")).thenReturn(Uni.createFrom().item(extras));

        StatementExtras result = resource.knotStatementExtras("stmt-geoid").await().indefinitely();

        assertSame(extras, result);
        verify(knotService).knotStatementExtras("stmt-geoid");
    }

    // ── knotTableDetail ───────────────────────────────────────────────────────

    @Test
    void knotTableDetail_delegatesWithSessionAndTable() {
        KnotTableDetail detail = mock(KnotTableDetail.class);
        when(detail.columns()).thenReturn(List.of());
        when(knotService.knotTableDetail("sess-1", "tbl-geoid"))
                .thenReturn(Uni.createFrom().item(detail));

        KnotTableDetail result = resource.knotTableDetail("sess-1", "tbl-geoid").await().indefinitely();

        assertSame(detail, result);
        verify(knotService).knotTableDetail("sess-1", "tbl-geoid");
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    // ── knotTableRoutines ─────────────────────────────────────────────────────

    @Test
    void knotTableRoutines_delegatesAndReturnsList() {
        KnotTableUsage usage = mock(KnotTableUsage.class);
        when(knotService.knotTableRoutines("#16:4"))
                .thenReturn(Uni.createFrom().item(List.of(usage, usage)));

        List<KnotTableUsage> result = resource.knotTableRoutines("#16:4").await().indefinitely();

        assertEquals(2, result.size());
        verify(knotService).knotTableRoutines("#16:4");
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    // ── knotColumnStatements ──────────────────────────────────────────────────

    @Test
    void knotColumnStatements_delegatesWithColumnGeoid() {
        when(knotService.knotColumnStatements("DWH.SALES.ID"))
                .thenReturn(Uni.createFrom().item(List.of()));

        List<KnotColumnUsage> result = resource.knotColumnStatements("DWH.SALES.ID").await().indefinitely();

        assertTrue(result.isEmpty());
        verify(knotService).knotColumnStatements("DWH.SALES.ID");
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    // ── knotScript ────────────────────────────────────────────────────────────

    @Test
    void knotScript_delegatesAndReturnsScript() {
        KnotScript script = mock(KnotScript.class);
        when(script.lineCount()).thenReturn(42);
        when(script.charCount()).thenReturn(1200);
        when(knotService.knotScript("sess-abc")).thenReturn(Uni.createFrom().item(script));

        KnotScript result = resource.knotScript("sess-abc").await().indefinitely();

        assertSame(script, result);
        verify(knotService).knotScript("sess-abc");
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    @Test
    void knotScript_nullResult_emitsCompletedWithNullHandling() {
        when(knotService.knotScript("sess-xyz")).thenReturn(Uni.createFrom().nullItem());

        // null result should not cause NPE in invoke() lambda
        assertDoesNotThrow(() -> resource.knotScript("sess-xyz").await().indefinitely());
    }

    // ── knotSourceFile ────────────────────────────────────────────────────────

    @Test
    void knotSourceFile_delegatesAndReturnsFile() {
        KnotSourceFile file = mock(KnotSourceFile.class);
        when(file.sizeBytes()).thenReturn(8192L);
        when(knotService.knotSourceFile("sess-def")).thenReturn(Uni.createFrom().item(file));

        KnotSourceFile result = resource.knotSourceFile("sess-def").await().indefinitely();

        assertSame(file, result);
        verify(knotService).knotSourceFile("sess-def");
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    @Test
    void knotSourceFile_nullResult_noNpe() {
        when(knotService.knotSourceFile("missing")).thenReturn(Uni.createFrom().nullItem());

        assertDoesNotThrow(() -> resource.knotSourceFile("missing").await().indefinitely());
    }
}
