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
 * S2.BUG-1 regression tests — MERGE USING (SELECT...) SOURCE alias resolution.
 *
 * Verifies that column references through the USING-subquery alias (e.g. SOURCE.ID)
 * in MERGE WHEN MATCHED THEN UPDATE SET are resolved after the fix in
 * PlSqlSemanticListener.enterSelect_statement() that allows "MERGE" as a valid
 * parent context for inline-alias registration.
 */
class MergeSetSourceAliasResolvedTest {

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
    // MERGE USING (SELECT...) SOURCE — UPDATE SET SOURCE.*
    // ═══════════════════════════════════════════════════════

    @Test
    void mergeUsingSubquery_sourceAliasColumnsNotUnresolved() {
        var entries = log("""
                MERGE INTO TARGET_TABLE t
                USING (SELECT ID, VALUE FROM SOURCE_TABLE) SOURCE
                ON (t.ID = SOURCE.ID)
                WHEN MATCHED THEN UPDATE SET t.VALUE = SOURCE.VALUE
                """);

        long unresolvedSource = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toUpperCase().startsWith("SOURCE.");
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedSource,
                "SOURCE.* column refs in MERGE WHEN MATCHED UPDATE SET must not be unresolved");
    }

    @Test
    void mergeUsingSubquery_sourceIdAppearsResolved() {
        var entries = log("""
                MERGE INTO TARGET_TABLE t
                USING (SELECT ID, VALUE FROM SOURCE_TABLE) SOURCE
                ON (t.ID = SOURCE.ID)
                WHEN MATCHED THEN UPDATE SET t.VALUE = SOURCE.VALUE
                """);

        boolean sourceIdResolved = entries.stream()
                .anyMatch(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.equalsIgnoreCase("SOURCE.ID")
                            && "RESOLVED".equals(e.get("result_kind"));
                });

        assertTrue(sourceIdResolved, "SOURCE.ID should appear as resolved in the resolution log");
    }

    @Test
    void mergeUsingSubquery_sourceValueAppearsResolved() {
        var entries = log("""
                MERGE INTO TARGET_TABLE t
                USING (SELECT ID, VALUE FROM SOURCE_TABLE) SOURCE
                ON (t.ID = SOURCE.ID)
                WHEN MATCHED THEN UPDATE SET t.VALUE = SOURCE.VALUE
                """);

        boolean sourceValueResolved = entries.stream()
                .anyMatch(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.equalsIgnoreCase("SOURCE.VALUE")
                            && "RESOLVED".equals(e.get("result_kind"));
                });

        assertTrue(sourceValueResolved, "SOURCE.VALUE should appear as resolved in the resolution log");
    }

    // ═══════════════════════════════════════════════════════
    // Regression: target alias still resolves
    // ═══════════════════════════════════════════════════════

    @Test
    void mergeUsingSubquery_targetAliasColumnsNotUnresolved() {
        var entries = log("""
                MERGE INTO TARGET_TABLE t
                USING (SELECT ID, VALUE FROM SOURCE_TABLE) SOURCE
                ON (t.ID = SOURCE.ID)
                WHEN MATCHED THEN UPDATE SET t.VALUE = SOURCE.VALUE
                """);

        long unresolvedTarget = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toUpperCase().startsWith("T.");
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedTarget,
                "Target alias 't' columns (t.ID, t.VALUE) must not be unresolved");
    }

    // ═══════════════════════════════════════════════════════
    // MERGE with INSERT branch
    // ═══════════════════════════════════════════════════════

    @Test
    void mergeUsingSubquery_withInsertBranch_sourceColumnsNotUnresolved() {
        var entries = log("""
                MERGE INTO employees e
                USING (SELECT emp_id, name, salary FROM new_hires) src
                ON (e.emp_id = src.emp_id)
                WHEN MATCHED THEN
                    UPDATE SET e.salary = src.salary
                WHEN NOT MATCHED THEN
                    INSERT (emp_id, name, salary) VALUES (src.emp_id, src.name, src.salary)
                """);

        long unresolvedSrc = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toUpperCase().startsWith("SRC.");
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedSrc,
                "MERGE with INSERT branch: 'src.*' refs must not be unresolved");
    }
}
