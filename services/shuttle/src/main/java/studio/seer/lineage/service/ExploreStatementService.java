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
 * Statement/column/package explore methods extracted from ExploreService to keep
 * each class under the 500-LOC limit.
 *
 * Contains:
 *   - {@link #exploreStatementTree(String)} (L4 statement tree)
 *   - {@link #explorePackageRecords(String)} (package record support)
 *   - {@link #exploreStmtColumns(List)} (column enrichment)
 *   - {@link #explorePackage(String)} (package scope)
 */
@ApplicationScoped
public class ExploreStatementService {

    private static final Logger log = Logger.getLogger(ExploreStatementService.class);

    @Inject ArcadeGateway      arcade;
    @Inject SeerIdentity       identity;
    @Inject YggLineageRegistry lineageRegistry;

    String lineageDb() {
        return lineageRegistry.resourceFor(identity.tenantAlias()).databaseName();
    }

    // ── L4: Statement subquery tree ───────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Uni<ExploreResult> exploreStatementTree(String stmtId) {
        if (stmtId == null || stmtId.isBlank()) {
            return Uni.createFrom().item(new ExploreResult(List.of(), List.of(), false));
        }
        Map<String, Object> params = Map.of("stmtId", stmtId);

        String rootQ = """
            MATCH (root:DaliStatement)
            WHERE id(root) = $stmtId
            RETURN id(root) AS srcId,
                   coalesce(root.stmt_geoid, root.snippet, '') AS srcLabel, 'DaliStatement' AS srcType,
                   id(root) AS tgtId,
                   coalesce(root.stmt_geoid, root.snippet, '') AS tgtLabel, '' AS tgtScope,
                   'DaliStatement' AS tgtType, 'NODE_ONLY' AS edgeType
            """;

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
                arcade.cypherIn(lineageDb(), rootQ,     params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), childQ,    params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), readsQ,    params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), outColQ,   params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), dataFlowQ, params).onFailure().recoverWithItem(List.of())
            ))
            .combinedWith(results -> {
                var all = new ArrayList<Map<String, Object>>();
                for (Object raw : results)
                    all.addAll((List<Map<String, Object>>) raw);
                return ExploreService.buildResult(all, stmtId, "DaliStatement");
            })
            .flatMap(this::enrichDataSource);
    }

    // ── L2: DaliRecord support — package scope ────────────────────────────────

    @SuppressWarnings("unchecked")
    Uni<ExploreResult> explorePackageRecords(String packageName) {
        Map<String, Object> params = Map.of("pkg", packageName);

        String recNodeQ = """
            MATCH (p:DaliPackage {package_name: $pkg})-[:CONTAINS_ROUTINE]->(r:DaliRoutine)
            MATCH (rec:DaliRecord)
            WHERE rec.routine_geoid = r.routine_geoid
            RETURN id(r) AS srcId, r.routine_name AS srcLabel, 'DaliRoutine' AS srcType,
                   id(rec) AS tgtId, coalesce(rec.record_name, '') AS tgtLabel, '' AS tgtScope,
                   'DaliRecord' AS tgtType, 'CONTAINS_RECORD' AS edgeType
            LIMIT 300
            """;

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
                arcade.cypherIn(lineageDb(), recNodeQ,     params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), recFieldQ,    params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), returnsIntoQ, params).onFailure().recoverWithItem(List.of())
            ))
            .combinedWith(results -> {
                var all = new ArrayList<Map<String, Object>>();
                for (Object raw : results)
                    all.addAll((List<Map<String, Object>>) raw);
                return ExploreService.buildResult(all, packageName, "DaliPackage");
            });
    }

    // ── Statement columns (second-pass enrichment) ───────────────────────────

    @SuppressWarnings("unchecked")
    public Uni<ExploreResult> exploreStmtColumns(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Uni.createFrom().item(new ExploreResult(List.of(), List.of(), false));
        }
        Map<String, Object> params = Map.of("ids", ids);

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

        @SuppressWarnings("unchecked")
        Uni<List<Map<String, Object>>> countUni = arcade.cypherIn(lineageDb(), countColQ, params)
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
                arcade.cypherIn(lineageDb(), hasColQ,    params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), hasOutColQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), hasAffColQ, params).onFailure().recoverWithItem(List.of())
            ))
            .combinedWith(results -> {
                var all = new java.util.ArrayList<Map<String, Object>>();
                for (int i = 1; i < results.size(); i++)
                    all.addAll((List<Map<String, Object>>) results.get(i));
                return ExploreService.buildResult(all, "", "DaliTable");
            })
            .flatMap(this::enrichDataSource);
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
            MATCH (rootStmt)<-[:CHILD_OF*0..30]-(sub:DaliStatement)-[:READS_FROM]->(t:DaliTable)
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

        return arcade.cypherIn(lineageDb(), cypher, Map.of("dbName", dbName))
            .map(rows -> ExploreService.buildResult(rows, dbName, "DaliDatabase"))
            .flatMap(this::enrichDataSource);
    }

    // ── Package scope ─────────────────────────────────────────────────────────

    Uni<ExploreResult> explorePackage(String packageName) {
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

        var baseUni    = arcade.cypherIn(lineageDb(), cypher, Map.of("pkg", packageName));
        var recordsUni = explorePackageRecords(packageName).onFailure().recoverWithItem(
            new ExploreResult(List.of(), List.of(), false));

        return Uni.combine().all().unis(baseUni, recordsUni).asTuple()
            .map(tuple -> {
                var baseRows = tuple.getItem1();
                ExploreResult base    = ExploreService.buildResult(baseRows, packageName, "DaliPackage");
                ExploreResult records = tuple.getItem2();
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
