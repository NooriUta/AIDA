-- ERROR FIXTURE: err_unknown_token.sql
-- Category: Unknown / unmapped token
--
-- The identifier $SYS_VAR uses a dollar sign prefix which is valid Oracle syntax
-- in some contexts but triggers "token recognition error" in the PL/SQL lexer.
-- Expected: 1 ANTLR4 error at line 10, col ~17 (token recognition error for '$')
-- Parser recovers and registers the INSERT statement.

CREATE OR REPLACE PROCEDURE DWH.BAD_TOKEN_PROC AS
    v_ref VARCHAR2(100) := $SYS_VAR;
BEGIN
    INSERT INTO DWH.LOG_TABLE (msg, created_at)
    VALUES (v_ref, SYSDATE);
    COMMIT;
END BAD_TOKEN_PROC;
/
