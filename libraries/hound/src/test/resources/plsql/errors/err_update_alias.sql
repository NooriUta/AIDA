-- FIXTURE: err_update_alias.sql (formerly an error fixture, now parses cleanly)
-- Category: UPDATE with table alias — column_name supports alias.col via grammar rule
--
-- Oracle allows UPDATE tbl alias SET alias.col = val.
-- The PL/SQL grammar column_name rule: identifier ('.' id_expression)* handles alias.col.
-- Previously triggered a spurious ANTLR error because stripSqlPlusDirectives was
-- incorrectly stripping indented SET clauses (starting with "SET ") as SQL*Plus directives.
-- After the fix in HoundParserImpl.isSqlPlusDirective: 0 errors, 0 warnings expected.

CREATE OR REPLACE PROCEDURE HR.UPDATE_SALARIES(p_dept IN NUMBER) AS
BEGIN
    UPDATE HR.EMPLOYEES e
    SET    e.SALARY    = e.SALARY * 1.10,
           e.UPDATED_AT = SYSDATE
    WHERE  e.DEPARTMENT_ID = p_dept;
    COMMIT;
END UPDATE_SALARIES;
/
