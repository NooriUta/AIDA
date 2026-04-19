package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.RoutineInfo;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verifies that PROCEDURE_SPEC (package spec) + PROCEDURE (package body)
 * are merged into a single DaliRoutine entry — not two separate vertices.
 *
 * Root cause: PROCEDURE_SPEC used to embed "_SPEC" in the geoid key, producing
 * two separate RoutineInfo objects with no link between them.
 *
 * Fix: StructureAndLineageBuilder.addRoutine() normalises _SPEC → base type,
 * so both spec and body share the same geoid.  RoutineInfo tracks has_spec /
 * has_body flags; addTypedParameter is idempotent (skips duplicate names).
 */
class ProcedureSpecBodyMergeTest {

    private static final String SPEC_AND_BODY = """
            -- Package SPEC
            CREATE OR REPLACE PACKAGE DWH.PKG_MERGE_TEST AS
              PROCEDURE CALC(p_year IN NUMBER, p_code IN VARCHAR2);
              FUNCTION  GET_RATE(p_ccy IN VARCHAR2) RETURN NUMBER;
            END PKG_MERGE_TEST;

            -- Package BODY
            CREATE OR REPLACE PACKAGE BODY DWH.PKG_MERGE_TEST AS
              PROCEDURE CALC(p_year IN NUMBER, p_code IN VARCHAR2) IS
                v_cnt NUMBER := 0;
              BEGIN
                INSERT INTO DWH.LOG_TBL (yr, code) VALUES (p_year, p_code);
              END CALC;

              FUNCTION GET_RATE(p_ccy IN VARCHAR2) RETURN NUMBER IS
              BEGIN
                RETURN 1.0;
              END GET_RATE;
            END PKG_MERGE_TEST;
            """;

    private static final String BODY_ONLY = """
            CREATE OR REPLACE PACKAGE BODY DWH.PKG_BODY_ONLY AS
              PROCEDURE SAVE(p_id IN NUMBER) IS
              BEGIN NULL; END SAVE;
            END PKG_BODY_ONLY;
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

    // ── Spec + Body → single routine entry ───────────────────────────────────

    @Test
    void specAndBody_produceSingleRoutineEntry() {
        Map<String, RoutineInfo> routines = parse(SPEC_AND_BODY).getResult()
                .getStructure().getRoutines();

        long calcCount = routines.keySet().stream()
                .filter(k -> k.contains("CALC")).count();
        long rateCount = routines.keySet().stream()
                .filter(k -> k.contains("GET_RATE")).count();

        assertEquals(1, calcCount,
                "PROCEDURE CALC must produce exactly one routine entry (spec+body merged). Found: "
                        + routines.keySet());
        assertEquals(1, rateCount,
                "FUNCTION GET_RATE must produce exactly one routine entry (spec+body merged). Found: "
                        + routines.keySet());
    }

    @Test
    void specAndBody_geoidUsesBaseType_notSpecSuffix() {
        Map<String, RoutineInfo> routines = parse(SPEC_AND_BODY).getResult()
                .getStructure().getRoutines();

        assertTrue(routines.containsKey("DWH.PKG_MERGE_TEST:PROCEDURE:CALC"),
                "Geoid must use PROCEDURE (not PROCEDURE_SPEC). Keys: " + routines.keySet());
        assertTrue(routines.containsKey("DWH.PKG_MERGE_TEST:FUNCTION:GET_RATE"),
                "Geoid must use FUNCTION (not FUNCTION_SPEC). Keys: " + routines.keySet());
        assertFalse(routines.keySet().stream().anyMatch(k -> k.contains("_SPEC")),
                "No geoid must contain '_SPEC'. Keys: " + routines.keySet());
    }

    @Test
    void specAndBody_hasSpecAndHasBodyBothTrue() {
        Map<String, RoutineInfo> routines = parse(SPEC_AND_BODY).getResult()
                .getStructure().getRoutines();

        RoutineInfo calc = routines.get("DWH.PKG_MERGE_TEST:PROCEDURE:CALC");
        assertNotNull(calc, "CALC routine must be present");
        assertTrue(calc.isHasSpec(), "CALC must have has_spec=true (declared in package spec)");
        assertTrue(calc.isHasBody(), "CALC must have has_body=true (implemented in package body)");

        RoutineInfo rate = routines.get("DWH.PKG_MERGE_TEST:FUNCTION:GET_RATE");
        assertNotNull(rate, "GET_RATE routine must be present");
        assertTrue(rate.isHasSpec(), "GET_RATE must have has_spec=true");
        assertTrue(rate.isHasBody(), "GET_RATE must have has_body=true");
    }

    @Test
    void specAndBody_parametersNotDuplicated() {
        Map<String, RoutineInfo> routines = parse(SPEC_AND_BODY).getResult()
                .getStructure().getRoutines();

        RoutineInfo calc = routines.get("DWH.PKG_MERGE_TEST:PROCEDURE:CALC");
        assertNotNull(calc);
        // Spec declares [p_year, p_code]; body re-declares the same → must be exactly 2 params
        assertEquals(2, calc.getTypedParameters().size(),
                "CALC must have exactly 2 parameters (no duplicates from spec+body). Got: "
                        + calc.getTypedParameters());
    }

    @Test
    void specAndBody_routineTypeIsNormalised() {
        Map<String, RoutineInfo> routines = parse(SPEC_AND_BODY).getResult()
                .getStructure().getRoutines();

        RoutineInfo calc = routines.get("DWH.PKG_MERGE_TEST:PROCEDURE:CALC");
        assertNotNull(calc);
        assertEquals("PROCEDURE", calc.getRoutineType(),
                "routine_type must be 'PROCEDURE', not 'PROCEDURE_SPEC'");

        RoutineInfo rate = routines.get("DWH.PKG_MERGE_TEST:FUNCTION:GET_RATE");
        assertNotNull(rate);
        assertEquals("FUNCTION", rate.getRoutineType(),
                "routine_type must be 'FUNCTION', not 'FUNCTION_SPEC'");
    }

    // ── Body-only → has_body=true, has_spec=false ─────────────────────────────

    @Test
    void bodyOnly_hasBodyTrueHasSpecFalse() {
        Map<String, RoutineInfo> routines = parse(BODY_ONLY).getResult()
                .getStructure().getRoutines();

        RoutineInfo save = routines.get("DWH.PKG_BODY_ONLY:PROCEDURE:SAVE");
        assertNotNull(save, "SAVE routine must be present. Keys: " + routines.keySet());
        assertFalse(save.isHasSpec(), "Body-only routine must have has_spec=false");
        assertTrue(save.isHasBody(),  "Body-only routine must have has_body=true");
    }
}
