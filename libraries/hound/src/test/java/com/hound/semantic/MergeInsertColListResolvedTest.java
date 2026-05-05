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
 * G3-FIX: MERGE INSERT column list atoms should resolve to the MERGE target table.
 *
 * Before the fix, columns in WHEN NOT MATCHED THEN INSERT (col1, col2, ...)
 * were registered as unresolved atoms because the implicit resolver couldn't
 * determine the target table in MERGE context.
 */
class MergeInsertColListResolvedTest {

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

    /**
     * MERGE INSERT column list — columns should be resolved (not unresolved).
     * Reproduces the PKG_ETL_07_ANALYTICS.sql:CALC_REVENUE_ATTRIBUTION bug.
     */
    @Test
    void mergeInsertColumnList_resolvedToTargetTable() {
        var entries = log("""
                MERGE INTO CRM.CA_ATTRIBUTION_RESULTS tgt
                USING (
                    SELECT CUSTOMER_ID, CAMPAIGN_ID, CHANNEL,
                           ATTRIBUTED_REVENUE AS REVENUE_ATTRIBUTED,
                           LINEAR_WEIGHT AS WEIGHT
                    FROM DWH.STG_REVENUE_ATTRIBUTION
                    WHERE ANALYSIS_DATE = SYSDATE
                ) src ON (tgt.CAMPAIGN_ID = src.CAMPAIGN_ID AND tgt.CHANNEL = src.CHANNEL)
                WHEN MATCHED THEN
                    UPDATE SET tgt.REVENUE_ATTRIBUTED = src.REVENUE_ATTRIBUTED,
                               tgt.WEIGHT = src.WEIGHT
                WHEN NOT MATCHED THEN
                    INSERT (RESULT_ID, MODEL_ID, CAMPAIGN_ID, CHANNEL, WEIGHT, REVENUE_ATTRIBUTED)
                    VALUES (SEQ_ATTR.NEXTVAL, 1, src.CAMPAIGN_ID, src.CHANNEL, src.WEIGHT, src.REVENUE_ATTRIBUTED)
                """);

        // The 6 INSERT column list atoms should NOT be unresolved
        List<String> insertCols = List.of("RESULT_ID", "MODEL_ID", "CAMPAIGN_ID",
                                           "CHANNEL", "WEIGHT", "REVENUE_ATTRIBUTED");

        long unresolvedInsertCols = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && insertCols.contains(raw.toUpperCase());
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedInsertCols,
                "MERGE INSERT column list should be resolved to target table, found " +
                unresolvedInsertCols + " unresolved");
    }

    /**
     * Simpler case: basic MERGE with INSERT column list.
     */
    @Test
    void simpleMergeInsert_columnListResolved() {
        var entries = log("""
                MERGE INTO ORDERS tgt
                USING STAGING_ORDERS src ON (tgt.ORDER_ID = src.ORDER_ID)
                WHEN NOT MATCHED THEN
                    INSERT (ORDER_ID, CUSTOMER_ID, AMOUNT)
                    VALUES (src.ORDER_ID, src.CUSTOMER_ID, src.AMOUNT)
                """);

        long unresolvedInsertCols = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && List.of("ORDER_ID", "CUSTOMER_ID", "AMOUNT")
                            .contains(raw.toUpperCase());
                })
                .filter(e -> "UNRESOLVED".equals(e.get("result_kind")))
                .count();

        assertEquals(0, unresolvedInsertCols,
                "Simple MERGE INSERT columns should be resolved");
    }

    /**
     * VALUES clause atoms should still be resolved normally (not suppressed by the fix).
     */
    @Test
    void mergeInsert_valuesClauseAtomsStillResolved() {
        var entries = log("""
                MERGE INTO TARGET_T tgt
                USING SOURCE_T src ON (tgt.ID = src.ID)
                WHEN NOT MATCHED THEN
                    INSERT (ID, NAME, VALUE)
                    VALUES (src.ID, src.NAME, src.VALUE)
                """);

        // src.ID, src.NAME, src.VALUE in VALUES should be resolved (not suppressed)
        long resolvedSourceVals = entries.stream()
                .filter(e -> {
                    String raw = (String) e.get("raw_input");
                    return raw != null && raw.toUpperCase().startsWith("SRC.");
                })
                .filter(e -> "RESOLVED".equals(e.get("result_kind")))
                .count();

        assertTrue(resolvedSourceVals >= 3,
                "VALUES clause atoms (src.*) should be resolved, got: " + resolvedSourceVals);
    }
}
