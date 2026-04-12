// L2 — Routine group container node.
// Renders as a bordered box with header; child StatementNode(s) are placed
// inside using React Flow's parentId mechanism.
// Visual nesting: Schema → Routine → Statement.

import { memo } from 'react';
import { Handle, Position, type NodeProps, type Node } from '@xyflow/react';
import { Workflow } from 'lucide-react';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData } from '../../../types/domain';

export type RoutineGroupNodeType = Node<DaliNodeData>;

export const RoutineGroupNode = memo(({ data, selected, id }: NodeProps<RoutineGroupNodeType>) => {
  const { selectNode } = useLoomStore();
  return (
    <div
      style={{
        width:        '100%',
        height:       '100%',
        border:       selected
          ? '1.5px solid var(--suc)'
          : '1.5px solid color-mix(in srgb, var(--suc) 27%, transparent)',
        borderRadius: 'var(--seer-radius-md)',
        background:   'rgba(20,17,8,0.25)',
        position:     'relative',
        overflow:     'visible',
      }}
      onClick={(e) => { e.stopPropagation(); selectNode(id); }}
    >
      <Handle type="target" position={Position.Left} style={{ background: 'var(--suc)', zIndex: 5 }} />

      {/* Header */}
      <div style={{
        padding:       '6px 10px 5px',
        borderBottom:  '0.5px solid var(--bd)',
        borderRadius:  'var(--seer-radius-sm) var(--seer-radius-sm) 0 0',
        display:       'flex',
        alignItems:    'center',
        gap:           5,
        background:    'color-mix(in srgb, var(--suc) 7%, transparent)',
        userSelect:    'none',
        pointerEvents: 'none',
      }}>
        <Workflow size={12} color="var(--suc)" strokeWidth={1.5} />

        {/* Package prefix (when routine comes from a package) */}
        {data.metadata?.packageName && (
          <span style={{
            fontSize:     9,
            color:        'var(--t3)',
            opacity:      0.6,
            flexShrink:   0,
            maxWidth:     70,
            overflow:     'hidden',
            textOverflow: 'ellipsis',
            whiteSpace:   'nowrap',
          }}>
            {String(data.metadata.packageName)}.
          </span>
        )}

        <span
          title={data.label}
          style={{
            fontWeight:   600,
            fontSize:     11,
            color:        'var(--t1)',
            flex:         1,
            overflow:     'hidden',
            textOverflow: 'ellipsis',
            whiteSpace:   'nowrap',
          }}
        >
          {data.label}
        </span>

        <span style={{
          fontSize:      8,
          padding:       '1px 5px',
          borderRadius:  2,
          fontFamily:    'var(--mono)',
          border:        '0.5px solid color-mix(in srgb, var(--suc) 25%, transparent)',
          color:         'var(--suc)',
          opacity:       0.65,
          flexShrink:    0,
          letterSpacing: '0.03em',
        }}>
          {(data.metadata?.routineKind as string) ?? 'ROUTINE'}
        </span>
      </div>

      <Handle type="source" position={Position.Right} style={{ background: 'var(--suc)', zIndex: 5 }} />
    </div>
  );
});

RoutineGroupNode.displayName = 'RoutineGroupNode';
