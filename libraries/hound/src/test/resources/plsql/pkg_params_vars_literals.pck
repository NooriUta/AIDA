-- S3.C1 + S3.C3 fixture:
--   C1: string literals, SYSDATE, NULL, numeric constants in WHERE/CASE
--   C3: proc params, local vars — read and write, inside subqueries
CREATE OR REPLACE PACKAGE BODY TEST_PKG.PKG_PARAMS_VARS AS

    -- C1: literals in WHERE, CASE, comparison
    PROCEDURE filter_by_status(p_account_id IN NUMBER, p_from_date IN DATE) IS
        v_count  NUMBER := 0;
        v_status VARCHAR2(20);
    BEGIN
        SELECT COUNT(*)
        INTO v_count
        FROM TRANSACTIONS t
        WHERE t.ACCOUNT_ID = p_account_id
          AND t.TXN_DATE  >= p_from_date
          AND t.STATUS     = 'ACTIVE'
          AND t.TXN_TYPE  IN ('DEBIT', 'CREDIT')
          AND t.AMOUNT     > 0;

        IF v_count > 0 THEN
            v_status := 'FOUND';
        ELSE
            v_status := 'EMPTY';
        END IF;

        INSERT INTO AUDIT_LOG (ACCOUNT_ID, STATUS, LOG_DATE)
        VALUES (p_account_id, v_status, SYSDATE);
    END;

    -- C3: local var read+write, param in subquery
    PROCEDURE enrich_account(p_id IN NUMBER, p_cutoff IN DATE) IS
        v_balance   NUMBER;
        v_tier      VARCHAR2(10);
        v_updated   DATE;
    BEGIN
        -- C3-read: params used in subquery WHERE
        SELECT a.BALANCE, a.TIER
        INTO   v_balance, v_tier
        FROM   ACCOUNTS a
        WHERE  a.ACCOUNT_ID = p_id
          AND  a.VALID_UNTIL > p_cutoff;

        -- C1: numeric constant, NULL
        IF v_balance IS NULL THEN
            v_balance := 0;
        END IF;

        -- C3-write: local var on LHS of assignment
        v_updated := SYSDATE;

        -- C3-read: local var in WHERE of another query
        UPDATE ACCOUNTS
        SET    BALANCE      = v_balance,
               LAST_UPDATED = v_updated,
               TIER         = v_tier
        WHERE  ACCOUNT_ID   = p_id;

        COMMIT;
    END;

    -- C1: CASE with string literals, SYSDATE in SELECT list
    FUNCTION get_label(p_code IN VARCHAR2) RETURN VARCHAR2 IS
        v_label VARCHAR2(50);
    BEGIN
        SELECT CASE p_code
                   WHEN 'A' THEN 'Premium'
                   WHEN 'B' THEN 'Standard'
                   WHEN 'C' THEN 'Basic'
                   ELSE 'Unknown'
               END
        INTO v_label
        FROM DUAL;

        RETURN v_label;
    END;

    -- C3: param used deeply in nested subquery
    PROCEDURE archive_old(p_cutoff_date IN DATE, p_schema IN VARCHAR2) IS
        v_rows NUMBER := 0;
    BEGIN
        INSERT INTO ARCHIVE.TXN_HIST (TXN_ID, AMOUNT, TXN_DATE, ARCHIVED_AT)
        SELECT t.TXN_ID,
               t.AMOUNT,
               t.TXN_DATE,
               SYSDATE
        FROM   TRANSACTIONS t
        WHERE  t.TXN_DATE < p_cutoff_date
          AND  t.STATUS   = 'CLOSED'
          AND  t.TXN_ID NOT IN (
                   SELECT h.TXN_ID
                   FROM   ARCHIVE.TXN_HIST h
                   WHERE  h.TXN_DATE < p_cutoff_date
               );

        v_rows := SQL%ROWCOUNT;
        COMMIT;
    END;

END PKG_PARAMS_VARS;
/
