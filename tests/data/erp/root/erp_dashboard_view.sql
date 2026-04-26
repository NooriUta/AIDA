CREATE OR REPLACE VIEW erp_dashboard_vw AS
-- ERP Dashboard View — aggregated KPIs across CRM, HR, FIN (integration-test fixture)
SELECT
  -- CRM metrics
  (SELECT COUNT(*) FROM crm_customers WHERE status = 'ACTIVE')        AS active_customers,
  (SELECT COUNT(*) FROM crm_orders    WHERE status = 'PENDING')       AS pending_orders,
  (SELECT NVL(SUM(quantity * unit_price), 0)
     FROM crm_orders WHERE status IN ('SHIPPED', 'DELIVERED')
       AND created_at >= TRUNC(SYSDATE, 'MM'))                         AS monthly_sales,
  -- HR metrics
  (SELECT COUNT(*) FROM hr_employees  WHERE status = 'ACTIVE')        AS active_employees,
  (SELECT NVL(SUM(salary), 0) FROM hr_employees WHERE status = 'ACTIVE') AS total_payroll,
  -- FIN metrics
  (SELECT NVL(SUM(amount), 0)
     FROM fin_invoices WHERE status = 'UNPAID')                        AS total_receivables,
  (SELECT NVL(SUM(amount), 0)
     FROM fin_invoices WHERE status = 'UNPAID' AND due_date < SYSDATE) AS overdue_amount,
  SYSDATE                                                               AS snapshot_at
FROM dual;
/
