/**
 * ToolbarPrimitives — shared toolbar building blocks for KNOT and canvas panels.
 *
 * Replaces duplicated inline FilterBar / PageButton / ToolbarInput patterns in:
 *   - KnotAtoms.tsx
 *   - KnotStatements.tsx  (routine select)
 *   - SearchPanel.tsx     (scrollable type-filter tabs)
 */

import { useRef, useState, useEffect, useCallback, type ReactNode } from 'react';

// ── ToolbarFilterBar ──────────────────────────────────────────────────────────
// Horizontally laid-out toggle-button group. One value active at a time.

export interface FilterOption<T extends string> {
  value: T;
  label: string;
  /** Optional accent colour when inactive. Active always uses var(--acc). */
  color?: string;
}

export function ToolbarFilterBar<T extends string>({
  options,
  value,
  onChange,
}: {
  options: FilterOption<T>[];
  value: T;
  onChange: (v: T) => void;
}) {
  return (
    <div style={{ display: 'flex', gap: 2 }}>
      {options.map((opt) => (
        <button
          key={opt.value}
          onClick={() => onChange(opt.value)}
          style={{
            padding: '3px 8px', borderRadius: 4, fontSize: 10,
            border: '1px solid var(--bd)', cursor: 'pointer',
            fontFamily: 'inherit',
            background: value === opt.value ? 'var(--acc)' : 'var(--bg3)',
            color: value === opt.value ? 'var(--bg0)' : (opt.color ?? 'var(--t2)'),
            transition: 'background 0.1s',
          }}
        >
          {opt.label}
        </button>
      ))}
    </div>
  );
}

// ── ToolbarScrollTabs ─────────────────────────────────────────────────────────
// Scrollable tab bar used in SearchPanel for type filters (many options).
// Supports multiple active values (Set<T>).

export function ToolbarScrollTabs<T extends string>({
  tabs,
  activeKeys,
  onToggle,
}: {
  tabs: { key: T; label: string }[];
  activeKeys: Set<T> | 'all';
  onToggle: (key: T) => void;
}) {
  const ref = useRef<HTMLDivElement>(null);
  const [canScrollLeft, setCanScrollLeft]   = useState(false);
  const [canScrollRight, setCanScrollRight] = useState(false);

  const checkScroll = useCallback(() => {
    const el = ref.current;
    if (!el) return;
    setCanScrollLeft(el.scrollLeft > 0);
    setCanScrollRight(el.scrollLeft + el.clientWidth < el.scrollWidth - 1);
  }, []);

  useEffect(() => {
    checkScroll();
    const el = ref.current;
    if (!el) return;
    el.addEventListener('scroll', checkScroll, { passive: true });
    const ro = new ResizeObserver(checkScroll);
    ro.observe(el);
    return () => { el.removeEventListener('scroll', checkScroll); ro.disconnect(); };
  }, [checkScroll]);

  const scroll = (dir: 1 | -1) => {
    ref.current?.scrollBy({ left: dir * 80, behavior: 'smooth' });
  };

  return (
    <div style={{ display: 'flex', alignItems: 'center', flexShrink: 0, position: 'relative' }}>
      {canScrollLeft && (
        <button onClick={() => scroll(-1)} style={scrollBtnStyle('left')}>«</button>
      )}
      <div
        ref={ref}
        style={{ display: 'flex', gap: 2, padding: '2px 4px', flex: 1, overflowX: 'auto', scrollbarWidth: 'none' }}
      >
        {tabs.map((tab) => {
          const active = activeKeys === 'all' ? tab.key === 'all' as T : activeKeys.has(tab.key);
          return (
            <button
              key={tab.key}
              onClick={() => onToggle(tab.key)}
              style={{
                padding: '2px 7px', borderRadius: 4, border: 'none', cursor: 'pointer',
                fontSize: 10, fontWeight: active ? 600 : 400, whiteSpace: 'nowrap', flexShrink: 0,
                background: active ? 'var(--acc)' : 'transparent',
                color: active ? 'var(--bg1)' : 'var(--t3)',
                transition: 'background 0.1s, color 0.1s',
              }}
            >
              {tab.label}
            </button>
          );
        })}
      </div>
      {canScrollRight && (
        <button onClick={() => scroll(1)} style={scrollBtnStyle('right')}>»</button>
      )}
    </div>
  );
}

const scrollBtnStyle = (side: 'left' | 'right'): React.CSSProperties => ({
  flexShrink: 0, padding: '2px 5px', border: 'none', cursor: 'pointer',
  fontSize: 10, color: 'var(--t3)', position: 'absolute',
  [side]: 0, top: 0, bottom: 0, zIndex: 1,
  display: 'flex', alignItems: 'center',
  background: `linear-gradient(to ${side === 'left' ? 'left' : 'right'}, transparent, var(--bg2) 40%)`,
});

// ── ToolbarInput ──────────────────────────────────────────────────────────────
// Styled text search input (dark-theme aware).

export function ToolbarInput({
  value, onChange, placeholder,
}: {
  value: string;
  onChange: (v: string) => void;
  placeholder?: string;
}) {
  return (
    <input
      type="text"
      value={value}
      placeholder={placeholder}
      onChange={(e) => onChange(e.target.value)}
      style={{
        padding: '4px 10px', borderRadius: 4, fontSize: 11,
        border: '1px solid var(--bd)', background: 'var(--bg3)',
        color: 'var(--t1)', outline: 'none', flex: '1 1 160px', minWidth: 120,
        fontFamily: 'inherit',
      }}
    />
  );
}

// ── ToolbarSelect ─────────────────────────────────────────────────────────────
// Styled <select> dropdown (routine picker in KnotStatements).

export function ToolbarSelect({
  value, onChange, options,
}: {
  value: string;
  onChange: (v: string) => void;
  options: { value: string; label: string }[];
}) {
  return (
    <select
      value={value}
      onChange={(e) => onChange(e.target.value)}
      style={{
        background: 'var(--bg2)', border: '1px solid var(--bd)',
        color: 'var(--t1)', padding: '5px 8px', borderRadius: 5,
        fontSize: 12, outline: 'none', fontFamily: "'DM Sans', sans-serif", cursor: 'pointer',
      }}
    >
      {options.map((opt) => (
        <option key={opt.value} value={opt.value}>{opt.label}</option>
      ))}
    </select>
  );
}

// ── ToolbarCount ──────────────────────────────────────────────────────────────
// "filtered / total" count label.

export function ToolbarCount({ filtered, total }: { filtered: number; total: number }) {
  return (
    <span style={{ fontSize: 10, color: 'var(--t3)', whiteSpace: 'nowrap' }}>
      {filtered.toLocaleString()} / {total.toLocaleString()}
    </span>
  );
}

// ── PageButton ────────────────────────────────────────────────────────────────
// Pagination prev/next button.

export function PageButton({
  disabled, onClick, children,
}: {
  disabled: boolean; onClick: () => void; children: ReactNode;
}) {
  return (
    <button
      disabled={disabled}
      onClick={onClick}
      style={{
        padding: '2px 8px', borderRadius: 4, fontSize: 12,
        border: '1px solid var(--bd)', cursor: disabled ? 'default' : 'pointer',
        background: 'var(--bg3)', color: disabled ? 'var(--t3)' : 'var(--t1)',
        fontFamily: 'inherit',
      }}
    >
      {children}
    </button>
  );
}
