// src/components/canvas/nodes/RecordNode.tsx
// Phase S2.4 — PL/SQL record container (BULK COLLECT / RETURNING INTO / %ROWTYPE).
// Renders analogously to TableNode: header with "REC" badge, body lists DaliRecordField
// children (from data.columns) with field_name + data_type.

import { memo } from 'react';
import { Handle, Position, type NodeProps, type Node } from '@xyflow/react';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData } from '../../../types/domain';

export type RecordNodeType = Node<DaliNodeData>;

export const RecordNode = memo(({ data, selected, id }: NodeProps<RecordNodeType>) => {
  const { selectNode } = useLoomStore();
  const fields = data.columns ?? [];

  return (
    <div
      className={`loom-node${selected ? ' selected' : ''}`}
      style={{
        background:      'var(--bg2)',
        borderLeftWidth: '3px',
        borderLeftColor: selected ? 'var(--acc)' : '#B87AA8',
        minWidth:        '160px',
        padding:         0,
        overflow:        'hidden',
      }}
      onClick={() => selectNode(id, data)}
    >
      <Handle type="target" position={Position.Left}  style={{ background: '#B87AA8' }} />

      {/* Header */}
      <div style={{
        padding:      'var(--seer-space-2) var(--seer-space-3)',
        display:      'flex',
        alignItems:   'center',
        gap:          'var(--seer-space-2)',
        background:   'var(--bg3)',
        borderBottom: fields.length > 0 ? '1px solid var(--bd)' : 'none',
      }}>
        {/* Record icon */}
        <svg width="12" height="12" viewBox="0 0 12 12" fill="none" style={{ flexShrink: 0 }}>
          <rect x="1" y="1" width="10" height="10" rx="1.5"
            stroke="#B87AA8" strokeWidth="1.2" fill="none" />
          <path d="M3 4h6M3 6h4M3 8h3"
            stroke="#B87AA8" strokeWidth="1" strokeLinecap="round" />
        </svg>
        <span
          title={data.label}
          style={{
            fontWeight:   600,
            fontSize:     '13px',
            color:        'var(--t1)',
            flex:         1,
            overflow:     'hidden',
            textOverflow: 'ellipsis',
            whiteSpace:   'nowrap',
          }}
        >
          {data.label}
        </span>
        {/* REC badge */}
        <span style={{
          fontSize:      '8px',
          padding:       '1px 5px',
          borderRadius:  2,
          fontFamily:    'var(--mono)',
          border:        '0.5px solid #B87AA8',
          color:         '#B87AA8',
          opacity:       0.8,
          flexShrink:    0,
          letterSpacing: '0.03em',
          fontWeight:    600,
        }}>
          REC
        </span>
      </div>

      {/* Field rows */}
      {fields.map((field) => (
        <div key={field.id} style={{
          display:    'flex',
          alignItems: 'center',
          gap:        'var(--seer-space-2)',
          padding:    '3px 10px',
          borderTop:  '1px solid var(--bd)',
          fontSize:   '12px',
          position:   'relative',
        }}>
          <Handle
            type="source"
            position={Position.Right}
            id={`src-${field.id}`}
            style={{ background: '#B87AA8', width: '6px', height: '6px', right: '-4px' }}
          />
          <Handle
            type="target"
            position={Position.Left}
            id={`tgt-${field.id}`}
            style={{ background: '#B87AA8', width: '6px', height: '6px', left: '-4px' }}
          />
          <span className="mono" style={{
            color:        'var(--t1)',
            flex:         1,
            overflow:     'hidden',
            textOverflow: 'ellipsis',
            whiteSpace:   'nowrap',
            fontSize:     '12px',
          }}>
            {field.name}
          </span>
          {field.type && (
            <span style={{ fontSize: '10px', color: 'var(--t3)', flexShrink: 0 }}>
              {field.type}
            </span>
          )}
          {/* Arrow badge when source_column_geoid is set (%ROWTYPE origin) */}
          {field.isForeignKey && (
            <span style={{
              fontSize:  '9px',
              color:     '#B87AA8',
              flexShrink: 0,
              opacity:   0.8,
            }}>→</span>
          )}
        </div>
      ))}

      <Handle type="source" position={Position.Right} style={{ background: '#B87AA8' }} />
    </div>
  );
});

RecordNode.displayName = 'RecordNode';
