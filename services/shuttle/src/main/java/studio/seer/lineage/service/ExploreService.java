package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.ExploreResult;
import studio.seer.lineage.model.GraphEdge;
import studio.seer.lineage.model.GraphNode;
import studio.seer.lineage.security.SeerIdentity;
import studio.seer.tenantrouting.YggLineageRegistry;

import java.util.*;

/**
 * L2 — explore a schema or package scope.
 *
 * SHT-04: All ArcadeDB queries are routed to {@code hound_{alias}} via
 * {@link YggLineageRegistry}. The DB name is resolved once per request
 * via {@link #lineageDb()}.
 *
 * Routine-focused methods delegated to {@link ExploreRoutineService}.
 * Statement/column/package methods delegated to {@link ExploreStatementService}.
 */
@ApplicationScoped
public class ExploreService {

    private static final Logger log = Logger.getLogger(ExploreService.class);

    static final int NODE_LIMIT = 500;

    @Inject ArcadeGateway         arcade;
    @Inject SeerIdentity          identity;
    @Inject YggLineageRegistry    lineageRegistry;
    @Inject ExploreRoutineService  routineService;
    @Inject ExploreStatementService statementService;

    String lineageDb() {
        return lineageRegistry.resourceFor(identity.tenantAlias()).databaseName();
    }

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
            case "schema"   -> statementService.exploreSchema(ref.name(), ref.dbName(), includeExternal);
            case "pkg"      -> statementService.explorePackage(ref.name());
            case "db"       -> statementService.exploreByDatabase(ref.name());
            // "routine-<@rid>": focused L3 view — root stmts + tables + records only.
            // Set by LoomCanvas when drilling from L2 AGG into a DaliRoutine node.
            case "routine"  -> routineService.exploreRoutineScope(ref.name());
            default         -> exploreByRid(ref.name());
        };
    }

    // ── Public delegation methods (callers in LineageResource use these) ──────

    public Uni<ExploreResult> exploreRoutineAggregate(String scope) {
        return routineService.exploreRoutineAggregate(scope);
    }

    public Uni<ExploreResult> exploreRoutineScope(String routineRid) {
        return routineService.exploreRoutineScope(routineRid);
    }

    public Uni<ExploreResult> exploreRoutineDetail(String nodeId) {
        return routineService.exploreRoutineDetail(nodeId);
    }

    public Uni<ExploreResult> exploreStatementTree(String stmtId) {
        return statementService.exploreStatementTree(stmtId);
    }

    public Uni<ExploreResult> exploreStmtColumns(List<String> ids) {
        return statementService.exploreStmtColumns(ids);
    }

    // ── RID-based (generic, bidirectional) ───────────────────────────────────

    /**
     * 1-hop bidirectional explore for any node (table, column, statement, routine...).
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

        String outColQ = """
            MATCH (n)-[:CONTAINS_STMT]->(stmt:DaliStatement)-[:HAS_OUTPUT_COL]->(col:DaliOutputColumn)
            WHERE id(n) = $rid
            RETURN id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(col) AS tgtId, coalesce(col.name, col.col_key, '') AS tgtLabel, '' AS tgtScope,
                   'DaliOutputColumn' AS tgtType, 'HAS_OUTPUT_COL' AS edgeType
            LIMIT 200
            """;

        String stmtOutColQ = """
            MATCH (n:DaliStatement)-[:HAS_OUTPUT_COL]->(col:DaliOutputColumn)
            WHERE id(n) = $rid
            RETURN id(n) AS srcId, coalesce(n.stmt_geoid, n.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(col) AS tgtId, coalesce(col.name, col.col_key, '') AS tgtLabel, '' AS tgtScope,
                   'DaliOutputColumn' AS tgtType, 'HAS_OUTPUT_COL' AS edgeType
            LIMIT 100
            """;

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

        return Uni.combine().all()
            .unis(List.of(
                arcade.cypherIn(lineageDb(), outQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), inQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), outColQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), sibColQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), sibOutColQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), stmtOutColQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), hoistReadsQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), hoistWritesQ, params).onFailure().recoverWithItem(List.of())
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
            String srcType  = str(row, "srcType");
            if (srcType.isBlank()) srcType = rootType;
            String tgtId    = str(row, "tgtId");
            String tgtLabel = str(row, "tgtLabel");
            String tgtScope = str(row, "tgtScope");
            String tgtType  = str(row, "tgtType");
            String edgeType = str(row, "edgeType");

            // Optional PK/FK/dataType columns — only present in hasColQ (stmtColumns).
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
        return arcade.cypherIn(lineageDb(), cypher, Map.of("ids", tableIds))
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

    static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) return "";
        // labels()[0] returns a List<String> in ArcadeDB Cypher — unwrap first element
        if (v instanceof java.util.List<?> list) {
            Object first = list.isEmpty() ? null : list.get(0);
            return first != null ? first.toString() : "";
        }
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
