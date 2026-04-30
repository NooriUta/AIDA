-- HND-v2+v3 E2E fixture: schema-level OBJECT + COLLECTION + VARRAY + REF CURSOR + PIPELINED
-- Used by: pltype-object-e2e.spec.ts (TC-TPG-HOUND-OP-E2E-00..07)

-- OBJECT + COLLECTION (HND-08)
CREATE OR REPLACE TYPE TESTSCHEMA.T_LINE_REC AS OBJECT (
    item_id    NUMBER(10),
    qty        NUMBER(12,3),
    unit_price NUMBER(18,4)
);
/
CREATE OR REPLACE TYPE TESTSCHEMA.T_LINE_LIST AS TABLE OF TESTSCHEMA.T_LINE_REC;
/

-- VARRAY schema-level (HND-12)
CREATE OR REPLACE TYPE TESTSCHEMA.T_ID_ARR AS VARRAY(100) OF NUMBER(10);
/

-- Package with: REF CURSOR (HND-13) + PIPELINED function (HND-09) + TABLE() usage (HND-14)
CREATE OR REPLACE PACKAGE PKG_PIPE_TEST AS
    TYPE t_lines_cur IS REF CURSOR RETURN ORDER_LINES%ROWTYPE;
    FUNCTION GET_LINES(p_order_id IN NUMBER) RETURN TESTSCHEMA.T_LINE_LIST PIPELINED;
END PKG_PIPE_TEST;
/
CREATE OR REPLACE PACKAGE BODY PKG_PIPE_TEST AS
    FUNCTION GET_LINES(p_order_id IN NUMBER) RETURN TESTSCHEMA.T_LINE_LIST PIPELINED
    IS
        CURSOR c IS
            SELECT item_id, qty, unit_price FROM ORDER_LINES WHERE order_id = p_order_id;
    BEGIN
        FOR r IN c LOOP
            PIPE ROW (TESTSCHEMA.T_LINE_REC(
                item_id => r.item_id, qty => r.qty, unit_price => r.unit_price));
        END LOOP;
    END GET_LINES;

    PROCEDURE LOAD_SUMMARY(p_order_id IN NUMBER) IS
    BEGIN
        -- TABLE() downstream (HND-14)
        INSERT INTO ORDER_SUMMARY (item_id, total_qty, total_price)
        SELECT t.item_id, SUM(t.qty), SUM(t.unit_price)
          FROM TABLE(GET_LINES(p_order_id)) t
         GROUP BY t.item_id;
    END LOAD_SUMMARY;
END PKG_PIPE_TEST;
