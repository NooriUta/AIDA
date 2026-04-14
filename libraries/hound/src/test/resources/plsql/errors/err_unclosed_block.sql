-- ERROR FIXTURE: err_unclosed_block.sql
-- Category: Structural syntax error — missing END
--
-- The procedure body opens BEGIN but never closes it with END.
-- Expected: ANTLR4 reports "mismatched input '<EOF>'" or "no viable alternative"
--           at or near the last line.
-- Parser reports ≥1 error; partial parse may still register the procedure name.

CREATE OR REPLACE PROCEDURE DWH.BROKEN_PROC AS
    v_cnt NUMBER := 0;
BEGIN
    SELECT COUNT(*) INTO v_cnt FROM DWH.FACT_SALES;
    INSERT INTO DWH.LOG_TABLE (msg, created_at)
    VALUES ('count=' || v_cnt, SYSDATE);
-- intentionally missing: END BROKEN_PROC;
