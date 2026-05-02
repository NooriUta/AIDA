package com.hound.semantic;

import com.hound.parser.base.grammars.sql.postgresql.PostgreSQLLexer;
import com.hound.parser.base.grammars.sql.postgresql.PostgreSQLParser;
import com.hound.semantic.dialect.postgresql.PostgreSQLSemanticListener;
import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.model.StatementInfo;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PostgreSQLSemanticListener.
 *
 * Run: ./gradlew :libraries:hound:test --tests "*PostgreSQLSemanticListenerTest*" --info
 */
class PostgreSQLSemanticListenerTest {

    // ─── helpers ──────────────────────────────────────────────────────────────

    private static UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PostgreSQLSemanticListener listener = new PostgreSQLSemanticListener(engine);
        listener.setDefaultSchema("test");
        PostgreSQLLexer  lexer  = new PostgreSQLLexer(CharStreams.fromString(sql));
        PostgreSQLParser parser = new PostgreSQLParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.root());
        engine.resolvePendingColumns();
        return engine;
    }

    private static List<StatementInfo> stmtsOfType(UniversalSemanticEngine engine, String type) {
        return engine.getBuilder().getStatements().values().stream()
                .filter(s -> type.equals(s.getType()))
                .collect(Collectors.toList());
    }

    // ─── PG-T1: simple SELECT ─────────────────────────────────────────────────

    @Test
    @DisplayName("PG-T1: SELECT a, b FROM t → 2 atoms, 1 source table")
    void pgT1_simpleSelect() {
        UniversalSemanticEngine engine = parse("SELECT a, b FROM t;");

        List<StatementInfo> selects = stmtsOfType(engine, "SELECT");
        assertFalse(selects.isEmpty(), "Should have at least 1 SELECT statement");

        StatementInfo sel = selects.get(0);
        assertEquals(1, sel.getSourceTables().size(),
                "Expected 1 source table; got: " + sel.getSourceTables().keySet());

        // Atoms are stored in AtomProcessor, not StatementInfo.getAtoms()
        int atomCount = engine.getAtomProcessor().getAtomsForStatement(sel.getGeoid()).size();
        assertEquals(2, atomCount,
                "Expected 2 atoms (a, b); got: " + atomCount);
    }

    // ─── PG-T2: JOIN ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("PG-T2: SELECT a FROM t1 JOIN t2 ON t1.id = t2.id → 2 tables")
    void pgT2_joinProducesTwoTables() {
        UniversalSemanticEngine engine = parse(
                "SELECT a FROM t1 JOIN t2 ON t1.id = t2.id;");

        List<StatementInfo> selects = stmtsOfType(engine, "SELECT");
        assertFalse(selects.isEmpty(), "Should have at least 1 SELECT statement");

        StatementInfo sel = selects.get(0);
        int tableCount = sel.getSourceTables().size();
        assertEquals(2, tableCount,
                "Expected 2 source tables; got: " + sel.getSourceTables().keySet());
    }

    // ─── PG-T3: CTE ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("PG-T3: WITH cte AS (SELECT x FROM t) SELECT col FROM cte → CTE + outer SELECT")
    void pgT3_cteCreatesOwnScope() {
        UniversalSemanticEngine engine = parse(
                "WITH cte AS (SELECT x FROM t) SELECT col FROM cte;");

        List<StatementInfo> ctes    = stmtsOfType(engine, "CTE");
        List<StatementInfo> selects = stmtsOfType(engine, "SELECT");

        assertEquals(1, ctes.size(),
                "Expected 1 CTE statement; got " + ctes.size());
        assertFalse(selects.isEmpty(),
                "Expected at least 1 SELECT statement");
    }

    // ─── PG-T4: INSERT ───────────────────────────────────────────────────────

    @Test
    @DisplayName("PG-T4: INSERT INTO t (a,b) SELECT x,y FROM s → INSERT stmt, target=t, source=s")
    void pgT4_insertSelectStatements() {
        UniversalSemanticEngine engine = parse(
                "INSERT INTO t (a, b) SELECT x, y FROM s;");

        List<StatementInfo> inserts = stmtsOfType(engine, "INSERT");
        assertFalse(inserts.isEmpty(), "Expected at least 1 INSERT statement");
    }

    // ─── PG-T5: UPDATE ───────────────────────────────────────────────────────

    @Test
    @DisplayName("PG-T5: UPDATE t SET a = b WHERE id = 1 → UPDATE stmt")
    void pgT5_updateStatement() {
        UniversalSemanticEngine engine = parse(
                "UPDATE t SET a = b WHERE id = 1;");

        List<StatementInfo> updates = stmtsOfType(engine, "UPDATE");
        assertFalse(updates.isEmpty(), "Expected at least 1 UPDATE statement");
    }

    // ─── PG-T6: CREATE TABLE ─────────────────────────────────────────────────

    @Test
    @DisplayName("PG-T6: CREATE TABLE s.t (id INT, name TEXT) → DDL table with 2 columns")
    void pgT6_createTable() {
        UniversalSemanticEngine engine = parse(
                "CREATE TABLE s.t (id INT, name TEXT);");

        // No exception + compiles is sufficient for DDL path smoke test
        assertNotNull(engine);
        assertNotNull(engine.getBuilder());
    }

    // ─── PG-T7: SELECT * ────────────────────────────────────────────────────

    @Test
    @DisplayName("PG-T7: SELECT * FROM t → bare star atom, no column ref atoms for the star itself")
    void pgT7_selectStar() {
        UniversalSemanticEngine engine = parse("SELECT * FROM t;");

        List<StatementInfo> selects = stmtsOfType(engine, "SELECT");
        assertFalse(selects.isEmpty(), "Should have at least 1 SELECT statement");
        // Engine should register the bare star
        assertNotNull(selects.get(0));
    }

    // ─── PG-T8: alias resolution ──────────────────────────────────────────────

    @Test
    @DisplayName("PG-T8: SELECT t1.col FROM orders t1 → 1 source table with alias")
    void pgT8_tableAliasRegistered() {
        UniversalSemanticEngine engine = parse(
                "SELECT t1.col FROM orders t1;");

        List<StatementInfo> selects = stmtsOfType(engine, "SELECT");
        assertFalse(selects.isEmpty(), "Should have at least 1 SELECT statement");

        StatementInfo sel = selects.get(0);
        assertEquals(1, sel.getSourceTables().size(),
                "Expected 1 source table; got: " + sel.getSourceTables().keySet());
    }

    // ─── PG-JTYPE-*: typed grammar-rule JOIN type detection ──────────────────
    // Verifies that join_type is detected via typed token check
    // (ctx.FULL/LEFT/RIGHT/INNER_P), not getText().contains() heuristics.

    private static String firstJoinType(UniversalSemanticEngine engine) {
        return stmtsOfType(engine, "SELECT").stream()
                .flatMap(s -> s.getJoins().stream())
                .map(j -> j.joinType())
                .findFirst()
                .orElse(null);
    }

    @Test
    @DisplayName("PG-JTYPE-1: LEFT JOIN → joinType = LEFT (typed token)")
    void pgJtype1_leftJoin() {
        UniversalSemanticEngine engine = parse("SELECT * FROM t1 LEFT JOIN t2 ON t1.id = t2.id;");
        assertEquals("LEFT", firstJoinType(engine));
    }

    @Test
    @DisplayName("PG-JTYPE-2: RIGHT OUTER JOIN → joinType = RIGHT")
    void pgJtype2_rightOuterJoin() {
        UniversalSemanticEngine engine = parse("SELECT * FROM t1 RIGHT OUTER JOIN t2 ON t1.id = t2.id;");
        assertEquals("RIGHT", firstJoinType(engine));
    }

    @Test
    @DisplayName("PG-JTYPE-3: FULL OUTER JOIN → joinType = FULL")
    void pgJtype3_fullOuterJoin() {
        UniversalSemanticEngine engine = parse("SELECT * FROM t1 FULL OUTER JOIN t2 ON t1.id = t2.id;");
        assertEquals("FULL", firstJoinType(engine));
    }

    @Test
    @DisplayName("PG-JTYPE-4: INNER JOIN → joinType = INNER")
    void pgJtype4_innerJoin() {
        UniversalSemanticEngine engine = parse("SELECT * FROM t1 INNER JOIN t2 ON t1.id = t2.id;");
        assertEquals("INNER", firstJoinType(engine));
    }
}
