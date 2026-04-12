import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import type { DaliNodeData } from '../../types/domain';
import { InspectorSection, InspectorRow } from './InspectorSection';

interface Props { data: DaliNodeData; nodeId: string }

const TYPE_COLORS: Record<string, string> = {
  DaliParameter: 'var(--acc, #D4922A)',
  DaliVariable:  'var(--inf, #88B8A8)',
};

function ParamBadge({ nodeType }: { nodeType: string }) {
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

export const InspectorParameter = memo(({ data, nodeId }: Props) => {
  const { t } = useTranslation();

  const dataType  = typeof data.dataType  === 'string' ? data.dataType  : '';
  const direction = typeof data.metadata?.direction === 'string' ? data.metadata.direction : '';
  const routine   = typeof data.metadata?.routineName === 'string' ? data.metadata.routineName : '';
  const defValue  = typeof data.metadata?.defaultValue === 'string' ? data.metadata.defaultValue : '';

  return (
    <>
      <InspectorSection title={t('inspector.properties')}>
        <InspectorRow label={t('inspector.label')}     value={data.label} />
        <InspectorRow label={t('inspector.type')}      value={<ParamBadge nodeType={data.nodeType} />} />
        {dataType  && <InspectorRow label={t('inspector.dataType')}  value={dataType} />}
        {direction && <InspectorRow label={t('inspector.direction')} value={direction.toUpperCase()} />}
        {routine   && <InspectorRow label={t('inspector.routine')}   value={routine} />}
        {defValue  && <InspectorRow label={t('inspector.default')}   value={defValue} />}
        <InspectorRow label={t('inspector.id')} value={nodeId} />
      </InspectorSection>
    </>
  );
});

InspectorParameter.displayName = 'InspectorParameter';
