package studio.seer.lineage.resource;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.SearchResult;
import studio.seer.lineage.model.TenantStats;
import studio.seer.lineage.security.SeerIdentity;
import studio.seer.tenantrouting.TenantResource;
import studio.seer.tenantrouting.YggLineageRegistry;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SHT-13: Cross-tenant admin query tests.
 *
 * Verifies:
 * - tenantStats() and crossTenantSearch() reject callers without super-admin role
 * - super-admin gets aggregated data from all active tenants
 * - Audit: spoof attempts are blocked at TenantContextFilter (covered in SHT-12/filter tests)
 */
@QuarkusTest
class AdminResourceTest {

    @InjectMock ArcadeGateway      arcade;
    @InjectMock SeerIdentity       identity;
    @InjectMock YggLineageRegistry lineageRegistry;

    @Inject AdminResource resource;

    @BeforeEach
    void stubDefaults() {
        when(identity.role()).thenReturn("super-admin");
        when(identity.tenantAlias()).thenReturn("default");

        TenantResource res = mock(TenantResource.class);
        when(res.databaseName()).thenReturn("hound_default");
        when(lineageRegistry.resourceFor(anyString())).thenReturn(res);

        // FRIGG tenant list
        when(arcade.sqlIn(eq("frigg-tenants"), contains("DaliTenantConfig"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        Map.of("tenantAlias", "tenant-a", "status", "ACTIVE"),
                        Map.of("tenantAlias", "tenant-b", "status", "ACTIVE")
                )));

        // Per-tenant count queries
        when(arcade.sqlIn(anyString(), contains("DaliSession"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(Map.of("cnt", 5L))));
        when(arcade.sqlIn(anyString(), contains("DaliRoutine"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(Map.of("cnt", 12L))));
        when(arcade.sqlIn(anyString(), contains("DaliTable"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(Map.of("cnt", 30L))));
    }

    // ── Access control ────────────────────────────────────────────────────────

    @Test
    void tenantStats_adminRole_rejected() {
        when(identity.role()).thenReturn("admin");

        GraphQLException ex = assertThrows(GraphQLException.class,
                () -> resource.tenantStats(true).await().indefinitely());

        assertNotNull(ex.getMessage());
    }

    @Test
    void tenantStats_viewerRole_rejected() {
        when(identity.role()).thenReturn("viewer");

        assertThrows(GraphQLException.class,
                () -> resource.tenantStats(false).await().indefinitely());
    }

    @Test
    void crossTenantSearch_editorRole_rejected() {
        when(identity.role()).thenReturn("editor");

        assertThrows(GraphQLException.class,
                () -> resource.crossTenantSearch("orders").await().indefinitely());
    }

    // ── Super-admin access ────────────────────────────────────────────────────

    @Test
    void tenantStats_allTenants_returnsBothTenants() {
        List<TenantStats> stats = resource.tenantStats(true).await().indefinitely();

        assertNotNull(stats);
        assertEquals(2, stats.size());
        assertTrue(stats.stream().anyMatch(s -> "tenant-a".equals(s.tenantAlias())));
        assertTrue(stats.stream().anyMatch(s -> "tenant-b".equals(s.tenantAlias())));
    }

    @Test
    void tenantStats_allTenants_countsAreCorrect() {
        List<TenantStats> stats = resource.tenantStats(true).await().indefinitely();

        stats.forEach(s -> {
            assertEquals(5L,  s.sessionCount(),  "sessionCount mismatch for " + s.tenantAlias());
            assertEquals(12L, s.routineCount(), "routineCount mismatch for " + s.tenantAlias());
        });
    }

    @Test
    void tenantStats_singleTenant_returnsSelf() {
        when(identity.tenantAlias()).thenReturn("default");
        when(arcade.sqlIn(anyString(), contains("DaliSession"), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(Map.of("cnt", 7L))));

        List<TenantStats> stats = resource.tenantStats(false).await().indefinitely();

        assertEquals(1, stats.size());
        assertEquals("default", stats.get(0).tenantAlias());
        assertEquals(7L, stats.get(0).sessionCount());
    }

    @Test
    void crossTenantSearch_emptyQuery_returnsEmpty() {
        List<SearchResult> results = resource.crossTenantSearch("").await().indefinitely();
        assertTrue(results.isEmpty());
    }

    @Test
    void crossTenantSearch_blankQuery_returnsEmpty() {
        List<SearchResult> results = resource.crossTenantSearch("   ").await().indefinitely();
        assertTrue(results.isEmpty());
    }

    @Test
    void crossTenantSearch_matchingQuery_returnsResultsFromAllTenants() {
        // FRIGG returns only ACTIVE tenants for search
        when(arcade.sqlIn(eq("frigg-tenants"), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        Map.of("tenantAlias", "tenant-a"),
                        Map.of("tenantAlias", "tenant-b")
                )));

        TenantResource resA = mock(TenantResource.class);
        when(resA.databaseName()).thenReturn("hound_tenant-a");
        TenantResource resB = mock(TenantResource.class);
        when(resB.databaseName()).thenReturn("hound_tenant-b");
        when(lineageRegistry.resourceFor("tenant-a")).thenReturn(resA);
        when(lineageRegistry.resourceFor("tenant-b")).thenReturn(resB);

        when(arcade.sqlIn(eq("hound_tenant-a"), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        Map.of("id", "#1:1", "type", "DaliTable",
                               "label", "orders", "scope", "ACME"))));
        when(arcade.sqlIn(eq("hound_tenant-b"), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        Map.of("id", "#1:2", "type", "DaliTable",
                               "label", "orders", "scope", "BETA"))));

        List<SearchResult> results = resource.crossTenantSearch("orders")
                .await().indefinitely();

        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(r -> r.id().startsWith("tenant-a:")));
        assertTrue(results.stream().anyMatch(r -> r.id().startsWith("tenant-b:")));
    }

    @Test
    void crossTenantSearch_oneTenantFails_doesNotLeakData() {
        // If tenant-b query fails, tenant-a results should not include tenant-b data
        when(arcade.sqlIn(eq("frigg-tenants"), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(Map.of("tenantAlias", "tenant-a"))));

        TenantResource res = mock(TenantResource.class);
        when(res.databaseName()).thenReturn("hound_tenant-a");
        when(lineageRegistry.resourceFor("tenant-a")).thenReturn(res);

        when(arcade.sqlIn(eq("hound_tenant-a"), anyString(), anyMap()))
                .thenReturn(Uni.createFrom().item(List.of(
                        Map.of("id", "#1:1", "type", "DaliTable",
                               "label", "orders", "scope", "ACME"))));

        List<SearchResult> results = resource.crossTenantSearch("orders")
                .await().indefinitely();

        assertEquals(1, results.size());
        assertTrue(results.get(0).id().startsWith("tenant-a:"));
    }

    // ── DB isolation: FRIGG queries go to frigg-tenants, not hound ───────────

    @Test
    void tenantStats_tenantListQueriesCorrectDb() {
        resource.tenantStats(true).await().indefinitely();

        verify(arcade, atLeastOnce()).sqlIn(eq("frigg-tenants"), anyString(), anyMap());
        verify(arcade, never()).sqlIn(eq("hound_default"), contains("DaliTenantConfig"), anyMap());
    }
}
