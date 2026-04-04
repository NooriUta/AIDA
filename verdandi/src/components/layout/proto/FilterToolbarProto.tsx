// src/components/layout/proto/FilterToolbarProto.tsx
// PROTO: статичный визуальный прототип Filter Toolbar — без логики
// Показывает три состояния рядом на одном экране:
//   1. Toolbar без фильтра (только start object pill)
//   2. Toolbar с активным фильтром поля (total_amount)
//   3. Toolbar в table-level режиме
// Утверждается до реализации LOOM-023b.
// Удалить или переработать после утверждения.

import type { CSSProperties, ReactNode } from 'react';

// ─── Design-system tokens (Amber Forest dark) ─────────────────────────────────
const S: Record<string, CSSProperties> = {
  page: {
    minHeight: '100vh',
    background: 'var(--bg1, #141108)',
    padding: '32px 24px',
    display: 'flex',
    flexDirection: 'column',
    gap: 32,
  },
  label: {
    fontSize: 11,
    color: 'var(--t3, #665c48)',
    letterSpacing: '0.1em',
    textTransform: 'uppercase' as const,
    marginBottom: 8,
  },
  toolbar: {
    display: 'flex',
    alignItems: 'center',
    gap: 8,
    height: 40,
    background: 'var(--bg2, #1c1810)',
    border: '1px solid var(--bd, #2e2819)',
    borderRadius: 8,
    padding: '0 12px',
  },
  divider: {
    width: 1,
    height: 20,
    background: 'var(--bd, #2e2819)',
    flexShrink: 0,
  },
  pill: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: 6,
    background: 'var(--bg3, #24201a)',
    border: '1px solid var(--bd, #2e2819)',
    borderRadius: 6,
    padding: '3px 8px',
    fontSize: 12,
    color: 'var(--t1, #e8dfc8)',
    cursor: 'default',
    flexShrink: 0,
  },
  pillActive: {
    borderColor: 'var(--acc, #A8B860)',
  },
  label12: {
    fontSize: 11,
    color: 'var(--t3, #665c48)',
    flexShrink: 0,
  },
  select: {
    background: 'var(--bg3, #24201a)',
    border: '1px solid var(--bd, #2e2819)',
    borderRadius: 6,
    color: 'var(--t1, #e8dfc8)',
    fontSize: 12,
    padding: '3px 8px',
    outline: 'none',
    cursor: 'pointer',
  },
  selectActive: {
    borderColor: 'var(--acc, #A8B860)',
    color: 'var(--acc, #A8B860)',
  },
  btn: {
    display: 'inline-flex',
    alignItems: 'center',
    justifyContent: 'center',
    minWidth: 28,
    height: 26,
    padding: '0 8px',
    borderRadius: 5,
    border: '1px solid var(--bd, #2e2819)',
    background: 'transparent',
    color: 'var(--t2, #a89a7a)',
    fontSize: 12,
    cursor: 'pointer',
    flexShrink: 0,
  },
  btnActive: {
    background: 'var(--bg3, #24201a)',
    borderColor: 'var(--acc, #A8B860)',
    color: 'var(--acc, #A8B860)',
  },
  btnDanger: {
    color: 'var(--wrn, #D4922A)',
    borderColor: 'transparent',
    background: 'transparent',
  },
  badge: {
    display: 'inline-flex',
    alignItems: 'center',
    gap: 4,
    fontSize: 11,
    color: 'var(--t3, #665c48)',
    marginLeft: 'auto',
    flexShrink: 0,
  },
  spacer: {
    flex: '1 1 auto',
  },
};

// ─── Utility ──────────────────────────────────────────────────────────────────
function Divider() {
  return <div style={S.divider} />;
}

function IconRoutine() {
  return (
    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
      <rect x="1" y="1" width="10" height="10" rx="2"
        stroke="var(--acc,#A8B860)" strokeWidth="1.2" fill="none" />
      <path d="M3 4h6M3 6h4M3 8h5" stroke="var(--acc,#A8B860)"
        strokeWidth="1" strokeLinecap="round" />
    </svg>
  );
}

function IconTable() {
  return (
    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
      <rect x="1" y="1" width="10" height="10" rx="2"
        stroke="var(--inf,#88B8A8)" strokeWidth="1.2" fill="none" />
      <path d="M1 4.5h10M4 4.5v5.5" stroke="var(--inf,#88B8A8)"
        strokeWidth="1" />
    </svg>
  );
}

function IconSwap() {
  return (
    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
      <path d="M2 4h8M8 2l2 2-2 2M10 8H2M4 6l-2 2 2 2"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round"
        strokeLinejoin="round" />
    </svg>
  );
}

function IconLayers() {
  return (
    <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
      <path d="M1 4l5-3 5 3-5 3-5-3z" stroke="currentColor"
        strokeWidth="1.2" strokeLinejoin="round" fill="none" />
      <path d="M1 7l5 3 5-3" stroke="currentColor"
        strokeWidth="1.2" strokeLinecap="round" />
    </svg>
  );
}

// ─── Depth buttons ─────────────────────────────────────────────────────────────
const DEPTHS = [1, 2, 3, 5, Infinity] as const;

function DepthButtons({ active }: { active: number }) {
  return (
    <div style={{ display: 'flex', gap: 3 }}>
      {DEPTHS.map((d) => (
        <button
          key={d}
          style={{
            ...S.btn,
            ...(active === d ? S.btnActive : {}),
          }}
        >
          {d === Infinity ? '∞' : d}
        </button>
      ))}
    </div>
  );
}

// ─── Direction toggle ──────────────────────────────────────────────────────────
function DirectionToggle({ up, down }: { up: boolean; down: boolean }) {
  return (
    <div style={{ display: 'flex', gap: 3 }}>
      <button style={{ ...S.btn, ...(up ? S.btnActive : {}) }}>↑ Upstream</button>
      <button style={{ ...S.btn, ...(down ? S.btnActive : {}) }}>↓ Downstream</button>
    </div>
  );
}

// ─── ToolbarWrapper ────────────────────────────────────────────────────────────
function ToolbarWrapper({ children }: { children: ReactNode }) {
  return <div style={S.toolbar}>{children}</div>;
}

// ─── State 1: No active filter ────────────────────────────────────────────────
function ToolbarEmpty() {
  return (
    <ToolbarWrapper>
      {/* Start object pill */}
      <div style={S.pill}>
        <IconRoutine />
        <span>calc_order_revenue()</span>
        <span style={{ color: 'var(--t3)' }}><IconSwap /></span>
      </div>

      <Divider />

      {/* Field select */}
      <span style={S.label12}>Field:</span>
      <select style={S.select} defaultValue="">
        <option value="">all columns</option>
        <option>total_amount</option>
        <option>order_id</option>
        <option>customer_id</option>
      </select>

      <Divider />

      {/* Depth */}
      <span style={S.label12}>Depth:</span>
      <DepthButtons active={Infinity} />

      <Divider />

      {/* Direction */}
      <DirectionToggle up down />

      <div style={S.spacer} />

      {/* Table-level toggle (off) */}
      <button style={S.btn}>
        <IconLayers /> &nbsp;Table-level view
      </button>

      {/* Level badge */}
      <div style={S.badge}>L2 · ∞ steps</div>
    </ToolbarWrapper>
  );
}

// ─── State 2: Field filter active (total_amount) ──────────────────────────────
function ToolbarWithField() {
  return (
    <ToolbarWrapper>
      {/* Start object pill */}
      <div style={{ ...S.pill, ...S.pillActive }}>
        <IconRoutine />
        <span>calc_order_revenue()</span>
        <span style={{ color: 'var(--t3)' }}><IconSwap /></span>
      </div>

      <Divider />

      {/* Field select — active */}
      <span style={S.label12}>Field:</span>
      <select style={{ ...S.select, ...S.selectActive }} defaultValue="total_amount">
        <option value="">all columns</option>
        <option value="total_amount">total_amount</option>
        <option>order_id</option>
        <option>customer_id</option>
      </select>
      {/* Clear field button */}
      <button style={{ ...S.btn, ...S.btnDanger }} title="Clear filter">✕</button>

      <Divider />

      {/* Depth */}
      <span style={S.label12}>Depth:</span>
      <DepthButtons active={2} />

      <Divider />

      {/* Direction */}
      <DirectionToggle up down />

      <div style={S.spacer} />

      {/* Table-level toggle (off) */}
      <button style={S.btn}>
        <IconLayers /> &nbsp;Table-level view
      </button>

      {/* Level badge */}
      <div style={S.badge}>L2 · total_amount · 2 steps</div>
    </ToolbarWrapper>
  );
}

// ─── State 3: Table-level view active ─────────────────────────────────────────
function ToolbarTableLevel() {
  return (
    <ToolbarWrapper>
      {/* Start object pill */}
      <div style={S.pill}>
        <IconTable />
        <span>orders</span>
        <span style={{ color: 'var(--t3)' }}><IconSwap /></span>
      </div>

      <Divider />

      {/* Field select — disabled in table-level */}
      <span style={{ ...S.label12, opacity: 0.4 }}>Field:</span>
      <select style={{ ...S.select, opacity: 0.4 }} disabled defaultValue="">
        <option value="">all columns</option>
      </select>

      <Divider />

      {/* Depth */}
      <span style={S.label12}>Depth:</span>
      <DepthButtons active={3} />

      <Divider />

      {/* Direction */}
      <DirectionToggle up={false} down />

      <div style={S.spacer} />

      {/* Table-level toggle — ACTIVE */}
      <button style={{ ...S.btn, ...S.btnActive }}>
        <IconLayers /> &nbsp;Table-level view
      </button>

      {/* Level badge */}
      <div style={S.badge}>L2 · table view · ↓ · 3 steps</div>
    </ToolbarWrapper>
  );
}

// ─── Export ────────────────────────────────────────────────────────────────────
export function FilterToolbarProto() {
  return (
    <div style={S.page}>
      <h2 style={{ color: 'var(--t1,#e8dfc8)', fontSize: 14, margin: 0 }}>
        PROTO-023b — Filter Toolbar · три состояния
      </h2>

      {/* State 1 */}
      <div>
        <div style={S.label}>State 1 — no active filter</div>
        <ToolbarEmpty />
      </div>

      {/* State 2 */}
      <div>
        <div style={S.label}>State 2 — field filter: total_amount · depth 2</div>
        <ToolbarWithField />
      </div>

      {/* State 3 */}
      <div>
        <div style={S.label}>State 3 — table-level view · start object: orders · downstream · depth 3</div>
        <ToolbarTableLevel />
      </div>
    </div>
  );
}
