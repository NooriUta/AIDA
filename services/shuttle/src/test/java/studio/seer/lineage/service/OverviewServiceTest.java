package studio.seer.lineage.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.SchemaNode;
import studio.seer.lineage.security.SeerIdentity;
import studio.seer.tenantrouting.ArcadeConnection;
import studio.seer.tenantrouting.YggLineageRegistry;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OverviewService — L1 aggregated schema overview.
 *
 * SHT-P3D-2: OverviewService coverage
 */
@QuarkusTest
class OverviewServiceTest {

    @InjectMock ArcadeGateway      arcade;
    @InjectMock SeerIdentity       identity;
    @InjectMock YggLineageRegistry lineageRegistry;

    @Inject OverviewService service;

    @BeforeEach
    void stubDefaults() {
        when(identity.tenantAlias()).thenReturn("acme");
        ArcadeConnection conn = mock(ArcadeConnection.class);
        when(conn.databaseName()).thenReturn("hound_acme");
        when(lineageRegistry.resourceFor("acme")).thenReturn(conn);
    }

    // ── overview() ─────────────────────────────────────────────────────────────

    @Test
    void overview_returnsAllMappedNodes() {
        when(arcade.sqlIn(eq("hound_acme"), contains("DaliSchema"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        Map.of("rid", "#5:1", "schema_name", "DWH",
                               "tableCount", 12, "routineCount", 3, "packageCount", 5,
                               "databaseGeoid", "db-geoid-1", "databaseName", "DWH_DB",
                               "applicationGeoid", "app-geoid-1", "applicationName", "Analytics"),
                        Map.of("rid", "#5:2", "schema_name", "STAGE",
                               "tableCount", 7, "routineCount", 0, "packageCount", 2,
                               "databaseGeoid", "db-geoid-2", "databaseName", "STAGE_DB",
                               "applicationGeoid", "", "applicationName", "")
                )));

        List<SchemaNode> nodes = service.overview().await().indefinitely();

        assertEquals(2, nodes.size());

        SchemaNode first = nodes.get(0);
        assertEquals("#5:1",       first.id());
        assertEquals("DWH",        first.name());
        assertEquals(12,           first.tableCount());
        assertEquals(3,            first.routineCount());
        assertEquals(5,            first.packageCount());
        assertEquals("db-geoid-1", first.databaseGeoid());
        assertEquals("DWH_DB",     first.databaseName());
        assertEquals("app-geoid-1",first.applicationGeoid());
        assertEquals("Analytics",  first.applicationName());

        SchemaNode second = nodes.get(1);
        assertEquals("STAGE",      second.name());
        // empty string applicationGeoid → null (strOrNull)
        assertNull(second.applicationGeoid());
        assertNull(second.applicationName());
    }

    @Test
    void overview_emptyDatabase_returnsEmptyList() {
        when(arcade.sqlIn(eq("hound_acme"), contains("DaliSchema"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));

        List<SchemaNode> nodes = service.overview().await().indefinitely();

        assertTrue(nodes.isEmpty());
    }

    @Test
    void overview_routesToTenantDatabase() {
        when(arcade.sqlIn(anyString(), contains("DaliSchema"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));

        service.overview().await().indefinitely();

        // Must query the tenant-specific DB, not a hardcoded one
        verify(arcade).sqlIn(eq("hound_acme"), anyString(), anyMap());
    }

    @Test
    void overview_rowWithNullOptionalFields_noNpe() {
        // Row where databaseGeoid and applicationGeoid are absent
        when(arcade.sqlIn(anyString(), contains("DaliSchema"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        Map.of("rid", "#5:3", "schema_name", "ORPHAN",
                               "tableCount", 1, "routineCount", 0, "packageCount", 0)
                )));

        List<SchemaNode> nodes = service.overview().await().indefinitely();

        assertEquals(1, nodes.size());
        SchemaNode orphan = nodes.get(0);
        assertEquals("ORPHAN", orphan.name());
        assertNull(orphan.databaseGeoid());
        assertNull(orphan.databaseName());
        assertNull(orphan.applicationGeoid());
        assertNull(orphan.applicationName());
    }

    // ── Static helpers ─────────────────────────────────────────────────────────

    @Test
    void str_presentKey_returnsToString() {
        Map<String, Object> row = Map.of("k", 42);
        assertEquals("42", OverviewService.str(row, "k"));
    }

    @Test
    void str_missingKey_returnsEmptyString() {
        assertEquals("", OverviewService.str(Map.of(), "missing"));
    }

    @Test
    void strOrNull_presentNonEmpty_returnsValue() {
        Map<String, Object> row = Map.of("k", "hello");
        assertEquals("hello", OverviewService.strOrNull(row, "k"));
    }

    @Test
    void strOrNull_emptyString_returnsNull() {
        Map<String, Object> row = Map.of("k", "   ");
        assertNull(OverviewService.strOrNull(row, "k"));
    }

    @Test
    void strOrNull_missingKey_returnsNull() {
        assertNull(OverviewService.strOrNull(Map.of(), "missing"));
    }

    @Test
    void num_integerValue_returnsIntValue() {
        Map<String, Object> row = Map.of("cnt", 7L);
        assertEquals(7, OverviewService.num(row, "cnt"));
    }

    @Test
    void num_missingKey_returnsZero() {
        assertEquals(0, OverviewService.num(Map.of(), "missing"));
    }

    @Test
    void num_nonNumericValue_returnsZero() {
        Map<String, Object> row = Map.of("cnt", "not-a-number");
        assertEquals(0, OverviewService.num(row, "cnt"));
    }
}
