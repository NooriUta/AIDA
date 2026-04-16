import { memo } from 'react';
import { Handle, Position, type NodeProps, type Node } from '@xyflow/react';
import { Package } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData } from '../../../types/domain';
import { NodeExpandButtons } from './NodeExpandButtons';

export type PackageNodeType = Node<DaliNodeData>;

export const PackageNode = memo(({ data, selected, id }: NodeProps<PackageNodeType>) => {
  const { drillDown, selectNode } = useLoomStore();
  const { t } = useTranslation();

  return (
    <div
      className={`loom-node${selected ? ' selected' : ''}`}
      style={{
        background: 'var(--bg2)',
        borderLeftWidth: '3px',
        borderLeftColor: selected ? 'var(--acc)' : 'var(--t3)',
        minWidth: '180px',
        padding: 0,
      }}
      onClick={() => selectNode(id)}
      onDoubleClick={() => {
        if (data.childrenAvailable) drillDown(id, data.label, data.nodeType);
      }}
    >
      <NodeExpandButtons nodeId={id} show />
      <Handle type="target" position={Position.Left}  style={{ background: 'var(--bd)' }} />

      <div style={{ padding: 'var(--seer-space-3)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 'var(--seer-space-2)', marginBottom: 'var(--seer-space-1)' }}>
          <Package size={14} color="var(--t3)" strokeWidth={1.5} />
          <span style={{ fontWeight: 600, fontSize: '13px', color: 'var(--t1)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {data.label}
          </span>
        </div>

        <div style={{ fontSize: '11px', color: 'var(--t3)' }}>
          {data.routinesCount !== undefined && (
            <span>{data.routinesCount} {t('nodes.routines')}</span>
          )}
        </div>

        {!!data.metadata?.description && (
          <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: 'var(--seer-space-1)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
            {String(data.metadata.description)}
          </div>
        )}
      </div>

      <Handle type="source" position={Position.Right} style={{ background: 'var(--bd)' }} />
    </div>
  );
});

PackageNode.displayName = 'PackageNode';
