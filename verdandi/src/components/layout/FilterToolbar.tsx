// src/components/layout/FilterToolbar.tsx
// LOOM-023b: Filter Toolbar — L2/L3 filter controls
//
// L2 order: [Schema pill] | [Table ▾] [Stmt ▾] [Column ▾] | [Depth 1 2 3 5 ∞] | [↑ ↓] — [CF] [Table view] [badge]
// L3 order: same but Table/Stmt selects hidden (no table/stmt context)

import { memo, useCallback, useMemo, type ChangeEvent } from 'react';
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

/** Column-flow edge icon: two short dashed lines in amber+teal */
function IconCfEdges() {
  return (
    <svg width="16" height="10" viewBox="0 0 16 10" fill="none" style={{ flexShrink: 0 }}>
      <path d="M1 3h14" stroke="#D4922A" strokeWidth="1.2" strokeDasharray="2.5 1.5" strokeLinecap="round" />
      <path d="M1 7h14" stroke="#88B8A8" strokeWidth="1.2" strokeDasharray="2.5 1.5" strokeLinecap="round" />
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
    availableTables,
    availableStmts,
    availableColumns,
    setTableFilter,
    setStmtFilter,
    setFieldFilter,
    setDepth,
    setDirection,
    toggleTableLevelView,
    toggleCfEdges,
    clearFilter,
    navigateToLevel,
  } = useLoomStore();

  // Only show on L2 / L3
  if (viewLevel === 'L1') return null;

  const {
    startObjectLabel,
    startObjectType,
    tableFilter,
    stmtFilter,
    fieldFilter,
    depth,
    upstream,
    downstream,
    tableLevelView,
    showCfEdges,
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

  const hasActiveFilter = tableFilter !== null || stmtFilter !== null || fieldFilter !== null
    || depth !== Infinity || !upstream || !downstream;

  // ── Stmt options — cascade: filter by selected table ─────────────────────
  const cascadedStmts = useMemo(() => (
    tableFilter
      ? availableStmts.filter((s) => s.connectedTableIds.includes(tableFilter))
      : availableStmts
  ), [availableStmts, tableFilter]);

  const showColumnDropdown = availableColumns.length > 0 || availableFields.length > 0;

  const handleTableChange = useCallback(
    (e: ChangeEvent<HTMLSelectElement>) => setTableFilter(e.target.value || null),
    [setTableFilter],
  );
  const handleStmtChange = useCallback(
    (e: ChangeEvent<HTMLSelectElement>) => setStmtFilter(e.target.value || null),
    [setStmtFilter],
  );
  const handleFieldChange = useCallback(
    (e: ChangeEvent<HTMLSelectElement>) => setFieldFilter(e.target.value || null),
    [setFieldFilter],
  );

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

      {/* ── Table / Stmt selects (L2 only) ────────────────────────────────── */}
      {viewLevel === 'L2' && availableTables.length > 0 && (
        <>
          <span style={{ fontSize: 11, color: 'var(--t3)', flexShrink: 0 }}>{t('toolbar.table')}:</span>
          <select
            value={tableFilter ?? ''}
            onChange={handleTableChange}
            style={{
              background: 'var(--bg3)',
              border: `1px solid ${tableFilter ? 'var(--acc)' : 'var(--bd)'}`,
              borderRadius: 6,
              color: tableFilter ? 'var(--acc)' : 'var(--t2)',
              fontSize: 12,
              padding: '3px 8px',
              outline: 'none',
              cursor: 'pointer',
              maxWidth: 160,
            }}
          >
            <option value="">{t('toolbar.allTables')}</option>
            {availableTables.map((tbl) => (
              <option key={tbl.id} value={tbl.id}>{tbl.label}</option>
            ))}
          </select>

          <span style={{ fontSize: 11, color: 'var(--t3)', flexShrink: 0 }}>{t('toolbar.stmt')}:</span>
          <select
            value={stmtFilter ?? ''}
            onChange={handleStmtChange}
            style={{
              background: 'var(--bg3)',
              border: `1px solid ${stmtFilter ? 'var(--acc)' : 'var(--bd)'}`,
              borderRadius: 6,
              color: stmtFilter ? 'var(--acc)' : 'var(--t2)',
              fontSize: 12,
              padding: '3px 8px',
              outline: 'none',
              cursor: 'pointer',
              maxWidth: 160,
            }}
          >
            <option value="">{t('toolbar.allStmts')}</option>
            {cascadedStmts.map((s) => (
              <option key={s.id} value={s.id}>{s.label}</option>
            ))}
          </select>
          <Divider />
        </>
      )}

      {/* ── Column select: cascade from selected table/stmt ───────────────── */}
      {showColumnDropdown && (
        <>
          <span style={{ fontSize: 11, color: 'var(--t3)', flexShrink: 0 }}>{t('toolbar.field')}:</span>
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
            {availableColumns.length > 0
              ? availableColumns.map((c) => <option key={c.name} value={c.name}>{c.name}</option>)
              : availableFields.map((f) => <option key={f} value={f}>{f}</option>)
            }
          </select>
          <Divider />
        </>
      )}

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
        &#x2191; {t('toolbar.upstream')}
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
        &#x2193; {t('toolbar.downstream')}
      </button>

      {/* ── Spacer ─────────────────────────────────────────────────────────── */}
      <div style={{ flex: '1 1 auto' }} />

      {/* ── Column-flow edge toggle ────────────────────────────────────────── */}
      <button
        onClick={toggleCfEdges}
        title={showCfEdges ? t('toolbar.hideCfEdges') : t('toolbar.showCfEdges')}
        style={{
          display: 'inline-flex', alignItems: 'center', gap: 5,
          height: 26, padding: '0 8px',
          borderRadius: 5,
          border: `1px solid ${showCfEdges ? 'var(--inf)' : 'var(--bd)'}`,
          background: showCfEdges ? 'var(--bg3)' : 'transparent',
          color: showCfEdges ? 'var(--inf)' : 'var(--t3)',
          fontSize: 11, cursor: 'pointer', flexShrink: 0,
          transition: 'border-color 0.1s, color 0.1s',
          opacity: tableLevelView ? 0.35 : 1,  // dim when tableLevelView overrides
        }}
        disabled={tableLevelView}
      >
        <IconCfEdges />
        CF
      </button>

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
        {tableFilter && <span>· &#x229E;</span>}
        {stmtFilter  && <span>· &#x2261;</span>}
        {fieldFilter && <span>· {fieldFilter}</span>}
        <span>· {depthLabel}</span>
        <span>· {dirLabel}</span>
        {tableLevelView && <span>· &#x229F;</span>}
        {!showCfEdges   && <span>· ~&#x2205;</span>}
      </div>

    </div>
  );
});

FilterToolbar.displayName = 'FilterToolbar';
