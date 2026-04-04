// src/components/layout/FilterToolbar.tsx
// LOOM-023b: Filter Toolbar — connects to loomStore filter state
// Visible only on L2 and L3 (when a scope is active).
// Layout: [Start pill] | [Field select] [✕] | [Depth 1 2 3 5 ∞] | [↑ ↓] — [Table view] [badge]

import { memo, useCallback, type ChangeEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../stores/loomStore';

// ─── Constants ────────────────────────────────────────────────────────────────
const DEPTH_STEPS = [1, 2, 3, 5, Infinity] as const;

// ─── Icons (inline SVG — no dependency) ──────────────────────────────────────
function IconRoutine() {
  return (
    <svg width="11" height="11" viewBox="0 0 12 12" fill="none" style={{ flexShrink: 0 }}>
      <rect x="1" y="1" width="10" height="10" rx="2"
        stroke="var(--acc)" strokeWidth="1.2" fill="none" />
      <path d="M3 4h6M3 6h4M3 8h5" stroke="var(--acc)"
        strokeWidth="1" strokeLinecap="round" />
    </svg>
  );
}

function IconTable() {
  return (
    <svg width="11" height="11" viewBox="0 0 12 12" fill="none" style={{ flexShrink: 0 }}>
      <rect x="1" y="1" width="10" height="10" rx="2"
        stroke="var(--inf)" strokeWidth="1.2" fill="none" />
      <path d="M1 4.5h10M4 4.5v5.5" stroke="var(--inf)" strokeWidth="1" />
    </svg>
  );
}

function IconGeneric() {
  return (
    <svg width="11" height="11" viewBox="0 0 12 12" fill="none" style={{ flexShrink: 0 }}>
      <circle cx="6" cy="6" r="4.5" stroke="var(--t2)" strokeWidth="1.2" fill="none" />
      <path d="M4 6h4M6 4v4" stroke="var(--t2)" strokeWidth="1" strokeLinecap="round" />
    </svg>
  );
}

function IconSwap() {
  return (
    <svg width="10" height="10" viewBox="0 0 12 12" fill="none" style={{ flexShrink: 0 }}>
      <path d="M2 4h8M8 2l2 2-2 2M10 8H2M4 6l-2 2 2 2"
        stroke="var(--t3)" strokeWidth="1.2"
        strokeLinecap="round" strokeLinejoin="round" />
    </svg>
  );
}

function IconLayers() {
  return (
    <svg width="11" height="11" viewBox="0 0 12 12" fill="none" style={{ flexShrink: 0 }}>
      <path d="M1 4l5-3 5 3-5 3-5-3z" stroke="currentColor"
        strokeWidth="1.2" strokeLinejoin="round" fill="none" />
      <path d="M1 7l5 3 5-3" stroke="currentColor"
        strokeWidth="1.2" strokeLinecap="round" />
    </svg>
  );
}

// ─── Divider ──────────────────────────────────────────────────────────────────
function Divider() {
  return (
    <div style={{
      width: 1, height: 20,
      background: 'var(--bd)',
      flexShrink: 0, margin: '0 2px',
    }} />
  );
}

// ─── FilterToolbar ────────────────────────────────────────────────────────────
export const FilterToolbar = memo(() => {
  const { t } = useTranslation();
  const {
    viewLevel,
    currentScopeLabel,
    filter,
    availableFields,
    setFieldFilter,
    setDepth,
    setDirection,
    toggleTableLevelView,
    clearFilter,
    navigateToLevel,
  } = useLoomStore();

  // Only show on L2 / L3
  if (viewLevel === 'L1') return null;

  const {
    startObjectLabel,
    startObjectType,
    fieldFilter,
    depth,
    upstream,
    downstream,
    tableLevelView,
  } = filter;

  const scopeLabel = startObjectLabel ?? currentScopeLabel ?? viewLevel;

  // Depth label for badge
  const depthLabel = depth === Infinity
    ? t('toolbar.depthInfinity')
    : t('toolbar.depthSteps', { n: depth });

  // Direction label for badge
  const dirLabel = upstream && downstream ? '↑↓'
    : upstream ? '↑'
    : downstream ? '↓'
    : '—';

  // Pick icon by node type
  const StartIcon = startObjectType === 'DaliTable' || startObjectType === 'DaliDatabase' || startObjectType === 'DaliSchema'
    ? IconTable
    : startObjectType === 'DaliRoutine' || startObjectType === 'DaliPackage'
    ? IconRoutine
    : IconGeneric;

  const handleFieldChange = useCallback(
    (e: ChangeEvent<HTMLSelectElement>) => {
      setFieldFilter(e.target.value || null);
    },
    [setFieldFilter],
  );

  const hasActiveFilter = fieldFilter !== null || depth !== Infinity || !upstream || !downstream;

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      gap: 6,
      height: 40,
      flexShrink: 0,
      padding: '0 12px',
      background: 'var(--bg2)',
      borderBottom: '1px solid var(--bd)',
      overflow: 'hidden',
    }}>

      {/* ── Start object pill ──────────────────────────────────────────────── */}
      <div
        title={t('toolbar.startObject')}
        style={{
          display: 'inline-flex',
          alignItems: 'center',
          gap: 5,
          padding: '3px 8px',
          background: 'var(--bg3)',
          border: `1px solid ${hasActiveFilter ? 'var(--acc)' : 'var(--bd)'}`,
          borderRadius: 6,
          fontSize: 12,
          color: 'var(--t1)',
          flexShrink: 0,
          cursor: 'default',
          maxWidth: 200,
          overflow: 'hidden',
        }}
      >
        <StartIcon />
        <span style={{
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          whiteSpace: 'nowrap',
        }}>
          {scopeLabel}
        </span>
        {/* ⇄ navigate back to L1 to pick different scope */}
        <span
          onClick={() => navigateToLevel('L1')}
          title={t('toolbar.changeObject')}
          style={{ cursor: 'pointer', color: 'var(--t3)', display: 'flex', alignItems: 'center' }}
        >
          <IconSwap />
        </span>
      </div>

      <Divider />

      {/* ── Field select ───────────────────────────────────────────────────── */}
      <span style={{ fontSize: 11, color: 'var(--t3)', flexShrink: 0 }}>
        {t('toolbar.field')}:
      </span>
      <select
        value={fieldFilter ?? ''}
        onChange={handleFieldChange}
        disabled={tableLevelView}
        style={{
          background: 'var(--bg3)',
          border: `1px solid ${fieldFilter ? 'var(--acc)' : 'var(--bd)'}`,
          borderRadius: 6,
          color: fieldFilter ? 'var(--acc)' : 'var(--t2)',
          fontSize: 12,
          padding: '3px 8px',
          outline: 'none',
          cursor: tableLevelView ? 'not-allowed' : 'pointer',
          opacity: tableLevelView ? 0.4 : 1,
          maxWidth: 160,
        }}
      >
        <option value="">{t('toolbar.allColumns')}</option>
        {availableFields.map((f) => (
          <option key={f} value={f}>{f}</option>
        ))}
      </select>

      {/* ✕ clear field filter */}
      {fieldFilter && (
        <button
          onClick={() => setFieldFilter(null)}
          title={t('toolbar.clearFilter')}
          style={{
            display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
            width: 20, height: 20,
            border: 'none',
            background: 'transparent',
            color: 'var(--wrn)',
            fontSize: 13,
            cursor: 'pointer',
            borderRadius: 4,
            padding: 0,
            flexShrink: 0,
          }}
        >
          ✕
        </button>
      )}

      <Divider />

      {/* ── Depth buttons ──────────────────────────────────────────────────── */}
      <span style={{ fontSize: 11, color: 'var(--t3)', flexShrink: 0 }}>
        {t('toolbar.depth')}:
      </span>
      <div style={{ display: 'flex', gap: 2, flexShrink: 0 }}>
        {DEPTH_STEPS.map((d) => {
          const isActive = d === depth;
          return (
            <button
              key={String(d)}
              onClick={() => setDepth(d)}
              style={{
                display: 'inline-flex',
                alignItems: 'center',
                justifyContent: 'center',
                minWidth: 24, height: 24,
                padding: '0 5px',
                borderRadius: 4,
                border: `1px solid ${isActive ? 'var(--acc)' : 'var(--bd)'}`,
                background: isActive ? 'var(--bg3)' : 'transparent',
                color: isActive ? 'var(--acc)' : 'var(--t3)',
                fontSize: 11,
                cursor: 'pointer',
                fontWeight: isActive ? 600 : 400,
                transition: 'border-color 0.1s, color 0.1s',
              }}
            >
              {d === Infinity ? '∞' : d}
            </button>
          );
        })}
      </div>

      <Divider />

      {/* ── Direction toggles ──────────────────────────────────────────────── */}
      <button
        onClick={() => setDirection(!upstream, downstream)}
        style={{
          display: 'inline-flex', alignItems: 'center', gap: 3,
          height: 24, padding: '0 7px',
          borderRadius: 4,
          border: `1px solid ${upstream ? 'var(--acc)' : 'var(--bd)'}`,
          background: upstream ? 'var(--bg3)' : 'transparent',
          color: upstream ? 'var(--acc)' : 'var(--t3)',
          fontSize: 11, cursor: 'pointer', flexShrink: 0,
          transition: 'border-color 0.1s, color 0.1s',
        }}
      >
        ↑ {t('toolbar.upstream')}
      </button>
      <button
        onClick={() => setDirection(upstream, !downstream)}
        style={{
          display: 'inline-flex', alignItems: 'center', gap: 3,
          height: 24, padding: '0 7px',
          borderRadius: 4,
          border: `1px solid ${downstream ? 'var(--acc)' : 'var(--bd)'}`,
          background: downstream ? 'var(--bg3)' : 'transparent',
          color: downstream ? 'var(--acc)' : 'var(--t3)',
          fontSize: 11, cursor: 'pointer', flexShrink: 0,
          transition: 'border-color 0.1s, color 0.1s',
        }}
      >
        ↓ {t('toolbar.downstream')}
      </button>

      {/* ── Spacer ─────────────────────────────────────────────────────────── */}
      <div style={{ flex: '1 1 auto' }} />

      {/* ── Table-level view toggle ────────────────────────────────────────── */}
      <button
        onClick={toggleTableLevelView}
        title={tableLevelView ? t('toolbar.columnLevelView') : t('toolbar.tableLevelView')}
        style={{
          display: 'inline-flex', alignItems: 'center', gap: 5,
          height: 26, padding: '0 8px',
          borderRadius: 5,
          border: `1px solid ${tableLevelView ? 'var(--acc)' : 'var(--bd)'}`,
          background: tableLevelView ? 'var(--bg3)' : 'transparent',
          color: tableLevelView ? 'var(--acc)' : 'var(--t2)',
          fontSize: 11, cursor: 'pointer', flexShrink: 0,
          transition: 'border-color 0.1s, color 0.1s',
        }}
      >
        <IconLayers />
        {tableLevelView ? t('toolbar.columnLevelView') : t('toolbar.tableLevelView')}
      </button>

      {/* ── Level + filter badge ────────────────────────────────────────────── */}
      <div style={{
        display: 'inline-flex', alignItems: 'center', gap: 4,
        fontSize: 10,
        color: 'var(--t3)',
        flexShrink: 0,
        marginLeft: 4,
      }}>
        <span>{viewLevel}</span>
        {fieldFilter && <span>· {fieldFilter}</span>}
        <span>· {depthLabel}</span>
        <span>· {dirLabel}</span>
        {tableLevelView && <span>· ⊞</span>}
      </div>

    </div>
  );
});

FilterToolbar.displayName = 'FilterToolbar';
