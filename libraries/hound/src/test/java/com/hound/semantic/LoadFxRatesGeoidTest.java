package com.hound.semantic;

import com.hound.semantic.engine.UniversalSemanticEngine;
import com.hound.semantic.dialect.plsql.PlSqlSemanticListener;
import com.hound.semantic.model.StatementInfo;
import com.hound.parser.base.grammars.sql.plsql.PlSqlParser;
import com.hound.parser.base.grammars.sql.plsql.PlSqlLexer;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression test: verifies that sequential UPDATE statements inside
 * LOAD_FX_RATES get correct GEOIDs with the routine prefix.
 *
 * Bug B: UPDATE at line 1307 (approx) was getting geoid "UPDATE:1307"
 * instead of "PROCEDURE:LOAD_FX_RATES:UPDATE:1307" because the routine
 * scope context was being reset mid-procedure.
 */
class LoadFxRatesGeoidTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoadFxRatesGeoidTest.class);

    // Full standalone procedure as sent by the user (line numbers match PKG_ETL_08_TREASURY.sql
    // lines 906-1331 approximately, here renumbered starting from 1 in this string).
    private static final String SQL = """
        CREATE OR REPLACE PROCEDURE LOAD_FX_RATES (
            p_rate_date     IN DATE     DEFAULT TRUNC(SYSDATE),
            p_rate_type     IN VARCHAR2 DEFAULT 'SPOT',
            p_batch_id      IN NUMBER   DEFAULT NULL
        ) IS
            l_step          VARCHAR2(200) := 'LOAD_FX_RATES';
            l_batch_id      NUMBER(19)    := NVL(p_batch_id, TO_NUMBER(TO_CHAR(SYSDATE, 'YYYYMMDDHH24MISS')));
            l_cnt           PLS_INTEGER   := 0;
            l_usd_ccy_id    NUMBER(19);

            TYPE t_rate_id_tab      IS TABLE OF DWH.STG_FX_RATES.RATE_ID%TYPE;
            TYPE t_from_ccy_id_tab  IS TABLE OF DWH.STG_FX_RATES.FROM_CURRENCY_ID%TYPE;
            TYPE t_to_ccy_id_tab    IS TABLE OF DWH.STG_FX_RATES.TO_CURRENCY_ID%TYPE;
            TYPE t_from_ccy_cd_tab  IS TABLE OF DWH.STG_FX_RATES.FROM_CURRENCY_CODE%TYPE;
            TYPE t_to_ccy_cd_tab    IS TABLE OF DWH.STG_FX_RATES.TO_CURRENCY_CODE%TYPE;
            TYPE t_rate_dt_tab      IS TABLE OF DWH.STG_FX_RATES.RATE_DATE%TYPE;
            TYPE t_exch_rate_tab    IS TABLE OF DWH.STG_FX_RATES.EXCHANGE_RATE%TYPE;
            TYPE t_inv_rate_tab     IS TABLE OF DWH.STG_FX_RATES.INVERSE_RATE%TYPE;
            TYPE t_rate_type_tab    IS TABLE OF DWH.STG_FX_RATES.RATE_TYPE%TYPE;
            TYPE t_src_name_tab     IS TABLE OF DWH.STG_FX_RATES.SOURCE_NAME%TYPE;
            TYPE t_is_prim_tab      IS TABLE OF DWH.STG_FX_RATES.IS_PRIMARY%TYPE;
            TYPE t_cross_rate_tab   IS TABLE OF DWH.STG_FX_RATES.CROSS_RATE%TYPE;
            TYPE t_rate_chg_tab     IS TABLE OF DWH.STG_FX_RATES.RATE_CHANGE_PCT%TYPE;
            TYPE t_vol_tab          IS TABLE OF DWH.STG_FX_RATES.VOLATILITY_30D%TYPE;

            v_rate_id       t_rate_id_tab;
            v_from_ccy_id   t_from_ccy_id_tab;
            v_to_ccy_id     t_to_ccy_id_tab;
            v_from_ccy_cd   t_from_ccy_cd_tab;
            v_to_ccy_cd     t_to_ccy_cd_tab;
            v_rate_dt       t_rate_dt_tab;
            v_exch_rate     t_exch_rate_tab;
            v_inv_rate      t_inv_rate_tab;
            v_rate_type     t_rate_type_tab;
            v_src_name      t_src_name_tab;
            v_is_prim       t_is_prim_tab;
            v_cross_rate    t_cross_rate_tab;
            v_rate_chg      t_rate_chg_tab;
            v_vol           t_vol_tab;

            CURSOR c_fx_rates IS
                WITH latest_rate_per_pair AS (
                    SELECT
                        fr.RATE_ID,
                        fr.FROM_CURRENCY_ID,
                        fr.TO_CURRENCY_ID,
                        fr.RATE_DATE,
                        fr.EXCHANGE_RATE,
                        fr.RATE_TYPE,
                        ROW_NUMBER() OVER (
                            PARTITION BY fr.FROM_CURRENCY_ID, fr.TO_CURRENCY_ID
                            ORDER BY fr.RATE_DATE DESC, fr.RATE_ID DESC
                        ) AS rn
                    FROM FIN.TRS_FX_RATES fr
                    WHERE fr.RATE_DATE <= p_rate_date
                      AND fr.RATE_TYPE  = p_rate_type
                      AND EXISTS (
                            SELECT 1
                              FROM FIN.CURRENCIES c1
                             WHERE c1.CURRENCY_ID = fr.FROM_CURRENCY_ID
                               AND c1.IS_ACTIVE   = 'Y'
                          )
                      AND EXISTS (
                            SELECT 1
                              FROM FIN.CURRENCIES c2
                             WHERE c2.CURRENCY_ID = fr.TO_CURRENCY_ID
                               AND c2.IS_ACTIVE   = 'Y'
                          )
                ),
                rate_volatility AS (
                    SELECT
                        fv.FROM_CURRENCY_ID,
                        fv.TO_CURRENCY_ID,
                        STDDEV(fv.EXCHANGE_RATE) AS volatility_30d,
                        AVG(fv.EXCHANGE_RATE)    AS avg_rate_30d,
                        MIN(fv.EXCHANGE_RATE)    AS min_rate_30d,
                        MAX(fv.EXCHANGE_RATE)    AS max_rate_30d,
                        COUNT(*)                 AS obs_count
                    FROM FIN.TRS_FX_RATES fv
                    WHERE fv.RATE_DATE BETWEEN (p_rate_date - 30) AND p_rate_date
                      AND fv.RATE_TYPE = p_rate_type
                    GROUP BY fv.FROM_CURRENCY_ID, fv.TO_CURRENCY_ID
                    HAVING COUNT(*) >= 5
                ),
                prior_day_rates AS (
                    SELECT
                        pd.FROM_CURRENCY_ID,
                        pd.TO_CURRENCY_ID,
                        pd.EXCHANGE_RATE AS prior_rate
                    FROM FIN.TRS_FX_RATES pd
                    WHERE pd.RATE_DATE = (
                            SELECT MAX(pd2.RATE_DATE)
                              FROM FIN.TRS_FX_RATES pd2
                             WHERE pd2.FROM_CURRENCY_ID = pd.FROM_CURRENCY_ID
                               AND pd2.TO_CURRENCY_ID   = pd.TO_CURRENCY_ID
                               AND pd2.RATE_DATE        < p_rate_date
                               AND pd2.RATE_TYPE        = p_rate_type
                          )
                      AND pd.RATE_TYPE = p_rate_type
                ),
                usd_rates AS (
                    SELECT
                        ur.FROM_CURRENCY_ID,
                        ur.EXCHANGE_RATE AS rate_to_usd
                    FROM FIN.TRS_FX_RATES ur
                    WHERE ur.TO_CURRENCY_ID = (
                            SELECT cu.CURRENCY_ID
                              FROM FIN.CURRENCIES cu
                             WHERE cu.CURRENCY_CODE = 'USD'
                               AND cu.IS_ACTIVE     = 'Y'
                          )
                      AND ur.RATE_DATE = p_rate_date
                      AND ur.RATE_TYPE = p_rate_type
                )
                SELECT
                    lr.RATE_ID,
                    lr.FROM_CURRENCY_ID,
                    lr.TO_CURRENCY_ID,
                    (SELECT cf.CURRENCY_CODE
                       FROM FIN.CURRENCIES cf
                      WHERE cf.CURRENCY_ID = lr.FROM_CURRENCY_ID
                    ) AS FROM_CURRENCY_CODE,
                    (SELECT ct.CURRENCY_CODE
                       FROM FIN.CURRENCIES ct
                      WHERE ct.CURRENCY_ID = lr.TO_CURRENCY_ID
                    ) AS TO_CURRENCY_CODE,
                    lr.RATE_DATE,
                    lr.EXCHANGE_RATE,
                    CASE
                        WHEN lr.EXCHANGE_RATE <> 0 THEN
                            ROUND(1 / lr.EXCHANGE_RATE, 10)
                        ELSE NULL
                    END AS INVERSE_RATE,
                    lr.RATE_TYPE,
                    (SELECT rs.SOURCE_NAME
                       FROM FIN.TRS_FX_RATE_SOURCES rs
                      WHERE rs.IS_PRIMARY = 'Y'
                        AND ROWNUM = 1
                    ) AS SOURCE_NAME,
                    (SELECT rs2.IS_PRIMARY
                       FROM FIN.TRS_FX_RATE_SOURCES rs2
                      WHERE rs2.IS_PRIMARY = 'Y'
                        AND ROWNUM = 1
                    ) AS IS_PRIMARY,
                    CASE
                        WHEN u_from.rate_to_usd IS NOT NULL
                         AND u_to.rate_to_usd   IS NOT NULL
                         AND u_to.rate_to_usd  <> 0 THEN
                            ROUND(u_from.rate_to_usd / u_to.rate_to_usd, 10)
                        ELSE NULL
                    END AS CROSS_RATE,
                    CASE
                        WHEN pdr.prior_rate IS NOT NULL AND pdr.prior_rate <> 0 THEN
                            ROUND(
                                (lr.EXCHANGE_RATE - pdr.prior_rate) / pdr.prior_rate * 100,
                                4
                            )
                        ELSE 0
                    END AS RATE_CHANGE_PCT,
                    NVL(rv.volatility_30d, 0) AS VOLATILITY_30D
                FROM latest_rate_per_pair lr
                LEFT JOIN rate_volatility rv
                       ON rv.FROM_CURRENCY_ID = lr.FROM_CURRENCY_ID
                      AND rv.TO_CURRENCY_ID   = lr.TO_CURRENCY_ID
                LEFT JOIN prior_day_rates pdr
                       ON pdr.FROM_CURRENCY_ID = lr.FROM_CURRENCY_ID
                      AND pdr.TO_CURRENCY_ID   = lr.TO_CURRENCY_ID
                LEFT JOIN usd_rates u_from
                       ON u_from.FROM_CURRENCY_ID = lr.FROM_CURRENCY_ID
                LEFT JOIN usd_rates u_to
                       ON u_to.FROM_CURRENCY_ID = lr.TO_CURRENCY_ID
                WHERE lr.rn = 1
                ORDER BY lr.FROM_CURRENCY_ID, lr.TO_CURRENCY_ID;

        BEGIN
            DELETE FROM DWH.STG_FX_RATES
             WHERE RATE_DATE = p_rate_date
               AND RATE_TYPE = p_rate_type;

            OPEN c_fx_rates;
            LOOP
                FETCH c_fx_rates
                BULK COLLECT INTO
                    v_rate_id, v_from_ccy_id, v_to_ccy_id,
                    v_from_ccy_cd, v_to_ccy_cd,
                    v_rate_dt, v_exch_rate, v_inv_rate,
                    v_rate_type, v_src_name, v_is_prim,
                    v_cross_rate, v_rate_chg, v_vol
                LIMIT 1000;

                EXIT WHEN v_rate_id.COUNT = 0;

                FORALL i IN 1 .. v_rate_id.COUNT
                    INSERT INTO DWH.STG_FX_RATES (
                        RATE_ID, FROM_CURRENCY_ID, TO_CURRENCY_ID,
                        FROM_CURRENCY_CODE, TO_CURRENCY_CODE,
                        RATE_DATE, EXCHANGE_RATE, INVERSE_RATE,
                        RATE_TYPE, SOURCE_NAME, IS_PRIMARY,
                        CROSS_RATE, RATE_CHANGE_PCT, VOLATILITY_30D,
                        DW_BATCH_ID
                    ) VALUES (
                        v_rate_id(i), v_from_ccy_id(i), v_to_ccy_id(i),
                        v_from_ccy_cd(i), v_to_ccy_cd(i),
                        v_rate_dt(i), v_exch_rate(i), v_inv_rate(i),
                        v_rate_type(i), v_src_name(i), v_is_prim(i),
                        v_cross_rate(i), v_rate_chg(i), v_vol(i),
                        1
                    );

                COMMIT;
            END LOOP;
            CLOSE c_fx_rates;

            MERGE INTO DWH.DIM_FX_RATES tgt
            USING (
                SELECT
                    s.RATE_ID,
                    s.FROM_CURRENCY_ID,
                    s.TO_CURRENCY_ID,
                    s.FROM_CURRENCY_CODE,
                    s.TO_CURRENCY_CODE,
                    s.RATE_DATE,
                    s.EXCHANGE_RATE,
                    s.INVERSE_RATE,
                    s.RATE_TYPE,
                    s.SOURCE_NAME,
                    s.IS_PRIMARY,
                    s.CROSS_RATE,
                    s.RATE_CHANGE_PCT,
                    s.VOLATILITY_30D,
                    (SELECT cf.CURRENCY_NAME
                       FROM FIN.CURRENCIES cf
                      WHERE cf.CURRENCY_ID = s.FROM_CURRENCY_ID
                    ) AS FROM_CURRENCY_NAME,
                    (SELECT ct.CURRENCY_NAME
                       FROM FIN.CURRENCIES ct
                      WHERE ct.CURRENCY_ID = s.TO_CURRENCY_ID
                    ) AS TO_CURRENCY_NAME,
                    (SELECT cf2.IS_FUNCTIONAL
                       FROM FIN.CURRENCIES cf2
                      WHERE cf2.CURRENCY_ID = s.FROM_CURRENCY_ID
                    ) AS FROM_IS_FUNCTIONAL,
                    (SELECT ct2.IS_REPORTING
                       FROM FIN.CURRENCIES ct2
                      WHERE ct2.CURRENCY_ID = s.TO_CURRENCY_ID
                    ) AS TO_IS_REPORTING
                FROM DWH.STG_FX_RATES s
                WHERE s.DW_BATCH_ID = 1
            ) src
            ON (    tgt.FROM_CURRENCY_ID = src.FROM_CURRENCY_ID
                AND tgt.TO_CURRENCY_ID   = src.TO_CURRENCY_ID
                AND tgt.RATE_DATE        = src.RATE_DATE
                AND tgt.RATE_TYPE        = src.RATE_TYPE)
            WHEN MATCHED THEN
                UPDATE SET
                    tgt.EXCHANGE_RATE    = src.EXCHANGE_RATE,
                    tgt.INVERSE_RATE     = src.INVERSE_RATE,
                    tgt.SOURCE_NAME      = src.SOURCE_NAME,
                    tgt.CROSS_RATE       = src.CROSS_RATE,
                    tgt.RATE_CHANGE_PCT  = src.RATE_CHANGE_PCT,
                    tgt.VOLATILITY_30D   = src.VOLATILITY_30D,
                    tgt.DW_UPDATE_TS     = SYSTIMESTAMP
            WHEN NOT MATCHED THEN
                INSERT (
                    RATE_ID, FROM_CURRENCY_ID, TO_CURRENCY_ID,
                    FROM_CURRENCY_CODE, TO_CURRENCY_CODE,
                    RATE_DATE, EXCHANGE_RATE, INVERSE_RATE,
                    RATE_TYPE, SOURCE_NAME, IS_PRIMARY,
                    CROSS_RATE, RATE_CHANGE_PCT, VOLATILITY_30D,
                    DW_INSERT_TS
                ) VALUES (
                    src.RATE_ID, src.FROM_CURRENCY_ID, src.TO_CURRENCY_ID,
                    src.FROM_CURRENCY_CODE, src.TO_CURRENCY_CODE,
                    src.RATE_DATE, src.EXCHANGE_RATE, src.INVERSE_RATE,
                    src.RATE_TYPE, src.SOURCE_NAME, src.IS_PRIMARY,
                    src.CROSS_RATE, src.RATE_CHANGE_PCT, src.VOLATILITY_30D,
                    SYSTIMESTAMP
                );

            DECLARE
                l_src_count   PLS_INTEGER;
                l_tgt_count   PLS_INTEGER;
                l_match_count PLS_INTEGER;
            BEGIN
                SELECT COUNT(*)
                  INTO l_src_count
                  FROM DWH.STG_FX_RATES s
                 WHERE s.DW_BATCH_ID = 1
                   AND s.RATE_DATE   = p_rate_date;

                SELECT COUNT(*)
                  INTO l_tgt_count
                  FROM DWH.DIM_FX_RATES t
                 WHERE t.RATE_DATE = p_rate_date
                   AND t.RATE_TYPE = p_rate_type;

                SELECT COUNT(*)
                  INTO l_match_count
                  FROM DWH.STG_FX_RATES s2
                 WHERE s2.DW_BATCH_ID = 1
                   AND EXISTS (
                         SELECT 1
                           FROM DWH.DIM_FX_RATES d
                          WHERE d.FROM_CURRENCY_ID = s2.FROM_CURRENCY_ID
                            AND d.TO_CURRENCY_ID   = s2.TO_CURRENCY_ID
                            AND d.RATE_DATE        = s2.RATE_DATE
                            AND d.RATE_TYPE        = s2.RATE_TYPE
                            AND d.EXCHANGE_RATE    = s2.EXCHANGE_RATE
                       );
            END;

            INSERT INTO DWH.STG_FX_RATES (
                RATE_ID, FROM_CURRENCY_ID, TO_CURRENCY_ID,
                FROM_CURRENCY_CODE, TO_CURRENCY_CODE,
                RATE_DATE, EXCHANGE_RATE, INVERSE_RATE,
                RATE_TYPE, SOURCE_NAME, IS_PRIMARY,
                CROSS_RATE, RATE_CHANGE_PCT, VOLATILITY_30D,
                DW_BATCH_ID
            )
            SELECT
                1,
                s.TO_CURRENCY_ID,
                s.FROM_CURRENCY_ID,
                s.TO_CURRENCY_CODE,
                s.FROM_CURRENCY_CODE,
                s.RATE_DATE,
                s.INVERSE_RATE,
                s.EXCHANGE_RATE,
                s.RATE_TYPE,
                s.SOURCE_NAME || '_INV',
                'N',
                CASE
                    WHEN s.CROSS_RATE IS NOT NULL AND s.CROSS_RATE <> 0
                    THEN ROUND(1 / s.CROSS_RATE, 10)
                    ELSE NULL
                END,
                CASE
                    WHEN s.RATE_CHANGE_PCT IS NOT NULL
                    THEN -s.RATE_CHANGE_PCT
                    ELSE 0
                END,
                s.VOLATILITY_30D,
                1
            FROM DWH.STG_FX_RATES s
            WHERE s.DW_BATCH_ID = 1
              AND s.RATE_DATE   = p_rate_date
              AND s.INVERSE_RATE IS NOT NULL
              AND NOT EXISTS (
                    SELECT 1
                      FROM DWH.STG_FX_RATES ex
                     WHERE ex.FROM_CURRENCY_ID = s.TO_CURRENCY_ID
                       AND ex.TO_CURRENCY_ID   = s.FROM_CURRENCY_ID
                       AND ex.RATE_DATE        = s.RATE_DATE
                       AND ex.DW_BATCH_ID      = 1
                  );

            UPDATE DWH.STG_FX_RATES s
               SET s.RATE_CHANGE_PCT = (
                     SELECT ROUND(
                         (s.EXCHANGE_RATE - pr.EXCHANGE_RATE) / NULLIF(pr.EXCHANGE_RATE, 0) * 100, 4
                     )
                       FROM FIN.TRS_FX_RATES pr
                      WHERE pr.FROM_CURRENCY_ID = s.FROM_CURRENCY_ID
                        AND pr.TO_CURRENCY_ID   = s.TO_CURRENCY_ID
                        AND pr.RATE_DATE = (
                              SELECT MAX(pr2.RATE_DATE)
                                FROM FIN.TRS_FX_RATES pr2
                               WHERE pr2.FROM_CURRENCY_ID = s.FROM_CURRENCY_ID
                                 AND pr2.TO_CURRENCY_ID   = s.TO_CURRENCY_ID
                                 AND pr2.RATE_DATE        < p_rate_date
                                 AND pr2.RATE_TYPE        = p_rate_type
                            )
                        AND pr.RATE_TYPE = p_rate_type
                        AND ROWNUM = 1
                   ),
                   s.DW_UPDATE_TS = SYSTIMESTAMP
             WHERE s.DW_BATCH_ID      = 1
               AND s.RATE_DATE         = p_rate_date
               AND s.RATE_CHANGE_PCT   IS NULL
               AND s.SOURCE_NAME LIKE '%_INV';

            UPDATE DWH.STG_FX_RATES s
               SET s.VOLATILITY_30D = (
                     SELECT STDDEV(fv.EXCHANGE_RATE)
                       FROM FIN.TRS_FX_RATES fv
                      WHERE fv.FROM_CURRENCY_ID = s.FROM_CURRENCY_ID
                        AND fv.TO_CURRENCY_ID   = s.TO_CURRENCY_ID
                        AND fv.RATE_DATE BETWEEN (p_rate_date - 30) AND p_rate_date
                        AND fv.RATE_TYPE = p_rate_type
                      HAVING COUNT(*) >= 3
                   )
             WHERE s.DW_BATCH_ID    = 1
               AND s.RATE_DATE       = p_rate_date
               AND s.VOLATILITY_30D  = 0
               AND s.SOURCE_NAME LIKE '%_INV';

        EXCEPTION
            WHEN OTHERS THEN
                IF c_fx_rates%ISOPEN THEN CLOSE c_fx_rates; END IF;
                ROLLBACK;
                RAISE;
        END LOAD_FX_RATES;
        """;

    private UniversalSemanticEngine parse(String sql) {
        UniversalSemanticEngine engine = new UniversalSemanticEngine();
        PlSqlSemanticListener listener = new PlSqlSemanticListener(engine);
        listener.setDefaultSchema("DWH");
        PlSqlLexer lexer = new PlSqlLexer(CharStreams.fromString(sql));
        PlSqlParser parser = new PlSqlParser(new CommonTokenStream(lexer));
        new ParseTreeWalker().walk(listener, parser.sql_script());
        engine.resolvePendingColumns();
        return engine;
    }

    /**
     * Diagnostic dump: prints all statements sorted by lineStart.
     * Called on assertion failure to help pinpoint the bug.
     */
    private void dumpAllStatements(UniversalSemanticEngine engine) {
        var stmts = engine.getBuilder().getStatements();
        System.out.println("\n=== ALL STATEMENTS (" + stmts.size() + " total) ===");
        stmts.values().stream()
                .sorted(Comparator.comparingInt(StatementInfo::getLineStart))
                .forEach(si -> System.out.printf(
                        "  line %4d-%4d  type=%-14s routineGeoid=%-50s geoid=%s%n",
                        si.getLineStart(), si.getLineEnd(),
                        si.getType(),
                        si.getRoutineGeoid() != null ? si.getRoutineGeoid() : "<null>",
                        si.getGeoid()));
        System.out.println("=== END STATEMENTS ===\n");
    }

    // ─────────────────────────────────────────────────────────────
    // Test 1: procedure is registered
    // ─────────────────────────────────────────────────────────────

    @Test
    void procedureIsRegistered() {
        var engine = parse(SQL);
        var routines = engine.getBuilder().getRoutines();

        // Should have exactly one routine: PROCEDURE:LOAD_FX_RATES
        assertTrue(routines.values().stream()
                        .anyMatch(r -> r.getName().equalsIgnoreCase("LOAD_FX_RATES")),
                "Expected routine LOAD_FX_RATES. Got: " + routines.keySet());

        String routineGeoid = routines.keySet().stream()
                .filter(k -> k.toUpperCase().contains("LOAD_FX_RATES"))
                .findFirst().orElse(null);
        assertNotNull(routineGeoid, "LOAD_FX_RATES routine geoid must not be null");
        LOG.info("LOAD_FX_RATES geoid: {}", routineGeoid);
    }

    // ─────────────────────────────────────────────────────────────
    // Test 2: both top-level UPDATEs carry the routine prefix
    // ─────────────────────────────────────────────────────────────

    @Test
    void allTopLevelUpdatesMustCarryRoutineGeoid() {
        var engine = parse(SQL);
        var stmts = engine.getBuilder().getStatements();

        // Find the expected routine geoid
        String routineGeoid = engine.getBuilder().getRoutines().keySet().stream()
                .filter(k -> k.toUpperCase().contains("LOAD_FX_RATES"))
                .findFirst().orElse(null);
        assertNotNull(routineGeoid, "LOAD_FX_RATES routine must exist");

        // Collect all top-level UPDATE statements (no parent statement)
        List<StatementInfo> topUpdates = stmts.values().stream()
                .filter(si -> "UPDATE".equals(si.getType()))
                .filter(si -> si.getParentStatementGeoid() == null)
                .sorted(Comparator.comparingInt(StatementInfo::getLineStart))
                .collect(Collectors.toList());

        LOG.info("Top-level UPDATE statements found: {}", topUpdates.size());
        topUpdates.forEach(u -> LOG.info("  UPDATE line={} routineGeoid={} geoid={}",
                u.getLineStart(), u.getRoutineGeoid(), u.getGeoid()));

        // Dump all for diagnostics (always visible in test output)
        dumpAllStatements(engine);

        // There must be exactly 2 top-level UPDATEs
        assertEquals(2, topUpdates.size(),
                "Expected 2 top-level UPDATE statements, got " + topUpdates.size() +
                "\nAll statements:\n" + stmts.keySet());

        for (StatementInfo upd : topUpdates) {
            // Each UPDATE geoid must contain the routine name
            assertNotNull(upd.getRoutineGeoid(),
                    "UPDATE at line " + upd.getLineStart() + " has null routineGeoid. " +
                    "Geoid: " + upd.getGeoid());

            assertTrue(upd.getRoutineGeoid().toUpperCase().contains("LOAD_FX_RATES"),
                    "UPDATE at line " + upd.getLineStart() +
                    " routineGeoid does not contain LOAD_FX_RATES: " + upd.getRoutineGeoid() +
                    ". Geoid: " + upd.getGeoid());

            // Geoid must NOT be bare "UPDATE:N"
            assertFalse(upd.getGeoid().startsWith("UPDATE:"),
                    "UPDATE at line " + upd.getLineStart() +
                    " has bare geoid without routine prefix: " + upd.getGeoid());
        }
    }

    // ─────────────────────────────────────────────────────────────
    // Test 3: no bare "UPDATE:N" geoid exists anywhere
    // ─────────────────────────────────────────────────────────────

    @Test
    void noBareUpdateGeoidExists() {
        var engine = parse(SQL);
        var stmts = engine.getBuilder().getStatements();

        List<String> bareUpdates = stmts.keySet().stream()
                .filter(g -> g.matches("UPDATE:\\d+"))
                .collect(Collectors.toList());

        if (!bareUpdates.isEmpty()) {
            dumpAllStatements(engine);
        }

        assertTrue(bareUpdates.isEmpty(),
                "Found bare UPDATE geoids without routine prefix: " + bareUpdates +
                "\nThis indicates routineGeoid was null when the statement was created.");
    }

    // ─────────────────────────────────────────────────────────────
    // Test 4: parameters p_rate_date / p_rate_type are NOT classified as columns
    //         (they must appear as routine_param atoms, not DaliColumn references)
    // ─────────────────────────────────────────────────────────────

    @Test
    void parametersNotClassifiedAsColumns() {
        var engine = parse(SQL);
        var routines = engine.getBuilder().getRoutines();

        var loadFxRates = routines.values().stream()
                .filter(r -> r.getName().equalsIgnoreCase("LOAD_FX_RATES"))
                .findFirst().orElse(null);
        assertNotNull(loadFxRates, "LOAD_FX_RATES routine must exist");

        // Verify parameters are registered
        assertTrue(loadFxRates.hasParameter("P_RATE_DATE"),
                "P_RATE_DATE must be registered as parameter");
        assertTrue(loadFxRates.hasParameter("P_RATE_TYPE"),
                "P_RATE_TYPE must be registered as parameter");
        assertTrue(loadFxRates.hasParameter("P_BATCH_ID"),
                "P_BATCH_ID must be registered as parameter");

        LOG.info("LOAD_FX_RATES parameters registered: {}", loadFxRates.getTypedParameters());

        // Verify no DaliColumn was created for the parameter names
        // (columns are stored in the builder's columns map by geoid TABLE.COLNAME)
        var columns = engine.getBuilder().getColumns();
        boolean pRateDateAsColumn = columns.keySet().stream()
                .anyMatch(geoid -> geoid.toUpperCase().endsWith(".P_RATE_DATE"));
        boolean pRateTypeAsColumn = columns.keySet().stream()
                .anyMatch(geoid -> geoid.toUpperCase().endsWith(".P_RATE_TYPE"));

        if (pRateDateAsColumn || pRateTypeAsColumn) {
            System.out.println("\n=== COLUMNS (parameters wrongly classified) ===");
            columns.keySet().stream()
                    .filter(g -> g.toUpperCase().contains("P_RATE"))
                    .forEach(g -> System.out.println("  " + g));
            System.out.println("=== END ===\n");
        }

        assertFalse(pRateDateAsColumn,
                "p_rate_date must not be classified as a DaliColumn. " +
                "Matching geoids: " + columns.keySet().stream()
                        .filter(g -> g.toUpperCase().endsWith(".P_RATE_DATE"))
                        .collect(Collectors.joining(", ")));

        assertFalse(pRateTypeAsColumn,
                "p_rate_type must not be classified as a DaliColumn. " +
                "Matching geoids: " + columns.keySet().stream()
                        .filter(g -> g.toUpperCase().endsWith(".P_RATE_TYPE"))
                        .collect(Collectors.joining(", ")));
    }

    // ─────────────────────────────────────────────────────────────
    // Test 5: FORALL loop variable i is NOT classified as a column
    // ─────────────────────────────────────────────────────────────

    @Test
    void forallIndexVariableNotClassifiedAsColumn() {
        var engine = parse(SQL);
        var columns = engine.getBuilder().getColumns();

        // Column "I" on STG_FX_RATES would be the bug symptom
        boolean iAsColumn = columns.keySet().stream()
                .anyMatch(geoid -> geoid.toUpperCase().endsWith(".I"));

        if (iAsColumn) {
            System.out.println("\n=== COLUMNS ending in .I (FORALL index bug) ===");
            columns.keySet().stream()
                    .filter(g -> g.toUpperCase().endsWith(".I"))
                    .forEach(g -> System.out.println("  " + g));
            System.out.println("=== END ===\n");
        }

        assertFalse(iAsColumn,
                "FORALL index variable 'i' must not be classified as a DaliColumn. " +
                "Matching geoids: " + columns.keySet().stream()
                        .filter(g -> g.toUpperCase().endsWith(".I"))
                        .collect(Collectors.joining(", ")));
    }

    // ─────────────────────────────────────────────────────────────
    // Test 6: scope balance — INSERT and MERGE have independent geoids
    //         (regression from GeoidParentChainTest pattern)
    // ─────────────────────────────────────────────────────────────

    @Test
    void insertAndMergeHaveIndependentGeoids() {
        var engine = parse(SQL);
        var stmts = engine.getBuilder().getStatements();

        String insertGeoid = stmts.values().stream()
                .filter(si -> "INSERT".equals(si.getType()) && si.getParentStatementGeoid() == null)
                .map(StatementInfo::getGeoid)
                .findFirst().orElse(null);
        String mergeGeoid = stmts.values().stream()
                .filter(si -> "MERGE".equals(si.getType()))
                .map(StatementInfo::getGeoid)
                .findFirst().orElse(null);

        LOG.info("INSERT geoid: {}", insertGeoid);
        LOG.info("MERGE geoid:  {}", mergeGeoid);

        assertNotNull(insertGeoid, "Top-level INSERT must exist");
        assertNotNull(mergeGeoid,  "MERGE must exist");

        assertNotEquals(insertGeoid, mergeGeoid,
                "INSERT and MERGE must have different geoids");
        assertFalse(mergeGeoid.startsWith(insertGeoid + ":"),
                "MERGE geoid must not start with INSERT geoid (scope chain broken). " +
                "INSERT=" + insertGeoid + " MERGE=" + mergeGeoid);
    }
}
