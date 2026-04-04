// src/components/canvas/nodes/ApplicationNode.tsx
// LOOM-024 v2: L1 — Application (DaliApplication)
//
// RF Group parent node — dashed-border container.
// DatabaseNode children use { parentId: appId, extent: 'parent' }.
// Double-click → L1 scope filter (stays on L1, dims other apps).

import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { Handle, Position, type NodeProps, type Node } from '@xyflow/react';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData } from '../../../types/domain';

export type ApplicationNodeType = Node<DaliNodeData>;

export const ApplicationNode = memo(({ data, selected, id }: NodeProps<ApplicationNodeType>) => {
  const { pushL1Scope, selectNode } = useLoomStore();
  const { t } = useTranslation();

  const color   = (data.metadata?.color as string | undefined) ?? 'var(--acc)';
  const dbCount = (data.metadata?.databaseCount as number | undefined) ?? 0;

  return (
    <div
      style={{
        width:   '100%',
        height:  '100%',
        border:  `1.5px dashed ${selected ? color : color + '70'}`,
        borderRadius: 8,
        background: 'rgba(20,17,8,0.55)',
        position: 'relative',
        cursor: 'default',
        transition: 'border-color 0.15s',
      }}
      onClick={(e) => { e.stopPropagation(); selectNode(id); }}
      onDoubleClick={(e) => {
        e.stopPropagation();
        pushL1Scope(id, data.label, 'DaliApplication');
      }}
    >
      {/* ── Header ─────────────────────────────────────────────────────────── */}
      <div
        style={{
          padding:       '7px 10px 6px',
          borderBottom:  '0.5px solid var(--bd)',
          borderRadius:  '6px 6px 0 0',
          display:       'flex',
          alignItems:    'center',
          gap:           7,
          background:    selected ? `${color}14` : 'transparent',
          transition:    'background 0.15s',
          userSelect:    'none',
        }}
      >
        <AppIcon color={color} />
        <span style={{
          fontWeight:     600,
          fontSize:       12,
          color:          'var(--t1)',
          flex:           1,
          overflow:       'hidden',
          textOverflow:   'ellipsis',
          whiteSpace:     'nowrap',
          lineHeight:     1.2,
        }}>
          {data.label}
        </span>
        <span style={{
          fontSize:       8,
          padding:        '1px 5px',
          borderRadius:   2,
          fontFamily:     'monospace',
          border:         `0.5px solid ${color}40`,
          color:          color,
          opacity:        0.7,
          flexShrink:     0,
          letterSpacing: '0.03em',
        }}>
          {t('l1.appBadge')}
        </span>
      </div>

      {/* ── Meta row ───────────────────────────────────────────────────────── */}
      <div style={{
        fontSize:     9,
        color:        'var(--t3)',
        padding:      '3px 10px 0',
        marginBottom: 4,
        userSelect:   'none',
      }}>
        {dbCount > 0 ? `${dbCount}\u00a0${t('l1.dbUnit')}` : '—'}
      </div>

      {/* ── Handles ────────────────────────────────────────────────────────── */}
      <Handle
        type="target"
        position={Position.Left}
        style={{
          background:   color,
          top:          30,
          width:        8,
          height:       8,
          border:       `1.5px solid ${color}`,
        }}
      />
      <Handle
        type="source"
        position={Position.Right}
        style={{
          background:   color,
          top:          30,
          width:        8,
          height:       8,
          border:       `1.5px solid ${color}`,
        }}
      />
    </div>
  );
});

ApplicationNode.displayName = 'ApplicationNode';

// ─── App icon (grid of 4 squares) ────────────────────────────────────────────
function AppIcon({ color }: { color: string }) {
  return (
    <div style={{
      width:          24,
      height:         24,
      borderRadius:   4,
      background:     'rgba(0,0,0,0.22)',
      display:        'flex',
      alignItems:     'center',
      justifyContent: 'center',
      flexShrink:     0,
    }}>
      <svg width="13" height="13" viewBox="0 0 16 16" fill="none">
        <rect x="1"   y="1"   width="6" height="6" rx="1.5" fill={color} opacity="0.8" />
        <rect x="9"   y="1"   width="6" height="6" rx="1.5" fill={color} opacity="0.5" />
        <rect x="1"   y="9"   width="6" height="6" rx="1.5" fill={color} opacity="0.5" />
        <rect x="9"   y="9"   width="6" height="6" rx="1.5" fill={color} opacity="0.25" />
      </svg>
    </div>
  );
}
