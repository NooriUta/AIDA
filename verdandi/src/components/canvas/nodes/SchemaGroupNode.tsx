// L2 — Schema group container node.
// Renders as a dashed-border box with a header bar; child table/routine nodes
// are placed inside using React Flow's parentId mechanism.
// No drill-down (we're already at L2); clicking selects the group.

import { memo } from 'react';
import { type NodeProps, type Node } from '@xyflow/react';
import { FolderTree } from 'lucide-react';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData } from '../../../types/domain';

export type SchemaGroupNodeType = Node<DaliNodeData>;

export const SchemaGroupNode = memo(({ data, selected, id }: NodeProps<SchemaGroupNodeType>) => {
  const { selectNode } = useLoomStore();
  const color = '#88B8A8'; // --inf

  return (
    <div
      style={{
        width:        '100%',
        height:       '100%',
        border:       `1.5px dashed ${selected ? color : color + '55'}`,
        borderRadius: 8,
        background:   'rgba(20,17,8,0.35)',
        position:     'relative',
      }}
      onClick={(e) => { e.stopPropagation(); selectNode(id); }}
    >
      {/* Header */}
      <div style={{
        padding:      '7px 10px 6px',
        borderBottom: '0.5px solid var(--bd)',
        borderRadius: '6px 6px 0 0',
        display:      'flex',
        alignItems:   'center',
        gap:          6,
        background:   `${color}10`,
        userSelect:   'none',
        pointerEvents: 'none',
      }}>
        <FolderTree size={13} color={color} strokeWidth={1.5} />
        <span style={{
          fontWeight:   600,
          fontSize:     11,
          color:        'var(--t1)',
          flex:         1,
          overflow:     'hidden',
          textOverflow: 'ellipsis',
          whiteSpace:   'nowrap',
        }}>
          {data.label}
        </span>
        <span style={{
          fontSize:      8,
          padding:       '1px 5px',
          borderRadius:  2,
          fontFamily:    'monospace',
          border:        `0.5px solid ${color}40`,
          color:         color,
          opacity:       0.65,
          flexShrink:    0,
          letterSpacing: '0.03em',
        }}>
          SCHEMA
        </span>
      </div>
    </div>
  );
});

SchemaGroupNode.displayName = 'SchemaGroupNode';
