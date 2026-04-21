package studio.seer.lineage.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.*;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.SearchResult;
import studio.seer.lineage.model.TenantStats;
import studio.seer.lineage.security.SeerIdentity;
import studio.seer.tenantrouting.YggLineageRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * SHT-05: Cross-tenant admin GraphQL queries.
 *
 * All operations require super-admin role; any other caller receives 403.
 * Tenant list is fetched from the FRIGG frigg-tenants database.
 */
@GraphQLApi
public class AdminResource {

    private static final String FRIGG_TENANTS_DB = "frigg-tenants";

    @Inject SeerIdentity       identity;
    @Inject ArcadeGateway      arcade;
    @Inject YggLineageRegistry lineageRegistry;

    // ── tenantStats ────────────────────────────────────────────────────────────

    @Query("tenantStats")
    @Description("Aggregated stats for all tenants (allTenants=true) or caller's tenant. Role: super-admin")
    public Uni<List<TenantStats>> tenantStats(
            @Name("allTenants") @DefaultValue("false") boolean allTenants) {

        requireSuperadmin();

        if (!allTenants) {
            String alias = identity.tenantAlias();
            return statsForAlias(alias).map(List::of);
        }

        return arcade.sqlIn(FRIGG_TENANTS_DB,
                "SELECT tenantAlias, status FROM DaliTenantConfig WHERE status <> 'ARCHIVED'",
                Map.of())
                .flatMap(rows -> {
                    List<Uni<TenantStats>> unis = rows.stream()
                            .map(r -> {
                                String alias  = (String) r.get("tenantAlias");
                                String status = (String) r.getOrDefault("status", "UNKNOWN");
                                return statsForAlias(alias)
                                        .map(s -> new TenantStats(
                                                alias, status, s.sessionCount(), s.routineCount(), s.tableCount()));
                            })
                            .toList();
                    return Uni.join().all(unis).andFailFast();
                });
    }

    // ── crossTenantSearch ─────────────────────────────────────────────────────

    @Query("crossTenantSearch")
    @Description("Full-text search across all active tenants. Role: super-admin")
    public Uni<List<SearchResult>> crossTenantSearch(@Name("query") String query) {

        requireSuperadmin();

        if (query == null || query.isBlank()) {
            return Uni.createFrom().item(List.of());
        }
        String like = "%" + query.replace("%", "\\%") + "%";

        return arcade.sqlIn(FRIGG_TENANTS_DB,
                "SELECT tenantAlias FROM DaliTenantConfig WHERE status = 'ACTIVE'",
                Map.of())
                .flatMap(rows -> {
                    List<Uni<List<SearchResult>>> unis = rows.stream()
                            .map(r -> searchInTenant((String) r.get("tenantAlias"), like))
                            .toList();
                    return Uni.join().all(unis).andFailFast()
                            .map(lists -> lists.stream()
                                    .flatMap(List::stream)
                                    .toList());
                });
    }

    // ── internal ──────────────────────────────────────────────────────────────

    private void requireSuperadmin() {
        if (!"super-admin".equals(identity.role())) {
            throw new GraphQLException("Access denied: super-admin role required");
        }
    }

    private Uni<TenantStats> statsForAlias(String alias) {
        String db = lineageRegistry.resourceFor(alias).databaseName();
        Uni<Long> sessions = arcade.sqlIn(db, "SELECT count(*) as cnt FROM DaliSession", Map.of())
                .map(r -> r.isEmpty() ? 0L : toLong(r.get(0).get("cnt")));
        Uni<Long> routines = arcade.sqlIn(db, "SELECT count(*) as cnt FROM DaliRoutine", Map.of())
                .map(r -> r.isEmpty() ? 0L : toLong(r.get(0).get("cnt")));
        Uni<Long> tables   = arcade.sqlIn(db, "SELECT count(*) as cnt FROM DaliTable", Map.of())
                .map(r -> r.isEmpty() ? 0L : toLong(r.get(0).get("cnt")));

        return Uni.combine().all().unis(sessions, routines, tables)
                .asTuple()
                .map(t -> new TenantStats(alias, "ACTIVE", t.getItem1(), t.getItem2(), t.getItem3()));
    }

    private Uni<List<SearchResult>> searchInTenant(String alias, String like) {
        String db = lineageRegistry.resourceFor(alias).databaseName();
        return arcade.sqlIn(db,
                "SELECT @rid.toString() as id, @class as type, table_name as label, schema_name as scope " +
                "FROM DaliTable WHERE table_name LIKE :q LIMIT 20",
                Map.of("q", like))
                .map(rows -> {
                    List<SearchResult> results = new ArrayList<>();
                    for (Map<String, Object> row : rows) {
                        results.add(new SearchResult(
                                alias + ":" + row.get("id"),
                                (String) row.getOrDefault("type", "DaliTable"),
                                (String) row.getOrDefault("label", ""),
                                (String) row.getOrDefault("scope", alias),
                                1.0));
                    }
                    return results;
                });
    }

    private static long toLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number n) return n.longValue();
        try { return Long.parseLong(v.toString()); } catch (NumberFormatException e) { return 0L; }
    }
}
