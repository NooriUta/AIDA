import { memo } from 'react';
import { Handle, Position, type NodeProps, type Node } from '@xyflow/react';
import { Workflow } from 'lucide-react';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData } from '../../../types/domain';
import { NodeExpandButtons } from './NodeExpandButtons';

export type RoutineNodeType = Node<DaliNodeData>;

export const RoutineNode = memo(({ data, selected, id }: NodeProps<RoutineNodeType>) => {
  const { selectNode } = useLoomStore();

  const routineKind  = (data.metadata?.routineKind as string | undefined) ?? 'ROUTINE';
  const schemaName   = data.metadata?.schemaName  as string | undefined;
  const packageName  = data.metadata?.packageName as string | undefined;

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
      <Handle type="target" position={Position.Left}  style={{ background: 'var(--suc)', zIndex: 5 }} />

      <div style={{ padding: 'var(--seer-space-2) var(--seer-space-3)' }}>
        {/* Schema / Package breadcrumb — shown when available (L2 AGG returns them) */}
        {(schemaName || packageName) && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 0, marginBottom: '2px' }}>
            {schemaName && (
              <div style={{
                fontSize: '9px', color: 'var(--t3)', opacity: 0.65,
                overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                lineHeight: '12px', letterSpacing: '0.02em',
              }}>
                {schemaName}
              </div>
            )}
            {packageName && packageName !== schemaName && (
              <div style={{
                fontSize: '9px', color: 'var(--t3)', opacity: 0.8,
                overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                lineHeight: '12px', letterSpacing: '0.02em',
              }}>
                {packageName}
              </div>
            )}
          </div>
        )}

        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-2)', marginBottom: 'var(--seer-space-1)' }}>
          <Workflow size={13} color="var(--suc)" strokeWidth={1.5} />
          <span title={data.label} style={{ fontWeight: 600, fontSize: '13px', color: 'var(--t1)', flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {data.label}
          </span>
          {/* Kind badge */}
          <span style={{
            fontSize:     '8px',
            padding:      '1px 5px',
            borderRadius: 2,
            fontFamily:   'var(--mono)',
            border:       '0.5px solid var(--suc)',
            color:        'var(--suc)',
            opacity:      0.8,
            flexShrink:   0,
            letterSpacing:'0.03em',
            fontWeight:   600,
          }}>
            {routineKind}
          </span>
        </div>
      </div>

      <Handle type="source" position={Position.Right} style={{ background: 'var(--suc)', zIndex: 5 }} />
    </div>
  );
});

RoutineNode.displayName = 'RoutineNode';
