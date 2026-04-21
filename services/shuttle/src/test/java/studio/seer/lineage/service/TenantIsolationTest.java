package studio.seer.lineage.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.SearchResult;
import studio.seer.lineage.security.SeerIdentity;
import studio.seer.tenantrouting.TenantResource;
import studio.seer.tenantrouting.YggLineageRegistry;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SHT-12: Two-tenant data isolation tests.
 *
 * Verifies that LineageService, ExploreService, and SearchService route
 * all ArcadeDB queries to the correct per-tenant database (hound_{alias})
 * and never allow cross-tenant data to leak into another tenant's results.
 *
 * Pattern: mock YggLineageRegistry to return different databaseNames per alias,
 * then assert that ArcadeGateway.cypherIn/sqlIn is called with the expected DB.
 */
@QuarkusTest
class TenantIsolationTest {

    @InjectMock ArcadeGateway      arcade;
    @InjectMock SeerIdentity       identity;
    @InjectMock YggLineageRegistry lineageRegistry;

    @Inject LineageService lineageService;
    @Inject SearchService  searchService;

    private static final String DB_A = "hound_tenant-a";
    private static final String DB_B = "hound_tenant-b";

    @BeforeEach
    void stubRegistry() {
        TenantResource resA = mock(TenantResource.class);
        TenantResource resB = mock(TenantResource.class);
        when(resA.databaseName()).thenReturn(DB_A);
        when(resB.databaseName()).thenReturn(DB_B);
        when(lineageRegistry.resourceFor("tenant-a")).thenReturn(resA);
        when(lineageRegistry.resourceFor("tenant-b")).thenReturn(resB);
        when(arcade.cypherIn(anyString(), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));
        when(arcade.sqlIn(anyString(), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));
    }

    // ── LineageService routes to correct DB ───────────────────────────────────

    @Test
    void lineageService_tenantA_queriesDbA() {
        when(identity.tenantAlias()).thenReturn("tenant-a");

        lineageService.expandDeep("#1:0", 2).await().indefinitely();

        verify(arcade, atLeastOnce()).cypherIn(eq(DB_A), anyString(), anyMap());
        verify(arcade, never()).cypherIn(eq(DB_B), anyString(), anyMap());
    }

    @Test
    void lineageService_tenantB_queriesDbB() {
        when(identity.tenantAlias()).thenReturn("tenant-b");

        lineageService.expandDeep("#1:0", 2).await().indefinitely();

        verify(arcade, atLeastOnce()).cypherIn(eq(DB_B), anyString(), anyMap());
        verify(arcade, never()).cypherIn(eq(DB_A), anyString(), anyMap());
    }

    // ── SearchService routes to correct DB ────────────────────────────────────

    @Test
    void searchService_tenantA_queriesDbA() {
        when(identity.tenantAlias()).thenReturn("tenant-a");

        searchService.search("orders", 10).await().indefinitely();

        verify(arcade, atLeastOnce()).sqlIn(eq(DB_A), anyString(), anyMap());
        verify(arcade, never()).sqlIn(eq(DB_B), anyString(), anyMap());
    }

    @Test
    void searchService_tenantB_queriesDbB() {
        when(identity.tenantAlias()).thenReturn("tenant-b");

        searchService.search("orders", 10).await().indefinitely();

        verify(arcade, atLeastOnce()).sqlIn(eq(DB_B), anyString(), anyMap());
        verify(arcade, never()).sqlIn(eq(DB_A), anyString(), anyMap());
    }

    // ── No cross-tenant result leakage ────────────────────────────────────────

    @Test
    void searchService_tenantAData_doesNotLeakToTenantB() {
        when(arcade.sqlIn(eq(DB_A), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        Map.of("id", "#1:1", "type", "DaliTable",
                               "label", "secret_orders", "scope", "ACME_SCHEMA"))));
        when(arcade.sqlIn(eq(DB_B), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of()));

        when(identity.tenantAlias()).thenReturn("tenant-a");
        List<SearchResult> resultsA = searchService.search("secret_orders", 10)
                .await().indefinitely();

        // Clear invocations so the subsequent verify covers only tenant-b's call
        clearInvocations(arcade);

        when(identity.tenantAlias()).thenReturn("tenant-b");
        List<SearchResult> resultsB = searchService.search("secret_orders", 10)
                .await().indefinitely();

        assertFalse(resultsA.isEmpty(), "tenant-a should see its own data");
        assertTrue(resultsB.isEmpty(), "tenant-b must not see tenant-a data");
        verify(arcade, never()).sqlIn(eq(DB_A), anyString(), anyMap());
    }

    @Test
    void lineageService_queriesUseNamedParams_notStringInterpolation() {
        when(identity.tenantAlias()).thenReturn("tenant-a");

        lineageService.expandDeep("#42:7", 3).await().indefinitely();

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> paramsCaptor =
                ArgumentCaptor.forClass((Class) Map.class);
        verify(arcade, atLeastOnce()).cypherIn(anyString(), anyString(), paramsCaptor.capture());

        boolean hasNodeId = paramsCaptor.getAllValues().stream()
                .anyMatch(m -> "#42:7".equals(m.get("nodeId")));
        assertTrue(hasNodeId, "nodeId must be passed as named param, not interpolated");
    }

    @Test
    void switchingTenantsBetweenRequests_eachGetsOwnDb() {
        // Simulate two sequential requests from different tenants
        when(identity.tenantAlias()).thenReturn("tenant-a");
        lineageService.expandDeep("#1:0", 1).await().indefinitely();

        clearInvocations(arcade);

        when(identity.tenantAlias()).thenReturn("tenant-b");
        lineageService.expandDeep("#1:0", 1).await().indefinitely();

        verify(arcade, atLeastOnce()).cypherIn(eq(DB_B), anyString(), anyMap());
        verify(arcade, never()).cypherIn(eq(DB_A), anyString(), anyMap());
    }
}
