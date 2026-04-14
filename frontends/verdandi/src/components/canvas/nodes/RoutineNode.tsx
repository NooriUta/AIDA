import { memo } from 'react';
import { Handle, Position, type NodeProps, type Node } from '@xyflow/react';
import { Workflow } from 'lucide-react';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData } from '../../../types/domain';
import { NodeExpandButtons } from './NodeExpandButtons';

export type RoutineNodeType = Node<DaliNodeData>;

export const RoutineNode = memo(({ data, selected, id }: NodeProps<RoutineNodeType>) => {
  const { selectNode } = useLoomStore();

  return (
    <div
      className={`loom-node${selected ? ' selected' : ''}`}
      style={{
        background: 'var(--bg2)',
        borderLeftWidth: '3px',
        borderLeftColor: selected ? 'var(--acc)' : 'var(--suc)',
        minWidth: '180px',
        padding: 0,
      }}
      onClick={() => selectNode(id)}
    >
      <NodeExpandButtons nodeId={id} show />
      <Handle type="target" position={Position.Left}  style={{ background: 'var(--suc)' }} />

      <div style={{ padding: 'var(--seer-space-3)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-2)', marginBottom: 'var(--seer-space-1)' }}>
          <Workflow size={13} color="var(--suc)" strokeWidth={1.5} />
          <span title={data.label} style={{ fontWeight: 600, fontSize: '13px', color: 'var(--t1)', flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {data.label}
          </span>
        </div>

        <div style={{ fontSize: '11px', color: 'var(--t3)', display: 'flex', gap: 'var(--seer-space-2)' }}>
          <span className="mono" style={{ letterSpacing: '0.07em', fontSize: '11px' }}>
            {(data.metadata?.routineKind as string) ?? data.language ?? 'SQL'}
          </span>
          {data.metadata?.description && (
            <span style={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              {String(data.metadata.description)}
            </span>
          )}
        </div>
      </div>

      <Handle type="source" position={Position.Right} style={{ background: 'var(--suc)' }} />
    </div>
  );
});

RoutineNode.displayName = 'RoutineNode';
