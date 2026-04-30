-- HND-v2 E2E fixture: schema-level OBJECT types + PIPELINED function + PIPE ROW
-- Used by: pltype-object-e2e.spec.ts (TC-TPG-HOUND-OP-E2E-00..03)

CREATE OR REPLACE TYPE TESTSCHEMA.T_LINE_REC AS OBJECT (
    item_id    NUMBER(10),
    qty        NUMBER(12,3),
    unit_price NUMBER(18,4)
);
/
CREATE OR REPLACE TYPE TESTSCHEMA.T_LINE_LIST AS TABLE OF TESTSCHEMA.T_LINE_REC;
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
END PKG_PIPE_TEST;
