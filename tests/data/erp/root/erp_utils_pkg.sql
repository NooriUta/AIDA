CREATE OR REPLACE PACKAGE erp_utils_pkg AS
  -- ERP Utility Package — cross-module helpers (integration-test fixture)
  FUNCTION format_currency(p_amount IN NUMBER, p_currency IN VARCHAR2 DEFAULT 'RUB') RETURN VARCHAR2;
  PROCEDURE log_error(
    p_module    IN VARCHAR2,
    p_procedure IN VARCHAR2,
    p_error     IN VARCHAR2,
    p_params    IN VARCHAR2 DEFAULT NULL
  );
  FUNCTION get_next_business_day(p_date IN DATE) RETURN DATE;
END erp_utils_pkg;
/

CREATE OR REPLACE PACKAGE BODY erp_utils_pkg AS
  FUNCTION format_currency(p_amount IN NUMBER, p_currency IN VARCHAR2 DEFAULT 'RUB') RETURN VARCHAR2 IS
  BEGIN
    RETURN TO_CHAR(p_amount, 'FM999,999,999,990.00') || ' ' || p_currency;
  END format_currency;

  PROCEDURE log_error(
    p_module    IN VARCHAR2,
    p_procedure IN VARCHAR2,
    p_error     IN VARCHAR2,
    p_params    IN VARCHAR2 DEFAULT NULL
  ) IS
    PRAGMA AUTONOMOUS_TRANSACTION;
  BEGIN
    INSERT INTO erp_error_log(module, procedure_name, error_text, params, logged_at)
    VALUES (p_module, p_procedure, p_error, p_params, SYSDATE);
    COMMIT;
  END log_error;

  FUNCTION get_next_business_day(p_date IN DATE) RETURN DATE IS
    v_date DATE := p_date + 1;
  BEGIN
    WHILE TO_CHAR(v_date, 'DY', 'NLS_DATE_LANGUAGE=ENGLISH') IN ('SAT', 'SUN') LOOP
      v_date := v_date + 1;
    END LOOP;
    RETURN v_date;
  END get_next_business_day;
END erp_utils_pkg;
/
