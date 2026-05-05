package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.hound.semantic.model.StatementInfo;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Phase 4-A: KI-MERGE-2 — MERGE WHEN MATCHED DATA_FLOW tests.
 *
 * TC-HOUND-M1..M4: verifies that source alias columns in MERGE WHEN MATCHED
 * SET clause are resolved (no unresolved atoms for src.*).
 */
class MergeWhenMatchedDataFlowTest {

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

    private static List<StatementInfo> stmtsOfType(UniversalSemanticEngine e, String type) {
        return e.getBuilder().getStatements().values().stream()
                .filter(s -> type.equals(s.getType()))
                .collect(Collectors.toList());
    }

    // ── TC-HOUND-M1: MERGE WHEN MATCHED DATA_FLOW ────────────────────────────

    @Test
    @DisplayName("TC-HOUND-M1: MERGE WHEN MATCHED — src.* atoms not unresolved")
    void merge_whenMatched_srcColumnsResolved() {
        var entries = log("""
                MERGE INTO target_table t
                USING source_table src
                ON (t.id = src.id)
                WHEN MATCHED THEN
                    UPDATE SET t.value = src.value,
                               t.updated_at = src.updated_at
                """);

        long unresolvedSrc = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toLowerCase().startsWith("src.");
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedSrc,
                "src.* in MERGE WHEN MATCHED UPDATE SET must not be unresolved");
    }

    // ── TC-HOUND-M2: MERGE INTO ... USING (SELECT) subquery ──────────────────

    @Test
    @DisplayName("TC-HOUND-M2: MERGE INTO ... USING (SELECT) subquery — src.* not unresolved")
    void merge_usingSelectSubquery_srcColumnsResolved() {
        var entries = log("""
                MERGE INTO orders o
                USING (SELECT order_id, status, amount FROM staging_orders) src
                ON (o.order_id = src.order_id)
                WHEN MATCHED THEN
                    UPDATE SET o.status = src.status,
                               o.amount = src.amount
                WHEN NOT MATCHED THEN
                    INSERT (order_id, status, amount)
                    VALUES (src.order_id, src.status, src.amount)
                """);

        long unresolvedSrc = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toLowerCase().startsWith("src.");
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedSrc,
                "MERGE USING (SELECT) subquery: src.* refs must not be unresolved");
    }

    // ── TC-HOUND-M3: SCD2 MERGE pair ─────────────────────────────────────────

    @Test
    @DisplayName("TC-HOUND-M3: SCD2 MERGE pair — both statements recognised, src.effective_from resolved")
    void scd2_mergePair_bothRecognised() {
        UniversalSemanticEngine engine = parse("""
                -- SCD2 step 1: expire current row
                MERGE INTO dim_customer t
                USING (SELECT customer_id, effective_from FROM stg_customer) src
                ON (t.customer_id = src.customer_id AND t.is_current = 1)
                WHEN MATCHED THEN
                    UPDATE SET t.is_current = 0,
                               t.effective_to = src.effective_from;

                -- SCD2 step 2: insert new row
                MERGE INTO dim_customer t
                USING (SELECT customer_id, name, effective_from FROM stg_customer) src
                ON (1 = 0)
                WHEN NOT MATCHED THEN
                    INSERT (customer_id, name, effective_from, is_current)
                    VALUES (src.customer_id, src.name, src.effective_from, 1);
                """);

        List<StatementInfo> merges = stmtsOfType(engine, "MERGE");
        assertTrue(merges.size() >= 2,
                "SCD2 pair: expected ≥2 MERGE statements, got " + merges.size());

        long unresolvedSrcEffective = engine.getResolutionLog().stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toLowerCase().contains("effective_from");
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedSrcEffective,
                "src.effective_from should be resolved in both SCD2 MERGE statements");
    }

    // ── TC-HOUND-M4: MERGE with PARALLEL hint ────────────────────────────────

    @Test
    @DisplayName("TC-HOUND-M4: MERGE with /*+ PARALLEL(t,8) */ hint — hint does not break resolution")
    void merge_withParallelHint_columnResolutionUnaffected() {
        var entries = log("""
                MERGE /*+ PARALLEL(t,8) */ INTO target_table t
                USING source_table src ON (t.id = src.id)
                WHEN MATCHED THEN
                    UPDATE SET t.product_id = src.product_id
                """);

        long unresolvedSrc = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toLowerCase().startsWith("src.");
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedSrc,
                "PARALLEL hint must not break MERGE source column resolution");
    }

    // ── TC-HOUND-CH1: TRUNCATE → INSERT → MERGE delta chain ─────────────────

    @Test
    @DisplayName("TC-HOUND-CH1: TRUNCATE→INSERT staging→MERGE delta — 3 stmts, src.product_id resolved")
    void deltaLoadChain_threeStatements_productIdResolved() {
        UniversalSemanticEngine engine = parse("""
                TRUNCATE TABLE delta_stg;

                INSERT INTO delta_stg (product_id, qty, price)
                SELECT product_id, qty, price FROM raw_feed;

                MERGE INTO product_facts f
                USING delta_stg src ON (f.product_id = src.product_id)
                WHEN MATCHED THEN
                    UPDATE SET f.qty = src.qty, f.price = src.price
                WHEN NOT MATCHED THEN
                    INSERT (product_id, qty, price)
                    VALUES (src.product_id, src.qty, src.price);
                """);

        List<StatementInfo> merges = stmtsOfType(engine, "MERGE");
        assertFalse(merges.isEmpty(), "Expected at least one MERGE statement in delta chain");

        long unresolvedProductId = engine.getResolutionLog().stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toLowerCase().contains("product_id");
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedProductId,
                "src.product_id in MERGE delta chain must be resolved from delta_stg");
    }
}
