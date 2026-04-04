// src/components/canvas/nodes/DatabaseNode.tsx
// LOOM-024 v3: L1 — СУБД (DaliDatabase)
//
// RF child node: parentId = applicationNodeId, extent = 'parent'.
// Schema children (L1SchemaNode) are separate RF nodes with parentId: this.id.
// "схемы ↓" toggle dispatches store.toggleDbExpansion(id) which triggers
// applyL1Layout in LoomCanvas, showing/hiding schema children and resizing
// this node's parent ApplicationNode.
//
// Double-click with no schema children → drillDown directly.

import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { Handle, Position, type NodeProps, type Node } from '@xyflow/react';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData } from '../../../types/domain';

export type DatabaseNodeType = Node<DaliNodeData>;

export const DatabaseNode = memo(({ data, selected, id }: NodeProps<DatabaseNodeType>) => {
  const { drillDown, selectNode, expandedDbs, toggleDbExpansion } = useLoomStore();
  const { t } = useTranslation();

  const color      = (data.metadata?.color      as string  | undefined) ?? 'var(--t3)';
  const engine     = (data.metadata?.engine     as string  | undefined) ?? '';
  const isShared   = (data.metadata?.shared     as boolean | undefined) ?? false;
  const schemaCount = (data.metadata?.schemaCount as number | undefined) ?? 0;
  const tableCount = (data.metadata?.tableCount as number | undefined)
                  ?? data.tablesCount
                  ?? 0;

  const isExpanded = expandedDbs.has(id);
  const hasSchemas = schemaCount > 0;

  // Double-click always drills to L2: DB scope → SHUTTLE explore returns all
  // schemas in this DB. Schema chips (L1SchemaNode) handle schema-scoped L2.
  const drillableNode = data.childrenAvailable;

  return (
    <div
      style={{
        border:       `0.5px solid ${selected ? color + '80' : 'var(--bd)'}`,
        borderLeft:   `3px solid ${color}`,
        borderRadius: 5,
        background:   'var(--bg2)',
        width:        '100%',
        height:       '100%',     // fill RF node height (grows when schemas expanded)
        cursor:       drillableNode ? 'pointer' : 'default',
        userSelect:   'none',
        transition:   'border-color 0.12s',
        boxSizing:    'border-box' as const,
      }}
      onClick={(e) => { e.stopPropagation(); selectNode(id); }}
      onDoubleClick={(e) => {
        e.stopPropagation();
        if (drillableNode) drillDown(id, data.label, data.nodeType);
      }}
    >
      {/* ── Header ─────────────────────────────────────────────────────────── */}
      <div style={{
        padding:      '5px 8px',
        display:      'flex',
        alignItems:   'center',
        gap:          5,
        borderBottom: '0.5px solid var(--bd)',
      }}>
        <DbIcon color={color} />
        <span style={{
          fontSize:     11,
          fontWeight:   600,
          color:        'var(--t1)',
          flex:         1,
          fontFamily:   'monospace',
          overflow:     'hidden',
          textOverflow: 'ellipsis',
          whiteSpace:   'nowrap',
          minWidth:     0,
        }}>
          {data.label}
        </span>
        {tableCount > 0 && (
          <span style={{
            fontSize:   9,
            color:      'var(--t3)',
            fontFamily: 'monospace',
            flexShrink: 0,
          }}>
            {tableCount}
          </span>
        )}
        {isShared && (
          <span style={{
            fontSize:   8,
            padding:    '1px 4px',
            borderRadius: 2,
            background:  'rgba(168,184,96,0.08)',
            border:      '0.5px solid rgba(168,184,96,0.3)',
            color:       'var(--acc)',
            flexShrink:  0,
          }}>
            shared
          </span>
        )}
      </div>

      {/* ── Footer ─────────────────────────────────────────────────────────── */}
      <div style={{
        padding:        '3px 8px 4px',
        display:        'flex',
        alignItems:     'center',
        justifyContent: 'space-between',
        gap:            6,
      }}>
        <span style={{
          fontSize:   9,
          color:      'var(--t3)',
          fontFamily: 'monospace',
          flex:       1,
        }}>
          {engine || (drillableNode ? '↓ L2 →' : '')}
        </span>

        {/* Schema expand toggle — dispatches to store, not local state */}
        {hasSchemas && (
          <button
            style={{
              fontSize:    9,
              color:       isExpanded ? color : 'var(--t2)',
              padding:     '1px 5px',
              borderRadius: 3,
              border:      `0.5px solid ${isExpanded ? color + '60' : 'var(--bd)'}`,
              background:  'transparent',
              cursor:      'pointer',
              fontFamily:  'var(--sans)',
              lineHeight:  1.2,
              whiteSpace:  'nowrap',
              transition:  'color 0.1s, border-color 0.1s',
            }}
            onClick={(e) => {
              e.stopPropagation();
              toggleDbExpansion(id);
            }}
          >
            {t('l1.schemas')} {isExpanded ? '↑' : '↓'}
          </button>
        )}
      </div>

      {/* ── Handles ────────────────────────────────────────────────────────── */}
      <Handle
        type="target"
        position={Position.Left}
        style={{ background: color, width: 7, height: 7 }}
      />
      <Handle
        type="source"
        position={Position.Right}
        style={{ background: color, width: 7, height: 7 }}
      />
    </div>
  );
});

DatabaseNode.displayName = 'DatabaseNode';

// ─── Cylinder icon ────────────────────────────────────────────────────────────
function DbIcon({ color }: { color: string }) {
  return (
    <svg width="11" height="11" viewBox="0 0 16 16" fill="none" style={{ flexShrink: 0 }}>
      <ellipse cx="8" cy="4" rx="6" ry="2"
        stroke={color} strokeWidth="1.3" fill="none" />
      <path d="M2 4v8c0 1.1 2.69 2 6 2s6-.9 6-2V4"
        stroke={color} strokeWidth="1.3" fill="none" />
      <path d="M2 8c0 1.1 2.69 2 6 2s6-.9 6-2"
        stroke={color} strokeWidth="1" opacity="0.5" />
    </svg>
  );
}
