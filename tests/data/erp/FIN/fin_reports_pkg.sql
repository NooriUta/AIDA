CREATE OR REPLACE PACKAGE fin_reports_pkg AS
  -- Finance Reporting Package (integration-test fixture)
  PROCEDURE generate_monthly_report(
    p_year  IN NUMBER,
    p_month IN NUMBER
  );
  FUNCTION get_revenue_ytd(p_year IN NUMBER) RETURN NUMBER;
  PROCEDURE generate_balance_sheet(p_as_of_date IN DATE DEFAULT SYSDATE);
END fin_reports_pkg;
/

CREATE OR REPLACE PACKAGE BODY fin_reports_pkg AS
  PROCEDURE generate_monthly_report(
    p_year  IN NUMBER,
    p_month IN NUMBER
  ) IS
    v_total_invoiced NUMBER := 0;
    v_total_paid     NUMBER := 0;
    v_total_overdue  NUMBER := 0;
  BEGIN
    SELECT NVL(SUM(amount), 0)
    INTO v_total_invoiced
    FROM fin_invoices
    WHERE EXTRACT(YEAR FROM created_at) = p_year
      AND EXTRACT(MONTH FROM created_at) = p_month;

    SELECT NVL(SUM(amount), 0)
    INTO v_total_paid
    FROM fin_invoices
    WHERE EXTRACT(YEAR FROM payment_date) = p_year
      AND EXTRACT(MONTH FROM payment_date) = p_month
      AND status = 'PAID';

    SELECT NVL(SUM(amount), 0)
    INTO v_total_overdue
    FROM fin_invoices
    WHERE status = 'UNPAID'
      AND due_date < SYSDATE;

    INSERT INTO fin_monthly_reports(report_year, report_month,
                                     total_invoiced, total_paid, total_overdue, created_at)
    VALUES (p_year, p_month, v_total_invoiced, v_total_paid, v_total_overdue, SYSDATE);
    COMMIT;
  END generate_monthly_report;

  FUNCTION get_revenue_ytd(p_year IN NUMBER) RETURN NUMBER IS
    v_revenue NUMBER;
  BEGIN
    SELECT NVL(SUM(amount), 0) INTO v_revenue
    FROM fin_invoices
    WHERE EXTRACT(YEAR FROM payment_date) = p_year
      AND status = 'PAID';
    RETURN v_revenue;
  END get_revenue_ytd;

  PROCEDURE generate_balance_sheet(p_as_of_date IN DATE DEFAULT SYSDATE) IS
  BEGIN
    INSERT INTO fin_balance_sheets(as_of_date, total_receivables, total_revenue, created_at)
    SELECT p_as_of_date,
           NVL(SUM(CASE WHEN status = 'UNPAID' THEN amount ELSE 0 END), 0),
           NVL(SUM(CASE WHEN status = 'PAID'   THEN amount ELSE 0 END), 0),
           SYSDATE
    FROM fin_invoices
    WHERE created_at <= p_as_of_date;
    COMMIT;
  END generate_balance_sheet;
END fin_reports_pkg;
/
