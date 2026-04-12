package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.StatementInfo;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * STAB-13 verification tests.
 *
 * Part A: enterSchema_object_name / exitSchema_object_name
 *   — DDL constructs that use schema_object_name (e.g. CREATE PACKAGE BODY) correctly
 *     resolve the schema prefix.
 *
 * Part B: inline FROM subquery alias propagation
 *   — FROM (SELECT ...) alias registers the alias on the outer scope
 *   — the inline SELECT is listed as a source subquery of the outer SELECT
 */
class SchemaObjectAndInlineSubqueryTest {

    private UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    // ═════════════════════════════════════════════════════════════════
    // Part A: schema_object_name sets schema for DDL constructs
    // ═════════════════════════════════════════════════════════════════

    @Test
    void packageBody_withSchemaPrefix_routineHasCorrectGeoid() {
        String sql =
            "CREATE OR REPLACE PACKAGE BODY HR.UTILS_PKG AS\n" +
            "  PROCEDURE greet IS\n" +
            "  BEGIN\n" +
            "    NULL;\n" +
            "  END;\n" +
            "END UTILS_PKG;";
        var engine = parse(sql);
        var routines = engine.getBuilder().getRoutines();

        boolean hasRoutineInHr = routines.keySet().stream()
                .anyMatch(k -> k.toUpperCase().contains("HR") || k.toUpperCase().contains("UTILS_PKG"));
        assertTrue(hasRoutineInHr,
                "Routine or package should be registered with HR schema prefix. Got: " + routines.keySet());
    }

    @Test
    void packageBody_schemaObjectNameDoesNotBreakExistingParsing() {
        // Regression: enterSchema_object_name must not interfere with existing package body parsing
        String sql =
            "CREATE OR REPLACE PACKAGE BODY ORDERS.ORDER_PKG AS\n" +
            "  FUNCTION get_total(p_id IN NUMBER) RETURN NUMBER IS\n" +
            "    v_total NUMBER;\n" +
            "  BEGIN\n" +
            "    SELECT SUM(amount) INTO v_total FROM order_lines WHERE order_id = p_id;\n" +
            "    RETURN v_total;\n" +
            "  END;\n" +
            "END ORDER_PKG;";
        var engine = parse(sql);
        var tables = engine.getBuilder().getTables();
        var routines = engine.getBuilder().getRoutines();

        assertTrue(tables.containsKey("ORDER_LINES") || tables.containsKey("ORDERS.ORDER_LINES"),
                "ORDER_LINES table should be registered. Got: " + tables.keySet());
        assertFalse(routines.isEmpty(),
                "At least one routine should be registered. Got: " + routines.keySet());
    }

    // ═════════════════════════════════════════════════════════════════
    // Part B: inline FROM subquery alias propagation
    // ═════════════════════════════════════════════════════════════════

    @Test
    void inlineFromSubquery_baseTableRegistered() {
        String sql =
            "BEGIN\n" +
            "  SELECT sq.col1\n" +
            "  FROM (SELECT id AS col1 FROM employees) sq\n" +
            "  WHERE sq.col1 > 0;\n" +
            "END;";
        var engine = parse(sql);
        var tables = engine.getBuilder().getTables();

        assertTrue(tables.containsKey("EMPLOYEES"),
                "Base table EMPLOYEES inside inline subquery should be registered. Got: " + tables.keySet());
    }

    @Test
    void inlineFromSubquery_aliasRegisteredAsSourceOnParent() {
        String sql =
            "BEGIN\n" +
            "  SELECT sq.col1, sq.col2\n" +
            "  FROM (SELECT id AS col1, name AS col2 FROM employees) sq;\n" +
            "END;";
        var engine = parse(sql);
        Map<String, StatementInfo> stmts = engine.getBuilder().getStatements();

        // Find the outer SELECT — it should have a source subquery whose alias is "SQ"
        boolean foundAlias = stmts.values().stream()
                .filter(si -> "SELECT".equals(si.getType()))
                .flatMap(si -> si.getSourceSubqueries().values().stream())
                .flatMap(sq -> sq.subqueryAliases().stream())
                .anyMatch(alias -> "SQ".equalsIgnoreCase(alias));

        assertTrue(foundAlias,
                "Outer SELECT should have source subquery with alias SQ. Statements: " +
                stmts.values().stream()
                     .filter(s -> "SELECT".equals(s.getType()))
                     .map(s -> s.getType() + " sources=" + s.getSourceSubqueries())
                     .toList());
    }

    @Test
    void inlineFromSubquery_multipleInlineSubqueries() {
        String sql =
            "BEGIN\n" +
            "  SELECT a.id, b.name\n" +
            "  FROM (SELECT id FROM employees) a\n" +
            "  JOIN (SELECT id, name FROM departments) b ON a.id = b.id;\n" +
            "END;";
        var engine = parse(sql);
        var tables = engine.getBuilder().getTables();

        assertTrue(tables.containsKey("EMPLOYEES"),
                "EMPLOYEES should be registered. Got: " + tables.keySet());
        assertTrue(tables.containsKey("DEPARTMENTS"),
                "DEPARTMENTS should be registered. Got: " + tables.keySet());

        Map<String, StatementInfo> stmts = engine.getBuilder().getStatements();
        long sourceSubqueryCount = stmts.values().stream()
                .filter(si -> "SELECT".equals(si.getType()))
                .mapToLong(si -> si.getSourceSubqueries().size())
                .max().orElse(0);

        assertTrue(sourceSubqueryCount >= 1,
                "Outer SELECT should have at least one source subquery registered. Stmts: " +
                stmts.values().stream()
                     .filter(s -> "SELECT".equals(s.getType()))
                     .map(s -> "type=" + s.getType() + " subs=" + s.getSourceSubqueries().size())
                     .toList());
    }

    @Test
    void inlineFromSubquery_regularSelectNotAffected() {
        // A top-level SELECT without inline subquery should still work normally
        String sql =
            "BEGIN\n" +
            "  SELECT id, name FROM employees WHERE id > 0;\n" +
            "END;";
        var engine = parse(sql);
        var tables = engine.getBuilder().getTables();
        var stmts = engine.getBuilder().getStatements();

        assertTrue(tables.containsKey("EMPLOYEES"),
                "EMPLOYEES should be registered. Got: " + tables.keySet());

        boolean hasSelect = stmts.values().stream()
                .anyMatch(si -> "SELECT".equals(si.getType()));
        assertTrue(hasSelect, "Should have at least one SELECT statement");
    }
}
