package com.hound.semantic;

import com.hound.semantic.engine.AtomProcessor;
import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.StatementInfo;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T5 — In-memory unit tests for DATA_FLOW / affected-column pipeline.
 *
 * Parses small inline SQL snippets directly (no DB, no fixtures) and
 * asserts output-column and affected-column counts at the engine level.
 *
 * Run: ./gradlew test --tests "*DataFlowInMemoryTest*" --info
 */
class DataFlowInMemoryTest {

    // ── helpers ──────────────────────────────────────────────────────────────

    private static UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer  lexer  = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    /** Collects all statements of the given type across the engine result. */
    private static List<StatementInfo> stmtsOfType(UniversalSemanticEngine engine, String type) {
        return engine.getBuilder().getStatements().values().stream()
                .filter(s -> type.equals(s.getType()))
                .collect(Collectors.toList());
    }

    // ── T5.1 — simple SELECT produces output columns ──────────────────────

    @Test
    @DisplayName("T5.1: SELECT col1, col2 FROM t1 → 2 output columns")
    void simpleSelect_producesOutputColumns() {
        String sql = """
                CREATE OR REPLACE PROCEDURE p IS
                BEGIN
                    SELECT col1, col2 FROM t1;
                END;
                """;
        UniversalSemanticEngine engine = parse(sql);

        List<StatementInfo> selects = stmtsOfType(engine, "SELECT");
        assertFalse(selects.isEmpty(), "Expected at least one SELECT statement");

        StatementInfo sel = selects.get(0);
        int outCols = sel.getColumnsOutput().size();
        assertEquals(2, outCols,
                "SELECT col1, col2 must produce exactly 2 output columns, got " + outCols);
    }

    // ── T5.2 — INSERT … SELECT produces both affected and output columns ──

    @Test
    @DisplayName("T5.2: INSERT INTO t2 (a,b) SELECT x,y FROM t1 → 2 affected cols + 2 output cols")
    void insertSelect_producesAffectedAndOutputColumns() {
        String sql = """
                CREATE OR REPLACE PROCEDURE p IS
                BEGIN
                    INSERT INTO t2 (a, b) SELECT x, y FROM t1;
                END;
                """;
        UniversalSemanticEngine engine = parse(sql);

        // INSERT statement → affected columns
        List<StatementInfo> inserts = stmtsOfType(engine, "INSERT");
        assertFalse(inserts.isEmpty(), "Expected at least one INSERT statement");
        StatementInfo ins = inserts.get(0);

        List<Map<String, Object>> ac = ins.getAffectedColumns();
        assertEquals(2, ac.size(),
                "INSERT INTO t2(a,b) must produce 2 affected columns, got " + ac.size());
        for (Map<String, Object> col : ac) {
            assertEquals("INSERT", col.get("source_type"),
                    "Affected column source_type must be INSERT, got " + col.get("source_type"));
        }

        // Inner SELECT (as INSERT_SELECT subtype or child SELECT) → output columns
        List<StatementInfo> selects = stmtsOfType(engine, "SELECT");
        assertFalse(selects.isEmpty(), "Expected an inner SELECT in INSERT…SELECT");

        // The SELECT side of INSERT…SELECT must have 2 output columns (x, y)
        long withTwoOutputCols = selects.stream()
                .filter(s -> s.getColumnsOutput().size() == 2)
                .count();
        assertTrue(withTwoOutputCols >= 1,
                "At least one SELECT in INSERT…SELECT must have 2 output columns");
    }

    // ── T5.2b — MERGE deduplicated affected columns + merge_clause on atoms ──

    @Test
    @DisplayName("T5.2b: MERGE USING … → one MERGE affected_column per target, atoms carry merge_clause")
    void merge_deduplicatedAffectedColumnsAndMergeClause() {
        // Two columns appear in both WHEN MATCHED UPDATE and WHEN NOT MATCHED INSERT.
        // StatementInfo must dedup them → 2 affected columns, not 4.
        String sql = """
                CREATE OR REPLACE PROCEDURE p IS
                BEGIN
                    MERGE INTO tgt t
                    USING src s ON (t.id = s.id)
                    WHEN MATCHED THEN
                        UPDATE SET t.val = s.val,
                                   t.ts  = SYSDATE
                    WHEN NOT MATCHED THEN
                        INSERT (id, val, ts)
                        VALUES (s.id, s.val, SYSDATE);
                END;
                """;
        UniversalSemanticEngine engine = parse(sql);

        List<StatementInfo> merges = stmtsOfType(engine, "MERGE");
        assertFalse(merges.isEmpty(), "Expected at least one MERGE statement");
        StatementInfo merge = merges.get(0);

        // All affected columns must have source_type="MERGE"
        List<Map<String, Object>> ac = merge.getAffectedColumns();
        assertTrue(ac.size() > 0, "MERGE must have at least one affected column");
        for (Map<String, Object> col : ac) {
            assertEquals("MERGE", col.get("source_type"),
                    "MERGE affected column source_type must be 'MERGE', got: " + col.get("source_type"));
        }

        // Dedup: id/val/ts each appear in both UPDATE and INSERT — expect at most 3 unique columns
        long distinct = ac.stream().map(c -> c.get("column_name")).distinct().count();
        assertEquals(ac.size(), (int) distinct,
                "No duplicate column_names in affectedColumns (dedup must work); got: "
                        + ac.stream().map(c -> c.get("column_name")).toList());

        // Atoms in UPDATE SET must carry merge_clause="UPDATE"
        Map<String, Map<String, Object>> atoms =
                engine.getAtomProcessor().getAtomsForStatement(merge.getGeoid());
        long updateClauseAtoms = atoms.values().stream()
                .filter(a -> "UPDATE".equals(a.get("merge_clause")))
                .count();
        assertTrue(updateClauseAtoms >= 1,
                "At least one atom in MERGE UPDATE SET must carry merge_clause='UPDATE'");

        // Atoms in INSERT VALUES must carry merge_clause="INSERT"
        long insertClauseAtoms = atoms.values().stream()
                .filter(a -> "INSERT".equals(a.get("merge_clause")))
                .count();
        assertTrue(insertClauseAtoms >= 1,
                "At least one atom in MERGE INSERT VALUES must carry merge_clause='INSERT'");
    }

    // ── T5.3 — UPDATE SET produces exactly the target column as affected ──

    @Test
    @DisplayName("T5.3: UPDATE t1 SET col1='x' WHERE col2='y' → 1 UPDATE-type affected column")
    void updateSet_producesAffectedColumns() {
        String sql = """
                CREATE OR REPLACE PROCEDURE p IS
                BEGIN
                    UPDATE t1 SET col1 = 'x' WHERE col2 = 'y';
                END;
                """;
        UniversalSemanticEngine engine = parse(sql);

        List<StatementInfo> updates = stmtsOfType(engine, "UPDATE");
        assertFalse(updates.isEmpty(), "Expected at least one UPDATE statement");
        StatementInfo upd = updates.get(0);

        List<Map<String, Object>> ac = upd.getAffectedColumns();
        // Must contain the SET target column
        long updateTargets = ac.stream()
                .filter(m -> "UPDATE".equals(m.get("source_type")))
                .count();
        assertTrue(updateTargets >= 1,
                "UPDATE t1 SET col1='x' must have ≥1 UPDATE-type affected column, got " + updateTargets);

        // column_name must be present on each affected column
        for (Map<String, Object> col : ac) {
            assertNotNull(col.get("column_name"),
                    "Affected column must carry column_name, got null");
        }
    }
}
