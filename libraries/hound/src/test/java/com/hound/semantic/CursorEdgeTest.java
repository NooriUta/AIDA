package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.CompensationStats;
import com.hound.semantic.model.RoutineInfo;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CursorEdgeTest {

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
    void fetchBulkCollect_emitsReadsFromCursorEdge() {
        String sql = """
                CREATE OR REPLACE PROCEDURE DWH.PROC_CURSOR_TEST IS
                  CURSOR c_emp IS SELECT id, name FROM DWH.EMPLOYEES;
                  TYPE t_emp IS TABLE OF c_emp%ROWTYPE;
                  l_data t_emp;
                BEGIN
                  OPEN c_emp;
                  FETCH c_emp BULK COLLECT INTO l_data;
                  CLOSE c_emp;
                END PROC_CURSOR_TEST;
                """;
        UniversalSemanticEngine engine = parse(sql);

        List<CompensationStats> stats = engine.getBuilder().getCompensationStats();
        long cursorEdges = stats.stream()
                .filter(cs -> CompensationStats.EDGE_READS_FROM_CURSOR.equals(cs.edgeType()))
                .count();
        assertTrue(cursorEdges > 0, "FETCH BULK COLLECT should emit READS_FROM_CURSOR edge");

        CompensationStats edge = stats.stream()
                .filter(cs -> CompensationStats.EDGE_READS_FROM_CURSOR.equals(cs.edgeType()))
                .findFirst().orElseThrow();
        assertTrue(edge.targetGeoid().contains(":CURSOR:C_EMP"),
                "Target geoid should reference cursor C_EMP, got: " + edge.targetGeoid());
        assertEquals(CompensationStats.KIND_CURSOR, edge.targetKind());
    }

    @Test
    void cursorDeclaration_registeredInRoutineInfo() {
        String sql = """
                CREATE OR REPLACE PROCEDURE DWH.PROC_WITH_CURSORS IS
                  CURSOR c_dept IS SELECT dept_id, dept_name FROM DWH.DEPARTMENTS;
                  CURSOR c_emp IS SELECT id, name FROM DWH.EMPLOYEES;
                  v_dummy NUMBER;
                BEGIN
                  NULL;
                END PROC_WITH_CURSORS;
                """;
        UniversalSemanticEngine engine = parse(sql);

        var routines = engine.getBuilder().getRoutines();
        String foundNames = routines.values().stream().map(RoutineInfo::getName)
                .toList().toString();
        RoutineInfo ri = routines.values().stream()
                .filter(r -> r.getName() != null && r.getName().contains("PROC_WITH_CURSORS"))
                .findFirst()
                .orElse(null);
        assertNotNull(ri, "Expected routine PROC_WITH_CURSORS, found: " + foundNames);

        List<RoutineInfo.CursorInfo> cursors = ri.getCursors();
        assertEquals(2, cursors.size(), "Should register 2 cursors");
        assertTrue(cursors.stream().anyMatch(c -> "C_DEPT".equals(c.name())));
        assertTrue(cursors.stream().anyMatch(c -> "C_EMP".equals(c.name())));
    }

    @Test
    void returningInto_emitsCompensationStats() {
        String sql = """
                CREATE OR REPLACE PROCEDURE DWH.PROC_RETURNING IS
                  v_id NUMBER;
                BEGIN
                  INSERT INTO DWH.TARGET_TABLE (col_a)
                  VALUES (1)
                  RETURNING id INTO v_id;
                END PROC_RETURNING;
                """;
        UniversalSemanticEngine engine = parse(sql);

        List<CompensationStats> stats = engine.getBuilder().getCompensationStats();
        long assignEdges = stats.stream()
                .filter(cs -> CompensationStats.EDGE_ASSIGNS_TO_VARIABLE.equals(cs.edgeType()))
                .filter(cs -> cs.targetGeoid().contains(":VAR:"))
                .count();
        assertTrue(assignEdges > 0,
                "RETURNING INTO should emit ASSIGNS_TO_VARIABLE CompensationStats for v_id");
    }

    @Test
    void noFetchNoBulk_noCursorEdge() {
        String sql = """
                CREATE OR REPLACE PROCEDURE DWH.PROC_NO_CURSOR IS
                  v_x NUMBER;
                BEGIN
                  SELECT id INTO v_x FROM DWH.MY_TABLE WHERE ROWNUM = 1;
                END PROC_NO_CURSOR;
                """;
        UniversalSemanticEngine engine = parse(sql);

        List<CompensationStats> stats = engine.getBuilder().getCompensationStats();
        long cursorEdges = stats.stream()
                .filter(cs -> CompensationStats.EDGE_READS_FROM_CURSOR.equals(cs.edgeType()))
                .count();
        assertEquals(0, cursorEdges, "Simple SELECT INTO should not produce READS_FROM_CURSOR edges");
    }
}
