CREATE OR REPLACE PROCEDURE TEST.PROC_HIER AS
    CURSOR c IS
        WITH cte_hier AS (
            SELECT a.ACCOUNT_ID,
                   LEVEL AS lvl
              FROM FIN.ACCOUNTS a
             WHERE a.IS_ACTIVE = 'Y'
             START WITH a.PARENT_ACCOUNT_ID IS NULL
           CONNECT BY PRIOR a.ACCOUNT_ID = a.PARENT_ACCOUNT_ID
        )
        SELECT * FROM cte_hier;
BEGIN
    NULL;
END;
/
