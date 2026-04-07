package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.*;

import java.util.*;

/**
 * KNOT Report service — session-level analytics for parsed Hound data.
 *
 * Confirmed ArcadeDB schema (verified 2026-04-06 against live hound DB, 241 sessions, 561995 atoms):
 *
 * ┌──────────────────┬─────────────────────────┬─────────────────────────────────────────────────────┐
 * │ Entity           │ Fields (populated)      │ Deficiencies found                                  │
 * ├──────────────────┼─────────────────────────┼─────────────────────────────────────────────────────┤
 * │ DaliSession      │ session_id, file_path,  │ session_name = null (all rows) → derive from        │
 * │                  │ dialect                 │ file_path; processing_ms = null                     │
 * ├──────────────────┼─────────────────────────┼─────────────────────────────────────────────────────┤
 * │ DaliRoutine      │ routine_name,           │ No deficiencies found                               │
 * │                  │ routine_type            │                                                     │
 * ├──────────────────┼─────────────────────────┼─────────────────────────────────────────────────────┤
 * │ DaliStatement    │ stmt_geoid              │ stmt_type = null (all rows) → parse from geoid[3]   │
 * │                  │                         │ line_number = null (all rows) → parse from geoid[4] │
 * ├──────────────────┼─────────────────────────┼─────────────────────────────────────────────────────┤
 * │ DaliAtom         │ status                  │ atom_type = null (all rows) — type is encoded in    │
 * │                  │                         │ status: 'Обработано'|'unresolved'|'constant'|        │
 * │                  │                         │ 'function_call' (NOT the English values in plan)    │
 * ├──────────────────┼─────────────────────────┼─────────────────────────────────────────────────────┤
 * │ DaliTable        │ table_name, table_geoid,│ No direct BELONGS_TO_SESSION edges (count = 0) →   │
 * │                  │ schema_geoid            │ must traverse via Routine→Statement→READS_FROM       │
 * ├──────────────────┼─────────────────────────┼─────────────────────────────────────────────────────┤
 * │ DaliColumn       │ column_name             │ data_type = null, position = null (all rows)        │
 * ├──────────────────┼─────────────────────────┼─────────────────────────────────────────────────────┤
 * │ DaliPackage      │ (not verified)          │ NOT in BELONGS_TO_SESSION path — edge goes directly │
 * │                  │                         │ Session→Routine, DaliPackage is orphaned here        │
 * └──────────────────┴─────────────────────────┴─────────────────────────────────────────────────────┘
 *
 * Edge topology (verified):
 *   DaliSession   -[BELONGS_TO_SESSION]-> DaliRoutine      (NOT via DaliPackage — was wrong in plan)
 *   DaliRoutine   -[CONTAINS_STMT]->      DaliStatement
 *   DaliStatement -[CHILD_OF]->           DaliStatement    (child→parent; was inverted in original query)
 *   DaliStatement -[READS_FROM]->         DaliTable
 *   DaliStatement -[WRITES_TO]->          DaliTable
 *   DaliStatement -[HAS_ATOM]->           DaliAtom
 *   DaliTable     -[HAS_COLUMN]->         DaliColumn
 *   DaliRoutine   -[HAS_PARAMETER]->      DaliParameter
 *   DaliRoutine   -[HAS_VARIABLE]->       DaliVariable
 *
 * stmt_geoid format: "SCHEMA.PACKAGE:ROUTINE_TYPE:ROUTINE_NAME:STMT_TYPE:LINE[:nested...]"
 *   e.g. "DWH.CALC_PKL_CRED:PROCEDURE:CALC_AGG:INSERT:152"
 *   Nested: "DWH.CALC_PKL_CRED:PROCEDURE:CALC_AGG:INSERT:2333:SELECT:2336:ACC_DEAL:2370:SQ:2372"
 */
@ApplicationScoped
public class KnotService {

    @Inject
    ArcadeGateway arcade;

    // ── Session list ──────────────────────────────────────────────────────────

    public Uni<List<KnotSession>> knotSessions() {
        String sql = """
            SELECT
                @rid                                              AS id,
                session_id,
                session_name,
                coalesce(dialect, 'plsql')                       AS dialect,
                coalesce(file_path, '')                          AS file_path,
                coalesce(processing_ms, 0)                       AS processing_ms
            FROM DaliSession
            ORDER BY file_path
            """;

        return arcade.sql(sql).map(rows -> rows.stream()
            .map(r -> {
                String filePath    = str(r, "file_path");
                String sessionName = deriveName(str(r, "session_name"), filePath);
                return new KnotSession(
                    str(r, "id"),
                    str(r, "session_id"),
                    sessionName,
                    str(r, "dialect"),
                    filePath,
                    num(r, "processing_ms"),
                    0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0, 0, 0,
                    0, 0, 0, 0, 0,
                    0, 0, 0, 0
                );
            })
            .toList()
        );
    }

    // ── Full report ───────────────────────────────────────────────────────────

    public Uni<KnotReport> knotReport(String sessionId) {
        Map<String, Object> params = Map.of("sid", sessionId);

        Uni<KnotSession>             sessionUni    = loadSession(sessionId, params);
        Uni<List<KnotTable>>         tablesUni     = loadTables(params);
        Uni<List<KnotStatement>>     statementsUni = loadStatements(params);
        Uni<List<KnotSnippet>>       snippetsUni   = loadSnippets(params);
        Uni<List<KnotAtom>>          atomsUni      = loadAtoms(params);
        Uni<List<KnotOutputColumn>>  outColsUni    = loadOutputColumns(params);
        Uni<List<KnotCall>>          callsUni      = loadCalls(params);
        Uni<KnotParamVars>           paramVarsUni  = loadParamsAndVars(params);

        return Uni.combine().all()
            .unis(sessionUni, tablesUni, statementsUni, snippetsUni, atomsUni, outColsUni, callsUni, paramVarsUni)
            .asTuple()
            .map(t -> {
                KnotParamVars pv = t.getItem8();
                return new KnotReport(
                    t.getItem1(), t.getItem2(), t.getItem3(),
                    t.getItem4(), t.getItem5(), t.getItem6(), t.getItem7(),
                    pv.parameters(), pv.variables()
                );
            });
    }

    /** Internal holder for params + vars combined query. */
    private record KnotParamVars(List<KnotParameter> parameters, List<KnotVariable> variables) {}

    // ── Session summary ───────────────────────────────────────────────────────

    private Uni<KnotSession> loadSession(String sessionId, Map<String, Object> params) {

        // ArcadeDB SQL uses :param syntax; Cypher uses $param
        String sqlMeta = """
            SELECT @rid AS id, session_id, session_name,
                   coalesce(dialect,'plsql') AS dialect,
                   coalesce(file_path,'')    AS file_path,
                   coalesce(processing_ms,0) AS processing_ms
            FROM DaliSession WHERE session_id = :sid
            LIMIT 1
            """;

        // Routine count — edge: DaliSession-[BELONGS_TO_SESSION]->DaliRoutine
        String sqlRoutineCount = """
            SELECT count(*) AS routineCount
            FROM DaliRoutine
            WHERE in('BELONGS_TO_SESSION')[session_id = :sid].size() > 0
            """;

        // Stmt geoids — stmt_type and line_number are null in DB, parse from geoid
        String sqlStmtGeoids = """
            SELECT stmt_geoid
            FROM DaliStatement
            WHERE in('CONTAINS_STMT').in('BELONGS_TO_SESSION')[session_id = :sid].size() > 0
              AND outE('CHILD_OF').size() = 0
            """;

        // Atom counts grouped by status
        String sqlAtoms = """
            SELECT status, count(*) AS cnt
            FROM DaliAtom
            WHERE in('HAS_ATOM').in('CONTAINS_STMT').in('BELONGS_TO_SESSION')[session_id = :sid].size() > 0
            GROUP BY status
            """;

        return Uni.combine().all()
            .unis(
                arcade.sql(sqlMeta, params).onFailure().recoverWithItem(List.of()),
                arcade.sql(sqlRoutineCount, params).onFailure().recoverWithItem(List.of()),
                arcade.sql(sqlStmtGeoids, params).onFailure().recoverWithItem(List.of()),
                arcade.sql(sqlAtoms, params).onFailure().recoverWithItem(List.of())
            )
            .asTuple()
            .map(t -> {
                List<Map<String, Object>> metaRows     = t.getItem1();
                List<Map<String, Object>> routineRows  = t.getItem2();
                List<Map<String, Object>> stmtRows     = t.getItem3();
                List<Map<String, Object>> atomRows     = t.getItem4();

                if (metaRows.isEmpty()) return emptySession(sessionId);

                Map<String, Object> meta = metaRows.get(0);

                int routineCount = routineRows.isEmpty() ? 0 : num(routineRows.get(0), "routineCount");

                // Parse stmt type breakdown from geoids
                int sel = 0, ins = 0, upd = 0, del = 0, mer = 0, cur = 0, oth = 0;
                for (var row : stmtRows) {
                    String geoid = str(row, "stmt_geoid");
                    String stype = parseStmtType(geoid).toUpperCase();
                    switch (stype) {
                        case "SELECT"                            -> sel++;
                        case "INSERT"                            -> ins++;
                        case "UPDATE"                            -> upd++;
                        case "DELETE"                            -> del++;
                        case "MERGE"                             -> mer++;
                        case "CURSOR", "DINAMIC_CURSOR",
                             "DYNAMIC_CURSOR", "FOR_CURSOR"      -> cur++;
                        default                                  -> { if (!stype.isEmpty()) oth++; }
                    }
                }

                // Atom breakdown — actual status values in hound DB
                int atomTotal = 0, atomResolved = 0, atomFailed = 0, atomConst = 0, atomFunc = 0;
                for (var row : atomRows) {
                    String status = str(row, "status").toLowerCase();
                    int cnt = num(row, "cnt");
                    atomTotal += cnt;
                    switch (status) {
                        case "обработано"    -> atomResolved += cnt;
                        case "unresolved"    -> atomFailed   += cnt;
                        case "constant"      -> atomConst    += cnt;
                        case "function_call" -> atomFunc     += cnt;
                    }
                }

                String filePath    = str(meta, "file_path");
                String sessionName = deriveName(str(meta, "session_name"), filePath);

                return new KnotSession(
                    str(meta, "id"),
                    str(meta, "session_id"),
                    sessionName,
                    str(meta, "dialect"),
                    filePath,
                    num(meta, "processing_ms"),
                    0, 0, 0, 0, routineCount, 0, 0,
                    sel, ins, upd, del, mer, cur, oth,
                    atomTotal, atomResolved, atomFailed, atomConst, atomFunc,
                    0, 0, 0, 0
                );
            });
    }

    // ── Tables ────────────────────────────────────────────────────────────────

    private Uni<List<KnotTable>> loadTables(Map<String, Object> params) {
        // Correct path: Session -[BELONGS_TO_SESSION]-> Routine -[CONTAINS_STMT]-> Statement -[READS_FROM|WRITES_TO]-> Table
        String cypherMain = """
            MATCH (sess:DaliSession {session_id: $sid})-[:BELONGS_TO_SESSION]->(r:DaliRoutine)
            MATCH (r)-[:CONTAINS_STMT]->(stmt:DaliStatement)
            MATCH (stmt)-[:READS_FROM|WRITES_TO]->(t:DaliTable)
            OPTIONAL MATCH (t)-[:HAS_COLUMN]->(c:DaliColumn)
            WITH t, c
            RETURN DISTINCT
                   id(t)                        AS tid,
                   t.table_geoid               AS geoid,
                   t.table_name                AS name,
                   coalesce(t.schema_geoid,'') AS schema,
                   'TABLE'                     AS tableType,
                   t.aliases                   AS tableAliases,
                   id(c)                        AS cid,
                   c.column_name               AS cname,
                   coalesce(c.data_type,'')    AS dtype,
                   coalesce(c.position, 0)     AS pos,
                   coalesce(c.alias,'')        AS calias
            ORDER BY t.table_name, c.column_name
            LIMIT 300
            """;

        // Count READS_FROM and WRITES_TO per table by name (geoid may be null)
        String cypherSrc = """
            MATCH (sess:DaliSession {session_id: $sid})-[:BELONGS_TO_SESSION]->(r:DaliRoutine)
            MATCH (r)-[:CONTAINS_STMT]->(stmt:DaliStatement)-[:READS_FROM]->(t:DaliTable)
            RETURN t.table_name AS tableName, count(*) AS cnt
            """;

        String cypherTgt = """
            MATCH (sess:DaliSession {session_id: $sid})-[:BELONGS_TO_SESSION]->(r:DaliRoutine)
            MATCH (r)-[:CONTAINS_STMT]->(stmt:DaliStatement)-[:WRITES_TO]->(t:DaliTable)
            RETURN t.table_name AS tableName, count(*) AS cnt
            """;

        return Uni.combine().all()
            .unis(
                arcade.cypher(cypherMain, params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(cypherSrc,  params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(cypherTgt,  params).onFailure().recoverWithItem(List.of())
            )
            .asTuple()
            .map(t -> buildTables(t.getItem1(), t.getItem2(), t.getItem3()));
    }

    private List<KnotTable> buildTables(
        List<Map<String, Object>> rows,
        List<Map<String, Object>> srcRows,
        List<Map<String, Object>> tgtRows
    ) {
        // Build name → count maps for source/target usage
        Map<String, Integer> srcMap = new HashMap<>();
        for (var r : srcRows) srcMap.put(str(r, "tableName"), num(r, "cnt"));
        Map<String, Integer> tgtMap = new HashMap<>();
        for (var r : tgtRows) tgtMap.put(str(r, "tableName"), num(r, "cnt"));

        LinkedHashMap<String, List<Map<String, Object>>> byTable = new LinkedHashMap<>();
        for (var r : rows) {
            String tid = str(r, "tid");
            byTable.computeIfAbsent(tid, k -> new ArrayList<>()).add(r);
        }

        List<KnotTable> result = new ArrayList<>();
        for (var entry : byTable.entrySet()) {
            var tRows = entry.getValue();
            Map<String, Object> first = tRows.get(0);
            String tableName = str(first, "name");

            List<KnotColumn> cols = new ArrayList<>();
            Set<String> seenCols = new HashSet<>();
            for (var r : tRows) {
                String cid = str(r, "cid");
                if (!cid.isEmpty() && seenCols.add(cid)) {
                    cols.add(new KnotColumn(
                        cid,
                        str(r, "cname"),
                        str(r, "dtype"),
                        num(r, "pos"),
                        0,
                        str(r, "calias")
                    ));
                }
            }

            List<String> aliases = toStringList(first.get("tableAliases"));

            result.add(new KnotTable(
                entry.getKey(),
                str(first, "geoid"),
                tableName,
                str(first, "schema"),
                str(first, "tableType"),
                cols.size(),
                srcMap.getOrDefault(tableName, 0),
                tgtMap.getOrDefault(tableName, 0),
                cols,
                aliases
            ));
        }
        return result;
    }

    // ── Statements ────────────────────────────────────────────────────────────

    private Uni<List<KnotStatement>> loadStatements(Map<String, Object> params) {
        // Fetch ALL statements for the session (not just roots).
        // Tree is built in Java via CHILD_OF edges from a second query.
        // Atom status values in hound DB: 'Обработано' | 'unresolved' | 'constant' | 'function_call'
        String cypherStmts = """
            MATCH (sess:DaliSession {session_id: $sid})-[:BELONGS_TO_SESSION]->(r:DaliRoutine)
            MATCH (r)-[:CONTAINS_STMT]->(stmt:DaliStatement)
            OPTIONAL MATCH (stmt)-[:READS_FROM]->(src:DaliTable)
            OPTIONAL MATCH (stmt)-[:WRITES_TO]->(tgt:DaliTable)
            OPTIONAL MATCH (stmt)-[:HAS_ATOM]->(a:DaliAtom)
            RETURN id(stmt)                                                         AS sid,
                   stmt.stmt_geoid                                                  AS geoid,
                   r.routine_name                                                   AS routineName,
                   coalesce(r.routine_type, '')                                     AS routineType,
                   stmt.aliases                                                     AS stmtAliases,
                   collect(DISTINCT src.table_name)                                 AS sources,
                   collect(DISTINCT tgt.table_name)                                 AS targets,
                   count(a)                                                         AS atomTotal,
                   count(CASE WHEN a.status='Обработано'    THEN 1 END)             AS atomResolved,
                   count(CASE WHEN a.status='unresolved'    THEN 1 END)             AS atomFailed,
                   count(CASE WHEN a.status='constant'      THEN 1 END)             AS atomConst
            ORDER BY r.routine_name, geoid
            LIMIT 1000
            """;

        // CHILD_OF direction: child -[CHILD_OF]-> parent
        String cypherEdges = """
            MATCH (sess:DaliSession {session_id: $sid})-[:BELONGS_TO_SESSION]->(r:DaliRoutine)
            MATCH (r)-[:CONTAINS_STMT]->(child:DaliStatement)
            MATCH (child)-[:CHILD_OF]->(parent:DaliStatement)
            RETURN id(child) AS childId, id(parent) AS parentId
            """;

        return Uni.combine().all()
            .unis(
                arcade.cypher(cypherStmts, params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(cypherEdges, params).onFailure().recoverWithItem(List.of())
            )
            .asTuple()
            .map(t -> buildStatementTree(t.getItem1(), t.getItem2()));
    }

    private List<KnotStatement> buildStatementTree(
        List<Map<String, Object>> stmtRows,
        List<Map<String, Object>> edgeRows
    ) {
        // Build flat map of all statements (children list is mutable ArrayList)
        LinkedHashMap<String, KnotStatement> byId = new LinkedHashMap<>();
        for (var r : stmtRows) {
            String id    = str(r, "sid");
            String geoid = str(r, "geoid");
            byId.put(id, new KnotStatement(
                id, geoid,
                parseStmtType(geoid),
                parseLineNumber(geoid),
                str(r, "routineName"),
                parsePackageName(geoid),
                str(r, "routineType"),
                toStringList(r.get("sources")),
                toStringList(r.get("targets")),
                toStringList(r.get("stmtAliases")),
                num(r, "atomTotal"),
                num(r, "atomResolved"),
                num(r, "atomFailed"),
                num(r, "atomConst"),
                new ArrayList<>()
            ));
        }

        // Attach children to parents via CHILD_OF edges
        Set<String> childIds = new HashSet<>();
        for (var e : edgeRows) {
            String childId  = str(e, "childId");
            String parentId = str(e, "parentId");
            KnotStatement parent = byId.get(parentId);
            KnotStatement child  = byId.get(childId);
            if (parent != null && child != null) {
                parent.children().add(child);
                childIds.add(childId);
            }
        }

        // Return only root statements (those not appearing as children)
        return byId.entrySet().stream()
            .filter(e -> !childIds.contains(e.getKey()))
            .map(Map.Entry::getValue)
            .toList();
    }

    // ── Snippets ──────────────────────────────────────────────────────────────

    private Uni<List<KnotSnippet>> loadSnippets(Map<String, Object> params) {
        // DaliSnippet has no graph edges — standalone records joined by session_id + stmt_geoid
        String sql = """
            SELECT stmt_geoid, snippet
            FROM DaliSnippet
            WHERE session_id = :sid
            ORDER BY stmt_geoid
            LIMIT 2000
            """;

        return arcade.sql(sql, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> new KnotSnippet(str(r, "stmt_geoid"), str(r, "snippet")))
                .toList()
            );
    }

    // ── Atoms ─────────────────────────────────────────────────────────────────

    private Uni<List<KnotAtom>> loadAtoms(Map<String, Object> params) {
        String sql = """
            SELECT statement_geoid, atom_text, column_name,
                   coalesce(table_geoid, '')           AS table_geoid,
                   coalesce(table_name,  '')           AS table_name,
                   coalesce(status, '')                AS status,
                   coalesce(atom_context, '')          AS atom_context,
                   coalesce(parent_context, '')        AS parent_context,
                   output_column_sequence,
                   is_column_reference, is_function_call, is_constant,
                   coalesce(s_complex, false)          AS s_complex,
                   coalesce(is_routine_param, false)   AS is_routine_param,
                   coalesce(is_routine_var, false)     AS is_routine_var,
                   nested_atoms_count
            FROM DaliAtom
            WHERE session_id = :sid
            ORDER BY statement_geoid
            LIMIT 5000
            """;

        return arcade.sql(sql, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> {
                List<KnotAtom> atoms = rows.stream()
                    .map(r -> {
                        String atomText = str(r, "atom_text");
                        return new KnotAtom(
                            str(r, "statement_geoid"),
                            atomText,
                            str(r, "column_name"),
                            str(r, "table_geoid"),
                            str(r, "table_name"),
                            str(r, "status"),
                            str(r, "atom_context"),
                            str(r, "parent_context"),
                            intOrNull(r, "output_column_sequence"),
                            bool(r, "is_column_reference"),
                            bool(r, "is_function_call"),
                            bool(r, "is_constant"),
                            bool(r, "s_complex"),
                            bool(r, "is_routine_param"),
                            bool(r, "is_routine_var"),
                            intOrNull(r, "nested_atoms_count"),
                            atomLine(atomText),
                            atomPos(atomText)
                        );
                    })
                    .sorted(Comparator.comparing(KnotAtom::stmtGeoid)
                        .thenComparingInt(KnotAtom::atomLine)
                        .thenComparingInt(KnotAtom::atomPos))
                    .toList();
                return atoms;
            });
    }

    // ── Routine calls ─────────────────────────────────────────────────────────

    private Uni<List<KnotCall>> loadCalls(Map<String, Object> params) {
        // CALLS edge: DaliRoutine -[CALLS]-> DaliRoutine (or any vertex)
        // Edge properties: callee_name, line_start; caller identified by caller node
        String cypher = """
            MATCH (sess:DaliSession {session_id: $sid})-[:BELONGS_TO_SESSION]->(caller:DaliRoutine)
            MATCH (caller)-[c:CALLS]->(callee)
            RETURN caller.routine_name                             AS callerName,
                   coalesce(caller.package_geoid, '')             AS callerPackage,
                   coalesce(c.callee_name, callee.routine_name, '') AS calleeName,
                   coalesce(c.line_start, 0)                      AS lineStart
            ORDER BY callerName, lineStart
            LIMIT 5000
            """;

        return arcade.cypher(cypher, params)
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

    // ── Output columns ───────────────────────────────────────────────────────

    private Uni<List<KnotOutputColumn>> loadOutputColumns(Map<String, Object> params) {
        String sql = """
            SELECT statement_geoid, name, expression,
                   coalesce(alias, '')       AS alias,
                   coalesce(col_order, 0)    AS col_order,
                   coalesce(source_type, '') AS source_type,
                   coalesce(table_ref, '')   AS table_ref
            FROM DaliOutputColumn
            WHERE session_id = :sid
            ORDER BY statement_geoid, col_order
            LIMIT 5000
            """;

        return arcade.sql(sql, params)
            .onFailure().recoverWithItem(List.of())
            .map(rows -> rows.stream()
                .map(r -> new KnotOutputColumn(
                    str(r, "statement_geoid"),
                    str(r, "name"),
                    str(r, "expression"),
                    str(r, "alias"),
                    num(r, "col_order"),
                    str(r, "source_type"),
                    str(r, "table_ref")
                ))
                .toList()
            );
    }

    // ── Parameters & Variables ────────────────────────────────────────────────

    private Uni<KnotParamVars> loadParamsAndVars(Map<String, Object> params) {
        String cypherParams = """
            MATCH (sess:DaliSession {session_id: $sid})-[:BELONGS_TO_SESSION]->(r:DaliRoutine)
            MATCH (r)-[:HAS_PARAMETER]->(p:DaliParameter)
            RETURN r.routine_name                   AS routineName,
                   p.param_name                     AS paramName,
                   coalesce(p.data_type, '')         AS dataType,
                   coalesce(p.direction, '')         AS direction
            ORDER BY routineName, paramName
            LIMIT 5000
            """;

        String cypherVars = """
            MATCH (sess:DaliSession {session_id: $sid})-[:BELONGS_TO_SESSION]->(r:DaliRoutine)
            MATCH (r)-[:HAS_VARIABLE]->(v:DaliVariable)
            RETURN r.routine_name                   AS routineName,
                   v.var_name                       AS varName,
                   coalesce(v.data_type, '')         AS dataType
            ORDER BY routineName, varName
            LIMIT 5000
            """;

        return Uni.combine().all()
            .unis(
                arcade.cypher(cypherParams, params).onFailure().recoverWithItem(List.of()),
                arcade.cypher(cypherVars,   params).onFailure().recoverWithItem(List.of())
            )
            .asTuple()
            .map(t -> {
                List<KnotParameter> pList = t.getItem1().stream()
                    .map(r -> new KnotParameter(
                        str(r, "routineName"),
                        str(r, "paramName"),
                        str(r, "dataType"),
                        str(r, "direction")
                    ))
                    .toList();

                List<KnotVariable> vList = t.getItem2().stream()
                    .map(r -> new KnotVariable(
                        str(r, "routineName"),
                        str(r, "varName"),
                        str(r, "dataType")
                    ))
                    .toList();

                return new KnotParamVars(pList, vList);
            });
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Parse stmt type from geoid: "SCHEMA.PKG:RTYPE:RNAME:STMT_TYPE:LINE"
     * Returns part[3] (e.g. "INSERT", "SELECT").
     */
    static String parseStmtType(String geoid) {
        if (geoid == null || geoid.isEmpty()) return "UNKNOWN";
        String[] parts = geoid.split(":");
        String t = parts.length >= 4 ? parts[3] : "";
        return t.isEmpty() ? "UNKNOWN" : t;
    }

    /**
     * Parse line number from geoid: part[4].
     */
    static int parseLineNumber(String geoid) {
        if (geoid == null || geoid.isEmpty()) return 0;
        String[] parts = geoid.split(":");
        if (parts.length >= 5) {
            try { return Integer.parseInt(parts[4]); }
            catch (NumberFormatException ignored) { return 0; }
        }
        return 0;
    }

    /**
     * Parse package name from geoid: part[0] (e.g. "DWH.CALC_PKL_CRED").
     */
    static String parsePackageName(String geoid) {
        if (geoid == null || geoid.isEmpty()) return "";
        int idx = geoid.indexOf(':');
        return idx > 0 ? geoid.substring(0, idx) : geoid;
    }

    /**
     * Derive a display name: use session_name if present, otherwise filename without extension from filePath.
     */
    static String deriveName(String sessionName, String filePath) {
        if (sessionName != null && !sessionName.isBlank()) return sessionName;
        if (filePath == null || filePath.isBlank()) return "";
        // Extract filename from Windows or Unix path
        int slash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        String filename = slash >= 0 ? filePath.substring(slash + 1) : filePath;
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }

    /**
     * Extract line number from atom_text: format is "NAME~line:pos" (e.g. "CODE~78:0").
     * Returns the integer before ':' after the last '~'. Returns 0 if not parseable.
     */
    static int atomLine(String atomText) {
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
    static int atomPos(String atomText) {
        if (atomText == null) return 0;
        int tilde = atomText.lastIndexOf('~');
        if (tilde < 0) return 0;
        String rest = atomText.substring(tilde + 1);
        int colon = rest.indexOf(':');
        if (colon < 0) return 0;
        try { return Integer.parseInt(rest.substring(colon + 1)); }
        catch (NumberFormatException ignored) { return 0; }
    }

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

    @SuppressWarnings("unchecked")
    private static List<String> toStringList(Object v) {
        if (v instanceof List<?> list) {
            return list.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(s -> !s.isEmpty())
                .toList();
        }
        return List.of();
    }

    private static KnotSession emptySession(String sessionId) {
        return new KnotSession(sessionId, sessionId, sessionId, "plsql", "", 0,
            0,0,0,0,0,0,0, 0,0,0,0,0,0,0, 0,0,0,0,0, 0,0,0,0);
    }
}
