CREATE OR REPLACE PACKAGE crm_customers_pkg AS
  -- CRM Customer Management Package (integration-test fixture)
  PROCEDURE create_customer(
    p_name    IN VARCHAR2,
    p_email   IN VARCHAR2,
    p_phone   IN VARCHAR2 DEFAULT NULL
  );
  FUNCTION get_customer_count RETURN NUMBER;
  PROCEDURE update_customer_status(
    p_customer_id IN NUMBER,
    p_status      IN VARCHAR2
  );
END crm_customers_pkg;
/

CREATE OR REPLACE PACKAGE BODY crm_customers_pkg AS
  PROCEDURE create_customer(
    p_name    IN VARCHAR2,
    p_email   IN VARCHAR2,
    p_phone   IN VARCHAR2 DEFAULT NULL
  ) IS
  BEGIN
    INSERT INTO crm_customers(name, email, phone, created_at)
    VALUES (p_name, p_email, p_phone, SYSDATE);
    COMMIT;
  END create_customer;

  FUNCTION get_customer_count RETURN NUMBER IS
    v_count NUMBER;
  BEGIN
    SELECT COUNT(*) INTO v_count FROM crm_customers;
    RETURN v_count;
  END get_customer_count;

  PROCEDURE update_customer_status(
    p_customer_id IN NUMBER,
    p_status      IN VARCHAR2
  ) IS
  BEGIN
    UPDATE crm_customers
    SET status = p_status,
        updated_at = SYSDATE
    WHERE customer_id = p_customer_id;
    COMMIT;
  END update_customer_status;
END crm_customers_pkg;
/
