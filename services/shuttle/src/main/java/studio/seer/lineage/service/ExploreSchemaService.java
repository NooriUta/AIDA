package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.ExploreResult;
import studio.seer.lineage.model.GraphNode;
import studio.seer.lineage.security.SeerIdentity;
import studio.seer.tenantrouting.YggLineageRegistry;

import java.util.*;

/**
 * Schema-scoped explore methods extracted from {@link ExploreStatementService}
 * (LOC refactor — QG-ARCH-INVARIANTS §2.4).
 *
 * <p>Handles {@link #exploreSchema} (both overloads) and {@link #exploreByDatabase}.
 * Delegation stubs in ExploreStatementService preserve the package-private API
 * consumed by ExploreService — no caller changes required.
 */
@ApplicationScoped
class ExploreSchemaService {

    @Inject ArcadeGateway      arcade;
    @Inject SeerIdentity       identity;
    @Inject YggLineageRegistry lineageRegistry;

    String lineageDb() {
        return lineageRegistry.resourceFor(identity.tenantAlias()).databaseName();
    }

    // ── Schema scope ──────────────────────────────────────────────────────────

    Uni<ExploreResult> exploreSchema(String schemaName, String dbName) {
        return exploreSchema(schemaName, dbName, false);
    }

    /**
     * Extra UNION ALL branches appended when includeExternal=true.
     * See ExploreService.EXTERNAL_EXTENSION design note for details.
     */
    private static final String EXTERNAL_EXTENSION = """
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_ROUTINE]->(r1)-[:CONTAINS_ROUTINE*0..1]->(rr:DaliRoutine)
                  -[:CONTAINS_STMT]->(rootStmt:DaliStatement)
            WHERE coalesce(rootStmt.parent_statement, '') = ''
            MATCH (rootStmt)<-[:CHILD_OF*0..30]-(sub:DaliStatement), (t:DaliTable)-[:READS_FROM]->(sub)
            WHERE t.schema_geoid IS NOT NULL AND t.schema_geoid <> $schema
            RETURN DISTINCT id(rootStmt) AS srcId, coalesce(rootStmt.stmt_geoid, rootStmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 2000
            UNION ALL
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

    Uni<ExploreResult> exploreSchema(String schemaName, String dbName, boolean includeExternal) {
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
            MATCH (s)-[:CONTAINS_TABLE]->(t:DaliTable), (t)-[:READS_FROM]->(stmt:DaliStatement)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 3000
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_TABLE]->(t:DaliTable)
            MATCH (t)-[:READS_FROM]->(:DaliStatement)-[:CHILD_OF*1..30]->(rootStmt:DaliStatement)
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
            MATCH (s)-[:CONTAINS_TABLE]->(srcT:DaliTable)-[:READS_FROM]->(stmt:DaliStatement)
                  -[:WRITES_TO]->(target:DaliTable)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(target) AS tgtId, target.table_name AS tgtLabel, target.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'WRITES_TO' AS edgeType
            LIMIT 200
            """ + (includeExternal ? EXTERNAL_EXTENSION : "");

        Map<String, Object> params = Map.of(
            "schema", schemaName,
            "dbName", dbName == null || dbName.isBlank() ? "" : dbName
        );

        String columnFlowCypher = """
            MATCH (s:DaliSchema)
            WHERE s.schema_geoid = $schema AND ($dbName = '' OR s.db_name = $dbName)
            MATCH (s)-[:CONTAINS_TABLE]->(srcTbl:DaliTable)-[:HAS_COLUMN]->(srcCol:DaliColumn)
            MATCH (srcCol)-[:DATA_FLOW]->(oc:DaliOutputColumn)<-[:HAS_OUTPUT_COL]-(sub:DaliStatement)
            MATCH (sub)-[:CHILD_OF*0..30]->(root:DaliStatement)
            WHERE coalesce(root.parent_statement, '') = ''
            OPTIONAL MATCH (root)-[:HAS_AFFECTED_COL]->(rootAff:DaliAffectedColumn)
                WHERE toUpper(last(split(coalesce(rootAff.column_name, ''), '.'))) = toUpper(last(split(coalesce(oc.name, oc.col_key, ''), '.')))
            OPTIONAL MATCH (root)-[:HAS_OUTPUT_COL]->(rootOc:DaliOutputColumn)
                WHERE toUpper(last(split(coalesce(rootOc.name, rootOc.col_key, ''), '.'))) = toUpper(last(split(coalesce(oc.name, oc.col_key, ''), '.')))
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

        var baseUni = arcade.cypherIn(lineageDb(), cypher, params);
        var colUni  = arcade.cypherIn(lineageDb(), columnFlowCypher, params).onFailure().recoverWithItem(List.of());

        return Uni.combine().all().unis(baseUni, colUni).asTuple()
            .map(tuple -> {
                var merged = new ArrayList<Map<String, Object>>(tuple.getItem1().size() + tuple.getItem2().size());
                merged.addAll(tuple.getItem1());
                merged.addAll(tuple.getItem2());
                return ExploreService.buildResult(merged, schemaName, "DaliSchema");
            })
            .flatMap(this::enrichDataSource);
    }

    // ── Database scope (all schemas in a DB) ─────────────────────────────────

    /**
     * Explores all schemas within a database.
     * Equivalent to running exploreSchema for every DaliSchema where db_name = $dbName,
     * but in a single set of queries.
     */
    Uni<ExploreResult> exploreByDatabase(String dbName) {
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
            MATCH (s)-[:CONTAINS_TABLE]->(t:DaliTable), (t)-[:READS_FROM]->(stmt:DaliStatement)
            WHERE coalesce(stmt.parent_statement, '') = ''
            RETURN DISTINCT id(stmt) AS srcId, coalesce(stmt.stmt_geoid, stmt.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(t) AS tgtId, t.table_name AS tgtLabel, t.schema_geoid AS tgtScope,
                   'DaliTable' AS tgtType, 'READS_FROM' AS edgeType
            LIMIT 3000
            UNION ALL
            MATCH (s:DaliSchema)
            WHERE s.db_name = $dbName
            MATCH (s)-[:CONTAINS_TABLE]->(t:DaliTable)
            MATCH (t)-[:READS_FROM]->(:DaliStatement)-[:CHILD_OF*1..30]->(rootStmt:DaliStatement)
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

        return arcade.cypherIn(lineageDb(), cypher, Map.of("dbName", dbName))
            .map(rows -> ExploreService.buildResult(rows, dbName, "DaliDatabase"))
            .flatMap(this::enrichDataSource);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private Uni<ExploreResult> enrichDataSource(ExploreResult result) {
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

    private static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) return "";
        if (v instanceof java.util.List<?> list) {
            Object first = list.isEmpty() ? null : list.get(0);
            return first != null ? first.toString() : "";
        }
        return v.toString();
    }
}
