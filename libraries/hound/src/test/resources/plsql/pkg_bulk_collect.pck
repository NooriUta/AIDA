-- G6 fixture: BULK COLLECT INTO + FORALL INSERT without column list
-- Tests DaliRecord vertex creation and BULK_COLLECTS_INTO / RECORD_HAS_FIELD edges
CREATE OR REPLACE PACKAGE BODY TEST_PKG.PKG_BULK_COLLECT AS

    -- Case A: Simple BULK COLLECT + FORALL INSERT (no column list)
    PROCEDURE load_sales IS
        TYPE t_tab IS TABLE OF orders_stg%ROWTYPE;
        l_tab t_tab;
    BEGIN
        SELECT order_id, line_num, amount
        BULK COLLECT INTO l_tab
        FROM orders_stg
        WHERE status = 'ACTIVE';

        FORALL i IN 1..l_tab.COUNT
            INSERT INTO fact_sales
            VALUES (l_tab(i).order_id, l_tab(i).line_num, l_tab(i).amount);

        COMMIT;
    END;

    -- Case B: BULK COLLECT with sequence in VALUES
    PROCEDURE load_with_seq IS
        TYPE t_rec IS TABLE OF staging_orders%ROWTYPE;
        l_rec t_rec;
    BEGIN
        SELECT customer_id, product_code, qty, price
        BULK COLLECT INTO l_rec
        FROM staging_orders;

        FORALL j IN 1..l_rec.COUNT
            INSERT INTO dwh_orders
            VALUES (
                SEQ_DWH.NEXTVAL,
                l_rec(j).customer_id,
                l_rec(j).product_code,
                l_rec(j).qty,
                l_rec(j).price
            );

        COMMIT;
    END;

END PKG_BULK_COLLECT;
/
