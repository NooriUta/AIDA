package studio.seer.lineage.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.lineage.client.dali.DaliClient;
import studio.seer.lineage.heimdall.HeimdallEmitter;
import studio.seer.lineage.heimdall.model.EventType;
import studio.seer.lineage.model.*;
import studio.seer.lineage.security.SeerIdentity;
import studio.seer.lineage.service.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LineageResource — main GraphQL API for SEER LOOM.
 *
 * Verifies:
 * - Each query method delegates to the correct service and returns the result
 * - heimdall.emit() called twice per query (REQUEST_RECEIVED + REQUEST_COMPLETED)
 * - aida:harvest scope triggers DaliClient and returns synthetic ExploreResult
 * - aida: unknown scope returns failure
 * - me() returns identity string
 *
 * SHT-P3D-5: LineageResource coverage
 */
@QuarkusTest
class LineageResourceTest {

    @InjectMock SeerIdentity    identity;
    @InjectMock OverviewService overviewService;
    @InjectMock ExploreService  exploreService;
    @InjectMock LineageService  lineageService;
    @InjectMock SearchService   searchService;
    @InjectMock HeimdallEmitter heimdall;
    @InjectMock @RestClient DaliClient daliClient;

    @Inject LineageResource resource;

    private static final ExploreResult EMPTY_RESULT =
            new ExploreResult(List.of(), List.of(), false);

    @BeforeEach
    void stubIdentity() {
        when(identity.tenantAlias()).thenReturn("acme");
        when(identity.username()).thenReturn("alice");
        when(identity.role()).thenReturn("viewer");
        reset(heimdall);
    }

    // ── me() ──────────────────────────────────────────────────────────────────

    @Test
    void me_returnsIdentityString() {
        String result = resource.me();
        assertEquals("alice (viewer)", result);
    }

    // ── overview ──────────────────────────────────────────────────────────────

    @Test
    void overview_delegatesAndReturnsNodes() {
        SchemaNode node = mock(SchemaNode.class);
        when(overviewService.overview()).thenReturn(Uni.createFrom().item(List.of(node)));

        List<SchemaNode> result = resource.overview().await().indefinitely();

        assertEquals(1, result.size());
        verify(overviewService).overview();
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    @Test
    void overview_emptyResult_emitsZeroSchemas() {
        when(overviewService.overview()).thenReturn(Uni.createFrom().item(List.of()));

        List<SchemaNode> result = resource.overview().await().indefinitely();

        assertTrue(result.isEmpty());
        verify(heimdall, atLeastOnce()).emit(eq(EventType.REQUEST_COMPLETED), any(), any(), any(), anyLong(),
                argThat(payload -> Integer.valueOf(0).equals(payload.get("schemaCount"))));
    }

    // ── exploreRoutineAggregate ───────────────────────────────────────────────

    @Test
    void exploreRoutineAggregate_delegatesWithScope() {
        when(exploreService.exploreRoutineAggregate("schema-DWH"))
                .thenReturn(Uni.createFrom().item(EMPTY_RESULT));

        ExploreResult result = resource.exploreRoutineAggregate("schema-DWH").await().indefinitely();

        assertSame(EMPTY_RESULT, result);
        verify(exploreService).exploreRoutineAggregate("schema-DWH");
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    // ── routineDetail ─────────────────────────────────────────────────────────

    @Test
    void routineDetail_delegatesWithNodeId() {
        when(exploreService.exploreRoutineDetail("#12:55"))
                .thenReturn(Uni.createFrom().item(EMPTY_RESULT));

        ExploreResult result = resource.routineDetail("#12:55").await().indefinitely();

        assertSame(EMPTY_RESULT, result);
        verify(exploreService).exploreRoutineDetail("#12:55");
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    // ── explore ───────────────────────────────────────────────────────────────

    @Test
    void explore_normalScope_delegatesToExploreService() {
        when(exploreService.explore("schema-PROD", false))
                .thenReturn(Uni.createFrom().item(EMPTY_RESULT));

        ExploreResult result = resource.explore("schema-PROD", false).await().indefinitely();

        assertSame(EMPTY_RESULT, result);
        verify(exploreService).explore("schema-PROD", false);
    }

    @Test
    void explore_includeExternal_passedThrough() {
        when(exploreService.explore("schema-PROD", true))
                .thenReturn(Uni.createFrom().item(EMPTY_RESULT));

        resource.explore("schema-PROD", true).await().indefinitely();

        verify(exploreService).explore("schema-PROD", true);
    }

    @Test
    void explore_aidaHarvestScope_callsDaliClient() {
        when(daliClient.startHarvest("acme"))
                .thenReturn(Map.of("harvestId", "h-42", "status", "enqueued"));

        ExploreResult result = resource.explore("aida:harvest", null).await().indefinitely();

        assertNotNull(result);
        assertEquals(1, result.nodes().size());
        assertEquals("HarvestSession", result.nodes().get(0).type());
        verify(daliClient).startHarvest("acme");
        // ExploreService must NOT be called for aida: scopes
        verifyNoInteractions(exploreService);
    }

    @Test
    void explore_aidaHarvestScope_daliClientFails_returnsSyntheticErrorNode() {
        when(daliClient.startHarvest(anyString()))
                .thenThrow(new RuntimeException("Dali down"));

        ExploreResult result = resource.explore("aida:harvest", null).await().indefinitely();

        assertNotNull(result);
        assertEquals(1, result.nodes().size());
        GraphNode node = result.nodes().get(0);
        assertEquals("harvest-error", node.id());
        assertTrue(node.meta().containsKey("error"));
    }

    @Test
    void explore_aidaUnknownScope_returnsFailure() {
        assertThrows(Exception.class,
                () -> resource.explore("aida:unknown-action", null).await().indefinitely());
    }

    @Test
    void explore_nullScope_delegatesToExploreService() {
        when(exploreService.explore(null, false))
                .thenReturn(Uni.createFrom().item(EMPTY_RESULT));

        assertDoesNotThrow(() -> resource.explore(null, null).await().indefinitely());
    }

    // ── exploreStatementTree ──────────────────────────────────────────────────

    @Test
    void exploreStatementTree_delegatesWithStmtId() {
        when(exploreService.exploreStatementTree("#15:100"))
                .thenReturn(Uni.createFrom().item(EMPTY_RESULT));

        ExploreResult result = resource.exploreStatementTree("#15:100").await().indefinitely();

        assertSame(EMPTY_RESULT, result);
        verify(exploreService).exploreStatementTree("#15:100");
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    // ── stmtColumns ───────────────────────────────────────────────────────────

    @Test
    void stmtColumns_delegatesWithIds() {
        List<String> ids = List.of("#15:1", "#15:2");
        when(exploreService.exploreStmtColumns(ids))
                .thenReturn(Uni.createFrom().item(EMPTY_RESULT));

        ExploreResult result = resource.stmtColumns(ids).await().indefinitely();

        assertSame(EMPTY_RESULT, result);
        verify(exploreService).exploreStmtColumns(ids);
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    @Test
    void stmtColumns_nullIds_noNpe() {
        when(exploreService.exploreStmtColumns(null))
                .thenReturn(Uni.createFrom().item(EMPTY_RESULT));

        assertDoesNotThrow(() -> resource.stmtColumns(null).await().indefinitely());
    }

    // ── lineage ───────────────────────────────────────────────────────────────

    @Test
    void lineage_delegatesWithNodeId() {
        when(lineageService.lineage("#5:10")).thenReturn(Uni.createFrom().item(EMPTY_RESULT));

        ExploreResult result = resource.lineage("#5:10").await().indefinitely();

        assertSame(EMPTY_RESULT, result);
        verify(lineageService).lineage("#5:10");
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    // ── upstream ──────────────────────────────────────────────────────────────

    @Test
    void upstream_delegatesWithNodeId() {
        when(lineageService.upstream("#5:20")).thenReturn(Uni.createFrom().item(EMPTY_RESULT));

        ExploreResult result = resource.upstream("#5:20").await().indefinitely();

        assertSame(EMPTY_RESULT, result);
        verify(lineageService).upstream("#5:20");
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    // ── downstream ────────────────────────────────────────────────────────────

    @Test
    void downstream_delegatesWithNodeId() {
        when(lineageService.downstream("#5:30")).thenReturn(Uni.createFrom().item(EMPTY_RESULT));

        ExploreResult result = resource.downstream("#5:30").await().indefinitely();

        assertSame(EMPTY_RESULT, result);
        verify(lineageService).downstream("#5:30");
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    // ── expandDeep ────────────────────────────────────────────────────────────

    @Test
    void expandDeep_delegatesWithDepth() {
        when(lineageService.expandDeep("#5:40", 3)).thenReturn(Uni.createFrom().item(EMPTY_RESULT));

        ExploreResult result = resource.expandDeep("#5:40", 3).await().indefinitely();

        assertSame(EMPTY_RESULT, result);
        verify(lineageService).expandDeep("#5:40", 3);
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    // ── search ────────────────────────────────────────────────────────────────

    @Test
    void search_delegatesWithQueryAndLimit() {
        SearchResult hit = mock(SearchResult.class);
        when(searchService.search("bonus", 20)).thenReturn(Uni.createFrom().item(List.of(hit)));

        List<SearchResult> result = resource.search("bonus", 20).await().indefinitely();

        assertEquals(1, result.size());
        verify(searchService).search("bonus", 20);
        verify(heimdall, times(2)).emit(any(EventType.class), any(), any(), any(), anyLong(), anyMap());
    }

    @Test
    void search_limitAbove100_cappedAt100() {
        when(searchService.search(eq("x"), eq(100)))
                .thenReturn(Uni.createFrom().item(List.of()));

        resource.search("x", 500).await().indefinitely();

        // limit must be capped at min(500, 100) = 100
        verify(searchService).search("x", 100);
    }

    @Test
    void search_nullQuery_handledGracefully() {
        when(searchService.search(isNull(), anyInt()))
                .thenReturn(Uni.createFrom().item(List.of()));

        assertDoesNotThrow(() -> resource.search(null, 10).await().indefinitely());
    }
}
