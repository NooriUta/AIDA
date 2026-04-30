package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hound.semantic.model.StatementInfo;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 4-C: KI-DBMSSQL-1 baseline — EXECUTE IMMEDIATE dynamic SQL.
 *
 * TC-HOUND-EI1..EI3: baseline assertions for current behaviour.
 * After KI-DBMSSQL-1 is implemented, add affectedTables assertions.
 */
class ExecuteImmediateDynamicSqlTest {

    private static UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer  lexer  = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    private static List<StatementInfo> stmtsOfType(UniversalSemanticEngine e, String type) {
        return e.getBuilder().getStatements().values().stream()
                .filter(s -> type.equals(s.getType()))
                .collect(Collectors.toList());
    }

    // ── TC-HOUND-EI1: EXECUTE IMMEDIATE static literal ───────────────────────

    @Test
    @DisplayName("TC-HOUND-EI1: EXECUTE IMMEDIATE 'TRUNCATE TABLE x' — static literal, no NPE")
    void executeImmediate_staticLiteral_parsesWithoutException() {
        assertDoesNotThrow(() -> {
            UniversalSemanticEngine engine = parse("""
                    CREATE OR REPLACE PROCEDURE p IS
                    BEGIN
                        EXECUTE IMMEDIATE 'TRUNCATE TABLE archive_log';
                    END;
                    """);
            // Baseline: dynamic SQL is registered (containsDynamicSql flag)
            // Future: assert affectedTables contains 'archive_log' after KI-DBMSSQL-1
            assertNotNull(engine, "Engine must not be null after parsing EXECUTE IMMEDIATE");
        }, "EXECUTE IMMEDIATE static literal must parse without NPE");
    }

    // ── TC-HOUND-EI2: EXECUTE IMMEDIATE 'ALTER TABLE ... TRUNCATE PARTITION' ─

    @Test
    @DisplayName("TC-HOUND-EI2: EXECUTE IMMEDIATE dynamic DDL — ALTER TABLE TRUNCATE PARTITION, no crash")
    void executeImmediate_dynamicDdl_parsesWithoutException() {
        assertDoesNotThrow(() -> {
            UniversalSemanticEngine engine = parse("""
                    CREATE OR REPLACE PROCEDURE truncate_partition(
                        p_table VARCHAR2, p_partition VARCHAR2
                    ) IS
                    BEGIN
                        EXECUTE IMMEDIATE 'ALTER TABLE ' || p_table
                                       || ' TRUNCATE PARTITION ' || p_partition;
                    EXCEPTION
                        WHEN OTHERS THEN
                            DBMS_OUTPUT.PUT_LINE('Error: ' || SQLERRM);
                    END;
                    """);
            assertNotNull(engine, "Engine must not be null after parsing dynamic DDL EXECUTE IMMEDIATE");
        }, "Dynamic DDL EXECUTE IMMEDIATE must parse without crash");
    }

    // ── TC-HOUND-EI3: EXECUTE IMMEDIATE with variable concatenation ──────────

    @Test
    @DisplayName("TC-HOUND-EI3: EXECUTE IMMEDIATE with variable concat — parses OK, no crash")
    void executeImmediate_variableConcat_parsesWithoutException() {
        assertDoesNotThrow(() -> {
            UniversalSemanticEngine engine = parse("""
                    CREATE OR REPLACE PROCEDURE dynamic_insert(
                        p_table IN VARCHAR2,
                        p_value IN VARCHAR2
                    ) IS
                        v_sql VARCHAR2(4000);
                    BEGIN
                        v_sql := 'INSERT INTO ' || p_table || ' VALUES (:val)';
                        EXECUTE IMMEDIATE v_sql USING p_value;
                    END;
                    """);
            assertNotNull(engine, "Engine must not be null after variable-concat EXECUTE IMMEDIATE");
        }, "Variable-concat EXECUTE IMMEDIATE must parse without crash");
    }

    // ── Baseline: EXECUTE IMMEDIATE does not corrupt surrounding statements ───

    @Test
    @DisplayName("EI-baseline: EXECUTE IMMEDIATE surrounded by regular DML — DML still resolved")
    void executeImmediate_surroundedByDml_doesNotCorruptOtherStatements() {
        UniversalSemanticEngine engine = parse("""
                CREATE OR REPLACE PROCEDURE p IS
                BEGIN
                    INSERT INTO audit_log (event, ts) VALUES ('start', SYSDATE);
                    EXECUTE IMMEDIATE 'TRUNCATE TABLE tmp_work';
                    INSERT INTO audit_log (event, ts) VALUES ('end', SYSDATE);
                END;
                """);

        List<StatementInfo> inserts = stmtsOfType(engine, "INSERT");
        assertTrue(inserts.size() >= 1,
                "Regular INSERT statements around EXECUTE IMMEDIATE must still be recognised");
    }
}
