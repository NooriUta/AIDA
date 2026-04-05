package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
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

    @Inject
    ArcadeGateway arcade;

    public Uni<ExploreResult> explore(String scope) {
        ScopeRef ref = ScopeRef.parse(scope);
        return switch (ref.type()) {
            case "schema" -> exploreSchema(ref.name());
            case "pkg"    -> explorePackage(ref.name());
            default       -> exploreByRid(ref.name());
        };
    }

    // ── Schema scope ──────────────────────────────────────────────────────────

    private Uni<ExploreResult> exploreSchema(String schemaName) {
        // Full chain: Schema/Table (group) + Package/Session → Routine → Statement → Table.
        //
        // Two access patterns in the data:
        //   A) DaliPackage -[CONTAINS_ROUTINE]-> DaliRoutine -[CONTAINS_STMT]-> DaliStatement
        //                  -[READS_FROM|WRITES_TO]-> DaliTable
        //   B) DaliSession -[BELONGS_TO_SESSION]-> DaliRoutine -[CONTAINS_STMT]-> DaliStatement
        //                  -[READS_FROM|WRITES_TO]-> DaliTable
        //
        // Branch 1: tables owned by schema — children inside schema group.
        // Branch 2: pkg → routine (only routines whose stmts access schema tables, path A).
        // Branch 3: routine → root statement (CONTAINS_STMT, path A).
        // Branch 4: statement → table READS_FROM (path A).
        // Branch 5: statement → table WRITES_TO (path A).
        // Branch 6: session → routine (path B).
        // Branch 7: routine → root statement (CONTAINS_STMT, path B).
        // Branch 8: statement → table READS_FROM (path B, same stmt nodes deduped by buildResult).
        // Branch 9: statement → table WRITES_TO (path B).
        // Branch 10: statement → output column (HAS_OUTPUT_COL).
        // Branch 11: table → column (HAS_COLUMN, inline card data).
        String cypher = """
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(t:DaliTable)
            RETURN id(s) AS srcId, s.schema_name AS srcLabel, 'DaliSchema' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'CONTAINS_TABLE' AS edgeType
            LIMIT 300
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(:DaliTable)
                  <-[:READS_FROM|WRITES_TO]-(:DaliStatement)<-[:CONTAINS_STMT]-(r:DaliRoutine)
                  <-[:CONTAINS_ROUTINE]-(pkg:DaliPackage)
            RETURN DISTINCT id(pkg) AS srcId, pkg.package_name AS srcLabel, 'DaliPackage' AS srcType,
                   id(r) AS tgtId, r.routine_name AS tgtLabel, '' AS tgtScope,
                   'DaliRoutine' AS tgtType, 'CONTAINS_ROUTINE' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(:DaliTable)
                  <-[:READS_FROM|WRITES_TO]-(stmt:DaliStatement)<-[:CONTAINS_STMT]-(r:DaliRoutine)
                  <-[:CONTAINS_ROUTINE]-(:DaliPackage)
            WHERE NOT exists((stmt)<-[:CONTAINS_STMT]-(:DaliStatement))
            RETURN DISTINCT id(r) AS srcId, r.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
                   id(stmt) AS tgtId, coalesce(stmt.stmt_text, stmt.stmt_geoid, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'CONTAINS_STMT' AS edgeType
            LIMIT 300
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(t:DaliTable)
                  <-[:READS_FROM]-(stmt:DaliStatement)
            WHERE NOT exists((stmt)<-[:CONTAINS_STMT]-(:DaliStatement))
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_text, stmt.stmt_geoid, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(t:DaliTable)
                  <-[:WRITES_TO]-(stmt:DaliStatement)
            WHERE NOT exists((stmt)<-[:CONTAINS_STMT]-(:DaliStatement))
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_text, stmt.stmt_geoid, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'WRITES_TO' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(:DaliTable)
                  <-[:READS_FROM|WRITES_TO]-(:DaliStatement)<-[:CONTAINS_STMT]-(r:DaliRoutine)
                  <-[:BELONGS_TO_SESSION]-(sess:DaliSession)
            RETURN DISTINCT id(sess) AS srcId, sess.file_path AS srcLabel, 'DaliSession' AS srcType,
                   id(r) AS tgtId, r.routine_name AS tgtLabel, '' AS tgtScope,
                   'DaliRoutine' AS tgtType, 'BELONGS_TO_SESSION' AS edgeType
            LIMIT 50
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(:DaliTable)
                  <-[:READS_FROM|WRITES_TO]-(stmt:DaliStatement)<-[:CONTAINS_STMT]-(r:DaliRoutine)
                  <-[:BELONGS_TO_SESSION]-(:DaliSession)
            WHERE NOT exists((stmt)<-[:CONTAINS_STMT]-(:DaliStatement))
            RETURN DISTINCT id(r) AS srcId, r.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
                   id(stmt) AS tgtId, coalesce(stmt.stmt_text, stmt.stmt_geoid, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'CONTAINS_STMT' AS edgeType
            LIMIT 300
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(t:DaliTable)
                  <-[:READS_FROM]-(stmt:DaliStatement)<-[:CONTAINS_STMT]-(:DaliRoutine)
                  <-[:BELONGS_TO_SESSION]-(:DaliSession)
            WHERE NOT exists((stmt)<-[:CONTAINS_STMT]-(:DaliStatement))
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_text, stmt.stmt_geoid, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(t:DaliTable)
                  <-[:WRITES_TO]-(stmt:DaliStatement)<-[:CONTAINS_STMT]-(:DaliRoutine)
                  <-[:BELONGS_TO_SESSION]-(:DaliSession)
            WHERE NOT exists((stmt)<-[:CONTAINS_STMT]-(:DaliStatement))
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_text, stmt.stmt_geoid, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'WRITES_TO' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(:DaliTable)
                  <-[:READS_FROM|WRITES_TO]-(stmt:DaliStatement)-[:HAS_OUTPUT_COL]->(col:DaliOutputColumn)
            WHERE NOT exists((stmt)<-[:CONTAINS_STMT]-(:DaliStatement))
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_text, stmt.stmt_geoid, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(col) AS tgtId, coalesce(col.name, col.col_key, '') AS tgtLabel, '' AS tgtScope,
                   'DaliOutputColumn' AS tgtType, 'HAS_OUTPUT_COL' AS edgeType
            LIMIT 500
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(t:DaliTable)-[:HAS_COLUMN]->(c:DaliColumn)
            RETURN id(t) AS srcId, t.table_name AS srcLabel, 'DaliTable' AS srcType,
                   id(c) AS tgtId, c.column_name AS tgtLabel, '' AS tgtScope,
                   'DaliColumn' AS tgtType, 'HAS_COLUMN' AS edgeType
            LIMIT 500
            """;

        return arcade.cypher(cypher, Map.of("schema", schemaName))
            .map(rows -> buildResult(rows, schemaName, "DaliSchema"));
    }

    // ── Package scope ─────────────────────────────────────────────────────────

    private Uni<ExploreResult> explorePackage(String packageName) {
        // Confirmed against hound DB (2026-04-04):
        //   ROUTINE_USES_TABLE = 0 edges (not populated).
        //   Tables accessed via CONTAINS_STMT → READS_FROM/WRITES_TO paths.
        //
        // Branch 1: routines owned by the package.
        // Branch 2: root statements inside routines (CONTAINS_STMT).
        // Branch 3: statement → table READS_FROM (statement-level, not aggregated).
        // Branch 4: statement → table WRITES_TO (statement-level).
        // Branch 5: statement → output column (HAS_OUTPUT_COL, inline card data).
        String cypher = """
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(r:DaliRoutine)
            RETURN id(p) AS srcId, p.package_name AS srcLabel, 'DaliPackage' AS srcType,
                   id(r) AS tgtId, r.routine_name AS tgtLabel, r.package_geoid AS tgtScope,
                   'DaliRoutine' AS tgtType, 'CONTAINS_ROUTINE' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(r:DaliRoutine)-[:CONTAINS_STMT]->(stmt:DaliStatement)
            WHERE NOT exists((stmt)<-[:CONTAINS_STMT]-(:DaliStatement))
            RETURN id(r) AS srcId, r.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
                   id(stmt) AS tgtId, coalesce(stmt.stmt_text, stmt.stmt_geoid, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'CONTAINS_STMT' AS edgeType
            LIMIT 300
            UNION ALL
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(:DaliRoutine)
                  -[:CONTAINS_STMT]->(stmt:DaliStatement)-[:READS_FROM]->(t:DaliTable)
            WHERE NOT exists((stmt)<-[:CONTAINS_STMT]-(:DaliStatement))
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_text, stmt.stmt_geoid, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(:DaliRoutine)
                  -[:CONTAINS_STMT]->(stmt:DaliStatement)-[:WRITES_TO]->(t:DaliTable)
            WHERE NOT exists((stmt)<-[:CONTAINS_STMT]-(:DaliStatement))
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_text, stmt.stmt_geoid, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'WRITES_TO' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(:DaliRoutine)-[:CONTAINS_STMT]->(stmt:DaliStatement)-[:HAS_OUTPUT_COL]->(col:DaliOutputColumn)
            WHERE NOT exists((stmt)<-[:CONTAINS_STMT]-(:DaliStatement))
            RETURN id(stmt) AS srcId, coalesce(stmt.stmt_text, stmt.stmt_geoid, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(col) AS tgtId, coalesce(col.name, col.col_key, '') AS tgtLabel, '' AS tgtScope,
                   'DaliOutputColumn' AS tgtType, 'HAS_OUTPUT_COL' AS edgeType
            LIMIT 500
            """;

        return arcade.cypher(cypher, Map.of("pkg", packageName))
            .map(rows -> buildResult(rows, packageName, "DaliPackage"));
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

        String outQ = """
            MATCH (n)-[r]->(m)
            WHERE id(n) = $rid
            RETURN id(n) AS srcId,
                   coalesce(n.schema_name, n.table_name, n.package_name, n.routine_name, n.stmt_geoid, n.column_name, n.name, n.col_key, '') AS srcLabel,
                   labels(n)[0] AS srcType,
                   id(m) AS tgtId,
                   coalesce(m.schema_name, m.table_name, m.package_name, m.routine_name, m.stmt_geoid, m.column_name, m.name, m.col_key, '') AS tgtLabel,
                   m.schema_geoid AS tgtScope, labels(m)[0] AS tgtType, type(r) AS edgeType
            LIMIT 300
            """;

        String inQ = """
            MATCH (m)-[r]->(n)
            WHERE id(n) = $rid
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
            RETURN id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.stmt_text, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(col) AS tgtId, coalesce(col.name, col.col_key, '') AS tgtLabel, '' AS tgtScope,
                   'DaliOutputColumn' AS tgtType, 'HAS_OUTPUT_COL' AS edgeType
            LIMIT 200
            """;

        // Output columns when $rid IS a DaliStatement (root statement explore)
        String stmtOutColQ = """
            MATCH (n:DaliStatement)-[:HAS_OUTPUT_COL]->(col:DaliOutputColumn)
            WHERE id(n) = $rid
            RETURN id(n) AS srcId, coalesce(n.stmt_geoid, n.stmt_text, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(col) AS tgtId, coalesce(col.name, col.col_key, '') AS tgtLabel, '' AS tgtScope,
                   'DaliOutputColumn' AS tgtType, 'HAS_OUTPUT_COL' AS edgeType
            LIMIT 100
            """;

        // If $rid is a DaliColumn: resolve parent table + ALL its sibling columns (inline display)
        String sibColQ = """
            MATCH (parent)-[:HAS_COLUMN]->(n)
            WHERE id(n) = $rid
            WITH parent
            MATCH (parent)-[:HAS_COLUMN]->(sibling)
            RETURN id(parent) AS srcId, coalesce(parent.table_name, '') AS srcLabel,
                   labels(parent)[0] AS srcType,
                   id(sibling) AS tgtId, coalesce(sibling.column_name, '') AS tgtLabel,
                   '' AS tgtScope, labels(sibling)[0] AS tgtType, 'HAS_COLUMN' AS edgeType
            LIMIT 100
            """;

        // If $rid is a DaliOutputColumn: resolve parent statement + ALL its sibling output cols
        String sibOutColQ = """
            MATCH (parent)-[:HAS_OUTPUT_COL]->(n)
            WHERE id(n) = $rid
            WITH parent
            MATCH (parent)-[:HAS_OUTPUT_COL]->(sibling)
            RETURN id(parent) AS srcId, coalesce(parent.stmt_geoid, parent.stmt_text, '') AS srcLabel,
                   labels(parent)[0] AS srcType,
                   id(sibling) AS tgtId, coalesce(sibling.name, sibling.col_key, '') AS tgtLabel,
                   '' AS tgtScope, labels(sibling)[0] AS tgtType, 'HAS_OUTPUT_COL' AS edgeType
            LIMIT 100
            """;

        return Uni.combine().all()
            .unis(List.of(
                arcade.cypher(outQ, params),
                arcade.cypher(inQ, params),
                arcade.cypher(outColQ, params),
                arcade.cypher(sibColQ, params),
                arcade.cypher(sibOutColQ, params),
                arcade.cypher(stmtOutColQ, params)
            ))
            .combinedWith(results -> {
                var all = new ArrayList<Map<String, Object>>();
                for (Object raw : results)
                    all.addAll((List<Map<String, Object>>) raw);
                return buildResult(all, rid, "");
            });
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

            if (rootId == null) rootId = srcId;

            nodesById.putIfAbsent(srcId, new GraphNode(srcId, srcType, srcLabel, "", Map.of()));
            nodesById.putIfAbsent(tgtId, new GraphNode(tgtId, tgtType, tgtLabel, tgtScope, Map.of()));

            String edgeId = srcId + "__" + edgeType + "__" + tgtId;
            if (edgeIdsSeen.add(edgeId)) {
                edges.add(new GraphEdge(edgeId, srcId, tgtId, edgeType));
            }
        }

        return new ExploreResult(new ArrayList<>(nodesById.values()), edges);
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
            String tgtId    = str(m, "@rid");
            String tgtType  = str(m, "@type");
            String tgtLabel = nodeLabel(m);
            String tgtScope = str(m, "schema_geoid");
            String edgeType = str(r, "@type");
            String edgeId   = str(r, "@rid");
            if (edgeId.isBlank()) edgeId = srcId + "__" + edgeType + "__" + tgtId;

            nodesById.putIfAbsent(srcId, new GraphNode(srcId, srcType, srcLabel, "", Map.of()));
            nodesById.putIfAbsent(tgtId, new GraphNode(tgtId, tgtType, tgtLabel, tgtScope, Map.of()));
            edges.add(new GraphEdge(edgeId, srcId, tgtId, edgeType));
        }

        return new ExploreResult(new ArrayList<>(nodesById.values()), edges);
    }

    /** Best-effort human label from any vertex property map. */
    private static String nodeLabel(Map<String, Object> node) {
        for (String key : new String[]{
                "schema_name", "table_name", "package_name",
                "routine_name", "column_name", "stmt_text"}) {
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

    record ScopeRef(String type, String name) {
        static ScopeRef parse(String scope) {
            if (scope == null || scope.isBlank()) return new ScopeRef("rid", "");
            int dash = scope.indexOf('-');
            if (dash < 0) return new ScopeRef("rid", scope);
            return new ScopeRef(scope.substring(0, dash), scope.substring(dash + 1));
        }
    }
}
