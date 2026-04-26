CREATE OR REPLACE PACKAGE fin_invoices_pkg AS
  -- Finance Invoice Management Package (integration-test fixture)
  PROCEDURE create_invoice(
    p_customer_id IN NUMBER,
    p_order_id    IN NUMBER,
    p_amount      IN NUMBER,
    p_due_date    IN DATE
  );
  FUNCTION get_overdue_invoices_count RETURN NUMBER;
  PROCEDURE mark_paid(
    p_invoice_id    IN NUMBER,
    p_payment_date  IN DATE DEFAULT SYSDATE,
    p_payment_ref   IN VARCHAR2
  );
END fin_invoices_pkg;
/

CREATE OR REPLACE PACKAGE BODY fin_invoices_pkg AS
  PROCEDURE create_invoice(
    p_customer_id IN NUMBER,
    p_order_id    IN NUMBER,
    p_amount      IN NUMBER,
    p_due_date    IN DATE
  ) IS
    v_invoice_id NUMBER;
  BEGIN
    SELECT fin_invoices_seq.NEXTVAL INTO v_invoice_id FROM dual;
    INSERT INTO fin_invoices(invoice_id, customer_id, order_id, amount,
                              due_date, status, created_at)
    VALUES (v_invoice_id, p_customer_id, p_order_id, p_amount,
            p_due_date, 'UNPAID', SYSDATE);
    COMMIT;
  END create_invoice;

  FUNCTION get_overdue_invoices_count RETURN NUMBER IS
    v_count NUMBER;
  BEGIN
    SELECT COUNT(*) INTO v_count
    FROM fin_invoices
    WHERE status = 'UNPAID'
      AND due_date < SYSDATE;
    RETURN v_count;
  END get_overdue_invoices_count;

  PROCEDURE mark_paid(
    p_invoice_id    IN NUMBER,
    p_payment_date  IN DATE DEFAULT SYSDATE,
    p_payment_ref   IN VARCHAR2
  ) IS
  BEGIN
    UPDATE fin_invoices
    SET status = 'PAID',
        payment_date = p_payment_date,
        payment_ref = p_payment_ref
    WHERE invoice_id = p_invoice_id
      AND status = 'UNPAID';
    IF SQL%ROWCOUNT = 0 THEN
      RAISE_APPLICATION_ERROR(-20001, 'Invoice ' || p_invoice_id || ' not found or already paid');
    END IF;
    COMMIT;
  END mark_paid;
END fin_invoices_pkg;
/
