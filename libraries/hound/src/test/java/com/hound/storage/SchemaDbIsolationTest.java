package com.hound.storage;

import com.arcadedb.database.Database;
import com.arcadedb.database.DatabaseFactory;
import com.arcadedb.query.sql.executor.ResultSet;
import com.hound.metrics.PipelineTimer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.engine.UniversalSemanticEngine;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DB-level isolation test: two databases that both contain a schema named "DWH".
 *
 * Verifies that:
 *   1. ArcadeDBSemanticWriter creates two separate DaliSchema vertices (one per db_name).
 *   2. CONTAINS_TABLE edges connect each table exclusively to its own database's schema.
 *   3. A db_name-filtered query returns tables only from the requested database.
 *   4. An unfiltered query (no db_name) returns tables from both databases (proves the bug
 *      would exist without the filter, and that the filter is the correct guard).
 *
 * Run: ./gradlew test --tests "*SchemaDbIsolationTest*" --info
 */
class SchemaDbIsolationTest {

    private static final String DB = "target/test-db-schema-isolation";

    // ── PL/SQL fixtures ───────────────────────────────────────────────────────
    //
    // Both packages live inside schema "DWH" but belong to different databases.
    // Distinct table names (TBL_ISO_A/B vs TBL_ISO_C/D) let us verify isolation.

    /** Package from database "DWH" — references TBL_ISO_A (target) and TBL_ISO_B (source). */
    private static final String SQL_DB1 = """
            CREATE OR REPLACE PACKAGE BODY DWH.PKG_ISO_TEST AS
              PROCEDURE P1 IS
              BEGIN
                INSERT INTO DWH.TBL_ISO_A (id)
                  SELECT id FROM DWH.TBL_ISO_B;
              END;
            END;
            """;

    /** Package from database "DWH2" — same schema name "DWH", different tables. */
    private static final String SQL_DB2 = """
            CREATE OR REPLACE PACKAGE BODY DWH.PKG_ISO_TEST AS
              PROCEDURE P1 IS
              BEGIN
                INSERT INTO DWH.TBL_ISO_C (id)
                  SELECT id FROM DWH.TBL_ISO_D;
              END;
            END;
            """;

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    @BeforeEach
    void setUp() throws IOException { wipe(Path.of(DB)); }

    @AfterEach
    void tearDown() throws IOException { wipe(Path.of(DB)); }

    private static void wipe(Path p) throws IOException {
        if (!Files.exists(p)) return;
        try (var w = Files.walk(p)) {
            w.sorted(Comparator.reverseOrder())
             .forEach(x -> { try { Files.deleteIfExists(x); } catch (IOException ignored) {} });
        }
    }

    // ── Main test ─────────────────────────────────────────────────────────────

    @Test
    void duplicateSchemaName_differentDb_schemasAndTableEdgesAreIsolated() throws Exception {
        String dbPath = Path.of(DB).toAbsolutePath().toString();

        // ── Phase 1: write two databases into the same ArcadeDB instance ─────
        try (ArcadeDBSemanticWriter writer = new ArcadeDBSemanticWriter(dbPath)) {

            // Database "DWH" — schema DWH, tables TBL_ISO_A + TBL_ISO_B
            CanonicalPool poolDwh = writer.ensureCanonicalPool("DWH");
            UniversalSemanticEngine eng1 = parse(SQL_DB1, "DWH");
            writer.saveResult(
                eng1.getResult("DWH:pkg_iso_test.pck", "pkg_iso_test.pck", "PLSQL", 0L),
                new PipelineTimer(), poolDwh, "DWH");

            // Database "DWH2" — same schema name "DWH", different tables TBL_ISO_C + TBL_ISO_D
            CanonicalPool poolDwh2 = writer.ensureCanonicalPool("DWH2");
            UniversalSemanticEngine eng2 = parse(SQL_DB2, "DWH");
            writer.saveResult(
                eng2.getResult("DWH2:pkg_iso_test.pck", "pkg_iso_test.pck", "PLSQL", 0L),
                new PipelineTimer(), poolDwh2, "DWH2");
        }

        // ── Phase 2: verify isolation ─────────────────────────────────────────
        try (Database db = new DatabaseFactory(dbPath).open()) {

            // 1. Exactly two DaliSchema records share the same schema_name "DWH"
            long totalDwh = scalar(db,
                "SELECT count(*) AS cnt FROM DaliSchema WHERE schema_name = 'DWH'");
            assertEquals(2, totalDwh,
                "Must have exactly 2 DaliSchema records with schema_name='DWH' (one per db_name)");

            // 2. One per db_name
            long schDwh  = scalar(db,
                "SELECT count(*) AS cnt FROM DaliSchema WHERE schema_name='DWH' AND db_name='DWH'");
            long schDwh2 = scalar(db,
                "SELECT count(*) AS cnt FROM DaliSchema WHERE schema_name='DWH' AND db_name='DWH2'");
            assertEquals(1, schDwh,  "Exactly 1 DaliSchema for db=DWH,  schema=DWH");
            assertEquals(1, schDwh2, "Exactly 1 DaliSchema for db=DWH2, schema=DWH");

            // 3. CONTAINS_TABLE edges point only to the correct database's schema
            Set<String> tablesDwh  = tablesViaContainsTable(db, "DWH",  "DWH");
            Set<String> tablesDwh2 = tablesViaContainsTable(db, "DWH2", "DWH");

            assertFalse(tablesDwh.isEmpty(),  "DWH/DWH schema must own at least one table");
            assertFalse(tablesDwh2.isEmpty(), "DWH2/DWH schema must own at least one table");

            // Each schema owns its own tables
            assertTrue(tablesDwh.stream().anyMatch(t -> t.contains("TBL_ISO_A")),
                "DWH/DWH schema must contain TBL_ISO_A. Found: " + tablesDwh);
            assertTrue(tablesDwh.stream().anyMatch(t -> t.contains("TBL_ISO_B")),
                "DWH/DWH schema must contain TBL_ISO_B. Found: " + tablesDwh);
            assertTrue(tablesDwh2.stream().anyMatch(t -> t.contains("TBL_ISO_C")),
                "DWH2/DWH schema must contain TBL_ISO_C. Found: " + tablesDwh2);
            assertTrue(tablesDwh2.stream().anyMatch(t -> t.contains("TBL_ISO_D")),
                "DWH2/DWH schema must contain TBL_ISO_D. Found: " + tablesDwh2);

            // No table leaks across databases
            assertFalse(tablesDwh.stream().anyMatch(t -> t.contains("TBL_ISO_C")),
                "DWH/DWH schema must NOT contain TBL_ISO_C (belongs to DWH2). Found: " + tablesDwh);
            assertFalse(tablesDwh.stream().anyMatch(t -> t.contains("TBL_ISO_D")),
                "DWH/DWH schema must NOT contain TBL_ISO_D (belongs to DWH2). Found: " + tablesDwh);
            assertFalse(tablesDwh2.stream().anyMatch(t -> t.contains("TBL_ISO_A")),
                "DWH2/DWH schema must NOT contain TBL_ISO_A (belongs to DWH). Found: " + tablesDwh2);
            assertFalse(tablesDwh2.stream().anyMatch(t -> t.contains("TBL_ISO_B")),
                "DWH2/DWH schema must NOT contain TBL_ISO_B (belongs to DWH). Found: " + tablesDwh2);

            // 4. No table appears in both sets (complete isolation)
            Set<String> intersection = new HashSet<>(tablesDwh);
            intersection.retainAll(tablesDwh2);
            assertTrue(intersection.isEmpty(),
                "No table should appear under both schemas. Intersection: " + intersection);

            // 5. Without db_name filter both schemas' tables are returned together —
            //    this is the "would be broken without the filter" assertion, confirming
            //    the WHERE clause in SHUTTLE's Cypher is the necessary guard.
            Set<String> allTablesUnfiltered = tablesViaContainsTableUnfiltered(db, "DWH");
            assertTrue(allTablesUnfiltered.stream().anyMatch(t -> t.contains("TBL_ISO_A")),
                "Unfiltered query must return TBL_ISO_A");
            assertTrue(allTablesUnfiltered.stream().anyMatch(t -> t.contains("TBL_ISO_C")),
                "Unfiltered query must return TBL_ISO_C — proves mixing would occur without filter");
            assertTrue(allTablesUnfiltered.size() >= tablesDwh.size() + tablesDwh2.size(),
                "Unfiltered result must be superset of both filtered results. " +
                "Unfiltered=" + allTablesUnfiltered.size() +
                " DWH=" + tablesDwh.size() + " DWH2=" + tablesDwh2.size());

            System.out.println("DWH/DWH  tables: " + tablesDwh);
            System.out.println("DWH2/DWH tables: " + tablesDwh2);
            System.out.println("Unfiltered:      " + allTablesUnfiltered);
            System.out.println("All assertions passed.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Parse a PL/SQL string with the given defaultSchema, return the populated engine. */
    private static UniversalSemanticEngine parse(String sql, String defaultSchema) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        listener.setDefaultSchema(defaultSchema);
        PlSqlLexer lexer   = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser  = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    /**
     * Returns table_name values of all DaliTable vertices connected via CONTAINS_TABLE
     * from the single DaliSchema with the given (dbName, schemaName) pair.
     *
     * Strategy:
     *   1. Resolve the schema @rid.
     *   2. Expand its out('CONTAINS_TABLE') to DaliTable vertices.
     */
    private static Set<String> tablesViaContainsTable(Database db, String dbName, String schemaName) {
        // Step 1: get schema RID
        String schemaRid = null;
        try (ResultSet rs = db.query("sql",
                "SELECT @rid AS r FROM DaliSchema WHERE db_name = :d AND schema_name = :s LIMIT 1",
                Map.of("d", dbName, "s", schemaName))) {
            if (rs.hasNext()) {
                Object v = rs.next().getProperty("r");
                if (v != null) schemaRid = v.toString();
            }
        } catch (Exception e) {
            System.out.println("[WARN] schema RID lookup failed: " + e.getMessage());
        }
        if (schemaRid == null) return Set.of();

        // Step 2: expand CONTAINS_TABLE to get DaliTable vertices
        Set<String> tables = new LinkedHashSet<>();
        String expandSql = "SELECT table_name FROM (SELECT expand(out('CONTAINS_TABLE')) FROM " + schemaRid + ")";
        try (ResultSet rs = db.query("sql", expandSql)) {
            while (rs.hasNext()) {
                Object v = rs.next().getProperty("table_name");
                if (v != null) tables.add(v.toString().toUpperCase());
            }
        } catch (Exception e) {
            System.out.println("[WARN] CONTAINS_TABLE expand failed: " + e.getMessage());
        }
        return tables;
    }

    /**
     * Returns table_name values across ALL DaliSchema vertices with the given schemaName,
     * regardless of db_name — simulating the unfiltered (pre-fix) SHUTTLE query.
     */
    private static Set<String> tablesViaContainsTableUnfiltered(Database db, String schemaName) {
        Set<String> tables = new LinkedHashSet<>();
        // collect all schema RIDs with this name
        List<String> rids = new ArrayList<>();
        try (ResultSet rs = db.query("sql",
                "SELECT @rid AS r FROM DaliSchema WHERE schema_name = :s",
                Map.of("s", schemaName))) {
            while (rs.hasNext()) {
                Object v = rs.next().getProperty("r");
                if (v != null) rids.add(v.toString());
            }
        } catch (Exception e) {
            System.out.println("[WARN] schema RID lookup (unfiltered) failed: " + e.getMessage());
        }
        for (String rid : rids) {
            String expandSql = "SELECT table_name FROM (SELECT expand(out('CONTAINS_TABLE')) FROM " + rid + ")";
            try (ResultSet rs = db.query("sql", expandSql)) {
                while (rs.hasNext()) {
                    Object v = rs.next().getProperty("table_name");
                    if (v != null) tables.add(v.toString().toUpperCase());
                }
            } catch (Exception e) {
                System.out.println("[WARN] CONTAINS_TABLE expand failed for " + rid + ": " + e.getMessage());
            }
        }
        return tables;
    }

    private static long scalar(Database db, String sql) {
        try (ResultSet rs = db.query("sql", sql)) {
            if (rs.hasNext()) {
                Object v = rs.next().getProperty("cnt");
                return v instanceof Number n ? n.longValue() : 0L;
            }
        } catch (Exception e) {
            System.out.println("[WARN] scalar failed: " + sql + " — " + e.getMessage());
        }
        return 0L;
    }
}
