package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 4-B + 4-G:
 *   TC-HOUND-CB1..CB3 — CONNECT BY hierarchical queries
 *   TC-HOUND-LT1      — LATERAL correlated subquery (active, not @Disabled)
 */
class ConnectByHierarchicalTest {

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

    // ── TC-HOUND-CB1: CONNECT BY PRIOR recursive hierarchy ───────────────────

    @Test
    @DisplayName("TC-HOUND-CB1: CONNECT BY PRIOR — employee_id/manager_id resolved, LEVEL not unresolved")
    void connectBy_recursiveHierarchy_columnsResolved() {
        var entries = log("""
                SELECT employee_id, manager_id, LEVEL AS depth, SYS_CONNECT_BY_PATH(employee_id, '/') AS path
                FROM employees
                START WITH manager_id IS NULL
                CONNECT BY PRIOR employee_id = manager_id
                """);

        // employee_id and manager_id should not be unresolved
        long unresolvedKey = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && (raw.equalsIgnoreCase("employee_id")
                            || raw.equalsIgnoreCase("manager_id"));
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedKey,
                "employee_id and manager_id must be resolved in CONNECT BY query");

        // LEVEL pseudo-column must not appear as unresolved
        long unresolvedLevel = entries.stream()
                .filter(e -> "LEVEL".equalsIgnoreCase((String) e.get("raw_input")))
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedLevel, "LEVEL pseudo-column must not be unresolved");
    }

    // ── TC-HOUND-CB2: CONNECT BY tokenizer ───────────────────────────────────

    @Test
    @DisplayName("TC-HOUND-CB2: CONNECT BY tokenizer (REGEXP_SUBSTR string split) — no unresolved")
    void connectBy_regexpTokenizer_noUnresolved() {
        var entries = log("""
                SELECT REGEXP_SUBSTR(seg_tags, '[^,]+', 1, LEVEL) AS tag,
                       row_id
                FROM stg_promo_seed
                CONNECT BY REGEXP_SUBSTR(seg_tags, '[^,]+', 1, LEVEL) IS NOT NULL
                """);

        // seg_tags and row_id must not be unresolved
        long unresolvedCols = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && (raw.equalsIgnoreCase("seg_tags")
                            || raw.equalsIgnoreCase("row_id"));
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedCols,
                "seg_tags and row_id from stg_promo_seed must not be unresolved in CONNECT BY tokenizer");
    }

    // ── TC-HOUND-CB3: CONNECT BY date series from DUAL ───────────────────────

    @Test
    @DisplayName("TC-HOUND-CB3: CONNECT BY date series from DUAL — no bogus unresolved atoms")
    void connectBy_dateSeries_dual_noUnresolved() {
        var entries = log("""
                SELECT TRUNC(SYSDATE) - (LEVEL - 1) AS day_date
                FROM DUAL
                CONNECT BY LEVEL <= 30
                """);

        // No user-table column refs here — only pseudo-columns. None should be unresolved.
        long unexpectedUnresolved = entries.stream()
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    // LEVEL is pseudo-column; SYSDATE is a function — exclude known safe names
                    return raw != null && !raw.equalsIgnoreCase("LEVEL")
                            && !raw.equalsIgnoreCase("SYSDATE")
                            && !raw.equalsIgnoreCase("DUAL");
                })
                .count();

        assertEquals(0, unexpectedUnresolved,
                "CONNECT BY date series from DUAL must not produce unexpected unresolved atoms");
    }

    // ── TC-HOUND-LT1: LATERAL correlated subquery (active, not @Disabled) ────

    @Test
    @DisplayName("TC-HOUND-LT1: LATERAL correlated subquery — outer table alias resolved inside subquery")
    void lateralJoin_outerColumnsVisibleInsideSubquery() {
        var entries = log("""
                SELECT o.order_id, o.customer_id, top_item.product_id, top_item.revenue
                FROM orders o,
                     LATERAL (
                         SELECT product_id, SUM(amount) AS revenue
                         FROM order_items oi
                         WHERE oi.order_id = o.order_id
                         GROUP BY product_id
                         ORDER BY revenue DESC
                         FETCH FIRST 1 ROW ONLY
                     ) top_item
                """);

        // o.order_id inside LATERAL WHERE must be resolved from outer FROM orders o
        long unresolvedOuterRef = entries.stream()
                .filter(e -> "o.order_id".equalsIgnoreCase((String) e.get("raw_input")))
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedOuterRef,
                "o.order_id in LATERAL WHERE must be resolved from outer FROM orders o");

        // Inner columns from order_items must also be resolved
        long unresolvedInner = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && Set.of("PRODUCT_ID", "AMOUNT")
                            .contains(raw.toUpperCase());
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedInner,
                "Inner columns from order_items must be resolved inside LATERAL subquery");
    }
}
