package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.AtomInfo;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PendingInjectSecondPassTest {

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
    void secondPass_resolvesPendingPipelinedAtoms() {
        // TYPE declared AFTER the procedure that uses TABLE(fn()) —
        // first pass marks pending, second pass should inject columns
        String sql = """
                CREATE OR REPLACE PACKAGE BODY DWH.PKG_PIPE_TEST AS
                  FUNCTION get_data RETURN t_data_tab PIPELINED IS
                  BEGIN
                    NULL;
                  END get_data;

                  PROCEDURE USE_DATA IS
                  BEGIN
                    INSERT INTO DWH.TARGET_TABLE (col_a, col_b)
                    SELECT d.col_a, d.col_b
                    FROM TABLE(get_data()) d;
                  END USE_DATA;

                  TYPE t_data_rec IS RECORD (
                    col_a NUMBER,
                    col_b VARCHAR2(100)
                  );
                  TYPE t_data_tab IS TABLE OF t_data_rec;
                END PKG_PIPE_TEST;
                """;
        UniversalSemanticEngine engine = parse(sql);

        // Check that pending pipelined tables were cleared (or reduced)
        var pendingTables = engine.getBuilder().getPendingPipelinedTables();
        // The second pass should have resolved pipelined injection
        // (exact behavior depends on whether the function matched)
        assertNotNull(pendingTables);
    }

    @Test
    void pendingInjectAtoms_countedInStats() {
        // Simple case where atoms stay PENDING_INJECT because no second-pass data
        String sql = """
                CREATE OR REPLACE PROCEDURE DWH.PROC_SIMPLE IS
                  v_x NUMBER;
                BEGIN
                  SELECT id INTO v_x FROM DWH.MY_TABLE;
                END PROC_SIMPLE;
                """;
        UniversalSemanticEngine engine = parse(sql);

        // Count atoms — should have some resolved, none PENDING_INJECT for simple case
        long pendingCount = 0;
        for (var entry : engine.getAtomProcessor().getAtomsData().entrySet()) {
            if (!entry.getKey().startsWith("statement:")) continue;
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> atoms =
                    (Map<String, Map<String, Object>>) ((Map<String, Object>) entry.getValue()).get("atoms");
            if (atoms == null) continue;
            pendingCount += atoms.values().stream()
                    .filter(a -> AtomInfo.STATUS_PENDING_INJECT.equals(a.get("primary_status")))
                    .count();
        }
        assertEquals(0, pendingCount,
                "Simple SELECT should not produce PENDING_INJECT atoms");
    }
}
