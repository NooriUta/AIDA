-- Regression test fixture: UNION ALL + CASE WHEN patterns
CREATE OR REPLACE PACKAGE BODY TEST.PKG_UNION_CASE AS

    -- UNION ALL: combines two result sets
    PROCEDURE proc_union IS
    BEGIN
        INSERT INTO TEST.COMBINED_DATA (col1, col2, data_source)
        SELECT t1.col1, t1.col2, 'SOURCE_A' AS data_source
          FROM TEST.TABLE_A t1
         WHERE t1.active = 'Y'
        UNION ALL
        SELECT t2.col3, t2.col4, 'SOURCE_B' AS data_source
          FROM TEST.TABLE_B t2
         WHERE t2.status = 'ACTIVE';
        COMMIT;
    END;

    -- CASE WHEN in SELECT list
    PROCEDURE proc_case IS
    BEGIN
        INSERT INTO TEST.REPORT_DATA (label, value, category)
        SELECT
            CASE
                WHEN t.status = 'A' THEN t.name
                WHEN t.status = 'I' THEN 'Inactive: ' || t.name
                ELSE 'N/A'
            END AS label,
            t.col1,
            t.category
        FROM TEST.TABLE_A t
        WHERE t.col1 IS NOT NULL;
        COMMIT;
    END;

    -- CASE in WHERE clause + GROUP BY
    PROCEDURE proc_case_group IS
        v_threshold NUMBER := 100;
    BEGIN
        INSERT INTO TEST.SUMMARY (dept_id, total_val, flag)
        SELECT
            t.dept_id,
            SUM(t.value) AS total_val,
            CASE WHEN SUM(t.value) > v_threshold THEN 'HIGH' ELSE 'LOW' END AS flag
        FROM TEST.TRANSACTIONS t
        WHERE t.type IN ('DEBIT', 'CREDIT')
        GROUP BY t.dept_id;
        COMMIT;
    END;

END PKG_UNION_CASE;
