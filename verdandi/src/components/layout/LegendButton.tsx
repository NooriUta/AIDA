// src/components/layout/LegendButton.tsx
// LOOM: Graph legend — dropdown button in the Header nav bar.
// Pattern mirrors PaletteMenu: position:relative wrapper, absolute dropdown,
// onBlur auto-close. Content adapts per view level (L1 / L2 / L3).

import { memo, useRef, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../stores/loomStore';

// ─── Edge / node data per level ───────────────────────────────────────────────

interface EdgeRow {
  color:     string;
  dash?:     string;
  animated?: boolean;
  label:     string;   // i18n key
}

interface NodeRow {
  color: string;
  shape: 'rect' | 'rect-dash' | 'circle';
  label: string;
}

const EDGE_ROWS: Record<'L1' | 'L2' | 'L3', EdgeRow[]> = {
  L1: [
    { color: '#A8B860',              label: 'legend.edge.hasDatabase'    },
    { color: '#88B8A8',              label: 'legend.edge.containsSchema' },
    { color: '#665c48', dash: '6 3', label: 'legend.edge.usesDatabase'   },
  ],
  L2: [
    { color: '#88B8A8',                   label: 'legend.edge.readsFrom'    },
    { color: '#D4922A', dash: '5 3',      label: 'legend.edge.writesTo'     },
    { color: '#A8B860', animated: true,   label: 'legend.edge.dataFlow'     },
    { color: '#D4922A',                   label: 'legend.edge.filterFlow'   },
    { color: '#88B8A8',                   label: 'legend.edge.joinFlow'     },
    { color: '#665c48', dash: '4 2',      label: 'legend.edge.containsStmt' },
  ],
  L3: [
    { color: '#88B8A8',                   label: 'legend.edge.readsFrom'    },
    { color: '#D4922A', dash: '5 3',      label: 'legend.edge.writesTo'     },
    { color: '#A8B860', animated: true,   label: 'legend.edge.dataFlow'     },
    { color: '#A8B860', animated: true,   label: 'legend.edge.atomProduces' },
    { color: '#88B8A8', dash: '4 3',      label: 'legend.edge.atomRefCol'   },
  ],
};

const NODE_ROWS: Record<'L1' | 'L2' | 'L3', NodeRow[]> = {
  L1: [
    { color: '#A8B860', shape: 'rect',      label: 'legend.node.application' },
    { color: '#a89a7a', shape: 'rect',      label: 'legend.node.database'    },
    { color: '#88B8A8', shape: 'rect-dash', label: 'legend.node.schema'      },
  ],
  L2: [
    { color: '#A8B860', shape: 'rect',      label: 'legend.node.table'     },
    { color: '#665c48', shape: 'rect',      label: 'legend.node.package'   },
    { color: '#7DBF78', shape: 'rect',      label: 'legend.node.routine'   },
    { color: '#7DBF78', shape: 'rect',      label: 'legend.node.statement' },
  ],
  L3: [
    { color: '#A8B860', shape: 'rect',      label: 'legend.node.table'  },
    { color: '#88B8A8', shape: 'circle',    label: 'legend.node.column' },
    { color: '#D4922A', shape: 'circle',    label: 'legend.node.atom'   },
  ],
};

// ─── Swatch helpers ───────────────────────────────────────────────────────────

const DASH_ANIM_STYLE = `
@keyframes loom-dash-march { to { stroke-dashoffset: -16; } }`;

function EdgeSwatch({ color, dash, animated }: Pick<EdgeRow, 'color' | 'dash' | 'animated'>) {
  return (
    <svg width="26" height="10" viewBox="0 0 26 10" style={{ flexShrink: 0 }}>
      {animated && <style>{DASH_ANIM_STYLE}</style>}
      <line
        x1="0" y1="5" x2="26" y2="5"
        stroke={color} strokeWidth="1.5"
        strokeDasharray={animated ? '5 3' : (dash ?? undefined)}
        style={animated ? { animation: 'loom-dash-march 0.5s linear infinite' } : undefined}
      />
    </svg>
  );
}

function NodeSwatch({ color, shape }: Pick<NodeRow, 'color' | 'shape'>) {
  if (shape === 'circle') {
    return (
      <svg width="12" height="12" viewBox="0 0 12 12" style={{ flexShrink: 0 }}>
        <circle cx="6" cy="6" r="4.5" fill={color} opacity="0.85" />
      </svg>
    );
  }
  return (
    <svg width="14" height="10" viewBox="0 0 14 10" style={{ flexShrink: 0 }}>
      <rect
        x="1" y="1" width="12" height="8" rx="1.5"
        fill={color + '22'} stroke={color} strokeWidth="1.2"
        strokeDasharray={shape === 'rect-dash' ? '3 2' : undefined}
      />
    </svg>
  );
}

// ─── LegendButton ─────────────────────────────────────────────────────────────

export const LegendButton = memo(() => {
  const { t } = useTranslation();
  const viewLevel = useLoomStore((s) => s.viewLevel);
  const [open, setOpen] = useState(false);
  const wrapperRef = useRef<HTMLDivElement>(null);

  const level = viewLevel as 'L1' | 'L2' | 'L3';
  const edges = EDGE_ROWS[level] ?? EDGE_ROWS.L2;
  const nodes = NODE_ROWS[level] ?? NODE_ROWS.L2;

  const handleBlur = (e: React.FocusEvent) => {
    if (!wrapperRef.current?.contains(e.relatedTarget as Node)) {
      setOpen(false);
    }
  };

  return (
    <div
      ref={wrapperRef}
      style={{ position: 'relative' }}
      onBlur={handleBlur}
    >
      {/* ── Toggle button (same sizing as Palette / Theme buttons) ── */}
      <button
        onClick={() => setOpen((v) => !v)}
        title={t(open ? 'legend.close' : 'legend.open')}
        style={{
          background:   'transparent',
          border:       `1px solid ${open ? 'var(--acc)' : 'var(--bd)'}`,
          borderRadius: 'var(--seer-radius-md)',
          padding:      '5px 7px',
          cursor:       'pointer',
          color:        open ? 'var(--acc)' : 'var(--t2)',
          display:      'flex',
          alignItems:   'center',
          transition:   'border-color 0.12s, color 0.12s',
        }}
      >
        <LegendIcon active={open} />
      </button>

      {/* ── Dropdown panel ── */}
      {open && (
        <div style={{
          position:  'absolute',
          top:       'calc(100% + 4px)',
          right:     0,
          zIndex:    300,
          width:     186,
          background: 'var(--bg1)',
          border:    '1px solid var(--bd)',
          borderRadius: 'var(--seer-radius-md)',
          boxShadow: '0 4px 16px rgba(0,0,0,0.4)',
          overflow:  'hidden',
        }}>
          {/* Header */}
          <div style={{
            display:        'flex',
            alignItems:     'center',
            justifyContent: 'space-between',
            padding:        '6px 12px 5px',
            borderBottom:   '1px solid var(--bd)',
          }}>
            <span style={{ fontSize: 10, fontWeight: 600, color: 'var(--t2)', letterSpacing: '0.06em' }}>
              {t('legend.title').toUpperCase()}
            </span>
            <span style={{
              fontSize: 9, color: 'var(--acc)',
              fontFamily: 'var(--mono)', letterSpacing: '0.06em',
              padding: '1px 5px', border: '0.5px solid var(--acc)',
              borderRadius: 2, opacity: 0.8,
            }}>
              {level}
            </span>
          </div>

          {/* Edges */}
          <div style={{ padding: '6px 12px 5px' }}>
            <div style={{
              fontSize: 8, color: 'var(--t3)', textTransform: 'uppercase',
              letterSpacing: '0.08em', marginBottom: 5, fontWeight: 600,
            }}>
              {t('legend.section.edges')}
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              {edges.map((e) => (
                <div key={e.label} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <EdgeSwatch color={e.color} dash={e.dash} animated={e.animated} />
                  <span style={{ fontSize: 9, color: 'var(--t2)', fontFamily: 'var(--mono)' }}>
                    {t(e.label)}
                  </span>
                </div>
              ))}
            </div>
          </div>

          <div style={{ height: '0.5px', background: 'var(--bd)', margin: '0 12px' }} />

          {/* Nodes */}
          <div style={{ padding: '5px 12px 6px' }}>
            <div style={{
              fontSize: 8, color: 'var(--t3)', textTransform: 'uppercase',
              letterSpacing: '0.08em', marginBottom: 5, fontWeight: 600,
            }}>
              {t('legend.section.nodes')}
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
              {nodes.map((n) => (
                <div key={n.label} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <NodeSwatch color={n.color} shape={n.shape} />
                  <span style={{ fontSize: 9, color: 'var(--t2)', fontFamily: 'var(--mono)' }}>
                    {t(n.label)}
                  </span>
                </div>
              ))}
            </div>
          </div>

          <div style={{ height: '0.5px', background: 'var(--bd)', margin: '0 12px' }} />

          {/* Interactions */}
          <div style={{ padding: '4px 12px 7px', display: 'flex', flexDirection: 'column', gap: 3 }}>
            {[
              { key: '×2', hint: 'legend.hint.dblclick' },
              { key: '×1', hint: 'legend.hint.click'    },
            ].map(({ key, hint }) => (
              <div key={hint} style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                <span style={{
                  fontSize: 8, color: 'var(--t3)', fontFamily: 'var(--mono)',
                  flexShrink: 0, minWidth: 14, textAlign: 'right',
                }}>
                  {key}
                </span>
                <span style={{ fontSize: 9, color: 'var(--t3)' }}>{t(hint)}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
});

LegendButton.displayName = 'LegendButton';

// ─── Icon ─────────────────────────────────────────────────────────────────────

function LegendIcon({ active }: { active: boolean }) {
  const c = active ? 'var(--acc)' : 'currentColor';
  return (
    <svg width="13" height="13" viewBox="0 0 16 16" fill="none">
      <rect x="2" y="3" width="12" height="10" rx="1.5"
        stroke={c} strokeWidth="1.3" fill="none" />
      <line x1="2"  y1="7"  x2="14" y2="7"  stroke={c} strokeWidth="1"   opacity="0.6" />
      <line x1="2"  y1="10" x2="10" y2="10" stroke={c} strokeWidth="1"   opacity="0.4" />
      <line x1="6"  y1="3"  x2="6"  y2="13" stroke={c} strokeWidth="1"   opacity="0.35" />
    </svg>
  );
}
