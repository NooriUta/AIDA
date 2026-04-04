import { memo } from 'react';
import { Handle, Position, type NodeProps, type Node } from '@xyflow/react';
import { FileCode } from 'lucide-react';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData, ColumnInfo } from '../../../types/domain';

export type StatementNodeType = Node<DaliNodeData>;

const MAX_VISIBLE_COLS = 5;

function OutputColRow({ col }: { col: ColumnInfo }) {
  return (
    <div style={{
      display:     'flex',
      alignItems:  'center',
      gap:         'var(--seer-space-2)',
      padding:     '3px 10px',
      borderTop:   '1px solid var(--bd)',
      fontSize:    '12px',
      position:    'relative',
    }}>
      <Handle
        type="source"
        position={Position.Right}
        id={`src-${col.id}`}
        style={{ background: 'var(--suc)', width: '6px', height: '6px', right: '-4px' }}
      />
      <Handle
        type="target"
        position={Position.Left}
        id={`tgt-${col.id}`}
        style={{ background: 'var(--suc)', width: '6px', height: '6px', left: '-4px' }}
      />
      <span className="mono" style={{
        color:        'var(--t1)',
        flex:         1,
        overflow:     'hidden',
        textOverflow: 'ellipsis',
        whiteSpace:   'nowrap',
        fontSize:     '12px',
      }}>
        {col.name}
      </span>
    </div>
  );
}

export const StatementNode = memo(({ data, selected, id }: NodeProps<StatementNodeType>) => {
  const { selectNode } = useLoomStore();
  const columns  = data.columns ?? [];
  const visible  = columns.slice(0, MAX_VISIBLE_COLS);
  const overflow = columns.length - MAX_VISIBLE_COLS;

  return (
    <div
      className={`loom-node${selected ? ' selected' : ''}`}
      style={{
        background:      'var(--bg2)',
        borderLeftWidth: '3px',
        borderLeftColor: selected ? 'var(--acc)' : 'var(--suc)',
        minWidth:        '220px',
        padding:         0,
        overflow:        'hidden',
      }}
      onClick={() => selectNode(id)}
    >
      <Handle type="target" position={Position.Left}  style={{ background: 'var(--suc)', zIndex: 5 }} />

      {/* Header */}
      <div style={{
        padding:      'var(--seer-space-2) var(--seer-space-3)',
        display:      'flex',
        alignItems:   'center',
        gap:          'var(--seer-space-2)',
        background:   'var(--bg3)',
        borderBottom: '1px solid var(--bd)',
      }}>
        <FileCode size={13} color="var(--suc)" strokeWidth={1.5} />
        <div style={{ flex: 1, overflow: 'hidden' }}>
          <div style={{
            fontWeight:   600,
            fontSize:     '13px',
            color:        'var(--t1)',
            overflow:     'hidden',
            textOverflow: 'ellipsis',
            whiteSpace:   'nowrap',
          }}>
            {data.label || 'Statement'}
          </div>
          {columns.length > 0 && (
            <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: '1px' }}>
              {columns.length} output col{columns.length !== 1 ? 's' : ''}
            </div>
          )}
        </div>
        <span style={{
          fontSize:      '8px',
          padding:       '1px 5px',
          borderRadius:  2,
          fontFamily:    'monospace',
          border:        '0.5px solid var(--suc)',
          color:         'var(--suc)',
          opacity:       0.65,
          flexShrink:    0,
          letterSpacing: '0.03em',
        }}>
          STMT
        </span>
      </div>

      {/* Output column rows */}
      {visible.map((col) => (
        <OutputColRow key={col.id} col={col} />
      ))}

      {overflow > 0 && (
        <div style={{
          padding:   '3px var(--seer-space-3)',
          borderTop: '1px solid var(--bd)',
          fontSize:  '11px',
          color:     'var(--acc)',
        }}>
          +{overflow} more
        </div>
      )}

      <Handle type="source" position={Position.Right} style={{ background: 'var(--suc)', zIndex: 5 }} />
    </div>
  );
});

StatementNode.displayName = 'StatementNode';
