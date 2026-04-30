package studio.seer.lineage.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.ExploreResult;
import studio.seer.lineage.security.SeerIdentity;
import studio.seer.tenantrouting.ArcadeConnection;
import studio.seer.tenantrouting.YggLineageRegistry;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ExploreStatementService — L4 statement tree, columns, schema, db, package scopes.
 *
 * SHT-P3D-5
 */
@QuarkusTest
class ExploreStatementServiceTest {

    @InjectMock ArcadeGateway      arcade;
    @InjectMock SeerIdentity       identity;
    @InjectMock YggLineageRegistry lineageRegistry;

    @Inject ExploreStatementService service;

    private final ArcadeConnection conn = mock(ArcadeConnection.class);

    @BeforeEach
    void setUp() {
        when(identity.tenantAlias()).thenReturn("acme");
        when(lineageRegistry.resourceFor("acme")).thenReturn(conn);
        when(conn.databaseName()).thenReturn("hound_acme");
        // Default: all cypher queries return empty rows (graceful, no DB needed)
        when(arcade.cypherIn(anyString(), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));
    }

    // ── exploreStatementTree ──────────────────────────────────────────────────

    @Test
    void exploreStatementTree_nullId_returnsEmptyResult() {
        ExploreResult result = service.exploreStatementTree(null).await().indefinitely();
        assertNotNull(result);
        assertTrue(result.nodes().isEmpty());
        assertTrue(result.edges().isEmpty());
    }

    @Test
    void exploreStatementTree_blankId_returnsEmptyResult() {
        ExploreResult result = service.exploreStatementTree("  ").await().indefinitely();
        assertNotNull(result);
        assertTrue(result.nodes().isEmpty());
    }

    @Test
    void exploreStatementTree_validId_callsArcadeAndReturnsResult() {
        ExploreResult result = service.exploreStatementTree("1234").await().indefinitely();
        assertNotNull(result);
        // arcade.cypherIn called for root, child, reads, outCol, dataFlow queries
        verify(arcade, atLeast(3)).cypherIn(eq("hound_acme"), anyString(), anyMap());
    }

    @Test
    void exploreStatementTree_validId_withTableNodes_enrichesDataSource() {
        // Return a DaliTable node row so enrichDataSource is exercised
        var tableRow = Map.<String, Object>of(
                "srcId", "10", "srcLabel", "MY_TABLE", "srcType", "DaliTable",
                "tgtId", "10", "tgtLabel", "MY_TABLE", "tgtScope", "MYSCHEMA",
                "tgtType", "DaliTable", "edgeType", "NODE_ONLY",
                "sourceHandle", "", "targetHandle", "");
        var dsRow = Map.<String, Object>of("id", "10", "ds", "oracle");

        // First calls (5 queries) return table row; enrichment call returns ds
        when(arcade.cypherIn(eq("hound_acme"), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(tableRow)))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of(dsRow))); // enrichDataSource query

        ExploreResult result = service.exploreStatementTree("1234").await().indefinitely();
        assertNotNull(result);
        // At least one node should be present from the table row
        assertFalse(result.nodes().isEmpty());
    }

    // ── exploreStmtColumns ────────────────────────────────────────────────────

    @Test
    void exploreStmtColumns_nullInput_returnsEmptyResult() {
        ExploreResult result = service.exploreStmtColumns(null).await().indefinitely();
        assertNotNull(result);
        assertTrue(result.nodes().isEmpty());
    }

    @Test
    void exploreStmtColumns_emptyInput_returnsEmptyResult() {
        ExploreResult result = service.exploreStmtColumns(List.of()).await().indefinitely();
        assertNotNull(result);
        assertTrue(result.nodes().isEmpty());
        verifyNoInteractions(arcade);
    }

    @Test
    void exploreStmtColumns_validIds_callsArcadeAndReturnsResult() {
        ExploreResult result = service.exploreStmtColumns(List.of("tbl-1", "stmt-2")).await().indefinitely();
        assertNotNull(result);
        // count, hasCol, hasOutCol, hasAffCol queries
        verify(arcade, atLeast(2)).cypherIn(eq("hound_acme"), anyString(), anyMap());
    }

    // ── exploreSchema ─────────────────────────────────────────────────────────

    @Test
    void exploreSchema_twoArg_callsArcadeAndReturnsResult() {
        ExploreResult result = service.exploreSchema("DWH.CORE", "DWH").await().indefinitely();
        assertNotNull(result);
        verify(arcade, atLeast(1)).cypherIn(eq("hound_acme"), anyString(), anyMap());
    }

    @Test
    void exploreSchema_withExternalFalse_callsArcadeAndReturnsResult() {
        ExploreResult result = service.exploreSchema("DWH.CORE", "", false).await().indefinitely();
        assertNotNull(result);
    }

    @Test
    void exploreSchema_withExternalTrue_callsArcadeAndReturnsResult() {
        ExploreResult result = service.exploreSchema("DWH.CORE", "", true).await().indefinitely();
        assertNotNull(result);
        verify(arcade, atLeast(1)).cypherIn(eq("hound_acme"), anyString(), anyMap());
    }

    @Test
    void exploreSchema_nullDbName_treatedAsEmpty() {
        ExploreResult result = service.exploreSchema("DWH.CORE", null, false).await().indefinitely();
        assertNotNull(result);
    }

    // ── exploreByDatabase ─────────────────────────────────────────────────────

    @Test
    void exploreByDatabase_callsArcadeAndReturnsResult() {
        ExploreResult result = service.exploreByDatabase("DWH").await().indefinitely();
        assertNotNull(result);
        verify(arcade).cypherIn(eq("hound_acme"), anyString(), eq(Map.of("dbName", "DWH")));
    }

    @Test
    void exploreByDatabase_withResults_buildsNodes() {
        var row = Map.<String, Object>of(
                "srcId", "s1", "srcLabel", "CORE", "srcType", "DaliSchema",
                "tgtId", "t1", "tgtLabel", "ORDERS", "tgtScope", "CORE",
                "tgtType", "DaliTable", "edgeType", "CONTAINS_TABLE",
                "sourceHandle", "", "targetHandle", "");
        when(arcade.cypherIn(eq("hound_acme"), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(row)));

        ExploreResult result = service.exploreByDatabase("DWH").await().indefinitely();
        assertFalse(result.nodes().isEmpty());
        assertEquals("CORE", result.nodes().get(0).label());
    }

    // ── explorePackage ────────────────────────────────────────────────────────

    @Test
    void explorePackage_callsArcadeAndReturnsResult() {
        ExploreResult result = service.explorePackage("MY_PKG").await().indefinitely();
        assertNotNull(result);
        // base query + package records queries (recNodeQ, recFieldQ, returnsIntoQ)
        verify(arcade, atLeast(2)).cypherIn(eq("hound_acme"), anyString(), anyMap());
    }

    @Test
    void explorePackage_withNodes_mergesBaseAndRecords() {
        var routineRow = Map.<String, Object>of(
                "srcId", "r1", "srcLabel", "MY_PKG", "srcType", "DaliPackage",
                "tgtId", "r2", "tgtLabel", "GET_EMP", "tgtScope", "",
                "tgtType", "DaliRoutine", "edgeType", "CONTAINS_ROUTINE",
                "sourceHandle", "", "targetHandle", "");
        // base returns routineRow; everything else returns empty
        when(arcade.cypherIn(eq("hound_acme"), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(routineRow)))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of()));

        ExploreResult result = service.explorePackage("MY_PKG").await().indefinitely();
        assertFalse(result.nodes().isEmpty());
    }
}
