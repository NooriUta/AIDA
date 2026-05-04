package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.CompensationStats;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AssignmentEdgeTest {

    private static final String SIMPLE_ASSIGNMENT = """
            CREATE OR REPLACE PROCEDURE DWH.PROC_ASSIGN_TEST IS
              v_name  VARCHAR2(100);
              v_count NUMBER;
            BEGIN
              v_name  := 'hello';
              v_count := 42;
            END PROC_ASSIGN_TEST;
            """;

    private static final String PARAM_ASSIGNMENT = """
            CREATE OR REPLACE PROCEDURE DWH.PROC_PARAM_OUT(
              p_result OUT NUMBER,
              p_input  IN  NUMBER
            ) IS
              v_temp NUMBER;
            BEGIN
              v_temp   := p_input * 2;
              p_result := v_temp + 1;
            END PROC_PARAM_OUT;
            """;

    private UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        listener.setDefaultSchema("DWH");
        PlSqlLexer  lexer  = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    @Test
    void simpleVariableAssignment_emitsCompensationStats() {
        UniversalSemanticEngine engine = parse(SIMPLE_ASSIGNMENT);
        List<CompensationStats> stats = engine.getBuilder().getCompensationStats();

        long varAssigns = stats.stream()
                .filter(cs -> CompensationStats.EDGE_ASSIGNS_TO_VARIABLE.equals(cs.edgeType()))
                .filter(cs -> CompensationStats.KIND_VARIABLE.equals(cs.targetKind()))
                .count();

        assertTrue(varAssigns >= 2,
                "Expected at least 2 ASSIGNS_TO_VARIABLE edges for v_name and v_count, got " + varAssigns);

        boolean hasVarTarget = stats.stream()
                .anyMatch(cs -> cs.targetGeoid() != null && cs.targetGeoid().contains(":VAR:"));
        assertTrue(hasVarTarget, "Target geoid should reference a VAR index");
    }

    @Test
    void parameterAssignment_emitsCompensationStats() {
        UniversalSemanticEngine engine = parse(PARAM_ASSIGNMENT);
        List<CompensationStats> stats = engine.getBuilder().getCompensationStats();

        long paramAssigns = stats.stream()
                .filter(cs -> CompensationStats.EDGE_ASSIGNS_TO_VARIABLE.equals(cs.edgeType()))
                .filter(cs -> CompensationStats.KIND_PARAMETER.equals(cs.targetKind()))
                .count();

        assertTrue(paramAssigns >= 1,
                "Expected at least 1 assignment to parameter p_result, got " + paramAssigns);

        boolean hasParamTarget = stats.stream()
                .anyMatch(cs -> cs.targetGeoid() != null && cs.targetGeoid().contains(":PARAM:"));
        assertTrue(hasParamTarget, "Target geoid should reference a PARAM index for p_result");
    }

    @Test
    void noAssignmentInSelectOnly_noCompensationStats() {
        String sql = """
                CREATE OR REPLACE PROCEDURE DWH.PROC_SELECT IS
                BEGIN
                  NULL;
                END PROC_SELECT;
                """;
        UniversalSemanticEngine engine = parse(sql);
        List<CompensationStats> stats = engine.getBuilder().getCompensationStats();
        assertTrue(stats.isEmpty(), "No assignments → no CompensationStats");
    }
}
