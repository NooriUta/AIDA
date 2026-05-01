CREATE OR REPLACE PACKAGE BODY DWH.PKG_PLTYPE_E2E AS

  TYPE t_item_rec IS RECORD (
    item_sk      NUMBER(19),
    item_name    VARCHAR2(255),
    unit_price   NUMBER(19,4)
  );
  TYPE t_item_tab IS TABLE OF t_item_rec INDEX BY PLS_INTEGER;

  PROCEDURE LOAD_ITEMS IS
    l_items t_item_tab;
  BEGIN
    SELECT item_sk, item_name, unit_price
    BULK COLLECT INTO l_items
    FROM DWH.STG_ITEMS;

    FORALL i IN l_items.FIRST..l_items.LAST
      INSERT INTO DWH.DIM_ITEM (item_sk, item_name, unit_price)
      VALUES (l_items(i).item_sk, l_items(i).item_name, l_items(i).unit_price);
  END LOAD_ITEMS;

END PKG_PLTYPE_E2E;
