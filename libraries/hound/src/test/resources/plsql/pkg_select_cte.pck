-- Regression test fixture: SELECT patterns — CTEs, window functions, GROUP BY, cursors
CREATE OR REPLACE PACKAGE BODY FINANCE.PKG_REPORTS AS

    -- CTE + window function + GROUP BY
    PROCEDURE build_salary_report(p_year IN NUMBER) IS
        CURSOR c_dept IS
            WITH dept_totals AS (
                SELECT
                    e.DEPARTMENT_ID,
                    SUM(e.SALARY)           AS total_salary,
                    COUNT(e.EMPLOYEE_ID)    AS headcount,
                    AVG(e.SALARY)           AS avg_salary,
                    MAX(e.SALARY)           AS max_salary
                FROM FINANCE.EMPLOYEES e
                WHERE e.HIRE_YEAR = p_year
                  AND e.ACTIVE = 'Y'
                GROUP BY e.DEPARTMENT_ID
            ),
            ranked AS (
                SELECT
                    dt.DEPARTMENT_ID,
                    dt.total_salary,
                    dt.headcount,
                    dt.avg_salary,
                    dt.max_salary,
                    RANK() OVER (ORDER BY dt.total_salary DESC) AS salary_rank
                FROM dept_totals dt
            )
            SELECT r.DEPARTMENT_ID, r.total_salary, r.headcount,
                   r.avg_salary, r.max_salary, r.salary_rank
            FROM ranked r
            WHERE r.salary_rank <= 10;
        v_rec c_dept%ROWTYPE;
    BEGIN
        FOR v_rec IN c_dept LOOP
            INSERT INTO FINANCE.SALARY_REPORT (
                DEPARTMENT_ID, TOTAL_SALARY, HEADCOUNT,
                AVG_SALARY, MAX_SALARY, SALARY_RANK, REPORT_YEAR
            ) VALUES (
                v_rec.DEPARTMENT_ID,
                v_rec.total_salary,
                v_rec.headcount,
                v_rec.avg_salary,
                v_rec.max_salary,
                v_rec.salary_rank,
                p_year
            );
        END LOOP;
        COMMIT;
    END;

    -- Aggregation with multi-table JOIN
    PROCEDURE summarise_transactions(p_from_date IN DATE, p_to_date IN DATE) IS
    BEGIN
        INSERT INTO FINANCE.TRANSACTION_SUMMARY (
            ACCOUNT_ID, TX_COUNT, TOTAL_AMOUNT, AVG_AMOUNT, PERIOD_FROM, PERIOD_TO
        )
        SELECT
            t.ACCOUNT_ID,
            COUNT(t.TX_ID)             AS tx_count,
            SUM(t.AMOUNT)              AS total_amount,
            AVG(t.AMOUNT)              AS avg_amount,
            p_from_date,
            p_to_date
        FROM FINANCE.TRANSACTIONS t
        JOIN FINANCE.ACCOUNTS     a ON a.ACCOUNT_ID = t.ACCOUNT_ID
        WHERE t.TX_DATE BETWEEN p_from_date AND p_to_date
          AND a.STATUS = 'ACTIVE'
        GROUP BY t.ACCOUNT_ID
        HAVING COUNT(t.TX_ID) > 0;
        COMMIT;
    END;

    -- Cursor FOR loop with analytic
    PROCEDURE rank_accounts(p_portfolio_id IN NUMBER) IS
        v_rank   NUMBER;
        v_prev   NUMBER := 0;
    BEGIN
        FOR rec IN (
            SELECT
                a.ACCOUNT_ID,
                a.BALANCE,
                DENSE_RANK() OVER (ORDER BY a.BALANCE DESC) AS bal_rank
            FROM FINANCE.ACCOUNTS a
            WHERE a.PORTFOLIO_ID = p_portfolio_id
              AND a.STATUS = 'ACTIVE'
        ) LOOP
            UPDATE FINANCE.ACCOUNTS a2
            SET    a2.RANK = rec.bal_rank
            WHERE  a2.ACCOUNT_ID = rec.ACCOUNT_ID;
        END LOOP;
        COMMIT;
    END;

END PKG_REPORTS;
/
