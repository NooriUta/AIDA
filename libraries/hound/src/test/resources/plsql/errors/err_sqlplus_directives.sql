-- ERROR FIXTURE: err_sqlplus_directives.sql
-- Category: SQL*Plus directives (preprocessor test)
--
-- SQL*Plus directives must be STRIPPED before ANTLR4 sees the file.
-- After stripping lines 1-5, the procedure body parses cleanly → 0 ANTLR4 errors.
-- If stripping is disabled, ANTLR4 fires errors on lines 6-9.

SET SERVEROUTPUT ON SIZE UNLIMITED
SET DEFINE OFF
PROMPT Loading DWH.LOAD_SALES...
WHENEVER SQLERROR EXIT FAILURE ROLLBACK
SPOOL /tmp/load_sales.log

CREATE OR REPLACE PROCEDURE DWH.LOAD_SALES AS
    v_cnt NUMBER := 0;
BEGIN
    SELECT COUNT(*) INTO v_cnt FROM DWH.FACT_SALES;
    DBMS_OUTPUT.PUT_LINE('rows: ' || v_cnt);
END LOAD_SALES;
/
