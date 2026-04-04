// src/components/layout/FilterToolbarL1.tsx
// LOOM-024b: L1 Filter Toolbar — scope pill + depth + direction + system-level toggle.
// Visible only on L1. Height 32px (compact vs L2/L3 toolbar 40px).

import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../stores/loomStore';

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

function IconLayers() {
  return (
    <svg width="10" height="10" viewBox="0 0 12 12" fill="none" style={{ flexShrink: 0 }}>
      <path d="M1 4l5-3 5 3-5 3-5-3z"
        stroke="currentColor" strokeWidth="1.2" strokeLinejoin="round" fill="none" />
      <path d="M1 7l5 3 5-3"
        stroke="currentColor" strokeWidth="1.2" strokeLinecap="round" />
    </svg>
  );
}

// ─── Divider ─────────────────────────────────────────────────────────────────

function Sep() {
  return (
    <div style={{
      width: '0.5px', height: 16,
      background: 'var(--bd)',
      flexShrink: 0, margin: '0 2px',
    }} />
  );
}

// ─── FilterToolbarL1 ─────────────────────────────────────────────────────────

export const FilterToolbarL1 = memo(() => {
  const { t } = useTranslation();
  const {
    l1ScopeStack, clearL1Scope, setL1Scope,
    availableApps,
    l1Filter, setL1Depth, toggleL1DirUp, toggleL1DirDown, toggleL1SystemLevel,
  } = useLoomStore();

  const activeScope = l1ScopeStack.length > 0
    ? l1ScopeStack[l1ScopeStack.length - 1]
    : null;

  const { depth, dirUp, dirDown, systemLevel } = l1Filter;
  const depthLabel = (d: L1Depth) => d === 99 ? '∞' : String(d);

  const badgeText = systemLevel
    ? `L1 · ${t('l1.systemLevel')}`
    : `L1 · ${depthLabel(depth)}`;

  return (
    <div style={{
      display: 'flex',
      alignItems: 'center',
      gap: 5,
      height: 32,
      flexShrink: 0,
      padding: '0 10px',
      background: 'var(--bg0)',
      borderBottom: '0.5px solid var(--bd)',
      overflow: 'hidden',
    }}>

      {/* ── Scope selector ─────────────────────────────────────────────── */}
      {/* Invisible <select> sits on top of the styled pill for native dropdown UX. */}
      <div style={{
        position: 'relative',
        display: 'inline-flex',
        alignItems: 'center',
        gap: 4,
        padding: '2px 8px',
        borderRadius: 4,
        border: `0.5px solid ${activeScope ? 'var(--acc)' : 'var(--bd)'}`,
        background: activeScope ? 'rgba(168,184,96,.07)' : 'transparent',
        fontSize: 10,
        color: activeScope ? 'var(--acc)' : 'var(--t2)',
        whiteSpace: 'nowrap',
        flexShrink: 0,
        maxWidth: 180,
        cursor: 'pointer',
      }}>
        <IconApp active={!!activeScope} />
        <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', pointerEvents: 'none' }}>
          {activeScope ? activeScope.label : t('l1.allSystems')}
        </span>

        {/* Transparent select overlay — provides native OS dropdown */}
        <select
          value={activeScope?.nodeId ?? ''}
          onChange={(e) => {
            const id = e.target.value;
            if (!id) { clearL1Scope(); return; }
            const app = availableApps.find((a) => a.id === id);
            if (app) setL1Scope(app.id, app.label);
          }}
          style={{
            position: 'absolute',
            inset: 0,
            opacity: 0,
            cursor: 'pointer',
            width: '100%',
            fontSize: 10,
          }}
        >
          <option value="">{t('l1.allSystems')}</option>
          {availableApps.map((app) => (
            <option key={app.id} value={app.id}>{app.label}</option>
          ))}
        </select>

        {activeScope && (
          <button
            onClick={(e) => { e.stopPropagation(); clearL1Scope(); }}
            title={t('l1.clearScope')}
            style={{
              position: 'relative', zIndex: 1,
              display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
              width: 14, height: 14,
              border: 'none', background: 'transparent',
              color: 'var(--t3)', fontSize: 10,
              cursor: 'pointer', padding: 0, lineHeight: 1,
              marginLeft: 2, flexShrink: 0,
            }}
          >✕</button>
        )}
      </div>

      <Sep />

      {/* ── Depth ───────────────────────────────────────────────────────── */}
      <span style={{ fontSize: 10, color: 'var(--t3)', whiteSpace: 'nowrap', flexShrink: 0 }}>
        {t('toolbar.depth')}:
      </span>
      <div style={{ display: 'flex', gap: 2, flexShrink: 0 }}>
        {DEPTHS.map((d) => {
          const isOn = d === depth;
          return (
            <button
              key={d}
              onClick={() => setL1Depth(d)}
              style={{
                padding: '2px 6px', borderRadius: 3,
                border: `0.5px solid ${isOn ? 'var(--acc)' : 'var(--bd)'}`,
                fontSize: 9,
                color: isOn ? 'var(--acc)' : 'var(--t3)',
                cursor: 'pointer',
                background: isOn ? 'rgba(168,184,96,.08)' : 'transparent',
                fontWeight: isOn ? 600 : 400,
                fontFamily: 'var(--sans)',
              }}
            >
              {depthLabel(d)}
            </button>
          );
        })}
      </div>

      <Sep />

      {/* ── Direction ───────────────────────────────────────────────────── */}
      <div style={{ display: 'flex', gap: 3, flexShrink: 0 }}>
        <button
          onClick={toggleL1DirUp}
          style={{
            padding: '2px 7px', borderRadius: 3,
            border: `0.5px solid ${dirUp ? 'var(--inf)' : 'var(--bd)'}`,
            fontSize: 10,
            color: dirUp ? 'var(--inf)' : 'var(--t3)',
            cursor: 'pointer',
            background: dirUp ? 'rgba(136,184,168,.08)' : 'transparent',
            display: 'inline-flex', alignItems: 'center', gap: 3,
            fontFamily: 'var(--sans)',
          }}
        >
          ↑ {t('toolbar.upstream')}
        </button>
        <button
          onClick={toggleL1DirDown}
          style={{
            padding: '2px 7px', borderRadius: 3,
            border: `0.5px solid ${dirDown ? 'var(--wrn)' : 'var(--bd)'}`,
            fontSize: 10,
            color: dirDown ? 'var(--wrn)' : 'var(--t3)',
            cursor: 'pointer',
            background: dirDown ? 'rgba(212,146,42,.08)' : 'transparent',
            display: 'inline-flex', alignItems: 'center', gap: 3,
            fontFamily: 'var(--sans)',
          }}
        >
          ↓ {t('toolbar.downstream')}
        </button>
      </div>

      <Sep />

      {/* ── System-level toggle ─────────────────────────────────────────── */}
      <button
        onClick={toggleL1SystemLevel}
        title={t('l1.systemLevelHint')}
        style={{
          padding: '2px 8px', borderRadius: 3,
          border: `0.5px solid ${systemLevel ? 'var(--wrn)' : 'var(--bd)'}`,
          fontSize: 9,
          color: systemLevel ? 'var(--wrn)' : 'var(--t3)',
          cursor: 'pointer',
          background: systemLevel ? 'rgba(212,146,42,.07)' : 'transparent',
          display: 'inline-flex', alignItems: 'center', gap: 4,
          whiteSpace: 'nowrap', flexShrink: 0,
          fontFamily: 'var(--sans)',
        }}
      >
        <IconLayers />
        {t('l1.systemLevel')}
      </button>

      {/* ── Spacer ──────────────────────────────────────────────────────── */}
      <div style={{ flex: '1 1 auto' }} />

      {/* ── Level badge ─────────────────────────────────────────────────── */}
      <span style={{
        fontSize: 9, padding: '2px 7px',
        border: `0.5px solid ${systemLevel ? 'var(--wrn)' : 'var(--bd)'}`,
        borderRadius: 3,
        color: systemLevel ? 'var(--wrn)' : 'var(--t3)',
        fontFamily: 'var(--mono)',
        whiteSpace: 'nowrap', flexShrink: 0,
      }}>
        {badgeText}
      </span>

    </div>
  );
});

FilterToolbarL1.displayName = 'FilterToolbarL1';
