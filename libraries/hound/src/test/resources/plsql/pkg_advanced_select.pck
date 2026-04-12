-- Regression test fixture: EXISTS / NOT IN / scalar subquery / UPDATE with subquery
CREATE OR REPLACE PACKAGE BODY TEST.PKG_ADVANCED_SELECT AS

    -- EXISTS subquery in WHERE
    PROCEDURE proc_exists IS
    BEGIN
        INSERT INTO TEST.RESULT_A (col1, col2)
        SELECT t1.col1, t1.col2
          FROM TEST.TABLE_A t1
         WHERE EXISTS (
                   SELECT 1 FROM TEST.TABLE_B t2
                    WHERE t2.id = t1.id
                      AND t2.active = 'Y'
               );
        COMMIT;
    END;

    -- NOT IN with subquery
    PROCEDURE proc_not_in IS
    BEGIN
        DELETE FROM TEST.TABLE_A
         WHERE col1 NOT IN (
                   SELECT col2 FROM TEST.TABLE_B WHERE status = 'VALID'
               );
        COMMIT;
    END;

    -- Scalar subquery in SET clause
    PROCEDURE proc_scalar_subquery IS
    BEGIN
        UPDATE TEST.TABLE_A t
           SET t.col1 = (
                   SELECT MAX(t2.col2)
                     FROM TEST.TABLE_B t2
                    WHERE t2.id = t.id
               )
         WHERE t.needs_update = 'Y';
        COMMIT;
    END;

    -- Correlated subquery in SELECT list
    PROCEDURE proc_correlated IS
    BEGIN
        INSERT INTO TEST.ENRICHED (id, col1, max_related)
        SELECT
            t1.id,
            t1.col1,
            (SELECT MAX(t2.value) FROM TEST.TABLE_B t2 WHERE t2.ref_id = t1.id) AS max_related
        FROM TEST.TABLE_A t1
        WHERE t1.active = 'Y';
        COMMIT;
    END;

END PKG_ADVANCED_SELECT;
