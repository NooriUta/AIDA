// src/components/canvas/nodes/proto/L1NodesProto.tsx
// PROTO-024 v2 — L1 grouped architecture
//
// ApplicationNode = RF group parent (dashed border container)
//   └── DatabaseNode  = RF child node (parentId: appId)
//         └── Schema chips = inline in DatabaseNode (not separate RF nodes)
//
// Separate nodes: ETL pipeline (orange), standalone
// Edges: ETL → App (dashed orange), USES_DATABASE (dashed --t3)

import { useState, type CSSProperties, type ReactNode } from 'react';

// ─── Design tokens ─────────────────────────────────────────────────────────────
const C = {
  b0: '#141108', b1: '#1c1810', b2: '#252019', b3: '#302a20',
  bd: '#42382a', bdh: '#584c38',
  t1: '#ede5d0', t2: '#9a8c6e', t3: '#665c48',
  acc: '#A8B860',   // green
  inf: '#88B8A8',   // teal
  wrn: '#D4922A',   // amber
  suc: '#7DBF78',   // light-green
  etl: '#c87f3c',   // orange
};

// ─── Icons ─────────────────────────────────────────────────────────────────────
function IconApp({ color }: { color: string }) {
  return (
    <div style={{
      width: 22, height: 22, borderRadius: 4,
      background: 'rgba(0,0,0,0.22)',
      display: 'flex', alignItems: 'center', justifyContent: 'center',
      flexShrink: 0,
    }}>
      <svg width="12" height="12" viewBox="0 0 16 16" fill="none">
        <rect x="1"  y="1"  width="6" height="6" rx="1.5" fill={color} opacity="0.8" />
        <rect x="9"  y="1"  width="6" height="6" rx="1.5" fill={color} opacity="0.5" />
        <rect x="1"  y="9"  width="6" height="6" rx="1.5" fill={color} opacity="0.5" />
        <rect x="9"  y="9"  width="6" height="6" rx="1.5" fill={color} opacity="0.25" />
      </svg>
    </div>
  );
}

function IconDb({ color }: { color: string }) {
  return (
    <svg width="11" height="11" viewBox="0 0 16 16" fill="none" style={{ flexShrink: 0 }}>
      <ellipse cx="8" cy="4" rx="6" ry="2"
        stroke={color} strokeWidth="1.3" fill="none" />
      <path d="M2 4v8c0 1.1 2.69 2 6 2s6-.9 6-2V4"
        stroke={color} strokeWidth="1.3" fill="none" />
      <path d="M2 8c0 1.1 2.69 2 6 2s6-.9 6-2"
        stroke={color} strokeWidth="1" opacity="0.5" />
    </svg>
  );
}

function IconWorkflow() {
  return (
    <svg width="12" height="12" viewBox="0 0 24 24" fill="none"
      stroke={C.etl} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"
      style={{ flexShrink: 0 }}>
      <rect x="3"  y="3"  width="6" height="6" rx="1" />
      <rect x="15" y="3"  width="6" height="6" rx="1" />
      <rect x="9"  y="15" width="6" height="6" rx="1" />
      <path d="M9 6h6M6 9v3a3 3 0 0 0 3 3h6a3 3 0 0 0 3-3V9" />
    </svg>
  );
}

// ─── Schema chip ───────────────────────────────────────────────────────────────
function SchemaChip({ name, color, onClick }: {
  name: string; color: string; onClick?: () => void;
}) {
  const [hovered, setHovered] = useState(false);
  return (
    <span
      style={{
        display: 'inline-flex', alignItems: 'center', gap: 3,
        padding: '2px 5px', borderRadius: 3, cursor: 'pointer',
        border: `0.5px dashed ${hovered ? C.bdh : C.bd}`,
        background: C.b1,
        fontSize: 9, color: hovered ? C.t1 : C.t2, fontFamily: 'monospace',
        whiteSpace: 'nowrap', transition: 'color .1s, border-color .1s',
      }}
      onClick={onClick}
      onMouseEnter={() => setHovered(true)}
      onMouseLeave={() => setHovered(false)}
    >
      <span style={{ width: 4, height: 4, borderRadius: '50%', background: color, flexShrink: 0 }} />
      {name}
    </span>
  );
}

// ─── Database card ─────────────────────────────────────────────────────────────
function DbCard({
  name, engine, tableCount, shared, color, schemas, dim,
}: {
  name: string; engine: string; tableCount?: number; shared?: boolean;
  color: string; schemas?: string[]; dim?: boolean;
}) {
  const [open, setOpen] = useState(false);
  return (
    <div style={{
      border: `0.5px solid ${C.bd}`,
      borderLeft: `3px solid ${color}`,
      borderRadius: 5, background: C.b2,
      margin: '0 8px 6px',
      opacity: dim ? 0.35 : 1,
      transition: 'opacity .18s',
    }}>
      {/* Header */}
      <div style={{
        padding: '5px 8px', display: 'flex', alignItems: 'center', gap: 5,
        borderBottom: '0.5px solid var(--bd)',
      }}>
        <IconDb color={color} />
        <span style={{ fontSize: 11, fontWeight: 600, color: C.t1, flex: 1, fontFamily: 'monospace', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
          {name}
        </span>
        {tableCount !== undefined && (
          <span style={{ fontSize: 9, color: C.t3, fontFamily: 'monospace', flexShrink: 0 }}>{tableCount}</span>
        )}
        {shared && (
          <span style={{
            fontSize: 8, padding: '1px 4px', borderRadius: 2,
            background: 'rgba(168,184,96,0.08)', border: '0.5px solid rgba(168,184,96,0.3)',
            color: C.acc, flexShrink: 0,
          }}>shared</span>
        )}
      </div>

      {/* Footer */}
      <div style={{ padding: '3px 8px 4px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        <span style={{ fontSize: 9, color: C.t3, fontFamily: 'monospace' }}>{engine}</span>
        {schemas && schemas.length > 0 && (
          <button
            style={{
              fontSize: 9, color: open ? color : C.t2, padding: '1px 5px',
              borderRadius: 3, border: `0.5px solid ${open ? color + '60' : C.bd}`,
              background: 'transparent', cursor: 'pointer', fontFamily: 'sans-serif',
              lineHeight: 1.4, transition: 'color .1s, border-color .1s',
            }}
            onClick={() => setOpen(v => !v)}
          >
            схемы {open ? '↑' : '↓'}
          </button>
        )}
      </div>

      {/* Schema RF nodes (stacked, full-width — each is an l1SchemaNode in real app) */}
      {open && schemas && schemas.length > 0 && (
        <div style={{ padding: '5px 4px 5px', borderTop: `0.5px solid ${C.bd}`, display: 'flex', flexDirection: 'column', gap: 2 }}>
          {schemas.map(s => (
            <div key={s} style={{
              display: 'flex', alignItems: 'center', gap: 4,
              padding: '0 5px', height: 20, borderRadius: 3,
              border: `0.5px dashed ${C.bd}`, background: C.b1,
              cursor: 'pointer', overflow: 'hidden',
              fontSize: 9, color: C.t2, fontFamily: 'monospace',
              // mini RF node indicator — left/right handles simulated
              position: 'relative',
            }}>
              {/* Handle left (simulated) */}
              <div style={{ position: 'absolute', left: -4, top: '50%', transform: 'translateY(-50%)', width: 5, height: 5, borderRadius: '50%', background: color, opacity: 0.55 }} />
              <span style={{ width: 4, height: 4, borderRadius: '50%', background: color, flexShrink: 0 }} />
              <span style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{s}</span>
              {/* Handle right (simulated) */}
              <div style={{ position: 'absolute', right: -4, top: '50%', transform: 'translateY(-50%)', width: 5, height: 5, borderRadius: '50%', background: color, opacity: 0.55 }} />
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

// ─── Application group ─────────────────────────────────────────────────────────
function AppGroup({
  name, color, metaText, dim, selected, children,
}: {
  name: string; color: string; metaText?: string;
  dim?: boolean; selected?: boolean;
  children: ReactNode;
}) {
  return (
    <div style={{
      border: `1.5px dashed ${selected ? color : color + '70'}`,
      borderRadius: 8,
      background: 'rgba(20,17,8,0.55)',
      opacity: dim ? 0.15 : 1,
      transition: 'opacity .18s, border-color .15s',
    }}>
      {/* Header */}
      <div style={{
        padding: '7px 10px 6px',
        borderBottom: `0.5px solid ${C.bd}`,
        borderRadius: '6px 6px 0 0',
        display: 'flex', alignItems: 'center', gap: 7,
        background: selected ? `${color}14` : 'transparent',
      }}>
        <IconApp color={color} />
        <span style={{ fontWeight: 600, fontSize: 12, color: C.t1, flex: 1 }}>{name}</span>
        <span style={{
          fontSize: 8, padding: '1px 5px', borderRadius: 2,
          fontFamily: 'monospace', border: `0.5px solid ${color}40`,
          color, opacity: 0.7,
        }}>App</span>
      </div>

      {/* Meta */}
      {metaText && (
        <div style={{ fontSize: 9, color: C.t3, padding: '3px 10px 0', marginBottom: 4 }}>
          {metaText}
        </div>
      )}

      {/* DB children */}
      {children}

      <div style={{ height: 2 }} />
    </div>
  );
}

// ─── ETL node ──────────────────────────────────────────────────────────────────
function EtlNode({ name, reads, writes }: { name: string; reads: string[]; writes: string[] }) {
  return (
    <div style={{
      border: `0.5px solid ${C.etl}`,
      borderLeft: `3px solid ${C.etl}`,
      borderRadius: 6, background: C.b3, width: 130,
    }}>
      <div style={{
        padding: '5px 8px', borderBottom: `0.5px solid ${C.etl}40`,
        display: 'flex', alignItems: 'center', gap: 5,
      }}>
        <IconWorkflow />
        <span style={{ fontSize: 11, fontWeight: 600, color: C.t1, flex: 1 }}>{name}</span>
        <span style={{
          fontSize: 8, padding: '1px 4px', borderRadius: 2,
          background: 'rgba(200,127,60,0.1)', border: `0.5px solid rgba(200,127,60,0.35)`,
          color: C.etl, fontFamily: 'monospace',
        }}>ETL</span>
      </div>
      <div style={{ padding: '4px 8px 5px' }}>
        {reads.map(r  => <div key={r} style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 9, color: C.t3, margin: '2px 0' }}>
          <span style={{ width: 5, height: 5, borderRadius: '50%', background: C.acc, flexShrink: 0 }} />{r}
        </div>)}
        {writes.map(w => <div key={w} style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: 9, color: C.t3, margin: '2px 0' }}>
          <span style={{ width: 5, height: 5, borderRadius: '50%', background: C.wrn, flexShrink: 0 }} />{w}
        </div>)}
      </div>
    </div>
  );
}

// ─── USES reference row inside an app ──────────────────────────────────────────
function UsesRef({ name }: { name: string }) {
  return (
    <div style={{
      margin: '0 8px 6px',
      border: `0.5px dashed ${C.t3}`,
      borderRadius: 5, padding: '5px 8px',
      display: 'flex', alignItems: 'center', gap: 5,
      opacity: 0.75,
    }}>
      <IconDb color={C.t3} />
      <span style={{ fontSize: 10, color: C.t3, fontFamily: 'monospace', flex: 1 }}>{name}</span>
      <span style={{
        fontSize: 8, padding: '1px 4px', borderRadius: 2,
        background: 'rgba(101,92,72,.15)', border: `0.5px solid ${C.t3}`,
        color: C.t3, fontFamily: 'monospace',
      }}>USES</span>
    </div>
  );
}

// ─── Section ───────────────────────────────────────────────────────────────────
function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <div>
      <div style={{ fontSize: 11, color: C.t3, letterSpacing: '0.1em', textTransform: 'uppercase', marginBottom: 16 }}>
        {title}
      </div>
      {children}
    </div>
  );
}

const label: CSSProperties = { fontSize: 10, color: C.t3, marginBottom: 6 };

// ─── Canvas mock with SVG edges ────────────────────────────────────────────────
function CanvasMock({ scopeApp }: { scopeApp: string | null }) {
  const apps: Record<string, { color: string; label: string }> = {
    s1: { color: C.acc, label: 'OrderSystem' },
    s2: { color: C.inf, label: 'CRMSystem' },
    s3: { color: C.wrn, label: 'DataWarehouse' },
  };

  const isActive = (appId: string) => scopeApp === null || scopeApp === appId;

  return (
    <div style={{ position: 'relative', width: 780, height: 280 }}>
      {/* Dot grid */}
      <div style={{
        position: 'absolute', inset: 0,
        backgroundImage: `radial-gradient(circle, ${C.bd} 1px, transparent 1px)`,
        backgroundSize: '18px 18px', opacity: 0.35, pointerEvents: 'none', borderRadius: 8,
      }} />

      {/* SVG edges */}
      <svg style={{ position: 'absolute', inset: 0, overflow: 'visible', pointerEvents: 'none' }}
        width="780" height="280">
        <defs>
          <marker id="a-etl" viewBox="0 0 10 10" refX="8" refY="5"
            markerWidth="5" markerHeight="5" orient="auto-start-reverse">
            <path d="M2 1L8 5L2 9" fill="none" stroke={C.etl} strokeWidth="1.5"
              strokeLinecap="round" strokeLinejoin="round" />
          </marker>
          <marker id="a-uses" viewBox="0 0 10 10" refX="8" refY="5"
            markerWidth="5" markerHeight="5" orient="auto-start-reverse">
            <path d="M2 1L8 5L2 9" fill="none" stroke={C.t3} strokeWidth="1.5"
              strokeLinecap="round" strokeLinejoin="round" />
          </marker>
        </defs>

        {/* ETL → OrderSystem */}
        <path d="M498,42 C560,8 600,8 628,42"
          stroke={C.etl} strokeWidth="1.5" strokeDasharray="5 2.5" fill="none"
          opacity={!scopeApp || scopeApp === 's1' ? 0.9 : 0.08}
          markerEnd="url(#a-etl)" />
        <text x="555" y="14" fontSize="8" fontFamily="monospace" fill={C.etl}
          textAnchor="middle" opacity={!scopeApp || scopeApp === 's1' ? 0.8 : 0.05}>reads</text>

        {/* ETL → DataWarehouse */}
        <path d="M498,58 L535,58"
          stroke={C.etl} strokeWidth="1.5" strokeDasharray="5 2.5" fill="none"
          opacity={!scopeApp || scopeApp === 's3' ? 0.9 : 0.08}
          markerEnd="url(#a-etl)" />

        {/* USES_DATABASE: CRMSystem → orders_db in OrderSystem */}
        <path d="M258,80 C280,80 300,68 628,68"
          stroke={C.t3} strokeWidth="1.2" strokeDasharray="4 3" fill="none"
          opacity={!scopeApp || scopeApp === 's1' || scopeApp === 's2' ? 0.85 : 0.05}
          markerEnd="url(#a-uses)" />
        <text x="440" y="74" fontSize="8" fontFamily="monospace" fill={C.t3}
          textAnchor="middle" opacity={!scopeApp || scopeApp === 's1' || scopeApp === 's2' ? 0.85 : 0.05}>USES</text>
      </svg>

      {/* ETL node (standalone, not inside any app) */}
      <div style={{
        position: 'absolute', left: 360, top: 28,
        opacity: scopeApp && scopeApp !== 's1' && scopeApp !== 's3' ? 0.12 : 1,
        transition: 'opacity .18s',
      }}>
        <EtlNode
          name="Airflow"
          reads={['reads OrderSystem']}
          writes={['writes DataWarehouse']}
        />
      </div>

      {/* s1: OrderSystem */}
      <div style={{ position: 'absolute', left: 10, top: 20 }}>
        <AppGroup name="OrderSystem" color={C.acc} metaText="2 СУБД"
          dim={!isActive('s1')} selected={scopeApp === 's1'}>
          <DbCard name="orders_db" engine="PostgreSQL" tableCount={20} shared
            color={C.acc}
            schemas={['public','reporting','staging','audit','archive','raw','processed','analytics']} />
          <DbCard name="analytics_db" engine="ClickHouse" tableCount={3}
            color={C.acc}
            schemas={['analytics','metrics_daily','aggregates']} />
        </AppGroup>
      </div>

      {/* s2: CRMSystem */}
      <div style={{ position: 'absolute', left: 258, top: 20 }}>
        <AppGroup name="CRMSystem" color={C.inf} metaText="1 СУБД"
          dim={!isActive('s2')} selected={scopeApp === 's2'}>
          <DbCard name="crm_db" engine="PostgreSQL" tableCount={4}
            color={C.inf}
            schemas={['customers','campaigns','support','audit_log']} />
          <UsesRef name="orders_db" />
        </AppGroup>
      </div>

      {/* s3: DataWarehouse */}
      <div style={{ position: 'absolute', left: 535, top: 20 }}>
        <AppGroup name="DataWarehouse" color={C.wrn} metaText="1 СУБД"
          dim={!isActive('s3')} selected={scopeApp === 's3'}>
          <DbCard name="dwh_db" engine="Redshift" tableCount={8}
            color={C.wrn}
            schemas={['fact_sales','fact_orders','dim_customer','dim_product','staging','mart']} />
        </AppGroup>
      </div>
    </div>
  );
}

// ─── Main export ───────────────────────────────────────────────────────────────
export function L1NodesProto() {
  const [scopeApp, setScopeApp] = useState<string | null>(null);

  return (
    <div style={{
      minHeight: '100vh', background: C.b1,
      padding: '32px 28px', display: 'flex', flexDirection: 'column', gap: 44,
      fontFamily: 'system-ui, sans-serif', color: C.t1,
    }}>
      <div>
        <h2 style={{ fontSize: 14, fontWeight: 500, margin: '0 0 4px' }}>
          PROTO-024 v2 — L1: Application (группа) → СУБД (дочерняя нода) → Схемы (чипы)
        </h2>
        <p style={{ fontSize: 11, color: C.t3, margin: 0 }}>
          ApplicationNode = RF group parent (dashed border) ·
          DatabaseNode = parentId child ·
          Schema = inline chip → drill-down L2
        </p>
      </div>

      {/* ── 1. Компоненты ── */}
      <Section title="1 · Компоненты L1">
        <div style={{ display: 'flex', gap: 28, flexWrap: 'wrap', alignItems: 'flex-start' }}>

          {/* Application node */}
          <div>
            <div style={label}>ApplicationNode · RF group parent</div>
            <div style={{ width: 220 }}>
              <AppGroup name="OrderSystem" color={C.acc} metaText="2 СУБД">
                <DbCard name="orders_db" engine="PostgreSQL" tableCount={20} shared
                  color={C.acc}
                  schemas={['public','reporting','staging','audit']} />
                <DbCard name="analytics_db" engine="ClickHouse" tableCount={3}
                  color={C.acc}
                  schemas={['analytics','metrics_daily']} />
              </AppGroup>
            </div>
          </div>

          {/* Database node standalone */}
          <div>
            <div style={label}>DatabaseNode · parentId child</div>
            <div style={{ width: 204 }}>
              <DbCard name="orders_db" engine="PostgreSQL" tableCount={20} shared
                color={C.acc}
                schemas={['public','reporting','staging','audit','archive','raw']} />
            </div>
          </div>

          {/* Schema chip */}
          <div>
            <div style={label}>Schema chip · click → drill-down L2</div>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4, maxWidth: 180 }}>
              {['public','reporting','staging','audit','raw','analytics'].map(s => (
                <SchemaChip key={s} name={s} color={C.acc} />
              ))}
            </div>
          </div>

          {/* ETL node */}
          <div>
            <div style={label}>ETL node · standalone RF node</div>
            <EtlNode
              name="Airflow"
              reads={['reads OrderSystem']}
              writes={['writes DataWarehouse']}
            />
          </div>
        </div>
      </Section>

      {/* ── 2. Цвета по типу системы ── */}
      <Section title="2 · Цветовая кодировка систем">
        <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap' }}>
          {[
            { name: 'OrderSystem',    color: C.acc, meta: 'Сервис заказов' },
            { name: 'CRMSystem',      color: C.inf, meta: 'CRM' },
            { name: 'DataWarehouse',  color: C.wrn, meta: 'Хранилище данных' },
            { name: 'BillingSystem',  color: C.suc, meta: 'Биллинг' },
            { name: 'ETL Pipeline',   color: C.etl, meta: 'Airflow / Spark' },
          ].map(({ name, color, meta }) => (
            <div key={name} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <div style={{ width: 10, height: 10, borderRadius: 2, background: color }} />
              <div>
                <div style={{ fontSize: 11, color: C.t1, fontWeight: 500 }}>{name}</div>
                <div style={{ fontSize: 9, color: C.t3 }}>{meta}</div>
              </div>
            </div>
          ))}
        </div>
        <div style={{ display: 'flex', gap: 24, marginTop: 20, flexWrap: 'wrap' }}>
          {[
            { stroke: C.t3,  dash: '4 2.5', label: 'USES_DATABASE — ссылка между приложениями' },
            { stroke: C.etl, dash: '5 2.5', label: 'ETL pipeline → приложение' },
          ].map(({ stroke, dash, label: lbl }) => (
            <div key={lbl} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <svg width="32" height="6" style={{ flexShrink: 0 }}>
                <line x1="0" y1="3" x2="32" y2="3"
                  stroke={stroke} strokeWidth="1.5" strokeDasharray={dash} />
              </svg>
              <span style={{ fontSize: 10, color: C.t2 }}>{lbl}</span>
            </div>
          ))}
        </div>
      </Section>

      {/* ── 3. Граф с рёбрами ── */}
      <Section title="3 · Полный граф L1 с ETL и USES_DATABASE">
        <CanvasMock scopeApp={null} />
      </Section>

      {/* ── 4. Scope filter ── */}
      <Section title="4 · Scope filter — click на приложении">
        <div style={{ display: 'flex', gap: 8, marginBottom: 14, flexWrap: 'wrap' }}>
          <button
            style={{
              fontSize: 10, padding: '3px 10px', borderRadius: 4,
              border: `0.5px solid ${!scopeApp ? C.acc : C.bd}`,
              color: !scopeApp ? C.acc : C.t3,
              background: !scopeApp ? `${C.acc}10` : 'transparent',
              cursor: 'pointer', fontFamily: 'sans-serif',
            }}
            onClick={() => setScopeApp(null)}
          >
            все системы
          </button>
          {[
            { id: 's1', label: 'OrderSystem', color: C.acc },
            { id: 's2', label: 'CRMSystem',   color: C.inf },
            { id: 's3', label: 'DataWarehouse', color: C.wrn },
          ].map(({ id, label: lbl, color }) => (
            <button
              key={id}
              style={{
                fontSize: 10, padding: '3px 10px', borderRadius: 4,
                border: `0.5px solid ${scopeApp === id ? color : C.bd}`,
                color: scopeApp === id ? color : C.t3,
                background: scopeApp === id ? `${color}10` : 'transparent',
                cursor: 'pointer', fontFamily: 'sans-serif',
                transition: 'color .12s, border-color .12s',
              }}
              onClick={() => setScopeApp(v => v === id ? null : id)}
            >
              ⊙ {lbl}
            </button>
          ))}
        </div>
        <CanvasMock scopeApp={scopeApp} />
      </Section>

      {/* ── 5. Состояния нод ── */}
      <Section title="5 · Состояния нод">
        <div style={{ display: 'flex', gap: 16, flexWrap: 'wrap', alignItems: 'flex-start' }}>

          {/* App states */}
          {[
            { l: 'App · normal',   selected: false, dim: false  },
            { l: 'App · selected', selected: true,  dim: false  },
            { l: 'App · dim',      selected: false, dim: true   },
          ].map(({ l, selected, dim }) => (
            <div key={l}>
              <div style={label}>{l}</div>
              <div style={{ width: 200 }}>
                <AppGroup name="OrderSystem" color={C.acc} metaText="2 СУБД"
                  selected={selected} dim={dim}>
                  <DbCard name="orders_db" engine="PostgreSQL" tableCount={20}
                    color={C.acc} schemas={['public','reporting']} />
                </AppGroup>
              </div>
            </div>
          ))}

          {/* DB states */}
          {[
            { l: 'DB · normal',   dim: false },
            { l: 'DB · dim',      dim: true  },
          ].map(({ l, dim }) => (
            <div key={l}>
              <div style={label}>{l}</div>
              <div style={{ width: 200 }}>
                <DbCard name="orders_db" engine="PostgreSQL" tableCount={20}
                  color={C.acc} schemas={['public','reporting','staging']} dim={dim} />
              </div>
            </div>
          ))}
        </div>
      </Section>

      {/* ── 6. Легенда ReactFlow ── */}
      <Section title="6 · Архитектура ReactFlow (3 уровня вложенности)">
        <div style={{
          background: C.b2, border: `0.5px solid ${C.bd}`, borderRadius: 6,
          padding: '12px 16px', maxWidth: 490,
        }}>
          {[
            { icon: '□', color: C.acc, text: 'ApplicationNode — RF group parent (type: applicationNode, style.width/height)' },
            { icon: '▪', color: C.t2,  text: 'DatabaseNode — { parentId: appId, extent: "parent" } — "схемы ↓" dispatches toggleDbExpansion' },
            { icon: '⬚', color: C.t3,  text: 'L1SchemaNode — { parentId: dbId, extent: "parent", hidden: true } — revealed by applyL1Layout' },
            { icon: '◇', color: C.etl, text: 'ETL / pipeline node — standalone RF node, Handle left/right' },
            { icon: '╌', color: C.t3,  text: 'USES_DATABASE — RF Edge (dashed, db-handle → db-handle)' },
            { icon: '╌', color: C.etl, text: 'ETL edge — connects ETL node to ApplicationNode or L1SchemaNode' },
          ].map(({ icon, color, text }) => (
            <div key={text} style={{ display: 'flex', gap: 10, marginBottom: 8, alignItems: 'flex-start' }}>
              <span style={{ color, fontSize: 12, width: 16, flexShrink: 0, lineHeight: 1.3 }}>{icon}</span>
              <span style={{ fontSize: 10, color: C.t2, lineHeight: 1.5 }}>{text}</span>
            </div>
          ))}
        </div>
        <div style={{ marginTop: 10, fontSize: 10, color: C.t3, maxWidth: 490, lineHeight: 1.6 }}>
          applyL1Layout(nodes, expandedDbs) пересчитывает позиции DatabaseNode и высоту ApplicationNode
          при каждом expand/collapse, обеспечивая динамическое вложение схем без ELK.
        </div>
      </Section>

    </div>
  );
}
