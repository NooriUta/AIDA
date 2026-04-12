package com.hound.semantic;

import com.hound.metrics.PipelineTimer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.storage.ArcadeDBSemanticWriter;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that CONTAINS_ROUTINE edges are created correctly at every level:
 *
 *   Level 1 — DaliSchema → DaliPackage        (Schema → Package)
 *   Level 2 — DaliPackage → DaliRoutine       (Package → Procedure / Function)
 *   Level 3 — DaliRoutine → DaliRoutine       (Procedure → nested Function)
 *
 * Root-cause guard: StructureAndLineageBuilder.ensurePackage() used to prepend
 * schemaGeoid to a name that already contained the full "SCHEMA.PKG" geoid,
 * producing "DWH.DWH.PK_TEST" as the package key.  That made pkgV.get(packageGeoid)
 * return null → zero Package→Routine edges even though the DaliPackage vertex existed.
 *
 * Run: ./gradlew test --tests "*ContainsRoutineHierarchyTest*" -Dintegration=true
 */
@Tag("integration")
@EnabledIfSystemProperty(named = "integration", matches = "true")
class ContainsRoutineHierarchyTest {

    // ── ArcadeDB connection ──────────────────────────────────────────────────
    private static final String HOST    = "localhost";
    private static final int    PORT    = 2480;
    private static final String USER    = "root";
    private static final String PASS    = "playwithdata";
    private static final String DB_TEST = "hound_test";
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    // ── Fixture ───────────────────────────────────────────────────────────────

    private static final String SQL = """
            CREATE OR REPLACE PACKAGE BODY DWH.PK_HIER_TEST AS

              PROCEDURE PROC1(p_id IN NUMBER, p_name IN VARCHAR2) IS
                v_count NUMBER := 0;
                v_flag  VARCHAR2(1) := 'N';
                FUNCTION INNER_FUNC RETURN NUMBER IS
                BEGIN
                  RETURN 42;
                END;
              BEGIN
                INSERT INTO DWH.TGT_TBL (id, name)
                  SELECT id, name FROM DWH.SRC_TBL WHERE id = p_id;
              END PROC1;

              FUNCTION FUNC1(p_val IN NUMBER) RETURN NUMBER IS
              BEGIN
                RETURN p_val + 1;
              END FUNC1;

            END PK_HIER_TEST;

            -- Standalone procedure (schema-routed, no package)
            CREATE OR REPLACE PROCEDURE DWH.STANDALONE_PROC(p_code IN VARCHAR2) IS
            BEGIN
              NULL;
            END;
            """;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws Exception {
        recreateDb();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private static UniversalSemanticEngine parse(String sql, String defaultSchema) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        listener.setDefaultSchema(defaultSchema);
        PlSqlLexer  lexer  = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    /** Returns stmt_geoid of first statement of given type in the given routine. */
    private static String stmtGeoidByType(String routineGeoid, String stmtType) throws Exception {
        String sql = "SELECT stmt_geoid FROM (SELECT expand(out('CONTAINS_STMT')) FROM DaliRoutine " +
                     "WHERE routine_geoid = '" + routineGeoid + "') WHERE type = '" + stmtType + "' LIMIT 1";
        Set<String> geoids = stringField(sql, "stmt_geoid");
        return geoids.isEmpty() ? null : geoids.iterator().next();
    }

    private static long scalar(String sql) throws Exception {
        String resp = arcadePost(sql);
        int idx = resp.indexOf("\"cnt\":");
        if (idx < 0) return 0L;
        int s = idx + 6;
        while (s < resp.length() && !Character.isDigit(resp.charAt(s)) && resp.charAt(s) != '-') s++;
        int e = s;
        while (e < resp.length() && (Character.isDigit(resp.charAt(e)) || resp.charAt(e) == '-')) e++;
        try { return Long.parseLong(resp.substring(s, e)); } catch (NumberFormatException ex) { return 0L; }
    }

    /** Get set of string field values from a traversal query result. */
    private static Set<String> outLabels(String fromType, String fromField, String fromVal,
                                          String edgeType, String labelField) throws Exception {
        String sql = "SELECT " + labelField + " FROM (SELECT expand(out('" + edgeType + "')) FROM "
                + fromType + " WHERE " + fromField + " = '" + fromVal + "')";
        return stringField(sql, labelField);
    }

    private static Set<String> stringField(String sql, String field) throws Exception {
        String resp   = arcadePost(sql);
        Set<String> result = new LinkedHashSet<>();
        String marker = "\"" + field + "\":\"";
        int pos = 0;
        while ((pos = resp.indexOf(marker, pos)) >= 0) {
            int start = pos + marker.length();
            int end   = resp.indexOf("\"", start);
            if (end > start) result.add(resp.substring(start, end).toUpperCase());
            pos = end > start ? end + 1 : pos + 1;
        }
        return result;
    }

    // ── Test ──────────────────────────────────────────────────────────────────

    @Test
    void containsRoutineHierarchy_allThreeLevelsWired() throws Exception {
        // Write via REMOTE_BATCH
        try (ArcadeDBSemanticWriter writer = new ArcadeDBSemanticWriter(HOST, PORT, DB_TEST, USER, PASS, true)) {
            UniversalSemanticEngine engine = parse(SQL, "DWH");
            writer.saveResult(
                    engine.getResult("DWH:pk_hier_test.pck", "pk_hier_test.pck", "PLSQL", 0L),
                    new PipelineTimer(), null, "DWH");
        }

        // ── Level 0: basic vertex counts ─────────────────────────────────────
        long pkgCount = scalar("SELECT count(*) AS cnt FROM DaliPackage");
        long rtnCount = scalar("SELECT count(*) AS cnt FROM DaliRoutine");

        System.out.println("DaliPackage count: " + pkgCount);
        System.out.println("DaliRoutine count: " + rtnCount);

        assertEquals(1, pkgCount, "Expected exactly 1 DaliPackage (PK_HIER_TEST)");
        assertTrue(rtnCount >= 3, "Expected at least 3 DaliRoutine entries; got " + rtnCount);

        // ── Verify package vertex fields ──────────────────────────────────────
        long pkgGeoidOk = scalar("SELECT count(*) AS cnt FROM DaliPackage WHERE package_geoid = 'DWH.PK_HIER_TEST'");
        assertEquals(1, pkgGeoidOk, "DaliPackage.package_geoid must be 'DWH.PK_HIER_TEST' (no double-schema)");

        long pkgNameOk = scalar("SELECT count(*) AS cnt FROM DaliPackage WHERE package_name = 'PK_HIER_TEST'");
        assertEquals(1, pkgNameOk, "DaliPackage.package_name must be bare 'PK_HIER_TEST', not 'DWH.PK_HIER_TEST'");

        // ── Level 1: DaliSchema → DaliPackage (CONTAINS_ROUTINE) ─────────────
        Set<String> schemaToPkg = outLabels("DaliSchema", "schema_geoid", "DWH", "CONTAINS_ROUTINE", "package_geoid");
        System.out.println("L1 Schema→Package: " + schemaToPkg);
        assertFalse(schemaToPkg.isEmpty(), "Level 1: DaliSchema must have CONTAINS_ROUTINE → DaliPackage");
        assertTrue(schemaToPkg.stream().anyMatch(s -> s.contains("PK_HIER_TEST")),
                "Level 1: Schema must contain PK_HIER_TEST. Found: " + schemaToPkg);

        // ── Level 2: DaliPackage → DaliRoutine (CONTAINS_ROUTINE) ────────────
        Set<String> pkgToRoutine = outLabels("DaliPackage", "package_geoid", "DWH.PK_HIER_TEST", "CONTAINS_ROUTINE", "routine_name");
        System.out.println("L2 Package→Routine: " + pkgToRoutine);
        assertFalse(pkgToRoutine.isEmpty(),
                "Level 2: DaliPackage must have CONTAINS_ROUTINE → DaliRoutine. " +
                "Bug was: ensurePackage double-prefixed schema → pkgV lookup returned null.");
        assertTrue(pkgToRoutine.stream().anyMatch(n -> n.contains("PROC1")),
                "Level 2: Package must contain PROC1. Found: " + pkgToRoutine);
        assertTrue(pkgToRoutine.stream().anyMatch(n -> n.contains("FUNC1")),
                "Level 2: Package must contain FUNC1. Found: " + pkgToRoutine);

        // ── Level 2b: Schema → standalone Procedure (CONTAINS_ROUTINE) ────────
        Set<String> schemaToStandalone = outLabels("DaliSchema", "schema_geoid", "DWH", "CONTAINS_ROUTINE", "routine_name");
        System.out.println("L2b Schema→standalone Routine: " + schemaToStandalone);
        assertTrue(schemaToStandalone.stream().anyMatch(n -> n.contains("STANDALONE_PROC")),
                "Level 2b: Schema must contain STANDALONE_PROC. Found: " + schemaToStandalone);

        // ── Level 3: NESTED_IN (Routine → parent Routine) ─────────────────────
        Set<String> nestedRoutines = outLabels("DaliRoutine",
                "routine_geoid", "DWH.PK_HIER_TEST:PROCEDURE:PROC1", "NESTED_IN", "routine_name");
        System.out.println("L3 PROC1 NESTED_IN→: " + nestedRoutines);
        assertFalse(nestedRoutines.isEmpty(), "Level 3: PROC1 must have NESTED_IN → INNER_FUNC. Found: " + nestedRoutines);
        assertTrue(nestedRoutines.stream().anyMatch(n -> n.contains("INNER_FUNC")),
                "Level 3: NESTED_IN must point to INNER_FUNC. Found: " + nestedRoutines);

        // ── HAS_PARAMETER ─────────────────────────────────────────────────────
        Set<String> proc1Params = outLabels("DaliRoutine",
                "routine_geoid", "DWH.PK_HIER_TEST:PROCEDURE:PROC1", "HAS_PARAMETER", "param_name");
        System.out.println("HAS_PARAMETER PROC1→: " + proc1Params);
        assertFalse(proc1Params.isEmpty(), "PROC1 must have HAS_PARAMETER edges. Found: " + proc1Params);
        assertTrue(proc1Params.stream().anyMatch(n -> n.contains("P_ID")),
                "PROC1 must have param P_ID. Found: " + proc1Params);
        assertTrue(proc1Params.stream().anyMatch(n -> n.contains("P_NAME")),
                "PROC1 must have param P_NAME. Found: " + proc1Params);

        Set<String> func1Params = outLabels("DaliRoutine",
                "routine_geoid", "DWH.PK_HIER_TEST:FUNCTION:FUNC1", "HAS_PARAMETER", "param_name");
        System.out.println("HAS_PARAMETER FUNC1→: " + func1Params);
        assertTrue(func1Params.stream().anyMatch(n -> n.contains("P_VAL")),
                "FUNC1 must have param P_VAL. Found: " + func1Params);

        Set<String> standaloneParams = outLabels("DaliRoutine",
                "routine_name", "DWH.STANDALONE_PROC", "HAS_PARAMETER", "param_name");
        System.out.println("HAS_PARAMETER STANDALONE_PROC→: " + standaloneParams);
        assertTrue(standaloneParams.stream().anyMatch(n -> n.contains("P_CODE")),
                "STANDALONE_PROC must have param P_CODE. Found: " + standaloneParams);

        // ── HAS_VARIABLE ──────────────────────────────────────────────────────
        Set<String> proc1Vars = outLabels("DaliRoutine",
                "routine_geoid", "DWH.PK_HIER_TEST:PROCEDURE:PROC1", "HAS_VARIABLE", "var_name");
        System.out.println("HAS_VARIABLE PROC1→: " + proc1Vars);
        assertFalse(proc1Vars.isEmpty(), "PROC1 must have HAS_VARIABLE edges. Found: " + proc1Vars);
        assertTrue(proc1Vars.stream().anyMatch(n -> n.contains("V_COUNT")),
                "PROC1 must have variable V_COUNT. Found: " + proc1Vars);
        assertTrue(proc1Vars.stream().anyMatch(n -> n.contains("V_FLAG")),
                "PROC1 must have variable V_FLAG. Found: " + proc1Vars);

        // ── CONTAINS_STMT ─────────────────────────────────────────────────────
        Set<String> proc1Stmts = outLabels("DaliRoutine",
                "routine_geoid", "DWH.PK_HIER_TEST:PROCEDURE:PROC1", "CONTAINS_STMT", "type");
        System.out.println("CONTAINS_STMT PROC1→: " + proc1Stmts);
        assertFalse(proc1Stmts.isEmpty(), "PROC1 must have CONTAINS_STMT edges. Found: " + proc1Stmts);
        assertTrue(proc1Stmts.stream().anyMatch(n -> n.contains("INSERT")),
                "PROC1 must contain an INSERT statement. Found: " + proc1Stmts);
        assertTrue(proc1Stmts.stream().anyMatch(n -> n.contains("SELECT")),
                "PROC1 must contain a SELECT statement (sub-select of INSERT). Found: " + proc1Stmts);

        // ── CHILD_OF: SELECT is a child of INSERT ─────────────────────────────
        String insertGeoid = stmtGeoidByType("DWH.PK_HIER_TEST:PROCEDURE:PROC1", "INSERT");
        String selectGeoid = stmtGeoidByType("DWH.PK_HIER_TEST:PROCEDURE:PROC1", "SELECT");
        System.out.println("INSERT stmt_geoid: " + insertGeoid);
        System.out.println("SELECT stmt_geoid: " + selectGeoid);
        assertNotNull(insertGeoid, "INSERT statement must exist in PROC1");
        assertNotNull(selectGeoid, "SELECT statement must exist in PROC1");

        Set<String> selectParents = outLabels("DaliStatement", "stmt_geoid", selectGeoid, "CHILD_OF", "type");
        System.out.println("SELECT CHILD_OF→: " + selectParents);
        assertTrue(selectParents.stream().anyMatch(n -> n.contains("INSERT")),
                "SELECT must be CHILD_OF INSERT (not a standalone SELECT). Found: " + selectParents);

        // ── Total CONTAINS_ROUTINE sanity ─────────────────────────────────────
        long totalEdges = scalar("SELECT count(*) AS cnt FROM CONTAINS_ROUTINE");
        System.out.println("Total CONTAINS_ROUTINE edges: " + totalEdges);
        assertTrue(totalEdges >= 4,
                "Expected at least 4 CONTAINS_ROUTINE edges. Got: " + totalEdges);

        System.out.println("All hierarchy assertions passed.");
    }

    // ── HTTP helpers ──────────────────────────────────────────────────────────

    private static String arcadePost(String sql) throws Exception {
        String auth = "Basic " + Base64.getEncoder()
                .encodeToString((USER + ":" + PASS).getBytes(StandardCharsets.UTF_8));
        String escaped = sql.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ");
        String body = "{\"language\":\"sql\",\"command\":\"" + escaped + "\"}";
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create("http://" + HOST + ":" + PORT + "/api/v1/command/" + DB_TEST))
                .header("Authorization", auth)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        return HTTP.send(req, HttpResponse.BodyHandlers.ofString()).body();
    }

    private static void recreateDb() throws Exception {
        String auth = "Basic " + Base64.getEncoder()
                .encodeToString((USER + ":" + PASS).getBytes(StandardCharsets.UTF_8));
        HttpRequest drop = HttpRequest.newBuilder()
                .uri(URI.create("http://" + HOST + ":" + PORT + "/api/v1/server"))
                .header("Authorization", auth).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"command\":\"drop database " + DB_TEST + "\"}"))
                .build();
        try { HTTP.send(drop, HttpResponse.BodyHandlers.ofString()); } catch (Exception ignored) {}
        HttpRequest create = HttpRequest.newBuilder()
                .uri(URI.create("http://" + HOST + ":" + PORT + "/api/v1/server"))
                .header("Authorization", auth).header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("{\"command\":\"create database " + DB_TEST + "\"}"))
                .build();
        String res = HTTP.send(create, HttpResponse.BodyHandlers.ofString()).body();
        if (!res.contains("\"ok\"")) throw new RuntimeException("Failed to create DB " + DB_TEST + ": " + res);
    }
}
