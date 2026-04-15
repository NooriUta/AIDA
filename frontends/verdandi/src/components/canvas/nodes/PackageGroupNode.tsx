// src/components/canvas/nodes/PackageGroupNode.tsx
// L2 AGG — Package compound/group container.
// Renders as a wide bordered box with a package header; child RoutineNodes
// are placed inside by React Flow's parentId mechanism.
// Size is pre-computed in transformExplore (compound ELK mode).

import { memo } from 'react';
import { Handle, Position, type NodeProps, type Node } from '@xyflow/react';
import { Package } from 'lucide-react';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData } from '../../../types/domain';

export type PackageGroupNodeType = Node<DaliNodeData>;

const PKG_COLOR = '#A8B860';   // lime-green — same as WRITES_TO / DATA_FLOW accent

export const PackageGroupNode = memo(({ data, selected, id }: NodeProps<PackageGroupNodeType>) => {
  const { selectNode, drillDown } = useLoomStore();

  return (
    <div
      style={{
        width:        '100%',
        height:       '100%',
        border:       selected
          ? `1.5px solid ${PKG_COLOR}`
          : `1.5px solid color-mix(in srgb, ${PKG_COLOR} 30%, transparent)`,
        borderRadius: 'var(--seer-radius-md)',
        background:   `color-mix(in srgb, ${PKG_COLOR} 5%, var(--bg2))`,
        position:     'relative',
        overflow:     'visible',
      }}
      onClick={(e) => { e.stopPropagation(); selectNode(id); }}
      onDoubleClick={(e) => {
        e.stopPropagation();
        if (data.childrenAvailable) drillDown(id, data.label, data.nodeType);
      }}
    >
      <Handle type="target" position={Position.Left}
        style={{ background: PKG_COLOR, zIndex: 5 }} />

      {/* Header */}
      <div style={{
        padding:      '7px 12px 5px',
        borderBottom: `0.5px solid color-mix(in srgb, ${PKG_COLOR} 25%, transparent)`,
        borderRadius: 'var(--seer-radius-sm) var(--seer-radius-sm) 0 0',
        display:      'flex',
        alignItems:   'center',
        gap:          6,
        background:   `color-mix(in srgb, ${PKG_COLOR} 8%, transparent)`,
        userSelect:   'none',
        pointerEvents: 'none',
      }}>
        <Package size={13} color={PKG_COLOR} strokeWidth={1.5} />
        <span
          title={data.label}
          style={{
            fontWeight:   700,
            fontSize:     '12px',
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
          fontSize:      '8px',
          padding:       '1px 5px',
          borderRadius:  2,
          fontFamily:    'var(--mono)',
          border:        `0.5px solid color-mix(in srgb, ${PKG_COLOR} 40%, transparent)`,
          color:         PKG_COLOR,
          opacity:       0.75,
          flexShrink:    0,
          letterSpacing: '0.03em',
          fontWeight:    600,
        }}>
          PKG
        </span>
      </div>

      <Handle type="source" position={Position.Right}
        style={{ background: PKG_COLOR, zIndex: 5 }} />
    </div>
  );
});

PackageGroupNode.displayName = 'PackageGroupNode';
