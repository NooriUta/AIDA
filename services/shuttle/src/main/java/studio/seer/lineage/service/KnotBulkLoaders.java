package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.*;
import studio.seer.lineage.security.SeerIdentity;
import studio.seer.tenantrouting.YggLineageRegistry;

import java.util.*;

/**
 * Bulk loaders for KNOT report assembly — fetches large per-session result sets.
 *
 * <p>Extracted from {@link KnotColumnLineageService} (LOC refactor — QG-ARCH-INVARIANTS §2.4).
 * All six methods are package-private and called via delegation stubs in KnotColumnLineageService.
 */
@ApplicationScoped
class KnotBulkLoaders {

    @Inject ArcadeGateway      arcade;
    @Inject SeerIdentity       identity;
    @Inject YggLineageRegistry lineageRegistry;

    String lineageDb() {
        return lineageRegistry.resourceFor(identity.tenantAlias()).databaseName();
    }

    // ── G4: source_file-filtered overloads ──────────────────────────────────
    // Each delegates to the original when sourceFile is null; otherwise builds
    // Cypher/SQL with source_file property added to narrow scope to one file.

    Uni<List<KnotOutputColumn>>   loadOutputColumns(Map<String, Object> p, String sf)  { return sf == null ? loadOutputColumns(p)  : loadOutputColumnsFiltered(p); }
    Uni<List<KnotAffectedColumn>> loadAffectedColumns(Map<String, Object> p, String sf){ return sf == null ? loadAffectedColumns(p): loadAffectedColumnsFiltered(p); }
    Uni<KnotParamVars>            loadParamsAndVars(Map<String, Object> p, String sf)   { return sf == null ? loadParamsAndVars(p)  : loadParamsAndVarsFiltered(p); }
    Uni<List<KnotSnippet>>        loadSnippets(Map<String, Object> p, String sf)        { return sf == null ? loadSnippets(p)       : loadSnippetsFiltered(p); }
    Uni<List<KnotAtom>>           loadAtoms(Map<String, Object> p, String sf)           { return sf == null ? loadAtoms(p)          : loadAtomsFiltered(p); }
    Uni<List<KnotCall>>           loadCalls(Map<String, Object> p, String sf)           { return sf == null ? loadCalls(p)          : loadCallsFiltered(p); }

    // ── Output columns ────────────────────────────────────────────────────────

    Uni<List<KnotOutputColumn>> loadOutputColumns(Map<String, Object> params) {
        // Start from DaliStatement(session_id) index — avoids 3-hop traversal prefix.
        String cypher = """
            MATCH (stmt:DaliStatement {session_id: $sid})-[:HAS_OUTPUT_COL]->(oc:DaliOutputColumn)
            OPTIONAL MATCH (a:DaliAtom)-[:ATOM_PRODUCES]->(oc)
            RETURN stmt.stmt_geoid             AS stmtGeoid,
                   coalesce(oc.col_order, 0)   AS colOrder,
                   coalesce(oc.name, '')        AS name,
                   coalesce(oc.expression, '')  AS expression,
                   coalesce(oc.alias, '')       AS alias,
                   coalesce(oc.source_type, '') AS sourceType,
                   coalesce(oc.table_ref, '')   AS tableRef,
                   collect({
                       text:   a.atom_text,
                       col:    a.column_name,
                       tbl:    a.table_name,
                       status: a.status
                   })                          AS atoms
            ORDER BY stmtGeoid, colOrder
            LIMIT 5000
            """;

        return arcade.cypherIn(lineageDb(), cypher, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> {
                    // collect() with OPTIONAL MATCH returns [{text:null,...}] when no atom — filter out
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> rawAtoms =
                        (List<Map<String, Object>>) r.getOrDefault("atoms", List.of());

                    List<KnotOutputColumnAtom> atoms = rawAtoms.stream()
                        .filter(a -> a.get("text") != null)
                        .map(a -> new KnotOutputColumnAtom(
                            str(a, "text"),
                            str(a, "col"),
                            str(a, "tbl"),
                            str(a, "status")
                        ))
                        .toList();

                    return new KnotOutputColumn(
                        str(r, "stmtGeoid"),
                        str(r, "name"),
                        str(r, "expression"),
                        str(r, "alias"),
                        num(r, "colOrder"),
                        str(r, "sourceType"),
                        str(r, "tableRef"),
                        atoms
                    );
                })
                .toList()
            );
    }

    // ── Affected columns ──────────────────────────────────────────────────────

    Uni<List<KnotAffectedColumn>> loadAffectedColumns(Map<String, Object> params) {
        // Start from DaliStatement(session_id) index — avoids 3-hop traversal prefix.
        String cypher = """
            MATCH (stmt:DaliStatement {session_id: $sid})-[:HAS_AFFECTED_COL]->(col:DaliAffectedColumn)
            RETURN stmt.stmt_geoid                            AS stmtGeoid,
                   coalesce(col.column_name, '')              AS columnName,
                   coalesce(col.table_name, '')               AS tableName,
                   coalesce(col.position, 0)                  AS position
            ORDER BY stmtGeoid, position
            LIMIT 5000
            """;

        return arcade.cypherIn(lineageDb(), cypher, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> new KnotAffectedColumn(
                    str(r, "stmtGeoid"),
                    str(r, "columnName"),
                    str(r, "tableName"),
                    num(r, "position")
                ))
                .toList()
            );
    }

    // ── Parameters, Variables & Routines ─────────────────────────────────────

    Uni<KnotParamVars> loadParamsAndVars(Map<String, Object> params) {
        // All three use DaliRoutine(session_id) NOTUNIQUE index directly.
        // Was: 2-hop traversal Session→BELONGS_TO_SESSION→Routine.
        String cypherRoutines = """
            MATCH (r:DaliRoutine {session_id: $sid})
            RETURN r.routine_name                AS routineName,
                   coalesce(r.routine_type, '')  AS routineType,
                   coalesce(r.package_geoid, '') AS packageGeoid
            ORDER BY routineName
            LIMIT 5000
            """;

        String cypherParams = """
            MATCH (r:DaliRoutine {session_id: $sid})-[:HAS_PARAMETER]->(p:DaliParameter)
            RETURN r.routine_name                   AS routineName,
                   p.param_name                     AS paramName,
                   coalesce(p.data_type, '')         AS dataType,
                   coalesce(p.direction, '')         AS direction
            ORDER BY routineName, paramName
            LIMIT 5000
            """;

        String cypherVars = """
            MATCH (r:DaliRoutine {session_id: $sid})-[:HAS_VARIABLE]->(v:DaliVariable)
            RETURN r.routine_name                   AS routineName,
                   v.var_name                       AS varName,
                   coalesce(v.data_type, '')         AS dataType
            ORDER BY routineName, varName
            LIMIT 5000
            """;

        return Uni.combine().all()
            .unis(
                arcade.cypherIn(lineageDb(), cypherRoutines, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), cypherParams,   params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), cypherVars,     params).onFailure().recoverWithItem(List.of())
            )
            .asTuple()
            .map(t -> {
                List<KnotRoutine> rList = t.getItem1().stream()
                    .map(r -> new KnotRoutine(
                        str(r, "routineName"),
                        str(r, "routineType"),
                        str(r, "packageGeoid")
                    ))
                    .toList();

                List<KnotParameter> pList = t.getItem2().stream()
                    .map(r -> new KnotParameter(
                        str(r, "routineName"),
                        str(r, "paramName"),
                        str(r, "dataType"),
                        str(r, "direction")
                    ))
                    .toList();

                List<KnotVariable> vList = t.getItem3().stream()
                    .map(r -> new KnotVariable(
                        str(r, "routineName"),
                        str(r, "varName"),
                        str(r, "dataType")
                    ))
                    .toList();

                return new KnotParamVars(rList, pList, vList);
            });
    }

    // ── Snippets ──────────────────────────────────────────────────────────────

    Uni<List<KnotSnippet>> loadSnippets(Map<String, Object> params) {
        // DaliSnippet has no graph edges — DOCUMENT type joined by session_id + stmt_geoid.
        // Bulk load for knotReport: fetch all snippets for a session via session_id NOTUNIQUE index.
        //
        // BUG-SS-042: LIMIT 2000 was too low for large packages (e.g. >100 routines × 20+ stmts).
        // Snippets are small text records; 50 000 rows load in <100ms on typical hardware.
        // Alphabetical ORDER BY stmt_geoid means CURSORs (sorted after SELECT/INSERT) were cut off.
        String sql = """
            SELECT stmt_geoid, snippet
            FROM DaliSnippet
            WHERE session_id = :sid
            ORDER BY stmt_geoid
            LIMIT 50000
            """;

        return arcade.sqlIn(lineageDb(), sql, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> new KnotSnippet(str(r, "stmt_geoid"), str(r, "snippet")))
                .filter(s -> s.stmtGeoid() != null && !s.stmtGeoid().isBlank()
                          && s.snippet()   != null && !s.snippet().isBlank())
                .toList()
            );
    }

    // ── Atoms ─────────────────────────────────────────────────────────────────

    Uni<List<KnotAtom>> loadAtoms(Map<String, Object> params) {
        // Cypher instead of SQL — each graph edge traversal is an explicit OPTIONAL MATCH,
        // which is safe and portable. ArcadeDB SQL out()[0].field syntax is fragile
        // (returns null or wrong type when the edge list is empty).
        // Start from DaliAtom(session_id) NOTUNIQUE index — avoids 3-hop traversal prefix.
        // Reverse edge (a)<-[:HAS_ATOM]-(stmt) is a single RID lookup after index hit.
        String cypher = """
            MATCH (a:DaliAtom {session_id: $sid})<-[:HAS_ATOM]-(stmt:DaliStatement)
            OPTIONAL MATCH (a)-[:ATOM_PRODUCES]->(oc:DaliOutputColumn)
            OPTIONAL MATCH (a)-[:ATOM_REF_OUTPUT_COL]->(roc:DaliOutputColumn)
            OPTIONAL MATCH (a)-[:ATOM_REF_STMT]->(rs:DaliStatement)
            OPTIONAL MATCH (a)-[:ATOM_REF_COLUMN]->(rc:DaliColumn)
            OPTIONAL MATCH (a)-[:ATOM_REF_TABLE]->(rt:DaliTable)
            RETURN stmt.stmt_geoid                             AS stmtGeoid,
                   coalesce(a.atom_text, '')                   AS atomText,
                   coalesce(a.column_name, '')                 AS columnName,
                   coalesce(a.table_geoid, '')                 AS tableGeoid,
                   coalesce(a.table_name, '')                  AS tableName,
                   coalesce(a.status, '')                      AS status,
                   coalesce(a.atom_context, '')                AS atomContext,
                   coalesce(a.parent_context, '')              AS parentContext,
                   a.output_column_sequence                    AS outputColumnSequence,
                   coalesce(oc.name, '')                       AS outputColName,
                   coalesce(roc.output_col_name, roc.name, '') AS refSourceName,
                   coalesce(rs.stmt_geoid, '')                 AS refStmtGeoid,
                   coalesce(rc.column_name, '')                AS refColEdge,
                   coalesce(rt.table_name, '')                 AS refTblEdge,
                   coalesce(rt.table_geoid, '')                AS refTblGeoidEdge,
                   coalesce(a.is_column_reference, false)      AS isColumnRef,
                   coalesce(a.is_function_call, false)         AS isFuncCall,
                   coalesce(a.is_constant, false)              AS isConst,
                   coalesce(a.s_complex, false)                AS isComplex,
                   coalesce(a.is_routine_param, false)         AS isRoutineParam,
                   coalesce(a.is_routine_var, false)           AS isRoutineVar,
                   a.nested_atoms_count                        AS nestedAtomsCount
            LIMIT 5000
            """; // ORDER BY removed — Java re-sorts by stmtGeoid+atomLine+atomPos below

        return arcade.cypherIn(lineageDb(), cypher, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> {
                    String atomText = str(r, "atomText");
                    return new KnotAtom(
                        str(r, "stmtGeoid"),
                        atomText,
                        str(r, "columnName"),
                        str(r, "tableGeoid"),
                        str(r, "tableName"),
                        str(r, "status"),
                        str(r, "atomContext"),
                        str(r, "parentContext"),
                        intOrNull(r, "outputColumnSequence"),
                        str(r, "outputColName"),
                        str(r, "refSourceName"),
                        str(r, "refStmtGeoid"),
                        str(r, "refColEdge"),
                        str(r, "refTblEdge"),
                        str(r, "refTblGeoidEdge"),
                        bool(r, "isColumnRef"),
                        bool(r, "isFuncCall"),
                        bool(r, "isConst"),
                        bool(r, "isComplex"),
                        bool(r, "isRoutineParam"),
                        bool(r, "isRoutineVar"),
                        intOrNull(r, "nestedAtomsCount"),
                        atomLine(atomText),
                        atomPos(atomText)
                    );
                })
                .sorted(Comparator.comparing(KnotAtom::stmtGeoid)
                    .thenComparingInt(KnotAtom::atomLine)
                    .thenComparingInt(KnotAtom::atomPos))
                .toList()
            );
    }

    // ── Routine calls ─────────────────────────────────────────────────────────

    Uni<List<KnotCall>> loadCalls(Map<String, Object> params) {
        // CALLS edge: DaliRoutine -[CALLS]-> DaliRoutine (or any vertex)
        // Edge properties: callee_name, line_start; caller identified by caller node
        // Start from DaliRoutine(session_id) index — avoids 2-hop traversal prefix.
        String cypher = """
            MATCH (caller:DaliRoutine {session_id: $sid})-[c:CALLS]->(callee)
            RETURN caller.routine_name                             AS callerName,
                   coalesce(caller.package_geoid, '')             AS callerPackage,
                   coalesce(c.callee_name, callee.routine_name, '') AS calleeName,
                   coalesce(c.line_start, 0)                      AS lineStart
            ORDER BY callerName, lineStart
            LIMIT 5000
            """;

        return arcade.cypherIn(lineageDb(), cypher, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> new KnotCall(
                    str(r, "callerName"),
                    str(r, "callerPackage"),
                    str(r, "calleeName"),
                    num(r, "lineStart")
                ))
                .toList()
            );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // G4 — source_file filtered variants (batch per-file view)
    // All queries identical to the originals but with ", source_file: $sf" in property maps.
    // ══════════════════════════════════════════════════════════════════════════

    private Uni<List<KnotOutputColumn>> loadOutputColumnsFiltered(Map<String, Object> params) {
        String cypher = """
            MATCH (stmt:DaliStatement {session_id: $sid, source_file: $sf})-[:HAS_OUTPUT_COL]->(oc:DaliOutputColumn)
            OPTIONAL MATCH (a:DaliAtom)-[:ATOM_PRODUCES]->(oc)
            RETURN stmt.stmt_geoid             AS stmtGeoid,
                   coalesce(oc.col_order, 0)   AS colOrder,
                   coalesce(oc.name, '')        AS name,
                   coalesce(oc.expression, '')  AS expression,
                   coalesce(oc.alias, '')       AS alias,
                   coalesce(oc.source_type, '') AS sourceType,
                   coalesce(oc.table_ref, '')   AS tableRef,
                   collect({
                       text:   a.atom_text,
                       col:    a.column_name,
                       tbl:    a.table_name,
                       status: a.status
                   })                          AS atoms
            ORDER BY stmtGeoid, colOrder
            LIMIT 5000
            """;

        return arcade.cypherIn(lineageDb(), cypher, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> rawAtoms =
                        (List<Map<String, Object>>) r.getOrDefault("atoms", List.of());
                    List<KnotOutputColumnAtom> atoms = rawAtoms.stream()
                        .filter(a -> a.get("text") != null)
                        .map(a -> new KnotOutputColumnAtom(str(a, "text"), str(a, "col"), str(a, "tbl"), str(a, "status")))
                        .toList();
                    return new KnotOutputColumn(
                        str(r, "stmtGeoid"), str(r, "name"), str(r, "expression"),
                        str(r, "alias"), num(r, "colOrder"), str(r, "sourceType"), str(r, "tableRef"), atoms);
                })
                .toList()
            );
    }

    private Uni<List<KnotAffectedColumn>> loadAffectedColumnsFiltered(Map<String, Object> params) {
        String cypher = """
            MATCH (stmt:DaliStatement {session_id: $sid, source_file: $sf})-[:HAS_AFFECTED_COL]->(col:DaliAffectedColumn)
            RETURN stmt.stmt_geoid                            AS stmtGeoid,
                   coalesce(col.column_name, '')              AS columnName,
                   coalesce(col.table_name, '')               AS tableName,
                   coalesce(col.position, 0)                  AS position
            ORDER BY stmtGeoid, position
            LIMIT 5000
            """;

        return arcade.cypherIn(lineageDb(), cypher, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> new KnotAffectedColumn(str(r, "stmtGeoid"), str(r, "columnName"), str(r, "tableName"), num(r, "position")))
                .toList()
            );
    }

    private Uni<KnotParamVars> loadParamsAndVarsFiltered(Map<String, Object> params) {
        String cypherRoutines = """
            MATCH (r:DaliRoutine {session_id: $sid, source_file: $sf})
            RETURN r.routine_name                AS routineName,
                   coalesce(r.routine_type, '')  AS routineType,
                   coalesce(r.package_geoid, '') AS packageGeoid
            ORDER BY routineName
            LIMIT 5000
            """;

        String cypherParams = """
            MATCH (r:DaliRoutine {session_id: $sid, source_file: $sf})-[:HAS_PARAMETER]->(p:DaliParameter)
            RETURN r.routine_name                   AS routineName,
                   p.param_name                     AS paramName,
                   coalesce(p.data_type, '')         AS dataType,
                   coalesce(p.direction, '')         AS direction
            ORDER BY routineName, paramName
            LIMIT 5000
            """;

        String cypherVars = """
            MATCH (r:DaliRoutine {session_id: $sid, source_file: $sf})-[:HAS_VARIABLE]->(v:DaliVariable)
            RETURN r.routine_name                   AS routineName,
                   v.var_name                       AS varName,
                   coalesce(v.data_type, '')         AS dataType
            ORDER BY routineName, varName
            LIMIT 5000
            """;

        return Uni.combine().all()
            .unis(
                arcade.cypherIn(lineageDb(), cypherRoutines, params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), cypherParams,   params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), cypherVars,     params).onFailure().recoverWithItem(List.of())
            )
            .asTuple()
            .map(t -> {
                List<KnotRoutine> rList = t.getItem1().stream()
                    .map(r -> new KnotRoutine(str(r, "routineName"), str(r, "routineType"), str(r, "packageGeoid")))
                    .toList();
                List<KnotParameter> pList = t.getItem2().stream()
                    .map(r -> new KnotParameter(str(r, "routineName"), str(r, "paramName"), str(r, "dataType"), str(r, "direction")))
                    .toList();
                List<KnotVariable> vList = t.getItem3().stream()
                    .map(r -> new KnotVariable(str(r, "routineName"), str(r, "varName"), str(r, "dataType")))
                    .toList();
                return new KnotParamVars(rList, pList, vList);
            });
    }

    private Uni<List<KnotSnippet>> loadSnippetsFiltered(Map<String, Object> params) {
        String sql = """
            SELECT stmt_geoid, snippet
            FROM DaliSnippet
            WHERE session_id = :sid AND source_file = :sf
            ORDER BY stmt_geoid
            LIMIT 50000
            """;

        return arcade.sqlIn(lineageDb(), sql, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> new KnotSnippet(str(r, "stmt_geoid"), str(r, "snippet")))
                .filter(s -> s.stmtGeoid() != null && !s.stmtGeoid().isBlank()
                          && s.snippet()   != null && !s.snippet().isBlank())
                .toList()
            );
    }

    private Uni<List<KnotAtom>> loadAtomsFiltered(Map<String, Object> params) {
        String cypher = """
            MATCH (a:DaliAtom {session_id: $sid, source_file: $sf})<-[:HAS_ATOM]-(stmt:DaliStatement)
            OPTIONAL MATCH (a)-[:ATOM_PRODUCES]->(oc:DaliOutputColumn)
            OPTIONAL MATCH (a)-[:ATOM_REF_OUTPUT_COL]->(roc:DaliOutputColumn)
            OPTIONAL MATCH (a)-[:ATOM_REF_STMT]->(rs:DaliStatement)
            OPTIONAL MATCH (a)-[:ATOM_REF_COLUMN]->(rc:DaliColumn)
            OPTIONAL MATCH (a)-[:ATOM_REF_TABLE]->(rt:DaliTable)
            RETURN stmt.stmt_geoid                             AS stmtGeoid,
                   coalesce(a.atom_text, '')                   AS atomText,
                   coalesce(a.column_name, '')                 AS columnName,
                   coalesce(a.table_geoid, '')                 AS tableGeoid,
                   coalesce(a.table_name, '')                  AS tableName,
                   coalesce(a.status, '')                      AS status,
                   coalesce(a.atom_context, '')                AS atomContext,
                   coalesce(a.parent_context, '')              AS parentContext,
                   a.output_column_sequence                    AS outputColumnSequence,
                   coalesce(oc.name, '')                       AS outputColName,
                   coalesce(roc.output_col_name, roc.name, '') AS refSourceName,
                   coalesce(rs.stmt_geoid, '')                 AS refStmtGeoid,
                   coalesce(rc.column_name, '')                AS refColEdge,
                   coalesce(rt.table_name, '')                 AS refTblEdge,
                   coalesce(rt.table_geoid, '')                AS refTblGeoidEdge,
                   coalesce(a.is_column_reference, false)      AS isColumnRef,
                   coalesce(a.is_function_call, false)         AS isFuncCall,
                   coalesce(a.is_constant, false)              AS isConst,
                   coalesce(a.s_complex, false)                AS isComplex,
                   coalesce(a.is_routine_param, false)         AS isRoutineParam,
                   coalesce(a.is_routine_var, false)           AS isRoutineVar,
                   a.nested_atoms_count                        AS nestedAtomsCount
            LIMIT 5000
            """;

        return arcade.cypherIn(lineageDb(), cypher, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> {
                    String atomText = str(r, "atomText");
                    return new KnotAtom(
                        str(r, "stmtGeoid"), atomText, str(r, "columnName"),
                        str(r, "tableGeoid"), str(r, "tableName"), str(r, "status"),
                        str(r, "atomContext"), str(r, "parentContext"),
                        intOrNull(r, "outputColumnSequence"), str(r, "outputColName"),
                        str(r, "refSourceName"), str(r, "refStmtGeoid"),
                        str(r, "refColEdge"), str(r, "refTblEdge"), str(r, "refTblGeoidEdge"),
                        bool(r, "isColumnRef"), bool(r, "isFuncCall"), bool(r, "isConst"),
                        bool(r, "isComplex"), bool(r, "isRoutineParam"), bool(r, "isRoutineVar"),
                        intOrNull(r, "nestedAtomsCount"), atomLine(atomText), atomPos(atomText)
                    );
                })
                .sorted(Comparator.comparing(KnotAtom::stmtGeoid)
                    .thenComparingInt(KnotAtom::atomLine)
                    .thenComparingInt(KnotAtom::atomPos))
                .toList()
            );
    }

    private Uni<List<KnotCall>> loadCallsFiltered(Map<String, Object> params) {
        String cypher = """
            MATCH (caller:DaliRoutine {session_id: $sid, source_file: $sf})-[c:CALLS]->(callee)
            RETURN caller.routine_name                             AS callerName,
                   coalesce(caller.package_geoid, '')             AS callerPackage,
                   coalesce(c.callee_name, callee.routine_name, '') AS calleeName,
                   coalesce(c.line_start, 0)                      AS lineStart
            ORDER BY callerName, lineStart
            LIMIT 5000
            """;

        return arcade.cypherIn(lineageDb(), cypher, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> new KnotCall(str(r, "callerName"), str(r, "callerPackage"), str(r, "calleeName"), num(r, "lineStart")))
                .toList()
            );
    }

    // ── Row helpers ───────────────────────────────────────────────────────────

    private static String str(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v != null ? v.toString() : "";
    }

    private static int num(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v instanceof Number n) return n.intValue();
        return 0;
    }

    private static boolean bool(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v instanceof Boolean b) return b;
        return false;
    }

    private static Integer intOrNull(Map<String, Object> row, String key) {
        Object v = row.get(key);
        if (v instanceof Number n) return n.intValue();
        return null;
    }

    /**
     * Extract line number from atom_text: format is "NAME~line:pos" (e.g. "CODE~78:0").
     * Returns the integer before ':' after the last '~'. Returns 0 if not parseable.
     */
    private static int atomLine(String atomText) {
        if (atomText == null) return 0;
        int tilde = atomText.lastIndexOf('~');
        if (tilde < 0) return 0;
        String rest = atomText.substring(tilde + 1);
        int colon = rest.indexOf(':');
        String lineStr = colon >= 0 ? rest.substring(0, colon) : rest;
        try { return Integer.parseInt(lineStr); }
        catch (NumberFormatException ignored) { return 0; }
    }

    /**
     * Extract column position from atom_text: format is "NAME~line:pos" (e.g. "CODE~78:0").
     * Returns the integer after ':' after the last '~'. Returns 0 if not parseable.
     */
    private static int atomPos(String atomText) {
        if (atomText == null) return 0;
        int tilde = atomText.lastIndexOf('~');
        if (tilde < 0) return 0;
        String rest = atomText.substring(tilde + 1);
        int colon = rest.indexOf(':');
        if (colon < 0) return 0;
        try { return Integer.parseInt(rest.substring(colon + 1)); }
        catch (NumberFormatException ignored) { return 0; }
    }
}
