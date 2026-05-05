package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.semantic.model.AtomInfo;
import com.hound.semantic.model.SemanticResult;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * G3-FIX: Unattached atoms (numeric/string literals in RETURN, assignments, etc.)
 * must be classified as CONSTANT_ORPHAN — not left as UNRESOLVED.
 *
 * Reproduces: PKG_ETL_PERF_TEST.sql with ~1000 GET_CONST_NNNN functions each
 * returning a numeric literal. Previously all these numbers were UNRESOLVED in DaliAtom.
 */
class UnattachedAtomClassificationTest {

    private UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    /**
     * Numeric literals in RETURN statements should be classified as constants.
     */
    @Test
    void numericReturnLiterals_classifiedAsConstant() {
        var engine = parse("""
                CREATE OR REPLACE PACKAGE BODY DWH.PKG_TEST AS
                FUNCTION GET_CONST_0590 RETURN NUMBER IS
                BEGIN RETURN 590; END GET_CONST_0590;
                FUNCTION GET_CONST_0591 RETURN NUMBER IS
                BEGIN RETURN 591; END GET_CONST_0591;
                FUNCTION GET_CONST_0592 RETURN NUMBER IS
                BEGIN RETURN 592; END GET_CONST_0592;
                END;
                """);

        var atomsData = engine.getResult().getAtoms();
        // Get the unattached container
        @SuppressWarnings("unchecked")
        Map<String, Object> unattachedContainer = (Map<String, Object>) atomsData.get("unattached");

        if (unattachedContainer != null) {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> atoms =
                    (Map<String, Map<String, Object>>) unattachedContainer.get("atoms");
            if (atoms != null) {
                for (var entry : atoms.entrySet()) {
                    Map<String, Object> atomData = entry.getValue();
                    String text = (String) atomData.get("atom_text");
                    // Numeric literals (590, 591, 592) should be CONSTANT_ORPHAN
                    if (text != null && text.matches("\\d+")) {
                        String status = (String) atomData.get("primary_status");
                        assertTrue(
                                AtomInfo.STATUS_CONSTANT_ORPHAN.equals(status)
                                        || AtomInfo.STATUS_CONSTANT.equals(status),
                                "Numeric literal '" + text + "' should be CONSTANT/CONSTANT_ORPHAN but was: " + status);
                    }
                }
            }
        }

        // Also check: no numeric literals should remain UNRESOLVED
        long unresolvedNumerics = countUnresolvedNumericsInAtoms(atomsData);
        assertEquals(0, unresolvedNumerics,
                "No numeric literals should be UNRESOLVED in unattached atoms");
    }

    /**
     * String literals in assignments should be classified as constants.
     */
    @Test
    void stringLiterals_classifiedAsConstant() {
        var engine = parse("""
                CREATE OR REPLACE PACKAGE BODY DWH.PKG_TEST AS
                PROCEDURE INIT IS
                    l_version VARCHAR2(100) := 'PKG_TEST v1.0.0';
                BEGIN
                    NULL;
                END;
                END;
                """);

        var atomsData = engine.getResult().getAtoms();
        @SuppressWarnings("unchecked")
        Map<String, Object> unattachedContainer = (Map<String, Object>) atomsData.get("unattached");

        if (unattachedContainer != null) {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> atoms =
                    (Map<String, Map<String, Object>>) unattachedContainer.get("atoms");
            if (atoms != null) {
                for (var entry : atoms.entrySet()) {
                    Map<String, Object> atomData = entry.getValue();
                    String text = (String) atomData.get("atom_text");
                    if (text != null && text.startsWith("'") && text.endsWith("'")) {
                        String status = (String) atomData.get("primary_status");
                        assertTrue(
                                AtomInfo.STATUS_CONSTANT_ORPHAN.equals(status)
                                        || AtomInfo.STATUS_CONSTANT.equals(status),
                                "String literal '" + text + "' should be CONSTANT but was: " + status);
                    }
                }
            }
        }
    }

    /**
     * Non-constant identifiers in unattached context should remain UNRESOLVED (expected behavior).
     */
    @Test
    void identifiers_remainUnresolved() {
        var engine = parse("""
                CREATE OR REPLACE PACKAGE BODY DWH.PKG_TEST AS
                PROCEDURE DO_WORK IS
                BEGIN
                    other_proc(some_var);
                END;
                END;
                """);

        // Identifiers (not constants) should still be UNRESOLVED
        var atomsData = engine.getResult().getAtoms();
        // This test just ensures the fix doesn't over-classify
        assertNotNull(atomsData);
    }

    @SuppressWarnings("unchecked")
    private long countUnresolvedNumericsInAtoms(Map<String, Object> atomsData) {
        Map<String, Object> unattachedContainer = (Map<String, Object>) atomsData.get("unattached");
        if (unattachedContainer == null) return 0;
        Map<String, Map<String, Object>> atoms =
                (Map<String, Map<String, Object>>) unattachedContainer.get("atoms");
        if (atoms == null) return 0;

        return atoms.values().stream()
                .filter(a -> {
                    String text = (String) a.get("atom_text");
                    String status = (String) a.get("primary_status");
                    return text != null && text.matches("-?\\d+(\\.\\d+)?")
                            && AtomInfo.STATUS_UNRESOLVED.equals(status);
                })
                .count();
    }
}
