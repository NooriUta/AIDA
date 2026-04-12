-- Regression test fixture: MERGE patterns — USING subquery, collection, direct table
CREATE OR REPLACE PACKAGE BODY LOADS.PKG_MERGE_LOADER AS

    -- MERGE USING (SELECT ...) SOURCE  ← S1.BUG-4 regression
    PROCEDURE load_assets(p_session_id IN VARCHAR2) IS
    BEGIN
        MERGE INTO LOADS.EAD_ASSET_LIST t
        USING (
            SELECT
                d.AGR_ID,
                d.CLIENT_ID,
                d.PRODUCT_CODE,
                d.DATE_BEGIN,
                d.DATE_END,
                d.AMOUNT,
                d.CURRENCY,
                d.STATUS_CODE,
                d.RISK_CLASS
            FROM LOADS.T_DEF_AGR_CORP d
            WHERE d.LOAD_DATE = TRUNC(SYSDATE)
        ) SOURCE
        ON (t.AGR_ID = SOURCE.AGR_ID)
        WHEN MATCHED THEN
            UPDATE SET
                t.CLIENT_ID    = SOURCE.CLIENT_ID,
                t.PRODUCT_CODE = SOURCE.PRODUCT_CODE,
                t.DATE_BEGIN   = SOURCE.DATE_BEGIN,
                t.DATE_END     = SOURCE.DATE_END,
                t.AMOUNT       = SOURCE.AMOUNT,
                t.CURRENCY     = SOURCE.CURRENCY,
                t.STATUS_CODE  = SOURCE.STATUS_CODE,
                t.RISK_CLASS   = SOURCE.RISK_CLASS,
                t.UPDATED_AT   = SYSDATE,
                t.SESSION_ID   = p_session_id
            WHERE ORA_HASH(t.AGR_ID) > 0
        WHEN NOT MATCHED THEN
            INSERT (AGR_ID, CLIENT_ID, PRODUCT_CODE, DATE_BEGIN, DATE_END,
                    AMOUNT, CURRENCY, STATUS_CODE, RISK_CLASS, CREATED_AT, SESSION_ID)
            VALUES (SOURCE.AGR_ID, SOURCE.CLIENT_ID, SOURCE.PRODUCT_CODE,
                    SOURCE.DATE_BEGIN, SOURCE.DATE_END, SOURCE.AMOUNT,
                    SOURCE.CURRENCY, SOURCE.STATUS_CODE, SOURCE.RISK_CLASS,
                    SYSDATE, p_session_id);
        COMMIT;
    END;

    -- MERGE USING TABLE(collection)
    PROCEDURE load_from_collection(p_data IN SYS.ODCIVARCHAR2LIST) IS
    BEGIN
        MERGE INTO LOADS.STAGING_TABLE t
        USING TABLE(p_data) v
        ON (t.KEY_VALUE = v.COLUMN_VALUE)
        WHEN MATCHED THEN
            UPDATE SET t.PROCESSED = 'Y', t.UPDATED_AT = SYSDATE
        WHEN NOT MATCHED THEN
            INSERT (KEY_VALUE, PROCESSED, CREATED_AT)
            VALUES (v.COLUMN_VALUE, 'N', SYSDATE);
        COMMIT;
    END;

    -- Multi-table INSERT via SELECT with JOIN
    PROCEDURE copy_active_deals(p_cutoff_date IN DATE) IS
    BEGIN
        INSERT INTO LOADS.DEAL_ARCHIVE (
            DEAL_ID, CLIENT_ID, DEAL_DATE, AMOUNT, CATEGORY, ARCHIVED_AT
        )
        SELECT
            d.DEAL_ID,
            d.CLIENT_ID,
            d.DEAL_DATE,
            d.AMOUNT,
            NVL(c.CATEGORY, 'UNKNOWN')  AS CATEGORY,
            SYSDATE                     AS ARCHIVED_AT
        FROM   LOADS.DEALS       d
        JOIN   LOADS.CLIENTS     c ON c.CLIENT_ID = d.CLIENT_ID
        WHERE  d.DEAL_DATE  < p_cutoff_date
          AND  d.STATUS      = 'ACTIVE'
          AND  d.AMOUNT     > 0;
        COMMIT;
    END;

END PKG_MERGE_LOADER;
/
