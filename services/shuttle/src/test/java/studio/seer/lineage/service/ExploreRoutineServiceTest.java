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
 * Tests for ExploreRoutineService — routine aggregate (L2), scope (L3), detail inspector.
 *
 * SHT-P3D-6
 */
@QuarkusTest
class ExploreRoutineServiceTest {

    @InjectMock ArcadeGateway      arcade;
    @InjectMock SeerIdentity       identity;
    @InjectMock YggLineageRegistry lineageRegistry;

    @Inject ExploreRoutineService service;

    private final ArcadeConnection conn = mock(ArcadeConnection.class);

    @BeforeEach
    void setUp() {
        when(identity.tenantAlias()).thenReturn("acme");
        when(lineageRegistry.resourceFor("acme")).thenReturn(conn);
        when(conn.databaseName()).thenReturn("hound_acme");
        when(arcade.cypherIn(anyString(), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));
    }

    // ── exploreRoutineAggregate — package scope ────────────────────────────────

    @Test
    void exploreRoutineAggregate_packageScope_callsPackageCypher() {
        ExploreResult result = service.exploreRoutineAggregate("pkg-MY_PKG").await().indefinitely();
        assertNotNull(result);
        // main, ext (empty for pkg), calls queries + enrichDataSource
        verify(arcade, atLeast(2)).cypherIn(eq("hound_acme"), anyString(), anyMap());
    }

    @Test
    void exploreRoutineAggregate_packageScope_withNodes_buildsGraph() {
        // Return rows with a package+routine for the isPackage branch
        var row = Map.ofEntries(
                Map.entry("pkgId", (Object) "p1"), Map.entry("pkgName", "MY_PKG"),
                Map.entry("pkgSchema", "DWH"),
                Map.entry("src", "r1"), Map.entry("srcLabel", "GET_EMP"),
                Map.entry("srcSchema", "DWH"), Map.entry("srcPackage", "MY_PKG"),
                Map.entry("srcKind", "FUNCTION"),
                Map.entry("reads", List.of()), Map.entry("writes", List.of()));
        var callRow = Map.<String, Object>of(
                "srcId", "r1", "srcLabel", "GET_EMP", "srcType", "DaliRoutine",
                "srcScope", "DWH", "srcPackage", "MY_PKG", "srcKind", "FUNCTION",
                "tgtId", "r2", "tgtLabel", "OTHER_FN", "tgtType", "DaliRoutine");

        when(arcade.cypherIn(eq("hound_acme"), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(row)))   // main
                .thenReturn(Uni.createFrom().item(List.of()))       // ext (pkg → empty)
                .thenReturn(Uni.createFrom().item(List.of(callRow))) // calls
                .thenReturn(Uni.createFrom().item(List.of()));       // enrichDataSource

        ExploreResult result = service.exploreRoutineAggregate("pkg-MY_PKG").await().indefinitely();
        assertFalse(result.nodes().isEmpty());
    }

    @Test
    void exploreRoutineAggregate_schemaScope_callsSchemaCypher() {
        ExploreResult result = service.exploreRoutineAggregate("schema-DWH.CORE").await().indefinitely();
        assertNotNull(result);
        // main, ext (schema fires extQuery), calls + enrichDataSource
        verify(arcade, atLeast(3)).cypherIn(eq("hound_acme"), anyString(), anyMap());
    }

    @Test
    void exploreRoutineAggregate_schemaScope_withReadsAndWrites() {
        // A row with reads/writes lists — exercises makeRoutineTableRow
        var tableMap = Map.<String, Object>of(
                "@rid", "t1", "table_name", "ORDERS", "schema_geoid", "DWH");
        var row = Map.<String, Object>of(
                "src", "r1", "srcLabel", "CALC_TOTAL", "srcSchema", "DWH",
                "srcPackage", "", "srcKind", "FUNCTION",
                "reads", List.of(tableMap),
                "writes", List.of(tableMap));

        when(arcade.cypherIn(eq("hound_acme"), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(row)))  // main
                .thenReturn(Uni.createFrom().item(List.of()))      // ext
                .thenReturn(Uni.createFrom().item(List.of()))      // calls
                .thenReturn(Uni.createFrom().item(List.of()));     // enrichDataSource

        ExploreResult result = service.exploreRoutineAggregate("schema-DWH.CORE").await().indefinitely();
        // Should have the DaliTable edges from reads/writes
        assertFalse(result.edges().isEmpty());
    }

    @Test
    void exploreRoutineAggregate_schemaScope_extQueryFailure_gracefullyIgnored() {
        // ext query fails → should recover and still return result
        when(arcade.cypherIn(eq("hound_acme"), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()))                                  // main
                .thenReturn(Uni.createFrom().failure(new RuntimeException("ArcadeDB down")))  // ext
                .thenReturn(Uni.createFrom().item(List.of()))                                  // calls
                .thenReturn(Uni.createFrom().item(List.of()));                                 // enrichDataSource

        ExploreResult result = service.exploreRoutineAggregate("schema-DWH.CORE").await().indefinitely();
        assertNotNull(result); // no exception propagated
    }

    // ── exploreRoutineScope ───────────────────────────────────────────────────

    @Test
    void exploreRoutineScope_emptyRows_returnsEmptyResult() {
        ExploreResult result = service.exploreRoutineScope("r-12345").await().indefinitely();
        assertNotNull(result);
        assertTrue(result.nodes().isEmpty());
        // 9 queries + enrichDataSource
        verify(arcade, atLeast(5)).cypherIn(eq("hound_acme"), anyString(), anyMap());
    }

    @Test
    void exploreRoutineScope_withStatementRows_buildsNodes() {
        var stmtRow = Map.<String, Object>of(
                "srcId", "s1", "srcLabel", "SELECT 1", "srcType", "DaliStatement",
                "tgtId", "s1", "tgtLabel", "SELECT 1", "tgtScope", "",
                "tgtType", "DaliStatement", "edgeType", "NODE_ONLY",
                "sourceHandle", "", "targetHandle", "");

        // First query returns the statement; rest return empty
        when(arcade.cypherIn(eq("hound_acme"), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(stmtRow)))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of())); // enrichDataSource

        ExploreResult result = service.exploreRoutineScope("r-12345").await().indefinitely();
        assertFalse(result.nodes().isEmpty());
    }

    // ── exploreRoutineDetail ──────────────────────────────────────────────────

    @Test
    void exploreRoutineDetail_emptyRows_returnsEmptyResult() {
        ExploreResult result = service.exploreRoutineDetail("r-999").await().indefinitely();
        assertNotNull(result);
        assertTrue(result.nodes().isEmpty());
        // 5 queries (params, vars, stmts, callsOut, callsIn) — no enrichDataSource
        verify(arcade, atLeast(5)).cypherIn(eq("hound_acme"), anyString(), anyMap());
    }

    @Test
    void exploreRoutineDetail_withParameterRows_buildsNodes() {
        var paramRow = Map.ofEntries(
                Map.entry("srcId", (Object) "rtn1"), Map.entry("srcLabel", "MY_FUNC"),
                Map.entry("srcType", "DaliRoutine"), Map.entry("srcScope", ""),
                Map.entry("srcPackage", "MY_PKG"), Map.entry("srcKind", "FUNCTION"),
                Map.entry("tgtId", "p1"), Map.entry("tgtLabel", "IN_VAL"),
                Map.entry("tgtScope", ""), Map.entry("tgtType", "DaliParameter"),
                Map.entry("edgeType", "HAS_PARAMETER"),
                Map.entry("sourceHandle", ""), Map.entry("targetHandle", ""),
                Map.entry("tgtDataType", "NUMBER"));

        when(arcade.cypherIn(eq("hound_acme"), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(paramRow)))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of()))
                .thenReturn(Uni.createFrom().item(List.of()));

        ExploreResult result = service.exploreRoutineDetail("rtn1").await().indefinitely();
        assertFalse(result.nodes().isEmpty());
        assertEquals("MY_FUNC", result.nodes().get(0).label());
    }

    @Test
    void exploreRoutineDetail_arcadeFailures_recoversGracefully() {
        when(arcade.cypherIn(eq("hound_acme"), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().failure(new RuntimeException("DB error")));

        // All 5 queries have onFailure().recoverWithItem(List.of()) — no exception expected
        ExploreResult result = service.exploreRoutineDetail("r-err").await().indefinitely();
        assertNotNull(result);
    }
}
