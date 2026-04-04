package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.ExploreResult;

import java.util.Map;

/**
 * L3 — direct-edge lineage for any node (table, column, routine, statement…).
 *
 * Uses id(n) for vertex lookup — @rid is NOT valid in ArcadeDB Cypher WHERE clauses.
 * Cypher UNION (deduplicating) is used for bidirectional edges; SQL UNION ALL is unsupported.
 */
@ApplicationScoped
public class LineageService {

    @Inject
    ArcadeGateway arcade;

    /** Bidirectional 1-hop lineage — all edges incident to nodeId. */
    public Uni<ExploreResult> lineage(String nodeId) {
        String cypher = """
            MATCH (src)-[r]->(dst)
            WHERE id(src) = $nodeId
            RETURN id(src) AS srcId, labels(src)[0] AS srcType,
                   coalesce(src.table_name, src.column_name, src.routine_name,
                            src.package_name, src.stmt_geoid, src.app_name, src.schema_name, '') AS srcLabel,
                   id(dst) AS tgtId, labels(dst)[0] AS tgtType,
                   coalesce(dst.table_name, dst.column_name, dst.routine_name,
                            dst.package_name, dst.stmt_geoid, dst.app_name, dst.schema_name, '') AS tgtLabel,
                   dst.schema_geoid AS tgtScope, type(r) AS edgeType
            UNION
            MATCH (src)-[r]->(dst)
            WHERE id(dst) = $nodeId
            RETURN id(src) AS srcId, labels(src)[0] AS srcType,
                   coalesce(src.table_name, src.column_name, src.routine_name,
                            src.package_name, src.stmt_geoid, src.app_name, src.schema_name, '') AS srcLabel,
                   id(dst) AS tgtId, labels(dst)[0] AS tgtType,
                   coalesce(dst.table_name, dst.column_name, dst.routine_name,
                            dst.package_name, dst.stmt_geoid, dst.app_name, dst.schema_name, '') AS tgtLabel,
                   dst.schema_geoid AS tgtScope, type(r) AS edgeType
            """;
        return arcade.cypher(cypher, Map.of("nodeId", nodeId))
                .map(rows -> ExploreService.buildResult(rows, nodeId, ""));
    }

    /** Upstream only — what feeds into nodeId (incoming DATA_FLOW / READS_FROM paths). */
    public Uni<ExploreResult> upstream(String nodeId) {
        String cypher = """
            MATCH (src)-[r]->(dst)
            WHERE id(dst) = $nodeId
            RETURN id(src) AS srcId, labels(src)[0] AS srcType,
                   coalesce(src.table_name, src.column_name, src.routine_name,
                            src.package_name, src.stmt_geoid, '') AS srcLabel,
                   id(dst) AS tgtId, labels(dst)[0] AS tgtType,
                   coalesce(dst.table_name, dst.column_name, dst.routine_name,
                            dst.package_name, dst.stmt_geoid, '') AS tgtLabel,
                   dst.schema_geoid AS tgtScope, type(r) AS edgeType
            """;
        return arcade.cypher(cypher, Map.of("nodeId", nodeId))
                .map(rows -> ExploreService.buildResult(rows, nodeId, ""));
    }

    /** Downstream only — what nodeId feeds into (outgoing DATA_FLOW / WRITES_TO paths). */
    public Uni<ExploreResult> downstream(String nodeId) {
        String cypher = """
            MATCH (src)-[r]->(dst)
            WHERE id(src) = $nodeId
            RETURN id(src) AS srcId, labels(src)[0] AS srcType,
                   coalesce(src.table_name, src.column_name, src.routine_name,
                            src.package_name, src.stmt_geoid, '') AS srcLabel,
                   id(dst) AS tgtId, labels(dst)[0] AS tgtType,
                   coalesce(dst.table_name, dst.column_name, dst.routine_name,
                            dst.package_name, dst.stmt_geoid, '') AS tgtLabel,
                   dst.schema_geoid AS tgtScope, type(r) AS edgeType
            """;
        return arcade.cypher(cypher, Map.of("nodeId", nodeId))
                .map(rows -> ExploreService.buildResult(rows, nodeId, ""));
    }
}
