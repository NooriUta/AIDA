// src/components/canvas/nodes/L1SchemaNode.tsx
// LOOM-024 v3: L1 Schema node — mini chip, nested inside DatabaseNode
//
// parentId: dbNodeId, extent: 'parent'  (stays within DB bounds; follows DB on drag)
// hidden = true by default; shown by applyL1Layout when store.expandedDbs.has(parentDbId)
// Click / double-click → drillDown → L2
// Has handles for Dataflow edges connecting to specific schemas

import { memo } from 'react';
import { Handle, Position, type NodeProps, type Node } from '@xyflow/react';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData } from '../../../types/domain';

export type L1SchemaNodeType = Node<DaliNodeData>;

export const L1SchemaNode = memo(({ data, selected, id }: NodeProps<L1SchemaNodeType>) => {
  const { drillDown, selectNode } = useLoomStore();

  const color = (data.metadata?.color as string | undefined) ?? 'var(--t3)';

  return (
    <div
      style={{
        width:         '100%',
        height:        '100%',
        display:       'inline-flex',
        alignItems:    'center',
        gap:           4,
        padding:       '0 6px',
        borderRadius:  3,
        border:        `0.5px dashed ${selected ? color + '80' : 'var(--bd)'}`,
        background:    'var(--bg1)',
        cursor:        'pointer',
        overflow:      'hidden',
        userSelect:    'none',
        transition:    'border-color 0.1s',
        boxSizing:     'border-box' as const,
      }}
      onClick={(e) => { e.stopPropagation(); selectNode(id, data); }}
      onDoubleClick={(e) => {
        e.stopPropagation();
        const dbName = data.metadata?.databaseName as string | null | undefined;
        const scope = dbName ? `schema-${data.label}|${dbName}` : `schema-${data.label}`;
        drillDown(scope, data.label, 'DaliSchema');
      }}
    >
      {/* Colour dot */}
      <span style={{
        width:        4,
        height:       4,
        borderRadius: '50%',
        background:   color,
        flexShrink:   0,
      }} />

      {/* Schema name */}
      <span title={data.label} style={{
        fontSize:     9,
        color:        selected ? 'var(--t1)' : 'var(--t2)',
        fontFamily:   'var(--mono)',
        overflow:     'hidden',
        textOverflow: 'ellipsis',
        whiteSpace:   'nowrap',
        flex:         1,
        transition:   'color 0.1s',
      }}>
        {data.label}
      </span>

      {/* Table count (if available) */}
      {data.tablesCount !== undefined && data.tablesCount > 0 && (
        <span style={{
          fontSize:   8,
          color:      'var(--t3)',
          fontFamily: 'var(--mono)',
          flexShrink: 0,
        }}>
          {data.tablesCount}
        </span>
      )}

      {/* Package count (if available) — CONTAINS_ROUTINE → DaliPackage */}
      {data.routinesCount !== undefined && data.routinesCount > 0 && (
        <span style={{
          fontSize:   8,
          color:      'var(--inf)',  // #88B8A8 teal — structural/package colour
          fontFamily: 'var(--mono)',
          flexShrink: 0,
          opacity:    0.8,
        }}>
          {data.routinesCount}p
        </span>
      )}

      {/* Handles for Dataflow edges (schema participates in data flows) */}
      <Handle
        type="target"
        position={Position.Left}
        style={{
          width:      5,
          height:     5,
          background: color,
          opacity:    0.55,
          border:     'none',
        }}
      />
      <Handle
        type="source"
        position={Position.Right}
        style={{
          width:      5,
          height:     5,
          background: color,
          opacity:    0.55,
          border:     'none',
        }}
      />
    </div>
  );
});

L1SchemaNode.displayName = 'L1SchemaNode';
