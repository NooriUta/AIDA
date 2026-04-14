-- Fixture: SQL*Plus directives that must be stripped before ANTLR4 parsing.
-- Without stripping these lines, ANTLR4 fails on the first line.
SET SERVEROUTPUT ON SIZE UNLIMITED
SET DEFINE OFF
PROMPT Initialising schema...
WHENEVER SQLERROR EXIT FAILURE ROLLBACK

CREATE OR REPLACE PROCEDURE DWH.LOAD_SALES AS
    v_cnt NUMBER := 0;
BEGIN
    SELECT COUNT(*) INTO v_cnt FROM DWH.FACT_SALES;
    DBMS_OUTPUT.PUT_LINE('rows: ' || v_cnt);
END LOAD_SALES;
/
