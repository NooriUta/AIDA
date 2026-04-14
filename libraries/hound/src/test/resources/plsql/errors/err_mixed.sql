-- ERROR FIXTURE: err_mixed.sql
-- Category: Multiple errors in one file (SQL*Plus + grammar)
--
-- Combines SQL*Plus directives (stripped by preprocessor) with a genuine
-- grammar error (DELETE with subquery alias not supported).
-- Expected after stripping:
--   - ≥1 ANTLR4 error for the DELETE subquery construct
--   - SQL*Plus lines produce 0 ANTLR4 errors (stripped before parsing)
--   - Procedure DWH.CLEANUP_OLD_DATA is registered
--   - SELECT INTO atom in the procedure is extracted

SET SERVEROUTPUT ON
PROMPT Running cleanup...

CREATE OR REPLACE PROCEDURE DWH.CLEANUP_OLD_DATA(p_days IN NUMBER) AS
    v_cutoff DATE := SYSDATE - p_days;
    v_deleted NUMBER := 0;
BEGIN
    SELECT COUNT(*) INTO v_deleted
    FROM   DWH.STAGING_DATA
    WHERE  created_at < v_cutoff;

    DELETE FROM DWH.STAGING_DATA s
    WHERE  s.created_at < v_cutoff;

    COMMIT;
    DBMS_OUTPUT.PUT_LINE('Deleted: ' || v_deleted);
END CLEANUP_OLD_DATA;
/
