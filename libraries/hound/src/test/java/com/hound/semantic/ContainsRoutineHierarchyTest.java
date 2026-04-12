package com.hound.semantic;

import com.arcadedb.database.Database;
import com.arcadedb.database.DatabaseFactory;
import com.arcadedb.query.sql.executor.ResultSet;
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

import java.io.IOException;
import java.nio.file.*;
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
 * Run: ./gradlew test --tests "*ContainsRoutineHierarchyTest*"
 */
class ContainsRoutineHierarchyTest {

    private static final String DB = "target/test-db-contains-routine";

    // ── Fixture ───────────────────────────────────────────────────────────────

    /**
     * One package with:
     *   - PROCEDURE PROC1(p_id IN NUMBER, p_name IN VARCHAR2)
     *       variables: v_count NUMBER, v_flag VARCHAR2
     *       body: INSERT INTO DWH.TGT_TBL + nested INNER_FUNC
     *   - FUNCTION  FUNC1(p_val IN NUMBER) RETURN NUMBER
     *       body: SELECT … FROM DWH.SRC_TBL
     * Plus a standalone schema-routed procedure with one parameter.
     *
     * Covers: CONTAINS_ROUTINE (L1/L2/L2b), NESTED_IN (L3),
     *         HAS_PARAMETER, HAS_VARIABLE, CONTAINS_STMT.
     */
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

    @BeforeEach void setUp()    throws IOException { wipe(Path.of(DB)); }
    @AfterEach  void tearDown() throws IOException { wipe(Path.of(DB)); }

    private static void wipe(Path p) throws IOException {
        if (!Files.exists(p)) return;
        try (var w = Files.walk(p)) {
            w.sorted(Comparator.reverseOrder())
             .forEach(x -> { try { Files.deleteIfExists(x); } catch (IOException ignored) {} });
        }
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

    /**
     * Returns stmt_geoid of the first statement of the given type
     * that belongs to the given routine (via CONTAINS_STMT).
     */
    private static String stmtGeoidByType(Database db, String routineGeoid, String stmtType) {
        String routineRid = null;
        try (ResultSet rs = db.query("sql",
                "SELECT @rid AS r FROM DaliRoutine WHERE routine_geoid = :v LIMIT 1",
                Map.of("v", routineGeoid))) {
            if (rs.hasNext()) {
                Object o = rs.next().getProperty("r");
                if (o != null) routineRid = o.toString();
            }
        } catch (Exception e) {
            System.out.println("[WARN] routine rid: " + e.getMessage());
        }
        if (routineRid == null) return null;
        try (ResultSet rs = db.query("sql",
                "SELECT stmt_geoid FROM (SELECT expand(out('CONTAINS_STMT')) FROM " + routineRid + ")" +
                " WHERE type = :t LIMIT 1",
                Map.of("t", stmtType))) {
            if (rs.hasNext()) {
                Object v = rs.next().getProperty("stmt_geoid");
                return v != null ? v.toString() : null;
            }
        } catch (Exception e) {
            System.out.println("[WARN] stmtGeoidByType: " + e.getMessage());
        }
        return null;
    }

    private static long scalar(Database db, String sql) {
        try (ResultSet rs = db.query("sql", sql)) {
            if (rs.hasNext()) {
                Object v = rs.next().getProperty("cnt");
                return v instanceof Number n ? n.longValue() : 0L;
            }
        } catch (Exception e) {
            System.out.println("[WARN] scalar: " + e.getMessage());
        }
        return 0L;
    }

    private static Set<String> outLabels(Database db, String fromType, String fromField,
                                         String fromVal, String edgeType, String labelField) {
        Set<String> result = new LinkedHashSet<>();
        String schemaRid = null;
        try (ResultSet rs = db.query("sql",
                "SELECT @rid AS r FROM " + fromType + " WHERE " + fromField + " = :v LIMIT 1",
                Map.of("v", fromVal))) {
            if (rs.hasNext()) {
                Object o = rs.next().getProperty("r");
                if (o != null) schemaRid = o.toString();
            }
        } catch (Exception e) {
            System.out.println("[WARN] rid lookup: " + e.getMessage());
        }
        if (schemaRid == null) return result;
        String q = "SELECT " + labelField + " FROM (SELECT expand(out('" + edgeType + "')) FROM " + schemaRid + ")";
        try (ResultSet rs = db.query("sql", q)) {
            while (rs.hasNext()) {
                Object v = rs.next().getProperty(labelField);
                if (v != null) result.add(v.toString().toUpperCase());
            }
        } catch (Exception e) {
            System.out.println("[WARN] expand: " + e.getMessage());
        }
        return result;
    }

    // ── Test ──────────────────────────────────────────────────────────────────

    @Test
    void containsRoutineHierarchy_allThreeLevelsWired() throws Exception {
        String dbPath = Path.of(DB).toAbsolutePath().toString();

        try (ArcadeDBSemanticWriter writer = new ArcadeDBSemanticWriter(dbPath)) {
            UniversalSemanticEngine engine = parse(SQL, "DWH");
            writer.saveResult(
                    engine.getResult("DWH:pk_hier_test.pck", "pk_hier_test.pck", "PLSQL", 0L),
                    new PipelineTimer(), null, "DWH");
        }

        try (Database db = new DatabaseFactory(dbPath).open()) {

            // ── Level 0: basic vertex counts ─────────────────────────────────
            long pkgCount = scalar(db, "SELECT count(*) AS cnt FROM DaliPackage");
            long rtnCount = scalar(db, "SELECT count(*) AS cnt FROM DaliRoutine");

            System.out.println("DaliPackage count: " + pkgCount);
            System.out.println("DaliRoutine count: " + rtnCount);

            assertEquals(1, pkgCount, "Expected exactly 1 DaliPackage (PK_HIER_TEST)");
            // Routines: PROC1, FUNC1, INNER_FUNC (nested), STANDALONE_PROC = 4
            assertTrue(rtnCount >= 3, "Expected at least 3 DaliRoutine entries; got " + rtnCount);

            // ── Verify package vertex fields ──────────────────────────────────
            long pkgGeoidOk = scalar(db,
                    "SELECT count(*) AS cnt FROM DaliPackage WHERE package_geoid = 'DWH.PK_HIER_TEST'");
            assertEquals(1, pkgGeoidOk, "DaliPackage.package_geoid must be 'DWH.PK_HIER_TEST' (no double-schema)");

            long pkgNameOk = scalar(db,
                    "SELECT count(*) AS cnt FROM DaliPackage WHERE package_name = 'PK_HIER_TEST'");
            assertEquals(1, pkgNameOk, "DaliPackage.package_name must be bare 'PK_HIER_TEST', not 'DWH.PK_HIER_TEST'");

            // ── Level 1: DaliSchema → DaliPackage (CONTAINS_ROUTINE) ──────────
            Set<String> schemaToPkg = outLabels(db, "DaliSchema", "schema_geoid", "DWH",
                    "CONTAINS_ROUTINE", "package_geoid");
            System.out.println("L1 Schema→Package: " + schemaToPkg);
            assertFalse(schemaToPkg.isEmpty(),
                    "Level 1: DaliSchema must have CONTAINS_ROUTINE → DaliPackage");
            assertTrue(schemaToPkg.stream().anyMatch(s -> s.contains("PK_HIER_TEST")),
                    "Level 1: Schema must contain PK_HIER_TEST. Found: " + schemaToPkg);

            // ── Level 2: DaliPackage → DaliRoutine (CONTAINS_ROUTINE) ─────────
            Set<String> pkgToRoutine = outLabels(db, "DaliPackage", "package_geoid", "DWH.PK_HIER_TEST",
                    "CONTAINS_ROUTINE", "routine_name");
            System.out.println("L2 Package→Routine: " + pkgToRoutine);
            assertFalse(pkgToRoutine.isEmpty(),
                    "Level 2: DaliPackage must have CONTAINS_ROUTINE → DaliRoutine. " +
                    "Bug was: ensurePackage double-prefixed schema → pkgV lookup returned null.");
            assertTrue(pkgToRoutine.stream().anyMatch(n -> n.contains("PROC1")),
                    "Level 2: Package must contain PROC1. Found: " + pkgToRoutine);
            assertTrue(pkgToRoutine.stream().anyMatch(n -> n.contains("FUNC1")),
                    "Level 2: Package must contain FUNC1. Found: " + pkgToRoutine);

            // ── Level 2b: Schema → standalone Procedure (CONTAINS_ROUTINE) ────
            Set<String> schemaToStandalone = outLabels(db, "DaliSchema", "schema_geoid", "DWH",
                    "CONTAINS_ROUTINE", "routine_name");
            System.out.println("L2b Schema→standalone Routine: " + schemaToStandalone);
            assertTrue(schemaToStandalone.stream().anyMatch(n -> n.contains("STANDALONE_PROC")),
                    "Level 2b: Schema must contain STANDALONE_PROC. Found: " + schemaToStandalone);

            // ── Level 3: NESTED_IN (Routine → parent Routine) ─────────────────
            Set<String> nestedRoutines = outLabels(db, "DaliRoutine",
                    "routine_geoid", "DWH.PK_HIER_TEST:PROCEDURE:PROC1",
                    "NESTED_IN", "routine_name");
            System.out.println("L3 PROC1 NESTED_IN→: " + nestedRoutines);
            assertFalse(nestedRoutines.isEmpty(),
                    "Level 3: PROC1 must have NESTED_IN → INNER_FUNC. Found: " + nestedRoutines);
            assertTrue(nestedRoutines.stream().anyMatch(n -> n.contains("INNER_FUNC")),
                    "Level 3: NESTED_IN must point to INNER_FUNC. Found: " + nestedRoutines);

            // ── HAS_PARAMETER: DaliRoutine → DaliParameter ────────────────────
            Set<String> proc1Params = outLabels(db, "DaliRoutine",
                    "routine_geoid", "DWH.PK_HIER_TEST:PROCEDURE:PROC1",
                    "HAS_PARAMETER", "param_name");
            System.out.println("HAS_PARAMETER PROC1→: " + proc1Params);
            assertFalse(proc1Params.isEmpty(),
                    "PROC1 must have HAS_PARAMETER edges. Found: " + proc1Params);
            assertTrue(proc1Params.stream().anyMatch(n -> n.contains("P_ID")),
                    "PROC1 must have param P_ID. Found: " + proc1Params);
            assertTrue(proc1Params.stream().anyMatch(n -> n.contains("P_NAME")),
                    "PROC1 must have param P_NAME. Found: " + proc1Params);

            Set<String> func1Params = outLabels(db, "DaliRoutine",
                    "routine_geoid", "DWH.PK_HIER_TEST:FUNCTION:FUNC1",
                    "HAS_PARAMETER", "param_name");
            System.out.println("HAS_PARAMETER FUNC1→: " + func1Params);
            assertTrue(func1Params.stream().anyMatch(n -> n.contains("P_VAL")),
                    "FUNC1 must have param P_VAL. Found: " + func1Params);

            // lookup by routine_name — standalone proc geoid includes qualified name
            Set<String> standaloneParams = outLabels(db, "DaliRoutine",
                    "routine_name", "DWH.STANDALONE_PROC",
                    "HAS_PARAMETER", "param_name");
            System.out.println("HAS_PARAMETER STANDALONE_PROC→: " + standaloneParams);
            assertTrue(standaloneParams.stream().anyMatch(n -> n.contains("P_CODE")),
                    "STANDALONE_PROC must have param P_CODE. Found: " + standaloneParams);

            // ── HAS_VARIABLE: DaliRoutine → DaliVariable ──────────────────────
            Set<String> proc1Vars = outLabels(db, "DaliRoutine",
                    "routine_geoid", "DWH.PK_HIER_TEST:PROCEDURE:PROC1",
                    "HAS_VARIABLE", "var_name");
            System.out.println("HAS_VARIABLE PROC1→: " + proc1Vars);
            assertFalse(proc1Vars.isEmpty(),
                    "PROC1 must have HAS_VARIABLE edges. Found: " + proc1Vars);
            assertTrue(proc1Vars.stream().anyMatch(n -> n.contains("V_COUNT")),
                    "PROC1 must have variable V_COUNT. Found: " + proc1Vars);
            assertTrue(proc1Vars.stream().anyMatch(n -> n.contains("V_FLAG")),
                    "PROC1 must have variable V_FLAG. Found: " + proc1Vars);

            // ── CONTAINS_STMT: DaliRoutine → DaliStatement ────────────────────
            Set<String> proc1Stmts = outLabels(db, "DaliRoutine",
                    "routine_geoid", "DWH.PK_HIER_TEST:PROCEDURE:PROC1",
                    "CONTAINS_STMT", "type");
            System.out.println("CONTAINS_STMT PROC1→: " + proc1Stmts);
            assertFalse(proc1Stmts.isEmpty(),
                    "PROC1 must have CONTAINS_STMT edges. Found: " + proc1Stmts);
            assertTrue(proc1Stmts.stream().anyMatch(n -> n.contains("INSERT")),
                    "PROC1 must contain an INSERT statement. Found: " + proc1Stmts);
            assertTrue(proc1Stmts.stream().anyMatch(n -> n.contains("SELECT")),
                    "PROC1 must contain a SELECT statement (sub-select of INSERT). Found: " + proc1Stmts);

            // ── CHILD_OF: SELECT is a child of INSERT (не самостоятельный SELECT) ─
            // Получаем stmt_geoid INSERT и SELECT из PROC1, затем проверяем
            // что SELECT.out(CHILD_OF) → INSERT.
            String insertGeoid = stmtGeoidByType(db, "DWH.PK_HIER_TEST:PROCEDURE:PROC1", "INSERT");
            String selectGeoid = stmtGeoidByType(db, "DWH.PK_HIER_TEST:PROCEDURE:PROC1", "SELECT");
            System.out.println("INSERT stmt_geoid: " + insertGeoid);
            System.out.println("SELECT stmt_geoid: " + selectGeoid);
            assertNotNull(insertGeoid, "INSERT statement must exist in PROC1");
            assertNotNull(selectGeoid, "SELECT statement must exist in PROC1");

            // SELECT -[CHILD_OF]-> INSERT
            Set<String> selectParents = outLabels(db, "DaliStatement", "stmt_geoid", selectGeoid,
                    "CHILD_OF", "type");
            System.out.println("SELECT CHILD_OF→: " + selectParents);
            assertTrue(selectParents.stream().anyMatch(n -> n.contains("INSERT")),
                    "SELECT must be CHILD_OF INSERT (not a standalone SELECT). Found: " + selectParents);

            // ── Total CONTAINS_ROUTINE sanity ─────────────────────────────────
            long totalEdges = scalar(db, "SELECT count(*) AS cnt FROM CONTAINS_ROUTINE");
            System.out.println("Total CONTAINS_ROUTINE edges: " + totalEdges);
            // Schema→Pkg(1) + Pkg→PROC1(1) + Pkg→FUNC1(1) + Schema→STANDALONE_PROC(1) = 4 minimum
            assertTrue(totalEdges >= 4,
                    "Expected at least 4 CONTAINS_ROUTINE edges. Got: " + totalEdges);

            System.out.println("All hierarchy assertions passed.");
        }
    }
}
