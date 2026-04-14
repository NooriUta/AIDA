package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.ExploreResult;

import java.util.List;
import java.util.Map;

/**
 * L3 — direct-edge lineage for any node (table, column, routine, statement…).
 *
 * Uses id(n) for vertex lookup — @rid is NOT valid in ArcadeDB Cypher WHERE clauses.
 * Bidirectional lineage uses two parallel queries merged in Java (no UNION) — avoids
 * ArcadeDB Cypher UNION deduplication issues when labels()[0] returns List<String> columns.
 */
@ApplicationScoped
public class LineageService {

    @Inject
    ArcadeGateway arcade;

    @Inject
    ExploreService exploreService;

    /**
     * Bidirectional 1-hop lineage — all edges incident to nodeId.
     *
     * Two separate queries (no UNION) run in parallel and merged in Java.
     * Avoids ArcadeDB Cypher UNION deduplication issues when labels()[0]
     * returns List<String> type columns — same pattern as SearchService.
     */
    @SuppressWarnings("unchecked")
    public Uni<ExploreResult> lineage(String nodeId) {
        Map<String, Object> params = Map.of("nodeId", nodeId);

        // Outgoing: nodeId is the source.
        // BUG-VC-002: Exclude DaliConstraint/DaliPrimaryKey/DaliForeignKey — created by
        // constraint model (commits 3849730/ef248cd) but not renderable in LOOM frontend.
        // DaliAtom — ANTLR4 atom nodes (50k+ vertices) not renderable in LOOM canvas.
        String outQ = """
            MATCH (n)-[r]->(m)
            WHERE id(n) = $nodeId
              AND (NOT m:DaliStatement OR coalesce(m.parent_statement, '') = '')
              AND NOT (m:DaliConstraint OR m:DaliPrimaryKey OR m:DaliForeignKey OR m:DaliAtom)
            RETURN id(n) AS srcId, labels(n)[0] AS srcType,
                   coalesce(n.table_name, n.column_name, n.routine_name,
                            n.package_name, n.stmt_geoid, n.app_name, n.schema_name, '') AS srcLabel,
                   id(m) AS tgtId, labels(m)[0] AS tgtType,
                   coalesce(m.table_name, m.column_name, m.routine_name,
                            m.package_name, m.stmt_geoid, m.app_name, m.schema_name, '') AS tgtLabel,
                   m.schema_geoid AS tgtScope, type(r) AS edgeType
            LIMIT 200
            """;

        // Incoming: nodeId is the destination — swap variables to avoid id(dst) pattern.
        // BUG-VC-002: Filter m (source) to exclude constraint nodes (IS_PK_COLUMN / IS_FK_COLUMN
        // edges from DaliPrimaryKey/DaliForeignKey would otherwise surface them as source nodes).
        // DaliAtom incoming via ATOM_REF_COLUMN edges — excluded same as in exploreByRid.
        String inQ = """
            MATCH (m)-[r]->(n)
            WHERE id(n) = $nodeId
              AND (NOT m:DaliStatement OR coalesce(m.parent_statement, '') = '')
              AND NOT (m:DaliConstraint OR m:DaliPrimaryKey OR m:DaliForeignKey OR m:DaliAtom)
            RETURN id(m) AS srcId, labels(m)[0] AS srcType,
                   coalesce(m.table_name, m.column_name, m.routine_name,
                            m.package_name, m.stmt_geoid, m.app_name, m.schema_name, '') AS srcLabel,
                   id(n) AS tgtId, labels(n)[0] AS tgtType,
                   coalesce(n.table_name, n.column_name, n.routine_name,
                            n.package_name, n.stmt_geoid, n.app_name, n.schema_name, '') AS tgtLabel,
                   n.schema_geoid AS tgtScope, type(r) AS edgeType
            LIMIT 200
            """;

        return Uni.combine().all()
            .unis(List.of(arcade.cypher(outQ, params), arcade.cypher(inQ, params)))
            .combinedWith(results -> {
                var all = new java.util.ArrayList<Map<String, Object>>();
                for (Object raw : results)
                    all.addAll((List<Map<String, Object>>) raw);
                return ExploreService.buildResult(all, nodeId, "");
            })
            .flatMap(exploreService::enrichDataSource);
    }

    /** Upstream only — what feeds into nodeId (incoming edges). Root statements only. */
    public Uni<ExploreResult> upstream(String nodeId) {
        // Use swapped variable so id(n) refers to the node we found, not the pattern destination
        // BUG-VC-002: Exclude DaliConstraint/DaliPrimaryKey/DaliForeignKey constraint nodes.
        // DaliAtom excluded — ANTLR4 atom nodes not renderable in LOOM canvas.
        String cypher = """
            MATCH (m)-[r]->(n)
            WHERE id(n) = $nodeId
              AND (NOT m:DaliStatement OR coalesce(m.parent_statement, '') = '')
              AND NOT (m:DaliConstraint OR m:DaliPrimaryKey OR m:DaliForeignKey OR m:DaliAtom)
            RETURN id(m) AS srcId, labels(m)[0] AS srcType,
                   coalesce(m.table_name, m.column_name, m.routine_name,
                            m.package_name, m.stmt_geoid, m.app_name, m.schema_name, '') AS srcLabel,
                   id(n) AS tgtId, labels(n)[0] AS tgtType,
                   coalesce(n.table_name, n.column_name, n.routine_name,
                            n.package_name, n.stmt_geoid, n.app_name, n.schema_name, '') AS tgtLabel,
                   n.schema_geoid AS tgtScope, type(r) AS edgeType
            LIMIT 200
            """;
        return arcade.cypher(cypher, Map.of("nodeId", nodeId))
                .map(rows -> ExploreService.buildResult(rows, nodeId, ""))
                .flatMap(exploreService::enrichDataSource);
    }

    /** Downstream only — what nodeId feeds into (outgoing edges). Root statements only. */
    public Uni<ExploreResult> downstream(String nodeId) {
        // BUG-VC-002: Exclude DaliConstraint/DaliPrimaryKey/DaliForeignKey constraint nodes.
        // DaliAtom excluded — ANTLR4 atom nodes not renderable in LOOM canvas.
        String cypher = """
            MATCH (n)-[r]->(m)
            WHERE id(n) = $nodeId
              AND (NOT m:DaliStatement OR coalesce(m.parent_statement, '') = '')
              AND NOT (m:DaliConstraint OR m:DaliPrimaryKey OR m:DaliForeignKey OR m:DaliAtom)
            RETURN id(n) AS srcId, labels(n)[0] AS srcType,
                   coalesce(n.table_name, n.column_name, n.routine_name,
                            n.package_name, n.stmt_geoid, n.app_name, n.schema_name, '') AS srcLabel,
                   id(m) AS tgtId, labels(m)[0] AS tgtType,
                   coalesce(m.table_name, m.column_name, m.routine_name,
                            m.package_name, m.stmt_geoid, m.app_name, m.schema_name, '') AS tgtLabel,
                   m.schema_geoid AS tgtScope, type(r) AS edgeType
            LIMIT 200
            """;
        return arcade.cypher(cypher, Map.of("nodeId", nodeId))
                .map(rows -> ExploreService.buildResult(rows, nodeId, ""))
                .flatMap(exploreService::enrichDataSource);
    }

    /**
     * Multi-hop bidirectional lineage — follows READS_FROM / WRITES_TO edges
     * up to {@code depth} hops from the starting node in both directions.
     *
     * Returns all distinct edges found along those paths so the frontend can
     * merge them into the existing L2 graph without rerunning ELK from scratch.
     * Depth is capped at 10 to prevent runaway traversals.
     */
    public Uni<ExploreResult> expandDeep(String nodeId, int depth) {
        int d = Math.max(1, Math.min(depth, 10));
        // ArcadeDB Cypher does not support $param inside *min..max, so we interpolate
        // the (validated, integer-only) depth directly into the query string.
        String cypher =
            "MATCH path = (start)-[:READS_FROM|WRITES_TO*1.." + d + "]-(end)\n" +
            "WHERE id(start) = $nodeId\n" +
            "  AND id(end) <> id(start)\n" +
            "  AND all(n IN nodes(path) WHERE n:DaliTable OR (n:DaliStatement AND coalesce(n.parent_statement, '') = ''))\n" +
            "UNWIND relationships(path) AS rel\n" +
            "WITH DISTINCT startNode(rel) AS s, endNode(rel) AS t, type(rel) AS et\n" +
            "RETURN id(s) AS srcId,\n" +
            "       coalesce(s.schema_name, s.table_name, s.stmt_geoid, s.snippet, '') AS srcLabel,\n" +
            "       labels(s)[0] AS srcType,\n" +
            "       id(t) AS tgtId,\n" +
            "       coalesce(t.schema_name, t.table_name, t.stmt_geoid, t.snippet, '') AS tgtLabel,\n" +
            "       coalesce(t.schema_geoid, '') AS tgtScope,\n" +
            "       labels(t)[0] AS tgtType,\n" +
            "       et AS edgeType\n" +
            "LIMIT 500";
        return arcade.cypher(cypher, Map.of("nodeId", nodeId))
                .map(rows -> ExploreService.buildResult(rows, nodeId, ""))
                .flatMap(exploreService::enrichDataSource);
    }
}
