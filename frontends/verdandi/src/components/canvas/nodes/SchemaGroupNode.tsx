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
  return (
    <div
      style={{
        width:        '100%',
        height:       '100%',
        border:       selected
          ? '1.5px dashed var(--inf)'
          : '1.5px dashed color-mix(in srgb, var(--inf) 33%, transparent)',
        borderRadius: 'var(--seer-radius-lg)',
        background:   'rgba(20,17,8,0.35)',
        position:     'relative',
      }}
      onClick={(e) => { e.stopPropagation(); selectNode(id, data); }}
    >
      {/* Header */}
      <div style={{
        padding:      '7px 10px 6px',
        borderBottom: '0.5px solid var(--bd)',
        borderRadius: 'var(--seer-radius-md) var(--seer-radius-md) 0 0',
        display:      'flex',
        alignItems:   'center',
        gap:          6,
        background:   'color-mix(in srgb, var(--inf) 6%, transparent)',
        userSelect:   'none',
        pointerEvents: 'none',
      }}>
        <FolderTree size={13} color="var(--inf)" strokeWidth={1.5} />
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
          fontFamily:    'var(--mono)',
          border:        '0.5px solid color-mix(in srgb, var(--inf) 25%, transparent)',
          color:         'var(--inf)',
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
