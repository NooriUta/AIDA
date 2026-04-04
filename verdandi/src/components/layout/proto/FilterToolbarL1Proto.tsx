// src/components/layout/proto/FilterToolbarL1Proto.tsx
// PROTO-024b: статичный визуальный прототип L1 Filter Toolbar — без логики
// Показывает три состояния на одном экране:
//   1. Нет scope — видны все системы, depth 2
//   2. Scope active — "OrderService" выбран, depth 3, только Upstream
//   3. System-level view ON — скрыты СУБД, только App-ноды
// Утверждается до реализации LOOM-024b.
// Удалить после утверждения.

import type { CSSProperties, ReactNode } from 'react';

// ─── Design tokens ─────────────────────────────────────────────────────────────
const S: Record<string, CSSProperties> = {
  page: {
    minHeight: '100vh',
    background: 'var(--bg1, #141108)',
    padding: '32px 24px',
    display: 'flex',
    flexDirection: 'column',
    gap: 36,
    fontFamily: 'var(--sans, DM Sans, sans-serif)',
  },
  sectionLabel: {
    fontSize: 11,
    color: 'var(--t3, #665c48)',
    letterSpacing: '0.09em',
    textTransform: 'uppercase' as const,
    marginBottom: 8,
  },
  // L1 toolbar is 32px — more compact than L2/L3 (40px)
  toolbar: {
    display: 'flex',
    alignItems: 'center',
    gap: 5,
    height: 32,
    background: 'var(--bg0, #0f0d07)',
    borderBottom: '0.5px solid var(--bd, #2e2819)',
    borderTop: '0.5px solid var(--bd, #2e2819)',
    padding: '0 10px',
    flexShrink: 0,
  },
  sep: {
    width: '0.5px',
    height: 16,
    background: 'var(--bd, #2e2819)',
    flexShrink: 0,
    margin: '0 2px',
  },
  lbl: {
    fontSize: 10,
    color: 'var(--t3, #665c48)',
    whiteSpace: 'nowrap' as const,
    flexShrink: 0,
  },
  // Scope pill — inactive (all systems)
  pill: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: 4,
    padding: '2px 8px',
    borderRadius: 4,
    border: '0.5px solid var(--bd, #2e2819)',
    fontSize: 10,
    color: 'var(--t2, #a89a7a)',
    whiteSpace: 'nowrap' as const,
    flexShrink: 0,
    background: 'transparent',
  },
  // Scope pill — active (scope selected)
  pillActive: {
    borderColor: 'var(--acc, #A8B860)',
    color: 'var(--acc, #A8B860)',
    background: 'rgba(168,184,96,.07)',
  },
  // Depth / system-level button base
  dBtn: {
    padding: '2px 6px',
    borderRadius: 3,
    border: '0.5px solid var(--bd, #2e2819)',
    fontSize: 9,
    color: 'var(--t3, #665c48)',
    cursor: 'pointer',
    background: 'transparent',
    fontFamily: 'var(--sans, DM Sans, sans-serif)',
  },
  dBtnOn: {
    borderColor: 'var(--acc, #A8B860)',
    color: 'var(--acc, #A8B860)',
    background: 'rgba(168,184,96,.08)',
    fontWeight: 600,
  },
  // Direction button base
  dirBtn: {
    padding: '2px 7px',
    borderRadius: 3,
    border: '0.5px solid var(--bd, #2e2819)',
    fontSize: 10,
    color: 'var(--t3, #665c48)',
    cursor: 'pointer',
    background: 'transparent',
    display: 'inline-flex',
    alignItems: 'center',
    gap: 3,
    fontFamily: 'var(--sans, DM Sans, sans-serif)',
  },
  dirBtnUp: {
    borderColor: 'var(--inf, #88B8A8)',
    color: 'var(--inf, #88B8A8)',
    background: 'rgba(136,184,168,.08)',
  },
  dirBtnDown: {
    borderColor: 'var(--wrn, #D4922A)',
    color: 'var(--wrn, #D4922A)',
    background: 'rgba(212,146,42,.08)',
  },
  // System-level toggle
  slBtn: {
    padding: '2px 8px',
    borderRadius: 3,
    border: '0.5px solid var(--bd, #2e2819)',
    fontSize: 9,
    color: 'var(--t3, #665c48)',
    cursor: 'pointer',
    background: 'transparent',
    display: 'inline-flex',
    alignItems: 'center',
    gap: 4,
    whiteSpace: 'nowrap' as const,
    fontFamily: 'var(--sans, DM Sans, sans-serif)',
  },
  slBtnOn: {
    borderColor: 'var(--wrn, #D4922A)',
    color: 'var(--wrn, #D4922A)',
    background: 'rgba(212,146,42,.07)',
  },
  // Level badge (right)
  badge: {
    fontSize: 9,
    padding: '2px 7px',
    border: '0.5px solid var(--bd, #2e2819)',
    borderRadius: 3,
    color: 'var(--t3, #665c48)',
    fontFamily: 'var(--mono, Fira Code, monospace)',
    whiteSpace: 'nowrap' as const,
  },
  spacer: { flex: '1 1 auto' },
};

// ─── Icons ─────────────────────────────────────────────────────────────────────

function IconApp() {
  return (
    <svg width="10" height="10" viewBox="0 0 16 16" fill="none" style={{ flexShrink: 0 }}>
      <rect x="1" y="1" width="6" height="6" rx="1.5" fill="var(--acc,#A8B860)" opacity="0.85" />
      <rect x="9" y="1" width="6" height="6" rx="1.5" fill="var(--acc,#A8B860)" opacity="0.5" />
      <rect x="1" y="9" width="6" height="6" rx="1.5" fill="var(--acc,#A8B860)" opacity="0.5" />
      <rect x="9" y="9" width="6" height="6" rx="1.5" fill="var(--acc,#A8B860)" opacity="0.25" />
    </svg>
  );
}

function IconAppDim() {
  // inactive state — grey squares
  return (
    <svg width="10" height="10" viewBox="0 0 16 16" fill="none" style={{ flexShrink: 0 }}>
      <rect x="1" y="1" width="6" height="6" rx="1.5" fill="var(--t3,#665c48)" opacity="0.85" />
      <rect x="9" y="1" width="6" height="6" rx="1.5" fill="var(--t3,#665c48)" opacity="0.5" />
      <rect x="1" y="9" width="6" height="6" rx="1.5" fill="var(--t3,#665c48)" opacity="0.5" />
      <rect x="9" y="9" width="6" height="6" rx="1.5" fill="var(--t3,#665c48)" opacity="0.25" />
    </svg>
  );
}

function IconLayers({ active }: { active: boolean }) {
  const col = active ? 'var(--wrn,#D4922A)' : 'currentColor';
  return (
    <svg width="10" height="10" viewBox="0 0 12 12" fill="none" style={{ flexShrink: 0 }}>
      <path d="M1 4l5-3 5 3-5 3-5-3z"
        stroke={col} strokeWidth="1.2" strokeLinejoin="round" fill="none" />
      <path d="M1 7l5 3 5-3"
        stroke={col} strokeWidth="1.2" strokeLinecap="round" />
    </svg>
  );
}

// ─── Sub-components ────────────────────────────────────────────────────────────

function Sep() {
  return <div style={S.sep} />;
}

function DepthBtns({ active }: { active: 1 | 2 | 3 | 99 }) {
  const labels: [1 | 2 | 3 | 99, string][] = [[1, '1'], [2, '2'], [3, '3'], [99, '∞']];
  return (
    <div style={{ display: 'flex', gap: 2, flexShrink: 0 }}>
      {labels.map(([d, lbl]) => (
        <button key={d} style={{ ...S.dBtn, ...(active === d ? S.dBtnOn : {}) }}>
          {lbl}
        </button>
      ))}
    </div>
  );
}

function DirBtns({ up, down }: { up: boolean; down: boolean }) {
  return (
    <div style={{ display: 'flex', gap: 3, flexShrink: 0 }}>
      <button style={{ ...S.dirBtn, ...(up ? S.dirBtnUp : {}) }}>↑ Upstream</button>
      <button style={{ ...S.dirBtn, ...(down ? S.dirBtnDown : {}) }}>↓ Downstream</button>
    </div>
  );
}

function Toolbar({ children }: { children: ReactNode }) {
  return <div style={S.toolbar}>{children}</div>;
}

// ─── Canvas mock — показывает эффект scope filter ─────────────────────────────

interface AppBlockProps {
  label: string;
  dbCount: number;
  color: string;
  dimmed?: boolean;
  schemaCount?: number;
}

function AppBlock({ label, dbCount, color, dimmed = false, schemaCount }: AppBlockProps) {
  return (
    <div style={{
      border: `1.5px dashed ${color}`,
      borderRadius: 8,
      padding: '8px 10px',
      minWidth: 130,
      opacity: dimmed ? 0.2 : 1,
      transition: 'opacity 0.15s',
      background: 'rgba(20,17,8,0.5)',
    }}>
      <div style={{ fontSize: 11, fontWeight: 600, color: 'var(--t1,#e8dfc8)', marginBottom: 4 }}>
        {label}
      </div>
      <div style={{ fontSize: 9, color: 'var(--t3,#665c48)' }}>
        {dbCount} DB{schemaCount ? ` · ${schemaCount} schemas` : ''}
      </div>
    </div>
  );
}

function CanvasMock({ children }: { children: ReactNode }) {
  return (
    <div style={{
      background: 'var(--bg1, #141108)',
      border: '0.5px solid var(--bd, #2e2819)',
      borderTop: 'none',
      borderRadius: '0 0 6px 6px',
      padding: '16px 20px',
      display: 'flex',
      gap: 16,
      alignItems: 'flex-start',
      flexWrap: 'wrap' as const,
      minHeight: 80,
    }}>
      {children}
    </div>
  );
}

function Frame({ children }: { children: ReactNode }) {
  return (
    <div style={{ overflowX: 'auto' }}>
      <div style={{
        border: '0.5px solid var(--bd,#2e2819)',
        borderRadius: 6,
        overflow: 'hidden',
        minWidth: 720,
      }}>
        {children}
      </div>
    </div>
  );
}

// ─── State 1 — нет scope, все системы ────────────────────────────────────────

function State1() {
  return (
    <Frame>
      <Toolbar>
        {/* Pill — inactive */}
        <div style={S.pill}>
          <IconAppDim />
          <span>все системы</span>
        </div>

        <Sep />

        <span style={S.lbl}>Depth:</span>
        <DepthBtns active={2} />

        <Sep />

        <DirBtns up down />

        <Sep />

        <button style={S.slBtn}>
          <IconLayers active={false} />
          System-level
        </button>

        <div style={S.spacer} />

        <span style={S.badge}>L1 · depth 2</span>
      </Toolbar>

      <CanvasMock>
        <AppBlock label="OrderService"   dbCount={2} color="var(--acc,#A8B860)" />
        <AppBlock label="ReportingApp"   dbCount={1} color="var(--inf,#88B8A8)" />
        <AppBlock label="Airflow ETL"    dbCount={3} color="var(--wrn,#D4922A)" />
      </CanvasMock>
    </Frame>
  );
}

// ─── State 2 — scope active "OrderService" ───────────────────────────────────

function State2() {
  return (
    <Frame>
      <Toolbar>
        {/* Pill — active */}
        <div style={{ ...S.pill, ...S.pillActive }}>
          <IconApp />
          <span>OrderService</span>
          {/* × clear button */}
          <button style={{
            display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
            width: 14, height: 14,
            border: 'none', background: 'transparent',
            color: 'var(--t3,#665c48)', fontSize: 10,
            cursor: 'pointer', padding: 0, lineHeight: 1,
            marginLeft: 2,
          }}>✕</button>
        </div>

        <Sep />

        <span style={S.lbl}>Depth:</span>
        <DepthBtns active={3} />

        <Sep />

        {/* Only Upstream active */}
        <DirBtns up={true} down={false} />

        <Sep />

        <button style={S.slBtn}>
          <IconLayers active={false} />
          System-level
        </button>

        <div style={S.spacer} />

        <span style={S.badge}>L1 · depth 3</span>
      </Toolbar>

      <CanvasMock>
        {/* OrderService — full brightness (in scope) */}
        <AppBlock label="OrderService"   dbCount={2} color="var(--acc,#A8B860)" schemaCount={5} />
        {/* Others — dimmed */}
        <AppBlock label="ReportingApp"   dbCount={1} color="var(--inf,#88B8A8)" dimmed />
        <AppBlock label="Airflow ETL"    dbCount={3} color="var(--wrn,#D4922A)" dimmed />
      </CanvasMock>
    </Frame>
  );
}

// ─── State 3 — system-level view ON ──────────────────────────────────────────

function State3() {
  return (
    <Frame>
      <Toolbar>
        {/* Pill — active */}
        <div style={{ ...S.pill, ...S.pillActive }}>
          <IconApp />
          <span>OrderService</span>
          <button style={{
            display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
            width: 14, height: 14,
            border: 'none', background: 'transparent',
            color: 'var(--t3,#665c48)', fontSize: 10,
            cursor: 'pointer', padding: 0, lineHeight: 1,
            marginLeft: 2,
          }}>✕</button>
        </div>

        <Sep />

        <span style={S.lbl}>Depth:</span>
        <DepthBtns active={2} />

        <Sep />

        <DirBtns up down />

        <Sep />

        {/* System-level ON */}
        <button style={{ ...S.slBtn, ...S.slBtnOn }}>
          <IconLayers active={true} />
          System-level
        </button>

        <div style={S.spacer} />

        <span style={{ ...S.badge, borderColor: 'var(--wrn,#D4922A)', color: 'var(--wrn,#D4922A)' }}>
          L1 · system-level
        </span>
      </Toolbar>

      {/* Canvas — только App-ноды, DB скрыты */}
      <div style={{
        background: 'var(--bg1, #141108)',
        border: '0.5px solid var(--bd, #2e2819)',
        borderTop: 'none',
        borderRadius: '0 0 6px 6px',
        padding: '16px 20px',
        display: 'flex',
        gap: 16,
        alignItems: 'flex-start',
      }}>
        {/* Только "шапки" App без DB/Schema children */}
        {(['OrderService', 'ReportingApp', 'Airflow ETL'] as const).map((name, i) => {
          const colors = ['var(--acc,#A8B860)', 'var(--inf,#88B8A8)', 'var(--wrn,#D4922A)'];
          const isActive = i === 0;
          return (
            <div key={name} style={{
              border: `1.5px dashed ${colors[i]}`,
              borderRadius: 8,
              padding: '7px 12px',
              minWidth: 110,
              opacity: isActive ? 1 : 0.2,
              background: 'rgba(20,17,8,0.5)',
            }}>
              <div style={{ fontSize: 11, fontWeight: 600, color: 'var(--t1,#e8dfc8)' }}>
                {name}
              </div>
              <div style={{ fontSize: 9, color: 'var(--t3,#665c48)', marginTop: 2 }}>
                — СУБД скрыты —
              </div>
            </div>
          );
        })}
      </div>
    </Frame>
  );
}

// ─── Root export ───────────────────────────────────────────────────────────────

export function FilterToolbarL1Proto() {
  return (
    <div style={S.page}>
      <h2 style={{ color: 'var(--t1,#e8dfc8)', fontSize: 14, margin: 0, letterSpacing: '0.02em' }}>
        PROTO-024b — L1 Filter Toolbar · три состояния
      </h2>

      <div>
        <div style={S.sectionLabel}>State 1 — нет scope · все системы · depth 2 · ↑↓</div>
        <State1 />
      </div>

      <div>
        <div style={S.sectionLabel}>State 2 — scope: OrderService · depth 3 · только ↑ Upstream</div>
        <State2 />
      </div>

      <div>
        <div style={S.sectionLabel}>State 3 — scope: OrderService · system-level ON · СУБД скрыты</div>
        <State3 />
      </div>
    </div>
  );
}
