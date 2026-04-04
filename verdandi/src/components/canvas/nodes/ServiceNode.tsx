// src/components/canvas/nodes/ServiceNode.tsx
// LOOM-024: L1 — Service node (второй уровень иерархии приложений)
// Double-click — scope filter (остаётся на L1, сужает граф до БД этого сервиса)

import { memo } from 'react';
import { Handle, Position, type NodeProps, type Node } from '@xyflow/react';
import { useTranslation } from 'react-i18next';
import { useLoomStore } from '../../../stores/loomStore';
import type { DaliNodeData } from '../../../types/domain';

export type ServiceNodeType = Node<DaliNodeData>;

export const ServiceNode = memo(({ data, selected, id }: NodeProps<ServiceNodeType>) => {
  const { pushL1Scope, selectNode } = useLoomStore();
  const { t } = useTranslation();

  const technology = (data.metadata?.technology as string | undefined) ?? '';
  const databaseCount = (data.metadata?.databaseCount as number | undefined) ?? 0;

  return (
    <div
      className={`loom-node${selected ? ' selected' : ''}`}
      style={{
        background: 'var(--bg2)',
        borderLeftWidth: '3px',
        borderLeftColor: selected ? 'var(--acc)' : 'var(--inf)',
        minWidth: '190px',
        padding: 0,
        boxShadow: selected ? '0 0 0 1px var(--inf)' : 'none',
      }}
      onClick={() => selectNode(id)}
      onDoubleClick={() => {
        // Scope filter — не меняет уровень
        pushL1Scope(id, data.label, 'DaliService');
      }}
    >
      <Handle type="target" position={Position.Left} style={{ background: 'var(--inf)' }} />

      <div style={{ padding: 'var(--seer-space-3)' }}>
        {/* Иконка + имя */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: 'var(--seer-space-2)',
          marginBottom: 'var(--seer-space-1)',
        }}>
          <ServiceIcon />
          <span style={{
            fontWeight: 600,
            fontSize: '13px',
            color: 'var(--t1)',
            overflow: 'hidden',
            textOverflow: 'ellipsis',
            whiteSpace: 'nowrap',
          }}>
            {data.label}
          </span>
        </div>

        {/* Технология + счётчик */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '11px' }}>
          {technology && (
            <span style={{
              background: 'var(--bg3)',
              border: '1px solid var(--bd)',
              borderRadius: '4px',
              padding: '1px 5px',
              color: 'var(--inf)',
              fontSize: '10px',
              fontFamily: 'monospace',
              letterSpacing: '0.03em',
              flexShrink: 0,
            }}>
              {technology}
            </span>
          )}
          {databaseCount > 0 && (
            <span style={{ color: 'var(--t3)' }}>
              {databaseCount} {t('nodes.databases')}
            </span>
          )}
        </div>

        {/* Хинт scope */}
        <div style={{
          marginTop: 'var(--seer-space-1)',
          fontSize: '10px',
          color: 'var(--inf)',
          opacity: 0.6,
        }}>
          ⊙ scope →
        </div>
      </div>

      <Handle type="source" position={Position.Right} style={{ background: 'var(--inf)' }} />
    </div>
  );
});

ServiceNode.displayName = 'ServiceNode';

// ─── Иконка сервиса ───────────────────────────────────────────────────────────
function ServiceIcon() {
  return (
    <svg width="13" height="13" viewBox="0 0 16 16" fill="none" style={{ flexShrink: 0 }}>
      <rect x="1" y="4" width="14" height="9" rx="2"
        stroke="var(--inf)" strokeWidth="1.4" fill="none" />
      <path d="M1 7.5h14" stroke="var(--inf)" strokeWidth="1" opacity="0.45" />
      <circle cx="4"  cy="5.75" r="0.9" fill="var(--inf)" />
      <circle cx="7"  cy="5.75" r="0.9" fill="var(--inf)" opacity="0.55" />
      <circle cx="10" cy="5.75" r="0.9" fill="var(--inf)" opacity="0.3" />
      <path d="M3.5 10.5h9" stroke="var(--inf)" strokeWidth="1.1"
        strokeLinecap="round" opacity="0.35" />
    </svg>
  );
}
