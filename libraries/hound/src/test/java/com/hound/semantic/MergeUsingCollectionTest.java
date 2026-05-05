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
 * S1.BUG-3 functional tests — MERGE USING TABLE(collection) alias registration.
 *
 * Verifies that column references through a TABLE() collection alias (e.g. v.ID, v.VALUE)
 * are resolved, not left unresolved, after the fix in PlSqlSemanticListener.enterSelected_tableview().
 */
class MergeUsingCollectionTest {

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
    // Basic MERGE USING TABLE(collection) — alias resolves
    // ═══════════════════════════════════════════════════════

    @Test
    void mergeUsingCollection_aliasColumnsNotUnresolved() {
        var entries = log("""
                MERGE INTO TARGET_TABLE t
                USING TABLE(vtSourceData) v
                ON (t.ID = v.ID)
                WHEN MATCHED THEN UPDATE SET t.VALUE = v.VALUE
                """);

        // v.ID and v.VALUE must NOT be unresolved
        long unresolvedVAlias = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toUpperCase().startsWith("V.");
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedVAlias,
                "Column references via TABLE() alias 'v' (v.ID, v.VALUE) must not be unresolved");
    }

    @Test
    void mergeUsingCollection_vIdAppearsInLog() {
        var entries = log("""
                MERGE INTO TARGET_TABLE t
                USING TABLE(vtSourceData) v
                ON (t.ID = v.ID)
                WHEN MATCHED THEN UPDATE SET t.VALUE = v.VALUE
                """);

        boolean vIdPresent = entries.stream()
                .anyMatch(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.equalsIgnoreCase("v.ID");
                });

        assertTrue(vIdPresent, "v.ID should appear in the resolution log");
    }

    @Test
    void mergeUsingCollection_vValueAppearsInLog() {
        var entries = log("""
                MERGE INTO TARGET_TABLE t
                USING TABLE(vtSourceData) v
                ON (t.ID = v.ID)
                WHEN MATCHED THEN UPDATE SET t.VALUE = v.VALUE
                """);

        boolean vValuePresent = entries.stream()
                .anyMatch(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.equalsIgnoreCase("v.VALUE");
                });

        assertTrue(vValuePresent, "v.VALUE should appear in the resolution log");
    }

    // ═══════════════════════════════════════════════════════
    // MERGE USING TABLE() with INSERT branch
    // ═══════════════════════════════════════════════════════

    @Test
    void mergeUsingCollection_withInsertBranch_noUnresolved() {
        var entries = log("""
                MERGE INTO employees e
                USING TABLE(newHires) n
                ON (e.emp_id = n.emp_id)
                WHEN MATCHED THEN
                    UPDATE SET e.salary = n.salary
                WHEN NOT MATCHED THEN
                    INSERT (emp_id, name, salary) VALUES (n.emp_id, n.name, n.salary)
                """);

        long unresolvedNAlias = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toUpperCase().startsWith("N.");
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedNAlias,
                "MERGE INSERT branch: column refs via TABLE() alias 'n' must not be unresolved");
    }

    // ═══════════════════════════════════════════════════════
    // Regression: target table alias still resolves
    // ═══════════════════════════════════════════════════════

    @Test
    void mergeTarget_aliasColumnsNotUnresolved() {
        var entries = log("""
                MERGE INTO TARGET_TABLE t
                USING TABLE(vtSourceData) v
                ON (t.ID = v.ID)
                WHEN MATCHED THEN UPDATE SET t.VALUE = v.VALUE
                """);

        // t.ID and t.VALUE must NOT be unresolved either
        long unresolvedTAlias = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toUpperCase().startsWith("T.");
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedTAlias,
                "Target table alias 't' columns (t.ID, t.VALUE) must not be unresolved");
    }
}
