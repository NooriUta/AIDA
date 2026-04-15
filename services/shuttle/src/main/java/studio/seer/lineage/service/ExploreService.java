package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.ExploreResult;
import studio.seer.lineage.model.GraphEdge;
import studio.seer.lineage.model.GraphNode;

import java.util.*;

/**
 * L2 — explore a schema or package scope.
 *
 * Confirmed against hound DB (2026-04-04):
 *   DaliSchema:  schema_name, schema_geoid
 *   DaliTable:   table_name,  table_geoid,  schema_geoid, column_count
 *   DaliPackage: package_name, package_geoid
 *   DaliRoutine: routine_name, routine_geoid, routine_type, package_geoid
 *
 * Scope format:
 *   "schema-ODS_TRP_CDWH"  → DaliSchema.schema_name
 *   "pkg-MY_PKG"           → DaliPackage.package_name
 *   "#10:0"                → raw @rid
 */
@ApplicationScoped
public class ExploreService {

    private static final Logger log = Logger.getLogger(ExploreService.class);

    /** Node count threshold above which the result is considered truncated. */
    static final int NODE_LIMIT = 500;

    @Inject
    ArcadeGateway arcade;

    public Uni<ExploreResult> explore(String scope) {
        return explore(scope, false);
    }

    /**
     * @param includeExternal when true, appends extra UNION ALL segments that
     *   fetch READS_FROM / WRITES_TO / DATA_FLOW / FILTER_FLOW edges whose
     *   table endpoint is in a DIFFERENT schema than {@code scope}. Default
     *   false (the legacy behavior — only same-schema edges are returned).
     *   Only honored for schema scope today; pkg / db / rid scopes ignore it.
     */
    public Uni<ExploreResult> explore(String scope, boolean includeExternal) {
        ScopeRef ref = ScopeRef.parse(scope);
        return switch (ref.type()) {
            case "schema"   -> exploreSchema(ref.name(), ref.dbName(), includeExternal);
            case "pkg"      -> explorePackage(ref.name());
            case "db"       -> exploreByDatabase(ref.name());
            // "routine-<@rid>": focused L3 view — root stmts + tables + records only.
            // Set by LoomCanvas when drilling from L2 AGG into a DaliRoutine node.
            case "routine"  -> exploreRoutineScope(ref.name());
            default         -> exploreByRid(ref.name());
        };
    }

    // ── Schema scope ──────────────────────────────────────────────────────────

    private Uni<ExploreResult> exploreSchema(String schemaName, String dbName) {
        return exploreSchema(schemaName, dbName, false);
    }

    /**
     * Extra UNION ALL branches appended when includeExternal=true. Picks up
     * cross-schema READS_FROM / WRITES_TO from both the root stmts and their
     * descendants (via CHILD_OF*0..30 — the `0` bound includes the root itself
     * so a single pattern covers both direct and hoisted reads).
     *
     * <p><b>Design note (performance).</b> The first attempt used the same
     * {@code NOT (s)-[:CONTAINS_TABLE]->(t)} exclusion as the same-schema
     * segments, but that combined two variable-length patterns with a negative
     * subgraph check and blew past the 30 s query budget on any real schema.
     * Replaced with a cheap property filter {@code t.schema_geoid <> $schema}
     * — ArcadeDB evaluates that per-row instead of scheduling a subgraph
     * probe. Returns the same rows, just orders of magnitude faster.</p>
     */
    private static final String EXTERNAL_EXTENSION = """
            UNION ALL
            // EXT-READS_FROM — any stmt (root or descendant) in this schema
            // that reads from a table in a DIFFERENT schema. `*0..30` on
            // CHILD_OF makes sub = rootStmt for direct reads, and 1..30
            // for hoisted reads, in a single pattern.
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_ROUTINE]->(r1)-[:CONTAINS_ROUTINE*0..1]->(rr:DaliRoutine)
                  -[:CONTAINS_STMT]->(rootStmt:DaliStatement)
            WHERE coalesce(rootStmt.parent_statement, '') = ''
            MATCH (rootStmt)<-[:CHILD_OF*0..30]-(sub:DaliStatement)-[:READS_FROM]->(t:DaliTable)
            WHERE t.schema_geoid IS NOT NULL AND t.schema_geoid <> $schema
            RETURN DISTINCT id(rootStmt) AS srcId, coalesce(rootStmt.stmt_geoid, rootStmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 2000
            UNION ALL
            // EXT-WRITES_TO — same as above but for WRITES_TO
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_ROUTINE]->(r1)-[:CONTAINS_ROUTINE*0..1]->(rr:DaliRoutine)
                  -[:CONTAINS_STMT]->(rootStmt:DaliStatement)
            WHERE coalesce(rootStmt.parent_statement, '') = ''
            MATCH (rootStmt)<-[:CHILD_OF*0..30]-(sub:DaliStatement)-[:WRITES_TO]->(t:DaliTable)
            WHERE t.schema_geoid IS NOT NULL AND t.schema_geoid <> $schema
            RETURN DISTINCT id(rootStmt) AS srcId, coalesce(rootStmt.stmt_geoid, rootStmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'WRITES_TO' AS edgeType
            LIMIT 1000
            """;

    private Uni<ExploreResult> exploreSchema(String schemaName, String dbName, boolean includeExternal) {
        // ─── Phase 1: Входит в схему (structural membership) ────────────────────
        // 1.1  schema → tables
        // 1.3  schema → routines / packages  (all direct CONTAINS_ROUTINE children)
        // 1.4  schema-direct routine → rootStmt
        // 1.5a schema → pkg → routine        (all routines in all packages, unfiltered)
        // 1.5b pkg routine → rootStmt
        //
        // ─── Phase 2: Является источником / приёмником ──────────────────────────
        // 2.1  rootStmt → WRITES_TO   (for all schema tables)
        // 2.2a rootStmt → READS_FROM  (direct, for all schema tables)
        // 2.2b rootStmt → READS_FROM  (hoisted from subquery via CHILD_OF)
        //   Pattern: table <-[:READS_FROM]- subStmt -[:CHILD_OF]-> rootStmt
        //   subStmt is intermediate — never appears in srcId/tgtId.
        //
        // ─── Phase 3: Колонки ────────────────────────────────────────────────────
        // HAS_COLUMN / HAS_OUTPUT_COL / HAS_AFFECTED_COL are NOT fetched here.
        // They are fetched in a single second-pass query exploreStmtColumns()
        // which accepts all rendered node @rids (tables + stmts) and returns all
        // column edges in one call — avoids LIMIT collisions with CONTAINS_STMT.
        //
        // $dbName filter: restricts to exact database to avoid cross-db schema name collisions.
        // ArcadeDB: schema_name has a FULL_TEXT (Lucene) index which does NOT support equality
        // lookup in Cypher/SQL. Use schema_geoid with the compound HASH index
        // DaliSchema[db_name,schema_geoid] instead. schema_geoid == schema_name for all
        // standard SQL databases (both store the schema identifier, e.g. "HR", "SYS").
        String cypher = """
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_TABLE]->(t:DaliTable)
            RETURN id(s) AS srcId, s.schema_name AS srcLabel, 'DaliSchema' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'CONTAINS_TABLE' AS edgeType
            LIMIT 300
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_ROUTINE]->(child)
            WHERE child:DaliPackage OR child:DaliRoutine
            RETURN id(s) AS srcId, s.schema_name AS srcLabel, 'DaliSchema' AS srcType,
                   id(child) AS tgtId,
                   coalesce(child.package_name, child.routine_name, '') AS tgtLabel,
                   '' AS tgtScope, labels(child)[0] AS tgtType, 'CONTAINS_ROUTINE' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_ROUTINE]->(r:DaliRoutine)-[:CONTAINS_STMT]->(stmt:DaliStatement)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(r) AS srcId, r.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
                   id(stmt) AS tgtId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'CONTAINS_STMT' AS edgeType
            LIMIT 300
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_ROUTINE]->(pkg:DaliPackage)-[:CONTAINS_ROUTINE]->(r:DaliRoutine)
            RETURN DISTINCT id(pkg) AS srcId, pkg.package_name AS srcLabel, 'DaliPackage' AS srcType,
                   id(r) AS tgtId, r.routine_name AS tgtLabel, '' AS tgtScope,
                   'DaliRoutine' AS tgtType, 'CONTAINS_ROUTINE' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_ROUTINE]->(:DaliPackage)-[:CONTAINS_ROUTINE]->(r:DaliRoutine)
                  -[:CONTAINS_STMT]->(stmt:DaliStatement)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(r) AS srcId, r.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
                   id(stmt) AS tgtId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'CONTAINS_STMT' AS edgeType
            LIMIT 300
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_TABLE]->(t:DaliTable)<-[:WRITES_TO]-(stmt:DaliStatement)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'WRITES_TO' AS edgeType
            LIMIT 3000
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_TABLE]->(t:DaliTable)<-[:READS_FROM]-(stmt:DaliStatement)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 3000
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_TABLE]->(t:DaliTable)
                  <-[:READS_FROM]-(:DaliStatement)-[:CHILD_OF*1..30]->(rootStmt:DaliStatement)
            WHERE coalesce(rootStmt.parent_statement, '') = ''
            RETURN DISTINCT id(rootStmt) AS srcId, coalesce(rootStmt.stmt_geoid, rootStmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_TABLE]->(t:DaliTable)
                  <-[:WRITES_TO]-(:DaliStatement)-[:CHILD_OF*1..30]->(rootStmt:DaliStatement)
            WHERE coalesce(rootStmt.parent_statement, '') = ''
            RETURN DISTINCT id(rootStmt) AS srcId, coalesce(rootStmt.stmt_geoid, rootStmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'WRITES_TO' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_TABLE]->(:DaliTable)<-[:READS_FROM]-(stmt:DaliStatement)
                  -[:WRITES_TO]->(target:DaliTable)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(target) AS tgtId, target.table_name AS tgtLabel, target.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'WRITES_TO' AS edgeType
            LIMIT 200
            """ + (includeExternal ? EXTERNAL_EXTENSION : "");

        // ArcadeDB Cypher bug: $param IS NULL does not work in WHERE clauses.
        // Workaround: use empty string as sentinel ($dbName = '' OR ...).
        Map<String, Object> params = Map.of(
            "schema", schemaName,
            "dbName", dbName == null || dbName.isBlank() ? "" : dbName
        );

        // Column-level DATA_FLOW / FILTER_FLOW is produced by a SEPARATE query
        // that emits per-row sourceHandle / targetHandle. We keep it separate
        // from the main UNION ALL chain because Cypher UNION ALL demands every
        // branch project the same column list, and adding handle columns to
        // all ~10 base branches would balloon the query text for little gain.
        // Running it in parallel via Uni.combine() is cheaper.
        //
        // The old aggregated segments 2.3 / 2.4 (table → stmt without handles)
        // have been removed — this query supersedes them by also producing
        // the same (srcTable, tgtStmt) pair rows plus the column handles
        // that React Flow uses to route into specific column rows.
        // Column-level DATA_FLOW: resolve the target handle to the ROOT stmt's
        // OWN column handle (DaliAffectedColumn for INSERT/UPDATE/MERGE, or
        // DaliOutputColumn for SELECT) by name-matching the descendant's
        // DaliOutputColumn. If the root has neither matching column,
        // targetHandle stays empty and the edge lands on the node default.
        String columnFlowCypher = """
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_TABLE]->(srcTbl:DaliTable)-[:HAS_COLUMN]->(srcCol:DaliColumn)
            MATCH (srcCol)-[:DATA_FLOW]->(oc:DaliOutputColumn)<-[:HAS_OUTPUT_COL]-(sub:DaliStatement)
            MATCH (sub)-[:CHILD_OF*0..30]->(root:DaliStatement)
            WHERE coalesce(root.parent_statement, '') = ''
            OPTIONAL MATCH (root)-[:HAS_AFFECTED_COL]->(rootAff:DaliAffectedColumn)
                WHERE toUpper(coalesce(rootAff.column_name, '')) = toUpper(coalesce(oc.name, oc.col_key, ''))
            OPTIONAL MATCH (root)-[:HAS_OUTPUT_COL]->(rootOc:DaliOutputColumn)
                WHERE toUpper(coalesce(rootOc.name, rootOc.col_key, '')) = toUpper(coalesce(oc.name, oc.col_key, ''))
            RETURN DISTINCT id(srcTbl) AS srcId, srcTbl.table_name AS srcLabel, 'DaliTable' AS srcType,
                   id(root) AS tgtId, coalesce(root.stmt_geoid, root.snippet, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'DATA_FLOW' AS edgeType,
                   'src-' + id(srcCol) AS sourceHandle,
                   CASE
                     WHEN rootAff IS NOT NULL THEN 'tgt-' + id(rootAff)
                     WHEN rootOc  IS NOT NULL THEN 'tgt-' + id(rootOc)
                     ELSE ''
                   END AS targetHandle
            UNION
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_TABLE]->(srcTbl:DaliTable)-[:HAS_COLUMN]->(srcCol:DaliColumn)
            MATCH (srcCol)-[:FILTER_FLOW]->(stmt:DaliStatement)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(srcTbl) AS srcId, srcTbl.table_name AS srcLabel, 'DaliTable' AS srcType,
                   id(stmt) AS tgtId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'FILTER_FLOW' AS edgeType,
                   'src-' + id(srcCol) AS sourceHandle,
                   ''                  AS targetHandle
            LIMIT 4000
            """;

        var baseUni = arcade.cypher(cypher, params);
        var colUni  = arcade.cypher(columnFlowCypher, params).onFailure().recoverWithItem(List.of());

        return Uni.combine().all().unis(baseUni, colUni).asTuple()
            .map(tuple -> {
                var merged = new ArrayList<Map<String, Object>>(tuple.getItem1().size() + tuple.getItem2().size());
                merged.addAll(tuple.getItem1());
                merged.addAll(tuple.getItem2());
                return buildResult(merged, schemaName, "DaliSchema");
            })
            .flatMap(this::enrichDataSource);
    }

    // ── Routine aggregate scope (new L2) ─────────────────────────────────────
    //
    // Phase S2.1 / Phase 2 from the original 5-level plan. Returns routines +
    // tables with aggregated READS_FROM / WRITES_TO edges: for every DaliRoutine
    // in the given schema/package scope, unions the edges of every DaliStatement
    // contained in that routine via CONTAINS_STMT and collapses them to a
    // single (routine → table) per (direction, table). Each edge carries a
    // stmt_count — how many distinct statements inside the routine hit that
    // table — via the meta-scope field so the frontend transform can pick it
    // up and draw thicker/labelled lines when the count is high.
    //
    // Scope parsing uses the same "schema-<name>" / "pkg-<name>" prefix
    // convention as explore(). Package scope resolves routines under that
    // package directly; schema scope covers both schema-direct routines and
    // routines nested inside packages (via CONTAINS_ROUTINE*0..1).

    public Uni<ExploreResult> exploreRoutineAggregate(String scope) {
        ScopeRef ref = ScopeRef.parse(scope);
        boolean isPackage = "pkg".equals(ref.type());
        String scopeName  = ref.name();

        // Scope match differs per kind — but we avoid stacking two variable-
        // length patterns with the data-flow walk because the combination
        // timed out at 30s on real schemas. Use TWO explicit schema branches
        // (direct routine + package-mediated routine) for schema scope, and
        // a single branch for package scope. No CHILD_OF hoist for the first
        // pass — aggregation counts direct-level reads/writes only. If
        // missing-hoist turns out to hide real sources, Phase S2.3 can add
        // it back with a careful Cypher budget.
        String cypher;
        Map<String, Object> params;
        // NOTE: CONTAINS_STMT connects DaliRoutine to ALL its statements (root + sub-query).
        // We intentionally do NOT filter by parent_statement here — the goal is to collect
        // ALL tables that any statement of the routine reads from / writes to, regardless
        // of nesting depth. collect(DISTINCT tR/tW) deduplicates at the routine level so
        // the canvas shows one consolidated edge per (routine, table) pair.
        if (isPackage) {
            cypher = """
                MATCH (p:DaliPackage {package_name: $scope})-[:CONTAINS_ROUTINE]->(r:DaliRoutine)
                OPTIONAL MATCH (r)-[:CONTAINS_STMT]->(stmt:DaliStatement)
                OPTIONAL MATCH (stmt)-[:READS_FROM]->(tR:DaliTable)
                OPTIONAL MATCH (stmt)-[:WRITES_TO]->(tW:DaliTable)
                WITH p, r, collect(DISTINCT tR) AS reads, collect(DISTINCT tW) AS writes
                RETURN id(p) AS pkgId, p.package_name AS pkgName,
                       coalesce(p.schema_geoid, '') AS pkgSchema,
                       id(r) AS src, r.routine_name AS srcLabel,
                       coalesce(r.schema_geoid, '') AS srcSchema,
                       coalesce(r.package_geoid, '') AS srcPackage,
                       coalesce(r.routine_type, '') AS srcKind,
                       reads, writes
                LIMIT 300
                """;
            params = Map.of("scope", scopeName);
        } else {
            cypher = """
                MATCH (s:DaliSchema) WHERE s.schema_geoid = $scope
                MATCH (s)-[:CONTAINS_ROUTINE]->(n1)
                OPTIONAL MATCH (n1)-[:CONTAINS_ROUTINE]->(nested:DaliRoutine)
                WITH s, CASE WHEN n1:DaliRoutine THEN n1 ELSE nested END AS r
                WHERE r IS NOT NULL
                OPTIONAL MATCH (r)-[:CONTAINS_STMT]->(stmt:DaliStatement)
                OPTIONAL MATCH (stmt)-[:READS_FROM]->(tR:DaliTable)
                OPTIONAL MATCH (stmt)-[:WRITES_TO]->(tW:DaliTable)
                WITH r, collect(DISTINCT tR) AS reads, collect(DISTINCT tW) AS writes
                RETURN id(r) AS src, r.routine_name AS srcLabel,
                       coalesce(r.schema_geoid, '') AS srcSchema,
                       coalesce(r.package_geoid, '') AS srcPackage,
                       coalesce(r.routine_type, '') AS srcKind,
                       reads, writes
                LIMIT 300
                """;
            params = Map.of("scope", scopeName);
        }

        // Post-process rows in Java: flatten (routine, table) pairs into
        // ExploreResult rows. buildResult() will dedup and build the node
        // map. We also emit one "node-only" row per routine so routines
        // with zero reads/writes still render as standalone nodes.
        //
        // For schema scope: also fetch external routines (from OTHER schemas)
        // that read/write tables owned by this schema — gives the complete
        // cross-schema data-flow picture on the aggregate canvas.
        final String finalScopeName = scopeName;
        final boolean finalIsPackage = isPackage;

        Uni<List<Map<String, Object>>> mainQuery = arcade.cypher(cypher, params);

        // External-routines query: only meaningful for schema scope.
        // Returns routines from other schemas that interact with tables in $scope.
        // All statements of the external routine are checked (no parent_statement filter)
        // so sub-query reads/writes are also captured.
        Uni<List<Map<String, Object>>> extQuery = finalIsPackage
            ? Uni.createFrom().item(List.of())
            : arcade.cypher("""
                MATCH (extR:DaliRoutine)-[:CONTAINS_STMT]->(stmt:DaliStatement)-[:READS_FROM]->(tR:DaliTable)
                WHERE extR.schema_geoid <> $scope
                  AND tR.schema_geoid = $scope
                WITH extR, collect(DISTINCT tR) AS reads
                OPTIONAL MATCH (extR)-[:CONTAINS_STMT]->(stmt2:DaliStatement)-[:WRITES_TO]->(tW:DaliTable)
                WHERE tW.schema_geoid = $scope
                WITH extR, reads, collect(DISTINCT tW) AS writes
                RETURN id(extR) AS src, extR.routine_name AS srcLabel,
                       coalesce(extR.schema_geoid, '') AS srcSchema,
                       coalesce(extR.package_geoid, '') AS srcPackage,
                       coalesce(extR.routine_type, '') AS srcKind,
                       reads, writes
                LIMIT 100
                """, params)
              .onFailure().recoverWithItem(List.of());

        // CALLS query: routine→routine invocations within scope.
        // Returns rows already in buildResult format (srcId/tgtId + CALLS edgeType).
        // The callee (r2) may reside outside the current scope — it is added as an
        // external RoutineNode with its own schema/package info.
        String callsCypher = finalIsPackage ? """
                MATCH (p:DaliPackage {package_name: $scope})-[:CONTAINS_ROUTINE]->(r1:DaliRoutine)
                MATCH (r1)-[:CALLS]->(r2:DaliRoutine)
                RETURN id(r1) AS srcId, r1.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
                       coalesce(r1.schema_geoid, '') AS srcScope,
                       coalesce(r1.package_geoid, '') AS srcPackage,
                       coalesce(r1.routine_type, '') AS srcKind,
                       id(r2) AS tgtId, r2.routine_name AS tgtLabel, 'DaliRoutine' AS tgtType,
                       coalesce(r2.schema_geoid, '') AS tgtScope,
                       'CALLS' AS edgeType, '' AS sourceHandle, '' AS targetHandle
                LIMIT 200
                """ : """
                MATCH (s:DaliSchema) WHERE s.schema_geoid = $scope
                MATCH (s)-[:CONTAINS_ROUTINE]->(n1)
                OPTIONAL MATCH (n1)-[:CONTAINS_ROUTINE]->(nested:DaliRoutine)
                WITH CASE WHEN n1:DaliRoutine THEN n1 ELSE nested END AS r1
                WHERE r1 IS NOT NULL
                MATCH (r1)-[:CALLS]->(r2:DaliRoutine)
                RETURN id(r1) AS srcId, r1.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
                       coalesce(r1.schema_geoid, '') AS srcScope,
                       coalesce(r1.package_geoid, '') AS srcPackage,
                       coalesce(r1.routine_type, '') AS srcKind,
                       id(r2) AS tgtId, r2.routine_name AS tgtLabel, 'DaliRoutine' AS tgtType,
                       coalesce(r2.schema_geoid, '') AS tgtScope,
                       'CALLS' AS edgeType, '' AS sourceHandle, '' AS targetHandle
                LIMIT 200
                """;
        Uni<List<Map<String, Object>>> callsQuery = arcade.cypher(callsCypher, params)
            .onFailure().recoverWithItem(List.of());

        return Uni.combine().all().unis(List.of(mainQuery, extQuery, callsQuery))
            .combinedWith(results -> {
                // results[0] = main rows (routine→table in aggregated format)
                // results[1] = ext rows  (cross-schema routine→table)
                // results[2] = calls rows (routine→routine, already in buildResult format)
                @SuppressWarnings("unchecked")
                var mainRows  = (List<Map<String, Object>>) results.get(0);
                @SuppressWarnings("unchecked")
                var extRows   = (List<Map<String, Object>>) results.get(1);
                @SuppressWarnings("unchecked")
                var callRows  = (List<Map<String, Object>>) results.get(2);

                var allRows = new ArrayList<Map<String, Object>>(mainRows);
                allRows.addAll(extRows);

                var flatRows = new ArrayList<Map<String, Object>>();

                // Package scope: emit the DaliPackage node once (as NODE_ONLY self-row)
                // so the frontend can use it as a compound group container for its Routines.
                // pkgNodeId captured here so CONTAINS_ROUTINE edges can be emitted per-routine.
                String pkgNodeId    = "";
                String pkgNodeName  = "";
                String pkgNodeScope = "";

                for (var row : allRows) {
                    String rId     = str(row, "src");
                    String rLabel  = str(row, "srcLabel");
                    String rSchema = str(row, "srcSchema");
                    String rPkg    = str(row, "srcPackage");
                    String rKind   = str(row, "srcKind");
                    if (rId == null || rId.isEmpty()) continue;

                    // First routine row for package scope: emit Package self-node
                    if (finalIsPackage && pkgNodeId.isEmpty()) {
                        pkgNodeId    = str(row, "pkgId");
                        pkgNodeName  = str(row, "pkgName");
                        pkgNodeScope = str(row, "pkgSchema");
                        if (!pkgNodeId.isEmpty()) {
                            var pkgSelf = new HashMap<String, Object>();
                            pkgSelf.put("srcId",        pkgNodeId);
                            pkgSelf.put("srcLabel",     pkgNodeName);
                            pkgSelf.put("srcType",      "DaliPackage");
                            pkgSelf.put("srcScope",     pkgNodeScope);
                            pkgSelf.put("srcPackage",   "");
                            pkgSelf.put("srcKind",      "PKG");
                            pkgSelf.put("tgtId",        pkgNodeId);
                            pkgSelf.put("tgtLabel",     pkgNodeName);
                            pkgSelf.put("tgtScope",     "");
                            pkgSelf.put("tgtType",      "DaliPackage");
                            pkgSelf.put("edgeType",     "NODE_ONLY");
                            pkgSelf.put("sourceHandle", "");
                            pkgSelf.put("targetHandle", "");
                            flatRows.add(pkgSelf);
                        }
                    }

                    // Routine self-node — emitted via a dummy edge so
                    // buildResult registers the routine even without any reads/writes.
                    // srcScope / srcPackage / srcKind are passed through so buildResult
                    // can populate scope and meta on the GraphNode.
                    var selfRow = new HashMap<String, Object>();
                    selfRow.put("srcId",       rId);
                    selfRow.put("srcLabel",    rLabel);
                    selfRow.put("srcType",     "DaliRoutine");
                    selfRow.put("srcScope",    rSchema);   // ← schema_geoid → node.scope
                    selfRow.put("srcPackage",  rPkg);      // ← package_geoid → node.meta
                    selfRow.put("srcKind",     rKind);     // ← routine_type  → node.meta
                    selfRow.put("tgtId",       rId);
                    selfRow.put("tgtLabel",    rLabel);
                    selfRow.put("tgtScope",    "");
                    selfRow.put("tgtType",     "DaliRoutine");
                    selfRow.put("edgeType",    "NODE_ONLY");
                    selfRow.put("sourceHandle", "");
                    selfRow.put("targetHandle", "");
                    flatRows.add(selfRow);

                    // CONTAINS_ROUTINE edge: Package → Routine.
                    // Not rendered as an arrow (SUPPRESSED_EDGES) but used by the
                    // frontend transform to build compound group layout.
                    if (finalIsPackage && !pkgNodeId.isEmpty()) {
                        var crRow = new HashMap<String, Object>();
                        crRow.put("srcId",        pkgNodeId);
                        crRow.put("srcLabel",     pkgNodeName);
                        crRow.put("srcType",      "DaliPackage");
                        crRow.put("srcScope",     pkgNodeScope);
                        crRow.put("srcPackage",   "");
                        crRow.put("srcKind",      "");
                        crRow.put("tgtId",        rId);
                        crRow.put("tgtLabel",     rLabel);
                        crRow.put("tgtScope",     "");
                        crRow.put("tgtType",      "DaliRoutine");
                        crRow.put("edgeType",     "CONTAINS_ROUTINE");
                        crRow.put("sourceHandle", "");
                        crRow.put("targetHandle", "");
                        flatRows.add(crRow);
                    }

                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> reads = (List<Map<String, Object>>) row.get("reads");
                    if (reads != null) {
                        for (var t : reads) {
                            if (t == null) continue;
                            var r = makeRoutineTableRow(rId, rLabel, t, "READS_FROM");
                            r.put("srcScope",   rSchema);
                            r.put("srcPackage", rPkg);
                            r.put("srcKind",    rKind);
                            flatRows.add(r);
                        }
                    }
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> writes = (List<Map<String, Object>>) row.get("writes");
                    if (writes != null) {
                        for (var t : writes) {
                            if (t == null) continue;
                            var w = makeRoutineTableRow(rId, rLabel, t, "WRITES_TO");
                            w.put("srcScope",   rSchema);
                            w.put("srcPackage", rPkg);
                            w.put("srcKind",    rKind);
                            flatRows.add(w);
                        }
                    }
                }

                // CALLS rows are already in buildResult column format — add directly.
                // They contain both the caller (as srcId/srcLabel/srcType/srcScope/srcPackage/srcKind)
                // and the callee (as tgtId/tgtLabel/tgtType/tgtScope) so both nodes are registered.
                flatRows.addAll(callRows);

                return buildResult(flatRows, finalScopeName, finalIsPackage ? "DaliPackage" : "DaliSchema");
            })
            .flatMap(this::enrichDataSource);
    }

    // ── Routine scope (L3 drill from L2 AGG into a specific routine) ─────────
    /**
     * Focused L3 view for a single DaliRoutine node.
     *
     * Returns only:
     *   - Root DaliStatement nodes (parent_statement = '') of this routine
     *   - DaliTable nodes reachable from those statements (READS_FROM / WRITES_TO),
     *     including tables only reachable via sub-statement hoist (CHILD_OF path)
     *   - DaliRecord + DaliRecordField (structural records: BULK COLLECT targets, %ROWTYPE)
     *
     * Intentionally EXCLUDES: DaliParameter, DaliVariable, DaliOutputColumn,
     * DaliAffectedColumn, DaliAtom, non-root sub-statement nodes.
     * Column detail arrives via the separate stmtColumns enrichment pass.
     */
    @SuppressWarnings("unchecked")
    public Uni<ExploreResult> exploreRoutineScope(String routineRid) {
        Map<String, Object> params = Map.of("rid", routineRid);

        // Q1: Root statements — registered as standalone nodes (self-loop NODE_ONLY).
        //     The routine itself is NOT included as a canvas node — the breadcrumb
        //     already shows the routine context. Statements appear as top-level nodes
        //     connected to tables via READS_FROM / WRITES_TO, which ELK can lay out cleanly.
        String stmtsQ = """
            MATCH (r:DaliRoutine) WHERE id(r) = $rid
            MATCH (r)-[:CONTAINS_STMT]->(s:DaliStatement)
            WHERE coalesce(s.parent_statement, '') = ''
            RETURN id(s) AS srcId,
                   coalesce(s.stmt_geoid, s.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   '' AS srcScope, '' AS srcPackage, '' AS srcKind,
                   id(s) AS tgtId,
                   coalesce(s.stmt_geoid, s.snippet, '') AS tgtLabel,
                   'DaliStatement' AS tgtType, '' AS tgtScope,
                   'NODE_ONLY' AS edgeType, '' AS sourceHandle, '' AS targetHandle,
                   '' AS tgtDataType
            LIMIT 300
            """;

        // Q2: Direct READS_FROM from root stmts to tables
        String directReadsQ = """
            MATCH (r:DaliRoutine) WHERE id(r) = $rid
            MATCH (r)-[:CONTAINS_STMT]->(s:DaliStatement)
            WHERE coalesce(s.parent_statement, '') = ''
            MATCH (s)-[:READS_FROM]->(t:DaliTable)
            RETURN id(s) AS srcId,
                   coalesce(s.stmt_geoid, s.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   '' AS srcScope, '' AS srcPackage, '' AS srcKind,
                   id(t) AS tgtId, coalesce(t.table_name, '') AS tgtLabel,
                   'DaliTable' AS tgtType, coalesce(t.schema_geoid, '') AS tgtScope,
                   'READS_FROM' AS edgeType, '' AS sourceHandle, '' AS targetHandle, '' AS tgtDataType
            LIMIT 500
            """;

        // Q3: Direct WRITES_TO from root stmts to tables
        String directWritesQ = """
            MATCH (r:DaliRoutine) WHERE id(r) = $rid
            MATCH (r)-[:CONTAINS_STMT]->(s:DaliStatement)
            WHERE coalesce(s.parent_statement, '') = ''
            MATCH (s)-[:WRITES_TO]->(t:DaliTable)
            RETURN id(s) AS srcId,
                   coalesce(s.stmt_geoid, s.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   '' AS srcScope, '' AS srcPackage, '' AS srcKind,
                   id(t) AS tgtId, coalesce(t.table_name, '') AS tgtLabel,
                   'DaliTable' AS tgtType, coalesce(t.schema_geoid, '') AS tgtScope,
                   'WRITES_TO' AS edgeType, '' AS sourceHandle, '' AS targetHandle, '' AS tgtDataType
            LIMIT 500
            """;

        // Q4: Hoisted READS_FROM from sub-statements to their root stmt.
        //     Edges appear as root_stmt → table, so no sub-stmt nodes leak into the graph.
        //     CHILD_OF*1..20: sub-statements point CHILD_OF back toward root.
        String hoistReadsQ = """
            MATCH (r:DaliRoutine) WHERE id(r) = $rid
            MATCH (r)-[:CONTAINS_STMT]->(root:DaliStatement)
            WHERE coalesce(root.parent_statement, '') = ''
            MATCH (root)<-[:CHILD_OF*1..20]-(sub:DaliStatement)-[:READS_FROM]->(t:DaliTable)
            RETURN DISTINCT id(root) AS srcId,
                   coalesce(root.stmt_geoid, root.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   '' AS srcScope, '' AS srcPackage, '' AS srcKind,
                   id(t) AS tgtId, coalesce(t.table_name, '') AS tgtLabel,
                   'DaliTable' AS tgtType, coalesce(t.schema_geoid, '') AS tgtScope,
                   'READS_FROM' AS edgeType, '' AS sourceHandle, '' AS targetHandle, '' AS tgtDataType
            LIMIT 1000
            """;

        // Q5a: Record self-loops — ensures records appear even if they have no fields.
        //      srcId = tgtId = recId, edgeType = NODE_ONLY (suppressed on canvas).
        String recordSelfQ = """
            MATCH (r:DaliRoutine) WHERE id(r) = $rid
            MATCH (rec:DaliRecord) WHERE rec.routine_geoid = r.routine_geoid
            RETURN id(rec) AS srcId, coalesce(rec.record_name, '') AS srcLabel, 'DaliRecord' AS srcType,
                   '' AS srcScope, '' AS srcPackage, '' AS srcKind,
                   id(rec) AS tgtId, coalesce(rec.record_name, '') AS tgtLabel,
                   'DaliRecord' AS tgtType, '' AS tgtScope,
                   'NODE_ONLY' AS edgeType, '' AS sourceHandle, '' AS targetHandle, '' AS tgtDataType
            LIMIT 200
            """;

        // Q5b: HAS_RECORD_FIELD edges (rec → field) — fields embed inside RecordNode as rows.
        //      tgtDataType populated so frontend fieldsByRecord map gets data_type.
        String recordFieldsQ = """
            MATCH (r:DaliRoutine) WHERE id(r) = $rid
            MATCH (rec:DaliRecord) WHERE rec.routine_geoid = r.routine_geoid
            MATCH (rec)-[:HAS_RECORD_FIELD]->(f:DaliRecordField)
            RETURN id(rec) AS srcId, coalesce(rec.record_name, '') AS srcLabel, 'DaliRecord' AS srcType,
                   '' AS srcScope, '' AS srcPackage, '' AS srcKind,
                   id(f) AS tgtId, coalesce(f.field_name, '') AS tgtLabel,
                   'DaliRecordField' AS tgtType, '' AS tgtScope,
                   'HAS_RECORD_FIELD' AS edgeType, '' AS sourceHandle, '' AS targetHandle,
                   coalesce(f.data_type, '') AS tgtDataType
            LIMIT 1000
            """;

        // Q6: BULK_COLLECTS_INTO — cursor SELECT → DaliRecord.
        //     Connects the statement that drives a BULK COLLECT to the record variable it fills.
        String bulkCollectsQ = """
            MATCH (r:DaliRoutine) WHERE id(r) = $rid
            MATCH (r)-[:CONTAINS_STMT]->(s:DaliStatement)
            WHERE coalesce(s.parent_statement, '') = ''
            MATCH (s)-[:BULK_COLLECTS_INTO]->(rec:DaliRecord)
            RETURN id(s) AS srcId, coalesce(s.stmt_geoid, s.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   '' AS srcScope, '' AS srcPackage, '' AS srcKind,
                   id(rec) AS tgtId, coalesce(rec.record_name, '') AS tgtLabel,
                   'DaliRecord' AS tgtType, '' AS tgtScope,
                   'BULK_COLLECTS_INTO' AS edgeType, '' AS sourceHandle, '' AS targetHandle, '' AS tgtDataType
            LIMIT 500
            """;

        // Q7: RETURNS_INTO — Statement → DaliRecord (RETURNING INTO <record>).
        //     Only edges targeting DaliRecord are useful at L3; field-level targets are embedded
        //     inside RecordNode and filtered from canvas nodes by the frontend.
        String returnsIntoQ = """
            MATCH (r:DaliRoutine) WHERE id(r) = $rid
            MATCH (r)-[:CONTAINS_STMT]->(s:DaliStatement)
            WHERE coalesce(s.parent_statement, '') = ''
            MATCH (s)-[:RETURNS_INTO]->(rec:DaliRecord)
            RETURN id(s) AS srcId, coalesce(s.stmt_geoid, s.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   '' AS srcScope, '' AS srcPackage, '' AS srcKind,
                   id(rec) AS tgtId, coalesce(rec.record_name, '') AS tgtLabel,
                   'DaliRecord' AS tgtType, '' AS tgtScope,
                   'RETURNS_INTO' AS edgeType, '' AS sourceHandle, '' AS targetHandle, '' AS tgtDataType
            LIMIT 500
            """;

        // Q8: RECORD_USED_IN — DaliRecord → DaliStatement (record consumed in INSERT/MERGE etc.)
        String recordUsedInQ = """
            MATCH (r:DaliRoutine) WHERE id(r) = $rid
            MATCH (rec:DaliRecord) WHERE rec.routine_geoid = r.routine_geoid
            MATCH (rec)-[:RECORD_USED_IN]->(s:DaliStatement)
            WHERE coalesce(s.parent_statement, '') = ''
            RETURN id(rec) AS srcId, coalesce(rec.record_name, '') AS srcLabel, 'DaliRecord' AS srcType,
                   '' AS srcScope, '' AS srcPackage, '' AS srcKind,
                   id(s) AS tgtId, coalesce(s.stmt_geoid, s.snippet, '') AS tgtLabel,
                   'DaliStatement' AS tgtType, '' AS tgtScope,
                   'RECORD_USED_IN' AS edgeType, '' AS sourceHandle, '' AS targetHandle, '' AS tgtDataType
            LIMIT 500
            """;

        return Uni.combine().all()
            .unis(List.of(
                arcade.cypher(stmtsQ,        params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(directReadsQ,  params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(directWritesQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(hoistReadsQ,   params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(recordSelfQ,   params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(recordFieldsQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(bulkCollectsQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(returnsIntoQ,  params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(recordUsedInQ, params).onFailure().recoverWithItem(List.of())
            ))
            .combinedWith(results -> {
                var all = new ArrayList<Map<String, Object>>();
                for (Object raw : results)
                    all.addAll((List<Map<String, Object>>) raw);
                return buildResult(all, routineRid, "DaliStatement");
            })
            .flatMap(this::enrichDataSource);
    }

    // ── Routine detail ────────────────────────────────────────────────────────
    /**
     * Inspector data for a single DaliRoutine node.
     * Returns ExploreResult with:
     *   - DaliParameter nodes (HAS_PARAMETER edges) with meta.dataType
     *   - DaliVariable  nodes (any edge, type=DaliVariable)  with meta.dataType
     *   - DaliStatement nodes (CONTAINS_STMT, root-level only — no parent_statement)
     *   - CALLS edges in both directions (src=routine→callee, src=caller→routine)
     *
     * Used by the LOOM right-side inspector when a Routine node is selected.
     * Zero canvas impact — result is not added to any graph.
     */
    @SuppressWarnings("unchecked")
    public Uni<ExploreResult> exploreRoutineDetail(String nodeId) {
        Map<String, Object> params = Map.of("rid", nodeId);

        // 1. Parameters — via any edge, filtered to DaliParameter instances.
        //    tgtDataType → populates meta.dataType in buildResult.
        String paramsQ = """
            MATCH (n:DaliRoutine) WHERE id(n) = $rid
            MATCH (n)-[r]->(p:DaliParameter)
            RETURN id(n) AS srcId, coalesce(n.routine_name, '') AS srcLabel, 'DaliRoutine' AS srcType,
                   '' AS srcScope, '' AS srcPackage, coalesce(n.routine_type, '') AS srcKind,
                   id(p) AS tgtId, coalesce(p.param_name, p.name, '') AS tgtLabel,
                   'DaliParameter' AS tgtType, '' AS tgtScope,
                   type(r) AS edgeType, '' AS sourceHandle, '' AS targetHandle,
                   coalesce(p.data_type, '') AS tgtDataType
            LIMIT 100
            """;

        // 2. Variables — matched by routine_geoid property (same approach as DaliRecord)
        //    to avoid dependency on HAS_VARIABLE edge existence in every parsed session.
        String varsQ = """
            MATCH (n:DaliRoutine) WHERE id(n) = $rid
            MATCH (v:DaliVariable) WHERE v.routine_geoid = n.routine_geoid
            RETURN id(n) AS srcId, coalesce(n.routine_name, '') AS srcLabel, 'DaliRoutine' AS srcType,
                   '' AS srcScope, '' AS srcPackage, coalesce(n.routine_type, '') AS srcKind,
                   id(v) AS tgtId, coalesce(v.var_name, v.name, v.variable_name, '') AS tgtLabel,
                   'DaliVariable' AS tgtType, '' AS tgtScope,
                   'HAS_VARIABLE' AS edgeType, '' AS sourceHandle, '' AS targetHandle,
                   coalesce(v.data_type, '') AS tgtDataType
            LIMIT 200
            """;

        // 3. Root statements only (parent_statement = '' means not a subquery).
        String stmtsQ = """
            MATCH (n:DaliRoutine) WHERE id(n) = $rid
            MATCH (n)-[:CONTAINS_STMT]->(s:DaliStatement)
            WHERE coalesce(s.parent_statement, '') = ''
            RETURN id(n) AS srcId, coalesce(n.routine_name, '') AS srcLabel, 'DaliRoutine' AS srcType,
                   '' AS srcScope, '' AS srcPackage, coalesce(n.routine_type, '') AS srcKind,
                   id(s) AS tgtId, coalesce(s.stmt_geoid, s.snippet, '') AS tgtLabel,
                   'DaliStatement' AS tgtType, '' AS tgtScope,
                   'CONTAINS_STMT' AS edgeType, '' AS sourceHandle, '' AS targetHandle, '' AS tgtDataType
            LIMIT 200
            """;

        // 4. CALLS outgoing (this routine → callees).
        //    srcId = inspected routine, tgtId = callee → frontend filters edge.source === nodeId.
        String callsOutQ = """
            MATCH (n:DaliRoutine) WHERE id(n) = $rid
            MATCH (n)-[:CALLS]->(r2:DaliRoutine)
            RETURN id(n) AS srcId, coalesce(n.routine_name, '') AS srcLabel, 'DaliRoutine' AS srcType,
                   coalesce(n.schema_geoid, '') AS srcScope, coalesce(n.package_geoid, '') AS srcPackage,
                   coalesce(n.routine_type, '') AS srcKind,
                   id(r2) AS tgtId, coalesce(r2.routine_name, '') AS tgtLabel,
                   'DaliRoutine' AS tgtType, coalesce(r2.schema_geoid, '') AS tgtScope,
                   'CALLS' AS edgeType, '' AS sourceHandle, '' AS targetHandle, '' AS tgtDataType
            LIMIT 50
            """;

        // 5. CALLS incoming (callers → this routine).
        //    srcId = caller, tgtId = inspected routine → frontend filters edge.target === nodeId.
        String callsInQ = """
            MATCH (n:DaliRoutine) WHERE id(n) = $rid
            MATCH (r1:DaliRoutine)-[:CALLS]->(n)
            RETURN id(r1) AS srcId, coalesce(r1.routine_name, '') AS srcLabel, 'DaliRoutine' AS srcType,
                   coalesce(r1.schema_geoid, '') AS srcScope, coalesce(r1.package_geoid, '') AS srcPackage,
                   coalesce(r1.routine_type, '') AS srcKind,
                   id(n) AS tgtId, coalesce(n.routine_name, '') AS tgtLabel,
                   'DaliRoutine' AS tgtType, coalesce(n.schema_geoid, '') AS tgtScope,
                   'CALLS' AS edgeType, '' AS sourceHandle, '' AS targetHandle, '' AS tgtDataType
            LIMIT 50
            """;

        return Uni.combine().all()
            .unis(List.of(
                arcade.cypher(paramsQ,    params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(varsQ,      params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(stmtsQ,     params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(callsOutQ,  params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(callsInQ,   params).onFailure().recoverWithItem(List.of())
            ))
            .combinedWith(results -> {
                var all = new ArrayList<Map<String, Object>>();
                for (Object raw : results)
                    all.addAll((List<Map<String, Object>>) raw);
                return buildResult(all, nodeId, "DaliRoutine");
            });
    }

    /** Helper: build one (routine → table) row in the uniform format buildResult() expects. */
    private static Map<String, Object> makeRoutineTableRow(String rId, String rLabel,
                                                           Map<String, Object> t, String edgeType) {
        var row = new HashMap<String, Object>();
        row.put("srcId",       rId);
        row.put("srcLabel",    rLabel);
        row.put("srcType",     "DaliRoutine");
        row.put("tgtId",       str(t, "@rid"));
        row.put("tgtLabel",    str(t, "table_name"));
        row.put("tgtScope",    str(t, "schema_geoid"));
        row.put("tgtType",     "DaliTable");
        row.put("edgeType",    edgeType);
        row.put("sourceHandle", "");
        row.put("targetHandle", "");
        return row;
    }

    // ── L4: Statement subquery tree ───────────────────────────────────────────

    /**
     * Phase S2.5 — single-statement drill view.
     *
     * Returns the root DaliStatement + all descendant sub-statements reachable
     * via {@code CHILD_OF} (up to 30 levels), plus:
     * <ul>
     *   <li>READS_FROM edges for every stmt in the tree → DaliTable nodes</li>
     *   <li>HAS_OUTPUT_COL edges for every stmt → DaliOutputColumn nodes</li>
     *   <li>DATA_FLOW edges from DaliOutputColumn to downstream OutputColumn /
     *       AffectedColumn (column-to-column flow within the subquery tree)</li>
     * </ul>
     *
     * All queries are independent Uni chains merged in Java so one missing edge
     * type in the DB never aborts the entire response.
     *
     * @param stmtId @rid of the root DaliStatement to drill into
     */
    @SuppressWarnings("unchecked")
    public Uni<ExploreResult> exploreStatementTree(String stmtId) {
        if (stmtId == null || stmtId.isBlank()) {
            return Uni.createFrom().item(new ExploreResult(List.of(), List.of(), false));
        }
        Map<String, Object> params = Map.of("stmtId", stmtId);

        // 1. Root statement self-node (ensures it renders even with no children)
        String rootQ = """
            MATCH (root:DaliStatement)
            WHERE id(root) = $stmtId
            RETURN id(root) AS srcId,
                   coalesce(root.stmt_geoid, root.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(root) AS tgtId,
                   coalesce(root.stmt_geoid, root.snippet, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'NODE_ONLY' AS edgeType
            """;

        // 2. All descendant sub-statements; returned as parent→child (root srcId, sub tgtId)
        //    so the frontend renders them as children of the root.
        String childQ = """
            MATCH (root:DaliStatement)
            WHERE id(root) = $stmtId
            MATCH (sub:DaliStatement)-[:CHILD_OF*1..30]->(root)
            RETURN id(root) AS srcId,
                   coalesce(root.stmt_geoid, root.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(sub) AS tgtId,
                   coalesce(sub.stmt_geoid, sub.snippet, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'CHILD_OF' AS edgeType
            LIMIT 100
            """;

        // 3. READS_FROM for root + all descendants
        String readsQ = """
            MATCH (root:DaliStatement)
            WHERE id(root) = $stmtId
            MATCH (sub:DaliStatement)-[:CHILD_OF*0..30]->(root)
            MATCH (sub)-[:READS_FROM]->(t:DaliTable)
            RETURN DISTINCT id(sub) AS srcId,
                   coalesce(sub.stmt_geoid, sub.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 200
            """;

        // 4. HAS_OUTPUT_COL for root + all descendants
        String outColQ = """
            MATCH (root:DaliStatement)
            WHERE id(root) = $stmtId
            MATCH (sub:DaliStatement)-[:CHILD_OF*0..30]->(root)
            MATCH (sub)-[:HAS_OUTPUT_COL]->(oc:DaliOutputColumn)
            RETURN id(sub) AS srcId,
                   coalesce(sub.stmt_geoid, sub.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(oc) AS tgtId, coalesce(oc.name, oc.col_key, '') AS tgtLabel, '' AS tgtScope,
                   'DaliOutputColumn' AS tgtType, 'HAS_OUTPUT_COL' AS edgeType
            LIMIT 500
            """;

        // 5. DATA_FLOW: DaliOutputColumn → downstream OutputColumn/AffectedColumn
        //    Shows column-level flow within the subquery tree.
        String dataFlowQ = """
            MATCH (root:DaliStatement)
            WHERE id(root) = $stmtId
            MATCH (sub:DaliStatement)-[:CHILD_OF*0..30]->(root)
            MATCH (sub)-[:HAS_OUTPUT_COL]->(oc:DaliOutputColumn)-[df:DATA_FLOW]->(target)
            RETURN id(oc) AS srcId,
                   coalesce(oc.name, oc.col_key, '') AS srcLabel, 'DaliOutputColumn' AS srcType,
                   id(target) AS tgtId,
                   coalesce(target.name, target.col_key, target.column_name, '') AS tgtLabel, '' AS tgtScope,
                   labels(target)[0] AS tgtType, 'DATA_FLOW' AS edgeType
            LIMIT 500
            """;

        return Uni.combine().all()
            .unis(List.of(
                arcade.cypher(rootQ,      params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(childQ,     params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(readsQ,     params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(outColQ,    params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(dataFlowQ,  params).onFailure().recoverWithItem(List.of())
            ))
            .combinedWith(results -> {
                var all = new ArrayList<Map<String, Object>>();
                for (Object raw : results)
                    all.addAll((List<Map<String, Object>>) raw);
                return buildResult(all, stmtId, "DaliStatement");
            })
            .flatMap(this::enrichDataSource);
    }

    // ── L2: DaliRecord support — package scope ────────────────────────────────

    /**
     * Returns DaliRecord nodes owned by routines inside {@code packageName} plus
     * their DaliRecordField children (via HAS_RECORD_FIELD edges) and
     * RETURNS_INTO edges from statements to record fields.
     *
     * Called after {@link #explorePackage} so the results are merged server-side.
     * Returns an empty ExploreResult if no records exist (safe to combine).
     */
    @SuppressWarnings("unchecked")
    Uni<ExploreResult> explorePackageRecords(String packageName) {
        Map<String, Object> params = Map.of("pkg", packageName);

        // DaliRecord nodes owned by routines in this package
        String recNodeQ = """
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(r:DaliRoutine)
            MATCH (rec:DaliRecord)
            WHERE rec.routine_geoid = r.routine_geoid
            RETURN id(r) AS srcId, r.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
                   id(rec) AS tgtId, coalesce(rec.record_name, '') AS tgtLabel, '' AS tgtScope,
                   'DaliRecord' AS tgtType, 'CONTAINS_RECORD' AS edgeType
            LIMIT 300
            """;

        // DaliRecordField children (HAS_RECORD_FIELD edges)
        String recFieldQ = """
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(r:DaliRoutine)
            MATCH (rec:DaliRecord)
            WHERE rec.routine_geoid = r.routine_geoid
            MATCH (rec)-[:HAS_RECORD_FIELD]->(f:DaliRecordField)
            RETURN id(rec) AS srcId, coalesce(rec.record_name, '') AS srcLabel, 'DaliRecord' AS srcType,
                   id(f) AS tgtId, coalesce(f.field_name, '') AS tgtLabel, '' AS tgtScope,
                   'DaliRecordField' AS tgtType, 'HAS_RECORD_FIELD' AS edgeType
            LIMIT 2000
            """;

        // RETURNS_INTO edges: statement → record-field / record / variable / parameter
        String returnsIntoQ = """
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(:DaliRoutine)
                  -[:CONTAINS_STMT]->(stmt:DaliStatement)-[:RETURNS_INTO]->(target)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(stmt) AS srcId,
                   coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(target) AS tgtId,
                   coalesce(target.field_name, target.record_name, target.variable_name,
                            target.parameter_name, '') AS tgtLabel, '' AS tgtScope,
                   labels(target)[0] AS tgtType, 'RETURNS_INTO' AS edgeType
            LIMIT 500
            """;

        return Uni.combine().all()
            .unis(List.of(
                arcade.cypher(recNodeQ,      params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(recFieldQ,     params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(returnsIntoQ,  params).onFailure().recoverWithItem(List.of())
            ))
            .combinedWith(results -> {
                var all = new ArrayList<Map<String, Object>>();
                for (Object raw : results)
                    all.addAll((List<Map<String, Object>>) raw);
                return buildResult(all, packageName, "DaliPackage");
            });
    }

    // ── Database scope (all schemas in a DB) ─────────────────────────────────

    /**
     * Explores all schemas within a database.
     * Equivalent to running exploreSchema for every DaliSchema where db_name = $dbName,
     * but in a single set of queries.
     *
     * LIMITs mirror exploreSchema — DB-level aggregation across multiple schemas
     * can produce 8000+ unbounded rows (verified: ODS 10 schemas → 4476 CHILD_OF
     * hoisted reads alone). The frontend ELK layout and browser cannot handle this.
     *
     * The frontend transformSchemaExplore handles multi-schema results by walking
     * all DaliSchema nodes found in the result.
     */
    private Uni<ExploreResult> exploreByDatabase(String dbName) {
        String cypher = """
            MATCH (s:DaliSchema)
            WHERE s.db_name = $dbName
            MATCH (s)-[:CONTAINS_TABLE]->(t:DaliTable)
            RETURN id(s) AS srcId, s.schema_name AS srcLabel, 'DaliSchema' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'CONTAINS_TABLE' AS edgeType
            LIMIT 500
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.db_name = $dbName
            MATCH (s)-[:CONTAINS_ROUTINE]->(child)
            WHERE child:DaliPackage OR child:DaliRoutine
            RETURN id(s) AS srcId, s.schema_name AS srcLabel, 'DaliSchema' AS srcType,
                   id(child) AS tgtId,
                   coalesce(child.package_name, child.routine_name, '') AS tgtLabel,
                   '' AS tgtScope, labels(child)[0] AS tgtType, 'CONTAINS_ROUTINE' AS edgeType
            LIMIT 300
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.db_name = $dbName
            MATCH (s)-[:CONTAINS_ROUTINE]->(r:DaliRoutine)-[:CONTAINS_STMT]->(stmt:DaliStatement)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(r) AS srcId, r.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
                   id(stmt) AS tgtId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'CONTAINS_STMT' AS edgeType
            LIMIT 500
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.db_name = $dbName
            MATCH (s)-[:CONTAINS_ROUTINE]->(pkg:DaliPackage)-[:CONTAINS_ROUTINE]->(r:DaliRoutine)
            RETURN DISTINCT id(pkg) AS srcId, pkg.package_name AS srcLabel, 'DaliPackage' AS srcType,
                   id(r) AS tgtId, r.routine_name AS tgtLabel, '' AS tgtScope,
                   'DaliRoutine' AS tgtType, 'CONTAINS_ROUTINE' AS edgeType
            LIMIT 300
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.db_name = $dbName
            MATCH (s)-[:CONTAINS_ROUTINE]->(:DaliPackage)-[:CONTAINS_ROUTINE]->(r:DaliRoutine)
                  -[:CONTAINS_STMT]->(stmt:DaliStatement)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(r) AS srcId, r.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
                   id(stmt) AS tgtId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'CONTAINS_STMT' AS edgeType
            LIMIT 500
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.db_name = $dbName
            MATCH (s)-[:CONTAINS_TABLE]->(t:DaliTable)<-[:WRITES_TO]-(stmt:DaliStatement)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'WRITES_TO' AS edgeType
            LIMIT 3000
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.db_name = $dbName
            MATCH (s)-[:CONTAINS_TABLE]->(t:DaliTable)<-[:READS_FROM]-(stmt:DaliStatement)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 3000
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.db_name = $dbName
            MATCH (s)-[:CONTAINS_TABLE]->(t:DaliTable)
                  <-[:READS_FROM]-(:DaliStatement)-[:CHILD_OF*1..30]->(rootStmt:DaliStatement)
            WHERE coalesce(rootStmt.parent_statement, '') = ''
            RETURN DISTINCT id(rootStmt) AS srcId, coalesce(rootStmt.stmt_geoid, rootStmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 500
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.db_name = $dbName
            MATCH (s)-[:CONTAINS_TABLE]->(t:DaliTable)
                  <-[:WRITES_TO]-(:DaliStatement)-[:CHILD_OF*1..30]->(rootStmt:DaliStatement)
            WHERE coalesce(rootStmt.parent_statement, '') = ''
            RETURN DISTINCT id(rootStmt) AS srcId, coalesce(rootStmt.stmt_geoid, rootStmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'WRITES_TO' AS edgeType
            LIMIT 500
            """;

        return arcade.cypher(cypher, Map.of("dbName", dbName))
            .map(rows -> buildResult(rows, dbName, "DaliDatabase"))
            .flatMap(this::enrichDataSource);
    }

    // ── Statement columns (second-pass enrichment) ───────────────────────────

    /**
     * Bulk-fetch all column edges for a mixed list of rendered node @rids.
     *
     * Accepts both DaliTable and DaliStatement @rids in a single call:
     *   - DaliTable     → DaliColumn        (via table_geoid property — no HAS_COLUMN edges in DB)
     *   - DaliStatement → HAS_OUTPUT_COL   → DaliOutputColumn
     *   - DaliStatement → HAS_AFFECTED_COL → DaliAffectedColumn
     *
     * BUG-VC-003: The original UNION ALL used id(t) IN $ids (ArcadeDB LINK vs String
     * type-mismatch) and also relied on HAS_COLUMN edges which do not exist in the DB.
     * DaliColumn rows are keyed by table_geoid property — same path as KnotService.
     *
     * BUG-VC-004: Added enrichDataSource so DaliTable nodes get dataSource populated.
     *
     * ArcadeDB Cypher note: chained MATCH clauses (MATCH…WHERE…MATCH) do not work —
     * the second MATCH returns empty. Use UNWIND + WHERE id(t)=rid + WITH + second MATCH.
     *
     * Three parallel Uni queries replace the UNION ALL. Each branch recovers on failure
     * so a missing edge type in the DB never aborts the other two branches.
     */
    @SuppressWarnings("unchecked")
    public Uni<ExploreResult> exploreStmtColumns(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Uni.createFrom().item(new ExploreResult(List.of(), List.of(), false));
        }
        Map<String, Object> params = Map.of("ids", ids);

        // DaliTable → DaliColumn via HAS_COLUMN edges (created by Hound parser).
        // UNWIND + WITH separates the two MATCH clauses — required by ArcadeDB Cypher.
        // Edge traversal is index-based (O(degree)) vs property scan (O(|DaliColumn|)).
        // No LIMIT — result is naturally bounded by ids.size() × columns_per_table.
        // COUNT query runs in parallel and warns if the expected result set is large.
        String countColQ = """
            UNWIND $ids AS rid
            MATCH (t:DaliTable)
            WHERE id(t) = rid
            WITH t
            MATCH (t)-[:HAS_COLUMN]->(col:DaliColumn)
            RETURN count(col) AS total
            """;

        String hasColQ = """
            UNWIND $ids AS rid
            MATCH (t:DaliTable)
            WHERE id(t) = rid
            WITH t
            MATCH (t)-[:HAS_COLUMN]->(col:DaliColumn)
            RETURN id(t) AS srcId, t.table_name AS srcLabel, 'DaliTable' AS srcType,
                   id(col) AS tgtId, coalesce(col.column_name, '') AS tgtLabel, '' AS tgtScope,
                   'DaliColumn' AS tgtType, 'HAS_COLUMN' AS edgeType,
                   toString(coalesce(col.is_pk,       false)) AS tgtPk,
                   toString(coalesce(col.is_fk,       false)) AS tgtFk,
                   toString(coalesce(col.is_required, false)) AS tgtReq,
                   coalesce(col.data_type, '')                AS tgtDataType
            """;

        String hasOutColQ = """
            UNWIND $ids AS rid
            MATCH (stmt:DaliStatement)
            WHERE id(stmt) = rid
            WITH stmt
            MATCH (stmt)-[:HAS_OUTPUT_COL]->(col:DaliOutputColumn)
            RETURN id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel,
                   'DaliStatement' AS srcType,
                   id(col) AS tgtId, coalesce(col.name, col.col_key, '') AS tgtLabel, '' AS tgtScope,
                   'DaliOutputColumn' AS tgtType, 'HAS_OUTPUT_COL' AS edgeType
            """;

        String hasAffColQ = """
            UNWIND $ids AS rid
            MATCH (stmt:DaliStatement)
            WHERE id(stmt) = rid
            WITH stmt
            MATCH (stmt)-[:HAS_AFFECTED_COL]->(col:DaliAffectedColumn)
            RETURN id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel,
                   'DaliStatement' AS srcType,
                   id(col) AS tgtId, coalesce(col.column_name, '') AS tgtLabel, '' AS tgtScope,
                   'DaliAffectedColumn' AS tgtType, 'HAS_AFFECTED_COL' AS edgeType
            """;

        // Run COUNT and data queries in parallel. COUNT result is used only for
        // a warning log — it does not gate the data fetch.
        @SuppressWarnings("unchecked")
        Uni<List<Map<String, Object>>> countUni = arcade.cypher(countColQ, params)
            .invoke(rows -> {
                if (rows != null && !rows.isEmpty()) {
                    Object total = rows.get(0).get("total");
                    long cnt = total instanceof Number ? ((Number) total).longValue() : 0L;
                    log.debugf("stmtColumns: %d ids → %d DaliColumn rows expected", ids.size(), cnt);
                    if (cnt > 10_000) {
                        log.warnf("stmtColumns: HIGH column count %d for %d ids " +
                                  "— consider pagination or scope narrowing", cnt, ids.size());
                    }
                }
            })
            .onFailure().recoverWithItem(List.of());

        return Uni.combine().all()
            .unis(List.of(
                countUni,
                arcade.cypher(hasColQ,    params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(hasOutColQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(hasAffColQ, params).onFailure().recoverWithItem(List.of())
            ))
            .combinedWith(results -> {
                // results[0] = countUni (ignored in data merge, used only for log)
                var all = new java.util.ArrayList<Map<String, Object>>();
                for (int i = 1; i < results.size(); i++)
                    all.addAll((List<Map<String, Object>>) results.get(i));
                return buildResult(all, "", "DaliTable");
            })
            .flatMap(this::enrichDataSource);
    }

    // ── Package scope ─────────────────────────────────────────────────────────

    private Uni<ExploreResult> explorePackage(String packageName) {
        // Confirmed against hound DB (2026-04-04):
        //   ROUTINE_USES_TABLE = 0 edges (not populated).
        //   Tables accessed via CONTAINS_STMT → READS_FROM/WRITES_TO paths.
        //
        // Branch 1: routines owned by the package.
        // Branch 2: root statements inside routines (CONTAINS_STMT).
        // Branch 3: statement → table READS_FROM (root stmts only).
        // Branch 3b: subquery READS_FROM hoisted to root statement.
        //   rootStmt -[:CONTAINS_STMT]-> subStmt -[:READS_FROM]-> table
        //   subStmt is never added to the result (not in srcId/tgtId).
        //   buildResult deduplicates via srcId__READS_FROM__tgtId.
        // Branch 4: statement → table WRITES_TO (root stmts only).
        // Branch 5: statement → output column (HAS_OUTPUT_COL, inline card data).
        String cypher = """
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(r:DaliRoutine)
            RETURN id(p) AS srcId, p.package_name AS srcLabel, 'DaliPackage' AS srcType,
                   id(r) AS tgtId, r.routine_name AS tgtLabel, r.package_geoid AS tgtScope,
                   'DaliRoutine' AS tgtType, 'CONTAINS_ROUTINE' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(r:DaliRoutine)-[:CONTAINS_STMT]->(stmt:DaliStatement)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN id(r) AS srcId, r.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
                   id(stmt) AS tgtId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'CONTAINS_STMT' AS edgeType
            LIMIT 300
            UNION ALL
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(:DaliRoutine)
                  -[:CONTAINS_STMT]->(stmt:DaliStatement)-[:READS_FROM]->(t:DaliTable)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(:DaliRoutine)
                  -[:CONTAINS_STMT]->(rootStmt:DaliStatement)<-[:CHILD_OF]-(subStmt:DaliStatement)
                  -[:READS_FROM]->(t:DaliTable)
            WHERE coalesce(rootStmt.parent_statement, '') = ''
            RETURN DISTINCT id(rootStmt) AS srcId, coalesce(rootStmt.stmt_geoid, rootStmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(:DaliRoutine)
                  -[:CONTAINS_STMT]->(stmt:DaliStatement)-[:WRITES_TO]->(t:DaliTable)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'WRITES_TO' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(:DaliRoutine)-[:CONTAINS_STMT]->(stmt:DaliStatement)-[:HAS_OUTPUT_COL]->(col:DaliOutputColumn)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(col) AS tgtId, coalesce(col.name, col.col_key, '') AS tgtLabel, '' AS tgtScope,
                   'DaliOutputColumn' AS tgtType, 'HAS_OUTPUT_COL' AS edgeType
            LIMIT 500
            """;

        // Phase S2.4: merge DaliRecord / DaliRecordField / RETURNS_INTO in parallel
        var baseUni    = arcade.cypher(cypher, Map.of("pkg", packageName));
        var recordsUni = explorePackageRecords(packageName).onFailure().recoverWithItem(
            new ExploreResult(List.of(), List.of(), false));

        return Uni.combine().all().unis(baseUni, recordsUni).asTuple()
            .map(tuple -> {
                var baseRows = tuple.getItem1();
                ExploreResult base    = buildResult(baseRows, packageName, "DaliPackage");
                ExploreResult records = tuple.getItem2();
                // Merge: combine nodes (by id) and edges (by id)
                var nodeMap = new LinkedHashMap<String, GraphNode>();
                base.nodes().forEach(n -> nodeMap.put(n.id(), n));
                records.nodes().forEach(n -> nodeMap.putIfAbsent(n.id(), n));
                var edgeSet = new LinkedHashSet<>(base.edges());
                edgeSet.addAll(records.edges());
                return new ExploreResult(new ArrayList<>(nodeMap.values()),
                    new ArrayList<>(edgeSet), base.hasMore() || records.hasMore());
            })
            .flatMap(this::enrichDataSource);
    }

    // ── RID-based (generic, bidirectional) ───────────────────────────────────

    /**
     * 1-hop bidirectional explore for any node (table, column, statement, routine…).
     *
     * Three parallel queries merged in Java (ArcadeDB UNION ALL collapses List<String>
     * from labels(), so we avoid UNION and combine in Java instead):
     *   1. Outgoing edges: (n)-[r]->(m)
     *   2. Incoming edges: (m)-[r]->(n)  — swap vars so id(n) stays consistent
     *   3. HAS_OUTPUT_COL for any DaliStatement children (inline column data)
     */
    @SuppressWarnings("unchecked")
    private Uni<ExploreResult> exploreByRid(String rid) {
        Map<String, Object> params = Map.of("rid", rid);

        // BUG-VC-001: Exclude DaliConstraint/DaliPrimaryKey/DaliForeignKey — not renderable.
        // Also exclude DaliAtom (50k+ nodes, canvas-irrelevant; linked via HAS_ATOM from stmts).
        String outQ = """
            MATCH (n)-[r]->(m)
            WHERE id(n) = $rid
              AND (NOT m:DaliStatement OR coalesce(m.parent_statement, '') = '')
              AND NOT (m:DaliConstraint OR m:DaliPrimaryKey OR m:DaliForeignKey OR m:DaliAtom)
            RETURN id(n) AS srcId,
                   coalesce(n.schema_name, n.table_name, n.package_name, n.routine_name, n.stmt_geoid, n.column_name, n.name, n.col_key, '') AS srcLabel,
                   labels(n)[0] AS srcType,
                   id(m) AS tgtId,
                   coalesce(m.schema_name, m.table_name, m.package_name, m.routine_name, m.stmt_geoid, m.column_name, m.name, m.col_key, '') AS tgtLabel,
                   m.schema_geoid AS tgtScope, labels(m)[0] AS tgtType, type(r) AS edgeType
            LIMIT 300
            """;

        // BUG-VC-001: Filter incoming constraint edges + DaliAtom sources (ATOM_REF_COLUMN etc).
        String inQ = """
            MATCH (m)-[r]->(n)
            WHERE id(n) = $rid
              AND (NOT m:DaliStatement OR coalesce(m.parent_statement, '') = '')
              AND NOT (m:DaliConstraint OR m:DaliPrimaryKey OR m:DaliForeignKey OR m:DaliAtom)
            RETURN id(m) AS srcId,
                   coalesce(m.schema_name, m.table_name, m.package_name, m.routine_name, m.stmt_geoid, m.column_name, m.name, m.col_key, '') AS srcLabel,
                   labels(m)[0] AS srcType,
                   id(n) AS tgtId,
                   coalesce(n.schema_name, n.table_name, n.package_name, n.routine_name, n.stmt_geoid, n.column_name, n.name, n.col_key, '') AS tgtLabel,
                   n.schema_geoid AS tgtScope, labels(n)[0] AS tgtType, type(r) AS edgeType
            LIMIT 300
            """;

        // Output columns for any DaliStatement children of $rid
        String outColQ = """
            MATCH (n)-[:CONTAINS_STMT]->(stmt:DaliStatement)-[:HAS_OUTPUT_COL]->(col:DaliOutputColumn)
            WHERE id(n) = $rid
            RETURN id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(col) AS tgtId, coalesce(col.name, col.col_key, '') AS tgtLabel, '' AS tgtScope,
                   'DaliOutputColumn' AS tgtType, 'HAS_OUTPUT_COL' AS edgeType
            LIMIT 200
            """;

        // Output columns when $rid IS a DaliStatement (root statement explore)
        String stmtOutColQ = """
            MATCH (n:DaliStatement)-[:HAS_OUTPUT_COL]->(col:DaliOutputColumn)
            WHERE id(n) = $rid
            RETURN id(n) AS srcId, coalesce(n.stmt_geoid, n.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(col) AS tgtId, coalesce(col.name, col.col_key, '') AS tgtLabel, '' AS tgtScope,
                   'DaliOutputColumn' AS tgtType, 'HAS_OUTPUT_COL' AS edgeType
            LIMIT 100
            """;

        // If $rid is a DaliColumn: resolve parent table + ALL its sibling columns (inline display)
        // Returns tgtPk/tgtFk/tgtReq/tgtDataType so buildResult can populate meta for ColumnRows.
        String sibColQ = """
            MATCH (parent)-[:HAS_COLUMN]->(n)
            WHERE id(n) = $rid
            WITH parent
            MATCH (parent)-[:HAS_COLUMN]->(sibling)
            RETURN id(parent) AS srcId, coalesce(parent.table_name, '') AS srcLabel,
                   labels(parent)[0] AS srcType,
                   id(sibling) AS tgtId, coalesce(sibling.column_name, '') AS tgtLabel,
                   '' AS tgtScope, labels(sibling)[0] AS tgtType, 'HAS_COLUMN' AS edgeType,
                   toString(coalesce(sibling.is_pk,       false)) AS tgtPk,
                   toString(coalesce(sibling.is_fk,       false)) AS tgtFk,
                   toString(coalesce(sibling.is_required, false)) AS tgtReq,
                   coalesce(sibling.data_type, '')                AS tgtDataType
            LIMIT 100
            """;

        // If $rid is a DaliOutputColumn: resolve parent statement + ALL its sibling output cols
        String sibOutColQ = """
            MATCH (parent)-[:HAS_OUTPUT_COL]->(n)
            WHERE id(n) = $rid
            WITH parent
            MATCH (parent)-[:HAS_OUTPUT_COL]->(sibling)
            RETURN id(parent) AS srcId, coalesce(parent.stmt_geoid, parent.snippet, '') AS srcLabel,
                   labels(parent)[0] AS srcType,
                   id(sibling) AS tgtId, coalesce(sibling.name, sibling.col_key, '') AS tgtLabel,
                   '' AS tgtScope, labels(sibling)[0] AS tgtType, 'HAS_OUTPUT_COL' AS edgeType
            LIMIT 100
            """;

        // Hoist sub-statement READS_FROM to root INSERT via CHILD_OF*1..30
        // Covers: INSERT reads table through nested subquery (not direct READS_FROM on root)
        String hoistReadsQ = """
            MATCH (sub:DaliStatement)-[:READS_FROM]->(n)
            WHERE id(n) = $rid
              AND coalesce(sub.parent_statement, '') <> ''
            MATCH (sub)-[:CHILD_OF*1..30]->(root:DaliStatement)
            WHERE coalesce(root.parent_statement, '') = ''
            RETURN DISTINCT id(root) AS srcId,
                   coalesce(root.stmt_geoid, root.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(n) AS tgtId,
                   coalesce(n.table_name, '') AS tgtLabel, n.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 200
            """;

        // Hoist sub-statement WRITES_TO to root INSERT via CHILD_OF*1..30
        String hoistWritesQ = """
            MATCH (sub:DaliStatement)-[:WRITES_TO]->(n)
            WHERE id(n) = $rid
              AND coalesce(sub.parent_statement, '') <> ''
            MATCH (sub)-[:CHILD_OF*1..30]->(root:DaliStatement)
            WHERE coalesce(root.parent_statement, '') = ''
            RETURN DISTINCT id(root) AS srcId,
                   coalesce(root.stmt_geoid, root.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(n) AS tgtId,
                   coalesce(n.table_name, '') AS tgtLabel, n.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'WRITES_TO' AS edgeType
            LIMIT 200
            """;

        // Each sub-query recovers independently so one failure doesn't kill the whole explore.
        return Uni.combine().all()
            .unis(List.of(
                arcade.cypher(outQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(inQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(outColQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(sibColQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(sibOutColQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(stmtOutColQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(hoistReadsQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(hoistWritesQ, params).onFailure().recoverWithItem(List.of())
            ))
            .combinedWith(results -> {
                var all = new ArrayList<Map<String, Object>>();
                for (Object raw : results)
                    all.addAll((List<Map<String, Object>>) raw);
                return buildResult(all, rid, "");
            })
            .flatMap(this::enrichDataSource);
    }

    // ── Result builder ────────────────────────────────────────────────────────

    /**
     * Build ExploreResult from projected Cypher rows.
     * Each row has: srcId, srcLabel, srcType (optional), tgtId, tgtLabel, tgtScope, tgtType, edgeType
     * srcType overrides rootType when present; used by schema queries where external routines
     * are source nodes with a different type than the schema root.
     */
    static ExploreResult buildResult(
            List<Map<String, Object>> rows,
            String rootLabel,
            String rootType) {

        Map<String, GraphNode> nodesById = new LinkedHashMap<>();
        List<GraphEdge> edges = new ArrayList<>();
        Set<String> edgeIdsSeen = new HashSet<>();
        String rootId = null;

        for (Map<String, Object> row : rows) {
            String srcId    = str(row, "srcId");
            String srcLabel = str(row, "srcLabel");
            // srcType column present in schema queries; fall back to rootType for others
            String srcType  = str(row, "srcType");
            if (srcType.isBlank()) srcType = rootType;
            String tgtId    = str(row, "tgtId");
            String tgtLabel = str(row, "tgtLabel");
            String tgtScope = str(row, "tgtScope");
            String tgtType  = str(row, "tgtType");
            String edgeType = str(row, "edgeType");

            // Optional PK/FK/dataType columns — only present in hasColQ (stmtColumns).
            // Populate meta for DaliColumn nodes so frontend can render PK/FK badges.
            String tgtPk       = str(row, "tgtPk");
            String tgtFk       = str(row, "tgtFk");
            String tgtReq      = str(row, "tgtReq");
            String tgtDataType = str(row, "tgtDataType");
            Map<String, String> tgtMeta = Map.of();
            if (!tgtPk.isEmpty() || !tgtFk.isEmpty() || !tgtReq.isEmpty() || !tgtDataType.isEmpty()) {
                var m = new java.util.HashMap<String, String>();
                if (!tgtPk.isEmpty())       m.put("isPk",       tgtPk);
                if (!tgtFk.isEmpty())       m.put("isFk",       tgtFk);
                if (!tgtReq.isEmpty())      m.put("isRequired", tgtReq);
                if (!tgtDataType.isEmpty()) m.put("dataType",   tgtDataType);
                tgtMeta = java.util.Collections.unmodifiableMap(m);
            }

            if (rootId == null) rootId = srcId;

            // Optional source-node scope and meta — present for routine nodes returned
            // by exploreRoutineAggregate (srcScope=schema_geoid, srcPackage, srcKind).
            // All other callers leave these columns absent; str() returns "" safely.
            String srcScope   = str(row, "srcScope");
            String srcPackage = str(row, "srcPackage");
            String srcKind    = str(row, "srcKind");
            Map<String, String> srcMeta = Map.of();
            if (!srcPackage.isEmpty() || !srcKind.isEmpty()) {
                var sm = new java.util.HashMap<String, String>();
                if (!srcPackage.isEmpty()) sm.put("packageName", srcPackage);
                if (!srcKind.isEmpty())    sm.put("routineType", srcKind);
                srcMeta = java.util.Collections.unmodifiableMap(sm);
            }
            nodesById.putIfAbsent(srcId, new GraphNode(srcId, srcType, srcLabel, srcScope, srcMeta, ""));
            nodesById.putIfAbsent(tgtId, new GraphNode(tgtId, tgtType, tgtLabel, tgtScope, tgtMeta, ""));

            // Column-level routing hints — when a Cypher segment wants the edge
            // to land on a specific column handle inside the parent card, it
            // returns sourceHandle / targetHandle columns and the edge id is
            // expanded to include them so dedup doesn't collapse multiple
            // column-to-column edges between the same parent pair.
            String srcHandle = str(row, "sourceHandle");
            String tgtHandle = str(row, "targetHandle");
            String edgeId = srcId
                + (srcHandle.isEmpty() ? "" : "/" + srcHandle)
                + "__" + edgeType + "__" + tgtId
                + (tgtHandle.isEmpty() ? "" : "/" + tgtHandle);
            if (edgeIdsSeen.add(edgeId)) {
                edges.add(new GraphEdge(edgeId, srcId, tgtId, edgeType,
                    srcHandle.isEmpty() ? null : srcHandle,
                    tgtHandle.isEmpty() ? null : tgtHandle));
            }
        }

        boolean hasMore = nodesById.size() >= NODE_LIMIT || rows.size() >= NODE_LIMIT;
        return new ExploreResult(new ArrayList<>(nodesById.values()), edges, hasMore);
    }

    // ── toExploreResult: used by LineageService (node/edge result rows) ──────────
    //
    // Lineage Cypher queries return rows of the form:
    //   { n: <vertex>, r: <edge>, m: <vertex> }
    // where each vertex is a Map with @rid, @type, and domain properties.

    @SuppressWarnings("unchecked")
    public static ExploreResult toExploreResult(List<Map<String, Object>> rows) {
        Map<String, GraphNode> nodesById = new LinkedHashMap<>();
        List<GraphEdge> edges = new ArrayList<>();

        for (Map<String, Object> row : rows) {
            Map<String, Object> n = (Map<String, Object>) row.get("n");
            Map<String, Object> r = (Map<String, Object>) row.get("r");
            Map<String, Object> m = (Map<String, Object>) row.get("m");
            if (n == null || r == null || m == null) continue;

            String srcId    = str(n, "@rid");
            String srcType  = str(n, "@type");
            String srcLabel = nodeLabel(n);
            String srcDs    = str(n, "data_source");
            String tgtId    = str(m, "@rid");
            String tgtType  = str(m, "@type");
            String tgtLabel = nodeLabel(m);
            String tgtScope = str(m, "schema_geoid");
            String tgtDs    = str(m, "data_source");
            String edgeType = str(r, "@type");
            String edgeId   = str(r, "@rid");
            if (edgeId.isBlank()) edgeId = srcId + "__" + edgeType + "__" + tgtId;

            nodesById.putIfAbsent(srcId, new GraphNode(srcId, srcType, srcLabel, "", Map.of(), srcDs));
            nodesById.putIfAbsent(tgtId, new GraphNode(tgtId, tgtType, tgtLabel, tgtScope, Map.of(), tgtDs));
            edges.add(new GraphEdge(edgeId, srcId, tgtId, edgeType));
        }

        boolean hasMore = nodesById.size() >= NODE_LIMIT || rows.size() >= NODE_LIMIT;
        return new ExploreResult(new ArrayList<>(nodesById.values()), edges, hasMore);
    }

    /**
     * Secondary enrichment: fetches data_source for all DaliTable nodes in the result.
     * Public so LineageService can chain it after buildResult().
     *
     * <p>Uses UNWIND + single-value id(t) = rid instead of id(t) IN $ids to avoid
     * ArcadeDB Cypher type-mismatch: id() returns a LINK type in WHERE context, which
     * does not compare equal to String list elements in the IN operator.
     */
    public Uni<ExploreResult> enrichDataSource(ExploreResult result) {
        List<String> tableIds = result.nodes().stream()
                .filter(n -> "DaliTable".equals(n.type()))
                .map(GraphNode::id)
                .toList();
        if (tableIds.isEmpty()) return Uni.createFrom().item(result);
        String cypher = """
                UNWIND $ids AS rid
                MATCH (t:DaliTable)
                WHERE id(t) = rid
                RETURN id(t) AS id, coalesce(t.data_source, '') AS ds
                """;
        return arcade.cypher(cypher, Map.of("ids", tableIds))
                .onFailure().recoverWithItem(List.of())
                .map(rows -> {
                    Map<String, String> dsMap = new java.util.HashMap<>();
                    for (Map<String, Object> row : rows) {
                        String id = str(row, "id");
                        String ds = str(row, "ds");
                        if (!id.isBlank() && !ds.isBlank()) dsMap.put(id, ds);
                    }
                    if (dsMap.isEmpty()) return result;
                    List<GraphNode> enriched = result.nodes().stream()
                            .map(n -> dsMap.containsKey(n.id())
                                    ? new GraphNode(n.id(), n.type(), n.label(), n.scope(), n.meta(), dsMap.get(n.id()))
                                    : n)
                            .toList();
                    return new ExploreResult(enriched, result.edges(), result.hasMore());
                });
    }

    /** Best-effort human label from any vertex property map. */
    private static String nodeLabel(Map<String, Object> node) {
        for (String key : new String[]{
                "schema_name", "table_name", "package_name",
                "routine_name", "column_name", "stmt_geoid", "snippet"}) {
            Object v = node.get(key);
            if (v != null && !v.toString().isBlank()) return v.toString();
        }
        return str(node, "@rid");
    }

    private static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) return "";
        // labels()[0] returns a List<String> in ArcadeDB Cypher — unwrap first element
        if (v instanceof java.util.List<?> list) return list.isEmpty() ? "" : list.get(0).toString();
        return v.toString();
    }

    // ── Scope parser ──────────────────────────────────────────────────────────

    /**
     * Parses a scope token into type + name + optional dbName.
     *
     * Formats:
     *   "schema-DWH"          → type=schema, name=DWH,  dbName=null  (no db filter)
     *   "schema-DWH|DWH2"     → type=schema, name=DWH,  dbName=DWH2  (filtered to db DWH2)
     *   "pkg-MY_PKG"          → type=pkg,    name=MY_PKG, dbName=null
     *   "#10:0"               → type=rid,    name=#10:0,  dbName=null
     *
     * The pipe-separated dbName disambiguates schemas with the same name in different databases.
     * OverviewService returns databaseName per SchemaNode, so LOOM can build: "schema-" + name + "|" + databaseName.
     */
    record ScopeRef(String type, String name, String dbName) {
        static ScopeRef parse(String scope) {
            if (scope == null || scope.isBlank()) return new ScopeRef("rid", "", null);
            int dash = scope.indexOf('-');
            if (dash < 0) return new ScopeRef("rid", scope, null);
            String type     = scope.substring(0, dash);
            String nameRaw  = scope.substring(dash + 1);
            // Optional db_name after pipe: "schema-DWH|DWH2"
            int pipe = nameRaw.indexOf('|');
            if (pipe >= 0) {
                return new ScopeRef(type, nameRaw.substring(0, pipe), nameRaw.substring(pipe + 1));
            }
            return new ScopeRef(type, nameRaw, null);
        }
    }
}
