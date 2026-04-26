CREATE OR REPLACE PACKAGE hr_employees_pkg AS
  -- HR Employee Management Package (integration-test fixture)
  PROCEDURE hire_employee(
    p_first_name  IN VARCHAR2,
    p_last_name   IN VARCHAR2,
    p_department  IN VARCHAR2,
    p_salary      IN NUMBER,
    p_hire_date   IN DATE DEFAULT SYSDATE
  );
  FUNCTION get_department_headcount(p_department IN VARCHAR2) RETURN NUMBER;
  PROCEDURE terminate_employee(
    p_employee_id IN NUMBER,
    p_reason      IN VARCHAR2,
    p_term_date   IN DATE DEFAULT SYSDATE
  );
END hr_employees_pkg;
/

CREATE OR REPLACE PACKAGE BODY hr_employees_pkg AS
  PROCEDURE hire_employee(
    p_first_name  IN VARCHAR2,
    p_last_name   IN VARCHAR2,
    p_department  IN VARCHAR2,
    p_salary      IN NUMBER,
    p_hire_date   IN DATE DEFAULT SYSDATE
  ) IS
  BEGIN
    INSERT INTO hr_employees(first_name, last_name, department, salary, hire_date, status)
    VALUES (p_first_name, p_last_name, p_department, p_salary, p_hire_date, 'ACTIVE');
    COMMIT;
  END hire_employee;

  FUNCTION get_department_headcount(p_department IN VARCHAR2) RETURN NUMBER IS
    v_count NUMBER;
  BEGIN
    SELECT COUNT(*) INTO v_count
    FROM hr_employees
    WHERE department = p_department
      AND status = 'ACTIVE';
    RETURN v_count;
  END get_department_headcount;

  PROCEDURE terminate_employee(
    p_employee_id IN NUMBER,
    p_reason      IN VARCHAR2,
    p_term_date   IN DATE DEFAULT SYSDATE
  ) IS
  BEGIN
    UPDATE hr_employees
    SET status = 'TERMINATED',
        term_reason = p_reason,
        term_date = p_term_date
    WHERE employee_id = p_employee_id;
    COMMIT;
  END terminate_employee;
END hr_employees_pkg;
/
