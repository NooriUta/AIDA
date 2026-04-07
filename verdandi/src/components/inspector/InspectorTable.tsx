import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import type { DaliNodeData, ColumnInfo } from '../../types/domain';
import { InspectorSection, InspectorRow } from './InspectorSection';

interface Props { data: DaliNodeData; nodeId: string }

function ColBadge({ label, color }: { label: string; color: string }) {
  return (
    <span style={{
      fontSize: '9px', fontWeight: 600, letterSpacing: '0.05em',
      padding: '1px 4px', borderRadius: 3,
      background: `color-mix(in srgb, ${color} 18%, transparent)`,
      color, border: `1px solid color-mix(in srgb, ${color} 40%, transparent)`,
      marginLeft: 4, flexShrink: 0,
    }}>
      {label}
    </span>
  );
}

function ColumnRow({ col }: { col: ColumnInfo }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center',
      padding: '3px 10px',
      borderTop: '1px solid var(--bd)',
      fontSize: '11px', gap: 4,
    }}>
      <span style={{
        flex: 1, color: 'var(--t1)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        fontFamily: 'var(--seer-font-mono, monospace)',
      }}>
        {col.name}
      </span>
      {col.type && (
        <span style={{ color: 'var(--t3)', fontSize: '10px', flexShrink: 0 }}>{col.type}</span>
      )}
      {col.isPrimaryKey && <ColBadge label="PK" color="var(--wrn)" />}
      {col.isForeignKey  && <ColBadge label="FK" color="var(--inf)" />}
    </div>
  );
}

export const InspectorTable = memo(({ data, nodeId }: Props) => {
  const { t } = useTranslation();
  const columns = data.columns ?? [];

  return (
    <>
      <InspectorSection title={t('inspector.properties')}>
        <InspectorRow label={t('inspector.label')}  value={data.label} />
        <InspectorRow label={t('inspector.type')}   value={data.nodeType} />
        {data.schema && <InspectorRow label={t('inspector.schema')} value={data.schema} />}
        <InspectorRow label={t('inspector.id')}     value={nodeId} />
      </InspectorSection>

      <InspectorSection title={`${t('inspector.columns')} (${columns.length})`} defaultOpen={columns.length > 0}>
        {columns.length === 0 ? (
          <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
            {t('inspector.noColumns')}
          </div>
        ) : (
          <div style={{ marginTop: 2 }}>
            {columns.map((col) => <ColumnRow key={col.id} col={col} />)}
          </div>
        )}
      </InspectorSection>
    </>
  );
});

InspectorTable.displayName = 'InspectorTable';
