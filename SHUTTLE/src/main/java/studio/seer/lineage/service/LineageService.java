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

        // Outgoing: nodeId is the source
        String outQ = """
            MATCH (n)-[r]->(m)
            WHERE id(n) = $nodeId
            RETURN id(n) AS srcId, labels(n)[0] AS srcType,
                   coalesce(n.table_name, n.column_name, n.routine_name,
                            n.package_name, n.stmt_geoid, n.app_name, n.schema_name, '') AS srcLabel,
                   id(m) AS tgtId, labels(m)[0] AS tgtType,
                   coalesce(m.table_name, m.column_name, m.routine_name,
                            m.package_name, m.stmt_geoid, m.app_name, m.schema_name, '') AS tgtLabel,
                   m.schema_geoid AS tgtScope, type(r) AS edgeType
            LIMIT 200
            """;

        // Incoming: nodeId is the destination — swap variables to avoid id(dst) pattern
        String inQ = """
            MATCH (m)-[r]->(n)
            WHERE id(n) = $nodeId
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
            });
    }

    /** Upstream only — what feeds into nodeId (incoming edges). */
    public Uni<ExploreResult> upstream(String nodeId) {
        // Use swapped variable so id(n) refers to the node we found, not the pattern destination
        String cypher = """
            MATCH (m)-[r]->(n)
            WHERE id(n) = $nodeId
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
                .map(rows -> ExploreService.buildResult(rows, nodeId, ""));
    }

    /** Downstream only — what nodeId feeds into (outgoing edges). */
    public Uni<ExploreResult> downstream(String nodeId) {
        String cypher = """
            MATCH (n)-[r]->(m)
            WHERE id(n) = $nodeId
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
                .map(rows -> ExploreService.buildResult(rows, nodeId, ""));
    }
}
