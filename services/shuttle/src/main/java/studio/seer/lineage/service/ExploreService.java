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
        ScopeRef ref = ScopeRef.parse(scope);
        return switch (ref.type()) {
            case "schema" -> exploreSchema(ref.name(), ref.dbName());
            case "pkg"    -> explorePackage(ref.name());
            case "db"     -> exploreByDatabase(ref.name());
            default       -> exploreByRid(ref.name());
        };
    }

    // ── Schema scope ──────────────────────────────────────────────────────────

    private Uni<ExploreResult> exploreSchema(String schemaName, String dbName) {
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
            UNION ALL
            // 2.3 DATA_FLOW aggregated to table → statement
            //   DB shape: DaliColumn -[:DATA_FLOW]-> DaliOutputColumn
            //   L2 has no DaliColumn / DaliOutputColumn nodes, so we aggregate:
            //     srcTable = the column's owning table (via HAS_COLUMN)
            //     tgtStmt  = the output column's owning statement (via HAS_OUTPUT_COL)
            //   Only root statements emitted (same convention as READS_FROM above).
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_TABLE]->(srcTbl:DaliTable)-[:HAS_COLUMN]->(srcCol:DaliColumn)
            MATCH (srcCol)-[:DATA_FLOW]->(oc:DaliOutputColumn)<-[:HAS_OUTPUT_COL]-(stmt:DaliStatement)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(srcTbl) AS srcId, srcTbl.table_name AS srcLabel, 'DaliTable' AS srcType,
                   id(stmt) AS tgtId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'DATA_FLOW' AS edgeType
            LIMIT 2000
            UNION ALL
            // 2.4 FILTER_FLOW aggregated to table → statement
            //   DB shape: DaliColumn -[:FILTER_FLOW]-> DaliStatement  (WHERE / HAVING / JOIN)
            //   L2 aggregation: srcTable = column's owning table, tgtStmt = stmt directly.
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_TABLE]->(srcTbl:DaliTable)-[:HAS_COLUMN]->(srcCol:DaliColumn)
            MATCH (srcCol)-[:FILTER_FLOW]->(stmt:DaliStatement)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(srcTbl) AS srcId, srcTbl.table_name AS srcLabel, 'DaliTable' AS srcType,
                   id(stmt) AS tgtId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'FILTER_FLOW' AS edgeType
            LIMIT 2000
            """;

        // ArcadeDB Cypher bug: $param IS NULL does not work in WHERE clauses.
        // Workaround: use empty string as sentinel ($dbName = '' OR ...).
        Map<String, Object> params = Map.of(
            "schema", schemaName,
            "dbName", dbName == null || dbName.isBlank() ? "" : dbName
        );

        return arcade.cypher(cypher, params)
            .map(rows -> buildResult(rows, schemaName, "DaliSchema"))
            .flatMap(this::enrichDataSource);
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

        return arcade.cypher(cypher, Map.of("pkg", packageName))
            .map(rows -> buildResult(rows, packageName, "DaliPackage"))
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

            nodesById.putIfAbsent(srcId, new GraphNode(srcId, srcType, srcLabel, "", Map.of(), ""));
            nodesById.putIfAbsent(tgtId, new GraphNode(tgtId, tgtType, tgtLabel, tgtScope, tgtMeta, ""));

            String edgeId = srcId + "__" + edgeType + "__" + tgtId;
            if (edgeIdsSeen.add(edgeId)) {
                edges.add(new GraphEdge(edgeId, srcId, tgtId, edgeType));
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
