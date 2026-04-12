-- Regression test fixture: MERGE USING (SELECT ... UNION ALL SELECT ...) patterns
-- T11: tests that UNION ALL inside MERGE USING is parsed without S1.BUG-4-style alias bleed
CREATE OR REPLACE PACKAGE BODY LOADS.PKG_MERGE_UNION_LOADER AS

    -- MERGE USING UNION ALL in USING clause
    -- Tests: source aliases (s1/s2) must NOT leak into outer MERGE target resolution.
    -- Columns: CLIENT_ID, PRODUCT_CODE, AMOUNT, STATUS_CODE from both SELECT branches.
    PROCEDURE load_combined(p_session_id IN VARCHAR2) IS
    BEGIN
        MERGE INTO LOADS.ASSET_SUMMARY t
        USING (
            SELECT
                s1.CLIENT_ID,
                s1.PRODUCT_CODE,
                s1.AMOUNT,
                'CORP'      AS STATUS_CODE
            FROM LOADS.CORP_DATA s1
            WHERE s1.LOAD_DATE = TRUNC(SYSDATE)
            UNION ALL
            SELECT
                s2.CLIENT_ID,
                s2.PRODUCT_CODE,
                s2.AMOUNT,
                'RETAIL'    AS STATUS_CODE
            FROM LOADS.RETAIL_DATA s2
            WHERE s2.LOAD_DATE = TRUNC(SYSDATE)
        ) SOURCE
        ON (t.CLIENT_ID = SOURCE.CLIENT_ID AND t.PRODUCT_CODE = SOURCE.PRODUCT_CODE)
        WHEN MATCHED THEN
            UPDATE SET
                t.AMOUNT      = SOURCE.AMOUNT,
                t.STATUS_CODE = SOURCE.STATUS_CODE,
                t.UPDATED_AT  = SYSDATE,
                t.SESSION_ID  = p_session_id
        WHEN NOT MATCHED THEN
            INSERT (CLIENT_ID, PRODUCT_CODE, AMOUNT, STATUS_CODE, CREATED_AT, SESSION_ID)
            VALUES (SOURCE.CLIENT_ID, SOURCE.PRODUCT_CODE, SOURCE.AMOUNT,
                    SOURCE.STATUS_CODE, SYSDATE, p_session_id);
        COMMIT;
    END;

    -- MERGE USING UNION ALL with three branches and GROUP BY in subquery
    PROCEDURE load_aggregated(p_cutoff DATE) IS
    BEGIN
        MERGE INTO LOADS.REGION_TOTALS t
        USING (
            SELECT region_id, SUM(amount) AS total_amount, 'EUR' AS currency
              FROM LOADS.DEALS_EUR
             WHERE deal_date >= p_cutoff
             GROUP BY region_id
            UNION ALL
            SELECT region_id, SUM(amount) AS total_amount, 'USD' AS currency
              FROM LOADS.DEALS_USD
             WHERE deal_date >= p_cutoff
             GROUP BY region_id
            UNION ALL
            SELECT region_id, SUM(amount) AS total_amount, 'GBP' AS currency
              FROM LOADS.DEALS_GBP
             WHERE deal_date >= p_cutoff
             GROUP BY region_id
        ) src
        ON (t.region_id = src.region_id AND t.currency = src.currency)
        WHEN MATCHED THEN
            UPDATE SET t.total_amount = src.total_amount,
                       t.updated_at   = SYSDATE
        WHEN NOT MATCHED THEN
            INSERT (region_id, total_amount, currency, created_at)
            VALUES (src.region_id, src.total_amount, src.currency, SYSDATE);
        COMMIT;
    END;

END PKG_MERGE_UNION_LOADER;
/
