// src/components/layout/FilterToolbarL1.tsx
// LOOM-024b: L1 Filter Toolbar — single 32px row.
//
// Layout:
//   [Scope pill] | [Depth 1 2 3 ∞] | [↑ ↓] | [⊞ System] | [App→DB→Schema] [× clear]  ···  [Badge]

import { memo, type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../stores/loomStore';
import { ToolbarDivider, IconLayers, ToolbarToggleButton } from '../ui/ToolbarPrimitives';

const DEPTHS = [1, 2, 3, 99] as const;
type L1Depth = typeof DEPTHS[number];

// ─── Icons ────────────────────────────────────────────────────────────────────

function IconApp({ active }: { active: boolean }) {
  const fill = active ? 'var(--acc)' : 'var(--t3)';
  return (
    <svg width="10" height="10" viewBox="0 0 16 16" fill="none" style={{ flexShrink: 0 }}>
      <rect x="1" y="1" width="6" height="6" rx="1.5" fill={fill} opacity="0.85" />
      <rect x="9" y="1" width="6" height="6" rx="1.5" fill={fill} opacity="0.5"  />
      <rect x="1" y="9" width="6" height="6" rx="1.5" fill={fill} opacity="0.5"  />
      <rect x="9" y="9" width="6" height="6" rx="1.5" fill={fill} opacity="0.25" />
    </svg>
  );
}

function IconDb({ active }: { active: boolean }) {
  const c = active ? 'var(--acc)' : 'var(--t3)';
  return (
    <svg width="10" height="10" viewBox="0 0 16 16" fill="none" style={{ flexShrink: 0 }}>
      <ellipse cx="8" cy="4" rx="6" ry="2" stroke={c} strokeWidth="1.4" fill="none" />
      <path d="M2 4v8c0 1.1 2.69 2 6 2s6-.9 6-2V4" stroke={c} strokeWidth="1.4" fill="none" />
      <path d="M2 8c0 1.1 2.69 2 6 2s6-.9 6-2" stroke={c} strokeWidth="1" opacity="0.5" />
    </svg>
  );
}

function IconSchema({ active }: { active: boolean }) {
  const c = active ? 'var(--acc)' : 'var(--t3)';
  return (
    <svg width="10" height="10" viewBox="0 0 16 16" fill="none" style={{ flexShrink: 0 }}>
      <rect x="1" y="2"  width="14" height="3" rx="1" stroke={c} strokeWidth="1.3" fill="none" />
      <rect x="1" y="7"  width="9"  height="3" rx="1" stroke={c} strokeWidth="1.3" fill="none" />
      <rect x="1" y="12" width="6"  height="2" rx="1" fill={c} opacity="0.5" />
    </svg>
  );
}



// ─── Cascade selector pill ────────────────────────────────────────────────────

interface CascadePillProps {
  icon:        ReactNode;
  placeholder: string;
  value:       string;
  options:     { id: string; label: string }[];
  onChange:    (id: string) => void;
  onClear?:    () => void;
  active:      boolean;
}

function CascadePill({ icon, placeholder, value, options, onChange, onClear, active }: CascadePillProps) {
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
      color:        active ? 'var(--acc)' : 'var(--t3)',
      whiteSpace:   'nowrap',
      flexShrink:   0,
      maxWidth:     130,
      cursor:       'pointer',
    }}>
      {icon}
      <span style={{
        overflow: 'hidden', textOverflow: 'ellipsis', pointerEvents: 'none',
        maxWidth: 85, fontFamily: 'var(--mono)',
      }}>
        {label}
      </span>
      <select
        value={value}
        onChange={(e) => onChange(e.target.value)}
        style={{ position: 'absolute', inset: 0, opacity: 0, cursor: 'pointer', width: '100%' }}
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

// ─── FilterToolbarL1 ─────────────────────────────────────────────────────────

export const FilterToolbarL1 = memo(() => {
  const { t } = useTranslation();
  const {
    l1ScopeStack, clearL1Scope, setL1Scope,
    availableApps, availableDbs, availableSchemas,
    l1Filter, setL1Depth, toggleL1DirUp, toggleL1DirDown, toggleL1SystemLevel,
    l1HierarchyFilter,
    setL1HierarchyDb, setL1HierarchySchema, clearL1HierarchyFilter,
  } = useLoomStore();

  const activeScope = l1ScopeStack.length > 0
    ? l1ScopeStack[l1ScopeStack.length - 1]
    : null;

  const { depth, dirUp, dirDown, systemLevel } = l1Filter;
  const { dbId, schemaId } = l1HierarchyFilter;
  const depthLabel = (d: L1Depth) => d === 99 ? '∞' : String(d);
  const hasFilter = !!(activeScope || dbId || schemaId);

  const badgeText = systemLevel
    ? `L1 · ${t('l1.systemLevel')}`
    : hasFilter
      ? 'L1 · filtered'
      : `L1 · ${depthLabel(depth)}`;

  // ── Cascading options ─────────────────────────────────────────────────────
  // DB list is narrowed by the active Scope selector (l1ScopeStack App) —
  // so App filtering stays in the Scope pill, no duplication.
  const scopedAppId = activeScope?.nodeId ?? null;
  const dbOptions = availableDbs.filter(
    (d) => !scopedAppId || d.appId === scopedAppId,
  );
  const schemaOptions = dbId
    ? availableSchemas.filter((s) => s.dbId === dbId)
    : availableSchemas.filter((s) => dbOptions.some((d) => d.id === s.dbId));

  // ── Handlers ─────────────────────────────────────────────────────────────
  const handleDbChange = (id: string) => {
    if (!id) { setL1HierarchyDb(null); return; }
    setL1HierarchyDb(id);
  };

  const handleSchemaChange = (id: string) => {
    if (!id) { setL1HierarchySchema(null); return; }
    // If the schema belongs to a different DB, auto-set that DB first
    const sch = availableSchemas.find((s) => s.id === id);
    if (sch && sch.dbId !== dbId) setL1HierarchyDb(sch.dbId);
    setL1HierarchySchema(id);
  };

  return (
    <div style={{
      display:      'flex',
      alignItems:   'center',
      gap:          5,
      height:       32,
      flexShrink:   0,
      padding:      '0 10px',
      background:   'var(--bg0)',
      borderBottom: '0.5px solid var(--bd)',
      overflow:     'hidden',
    }}>

      {/* ── Depth ─────────────────────────────────────────────────────── */}
      <span style={{ fontSize: 10, color: 'var(--t3)', whiteSpace: 'nowrap', flexShrink: 0 }}>
        {t('toolbar.depth')}:
      </span>
      <div style={{ display: 'flex', gap: 2, flexShrink: 0 }}>
        {DEPTHS.map((d) => {
          const isOn = d === depth;
          return (
            <button key={d} aria-pressed={isOn} onClick={() => setL1Depth(d)} style={{
              padding: '2px 6px', borderRadius: 3,
              border: `0.5px solid ${isOn ? 'var(--acc)' : 'var(--bd)'}`,
              fontSize: 9, color: isOn ? 'var(--acc)' : 'var(--t3)',
              cursor: 'pointer',
              background: isOn ? 'color-mix(in srgb, var(--acc) 8%, transparent)' : 'transparent',
              fontWeight: isOn ? 600 : 400, fontFamily: 'var(--font)',
            }}>
              {depthLabel(d)}
            </button>
          );
        })}
      </div>

      <ToolbarDivider size="sm" />

      {/* ── Direction ─────────────────────────────────────────────────── */}
      <div style={{ display: 'flex', gap: 3, flexShrink: 0 }}>
        <ToolbarToggleButton active={dirUp} onClick={toggleL1DirUp} size="sm" color="var(--inf)">
          ↑ {t('toolbar.upstream')}
        </ToolbarToggleButton>
        <ToolbarToggleButton active={dirDown} onClick={toggleL1DirDown} size="sm" color="var(--wrn)">
          ↓ {t('toolbar.downstream')}
        </ToolbarToggleButton>
      </div>

      <ToolbarDivider size="sm" />

      {/* ── System-level toggle ───────────────────────────────────────── */}
      <ToolbarToggleButton
        active={systemLevel}
        onClick={toggleL1SystemLevel}
        size="sm"
        color="var(--wrn)"
        title={t('l1.systemLevelHint')}
        style={{ fontSize: 9, padding: '2px 8px' }}
      >
        <IconLayers size={10} />
        {t('l1.systemLevel')}
      </ToolbarToggleButton>

      <ToolbarDivider size="sm" />

      {/* ── Cascade filter: App → DB → Schema ────────────────────────── */}
      <CascadePill
        icon={<IconApp active={!!activeScope} />}
        placeholder={t('l1.allSystems')}
        value={activeScope?.nodeId ?? ''}
        options={availableApps}
        onChange={(id) => {
          if (!id) { clearL1HierarchyFilter(); clearL1Scope(); return; }
          const app = availableApps.find((a) => a.id === id);
          if (app) setL1Scope(app.id, app.label);
        }}
        onClear={() => { clearL1Scope(); clearL1HierarchyFilter(); }}
        active={!!activeScope}
      />
      <span style={{ fontSize: 9, color: 'var(--t3)', flexShrink: 0, userSelect: 'none' }}>→</span>
      <CascadePill
        icon={<IconDb active={!!dbId} />}
        placeholder={t('l1.allDbs')}
        value={dbId ?? ''}
        options={dbOptions}
        onChange={handleDbChange}
        onClear={() => setL1HierarchyDb(null)}
        active={!!dbId}
      />
      <span style={{ fontSize: 9, color: 'var(--t3)', flexShrink: 0, userSelect: 'none' }}>→</span>
      <CascadePill
        icon={<IconSchema active={!!schemaId} />}
        placeholder={t('l1.allSchemas')}
        value={schemaId ?? ''}
        options={schemaOptions}
        onChange={handleSchemaChange}
        onClear={() => setL1HierarchySchema(null)}
        active={!!schemaId}
      />

      {/* Clear all filters */}
      {hasFilter && (
        <button
          onClick={() => { clearL1Scope(); clearL1HierarchyFilter(); }}
          style={{
            padding: '1px 6px', borderRadius: 3,
            border: '0.5px solid var(--wrn)',
            fontSize: 8, color: 'var(--wrn)',
            cursor: 'pointer',
            background: 'color-mix(in srgb, var(--wrn) 6%, transparent)',
            whiteSpace: 'nowrap', flexShrink: 0,
            fontFamily: 'var(--font)',
          }}
        >✕</button>
      )}

      {/* ── Spacer ────────────────────────────────────────────────────── */}
      <div style={{ flex: '1 1 auto' }} />

      {/* ── Level badge ───────────────────────────────────────────────── */}
      <span style={{
        fontSize: 9, padding: '2px 7px',
        border: `0.5px solid ${systemLevel || hasFilter ? 'var(--wrn)' : 'var(--bd)'}`,
        borderRadius: 3,
        color: systemLevel || hasFilter ? 'var(--wrn)' : 'var(--t3)',
        fontFamily: 'var(--mono)',
        whiteSpace: 'nowrap', flexShrink: 0,
      }}>
        {badgeText}
      </span>

    </div>
  );
});

FilterToolbarL1.displayName = 'FilterToolbarL1';
