/**
 * InspectorProto — visual prototype of all Inspector panel variants.
 * Route: /proto/inspector  (dev-only, no auth guard)
 */

import type { DaliNodeData } from '../../types/domain';
import { InspectorTable }     from './InspectorTable';
import { InspectorStatement } from './InspectorStatement';
import { InspectorRoutine }   from './InspectorRoutine';
import { InspectorRecord }    from './InspectorRecord';
import { InspectorSchema }    from './InspectorSchema';
import { InspectorJoin }      from './InspectorJoin';
import { InspectorSection, InspectorRow } from './InspectorSection';

// ── Mock data ─────────────────────────────────────────────────────────────────

const TABLE_DATA: DaliNodeData = {
  label: 'FACT_SALES',
  nodeType: 'DaliTable',
  schema: 'DWH',
  childrenAvailable: false,
  metadata: {
    dataSource: 'master',
    ddlText: `CREATE TABLE DWH.FACT_SALES (
  ORDER_ID     NUMBER        NOT NULL,
  CUSTOMER_ID  NUMBER        NOT NULL,
  PRODUCT_CODE VARCHAR2(20)  NOT NULL,
  AMOUNT       NUMBER(18,2),
  CREATED_AT   DATE,
  CONSTRAINT PK_FACT_SALES
    PRIMARY KEY (ORDER_ID),
  CONSTRAINT FK_FACT_CUST
    FOREIGN KEY (CUSTOMER_ID)
    REFERENCES DWH.DIM_CUSTOMER(ID),
  CONSTRAINT FK_FACT_PROD
    FOREIGN KEY (PRODUCT_CODE)
    REFERENCES DWH.DIM_PRODUCT(PRODUCT_CODE)
);`,
  },
  columns: [
    { id: 'c1', name: 'ORDER_ID',     type: 'NUMBER',       isPrimaryKey: true,  isForeignKey: false, isRequired: true  },
    { id: 'c2', name: 'CUSTOMER_ID',  type: 'NUMBER',       isPrimaryKey: false, isForeignKey: true,  isRequired: true  },
    { id: 'c3', name: 'PRODUCT_CODE', type: 'VARCHAR(20)',  isPrimaryKey: false, isForeignKey: true,  isRequired: true  },
    { id: 'c4', name: 'AMOUNT',       type: 'DECIMAL(18,2)',isPrimaryKey: false, isForeignKey: false, isRequired: false },
    { id: 'c5', name: 'CREATED_AT',   type: 'DATE',         isPrimaryKey: false, isForeignKey: false, isRequired: false },
  ],
};

const TABLE_DATA_RECONSTRUCTED: DaliNodeData = {
  label: 'ORDERS_STG',
  nodeType: 'DaliTable',
  schema: 'STAGING',
  childrenAvailable: false,
  metadata: { dataSource: 'reconstructed' },
  columns: [
    { id: 'r1', name: 'ORDER_ID',  type: 'NUMBER',      isPrimaryKey: true,  isForeignKey: false, isRequired: true },
    { id: 'r2', name: 'STATUS',    type: 'VARCHAR(10)', isPrimaryKey: false, isForeignKey: false, isRequired: true },
  ],
};

const STMT_INSERT_DATA: DaliNodeData = {
  label: 'INSERT:47',
  nodeType: 'DaliStatement',
  operation: 'INSERT',
  schema: 'DWH',
  childrenAvailable: false,
  metadata: {
    stmtType:  'INSERT',
    groupPath: ['PKG_ETL_ANALYTICS', 'BUILD_CUSTOMER_DASHBOARD'],
    sqlText: `INSERT INTO DWH.FACT_SALES (
  order_id, customer_id, product_code, amount, created_at
)
SELECT
  o.order_id,
  o.customer_id,
  p.product_code,
  SUM(oi.qty * oi.unit_price) AS amount,
  SYSDATE
FROM STAGING.ORDERS_STG o
JOIN STAGING.ORDER_ITEMS  oi ON oi.order_id = o.order_id
JOIN STAGING.PRODUCTS     p  ON p.product_id = oi.product_id
WHERE o.status = 'ACTIVE'
GROUP BY o.order_id, o.customer_id, p.product_code`,
    line_start:  47,
    line_end:    62,
    session_id: 'session-proto-2026',
  },
  columns: [
    { id: 'oc1', name: 'ORDER_ID',     type: 'NUMBER',       isPrimaryKey: false, isForeignKey: false, isRequired: true  },
    { id: 'oc2', name: 'CUSTOMER_ID',  type: 'NUMBER',       isPrimaryKey: false, isForeignKey: false, isRequired: false },
    { id: 'oc3', name: 'PRODUCT_CODE', type: 'VARCHAR(20)',  isPrimaryKey: false, isForeignKey: false, isRequired: false },
    { id: 'oc4', name: 'AMOUNT',       type: 'DECIMAL(18,2)',isPrimaryKey: false, isForeignKey: false, isRequired: false },
    { id: 'oc5', name: 'CREATED_AT',   type: 'DATE',         isPrimaryKey: false, isForeignKey: false, isRequired: false },
  ],
};

const STMT_SELECT_DATA: DaliNodeData = {
  label: 'SELECT:12',
  nodeType: 'DaliStatement',
  operation: 'SELECT',
  childrenAvailable: false,
  metadata: {
    stmtType:  'SELECT',
    groupPath: ['PKG_ETL_ANALYTICS', 'LOAD_STAGING'],
    sqlText: `SELECT order_id, customer_id, SUM(amount) AS total
FROM STAGING.ORDERS_STG
WHERE created_at >= TRUNC(SYSDATE) - 30
GROUP BY order_id, customer_id
BULK COLLECT INTO l_tab`,
    line_start: 12,
    line_end:   18,
    session_id: 'session-proto-2026',
  },
  columns: [
    { id: 's1', name: 'ORDER_ID',   type: 'NUMBER',       isPrimaryKey: false, isForeignKey: false, isRequired: true  },
    { id: 's2', name: 'CUSTOMER_ID',type: 'NUMBER',       isPrimaryKey: false, isForeignKey: false, isRequired: false },
    { id: 's3', name: 'TOTAL',      type: 'DECIMAL(18,2)',isPrimaryKey: false, isForeignKey: false, isRequired: false },
  ],
};

const ROUTINE_DATA: DaliNodeData = {
  label: 'BUILD_CUSTOMER_DASHBOARD',
  nodeType: 'DaliRoutine',
  childrenAvailable: false,
  metadata: {
    routineKind: 'PROCEDURE',
    packageName: 'PKG_ETL_ANALYTICS',
    language:    'PLSQL',
    schema:      'DWH',
    sqlText: `PROCEDURE BUILD_CUSTOMER_DASHBOARD IS
  l_tab  SYS_REFCURSOR;
  v_cnt  NUMBER := 0;
BEGIN
  -- Load active orders
  OPEN l_tab FOR
    SELECT o.order_id, o.customer_id, SUM(oi.qty * oi.unit_price) AS amount
    FROM   STAGING.ORDERS_STG o
    JOIN   STAGING.ORDER_ITEMS oi ON oi.order_id = o.order_id
    WHERE  o.status = 'ACTIVE'
    GROUP  BY o.order_id, o.customer_id;

  -- Merge into DWH
  INSERT INTO DWH.FACT_SALES
    SELECT * FROM TABLE(l_tab);

  COMMIT;
EXCEPTION
  WHEN OTHERS THEN ROLLBACK; RAISE;
END BUILD_CUSTOMER_DASHBOARD;`,
  },
};

const FUNCTION_DATA: DaliNodeData = {
  label: 'GET_CUSTOMER_TIER',
  nodeType: 'DaliRoutine',
  childrenAvailable: false,
  metadata: {
    routineKind: 'FUNCTION',
    packageName: 'PKG_ETL_ANALYTICS',
    language:    'PLSQL',
    schema:      'DWH',
    sqlText: `FUNCTION GET_CUSTOMER_TIER(p_customer_id IN NUMBER)
  RETURN VARCHAR2
IS
  v_total NUMBER;
BEGIN
  SELECT SUM(amount) INTO v_total
  FROM   DWH.FACT_SALES
  WHERE  customer_id = p_customer_id
  AND    created_at >= ADD_MONTHS(SYSDATE, -12);

  RETURN CASE
    WHEN v_total > 100000 THEN 'PLATINUM'
    WHEN v_total >  50000 THEN 'GOLD'
    WHEN v_total >  10000 THEN 'SILVER'
    ELSE 'STANDARD'
  END;
END GET_CUSTOMER_TIER;`,
  },
};

const PACKAGE_DATA: DaliNodeData = {
  label: 'PKG_ETL_ANALYTICS',
  nodeType: 'DaliPackage',
  childrenAvailable: false,
  routinesCount: 7,
  metadata: {
    routineKind: 'PACKAGE',
    language:    'PLSQL',
    schema:      'DWH',
  },
};

const RECORD_DATA: DaliNodeData = {
  label: 'L_TAB',
  nodeType: 'DaliRecord',
  childrenAvailable: false,
  metadata: {
    schema:       'DWH',
    packageName:  'PKG_ETL_ANALYTICS',
    routineGeoid: 'PROCEDURE:BUILD_CUSTOMER_DASHBOARD',
    record_geoid: '#45:123',
  },
  columns: [
    { id: 'rf1', name: 'ORDER_ID',    type: 'NUMBER',       isPrimaryKey: false, isForeignKey: true,  isRequired: true  },
    { id: 'rf2', name: 'CUSTOMER_ID', type: 'NUMBER',       isPrimaryKey: false, isForeignKey: true,  isRequired: false },
    { id: 'rf3', name: 'AMOUNT',      type: 'DECIMAL(18,2)',isPrimaryKey: false, isForeignKey: false, isRequired: false },
  ],
};

const SCHEMA_DATA: DaliNodeData = {
  label: 'DWH',
  nodeType: 'DaliSchema',
  childrenAvailable: true,
  metadata: { tablesCount: 24, routinesCount: 8 },
};

const DATABASE_DATA: DaliNodeData = {
  label: 'DataWarehouse',
  nodeType: 'DaliDatabase',
  childrenAvailable: true,
  metadata: { db_name: 'DataWarehouse', db_geoid: 'DWH' },
};

const JOIN_DATA: DaliNodeData = {
  label: 'JOIN:ORDERS-CUSTOMERS',
  nodeType: 'DaliJoin',
  childrenAvailable: false,
  metadata: {
    joinType:   'LEFT',
    leftTable:  'ORDERS_STG',
    rightTable: 'CUSTOMERS',
    condition:  'ORDERS_STG.CUSTOMER_ID = CUSTOMERS.ID',
  },
};

// ── Layout helpers ─────────────────────────────────────────────────────────────

function PanelWrapper({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 0, flexShrink: 0 }}>
      <div style={{
        padding: '4px 10px 6px',
        fontSize: 10, fontWeight: 700, letterSpacing: '0.08em',
        textTransform: 'uppercase', color: 'var(--t3)',
        background: 'var(--bg0)', borderBottom: '1px solid var(--bd)',
      }}>
        {title}
      </div>
      <div style={{
        width: 280, background: 'var(--bg1)',
        border: '1px solid var(--bd)', borderTop: 'none',
        overflowY: 'auto', maxHeight: '85vh',
      }}>
        {children}
      </div>
    </div>
  );
}

// ── Filled mock: Statement Extra tab ─────────────────────────────────────────
// Shows what ExtraPanel looks like with real data (READS_FROM + WRITES_TO + subqueries)

function MockStatementExtra() {
  const readTables = [
    { name: 'ORDERS_STG',   schema: 'STAGING', kind: 'ПРЯМЫЕ' },
    { name: 'ORDER_ITEMS',  schema: 'STAGING', kind: 'VIA ПОДЗАПРОСЫ' },
    { name: 'PRODUCTS',     schema: 'STAGING', kind: 'VIA ПОДЗАПРОСЫ' },
  ];
  const writeTables = [
    { name: 'FACT_SALES', schema: 'DWH', op: 'INSERT' },
  ];
  const subqueries = [
    { type: 'CTE',    label: 'cte_active_orders',   rid: '#31:200', indent: 0 },
    { type: 'CTE',    label: 'cte_product_totals',  rid: '#31:201', indent: 0 },
    { type: 'SELECT', label: 'inline_view_amounts',  rid: '#31:202', indent: 1 },
  ];

  const tableRow = (name: string, schema: string) => (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 6,
      padding: '3px 10px', borderTop: '1px solid var(--bd)', fontSize: '11px',
    }}>
      <span style={{ flex: 1, color: 'var(--t1)', fontFamily: 'var(--mono)' }}>{name}</span>
      <span style={{ fontSize: '9px', color: 'var(--t3)', fontFamily: 'var(--mono)', padding: '1px 5px', borderRadius: 2, border: '0.5px solid var(--bd)' }}>{schema}</span>
    </div>
  );

  return (
    <>
      <InspectorSection title={`ЧИТАЕМЫЕ ТАБЛИЦЫ (${readTables.length})`} defaultOpen>
        <div style={{ padding: '4px 10px 2px', fontSize: '9px', color: 'var(--t3)', letterSpacing: '0.06em', textTransform: 'uppercase' }}>
          ПРЯМЫЕ · 1
        </div>
        {tableRow('ORDERS_STG', 'STAGING')}
        <div style={{ padding: '6px 10px 2px', fontSize: '9px', color: 'var(--t3)', letterSpacing: '0.06em', textTransform: 'uppercase', borderTop: '1px solid var(--bd)', marginTop: 4 }}>
          VIA ПОДЗАПРОСЫ · 2
        </div>
        {tableRow('ORDER_ITEMS', 'STAGING')}
        {tableRow('PRODUCTS', 'STAGING')}
      </InspectorSection>

      <InspectorSection title={`ИЗМЕНЯЕМЫЕ ТАБЛИЦЫ (${writeTables.length})`} defaultOpen>
        {writeTables.map(({ name, schema, op }) => (
          <div key={name} style={{
            display: 'flex', alignItems: 'center', gap: 6,
            padding: '3px 10px', borderTop: '1px solid var(--bd)', fontSize: '11px',
          }}>
            <span style={{ flex: 1, color: 'var(--t1)', fontFamily: 'var(--mono)' }}>{name}</span>
            <span style={{ fontSize: '9px', color: 'var(--t3)', fontFamily: 'var(--mono)', padding: '1px 5px', borderRadius: 2, border: '0.5px solid var(--bd)' }}>{schema}</span>
            <span style={{ fontSize: '8px', color: '#D4922A', fontFamily: 'var(--mono)', padding: '1px 4px', borderRadius: 2, border: '0.5px solid #D4922A', fontWeight: 600 }}>{op}</span>
          </div>
        ))}
      </InspectorSection>

      <InspectorSection title={`ПОДЗАПРОСЫ (рекурсивно) (${subqueries.length})`} defaultOpen>
        {subqueries.map(({ type, label, rid, indent }) => (
          <div key={rid} style={{
            display: 'flex', alignItems: 'center', gap: 6,
            padding: '3px 10px', paddingLeft: 10 + indent * 12,
            borderTop: '1px solid var(--bd)', fontSize: '11px',
          }}>
            <span style={{ fontSize: '8px', padding: '1px 5px', borderRadius: 2, fontFamily: 'var(--mono)', border: `0.5px solid ${type === 'CTE' ? '#A8B860' : '#88B8A8'}`, color: type === 'CTE' ? '#A8B860' : '#88B8A8', fontWeight: 600, flexShrink: 0, letterSpacing: '0.03em' }}>{type}</span>
            <span style={{ flex: 1, color: 'var(--t1)', fontFamily: 'var(--mono)', fontSize: 10 }}>{label}</span>
            <span style={{ color: 'var(--t3)', fontSize: '9px', fontFamily: 'var(--mono)', flexShrink: 0 }}>{rid}</span>
          </div>
        ))}
      </InspectorSection>
    </>
  );
}

// ── Filled mock: Statement Stats tab ─────────────────────────────────────────

function MockStatementStats() {
  return (
    <>
      <InspectorSection title="СТАТИСТИКА">
        <InspectorRow label="Выходных колонок"  value="5" />
        <InspectorRow label="Строки"            value="47–62" />
        <InspectorRow label="Глубина"           value="2" />
      </InspectorSection>

      <InspectorSection title="Фильтры · JOIN · подзапросы">
        <InspectorRow label="WHERE атомы"    value="0" />
        <InspectorRow label="HAVING атомы"   value="0" />
        <InspectorRow label="JOIN атомы"     value="2" />
        <InspectorRow label="Подзапросные атомы (CTE+SQ)" value="6" />
      </InspectorSection>

      <InspectorSection title="Статистика атомов (8)" defaultOpen>
        <InspectorRow label="CTE"  value="6" />
        <InspectorRow label="JOIN" value="2" />
      </InspectorSection>
    </>
  );
}

// ── Page ──────────────────────────────────────────────────────────────────────

export function InspectorProto() {
  return (
    <div style={{
      height: '100vh',
      overflowY: 'auto',
      background: 'var(--bg0)',
      color: 'var(--t1)',
      padding: '24px 32px',
      fontFamily: 'var(--sans, system-ui)',
    }}>

      <div style={{ marginBottom: 24 }}>
        <div style={{ fontSize: 20, fontWeight: 700, marginBottom: 4 }}>
          Inspector Panel — прототип всех типов
        </div>
        <div style={{ fontSize: 12, color: 'var(--t3)' }}>
          Lazy-секции (Parameters, Variables, Statements, Extra) показывают loading-состояние — данных нет (mock nodeId).
          Заполненные примеры Extra/Stats показаны как отдельные статичные секции.
        </div>
      </div>

      {/* ── TABLES ─────────────────────────────────────────────────────────── */}
      <SectionLabel>TABLES</SectionLabel>
      <div style={{ display: 'flex', gap: 16, marginBottom: 32, flexWrap: 'wrap', alignItems: 'flex-start' }}>
        <PanelWrapper title="DaliTable — master (+ DDL tab)">
          <InspectorTable data={TABLE_DATA} nodeId="#11:42" />
        </PanelWrapper>
        <PanelWrapper title="DaliTable — reconstructed">
          <InspectorTable data={TABLE_DATA_RECONSTRUCTED} nodeId="#11:43" />
        </PanelWrapper>
      </div>

      {/* ── STATEMENTS ─────────────────────────────────────────────────────── */}
      <SectionLabel>STATEMENTS</SectionLabel>
      <div style={{ display: 'flex', gap: 16, marginBottom: 16, flexWrap: 'wrap', alignItems: 'flex-start' }}>
        <PanelWrapper title="DaliStatement — INSERT (SQL preloaded)">
          <InspectorStatement data={STMT_INSERT_DATA} nodeId="#25:47" />
        </PanelWrapper>
        <PanelWrapper title="DaliStatement — SELECT BULK COLLECT">
          <InspectorStatement data={STMT_SELECT_DATA} nodeId="#25:12" />
        </PanelWrapper>
      </div>

      {/* Extra / Stats filled mock examples */}
      <div style={{ marginBottom: 6, fontSize: 11, color: 'var(--t3)', letterSpacing: '0.04em' }}>
        ↓ Заполненный пример: вкладка ДОПОЛН (Extra) — Читаемые + Изменяемые + Подзапросы
      </div>
      <div style={{ display: 'flex', gap: 16, marginBottom: 8, flexWrap: 'wrap', alignItems: 'flex-start' }}>
        <PanelWrapper title="INSERT:47 — Extra (с данными)">
          <MockStatementExtra />
        </PanelWrapper>
        <PanelWrapper title="INSERT:47 — Stats (с данными)">
          <MockStatementStats />
        </PanelWrapper>
      </div>
      <div style={{ marginBottom: 32 }} />

      {/* ── ROUTINES ───────────────────────────────────────────────────────── */}
      <SectionLabel>ROUTINES &amp; PACKAGES</SectionLabel>
      <div style={{ display: 'flex', gap: 16, marginBottom: 32, flexWrap: 'wrap', alignItems: 'flex-start' }}>
        <PanelWrapper title="DaliRoutine — PROCEDURE (+ SQL tab)">
          <InspectorRoutine data={ROUTINE_DATA} nodeId="#30:1" />
        </PanelWrapper>
        <PanelWrapper title="DaliRoutine — FUNCTION (+ SQL tab)">
          <InspectorRoutine data={FUNCTION_DATA} nodeId="#30:2" />
        </PanelWrapper>
        <PanelWrapper title="DaliPackage (I-02: Routines TODO)">
          <InspectorRoutine data={PACKAGE_DATA} nodeId="#29:1" />
        </PanelWrapper>
      </div>

      {/* ── RECORD ─────────────────────────────────────────────────────────── */}
      <SectionLabel>RECORDS</SectionLabel>
      <div style={{ display: 'flex', gap: 16, marginBottom: 32, flexWrap: 'wrap', alignItems: 'flex-start' }}>
        <PanelWrapper title="DaliRecord (P2: Open in KNOT TODO)">
          <InspectorRecord data={RECORD_DATA} nodeId="#45:123" />
        </PanelWrapper>
      </div>

      {/* ── SCHEMA HIERARCHY ───────────────────────────────────────────────── */}
      <SectionLabel>SCHEMA / DATABASE / APPLICATION</SectionLabel>
      <div style={{ display: 'flex', gap: 16, marginBottom: 32, flexWrap: 'wrap', alignItems: 'flex-start' }}>
        <PanelWrapper title="DaliSchema">
          <InspectorSchema data={SCHEMA_DATA} nodeId="#5:10" />
        </PanelWrapper>
        <PanelWrapper title="DaliDatabase">
          <InspectorSchema data={DATABASE_DATA} nodeId="#4:1" />
        </PanelWrapper>
      </div>

      {/* ── JOIN ───────────────────────────────────────────────────────────── */}
      <SectionLabel>JOIN</SectionLabel>
      <div style={{ display: 'flex', gap: 16, marginBottom: 40, flexWrap: 'wrap', alignItems: 'flex-start' }}>
        <PanelWrapper title="DaliJoin — LEFT JOIN">
          <InspectorJoin data={JOIN_DATA} nodeId="#55:7" />
        </PanelWrapper>
      </div>

    </div>
  );
}

function SectionLabel({ children }: { children: React.ReactNode }) {
  return (
    <div style={{ marginBottom: 6, fontSize: 12, fontWeight: 600, color: 'var(--acc)', letterSpacing: '0.05em' }}>
      {children}
    </div>
  );
}
