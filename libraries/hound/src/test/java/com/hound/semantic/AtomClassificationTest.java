package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * S1.BUG functional tests — atom classification correctness.
 * Covers fixes for: INTERVAL literals, bind variables (:x), DATE/TIMESTAMP literals.
 */
class AtomClassificationTest {

    private UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    private List<Map<String, Object>> log(String sql) {
        return parse(sql).getResolutionLog();
    }

    // ═══════════════════════════════════════════════════════
    // INTERVAL literals (S1.BUG-1)
    // ═══════════════════════════════════════════════════════

    @Test
    void intervalDay_classifiedAsConstant() {
        var entries = log("SELECT INTERVAL '20' DAY FROM DUAL");
        var interval = entries.stream()
                .filter(e -> ((String) e.get("raw_input")).toUpperCase().startsWith("INTERVAL"))
                .findFirst();
        assertTrue(interval.isPresent(), "INTERVAL atom should appear in resolution log");
        assertEquals("constant", interval.get().get("result_kind"),
                "INTERVAL '20' DAY should be classified as constant");
    }

    @Test
    void intervalHour_classifiedAsConstant() {
        var entries = log("SELECT INTERVAL '240' HOUR FROM DUAL");
        var interval = entries.stream()
                .filter(e -> ((String) e.get("raw_input")).toUpperCase().startsWith("INTERVAL"))
                .findFirst();
        assertTrue(interval.isPresent());
        assertEquals("constant", interval.get().get("result_kind"),
                "INTERVAL '240' HOUR should be classified as constant");
    }

    @Test
    void intervalDayToSecond_classifiedAsConstant() {
        var entries = log("SELECT INTERVAL '0 02:30:00' DAY TO SECOND FROM DUAL");
        var interval = entries.stream()
                .filter(e -> ((String) e.get("raw_input")).toUpperCase().startsWith("INTERVAL"))
                .findFirst();
        assertTrue(interval.isPresent());
        assertEquals("constant", interval.get().get("result_kind"),
                "INTERVAL DAY TO SECOND should be classified as constant");
    }

    @Test
    void intervalAtoms_neverUnresolved() {
        var entries = log("""
                SELECT INTERVAL '20' DAY, INTERVAL '240' HOUR FROM DUAL
                """);
        long unresolvedIntervals = entries.stream()
                .filter(e -> ((String) e.get("raw_input")).toUpperCase().startsWith("INTERVAL"))
                .filter(e -> "unresolved".equals(e.get("result_kind")))
                .count();
        assertEquals(0, unresolvedIntervals, "No INTERVAL atom should be unresolved");
    }

    // ═══════════════════════════════════════════════════════
    // Bind variables (S1.BUG-2)
    // ═══════════════════════════════════════════════════════

    @Test
    void bindVariableNumeric_classifiedAsConstant() {
        var entries = log("SELECT * FROM t WHERE id = :1");
        var bind = entries.stream()
                .filter(e -> ":1".equals(e.get("raw_input")))
                .findFirst();
        assertTrue(bind.isPresent(), "Bind variable :1 should appear in resolution log");
        assertEquals("constant", bind.get().get("result_kind"),
                "Bind variable :1 should be classified as constant");
    }

    @Test
    void bindVariableNamed_classifiedAsConstant() {
        var entries = log("SELECT * FROM t WHERE name = :name AND code = :CODE");
        long bindConstants = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.startsWith(":");
                })
                .filter(e -> "constant".equals(e.get("result_kind")))
                .count();
        assertEquals(2, bindConstants, "Both named bind variables should be classified as constants");
    }

    @Test
    void bindVariableAlpha_classifiedAsConstant() {
        var entries = log("SELECT * FROM t WHERE val = :X AND other = :b");
        long bindConstants = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.startsWith(":");
                })
                .filter(e -> "constant".equals(e.get("result_kind")))
                .count();
        assertEquals(2, bindConstants, "Alphabetic bind variables should be classified as constants");
    }

    @Test
    void bindVariables_neverUnresolved() {
        var entries = log("""
                INSERT INTO t (a, b, c) VALUES (:1, :2, :3)
                """);
        long unresolvedBinds = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.startsWith(":");
                })
                .filter(e -> "unresolved".equals(e.get("result_kind")))
                .count();
        assertEquals(0, unresolvedBinds, "No bind variable should be unresolved");
    }

    // ═══════════════════════════════════════════════════════
    // DATE / TIMESTAMP literals
    // ═══════════════════════════════════════════════════════

    @Test
    void dateLiteral_classifiedAsConstant() {
        var entries = log("SELECT * FROM t WHERE dt > DATE '2024-01-01'");
        var date = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toUpperCase().startsWith("DATE");
                })
                .findFirst();
        assertTrue(date.isPresent(), "DATE literal should appear in resolution log");
        assertEquals("constant", date.get().get("result_kind"),
                "DATE 'string' should be classified as constant");
    }

    @Test
    void timestampLiteral_classifiedAsConstant() {
        var entries = log("SELECT * FROM t WHERE ts > TIMESTAMP '2024-01-01 00:00:00'");
        var ts = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toUpperCase().startsWith("TIMESTAMP");
                })
                .findFirst();
        assertTrue(ts.isPresent(), "TIMESTAMP literal should appear in resolution log");
        assertEquals("constant", ts.get().get("result_kind"),
                "TIMESTAMP 'string' should be classified as constant");
    }
}
