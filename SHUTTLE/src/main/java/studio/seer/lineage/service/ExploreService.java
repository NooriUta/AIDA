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
        // Confirmed against hound DB (2026-04-04):
        //   DaliSchema -[CONTAINS_ROUTINE]-> = 0 edges (not populated in current data).
        //   ROUTINE_USES_TABLE                = 0 edges (not populated).
        //
        // Two parallel access patterns exist in the data:
        //   A) DaliPackage -[CONTAINS_ROUTINE]-> DaliRoutine -[CONTAINS_STMT]-> DaliStatement
        //                  -[READS_FROM|WRITES_TO]-> DaliTable  (DWH schema, ~10 pkgs)
        //   B) DaliSession -[BELONGS_TO_SESSION]-> DaliRoutine -[CONTAINS_STMT]-> DaliStatement
        //                  -[READS_FROM|WRITES_TO]-> DaliTable  (BUDM_RMS etc., session files)
        //
        // Branch 1: tables owned by schema (children inside group).
        // Branch 2: packages that access schema tables via path A — fake CONTAINS_ROUTINE
        //           so transformSchemaExplore places them INSIDE the schema group.
        // Branch 3: READS_FROM data-flow edges (pkg → table) for path A.
        // Branch 4: WRITES_TO data-flow edges (pkg → table) for path A.
        // Branch 5: sessions that access schema tables via path B — fake CONTAINS_ROUTINE.
        //           Label = filename extracted from sess.file_path.
        // Branch 6: READS_FROM data-flow edges (sess → table) for path B.
        // Branch 7: WRITES_TO data-flow edges (sess → table) for path B.
        // Branch 8: column inline data for table cards.
        String cypher = """
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(t:DaliTable)
            RETURN id(s) AS srcId, s.schema_name AS srcLabel, 'DaliSchema' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'CONTAINS_TABLE' AS edgeType
            LIMIT 300
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(:DaliTable)
                  <-[:READS_FROM|WRITES_TO]-(:DaliStatement)<-[:CONTAINS_STMT]-(:DaliRoutine)
                  <-[:CONTAINS_ROUTINE]-(pkg:DaliPackage)
            RETURN DISTINCT id(s) AS srcId, s.schema_name AS srcLabel, 'DaliSchema' AS srcType,
                   id(pkg) AS tgtId, pkg.package_name AS tgtLabel, '' AS tgtScope,
                   'DaliPackage' AS tgtType, 'CONTAINS_ROUTINE' AS edgeType
            LIMIT 50
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(t:DaliTable)
                  <-[:READS_FROM]-(:DaliStatement)<-[:CONTAINS_STMT]-(:DaliRoutine)
                  <-[:CONTAINS_ROUTINE]-(pkg:DaliPackage)
            RETURN DISTINCT id(pkg) AS srcId, pkg.package_name AS srcLabel, 'DaliPackage' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(t:DaliTable)
                  <-[:WRITES_TO]-(:DaliStatement)<-[:CONTAINS_STMT]-(:DaliRoutine)
                  <-[:CONTAINS_ROUTINE]-(pkg:DaliPackage)
            RETURN DISTINCT id(pkg) AS srcId, pkg.package_name AS srcLabel, 'DaliPackage' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'WRITES_TO' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(:DaliTable)
                  <-[:READS_FROM|WRITES_TO]-(:DaliStatement)<-[:CONTAINS_STMT]-(:DaliRoutine)
                  <-[:BELONGS_TO_SESSION]-(sess:DaliSession)
            RETURN DISTINCT id(s) AS srcId, s.schema_name AS srcLabel, 'DaliSchema' AS srcType,
                   id(sess) AS tgtId, sess.file_path AS tgtLabel, '' AS tgtScope,
                   'DaliSession' AS tgtType, 'CONTAINS_ROUTINE' AS edgeType
            LIMIT 50
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(t:DaliTable)
                  <-[:READS_FROM]-(:DaliStatement)<-[:CONTAINS_STMT]-(:DaliRoutine)
                  <-[:BELONGS_TO_SESSION]-(sess:DaliSession)
            RETURN DISTINCT id(sess) AS srcId, sess.file_path AS srcLabel, 'DaliSession' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (s:DaliSchema {schema_name: $schema})-[:CONTAINS_TABLE]->(t:DaliTable)
                  <-[:WRITES_TO]-(:DaliStatement)<-[:CONTAINS_STMT]-(:DaliRoutine)
                  <-[:BELONGS_TO_SESSION]-(sess:DaliSession)
            RETURN DISTINCT id(sess) AS srcId, sess.file_path AS srcLabel, 'DaliSession' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'WRITES_TO' AS edgeType
            LIMIT 200
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
        //   Tables accessed by package routines must be found via CONTAINS_STMT → READS_FROM/WRITES_TO.
        //
        // Branch 1: routines owned by the package.
        // Branch 2: READS_FROM edges — routine → stmt → table (data-flow, source = routine, target = table).
        // Branch 3: WRITES_TO edges — routine → stmt → table (data-flow).
        // Branch 4: statements inside routines (drill-down to statement level).
        // Branch 5: output-column inline data for statement cards.
        String cypher = """
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(r:DaliRoutine)
            RETURN id(p) AS srcId, p.package_name AS srcLabel, 'DaliPackage' AS srcType,
                   id(r) AS tgtId, r.routine_name AS tgtLabel, r.package_geoid AS tgtScope,
                   'DaliRoutine' AS tgtType, 'CONTAINS_ROUTINE' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(r:DaliRoutine)
                  -[:CONTAINS_STMT]->(:DaliStatement)-[:READS_FROM]->(t:DaliTable)
            RETURN DISTINCT id(r) AS srcId, r.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(r:DaliRoutine)
                  -[:CONTAINS_STMT]->(:DaliStatement)-[:WRITES_TO]->(t:DaliTable)
            RETURN DISTINCT id(r) AS srcId, r.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'WRITES_TO' AS edgeType
            LIMIT 200
            UNION ALL
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(r:DaliRoutine)-[:CONTAINS_STMT]->(stmt:DaliStatement)
            RETURN id(r) AS srcId, r.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
                   id(stmt) AS tgtId, coalesce(stmt.stmt_text, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'CONTAINS_STMT' AS edgeType
            LIMIT 300
            UNION ALL
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(:DaliRoutine)-[:CONTAINS_STMT]->(stmt:DaliStatement)-[:HAS_OUTPUT_COL]->(col:DaliOutputColumn)
            RETURN id(stmt) AS srcId, coalesce(stmt.stmt_text, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(col) AS tgtId, coalesce(col.name, col.col_key, '') AS tgtLabel, '' AS tgtScope,
                   'DaliOutputColumn' AS tgtType, 'HAS_OUTPUT_COL' AS edgeType
            LIMIT 500
            """;

        return arcade.cypher(cypher, Map.of("pkg", packageName))
            .map(rows -> buildResult(rows, packageName, "DaliPackage"));
    }

    // ── RID-based (generic) ───────────────────────────────────────────────────

    private Uni<ExploreResult> exploreByRid(String rid) {
        // 1-hop generic + output columns for any DaliStatement children
        String cypher = """
            MATCH (n)-[r]->(m)
            WHERE id(n) = $rid
            RETURN id(n) AS srcId, coalesce(n.schema_name, n.table_name, n.package_name, n.routine_name, '') AS srcLabel,
                   labels(n)[0] AS srcType,
                   id(m) AS tgtId, coalesce(m.schema_name, m.table_name, m.package_name, m.routine_name, m.column_name, '') AS tgtLabel,
                   m.schema_geoid AS tgtScope, labels(m)[0] AS tgtType, type(r) AS edgeType
            LIMIT 300
            UNION ALL
            MATCH (n)-[:CONTAINS_STMT]->(stmt:DaliStatement)-[:HAS_OUTPUT_COL]->(col:DaliOutputColumn)
            WHERE id(n) = $rid
            RETURN id(stmt) AS srcId, coalesce(stmt.stmt_text, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(col) AS tgtId, coalesce(col.name, col.col_key, '') AS tgtLabel, '' AS tgtScope,
                   'DaliOutputColumn' AS tgtType, 'HAS_OUTPUT_COL' AS edgeType
            LIMIT 200
            """;

        return arcade.cypher(cypher, Map.of("rid", rid))
            .map(rows -> buildResult(rows, rid, ""));
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
