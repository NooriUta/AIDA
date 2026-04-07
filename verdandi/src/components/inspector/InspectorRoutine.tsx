import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import type { DaliNodeData } from '../../types/domain';
import { InspectorSection, InspectorRow } from './InspectorSection';

interface Props { data: DaliNodeData; nodeId: string }

export const InspectorRoutine = memo(({ data, nodeId }: Props) => {
  const { t } = useTranslation();
  const routineKind = typeof data.metadata?.routineKind === 'string' ? data.metadata.routineKind : '';

  return (
    <InspectorSection title={t('inspector.properties')}>
      <InspectorRow label={t('inspector.label')}   value={data.label} />
      <InspectorRow label={t('inspector.type')}    value={data.nodeType} />
      {routineKind && <InspectorRow label={t('inspector.kind')}  value={routineKind} />}
      {data.language && <InspectorRow label={t('inspector.language')} value={data.language} />}
      <InspectorRow label={t('inspector.id')}      value={nodeId} />
    </InspectorSection>
  );
});

InspectorRoutine.displayName = 'InspectorRoutine';
