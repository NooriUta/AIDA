-- ERROR FIXTURE: err_update_alias.sql
-- Category: Known grammar limitation — UPDATE with table alias
--
-- Oracle allows UPDATE table_alias SET alias.col = val.
-- The PL/SQL ANTLR4 grammar does not support table aliases in UPDATE DML.
-- Expected: ANTLR4 errors on the SET lines (≥1 error per aliased column).
-- Parser recovers; the procedure is still registered, atoms are extracted.
--
-- This is a KNOWN LIMITATION of the grammar, not a bug in Oracle SQL.

CREATE OR REPLACE PROCEDURE HR.UPDATE_SALARIES(p_dept IN NUMBER) AS
BEGIN
    UPDATE HR.EMPLOYEES e
    SET    e.SALARY    = e.SALARY * 1.10,
           e.UPDATED_AT = SYSDATE
    WHERE  e.DEPARTMENT_ID = p_dept;
    COMMIT;
END UPDATE_SALARIES;
/
