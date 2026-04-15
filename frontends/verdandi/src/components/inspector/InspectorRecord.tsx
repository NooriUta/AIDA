// src/components/inspector/InspectorRecord.tsx
// Phase S2.4 — Inspector panel for DaliRecord nodes selected on the L3 canvas.
// Shows record name, type badge, field list with data types and %ROWTYPE origin.

import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import type { DaliNodeData, ColumnInfo } from '../../types/domain';
import { InspectorSection, InspectorRow } from './InspectorSection';

interface Props { data: DaliNodeData; nodeId: string }

// ── Record type badge ─────────────────────────────────────────────────────────
const REC_COLOR = '#B87AA8';

function RecBadge() {
  return (
    <span style={{
      fontSize: '10px', fontWeight: 700, letterSpacing: '0.06em',
      padding: '1px 6px', borderRadius: 4,
      background: `color-mix(in srgb, ${REC_COLOR} 18%, transparent)`,
      color: REC_COLOR,
      border: `1px solid color-mix(in srgb, ${REC_COLOR} 40%, transparent)`,
    }}>
      RECORD
    </span>
  );
}

// ── Single field row ──────────────────────────────────────────────────────────
function FieldRow({ field }: { field: ColumnInfo }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center',
      padding: '3px 10px', borderTop: '1px solid var(--bd)',
      fontSize: '11px', gap: 6,
    }}>
      <span style={{
        flex: 1, color: 'var(--t1)', overflow: 'hidden',
        textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        fontFamily: 'var(--mono)',
      }}>
        {field.name}
      </span>
      {field.type && (
        <span style={{ color: 'var(--t3)', fontSize: '10px', flexShrink: 0 }}>
          {field.type}
        </span>
      )}
      {/* %ROWTYPE origin badge — isForeignKey is used as proxy for source_column_geoid */}
      {field.isForeignKey && (
        <span style={{
          fontSize: '9px', color: REC_COLOR, flexShrink: 0,
          border: `0.5px solid color-mix(in srgb, ${REC_COLOR} 40%, transparent)`,
          borderRadius: 3, padding: '0 4px',
          background: `color-mix(in srgb, ${REC_COLOR} 10%, transparent)`,
        }}>
          %ROWTYPE
        </span>
      )}
    </div>
  );
}

export const InspectorRecord = memo(({ data, nodeId }: Props) => {
  const { t } = useTranslation();

  const fields: ColumnInfo[] = Array.isArray(data.columns) ? data.columns : [];

  return (
    <>
      {/* ── Properties ─────────────────────────────────────────────────── */}
      <InspectorSection title={t('inspector.properties')}>
        <InspectorRow label={t('inspector.label')} value={data.label} />
        <InspectorRow label={t('inspector.type')}  value={<RecBadge />} />
        <InspectorRow label={t('inspector.id')}    value={nodeId} />
      </InspectorSection>

      {/* ── Fields ─────────────────────────────────────────────────────── */}
      <InspectorSection
        title={`${t('inspector.fields')} (${fields.length})`}
        defaultOpen={fields.length > 0}
      >
        {fields.length === 0 ? (
          <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
            {t('inspector.noFields')}
          </div>
        ) : (
          <div style={{ marginTop: 2 }}>
            {fields.map((f) => <FieldRow key={f.id} field={f} />)}
          </div>
        )}
      </InspectorSection>
    </>
  );
});

InspectorRecord.displayName = 'InspectorRecord';
