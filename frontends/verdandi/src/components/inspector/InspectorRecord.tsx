import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
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
  const navigate = useNavigate();

  const fields: ColumnInfo[]  = Array.isArray(data.columns) ? data.columns : [];
  const packageName  = data.metadata?.packageName  as string | undefined;
  const routineGeoid = data.metadata?.routineGeoid as string | undefined;

  const openInKnot = () => {
    const params = new URLSearchParams();
    if (packageName) params.set('pkg', packageName);
    navigate(`/knot?${params.toString()}`);
  };

  return (
    <>
      {/* ── Properties ─────────────────────────────────────────────────── */}
      <InspectorSection title={t('inspector.properties')}>
        <InspectorRow label={t('inspector.label')} value={data.label} />
        <InspectorRow label={t('inspector.type')}  value={<RecBadge />} />
        {packageName  && <InspectorRow label={t('inspector.package')} value={packageName} />}
        {routineGeoid && <InspectorRow label={t('inspector.routine')} value={routineGeoid} />}
        <InspectorRow label={t('inspector.id')}    value={nodeId} />
        <div style={{ padding: '6px 10px 4px' }}>
          <button
            onClick={openInKnot}
            style={{
              display: 'inline-flex', alignItems: 'center', gap: 5,
              padding: '4px 10px',
              fontSize: 11, fontWeight: 500, fontFamily: 'inherit',
              background: 'var(--bg3)',
              border: '1px solid var(--bd)',
              borderRadius: 4,
              color: 'var(--acc)',
              cursor: 'pointer',
              transition: 'border-color 0.1s',
            }}
            onMouseEnter={e => { (e.currentTarget as HTMLElement).style.borderColor = 'var(--acc)'; }}
            onMouseLeave={e => { (e.currentTarget as HTMLElement).style.borderColor = 'var(--bd)'; }}
          >
            ◈ {t('contextMenu.openInKnot')}
          </button>
        </div>
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
