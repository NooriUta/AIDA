import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import type { DaliNodeData, ColumnInfo } from '../../types/domain';
import { InspectorSection, InspectorRow } from './InspectorSection';

interface Props { data: DaliNodeData; nodeId: string }

const OP_COLORS: Record<string, string> = {
  INSERT: '#D4922A', UPDATE: '#D4922A', MERGE: '#D4922A', DELETE: '#c85c5c',
  SELECT: '#88B8A8', CTE: '#A8B860',   WITH: '#A8B860',  CREATE: '#7DBF78',
  DROP:   '#c85c5c', TRUNCATE: '#c85c5c', SQ: '#88B8A8', CURSOR: '#88B8A8',
};

function OpBadge({ op }: { op: string }) {
  const color = OP_COLORS[op] ?? 'var(--t3)';
  return (
    <span style={{
      fontSize: '10px', fontWeight: 700, letterSpacing: '0.06em',
      padding: '1px 6px', borderRadius: 4,
      background: `color-mix(in srgb, ${color} 18%, transparent)`,
      color, border: `1px solid color-mix(in srgb, ${color} 40%, transparent)`,
    }}>
      {op}
    </span>
  );
}

function OutputColRow({ col }: { col: ColumnInfo }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center',
      padding: '3px 10px', borderTop: '1px solid var(--bd)',
      fontSize: '11px',
    }}>
      <span style={{
        flex: 1, color: 'var(--t1)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        fontFamily: 'var(--seer-font-mono, monospace)',
      }}>
        {col.name}
      </span>
      {col.type && (
        <span style={{ color: 'var(--t3)', fontSize: '10px', marginLeft: 4 }}>{col.type}</span>
      )}
    </div>
  );
}

export const InspectorStatement = memo(({ data, nodeId }: Props) => {
  const { t } = useTranslation();
  const columns   = data.columns ?? [];
  const groupPath = Array.isArray(data.metadata?.groupPath) ? (data.metadata.groupPath as string[]) : [];
  const operation = typeof data.operation === 'string' ? data.operation : (data.metadata?.stmtType as string) ?? '';
  const fullLabel = typeof data.metadata?.fullLabel === 'string' ? data.metadata.fullLabel : data.label;

  return (
    <>
      <InspectorSection title={t('inspector.properties')}>
        <InspectorRow label={t('inspector.type')}      value={<OpBadge op={operation || data.nodeType} />} />
        <InspectorRow label={t('inspector.label')}     value={fullLabel} />
        {groupPath.length > 0 && (
          <InspectorRow label={t('inspector.path')} value={groupPath.join(' › ')} />
        )}
        <InspectorRow label={t('inspector.id')} value={nodeId} />
      </InspectorSection>

      <InspectorSection
        title={`${t('inspector.outputColumns')} (${columns.length})`}
        defaultOpen={columns.length > 0}
      >
        {columns.length === 0 ? (
          <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
            {t('inspector.noColumns')}
          </div>
        ) : (
          <div style={{ marginTop: 2 }}>
            {columns.map((col) => <OutputColRow key={col.id} col={col} />)}
          </div>
        )}
      </InspectorSection>
    </>
  );
});

InspectorStatement.displayName = 'InspectorStatement';
