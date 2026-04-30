package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 4-F: S3 planned feature stubs — active after KI implementation.
 *
 * TC-HOUND-HI1: PARALLEL APPEND hint does not break lineage (pending KI-HINT-1)
 * TC-HOUND-WF1: WITH FUNCTION inline — table columns resolved (pending KI-WITHFUNC-1)
 * TC-HOUND-JS1: JSON_TABLE COLUMNS PATH — jt.* resolved (pending KI-JSON-1)
 *
 * All tests are @Disabled until the corresponding KI is implemented.
 * Remove @Disabled when KI is merged and assertions confirmed passing.
 */
class HoundS3PlannedFeaturesTest {

    private static UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        PlSqlLexer  lexer  = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    private static List<Map<String, Object>> log(String sql) {
        return parse(sql).getResolutionLog();
    }

    // ── TC-HOUND-HI1: /*+ PARALLEL APPEND */ hint ────────────────────────────

    @Test
    @Disabled("Pending KI-HINT-1: hint-aware lineage resolution")
    @DisplayName("TC-HOUND-HI1: /*+ PARALLEL APPEND */ hint does not break lineage")
    void parallelAppendHint_doesNotBreakLineage() {
        var entries = log("""
                INSERT /*+ PARALLEL(t,4) APPEND */ INTO summary_table t
                SELECT dept_id, SUM(salary) AS total_salary
                FROM employees
                GROUP BY dept_id
                """);

        long unresolvedDeptId = entries.stream()
                .filter(e -> "dept_id".equalsIgnoreCase((String) e.get("raw_input")))
                .filter(e -> "unresolved".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedDeptId,
                "dept_id must be resolved even with PARALLEL APPEND hints (KI-HINT-1)");
    }

    // ── TC-HOUND-WF1: WITH FUNCTION inline ───────────────────────────────────

    @Test
    @Disabled("Pending KI-WITHFUNC-1: WITH FUNCTION inline resolution")
    @DisplayName("TC-HOUND-WF1: WITH FUNCTION inline — table columns resolved")
    void withFunctionInline_tableColumnsResolved() {
        var entries = log("""
                WITH FUNCTION apply_tax(p_salary NUMBER) RETURN NUMBER IS
                BEGIN RETURN p_salary * 0.87; END;
                SELECT employee_id, apply_tax(salary) AS net_salary
                FROM employees
                WHERE department_id = 10
                """);

        long unresolvedEmployee = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && (raw.equalsIgnoreCase("employee_id")
                            || raw.equalsIgnoreCase("salary")
                            || raw.equalsIgnoreCase("department_id"));
                })
                .filter(e -> "unresolved".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedEmployee,
                "Table columns must be resolved inside WITH FUNCTION query (KI-WITHFUNC-1)");
    }

    // ── TC-HOUND-JS1: JSON_TABLE COLUMNS PATH ────────────────────────────────

    @Test
    @Disabled("Pending KI-JSON-1: JSON_TABLE column path resolution")
    @DisplayName("TC-HOUND-JS1: JSON_TABLE COLUMNS PATH — jt.* resolved")
    void jsonTable_columnPath_resolved() {
        var entries = log("""
                SELECT jt.product_id, jt.product_name, jt.price
                FROM orders o,
                     JSON_TABLE(o.order_data, '$.items[*]'
                         COLUMNS (
                             product_id   NUMBER       PATH '$.id',
                             product_name VARCHAR2(100) PATH '$.name',
                             price        NUMBER       PATH '$.price'
                         )
                     ) jt
                """);

        long unresolvedJt = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toLowerCase().startsWith("jt.");
                })
                .filter(e -> "unresolved".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedJt,
                "jt.* columns from JSON_TABLE must be resolved (KI-JSON-1)");
    }
}
