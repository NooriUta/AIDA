import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import type { DaliNodeData } from '../../types/domain';
import { InspectorSection, InspectorRow } from './InspectorSection';

interface Props { data: DaliNodeData; nodeId: string }

// ── Join type badge ──────────────────────────────────────────────────────────

const JOIN_COLORS: Record<string, string> = {
  INNER: '#88B8A8', LEFT: '#A8B860', RIGHT: '#D4922A',
  FULL:  '#c85c5c', CROSS: '#7DBF78', NATURAL: '#88B8A8',
};

function JoinBadge({ joinType }: { joinType: string }) {
  const upper = joinType.toUpperCase();
  const color = JOIN_COLORS[upper] ?? 'var(--t3)';
  return (
    <span style={{
      fontSize: '10px', fontWeight: 700, letterSpacing: '0.06em',
      padding: '1px 6px', borderRadius: 4,
      background: `color-mix(in srgb, ${color} 18%, transparent)`,
      color, border: `1px solid color-mix(in srgb, ${color} 40%, transparent)`,
    }}>
      {upper} JOIN
    </span>
  );
}

export const InspectorJoin = memo(({ data, nodeId }: Props) => {
  const { t } = useTranslation();

  const joinType   = typeof data.metadata?.joinType   === 'string' ? data.metadata.joinType   : '';
  const leftTable  = typeof data.metadata?.leftTable  === 'string' ? data.metadata.leftTable  : '';
  const rightTable = typeof data.metadata?.rightTable === 'string' ? data.metadata.rightTable : '';
  const condition  = typeof data.metadata?.condition  === 'string' ? data.metadata.condition  : '';

  return (
    <>
      <InspectorSection title={t('inspector.properties')}>
        <InspectorRow label={t('inspector.label')} value={data.label} />
        <InspectorRow label={t('inspector.type')}  value={joinType ? <JoinBadge joinType={joinType} /> : 'JOIN'} />
        {leftTable  && <InspectorRow label={t('inspector.joinLeft')}  value={leftTable} />}
        {rightTable && <InspectorRow label={t('inspector.joinRight')} value={rightTable} />}
        {condition  && <InspectorRow label={t('inspector.joinOn')}    value={condition} />}
        <InspectorRow label={t('inspector.id')} value={nodeId} />
      </InspectorSection>
    </>
  );
});

InspectorJoin.displayName = 'InspectorJoin';
