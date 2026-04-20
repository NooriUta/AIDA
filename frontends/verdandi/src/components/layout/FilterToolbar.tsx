// src/components/layout/FilterToolbar.tsx
// LOOM-023b: Filter Toolbar — L2/L3 filter controls
//
// Row 1 (always 40px): [Schema pill] | [Depth] | [↑↓] [⇱] [AGG/EXP] | → | [view] [badge] [×]
// Row 2: FilterPills — Table / Stmt / Routine / Column
//   Desktop: horizontal row
//   Mobile:  vertical stack (collapsible)

import { memo, useCallback, useMemo, useState, type CSSProperties, type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../stores/loomStore';
import { ToolbarDivider, IconLayers, ToolbarToggleButton } from '../ui/ToolbarPrimitives';
import { useIsMobile } from '../../hooks/useIsMobile';

// ─── Constants ────────────────────────────────────────────────────────────────
const DEPTH_STEPS = [1, 2, 3, 5, 7, Infinity] as const;
const DEPTH_DEFAULT = Infinity;

// ─── Icons ────────────────────────────────────────────────────────────────────
function IconRoutineSmall({ active }: { active: boolean }) {
  const c = active ? 'var(--acc)' : 'var(--t3)';
  return (
    <svg width="10" height="10" viewBox="0 0 12 12" fill="none" style={{ flexShrink: 0 }}>
      <rect x="1" y="1" width="10" height="10" rx="2" stroke={c} strokeWidth="1.2" fill="none" />
      <path d="M3 4h6M3 6h4M3 8h5" stroke={c} strokeWidth="1" strokeLinecap="round" />
    </svg>
  );
}

function IconTableSmall({ active }: { active: boolean }) {
  const c = active ? 'var(--acc)' : 'var(--t3)';
  return (
    <svg width="10" height="10" viewBox="0 0 12 12" fill="none" style={{ flexShrink: 0 }}>
      <rect x="1" y="1" width="10" height="10" rx="2" stroke={c} strokeWidth="1.2" fill="none" />
      <path d="M1 4.5h10M4 4.5v5.5" stroke={c} strokeWidth="1" />
    </svg>
  );
}

function IconStmtSmall({ active }: { active: boolean }) {
  const c = active ? 'var(--acc)' : 'var(--t3)';
  return (
    <svg width="10" height="10" viewBox="0 0 12 12" fill="none" style={{ flexShrink: 0 }}>
      <path d="M2 3h8M2 6h6M2 9h7" stroke={c} strokeWidth="1.3" strokeLinecap="round" />
    </svg>
  );
}

function IconFieldSmall({ active }: { active: boolean }) {
  const c = active ? 'var(--acc)' : 'var(--t3)';
  return (
    <svg width="10" height="10" viewBox="0 0 12 12" fill="none" style={{ flexShrink: 0 }}>
      <rect x="1" y="1" width="10" height="3.5" rx="1" stroke={c} strokeWidth="1.2" fill="none" />
      <rect x="1" y="7.5" width="10" height="3.5" rx="1" stroke={c} strokeWidth="1.2" fill="none" />
    </svg>
  );
}

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

function IconCfEdges() {
  return (
    <svg width="16" height="10" viewBox="0 0 16 10" fill="none" style={{ flexShrink: 0 }}>
      <path d="M1 3h14" stroke="#D4922A" strokeWidth="1.2" strokeDasharray="2.5 1.5" strokeLinecap="round" />
      <path d="M1 7h14" stroke="#88B8A8" strokeWidth="1.2" strokeDasharray="2.5 1.5" strokeLinecap="round" />
    </svg>
  );
}

// ─── FilterPill ───────────────────────────────────────────────────────────────
// Styled pill with icon + invisible select overlay + optional clear button.
// Same visual language as L1's CascadePill.

interface FilterPillProps {
  icon:        ReactNode;
  placeholder: string;
  value:       string;
  options:     { id: string; label: string }[];
  onChange:    (id: string) => void;
  onClear?:    () => void;
  active:      boolean;
  disabled?:   boolean;
  style?:      CSSProperties;
}

function FilterPill({ icon, placeholder, value, options, onChange, onClear, active, disabled, style }: FilterPillProps) {
  const label = options.find((o) => o.id === value)?.label ?? placeholder;
  return (
    <div style={{
      position:     'relative',
      display:      'inline-flex',
      alignItems:   'center',
      gap:          3,
      padding:      '1px 5px',
      borderRadius: 4,
      border:       `0.5px solid ${active ? 'var(--acc)' : 'var(--bd)'}`,
      background:   active ? 'color-mix(in srgb, var(--acc) 7%, transparent)' : 'transparent',
      fontSize:     9,
      color:        active ? 'var(--acc)' : disabled ? 'var(--t3)' : 'var(--t3)',
      whiteSpace:   'nowrap',
      flexShrink:   0,
      cursor:       disabled ? 'not-allowed' : 'pointer',
      opacity:      disabled ? 0.45 : 1,
      ...style,
    }}>
      {icon}
      <span style={{
        overflow:      'hidden',
        textOverflow:  'ellipsis',
        pointerEvents: 'none',
        maxWidth:      100,
        fontFamily:    'var(--mono)',
        fontSize:      9,
      }}>
        {label}
      </span>
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        disabled={disabled}
        style={{ position: 'absolute', inset: 0, opacity: 0, cursor: disabled ? 'not-allowed' : 'pointer', width: '100%' }}
      >
        <option value="">{placeholder}</option>
        {options.map((o) => (
          <option key={o.id} value={o.id}>{o.label}</option>
        ))}
      </select>
      {active && onClear && (
        <button
          onClick={(e) => { e.stopPropagation(); onClear(); }}
          style={{
            position: 'relative', zIndex: 1,
            display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
            width: 11, height: 11,
            border: 'none', background: 'transparent',
            color: 'var(--t3)', fontSize: 8,
            cursor: 'pointer', padding: 0, lineHeight: 1, flexShrink: 0,
          }}
        >✕</button>
      )}
    </div>
  );
}

// ─── FilterToolbar ────────────────────────────────────────────────────────────
export const FilterToolbar = memo(() => {
  const { t } = useTranslation();
  const compact = useIsMobile();
  const [row2Collapsed, setRow2Collapsed] = useState(compact);

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
    availableRoutines,
    setTableFilter,
    setStmtFilter,
    setFieldFilter,
    setRoutineFilter,
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
    routineFilter,
    depth,
    upstream,
    downstream,
    tableLevelView,
    includeExternal,
    routineAggregate,
  } = filter;

  const cascadedStmts = useMemo(() => (
    tableFilter
      ? availableStmts.filter((s) => s.connectedTableIds.includes(tableFilter))
      : availableStmts
  ), [availableStmts, tableFilter]);

  const handleTableChange  = useCallback((id: string) => { setTableFilter(id || null);  if (!id) requestFitView(); }, [setTableFilter, requestFitView]);
  const handleStmtChange   = useCallback((id: string) => { setStmtFilter(id || null);   if (!id) requestFitView(); }, [setStmtFilter,  requestFitView]);
  const handleFieldChange  = useCallback((id: string) => { setFieldFilter(id || null); },  [setFieldFilter]);
  const handleRoutineChange = useCallback((id: string) => { setRoutineFilter(id || null); if (!id) requestFitView(); }, [setRoutineFilter, requestFitView]);

  const handleDepthChange = useCallback(
    (e: React.ChangeEvent<HTMLSelectElement>) => {
      const val = e.target.value;
      setDepth(val === 'Infinity' ? Infinity : Number(val));
    },
    [setDepth],
  );
  const handleClearFilter = useCallback(() => { clearFilter(); requestFitView(); }, [clearFilter, requestFitView]);

  if (viewLevel === 'L1') return null;

  const scopeLabel = startObjectLabel ?? currentScopeLabel ?? viewLevel;

  const depthLabel = depth === Infinity
    ? t('toolbar.depthInfinity')
    : t('toolbar.depthSteps', { n: depth });

  const dirLabel = upstream && downstream ? '↑↓'
    : upstream ? '↑'
    : downstream ? '↓'
    : '—';

  const StartIcon = startObjectType === 'DaliTable' || startObjectType === 'DaliDatabase' || startObjectType === 'DaliSchema'
    ? IconTable
    : startObjectType === 'DaliRoutine' || startObjectType === 'DaliPackage'
    ? IconRoutine
    : IconGeneric;

  const hasActiveFilter = tableFilter !== null || stmtFilter !== null || fieldFilter !== null
    || routineFilter !== null || depth !== DEPTH_DEFAULT || !upstream || !downstream;

  const showTableStmt    = (viewLevel === 'L2' || (viewLevel === 'L3' && !routineAggregate)) && availableTables.length > 0;
  const showRoutines     = viewLevel === 'L2' && availableRoutines.length > 0;
  const showColumnDropdown = availableColumns.length > 0 || availableFields.length > 0;
  const showRow2 = showTableStmt || showRoutines || showColumnDropdown;

  // Desktop pill style — natural width, not stretched
  const pillStyle: CSSProperties = compact
    ? { width: '100%', maxWidth: 'none', boxSizing: 'border-box' }
    : {};

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      flexShrink: 0,
      background: 'var(--bg0)',
      borderBottom: '0.5px solid var(--bd)',
    }}>

      {/* ══ Row 1: Controls ══════════════════════════════════════════════════ */}
      <div style={{
        display: 'flex',
        alignItems: 'center',
        gap: 5,
        height: compact ? 40 : 32,
        padding: '0 10px',
        overflowX: 'auto',
        overflowY: 'hidden',
      }}>

        {/* ── Schema pill / quick-switcher ─────────────────────────────────── */}
        {availableSchemas.length > 0 ? (
          <div
            title={t('toolbar.startObject')}
            style={{
              display: 'inline-flex', alignItems: 'center', gap: 4,
              padding: '1px 6px',
              background: 'transparent',
              border: `0.5px solid ${hasActiveFilter ? 'var(--acc)' : 'var(--bd)'}`,
              borderRadius: 4, fontSize: 10, color: 'var(--t1)',
              flexShrink: 0,
              maxWidth: compact ? 130 : 220,
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
                const scope = db ? `schema-${s.label}|${db.label}` : `schema-${s.label}`;
                jumpTo('L2', scope, s.label, 'DaliSchema');
              }}
              onClick={(e) => e.stopPropagation()}
              style={{
                background: 'transparent', border: 'none',
                color: 'var(--t1)', fontSize: 10, cursor: 'pointer',
                outline: 'none', maxWidth: compact ? 80 : 150, fontWeight: 500,
                fontFamily: 'var(--mono)',
              }}
            >
              {!availableSchemas.find((s) => s.label === currentScopeLabel) && (
                <option value="">{scopeLabel}</option>
              )}
              {availableSchemas.map((s) => (
                <option key={s.id} value={s.id}>{s.label}</option>
              ))}
            </select>
            <span
              onClick={() => navigateToLevel('L1')}
              title={t('toolbar.changeObject')}
              style={{ cursor: 'pointer', color: 'var(--t3)', display: 'flex', alignItems: 'center', flexShrink: 0 }}
            >
              <IconSwap />
            </span>
          </div>
        ) : (
          <div
            title={t('toolbar.startObject')}
            style={{
              display: 'inline-flex', alignItems: 'center', gap: 4,
              padding: '1px 6px',
              background: 'transparent',
              border: `0.5px solid ${hasActiveFilter ? 'var(--acc)' : 'var(--bd)'}`,
              borderRadius: 4, fontSize: 10, color: 'var(--t1)',
              flexShrink: 0, cursor: 'default',
              maxWidth: compact ? 130 : 200, overflow: 'hidden',
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

        {/* ── Depth ──────────────────────────────────────────────────────────── */}
        {compact ? (
          <select
            value={depth === Infinity ? 'Infinity' : String(depth)}
            onChange={handleDepthChange}
            aria-label={t('toolbar.depth')}
            title={t('toolbar.depth')}
            style={{
              background: 'var(--bg3)',
              border: `0.5px solid ${depth !== DEPTH_DEFAULT ? 'var(--acc)' : 'var(--bd)'}`,
              borderRadius: 4,
              color: depth !== DEPTH_DEFAULT ? 'var(--acc)' : 'var(--t2)',
              fontSize: 10, padding: '2px 6px',
              outline: 'none', cursor: 'pointer', flexShrink: 0,
            }}
          >
            {DEPTH_STEPS.map((d) => (
              <option key={String(d)} value={String(d)}>
                {d === Infinity ? '∞' : String(d)}
              </option>
            ))}
          </select>
        ) : (
          <>
            <span style={{ fontSize: 10, color: 'var(--t3)', flexShrink: 0, whiteSpace: 'nowrap' }}>
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
                      padding: '2px 6px', borderRadius: 3,
                      border: `0.5px solid ${isActive ? 'var(--acc)' : 'var(--bd)'}`,
                      background: isActive ? 'color-mix(in srgb, var(--acc) 8%, transparent)' : 'transparent',
                      color: isActive ? 'var(--acc)' : 'var(--t3)',
                      fontSize: 9, cursor: 'pointer',
                      fontWeight: isActive ? 600 : 400,
                      fontFamily: 'var(--font)',
                    }}
                  >
                    {d === Infinity ? '∞' : d}
                  </button>
                );
              })}
            </div>
          </>
        )}

        <ToolbarDivider />

        {/* ── Direction ──────────────────────────────────────────────────────── */}
        <ToolbarToggleButton size="sm" active={upstream}   onClick={() => setDirection(!upstream, downstream)} title={t('toolbar.upstream')}>
          &#x2191;{!compact && <> {t('toolbar.upstream')}</>}
        </ToolbarToggleButton>
        <ToolbarToggleButton size="sm" active={downstream} onClick={() => setDirection(upstream, !downstream)} title={t('toolbar.downstream')}>
          &#x2193;{!compact && <> {t('toolbar.downstream')}</>}
        </ToolbarToggleButton>

        {/* ── External sources ───────────────────────────────────────────────── */}
        <ToolbarToggleButton size="sm" active={includeExternal} onClick={toggleIncludeExternal} title={t('inspector.includeExternalHint')}>
          &#x21F1;{!compact && <> {t('inspector.includeExternal')}</>}
        </ToolbarToggleButton>

        {/* ── Routine-aggregate toggle ──────────────────────────────────────── */}
        {viewLevel === 'L2' && (
          <ToolbarToggleButton
            size="sm"
            active={routineAggregate}
            onClick={toggleRoutineAggregate}
            title={routineAggregate
              ? t('toolbar.routineAggregateHint', 'Switch to detailed exploration mode')
              : t('toolbar.routineExploreHint', 'Switch to aggregate overview')}
          >
            {routineAggregate ? '⊞' : '⊕'}{!compact && <> {routineAggregate ? 'AGG' : 'EXP'}</>}
          </ToolbarToggleButton>
        )}

        {/* ── Spacer ─────────────────────────────────────────────────────────── */}
        <div style={{ flex: '1 1 auto' }} />

        {/* ── Mapping mode ───────────────────────────────────────────────────── */}
        <ToolbarToggleButton
          size="sm"
          active={!tableLevelView}
          onClick={toggleMappingMode}
          color={tableLevelView ? 'var(--t3)' : 'var(--inf)'}
          title={tableLevelView ? t('toolbar.columnLevelView') : t('toolbar.tableLevelView')}
          style={{ padding: '2px 6px' }}
        >
          {tableLevelView ? <IconLayers /> : <IconCfEdges />}
        </ToolbarToggleButton>

        {/* ── Badge (desktop only) ───────────────────────────────────────────── */}
        {!compact && (
          <div style={{
            display: 'inline-flex', alignItems: 'center', gap: 4,
            fontSize: 9, color: 'var(--t3)', flexShrink: 0,
            padding: '2px 7px',
            border: `0.5px solid ${hasActiveFilter ? 'var(--wrn)' : 'var(--bd)'}`,
            borderRadius: 3,
            fontFamily: 'var(--mono)',
          }}>
            <span>{viewLevel}</span>
            {tableFilter   && <span>· &#x229E;</span>}
            {stmtFilter    && <span>· &#x2261;</span>}
            {routineFilter && <span>· ⊡</span>}
            {fieldFilter   && <span>· {fieldFilter}</span>}
            <span>· {depthLabel}</span>
            <span>· {dirLabel}</span>
            {tableLevelView && <span>· TBL</span>}
          </div>
        )}

        {/* ── Row 2 collapse toggle (mobile only) ──────────────────────────── */}
        {compact && showRow2 && (
          <button
            onClick={() => setRow2Collapsed((v) => !v)}
            title={row2Collapsed ? t('toolbar.showFilters') : t('toolbar.hideFilters')}
            style={{
              display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
              width: 20, height: 20, borderRadius: 3, flexShrink: 0,
              border: `0.5px solid ${row2Collapsed && (tableFilter || stmtFilter || fieldFilter || routineFilter) ? 'var(--acc)' : 'var(--bd)'}`,
              background: row2Collapsed && (tableFilter || stmtFilter || fieldFilter || routineFilter)
                ? 'color-mix(in srgb, var(--acc) 15%, transparent)' : 'transparent',
              color: row2Collapsed && (tableFilter || stmtFilter || fieldFilter || routineFilter) ? 'var(--acc)' : 'var(--t3)',
              fontSize: 11, cursor: 'pointer',
              transition: 'border-color 0.1s',
            }}
          >
            {row2Collapsed ? '▾' : '▴'}
          </button>
        )}

        {/* ── Clear filter ───────────────────────────────────────────────────── */}
        {hasActiveFilter && (
          <button
            onClick={handleClearFilter}
            title={t('toolbar.clearFilter')}
            style={{
              display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
              width: 18, height: 18, borderRadius: 3,
              border: '0.5px solid var(--acc)', background: 'transparent',
              color: 'var(--acc)', fontSize: 10, cursor: 'pointer',
              flexShrink: 0, lineHeight: 1, padding: 0, transition: 'background 0.1s',
            }}
            onMouseEnter={(e) => { (e.currentTarget as HTMLButtonElement).style.background = 'color-mix(in srgb, var(--acc) 15%, transparent)'; }}
            onMouseLeave={(e) => { (e.currentTarget as HTMLButtonElement).style.background = 'transparent'; }}
          >
            ×
          </button>
        )}

      </div>

      {/* ══ Row 2: Filter pills ═══════════════════════════════════════════════ */}
      {showRow2 && !(compact && row2Collapsed) && (
        <div style={{
          display:      'flex',
          flexDirection: compact ? 'column' : 'row',
          alignItems:   compact ? 'stretch' : 'center',
          flexWrap:     compact ? 'nowrap' : 'wrap',
          gap:          compact ? 4 : 6,
          padding:      compact ? '4px 10px 5px' : '4px 10px',
          borderTop:    '0.5px solid var(--bd)',
        }}>

          {showRoutines && (
            <FilterPill
              icon={<IconRoutineSmall active={!!routineFilter} />}
              placeholder={t('toolbar.allRoutines', { defaultValue: 'все процедуры' })}
              value={routineFilter ?? ''}
              options={availableRoutines}
              onChange={handleRoutineChange}
              onClear={() => { setRoutineFilter(null); requestFitView(); }}
              active={!!routineFilter}
              style={pillStyle}
            />
          )}

          {showTableStmt && (
            <>
              <FilterPill
                icon={<IconTableSmall active={!!tableFilter} />}
                placeholder={t('toolbar.allTables')}
                value={tableFilter ?? ''}
                options={availableTables}
                onChange={handleTableChange}
                onClear={() => { setTableFilter(null); requestFitView(); }}
                active={!!tableFilter}
                style={pillStyle}
              />
              <FilterPill
                icon={<IconStmtSmall active={!!stmtFilter} />}
                placeholder={t('toolbar.allStmts')}
                value={stmtFilter ?? ''}
                options={cascadedStmts}
                onChange={handleStmtChange}
                onClear={() => { setStmtFilter(null); requestFitView(); }}
                active={!!stmtFilter}
                style={pillStyle}
              />
            </>
          )}

          {showColumnDropdown && (
            <FilterPill
              icon={<IconFieldSmall active={!!fieldFilter} />}
              placeholder={t('toolbar.allColumns')}
              value={fieldFilter ?? ''}
              options={
                availableColumns.length > 0
                  ? availableColumns.map((c) => ({ id: c.name, label: c.name }))
                  : availableFields.map((f) => ({ id: f, label: f }))
              }
              onChange={handleFieldChange}
              onClear={() => setFieldFilter(null)}
              active={!!fieldFilter}
              disabled={tableLevelView}
              style={pillStyle}
            />
          )}

        </div>
      )}

    </div>
  );
});

FilterToolbar.displayName = 'FilterToolbar';
