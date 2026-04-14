import { memo } from 'react';
import { Handle, Position, type NodeProps, type Node } from '@xyflow/react';
import { Columns3, Zap } from 'lucide-react';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData } from '../../../types/domain';
import { NodeExpandButtons } from './NodeExpandButtons';

export type ColumnNodeType = Node<DaliNodeData>;

export const ColumnNode = memo(({ data, selected, id }: NodeProps<ColumnNodeType>) => {
  const { selectNode } = useLoomStore();
  const isAtom       = data.nodeType === 'DaliAtom';
  const isOutputCol  = data.nodeType === 'DaliOutputColumn';

  // DaliColumn → --inf, DaliAtom → --wrn, DaliOutputColumn → --suc
  const stripeColour = isAtom ? 'var(--wrn)' : isOutputCol ? 'var(--suc)' : 'var(--inf)';
  const iconColour   = isAtom ? 'var(--wrn)' : isOutputCol ? 'var(--suc)' : 'var(--inf)';

  return (
    <div
      className={`loom-node${selected ? ' selected' : ''}`}
      style={{
        background: 'var(--bg2)',
        borderLeftWidth: '3px',
        borderLeftColor: selected ? 'var(--acc)' : stripeColour,
        minWidth: '170px',
        padding: 0,
      }}
      onClick={() => selectNode(id)}
    >
      <NodeExpandButtons nodeId={id} show />
      <Handle type="target" position={Position.Left}  style={{ background: 'var(--inf)' }} />

      <div style={{ padding: 'var(--seer-space-2) var(--seer-space-3)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-2)', marginBottom: '2px' }}>
          {isAtom
            ? <Zap     size={12} color={iconColour} strokeWidth={1.5} />
            : <Columns3 size={12} color={iconColour} strokeWidth={1.5} />
          }
          <span className="mono" style={{ fontWeight: 500, fontSize: '12px', color: 'var(--t1)', flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {data.label}
          </span>
        </div>

        {isAtom && data.operation && (
          <div className="mono" style={{ fontSize: '11px', color: 'var(--wrn)', marginTop: '2px', opacity: 0.9 }}>
            {data.operation}
          </div>
        )}
        {!isAtom && data.dataType && (
          <div className="mono" style={{ fontSize: '11px', color: 'var(--t3)', marginTop: '1px' }}>
            {data.dataType}
          </div>
        )}
      </div>

      <Handle type="source" position={Position.Right} style={{ background: 'var(--inf)' }} />
    </div>
  );
});

ColumnNode.displayName = 'ColumnNode';
