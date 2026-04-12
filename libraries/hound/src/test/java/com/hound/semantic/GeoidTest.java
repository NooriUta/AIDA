package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GeoidTest {

    private static final String TEST_SQL = """
        CREATE OR REPLACE PACKAGE BODY test_pkg AS
            PROCEDURE hello IS
            BEGIN NULL; END hello;

            FUNCTION get_number RETURN NUMBER IS
            BEGIN RETURN 42; END get_number;

            PROCEDURE process_orders(p_year IN NUMBER) IS
                l_cnt NUMBER;
            BEGIN
                SELECT count(*) INTO l_cnt FROM orders WHERE year = p_year;
            END process_orders;
        END test_pkg;
        """;

    private UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        return engine;
    }

    @Test
    void routineGeoids_containPackageTypeAndName() {
        var engine = parse(TEST_SQL);
        var routines = engine.getBuilder().getRoutines();

        assertTrue(routines.containsKey("TEST_PKG:PROCEDURE:HELLO"),
            "Missing TEST_PKG:PROCEDURE:HELLO. Got: " + routines.keySet());
        assertTrue(routines.containsKey("TEST_PKG:FUNCTION:GET_NUMBER"),
            "Missing TEST_PKG:FUNCTION:GET_NUMBER. Got: " + routines.keySet());
        assertTrue(routines.containsKey("TEST_PKG:PROCEDURE:PROCESS_ORDERS"),
            "Missing TEST_PKG:PROCEDURE:PROCESS_ORDERS. Got: " + routines.keySet());
    }

    @Test
    void routineGeoids_useColonSeparator() {
        var engine = parse(TEST_SQL);
        for (String geoid : engine.getBuilder().getRoutines().keySet()) {
            assertTrue(geoid.contains(":"),
                "Routine geoid must use ':' separator: " + geoid);
            assertFalse(geoid.matches(".*\\.[A-Z]+$"),
                "Routine geoid must not end with '.NAME' pattern: " + geoid);
        }
    }

    @Test
    void statementGeoids_areHierarchical() {
        var engine = parse(TEST_SQL);
        var stmts = engine.getBuilder().getStatements();

        boolean found = stmts.keySet().stream()
            .anyMatch(k -> k.contains("PROCESS_ORDERS") && k.contains("SELECT"));
        assertTrue(found,
            "Expected hierarchical geoid with PROCESS_ORDERS+SELECT. Got: " + stmts.keySet());
    }
}
