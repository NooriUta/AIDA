package studio.seer.lineage.service;

import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import studio.seer.lineage.client.ArcadeGateway;
import studio.seer.lineage.model.*;
import studio.seer.tenantrouting.YggLineageRegistry;
import studio.seer.lineage.security.SeerIdentity;

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
 * │ DaliAtom         │ primary_status,         │ primary_status: RESOLVED|UNRESOLVED|CONSTANT|        │
 * │                  │ qualifier, status       │ FUNCTION_CALL (ADR-HND-002). Legacy status field    │
 * │                  │                         │ kept for migration. qualifier: LINKED|CTE|etc.      │
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
 * DaliSnippet is a DOCUMENT (not vertex): large SQL texts, VERTEX promotion rejected.
 *   v28+: element_rid = ArcadeDB @rid of owning DaliStatement → O(1) lookup by node id.
 *   Fallback: stmt_geoid NOTUNIQUE index (pre-v28 data or stmt_geoid path).
 *   DaliRoutine   -[HAS_PARAMETER]->      DaliParameter
 *   DaliRoutine   -[HAS_VARIABLE]->       DaliVariable
 *
 * stmt_geoid format: "SCHEMA.PACKAGE:ROUTINE_TYPE:ROUTINE_NAME:STMT_TYPE:LINE[:nested...]"
 *   e.g. "DWH.CALC_PKL_CRED:PROCEDURE:CALC_AGG:INSERT:152"
 *   Nested: "DWH.CALC_PKL_CRED:PROCEDURE:CALC_AGG:INSERT:2333:SELECT:2336:ACC_DEAL:2370:SQ:2372"
 */
@ApplicationScoped
public class KnotService {

    @Inject ArcadeGateway              arcade;
    @Inject SeerIdentity               identity;
    @Inject YggLineageRegistry         lineageRegistry;
    @Inject KnotColumnLineageService   columnLineageService;
    @Inject KnotSnippetService         knotSnippetService;
    @Inject KnotStatementLoader        knotStatementLoader;

    String lineageDb() {
        return lineageRegistry.resourceFor(identity.tenantAlias()).databaseName();
    }

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

        return arcade.sqlIn(lineageDb(), sql, Map.of()).map(rows -> rows.stream()
            .map(r -> {
                String filePath    = str(r, "file_path");
                String sessionName = KnotGeoidParser.deriveName(str(r, "session_name"), filePath);
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

        Uni<KnotSession>                 sessionUni    = loadSession(sessionId, params);
        Uni<List<KnotTable>>             tablesUni     = loadTables(params);
        Uni<List<KnotStatement>>         statementsUni = knotStatementLoader.loadStatements(params);
        Uni<List<KnotSnippet>>           snippetsUni   = columnLineageService.loadSnippets(params);
        Uni<List<KnotAtom>>              atomsUni      = columnLineageService.loadAtoms(params);
        Uni<List<KnotOutputColumn>>      outColsUni    = columnLineageService.loadOutputColumns(params);
        Uni<List<KnotAffectedColumn>>    affColsUni    = columnLineageService.loadAffectedColumns(params);
        Uni<List<KnotCall>>              callsUni      = columnLineageService.loadCalls(params);
        Uni<KnotParamVars> paramVarsUni = columnLineageService.loadParamsAndVars(params);

        return Uni.combine().all()
            .unis(sessionUni, tablesUni, statementsUni, snippetsUni, atomsUni, outColsUni, affColsUni, callsUni, paramVarsUni)
            .asTuple()
            .map(t -> {
                KnotParamVars pv = t.getItem9();
                return new KnotReport(
                    t.getItem1(), t.getItem2(), pv.routines(), t.getItem3(),
                    t.getItem4(), t.getItem5(), t.getItem6(), t.getItem7(),
                    t.getItem8(), pv.parameters(), pv.variables()
                );
            });
    }

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

        // Routine count — uses DaliRoutine(session_id) NOTUNIQUE index directly.
        // Was: in('BELONGS_TO_SESSION')[session_id=:sid].size() > 0 — O(n) full scan.
        String sqlRoutineCount = """
            SELECT count(*) AS routineCount
            FROM DaliRoutine
            WHERE session_id = :sid
            """;

        // Stmt geoids — uses DaliStatement(session_id) NOTUNIQUE index directly.
        // Was: in('CONTAINS_STMT').in('BELONGS_TO_SESSION')[...].size() > 0 — 2-hop reverse scan.
        String sqlStmtGeoids = """
            SELECT stmt_geoid
            FROM DaliStatement
            WHERE session_id = :sid
              AND outE('CHILD_OF').size() = 0
            """;

        // Atom counts grouped by status — uses DaliAtom(session_id) NOTUNIQUE index directly.
        // Was: 3-hop reverse traversal on ALL atoms (561 995 rows) — catastrophic full scan.
        String sqlAtoms = """
            SELECT coalesce(primary_status, status) AS ps, count(*) AS cnt
            FROM DaliAtom
            WHERE session_id = :sid
            GROUP BY coalesce(primary_status, status)
            """;

        return Uni.combine().all()
            .unis(
                arcade.sqlIn(lineageDb(), sqlMeta, params).onFailure().recoverWithItem(List.of()),
                arcade.sqlIn(lineageDb(), sqlRoutineCount, params).onFailure().recoverWithItem(List.of()),
                arcade.sqlIn(lineageDb(), sqlStmtGeoids, params).onFailure().recoverWithItem(List.of()),
                arcade.sqlIn(lineageDb(), sqlAtoms, params).onFailure().recoverWithItem(List.of())
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
                    String stype = KnotGeoidParser.parseStmtType(geoid).toUpperCase();
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

                int atomTotal = 0, atomResolved = 0, atomFailed = 0, atomConst = 0, atomFunc = 0;
                for (var row : atomRows) {
                    String ps = str(row, "ps");
                    int cnt = num(row, "cnt");
                    atomTotal += cnt;
                    switch (ps) {
                        case "RESOLVED", "обработано", "Обработано" -> atomResolved += cnt;
                        case "UNRESOLVED", "unresolved"             -> atomFailed   += cnt;
                        case "CONSTANT", "constant"                 -> atomConst    += cnt;
                        case "FUNCTION_CALL", "function_call"       -> atomFunc     += cnt;
                    }
                }

                String filePath    = str(meta, "file_path");
                String sessionName = KnotGeoidParser.deriveName(str(meta, "session_name"), filePath);

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
        // Both queries start from DaliStatement(session_id) NOTUNIQUE index.
        // Avoids 3-hop traversal Session→Routine→Statement used in the original code.
        // Column details are lazy-loaded via knotTableDetail — only count here.
        String cypherMain = """
            MATCH (stmt:DaliStatement {session_id: $sid})-[:READS_FROM|WRITES_TO]-(t:DaliTable)
            OPTIONAL MATCH (t)-[:HAS_COLUMN]->(c:DaliColumn)
            WITH t, count(DISTINCT c) AS colCount
            RETURN DISTINCT
                   id(t)                                        AS tid,
                   t.table_geoid                               AS geoid,
                   t.table_name                                AS name,
                   coalesce(t.schema_geoid,'')                 AS schema,
                   'TABLE'                                     AS tableType,
                   t.aliases                                   AS tableAliases,
                   coalesce(t.data_source,'reconstructed')     AS dataSource,
                   colCount
            ORDER BY name
            LIMIT 300
            """;

        // READS_FROM and WRITES_TO counts in a single query — split by edgeType in Java.
        // Was: 2 separate Cypher queries with 3-hop traversal each (3 total → now 2).
        String cypherCounts = """
            MATCH (stmt:DaliStatement {session_id: $sid})-[e:READS_FROM|WRITES_TO]-(t:DaliTable)
            RETURN t.table_name AS tableName, type(e) AS edgeType, count(*) AS cnt
            """;

        return Uni.combine().all()
            .unis(
                arcade.cypherIn(lineageDb(), cypherMain,   params).onFailure().recoverWithItem(List.of()),
                arcade.cypherIn(lineageDb(), cypherCounts, params).onFailure().recoverWithItem(List.of())
            )
            .asTuple()
            .map(t -> buildTables(t.getItem1(), t.getItem2()));
    }

    private List<KnotTable> buildTables(
        List<Map<String, Object>> rows,
        List<Map<String, Object>> countRows
    ) {
        // Split merged count query by edgeType into src/tgt maps
        Map<String, Integer> srcMap = new HashMap<>();
        Map<String, Integer> tgtMap = new HashMap<>();
        for (var r : countRows) {
            String name = str(r, "tableName");
            if ("READS_FROM".equals(str(r, "edgeType"))) srcMap.put(name, num(r, "cnt"));
            else                                          tgtMap.put(name, num(r, "cnt"));
        }

        // Each row is one table (columns aggregated to count by Cypher WITH clause)
        List<KnotTable> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (var r : rows) {
            String tid = str(r, "tid");
            if (!seen.add(tid)) continue; // safety dedup
            String tableName = str(r, "name");
            List<String> aliases = toStringList(r.get("tableAliases"));
            result.add(new KnotTable(
                tid,
                str(r, "geoid"),
                tableName,
                str(r, "schema"),
                str(r, "tableType"),
                num(r, "colCount"),
                srcMap.getOrDefault(tableName, 0),
                tgtMap.getOrDefault(tableName, 0),
                str(r, "dataSource"),
                aliases
            ));
        }
        return result;
    }

    // ── Table detail + column analytics (delegated to KnotColumnLineageService) ──

    public Uni<KnotTableDetail> knotTableDetail(String sessionId, String tableGeoid) {
        return columnLineageService.knotTableDetail(sessionId, tableGeoid);
    }

    public Uni<List<KnotTableUsage>> knotTableRoutines(String tableRid) {
        return columnLineageService.knotTableRoutines(tableRid);
    }

    public Uni<List<KnotColumnUsage>> knotColumnStatements(String columnGeoid) {
        return columnLineageService.knotColumnStatements(columnGeoid);
    }

    // ── Snippet / script / source-file (delegated to KnotSnippetService) ──────

    public Uni<String> knotSnippet(String idOrGeoid) {
        return knotSnippetService.knotSnippet(idOrGeoid);
    }

    public Uni<KnotScript> knotScript(String sessionId) {
        return knotSnippetService.knotScript(sessionId);
    }

    public Uni<KnotSourceFile> knotSourceFile(String sessionId) {
        return knotSnippetService.knotSourceFile(sessionId);
    }

    // ── Statement extras (delegated to KnotColumnLineageService) ────────────

    public Uni<StatementExtras> knotStatementExtras(String idOrGeoid) {
        return columnLineageService.knotStatementExtras(idOrGeoid);
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

    @SuppressWarnings("unchecked")
    private static List<String> toStringList(Object v) {
        if (v instanceof List<?> list) {
            return list.stream()
                .filter(Objects::nonNull)
                .map(Object::toString)
                .filter(s -> !s.isEmpty())
                .toList();
        }
        // ArcadeDB Cypher may return array-type properties as a JSON string e.g. ["alias1","alias2"]
        if (v instanceof String s && !s.isBlank() && s.startsWith("[")) {
            String inner = s.substring(1, s.length() - 1).trim();
            if (inner.isEmpty()) return List.of();
            return java.util.Arrays.stream(inner.split(","))
                .map(item -> item.trim().replaceAll("^\"|\"$", ""))
                .filter(item -> !item.isEmpty())
                .toList();
        }
        return List.of();
    }


    private static KnotSession emptySession(String sessionId) {
        return new KnotSession(sessionId, sessionId, sessionId, "plsql", "", 0,
            0,0,0,0,0,0,0, 0,0,0,0,0,0,0, 0,0,0,0,0, 0,0,0,0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HND-07: PL/SQL TYPE templates
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * HND-07: Returns all DaliPlType templates visible in a lineage session.
     * For RECORD kinds the associated DaliPlTypeField records are joined in-process.
     */
    public Uni<List<KnotPlType>> plTypes(String sessionId) {
        String sqlTypes =
            "SELECT type_geoid, type_name, kind, element_type_geoid, scope_geoid, declared_at_line " +
            "FROM DaliPlType WHERE session_id = :sid ORDER BY type_name";
        String sqlFields =
            "SELECT field_geoid, field_name, field_type, position " +
            "FROM DaliPlTypeField WHERE session_id = :sid ORDER BY type_geoid, position";

        Map<String, Object> params = Map.of("sid", sessionId);

        return Uni.combine().all().unis(
                arcade.sqlIn(lineageDb(), sqlTypes,  params).onFailure().recoverWithItem(List.of()),
                arcade.sqlIn(lineageDb(), sqlFields, params).onFailure().recoverWithItem(List.of())
        ).asTuple().map(t -> {
            List<Map<String, Object>> typeRows  = t.getItem1();
            List<Map<String, Object>> fieldRows = t.getItem2();

            // Group fields by type_geoid
            Map<String, List<KnotPlTypeField>> fieldsByType = new LinkedHashMap<>();
            for (var fr : fieldRows) {
                String tg = strRow(fr, "type_geoid");
                if (tg == null) continue;
                fieldsByType.computeIfAbsent(tg, k -> new ArrayList<>()).add(new KnotPlTypeField(
                        strRow(fr, "field_geoid"),
                        strRow(fr, "field_name"),
                        strRow(fr, "field_type"),
                        intRow(fr,  "position")));
            }

            return typeRows.stream().map(row -> new KnotPlType(
                    strRow(row, "type_geoid"),
                    strRow(row, "type_name"),
                    strRow(row, "kind"),
                    strRow(row, "element_type_geoid"),
                    strRow(row, "scope_geoid"),
                    intRow(row,  "declared_at_line"),
                    fieldsByType.getOrDefault(strRow(row, "type_geoid"), List.of())
            )).toList();
        });
    }

    private static String strRow(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v instanceof String s ? s : (v != null ? v.toString() : null);
    }

    private static int intRow(Map<String, Object> row, String key) {
        Object v = row.get(key);
        return v instanceof Number n ? n.intValue() : 0;
    }
}
