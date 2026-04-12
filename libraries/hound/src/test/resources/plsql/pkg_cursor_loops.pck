-- S3.C2 fixture: cursor declarations + FOR loops
-- Tests cursor-loop linkage: rec.col must resolve to cursor's source table columns
CREATE OR REPLACE PACKAGE BODY TEST_PKG.PKG_CURSOR_LOOPS AS

    -- Case A: Named cursor + FOR rec IN cursor LOOP
    PROCEDURE process_active_accounts IS
        CURSOR c_accounts IS
            SELECT a.ACCOUNT_ID, a.BALANCE, a.TIER, a.OWNER_ID
            FROM   ACCOUNTS a
            WHERE  a.STATUS = 'ACTIVE'
              AND  a.BALANCE > 0;

        v_total   NUMBER := 0;
        v_updated NUMBER := 0;
    BEGIN
        FOR rec IN c_accounts LOOP
            -- rec.BALANCE, rec.TIER, rec.ACCOUNT_ID must resolve to ACCOUNTS columns
            v_total := v_total + rec.BALANCE;

            IF rec.TIER = 'PREMIUM' THEN
                UPDATE ACCOUNTS
                SET    LAST_TOUCH = SYSDATE
                WHERE  ACCOUNT_ID = rec.ACCOUNT_ID;
                v_updated := v_updated + 1;
            END IF;
        END LOOP;

        INSERT INTO PROCESS_LOG (TOTAL_BALANCE, UPDATED_COUNT, RUN_DATE)
        VALUES (v_total, v_updated, SYSDATE);
        COMMIT;
    END;

    -- Case B: Inline cursor FOR rec IN (SELECT ...) LOOP
    PROCEDURE summarize_by_owner(p_cutoff IN DATE) IS
        v_count NUMBER := 0;
    BEGIN
        FOR rec IN (
            SELECT t.OWNER_ID, SUM(t.AMOUNT) AS TOTAL_AMT, COUNT(*) AS TXN_COUNT
            FROM   TRANSACTIONS t
            WHERE  t.TXN_DATE >= p_cutoff
              AND  t.STATUS    = 'POSTED'
            GROUP BY t.OWNER_ID
        ) LOOP
            -- rec.OWNER_ID, rec.TOTAL_AMT, rec.TXN_COUNT must resolve
            IF rec.TOTAL_AMT > 1000 THEN
                UPDATE OWNERS
                SET    TOTAL_SPENT  = rec.TOTAL_AMT,
                       TXN_COUNT    = rec.TXN_COUNT,
                       UPDATED_AT   = SYSDATE
                WHERE  OWNER_ID     = rec.OWNER_ID;
                v_count := v_count + 1;
            END IF;
        END LOOP;
        COMMIT;
    END;

    -- Case C: Named cursor with parameters
    PROCEDURE report_by_type(p_type IN VARCHAR2, p_from IN DATE) IS
        CURSOR c_txns(cp_type VARCHAR2, cp_from DATE) IS
            SELECT t.TXN_ID, t.AMOUNT, t.TXN_DATE, t.ACCOUNT_ID
            FROM   TRANSACTIONS t
            WHERE  t.TXN_TYPE = cp_type
              AND  t.TXN_DATE >= cp_from;
    BEGIN
        FOR rec IN c_txns(p_type, p_from) LOOP
            -- rec.TXN_ID, rec.AMOUNT must resolve to TRANSACTIONS
            INSERT INTO TXN_REPORT (TXN_ID, AMOUNT, REPORT_DATE)
            VALUES (rec.TXN_ID, rec.AMOUNT, SYSDATE);
        END LOOP;
        COMMIT;
    END;

END PKG_CURSOR_LOOPS;
/
