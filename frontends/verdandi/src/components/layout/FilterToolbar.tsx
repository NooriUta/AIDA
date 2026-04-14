// src/components/layout/FilterToolbar.tsx
// LOOM-023b: Filter Toolbar — L2/L3 filter controls
//
// L2 order: [Schema pill] | [Table ▾] [Stmt ▾] [Column ▾] | [Depth 1 2 3 5 ∞] | [↑ ↓] — [CF] [Table view] [badge]
// L3 order: same but Table/Stmt selects hidden (no table/stmt context)

import { memo, useCallback, useMemo, type ChangeEvent } from 'react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../stores/loomStore';
import { ToolbarDivider, IconLayers, ToolbarToggleButton } from '../ui/ToolbarPrimitives';

// ─── Constants ────────────────────────────────────────────────────────────────
const DEPTH_STEPS = [1, 2, 3, 5, 7, Infinity] as const;
// Default depth = ∞ so sources/sinks load lazily without arbitrary cap —
// user-requested: "не должно быть ограничений подгрузки". The user can
// still click a numeric preset if they want to narrow down.
const DEPTH_DEFAULT = Infinity;

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


/** Column-flow edge icon: two short dashed lines in amber+teal */
function IconCfEdges() {
  return (
    <svg width="16" height="10" viewBox="0 0 16 10" fill="none" style={{ flexShrink: 0 }}>
      <path d="M1 3h14" stroke="#D4922A" strokeWidth="1.2" strokeDasharray="2.5 1.5" strokeLinecap="round" />
      <path d="M1 7h14" stroke="#88B8A8" strokeWidth="1.2" strokeDasharray="2.5 1.5" strokeLinecap="round" />
    </svg>
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
    availableSchemas,
    availableDbs,
    setTableFilter,
    setStmtFilter,
    setFieldFilter,
    setDepth,
    setDirection,
    toggleMappingMode,
    toggleIncludeExternal,
    toggleRoutineAggregate,
    navigateToLevel,
    jumpTo,
    clearFilter,
    requestFitView,
  } = useLoomStore();

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
    includeExternal,
    routineAggregate,
  } = filter;

  // ── Hooks must be called unconditionally (Rules of Hooks) ─────────────────
  const cascadedStmts = useMemo(() => (
    tableFilter
      ? availableStmts.filter((s) => s.connectedTableIds.includes(tableFilter))
      : availableStmts
  ), [availableStmts, tableFilter]);

  const handleTableChange = useCallback(
    (e: ChangeEvent<HTMLSelectElement>) => {
      const id = e.target.value || null;
      setTableFilter(id);
      if (!id) requestFitView();   // cleared → fit all nodes
    },
    [setTableFilter, requestFitView],
  );
  const handleStmtChange = useCallback(
    (e: ChangeEvent<HTMLSelectElement>) => {
      const id = e.target.value || null;
      setStmtFilter(id);
      if (!id) requestFitView();   // cleared → fit all nodes
    },
    [setStmtFilter, requestFitView],
  );
  const handleFieldChange = useCallback(
    (e: ChangeEvent<HTMLSelectElement>) => setFieldFilter(e.target.value || null),
    [setFieldFilter],
  );
  const handleClearFilter = useCallback(() => {
    clearFilter();
    requestFitView();
  }, [clearFilter, requestFitView]);

  // Only show on L2 / L3
  if (viewLevel === 'L1') return null;

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
    || depth !== DEPTH_DEFAULT || !upstream || !downstream;

  const showColumnDropdown = availableColumns.length > 0 || availableFields.length > 0;

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

      {/* ── Start object pill / quick-switcher ────────────────────────────── */}
      {availableSchemas.length > 0 ? (
        /* Schema quick-switcher: select styled as a pill */
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
            maxWidth: 240,
            overflow: 'hidden',
          }}
        >
          <StartIcon />
          <select
            value={availableSchemas.find((s) => s.label === currentScopeLabel)?.id ?? ''}
            onChange={(e) => {
              const s = availableSchemas.find((x) => x.id === e.target.value);
              if (!s) return;
              const db = availableDbs.find((d) => d.id === s.dbId);
              const scope = db
                ? `schema-${s.label}|${db.label}`
                : `schema-${s.label}`;
              jumpTo('L2', scope, s.label, 'DaliSchema');
            }}
            onClick={(e) => e.stopPropagation()}
            style={{
              background:  'transparent',
              border:      'none',
              color:       'var(--t1)',
              fontSize:    12,
              cursor:      'pointer',
              outline:     'none',
              maxWidth:    160,
              fontWeight:  500,
            }}
          >
            {/* Blank option if current scope doesn't match any schema */}
            {!availableSchemas.find((s) => s.label === currentScopeLabel) && (
              <option value="">{scopeLabel}</option>
            )}
            {availableSchemas.map((s) => (
              <option key={s.id} value={s.id}>{s.label}</option>
            ))}
          </select>
          {/* ⇄ back to L1 overview */}
          <span
            onClick={() => navigateToLevel('L1')}
            title={t('toolbar.changeObject')}
            style={{ cursor: 'pointer', color: 'var(--t3)', display: 'flex', alignItems: 'center', flexShrink: 0 }}
          >
            <IconSwap />
          </span>
        </div>
      ) : (
        /* Fallback: static pill (no schema list available) */
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
          <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {scopeLabel}
          </span>
          <span
            onClick={() => navigateToLevel('L1')}
            title={t('toolbar.changeObject')}
            style={{ cursor: 'pointer', color: 'var(--t3)', display: 'flex', alignItems: 'center' }}
          >
            <IconSwap />
          </span>
        </div>
      )}

      <ToolbarDivider />

      {/* ── Table / Stmt selects (L2 only) ────────────────────────────────── */}
      {viewLevel === 'L2' && availableTables.length > 0 && (
        <>
          <span style={{ fontSize: 11, color: 'var(--t3)', flexShrink: 0 }}>{t('toolbar.table')}:</span>
          <select
            aria-label={t('toolbar.table')}
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
            aria-label={t('toolbar.stmt')}
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
          <ToolbarDivider />
        </>
      )}

      {/* ── Column select: cascade from selected table/stmt ───────────────── */}
      {showColumnDropdown && (
        <>
          <span style={{ fontSize: 11, color: 'var(--t3)', flexShrink: 0 }}>{t('toolbar.field')}:</span>
          <select
            aria-label={t('toolbar.field')}
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
          <ToolbarDivider />
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
              aria-pressed={isActive}
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

      <ToolbarDivider />

      {/* ── Direction toggles ──────────────────────────────────────────────── */}
      <ToolbarToggleButton active={upstream} onClick={() => setDirection(!upstream, downstream)}>
        &#x2191; {t('toolbar.upstream')}
      </ToolbarToggleButton>
      <ToolbarToggleButton active={downstream} onClick={() => setDirection(upstream, !downstream)}>
        &#x2193; {t('toolbar.downstream')}
      </ToolbarToggleButton>

      {/* ── External sources toggle — cross-schema READS/WRITES ─────────── */}
      <ToolbarToggleButton
        active={includeExternal}
        onClick={toggleIncludeExternal}
        title={t('inspector.includeExternalHint')}
      >
        &#x21F1; {t('inspector.includeExternal')}
      </ToolbarToggleButton>

      {/* ── Routine-aggregate toggle: AGG (new) ↔ EXP (explore) — L2 only ── */}
      {viewLevel === 'L2' && (
        <ToolbarToggleButton
          active={routineAggregate}
          onClick={toggleRoutineAggregate}
          title={routineAggregate
            ? t('toolbar.routineAggregateHint', 'Switch to detailed exploration mode')
            : t('toolbar.routineExploreHint', 'Switch to routine-aggregate overview')}
        >
          {routineAggregate ? '⊞ AGG' : '⊕ EXP'}
        </ToolbarToggleButton>
      )}

      {/* ── Spacer ─────────────────────────────────────────────────────────── */}
      <div style={{ flex: '1 1 auto' }} />

      {/* ── Mapping mode toggle: Column ↔ Table ─────────────────────────── */}
      <ToolbarToggleButton
        active={!tableLevelView}
        onClick={toggleMappingMode}
        color={tableLevelView ? 'var(--t3)' : 'var(--inf)'}
        title={tableLevelView ? t('toolbar.columnLevelView') : t('toolbar.tableLevelView')}
        style={{ height: 26, padding: '0 8px', borderRadius: 5 }}
      >
        {tableLevelView ? <IconLayers /> : <IconCfEdges />}
        {tableLevelView ? t('toolbar.tableLevelView') : t('toolbar.columnLevelView')}
      </ToolbarToggleButton>

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
        {tableLevelView && <span>· TBL</span>}
      </div>

      {/* ── Clear filter button — visible when any filter is active ────────── */}
      {hasActiveFilter && (
        <button
          onClick={handleClearFilter}
          title={t('toolbar.clearFilter')}
          style={{
            display:      'inline-flex',
            alignItems:   'center',
            justifyContent: 'center',
            width:        20,
            height:       20,
            borderRadius: 4,
            border:       '1px solid var(--acc)',
            background:   'transparent',
            color:        'var(--acc)',
            fontSize:     12,
            cursor:       'pointer',
            flexShrink:   0,
            lineHeight:   1,
            padding:      0,
            transition:   'background 0.1s',
          }}
          onMouseEnter={(e) => { (e.currentTarget as HTMLButtonElement).style.background = 'color-mix(in srgb, var(--acc) 15%, transparent)'; }}
          onMouseLeave={(e) => { (e.currentTarget as HTMLButtonElement).style.background = 'transparent'; }}
        >
          ×
        </button>
      )}

    </div>
  );
});

FilterToolbar.displayName = 'FilterToolbar';
