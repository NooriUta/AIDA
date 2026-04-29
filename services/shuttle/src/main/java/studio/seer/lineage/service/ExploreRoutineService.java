package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.ExploreResult;
import studio.seer.lineage.model.GraphNode;
import studio.seer.lineage.security.SeerIdentity;
import studio.seer.tenantrouting.YggLineageRegistry;

import java.util.*;

/**
 * Routine-focused explore methods extracted from ExploreService to keep
 * each class under the 500-LOC limit.
 *
 * Contains:
 *   - {@link #exploreRoutineAggregate(String)} (L2 aggregate)
 *   - {@link #exploreRoutineScope(String)} (L3 drill)
 *   - {@link #exploreRoutineDetail(String)} (routine inspector)
 */
@ApplicationScoped
public class ExploreRoutineService {

    private static final Logger log = Logger.getLogger(ExploreRoutineService.class);

    @Inject ArcadeGateway      arcade;
    @Inject SeerIdentity       identity;
    @Inject YggLineageRegistry lineageRegistry;

    String lineageDb() {
        return lineageRegistry.resourceFor(identity.tenantAlias()).databaseName();
    }

    // ── Routine aggregate scope (new L2) ─────────────────────────────────────

    public Uni<ExploreResult> exploreRoutineAggregate(String scope) {
        ExploreService.ScopeRef ref = ExploreService.ScopeRef.parse(scope);
        boolean isPackage = "pkg".equals(ref.type());
        String scopeName  = ref.name();

        String cypher;
        Map<String, Object> params;
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

        final String finalScopeName = scopeName;
        final boolean finalIsPackage = isPackage;

        Uni<List<Map<String, Object>>> mainQuery = arcade.cypherIn(lineageDb(), cypher, params);

        Uni<List<Map<String, Object>>> extQuery = finalIsPackage
            ? Uni.createFrom().item(List.of())
            : arcade.cypherIn(lineageDb(), """
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
        Uni<List<Map<String, Object>>> callsQuery = arcade.cypherIn(lineageDb(), callsCypher, params)
            .onFailure().recoverWithItem(List.of());

        return Uni.combine().all().unis(List.of(mainQuery, extQuery, callsQuery))
            .combinedWith(results -> {
                @SuppressWarnings("unchecked")
                var mainRows  = (List<Map<String, Object>>) results.get(0);
                @SuppressWarnings("unchecked")
                var extRows   = (List<Map<String, Object>>) results.get(1);
                @SuppressWarnings("unchecked")
                var callRows  = (List<Map<String, Object>>) results.get(2);

                var allRows = new ArrayList<Map<String, Object>>(mainRows);
                allRows.addAll(extRows);

                var flatRows = new ArrayList<Map<String, Object>>();

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

                    var selfRow = new HashMap<String, Object>();
                    selfRow.put("srcId",       rId);
                    selfRow.put("srcLabel",    rLabel);
                    selfRow.put("srcType",     "DaliRoutine");
                    selfRow.put("srcScope",    rSchema);
                    selfRow.put("srcPackage",  rPkg);
                    selfRow.put("srcKind",     rKind);
                    selfRow.put("tgtId",       rId);
                    selfRow.put("tgtLabel",    rLabel);
                    selfRow.put("tgtScope",    "");
                    selfRow.put("tgtType",     "DaliRoutine");
                    selfRow.put("edgeType",    "NODE_ONLY");
                    selfRow.put("sourceHandle", "");
                    selfRow.put("targetHandle", "");
                    flatRows.add(selfRow);

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

                flatRows.addAll(callRows);

                return ExploreService.buildResult(flatRows, finalScopeName,
                        finalIsPackage ? "DaliPackage" : "DaliSchema");
            })
            .flatMap(this::enrichDataSource);
    }

    // ── Routine scope (L3 drill) ──────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Uni<ExploreResult> exploreRoutineScope(String routineRid) {
        Map<String, Object> params = Map.of("rid", routineRid);

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
                arcade.cypherIn(lineageDb(), stmtsQ,        params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), directReadsQ,  params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), directWritesQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), hoistReadsQ,   params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), recordSelfQ,   params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), recordFieldsQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), bulkCollectsQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), returnsIntoQ,  params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), recordUsedInQ, params).onFailure().recoverWithItem(List.of())
            ))
            .combinedWith(results -> {
                var all = new ArrayList<Map<String, Object>>();
                for (Object raw : results)
                    all.addAll((List<Map<String, Object>>) raw);
                return ExploreService.buildResult(all, routineRid, "DaliStatement");
            })
            .flatMap(this::enrichDataSource);
    }

    // ── Routine detail ────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    public Uni<ExploreResult> exploreRoutineDetail(String nodeId) {
        Map<String, Object> params = Map.of("rid", nodeId);

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
                arcade.cypherIn(lineageDb(), paramsQ,   params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), varsQ,     params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), stmtsQ,    params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), callsOutQ, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), callsInQ,  params).onFailure().recoverWithItem(List.of())
            ))
            .combinedWith(results -> {
                var all = new ArrayList<Map<String, Object>>();
                for (Object raw : results)
                    all.addAll((List<Map<String, Object>>) raw);
                return ExploreService.buildResult(all, nodeId, "DaliRoutine");
            });
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /** Build one (routine → table) row in the uniform format buildResult() expects. */
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

    private static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v == null) return "";
        if (v instanceof java.util.List<?> list) return list.isEmpty() ? "" : list.get(0).toString();
        return v.toString();
    }

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
}
