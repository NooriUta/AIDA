-- Regression test fixture: basic DML patterns (INSERT / UPDATE / DELETE / SELECT INTO)
CREATE OR REPLACE PACKAGE BODY HR.PKG_EMPLOYEE_DML AS

    PROCEDURE insert_employee(
        p_id     IN NUMBER,
        p_name   IN VARCHAR2,
        p_dept   IN NUMBER,
        p_salary IN NUMBER
    ) IS
    BEGIN
        INSERT INTO HR.EMPLOYEES (EMPLOYEE_ID, EMPLOYEE_NAME, DEPARTMENT_ID, SALARY, HIRE_DATE, ACTIVE)
        VALUES (p_id, p_name, p_dept, p_salary, SYSDATE, 'Y');
        COMMIT;
    END;

    PROCEDURE update_salary(
        p_id        IN NUMBER,
        p_new_salary IN NUMBER
    ) IS
    BEGIN
        UPDATE HR.EMPLOYEES e
        SET    e.SALARY     = p_new_salary,
               e.UPDATED_AT = SYSDATE
        WHERE  e.EMPLOYEE_ID = p_id
          AND  e.ACTIVE      = 'Y';
        COMMIT;
    END;

    PROCEDURE deactivate_employee(p_id IN NUMBER) IS
    BEGIN
        UPDATE HR.EMPLOYEES e
        SET    e.ACTIVE      = 'N',
               e.UPDATED_AT  = SYSDATE
        WHERE  e.EMPLOYEE_ID = p_id;

        DELETE FROM HR.EMPLOYEE_ROLES er
        WHERE  er.EMPLOYEE_ID = p_id;

        COMMIT;
    END;

    FUNCTION get_salary(p_id IN NUMBER) RETURN NUMBER IS
        v_salary NUMBER := 0;
    BEGIN
        SELECT e.SALARY
        INTO   v_salary
        FROM   HR.EMPLOYEES e
        WHERE  e.EMPLOYEE_ID = p_id
          AND  e.ACTIVE = 'Y';
        RETURN v_salary;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN RETURN 0;
    END;

    PROCEDURE process_department(p_dept_id IN NUMBER) IS
        v_count  NUMBER := 0;
        v_total  NUMBER := 0;
        v_avg    NUMBER := 0;
    BEGIN
        SELECT COUNT(e.EMPLOYEE_ID), SUM(e.SALARY), AVG(e.SALARY)
        INTO   v_count, v_total, v_avg
        FROM   HR.EMPLOYEES e
        WHERE  e.DEPARTMENT_ID = p_dept_id
          AND  e.ACTIVE = 'Y';

        IF v_count > 0 THEN
            UPDATE HR.DEPARTMENTS d
            SET    d.HEADCOUNT       = v_count,
                   d.TOTAL_SALARY    = v_total,
                   d.AVG_SALARY      = v_avg,
                   d.UPDATED_AT      = SYSDATE
            WHERE  d.DEPARTMENT_ID = p_dept_id;
        END IF;
        COMMIT;
    END;

END PKG_EMPLOYEE_DML;
/
