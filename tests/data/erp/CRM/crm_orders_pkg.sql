CREATE OR REPLACE PACKAGE crm_orders_pkg AS
  -- CRM Order Management Package (integration-test fixture)
  PROCEDURE create_order(
    p_customer_id IN NUMBER,
    p_product_id  IN NUMBER,
    p_quantity    IN NUMBER,
    p_unit_price  IN NUMBER
  );
  FUNCTION get_order_total(p_order_id IN NUMBER) RETURN NUMBER;
  PROCEDURE cancel_order(p_order_id IN NUMBER, p_reason IN VARCHAR2);
END crm_orders_pkg;
/

CREATE OR REPLACE PACKAGE BODY crm_orders_pkg AS
  PROCEDURE create_order(
    p_customer_id IN NUMBER,
    p_product_id  IN NUMBER,
    p_quantity    IN NUMBER,
    p_unit_price  IN NUMBER
  ) IS
    v_order_id NUMBER;
  BEGIN
    SELECT crm_orders_seq.NEXTVAL INTO v_order_id FROM dual;
    INSERT INTO crm_orders(order_id, customer_id, product_id, quantity, unit_price, status, created_at)
    VALUES (v_order_id, p_customer_id, p_product_id, p_quantity, p_unit_price, 'PENDING', SYSDATE);
    COMMIT;
  END create_order;

  FUNCTION get_order_total(p_order_id IN NUMBER) RETURN NUMBER IS
    v_total NUMBER;
  BEGIN
    SELECT quantity * unit_price INTO v_total
    FROM crm_orders
    WHERE order_id = p_order_id;
    RETURN NVL(v_total, 0);
  END get_order_total;

  PROCEDURE cancel_order(p_order_id IN NUMBER, p_reason IN VARCHAR2) IS
  BEGIN
    UPDATE crm_orders
    SET status = 'CANCELLED',
        cancel_reason = p_reason,
        updated_at = SYSDATE
    WHERE order_id = p_order_id
      AND status NOT IN ('SHIPPED', 'DELIVERED');
    COMMIT;
  END cancel_order;
END crm_orders_pkg;
/
