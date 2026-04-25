CREATE OR REPLACE PACKAGE hr_payroll_pkg AS
  -- HR Payroll Package (integration-test fixture)
  PROCEDURE run_payroll(p_period_month IN NUMBER, p_period_year IN NUMBER);
  FUNCTION calculate_net_salary(
    p_employee_id IN NUMBER,
    p_gross       IN NUMBER
  ) RETURN NUMBER;
  PROCEDURE process_bonus(
    p_employee_id IN NUMBER,
    p_amount      IN NUMBER,
    p_reason      IN VARCHAR2
  );
END hr_payroll_pkg;
/

CREATE OR REPLACE PACKAGE BODY hr_payroll_pkg AS
  c_tax_rate CONSTANT NUMBER := 0.13;
  c_pension_rate CONSTANT NUMBER := 0.06;

  PROCEDURE run_payroll(p_period_month IN NUMBER, p_period_year IN NUMBER) IS
    CURSOR c_employees IS
      SELECT employee_id, salary
      FROM hr_employees
      WHERE status = 'ACTIVE';
  BEGIN
    FOR emp IN c_employees LOOP
      INSERT INTO hr_payroll_records(employee_id, period_month, period_year,
                                     gross_salary, net_salary, processed_at)
      VALUES (emp.employee_id, p_period_month, p_period_year,
              emp.salary,
              calculate_net_salary(emp.employee_id, emp.salary),
              SYSDATE);
    END LOOP;
    COMMIT;
  END run_payroll;

  FUNCTION calculate_net_salary(
    p_employee_id IN NUMBER,
    p_gross       IN NUMBER
  ) RETURN NUMBER IS
  BEGIN
    RETURN ROUND(p_gross * (1 - c_tax_rate - c_pension_rate), 2);
  END calculate_net_salary;

  PROCEDURE process_bonus(
    p_employee_id IN NUMBER,
    p_amount      IN NUMBER,
    p_reason      IN VARCHAR2
  ) IS
  BEGIN
    INSERT INTO hr_bonuses(employee_id, amount, reason, paid_at)
    VALUES (p_employee_id, p_amount, p_reason, SYSDATE);
    COMMIT;
  END process_bonus;
END hr_payroll_pkg;
/
