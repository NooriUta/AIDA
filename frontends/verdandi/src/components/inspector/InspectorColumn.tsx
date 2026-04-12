import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import type { DaliNodeData } from '../../types/domain';
import { InspectorSection, InspectorRow } from './InspectorSection';

interface Props { data: DaliNodeData; nodeId: string }

// ── Badge colour by type ─────────────────────────────────────────────────────
const TYPE_COLORS: Record<string, string> = {
  DaliColumn:       'var(--inf, #88B8A8)',
  DaliOutputColumn: 'var(--suc, #7DBF78)',
  DaliAtom:         'var(--wrn, #D4922A)',
  DaliParameter:    'var(--acc, #D4922A)',
  DaliVariable:     'var(--t2, #888)',
  DaliAffectedColumn: 'var(--danger, #c85c5c)',
};

function TypeBadge({ nodeType }: { nodeType: string }) {
  const short = nodeType.replace(/^Dali/, '');
  const color = TYPE_COLORS[nodeType] ?? 'var(--t3)';
  return (
    <span style={{
      fontSize: '10px', fontWeight: 700, letterSpacing: '0.06em',
      padding: '1px 6px', borderRadius: 4,
      background: `color-mix(in srgb, ${color} 18%, transparent)`,
      color, border: `1px solid color-mix(in srgb, ${color} 40%, transparent)`,
    }}>
      {short}
    </span>
  );
}

export const InspectorColumn = memo(({ data, nodeId }: Props) => {
  const { t } = useTranslation();

  const dataType  = typeof data.dataType  === 'string' ? data.dataType  : '';
  const operation = typeof data.operation  === 'string' ? data.operation : '';
  const schema    = typeof data.schema     === 'string' ? data.schema    : '';
  const tableName = typeof data.metadata?.tableName === 'string' ? data.metadata.tableName : '';

  return (
    <InspectorSection title={t('inspector.properties')}>
      <InspectorRow label={t('inspector.label')}   value={data.label} />
      <InspectorRow label={t('inspector.type')}    value={<TypeBadge nodeType={data.nodeType} />} />
      {dataType  && <InspectorRow label={t('inspector.dataType')}  value={dataType} />}
      {operation && <InspectorRow label={t('inspector.operation')} value={operation} />}
      {schema    && <InspectorRow label={t('inspector.schema')}    value={schema} />}
      {tableName && <InspectorRow label={t('inspector.table')}     value={tableName} />}
      <InspectorRow label={t('inspector.id')}      value={nodeId} />
    </InspectorSection>
  );
});

InspectorColumn.displayName = 'InspectorColumn';
