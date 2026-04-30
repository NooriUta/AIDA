package studio.seer.lineage.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.DaliApplicationDto;
import studio.seer.lineage.model.DaliDatabaseDto;
import studio.seer.lineage.security.SeerIdentity;
import studio.seer.tenantrouting.ArcadeConnection;
import studio.seer.tenantrouting.YggLineageRegistry;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ApplicationsService — App → DB → Schema hierarchy builder.
 *
 * All tests exercise the buildHierarchy() logic through the public
 * fetchApplicationHierarchy() method with mocked ArcadeGateway.
 *
 * SHT-P3D-6: ApplicationsService coverage
 */
@QuarkusTest
class ApplicationsServiceTest {

    @InjectMock ArcadeGateway      arcade;
    @InjectMock SeerIdentity       identity;
    @InjectMock YggLineageRegistry lineageRegistry;

    @Inject ApplicationsService service;

    @BeforeEach
    void stubDefaults() {
        when(identity.tenantAlias()).thenReturn("acme");
        ArcadeConnection conn = mock(ArcadeConnection.class);
        when(conn.databaseName()).thenReturn("hound_acme");
        when(lineageRegistry.resourceFor("acme")).thenReturn(conn);
    }

    // ── Empty DB ───────────────────────────────────────────────────────────────

    @Test
    void emptyDatabase_returnsEmptyList() {
        when(arcade.sqlIn(eq("hound_acme"), contains("DaliSchema"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));

        List<DaliApplicationDto> result = service.fetchApplicationHierarchy().await().indefinitely();

        assertTrue(result.isEmpty());
    }

    // ── Full hierarchy: App → DB → Schema ─────────────────────────────────────

    @Test
    void fullHierarchy_singleAppWithTwoDbsAndSchemas() {
        when(arcade.sqlIn(eq("hound_acme"), contains("DaliSchema"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        schemaRow("sc1", "DWH_STAGE",    "geoid-s1", 10, 2, 1,
                                  "db1", "DWH",    "geoid-db1", "app1", "DataHub", "geoid-app1"),
                        schemaRow("sc2", "DWH_PROD",     "geoid-s2",  5, 0, 0,
                                  "db1", "DWH",    "geoid-db1", "app1", "DataHub", "geoid-app1"),
                        schemaRow("sc3", "REPORTING",    "geoid-s3",  3, 1, 0,
                                  "db2", "REPORT", "geoid-db2", "app1", "DataHub", "geoid-app1")
                )));

        List<DaliApplicationDto> apps = service.fetchApplicationHierarchy().await().indefinitely();

        assertEquals(1, apps.size(), "Should have exactly one named application");
        DaliApplicationDto app = apps.get(0);
        assertEquals("DataHub", app.name());
        assertEquals("app1",    app.id());
        assertEquals(2, app.databases().size(), "App should have 2 databases");

        // DWH database has 2 schemas
        DaliDatabaseDto dwh = app.databases().stream()
                .filter(d -> "DWH".equals(d.dbName())).findFirst().orElseThrow();
        assertEquals(2, dwh.schemas().size());

        // REPORT database has 1 schema
        DaliDatabaseDto report = app.databases().stream()
                .filter(d -> "REPORT".equals(d.dbName())).findFirst().orElseThrow();
        assertEquals(1, report.schemas().size());
        assertEquals("REPORTING", report.schemas().get(0).schemaName());
    }

    // ── Multiple apps ──────────────────────────────────────────────────────────

    @Test
    void multipleApps_eachGetsOwnEntry() {
        when(arcade.sqlIn(eq("hound_acme"), contains("DaliSchema"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        schemaRow("sc1", "HR_SCH", "g1", 4, 0, 0, "db1", "HR",    "gdb1", "app1", "HR App",   "gapp1"),
                        schemaRow("sc2", "FIN_SCH","g2", 7, 1, 0, "db2", "FINANCE","gdb2", "app2", "Fin App",  "gapp2")
                )));

        List<DaliApplicationDto> apps = service.fetchApplicationHierarchy().await().indefinitely();

        assertEquals(2, apps.size());
        assertTrue(apps.stream().anyMatch(a -> "HR App".equals(a.name())));
        assertTrue(apps.stream().anyMatch(a -> "Fin App".equals(a.name())));
    }

    // ── Orphan databases (no app) ──────────────────────────────────────────────

    @Test
    void orphanDatabases_groupedInNullApp() {
        when(arcade.sqlIn(eq("hound_acme"), contains("DaliSchema"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        schemaRow("sc1", "SCH_A", "g1", 2, 0, 0, "db1", "LEGACY", "gdb1", null, null, null)
                )));

        List<DaliApplicationDto> apps = service.fetchApplicationHierarchy().await().indefinitely();

        assertEquals(1, apps.size());
        DaliApplicationDto orphanApp = apps.get(0);
        assertNull(orphanApp.id());
        assertNull(orphanApp.name());
        assertEquals(1, orphanApp.databases().size());
        assertEquals("LEGACY", orphanApp.databases().get(0).dbName());
    }

    @Test
    void mixedAppsAndOrphans_bothPresent() {
        when(arcade.sqlIn(eq("hound_acme"), contains("DaliSchema"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        schemaRow("sc1", "SCH_NAMED", "g1", 2, 0, 0,
                                  "db1", "DWH",    "gdb1", "app1", "DataHub", "gapp1"),
                        schemaRow("sc2", "SCH_ORPHAN","g2", 3, 0, 0,
                                  "db2", "LEGACY", "gdb2", null,   null,      null)
                )));

        List<DaliApplicationDto> apps = service.fetchApplicationHierarchy().await().indefinitely();

        assertEquals(2, apps.size());
        // Named app comes first (inserted before orphan group)
        assertTrue(apps.stream().anyMatch(a -> "DataHub".equals(a.name())));
        assertTrue(apps.stream().anyMatch(a -> a.name() == null));
    }

    // ── Tenant routing ─────────────────────────────────────────────────────────

    @Test
    void routesToTenantDatabase() {
        when(arcade.sqlIn(anyString(), contains("DaliSchema"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));

        service.fetchApplicationHierarchy().await().indefinitely();

        verify(arcade).sqlIn(eq("hound_acme"), anyString(), anyMap());
    }

    // ── Static helper edge cases ───────────────────────────────────────────────

    @Test
    void schemaCounts_mappedCorrectly() {
        when(arcade.sqlIn(eq("hound_acme"), contains("DaliSchema"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        schemaRow("sc1", "SCH", "g1", 12, 3, 7,
                                  "db1", "DWH", "gdb1", "app1", "App", "gapp1")
                )));

        List<DaliApplicationDto> apps = service.fetchApplicationHierarchy().await().indefinitely();
        var schema = apps.get(0).databases().get(0).schemas().get(0);

        assertEquals(12, schema.tableCount());
        assertEquals(3,  schema.routineCount());
        assertEquals(7,  schema.packageCount());
    }

    // ── Helper: build a row map ────────────────────────────────────────────────

    private static Map<String, Object> schemaRow(
            String schId, String schName, String schGeoid,
            int tableCount, int routineCount, int packageCount,
            String dbId, String dbName, String dbGeoid,
            String appId, String appName, String appGeoid) {
        Map<String, Object> row = new java.util.HashMap<>();
        row.put("schId",       schId);
        row.put("schName",     schName);
        row.put("schGeoid",    schGeoid);
        row.put("tableCount",  tableCount);
        row.put("routineCount",routineCount);
        row.put("packageCount",packageCount);
        row.put("dbId",        dbId);
        row.put("dbName",      dbName);
        row.put("dbGeoid",     dbGeoid);
        row.put("appId",       appId);
        row.put("appName",     appName);
        row.put("appGeoid",    appGeoid);
        return row;
    }
}
