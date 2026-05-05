package studio.seer.lineage.resource;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.RepairResult;
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

    private static final Logger log = LoggerFactory.getLogger(AdminResource.class);
    private static final String FRIGG_TENANTS_DB = "frigg-tenants";

    @Inject SeerIdentity       identity;
    @Inject ArcadeGateway      arcade;
    @Inject YggLineageRegistry lineageRegistry;

    // ── tenantStats ────────────────────────────────────────────────────────────

    @Query("tenantStats")
    @Description("Aggregated stats for all tenants (allTenants=true) or caller's tenant. Role: super-admin")
    public Uni<List<TenantStats>> tenantStats(
            @Name("allTenants") @DefaultValue("false") boolean allTenants)
            throws GraphQLException {

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
    public Uni<List<SearchResult>> crossTenantSearch(@Name("query") String query)
            throws GraphQLException {

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

    // ── repairHierarchyEdges ─────────────────────────────────────────────────

    @Mutation("repairHierarchyEdges")
    @Description("Repair missing CONTAINS_TABLE and HAS_COLUMN edges for the caller's tenant. " +
                 "dryRun=true only counts orphans without creating edges. Role: super-admin")
    public Uni<RepairResult> repairHierarchyEdges(
            @Name("dryRun") @DefaultValue("true") boolean dryRun)
            throws GraphQLException {

        requireSuperadmin();

        String alias = identity.tenantAlias();
        String db    = lineageRegistry.resourceFor(alias).databaseName();
        log.info("[ADMIN] repairHierarchyEdges(dryRun={}) for tenant={} db={}", dryRun, alias, db);

        List<String> errors = new ArrayList<>();

        // ── 1. CONTAINS_TABLE: find DaliTable without incoming CONTAINS_TABLE edge
        Uni<Integer> containsTableUni = arcade.sqlIn(db,
                "SELECT @rid.asString() AS trid, schema_geoid FROM DaliTable " +
                "WHERE IN('CONTAINS_TABLE').size() = 0", Map.of())
            .flatMap(orphanTables -> {
                if (orphanTables.isEmpty()) return Uni.createFrom().item(0);

                List<Uni<Integer>> repairs = new ArrayList<>();
                for (Map<String, Object> row : orphanTables) {
                    String tRid     = str(row, "trid");
                    String schGeoid = str(row, "schema_geoid");
                    if (tRid == null || schGeoid == null) continue;

                    if (dryRun) {
                        repairs.add(Uni.createFrom().item(1));
                    } else {
                        repairs.add(
                            arcade.sqlIn(db,
                                "SELECT @rid.asString() AS srid FROM DaliSchema WHERE schema_geoid = :sg",
                                Map.of("sg", schGeoid))
                            .flatMap(schemas -> {
                                if (schemas.isEmpty()) return Uni.createFrom().item(0);
                                String sRid = str(schemas.get(0), "srid");
                                if (sRid == null) return Uni.createFrom().item(0);
                                return arcade.sqlIn(db,
                                    "CREATE EDGE CONTAINS_TABLE FROM " + sRid + " TO " + tRid +
                                    " SET session_id = 'admin-repair'", Map.of())
                                    .map(__ -> 1)
                                    .onFailure().recoverWithUni(ex -> {
                                        errors.add("CONTAINS_TABLE " + sRid + "→" + tRid + ": " + ex.getMessage());
                                        return Uni.createFrom().item(0);
                                    });
                            })
                        );
                    }
                }
                if (repairs.isEmpty()) return Uni.createFrom().item(0);
                return Uni.join().all(repairs).andFailFast()
                        .map(list -> list.stream().mapToInt(Integer::intValue).sum());
            });

        // ── 2. HAS_COLUMN: find DaliColumn without incoming HAS_COLUMN edge
        Uni<Integer> hasColumnUni = arcade.sqlIn(db,
                "SELECT @rid.asString() AS crid, table_geoid FROM DaliColumn " +
                "WHERE IN('HAS_COLUMN').size() = 0", Map.of())
            .flatMap(orphanCols -> {
                if (orphanCols.isEmpty()) return Uni.createFrom().item(0);

                List<Uni<Integer>> repairs = new ArrayList<>();
                for (Map<String, Object> row : orphanCols) {
                    String cRid     = str(row, "crid");
                    String tblGeoid = str(row, "table_geoid");
                    if (cRid == null || tblGeoid == null) continue;

                    if (dryRun) {
                        repairs.add(Uni.createFrom().item(1));
                    } else {
                        repairs.add(
                            arcade.sqlIn(db,
                                "SELECT @rid.asString() AS trid FROM DaliTable WHERE table_geoid = :tg",
                                Map.of("tg", tblGeoid))
                            .flatMap(tables -> {
                                if (tables.isEmpty()) return Uni.createFrom().item(0);
                                String tRid = str(tables.get(0), "trid");
                                if (tRid == null) return Uni.createFrom().item(0);
                                return arcade.sqlIn(db,
                                    "CREATE EDGE HAS_COLUMN FROM " + tRid + " TO " + cRid +
                                    " SET session_id = 'admin-repair'", Map.of())
                                    .map(__ -> 1)
                                    .onFailure().recoverWithUni(ex -> {
                                        errors.add("HAS_COLUMN " + tRid + "→" + cRid + ": " + ex.getMessage());
                                        return Uni.createFrom().item(0);
                                    });
                            })
                        );
                    }
                }
                if (repairs.isEmpty()) return Uni.createFrom().item(0);
                return Uni.join().all(repairs).andFailFast()
                        .map(list -> list.stream().mapToInt(Integer::intValue).sum());
            });

        return Uni.combine().all().unis(containsTableUni, hasColumnUni)
                .asTuple()
                .map(t -> {
                    int ct = t.getItem1();
                    int hc = t.getItem2();
                    log.info("[ADMIN] repairHierarchyEdges done — CONTAINS_TABLE: {}, HAS_COLUMN: {}, dryRun={}",
                             ct, hc, dryRun);
                    return new RepairResult(dryRun, ct, hc, ct + hc, errors);
                });
    }

    // ── internal ──────────────────────────────────────────────────────────────

    private void requireSuperadmin() throws GraphQLException {
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

    private static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v == null ? null : v.toString();
    }
}
