-- =============================================================================
-- DWH ETL :: PKG_ETL_FACT_FINANCE
-- Oracle 19c / 23c  |  Financial Fact Tables  |  Data Lineage Material
-- =============================================================================
SET SERVEROUTPUT ON;

-- *****************************************************************************
-- PACKAGE SPECIFICATION
-- *****************************************************************************
CREATE OR REPLACE PACKAGE DWH.PKG_ETL_FACT_FINANCE AS

    gc_package_name    CONSTANT VARCHAR2(60) := 'PKG_ETL_FACT_FINANCE';
    gc_batch_limit     CONSTANT PLS_INTEGER  := 10000;
    gc_parallel_degree CONSTANT PLS_INTEGER  := 8;

    TYPE t_id_array        IS TABLE OF NUMBER(19)     INDEX BY PLS_INTEGER;
    TYPE t_amount_array    IS TABLE OF NUMBER(19,4)   INDEX BY PLS_INTEGER;
    TYPE t_varchar_array   IS TABLE OF VARCHAR2(4000) INDEX BY PLS_INTEGER;
    TYPE t_date_array      IS TABLE OF DATE           INDEX BY PLS_INTEGER;

    TYPE t_invoice_stg_rec IS RECORD (
        invoice_sk          NUMBER(19),
        date_id             NUMBER(19),
        customer_sk         NUMBER(19),
        account_sk          NUMBER(19),
        invoice_id          NUMBER(19),
        invoice_num         VARCHAR2(50),
        invoice_type        VARCHAR2(15),
        invoice_date        DATE,
        due_date            DATE,
        line_num            NUMBER(10),
        line_description    VARCHAR2(400),
        item_code           VARCHAR2(50),
        quantity            NUMBER(19,4),
        unit_price          NUMBER(19,4),
        discount_pct        NUMBER(9,4),
        line_amount         NUMBER(19,4),
        tax_code            VARCHAR2(30),
        tax_rate            NUMBER(9,4),
        tax_amount          NUMBER(19,4),
        total_amount        NUMBER(19,4),
        paid_amount         NUMBER(19,4),
        outstanding_amount  NUMBER(19,4),
        currency_code       VARCHAR2(3),
        exchange_rate       NUMBER(19,10),
        base_line_amount    NUMBER(19,4),
        base_tax_amount     NUMBER(19,4),
        base_total_amount   NUMBER(19,4),
        fiscal_year         NUMBER(4),
        fiscal_period       NUMBER(2),
        payment_count       NUMBER(10),
        days_overdue        NUMBER(10),
        aging_bucket        VARCHAR2(20),
        journal_ref         VARCHAR2(50),
        cost_center_name    VARCHAR2(200),
        credit_note_amount  NUMBER(19,4),
        running_balance     NUMBER(19,4),
        row_seq             NUMBER(10),
        status              VARCHAR2(20)
    );

    TYPE t_invoice_stg_tab IS TABLE OF t_invoice_stg_rec INDEX BY PLS_INTEGER;

    TYPE t_payment_stg_rec IS RECORD (
        payment_sk          NUMBER(19),
        date_id             NUMBER(19),
        customer_sk         NUMBER(19),
        account_sk          NUMBER(19),
        payment_id          NUMBER(19),
        payment_num         VARCHAR2(50),
        payment_type        VARCHAR2(20),
        payment_date        DATE,
        bank_account_id     NUMBER(19),
        bank_name           VARCHAR2(400),
        bank_currency       VARCHAR2(3),
        invoice_id          NUMBER(19),
        invoice_num         VARCHAR2(50),
        allocated_amount    NUMBER(19,4),
        discount_taken      NUMBER(19,4),
        write_off_amount    NUMBER(19,4),
        payment_amount      NUMBER(19,4),
        unallocated_amount  NUMBER(19,4),
        currency_code       VARCHAR2(3),
        exchange_rate       NUMBER(19,10),
        base_amount         NUMBER(19,4),
        inv_exchange_rate   NUMBER(19,10),
        fx_gain_loss        NUMBER(19,4),
        fiscal_year         NUMBER(4),
        fiscal_period       NUMBER(2),
        payment_method      VARCHAR2(30),
        reference_num       VARCHAR2(50),
        cleared_date        DATE,
        clearing_status     VARCHAR2(20),
        status              VARCHAR2(20)
    );

    TYPE t_payment_stg_tab IS TABLE OF t_payment_stg_rec INDEX BY PLS_INTEGER;

    TYPE t_journal_stg_rec IS RECORD (
        journal_line_sk     NUMBER(19),
        date_id             NUMBER(19),
        account_sk          NUMBER(19),
        journal_line_id     NUMBER(19),
        journal_id          NUMBER(19),
        journal_num         VARCHAR2(30),
        journal_type        VARCHAR2(20),
        journal_date        DATE,
        line_num            NUMBER(10),
        account_id          NUMBER(19),
        account_code        VARCHAR2(30),
        account_name        VARCHAR2(400),
        account_type        VARCHAR2(20),
        account_subtype     VARCHAR2(30),
        normal_balance      VARCHAR2(6),
        is_posting          VARCHAR2(1),
        account_path        VARCHAR2(2000),
        cost_center_id      NUMBER(19),
        cc_code             VARCHAR2(20),
        cc_name             VARCHAR2(200),
        cc_type             VARCHAR2(30),
        budget_holder       VARCHAR2(200),
        cc_hierarchy_path   VARCHAR2(2000),
        debit_amount        NUMBER(19,4),
        credit_amount       NUMBER(19,4),
        base_debit          NUMBER(19,4),
        base_credit         NUMBER(19,4),
        currency_code       VARCHAR2(3),
        exchange_rate       NUMBER(19,10),
        fiscal_year         NUMBER(4),
        fiscal_period       NUMBER(2),
        period_status       VARCHAR2(20),
        source_type         VARCHAR2(30),
        source_id           NUMBER(19),
        reversing_journal_id NUMBER(19),
        is_intercompany     VARCHAR2(1),
        is_recurring        VARCHAR2(1),
        budget_planned      NUMBER(19,4),
        budget_variance     NUMBER(19,4),
        approval_status     VARCHAR2(20),
        status              VARCHAR2(20)
    );

    TYPE t_journal_stg_tab IS TABLE OF t_journal_stg_rec INDEX BY PLS_INTEGER;

    TYPE t_budget_var_rec IS RECORD (
        budget_var_sk       NUMBER(19),
        date_id             NUMBER(19),
        account_sk          NUMBER(19),
        budget_line_id      NUMBER(19),
        budget_id           NUMBER(19),
        budget_code         VARCHAR2(30),
        budget_name         VARCHAR2(200),
        budget_type         VARCHAR2(20),
        fiscal_year         NUMBER(4),
        fiscal_period       NUMBER(2),
        account_id          NUMBER(19),
        account_code        VARCHAR2(30),
        account_name        VARCHAR2(400),
        cost_center_id      NUMBER(19),
        cc_code             VARCHAR2(20),
        cc_name             VARCHAR2(200),
        planned_amount      NUMBER(19,4),
        revised_amount      NUMBER(19,4),
        actual_amount       NUMBER(19,4),
        encumbered_amount   NUMBER(19,4),
        variance_amount     NUMBER(19,4),
        variance_pct        NUMBER(9,4),
        ytd_planned         NUMBER(19,4),
        ytd_actual          NUMBER(19,4),
        ytd_variance        NUMBER(19,4),
        run_rate_proj       NUMBER(19,4),
        forecast_at_compl   NUMBER(19,4),
        prior_year_actual   NUMBER(19,4),
        prior_year_variance NUMBER(19,4),
        variance_rank       NUMBER(10),
        status              VARCHAR2(20)
    );

    TYPE t_budget_var_tab IS TABLE OF t_budget_var_rec INDEX BY PLS_INTEGER;

    TYPE t_gl_balance_rec IS RECORD (
        gl_balance_sk       NUMBER(19),
        date_id             NUMBER(19),
        account_sk          NUMBER(19),
        account_id          NUMBER(19),
        account_code        VARCHAR2(30),
        account_name        VARCHAR2(400),
        account_type        VARCHAR2(20),
        cost_center_id      NUMBER(19),
        cc_code             VARCHAR2(20),
        cc_name             VARCHAR2(200),
        fiscal_year         NUMBER(4),
        fiscal_period       NUMBER(2),
        currency_code       VARCHAR2(3),
        opening_balance     NUMBER(19,4),
        period_debit        NUMBER(19,4),
        period_credit       NUMBER(19,4),
        period_activity     NUMBER(19,4),
        closing_balance     NUMBER(19,4),
        base_opening        NUMBER(19,4),
        base_period_debit   NUMBER(19,4),
        base_period_credit  NUMBER(19,4),
        base_closing        NUMBER(19,4),
        adjustment_amount   NUMBER(19,4),
        reclass_amount      NUMBER(19,4),
        hierarchy_level     NUMBER(2),
        parent_account_id   NUMBER(19),
        period_status       VARCHAR2(20),
        status              VARCHAR2(20)
    );

    TYPE t_gl_balance_tab IS TABLE OF t_gl_balance_rec INDEX BY PLS_INTEGER;

    TYPE t_ic_elim_rec IS RECORD (
        elim_sk             NUMBER(19),
        journal_id_dr       NUMBER(19),
        journal_id_cr       NUMBER(19),
        entity_dr           NUMBER(19),
        entity_cr           NUMBER(19),
        account_id          NUMBER(19),
        account_code        VARCHAR2(30),
        fiscal_year         NUMBER(4),
        fiscal_period       NUMBER(2),
        debit_amount        NUMBER(19,4),
        credit_amount       NUMBER(19,4),
        base_debit          NUMBER(19,4),
        base_credit         NUMBER(19,4),
        currency_code       VARCHAR2(3),
        exchange_rate       NUMBER(19,10),
        mismatch_amount     NUMBER(19,4),
        elim_journal_num    VARCHAR2(30),
        status              VARCHAR2(20)
    );

    TYPE t_ic_elim_tab IS TABLE OF t_ic_elim_rec INDEX BY PLS_INTEGER;

    TYPE t_inv_ref_cursor IS REF CURSOR RETURN t_invoice_stg_rec;
    TYPE t_pay_ref_cursor IS REF CURSOR RETURN t_payment_stg_rec;
    TYPE t_jnl_ref_cursor IS REF CURSOR RETURN t_journal_stg_rec;

    PROCEDURE load_fact_invoices (
        p_from_date  IN DATE,
        p_to_date    IN DATE,
        p_mode       IN VARCHAR2 DEFAULT 'INCREMENTAL'
    );

    PROCEDURE load_fact_payments (
        p_from_date  IN DATE,
        p_to_date    IN DATE,
        p_mode       IN VARCHAR2 DEFAULT 'INCREMENTAL'
    );

    PROCEDURE load_fact_journal_entries (
        p_from_date  IN DATE,
        p_to_date    IN DATE,
        p_mode       IN VARCHAR2 DEFAULT 'INCREMENTAL'
    );

    PROCEDURE load_fact_budget_variance (
        p_fiscal_year   IN NUMBER,
        p_period_from   IN NUMBER DEFAULT 1,
        p_period_to     IN NUMBER DEFAULT 12,
        p_mode          IN VARCHAR2 DEFAULT 'INCREMENTAL'
    );

    PROCEDURE load_fact_payment_aging (
        p_as_of_date IN DATE DEFAULT TRUNC(SYSDATE),
        p_mode       IN VARCHAR2 DEFAULT 'FULL'
    );

    PROCEDURE load_fact_gl_balance (
        p_fiscal_year   IN NUMBER,
        p_fiscal_period IN NUMBER,
        p_mode          IN VARCHAR2 DEFAULT 'INCREMENTAL'
    );

    PROCEDURE calc_intercompany_elim (
        p_fiscal_year   IN NUMBER,
        p_fiscal_period IN NUMBER
    );

    FUNCTION pipe_fact_invoices (
        p_from_date IN DATE,
        p_to_date   IN DATE
    ) RETURN DWH.T_INVOICE_FACT_TAB PIPELINED PARALLEL_ENABLE;

    FUNCTION pipe_fact_journals (
        p_from_date IN DATE,
        p_to_date   IN DATE
    ) RETURN DWH.T_JOURNAL_TAB PIPELINED PARALLEL_ENABLE;

END PKG_ETL_FACT_FINANCE;
/

-- *****************************************************************************
-- PACKAGE BODY
-- *****************************************************************************
CREATE OR REPLACE PACKAGE BODY DWH.PKG_ETL_FACT_FINANCE AS

    g_run_id        NUMBER(19);
    g_step_start    TIMESTAMP(6);
    g_row_count     NUMBER(19);

    -- =========================================================================
    -- Private: log_step
    -- =========================================================================
    PROCEDURE log_step (
        p_step_name IN VARCHAR2,
        p_status    IN VARCHAR2,
        p_row_count IN NUMBER   DEFAULT 0,
        p_error_msg IN VARCHAR2 DEFAULT NULL
    ) IS
        PRAGMA AUTONOMOUS_TRANSACTION;
    BEGIN
        INSERT INTO DWH.ETL_LOG (
            log_id, run_id, package_name, step_name,
            status, row_count, error_msg, start_ts, end_ts
        ) VALUES (
            DWH.SEQ_ETL_LOG.NEXTVAL, g_run_id, gc_package_name, p_step_name,
            p_status, p_row_count, SUBSTR(p_error_msg, 1, 4000),
            g_step_start, SYSTIMESTAMP
        );
        COMMIT;
    END log_step;

    -- =========================================================================
    -- Private: init_run
    -- =========================================================================
    PROCEDURE init_run (p_procedure_name IN VARCHAR2) IS
    BEGIN
        SELECT DWH.SEQ_ETL_RUN.NEXTVAL INTO g_run_id FROM DUAL;
        g_step_start := SYSTIMESTAMP;
        log_step(p_procedure_name || '.INIT', 'STARTED');
    END init_run;

    -- =========================================================================
    -- Private: handle_forall_exceptions
    -- =========================================================================
    PROCEDURE handle_forall_exceptions (
        p_step_name   IN VARCHAR2,
        p_error_count IN PLS_INTEGER
    ) IS
        v_msg VARCHAR2(4000);
    BEGIN
        FOR i IN 1 .. p_error_count LOOP
            v_msg := 'Bulk error idx=' || SQL%BULK_EXCEPTIONS(i).ERROR_INDEX
                  || ' ora=' || SQLERRM(-SQL%BULK_EXCEPTIONS(i).ERROR_CODE);
            log_step(p_step_name || '.BULK_ERR[' || i || ']', 'ERROR', 0, v_msg);
            EXIT WHEN i >= 50;
        END LOOP;
    END handle_forall_exceptions;

    -- =========================================================================
    -- Private: get_functional_currency
    -- =========================================================================
    FUNCTION get_functional_currency RETURN VARCHAR2 IS
        v_code VARCHAR2(3);
    BEGIN
        SELECT c.CURRENCY_CODE
          INTO v_code
          FROM FIN.CURRENCIES c
         WHERE c.IS_FUNCTIONAL = 'Y'
           AND ROWNUM = 1;
        RETURN v_code;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN RETURN 'USD';
    END get_functional_currency;

    -- =========================================================================
    -- Private: get_reporting_currency
    -- =========================================================================
    FUNCTION get_reporting_currency RETURN VARCHAR2 IS
        v_code VARCHAR2(3);
    BEGIN
        SELECT c.CURRENCY_CODE
          INTO v_code
          FROM FIN.CURRENCIES c
         WHERE c.IS_REPORTING = 'Y'
           AND ROWNUM = 1;
        RETURN v_code;
    EXCEPTION
        WHEN NO_DATA_FOUND THEN RETURN 'USD';
    END get_reporting_currency;

    -- =========================================================================
    -- 1. LOAD_FACT_INVOICES
    -- =========================================================================
    PROCEDURE load_fact_invoices (
        p_from_date  IN DATE,
        p_to_date    IN DATE,
        p_mode       IN VARCHAR2 DEFAULT 'INCREMENTAL'
    ) IS
        v_step          VARCHAR2(200) := 'LOAD_FACT_INVOICES';
        v_cur           t_inv_ref_cursor;
        v_buffer        t_invoice_stg_tab;
        v_total_rows    NUMBER(19) := 0;
        v_error_count   PLS_INTEGER := 0;
        v_func_curr     VARCHAR2(3);

        v_inv_sk_arr    t_id_array;
        v_date_arr      t_id_array;
        v_cust_sk_arr   t_id_array;
        v_acct_sk_arr   t_id_array;
        v_inv_id_arr    t_id_array;
        v_inv_num_arr   t_varchar_array;
        v_inv_type_arr  t_varchar_array;
        v_inv_date_arr  t_date_array;
        v_due_date_arr  t_date_array;
        v_line_num_arr  t_id_array;
        v_line_desc_arr t_varchar_array;
        v_item_arr      t_varchar_array;
        v_qty_arr       t_amount_array;
        v_uprice_arr    t_amount_array;
        v_disc_arr      t_amount_array;
        v_line_amt_arr  t_amount_array;
        v_tax_code_arr  t_varchar_array;
        v_tax_rate_arr  t_amount_array;
        v_tax_amt_arr   t_amount_array;
        v_total_arr     t_amount_array;
        v_paid_arr      t_amount_array;
        v_outst_arr     t_amount_array;
        v_curr_arr      t_varchar_array;
        v_xrate_arr     t_amount_array;
        v_bline_arr     t_amount_array;
        v_btax_arr      t_amount_array;
        v_btotal_arr    t_amount_array;
        v_fyear_arr     t_id_array;
        v_fperiod_arr   t_id_array;
        v_paycnt_arr    t_id_array;
        v_dayso_arr     t_id_array;
        v_aging_arr     t_varchar_array;
        v_jref_arr      t_varchar_array;
        v_ccname_arr    t_varchar_array;
        v_cnamt_arr     t_amount_array;
        v_runbal_arr    t_amount_array;
        v_rowseq_arr    t_id_array;
        v_status_arr    t_varchar_array;
    BEGIN
        init_run(v_step);
        v_func_curr := get_functional_currency;

        IF p_mode = 'FULL' THEN
            EXECUTE IMMEDIATE 'TRUNCATE TABLE DWH.FACT_INVOICES_STG';
            g_step_start := SYSTIMESTAMP;
            log_step(v_step || '.TRUNCATE', 'DONE');
        END IF;

        g_step_start := SYSTIMESTAMP;

        OPEN v_cur FOR
            WITH cte_invoices AS (
                SELECT /*+ PARALLEL(inv, 8) */
                       inv.INVOICE_ID,
                       inv.INVOICE_NUM,
                       inv.INVOICE_TYPE,
                       inv.INVOICE_DATE,
                       inv.DUE_DATE,
                       inv.FISCAL_PERIOD_ID,
                       inv.CUSTOMER_ID,
                       inv.CURRENCY_ID,
                       inv.EXCHANGE_RATE          AS inv_exchange_rate,
                       inv.SUBTOTAL_AMOUNT,
                       inv.TAX_AMOUNT             AS inv_tax_amount,
                       inv.TOTAL_AMOUNT           AS inv_total_amount,
                       inv.PAID_AMOUNT,
                       inv.PAYMENT_TERM_ID,
                       inv.STATUS                 AS inv_status,
                       il.INVOICE_LINE_ID,
                       il.LINE_NUM,
                       il.LINE_DESCRIPTION,
                       il.ITEM_CODE,
                       il.QUANTITY,
                       il.UNIT_OF_MEASURE,
                       il.UNIT_PRICE,
                       il.DISCOUNT_PCT,
                       il.LINE_AMOUNT,
                       il.TAX_CODE_ID,
                       il.TAX_AMOUNT              AS line_tax_amount,
                       il.ACCOUNT_ID,
                       il.COST_CENTER_ID
                  FROM FIN.INVOICES inv
                  JOIN FIN.INVOICE_LINES il
                    ON il.INVOICE_ID = inv.INVOICE_ID
                 WHERE inv.INVOICE_DATE BETWEEN p_from_date AND p_to_date
                   AND inv.STATUS IN ('APPROVED', 'POSTED', 'PARTIALLY_PAID', 'PAID')
            ),
            cte_customer_sk AS (
                SELECT dc.CUSTOMER_SK,
                       dc.CUSTOMER_ID,
                       cust.CUSTOMER_NAME,
                       cust.CUSTOMER_CODE,
                       cust.SEGMENT_ID,
                       cust.CREDIT_LIMIT AS cust_credit_limit,
                       (SELECT seg.SEGMENT_NAME
                          FROM CRM.CUSTOMER_SEGMENTS seg
                         WHERE seg.SEGMENT_ID = cust.SEGMENT_ID) AS segment_name
                  FROM DWH.DIM_CUSTOMER dc
                  JOIN CRM.CUSTOMERS cust
                    ON cust.CUSTOMER_ID = dc.CUSTOMER_ID
                 WHERE dc.IS_CURRENT = 'Y'
            ),
            cte_account_sk AS (
                SELECT da.ACCOUNT_SK,
                       da.ACCOUNT_ID,
                       da.ACCOUNT_CODE,
                       (SELECT a.ACCOUNT_NAME
                          FROM FIN.ACCOUNTS a
                         WHERE a.ACCOUNT_ID = da.ACCOUNT_ID) AS account_name,
                       (SELECT a.ACCOUNT_TYPE
                          FROM FIN.ACCOUNTS a
                         WHERE a.ACCOUNT_ID = da.ACCOUNT_ID) AS account_type
                  FROM DWH.DIM_ACCOUNT da
            ),
            cte_date_dim AS (
                SELECT dd.DATE_ID,
                       dd.FULL_DATE
                  FROM DWH.DIM_DATE dd
                 WHERE dd.FULL_DATE BETWEEN p_from_date AND p_to_date
            ),
            cte_fiscal AS (
                SELECT fp.FISCAL_PERIOD_ID,
                       fp.FISCAL_YEAR,
                       fp.PERIOD_NUMBER,
                       fp.PERIOD_NAME,
                       fp.START_DATE      AS period_start,
                       fp.END_DATE        AS period_end,
                       fp.QUARTER_NUM,
                       fp.STATUS          AS period_status
                  FROM FIN.FISCAL_PERIODS fp
                 WHERE fp.STATUS IN ('OPEN', 'CLOSED')
            ),
            cte_payment_alloc AS (
                SELECT pa.INVOICE_ID,
                       SUM(pa.ALLOCATED_AMOUNT)  AS total_allocated,
                       SUM(pa.DISCOUNT_TAKEN)    AS total_discount,
                       SUM(pa.WRITE_OFF_AMOUNT)  AS total_write_off,
                       COUNT(DISTINCT pa.PAYMENT_ID) AS payment_count,
                       MAX(pa.ALLOCATION_DATE)   AS last_alloc_date
                  FROM FIN.PAYMENT_ALLOCATIONS pa
                 WHERE pa.STATUS = 'APPLIED'
                 GROUP BY pa.INVOICE_ID
            ),
            cte_tax_detail AS (
                SELECT tc.TAX_CODE_ID,
                       tc.TAX_CODE,
                       tc.TAX_NAME,
                       tc.TAX_TYPE,
                       tc.TAX_RATE,
                       tc.IS_RECOVERABLE
                  FROM FIN.TAX_CODES tc
                 WHERE tc.TAX_CODE_ID IN (
                           SELECT DISTINCT il2.TAX_CODE_ID
                             FROM FIN.INVOICE_LINES il2
                             JOIN FIN.INVOICES inv2
                               ON inv2.INVOICE_ID = il2.INVOICE_ID
                            WHERE inv2.INVOICE_DATE BETWEEN p_from_date AND p_to_date
                       )
            ),
            cte_fx_rate AS (
                SELECT cur.CURRENCY_ID,
                       cur.CURRENCY_CODE,
                       cur.IS_FUNCTIONAL,
                       cur.IS_REPORTING,
                       (SELECT MAX(inv3.EXCHANGE_RATE)
                          FROM FIN.INVOICES inv3
                         WHERE inv3.CURRENCY_ID = cur.CURRENCY_ID
                           AND inv3.INVOICE_DATE BETWEEN p_from_date AND p_to_date
                           AND ROWNUM = 1) AS sample_rate
                  FROM FIN.CURRENCIES cur
            ),
            cte_credit_notes AS (
                SELECT cn.INVOICE_ID     AS original_invoice_id,
                       SUM(cn.TOTAL_AMOUNT) AS credit_note_total
                  FROM FIN.INVOICES cn
                 WHERE cn.INVOICE_TYPE = 'CREDIT_NOTE'
                   AND cn.STATUS IN ('APPROVED', 'POSTED')
                   AND cn.INVOICE_DATE BETWEEN p_from_date AND p_to_date
                 GROUP BY cn.INVOICE_ID
            ),
            cte_cost_centers AS (
                SELECT cc.COST_CENTER_ID,
                       cc.CC_CODE,
                       cc.CC_NAME,
                       cc.CC_TYPE,
                       cc.BUDGET_HOLDER
                  FROM FIN.COST_CENTERS cc
                 WHERE cc.COST_CENTER_ID IN (
                           SELECT DISTINCT il3.COST_CENTER_ID
                             FROM FIN.INVOICE_LINES il3
                             JOIN FIN.INVOICES inv4
                               ON inv4.INVOICE_ID = il3.INVOICE_ID
                            WHERE inv4.INVOICE_DATE BETWEEN p_from_date AND p_to_date
                       )
            ),
            cte_payment_terms AS (
                SELECT pt.PAYMENT_TERM_ID,
                       pt.TERM_CODE,
                       pt.TERM_NAME,
                       pt.NET_DAYS,
                       pt.DISCOUNT_DAYS,
                       pt.DISCOUNT_PCT
                  FROM FIN.PAYMENT_TERMS pt
            ),
            cte_journal_refs AS (
                SELECT j.SOURCE_ID    AS ref_invoice_id,
                       j.JOURNAL_NUM  AS journal_ref,
                       j.JOURNAL_DATE,
                       ROW_NUMBER() OVER (
                           PARTITION BY j.SOURCE_ID
                           ORDER BY j.JOURNAL_DATE DESC
                       ) AS rn
                  FROM FIN.JOURNALS j
                 WHERE j.SOURCE_TYPE = 'INVOICE'
                   AND j.STATUS = 'POSTED'
            ),
            cte_running_balance AS (
                SELECT inv5.INVOICE_ID,
                       inv5.INVOICE_DATE,
                       inv5.CUSTOMER_ID,
                       inv5.TOTAL_AMOUNT,
                       SUM(inv5.TOTAL_AMOUNT) OVER (
                           PARTITION BY inv5.CUSTOMER_ID
                           ORDER BY inv5.INVOICE_DATE, inv5.INVOICE_ID
                           ROWS UNBOUNDED PRECEDING
                       ) AS customer_running_balance
                  FROM FIN.INVOICES inv5
                 WHERE inv5.INVOICE_DATE BETWEEN p_from_date AND p_to_date
                   AND inv5.STATUS IN ('APPROVED', 'POSTED', 'PARTIALLY_PAID', 'PAID')
            ),
            cte_dedup AS (
                SELECT ci.INVOICE_ID,
                       ci.INVOICE_NUM,
                       ci.INVOICE_TYPE,
                       ci.INVOICE_DATE,
                       ci.DUE_DATE,
                       ci.FISCAL_PERIOD_ID,
                       ci.CUSTOMER_ID,
                       ci.CURRENCY_ID,
                       ci.inv_exchange_rate,
                       ci.SUBTOTAL_AMOUNT,
                       ci.inv_tax_amount,
                       ci.inv_total_amount,
                       ci.PAID_AMOUNT,
                       ci.PAYMENT_TERM_ID,
                       ci.inv_status,
                       ci.INVOICE_LINE_ID,
                       ci.LINE_NUM,
                       ci.LINE_DESCRIPTION,
                       ci.ITEM_CODE,
                       ci.QUANTITY,
                       ci.UNIT_PRICE,
                       ci.DISCOUNT_PCT,
                       ci.LINE_AMOUNT,
                       ci.TAX_CODE_ID,
                       ci.line_tax_amount,
                       ci.ACCOUNT_ID,
                       ci.COST_CENTER_ID,
                       ROW_NUMBER() OVER (
                           PARTITION BY ci.INVOICE_ID, ci.LINE_NUM
                           ORDER BY ci.INVOICE_LINE_ID
                       ) AS row_seq
                  FROM cte_invoices ci
            )
            SELECT /*+ PARALLEL(8) */
                   DWH.SEQ_DWH_INVOICE.NEXTVAL       AS invoice_sk,
                   NVL(ddt.DATE_ID, -1)               AS date_id,
                   NVL(csk.CUSTOMER_SK, -1)            AS customer_sk,
                   NVL(ask_lu.ACCOUNT_SK, -1)          AS account_sk,
                   d.INVOICE_ID,
                   d.INVOICE_NUM,
                   d.INVOICE_TYPE,
                   d.INVOICE_DATE,
                   d.DUE_DATE,
                   d.LINE_NUM,
                   d.LINE_DESCRIPTION,
                   d.ITEM_CODE,
                   d.QUANTITY,
                   d.UNIT_PRICE,
                   d.DISCOUNT_PCT,
                   d.LINE_AMOUNT,
                   NVL(tx.TAX_CODE, 'N/A')             AS tax_code,
                   NVL(tx.TAX_RATE, 0)                 AS tax_rate,
                   d.line_tax_amount                    AS tax_amount,
                   d.inv_total_amount                   AS total_amount,
                   NVL(d.PAID_AMOUNT, 0)               AS paid_amount,
                   d.inv_total_amount - NVL(d.PAID_AMOUNT, 0)
                       - NVL(cn.credit_note_total, 0)  AS outstanding_amount,
                   NVL(fx.CURRENCY_CODE, v_func_curr)  AS currency_code,
                   NVL(d.inv_exchange_rate, 1)          AS exchange_rate,
                   ROUND(d.LINE_AMOUNT * NVL(d.inv_exchange_rate, 1), 4)
                                                        AS base_line_amount,
                   ROUND(d.line_tax_amount * NVL(d.inv_exchange_rate, 1), 4)
                                                        AS base_tax_amount,
                   ROUND(d.inv_total_amount * NVL(d.inv_exchange_rate, 1), 4)
                                                        AS base_total_amount,
                   NVL(fis.FISCAL_YEAR, EXTRACT(YEAR FROM d.INVOICE_DATE))
                                                        AS fiscal_year,
                   NVL(fis.PERIOD_NUMBER, EXTRACT(MONTH FROM d.INVOICE_DATE))
                                                        AS fiscal_period,
                   (SELECT COUNT(*)
                      FROM FIN.PAYMENT_ALLOCATIONS pa2
                     WHERE pa2.INVOICE_ID = d.INVOICE_ID
                       AND pa2.STATUS = 'APPLIED')      AS payment_count,
                   GREATEST(TRUNC(SYSDATE) - d.DUE_DATE, 0)
                                                        AS days_overdue,
                   CASE
                       WHEN TRUNC(SYSDATE) <= d.DUE_DATE        THEN 'CURRENT'
                       WHEN TRUNC(SYSDATE) - d.DUE_DATE <= 30   THEN '1-30'
                       WHEN TRUNC(SYSDATE) - d.DUE_DATE <= 60   THEN '31-60'
                       WHEN TRUNC(SYSDATE) - d.DUE_DATE <= 90   THEN '61-90'
                       WHEN TRUNC(SYSDATE) - d.DUE_DATE <= 120  THEN '91-120'
                       ELSE '120+'
                   END                                  AS aging_bucket,
                   (SELECT jr.journal_ref
                      FROM cte_journal_refs jr
                     WHERE jr.ref_invoice_id = d.INVOICE_ID
                       AND jr.rn = 1)                   AS journal_ref,
                   NVL(cctr.CC_NAME, 'UNASSIGNED')     AS cost_center_name,
                   NVL(cn.credit_note_total, 0)        AS credit_note_amount,
                   NVL(rb.customer_running_balance, 0)  AS running_balance,
                   d.row_seq,
                   d.inv_status                         AS status
              FROM cte_dedup d
              LEFT JOIN cte_customer_sk csk
                ON csk.CUSTOMER_ID = d.CUSTOMER_ID
              LEFT JOIN cte_account_sk ask_lu
                ON ask_lu.ACCOUNT_ID = d.ACCOUNT_ID
              LEFT JOIN cte_date_dim ddt
                ON ddt.FULL_DATE = TRUNC(d.INVOICE_DATE)
              LEFT JOIN cte_fiscal fis
                ON fis.FISCAL_PERIOD_ID = d.FISCAL_PERIOD_ID
              LEFT JOIN cte_payment_alloc pa
                ON pa.INVOICE_ID = d.INVOICE_ID
              LEFT JOIN cte_tax_detail tx
                ON tx.TAX_CODE_ID = d.TAX_CODE_ID
              LEFT JOIN cte_fx_rate fx
                ON fx.CURRENCY_ID = d.CURRENCY_ID
              LEFT JOIN cte_credit_notes cn
                ON cn.original_invoice_id = d.INVOICE_ID
              LEFT JOIN cte_cost_centers cctr
                ON cctr.COST_CENTER_ID = d.COST_CENTER_ID
              LEFT JOIN cte_running_balance rb
                ON rb.INVOICE_ID = d.INVOICE_ID
             WHERE d.row_seq = 1
               AND NOT EXISTS (
                       SELECT 1
                         FROM DWH.FACT_INVOICES fi_exist
                        WHERE fi_exist.INVOICE_ID = d.INVOICE_ID
                          AND fi_exist.INVOICE_NUM = d.INVOICE_NUM
                          AND p_mode = 'INCREMENTAL'
                   )
             ORDER BY d.INVOICE_DATE, d.INVOICE_ID, d.LINE_NUM;

        LOOP
            FETCH v_cur BULK COLLECT INTO v_buffer LIMIT gc_batch_limit;
            EXIT WHEN v_buffer.COUNT = 0;

            v_inv_sk_arr.DELETE;   v_date_arr.DELETE;
            v_cust_sk_arr.DELETE;  v_acct_sk_arr.DELETE;
            v_inv_id_arr.DELETE;   v_inv_num_arr.DELETE;
            v_inv_type_arr.DELETE; v_inv_date_arr.DELETE;
            v_due_date_arr.DELETE; v_line_num_arr.DELETE;
            v_line_desc_arr.DELETE; v_item_arr.DELETE;
            v_qty_arr.DELETE;      v_uprice_arr.DELETE;
            v_disc_arr.DELETE;     v_line_amt_arr.DELETE;
            v_tax_code_arr.DELETE; v_tax_rate_arr.DELETE;
            v_tax_amt_arr.DELETE;  v_total_arr.DELETE;
            v_paid_arr.DELETE;     v_outst_arr.DELETE;
            v_curr_arr.DELETE;     v_xrate_arr.DELETE;
            v_bline_arr.DELETE;    v_btax_arr.DELETE;
            v_btotal_arr.DELETE;   v_fyear_arr.DELETE;
            v_fperiod_arr.DELETE;  v_paycnt_arr.DELETE;
            v_dayso_arr.DELETE;    v_aging_arr.DELETE;
            v_jref_arr.DELETE;     v_ccname_arr.DELETE;
            v_cnamt_arr.DELETE;    v_runbal_arr.DELETE;
            v_rowseq_arr.DELETE;   v_status_arr.DELETE;

            FOR i IN 1 .. v_buffer.COUNT LOOP
                v_inv_sk_arr(i)   := v_buffer(i).invoice_sk;
                v_date_arr(i)     := v_buffer(i).date_id;
                v_cust_sk_arr(i)  := v_buffer(i).customer_sk;
                v_acct_sk_arr(i)  := v_buffer(i).account_sk;
                v_inv_id_arr(i)   := v_buffer(i).invoice_id;
                v_inv_num_arr(i)  := v_buffer(i).invoice_num;
                v_inv_type_arr(i) := v_buffer(i).invoice_type;
                v_inv_date_arr(i) := v_buffer(i).invoice_date;
                v_due_date_arr(i) := v_buffer(i).due_date;
                v_line_num_arr(i) := v_buffer(i).line_num;
                v_line_desc_arr(i):= v_buffer(i).line_description;
                v_item_arr(i)     := v_buffer(i).item_code;
                v_qty_arr(i)      := v_buffer(i).quantity;
                v_uprice_arr(i)   := v_buffer(i).unit_price;
                v_disc_arr(i)     := v_buffer(i).discount_pct;
                v_line_amt_arr(i) := v_buffer(i).line_amount;
                v_tax_code_arr(i) := v_buffer(i).tax_code;
                v_tax_rate_arr(i) := v_buffer(i).tax_rate;
                v_tax_amt_arr(i)  := v_buffer(i).tax_amount;
                v_total_arr(i)    := v_buffer(i).total_amount;
                v_paid_arr(i)     := v_buffer(i).paid_amount;
                v_outst_arr(i)    := v_buffer(i).outstanding_amount;
                v_curr_arr(i)     := v_buffer(i).currency_code;
                v_xrate_arr(i)    := v_buffer(i).exchange_rate;
                v_bline_arr(i)    := v_buffer(i).base_line_amount;
                v_btax_arr(i)     := v_buffer(i).base_tax_amount;
                v_btotal_arr(i)   := v_buffer(i).base_total_amount;
                v_fyear_arr(i)    := v_buffer(i).fiscal_year;
                v_fperiod_arr(i)  := v_buffer(i).fiscal_period;
                v_paycnt_arr(i)   := v_buffer(i).payment_count;
                v_dayso_arr(i)    := v_buffer(i).days_overdue;
                v_aging_arr(i)    := v_buffer(i).aging_bucket;
                v_jref_arr(i)     := v_buffer(i).journal_ref;
                v_ccname_arr(i)   := v_buffer(i).cost_center_name;
                v_cnamt_arr(i)    := v_buffer(i).credit_note_amount;
                v_runbal_arr(i)   := v_buffer(i).running_balance;
                v_rowseq_arr(i)   := v_buffer(i).row_seq;
                v_status_arr(i)   := v_buffer(i).status;
            END LOOP;

            BEGIN
                FORALL i IN VALUES OF v_inv_sk_arr SAVE EXCEPTIONS
                    INSERT /*+ APPEND PARALLEL(8) */
                      INTO DWH.FACT_INVOICES_STG (
                               invoice_sk, date_id, customer_sk, account_sk,
                               invoice_id, invoice_num, invoice_type,
                               invoice_date, due_date, line_num,
                               line_description, item_code, quantity,
                               unit_price, discount_pct, line_amount,
                               tax_code, tax_rate, tax_amount,
                               total_amount, paid_amount, outstanding_amount,
                               currency_code, exchange_rate,
                               base_line_amount, base_tax_amount, base_total_amount,
                               fiscal_year, fiscal_period, payment_count,
                               days_overdue, aging_bucket, journal_ref,
                               cost_center_name, credit_note_amount,
                               running_balance, row_seq, status,
                               created_date
                           )
                    VALUES (
                               v_inv_sk_arr(i), v_date_arr(i), v_cust_sk_arr(i), v_acct_sk_arr(i),
                               v_inv_id_arr(i), v_inv_num_arr(i), v_inv_type_arr(i),
                               v_inv_date_arr(i), v_due_date_arr(i), v_line_num_arr(i),
                               v_line_desc_arr(i), v_item_arr(i), v_qty_arr(i),
                               v_uprice_arr(i), v_disc_arr(i), v_line_amt_arr(i),
                               v_tax_code_arr(i), v_tax_rate_arr(i), v_tax_amt_arr(i),
                               v_total_arr(i), v_paid_arr(i), v_outst_arr(i),
                               v_curr_arr(i), v_xrate_arr(i),
                               v_bline_arr(i), v_btax_arr(i), v_btotal_arr(i),
                               v_fyear_arr(i), v_fperiod_arr(i), v_paycnt_arr(i),
                               v_dayso_arr(i), v_aging_arr(i), v_jref_arr(i),
                               v_ccname_arr(i), v_cnamt_arr(i),
                               v_runbal_arr(i), v_rowseq_arr(i), v_status_arr(i),
                               SYSDATE
                           );
            EXCEPTION
                WHEN OTHERS THEN
                    IF SQLCODE = -24381 THEN
                        v_error_count := v_error_count + SQL%BULK_EXCEPTIONS.COUNT;
                        handle_forall_exceptions(v_step, SQL%BULK_EXCEPTIONS.COUNT);
                    ELSE
                        RAISE;
                    END IF;
            END;

            v_total_rows := v_total_rows + v_buffer.COUNT;
            COMMIT;
        END LOOP;

        CLOSE v_cur;

        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_INVOICES tgt
        USING (
            WITH cte_stg_ranked AS (
                SELECT s.*,
                       ROW_NUMBER() OVER (
                           PARTITION BY s.invoice_id, s.line_num
                           ORDER BY s.invoice_sk DESC
                       ) AS merge_rn
                  FROM DWH.FACT_INVOICES_STG s
                 WHERE s.created_date >= TRUNC(SYSDATE)
            ),
            cte_stg_valid AS (
                SELECT r.*
                  FROM cte_stg_ranked r
                 WHERE r.merge_rn = 1
            ),
            cte_stg_enriched AS (
                SELECT v.*,
                       (SELECT MAX(pa3.ALLOCATION_DATE)
                          FROM FIN.PAYMENT_ALLOCATIONS pa3
                         WHERE pa3.INVOICE_ID = v.invoice_id
                           AND pa3.STATUS = 'APPLIED') AS last_payment_date,
                       (SELECT NVL(SUM(pa4.DISCOUNT_TAKEN), 0)
                          FROM FIN.PAYMENT_ALLOCATIONS pa4
                         WHERE pa4.INVOICE_ID = v.invoice_id) AS total_discount_taken,
                       LAG(v.total_amount) OVER (
                           PARTITION BY v.customer_sk
                           ORDER BY v.invoice_date, v.invoice_id
                       ) AS prev_invoice_amount,
                       LEAD(v.invoice_date) OVER (
                           PARTITION BY v.customer_sk
                           ORDER BY v.invoice_date, v.invoice_id
                       ) AS next_invoice_date,
                       SUM(v.line_amount) OVER (
                           PARTITION BY v.fiscal_year, v.fiscal_period
                       ) AS period_total_line_amount,
                       RANK() OVER (
                           PARTITION BY v.fiscal_year, v.fiscal_period
                           ORDER BY v.total_amount DESC
                       ) AS amount_rank_in_period
                  FROM cte_stg_valid v
            )
            SELECT e.invoice_sk, e.date_id, e.customer_sk, e.account_sk,
                   e.invoice_id, e.invoice_num, e.invoice_type,
                   e.invoice_date, e.due_date, e.line_num,
                   e.line_amount, e.tax_amount, e.total_amount,
                   e.paid_amount, e.outstanding_amount,
                   e.currency_code, e.exchange_rate,
                   e.base_line_amount, e.base_tax_amount, e.base_total_amount,
                   e.fiscal_year, e.fiscal_period,
                   e.payment_count, e.days_overdue, e.aging_bucket,
                   e.status
              FROM cte_stg_enriched e
        ) src
        ON (tgt.INVOICE_ID = src.invoice_id AND tgt.LINE_NUM = src.line_num)
        WHEN MATCHED THEN
            UPDATE SET
                tgt.PAID_AMOUNT         = src.paid_amount,
                tgt.DAYS_OVERDUE        = src.days_overdue,
                tgt.AGING_BUCKET        = src.aging_bucket,
                tgt.STATUS              = src.status,
                tgt.UPDATED_DATE        = SYSDATE
             WHERE tgt.PAID_AMOUNT    <> src.paid_amount
                OR tgt.STATUS         <> src.status
        WHEN NOT MATCHED THEN
            INSERT (
                INVOICE_SK, DATE_ID, CUSTOMER_SK, ACCOUNT_SK,
                INVOICE_ID, INVOICE_NUM,
                LINE_AMOUNT, TAX_AMOUNT, TOTAL_AMOUNT,
                PAID_AMOUNT,
                STATUS, CREATED_DATE
            )
            VALUES (
                src.invoice_sk, src.date_id, src.customer_sk, src.account_sk,
                src.invoice_id, src.invoice_num,
                src.line_amount, src.tax_amount, src.total_amount,
                src.paid_amount,
                src.status, SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;

        log_step(v_step || '.MERGE', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        UPDATE /*+ PARALLEL(8) */ DWH.FACT_INVOICES tgt
           SET tgt.WEIGHTED_OUTSTANDING = (
                   SELECT NVL(SUM(
                              CASE
                                  WHEN fi2.AGING_BUCKET = 'CURRENT'  THEN fi2.OUTSTANDING_AMOUNT * 1.00
                                  WHEN fi2.AGING_BUCKET = '1-30'     THEN fi2.OUTSTANDING_AMOUNT * 0.98
                                  WHEN fi2.AGING_BUCKET = '31-60'    THEN fi2.OUTSTANDING_AMOUNT * 0.95
                                  WHEN fi2.AGING_BUCKET = '61-90'    THEN fi2.OUTSTANDING_AMOUNT * 0.85
                                  WHEN fi2.AGING_BUCKET = '91-120'   THEN fi2.OUTSTANDING_AMOUNT * 0.70
                                  ELSE fi2.OUTSTANDING_AMOUNT * 0.50
                              END
                          ), 0)
                     FROM DWH.FACT_INVOICES fi2
                    WHERE fi2.CUSTOMER_SK = tgt.CUSTOMER_SK
                      AND fi2.INVOICE_ID  = tgt.INVOICE_ID
               ),
               tgt.CUSTOMER_TOTAL_OUTSTANDING = (
                   SELECT NVL(SUM(fi3.OUTSTANDING_AMOUNT), 0)
                     FROM DWH.FACT_INVOICES fi3
                    WHERE fi3.CUSTOMER_SK = tgt.CUSTOMER_SK
                      AND fi3.STATUS NOT IN ('CANCELLED', 'VOID')
               ),
               tgt.PERIOD_INVOICE_COUNT = (
                   SELECT COUNT(DISTINCT fi4.INVOICE_ID)
                     FROM DWH.FACT_INVOICES fi4
                    WHERE fi4.FISCAL_YEAR   = tgt.FISCAL_YEAR
                      AND fi4.FISCAL_PERIOD = tgt.FISCAL_PERIOD
                      AND fi4.CUSTOMER_SK   = tgt.CUSTOMER_SK
               ),
               tgt.UPDATED_DATE = SYSDATE
         WHERE tgt.INVOICE_ID IN (
                   SELECT DISTINCT s2.invoice_id
                     FROM DWH.FACT_INVOICES_STG s2
                    WHERE s2.created_date >= TRUNC(SYSDATE)
               );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.POST_UPDATE_WEIGHTS', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        INSERT /*+ PARALLEL(8) */
          INTO DWH.FACT_INVOICE_DAILY_SNAPSHOT (
                   snapshot_date, fiscal_year, fiscal_period,
                   total_invoiced, total_paid, total_outstanding,
                   total_overdue, total_current,
                   invoice_count, overdue_count,
                   avg_days_overdue, max_days_overdue,
                   weighted_outstanding, credit_note_total,
                   currency_code, created_date
               )
        WITH cte_snapshot_base AS (
            SELECT fi.FISCAL_YEAR,
                   fi.FISCAL_PERIOD,
                   fi.INVOICE_ID,
                   fi.TOTAL_AMOUNT,
                   fi.PAID_AMOUNT,
                   fi.OUTSTANDING_AMOUNT,
                   fi.DAYS_OVERDUE,
                   fi.AGING_BUCKET,
                   fi.CURRENCY_CODE,
                   fi.STATUS,
                   ROW_NUMBER() OVER (
                       PARTITION BY fi.INVOICE_ID
                       ORDER BY fi.INVOICE_SK DESC
                   ) AS snap_rn
              FROM DWH.FACT_INVOICES fi
             WHERE fi.STATUS NOT IN ('CANCELLED', 'VOID')
        ),
        cte_snapshot_dedup AS (
            SELECT sb.*
              FROM cte_snapshot_base sb
             WHERE sb.snap_rn = 1
        ),
        cte_credit_snap AS (
            SELECT cn.FISCAL_YEAR,
                   cn.FISCAL_PERIOD,
                   SUM(cn.TOTAL_AMOUNT) AS cn_total
              FROM DWH.FACT_INVOICES cn
             WHERE cn.INVOICE_TYPE = 'CREDIT_NOTE'
             GROUP BY cn.FISCAL_YEAR, cn.FISCAL_PERIOD
        ),
        cte_weighted AS (
            SELECT sd.FISCAL_YEAR,
                   sd.FISCAL_PERIOD,
                   SUM(
                       CASE
                           WHEN sd.AGING_BUCKET = 'CURRENT'  THEN sd.OUTSTANDING_AMOUNT * 1.00
                           WHEN sd.AGING_BUCKET = '1-30'     THEN sd.OUTSTANDING_AMOUNT * 0.98
                           WHEN sd.AGING_BUCKET = '31-60'    THEN sd.OUTSTANDING_AMOUNT * 0.95
                           WHEN sd.AGING_BUCKET = '61-90'    THEN sd.OUTSTANDING_AMOUNT * 0.85
                           WHEN sd.AGING_BUCKET = '91-120'   THEN sd.OUTSTANDING_AMOUNT * 0.70
                           ELSE sd.OUTSTANDING_AMOUNT * 0.50
                       END
                   ) AS weighted_out
              FROM cte_snapshot_dedup sd
             GROUP BY sd.FISCAL_YEAR, sd.FISCAL_PERIOD
        )
        SELECT TRUNC(SYSDATE),
               sd.FISCAL_YEAR,
               sd.FISCAL_PERIOD,
               SUM(sd.TOTAL_AMOUNT),
               SUM(sd.PAID_AMOUNT),
               SUM(sd.OUTSTANDING_AMOUNT),
               SUM(CASE WHEN sd.DAYS_OVERDUE > 0 THEN sd.OUTSTANDING_AMOUNT ELSE 0 END),
               SUM(CASE WHEN sd.DAYS_OVERDUE <= 0 THEN sd.OUTSTANDING_AMOUNT ELSE 0 END),
               COUNT(DISTINCT sd.INVOICE_ID),
               COUNT(DISTINCT CASE WHEN sd.DAYS_OVERDUE > 0 THEN sd.INVOICE_ID END),
               AVG(sd.DAYS_OVERDUE),
               MAX(sd.DAYS_OVERDUE),
               NVL(w.weighted_out, 0),
               NVL(cs.cn_total, 0),
               NVL(sd.CURRENCY_CODE, v_func_curr),
               SYSDATE
          FROM cte_snapshot_dedup sd
          LEFT JOIN cte_weighted w
            ON w.FISCAL_YEAR   = sd.FISCAL_YEAR
           AND w.FISCAL_PERIOD = sd.FISCAL_PERIOD
          LEFT JOIN cte_credit_snap cs
            ON cs.FISCAL_YEAR   = sd.FISCAL_YEAR
           AND cs.FISCAL_PERIOD = sd.FISCAL_PERIOD
         GROUP BY sd.FISCAL_YEAR, sd.FISCAL_PERIOD,
                  sd.CURRENCY_CODE, w.weighted_out, cs.cn_total;

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.DAILY_SNAPSHOT', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_INVOICE_CUSTOMER_SUMMARY tgt
        USING (
            WITH cte_cust_invoices AS (
                SELECT fi.CUSTOMER_SK,
                       fi.FISCAL_YEAR,
                       fi.INVOICE_ID,
                       fi.TOTAL_AMOUNT,
                       fi.PAID_AMOUNT,
                       fi.OUTSTANDING_AMOUNT,
                       fi.DAYS_OVERDUE,
                       fi.AGING_BUCKET,
                       fi.INVOICE_DATE,
                       fi.DUE_DATE,
                       fi.STATUS,
                       ROW_NUMBER() OVER (
                           PARTITION BY fi.CUSTOMER_SK, fi.INVOICE_ID
                           ORDER BY fi.INVOICE_SK DESC
                       ) AS cust_rn
                  FROM DWH.FACT_INVOICES fi
                 WHERE fi.STATUS NOT IN ('CANCELLED', 'VOID')
            ),
            cte_cust_dedup AS (
                SELECT ci.*
                  FROM cte_cust_invoices ci
                 WHERE ci.cust_rn = 1
            ),
            cte_cust_payments AS (
                SELECT fp.CUSTOMER_SK,
                       COUNT(DISTINCT fp.PAYMENT_ID) AS payment_count,
                       AVG(fp.PAYMENT_DATE - (
                           SELECT inv5.DUE_DATE
                             FROM FIN.INVOICES inv5
                            WHERE inv5.INVOICE_ID = fp.INVOICE_ID
                       )) AS avg_days_to_pay
                  FROM DWH.FACT_PAYMENTS fp
                 WHERE fp.STATUS IN ('COMPLETED', 'RECONCILED')
                 GROUP BY fp.CUSTOMER_SK
            ),
            cte_cust_summary AS (
                SELECT cd.CUSTOMER_SK,
                       cd.FISCAL_YEAR,
                       COUNT(DISTINCT cd.INVOICE_ID)     AS invoice_count,
                       SUM(cd.TOTAL_AMOUNT)              AS total_invoiced,
                       SUM(cd.PAID_AMOUNT)               AS total_paid,
                       SUM(cd.OUTSTANDING_AMOUNT)        AS total_outstanding,
                       AVG(cd.DAYS_OVERDUE)              AS avg_days_overdue,
                       MAX(cd.DAYS_OVERDUE)              AS max_days_overdue,
                       MIN(cd.INVOICE_DATE)              AS first_invoice_date,
                       MAX(cd.INVOICE_DATE)              AS last_invoice_date,
                       SUM(CASE WHEN cd.AGING_BUCKET IN ('91-120', '120+')
                                THEN cd.OUTSTANDING_AMOUNT ELSE 0 END)
                                                          AS high_risk_amount,
                       NVL(cp.payment_count, 0)          AS payment_count,
                       NVL(cp.avg_days_to_pay, 0)        AS avg_days_to_pay,
                       RANK() OVER (
                           PARTITION BY cd.FISCAL_YEAR
                           ORDER BY SUM(cd.TOTAL_AMOUNT) DESC
                       ) AS revenue_rank
                  FROM cte_cust_dedup cd
                  LEFT JOIN cte_cust_payments cp
                    ON cp.CUSTOMER_SK = cd.CUSTOMER_SK
                 GROUP BY cd.CUSTOMER_SK, cd.FISCAL_YEAR,
                          cp.payment_count, cp.avg_days_to_pay
            )
            SELECT cs.CUSTOMER_SK, cs.FISCAL_YEAR,
                   cs.invoice_count, cs.total_invoiced,
                   cs.total_paid, cs.total_outstanding,
                   cs.avg_days_overdue, cs.max_days_overdue,
                   cs.first_invoice_date, cs.last_invoice_date,
                   cs.high_risk_amount,
                   cs.payment_count, cs.avg_days_to_pay,
                   cs.revenue_rank
              FROM cte_cust_summary cs
        ) src
        ON (tgt.CUSTOMER_SK = src.CUSTOMER_SK AND tgt.FISCAL_YEAR = src.FISCAL_YEAR)
        WHEN MATCHED THEN
            UPDATE SET
                tgt.INVOICE_COUNT       = src.invoice_count,
                tgt.TOTAL_INVOICED      = src.total_invoiced,
                tgt.TOTAL_PAID          = src.total_paid,
                tgt.TOTAL_OUTSTANDING   = src.total_outstanding,
                tgt.AVG_DAYS_OVERDUE    = src.avg_days_overdue,
                tgt.MAX_DAYS_OVERDUE    = src.max_days_overdue,
                tgt.HIGH_RISK_AMOUNT    = src.high_risk_amount,
                tgt.PAYMENT_COUNT       = src.payment_count,
                tgt.AVG_DAYS_TO_PAY     = src.avg_days_to_pay,
                tgt.REVENUE_RANK        = src.revenue_rank,
                tgt.UPDATED_DATE        = SYSDATE
             WHERE tgt.TOTAL_OUTSTANDING <> src.total_outstanding
                OR tgt.TOTAL_PAID        <> src.total_paid
        WHEN NOT MATCHED THEN
            INSERT (
                CUSTOMER_SK, FISCAL_YEAR,
                INVOICE_COUNT, TOTAL_INVOICED,
                TOTAL_PAID, TOTAL_OUTSTANDING,
                AVG_DAYS_OVERDUE, MAX_DAYS_OVERDUE,
                FIRST_INVOICE_DATE, LAST_INVOICE_DATE,
                HIGH_RISK_AMOUNT,
                PAYMENT_COUNT, AVG_DAYS_TO_PAY,
                REVENUE_RANK, CREATED_DATE
            )
            VALUES (
                src.CUSTOMER_SK, src.FISCAL_YEAR,
                src.invoice_count, src.total_invoiced,
                src.total_paid, src.total_outstanding,
                src.avg_days_overdue, src.max_days_overdue,
                src.first_invoice_date, src.last_invoice_date,
                src.high_risk_amount,
                src.payment_count, src.avg_days_to_pay,
                src.revenue_rank, SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.CUSTOMER_SUMMARY', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_INVOICE_PERIOD_SUMMARY tgt
        USING (
            WITH cte_period_base AS (
                SELECT fi.FISCAL_YEAR,
                       fi.FISCAL_PERIOD,
                       fi.INVOICE_ID,
                       fi.CUSTOMER_SK,
                       fi.TOTAL_AMOUNT,
                       fi.PAID_AMOUNT,
                       fi.OUTSTANDING_AMOUNT,
                       fi.LINE_AMOUNT,
                       fi.TAX_AMOUNT,
                       fi.DAYS_OVERDUE,
                       fi.STATUS,
                       ROW_NUMBER() OVER (
                           PARTITION BY fi.INVOICE_ID
                           ORDER BY fi.INVOICE_SK DESC
                       ) AS prn
                  FROM DWH.FACT_INVOICES fi
                 WHERE fi.STATUS NOT IN ('CANCELLED', 'VOID')
            ),
            cte_period_dedup AS (
                SELECT pb.*
                  FROM cte_period_base pb
                 WHERE pb.prn = 1
            ),
            cte_prior_period AS (
                SELECT fi2.FISCAL_YEAR,
                       fi2.FISCAL_PERIOD,
                       SUM(fi2.TOTAL_AMOUNT) AS prior_total
                  FROM DWH.FACT_INVOICES fi2
                 WHERE fi2.STATUS NOT IN ('CANCELLED', 'VOID')
                 GROUP BY fi2.FISCAL_YEAR, fi2.FISCAL_PERIOD
            ),
            cte_period_agg AS (
                SELECT pd.FISCAL_YEAR,
                       pd.FISCAL_PERIOD,
                       COUNT(DISTINCT pd.INVOICE_ID)      AS invoice_count,
                       COUNT(DISTINCT pd.CUSTOMER_SK)     AS customer_count,
                       SUM(pd.TOTAL_AMOUNT)               AS total_invoiced,
                       SUM(pd.LINE_AMOUNT)                AS total_line_amount,
                       SUM(pd.TAX_AMOUNT)                 AS total_tax_amount,
                       SUM(pd.PAID_AMOUNT)                AS total_paid,
                       SUM(pd.OUTSTANDING_AMOUNT)         AS total_outstanding,
                       AVG(pd.TOTAL_AMOUNT)               AS avg_invoice_amount,
                       MAX(pd.TOTAL_AMOUNT)               AS max_invoice_amount,
                       MIN(pd.TOTAL_AMOUNT)               AS min_invoice_amount,
                       PERCENTILE_CONT(0.5) WITHIN GROUP (
                           ORDER BY pd.TOTAL_AMOUNT
                       )                                   AS median_invoice_amount,
                       AVG(pd.DAYS_OVERDUE)               AS avg_days_overdue,
                       SUM(CASE WHEN pd.DAYS_OVERDUE > 90
                                THEN pd.OUTSTANDING_AMOUNT ELSE 0 END)
                                                           AS high_risk_outstanding,
                       LAG(SUM(pd.TOTAL_AMOUNT)) OVER (
                           PARTITION BY pd.FISCAL_YEAR
                           ORDER BY pd.FISCAL_PERIOD
                       ) AS prev_period_total
                  FROM cte_period_dedup pd
                 GROUP BY pd.FISCAL_YEAR, pd.FISCAL_PERIOD
            )
            SELECT pa.FISCAL_YEAR, pa.FISCAL_PERIOD,
                   pa.invoice_count, pa.customer_count,
                   pa.total_invoiced, pa.total_line_amount,
                   pa.total_tax_amount, pa.total_paid,
                   pa.total_outstanding, pa.avg_invoice_amount,
                   pa.max_invoice_amount, pa.min_invoice_amount,
                   pa.median_invoice_amount, pa.avg_days_overdue,
                   pa.high_risk_outstanding,
                   NVL(pa.prev_period_total, 0) AS prev_period_total,
                   CASE
                       WHEN NVL(pa.prev_period_total, 0) > 0
                       THEN ROUND(
                           (pa.total_invoiced - NVL(pa.prev_period_total, 0))
                           / ABS(pa.prev_period_total) * 100, 2
                       )
                       ELSE 0
                   END AS period_growth_pct
              FROM cte_period_agg pa
        ) src
        ON (tgt.FISCAL_YEAR = src.FISCAL_YEAR AND tgt.FISCAL_PERIOD = src.FISCAL_PERIOD)
        WHEN MATCHED THEN
            UPDATE SET
                tgt.INVOICE_COUNT        = src.invoice_count,
                tgt.CUSTOMER_COUNT       = src.customer_count,
                tgt.TOTAL_INVOICED       = src.total_invoiced,
                tgt.TOTAL_PAID           = src.total_paid,
                tgt.TOTAL_OUTSTANDING    = src.total_outstanding,
                tgt.AVG_INVOICE_AMOUNT   = src.avg_invoice_amount,
                tgt.AVG_DAYS_OVERDUE     = src.avg_days_overdue,
                tgt.HIGH_RISK_OUTSTANDING = src.high_risk_outstanding,
                tgt.PERIOD_GROWTH_PCT    = src.period_growth_pct,
                tgt.UPDATED_DATE         = SYSDATE
             WHERE tgt.TOTAL_INVOICED <> src.total_invoiced
                OR tgt.TOTAL_PAID     <> src.total_paid
        WHEN NOT MATCHED THEN
            INSERT (
                FISCAL_YEAR, FISCAL_PERIOD,
                INVOICE_COUNT, CUSTOMER_COUNT,
                TOTAL_INVOICED, TOTAL_LINE_AMOUNT,
                TOTAL_TAX_AMOUNT, TOTAL_PAID,
                TOTAL_OUTSTANDING, AVG_INVOICE_AMOUNT,
                MAX_INVOICE_AMOUNT, MIN_INVOICE_AMOUNT,
                MEDIAN_INVOICE_AMOUNT, AVG_DAYS_OVERDUE,
                HIGH_RISK_OUTSTANDING,
                PREV_PERIOD_TOTAL, PERIOD_GROWTH_PCT,
                CREATED_DATE
            )
            VALUES (
                src.FISCAL_YEAR, src.FISCAL_PERIOD,
                src.invoice_count, src.customer_count,
                src.total_invoiced, src.total_line_amount,
                src.total_tax_amount, src.total_paid,
                src.total_outstanding, src.avg_invoice_amount,
                src.max_invoice_amount, src.min_invoice_amount,
                src.median_invoice_amount, src.avg_days_overdue,
                src.high_risk_outstanding,
                src.prev_period_total, src.period_growth_pct,
                SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.PERIOD_SUMMARY', 'DONE', g_row_count);

        log_step(v_step || '.COMPLETE', 'SUCCESS', v_total_rows);

    EXCEPTION
        WHEN OTHERS THEN
            IF v_cur%ISOPEN THEN CLOSE v_cur; END IF;
            ROLLBACK;
            log_step(v_step || '.FATAL', 'ERROR', 0, SQLERRM);
            RAISE;
    END load_fact_invoices;

    -- =========================================================================
    -- 2. LOAD_FACT_PAYMENTS
    -- =========================================================================
    PROCEDURE load_fact_payments (
        p_from_date  IN DATE,
        p_to_date    IN DATE,
        p_mode       IN VARCHAR2 DEFAULT 'INCREMENTAL'
    ) IS
        v_step          VARCHAR2(200) := 'LOAD_FACT_PAYMENTS';
        v_cur           t_pay_ref_cursor;
        v_buffer        t_payment_stg_tab;
        v_total_rows    NUMBER(19) := 0;
        v_error_count   PLS_INTEGER := 0;
        v_func_curr     VARCHAR2(3);

        v_pay_sk_arr     t_id_array;
        v_date_arr       t_id_array;
        v_cust_sk_arr    t_id_array;
        v_acct_sk_arr    t_id_array;
        v_pay_id_arr     t_id_array;
        v_pay_num_arr    t_varchar_array;
        v_pay_type_arr   t_varchar_array;
        v_pay_date_arr   t_date_array;
        v_bank_id_arr    t_id_array;
        v_bank_nm_arr    t_varchar_array;
        v_bank_cur_arr   t_varchar_array;
        v_inv_id_arr     t_id_array;
        v_inv_num_arr    t_varchar_array;
        v_alloc_amt_arr  t_amount_array;
        v_disc_arr       t_amount_array;
        v_wo_arr         t_amount_array;
        v_pay_amt_arr    t_amount_array;
        v_unalloc_arr    t_amount_array;
        v_curr_arr       t_varchar_array;
        v_xrate_arr      t_amount_array;
        v_base_arr       t_amount_array;
        v_inv_xr_arr     t_amount_array;
        v_fxgl_arr       t_amount_array;
        v_fyear_arr      t_id_array;
        v_fperiod_arr    t_id_array;
        v_method_arr     t_varchar_array;
        v_ref_arr        t_varchar_array;
        v_clear_dt_arr   t_date_array;
        v_clr_stat_arr   t_varchar_array;
        v_status_arr     t_varchar_array;
    BEGIN
        init_run(v_step);
        v_func_curr := get_functional_currency;

        IF p_mode = 'FULL' THEN
            EXECUTE IMMEDIATE 'TRUNCATE TABLE DWH.FACT_PAYMENTS_STG';
            g_step_start := SYSTIMESTAMP;
            log_step(v_step || '.TRUNCATE', 'DONE');
        END IF;

        g_step_start := SYSTIMESTAMP;

        OPEN v_cur FOR
            WITH cte_payments AS (
                SELECT /*+ PARALLEL(p, 8) */
                       p.PAYMENT_ID,
                       p.PAYMENT_NUM,
                       p.PAYMENT_TYPE,
                       p.PAYMENT_DATE,
                       p.BANK_ACCOUNT_ID,
                       p.CURRENCY_ID,
                       p.EXCHANGE_RATE          AS pay_exchange_rate,
                       p.TOTAL_AMOUNT           AS pay_total_amount,
                       p.ALLOCATED_AMOUNT       AS pay_allocated_amount,
                       p.FISCAL_PERIOD_ID,
                       p.STATUS                 AS pay_status,
                       p.PAYMENT_METHOD,
                       p.REFERENCE_NUM,
                       p.CLEARED_DATE
                  FROM FIN.PAYMENTS p
                 WHERE p.PAYMENT_DATE BETWEEN p_from_date AND p_to_date
                   AND p.STATUS IN ('COMPLETED', 'RECONCILED', 'CLEARED')
            ),
            cte_bank_accounts AS (
                SELECT ba.BANK_ACCOUNT_ID,
                       ba.ACCOUNT_NUMBER,
                       ba.ACCOUNT_NAME  AS bank_account_name,
                       ba.BANK_NAME,
                       ba.CURRENCY_ID   AS bank_currency_id,
                       ba.CURRENT_BALANCE,
                       (SELECT cur.CURRENCY_CODE
                          FROM FIN.CURRENCIES cur
                         WHERE cur.CURRENCY_ID = ba.CURRENCY_ID) AS bank_currency_code
                  FROM FIN.BANK_ACCOUNTS ba
            ),
            cte_allocations AS (
                SELECT pa.ALLOCATION_ID,
                       pa.PAYMENT_ID,
                       pa.INVOICE_ID,
                       pa.ALLOCATION_DATE,
                       pa.ALLOCATED_AMOUNT,
                       pa.DISCOUNT_TAKEN,
                       pa.WRITE_OFF_AMOUNT,
                       pa.STATUS AS alloc_status,
                       ROW_NUMBER() OVER (
                           PARTITION BY pa.PAYMENT_ID, pa.INVOICE_ID
                           ORDER BY pa.ALLOCATION_ID
                       ) AS alloc_rn
                  FROM FIN.PAYMENT_ALLOCATIONS pa
                 WHERE pa.PAYMENT_ID IN (
                           SELECT cp.PAYMENT_ID FROM cte_payments cp
                       )
            ),
            cte_invoice_match AS (
                SELECT inv.INVOICE_ID,
                       inv.INVOICE_NUM,
                       inv.CUSTOMER_ID,
                       inv.EXCHANGE_RATE AS inv_exchange_rate,
                       inv.CURRENCY_ID   AS inv_currency_id,
                       inv.TOTAL_AMOUNT  AS inv_total_amount,
                       inv.PAID_AMOUNT   AS inv_paid_amount,
                       inv.DUE_DATE,
                       (SELECT cust.CUSTOMER_NAME
                          FROM CRM.CUSTOMERS cust
                         WHERE cust.CUSTOMER_ID = inv.CUSTOMER_ID) AS customer_name,
                       (SELECT cust.CUSTOMER_CODE
                          FROM CRM.CUSTOMERS cust
                         WHERE cust.CUSTOMER_ID = inv.CUSTOMER_ID) AS customer_code
                  FROM FIN.INVOICES inv
                 WHERE inv.INVOICE_ID IN (
                           SELECT ca.INVOICE_ID FROM cte_allocations ca
                       )
            ),
            cte_customer_sk AS (
                SELECT dc.CUSTOMER_SK,
                       dc.CUSTOMER_ID
                  FROM DWH.DIM_CUSTOMER dc
                 WHERE dc.IS_CURRENT = 'Y'
            ),
            cte_account_sk AS (
                SELECT da.ACCOUNT_SK,
                       da.ACCOUNT_ID,
                       da.ACCOUNT_CODE
                  FROM DWH.DIM_ACCOUNT da
            ),
            cte_date_dim AS (
                SELECT dd.DATE_ID,
                       dd.FULL_DATE
                  FROM DWH.DIM_DATE dd
                 WHERE dd.FULL_DATE BETWEEN p_from_date AND p_to_date
            ),
            cte_fiscal AS (
                SELECT fp.FISCAL_PERIOD_ID,
                       fp.FISCAL_YEAR,
                       fp.PERIOD_NUMBER,
                       fp.STATUS AS period_status
                  FROM FIN.FISCAL_PERIODS fp
            ),
            cte_discount_agg AS (
                SELECT pa2.PAYMENT_ID,
                       SUM(NVL(pa2.DISCOUNT_TAKEN, 0)) AS total_discount,
                       COUNT(DISTINCT pa2.INVOICE_ID)   AS invoice_count
                  FROM FIN.PAYMENT_ALLOCATIONS pa2
                 WHERE pa2.PAYMENT_ID IN (
                           SELECT cp2.PAYMENT_ID FROM cte_payments cp2
                       )
                 GROUP BY pa2.PAYMENT_ID
            ),
            cte_writeoff_agg AS (
                SELECT pa3.PAYMENT_ID,
                       SUM(NVL(pa3.WRITE_OFF_AMOUNT, 0)) AS total_writeoff
                  FROM FIN.PAYMENT_ALLOCATIONS pa3
                 WHERE pa3.PAYMENT_ID IN (
                           SELECT cp3.PAYMENT_ID FROM cte_payments cp3
                       )
                 GROUP BY pa3.PAYMENT_ID
            ),
            cte_fx_rate AS (
                SELECT cur.CURRENCY_ID,
                       cur.CURRENCY_CODE,
                       cur.IS_FUNCTIONAL
                  FROM FIN.CURRENCIES cur
            ),
            cte_clearing AS (
                SELECT cp4.PAYMENT_ID,
                       CASE
                           WHEN cp4.CLEARED_DATE IS NOT NULL THEN 'CLEARED'
                           WHEN cp4.PAYMENT_DATE < TRUNC(SYSDATE) - 30 THEN 'STALE'
                           ELSE 'PENDING'
                       END AS clearing_status
                  FROM cte_payments cp4
            ),
            cte_method_dist AS (
                SELECT cp5.PAYMENT_METHOD,
                       COUNT(*)                 AS method_count,
                       SUM(cp5.pay_total_amount) AS method_total,
                       RANK() OVER (
                           ORDER BY SUM(cp5.pay_total_amount) DESC
                       ) AS method_rank
                  FROM cte_payments cp5
                 GROUP BY cp5.PAYMENT_METHOD
            ),
            cte_payment_dedup AS (
                SELECT cp6.PAYMENT_ID,
                       cp6.PAYMENT_NUM,
                       cp6.PAYMENT_TYPE,
                       cp6.PAYMENT_DATE,
                       cp6.BANK_ACCOUNT_ID,
                       cp6.CURRENCY_ID,
                       cp6.pay_exchange_rate,
                       cp6.pay_total_amount,
                       cp6.pay_allocated_amount,
                       cp6.FISCAL_PERIOD_ID,
                       cp6.pay_status,
                       cp6.PAYMENT_METHOD,
                       cp6.REFERENCE_NUM,
                       cp6.CLEARED_DATE,
                       a.INVOICE_ID,
                       a.ALLOCATED_AMOUNT    AS line_allocated,
                       a.DISCOUNT_TAKEN      AS line_discount,
                       a.WRITE_OFF_AMOUNT    AS line_writeoff,
                       ROW_NUMBER() OVER (
                           PARTITION BY cp6.PAYMENT_ID, a.INVOICE_ID
                           ORDER BY a.ALLOCATION_ID
                       ) AS dedup_rn
                  FROM cte_payments cp6
                  LEFT JOIN cte_allocations a
                    ON a.PAYMENT_ID = cp6.PAYMENT_ID
                   AND a.alloc_rn = 1
            )
            SELECT DWH.SEQ_DWH_PAYMENT.NEXTVAL           AS payment_sk,
                   NVL(ddt.DATE_ID, -1)                   AS date_id,
                   NVL(csk.CUSTOMER_SK, -1)                AS customer_sk,
                   NVL(ask_lu.ACCOUNT_SK, -1)              AS account_sk,
                   pd.PAYMENT_ID,
                   pd.PAYMENT_NUM,
                   pd.PAYMENT_TYPE,
                   pd.PAYMENT_DATE,
                   pd.BANK_ACCOUNT_ID,
                   NVL(bk.BANK_NAME, 'UNKNOWN')           AS bank_name,
                   NVL(bk.bank_currency_code, v_func_curr) AS bank_currency,
                   pd.INVOICE_ID,
                   NVL(im.INVOICE_NUM, 'N/A')             AS invoice_num,
                   NVL(pd.line_allocated, 0)               AS allocated_amount,
                   NVL(pd.line_discount, 0)                AS discount_taken,
                   NVL(pd.line_writeoff, 0)                AS write_off_amount,
                   pd.pay_total_amount                     AS payment_amount,
                   pd.pay_total_amount
                       - NVL(pd.pay_allocated_amount, 0)   AS unallocated_amount,
                   NVL(fx.CURRENCY_CODE, v_func_curr)      AS currency_code,
                   NVL(pd.pay_exchange_rate, 1)             AS exchange_rate,
                   ROUND(pd.pay_total_amount
                       * NVL(pd.pay_exchange_rate, 1), 4)  AS base_amount,
                   NVL(im.inv_exchange_rate, 1)             AS inv_exchange_rate,
                   ROUND(
                       NVL(pd.line_allocated, 0)
                       * (NVL(pd.pay_exchange_rate, 1)
                          - NVL(im.inv_exchange_rate, 1)),
                       4
                   )                                        AS fx_gain_loss,
                   NVL(fis.FISCAL_YEAR,
                       EXTRACT(YEAR FROM pd.PAYMENT_DATE))  AS fiscal_year,
                   NVL(fis.PERIOD_NUMBER,
                       EXTRACT(MONTH FROM pd.PAYMENT_DATE)) AS fiscal_period,
                   pd.PAYMENT_METHOD,
                   pd.REFERENCE_NUM,
                   pd.CLEARED_DATE,
                   NVL(clr.clearing_status, 'UNKNOWN')     AS clearing_status,
                   pd.pay_status                            AS status
              FROM cte_payment_dedup pd
              LEFT JOIN cte_invoice_match im
                ON im.INVOICE_ID = pd.INVOICE_ID
              LEFT JOIN cte_customer_sk csk
                ON csk.CUSTOMER_ID = im.CUSTOMER_ID
              LEFT JOIN cte_account_sk ask_lu
                ON ask_lu.ACCOUNT_ID = (
                       SELECT il4.ACCOUNT_ID
                         FROM FIN.INVOICE_LINES il4
                        WHERE il4.INVOICE_ID = pd.INVOICE_ID
                          AND ROWNUM = 1
                   )
              LEFT JOIN cte_date_dim ddt
                ON ddt.FULL_DATE = TRUNC(pd.PAYMENT_DATE)
              LEFT JOIN cte_fiscal fis
                ON fis.FISCAL_PERIOD_ID = pd.FISCAL_PERIOD_ID
              LEFT JOIN cte_bank_accounts bk
                ON bk.BANK_ACCOUNT_ID = pd.BANK_ACCOUNT_ID
              LEFT JOIN cte_fx_rate fx
                ON fx.CURRENCY_ID = pd.CURRENCY_ID
              LEFT JOIN cte_clearing clr
                ON clr.PAYMENT_ID = pd.PAYMENT_ID
             WHERE pd.dedup_rn = 1
               AND NOT EXISTS (
                       SELECT 1
                         FROM DWH.FACT_PAYMENTS fp_exist
                        WHERE fp_exist.PAYMENT_ID = pd.PAYMENT_ID
                          AND fp_exist.INVOICE_ID = pd.INVOICE_ID
                          AND p_mode = 'INCREMENTAL'
                   )
             ORDER BY pd.PAYMENT_DATE, pd.PAYMENT_ID;

        LOOP
            FETCH v_cur BULK COLLECT INTO v_buffer LIMIT gc_batch_limit;
            EXIT WHEN v_buffer.COUNT = 0;

            v_pay_sk_arr.DELETE;    v_date_arr.DELETE;
            v_cust_sk_arr.DELETE;   v_acct_sk_arr.DELETE;
            v_pay_id_arr.DELETE;    v_pay_num_arr.DELETE;
            v_pay_type_arr.DELETE;  v_pay_date_arr.DELETE;
            v_bank_id_arr.DELETE;   v_bank_nm_arr.DELETE;
            v_bank_cur_arr.DELETE;  v_inv_id_arr.DELETE;
            v_inv_num_arr.DELETE;   v_alloc_amt_arr.DELETE;
            v_disc_arr.DELETE;      v_wo_arr.DELETE;
            v_pay_amt_arr.DELETE;   v_unalloc_arr.DELETE;
            v_curr_arr.DELETE;      v_xrate_arr.DELETE;
            v_base_arr.DELETE;      v_inv_xr_arr.DELETE;
            v_fxgl_arr.DELETE;      v_fyear_arr.DELETE;
            v_fperiod_arr.DELETE;   v_method_arr.DELETE;
            v_ref_arr.DELETE;       v_clear_dt_arr.DELETE;
            v_clr_stat_arr.DELETE;  v_status_arr.DELETE;

            FOR i IN 1 .. v_buffer.COUNT LOOP
                v_pay_sk_arr(i)    := v_buffer(i).payment_sk;
                v_date_arr(i)      := v_buffer(i).date_id;
                v_cust_sk_arr(i)   := v_buffer(i).customer_sk;
                v_acct_sk_arr(i)   := v_buffer(i).account_sk;
                v_pay_id_arr(i)    := v_buffer(i).payment_id;
                v_pay_num_arr(i)   := v_buffer(i).payment_num;
                v_pay_type_arr(i)  := v_buffer(i).payment_type;
                v_pay_date_arr(i)  := v_buffer(i).payment_date;
                v_bank_id_arr(i)   := v_buffer(i).bank_account_id;
                v_bank_nm_arr(i)   := v_buffer(i).bank_name;
                v_bank_cur_arr(i)  := v_buffer(i).bank_currency;
                v_inv_id_arr(i)    := v_buffer(i).invoice_id;
                v_inv_num_arr(i)   := v_buffer(i).invoice_num;
                v_alloc_amt_arr(i) := v_buffer(i).allocated_amount;
                v_disc_arr(i)      := v_buffer(i).discount_taken;
                v_wo_arr(i)        := v_buffer(i).write_off_amount;
                v_pay_amt_arr(i)   := v_buffer(i).payment_amount;
                v_unalloc_arr(i)   := v_buffer(i).unallocated_amount;
                v_curr_arr(i)      := v_buffer(i).currency_code;
                v_xrate_arr(i)     := v_buffer(i).exchange_rate;
                v_base_arr(i)      := v_buffer(i).base_amount;
                v_inv_xr_arr(i)    := v_buffer(i).inv_exchange_rate;
                v_fxgl_arr(i)      := v_buffer(i).fx_gain_loss;
                v_fyear_arr(i)     := v_buffer(i).fiscal_year;
                v_fperiod_arr(i)   := v_buffer(i).fiscal_period;
                v_method_arr(i)    := v_buffer(i).payment_method;
                v_ref_arr(i)       := v_buffer(i).reference_num;
                v_clear_dt_arr(i)  := v_buffer(i).cleared_date;
                v_clr_stat_arr(i)  := v_buffer(i).clearing_status;
                v_status_arr(i)    := v_buffer(i).status;
            END LOOP;

            BEGIN
                FORALL i IN VALUES OF v_pay_sk_arr SAVE EXCEPTIONS
                    INSERT /*+ APPEND PARALLEL(8) */
                      INTO DWH.FACT_PAYMENTS_STG (
                               payment_sk, date_id, customer_sk, account_sk,
                               payment_id, payment_num, payment_type,
                               payment_date, bank_account_id, bank_name,
                               bank_currency, invoice_id, invoice_num,
                               allocated_amount, discount_taken, write_off_amount,
                               payment_amount, unallocated_amount,
                               currency_code, exchange_rate, base_amount,
                               inv_exchange_rate, fx_gain_loss,
                               fiscal_year, fiscal_period,
                               payment_method, reference_num,
                               cleared_date, clearing_status, status,
                               created_date
                           )
                    VALUES (
                               v_pay_sk_arr(i), v_date_arr(i), v_cust_sk_arr(i), v_acct_sk_arr(i),
                               v_pay_id_arr(i), v_pay_num_arr(i), v_pay_type_arr(i),
                               v_pay_date_arr(i), v_bank_id_arr(i), v_bank_nm_arr(i),
                               v_bank_cur_arr(i), v_inv_id_arr(i), v_inv_num_arr(i),
                               v_alloc_amt_arr(i), v_disc_arr(i), v_wo_arr(i),
                               v_pay_amt_arr(i), v_unalloc_arr(i),
                               v_curr_arr(i), v_xrate_arr(i), v_base_arr(i),
                               v_inv_xr_arr(i), v_fxgl_arr(i),
                               v_fyear_arr(i), v_fperiod_arr(i),
                               v_method_arr(i), v_ref_arr(i),
                               v_clear_dt_arr(i), v_clr_stat_arr(i), v_status_arr(i),
                               SYSDATE
                           );
            EXCEPTION
                WHEN OTHERS THEN
                    IF SQLCODE = -24381 THEN
                        v_error_count := v_error_count + SQL%BULK_EXCEPTIONS.COUNT;
                        handle_forall_exceptions(v_step, SQL%BULK_EXCEPTIONS.COUNT);
                    ELSE
                        RAISE;
                    END IF;
            END;

            v_total_rows := v_total_rows + v_buffer.COUNT;
            COMMIT;
        END LOOP;

        CLOSE v_cur;

        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_PAYMENTS tgt
        USING (
            WITH cte_stg_ranked AS (
                SELECT s.*,
                       ROW_NUMBER() OVER (
                           PARTITION BY s.payment_id, s.invoice_id
                           ORDER BY s.payment_sk DESC
                       ) AS merge_rn
                  FROM DWH.FACT_PAYMENTS_STG s
                 WHERE s.created_date >= TRUNC(SYSDATE)
            ),
            cte_stg_valid AS (
                SELECT r.*
                  FROM cte_stg_ranked r
                 WHERE r.merge_rn = 1
            ),
            cte_stg_analytics AS (
                SELECT v.*,
                       SUM(v.allocated_amount) OVER (
                           PARTITION BY v.payment_id
                           ORDER BY v.invoice_id
                           ROWS UNBOUNDED PRECEDING
                       ) AS running_allocated,
                       LAG(v.payment_date) OVER (
                           PARTITION BY v.customer_sk
                           ORDER BY v.payment_date, v.payment_id
                       ) AS prev_payment_date,
                       LEAD(v.payment_date) OVER (
                           PARTITION BY v.customer_sk
                           ORDER BY v.payment_date, v.payment_id
                       ) AS next_payment_date,
                       COUNT(*) OVER (
                           PARTITION BY v.fiscal_year, v.fiscal_period
                       ) AS period_payment_count,
                       SUM(v.fx_gain_loss) OVER (
                           PARTITION BY v.fiscal_year, v.fiscal_period
                       ) AS period_fx_gain_loss
                  FROM cte_stg_valid v
            )
            SELECT a.payment_sk, a.date_id, a.customer_sk, a.account_sk,
                   a.payment_id, a.payment_num, a.payment_type,
                   a.payment_date, a.bank_account_id, a.bank_name,
                   a.invoice_id, a.allocated_amount, a.discount_taken,
                   a.write_off_amount, a.payment_amount, a.unallocated_amount,
                   a.currency_code, a.exchange_rate, a.base_amount,
                   a.fx_gain_loss, a.fiscal_year, a.fiscal_period,
                   a.payment_method, a.clearing_status, a.status
              FROM cte_stg_analytics a
        ) src
        ON (tgt.PAYMENT_ID = src.payment_id AND NVL(tgt.INVOICE_ID, -1) = NVL(src.invoice_id, -1))
        WHEN MATCHED THEN
            UPDATE SET
                tgt.ALLOCATED_AMOUNT = src.allocated_amount,
                tgt.CLEARING_STATUS  = src.clearing_status,
                tgt.STATUS           = src.status,
                tgt.UPDATED_DATE     = SYSDATE
             WHERE tgt.ALLOCATED_AMOUNT <> src.allocated_amount
                OR tgt.STATUS           <> src.status
                OR NVL(tgt.CLEARING_STATUS, 'X') <> NVL(src.clearing_status, 'X')
        WHEN NOT MATCHED THEN
            INSERT (
                PAYMENT_SK, DATE_ID, CUSTOMER_SK, ACCOUNT_SK,
                PAYMENT_ID, PAYMENT_NUM, PAYMENT_TYPE, PAYMENT_DATE,
                BANK_ACCOUNT_ID, INVOICE_ID, ALLOCATED_AMOUNT,
                DISCOUNT_TAKEN, WRITE_OFF_AMOUNT,
                PAYMENT_AMOUNT, UNALLOCATED_AMOUNT,
                CURRENCY_CODE, EXCHANGE_RATE, BASE_AMOUNT,
                FX_GAIN_LOSS, FISCAL_YEAR, FISCAL_PERIOD,
                PAYMENT_METHOD, CLEARING_STATUS, STATUS,
                CREATED_DATE
            )
            VALUES (
                src.payment_sk, src.date_id, src.customer_sk, src.account_sk,
                src.payment_id, src.payment_num, src.payment_type, src.payment_date,
                src.bank_account_id, src.invoice_id, src.allocated_amount,
                src.discount_taken, src.write_off_amount,
                src.payment_amount, src.unallocated_amount,
                src.currency_code, src.exchange_rate, src.base_amount,
                src.fx_gain_loss, src.fiscal_year, src.fiscal_period,
                src.payment_method, src.clearing_status, src.status,
                SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;

        log_step(v_step || '.MERGE', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        UPDATE /*+ PARALLEL(8) */ DWH.FACT_PAYMENTS tgt
           SET tgt.CUMULATIVE_ALLOCATED = (
                   SELECT NVL(SUM(fp2.ALLOCATED_AMOUNT), 0)
                     FROM DWH.FACT_PAYMENTS fp2
                    WHERE fp2.CUSTOMER_SK  = tgt.CUSTOMER_SK
                      AND fp2.PAYMENT_DATE <= tgt.PAYMENT_DATE
                      AND fp2.STATUS IN ('COMPLETED', 'RECONCILED', 'CLEARED')
               ),
               tgt.TOTAL_FX_IMPACT = (
                   SELECT NVL(SUM(fp3.FX_GAIN_LOSS), 0)
                     FROM DWH.FACT_PAYMENTS fp3
                    WHERE fp3.CUSTOMER_SK   = tgt.CUSTOMER_SK
                      AND fp3.FISCAL_YEAR   = tgt.FISCAL_YEAR
                      AND fp3.FISCAL_PERIOD = tgt.FISCAL_PERIOD
               ),
               tgt.BANK_PERIOD_TOTAL = (
                   SELECT NVL(SUM(fp4.PAYMENT_AMOUNT), 0)
                     FROM DWH.FACT_PAYMENTS fp4
                    WHERE fp4.BANK_ACCOUNT_ID = tgt.BANK_ACCOUNT_ID
                      AND fp4.FISCAL_YEAR     = tgt.FISCAL_YEAR
                      AND fp4.FISCAL_PERIOD   = tgt.FISCAL_PERIOD
                      AND fp4.STATUS IN ('COMPLETED', 'RECONCILED', 'CLEARED')
               ),
               tgt.DAYS_TO_CLEAR = (
                   CASE
                       WHEN tgt.CLEARED_DATE IS NOT NULL
                       THEN tgt.CLEARED_DATE - tgt.PAYMENT_DATE
                       ELSE NULL
                   END
               ),
               tgt.UPDATED_DATE = SYSDATE
         WHERE tgt.PAYMENT_ID IN (
                   SELECT DISTINCT s3.payment_id
                     FROM DWH.FACT_PAYMENTS_STG s3
                    WHERE s3.created_date >= TRUNC(SYSDATE)
               );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.POST_UPDATE_CUMULATIVE', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        INSERT /*+ PARALLEL(8) */
          INTO DWH.FACT_PAYMENT_DAILY_SNAPSHOT (
                   snapshot_date, fiscal_year, fiscal_period,
                   total_payments, total_allocated, total_unallocated,
                   total_discount_taken, total_write_off,
                   total_fx_gain_loss, payment_count,
                   avg_payment_amount, max_payment_amount,
                   cleared_count, uncleared_count,
                   avg_days_to_clear,
                   bank_account_count, payment_method_count,
                   currency_code, created_date
               )
        WITH cte_pay_snap AS (
            SELECT fp.FISCAL_YEAR,
                   fp.FISCAL_PERIOD,
                   fp.PAYMENT_ID,
                   fp.PAYMENT_AMOUNT,
                   fp.ALLOCATED_AMOUNT,
                   fp.UNALLOCATED_AMOUNT,
                   fp.DISCOUNT_TAKEN,
                   fp.WRITE_OFF_AMOUNT,
                   fp.FX_GAIN_LOSS,
                   fp.PAYMENT_DATE,
                   fp.CLEARED_DATE,
                   fp.BANK_ACCOUNT_ID,
                   fp.PAYMENT_METHOD,
                   fp.CURRENCY_CODE,
                   fp.CLEARING_STATUS,
                   ROW_NUMBER() OVER (
                       PARTITION BY fp.PAYMENT_ID, fp.INVOICE_ID
                       ORDER BY fp.PAYMENT_SK DESC
                   ) AS ps_rn
              FROM DWH.FACT_PAYMENTS fp
             WHERE fp.STATUS IN ('COMPLETED', 'RECONCILED', 'CLEARED')
        ),
        cte_pay_dedup AS (
            SELECT ps.*
              FROM cte_pay_snap ps
             WHERE ps.ps_rn = 1
        ),
        cte_clearing_stats AS (
            SELECT pd.FISCAL_YEAR,
                   pd.FISCAL_PERIOD,
                   AVG(CASE WHEN pd.CLEARED_DATE IS NOT NULL
                            THEN pd.CLEARED_DATE - pd.PAYMENT_DATE
                            ELSE NULL END) AS avg_clear_days,
                   SUM(CASE WHEN pd.CLEARING_STATUS = 'CLEARED' THEN 1 ELSE 0 END)
                       AS cleared_cnt,
                   SUM(CASE WHEN pd.CLEARING_STATUS <> 'CLEARED' THEN 1 ELSE 0 END)
                       AS uncleared_cnt
              FROM cte_pay_dedup pd
             GROUP BY pd.FISCAL_YEAR, pd.FISCAL_PERIOD
        ),
        cte_method_count AS (
            SELECT pd2.FISCAL_YEAR,
                   pd2.FISCAL_PERIOD,
                   COUNT(DISTINCT pd2.PAYMENT_METHOD) AS method_cnt,
                   COUNT(DISTINCT pd2.BANK_ACCOUNT_ID) AS bank_cnt
              FROM cte_pay_dedup pd2
             GROUP BY pd2.FISCAL_YEAR, pd2.FISCAL_PERIOD
        )
        SELECT TRUNC(SYSDATE),
               pd.FISCAL_YEAR,
               pd.FISCAL_PERIOD,
               SUM(pd.PAYMENT_AMOUNT),
               SUM(pd.ALLOCATED_AMOUNT),
               SUM(pd.UNALLOCATED_AMOUNT),
               SUM(pd.DISCOUNT_TAKEN),
               SUM(pd.WRITE_OFF_AMOUNT),
               SUM(pd.FX_GAIN_LOSS),
               COUNT(DISTINCT pd.PAYMENT_ID),
               AVG(pd.PAYMENT_AMOUNT),
               MAX(pd.PAYMENT_AMOUNT),
               NVL(cs.cleared_cnt, 0),
               NVL(cs.uncleared_cnt, 0),
               NVL(cs.avg_clear_days, 0),
               NVL(mc.bank_cnt, 0),
               NVL(mc.method_cnt, 0),
               pd.CURRENCY_CODE,
               SYSDATE
          FROM cte_pay_dedup pd
          LEFT JOIN cte_clearing_stats cs
            ON cs.FISCAL_YEAR   = pd.FISCAL_YEAR
           AND cs.FISCAL_PERIOD = pd.FISCAL_PERIOD
          LEFT JOIN cte_method_count mc
            ON mc.FISCAL_YEAR   = pd.FISCAL_YEAR
           AND mc.FISCAL_PERIOD = pd.FISCAL_PERIOD
         GROUP BY pd.FISCAL_YEAR, pd.FISCAL_PERIOD,
                  pd.CURRENCY_CODE,
                  cs.cleared_cnt, cs.uncleared_cnt, cs.avg_clear_days,
                  mc.bank_cnt, mc.method_cnt;

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.PAYMENT_SNAPSHOT', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_PAYMENT_BANK_SUMMARY tgt
        USING (
            WITH cte_bank_payments AS (
                SELECT fp.BANK_ACCOUNT_ID,
                       fp.FISCAL_YEAR,
                       fp.FISCAL_PERIOD,
                       fp.PAYMENT_ID,
                       fp.PAYMENT_AMOUNT,
                       fp.ALLOCATED_AMOUNT,
                       fp.FX_GAIN_LOSS,
                       fp.PAYMENT_METHOD,
                       fp.CLEARING_STATUS,
                       fp.PAYMENT_DATE,
                       fp.CLEARED_DATE,
                       fp.CURRENCY_CODE,
                       ROW_NUMBER() OVER (
                           PARTITION BY fp.PAYMENT_ID, fp.INVOICE_ID
                           ORDER BY fp.PAYMENT_SK DESC
                       ) AS bp_rn
                  FROM DWH.FACT_PAYMENTS fp
                 WHERE fp.STATUS IN ('COMPLETED', 'RECONCILED', 'CLEARED')
            ),
            cte_bank_dedup AS (
                SELECT bp.*
                  FROM cte_bank_payments bp
                 WHERE bp.bp_rn = 1
            ),
            cte_bank_info AS (
                SELECT ba.BANK_ACCOUNT_ID,
                       ba.ACCOUNT_NAME,
                       ba.BANK_NAME,
                       ba.CURRENT_BALANCE,
                       (SELECT cur.CURRENCY_CODE
                          FROM FIN.CURRENCIES cur
                         WHERE cur.CURRENCY_ID = ba.CURRENCY_ID) AS bank_currency
                  FROM FIN.BANK_ACCOUNTS ba
            ),
            cte_bank_agg AS (
                SELECT bd.BANK_ACCOUNT_ID,
                       bd.FISCAL_YEAR,
                       bd.FISCAL_PERIOD,
                       bi.BANK_NAME,
                       bi.ACCOUNT_NAME     AS bank_account_name,
                       bi.CURRENT_BALANCE,
                       bi.bank_currency,
                       COUNT(DISTINCT bd.PAYMENT_ID)   AS payment_count,
                       SUM(bd.PAYMENT_AMOUNT)          AS total_payments,
                       SUM(bd.ALLOCATED_AMOUNT)        AS total_allocated,
                       SUM(bd.FX_GAIN_LOSS)            AS total_fx_impact,
                       AVG(bd.PAYMENT_AMOUNT)          AS avg_payment,
                       MAX(bd.PAYMENT_AMOUNT)          AS max_payment,
                       COUNT(DISTINCT bd.PAYMENT_METHOD) AS method_count,
                       SUM(CASE WHEN bd.CLEARING_STATUS = 'CLEARED' THEN 1 ELSE 0 END)
                                                        AS cleared_count,
                       AVG(CASE WHEN bd.CLEARED_DATE IS NOT NULL
                                THEN bd.CLEARED_DATE - bd.PAYMENT_DATE END)
                                                        AS avg_clear_days,
                       RANK() OVER (
                           PARTITION BY bd.FISCAL_YEAR, bd.FISCAL_PERIOD
                           ORDER BY SUM(bd.PAYMENT_AMOUNT) DESC
                       ) AS bank_rank
                  FROM cte_bank_dedup bd
                  LEFT JOIN cte_bank_info bi
                    ON bi.BANK_ACCOUNT_ID = bd.BANK_ACCOUNT_ID
                 GROUP BY bd.BANK_ACCOUNT_ID, bd.FISCAL_YEAR, bd.FISCAL_PERIOD,
                          bi.BANK_NAME, bi.ACCOUNT_NAME, bi.CURRENT_BALANCE, bi.bank_currency
            )
            SELECT ba.BANK_ACCOUNT_ID, ba.FISCAL_YEAR, ba.FISCAL_PERIOD,
                   ba.BANK_NAME, ba.bank_account_name, ba.CURRENT_BALANCE,
                   ba.bank_currency, ba.payment_count, ba.total_payments,
                   ba.total_allocated, ba.total_fx_impact,
                   ba.avg_payment, ba.max_payment,
                   ba.method_count, ba.cleared_count,
                   ba.avg_clear_days, ba.bank_rank
              FROM cte_bank_agg ba
        ) src
        ON (    tgt.BANK_ACCOUNT_ID = src.BANK_ACCOUNT_ID
            AND tgt.FISCAL_YEAR     = src.FISCAL_YEAR
            AND tgt.FISCAL_PERIOD   = src.FISCAL_PERIOD)
        WHEN MATCHED THEN
            UPDATE SET
                tgt.PAYMENT_COUNT   = src.payment_count,
                tgt.TOTAL_PAYMENTS  = src.total_payments,
                tgt.TOTAL_ALLOCATED = src.total_allocated,
                tgt.TOTAL_FX_IMPACT = src.total_fx_impact,
                tgt.CLEARED_COUNT   = src.cleared_count,
                tgt.AVG_CLEAR_DAYS  = src.avg_clear_days,
                tgt.BANK_RANK       = src.bank_rank,
                tgt.UPDATED_DATE    = SYSDATE
             WHERE tgt.TOTAL_PAYMENTS <> src.total_payments
                OR tgt.CLEARED_COUNT  <> src.cleared_count
        WHEN NOT MATCHED THEN
            INSERT (
                BANK_ACCOUNT_ID, FISCAL_YEAR, FISCAL_PERIOD,
                BANK_NAME, BANK_ACCOUNT_NAME, CURRENT_BALANCE,
                BANK_CURRENCY, PAYMENT_COUNT, TOTAL_PAYMENTS,
                TOTAL_ALLOCATED, TOTAL_FX_IMPACT,
                AVG_PAYMENT, MAX_PAYMENT,
                METHOD_COUNT, CLEARED_COUNT,
                AVG_CLEAR_DAYS, BANK_RANK, CREATED_DATE
            )
            VALUES (
                src.BANK_ACCOUNT_ID, src.FISCAL_YEAR, src.FISCAL_PERIOD,
                src.BANK_NAME, src.bank_account_name, src.CURRENT_BALANCE,
                src.bank_currency, src.payment_count, src.total_payments,
                src.total_allocated, src.total_fx_impact,
                src.avg_payment, src.max_payment,
                src.method_count, src.cleared_count,
                src.avg_clear_days, src.bank_rank, SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.BANK_SUMMARY', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_PAYMENT_FX_SUMMARY tgt
        USING (
            WITH cte_fx_base AS (
                SELECT fp.CURRENCY_CODE,
                       fp.FISCAL_YEAR,
                       fp.FISCAL_PERIOD,
                       fp.PAYMENT_ID,
                       fp.PAYMENT_AMOUNT,
                       fp.EXCHANGE_RATE,
                       fp.INV_EXCHANGE_RATE,
                       fp.FX_GAIN_LOSS,
                       fp.BASE_AMOUNT,
                       ROW_NUMBER() OVER (
                           PARTITION BY fp.PAYMENT_ID, fp.INVOICE_ID
                           ORDER BY fp.PAYMENT_SK DESC
                       ) AS fx_rn
                  FROM DWH.FACT_PAYMENTS fp
                 WHERE fp.STATUS IN ('COMPLETED', 'RECONCILED', 'CLEARED')
                   AND fp.CURRENCY_CODE <> v_func_curr
            ),
            cte_fx_dedup AS (
                SELECT fb.*
                  FROM cte_fx_base fb
                 WHERE fb.fx_rn = 1
            ),
            cte_fx_agg AS (
                SELECT fd.CURRENCY_CODE,
                       fd.FISCAL_YEAR,
                       fd.FISCAL_PERIOD,
                       COUNT(DISTINCT fd.PAYMENT_ID)   AS payment_count,
                       SUM(fd.PAYMENT_AMOUNT)          AS total_foreign_amount,
                       SUM(fd.BASE_AMOUNT)             AS total_base_amount,
                       SUM(fd.FX_GAIN_LOSS)            AS total_fx_gain_loss,
                       AVG(fd.EXCHANGE_RATE)           AS avg_payment_rate,
                       AVG(fd.INV_EXCHANGE_RATE)       AS avg_invoice_rate,
                       MAX(fd.EXCHANGE_RATE)           AS max_rate,
                       MIN(fd.EXCHANGE_RATE)           AS min_rate,
                       STDDEV(fd.EXCHANGE_RATE)        AS rate_stddev,
                       SUM(CASE WHEN fd.FX_GAIN_LOSS > 0 THEN fd.FX_GAIN_LOSS ELSE 0 END)
                                                        AS total_fx_gain,
                       SUM(CASE WHEN fd.FX_GAIN_LOSS < 0 THEN fd.FX_GAIN_LOSS ELSE 0 END)
                                                        AS total_fx_loss,
                       RANK() OVER (
                           PARTITION BY fd.FISCAL_YEAR, fd.FISCAL_PERIOD
                           ORDER BY ABS(SUM(fd.FX_GAIN_LOSS)) DESC
                       ) AS fx_impact_rank
                  FROM cte_fx_dedup fd
                 GROUP BY fd.CURRENCY_CODE, fd.FISCAL_YEAR, fd.FISCAL_PERIOD
            )
            SELECT fa.CURRENCY_CODE, fa.FISCAL_YEAR, fa.FISCAL_PERIOD,
                   fa.payment_count, fa.total_foreign_amount,
                   fa.total_base_amount, fa.total_fx_gain_loss,
                   fa.avg_payment_rate, fa.avg_invoice_rate,
                   fa.max_rate, fa.min_rate, fa.rate_stddev,
                   fa.total_fx_gain, fa.total_fx_loss,
                   fa.fx_impact_rank
              FROM cte_fx_agg fa
        ) src
        ON (    tgt.CURRENCY_CODE = src.CURRENCY_CODE
            AND tgt.FISCAL_YEAR   = src.FISCAL_YEAR
            AND tgt.FISCAL_PERIOD = src.FISCAL_PERIOD)
        WHEN MATCHED THEN
            UPDATE SET
                tgt.PAYMENT_COUNT        = src.payment_count,
                tgt.TOTAL_FOREIGN_AMOUNT = src.total_foreign_amount,
                tgt.TOTAL_BASE_AMOUNT    = src.total_base_amount,
                tgt.TOTAL_FX_GAIN_LOSS   = src.total_fx_gain_loss,
                tgt.AVG_PAYMENT_RATE     = src.avg_payment_rate,
                tgt.TOTAL_FX_GAIN        = src.total_fx_gain,
                tgt.TOTAL_FX_LOSS        = src.total_fx_loss,
                tgt.FX_IMPACT_RANK       = src.fx_impact_rank,
                tgt.UPDATED_DATE         = SYSDATE
             WHERE tgt.TOTAL_FX_GAIN_LOSS <> src.total_fx_gain_loss
                OR tgt.PAYMENT_COUNT      <> src.payment_count
        WHEN NOT MATCHED THEN
            INSERT (
                CURRENCY_CODE, FISCAL_YEAR, FISCAL_PERIOD,
                PAYMENT_COUNT, TOTAL_FOREIGN_AMOUNT,
                TOTAL_BASE_AMOUNT, TOTAL_FX_GAIN_LOSS,
                AVG_PAYMENT_RATE, AVG_INVOICE_RATE,
                MAX_RATE, MIN_RATE, RATE_STDDEV,
                TOTAL_FX_GAIN, TOTAL_FX_LOSS,
                FX_IMPACT_RANK, CREATED_DATE
            )
            VALUES (
                src.CURRENCY_CODE, src.FISCAL_YEAR, src.FISCAL_PERIOD,
                src.payment_count, src.total_foreign_amount,
                src.total_base_amount, src.total_fx_gain_loss,
                src.avg_payment_rate, src.avg_invoice_rate,
                src.max_rate, src.min_rate, src.rate_stddev,
                src.total_fx_gain, src.total_fx_loss,
                src.fx_impact_rank, SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.FX_SUMMARY', 'DONE', g_row_count);

        log_step(v_step || '.COMPLETE', 'SUCCESS', v_total_rows);

    EXCEPTION
        WHEN OTHERS THEN
            IF v_cur%ISOPEN THEN CLOSE v_cur; END IF;
            ROLLBACK;
            log_step(v_step || '.FATAL', 'ERROR', 0, SQLERRM);
            RAISE;
    END load_fact_payments;

    -- =========================================================================
    -- 3. LOAD_FACT_JOURNAL_ENTRIES
    -- =========================================================================
    PROCEDURE load_fact_journal_entries (
        p_from_date  IN DATE,
        p_to_date    IN DATE,
        p_mode       IN VARCHAR2 DEFAULT 'INCREMENTAL'
    ) IS
        v_step          VARCHAR2(200) := 'LOAD_FACT_JOURNAL_ENTRIES';
        v_cur           t_jnl_ref_cursor;
        v_buffer        t_journal_stg_tab;
        v_total_rows    NUMBER(19) := 0;
        v_error_count   PLS_INTEGER := 0;
        v_func_curr     VARCHAR2(3);

        v_jl_sk_arr      t_id_array;
        v_date_arr       t_id_array;
        v_acct_sk_arr    t_id_array;
        v_jl_id_arr      t_id_array;
        v_j_id_arr       t_id_array;
        v_j_num_arr      t_varchar_array;
        v_j_type_arr     t_varchar_array;
        v_j_date_arr     t_date_array;
        v_line_arr       t_id_array;
        v_aid_arr        t_id_array;
        v_acode_arr      t_varchar_array;
        v_aname_arr      t_varchar_array;
        v_atype_arr      t_varchar_array;
        v_asub_arr       t_varchar_array;
        v_nbal_arr       t_varchar_array;
        v_ipost_arr      t_varchar_array;
        v_apath_arr      t_varchar_array;
        v_ccid_arr       t_id_array;
        v_cccode_arr     t_varchar_array;
        v_ccname_arr     t_varchar_array;
        v_cctype_arr     t_varchar_array;
        v_bhold_arr      t_varchar_array;
        v_ccpath_arr     t_varchar_array;
        v_dr_arr         t_amount_array;
        v_cr_arr         t_amount_array;
        v_bdr_arr        t_amount_array;
        v_bcr_arr        t_amount_array;
        v_curr_arr       t_varchar_array;
        v_xrate_arr      t_amount_array;
        v_fyear_arr      t_id_array;
        v_fperiod_arr    t_id_array;
        v_pstat_arr      t_varchar_array;
        v_stype_arr      t_varchar_array;
        v_sid_arr        t_id_array;
        v_revjid_arr     t_id_array;
        v_isic_arr       t_varchar_array;
        v_isrec_arr      t_varchar_array;
        v_bplan_arr      t_amount_array;
        v_bvar_arr       t_amount_array;
        v_approv_arr     t_varchar_array;
        v_status_arr     t_varchar_array;
    BEGIN
        init_run(v_step);
        v_func_curr := get_functional_currency;

        IF p_mode = 'FULL' THEN
            EXECUTE IMMEDIATE 'TRUNCATE TABLE DWH.FACT_JOURNAL_ENTRIES_STG';
            g_step_start := SYSTIMESTAMP;
            log_step(v_step || '.TRUNCATE', 'DONE');
        END IF;

        g_step_start := SYSTIMESTAMP;

        OPEN v_cur FOR
            WITH cte_journals AS (
                SELECT /*+ PARALLEL(j, 8) */
                       j.JOURNAL_ID,
                       j.JOURNAL_NUM,
                       j.JOURNAL_TYPE,
                       j.JOURNAL_DATE,
                       j.FISCAL_PERIOD_ID,
                       j.DESCRIPTION       AS journal_description,
                       j.REFERENCE_NUM     AS journal_ref,
                       j.TOTAL_DEBIT,
                       j.TOTAL_CREDIT,
                       j.STATUS            AS journal_status,
                       j.SOURCE_TYPE,
                       j.SOURCE_ID,
                       j.CURRENCY_ID,
                       j.REVERSING_JOURNAL_ID
                  FROM FIN.JOURNALS j
                 WHERE j.JOURNAL_DATE BETWEEN p_from_date AND p_to_date
                   AND j.STATUS IN ('POSTED', 'APPROVED')
            ),
            cte_journal_lines AS (
                SELECT /*+ PARALLEL(jl, 8) */
                       jl.JOURNAL_LINE_ID,
                       jl.JOURNAL_ID,
                       jl.LINE_NUM,
                       jl.ACCOUNT_ID,
                       jl.COST_CENTER_ID,
                       jl.DEBIT_AMOUNT,
                       jl.CREDIT_AMOUNT,
                       jl.CURRENCY_ID       AS line_currency_id,
                       jl.EXCHANGE_RATE     AS line_exchange_rate,
                       jl.BASE_DEBIT,
                       jl.BASE_CREDIT,
                       jl.LINE_DESCRIPTION,
                       jl.REFERENCE_NUM     AS line_ref,
                       jl.RECONCILED_FLAG
                  FROM FIN.JOURNAL_LINES jl
                 WHERE jl.JOURNAL_ID IN (
                           SELECT cj.JOURNAL_ID FROM cte_journals cj
                       )
            ),
            cte_account_hierarchy AS (
                SELECT a.ACCOUNT_ID,
                       a.ACCOUNT_CODE,
                       a.ACCOUNT_NAME,
                       a.ACCOUNT_TYPE,
                       a.ACCOUNT_SUBTYPE,
                       a.PARENT_ACCOUNT_ID,
                       a.LEVEL_NUM,
                       a.FULL_PATH_CODE,
                       a.IS_POSTING,
                       a.NORMAL_BALANCE,
                       a.IS_ACTIVE,
                       SYS_CONNECT_BY_PATH(a.ACCOUNT_CODE, '/') AS hierarchy_path,
                       CONNECT_BY_ROOT a.ACCOUNT_CODE            AS root_account_code,
                       LEVEL                                      AS depth_level
                  FROM FIN.ACCOUNTS a
                 WHERE a.IS_ACTIVE = 'Y'
                 START WITH a.PARENT_ACCOUNT_ID IS NULL
               CONNECT BY PRIOR a.ACCOUNT_ID = a.PARENT_ACCOUNT_ID
            ),
            cte_cc_hierarchy AS (
                SELECT cc.COST_CENTER_ID,
                       cc.CC_CODE,
                       cc.CC_NAME,
                       cc.CC_TYPE,
                       cc.PARENT_CC_ID,
                       cc.LEVEL_NUM,
                       cc.BUDGET_HOLDER,
                       SYS_CONNECT_BY_PATH(cc.CC_CODE, '/') AS cc_hierarchy_path,
                       CONNECT_BY_ROOT cc.CC_CODE            AS root_cc_code,
                       LEVEL                                  AS cc_depth
                  FROM FIN.COST_CENTERS cc
                 START WITH cc.PARENT_CC_ID IS NULL
               CONNECT BY PRIOR cc.COST_CENTER_ID = cc.PARENT_CC_ID
            ),
            cte_fiscal_validation AS (
                SELECT fp.FISCAL_PERIOD_ID,
                       fp.FISCAL_YEAR,
                       fp.PERIOD_NUMBER,
                       fp.PERIOD_NAME,
                       fp.START_DATE,
                       fp.END_DATE,
                       fp.STATUS AS period_status,
                       CASE
                           WHEN fp.STATUS = 'OPEN' THEN 'Y'
                           ELSE 'N'
                       END AS is_open,
                       LAG(fp.PERIOD_NUMBER) OVER (
                           PARTITION BY fp.FISCAL_YEAR
                           ORDER BY fp.PERIOD_NUMBER
                       ) AS prev_period,
                       LEAD(fp.PERIOD_NUMBER) OVER (
                           PARTITION BY fp.FISCAL_YEAR
                           ORDER BY fp.PERIOD_NUMBER
                       ) AS next_period
                  FROM FIN.FISCAL_PERIODS fp
            ),
            cte_fx_rate AS (
                SELECT cur.CURRENCY_ID,
                       cur.CURRENCY_CODE,
                       cur.IS_FUNCTIONAL,
                       cur.IS_REPORTING
                  FROM FIN.CURRENCIES cur
            ),
            cte_intercompany_detection AS (
                SELECT jl1.JOURNAL_ID,
                       CASE
                           WHEN COUNT(DISTINCT jl1.COST_CENTER_ID) > 1
                            AND SUM(jl1.DEBIT_AMOUNT) > 0
                            AND SUM(jl1.CREDIT_AMOUNT) > 0
                           THEN 'Y'
                           ELSE 'N'
                       END AS is_intercompany
                  FROM FIN.JOURNAL_LINES jl1
                 WHERE jl1.JOURNAL_ID IN (
                           SELECT cj2.JOURNAL_ID FROM cte_journals cj2
                       )
                 GROUP BY jl1.JOURNAL_ID
            ),
            cte_budget_check AS (
                SELECT bl.ACCOUNT_ID,
                       bl.COST_CENTER_ID,
                       bl.FISCAL_PERIOD_ID,
                       NVL(bl.REVISED_AMOUNT, bl.PLANNED_AMOUNT) AS budget_amount,
                       bl.ACTUAL_AMOUNT AS budget_actual,
                       NVL(bl.REVISED_AMOUNT, bl.PLANNED_AMOUNT)
                           - NVL(bl.ACTUAL_AMOUNT, 0)            AS budget_remaining
                  FROM FIN.BUDGET_LINES bl
                  JOIN FIN.BUDGETS b
                    ON b.BUDGET_ID = bl.BUDGET_ID
                 WHERE b.STATUS = 'APPROVED'
                   AND b.BUDGET_TYPE = 'OPERATING'
            ),
            cte_recurring_detection AS (
                SELECT j2.JOURNAL_ID,
                       CASE
                           WHEN EXISTS (
                                    SELECT 1
                                      FROM FIN.JOURNALS j3
                                     WHERE j3.JOURNAL_TYPE = j2.JOURNAL_TYPE
                                       AND j3.SOURCE_TYPE  = j2.SOURCE_TYPE
                                       AND j3.JOURNAL_ID   <> j2.JOURNAL_ID
                                       AND ABS(j3.TOTAL_DEBIT - j2.TOTAL_DEBIT) < 0.01
                                       AND j3.STATUS = 'POSTED'
                                )
                           THEN 'Y'
                           ELSE 'N'
                       END AS is_recurring
                  FROM cte_journals j2
            ),
            cte_reversal_link AS (
                SELECT j4.JOURNAL_ID,
                       j4.REVERSING_JOURNAL_ID,
                       (SELECT rj.JOURNAL_NUM
                          FROM FIN.JOURNALS rj
                         WHERE rj.JOURNAL_ID = j4.REVERSING_JOURNAL_ID) AS reversing_journal_num,
                       (SELECT rj.STATUS
                          FROM FIN.JOURNALS rj
                         WHERE rj.JOURNAL_ID = j4.REVERSING_JOURNAL_ID) AS reversing_status
                  FROM cte_journals j4
                 WHERE j4.REVERSING_JOURNAL_ID IS NOT NULL
            ),
            cte_source_doc AS (
                SELECT j5.JOURNAL_ID,
                       j5.SOURCE_TYPE,
                       j5.SOURCE_ID,
                       CASE j5.SOURCE_TYPE
                           WHEN 'INVOICE' THEN
                               (SELECT inv.INVOICE_NUM
                                  FROM FIN.INVOICES inv
                                 WHERE inv.INVOICE_ID = j5.SOURCE_ID)
                           WHEN 'PAYMENT' THEN
                               (SELECT pay.PAYMENT_NUM
                                  FROM FIN.PAYMENTS pay
                                 WHERE pay.PAYMENT_ID = j5.SOURCE_ID)
                           ELSE TO_CHAR(j5.SOURCE_ID)
                       END AS source_doc_num
                  FROM cte_journals j5
                 WHERE j5.SOURCE_TYPE IS NOT NULL
            ),
            cte_account_sk AS (
                SELECT da.ACCOUNT_SK,
                       da.ACCOUNT_ID,
                       da.ACCOUNT_CODE
                  FROM DWH.DIM_ACCOUNT da
            ),
            cte_date_dim AS (
                SELECT dd.DATE_ID,
                       dd.FULL_DATE
                  FROM DWH.DIM_DATE dd
                 WHERE dd.FULL_DATE BETWEEN p_from_date AND p_to_date
            ),
            cte_approval AS (
                SELECT j6.JOURNAL_ID,
                       j6.journal_status,
                       CASE
                           WHEN j6.journal_status = 'POSTED' THEN 'APPROVED_POSTED'
                           WHEN j6.journal_status = 'APPROVED' THEN 'APPROVED_PENDING'
                           ELSE 'PENDING_APPROVAL'
                       END AS approval_status
                  FROM cte_journals j6
            ),
            cte_joined AS (
                SELECT jl.JOURNAL_LINE_ID,
                       j.JOURNAL_ID,
                       j.JOURNAL_NUM,
                       j.JOURNAL_TYPE,
                       j.JOURNAL_DATE,
                       jl.LINE_NUM,
                       jl.ACCOUNT_ID,
                       ah.ACCOUNT_CODE,
                       ah.ACCOUNT_NAME,
                       ah.ACCOUNT_TYPE,
                       ah.ACCOUNT_SUBTYPE,
                       ah.NORMAL_BALANCE,
                       ah.IS_POSTING,
                       ah.hierarchy_path        AS account_path,
                       jl.COST_CENTER_ID,
                       ch.CC_CODE,
                       ch.CC_NAME,
                       ch.CC_TYPE,
                       ch.BUDGET_HOLDER,
                       ch.cc_hierarchy_path,
                       jl.DEBIT_AMOUNT,
                       jl.CREDIT_AMOUNT,
                       jl.BASE_DEBIT,
                       jl.BASE_CREDIT,
                       NVL(fx.CURRENCY_CODE, v_func_curr)  AS currency_code,
                       NVL(jl.line_exchange_rate, 1)        AS exchange_rate,
                       NVL(fv.FISCAL_YEAR,
                           EXTRACT(YEAR FROM j.JOURNAL_DATE))  AS fiscal_year,
                       NVL(fv.PERIOD_NUMBER,
                           EXTRACT(MONTH FROM j.JOURNAL_DATE)) AS fiscal_period,
                       NVL(fv.period_status, 'UNKNOWN')     AS period_status,
                       j.SOURCE_TYPE,
                       j.SOURCE_ID,
                       j.REVERSING_JOURNAL_ID,
                       NVL(ic.is_intercompany, 'N')         AS is_intercompany,
                       NVL(rd.is_recurring, 'N')            AS is_recurring,
                       bc.budget_amount                     AS budget_planned,
                       NVL(jl.DEBIT_AMOUNT, 0)
                           - NVL(bc.budget_amount, 0)       AS budget_variance,
                       NVL(appr.approval_status, 'UNKNOWN') AS approval_status,
                       j.journal_status                     AS status,
                       ROW_NUMBER() OVER (
                           PARTITION BY jl.JOURNAL_LINE_ID
                           ORDER BY j.JOURNAL_DATE DESC
                       ) AS dedup_rn
                  FROM cte_journals j
                  JOIN cte_journal_lines jl
                    ON jl.JOURNAL_ID = j.JOURNAL_ID
                  LEFT JOIN cte_account_hierarchy ah
                    ON ah.ACCOUNT_ID = jl.ACCOUNT_ID
                  LEFT JOIN cte_cc_hierarchy ch
                    ON ch.COST_CENTER_ID = jl.COST_CENTER_ID
                  LEFT JOIN cte_fiscal_validation fv
                    ON fv.FISCAL_PERIOD_ID = j.FISCAL_PERIOD_ID
                  LEFT JOIN cte_fx_rate fx
                    ON fx.CURRENCY_ID = jl.line_currency_id
                  LEFT JOIN cte_intercompany_detection ic
                    ON ic.JOURNAL_ID = j.JOURNAL_ID
                  LEFT JOIN cte_budget_check bc
                    ON bc.ACCOUNT_ID = jl.ACCOUNT_ID
                   AND bc.COST_CENTER_ID = jl.COST_CENTER_ID
                   AND bc.FISCAL_PERIOD_ID = j.FISCAL_PERIOD_ID
                  LEFT JOIN cte_recurring_detection rd
                    ON rd.JOURNAL_ID = j.JOURNAL_ID
                  LEFT JOIN cte_approval appr
                    ON appr.JOURNAL_ID = j.JOURNAL_ID
            )
            SELECT DWH.SEQ_DWH_JOURNAL.NEXTVAL       AS journal_line_sk,
                   NVL(ddt.DATE_ID, -1)               AS date_id,
                   NVL(ask_lu.ACCOUNT_SK, -1)          AS account_sk,
                   cj.JOURNAL_LINE_ID,
                   cj.JOURNAL_ID,
                   cj.JOURNAL_NUM,
                   cj.JOURNAL_TYPE,
                   cj.JOURNAL_DATE,
                   cj.LINE_NUM,
                   cj.ACCOUNT_ID,
                   cj.ACCOUNT_CODE,
                   cj.ACCOUNT_NAME,
                   cj.ACCOUNT_TYPE,
                   cj.ACCOUNT_SUBTYPE,
                   cj.NORMAL_BALANCE,
                   cj.IS_POSTING,
                   cj.account_path,
                   cj.COST_CENTER_ID,
                   cj.CC_CODE,
                   cj.CC_NAME,
                   cj.CC_TYPE,
                   cj.BUDGET_HOLDER,
                   cj.cc_hierarchy_path,
                   cj.DEBIT_AMOUNT,
                   cj.CREDIT_AMOUNT,
                   cj.BASE_DEBIT,
                   cj.BASE_CREDIT,
                   cj.currency_code,
                   cj.exchange_rate,
                   cj.fiscal_year,
                   cj.fiscal_period,
                   cj.period_status,
                   cj.SOURCE_TYPE,
                   cj.SOURCE_ID,
                   cj.REVERSING_JOURNAL_ID,
                   cj.is_intercompany,
                   cj.is_recurring,
                   cj.budget_planned,
                   cj.budget_variance,
                   cj.approval_status,
                   cj.status
              FROM cte_joined cj
              LEFT JOIN cte_account_sk ask_lu
                ON ask_lu.ACCOUNT_ID = cj.ACCOUNT_ID
              LEFT JOIN cte_date_dim ddt
                ON ddt.FULL_DATE = TRUNC(cj.JOURNAL_DATE)
             WHERE cj.dedup_rn = 1
               AND NOT EXISTS (
                       SELECT 1
                         FROM DWH.FACT_JOURNAL_ENTRIES fj_exist
                        WHERE fj_exist.JOURNAL_LINE_ID = cj.JOURNAL_LINE_ID
                          AND p_mode = 'INCREMENTAL'
                   )
             ORDER BY cj.JOURNAL_DATE, cj.JOURNAL_ID, cj.LINE_NUM;

        LOOP
            FETCH v_cur BULK COLLECT INTO v_buffer LIMIT gc_batch_limit;
            EXIT WHEN v_buffer.COUNT = 0;

            v_jl_sk_arr.DELETE;    v_date_arr.DELETE;
            v_acct_sk_arr.DELETE;  v_jl_id_arr.DELETE;
            v_j_id_arr.DELETE;     v_j_num_arr.DELETE;
            v_j_type_arr.DELETE;   v_j_date_arr.DELETE;
            v_line_arr.DELETE;     v_aid_arr.DELETE;
            v_acode_arr.DELETE;    v_aname_arr.DELETE;
            v_atype_arr.DELETE;    v_asub_arr.DELETE;
            v_nbal_arr.DELETE;     v_ipost_arr.DELETE;
            v_apath_arr.DELETE;    v_ccid_arr.DELETE;
            v_cccode_arr.DELETE;   v_ccname_arr.DELETE;
            v_cctype_arr.DELETE;   v_bhold_arr.DELETE;
            v_ccpath_arr.DELETE;   v_dr_arr.DELETE;
            v_cr_arr.DELETE;       v_bdr_arr.DELETE;
            v_bcr_arr.DELETE;      v_curr_arr.DELETE;
            v_xrate_arr.DELETE;    v_fyear_arr.DELETE;
            v_fperiod_arr.DELETE;  v_pstat_arr.DELETE;
            v_stype_arr.DELETE;    v_sid_arr.DELETE;
            v_revjid_arr.DELETE;   v_isic_arr.DELETE;
            v_isrec_arr.DELETE;    v_bplan_arr.DELETE;
            v_bvar_arr.DELETE;     v_approv_arr.DELETE;
            v_status_arr.DELETE;

            FOR i IN 1 .. v_buffer.COUNT LOOP
                v_jl_sk_arr(i)    := v_buffer(i).journal_line_sk;
                v_date_arr(i)     := v_buffer(i).date_id;
                v_acct_sk_arr(i)  := v_buffer(i).account_sk;
                v_jl_id_arr(i)    := v_buffer(i).journal_line_id;
                v_j_id_arr(i)     := v_buffer(i).journal_id;
                v_j_num_arr(i)    := v_buffer(i).journal_num;
                v_j_type_arr(i)   := v_buffer(i).journal_type;
                v_j_date_arr(i)   := v_buffer(i).journal_date;
                v_line_arr(i)     := v_buffer(i).line_num;
                v_aid_arr(i)      := v_buffer(i).account_id;
                v_acode_arr(i)    := v_buffer(i).account_code;
                v_aname_arr(i)    := v_buffer(i).account_name;
                v_atype_arr(i)    := v_buffer(i).account_type;
                v_asub_arr(i)     := v_buffer(i).account_subtype;
                v_nbal_arr(i)     := v_buffer(i).normal_balance;
                v_ipost_arr(i)    := v_buffer(i).is_posting;
                v_apath_arr(i)    := v_buffer(i).account_path;
                v_ccid_arr(i)     := v_buffer(i).cost_center_id;
                v_cccode_arr(i)   := v_buffer(i).cc_code;
                v_ccname_arr(i)   := v_buffer(i).cc_name;
                v_cctype_arr(i)   := v_buffer(i).cc_type;
                v_bhold_arr(i)    := v_buffer(i).budget_holder;
                v_ccpath_arr(i)   := v_buffer(i).cc_hierarchy_path;
                v_dr_arr(i)       := v_buffer(i).debit_amount;
                v_cr_arr(i)       := v_buffer(i).credit_amount;
                v_bdr_arr(i)      := v_buffer(i).base_debit;
                v_bcr_arr(i)      := v_buffer(i).base_credit;
                v_curr_arr(i)     := v_buffer(i).currency_code;
                v_xrate_arr(i)    := v_buffer(i).exchange_rate;
                v_fyear_arr(i)    := v_buffer(i).fiscal_year;
                v_fperiod_arr(i)  := v_buffer(i).fiscal_period;
                v_pstat_arr(i)    := v_buffer(i).period_status;
                v_stype_arr(i)    := v_buffer(i).source_type;
                v_sid_arr(i)      := v_buffer(i).source_id;
                v_revjid_arr(i)   := v_buffer(i).reversing_journal_id;
                v_isic_arr(i)     := v_buffer(i).is_intercompany;
                v_isrec_arr(i)    := v_buffer(i).is_recurring;
                v_bplan_arr(i)    := v_buffer(i).budget_planned;
                v_bvar_arr(i)     := v_buffer(i).budget_variance;
                v_approv_arr(i)   := v_buffer(i).approval_status;
                v_status_arr(i)   := v_buffer(i).status;
            END LOOP;

            BEGIN
                FORALL i IN VALUES OF v_jl_sk_arr SAVE EXCEPTIONS
                    INSERT /*+ APPEND PARALLEL(8) */
                      INTO DWH.FACT_JOURNAL_ENTRIES_STG (
                               journal_line_sk, date_id, account_sk,
                               journal_line_id, journal_id, journal_num,
                               journal_type, journal_date, line_num,
                               account_id, account_code, account_name,
                               account_type, account_subtype,
                               normal_balance, is_posting, account_path,
                               cost_center_id, cc_code, cc_name,
                               cc_type, budget_holder, cc_hierarchy_path,
                               debit_amount, credit_amount,
                               base_debit, base_credit,
                               currency_code, exchange_rate,
                               fiscal_year, fiscal_period, period_status,
                               source_type, source_id,
                               reversing_journal_id,
                               is_intercompany, is_recurring,
                               budget_planned, budget_variance,
                               approval_status, status,
                               created_date
                           )
                    VALUES (
                               v_jl_sk_arr(i), v_date_arr(i), v_acct_sk_arr(i),
                               v_jl_id_arr(i), v_j_id_arr(i), v_j_num_arr(i),
                               v_j_type_arr(i), v_j_date_arr(i), v_line_arr(i),
                               v_aid_arr(i), v_acode_arr(i), v_aname_arr(i),
                               v_atype_arr(i), v_asub_arr(i),
                               v_nbal_arr(i), v_ipost_arr(i), v_apath_arr(i),
                               v_ccid_arr(i), v_cccode_arr(i), v_ccname_arr(i),
                               v_cctype_arr(i), v_bhold_arr(i), v_ccpath_arr(i),
                               v_dr_arr(i), v_cr_arr(i),
                               v_bdr_arr(i), v_bcr_arr(i),
                               v_curr_arr(i), v_xrate_arr(i),
                               v_fyear_arr(i), v_fperiod_arr(i), v_pstat_arr(i),
                               v_stype_arr(i), v_sid_arr(i),
                               v_revjid_arr(i),
                               v_isic_arr(i), v_isrec_arr(i),
                               v_bplan_arr(i), v_bvar_arr(i),
                               v_approv_arr(i), v_status_arr(i),
                               SYSDATE
                           );
            EXCEPTION
                WHEN OTHERS THEN
                    IF SQLCODE = -24381 THEN
                        v_error_count := v_error_count + SQL%BULK_EXCEPTIONS.COUNT;
                        handle_forall_exceptions(v_step, SQL%BULK_EXCEPTIONS.COUNT);
                    ELSE
                        RAISE;
                    END IF;
            END;

            v_total_rows := v_total_rows + v_buffer.COUNT;
            COMMIT;
        END LOOP;

        CLOSE v_cur;

        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_JOURNAL_ENTRIES tgt
        USING (
            WITH cte_stg_ranked AS (
                SELECT s.*,
                       ROW_NUMBER() OVER (
                           PARTITION BY s.journal_line_id
                           ORDER BY s.journal_line_sk DESC
                       ) AS merge_rn
                  FROM DWH.FACT_JOURNAL_ENTRIES_STG s
                 WHERE s.created_date >= TRUNC(SYSDATE)
            ),
            cte_stg_final AS (
                SELECT r.*
                  FROM cte_stg_ranked r
                 WHERE r.merge_rn = 1
            ),
            cte_stg_enriched AS (
                SELECT f.*,
                       SUM(f.debit_amount) OVER (
                           PARTITION BY f.journal_id
                       ) AS journal_total_debit,
                       SUM(f.credit_amount) OVER (
                           PARTITION BY f.journal_id
                       ) AS journal_total_credit,
                       SUM(f.base_debit) OVER (
                           PARTITION BY f.fiscal_year, f.fiscal_period, f.account_id
                       ) AS period_account_debit,
                       SUM(f.base_credit) OVER (
                           PARTITION BY f.fiscal_year, f.fiscal_period, f.account_id
                       ) AS period_account_credit,
                       ROW_NUMBER() OVER (
                           PARTITION BY f.journal_id
                           ORDER BY f.line_num
                       ) AS line_order,
                       RANK() OVER (
                           PARTITION BY f.fiscal_year, f.fiscal_period
                           ORDER BY f.base_debit + f.base_credit DESC
                       ) AS amount_rank,
                       LAG(f.journal_date) OVER (
                           PARTITION BY f.account_id
                           ORDER BY f.journal_date, f.journal_id
                       ) AS prev_posting_date,
                       LEAD(f.journal_date) OVER (
                           PARTITION BY f.account_id
                           ORDER BY f.journal_date, f.journal_id
                       ) AS next_posting_date
                  FROM cte_stg_final f
            ),
            cte_balanced_check AS (
                SELECT e.journal_id,
                       CASE
                           WHEN ABS(e.journal_total_debit - e.journal_total_credit) < 0.01
                           THEN 'Y' ELSE 'N'
                       END AS is_balanced
                  FROM cte_stg_enriched e
                 GROUP BY e.journal_id, e.journal_total_debit, e.journal_total_credit
            )
            SELECT e.journal_line_sk, e.date_id, e.account_sk,
                   e.journal_line_id, e.journal_id, e.journal_num,
                   e.journal_type, e.journal_date, e.line_num,
                   e.account_id, e.account_code, e.account_name,
                   e.account_type, e.cost_center_id, e.cc_code, e.cc_name,
                   e.debit_amount, e.credit_amount,
                   e.base_debit, e.base_credit,
                   e.currency_code, e.exchange_rate,
                   e.fiscal_year, e.fiscal_period,
                   e.source_type, e.source_id,
                   e.reversing_journal_id,
                   e.is_intercompany, e.is_recurring,
                   e.budget_planned, e.budget_variance,
                   e.approval_status, e.status
              FROM cte_stg_enriched e
              LEFT JOIN cte_balanced_check bc
                ON bc.journal_id = e.journal_id
        ) src
        ON (tgt.JOURNAL_LINE_ID = src.journal_line_id)
        WHEN MATCHED THEN
            UPDATE SET
                tgt.DEBIT_AMOUNT  = src.debit_amount,
                tgt.CREDIT_AMOUNT = src.credit_amount,
                tgt.BASE_DEBIT    = src.base_debit,
                tgt.BASE_CREDIT   = src.base_credit,
                tgt.STATUS        = src.status,
                tgt.UPDATED_DATE  = SYSDATE
             WHERE tgt.DEBIT_AMOUNT  <> src.debit_amount
                OR tgt.CREDIT_AMOUNT <> src.credit_amount
                OR tgt.STATUS        <> src.status
        WHEN NOT MATCHED THEN
            INSERT (
                JOURNAL_LINE_SK, DATE_ID, ACCOUNT_SK,
                JOURNAL_LINE_ID, JOURNAL_ID, JOURNAL_NUM,
                JOURNAL_TYPE, JOURNAL_DATE,
                ACCOUNT_ID, ACCOUNT_CODE,
                COST_CENTER_ID, CC_CODE,
                DEBIT_AMOUNT, CREDIT_AMOUNT,
                BASE_DEBIT, BASE_CREDIT,
                CURRENCY_CODE, EXCHANGE_RATE,
                FISCAL_YEAR, FISCAL_PERIOD,
                SOURCE_TYPE, SOURCE_ID,
                IS_INTERCOMPANY, IS_RECURRING,
                STATUS, CREATED_DATE
            )
            VALUES (
                src.journal_line_sk, src.date_id, src.account_sk,
                src.journal_line_id, src.journal_id, src.journal_num,
                src.journal_type, src.journal_date,
                src.account_id, src.account_code,
                src.cost_center_id, src.cc_code,
                src.debit_amount, src.credit_amount,
                src.base_debit, src.base_credit,
                src.currency_code, src.exchange_rate,
                src.fiscal_year, src.fiscal_period,
                src.source_type, src.source_id,
                src.is_intercompany, src.is_recurring,
                src.status, SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;

        log_step(v_step || '.MERGE', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        UPDATE /*+ PARALLEL(8) */ DWH.FACT_JOURNAL_ENTRIES tgt
           SET tgt.ACCOUNT_PERIOD_DEBIT = (
                   SELECT NVL(SUM(fj2.DEBIT_AMOUNT), 0)
                     FROM DWH.FACT_JOURNAL_ENTRIES fj2
                    WHERE fj2.ACCOUNT_ID    = tgt.ACCOUNT_ID
                      AND fj2.FISCAL_YEAR   = tgt.FISCAL_YEAR
                      AND fj2.FISCAL_PERIOD = tgt.FISCAL_PERIOD
                      AND fj2.STATUS = 'POSTED'
               ),
               tgt.ACCOUNT_PERIOD_CREDIT = (
                   SELECT NVL(SUM(fj3.CREDIT_AMOUNT), 0)
                     FROM DWH.FACT_JOURNAL_ENTRIES fj3
                    WHERE fj3.ACCOUNT_ID    = tgt.ACCOUNT_ID
                      AND fj3.FISCAL_YEAR   = tgt.FISCAL_YEAR
                      AND fj3.FISCAL_PERIOD = tgt.FISCAL_PERIOD
                      AND fj3.STATUS = 'POSTED'
               ),
               tgt.CC_PERIOD_NET = (
                   SELECT NVL(SUM(fj4.BASE_DEBIT - fj4.BASE_CREDIT), 0)
                     FROM DWH.FACT_JOURNAL_ENTRIES fj4
                    WHERE fj4.COST_CENTER_ID = tgt.COST_CENTER_ID
                      AND fj4.FISCAL_YEAR    = tgt.FISCAL_YEAR
                      AND fj4.FISCAL_PERIOD  = tgt.FISCAL_PERIOD
                      AND fj4.STATUS = 'POSTED'
               ),
               tgt.JOURNAL_LINE_COUNT = (
                   SELECT COUNT(*)
                     FROM DWH.FACT_JOURNAL_ENTRIES fj5
                    WHERE fj5.JOURNAL_ID = tgt.JOURNAL_ID
               ),
               tgt.IS_BALANCED = (
                   SELECT CASE
                              WHEN ABS(SUM(fj6.DEBIT_AMOUNT) - SUM(fj6.CREDIT_AMOUNT)) < 0.01
                              THEN 'Y' ELSE 'N'
                          END
                     FROM DWH.FACT_JOURNAL_ENTRIES fj6
                    WHERE fj6.JOURNAL_ID = tgt.JOURNAL_ID
               ),
               tgt.UPDATED_DATE = SYSDATE
         WHERE tgt.JOURNAL_LINE_ID IN (
                   SELECT DISTINCT s4.journal_line_id
                     FROM DWH.FACT_JOURNAL_ENTRIES_STG s4
                    WHERE s4.created_date >= TRUNC(SYSDATE)
               );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.POST_UPDATE_AGGREGATES', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_JOURNAL_ACCOUNT_SUMMARY tgt
        USING (
            WITH cte_je_base AS (
                SELECT fj.ACCOUNT_ID,
                       fj.ACCOUNT_CODE,
                       fj.ACCOUNT_NAME,
                       fj.ACCOUNT_TYPE,
                       fj.FISCAL_YEAR,
                       fj.FISCAL_PERIOD,
                       fj.JOURNAL_LINE_ID,
                       fj.JOURNAL_ID,
                       fj.DEBIT_AMOUNT,
                       fj.CREDIT_AMOUNT,
                       fj.BASE_DEBIT,
                       fj.BASE_CREDIT,
                       fj.IS_INTERCOMPANY,
                       fj.IS_RECURRING,
                       fj.SOURCE_TYPE,
                       ROW_NUMBER() OVER (
                           PARTITION BY fj.JOURNAL_LINE_ID
                           ORDER BY fj.JOURNAL_LINE_SK DESC
                       ) AS je_rn
                  FROM DWH.FACT_JOURNAL_ENTRIES fj
                 WHERE fj.STATUS = 'POSTED'
            ),
            cte_je_dedup AS (
                SELECT jb.*
                  FROM cte_je_base jb
                 WHERE jb.je_rn = 1
            ),
            cte_source_breakdown AS (
                SELECT jd.ACCOUNT_ID,
                       jd.FISCAL_YEAR,
                       jd.FISCAL_PERIOD,
                       jd.SOURCE_TYPE,
                       SUM(NVL(jd.BASE_DEBIT, 0))  AS src_debit,
                       SUM(NVL(jd.BASE_CREDIT, 0)) AS src_credit,
                       COUNT(DISTINCT jd.JOURNAL_ID) AS src_journal_count
                  FROM cte_je_dedup jd
                 GROUP BY jd.ACCOUNT_ID, jd.FISCAL_YEAR, jd.FISCAL_PERIOD, jd.SOURCE_TYPE
            ),
            cte_ic_breakdown AS (
                SELECT jd2.ACCOUNT_ID,
                       jd2.FISCAL_YEAR,
                       jd2.FISCAL_PERIOD,
                       SUM(CASE WHEN jd2.IS_INTERCOMPANY = 'Y'
                                THEN NVL(jd2.BASE_DEBIT, 0) - NVL(jd2.BASE_CREDIT, 0)
                                ELSE 0 END) AS ic_net_amount,
                       COUNT(CASE WHEN jd2.IS_INTERCOMPANY = 'Y'
                                  THEN jd2.JOURNAL_LINE_ID END) AS ic_line_count
                  FROM cte_je_dedup jd2
                 GROUP BY jd2.ACCOUNT_ID, jd2.FISCAL_YEAR, jd2.FISCAL_PERIOD
            ),
            cte_recurring_breakdown AS (
                SELECT jd3.ACCOUNT_ID,
                       jd3.FISCAL_YEAR,
                       jd3.FISCAL_PERIOD,
                       SUM(CASE WHEN jd3.IS_RECURRING = 'Y'
                                THEN NVL(jd3.BASE_DEBIT, 0) - NVL(jd3.BASE_CREDIT, 0)
                                ELSE 0 END) AS recurring_net,
                       COUNT(CASE WHEN jd3.IS_RECURRING = 'Y'
                                  THEN jd3.JOURNAL_LINE_ID END) AS recurring_count
                  FROM cte_je_dedup jd3
                 GROUP BY jd3.ACCOUNT_ID, jd3.FISCAL_YEAR, jd3.FISCAL_PERIOD
            ),
            cte_account_agg AS (
                SELECT jd.ACCOUNT_ID,
                       jd.ACCOUNT_CODE,
                       MAX(jd.ACCOUNT_NAME)       AS account_name,
                       MAX(jd.ACCOUNT_TYPE)       AS account_type,
                       jd.FISCAL_YEAR,
                       jd.FISCAL_PERIOD,
                       COUNT(DISTINCT jd.JOURNAL_ID)     AS journal_count,
                       COUNT(jd.JOURNAL_LINE_ID)         AS line_count,
                       SUM(NVL(jd.DEBIT_AMOUNT, 0))     AS total_debit,
                       SUM(NVL(jd.CREDIT_AMOUNT, 0))    AS total_credit,
                       SUM(NVL(jd.BASE_DEBIT, 0))       AS base_total_debit,
                       SUM(NVL(jd.BASE_CREDIT, 0))      AS base_total_credit,
                       SUM(NVL(jd.BASE_DEBIT, 0)
                         - NVL(jd.BASE_CREDIT, 0))      AS net_amount,
                       AVG(NVL(jd.BASE_DEBIT, 0)
                         + NVL(jd.BASE_CREDIT, 0))      AS avg_line_amount,
                       MAX(NVL(jd.BASE_DEBIT, 0)
                         + NVL(jd.BASE_CREDIT, 0))      AS max_line_amount,
                       NVL(ic.ic_net_amount, 0)          AS ic_net_amount,
                       NVL(ic.ic_line_count, 0)          AS ic_line_count,
                       NVL(rc.recurring_net, 0)          AS recurring_net,
                       NVL(rc.recurring_count, 0)        AS recurring_count,
                       LAG(SUM(NVL(jd.BASE_DEBIT, 0)
                         - NVL(jd.BASE_CREDIT, 0))) OVER (
                           PARTITION BY jd.ACCOUNT_ID, jd.FISCAL_YEAR
                           ORDER BY jd.FISCAL_PERIOD
                       ) AS prev_period_net,
                       SUM(SUM(NVL(jd.BASE_DEBIT, 0)
                         - NVL(jd.BASE_CREDIT, 0))) OVER (
                           PARTITION BY jd.ACCOUNT_ID, jd.FISCAL_YEAR
                           ORDER BY jd.FISCAL_PERIOD
                           ROWS UNBOUNDED PRECEDING
                       ) AS ytd_net,
                       RANK() OVER (
                           PARTITION BY jd.FISCAL_YEAR, jd.FISCAL_PERIOD
                           ORDER BY ABS(SUM(NVL(jd.BASE_DEBIT, 0)
                             - NVL(jd.BASE_CREDIT, 0))) DESC
                       ) AS activity_rank
                  FROM cte_je_dedup jd
                  LEFT JOIN cte_ic_breakdown ic
                    ON ic.ACCOUNT_ID    = jd.ACCOUNT_ID
                   AND ic.FISCAL_YEAR   = jd.FISCAL_YEAR
                   AND ic.FISCAL_PERIOD = jd.FISCAL_PERIOD
                  LEFT JOIN cte_recurring_breakdown rc
                    ON rc.ACCOUNT_ID    = jd.ACCOUNT_ID
                   AND rc.FISCAL_YEAR   = jd.FISCAL_YEAR
                   AND rc.FISCAL_PERIOD = jd.FISCAL_PERIOD
                 GROUP BY jd.ACCOUNT_ID, jd.ACCOUNT_CODE,
                          jd.FISCAL_YEAR, jd.FISCAL_PERIOD,
                          ic.ic_net_amount, ic.ic_line_count,
                          rc.recurring_net, rc.recurring_count
            )
            SELECT aa.ACCOUNT_ID, aa.ACCOUNT_CODE, aa.account_name,
                   aa.account_type, aa.FISCAL_YEAR, aa.FISCAL_PERIOD,
                   aa.journal_count, aa.line_count,
                   aa.total_debit, aa.total_credit,
                   aa.base_total_debit, aa.base_total_credit,
                   aa.net_amount, aa.avg_line_amount, aa.max_line_amount,
                   aa.ic_net_amount, aa.ic_line_count,
                   aa.recurring_net, aa.recurring_count,
                   aa.prev_period_net, aa.ytd_net, aa.activity_rank
              FROM cte_account_agg aa
        ) src
        ON (    tgt.ACCOUNT_ID    = src.ACCOUNT_ID
            AND tgt.FISCAL_YEAR   = src.FISCAL_YEAR
            AND tgt.FISCAL_PERIOD = src.FISCAL_PERIOD)
        WHEN MATCHED THEN
            UPDATE SET
                tgt.JOURNAL_COUNT     = src.journal_count,
                tgt.LINE_COUNT        = src.line_count,
                tgt.TOTAL_DEBIT       = src.total_debit,
                tgt.TOTAL_CREDIT      = src.total_credit,
                tgt.NET_AMOUNT        = src.net_amount,
                tgt.IC_NET_AMOUNT     = src.ic_net_amount,
                tgt.RECURRING_NET     = src.recurring_net,
                tgt.YTD_NET           = src.ytd_net,
                tgt.ACTIVITY_RANK     = src.activity_rank,
                tgt.UPDATED_DATE      = SYSDATE
             WHERE tgt.NET_AMOUNT <> src.net_amount
                OR tgt.TOTAL_DEBIT <> src.total_debit
        WHEN NOT MATCHED THEN
            INSERT (
                ACCOUNT_ID, ACCOUNT_CODE, ACCOUNT_NAME, ACCOUNT_TYPE,
                FISCAL_YEAR, FISCAL_PERIOD,
                JOURNAL_COUNT, LINE_COUNT,
                TOTAL_DEBIT, TOTAL_CREDIT,
                BASE_TOTAL_DEBIT, BASE_TOTAL_CREDIT,
                NET_AMOUNT, AVG_LINE_AMOUNT, MAX_LINE_AMOUNT,
                IC_NET_AMOUNT, IC_LINE_COUNT,
                RECURRING_NET, RECURRING_COUNT,
                PREV_PERIOD_NET, YTD_NET, ACTIVITY_RANK,
                CREATED_DATE
            )
            VALUES (
                src.ACCOUNT_ID, src.ACCOUNT_CODE, src.account_name, src.account_type,
                src.FISCAL_YEAR, src.FISCAL_PERIOD,
                src.journal_count, src.line_count,
                src.total_debit, src.total_credit,
                src.base_total_debit, src.base_total_credit,
                src.net_amount, src.avg_line_amount, src.max_line_amount,
                src.ic_net_amount, src.ic_line_count,
                src.recurring_net, src.recurring_count,
                src.prev_period_net, src.ytd_net, src.activity_rank,
                SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.ACCOUNT_SUMMARY', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_JOURNAL_CC_SUMMARY tgt
        USING (
            WITH cte_cc_base AS (
                SELECT fj.COST_CENTER_ID,
                       fj.CC_CODE,
                       fj.CC_NAME,
                       fj.FISCAL_YEAR,
                       fj.FISCAL_PERIOD,
                       fj.JOURNAL_LINE_ID,
                       fj.JOURNAL_ID,
                       fj.ACCOUNT_TYPE,
                       fj.DEBIT_AMOUNT,
                       fj.CREDIT_AMOUNT,
                       fj.BASE_DEBIT,
                       fj.BASE_CREDIT,
                       fj.IS_INTERCOMPANY,
                       ROW_NUMBER() OVER (
                           PARTITION BY fj.JOURNAL_LINE_ID
                           ORDER BY fj.JOURNAL_LINE_SK DESC
                       ) AS cc_rn
                  FROM DWH.FACT_JOURNAL_ENTRIES fj
                 WHERE fj.STATUS = 'POSTED'
                   AND fj.COST_CENTER_ID IS NOT NULL
            ),
            cte_cc_dedup AS (
                SELECT cb.*
                  FROM cte_cc_base cb
                 WHERE cb.cc_rn = 1
            ),
            cte_budget_by_cc AS (
                SELECT bl.COST_CENTER_ID,
                       bl.FISCAL_PERIOD_ID,
                       SUM(NVL(bl.REVISED_AMOUNT, bl.PLANNED_AMOUNT)) AS budget_total
                  FROM FIN.BUDGET_LINES bl
                  JOIN FIN.BUDGETS b
                    ON b.BUDGET_ID = bl.BUDGET_ID
                 WHERE b.STATUS IN ('APPROVED', 'FINAL')
                 GROUP BY bl.COST_CENTER_ID, bl.FISCAL_PERIOD_ID
            ),
            cte_cc_agg AS (
                SELECT cd.COST_CENTER_ID,
                       cd.CC_CODE,
                       MAX(cd.CC_NAME) AS cc_name,
                       cd.FISCAL_YEAR,
                       cd.FISCAL_PERIOD,
                       COUNT(DISTINCT cd.JOURNAL_ID)     AS journal_count,
                       COUNT(cd.JOURNAL_LINE_ID)         AS line_count,
                       SUM(NVL(cd.BASE_DEBIT, 0))       AS total_debit,
                       SUM(NVL(cd.BASE_CREDIT, 0))      AS total_credit,
                       SUM(NVL(cd.BASE_DEBIT, 0)
                         - NVL(cd.BASE_CREDIT, 0))      AS net_amount,
                       SUM(CASE WHEN cd.ACCOUNT_TYPE = 'EXPENSE'
                                THEN NVL(cd.BASE_DEBIT, 0) - NVL(cd.BASE_CREDIT, 0)
                                ELSE 0 END)              AS expense_amount,
                       SUM(CASE WHEN cd.ACCOUNT_TYPE = 'REVENUE'
                                THEN NVL(cd.BASE_CREDIT, 0) - NVL(cd.BASE_DEBIT, 0)
                                ELSE 0 END)              AS revenue_amount,
                       SUM(CASE WHEN cd.IS_INTERCOMPANY = 'Y'
                                THEN NVL(cd.BASE_DEBIT, 0) - NVL(cd.BASE_CREDIT, 0)
                                ELSE 0 END)              AS ic_net,
                       LAG(SUM(NVL(cd.BASE_DEBIT, 0) - NVL(cd.BASE_CREDIT, 0))) OVER (
                           PARTITION BY cd.COST_CENTER_ID, cd.FISCAL_YEAR
                           ORDER BY cd.FISCAL_PERIOD
                       ) AS prev_period_net,
                       SUM(SUM(NVL(cd.BASE_DEBIT, 0) - NVL(cd.BASE_CREDIT, 0))) OVER (
                           PARTITION BY cd.COST_CENTER_ID, cd.FISCAL_YEAR
                           ORDER BY cd.FISCAL_PERIOD
                           ROWS UNBOUNDED PRECEDING
                       ) AS ytd_net,
                       RANK() OVER (
                           PARTITION BY cd.FISCAL_YEAR, cd.FISCAL_PERIOD
                           ORDER BY ABS(SUM(NVL(cd.BASE_DEBIT, 0)
                             - NVL(cd.BASE_CREDIT, 0))) DESC
                       ) AS cc_activity_rank
                  FROM cte_cc_dedup cd
                 GROUP BY cd.COST_CENTER_ID, cd.CC_CODE,
                          cd.FISCAL_YEAR, cd.FISCAL_PERIOD
            )
            SELECT ca.COST_CENTER_ID, ca.CC_CODE, ca.cc_name,
                   ca.FISCAL_YEAR, ca.FISCAL_PERIOD,
                   ca.journal_count, ca.line_count,
                   ca.total_debit, ca.total_credit, ca.net_amount,
                   ca.expense_amount, ca.revenue_amount, ca.ic_net,
                   ca.prev_period_net, ca.ytd_net, ca.cc_activity_rank
              FROM cte_cc_agg ca
        ) src
        ON (    tgt.COST_CENTER_ID = src.COST_CENTER_ID
            AND tgt.FISCAL_YEAR    = src.FISCAL_YEAR
            AND tgt.FISCAL_PERIOD  = src.FISCAL_PERIOD)
        WHEN MATCHED THEN
            UPDATE SET
                tgt.JOURNAL_COUNT  = src.journal_count,
                tgt.LINE_COUNT     = src.line_count,
                tgt.TOTAL_DEBIT    = src.total_debit,
                tgt.TOTAL_CREDIT   = src.total_credit,
                tgt.NET_AMOUNT     = src.net_amount,
                tgt.EXPENSE_AMOUNT = src.expense_amount,
                tgt.REVENUE_AMOUNT = src.revenue_amount,
                tgt.IC_NET         = src.ic_net,
                tgt.YTD_NET        = src.ytd_net,
                tgt.CC_ACTIVITY_RANK = src.cc_activity_rank,
                tgt.UPDATED_DATE   = SYSDATE
             WHERE tgt.NET_AMOUNT  <> src.net_amount
                OR tgt.TOTAL_DEBIT <> src.total_debit
        WHEN NOT MATCHED THEN
            INSERT (
                COST_CENTER_ID, CC_CODE, CC_NAME,
                FISCAL_YEAR, FISCAL_PERIOD,
                JOURNAL_COUNT, LINE_COUNT,
                TOTAL_DEBIT, TOTAL_CREDIT, NET_AMOUNT,
                EXPENSE_AMOUNT, REVENUE_AMOUNT, IC_NET,
                PREV_PERIOD_NET, YTD_NET, CC_ACTIVITY_RANK,
                CREATED_DATE
            )
            VALUES (
                src.COST_CENTER_ID, src.CC_CODE, src.cc_name,
                src.FISCAL_YEAR, src.FISCAL_PERIOD,
                src.journal_count, src.line_count,
                src.total_debit, src.total_credit, src.net_amount,
                src.expense_amount, src.revenue_amount, src.ic_net,
                src.prev_period_net, src.ytd_net, src.cc_activity_rank,
                SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.CC_SUMMARY', 'DONE', g_row_count);

        log_step(v_step || '.COMPLETE', 'SUCCESS', v_total_rows);

    EXCEPTION
        WHEN OTHERS THEN
            IF v_cur%ISOPEN THEN CLOSE v_cur; END IF;
            ROLLBACK;
            log_step(v_step || '.FATAL', 'ERROR', 0, SQLERRM);
            RAISE;
    END load_fact_journal_entries;

    -- =========================================================================
    -- 4. LOAD_FACT_BUDGET_VARIANCE
    -- =========================================================================
    PROCEDURE load_fact_budget_variance (
        p_fiscal_year   IN NUMBER,
        p_period_from   IN NUMBER DEFAULT 1,
        p_period_to     IN NUMBER DEFAULT 12,
        p_mode          IN VARCHAR2 DEFAULT 'INCREMENTAL'
    ) IS
        v_step          VARCHAR2(200) := 'LOAD_FACT_BUDGET_VARIANCE';
        v_total_rows    NUMBER(19) := 0;
        v_error_count   PLS_INTEGER := 0;
        v_buffer        t_budget_var_tab;
        v_func_curr     VARCHAR2(3);

        TYPE t_bv_cursor IS REF CURSOR;
        v_cur           t_bv_cursor;

        v_bv_sk_arr      t_id_array;
        v_date_arr       t_id_array;
        v_acct_sk_arr    t_id_array;
        v_bl_id_arr      t_id_array;
        v_b_id_arr       t_id_array;
        v_bcode_arr      t_varchar_array;
        v_bname_arr      t_varchar_array;
        v_btype_arr      t_varchar_array;
        v_fyear_arr      t_id_array;
        v_fperiod_arr    t_id_array;
        v_aid_arr        t_id_array;
        v_acode_arr      t_varchar_array;
        v_aname_arr      t_varchar_array;
        v_ccid_arr       t_id_array;
        v_cccode_arr     t_varchar_array;
        v_ccname_arr     t_varchar_array;
        v_plan_arr       t_amount_array;
        v_rev_arr        t_amount_array;
        v_act_arr        t_amount_array;
        v_enc_arr        t_amount_array;
        v_var_arr        t_amount_array;
        v_vpct_arr       t_amount_array;
        v_ytdp_arr       t_amount_array;
        v_ytda_arr       t_amount_array;
        v_ytdv_arr       t_amount_array;
        v_rrp_arr        t_amount_array;
        v_fac_arr        t_amount_array;
        v_pya_arr        t_amount_array;
        v_pyv_arr        t_amount_array;
        v_vrank_arr      t_id_array;
        v_status_arr     t_varchar_array;
    BEGIN
        init_run(v_step);
        v_func_curr := get_functional_currency;

        IF p_mode = 'FULL' THEN
            EXECUTE IMMEDIATE 'TRUNCATE TABLE DWH.FACT_BUDGET_VARIANCE_STG';
            g_step_start := SYSTIMESTAMP;
            log_step(v_step || '.TRUNCATE', 'DONE');
        END IF;

        g_step_start := SYSTIMESTAMP;

        OPEN v_cur FOR
            WITH cte_budgets AS (
                SELECT b.BUDGET_ID,
                       b.BUDGET_CODE,
                       b.BUDGET_NAME,
                       b.FISCAL_YEAR,
                       b.BUDGET_TYPE,
                       b.STATUS AS budget_status
                  FROM FIN.BUDGETS b
                 WHERE b.FISCAL_YEAR = p_fiscal_year
                   AND b.STATUS IN ('APPROVED', 'FINAL')
            ),
            cte_budget_lines AS (
                SELECT bl.BUDGET_LINE_ID,
                       bl.BUDGET_ID,
                       bl.ACCOUNT_ID,
                       bl.COST_CENTER_ID,
                       bl.FISCAL_PERIOD_ID,
                       bl.PLANNED_AMOUNT,
                       bl.REVISED_AMOUNT,
                       bl.ACTUAL_AMOUNT   AS bl_actual,
                       bl.ENCUMBERED_AMOUNT
                  FROM FIN.BUDGET_LINES bl
                 WHERE bl.BUDGET_ID IN (
                           SELECT cb.BUDGET_ID FROM cte_budgets cb
                       )
            ),
            cte_fiscal AS (
                SELECT fp.FISCAL_PERIOD_ID,
                       fp.FISCAL_YEAR,
                       fp.PERIOD_NUMBER,
                       fp.PERIOD_NAME,
                       fp.START_DATE,
                       fp.END_DATE,
                       fp.STATUS AS period_status
                  FROM FIN.FISCAL_PERIODS fp
                 WHERE fp.FISCAL_YEAR = p_fiscal_year
                   AND fp.PERIOD_NUMBER BETWEEN p_period_from AND p_period_to
            ),
            cte_actual_amounts AS (
                SELECT jl.ACCOUNT_ID,
                       jl.COST_CENTER_ID,
                       j.FISCAL_PERIOD_ID,
                       SUM(NVL(jl.BASE_DEBIT, 0))  AS actual_debit,
                       SUM(NVL(jl.BASE_CREDIT, 0)) AS actual_credit,
                       SUM(NVL(jl.BASE_DEBIT, 0)
                         - NVL(jl.BASE_CREDIT, 0))  AS actual_net
                  FROM FIN.JOURNAL_LINES jl
                  JOIN FIN.JOURNALS j
                    ON j.JOURNAL_ID = jl.JOURNAL_ID
                 WHERE j.STATUS = 'POSTED'
                   AND j.FISCAL_PERIOD_ID IN (
                           SELECT cf.FISCAL_PERIOD_ID FROM cte_fiscal cf
                       )
                 GROUP BY jl.ACCOUNT_ID, jl.COST_CENTER_ID, j.FISCAL_PERIOD_ID
            ),
            cte_encumbered AS (
                SELECT jl2.ACCOUNT_ID,
                       jl2.COST_CENTER_ID,
                       j2.FISCAL_PERIOD_ID,
                       SUM(NVL(jl2.BASE_DEBIT, 0)
                         - NVL(jl2.BASE_CREDIT, 0)) AS encumbered_amount
                  FROM FIN.JOURNAL_LINES jl2
                  JOIN FIN.JOURNALS j2
                    ON j2.JOURNAL_ID = jl2.JOURNAL_ID
                 WHERE j2.STATUS = 'POSTED'
                   AND j2.JOURNAL_TYPE = 'ENCUMBRANCE'
                   AND j2.FISCAL_PERIOD_ID IN (
                           SELECT cf2.FISCAL_PERIOD_ID FROM cte_fiscal cf2
                       )
                 GROUP BY jl2.ACCOUNT_ID, jl2.COST_CENTER_ID, j2.FISCAL_PERIOD_ID
            ),
            cte_accounts AS (
                SELECT a.ACCOUNT_ID,
                       a.ACCOUNT_CODE,
                       a.ACCOUNT_NAME,
                       a.ACCOUNT_TYPE,
                       a.NORMAL_BALANCE
                  FROM FIN.ACCOUNTS a
                 WHERE a.IS_ACTIVE = 'Y'
                   AND a.IS_POSTING = 'Y'
            ),
            cte_cost_centers AS (
                SELECT cc.COST_CENTER_ID,
                       cc.CC_CODE,
                       cc.CC_NAME,
                       cc.CC_TYPE,
                       cc.BUDGET_HOLDER
                  FROM FIN.COST_CENTERS cc
            ),
            cte_account_sk AS (
                SELECT da.ACCOUNT_SK,
                       da.ACCOUNT_ID,
                       da.ACCOUNT_CODE
                  FROM DWH.DIM_ACCOUNT da
            ),
            cte_date_dim AS (
                SELECT dd.DATE_ID,
                       dd.FULL_DATE
                  FROM DWH.DIM_DATE dd
                 WHERE EXTRACT(YEAR FROM dd.FULL_DATE) = p_fiscal_year
            ),
            cte_ytd_planned AS (
                SELECT bl_ytd.ACCOUNT_ID,
                       bl_ytd.COST_CENTER_ID,
                       fp_ytd.PERIOD_NUMBER AS through_period,
                       SUM(NVL(bl_ytd.REVISED_AMOUNT, bl_ytd.PLANNED_AMOUNT))
                           AS ytd_plan
                  FROM FIN.BUDGET_LINES bl_ytd
                  JOIN FIN.BUDGETS b_ytd
                    ON b_ytd.BUDGET_ID = bl_ytd.BUDGET_ID
                  JOIN FIN.FISCAL_PERIODS fp_ytd
                    ON fp_ytd.FISCAL_PERIOD_ID = bl_ytd.FISCAL_PERIOD_ID
                 WHERE b_ytd.FISCAL_YEAR = p_fiscal_year
                   AND b_ytd.STATUS IN ('APPROVED', 'FINAL')
                   AND fp_ytd.PERIOD_NUMBER <= p_period_to
                 GROUP BY bl_ytd.ACCOUNT_ID, bl_ytd.COST_CENTER_ID, fp_ytd.PERIOD_NUMBER
            ),
            cte_ytd_actual AS (
                SELECT jl_ytd.ACCOUNT_ID,
                       jl_ytd.COST_CENTER_ID,
                       fp_ytda.PERIOD_NUMBER AS through_period,
                       SUM(NVL(jl_ytd.BASE_DEBIT, 0)
                         - NVL(jl_ytd.BASE_CREDIT, 0)) AS ytd_actual
                  FROM FIN.JOURNAL_LINES jl_ytd
                  JOIN FIN.JOURNALS j_ytd
                    ON j_ytd.JOURNAL_ID = jl_ytd.JOURNAL_ID
                  JOIN FIN.FISCAL_PERIODS fp_ytda
                    ON fp_ytda.FISCAL_PERIOD_ID = j_ytd.FISCAL_PERIOD_ID
                 WHERE j_ytd.STATUS = 'POSTED'
                   AND fp_ytda.FISCAL_YEAR = p_fiscal_year
                   AND fp_ytda.PERIOD_NUMBER <= p_period_to
                 GROUP BY jl_ytd.ACCOUNT_ID, jl_ytd.COST_CENTER_ID, fp_ytda.PERIOD_NUMBER
            ),
            cte_prior_year AS (
                SELECT jl_py.ACCOUNT_ID,
                       jl_py.COST_CENTER_ID,
                       fp_py.PERIOD_NUMBER,
                       SUM(NVL(jl_py.BASE_DEBIT, 0)
                         - NVL(jl_py.BASE_CREDIT, 0)) AS py_actual
                  FROM FIN.JOURNAL_LINES jl_py
                  JOIN FIN.JOURNALS j_py
                    ON j_py.JOURNAL_ID = jl_py.JOURNAL_ID
                  JOIN FIN.FISCAL_PERIODS fp_py
                    ON fp_py.FISCAL_PERIOD_ID = j_py.FISCAL_PERIOD_ID
                 WHERE j_py.STATUS = 'POSTED'
                   AND fp_py.FISCAL_YEAR = p_fiscal_year - 1
                 GROUP BY jl_py.ACCOUNT_ID, jl_py.COST_CENTER_ID, fp_py.PERIOD_NUMBER
            ),
            cte_combined AS (
                SELECT bl.BUDGET_LINE_ID,
                       bl.BUDGET_ID,
                       bud.BUDGET_CODE,
                       bud.BUDGET_NAME,
                       bud.BUDGET_TYPE,
                       fis.FISCAL_YEAR,
                       fis.PERIOD_NUMBER                       AS fiscal_period,
                       bl.ACCOUNT_ID,
                       act.ACCOUNT_CODE,
                       act.ACCOUNT_NAME,
                       bl.COST_CENTER_ID,
                       cctr.CC_CODE,
                       cctr.CC_NAME,
                       NVL(bl.PLANNED_AMOUNT, 0)               AS planned_amount,
                       NVL(bl.REVISED_AMOUNT, bl.PLANNED_AMOUNT) AS revised_amount,
                       NVL(aa.actual_net, 0)                   AS actual_amount,
                       NVL(enc.encumbered_amount, 0)           AS encumbered_amount,
                       NVL(bl.REVISED_AMOUNT, bl.PLANNED_AMOUNT)
                           - NVL(aa.actual_net, 0)             AS variance_amount,
                       CASE
                           WHEN NVL(bl.REVISED_AMOUNT, bl.PLANNED_AMOUNT) <> 0
                           THEN ROUND(
                               (NVL(bl.REVISED_AMOUNT, bl.PLANNED_AMOUNT)
                                - NVL(aa.actual_net, 0))
                               / ABS(NVL(bl.REVISED_AMOUNT, bl.PLANNED_AMOUNT))
                               * 100, 4
                           )
                           ELSE 0
                       END                                     AS variance_pct,
                       NVL(ytdp.ytd_plan, 0)                   AS ytd_planned,
                       NVL(ytda.ytd_actual, 0)                 AS ytd_actual,
                       NVL(ytdp.ytd_plan, 0)
                           - NVL(ytda.ytd_actual, 0)           AS ytd_variance,
                       CASE
                           WHEN fis.PERIOD_NUMBER > 0
                           THEN ROUND(
                               NVL(ytda.ytd_actual, 0)
                               / fis.PERIOD_NUMBER * 12, 4
                           )
                           ELSE 0
                       END                                     AS run_rate_proj,
                       NVL(ytda.ytd_actual, 0)
                           + NVL(enc.encumbered_amount, 0)
                           + (12 - fis.PERIOD_NUMBER)
                             * (NVL(ytda.ytd_actual, 0)
                                / GREATEST(fis.PERIOD_NUMBER, 1))
                                                                AS forecast_at_compl,
                       NVL(py.py_actual, 0)                    AS prior_year_actual,
                       NVL(aa.actual_net, 0)
                           - NVL(py.py_actual, 0)              AS prior_year_variance,
                       RANK() OVER (
                           PARTITION BY fis.FISCAL_YEAR, fis.PERIOD_NUMBER
                           ORDER BY ABS(
                               NVL(bl.REVISED_AMOUNT, bl.PLANNED_AMOUNT)
                               - NVL(aa.actual_net, 0)
                           ) DESC
                       )                                        AS variance_rank,
                       bud.budget_status                        AS status
                  FROM cte_budget_lines bl
                  JOIN cte_budgets bud
                    ON bud.BUDGET_ID = bl.BUDGET_ID
                  JOIN cte_fiscal fis
                    ON fis.FISCAL_PERIOD_ID = bl.FISCAL_PERIOD_ID
                  LEFT JOIN cte_accounts act
                    ON act.ACCOUNT_ID = bl.ACCOUNT_ID
                  LEFT JOIN cte_cost_centers cctr
                    ON cctr.COST_CENTER_ID = bl.COST_CENTER_ID
                  LEFT JOIN cte_actual_amounts aa
                    ON aa.ACCOUNT_ID = bl.ACCOUNT_ID
                   AND aa.COST_CENTER_ID = bl.COST_CENTER_ID
                   AND aa.FISCAL_PERIOD_ID = bl.FISCAL_PERIOD_ID
                  LEFT JOIN cte_encumbered enc
                    ON enc.ACCOUNT_ID = bl.ACCOUNT_ID
                   AND enc.COST_CENTER_ID = bl.COST_CENTER_ID
                   AND enc.FISCAL_PERIOD_ID = bl.FISCAL_PERIOD_ID
                  LEFT JOIN cte_ytd_planned ytdp
                    ON ytdp.ACCOUNT_ID = bl.ACCOUNT_ID
                   AND ytdp.COST_CENTER_ID = bl.COST_CENTER_ID
                   AND ytdp.through_period = fis.PERIOD_NUMBER
                  LEFT JOIN cte_ytd_actual ytda
                    ON ytda.ACCOUNT_ID = bl.ACCOUNT_ID
                   AND ytda.COST_CENTER_ID = bl.COST_CENTER_ID
                   AND ytda.through_period = fis.PERIOD_NUMBER
                  LEFT JOIN cte_prior_year py
                    ON py.ACCOUNT_ID = bl.ACCOUNT_ID
                   AND py.COST_CENTER_ID = bl.COST_CENTER_ID
                   AND py.PERIOD_NUMBER = fis.PERIOD_NUMBER
            )
            SELECT /*+ PARALLEL(8) */
                   DWH.SEQ_DWH_BUDGET_VAR.NEXTVAL     AS budget_var_sk,
                   NVL(ddt.DATE_ID, -1)                AS date_id,
                   NVL(ask_lu.ACCOUNT_SK, -1)          AS account_sk,
                   c.BUDGET_LINE_ID,
                   c.BUDGET_ID,
                   c.BUDGET_CODE,
                   c.BUDGET_NAME,
                   c.BUDGET_TYPE,
                   c.FISCAL_YEAR,
                   c.fiscal_period,
                   c.ACCOUNT_ID,
                   c.ACCOUNT_CODE,
                   c.ACCOUNT_NAME,
                   c.COST_CENTER_ID,
                   c.CC_CODE,
                   c.CC_NAME,
                   c.planned_amount,
                   c.revised_amount,
                   c.actual_amount,
                   c.encumbered_amount,
                   c.variance_amount,
                   c.variance_pct,
                   c.ytd_planned,
                   c.ytd_actual,
                   c.ytd_variance,
                   c.run_rate_proj,
                   c.forecast_at_compl,
                   c.prior_year_actual,
                   c.prior_year_variance,
                   c.variance_rank,
                   c.status
              FROM cte_combined c
              LEFT JOIN cte_account_sk ask_lu
                ON ask_lu.ACCOUNT_ID = c.ACCOUNT_ID
              LEFT JOIN cte_date_dim ddt
                ON ddt.FULL_DATE = (
                       SELECT MIN(fp2.START_DATE)
                         FROM FIN.FISCAL_PERIODS fp2
                        WHERE fp2.FISCAL_YEAR = c.FISCAL_YEAR
                          AND fp2.PERIOD_NUMBER = c.fiscal_period
                   )
             WHERE NOT EXISTS (
                       SELECT 1
                         FROM DWH.FACT_BUDGET_VARIANCE fbv
                        WHERE fbv.BUDGET_LINE_ID = c.BUDGET_LINE_ID
                          AND p_mode = 'INCREMENTAL'
                   )
             ORDER BY c.FISCAL_YEAR, c.fiscal_period, c.ACCOUNT_CODE, c.CC_CODE;

        LOOP
            FETCH v_cur BULK COLLECT INTO v_buffer LIMIT gc_batch_limit;
            EXIT WHEN v_buffer.COUNT = 0;

            v_bv_sk_arr.DELETE;    v_date_arr.DELETE;
            v_acct_sk_arr.DELETE;  v_bl_id_arr.DELETE;
            v_b_id_arr.DELETE;     v_bcode_arr.DELETE;
            v_bname_arr.DELETE;    v_btype_arr.DELETE;
            v_fyear_arr.DELETE;    v_fperiod_arr.DELETE;
            v_aid_arr.DELETE;      v_acode_arr.DELETE;
            v_aname_arr.DELETE;    v_ccid_arr.DELETE;
            v_cccode_arr.DELETE;   v_ccname_arr.DELETE;
            v_plan_arr.DELETE;     v_rev_arr.DELETE;
            v_act_arr.DELETE;      v_enc_arr.DELETE;
            v_var_arr.DELETE;      v_vpct_arr.DELETE;
            v_ytdp_arr.DELETE;     v_ytda_arr.DELETE;
            v_ytdv_arr.DELETE;     v_rrp_arr.DELETE;
            v_fac_arr.DELETE;      v_pya_arr.DELETE;
            v_pyv_arr.DELETE;      v_vrank_arr.DELETE;
            v_status_arr.DELETE;

            FOR i IN 1 .. v_buffer.COUNT LOOP
                v_bv_sk_arr(i)    := v_buffer(i).budget_var_sk;
                v_date_arr(i)     := v_buffer(i).date_id;
                v_acct_sk_arr(i)  := v_buffer(i).account_sk;
                v_bl_id_arr(i)    := v_buffer(i).budget_line_id;
                v_b_id_arr(i)     := v_buffer(i).budget_id;
                v_bcode_arr(i)    := v_buffer(i).budget_code;
                v_bname_arr(i)    := v_buffer(i).budget_name;
                v_btype_arr(i)    := v_buffer(i).budget_type;
                v_fyear_arr(i)    := v_buffer(i).fiscal_year;
                v_fperiod_arr(i)  := v_buffer(i).fiscal_period;
                v_aid_arr(i)      := v_buffer(i).account_id;
                v_acode_arr(i)    := v_buffer(i).account_code;
                v_aname_arr(i)    := v_buffer(i).account_name;
                v_ccid_arr(i)     := v_buffer(i).cost_center_id;
                v_cccode_arr(i)   := v_buffer(i).cc_code;
                v_ccname_arr(i)   := v_buffer(i).cc_name;
                v_plan_arr(i)     := v_buffer(i).planned_amount;
                v_rev_arr(i)      := v_buffer(i).revised_amount;
                v_act_arr(i)      := v_buffer(i).actual_amount;
                v_enc_arr(i)      := v_buffer(i).encumbered_amount;
                v_var_arr(i)      := v_buffer(i).variance_amount;
                v_vpct_arr(i)     := v_buffer(i).variance_pct;
                v_ytdp_arr(i)     := v_buffer(i).ytd_planned;
                v_ytda_arr(i)     := v_buffer(i).ytd_actual;
                v_ytdv_arr(i)     := v_buffer(i).ytd_variance;
                v_rrp_arr(i)      := v_buffer(i).run_rate_proj;
                v_fac_arr(i)      := v_buffer(i).forecast_at_compl;
                v_pya_arr(i)      := v_buffer(i).prior_year_actual;
                v_pyv_arr(i)      := v_buffer(i).prior_year_variance;
                v_vrank_arr(i)    := v_buffer(i).variance_rank;
                v_status_arr(i)   := v_buffer(i).status;
            END LOOP;

            BEGIN
                FORALL i IN VALUES OF v_bv_sk_arr SAVE EXCEPTIONS
                    INSERT /*+ APPEND PARALLEL(8) */
                      INTO DWH.FACT_BUDGET_VARIANCE_STG (
                               budget_var_sk, date_id, account_sk,
                               budget_line_id, budget_id, budget_code,
                               budget_name, budget_type,
                               fiscal_year, fiscal_period,
                               account_id, account_code, account_name,
                               cost_center_id, cc_code, cc_name,
                               planned_amount, revised_amount,
                               actual_amount, encumbered_amount,
                               variance_amount, variance_pct,
                               ytd_planned, ytd_actual, ytd_variance,
                               run_rate_proj, forecast_at_compl,
                               prior_year_actual, prior_year_variance,
                               variance_rank, status,
                               created_date
                           )
                    VALUES (
                               v_bv_sk_arr(i), v_date_arr(i), v_acct_sk_arr(i),
                               v_bl_id_arr(i), v_b_id_arr(i), v_bcode_arr(i),
                               v_bname_arr(i), v_btype_arr(i),
                               v_fyear_arr(i), v_fperiod_arr(i),
                               v_aid_arr(i), v_acode_arr(i), v_aname_arr(i),
                               v_ccid_arr(i), v_cccode_arr(i), v_ccname_arr(i),
                               v_plan_arr(i), v_rev_arr(i),
                               v_act_arr(i), v_enc_arr(i),
                               v_var_arr(i), v_vpct_arr(i),
                               v_ytdp_arr(i), v_ytda_arr(i), v_ytdv_arr(i),
                               v_rrp_arr(i), v_fac_arr(i),
                               v_pya_arr(i), v_pyv_arr(i),
                               v_vrank_arr(i), v_status_arr(i),
                               SYSDATE
                           );
            EXCEPTION
                WHEN OTHERS THEN
                    IF SQLCODE = -24381 THEN
                        v_error_count := v_error_count + SQL%BULK_EXCEPTIONS.COUNT;
                        handle_forall_exceptions(v_step, SQL%BULK_EXCEPTIONS.COUNT);
                    ELSE
                        RAISE;
                    END IF;
            END;

            v_total_rows := v_total_rows + v_buffer.COUNT;
            COMMIT;
        END LOOP;

        CLOSE v_cur;

        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_BUDGET_VARIANCE tgt
        USING (
            WITH cte_stg_ranked AS (
                SELECT s.*,
                       ROW_NUMBER() OVER (
                           PARTITION BY s.budget_line_id
                           ORDER BY s.budget_var_sk DESC
                       ) AS merge_rn
                  FROM DWH.FACT_BUDGET_VARIANCE_STG s
                 WHERE s.created_date >= TRUNC(SYSDATE)
            ),
            cte_stg_valid AS (
                SELECT r.*
                  FROM cte_stg_ranked r
                 WHERE r.merge_rn = 1
            ),
            cte_stg_analytics AS (
                SELECT v.*,
                       SUM(v.variance_amount) OVER (
                           PARTITION BY v.fiscal_year
                           ORDER BY v.fiscal_period
                           ROWS UNBOUNDED PRECEDING
                       ) AS cumulative_variance,
                       AVG(ABS(v.variance_pct)) OVER (
                           PARTITION BY v.account_id
                       ) AS avg_abs_variance_pct,
                       LAG(v.actual_amount) OVER (
                           PARTITION BY v.account_id, v.cost_center_id
                           ORDER BY v.fiscal_period
                       ) AS prev_period_actual,
                       LEAD(v.planned_amount) OVER (
                           PARTITION BY v.account_id, v.cost_center_id
                           ORDER BY v.fiscal_period
                       ) AS next_period_planned,
                       SUM(v.actual_amount) OVER (
                           PARTITION BY v.fiscal_year, v.account_id
                           ORDER BY v.fiscal_period
                           ROWS UNBOUNDED PRECEDING
                       ) AS ytd_account_actual
                  FROM cte_stg_valid v
            )
            SELECT a.budget_var_sk, a.date_id, a.account_sk,
                   a.budget_line_id, a.budget_id, a.budget_code,
                   a.budget_name, a.budget_type,
                   a.fiscal_year, a.fiscal_period,
                   a.account_id, a.account_code,
                   a.cost_center_id, a.cc_code,
                   a.planned_amount, a.revised_amount,
                   a.actual_amount, a.encumbered_amount,
                   a.variance_amount, a.variance_pct,
                   a.ytd_planned, a.ytd_actual, a.ytd_variance,
                   a.run_rate_proj, a.forecast_at_compl,
                   a.prior_year_actual, a.prior_year_variance,
                   a.variance_rank, a.status
              FROM cte_stg_analytics a
        ) src
        ON (tgt.BUDGET_LINE_ID = src.budget_line_id)
        WHEN MATCHED THEN
            UPDATE SET
                tgt.ACTUAL_AMOUNT     = src.actual_amount,
                tgt.ENCUMBERED_AMOUNT = src.encumbered_amount,
                tgt.VARIANCE_AMOUNT   = src.variance_amount,
                tgt.VARIANCE_PCT      = src.variance_pct,
                tgt.YTD_ACTUAL        = src.ytd_actual,
                tgt.YTD_VARIANCE      = src.ytd_variance,
                tgt.RUN_RATE_PROJ     = src.run_rate_proj,
                tgt.FORECAST_AT_COMPL = src.forecast_at_compl,
                tgt.UPDATED_DATE      = SYSDATE
             WHERE tgt.ACTUAL_AMOUNT   <> src.actual_amount
                OR tgt.VARIANCE_AMOUNT <> src.variance_amount
        WHEN NOT MATCHED THEN
            INSERT (
                BUDGET_VAR_SK, DATE_ID, ACCOUNT_SK,
                BUDGET_LINE_ID, BUDGET_ID, BUDGET_CODE,
                BUDGET_NAME, BUDGET_TYPE,
                FISCAL_YEAR, FISCAL_PERIOD,
                ACCOUNT_ID, ACCOUNT_CODE,
                COST_CENTER_ID, CC_CODE,
                PLANNED_AMOUNT, REVISED_AMOUNT,
                ACTUAL_AMOUNT, ENCUMBERED_AMOUNT,
                VARIANCE_AMOUNT, VARIANCE_PCT,
                YTD_PLANNED, YTD_ACTUAL, YTD_VARIANCE,
                RUN_RATE_PROJ, FORECAST_AT_COMPL,
                PRIOR_YEAR_ACTUAL, PRIOR_YEAR_VARIANCE,
                VARIANCE_RANK, STATUS, CREATED_DATE
            )
            VALUES (
                src.budget_var_sk, src.date_id, src.account_sk,
                src.budget_line_id, src.budget_id, src.budget_code,
                src.budget_name, src.budget_type,
                src.fiscal_year, src.fiscal_period,
                src.account_id, src.account_code,
                src.cost_center_id, src.cc_code,
                src.planned_amount, src.revised_amount,
                src.actual_amount, src.encumbered_amount,
                src.variance_amount, src.variance_pct,
                src.ytd_planned, src.ytd_actual, src.ytd_variance,
                src.run_rate_proj, src.forecast_at_compl,
                src.prior_year_actual, src.prior_year_variance,
                src.variance_rank, src.status, SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;

        log_step(v_step || '.MERGE', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_BUDGET_CC_ROLLUP tgt
        USING (
            WITH cte_bv_base AS (
                SELECT fbv.COST_CENTER_ID,
                       fbv.CC_CODE,
                       fbv.FISCAL_YEAR,
                       fbv.FISCAL_PERIOD,
                       fbv.BUDGET_LINE_ID,
                       fbv.PLANNED_AMOUNT,
                       fbv.REVISED_AMOUNT,
                       fbv.ACTUAL_AMOUNT,
                       fbv.ENCUMBERED_AMOUNT,
                       fbv.VARIANCE_AMOUNT,
                       fbv.VARIANCE_PCT,
                       fbv.YTD_PLANNED,
                       fbv.YTD_ACTUAL,
                       fbv.YTD_VARIANCE,
                       fbv.RUN_RATE_PROJ,
                       fbv.FORECAST_AT_COMPL,
                       fbv.PRIOR_YEAR_ACTUAL,
                       ROW_NUMBER() OVER (
                           PARTITION BY fbv.BUDGET_LINE_ID
                           ORDER BY fbv.BUDGET_VAR_SK DESC
                       ) AS bv_rn
                  FROM DWH.FACT_BUDGET_VARIANCE fbv
                 WHERE fbv.FISCAL_YEAR = p_fiscal_year
            ),
            cte_bv_dedup AS (
                SELECT bb.*
                  FROM cte_bv_base bb
                 WHERE bb.bv_rn = 1
            ),
            cte_cc_hierarchy AS (
                SELECT cc.COST_CENTER_ID,
                       cc.CC_CODE,
                       cc.CC_NAME,
                       cc.CC_TYPE,
                       cc.PARENT_CC_ID,
                       cc.BUDGET_HOLDER,
                       SYS_CONNECT_BY_PATH(cc.CC_CODE, '/') AS cc_path,
                       LEVEL AS cc_depth
                  FROM FIN.COST_CENTERS cc
                 START WITH cc.PARENT_CC_ID IS NULL
               CONNECT BY PRIOR cc.COST_CENTER_ID = cc.PARENT_CC_ID
            ),
            cte_cc_agg AS (
                SELECT bd.COST_CENTER_ID,
                       bd.CC_CODE,
                       cch.CC_NAME,
                       cch.CC_TYPE,
                       cch.BUDGET_HOLDER,
                       bd.FISCAL_YEAR,
                       bd.FISCAL_PERIOD,
                       COUNT(bd.BUDGET_LINE_ID)              AS budget_line_count,
                       SUM(bd.PLANNED_AMOUNT)                AS total_planned,
                       SUM(bd.REVISED_AMOUNT)                AS total_revised,
                       SUM(bd.ACTUAL_AMOUNT)                 AS total_actual,
                       SUM(bd.ENCUMBERED_AMOUNT)             AS total_encumbered,
                       SUM(bd.VARIANCE_AMOUNT)               AS total_variance,
                       CASE
                           WHEN SUM(bd.REVISED_AMOUNT) <> 0
                           THEN ROUND(
                               SUM(bd.VARIANCE_AMOUNT)
                               / ABS(SUM(bd.REVISED_AMOUNT)) * 100, 2
                           )
                           ELSE 0
                       END                                    AS weighted_variance_pct,
                       SUM(bd.YTD_PLANNED)                   AS total_ytd_planned,
                       SUM(bd.YTD_ACTUAL)                    AS total_ytd_actual,
                       SUM(bd.YTD_VARIANCE)                  AS total_ytd_variance,
                       SUM(bd.RUN_RATE_PROJ)                 AS total_run_rate,
                       SUM(bd.FORECAST_AT_COMPL)             AS total_forecast,
                       SUM(bd.PRIOR_YEAR_ACTUAL)             AS total_prior_year,
                       SUM(bd.ACTUAL_AMOUNT)
                           - SUM(bd.PRIOR_YEAR_ACTUAL)       AS yoy_change,
                       CASE
                           WHEN SUM(bd.PRIOR_YEAR_ACTUAL) <> 0
                           THEN ROUND(
                               (SUM(bd.ACTUAL_AMOUNT) - SUM(bd.PRIOR_YEAR_ACTUAL))
                               / ABS(SUM(bd.PRIOR_YEAR_ACTUAL)) * 100, 2
                           )
                           ELSE 0
                       END                                    AS yoy_pct,
                       RANK() OVER (
                           PARTITION BY bd.FISCAL_YEAR, bd.FISCAL_PERIOD
                           ORDER BY ABS(SUM(bd.VARIANCE_AMOUNT)) DESC
                       ) AS cc_variance_rank,
                       SUM(SUM(bd.ACTUAL_AMOUNT)) OVER (
                           PARTITION BY bd.COST_CENTER_ID, bd.FISCAL_YEAR
                           ORDER BY bd.FISCAL_PERIOD
                           ROWS UNBOUNDED PRECEDING
                       ) AS cumulative_actual,
                       LAG(SUM(bd.ACTUAL_AMOUNT)) OVER (
                           PARTITION BY bd.COST_CENTER_ID, bd.FISCAL_YEAR
                           ORDER BY bd.FISCAL_PERIOD
                       ) AS prev_period_actual
                  FROM cte_bv_dedup bd
                  LEFT JOIN cte_cc_hierarchy cch
                    ON cch.COST_CENTER_ID = bd.COST_CENTER_ID
                 GROUP BY bd.COST_CENTER_ID, bd.CC_CODE,
                          cch.CC_NAME, cch.CC_TYPE, cch.BUDGET_HOLDER,
                          bd.FISCAL_YEAR, bd.FISCAL_PERIOD
            )
            SELECT ca.COST_CENTER_ID, ca.CC_CODE, ca.CC_NAME,
                   ca.CC_TYPE, ca.BUDGET_HOLDER,
                   ca.FISCAL_YEAR, ca.FISCAL_PERIOD,
                   ca.budget_line_count, ca.total_planned,
                   ca.total_revised, ca.total_actual,
                   ca.total_encumbered, ca.total_variance,
                   ca.weighted_variance_pct,
                   ca.total_ytd_planned, ca.total_ytd_actual,
                   ca.total_ytd_variance,
                   ca.total_run_rate, ca.total_forecast,
                   ca.total_prior_year, ca.yoy_change, ca.yoy_pct,
                   ca.cc_variance_rank, ca.cumulative_actual,
                   ca.prev_period_actual
              FROM cte_cc_agg ca
        ) src
        ON (    tgt.COST_CENTER_ID = src.COST_CENTER_ID
            AND tgt.FISCAL_YEAR    = src.FISCAL_YEAR
            AND tgt.FISCAL_PERIOD  = src.FISCAL_PERIOD)
        WHEN MATCHED THEN
            UPDATE SET
                tgt.TOTAL_PLANNED        = src.total_planned,
                tgt.TOTAL_REVISED        = src.total_revised,
                tgt.TOTAL_ACTUAL         = src.total_actual,
                tgt.TOTAL_ENCUMBERED     = src.total_encumbered,
                tgt.TOTAL_VARIANCE       = src.total_variance,
                tgt.WEIGHTED_VARIANCE_PCT = src.weighted_variance_pct,
                tgt.TOTAL_YTD_ACTUAL     = src.total_ytd_actual,
                tgt.TOTAL_YTD_VARIANCE   = src.total_ytd_variance,
                tgt.TOTAL_RUN_RATE       = src.total_run_rate,
                tgt.TOTAL_FORECAST       = src.total_forecast,
                tgt.YOY_CHANGE           = src.yoy_change,
                tgt.YOY_PCT              = src.yoy_pct,
                tgt.CC_VARIANCE_RANK     = src.cc_variance_rank,
                tgt.UPDATED_DATE         = SYSDATE
             WHERE tgt.TOTAL_ACTUAL    <> src.total_actual
                OR tgt.TOTAL_VARIANCE  <> src.total_variance
        WHEN NOT MATCHED THEN
            INSERT (
                COST_CENTER_ID, CC_CODE, CC_NAME, CC_TYPE, BUDGET_HOLDER,
                FISCAL_YEAR, FISCAL_PERIOD,
                BUDGET_LINE_COUNT, TOTAL_PLANNED,
                TOTAL_REVISED, TOTAL_ACTUAL,
                TOTAL_ENCUMBERED, TOTAL_VARIANCE,
                WEIGHTED_VARIANCE_PCT,
                TOTAL_YTD_PLANNED, TOTAL_YTD_ACTUAL,
                TOTAL_YTD_VARIANCE,
                TOTAL_RUN_RATE, TOTAL_FORECAST,
                TOTAL_PRIOR_YEAR, YOY_CHANGE, YOY_PCT,
                CC_VARIANCE_RANK, CUMULATIVE_ACTUAL,
                PREV_PERIOD_ACTUAL, CREATED_DATE
            )
            VALUES (
                src.COST_CENTER_ID, src.CC_CODE, src.CC_NAME, src.CC_TYPE, src.BUDGET_HOLDER,
                src.FISCAL_YEAR, src.FISCAL_PERIOD,
                src.budget_line_count, src.total_planned,
                src.total_revised, src.total_actual,
                src.total_encumbered, src.total_variance,
                src.weighted_variance_pct,
                src.total_ytd_planned, src.total_ytd_actual,
                src.total_ytd_variance,
                src.total_run_rate, src.total_forecast,
                src.total_prior_year, src.yoy_change, src.yoy_pct,
                src.cc_variance_rank, src.cumulative_actual,
                src.prev_period_actual, SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.CC_ROLLUP', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        INSERT /*+ PARALLEL(8) */
          INTO DWH.FACT_BUDGET_ALERT (
                   fiscal_year, fiscal_period,
                   account_id, account_code,
                   cost_center_id, cc_code,
                   alert_type, alert_severity,
                   variance_amount, variance_pct,
                   budget_amount, actual_amount,
                   description, created_date
               )
        WITH cte_variance_check AS (
            SELECT fbv.FISCAL_YEAR,
                   fbv.FISCAL_PERIOD,
                   fbv.ACCOUNT_ID,
                   fbv.ACCOUNT_CODE,
                   fbv.COST_CENTER_ID,
                   fbv.CC_CODE,
                   fbv.VARIANCE_AMOUNT,
                   fbv.VARIANCE_PCT,
                   fbv.REVISED_AMOUNT,
                   fbv.ACTUAL_AMOUNT,
                   fbv.FORECAST_AT_COMPL,
                   fbv.PLANNED_AMOUNT,
                   fbv.ENCUMBERED_AMOUNT,
                   ROW_NUMBER() OVER (
                       PARTITION BY fbv.BUDGET_LINE_ID
                       ORDER BY fbv.BUDGET_VAR_SK DESC
                   ) AS vc_rn
              FROM DWH.FACT_BUDGET_VARIANCE fbv
             WHERE fbv.FISCAL_YEAR = p_fiscal_year
               AND fbv.FISCAL_PERIOD BETWEEN p_period_from AND p_period_to
        ),
        cte_active AS (
            SELECT vc.*
              FROM cte_variance_check vc
             WHERE vc.vc_rn = 1
        ),
        cte_overspend AS (
            SELECT a.FISCAL_YEAR, a.FISCAL_PERIOD,
                   a.ACCOUNT_ID, a.ACCOUNT_CODE,
                   a.COST_CENTER_ID, a.CC_CODE,
                   'OVERSPEND' AS alert_type,
                   CASE
                       WHEN ABS(a.VARIANCE_PCT) > 25 THEN 'CRITICAL'
                       WHEN ABS(a.VARIANCE_PCT) > 15 THEN 'HIGH'
                       WHEN ABS(a.VARIANCE_PCT) > 10 THEN 'MEDIUM'
                       ELSE 'LOW'
                   END AS alert_severity,
                   a.VARIANCE_AMOUNT,
                   a.VARIANCE_PCT,
                   a.REVISED_AMOUNT,
                   a.ACTUAL_AMOUNT,
                   'Budget overspend: actual ' || TO_CHAR(a.ACTUAL_AMOUNT, 'FM999,999,990.00')
                       || ' vs budget ' || TO_CHAR(a.REVISED_AMOUNT, 'FM999,999,990.00')
                   AS description
              FROM cte_active a
             WHERE a.VARIANCE_AMOUNT < 0
               AND ABS(a.VARIANCE_PCT) > 5
        ),
        cte_forecast_breach AS (
            SELECT a2.FISCAL_YEAR, a2.FISCAL_PERIOD,
                   a2.ACCOUNT_ID, a2.ACCOUNT_CODE,
                   a2.COST_CENTER_ID, a2.CC_CODE,
                   'FORECAST_BREACH' AS alert_type,
                   CASE
                       WHEN a2.FORECAST_AT_COMPL > a2.PLANNED_AMOUNT * 1.20 THEN 'CRITICAL'
                       WHEN a2.FORECAST_AT_COMPL > a2.PLANNED_AMOUNT * 1.10 THEN 'HIGH'
                       ELSE 'MEDIUM'
                   END AS alert_severity,
                   a2.FORECAST_AT_COMPL - a2.PLANNED_AMOUNT AS variance_amount,
                   CASE
                       WHEN a2.PLANNED_AMOUNT <> 0
                       THEN ROUND((a2.FORECAST_AT_COMPL - a2.PLANNED_AMOUNT)
                                  / ABS(a2.PLANNED_AMOUNT) * 100, 2)
                       ELSE 0
                   END AS variance_pct,
                   a2.PLANNED_AMOUNT,
                   a2.FORECAST_AT_COMPL,
                   'Forecast breach: projected ' || TO_CHAR(a2.FORECAST_AT_COMPL, 'FM999,999,990.00')
                       || ' exceeds annual budget ' || TO_CHAR(a2.PLANNED_AMOUNT, 'FM999,999,990.00')
                   AS description
              FROM cte_active a2
             WHERE a2.FORECAST_AT_COMPL > a2.PLANNED_AMOUNT * 1.05
        ),
        cte_encumbrance_risk AS (
            SELECT a3.FISCAL_YEAR, a3.FISCAL_PERIOD,
                   a3.ACCOUNT_ID, a3.ACCOUNT_CODE,
                   a3.COST_CENTER_ID, a3.CC_CODE,
                   'ENCUMBRANCE_RISK' AS alert_type,
                   'MEDIUM' AS alert_severity,
                   a3.ENCUMBERED_AMOUNT AS variance_amount,
                   CASE
                       WHEN a3.REVISED_AMOUNT <> 0
                       THEN ROUND(a3.ENCUMBERED_AMOUNT / ABS(a3.REVISED_AMOUNT) * 100, 2)
                       ELSE 0
                   END AS variance_pct,
                   a3.REVISED_AMOUNT,
                   a3.ACTUAL_AMOUNT + a3.ENCUMBERED_AMOUNT,
                   'Committed+actual exceeds budget: '
                       || TO_CHAR(a3.ACTUAL_AMOUNT + a3.ENCUMBERED_AMOUNT, 'FM999,999,990.00')
                       || ' vs ' || TO_CHAR(a3.REVISED_AMOUNT, 'FM999,999,990.00')
                   AS description
              FROM cte_active a3
             WHERE a3.ACTUAL_AMOUNT + a3.ENCUMBERED_AMOUNT > a3.REVISED_AMOUNT
               AND a3.ENCUMBERED_AMOUNT > 0
        )
        SELECT * FROM cte_overspend
        UNION ALL
        SELECT * FROM cte_forecast_breach
        UNION ALL
        SELECT * FROM cte_encumbrance_risk;

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.BUDGET_ALERTS', 'DONE', g_row_count);

        log_step(v_step || '.COMPLETE', 'SUCCESS', v_total_rows);

    EXCEPTION
        WHEN OTHERS THEN
            IF v_cur%ISOPEN THEN CLOSE v_cur; END IF;
            ROLLBACK;
            log_step(v_step || '.FATAL', 'ERROR', 0, SQLERRM);
            RAISE;
    END load_fact_budget_variance;

    -- =========================================================================
    -- 5. LOAD_FACT_PAYMENT_AGING
    -- =========================================================================
    PROCEDURE load_fact_payment_aging (
        p_as_of_date IN DATE DEFAULT TRUNC(SYSDATE),
        p_mode       IN VARCHAR2 DEFAULT 'FULL'
    ) IS
        v_step          VARCHAR2(200) := 'LOAD_FACT_PAYMENT_AGING';
        v_total_rows    NUMBER(19) := 0;
        v_error_count   PLS_INTEGER := 0;
        v_func_curr     VARCHAR2(3);
        v_aging_data    DWH.T_AGING_TAB := DWH.T_AGING_TAB();

        v_cust_id_arr   t_id_array;
        v_cname_arr     t_varchar_array;
        v_curr_arr      t_varchar_array;
        v_current_arr   t_amount_array;
        v_d130_arr      t_amount_array;
        v_d3160_arr     t_amount_array;
        v_d6190_arr     t_amount_array;
        v_d91120_arr    t_amount_array;
        v_dover120_arr  t_amount_array;
        v_total_arr     t_amount_array;
        v_climit_arr    t_amount_array;
        v_cutil_arr     t_amount_array;
    BEGIN
        init_run(v_step);
        v_func_curr := get_functional_currency;

        IF p_mode = 'FULL' THEN
            EXECUTE IMMEDIATE 'TRUNCATE TABLE DWH.FACT_PAYMENT_AGING';
            g_step_start := SYSTIMESTAMP;
            log_step(v_step || '.TRUNCATE', 'DONE');
        END IF;

        g_step_start := SYSTIMESTAMP;

        SELECT DWH.T_AGING_REC(
                   ag.CUSTOMER_ID,
                   ag.customer_name,
                   ag.currency_code,
                   ag.current_amount,
                   ag.days_1_30,
                   ag.days_31_60,
                   ag.days_61_90,
                   ag.days_91_120,
                   ag.days_over_120,
                   ag.total_outstanding,
                   ag.credit_limit,
                   ag.credit_utilization
               )
          BULK COLLECT INTO v_aging_data
          FROM (
            WITH cte_open_invoices AS (
                SELECT inv.INVOICE_ID,
                       inv.INVOICE_NUM,
                       inv.INVOICE_DATE,
                       inv.DUE_DATE,
                       inv.CUSTOMER_ID,
                       inv.CURRENCY_ID,
                       inv.EXCHANGE_RATE,
                       inv.TOTAL_AMOUNT,
                       inv.PAID_AMOUNT,
                       inv.TOTAL_AMOUNT - NVL(inv.PAID_AMOUNT, 0) AS outstanding,
                       p_as_of_date - inv.DUE_DATE                AS days_past_due
                  FROM FIN.INVOICES inv
                 WHERE inv.STATUS IN ('APPROVED', 'POSTED', 'PARTIALLY_PAID')
                   AND inv.TOTAL_AMOUNT - NVL(inv.PAID_AMOUNT, 0) > 0
                   AND inv.INVOICE_TYPE <> 'CREDIT_NOTE'
            ),
            cte_credit_notes AS (
                SELECT cn.CUSTOMER_ID,
                       SUM(cn.TOTAL_AMOUNT) AS credit_note_total
                  FROM FIN.INVOICES cn
                 WHERE cn.INVOICE_TYPE = 'CREDIT_NOTE'
                   AND cn.STATUS IN ('APPROVED', 'POSTED')
                 GROUP BY cn.CUSTOMER_ID
            ),
            cte_customer_info AS (
                SELECT cust.CUSTOMER_ID,
                       cust.CUSTOMER_NAME,
                       cust.CUSTOMER_CODE,
                       cust.CREDIT_LIMIT,
                       cust.CURRENCY_CODE,
                       cust.RATING,
                       (SELECT seg.SEGMENT_NAME
                          FROM CRM.CUSTOMER_SEGMENTS seg
                         WHERE seg.SEGMENT_ID = cust.SEGMENT_ID) AS segment_name
                  FROM CRM.CUSTOMERS cust
                 WHERE cust.STATUS = 'ACTIVE'
            ),
            cte_payment_terms AS (
                SELECT pt.PAYMENT_TERM_ID,
                       pt.NET_DAYS,
                       pt.DISCOUNT_DAYS,
                       pt.DISCOUNT_PCT
                  FROM FIN.PAYMENT_TERMS pt
            ),
            cte_fx_rate AS (
                SELECT cur.CURRENCY_ID,
                       cur.CURRENCY_CODE,
                       cur.IS_FUNCTIONAL
                  FROM FIN.CURRENCIES cur
            ),
            cte_hist_payments AS (
                SELECT pa.INVOICE_ID,
                       AVG(pa.ALLOCATION_DATE - inv2.DUE_DATE) AS avg_days_to_pay,
                       COUNT(pa.ALLOCATION_ID)                 AS hist_payment_count
                  FROM FIN.PAYMENT_ALLOCATIONS pa
                  JOIN FIN.INVOICES inv2
                    ON inv2.INVOICE_ID = pa.INVOICE_ID
                 WHERE pa.STATUS = 'APPLIED'
                 GROUP BY pa.INVOICE_ID
            ),
            cte_collection_prob AS (
                SELECT oi.CUSTOMER_ID,
                       CASE
                           WHEN AVG(hp.avg_days_to_pay) IS NULL THEN 0.50
                           WHEN AVG(hp.avg_days_to_pay) <= 0    THEN 0.95
                           WHEN AVG(hp.avg_days_to_pay) <= 15   THEN 0.90
                           WHEN AVG(hp.avg_days_to_pay) <= 30   THEN 0.80
                           WHEN AVG(hp.avg_days_to_pay) <= 60   THEN 0.60
                           WHEN AVG(hp.avg_days_to_pay) <= 90   THEN 0.40
                           ELSE 0.20
                       END AS collection_probability
                  FROM cte_open_invoices oi
                  LEFT JOIN cte_hist_payments hp
                    ON hp.INVOICE_ID = oi.INVOICE_ID
                 GROUP BY oi.CUSTOMER_ID
            ),
            cte_prior_month AS (
                SELECT inv_pm.CUSTOMER_ID,
                       SUM(inv_pm.TOTAL_AMOUNT - NVL(inv_pm.PAID_AMOUNT, 0))
                           AS prior_outstanding
                  FROM FIN.INVOICES inv_pm
                 WHERE inv_pm.STATUS IN ('APPROVED', 'POSTED', 'PARTIALLY_PAID')
                   AND inv_pm.TOTAL_AMOUNT - NVL(inv_pm.PAID_AMOUNT, 0) > 0
                   AND inv_pm.INVOICE_TYPE <> 'CREDIT_NOTE'
                   AND inv_pm.DUE_DATE <= ADD_MONTHS(p_as_of_date, -1)
                 GROUP BY inv_pm.CUSTOMER_ID
            ),
            cte_aged AS (
                SELECT oi.CUSTOMER_ID,
                       ci.CUSTOMER_NAME,
                       NVL(ci.CURRENCY_CODE, v_func_curr) AS currency_code,
                       SUM(CASE WHEN oi.days_past_due <= 0     THEN oi.outstanding ELSE 0 END)
                           AS current_amount,
                       SUM(CASE WHEN oi.days_past_due BETWEEN  1 AND 30  THEN oi.outstanding ELSE 0 END)
                           AS days_1_30,
                       SUM(CASE WHEN oi.days_past_due BETWEEN 31 AND 60  THEN oi.outstanding ELSE 0 END)
                           AS days_31_60,
                       SUM(CASE WHEN oi.days_past_due BETWEEN 61 AND 90  THEN oi.outstanding ELSE 0 END)
                           AS days_61_90,
                       SUM(CASE WHEN oi.days_past_due BETWEEN 91 AND 120 THEN oi.outstanding ELSE 0 END)
                           AS days_91_120,
                       SUM(CASE WHEN oi.days_past_due > 120    THEN oi.outstanding ELSE 0 END)
                           AS days_over_120,
                       SUM(oi.outstanding)                     AS total_outstanding,
                       NVL(ci.CREDIT_LIMIT, 0)                 AS credit_limit,
                       CASE
                           WHEN NVL(ci.CREDIT_LIMIT, 0) > 0
                           THEN ROUND(SUM(oi.outstanding) / ci.CREDIT_LIMIT, 4)
                           ELSE 0
                       END                                     AS credit_utilization
                  FROM cte_open_invoices oi
                  JOIN cte_customer_info ci
                    ON ci.CUSTOMER_ID = oi.CUSTOMER_ID
                  LEFT JOIN cte_credit_notes cn
                    ON cn.CUSTOMER_ID = oi.CUSTOMER_ID
                  LEFT JOIN cte_collection_prob cp
                    ON cp.CUSTOMER_ID = oi.CUSTOMER_ID
                  LEFT JOIN cte_prior_month pm
                    ON pm.CUSTOMER_ID = oi.CUSTOMER_ID
                 GROUP BY oi.CUSTOMER_ID, ci.CUSTOMER_NAME,
                          ci.CURRENCY_CODE, ci.CREDIT_LIMIT
            )
            SELECT a.CUSTOMER_ID,
                   a.customer_name,
                   a.currency_code,
                   a.current_amount,
                   a.days_1_30,
                   a.days_31_60,
                   a.days_61_90,
                   a.days_91_120,
                   a.days_over_120,
                   a.total_outstanding,
                   a.credit_limit,
                   a.credit_utilization
              FROM cte_aged a
             ORDER BY a.total_outstanding DESC
          ) ag;

        IF v_aging_data.COUNT > 0 THEN
            FOR i IN 1 .. v_aging_data.COUNT LOOP
                v_cust_id_arr(i)  := v_aging_data(i).CUSTOMER_ID;
                v_cname_arr(i)    := v_aging_data(i).CUSTOMER_NAME;
                v_curr_arr(i)     := v_aging_data(i).CURRENCY_CODE;
                v_current_arr(i)  := v_aging_data(i).CURRENT_AMOUNT;
                v_d130_arr(i)     := v_aging_data(i).DAYS_1_30;
                v_d3160_arr(i)    := v_aging_data(i).DAYS_31_60;
                v_d6190_arr(i)    := v_aging_data(i).DAYS_61_90;
                v_d91120_arr(i)   := v_aging_data(i).DAYS_91_120;
                v_dover120_arr(i) := v_aging_data(i).DAYS_OVER_120;
                v_total_arr(i)    := v_aging_data(i).TOTAL_OUTSTANDING;
                v_climit_arr(i)   := v_aging_data(i).CREDIT_LIMIT;
                v_cutil_arr(i)    := v_aging_data(i).CREDIT_UTILIZATION;
            END LOOP;

            BEGIN
                FORALL i IN VALUES OF v_cust_id_arr SAVE EXCEPTIONS
                    INSERT /*+ APPEND PARALLEL(8) */
                      INTO DWH.FACT_PAYMENT_AGING (
                               customer_id, customer_name, currency_code,
                               current_amount, days_1_30, days_31_60,
                               days_61_90, days_91_120, days_over_120,
                               total_outstanding, credit_limit,
                               credit_utilization, as_of_date, created_date
                           )
                    VALUES (
                               v_cust_id_arr(i), v_cname_arr(i), v_curr_arr(i),
                               v_current_arr(i), v_d130_arr(i), v_d3160_arr(i),
                               v_d6190_arr(i), v_d91120_arr(i), v_dover120_arr(i),
                               v_total_arr(i), v_climit_arr(i),
                               v_cutil_arr(i), p_as_of_date, SYSDATE
                           );
            EXCEPTION
                WHEN OTHERS THEN
                    IF SQLCODE = -24381 THEN
                        v_error_count := v_error_count + SQL%BULK_EXCEPTIONS.COUNT;
                        handle_forall_exceptions(v_step, SQL%BULK_EXCEPTIONS.COUNT);
                    ELSE
                        RAISE;
                    END IF;
            END;

            v_total_rows := v_aging_data.COUNT;
        END IF;

        COMMIT;

        g_step_start := SYSTIMESTAMP;

        INSERT /*+ APPEND PARALLEL(8) */
          INTO DWH.FACT_PAYMENT_AGING_SUMMARY (
                   as_of_date, currency_code,
                   total_current, total_1_30, total_31_60,
                   total_61_90, total_91_120, total_over_120,
                   grand_total, customer_count,
                   avg_credit_utilization,
                   created_date
               )
        SELECT p_as_of_date,
               t.CURRENCY_CODE,
               SUM(t.CURRENT_AMOUNT),
               SUM(t.DAYS_1_30),
               SUM(t.DAYS_31_60),
               SUM(t.DAYS_61_90),
               SUM(t.DAYS_91_120),
               SUM(t.DAYS_OVER_120),
               SUM(t.TOTAL_OUTSTANDING),
               COUNT(DISTINCT t.CUSTOMER_ID),
               AVG(t.CREDIT_UTILIZATION),
               SYSDATE
          FROM TABLE(v_aging_data) t
         GROUP BY t.CURRENCY_CODE;

        COMMIT;
        log_step(v_step || '.AGING_SUMMARY', 'DONE', SQL%ROWCOUNT);

        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_AGING_TREND tgt
        USING (
            WITH cte_current_aging AS (
                SELECT fa.CUSTOMER_ID,
                       fa.CUSTOMER_NAME,
                       fa.CURRENCY_CODE,
                       fa.CURRENT_AMOUNT,
                       fa.DAYS_1_30,
                       fa.DAYS_31_60,
                       fa.DAYS_61_90,
                       fa.DAYS_91_120,
                       fa.DAYS_OVER_120,
                       fa.TOTAL_OUTSTANDING,
                       fa.CREDIT_LIMIT,
                       fa.CREDIT_UTILIZATION,
                       fa.AS_OF_DATE
                  FROM DWH.FACT_PAYMENT_AGING fa
                 WHERE fa.AS_OF_DATE = p_as_of_date
            ),
            cte_prior_aging AS (
                SELECT fa2.CUSTOMER_ID,
                       fa2.TOTAL_OUTSTANDING AS prior_outstanding,
                       fa2.DAYS_OVER_120     AS prior_over_120,
                       fa2.CREDIT_UTILIZATION AS prior_util,
                       fa2.AS_OF_DATE        AS prior_date,
                       ROW_NUMBER() OVER (
                           PARTITION BY fa2.CUSTOMER_ID
                           ORDER BY fa2.AS_OF_DATE DESC
                       ) AS pa_rn
                  FROM DWH.FACT_PAYMENT_AGING fa2
                 WHERE fa2.AS_OF_DATE < p_as_of_date
                   AND fa2.AS_OF_DATE >= ADD_MONTHS(p_as_of_date, -1)
            ),
            cte_hist_patterns AS (
                SELECT pa3.INVOICE_ID,
                       inv3.CUSTOMER_ID,
                       AVG(pa3.ALLOCATION_DATE - inv3.DUE_DATE) AS avg_pay_delay,
                       COUNT(pa3.ALLOCATION_ID)                 AS payment_count
                  FROM FIN.PAYMENT_ALLOCATIONS pa3
                  JOIN FIN.INVOICES inv3
                    ON inv3.INVOICE_ID = pa3.INVOICE_ID
                 WHERE pa3.STATUS = 'APPLIED'
                 GROUP BY pa3.INVOICE_ID, inv3.CUSTOMER_ID
            ),
            cte_customer_pay_behavior AS (
                SELECT hp.CUSTOMER_ID,
                       AVG(hp.avg_pay_delay)        AS cust_avg_delay,
                       SUM(hp.payment_count)        AS cust_total_payments,
                       CASE
                           WHEN AVG(hp.avg_pay_delay) <= 0    THEN 'PROMPT'
                           WHEN AVG(hp.avg_pay_delay) <= 15   THEN 'TIMELY'
                           WHEN AVG(hp.avg_pay_delay) <= 30   THEN 'MODERATE'
                           WHEN AVG(hp.avg_pay_delay) <= 60   THEN 'SLOW'
                           ELSE 'DELINQUENT'
                       END AS payment_behavior
                  FROM cte_hist_patterns hp
                 GROUP BY hp.CUSTOMER_ID
            ),
            cte_trend AS (
                SELECT ca.CUSTOMER_ID,
                       ca.CUSTOMER_NAME,
                       ca.CURRENCY_CODE,
                       ca.TOTAL_OUTSTANDING,
                       NVL(pa.prior_outstanding, 0) AS prior_outstanding,
                       ca.TOTAL_OUTSTANDING - NVL(pa.prior_outstanding, 0) AS outstanding_change,
                       CASE
                           WHEN NVL(pa.prior_outstanding, 0) > 0
                           THEN ROUND((ca.TOTAL_OUTSTANDING - pa.prior_outstanding)
                                      / ABS(pa.prior_outstanding) * 100, 2)
                           ELSE 0
                       END AS change_pct,
                       ca.DAYS_OVER_120,
                       NVL(pa.prior_over_120, 0) AS prior_over_120,
                       ca.DAYS_OVER_120 - NVL(pa.prior_over_120, 0) AS over120_change,
                       ca.CREDIT_UTILIZATION,
                       NVL(pa.prior_util, 0) AS prior_utilization,
                       NVL(cpb.cust_avg_delay, 0) AS avg_payment_delay,
                       NVL(cpb.payment_behavior, 'UNKNOWN') AS payment_behavior,
                       CASE
                           WHEN ca.CREDIT_UTILIZATION > 0.90 THEN 'CRITICAL'
                           WHEN ca.CREDIT_UTILIZATION > 0.75 THEN 'HIGH'
                           WHEN ca.CREDIT_UTILIZATION > 0.50 THEN 'MEDIUM'
                           ELSE 'LOW'
                       END AS risk_category,
                       RANK() OVER (
                           ORDER BY ca.TOTAL_OUTSTANDING DESC
                       ) AS outstanding_rank,
                       RANK() OVER (
                           ORDER BY ca.DAYS_OVER_120 DESC
                       ) AS aging_risk_rank,
                       ca.AS_OF_DATE
                  FROM cte_current_aging ca
                  LEFT JOIN cte_prior_aging pa
                    ON pa.CUSTOMER_ID = ca.CUSTOMER_ID
                   AND pa.pa_rn = 1
                  LEFT JOIN cte_customer_pay_behavior cpb
                    ON cpb.CUSTOMER_ID = ca.CUSTOMER_ID
            )
            SELECT t.CUSTOMER_ID, t.CUSTOMER_NAME, t.CURRENCY_CODE,
                   t.TOTAL_OUTSTANDING, t.prior_outstanding,
                   t.outstanding_change, t.change_pct,
                   t.DAYS_OVER_120, t.prior_over_120, t.over120_change,
                   t.CREDIT_UTILIZATION, t.prior_utilization,
                   t.avg_payment_delay, t.payment_behavior,
                   t.risk_category, t.outstanding_rank, t.aging_risk_rank,
                   t.AS_OF_DATE
              FROM cte_trend t
        ) src
        ON (    tgt.CUSTOMER_ID = src.CUSTOMER_ID
            AND tgt.AS_OF_DATE  = src.AS_OF_DATE)
        WHEN MATCHED THEN
            UPDATE SET
                tgt.TOTAL_OUTSTANDING  = src.TOTAL_OUTSTANDING,
                tgt.OUTSTANDING_CHANGE = src.outstanding_change,
                tgt.CHANGE_PCT         = src.change_pct,
                tgt.CREDIT_UTILIZATION = src.CREDIT_UTILIZATION,
                tgt.RISK_CATEGORY      = src.risk_category,
                tgt.OUTSTANDING_RANK   = src.outstanding_rank,
                tgt.AGING_RISK_RANK    = src.aging_risk_rank,
                tgt.UPDATED_DATE       = SYSDATE
             WHERE tgt.TOTAL_OUTSTANDING <> src.TOTAL_OUTSTANDING
                OR tgt.RISK_CATEGORY     <> src.risk_category
        WHEN NOT MATCHED THEN
            INSERT (
                CUSTOMER_ID, CUSTOMER_NAME, CURRENCY_CODE,
                TOTAL_OUTSTANDING, PRIOR_OUTSTANDING,
                OUTSTANDING_CHANGE, CHANGE_PCT,
                DAYS_OVER_120, PRIOR_OVER_120, OVER120_CHANGE,
                CREDIT_UTILIZATION, PRIOR_UTILIZATION,
                AVG_PAYMENT_DELAY, PAYMENT_BEHAVIOR,
                RISK_CATEGORY, OUTSTANDING_RANK, AGING_RISK_RANK,
                AS_OF_DATE, CREATED_DATE
            )
            VALUES (
                src.CUSTOMER_ID, src.CUSTOMER_NAME, src.CURRENCY_CODE,
                src.TOTAL_OUTSTANDING, src.prior_outstanding,
                src.outstanding_change, src.change_pct,
                src.DAYS_OVER_120, src.prior_over_120, src.over120_change,
                src.CREDIT_UTILIZATION, src.prior_utilization,
                src.avg_payment_delay, src.payment_behavior,
                src.risk_category, src.outstanding_rank, src.aging_risk_rank,
                src.AS_OF_DATE, SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.AGING_TREND', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        INSERT /*+ PARALLEL(8) */
          INTO DWH.FACT_AGING_RISK_ALERT (
                   as_of_date, customer_id, customer_name,
                   alert_type, severity,
                   total_outstanding, credit_limit,
                   credit_utilization, days_over_120,
                   description, created_date
               )
        WITH cte_risk_base AS (
            SELECT fa.CUSTOMER_ID,
                   fa.CUSTOMER_NAME,
                   fa.TOTAL_OUTSTANDING,
                   fa.CREDIT_LIMIT,
                   fa.CREDIT_UTILIZATION,
                   fa.DAYS_OVER_120,
                   fa.CURRENT_AMOUNT,
                   fa.DAYS_1_30,
                   fa.DAYS_31_60,
                   fa.DAYS_61_90,
                   fa.DAYS_91_120
              FROM DWH.FACT_PAYMENT_AGING fa
             WHERE fa.AS_OF_DATE = p_as_of_date
        ),
        cte_credit_breach AS (
            SELECT rb.CUSTOMER_ID, rb.CUSTOMER_NAME,
                   'CREDIT_LIMIT_BREACH' AS alert_type,
                   CASE
                       WHEN rb.CREDIT_UTILIZATION > 1.0 THEN 'CRITICAL'
                       WHEN rb.CREDIT_UTILIZATION > 0.9 THEN 'HIGH'
                       ELSE 'MEDIUM'
                   END AS severity,
                   rb.TOTAL_OUTSTANDING,
                   rb.CREDIT_LIMIT,
                   rb.CREDIT_UTILIZATION,
                   rb.DAYS_OVER_120,
                   'Credit utilization at '
                       || TO_CHAR(rb.CREDIT_UTILIZATION * 100, 'FM990.0') || '%'
                       || ' (limit ' || TO_CHAR(rb.CREDIT_LIMIT, 'FM999,999,990.00') || ')'
                   AS description
              FROM cte_risk_base rb
             WHERE rb.CREDIT_UTILIZATION > 0.85
               AND rb.CREDIT_LIMIT > 0
        ),
        cte_high_aging AS (
            SELECT rb2.CUSTOMER_ID, rb2.CUSTOMER_NAME,
                   'HIGH_AGING_CONCENTRATION' AS alert_type,
                   CASE
                       WHEN rb2.DAYS_OVER_120 / NULLIF(rb2.TOTAL_OUTSTANDING, 0) > 0.5 THEN 'CRITICAL'
                       WHEN rb2.DAYS_OVER_120 / NULLIF(rb2.TOTAL_OUTSTANDING, 0) > 0.3 THEN 'HIGH'
                       ELSE 'MEDIUM'
                   END AS severity,
                   rb2.TOTAL_OUTSTANDING,
                   rb2.CREDIT_LIMIT,
                   rb2.CREDIT_UTILIZATION,
                   rb2.DAYS_OVER_120,
                   'Over-120 days: ' || TO_CHAR(rb2.DAYS_OVER_120, 'FM999,999,990.00')
                       || ' of ' || TO_CHAR(rb2.TOTAL_OUTSTANDING, 'FM999,999,990.00')
                       || ' total'
                   AS description
              FROM cte_risk_base rb2
             WHERE rb2.DAYS_OVER_120 > 0
               AND rb2.DAYS_OVER_120 / NULLIF(rb2.TOTAL_OUTSTANDING, 0) > 0.2
        ),
        cte_deteriorating AS (
            SELECT rb3.CUSTOMER_ID, rb3.CUSTOMER_NAME,
                   'DETERIORATING_PROFILE' AS alert_type,
                   'HIGH' AS severity,
                   rb3.TOTAL_OUTSTANDING,
                   rb3.CREDIT_LIMIT,
                   rb3.CREDIT_UTILIZATION,
                   rb3.DAYS_OVER_120,
                   'Aging profile worsening: 91+ days amount exceeds 60-day amount'
                   AS description
              FROM cte_risk_base rb3
             WHERE (rb3.DAYS_91_120 + rb3.DAYS_OVER_120)
                 > (rb3.DAYS_31_60 + rb3.DAYS_61_90)
               AND rb3.TOTAL_OUTSTANDING > 10000
        )
        SELECT p_as_of_date, CUSTOMER_ID, CUSTOMER_NAME,
               alert_type, severity,
               TOTAL_OUTSTANDING, CREDIT_LIMIT,
               CREDIT_UTILIZATION, DAYS_OVER_120,
               description, SYSDATE
          FROM cte_credit_breach
        UNION ALL
        SELECT p_as_of_date, CUSTOMER_ID, CUSTOMER_NAME,
               alert_type, severity,
               TOTAL_OUTSTANDING, CREDIT_LIMIT,
               CREDIT_UTILIZATION, DAYS_OVER_120,
               description, SYSDATE
          FROM cte_high_aging
        UNION ALL
        SELECT p_as_of_date, CUSTOMER_ID, CUSTOMER_NAME,
               alert_type, severity,
               TOTAL_OUTSTANDING, CREDIT_LIMIT,
               CREDIT_UTILIZATION, DAYS_OVER_120,
               description, SYSDATE
          FROM cte_deteriorating;

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.RISK_ALERTS', 'DONE', g_row_count);

        log_step(v_step || '.COMPLETE', 'SUCCESS', v_total_rows);

    EXCEPTION
        WHEN OTHERS THEN
            ROLLBACK;
            log_step(v_step || '.FATAL', 'ERROR', 0, SQLERRM);
            RAISE;
    END load_fact_payment_aging;

    -- =========================================================================
    -- 6. LOAD_FACT_GL_BALANCE
    -- =========================================================================
    PROCEDURE load_fact_gl_balance (
        p_fiscal_year   IN NUMBER,
        p_fiscal_period IN NUMBER,
        p_mode          IN VARCHAR2 DEFAULT 'INCREMENTAL'
    ) IS
        v_step          VARCHAR2(200) := 'LOAD_FACT_GL_BALANCE';
        v_total_rows    NUMBER(19) := 0;
        v_error_count   PLS_INTEGER := 0;
        v_func_curr     VARCHAR2(3);

        TYPE t_glb_cursor IS REF CURSOR;
        v_cur           t_glb_cursor;
        v_buffer        t_gl_balance_tab;

        v_gl_sk_arr      t_id_array;
        v_date_arr       t_id_array;
        v_acct_sk_arr    t_id_array;
        v_aid_arr        t_id_array;
        v_acode_arr      t_varchar_array;
        v_aname_arr      t_varchar_array;
        v_atype_arr      t_varchar_array;
        v_ccid_arr       t_id_array;
        v_cccode_arr     t_varchar_array;
        v_ccname_arr     t_varchar_array;
        v_fyear_arr      t_id_array;
        v_fperiod_arr    t_id_array;
        v_curr_arr       t_varchar_array;
        v_opbal_arr      t_amount_array;
        v_pdr_arr        t_amount_array;
        v_pcr_arr        t_amount_array;
        v_pact_arr       t_amount_array;
        v_clbal_arr      t_amount_array;
        v_bopbal_arr     t_amount_array;
        v_bpdr_arr       t_amount_array;
        v_bpcr_arr       t_amount_array;
        v_bclbal_arr     t_amount_array;
        v_adjamt_arr     t_amount_array;
        v_rclamt_arr     t_amount_array;
        v_hlvl_arr       t_id_array;
        v_paid_arr       t_id_array;
        v_pstat_arr      t_varchar_array;
        v_status_arr     t_varchar_array;
    BEGIN
        init_run(v_step);
        v_func_curr := get_functional_currency;

        IF p_mode = 'FULL' THEN
            EXECUTE IMMEDIATE 'TRUNCATE TABLE DWH.FACT_GL_BALANCE_STG';
            g_step_start := SYSTIMESTAMP;
            log_step(v_step || '.TRUNCATE', 'DONE');
        END IF;

        g_step_start := SYSTIMESTAMP;

        OPEN v_cur FOR
            WITH cte_target_period AS (
                SELECT fp.FISCAL_PERIOD_ID,
                       fp.FISCAL_YEAR,
                       fp.PERIOD_NUMBER,
                       fp.PERIOD_NAME,
                       fp.START_DATE,
                       fp.END_DATE,
                       fp.STATUS AS period_status
                  FROM FIN.FISCAL_PERIODS fp
                 WHERE fp.FISCAL_YEAR   = p_fiscal_year
                   AND fp.PERIOD_NUMBER = p_fiscal_period
            ),
            cte_prior_period AS (
                SELECT fp2.FISCAL_PERIOD_ID AS prior_period_id,
                       fp2.FISCAL_YEAR      AS prior_year,
                       fp2.PERIOD_NUMBER    AS prior_number,
                       fp2.END_DATE         AS prior_end_date
                  FROM FIN.FISCAL_PERIODS fp2
                 WHERE (fp2.FISCAL_YEAR = p_fiscal_year AND fp2.PERIOD_NUMBER = p_fiscal_period - 1)
                    OR (fp2.FISCAL_YEAR = p_fiscal_year - 1 AND fp2.PERIOD_NUMBER = 12
                        AND p_fiscal_period = 1)
            ),
            cte_accounts AS (
                SELECT a.ACCOUNT_ID,
                       a.ACCOUNT_CODE,
                       a.ACCOUNT_NAME,
                       a.ACCOUNT_TYPE,
                       a.ACCOUNT_SUBTYPE,
                       a.PARENT_ACCOUNT_ID,
                       a.LEVEL_NUM,
                       a.FULL_PATH_CODE,
                       a.IS_POSTING,
                       a.NORMAL_BALANCE,
                       a.CURRENCY_ID
                  FROM FIN.ACCOUNTS a
                 WHERE a.IS_ACTIVE = 'Y'
            ),
            cte_cost_centers AS (
                SELECT cc.COST_CENTER_ID,
                       cc.CC_CODE,
                       cc.CC_NAME,
                       cc.CC_TYPE,
                       cc.LEVEL_NUM AS cc_level
                  FROM FIN.COST_CENTERS cc
            ),
            cte_period_activity AS (
                SELECT jl.ACCOUNT_ID,
                       jl.COST_CENTER_ID,
                       NVL(fx.CURRENCY_CODE, v_func_curr) AS currency_code,
                       SUM(NVL(jl.DEBIT_AMOUNT, 0))       AS period_debit,
                       SUM(NVL(jl.CREDIT_AMOUNT, 0))      AS period_credit,
                       SUM(NVL(jl.DEBIT_AMOUNT, 0))
                         - SUM(NVL(jl.CREDIT_AMOUNT, 0))  AS period_activity,
                       SUM(NVL(jl.BASE_DEBIT, 0))         AS base_period_debit,
                       SUM(NVL(jl.BASE_CREDIT, 0))        AS base_period_credit
                  FROM FIN.JOURNAL_LINES jl
                  JOIN FIN.JOURNALS j
                    ON j.JOURNAL_ID = jl.JOURNAL_ID
                  LEFT JOIN FIN.CURRENCIES fx
                    ON fx.CURRENCY_ID = jl.CURRENCY_ID
                 WHERE j.STATUS = 'POSTED'
                   AND j.FISCAL_PERIOD_ID IN (
                           SELECT tp.FISCAL_PERIOD_ID FROM cte_target_period tp
                       )
                 GROUP BY jl.ACCOUNT_ID, jl.COST_CENTER_ID, fx.CURRENCY_CODE
            ),
            cte_opening_balance AS (
                SELECT jl2.ACCOUNT_ID,
                       jl2.COST_CENTER_ID,
                       NVL(fx2.CURRENCY_CODE, v_func_curr) AS currency_code,
                       SUM(NVL(jl2.DEBIT_AMOUNT, 0))
                         - SUM(NVL(jl2.CREDIT_AMOUNT, 0))  AS opening_balance,
                       SUM(NVL(jl2.BASE_DEBIT, 0))
                         - SUM(NVL(jl2.BASE_CREDIT, 0))    AS base_opening
                  FROM FIN.JOURNAL_LINES jl2
                  JOIN FIN.JOURNALS j2
                    ON j2.JOURNAL_ID = jl2.JOURNAL_ID
                  JOIN FIN.FISCAL_PERIODS fp_ob
                    ON fp_ob.FISCAL_PERIOD_ID = j2.FISCAL_PERIOD_ID
                  LEFT JOIN FIN.CURRENCIES fx2
                    ON fx2.CURRENCY_ID = jl2.CURRENCY_ID
                 WHERE j2.STATUS = 'POSTED'
                   AND (
                           (fp_ob.FISCAL_YEAR = p_fiscal_year
                            AND fp_ob.PERIOD_NUMBER < p_fiscal_period)
                        OR fp_ob.FISCAL_YEAR < p_fiscal_year
                       )
                 GROUP BY jl2.ACCOUNT_ID, jl2.COST_CENTER_ID, fx2.CURRENCY_CODE
            ),
            cte_adjustments AS (
                SELECT jl3.ACCOUNT_ID,
                       jl3.COST_CENTER_ID,
                       SUM(NVL(jl3.BASE_DEBIT, 0)
                         - NVL(jl3.BASE_CREDIT, 0)) AS adjustment_amount
                  FROM FIN.JOURNAL_LINES jl3
                  JOIN FIN.JOURNALS j3
                    ON j3.JOURNAL_ID = jl3.JOURNAL_ID
                 WHERE j3.STATUS = 'POSTED'
                   AND j3.JOURNAL_TYPE = 'ADJUSTMENT'
                   AND j3.FISCAL_PERIOD_ID IN (
                           SELECT tp2.FISCAL_PERIOD_ID FROM cte_target_period tp2
                       )
                 GROUP BY jl3.ACCOUNT_ID, jl3.COST_CENTER_ID
            ),
            cte_reclassifications AS (
                SELECT jl4.ACCOUNT_ID,
                       jl4.COST_CENTER_ID,
                       SUM(NVL(jl4.BASE_DEBIT, 0)
                         - NVL(jl4.BASE_CREDIT, 0)) AS reclass_amount
                  FROM FIN.JOURNAL_LINES jl4
                  JOIN FIN.JOURNALS j4
                    ON j4.JOURNAL_ID = jl4.JOURNAL_ID
                 WHERE j4.STATUS = 'POSTED'
                   AND j4.JOURNAL_TYPE = 'RECLASS'
                   AND j4.FISCAL_PERIOD_ID IN (
                           SELECT tp3.FISCAL_PERIOD_ID FROM cte_target_period tp3
                       )
                 GROUP BY jl4.ACCOUNT_ID, jl4.COST_CENTER_ID
            ),
            cte_account_sk AS (
                SELECT da.ACCOUNT_SK,
                       da.ACCOUNT_ID,
                       da.ACCOUNT_CODE
                  FROM DWH.DIM_ACCOUNT da
            ),
            cte_date_dim AS (
                SELECT dd.DATE_ID,
                       dd.FULL_DATE
                  FROM DWH.DIM_DATE dd
                 WHERE dd.FULL_DATE = (
                           SELECT MAX(tp4.END_DATE)
                             FROM cte_target_period tp4
                       )
            ),
            cte_account_hierarchy AS (
                SELECT a2.ACCOUNT_ID,
                       a2.PARENT_ACCOUNT_ID,
                       a2.LEVEL_NUM,
                       SYS_CONNECT_BY_PATH(a2.ACCOUNT_CODE, '/') AS acct_hier_path,
                       LEVEL AS hier_depth
                  FROM FIN.ACCOUNTS a2
                 WHERE a2.IS_ACTIVE = 'Y'
                 START WITH a2.PARENT_ACCOUNT_ID IS NULL
               CONNECT BY PRIOR a2.ACCOUNT_ID = a2.PARENT_ACCOUNT_ID
            ),
            cte_combined AS (
                SELECT act.ACCOUNT_ID,
                       act.ACCOUNT_CODE,
                       act.ACCOUNT_NAME,
                       act.ACCOUNT_TYPE,
                       pa.COST_CENTER_ID,
                       cctr.CC_CODE,
                       cctr.CC_NAME,
                       p_fiscal_year                               AS fiscal_year,
                       p_fiscal_period                             AS fiscal_period,
                       NVL(pa.currency_code, v_func_curr)          AS currency_code,
                       NVL(ob.opening_balance, 0)                  AS opening_balance,
                       NVL(pa.period_debit, 0)                     AS period_debit,
                       NVL(pa.period_credit, 0)                    AS period_credit,
                       NVL(pa.period_activity, 0)                  AS period_activity,
                       NVL(ob.opening_balance, 0)
                           + NVL(pa.period_activity, 0)            AS closing_balance,
                       NVL(ob.base_opening, 0)                     AS base_opening,
                       NVL(pa.base_period_debit, 0)                AS base_period_debit,
                       NVL(pa.base_period_credit, 0)               AS base_period_credit,
                       NVL(ob.base_opening, 0)
                           + NVL(pa.base_period_debit, 0)
                           - NVL(pa.base_period_credit, 0)         AS base_closing,
                       NVL(adj.adjustment_amount, 0)               AS adjustment_amount,
                       NVL(rcl.reclass_amount, 0)                  AS reclass_amount,
                       NVL(ah.hier_depth, act.LEVEL_NUM)           AS hierarchy_level,
                       act.PARENT_ACCOUNT_ID,
                       NVL(tp5.period_status, 'UNKNOWN')           AS period_status,
                       'ACTIVE'                                    AS status
                  FROM cte_accounts act
                  JOIN cte_period_activity pa
                    ON pa.ACCOUNT_ID = act.ACCOUNT_ID
                  LEFT JOIN cte_cost_centers cctr
                    ON cctr.COST_CENTER_ID = pa.COST_CENTER_ID
                  LEFT JOIN cte_opening_balance ob
                    ON ob.ACCOUNT_ID = act.ACCOUNT_ID
                   AND NVL(ob.COST_CENTER_ID, -1) = NVL(pa.COST_CENTER_ID, -1)
                   AND ob.currency_code = pa.currency_code
                  LEFT JOIN cte_adjustments adj
                    ON adj.ACCOUNT_ID = act.ACCOUNT_ID
                   AND NVL(adj.COST_CENTER_ID, -1) = NVL(pa.COST_CENTER_ID, -1)
                  LEFT JOIN cte_reclassifications rcl
                    ON rcl.ACCOUNT_ID = act.ACCOUNT_ID
                   AND NVL(rcl.COST_CENTER_ID, -1) = NVL(pa.COST_CENTER_ID, -1)
                  LEFT JOIN cte_account_hierarchy ah
                    ON ah.ACCOUNT_ID = act.ACCOUNT_ID
                  LEFT JOIN cte_target_period tp5
                    ON 1 = 1
            )
            SELECT /*+ PARALLEL(8) */
                   DWH.SEQ_DWH_GL_BALANCE.NEXTVAL     AS gl_balance_sk,
                   NVL(ddt.DATE_ID, -1)                AS date_id,
                   NVL(ask_lu.ACCOUNT_SK, -1)          AS account_sk,
                   c.ACCOUNT_ID,
                   c.ACCOUNT_CODE,
                   c.ACCOUNT_NAME,
                   c.ACCOUNT_TYPE,
                   c.COST_CENTER_ID,
                   c.CC_CODE,
                   c.CC_NAME,
                   c.fiscal_year,
                   c.fiscal_period,
                   c.currency_code,
                   c.opening_balance,
                   c.period_debit,
                   c.period_credit,
                   c.period_activity,
                   c.closing_balance,
                   c.base_opening,
                   c.base_period_debit,
                   c.base_period_credit,
                   c.base_closing,
                   c.adjustment_amount,
                   c.reclass_amount,
                   c.hierarchy_level,
                   c.PARENT_ACCOUNT_ID,
                   c.period_status,
                   c.status
              FROM cte_combined c
              LEFT JOIN cte_account_sk ask_lu
                ON ask_lu.ACCOUNT_ID = c.ACCOUNT_ID
              LEFT JOIN cte_date_dim ddt
                ON 1 = 1
             WHERE NOT EXISTS (
                       SELECT 1
                         FROM DWH.FACT_GL_BALANCE fgl
                        WHERE fgl.ACCOUNT_ID = c.ACCOUNT_ID
                          AND NVL(fgl.COST_CENTER_ID, -1) = NVL(c.COST_CENTER_ID, -1)
                          AND fgl.FISCAL_YEAR   = c.fiscal_year
                          AND fgl.FISCAL_PERIOD = c.fiscal_period
                          AND p_mode = 'INCREMENTAL'
                   )
             ORDER BY c.ACCOUNT_CODE, c.CC_CODE;

        LOOP
            FETCH v_cur BULK COLLECT INTO v_buffer LIMIT gc_batch_limit;
            EXIT WHEN v_buffer.COUNT = 0;

            v_gl_sk_arr.DELETE;    v_date_arr.DELETE;
            v_acct_sk_arr.DELETE;  v_aid_arr.DELETE;
            v_acode_arr.DELETE;    v_aname_arr.DELETE;
            v_atype_arr.DELETE;    v_ccid_arr.DELETE;
            v_cccode_arr.DELETE;   v_ccname_arr.DELETE;
            v_fyear_arr.DELETE;    v_fperiod_arr.DELETE;
            v_curr_arr.DELETE;     v_opbal_arr.DELETE;
            v_pdr_arr.DELETE;      v_pcr_arr.DELETE;
            v_pact_arr.DELETE;     v_clbal_arr.DELETE;
            v_bopbal_arr.DELETE;   v_bpdr_arr.DELETE;
            v_bpcr_arr.DELETE;     v_bclbal_arr.DELETE;
            v_adjamt_arr.DELETE;   v_rclamt_arr.DELETE;
            v_hlvl_arr.DELETE;     v_paid_arr.DELETE;
            v_pstat_arr.DELETE;    v_status_arr.DELETE;

            FOR i IN 1 .. v_buffer.COUNT LOOP
                v_gl_sk_arr(i)    := v_buffer(i).gl_balance_sk;
                v_date_arr(i)     := v_buffer(i).date_id;
                v_acct_sk_arr(i)  := v_buffer(i).account_sk;
                v_aid_arr(i)      := v_buffer(i).account_id;
                v_acode_arr(i)    := v_buffer(i).account_code;
                v_aname_arr(i)    := v_buffer(i).account_name;
                v_atype_arr(i)    := v_buffer(i).account_type;
                v_ccid_arr(i)     := v_buffer(i).cost_center_id;
                v_cccode_arr(i)   := v_buffer(i).cc_code;
                v_ccname_arr(i)   := v_buffer(i).cc_name;
                v_fyear_arr(i)    := v_buffer(i).fiscal_year;
                v_fperiod_arr(i)  := v_buffer(i).fiscal_period;
                v_curr_arr(i)     := v_buffer(i).currency_code;
                v_opbal_arr(i)    := v_buffer(i).opening_balance;
                v_pdr_arr(i)      := v_buffer(i).period_debit;
                v_pcr_arr(i)      := v_buffer(i).period_credit;
                v_pact_arr(i)     := v_buffer(i).period_activity;
                v_clbal_arr(i)    := v_buffer(i).closing_balance;
                v_bopbal_arr(i)   := v_buffer(i).base_opening;
                v_bpdr_arr(i)     := v_buffer(i).base_period_debit;
                v_bpcr_arr(i)     := v_buffer(i).base_period_credit;
                v_bclbal_arr(i)   := v_buffer(i).base_closing;
                v_adjamt_arr(i)   := v_buffer(i).adjustment_amount;
                v_rclamt_arr(i)   := v_buffer(i).reclass_amount;
                v_hlvl_arr(i)     := v_buffer(i).hierarchy_level;
                v_paid_arr(i)     := v_buffer(i).parent_account_id;
                v_pstat_arr(i)    := v_buffer(i).period_status;
                v_status_arr(i)   := v_buffer(i).status;
            END LOOP;

            BEGIN
                FORALL i IN VALUES OF v_gl_sk_arr SAVE EXCEPTIONS
                    INSERT /*+ APPEND PARALLEL(8) */
                      INTO DWH.FACT_GL_BALANCE_STG (
                               gl_balance_sk, date_id, account_sk,
                               account_id, account_code, account_name,
                               account_type, cost_center_id, cc_code, cc_name,
                               fiscal_year, fiscal_period, currency_code,
                               opening_balance, period_debit, period_credit,
                               period_activity, closing_balance,
                               base_opening, base_period_debit,
                               base_period_credit, base_closing,
                               adjustment_amount, reclass_amount,
                               hierarchy_level, parent_account_id,
                               period_status, status, created_date
                           )
                    VALUES (
                               v_gl_sk_arr(i), v_date_arr(i), v_acct_sk_arr(i),
                               v_aid_arr(i), v_acode_arr(i), v_aname_arr(i),
                               v_atype_arr(i), v_ccid_arr(i), v_cccode_arr(i), v_ccname_arr(i),
                               v_fyear_arr(i), v_fperiod_arr(i), v_curr_arr(i),
                               v_opbal_arr(i), v_pdr_arr(i), v_pcr_arr(i),
                               v_pact_arr(i), v_clbal_arr(i),
                               v_bopbal_arr(i), v_bpdr_arr(i),
                               v_bpcr_arr(i), v_bclbal_arr(i),
                               v_adjamt_arr(i), v_rclamt_arr(i),
                               v_hlvl_arr(i), v_paid_arr(i),
                               v_pstat_arr(i), v_status_arr(i), SYSDATE
                           );
            EXCEPTION
                WHEN OTHERS THEN
                    IF SQLCODE = -24381 THEN
                        v_error_count := v_error_count + SQL%BULK_EXCEPTIONS.COUNT;
                        handle_forall_exceptions(v_step, SQL%BULK_EXCEPTIONS.COUNT);
                    ELSE
                        RAISE;
                    END IF;
            END;

            v_total_rows := v_total_rows + v_buffer.COUNT;
            COMMIT;
        END LOOP;

        CLOSE v_cur;

        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_GL_BALANCE tgt
        USING (
            WITH cte_stg_ranked AS (
                SELECT s.*,
                       ROW_NUMBER() OVER (
                           PARTITION BY s.account_id, s.cost_center_id,
                                        s.fiscal_year, s.fiscal_period
                           ORDER BY s.gl_balance_sk DESC
                       ) AS merge_rn
                  FROM DWH.FACT_GL_BALANCE_STG s
                 WHERE s.created_date >= TRUNC(SYSDATE)
            ),
            cte_stg_valid AS (
                SELECT r.*
                  FROM cte_stg_ranked r
                 WHERE r.merge_rn = 1
            ),
            cte_stg_analytics AS (
                SELECT v.*,
                       SUM(v.period_activity) OVER (
                           PARTITION BY v.account_id
                           ORDER BY v.fiscal_year, v.fiscal_period
                           ROWS UNBOUNDED PRECEDING
                       ) AS cumulative_activity,
                       LAG(v.closing_balance) OVER (
                           PARTITION BY v.account_id, v.cost_center_id
                           ORDER BY v.fiscal_year, v.fiscal_period
                       ) AS prior_closing_balance,
                       ROW_NUMBER() OVER (
                           PARTITION BY v.account_type
                           ORDER BY ABS(v.closing_balance) DESC
                       ) AS balance_rank
                  FROM cte_stg_valid v
            )
            SELECT a.gl_balance_sk, a.date_id, a.account_sk,
                   a.account_id, a.account_code, a.account_name,
                   a.account_type, a.cost_center_id, a.cc_code, a.cc_name,
                   a.fiscal_year, a.fiscal_period, a.currency_code,
                   a.opening_balance, a.period_debit, a.period_credit,
                   a.period_activity, a.closing_balance,
                   a.base_opening, a.base_period_debit,
                   a.base_period_credit, a.base_closing,
                   a.adjustment_amount, a.reclass_amount,
                   a.hierarchy_level, a.parent_account_id,
                   a.period_status, a.status
              FROM cte_stg_analytics a
        ) src
        ON (    tgt.ACCOUNT_ID   = src.account_id
            AND NVL(tgt.COST_CENTER_ID, -1) = NVL(src.cost_center_id, -1)
            AND tgt.FISCAL_YEAR  = src.fiscal_year
            AND tgt.FISCAL_PERIOD = src.fiscal_period)
        WHEN MATCHED THEN
            UPDATE SET
                tgt.OPENING_BALANCE  = src.opening_balance,
                tgt.PERIOD_DEBIT     = src.period_debit,
                tgt.PERIOD_CREDIT    = src.period_credit,
                tgt.PERIOD_ACTIVITY  = src.period_activity,
                tgt.CLOSING_BALANCE  = src.closing_balance,
                tgt.BASE_CLOSING     = src.base_closing,
                tgt.ADJUSTMENT_AMOUNT = src.adjustment_amount,
                tgt.RECLASS_AMOUNT   = src.reclass_amount,
                tgt.UPDATED_DATE     = SYSDATE
             WHERE tgt.CLOSING_BALANCE <> src.closing_balance
                OR tgt.PERIOD_DEBIT    <> src.period_debit
                OR tgt.PERIOD_CREDIT   <> src.period_credit
        WHEN NOT MATCHED THEN
            INSERT (
                GL_BALANCE_SK, DATE_ID, ACCOUNT_SK,
                ACCOUNT_ID, ACCOUNT_CODE, ACCOUNT_NAME, ACCOUNT_TYPE,
                COST_CENTER_ID, CC_CODE, CC_NAME,
                FISCAL_YEAR, FISCAL_PERIOD, CURRENCY_CODE,
                OPENING_BALANCE, PERIOD_DEBIT, PERIOD_CREDIT,
                PERIOD_ACTIVITY, CLOSING_BALANCE,
                BASE_OPENING, BASE_PERIOD_DEBIT,
                BASE_PERIOD_CREDIT, BASE_CLOSING,
                ADJUSTMENT_AMOUNT, RECLASS_AMOUNT,
                HIERARCHY_LEVEL, PARENT_ACCOUNT_ID,
                PERIOD_STATUS, STATUS, CREATED_DATE
            )
            VALUES (
                src.gl_balance_sk, src.date_id, src.account_sk,
                src.account_id, src.account_code, src.account_name, src.account_type,
                src.cost_center_id, src.cc_code, src.cc_name,
                src.fiscal_year, src.fiscal_period, src.currency_code,
                src.opening_balance, src.period_debit, src.period_credit,
                src.period_activity, src.closing_balance,
                src.base_opening, src.base_period_debit,
                src.base_period_credit, src.base_closing,
                src.adjustment_amount, src.reclass_amount,
                src.hierarchy_level, src.parent_account_id,
                src.period_status, src.status, SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;

        log_step(v_step || '.MERGE', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_GL_TRIAL_BALANCE tgt
        USING (
            WITH cte_gl_base AS (
                SELECT fgl.ACCOUNT_ID,
                       fgl.ACCOUNT_CODE,
                       fgl.ACCOUNT_NAME,
                       fgl.ACCOUNT_TYPE,
                       fgl.COST_CENTER_ID,
                       fgl.CC_CODE,
                       fgl.FISCAL_YEAR,
                       fgl.FISCAL_PERIOD,
                       fgl.CURRENCY_CODE,
                       fgl.OPENING_BALANCE,
                       fgl.PERIOD_DEBIT,
                       fgl.PERIOD_CREDIT,
                       fgl.CLOSING_BALANCE,
                       fgl.BASE_OPENING,
                       fgl.BASE_PERIOD_DEBIT,
                       fgl.BASE_PERIOD_CREDIT,
                       fgl.BASE_CLOSING,
                       fgl.ADJUSTMENT_AMOUNT,
                       fgl.RECLASS_AMOUNT,
                       fgl.HIERARCHY_LEVEL,
                       fgl.PARENT_ACCOUNT_ID,
                       ROW_NUMBER() OVER (
                           PARTITION BY fgl.ACCOUNT_ID, fgl.COST_CENTER_ID,
                                        fgl.FISCAL_YEAR, fgl.FISCAL_PERIOD
                           ORDER BY fgl.GL_BALANCE_SK DESC
                       ) AS tb_rn
                  FROM DWH.FACT_GL_BALANCE fgl
                 WHERE fgl.FISCAL_YEAR   = p_fiscal_year
                   AND fgl.FISCAL_PERIOD = p_fiscal_period
            ),
            cte_gl_dedup AS (
                SELECT gb.*
                  FROM cte_gl_base gb
                 WHERE gb.tb_rn = 1
            ),
            cte_account_hier AS (
                SELECT a.ACCOUNT_ID,
                       a.PARENT_ACCOUNT_ID,
                       a.ACCOUNT_TYPE,
                       a.NORMAL_BALANCE,
                       SYS_CONNECT_BY_PATH(a.ACCOUNT_CODE, '/') AS full_path,
                       CONNECT_BY_ROOT a.ACCOUNT_CODE AS root_code,
                       LEVEL AS hier_level
                  FROM FIN.ACCOUNTS a
                 WHERE a.IS_ACTIVE = 'Y'
                 START WITH a.PARENT_ACCOUNT_ID IS NULL
               CONNECT BY PRIOR a.ACCOUNT_ID = a.PARENT_ACCOUNT_ID
            ),
            cte_prior_period_balance AS (
                SELECT fgl2.ACCOUNT_ID,
                       fgl2.COST_CENTER_ID,
                       fgl2.CLOSING_BALANCE AS prior_closing,
                       fgl2.BASE_CLOSING    AS prior_base_closing,
                       ROW_NUMBER() OVER (
                           PARTITION BY fgl2.ACCOUNT_ID, fgl2.COST_CENTER_ID
                           ORDER BY fgl2.GL_BALANCE_SK DESC
                       ) AS pp_rn
                  FROM DWH.FACT_GL_BALANCE fgl2
                 WHERE (fgl2.FISCAL_YEAR = p_fiscal_year AND fgl2.FISCAL_PERIOD = p_fiscal_period - 1)
                    OR (fgl2.FISCAL_YEAR = p_fiscal_year - 1 AND fgl2.FISCAL_PERIOD = 12
                        AND p_fiscal_period = 1)
            ),
            cte_ytd_activity AS (
                SELECT fgl3.ACCOUNT_ID,
                       fgl3.COST_CENTER_ID,
                       SUM(fgl3.PERIOD_DEBIT)  AS ytd_debit,
                       SUM(fgl3.PERIOD_CREDIT) AS ytd_credit,
                       SUM(fgl3.PERIOD_DEBIT - fgl3.PERIOD_CREDIT) AS ytd_activity
                  FROM DWH.FACT_GL_BALANCE fgl3
                 WHERE fgl3.FISCAL_YEAR = p_fiscal_year
                   AND fgl3.FISCAL_PERIOD <= p_fiscal_period
                 GROUP BY fgl3.ACCOUNT_ID, fgl3.COST_CENTER_ID
            ),
            cte_tb_combined AS (
                SELECT gd.ACCOUNT_ID,
                       gd.ACCOUNT_CODE,
                       gd.ACCOUNT_NAME,
                       gd.ACCOUNT_TYPE,
                       gd.COST_CENTER_ID,
                       gd.CC_CODE,
                       gd.FISCAL_YEAR,
                       gd.FISCAL_PERIOD,
                       gd.CURRENCY_CODE,
                       gd.OPENING_BALANCE,
                       gd.PERIOD_DEBIT,
                       gd.PERIOD_CREDIT,
                       gd.CLOSING_BALANCE,
                       gd.BASE_OPENING,
                       gd.BASE_PERIOD_DEBIT,
                       gd.BASE_PERIOD_CREDIT,
                       gd.BASE_CLOSING,
                       gd.ADJUSTMENT_AMOUNT,
                       gd.RECLASS_AMOUNT,
                       NVL(ah.full_path, gd.ACCOUNT_CODE) AS account_hierarchy_path,
                       NVL(ah.root_code, gd.ACCOUNT_CODE) AS root_account_code,
                       NVL(ppb.prior_closing, 0)          AS prior_period_closing,
                       NVL(ppb.prior_base_closing, 0)     AS prior_base_closing,
                       gd.CLOSING_BALANCE - NVL(ppb.prior_closing, 0)
                                                           AS period_movement,
                       NVL(ya.ytd_debit, 0)               AS ytd_debit,
                       NVL(ya.ytd_credit, 0)              AS ytd_credit,
                       NVL(ya.ytd_activity, 0)            AS ytd_activity,
                       RANK() OVER (
                           ORDER BY ABS(gd.CLOSING_BALANCE) DESC
                       ) AS balance_rank,
                       SUM(gd.PERIOD_DEBIT) OVER (
                           PARTITION BY gd.ACCOUNT_TYPE
                       ) AS type_total_debit,
                       SUM(gd.PERIOD_CREDIT) OVER (
                           PARTITION BY gd.ACCOUNT_TYPE
                       ) AS type_total_credit,
                       CASE
                           WHEN gd.ACCOUNT_TYPE IN ('ASSET', 'EXPENSE')
                           THEN gd.PERIOD_DEBIT - gd.PERIOD_CREDIT
                           ELSE gd.PERIOD_CREDIT - gd.PERIOD_DEBIT
                       END AS normal_activity
                  FROM cte_gl_dedup gd
                  LEFT JOIN cte_account_hier ah
                    ON ah.ACCOUNT_ID = gd.ACCOUNT_ID
                  LEFT JOIN cte_prior_period_balance ppb
                    ON ppb.ACCOUNT_ID = gd.ACCOUNT_ID
                   AND NVL(ppb.COST_CENTER_ID, -1) = NVL(gd.COST_CENTER_ID, -1)
                   AND ppb.pp_rn = 1
                  LEFT JOIN cte_ytd_activity ya
                    ON ya.ACCOUNT_ID = gd.ACCOUNT_ID
                   AND NVL(ya.COST_CENTER_ID, -1) = NVL(gd.COST_CENTER_ID, -1)
            )
            SELECT tc.ACCOUNT_ID, tc.ACCOUNT_CODE, tc.ACCOUNT_NAME,
                   tc.ACCOUNT_TYPE, tc.COST_CENTER_ID, tc.CC_CODE,
                   tc.FISCAL_YEAR, tc.FISCAL_PERIOD, tc.CURRENCY_CODE,
                   tc.OPENING_BALANCE, tc.PERIOD_DEBIT, tc.PERIOD_CREDIT,
                   tc.CLOSING_BALANCE, tc.BASE_OPENING,
                   tc.BASE_PERIOD_DEBIT, tc.BASE_PERIOD_CREDIT, tc.BASE_CLOSING,
                   tc.ADJUSTMENT_AMOUNT, tc.RECLASS_AMOUNT,
                   tc.account_hierarchy_path, tc.root_account_code,
                   tc.prior_period_closing, tc.period_movement,
                   tc.ytd_debit, tc.ytd_credit, tc.ytd_activity,
                   tc.balance_rank, tc.normal_activity
              FROM cte_tb_combined tc
        ) src
        ON (    tgt.ACCOUNT_ID     = src.ACCOUNT_ID
            AND NVL(tgt.COST_CENTER_ID, -1) = NVL(src.COST_CENTER_ID, -1)
            AND tgt.FISCAL_YEAR    = src.FISCAL_YEAR
            AND tgt.FISCAL_PERIOD  = src.FISCAL_PERIOD)
        WHEN MATCHED THEN
            UPDATE SET
                tgt.OPENING_BALANCE   = src.OPENING_BALANCE,
                tgt.PERIOD_DEBIT      = src.PERIOD_DEBIT,
                tgt.PERIOD_CREDIT     = src.PERIOD_CREDIT,
                tgt.CLOSING_BALANCE   = src.CLOSING_BALANCE,
                tgt.ADJUSTMENT_AMOUNT = src.ADJUSTMENT_AMOUNT,
                tgt.RECLASS_AMOUNT    = src.RECLASS_AMOUNT,
                tgt.PERIOD_MOVEMENT   = src.period_movement,
                tgt.YTD_DEBIT         = src.ytd_debit,
                tgt.YTD_CREDIT        = src.ytd_credit,
                tgt.YTD_ACTIVITY      = src.ytd_activity,
                tgt.BALANCE_RANK      = src.balance_rank,
                tgt.UPDATED_DATE      = SYSDATE
             WHERE tgt.CLOSING_BALANCE <> src.CLOSING_BALANCE
                OR tgt.PERIOD_DEBIT    <> src.PERIOD_DEBIT
        WHEN NOT MATCHED THEN
            INSERT (
                ACCOUNT_ID, ACCOUNT_CODE, ACCOUNT_NAME, ACCOUNT_TYPE,
                COST_CENTER_ID, CC_CODE,
                FISCAL_YEAR, FISCAL_PERIOD, CURRENCY_CODE,
                OPENING_BALANCE, PERIOD_DEBIT, PERIOD_CREDIT,
                CLOSING_BALANCE, BASE_OPENING,
                BASE_PERIOD_DEBIT, BASE_PERIOD_CREDIT, BASE_CLOSING,
                ADJUSTMENT_AMOUNT, RECLASS_AMOUNT,
                ACCOUNT_HIERARCHY_PATH, ROOT_ACCOUNT_CODE,
                PRIOR_PERIOD_CLOSING, PERIOD_MOVEMENT,
                YTD_DEBIT, YTD_CREDIT, YTD_ACTIVITY,
                BALANCE_RANK, NORMAL_ACTIVITY,
                CREATED_DATE
            )
            VALUES (
                src.ACCOUNT_ID, src.ACCOUNT_CODE, src.ACCOUNT_NAME, src.ACCOUNT_TYPE,
                src.COST_CENTER_ID, src.CC_CODE,
                src.FISCAL_YEAR, src.FISCAL_PERIOD, src.CURRENCY_CODE,
                src.OPENING_BALANCE, src.PERIOD_DEBIT, src.PERIOD_CREDIT,
                src.CLOSING_BALANCE, src.BASE_OPENING,
                src.BASE_PERIOD_DEBIT, src.BASE_PERIOD_CREDIT, src.BASE_CLOSING,
                src.ADJUSTMENT_AMOUNT, src.RECLASS_AMOUNT,
                src.account_hierarchy_path, src.root_account_code,
                src.prior_period_closing, src.period_movement,
                src.ytd_debit, src.ytd_credit, src.ytd_activity,
                src.balance_rank, src.normal_activity,
                SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.TRIAL_BALANCE', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        INSERT /*+ PARALLEL(8) */
          INTO DWH.FACT_GL_BALANCE_CHECK (
                   fiscal_year, fiscal_period,
                   check_type, check_result,
                   total_debit, total_credit,
                   difference, account_type,
                   detail_text, created_date
               )
        WITH cte_debit_credit_check AS (
            SELECT fgl.FISCAL_YEAR,
                   fgl.FISCAL_PERIOD,
                   'DEBIT_CREDIT_BALANCE' AS check_type,
                   CASE
                       WHEN ABS(SUM(fgl.PERIOD_DEBIT) - SUM(fgl.PERIOD_CREDIT)) < 0.01
                       THEN 'PASS' ELSE 'FAIL'
                   END AS check_result,
                   SUM(fgl.PERIOD_DEBIT)  AS total_debit,
                   SUM(fgl.PERIOD_CREDIT) AS total_credit,
                   ABS(SUM(fgl.PERIOD_DEBIT) - SUM(fgl.PERIOD_CREDIT)) AS difference,
                   'ALL' AS account_type,
                   'Total DR=' || TO_CHAR(SUM(fgl.PERIOD_DEBIT), 'FM999,999,999,990.00')
                       || ' CR=' || TO_CHAR(SUM(fgl.PERIOD_CREDIT), 'FM999,999,999,990.00')
                   AS detail_text
              FROM DWH.FACT_GL_BALANCE fgl
             WHERE fgl.FISCAL_YEAR   = p_fiscal_year
               AND fgl.FISCAL_PERIOD = p_fiscal_period
             GROUP BY fgl.FISCAL_YEAR, fgl.FISCAL_PERIOD
        ),
        cte_type_check AS (
            SELECT fgl2.FISCAL_YEAR,
                   fgl2.FISCAL_PERIOD,
                   'ACCOUNT_TYPE_BALANCE' AS check_type,
                   CASE
                       WHEN ABS(SUM(CASE WHEN fgl2.ACCOUNT_TYPE IN ('ASSET', 'EXPENSE')
                                         THEN fgl2.CLOSING_BALANCE ELSE 0 END)
                              - SUM(CASE WHEN fgl2.ACCOUNT_TYPE IN ('LIABILITY', 'EQUITY', 'REVENUE')
                                         THEN fgl2.CLOSING_BALANCE ELSE 0 END)) < 1.00
                       THEN 'PASS' ELSE 'WARNING'
                   END AS check_result,
                   SUM(CASE WHEN fgl2.ACCOUNT_TYPE IN ('ASSET', 'EXPENSE')
                            THEN fgl2.CLOSING_BALANCE ELSE 0 END) AS total_debit,
                   SUM(CASE WHEN fgl2.ACCOUNT_TYPE IN ('LIABILITY', 'EQUITY', 'REVENUE')
                            THEN fgl2.CLOSING_BALANCE ELSE 0 END) AS total_credit,
                   ABS(SUM(CASE WHEN fgl2.ACCOUNT_TYPE IN ('ASSET', 'EXPENSE')
                                THEN fgl2.CLOSING_BALANCE ELSE 0 END)
                     - SUM(CASE WHEN fgl2.ACCOUNT_TYPE IN ('LIABILITY', 'EQUITY', 'REVENUE')
                                THEN fgl2.CLOSING_BALANCE ELSE 0 END)) AS difference,
                   'BALANCED_TYPES' AS account_type,
                   'Assets+Expenses vs Liabilities+Equity+Revenue check' AS detail_text
              FROM DWH.FACT_GL_BALANCE fgl2
             WHERE fgl2.FISCAL_YEAR   = p_fiscal_year
               AND fgl2.FISCAL_PERIOD = p_fiscal_period
             GROUP BY fgl2.FISCAL_YEAR, fgl2.FISCAL_PERIOD
        ),
        cte_opening_check AS (
            SELECT fgl3.FISCAL_YEAR,
                   fgl3.FISCAL_PERIOD,
                   'OPENING_CLOSING_CONT' AS check_type,
                   CASE
                       WHEN COUNT(CASE WHEN ABS(fgl3.OPENING_BALANCE
                                - NVL(pp.CLOSING_BALANCE, 0)) > 0.01 THEN 1 END) = 0
                       THEN 'PASS' ELSE 'FAIL'
                   END AS check_result,
                   SUM(fgl3.OPENING_BALANCE) AS total_debit,
                   SUM(NVL(pp.CLOSING_BALANCE, 0)) AS total_credit,
                   SUM(ABS(fgl3.OPENING_BALANCE - NVL(pp.CLOSING_BALANCE, 0))) AS difference,
                   'ALL' AS account_type,
                   'Opening balance continuity: '
                       || COUNT(CASE WHEN ABS(fgl3.OPENING_BALANCE
                                - NVL(pp.CLOSING_BALANCE, 0)) > 0.01 THEN 1 END)
                       || ' discrepancies found'
                   AS detail_text
              FROM DWH.FACT_GL_BALANCE fgl3
              LEFT JOIN DWH.FACT_GL_BALANCE pp
                ON pp.ACCOUNT_ID = fgl3.ACCOUNT_ID
               AND NVL(pp.COST_CENTER_ID, -1) = NVL(fgl3.COST_CENTER_ID, -1)
               AND ((pp.FISCAL_YEAR = fgl3.FISCAL_YEAR AND pp.FISCAL_PERIOD = fgl3.FISCAL_PERIOD - 1)
                 OR (pp.FISCAL_YEAR = fgl3.FISCAL_YEAR - 1 AND pp.FISCAL_PERIOD = 12
                     AND fgl3.FISCAL_PERIOD = 1))
             WHERE fgl3.FISCAL_YEAR   = p_fiscal_year
               AND fgl3.FISCAL_PERIOD = p_fiscal_period
             GROUP BY fgl3.FISCAL_YEAR, fgl3.FISCAL_PERIOD
        )
        SELECT * FROM cte_debit_credit_check
        UNION ALL
        SELECT * FROM cte_type_check
        UNION ALL
        SELECT * FROM cte_opening_check;

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.BALANCE_CHECK', 'DONE', g_row_count);

        log_step(v_step || '.COMPLETE', 'SUCCESS', v_total_rows);

    EXCEPTION
        WHEN OTHERS THEN
            IF v_cur%ISOPEN THEN CLOSE v_cur; END IF;
            ROLLBACK;
            log_step(v_step || '.FATAL', 'ERROR', 0, SQLERRM);
            RAISE;
    END load_fact_gl_balance;

    -- =========================================================================
    -- 7. CALC_INTERCOMPANY_ELIM
    -- =========================================================================
    PROCEDURE calc_intercompany_elim (
        p_fiscal_year   IN NUMBER,
        p_fiscal_period IN NUMBER
    ) IS
        v_step          VARCHAR2(200) := 'CALC_INTERCOMPANY_ELIM';
        v_total_rows    NUMBER(19) := 0;
        v_error_count   PLS_INTEGER := 0;

        TYPE t_ic_cursor IS REF CURSOR;
        v_cur           t_ic_cursor;
        v_buffer        t_ic_elim_tab;

        v_elim_sk_arr   t_id_array;
        v_jid_dr_arr    t_id_array;
        v_jid_cr_arr    t_id_array;
        v_ent_dr_arr    t_id_array;
        v_ent_cr_arr    t_id_array;
        v_aid_arr       t_id_array;
        v_acode_arr     t_varchar_array;
        v_fyear_arr     t_id_array;
        v_fperiod_arr   t_id_array;
        v_dr_arr        t_amount_array;
        v_cr_arr        t_amount_array;
        v_bdr_arr       t_amount_array;
        v_bcr_arr       t_amount_array;
        v_curr_arr      t_varchar_array;
        v_xrate_arr     t_amount_array;
        v_mismatch_arr  t_amount_array;
        v_ejnum_arr     t_varchar_array;
        v_status_arr    t_varchar_array;
    BEGIN
        init_run(v_step);

        g_step_start := SYSTIMESTAMP;

        OPEN v_cur FOR
            WITH cte_fiscal AS (
                SELECT fp.FISCAL_PERIOD_ID,
                       fp.FISCAL_YEAR,
                       fp.PERIOD_NUMBER,
                       fp.START_DATE,
                       fp.END_DATE,
                       fp.STATUS AS period_status
                  FROM FIN.FISCAL_PERIODS fp
                 WHERE fp.FISCAL_YEAR   = p_fiscal_year
                   AND fp.PERIOD_NUMBER = p_fiscal_period
            ),
            cte_ic_journals AS (
                SELECT j.JOURNAL_ID,
                       j.JOURNAL_NUM,
                       j.JOURNAL_TYPE,
                       j.JOURNAL_DATE,
                       j.FISCAL_PERIOD_ID,
                       j.STATUS,
                       j.CURRENCY_ID
                  FROM FIN.JOURNALS j
                 WHERE j.STATUS = 'POSTED'
                   AND j.FISCAL_PERIOD_ID IN (
                           SELECT cf.FISCAL_PERIOD_ID FROM cte_fiscal cf
                       )
                   AND EXISTS (
                           SELECT 1
                             FROM FIN.JOURNAL_LINES jl_chk
                            WHERE jl_chk.JOURNAL_ID = j.JOURNAL_ID
                            GROUP BY jl_chk.JOURNAL_ID
                           HAVING COUNT(DISTINCT jl_chk.COST_CENTER_ID) > 1
                       )
            ),
            cte_ic_lines AS (
                SELECT jl.JOURNAL_LINE_ID,
                       jl.JOURNAL_ID,
                       jl.LINE_NUM,
                       jl.ACCOUNT_ID,
                       jl.COST_CENTER_ID,
                       jl.DEBIT_AMOUNT,
                       jl.CREDIT_AMOUNT,
                       jl.BASE_DEBIT,
                       jl.BASE_CREDIT,
                       jl.CURRENCY_ID,
                       jl.EXCHANGE_RATE
                  FROM FIN.JOURNAL_LINES jl
                 WHERE jl.JOURNAL_ID IN (
                           SELECT icj.JOURNAL_ID FROM cte_ic_journals icj
                       )
            ),
            cte_dr_side AS (
                SELECT icl.JOURNAL_ID,
                       icl.ACCOUNT_ID,
                       icl.COST_CENTER_ID AS entity_dr,
                       SUM(NVL(icl.DEBIT_AMOUNT, 0))  AS total_debit,
                       SUM(NVL(icl.BASE_DEBIT, 0))    AS base_total_debit,
                       MAX(icl.CURRENCY_ID)            AS dr_currency_id,
                       MAX(icl.EXCHANGE_RATE)          AS dr_exchange_rate
                  FROM cte_ic_lines icl
                 WHERE NVL(icl.DEBIT_AMOUNT, 0) > 0
                 GROUP BY icl.JOURNAL_ID, icl.ACCOUNT_ID, icl.COST_CENTER_ID
            ),
            cte_cr_side AS (
                SELECT icl.JOURNAL_ID,
                       icl.ACCOUNT_ID,
                       icl.COST_CENTER_ID AS entity_cr,
                       SUM(NVL(icl.CREDIT_AMOUNT, 0)) AS total_credit,
                       SUM(NVL(icl.BASE_CREDIT, 0))   AS base_total_credit,
                       MAX(icl.CURRENCY_ID)            AS cr_currency_id,
                       MAX(icl.EXCHANGE_RATE)          AS cr_exchange_rate
                  FROM cte_ic_lines icl
                 WHERE NVL(icl.CREDIT_AMOUNT, 0) > 0
                 GROUP BY icl.JOURNAL_ID, icl.ACCOUNT_ID, icl.COST_CENTER_ID
            ),
            cte_accounts AS (
                SELECT a.ACCOUNT_ID,
                       a.ACCOUNT_CODE,
                       a.ACCOUNT_NAME,
                       a.ACCOUNT_TYPE
                  FROM FIN.ACCOUNTS a
                 WHERE a.IS_ACTIVE = 'Y'
            ),
            cte_fx_rate AS (
                SELECT cur.CURRENCY_ID,
                       cur.CURRENCY_CODE,
                       cur.IS_FUNCTIONAL
                  FROM FIN.CURRENCIES cur
            ),
            cte_matched AS (
                SELECT dr.JOURNAL_ID            AS journal_id_dr,
                       cr.JOURNAL_ID            AS journal_id_cr,
                       dr.entity_dr,
                       cr.entity_cr,
                       dr.ACCOUNT_ID,
                       act.ACCOUNT_CODE,
                       dr.total_debit,
                       cr.total_credit,
                       dr.base_total_debit,
                       cr.base_total_credit,
                       NVL(fx.CURRENCY_CODE, 'USD') AS currency_code,
                       NVL(dr.dr_exchange_rate, 1)  AS exchange_rate,
                       ABS(dr.base_total_debit - cr.base_total_credit) AS mismatch_amount,
                       ROW_NUMBER() OVER (
                           PARTITION BY dr.JOURNAL_ID, dr.ACCOUNT_ID
                           ORDER BY dr.entity_dr, cr.entity_cr
                       ) AS match_rn
                  FROM cte_dr_side dr
                  JOIN cte_cr_side cr
                    ON cr.JOURNAL_ID = dr.JOURNAL_ID
                   AND cr.ACCOUNT_ID = dr.ACCOUNT_ID
                   AND cr.entity_cr <> dr.entity_dr
                  LEFT JOIN cte_accounts act
                    ON act.ACCOUNT_ID = dr.ACCOUNT_ID
                  LEFT JOIN cte_fx_rate fx
                    ON fx.CURRENCY_ID = dr.dr_currency_id
            ),
            cte_recon AS (
                SELECT m.journal_id_dr,
                       m.journal_id_cr,
                       m.entity_dr,
                       m.entity_cr,
                       m.ACCOUNT_ID,
                       m.ACCOUNT_CODE,
                       m.total_debit,
                       m.total_credit,
                       m.base_total_debit,
                       m.base_total_credit,
                       m.currency_code,
                       m.exchange_rate,
                       m.mismatch_amount,
                       CASE
                           WHEN m.mismatch_amount < 0.01 THEN 'BALANCED'
                           WHEN m.mismatch_amount < 1.00 THEN 'MINOR_DIFF'
                           ELSE 'MISMATCH'
                       END AS recon_status,
                       SUM(m.base_total_debit) OVER (
                           PARTITION BY m.entity_dr, m.entity_cr
                       ) AS total_ic_debit_pair,
                       SUM(m.base_total_credit) OVER (
                           PARTITION BY m.entity_dr, m.entity_cr
                       ) AS total_ic_credit_pair
                  FROM cte_matched m
                 WHERE m.match_rn = 1
            )
            SELECT DWH.SEQ_DWH_IC_ELIM.NEXTVAL         AS elim_sk,
                   r.journal_id_dr,
                   r.journal_id_cr,
                   r.entity_dr,
                   r.entity_cr,
                   r.ACCOUNT_ID,
                   r.ACCOUNT_CODE,
                   p_fiscal_year                         AS fiscal_year,
                   p_fiscal_period                       AS fiscal_period,
                   r.total_debit                         AS debit_amount,
                   r.total_credit                        AS credit_amount,
                   r.base_total_debit                    AS base_debit,
                   r.base_total_credit                   AS base_credit,
                   r.currency_code,
                   r.exchange_rate,
                   r.mismatch_amount,
                   'ELIM-' || p_fiscal_year || '-'
                       || LPAD(p_fiscal_period, 2, '0')
                       || '-' || r.ACCOUNT_CODE          AS elim_journal_num,
                   r.recon_status                        AS status
              FROM cte_recon r
             WHERE NOT EXISTS (
                       SELECT 1
                         FROM DWH.FACT_IC_ELIMINATIONS fie
                        WHERE fie.JOURNAL_ID_DR = r.journal_id_dr
                          AND fie.ACCOUNT_ID    = r.ACCOUNT_ID
                          AND fie.FISCAL_YEAR   = p_fiscal_year
                          AND fie.FISCAL_PERIOD = p_fiscal_period
                   )
             ORDER BY r.entity_dr, r.entity_cr, r.ACCOUNT_CODE;

        LOOP
            FETCH v_cur BULK COLLECT INTO v_buffer LIMIT gc_batch_limit;
            EXIT WHEN v_buffer.COUNT = 0;

            v_elim_sk_arr.DELETE;  v_jid_dr_arr.DELETE;
            v_jid_cr_arr.DELETE;   v_ent_dr_arr.DELETE;
            v_ent_cr_arr.DELETE;   v_aid_arr.DELETE;
            v_acode_arr.DELETE;    v_fyear_arr.DELETE;
            v_fperiod_arr.DELETE;  v_dr_arr.DELETE;
            v_cr_arr.DELETE;       v_bdr_arr.DELETE;
            v_bcr_arr.DELETE;      v_curr_arr.DELETE;
            v_xrate_arr.DELETE;    v_mismatch_arr.DELETE;
            v_ejnum_arr.DELETE;    v_status_arr.DELETE;

            FOR i IN 1 .. v_buffer.COUNT LOOP
                v_elim_sk_arr(i)  := v_buffer(i).elim_sk;
                v_jid_dr_arr(i)   := v_buffer(i).journal_id_dr;
                v_jid_cr_arr(i)   := v_buffer(i).journal_id_cr;
                v_ent_dr_arr(i)   := v_buffer(i).entity_dr;
                v_ent_cr_arr(i)   := v_buffer(i).entity_cr;
                v_aid_arr(i)      := v_buffer(i).account_id;
                v_acode_arr(i)    := v_buffer(i).account_code;
                v_fyear_arr(i)    := v_buffer(i).fiscal_year;
                v_fperiod_arr(i)  := v_buffer(i).fiscal_period;
                v_dr_arr(i)       := v_buffer(i).debit_amount;
                v_cr_arr(i)       := v_buffer(i).credit_amount;
                v_bdr_arr(i)      := v_buffer(i).base_debit;
                v_bcr_arr(i)      := v_buffer(i).base_credit;
                v_curr_arr(i)     := v_buffer(i).currency_code;
                v_xrate_arr(i)    := v_buffer(i).exchange_rate;
                v_mismatch_arr(i) := v_buffer(i).mismatch_amount;
                v_ejnum_arr(i)    := v_buffer(i).elim_journal_num;
                v_status_arr(i)   := v_buffer(i).status;
            END LOOP;

            BEGIN
                FORALL i IN VALUES OF v_elim_sk_arr SAVE EXCEPTIONS
                    INSERT /*+ APPEND PARALLEL(8) */
                      INTO DWH.FACT_IC_ELIMINATIONS (
                               elim_sk, journal_id_dr, journal_id_cr,
                               entity_dr, entity_cr,
                               account_id, account_code,
                               fiscal_year, fiscal_period,
                               debit_amount, credit_amount,
                               base_debit, base_credit,
                               currency_code, exchange_rate,
                               mismatch_amount, elim_journal_num,
                               status, created_date
                           )
                    VALUES (
                               v_elim_sk_arr(i), v_jid_dr_arr(i), v_jid_cr_arr(i),
                               v_ent_dr_arr(i), v_ent_cr_arr(i),
                               v_aid_arr(i), v_acode_arr(i),
                               v_fyear_arr(i), v_fperiod_arr(i),
                               v_dr_arr(i), v_cr_arr(i),
                               v_bdr_arr(i), v_bcr_arr(i),
                               v_curr_arr(i), v_xrate_arr(i),
                               v_mismatch_arr(i), v_ejnum_arr(i),
                               v_status_arr(i), SYSDATE
                           );
            EXCEPTION
                WHEN OTHERS THEN
                    IF SQLCODE = -24381 THEN
                        v_error_count := v_error_count + SQL%BULK_EXCEPTIONS.COUNT;
                        handle_forall_exceptions(v_step, SQL%BULK_EXCEPTIONS.COUNT);
                    ELSE
                        RAISE;
                    END IF;
            END;

            v_total_rows := v_total_rows + v_buffer.COUNT;
            COMMIT;
        END LOOP;

        CLOSE v_cur;

        g_step_start := SYSTIMESTAMP;

        INSERT /*+ PARALLEL(8) */
          INTO DWH.FACT_IC_ELIM_SUMMARY (
                   fiscal_year, fiscal_period,
                   total_eliminations, total_debit, total_credit,
                   total_mismatch, mismatch_count,
                   created_date
               )
        SELECT p_fiscal_year,
               p_fiscal_period,
               COUNT(*),
               SUM(fie.BASE_DEBIT),
               SUM(fie.BASE_CREDIT),
               SUM(fie.MISMATCH_AMOUNT),
               SUM(CASE WHEN fie.STATUS = 'MISMATCH' THEN 1 ELSE 0 END),
               SYSDATE
          FROM DWH.FACT_IC_ELIMINATIONS fie
         WHERE fie.FISCAL_YEAR   = p_fiscal_year
           AND fie.FISCAL_PERIOD = p_fiscal_period;

        COMMIT;
        log_step(v_step || '.ELIM_SUMMARY', 'DONE', SQL%ROWCOUNT);

        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_IC_ENTITY_PAIR_ANALYSIS tgt
        USING (
            WITH cte_elim_base AS (
                SELECT fie.ENTITY_DR,
                       fie.ENTITY_CR,
                       fie.ACCOUNT_ID,
                       fie.ACCOUNT_CODE,
                       fie.FISCAL_YEAR,
                       fie.FISCAL_PERIOD,
                       fie.DEBIT_AMOUNT,
                       fie.CREDIT_AMOUNT,
                       fie.BASE_DEBIT,
                       fie.BASE_CREDIT,
                       fie.MISMATCH_AMOUNT,
                       fie.STATUS,
                       ROW_NUMBER() OVER (
                           PARTITION BY fie.ENTITY_DR, fie.ENTITY_CR,
                                        fie.ACCOUNT_ID, fie.FISCAL_YEAR, fie.FISCAL_PERIOD
                           ORDER BY fie.ELIM_SK DESC
                       ) AS ep_rn
                  FROM DWH.FACT_IC_ELIMINATIONS fie
                 WHERE fie.FISCAL_YEAR   = p_fiscal_year
                   AND fie.FISCAL_PERIOD = p_fiscal_period
            ),
            cte_elim_dedup AS (
                SELECT eb.*
                  FROM cte_elim_base eb
                 WHERE eb.ep_rn = 1
            ),
            cte_cc_names AS (
                SELECT cc.COST_CENTER_ID,
                       cc.CC_CODE,
                       cc.CC_NAME
                  FROM FIN.COST_CENTERS cc
            ),
            cte_prior_period AS (
                SELECT fie2.ENTITY_DR,
                       fie2.ENTITY_CR,
                       SUM(fie2.BASE_DEBIT)      AS pp_debit,
                       SUM(fie2.BASE_CREDIT)     AS pp_credit,
                       SUM(fie2.MISMATCH_AMOUNT) AS pp_mismatch
                  FROM DWH.FACT_IC_ELIMINATIONS fie2
                 WHERE fie2.FISCAL_YEAR = p_fiscal_year
                   AND fie2.FISCAL_PERIOD = p_fiscal_period - 1
                 GROUP BY fie2.ENTITY_DR, fie2.ENTITY_CR
            ),
            cte_pair_agg AS (
                SELECT ed.ENTITY_DR,
                       ed.ENTITY_CR,
                       NVL(cc_dr.CC_NAME, 'ENTITY_' || ed.ENTITY_DR) AS entity_dr_name,
                       NVL(cc_cr.CC_NAME, 'ENTITY_' || ed.ENTITY_CR) AS entity_cr_name,
                       ed.FISCAL_YEAR,
                       ed.FISCAL_PERIOD,
                       COUNT(DISTINCT ed.ACCOUNT_ID)   AS account_count,
                       SUM(ed.BASE_DEBIT)              AS total_debit,
                       SUM(ed.BASE_CREDIT)             AS total_credit,
                       SUM(ed.MISMATCH_AMOUNT)         AS total_mismatch,
                       COUNT(CASE WHEN ed.STATUS = 'MISMATCH' THEN 1 END)
                                                        AS mismatch_count,
                       COUNT(CASE WHEN ed.STATUS = 'BALANCED' THEN 1 END)
                                                        AS balanced_count,
                       NVL(pp.pp_debit, 0)             AS prior_debit,
                       NVL(pp.pp_credit, 0)            AS prior_credit,
                       NVL(pp.pp_mismatch, 0)          AS prior_mismatch,
                       SUM(ed.BASE_DEBIT) - NVL(pp.pp_debit, 0)
                                                        AS debit_movement,
                       SUM(ed.MISMATCH_AMOUNT) - NVL(pp.pp_mismatch, 0)
                                                        AS mismatch_movement,
                       RANK() OVER (
                           ORDER BY SUM(ed.BASE_DEBIT) + SUM(ed.BASE_CREDIT) DESC
                       ) AS pair_volume_rank,
                       RANK() OVER (
                           ORDER BY SUM(ed.MISMATCH_AMOUNT) DESC
                       ) AS pair_mismatch_rank
                  FROM cte_elim_dedup ed
                  LEFT JOIN cte_cc_names cc_dr
                    ON cc_dr.COST_CENTER_ID = ed.ENTITY_DR
                  LEFT JOIN cte_cc_names cc_cr
                    ON cc_cr.COST_CENTER_ID = ed.ENTITY_CR
                  LEFT JOIN cte_prior_period pp
                    ON pp.ENTITY_DR = ed.ENTITY_DR
                   AND pp.ENTITY_CR = ed.ENTITY_CR
                 GROUP BY ed.ENTITY_DR, ed.ENTITY_CR,
                          cc_dr.CC_NAME, cc_cr.CC_NAME,
                          ed.FISCAL_YEAR, ed.FISCAL_PERIOD,
                          pp.pp_debit, pp.pp_credit, pp.pp_mismatch
            )
            SELECT pa.ENTITY_DR, pa.ENTITY_CR,
                   pa.entity_dr_name, pa.entity_cr_name,
                   pa.FISCAL_YEAR, pa.FISCAL_PERIOD,
                   pa.account_count, pa.total_debit, pa.total_credit,
                   pa.total_mismatch, pa.mismatch_count, pa.balanced_count,
                   pa.prior_debit, pa.prior_credit, pa.prior_mismatch,
                   pa.debit_movement, pa.mismatch_movement,
                   pa.pair_volume_rank, pa.pair_mismatch_rank
              FROM cte_pair_agg pa
        ) src
        ON (    tgt.ENTITY_DR     = src.ENTITY_DR
            AND tgt.ENTITY_CR     = src.ENTITY_CR
            AND tgt.FISCAL_YEAR   = src.FISCAL_YEAR
            AND tgt.FISCAL_PERIOD = src.FISCAL_PERIOD)
        WHEN MATCHED THEN
            UPDATE SET
                tgt.ACCOUNT_COUNT     = src.account_count,
                tgt.TOTAL_DEBIT       = src.total_debit,
                tgt.TOTAL_CREDIT      = src.total_credit,
                tgt.TOTAL_MISMATCH    = src.total_mismatch,
                tgt.MISMATCH_COUNT    = src.mismatch_count,
                tgt.BALANCED_COUNT    = src.balanced_count,
                tgt.DEBIT_MOVEMENT    = src.debit_movement,
                tgt.MISMATCH_MOVEMENT = src.mismatch_movement,
                tgt.PAIR_VOLUME_RANK  = src.pair_volume_rank,
                tgt.PAIR_MISMATCH_RANK = src.pair_mismatch_rank,
                tgt.UPDATED_DATE      = SYSDATE
             WHERE tgt.TOTAL_DEBIT    <> src.total_debit
                OR tgt.TOTAL_MISMATCH <> src.total_mismatch
        WHEN NOT MATCHED THEN
            INSERT (
                ENTITY_DR, ENTITY_CR,
                ENTITY_DR_NAME, ENTITY_CR_NAME,
                FISCAL_YEAR, FISCAL_PERIOD,
                ACCOUNT_COUNT, TOTAL_DEBIT, TOTAL_CREDIT,
                TOTAL_MISMATCH, MISMATCH_COUNT, BALANCED_COUNT,
                PRIOR_DEBIT, PRIOR_CREDIT, PRIOR_MISMATCH,
                DEBIT_MOVEMENT, MISMATCH_MOVEMENT,
                PAIR_VOLUME_RANK, PAIR_MISMATCH_RANK,
                CREATED_DATE
            )
            VALUES (
                src.ENTITY_DR, src.ENTITY_CR,
                src.entity_dr_name, src.entity_cr_name,
                src.FISCAL_YEAR, src.FISCAL_PERIOD,
                src.account_count, src.total_debit, src.total_credit,
                src.total_mismatch, src.mismatch_count, src.balanced_count,
                src.prior_debit, src.prior_credit, src.prior_mismatch,
                src.debit_movement, src.mismatch_movement,
                src.pair_volume_rank, src.pair_mismatch_rank,
                SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.ENTITY_PAIR_ANALYSIS', 'DONE', g_row_count);

        g_step_start := SYSTIMESTAMP;

        INSERT /*+ PARALLEL(8) */
          INTO DWH.FACT_IC_MISMATCH_DETAIL (
                   fiscal_year, fiscal_period,
                   entity_dr, entity_cr,
                   account_id, account_code,
                   debit_amount, credit_amount,
                   mismatch_amount, mismatch_pct,
                   currency_code, exchange_rate,
                   severity, recommended_action,
                   created_date
               )
        WITH cte_mismatches AS (
            SELECT fie.ENTITY_DR,
                   fie.ENTITY_CR,
                   fie.ACCOUNT_ID,
                   fie.ACCOUNT_CODE,
                   fie.DEBIT_AMOUNT,
                   fie.CREDIT_AMOUNT,
                   fie.BASE_DEBIT,
                   fie.BASE_CREDIT,
                   fie.MISMATCH_AMOUNT,
                   fie.CURRENCY_CODE,
                   fie.EXCHANGE_RATE,
                   ROW_NUMBER() OVER (
                       PARTITION BY fie.ENTITY_DR, fie.ENTITY_CR, fie.ACCOUNT_ID
                       ORDER BY fie.ELIM_SK DESC
                   ) AS mm_rn
              FROM DWH.FACT_IC_ELIMINATIONS fie
             WHERE fie.FISCAL_YEAR   = p_fiscal_year
               AND fie.FISCAL_PERIOD = p_fiscal_period
               AND fie.STATUS IN ('MISMATCH', 'MINOR_DIFF')
        ),
        cte_detail AS (
            SELECT m.*,
                   CASE
                       WHEN m.MISMATCH_AMOUNT > 10000 THEN 'CRITICAL'
                       WHEN m.MISMATCH_AMOUNT > 1000  THEN 'HIGH'
                       WHEN m.MISMATCH_AMOUNT > 100   THEN 'MEDIUM'
                       ELSE 'LOW'
                   END AS severity,
                   CASE
                       WHEN ABS(m.DEBIT_AMOUNT - m.CREDIT_AMOUNT) < 0.01
                        AND m.MISMATCH_AMOUNT > 0
                       THEN 'FX_RATE_DIFFERENCE'
                       WHEN m.DEBIT_AMOUNT > 0 AND m.CREDIT_AMOUNT = 0
                       THEN 'MISSING_CREDIT_ENTRY'
                       WHEN m.CREDIT_AMOUNT > 0 AND m.DEBIT_AMOUNT = 0
                       THEN 'MISSING_DEBIT_ENTRY'
                       ELSE 'AMOUNT_DIFFERENCE'
                   END AS recommended_action,
                   CASE
                       WHEN m.BASE_DEBIT <> 0
                       THEN ROUND(m.MISMATCH_AMOUNT / m.BASE_DEBIT * 100, 4)
                       ELSE 0
                   END AS mismatch_pct
              FROM cte_mismatches m
             WHERE m.mm_rn = 1
        )
        SELECT p_fiscal_year, p_fiscal_period,
               d.ENTITY_DR, d.ENTITY_CR,
               d.ACCOUNT_ID, d.ACCOUNT_CODE,
               d.DEBIT_AMOUNT, d.CREDIT_AMOUNT,
               d.MISMATCH_AMOUNT, d.mismatch_pct,
               d.CURRENCY_CODE, d.EXCHANGE_RATE,
               d.severity, d.recommended_action,
               SYSDATE
          FROM cte_detail d
         ORDER BY d.MISMATCH_AMOUNT DESC;

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step || '.MISMATCH_DETAIL', 'DONE', g_row_count);

        log_step(v_step || '.COMPLETE', 'SUCCESS', v_total_rows);

    EXCEPTION
        WHEN OTHERS THEN
            IF v_cur%ISOPEN THEN CLOSE v_cur; END IF;
            ROLLBACK;
            log_step(v_step || '.FATAL', 'ERROR', 0, SQLERRM);
            RAISE;
    END calc_intercompany_elim;

    -- =========================================================================
    -- 8. PIPE_FACT_INVOICES
    -- =========================================================================
    FUNCTION pipe_fact_invoices (
        p_from_date IN DATE,
        p_to_date   IN DATE
    ) RETURN DWH.T_INVOICE_FACT_TAB PIPELINED PARALLEL_ENABLE IS
        v_func_curr VARCHAR2(3);
    BEGIN
        BEGIN
            SELECT c.CURRENCY_CODE INTO v_func_curr
              FROM FIN.CURRENCIES c
             WHERE c.IS_FUNCTIONAL = 'Y' AND ROWNUM = 1;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN v_func_curr := 'USD';
        END;

        FOR r IN (
            WITH cte_invoices AS (
                SELECT /*+ PARALLEL(inv, 8) */
                       inv.INVOICE_ID,
                       inv.INVOICE_NUM,
                       inv.INVOICE_TYPE,
                       inv.INVOICE_DATE,
                       inv.DUE_DATE,
                       inv.FISCAL_PERIOD_ID,
                       inv.CUSTOMER_ID,
                       inv.CURRENCY_ID,
                       inv.EXCHANGE_RATE,
                       inv.SUBTOTAL_AMOUNT,
                       inv.TAX_AMOUNT       AS inv_tax_amount,
                       inv.TOTAL_AMOUNT     AS inv_total,
                       inv.PAID_AMOUNT,
                       inv.STATUS           AS inv_status,
                       ROW_NUMBER() OVER (
                           PARTITION BY inv.INVOICE_ID
                           ORDER BY inv.INVOICE_DATE
                       ) AS inv_rn
                  FROM FIN.INVOICES inv
                 WHERE inv.INVOICE_DATE BETWEEN p_from_date AND p_to_date
                   AND inv.STATUS IN ('APPROVED', 'POSTED', 'PARTIALLY_PAID', 'PAID')
            ),
            cte_first_line AS (
                SELECT il.INVOICE_ID,
                       il.ACCOUNT_ID,
                       il.COST_CENTER_ID,
                       il.LINE_AMOUNT,
                       il.TAX_AMOUNT   AS line_tax,
                       il.TAX_CODE_ID,
                       ROW_NUMBER() OVER (
                           PARTITION BY il.INVOICE_ID
                           ORDER BY il.LINE_NUM
                       ) AS line_rn
                  FROM FIN.INVOICE_LINES il
                 WHERE il.INVOICE_ID IN (
                           SELECT ci.INVOICE_ID FROM cte_invoices ci
                       )
            ),
            cte_customer AS (
                SELECT cust.CUSTOMER_ID,
                       cust.CUSTOMER_NAME,
                       cust.CREDIT_LIMIT,
                       cust.CURRENCY_CODE AS cust_currency,
                       (SELECT seg.SEGMENT_NAME
                          FROM CRM.CUSTOMER_SEGMENTS seg
                         WHERE seg.SEGMENT_ID = cust.SEGMENT_ID) AS segment_name
                  FROM CRM.CUSTOMERS cust
            ),
            cte_fiscal AS (
                SELECT fp.FISCAL_PERIOD_ID,
                       fp.FISCAL_YEAR,
                       fp.PERIOD_NUMBER
                  FROM FIN.FISCAL_PERIODS fp
            ),
            cte_fx AS (
                SELECT cur.CURRENCY_ID,
                       cur.CURRENCY_CODE
                  FROM FIN.CURRENCIES cur
            ),
            cte_payments AS (
                SELECT pa.INVOICE_ID,
                       SUM(pa.ALLOCATED_AMOUNT) AS total_paid,
                       COUNT(*)                 AS pay_count
                  FROM FIN.PAYMENT_ALLOCATIONS pa
                 WHERE pa.STATUS = 'APPLIED'
                 GROUP BY pa.INVOICE_ID
            ),
            cte_credit_notes AS (
                SELECT cn.INVOICE_ID       AS orig_id,
                       SUM(cn.TOTAL_AMOUNT) AS cn_total
                  FROM FIN.INVOICES cn
                 WHERE cn.INVOICE_TYPE = 'CREDIT_NOTE'
                   AND cn.STATUS IN ('APPROVED', 'POSTED')
                 GROUP BY cn.INVOICE_ID
            ),
            cte_tax_info AS (
                SELECT tc.TAX_CODE_ID,
                       tc.TAX_RATE
                  FROM FIN.TAX_CODES tc
            ),
            cte_aging AS (
                SELECT ci2.INVOICE_ID,
                       GREATEST(TRUNC(SYSDATE) - ci2.DUE_DATE, 0) AS days_over,
                       CASE
                           WHEN TRUNC(SYSDATE) <= ci2.DUE_DATE         THEN 'CURRENT'
                           WHEN TRUNC(SYSDATE) - ci2.DUE_DATE <= 30    THEN '1-30'
                           WHEN TRUNC(SYSDATE) - ci2.DUE_DATE <= 60    THEN '31-60'
                           WHEN TRUNC(SYSDATE) - ci2.DUE_DATE <= 90    THEN '61-90'
                           WHEN TRUNC(SYSDATE) - ci2.DUE_DATE <= 120   THEN '91-120'
                           ELSE '120+'
                       END AS aging_bucket
                  FROM cte_invoices ci2
            ),
            cte_running AS (
                SELECT ci3.INVOICE_ID,
                       ci3.CUSTOMER_ID,
                       SUM(ci3.inv_total) OVER (
                           PARTITION BY ci3.CUSTOMER_ID
                           ORDER BY ci3.INVOICE_DATE, ci3.INVOICE_ID
                           ROWS UNBOUNDED PRECEDING
                       ) AS running_total
                  FROM cte_invoices ci3
                 WHERE ci3.inv_rn = 1
            ),
            cte_payment_terms AS (
                SELECT pt.PAYMENT_TERM_ID,
                       pt.TERM_CODE,
                       pt.TERM_NAME,
                       pt.NET_DAYS,
                       pt.DISCOUNT_DAYS,
                       pt.DISCOUNT_PCT
                  FROM FIN.PAYMENT_TERMS pt
            ),
            cte_line_aggregation AS (
                SELECT il2.INVOICE_ID,
                       COUNT(il2.INVOICE_LINE_ID)    AS total_lines,
                       SUM(il2.LINE_AMOUNT)          AS sum_line_amount,
                       SUM(il2.TAX_AMOUNT)           AS sum_line_tax,
                       AVG(il2.UNIT_PRICE)           AS avg_unit_price,
                       MAX(il2.LINE_AMOUNT)          AS max_line_amount,
                       MIN(il2.LINE_AMOUNT)          AS min_line_amount,
                       SUM(il2.QUANTITY)             AS total_quantity,
                       COUNT(DISTINCT il2.ACCOUNT_ID) AS distinct_accounts,
                       COUNT(DISTINCT il2.COST_CENTER_ID) AS distinct_cost_centers
                  FROM FIN.INVOICE_LINES il2
                 WHERE il2.INVOICE_ID IN (
                           SELECT ci4.INVOICE_ID FROM cte_invoices ci4
                       )
                 GROUP BY il2.INVOICE_ID
            ),
            cte_journal_link AS (
                SELECT j.SOURCE_ID AS inv_source_id,
                       j.JOURNAL_NUM,
                       j.JOURNAL_TYPE,
                       j.JOURNAL_DATE,
                       j.TOTAL_DEBIT AS journal_debit,
                       j.TOTAL_CREDIT AS journal_credit,
                       ROW_NUMBER() OVER (
                           PARTITION BY j.SOURCE_ID
                           ORDER BY j.JOURNAL_DATE DESC
                       ) AS jl_rn
                  FROM FIN.JOURNALS j
                 WHERE j.SOURCE_TYPE = 'INVOICE'
                   AND j.STATUS = 'POSTED'
                   AND j.SOURCE_ID IN (
                           SELECT ci5.INVOICE_ID FROM cte_invoices ci5
                       )
            )
            SELECT ci.INVOICE_ID,
                   ci.INVOICE_NUM,
                   ci.INVOICE_TYPE,
                   ci.INVOICE_DATE,
                   ci.DUE_DATE,
                   ci.CUSTOMER_ID,
                   fl.ACCOUNT_ID,
                   fl.COST_CENTER_ID,
                   NVL(fl.LINE_AMOUNT, 0)                  AS line_amount,
                   NVL(fl.line_tax, ci.inv_tax_amount)     AS tax_amount,
                   ci.inv_total                             AS total_amount,
                   NVL(ci.PAID_AMOUNT, 0)                  AS paid_amount,
                   ci.inv_total
                       - NVL(ci.PAID_AMOUNT, 0)
                       - NVL(cn.cn_total, 0)               AS outstanding_amount,
                   NVL(fx.CURRENCY_CODE, v_func_curr)      AS currency_code,
                   NVL(ci.EXCHANGE_RATE, 1)                AS exchange_rate,
                   ROUND(ci.inv_total
                       * NVL(ci.EXCHANGE_RATE, 1), 4)      AS base_amount,
                   NVL(fis.FISCAL_YEAR,
                       EXTRACT(YEAR FROM ci.INVOICE_DATE))  AS fiscal_year,
                   NVL(fis.PERIOD_NUMBER,
                       EXTRACT(MONTH FROM ci.INVOICE_DATE)) AS fiscal_period,
                   NVL(ag.days_over, 0)                    AS days_overdue,
                   NVL(ag.aging_bucket, 'CURRENT')         AS aging_bucket,
                   ci.inv_status                            AS status
              FROM cte_invoices ci
              LEFT JOIN cte_first_line fl
                ON fl.INVOICE_ID = ci.INVOICE_ID AND fl.line_rn = 1
              LEFT JOIN cte_fiscal fis
                ON fis.FISCAL_PERIOD_ID = ci.FISCAL_PERIOD_ID
              LEFT JOIN cte_fx fx
                ON fx.CURRENCY_ID = ci.CURRENCY_ID
              LEFT JOIN cte_payments pm
                ON pm.INVOICE_ID = ci.INVOICE_ID
              LEFT JOIN cte_credit_notes cn
                ON cn.orig_id = ci.INVOICE_ID
              LEFT JOIN cte_aging ag
                ON ag.INVOICE_ID = ci.INVOICE_ID
              LEFT JOIN cte_running rn
                ON rn.INVOICE_ID = ci.INVOICE_ID
             WHERE ci.inv_rn = 1
        ) LOOP
            PIPE ROW (
                DWH.T_INVOICE_FACT_REC(
                    r.INVOICE_ID,
                    r.INVOICE_NUM,
                    r.INVOICE_TYPE,
                    r.INVOICE_DATE,
                    r.DUE_DATE,
                    r.CUSTOMER_ID,
                    r.ACCOUNT_ID,
                    r.COST_CENTER_ID,
                    r.line_amount,
                    r.tax_amount,
                    r.total_amount,
                    r.paid_amount,
                    r.outstanding_amount,
                    r.currency_code,
                    r.exchange_rate,
                    r.base_amount,
                    r.fiscal_year,
                    r.fiscal_period,
                    r.days_overdue,
                    r.aging_bucket,
                    r.status
                )
            );
        END LOOP;

        RETURN;
    END pipe_fact_invoices;

    -- =========================================================================
    -- 9. PIPE_FACT_JOURNALS
    -- =========================================================================
    FUNCTION pipe_fact_journals (
        p_from_date IN DATE,
        p_to_date   IN DATE
    ) RETURN DWH.T_JOURNAL_TAB PIPELINED PARALLEL_ENABLE IS
        v_func_curr VARCHAR2(3);
    BEGIN
        BEGIN
            SELECT c.CURRENCY_CODE INTO v_func_curr
              FROM FIN.CURRENCIES c
             WHERE c.IS_FUNCTIONAL = 'Y' AND ROWNUM = 1;
        EXCEPTION
            WHEN NO_DATA_FOUND THEN v_func_curr := 'USD';
        END;

        FOR r IN (
            WITH cte_journals AS (
                SELECT /*+ PARALLEL(j, 8) */
                       j.JOURNAL_ID,
                       j.JOURNAL_NUM,
                       j.JOURNAL_TYPE,
                       j.JOURNAL_DATE,
                       j.FISCAL_PERIOD_ID,
                       j.STATUS          AS journal_status,
                       j.SOURCE_TYPE,
                       j.SOURCE_ID,
                       j.CURRENCY_ID
                  FROM FIN.JOURNALS j
                 WHERE j.JOURNAL_DATE BETWEEN p_from_date AND p_to_date
                   AND j.STATUS IN ('POSTED', 'APPROVED')
            ),
            cte_journal_lines AS (
                SELECT jl.JOURNAL_LINE_ID,
                       jl.JOURNAL_ID,
                       jl.LINE_NUM,
                       jl.ACCOUNT_ID,
                       jl.COST_CENTER_ID,
                       jl.DEBIT_AMOUNT,
                       jl.CREDIT_AMOUNT,
                       jl.CURRENCY_ID   AS line_currency_id,
                       jl.EXCHANGE_RATE AS line_exchange_rate,
                       jl.BASE_DEBIT,
                       jl.BASE_CREDIT,
                       ROW_NUMBER() OVER (
                           PARTITION BY jl.JOURNAL_LINE_ID
                           ORDER BY jl.JOURNAL_ID
                       ) AS line_rn
                  FROM FIN.JOURNAL_LINES jl
                 WHERE jl.JOURNAL_ID IN (
                           SELECT cj.JOURNAL_ID FROM cte_journals cj
                       )
            ),
            cte_accounts AS (
                SELECT a.ACCOUNT_ID,
                       a.ACCOUNT_CODE,
                       a.ACCOUNT_NAME,
                       a.ACCOUNT_TYPE,
                       a.NORMAL_BALANCE,
                       a.IS_POSTING,
                       SYS_CONNECT_BY_PATH(a.ACCOUNT_CODE, '/') AS acct_path,
                       LEVEL AS acct_depth
                  FROM FIN.ACCOUNTS a
                 WHERE a.IS_ACTIVE = 'Y'
                 START WITH a.PARENT_ACCOUNT_ID IS NULL
               CONNECT BY PRIOR a.ACCOUNT_ID = a.PARENT_ACCOUNT_ID
            ),
            cte_cost_centers AS (
                SELECT cc.COST_CENTER_ID,
                       cc.CC_CODE,
                       cc.CC_NAME,
                       cc.CC_TYPE,
                       cc.BUDGET_HOLDER,
                       SYS_CONNECT_BY_PATH(cc.CC_CODE, '/') AS cc_path,
                       LEVEL AS cc_depth
                  FROM FIN.COST_CENTERS cc
                 START WITH cc.PARENT_CC_ID IS NULL
               CONNECT BY PRIOR cc.COST_CENTER_ID = cc.PARENT_CC_ID
            ),
            cte_fiscal AS (
                SELECT fp.FISCAL_PERIOD_ID,
                       fp.FISCAL_YEAR,
                       fp.PERIOD_NUMBER,
                       fp.STATUS AS period_status
                  FROM FIN.FISCAL_PERIODS fp
            ),
            cte_fx AS (
                SELECT cur.CURRENCY_ID,
                       cur.CURRENCY_CODE
                  FROM FIN.CURRENCIES cur
            ),
            cte_intercompany AS (
                SELECT jl2.JOURNAL_ID,
                       CASE
                           WHEN COUNT(DISTINCT jl2.COST_CENTER_ID) > 1 THEN 'Y'
                           ELSE 'N'
                       END AS is_ic
                  FROM FIN.JOURNAL_LINES jl2
                 WHERE jl2.JOURNAL_ID IN (
                           SELECT cj2.JOURNAL_ID FROM cte_journals cj2
                       )
                 GROUP BY jl2.JOURNAL_ID
            ),
            cte_budget AS (
                SELECT bl.ACCOUNT_ID,
                       bl.COST_CENTER_ID,
                       bl.FISCAL_PERIOD_ID,
                       NVL(bl.REVISED_AMOUNT, bl.PLANNED_AMOUNT) AS budget_amt
                  FROM FIN.BUDGET_LINES bl
                  JOIN FIN.BUDGETS b
                    ON b.BUDGET_ID = bl.BUDGET_ID
                 WHERE b.STATUS = 'APPROVED'
            ),
            cte_reversal AS (
                SELECT j2.JOURNAL_ID,
                       j2.REVERSING_JOURNAL_ID,
                       (SELECT rj.JOURNAL_NUM
                          FROM FIN.JOURNALS rj
                         WHERE rj.JOURNAL_ID = j2.REVERSING_JOURNAL_ID) AS rev_num
                  FROM cte_journals j2
                 WHERE j2.REVERSING_JOURNAL_ID IS NOT NULL
            ),
            cte_analytics AS (
                SELECT jl3.JOURNAL_LINE_ID,
                       SUM(NVL(jl3.BASE_DEBIT, 0)) OVER (
                           PARTITION BY jl3.ACCOUNT_ID
                           ORDER BY j3.JOURNAL_DATE, jl3.JOURNAL_ID
                           ROWS UNBOUNDED PRECEDING
                       ) AS running_debit,
                       SUM(NVL(jl3.BASE_CREDIT, 0)) OVER (
                           PARTITION BY jl3.ACCOUNT_ID
                           ORDER BY j3.JOURNAL_DATE, jl3.JOURNAL_ID
                           ROWS UNBOUNDED PRECEDING
                       ) AS running_credit,
                       RANK() OVER (
                           PARTITION BY j3.FISCAL_PERIOD_ID
                           ORDER BY NVL(jl3.BASE_DEBIT, 0) + NVL(jl3.BASE_CREDIT, 0) DESC
                       ) AS amount_rank
                  FROM FIN.JOURNAL_LINES jl3
                  JOIN FIN.JOURNALS j3
                    ON j3.JOURNAL_ID = jl3.JOURNAL_ID
                 WHERE j3.JOURNAL_DATE BETWEEN p_from_date AND p_to_date
                   AND j3.STATUS IN ('POSTED', 'APPROVED')
            )
            SELECT jl.JOURNAL_LINE_ID,
                   j.JOURNAL_ID,
                   j.JOURNAL_NUM,
                   j.JOURNAL_TYPE,
                   j.JOURNAL_DATE,
                   jl.ACCOUNT_ID,
                   NVL(act.ACCOUNT_CODE, 'UNK')   AS account_code,
                   jl.COST_CENTER_ID,
                   NVL(cctr.CC_CODE, 'UNK')        AS cc_code,
                   NVL(jl.DEBIT_AMOUNT, 0)         AS debit_amount,
                   NVL(jl.CREDIT_AMOUNT, 0)        AS credit_amount,
                   NVL(jl.BASE_DEBIT, 0)           AS base_debit,
                   NVL(jl.BASE_CREDIT, 0)          AS base_credit,
                   NVL(fx.CURRENCY_CODE, v_func_curr) AS currency_code,
                   NVL(jl.line_exchange_rate, 1)    AS exchange_rate,
                   NVL(fis.FISCAL_YEAR,
                       EXTRACT(YEAR FROM j.JOURNAL_DATE))   AS fiscal_year,
                   NVL(fis.PERIOD_NUMBER,
                       EXTRACT(MONTH FROM j.JOURNAL_DATE))  AS fiscal_period,
                   j.SOURCE_TYPE,
                   j.SOURCE_ID,
                   j.journal_status                 AS status
              FROM cte_journals j
              JOIN cte_journal_lines jl
                ON jl.JOURNAL_ID = j.JOURNAL_ID
               AND jl.line_rn = 1
              LEFT JOIN cte_accounts act
                ON act.ACCOUNT_ID = jl.ACCOUNT_ID
              LEFT JOIN cte_cost_centers cctr
                ON cctr.COST_CENTER_ID = jl.COST_CENTER_ID
              LEFT JOIN cte_fiscal fis
                ON fis.FISCAL_PERIOD_ID = j.FISCAL_PERIOD_ID
              LEFT JOIN cte_fx fx
                ON fx.CURRENCY_ID = jl.line_currency_id
              LEFT JOIN cte_intercompany ic
                ON ic.JOURNAL_ID = j.JOURNAL_ID
              LEFT JOIN cte_analytics an
                ON an.JOURNAL_LINE_ID = jl.JOURNAL_LINE_ID
        ) LOOP
            PIPE ROW (
                DWH.T_JOURNAL_REC(
                    r.JOURNAL_LINE_ID,
                    r.JOURNAL_ID,
                    r.JOURNAL_NUM,
                    r.JOURNAL_TYPE,
                    r.JOURNAL_DATE,
                    r.ACCOUNT_ID,
                    r.account_code,
                    r.COST_CENTER_ID,
                    r.cc_code,
                    r.debit_amount,
                    r.credit_amount,
                    r.base_debit,
                    r.base_credit,
                    r.currency_code,
                    r.exchange_rate,
                    r.fiscal_year,
                    r.fiscal_period,
                    r.SOURCE_TYPE,
                    r.SOURCE_ID,
                    r.status
                )
            );
        END LOOP;

        RETURN;
    END pipe_fact_journals;

    -- =========================================================================
    -- Private: reconcile_invoice_totals
    -- =========================================================================
    PROCEDURE reconcile_invoice_totals (
        p_fiscal_year   IN NUMBER,
        p_fiscal_period IN NUMBER
    ) IS
        v_step VARCHAR2(200) := 'RECONCILE_INVOICE_TOTALS';
    BEGIN
        g_step_start := SYSTIMESTAMP;

        INSERT /*+ PARALLEL(8) */
          INTO DWH.FACT_RECON_INVOICE_TOTALS (
                   fiscal_year, fiscal_period,
                   source_system, recon_type,
                   source_total, dwh_total,
                   difference, difference_pct,
                   match_status, detail_text,
                   created_date
               )
        WITH cte_source_totals AS (
            SELECT fp.FISCAL_YEAR,
                   fp.PERIOD_NUMBER AS fiscal_period,
                   SUM(inv.TOTAL_AMOUNT) AS src_total_amount,
                   SUM(inv.TAX_AMOUNT)   AS src_tax_amount,
                   SUM(inv.PAID_AMOUNT)  AS src_paid_amount,
                   COUNT(DISTINCT inv.INVOICE_ID) AS src_invoice_count,
                   SUM(inv.SUBTOTAL_AMOUNT) AS src_subtotal
              FROM FIN.INVOICES inv
              JOIN FIN.FISCAL_PERIODS fp
                ON fp.FISCAL_PERIOD_ID = inv.FISCAL_PERIOD_ID
             WHERE fp.FISCAL_YEAR   = p_fiscal_year
               AND fp.PERIOD_NUMBER = p_fiscal_period
               AND inv.STATUS NOT IN ('DRAFT', 'CANCELLED')
             GROUP BY fp.FISCAL_YEAR, fp.PERIOD_NUMBER
        ),
        cte_dwh_totals AS (
            SELECT fi.FISCAL_YEAR,
                   fi.FISCAL_PERIOD,
                   SUM(fi.TOTAL_AMOUNT) AS dwh_total_amount,
                   SUM(fi.TAX_AMOUNT)   AS dwh_tax_amount,
                   SUM(fi.PAID_AMOUNT)  AS dwh_paid_amount,
                   COUNT(DISTINCT fi.INVOICE_ID) AS dwh_invoice_count,
                   SUM(fi.LINE_AMOUNT)  AS dwh_line_total,
                   ROW_NUMBER() OVER (
                       PARTITION BY fi.FISCAL_YEAR, fi.FISCAL_PERIOD
                       ORDER BY SUM(fi.TOTAL_AMOUNT) DESC
                   ) AS rn
              FROM DWH.FACT_INVOICES fi
             WHERE fi.FISCAL_YEAR   = p_fiscal_year
               AND fi.FISCAL_PERIOD = p_fiscal_period
               AND fi.STATUS NOT IN ('CANCELLED', 'VOID')
             GROUP BY fi.FISCAL_YEAR, fi.FISCAL_PERIOD
        ),
        cte_amount_recon AS (
            SELECT st.FISCAL_YEAR,
                   st.fiscal_period,
                   'FIN_INVOICES' AS source_system,
                   'TOTAL_AMOUNT' AS recon_type,
                   st.src_total_amount AS source_total,
                   NVL(dt.dwh_total_amount, 0) AS dwh_total,
                   st.src_total_amount - NVL(dt.dwh_total_amount, 0) AS difference,
                   CASE
                       WHEN st.src_total_amount <> 0
                       THEN ROUND(
                           (st.src_total_amount - NVL(dt.dwh_total_amount, 0))
                           / ABS(st.src_total_amount) * 100, 4
                       )
                       ELSE 0
                   END AS difference_pct,
                   CASE
                       WHEN ABS(st.src_total_amount - NVL(dt.dwh_total_amount, 0)) < 0.01
                       THEN 'MATCHED'
                       WHEN ABS(st.src_total_amount - NVL(dt.dwh_total_amount, 0)) < 1.00
                       THEN 'MINOR_DIFF'
                       ELSE 'MISMATCH'
                   END AS match_status,
                   'Source=' || TO_CHAR(st.src_total_amount, 'FM999,999,999,990.00')
                       || ' DWH=' || TO_CHAR(NVL(dt.dwh_total_amount, 0), 'FM999,999,999,990.00')
                   AS detail_text
              FROM cte_source_totals st
              LEFT JOIN cte_dwh_totals dt
                ON dt.FISCAL_YEAR   = st.FISCAL_YEAR
               AND dt.fiscal_period = st.fiscal_period
               AND dt.rn = 1
        ),
        cte_count_recon AS (
            SELECT st.FISCAL_YEAR,
                   st.fiscal_period,
                   'FIN_INVOICES' AS source_system,
                   'INVOICE_COUNT' AS recon_type,
                   st.src_invoice_count AS source_total,
                   NVL(dt.dwh_invoice_count, 0) AS dwh_total,
                   st.src_invoice_count - NVL(dt.dwh_invoice_count, 0) AS difference,
                   CASE
                       WHEN st.src_invoice_count <> 0
                       THEN ROUND(
                           (st.src_invoice_count - NVL(dt.dwh_invoice_count, 0))
                           / st.src_invoice_count * 100, 4
                       )
                       ELSE 0
                   END AS difference_pct,
                   CASE
                       WHEN st.src_invoice_count = NVL(dt.dwh_invoice_count, 0) THEN 'MATCHED'
                       ELSE 'MISMATCH'
                   END AS match_status,
                   'Source count=' || st.src_invoice_count
                       || ' DWH count=' || NVL(dt.dwh_invoice_count, 0)
                   AS detail_text
              FROM cte_source_totals st
              LEFT JOIN cte_dwh_totals dt
                ON dt.FISCAL_YEAR   = st.FISCAL_YEAR
               AND dt.fiscal_period = st.fiscal_period
               AND dt.rn = 1
        ),
        cte_tax_recon AS (
            SELECT st.FISCAL_YEAR,
                   st.fiscal_period,
                   'FIN_INVOICES' AS source_system,
                   'TAX_AMOUNT' AS recon_type,
                   st.src_tax_amount AS source_total,
                   NVL(dt.dwh_tax_amount, 0) AS dwh_total,
                   st.src_tax_amount - NVL(dt.dwh_tax_amount, 0) AS difference,
                   CASE
                       WHEN st.src_tax_amount <> 0
                       THEN ROUND(
                           (st.src_tax_amount - NVL(dt.dwh_tax_amount, 0))
                           / ABS(st.src_tax_amount) * 100, 4
                       )
                       ELSE 0
                   END AS difference_pct,
                   CASE
                       WHEN ABS(st.src_tax_amount - NVL(dt.dwh_tax_amount, 0)) < 0.01
                       THEN 'MATCHED'
                       ELSE 'MISMATCH'
                   END AS match_status,
                   'Tax source=' || TO_CHAR(st.src_tax_amount, 'FM999,999,999,990.00')
                       || ' DWH=' || TO_CHAR(NVL(dt.dwh_tax_amount, 0), 'FM999,999,999,990.00')
                   AS detail_text
              FROM cte_source_totals st
              LEFT JOIN cte_dwh_totals dt
                ON dt.FISCAL_YEAR   = st.FISCAL_YEAR
               AND dt.fiscal_period = st.fiscal_period
               AND dt.rn = 1
        )
        SELECT * FROM cte_amount_recon
        UNION ALL
        SELECT * FROM cte_count_recon
        UNION ALL
        SELECT * FROM cte_tax_recon;

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step, 'DONE', g_row_count);
    END reconcile_invoice_totals;

    -- =========================================================================
    -- Private: reconcile_payment_totals
    -- =========================================================================
    PROCEDURE reconcile_payment_totals (
        p_fiscal_year   IN NUMBER,
        p_fiscal_period IN NUMBER
    ) IS
        v_step VARCHAR2(200) := 'RECONCILE_PAYMENT_TOTALS';
    BEGIN
        g_step_start := SYSTIMESTAMP;

        INSERT /*+ PARALLEL(8) */
          INTO DWH.FACT_RECON_PAYMENT_TOTALS (
                   fiscal_year, fiscal_period,
                   source_system, recon_type,
                   source_total, dwh_total,
                   difference, difference_pct,
                   match_status, detail_text,
                   created_date
               )
        WITH cte_src_payments AS (
            SELECT fp.FISCAL_YEAR,
                   fp.PERIOD_NUMBER                        AS fiscal_period,
                   SUM(p.TOTAL_AMOUNT)                     AS src_total,
                   SUM(p.ALLOCATED_AMOUNT)                 AS src_allocated,
                   COUNT(DISTINCT p.PAYMENT_ID)            AS src_count,
                   SUM(CASE WHEN p.CLEARED_DATE IS NOT NULL
                            THEN p.TOTAL_AMOUNT ELSE 0 END) AS src_cleared
              FROM FIN.PAYMENTS p
              JOIN FIN.FISCAL_PERIODS fp
                ON fp.FISCAL_PERIOD_ID = p.FISCAL_PERIOD_ID
             WHERE fp.FISCAL_YEAR   = p_fiscal_year
               AND fp.PERIOD_NUMBER = p_fiscal_period
               AND p.STATUS IN ('COMPLETED', 'RECONCILED', 'CLEARED')
             GROUP BY fp.FISCAL_YEAR, fp.PERIOD_NUMBER
        ),
        cte_dwh_payments AS (
            SELECT fp2.FISCAL_YEAR,
                   fp2.FISCAL_PERIOD,
                   SUM(fp2.PAYMENT_AMOUNT)                 AS dwh_total,
                   SUM(fp2.ALLOCATED_AMOUNT)               AS dwh_allocated,
                   COUNT(DISTINCT fp2.PAYMENT_ID)          AS dwh_count,
                   SUM(fp2.FX_GAIN_LOSS)                   AS dwh_fx_total,
                   SUM(fp2.DISCOUNT_TAKEN)                 AS dwh_discount_total,
                   SUM(fp2.WRITE_OFF_AMOUNT)               AS dwh_writeoff_total,
                   ROW_NUMBER() OVER (
                       PARTITION BY fp2.FISCAL_YEAR, fp2.FISCAL_PERIOD
                       ORDER BY SUM(fp2.PAYMENT_AMOUNT) DESC
                   ) AS rn
              FROM DWH.FACT_PAYMENTS fp2
             WHERE fp2.FISCAL_YEAR   = p_fiscal_year
               AND fp2.FISCAL_PERIOD = p_fiscal_period
               AND fp2.STATUS IN ('COMPLETED', 'RECONCILED', 'CLEARED')
             GROUP BY fp2.FISCAL_YEAR, fp2.FISCAL_PERIOD
        ),
        cte_pay_amount_recon AS (
            SELECT sp.FISCAL_YEAR,
                   sp.fiscal_period,
                   'FIN_PAYMENTS' AS source_system,
                   'PAYMENT_TOTAL' AS recon_type,
                   sp.src_total AS source_total,
                   NVL(dp.dwh_total, 0) AS dwh_total,
                   sp.src_total - NVL(dp.dwh_total, 0) AS difference,
                   CASE
                       WHEN sp.src_total <> 0
                       THEN ROUND((sp.src_total - NVL(dp.dwh_total, 0))
                                  / ABS(sp.src_total) * 100, 4)
                       ELSE 0
                   END AS difference_pct,
                   CASE
                       WHEN ABS(sp.src_total - NVL(dp.dwh_total, 0)) < 0.01
                       THEN 'MATCHED' ELSE 'MISMATCH'
                   END AS match_status,
                   'Payment total: src=' || TO_CHAR(sp.src_total, 'FM999,999,999,990.00')
                       || ' dwh=' || TO_CHAR(NVL(dp.dwh_total, 0), 'FM999,999,999,990.00')
                   AS detail_text
              FROM cte_src_payments sp
              LEFT JOIN cte_dwh_payments dp
                ON dp.FISCAL_YEAR   = sp.FISCAL_YEAR
               AND dp.fiscal_period = sp.fiscal_period
               AND dp.rn = 1
        ),
        cte_pay_count_recon AS (
            SELECT sp.FISCAL_YEAR,
                   sp.fiscal_period,
                   'FIN_PAYMENTS' AS source_system,
                   'PAYMENT_COUNT' AS recon_type,
                   sp.src_count AS source_total,
                   NVL(dp.dwh_count, 0) AS dwh_total,
                   sp.src_count - NVL(dp.dwh_count, 0) AS difference,
                   CASE
                       WHEN sp.src_count <> 0
                       THEN ROUND((sp.src_count - NVL(dp.dwh_count, 0))
                                  / sp.src_count * 100, 4)
                       ELSE 0
                   END AS difference_pct,
                   CASE
                       WHEN sp.src_count = NVL(dp.dwh_count, 0)
                       THEN 'MATCHED' ELSE 'MISMATCH'
                   END AS match_status,
                   'Payment count: src=' || sp.src_count
                       || ' dwh=' || NVL(dp.dwh_count, 0)
                   AS detail_text
              FROM cte_src_payments sp
              LEFT JOIN cte_dwh_payments dp
                ON dp.FISCAL_YEAR   = sp.FISCAL_YEAR
               AND dp.fiscal_period = sp.fiscal_period
               AND dp.rn = 1
        ),
        cte_alloc_recon AS (
            SELECT sp.FISCAL_YEAR,
                   sp.fiscal_period,
                   'FIN_PAYMENTS' AS source_system,
                   'ALLOCATED_TOTAL' AS recon_type,
                   sp.src_allocated AS source_total,
                   NVL(dp.dwh_allocated, 0) AS dwh_total,
                   sp.src_allocated - NVL(dp.dwh_allocated, 0) AS difference,
                   CASE
                       WHEN sp.src_allocated <> 0
                       THEN ROUND((sp.src_allocated - NVL(dp.dwh_allocated, 0))
                                  / ABS(sp.src_allocated) * 100, 4)
                       ELSE 0
                   END AS difference_pct,
                   CASE
                       WHEN ABS(sp.src_allocated - NVL(dp.dwh_allocated, 0)) < 0.01
                       THEN 'MATCHED' ELSE 'MISMATCH'
                   END AS match_status,
                   'Allocated: src=' || TO_CHAR(sp.src_allocated, 'FM999,999,999,990.00')
                       || ' dwh=' || TO_CHAR(NVL(dp.dwh_allocated, 0), 'FM999,999,999,990.00')
                   AS detail_text
              FROM cte_src_payments sp
              LEFT JOIN cte_dwh_payments dp
                ON dp.FISCAL_YEAR   = sp.FISCAL_YEAR
               AND dp.fiscal_period = sp.fiscal_period
               AND dp.rn = 1
        )
        SELECT * FROM cte_pay_amount_recon
        UNION ALL
        SELECT * FROM cte_pay_count_recon
        UNION ALL
        SELECT * FROM cte_alloc_recon;

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step, 'DONE', g_row_count);
    END reconcile_payment_totals;

    -- =========================================================================
    -- Private: reconcile_journal_totals
    -- =========================================================================
    PROCEDURE reconcile_journal_totals (
        p_fiscal_year   IN NUMBER,
        p_fiscal_period IN NUMBER
    ) IS
        v_step VARCHAR2(200) := 'RECONCILE_JOURNAL_TOTALS';
    BEGIN
        g_step_start := SYSTIMESTAMP;

        INSERT /*+ PARALLEL(8) */
          INTO DWH.FACT_RECON_JOURNAL_TOTALS (
                   fiscal_year, fiscal_period,
                   source_system, recon_type,
                   source_total, dwh_total,
                   difference, difference_pct,
                   match_status, detail_text,
                   created_date
               )
        WITH cte_src_journals AS (
            SELECT fp.FISCAL_YEAR,
                   fp.PERIOD_NUMBER AS fiscal_period,
                   SUM(jl.DEBIT_AMOUNT)   AS src_debit,
                   SUM(jl.CREDIT_AMOUNT)  AS src_credit,
                   SUM(jl.BASE_DEBIT)     AS src_base_debit,
                   SUM(jl.BASE_CREDIT)    AS src_base_credit,
                   COUNT(DISTINCT j.JOURNAL_ID) AS src_journal_count,
                   COUNT(jl.JOURNAL_LINE_ID)    AS src_line_count
              FROM FIN.JOURNAL_LINES jl
              JOIN FIN.JOURNALS j
                ON j.JOURNAL_ID = jl.JOURNAL_ID
              JOIN FIN.FISCAL_PERIODS fp
                ON fp.FISCAL_PERIOD_ID = j.FISCAL_PERIOD_ID
             WHERE fp.FISCAL_YEAR   = p_fiscal_year
               AND fp.PERIOD_NUMBER = p_fiscal_period
               AND j.STATUS = 'POSTED'
             GROUP BY fp.FISCAL_YEAR, fp.PERIOD_NUMBER
        ),
        cte_dwh_journals AS (
            SELECT fj.FISCAL_YEAR,
                   fj.FISCAL_PERIOD,
                   SUM(fj.DEBIT_AMOUNT)   AS dwh_debit,
                   SUM(fj.CREDIT_AMOUNT)  AS dwh_credit,
                   SUM(fj.BASE_DEBIT)     AS dwh_base_debit,
                   SUM(fj.BASE_CREDIT)    AS dwh_base_credit,
                   COUNT(DISTINCT fj.JOURNAL_ID) AS dwh_journal_count,
                   COUNT(fj.JOURNAL_LINE_ID)     AS dwh_line_count,
                   ROW_NUMBER() OVER (
                       PARTITION BY fj.FISCAL_YEAR, fj.FISCAL_PERIOD
                       ORDER BY SUM(fj.DEBIT_AMOUNT) DESC
                   ) AS rn
              FROM DWH.FACT_JOURNAL_ENTRIES fj
             WHERE fj.FISCAL_YEAR   = p_fiscal_year
               AND fj.FISCAL_PERIOD = p_fiscal_period
               AND fj.STATUS = 'POSTED'
             GROUP BY fj.FISCAL_YEAR, fj.FISCAL_PERIOD
        ),
        cte_debit_recon AS (
            SELECT sj.FISCAL_YEAR,
                   sj.fiscal_period,
                   'FIN_JOURNALS' AS source_system,
                   'TOTAL_DEBIT' AS recon_type,
                   sj.src_debit AS source_total,
                   NVL(dj.dwh_debit, 0) AS dwh_total,
                   sj.src_debit - NVL(dj.dwh_debit, 0) AS difference,
                   CASE
                       WHEN sj.src_debit <> 0
                       THEN ROUND((sj.src_debit - NVL(dj.dwh_debit, 0))
                                  / ABS(sj.src_debit) * 100, 4)
                       ELSE 0
                   END AS difference_pct,
                   CASE
                       WHEN ABS(sj.src_debit - NVL(dj.dwh_debit, 0)) < 0.01
                       THEN 'MATCHED' ELSE 'MISMATCH'
                   END AS match_status,
                   'Debit: src=' || TO_CHAR(sj.src_debit, 'FM999,999,999,990.00')
                       || ' dwh=' || TO_CHAR(NVL(dj.dwh_debit, 0), 'FM999,999,999,990.00')
                   AS detail_text
              FROM cte_src_journals sj
              LEFT JOIN cte_dwh_journals dj
                ON dj.FISCAL_YEAR   = sj.FISCAL_YEAR
               AND dj.fiscal_period = sj.fiscal_period
               AND dj.rn = 1
        ),
        cte_credit_recon AS (
            SELECT sj.FISCAL_YEAR,
                   sj.fiscal_period,
                   'FIN_JOURNALS' AS source_system,
                   'TOTAL_CREDIT' AS recon_type,
                   sj.src_credit AS source_total,
                   NVL(dj.dwh_credit, 0) AS dwh_total,
                   sj.src_credit - NVL(dj.dwh_credit, 0) AS difference,
                   CASE
                       WHEN sj.src_credit <> 0
                       THEN ROUND((sj.src_credit - NVL(dj.dwh_credit, 0))
                                  / ABS(sj.src_credit) * 100, 4)
                       ELSE 0
                   END AS difference_pct,
                   CASE
                       WHEN ABS(sj.src_credit - NVL(dj.dwh_credit, 0)) < 0.01
                       THEN 'MATCHED' ELSE 'MISMATCH'
                   END AS match_status,
                   'Credit: src=' || TO_CHAR(sj.src_credit, 'FM999,999,999,990.00')
                       || ' dwh=' || TO_CHAR(NVL(dj.dwh_credit, 0), 'FM999,999,999,990.00')
                   AS detail_text
              FROM cte_src_journals sj
              LEFT JOIN cte_dwh_journals dj
                ON dj.FISCAL_YEAR   = sj.FISCAL_YEAR
               AND dj.fiscal_period = sj.fiscal_period
               AND dj.rn = 1
        ),
        cte_journal_count_recon AS (
            SELECT sj.FISCAL_YEAR,
                   sj.fiscal_period,
                   'FIN_JOURNALS' AS source_system,
                   'JOURNAL_COUNT' AS recon_type,
                   sj.src_journal_count AS source_total,
                   NVL(dj.dwh_journal_count, 0) AS dwh_total,
                   sj.src_journal_count - NVL(dj.dwh_journal_count, 0) AS difference,
                   CASE
                       WHEN sj.src_journal_count <> 0
                       THEN ROUND((sj.src_journal_count - NVL(dj.dwh_journal_count, 0))
                                  / sj.src_journal_count * 100, 4)
                       ELSE 0
                   END AS difference_pct,
                   CASE
                       WHEN sj.src_journal_count = NVL(dj.dwh_journal_count, 0)
                       THEN 'MATCHED' ELSE 'MISMATCH'
                   END AS match_status,
                   'Journal count: src=' || sj.src_journal_count
                       || ' dwh=' || NVL(dj.dwh_journal_count, 0)
                   AS detail_text
              FROM cte_src_journals sj
              LEFT JOIN cte_dwh_journals dj
                ON dj.FISCAL_YEAR   = sj.FISCAL_YEAR
               AND dj.fiscal_period = sj.fiscal_period
               AND dj.rn = 1
        ),
        cte_line_count_recon AS (
            SELECT sj.FISCAL_YEAR,
                   sj.fiscal_period,
                   'FIN_JOURNALS' AS source_system,
                   'LINE_COUNT' AS recon_type,
                   sj.src_line_count AS source_total,
                   NVL(dj.dwh_line_count, 0) AS dwh_total,
                   sj.src_line_count - NVL(dj.dwh_line_count, 0) AS difference,
                   CASE
                       WHEN sj.src_line_count <> 0
                       THEN ROUND((sj.src_line_count - NVL(dj.dwh_line_count, 0))
                                  / sj.src_line_count * 100, 4)
                       ELSE 0
                   END AS difference_pct,
                   CASE
                       WHEN sj.src_line_count = NVL(dj.dwh_line_count, 0)
                       THEN 'MATCHED' ELSE 'MISMATCH'
                   END AS match_status,
                   'Line count: src=' || sj.src_line_count
                       || ' dwh=' || NVL(dj.dwh_line_count, 0)
                   AS detail_text
              FROM cte_src_journals sj
              LEFT JOIN cte_dwh_journals dj
                ON dj.FISCAL_YEAR   = sj.FISCAL_YEAR
               AND dj.fiscal_period = sj.fiscal_period
               AND dj.rn = 1
        ),
        cte_balance_recon AS (
            SELECT sj.FISCAL_YEAR,
                   sj.fiscal_period,
                   'FIN_JOURNALS' AS source_system,
                   'DR_CR_BALANCE' AS recon_type,
                   sj.src_base_debit AS source_total,
                   sj.src_base_credit AS dwh_total,
                   ABS(sj.src_base_debit - sj.src_base_credit) AS difference,
                   CASE
                       WHEN sj.src_base_debit <> 0
                       THEN ROUND(ABS(sj.src_base_debit - sj.src_base_credit)
                                  / ABS(sj.src_base_debit) * 100, 4)
                       ELSE 0
                   END AS difference_pct,
                   CASE
                       WHEN ABS(sj.src_base_debit - sj.src_base_credit) < 0.01
                       THEN 'BALANCED' ELSE 'UNBALANCED'
                   END AS match_status,
                   'Source DR=' || TO_CHAR(sj.src_base_debit, 'FM999,999,999,990.00')
                       || ' CR=' || TO_CHAR(sj.src_base_credit, 'FM999,999,999,990.00')
                   AS detail_text
              FROM cte_src_journals sj
        )
        SELECT * FROM cte_debit_recon
        UNION ALL
        SELECT * FROM cte_credit_recon
        UNION ALL
        SELECT * FROM cte_journal_count_recon
        UNION ALL
        SELECT * FROM cte_line_count_recon
        UNION ALL
        SELECT * FROM cte_balance_recon;

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step, 'DONE', g_row_count);
    END reconcile_journal_totals;

    -- =========================================================================
    -- Private: calculate_cash_position
    -- =========================================================================
    PROCEDURE calculate_cash_position (
        p_as_of_date IN DATE DEFAULT TRUNC(SYSDATE)
    ) IS
        v_step VARCHAR2(200) := 'CALCULATE_CASH_POSITION';
    BEGIN
        g_step_start := SYSTIMESTAMP;

        MERGE /*+ PARALLEL(8) */ INTO DWH.FACT_CASH_POSITION tgt
        USING (
            WITH cte_bank_balances AS (
                SELECT ba.BANK_ACCOUNT_ID,
                       ba.ACCOUNT_NUMBER,
                       ba.ACCOUNT_NAME,
                       ba.BANK_NAME,
                       ba.CURRENT_BALANCE,
                       (SELECT cur.CURRENCY_CODE
                          FROM FIN.CURRENCIES cur
                         WHERE cur.CURRENCY_ID = ba.CURRENCY_ID) AS currency_code
                  FROM FIN.BANK_ACCOUNTS ba
            ),
            cte_pending_receipts AS (
                SELECT ba.BANK_ACCOUNT_ID,
                       SUM(inv.TOTAL_AMOUNT - NVL(inv.PAID_AMOUNT, 0)) AS expected_receipts,
                       COUNT(DISTINCT inv.INVOICE_ID) AS pending_invoice_count
                  FROM FIN.INVOICES inv
                  CROSS JOIN FIN.BANK_ACCOUNTS ba
                 WHERE inv.STATUS IN ('APPROVED', 'POSTED', 'PARTIALLY_PAID')
                   AND inv.DUE_DATE BETWEEN p_as_of_date AND p_as_of_date + 30
                   AND inv.TOTAL_AMOUNT - NVL(inv.PAID_AMOUNT, 0) > 0
                 GROUP BY ba.BANK_ACCOUNT_ID
            ),
            cte_recent_payments AS (
                SELECT p.BANK_ACCOUNT_ID,
                       SUM(p.TOTAL_AMOUNT)          AS recent_outflows,
                       COUNT(DISTINCT p.PAYMENT_ID) AS recent_payment_count,
                       AVG(p.TOTAL_AMOUNT)          AS avg_payment_amount
                  FROM FIN.PAYMENTS p
                 WHERE p.PAYMENT_DATE BETWEEN p_as_of_date - 30 AND p_as_of_date
                   AND p.STATUS IN ('COMPLETED', 'RECONCILED', 'CLEARED')
                 GROUP BY p.BANK_ACCOUNT_ID
            ),
            cte_uncleared_payments AS (
                SELECT p2.BANK_ACCOUNT_ID,
                       SUM(p2.TOTAL_AMOUNT) AS uncleared_amount,
                       COUNT(DISTINCT p2.PAYMENT_ID) AS uncleared_count
                  FROM FIN.PAYMENTS p2
                 WHERE p2.CLEARED_DATE IS NULL
                   AND p2.STATUS = 'COMPLETED'
                   AND p2.PAYMENT_DATE <= p_as_of_date
                 GROUP BY p2.BANK_ACCOUNT_ID
            ),
            cte_ar_aging AS (
                SELECT SUM(CASE WHEN inv2.DUE_DATE >= p_as_of_date
                                THEN inv2.TOTAL_AMOUNT - NVL(inv2.PAID_AMOUNT, 0) ELSE 0 END)
                           AS ar_current,
                       SUM(CASE WHEN inv2.DUE_DATE < p_as_of_date
                                 AND inv2.DUE_DATE >= p_as_of_date - 30
                                THEN inv2.TOTAL_AMOUNT - NVL(inv2.PAID_AMOUNT, 0) ELSE 0 END)
                           AS ar_1_30,
                       SUM(CASE WHEN inv2.DUE_DATE < p_as_of_date - 30
                                THEN inv2.TOTAL_AMOUNT - NVL(inv2.PAID_AMOUNT, 0) ELSE 0 END)
                           AS ar_over_30,
                       SUM(inv2.TOTAL_AMOUNT - NVL(inv2.PAID_AMOUNT, 0))
                           AS total_ar
                  FROM FIN.INVOICES inv2
                 WHERE inv2.STATUS IN ('APPROVED', 'POSTED', 'PARTIALLY_PAID')
                   AND inv2.TOTAL_AMOUNT - NVL(inv2.PAID_AMOUNT, 0) > 0
            ),
            cte_combined AS (
                SELECT bb.BANK_ACCOUNT_ID,
                       bb.ACCOUNT_NUMBER,
                       bb.ACCOUNT_NAME,
                       bb.BANK_NAME,
                       bb.currency_code,
                       bb.CURRENT_BALANCE,
                       bb.CURRENT_BALANCE
                           - NVL(uc.uncleared_amount, 0)  AS available_balance,
                       NVL(pr.expected_receipts, 0)        AS open_ar_amount,
                       NVL(rp.recent_outflows, 0)          AS recent_outflows,
                       bb.CURRENT_BALANCE
                           + NVL(pr.expected_receipts, 0)
                           - NVL(rp.avg_payment_amount, 0) * 30
                                                            AS forecast_30d,
                       RANK() OVER (
                           ORDER BY bb.CURRENT_BALANCE DESC
                       ) AS balance_rank,
                       SUM(bb.CURRENT_BALANCE) OVER () AS total_cash
                  FROM cte_bank_balances bb
                  LEFT JOIN cte_pending_receipts pr
                    ON pr.BANK_ACCOUNT_ID = bb.BANK_ACCOUNT_ID
                  LEFT JOIN cte_recent_payments rp
                    ON rp.BANK_ACCOUNT_ID = bb.BANK_ACCOUNT_ID
                  LEFT JOIN cte_uncleared_payments uc
                    ON uc.BANK_ACCOUNT_ID = bb.BANK_ACCOUNT_ID
            )
            SELECT c.BANK_ACCOUNT_ID, c.ACCOUNT_NUMBER,
                   c.ACCOUNT_NAME, c.BANK_NAME, c.currency_code,
                   c.CURRENT_BALANCE, c.available_balance,
                   c.open_ar_amount, c.recent_outflows,
                   c.forecast_30d, c.balance_rank, c.total_cash,
                   p_as_of_date AS position_date
              FROM cte_combined c
        ) src
        ON (    tgt.BANK_ACCOUNT_ID = src.BANK_ACCOUNT_ID
            AND tgt.POSITION_DATE   = src.position_date)
        WHEN MATCHED THEN
            UPDATE SET
                tgt.CURRENT_BALANCE  = src.CURRENT_BALANCE,
                tgt.AVAILABLE_BALANCE = src.available_balance,
                tgt.OPEN_AR_AMOUNT   = src.open_ar_amount,
                tgt.FORECAST_30D     = src.forecast_30d,
                tgt.BALANCE_RANK     = src.balance_rank,
                tgt.TOTAL_CASH       = src.total_cash,
                tgt.UPDATED_DATE     = SYSDATE
             WHERE tgt.CURRENT_BALANCE <> src.CURRENT_BALANCE
                OR tgt.OPEN_AR_AMOUNT  <> src.open_ar_amount
        WHEN NOT MATCHED THEN
            INSERT (
                BANK_ACCOUNT_ID, ACCOUNT_NUMBER,
                ACCOUNT_NAME, BANK_NAME, CURRENCY_CODE,
                CURRENT_BALANCE, AVAILABLE_BALANCE,
                OPEN_AR_AMOUNT, RECENT_OUTFLOWS,
                FORECAST_30D, BALANCE_RANK, TOTAL_CASH,
                POSITION_DATE, CREATED_DATE
            )
            VALUES (
                src.BANK_ACCOUNT_ID, src.ACCOUNT_NUMBER,
                src.ACCOUNT_NAME, src.BANK_NAME, src.currency_code,
                src.CURRENT_BALANCE, src.available_balance,
                src.open_ar_amount, src.recent_outflows,
                src.forecast_30d, src.balance_rank, src.total_cash,
                src.position_date, SYSDATE
            );

        g_row_count := SQL%ROWCOUNT;
        COMMIT;
        log_step(v_step, 'DONE', g_row_count);
    END calculate_cash_position;

END PKG_ETL_FACT_FINANCE;
/
