import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import type { DaliNodeData, ColumnInfo } from '../../types/domain';
import { InspectorSection, InspectorRow } from './InspectorSection';

interface Props { data: DaliNodeData; nodeId: string }

const REC_COLOR = '#B87AA8';

// ── Header card ──────────────────────────────────────────────────────────────

function RecordHeaderCard({
  label, schema, packageName, routineName,
  onSchemaClick, onPackageClick, onRoutineClick,
}: {
  label: string;
  schema: string | null;
  packageName: string | null;
  routineName: string | null;
  onSchemaClick?: () => void;
  onPackageClick?: () => void;
  onRoutineClick?: () => void;
}) {
  return (
    <div
      role="heading"
      aria-level={2}
      style={{
        display: 'flex', alignItems: 'flex-start', gap: 8,
        padding: '12px 14px',
        background: 'var(--bg0)', borderBottom: '1px solid var(--bd)',
        borderLeft: `3px solid ${REC_COLOR}`,
      }}
    >
      <div style={{ flex: 1, overflow: 'hidden' }}>
        <RecordBreadcrumb label={schema}      onClick={onSchemaClick} />
        <RecordBreadcrumb label={packageName} onClick={onPackageClick} />
        <RecordBreadcrumb label={routineName} onClick={onRoutineClick} />
        <div title={label} style={{
          fontWeight: 700, fontSize: '13px', color: 'var(--t1)',
          overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
          letterSpacing: '0.02em',
        }}>
          {label}
        </div>
      </div>
      <span style={{
        fontSize: '10px', fontWeight: 700, letterSpacing: '0.06em',
        padding: '1px 6px', borderRadius: 4, flexShrink: 0,
        background: `color-mix(in srgb, ${REC_COLOR} 18%, transparent)`,
        color: REC_COLOR,
        border: `1px solid color-mix(in srgb, ${REC_COLOR} 40%, transparent)`,
      }}>
        RECORD
      </span>
    </div>
  );
}

// ── Breadcrumb link ───────────────────────────────────────────────────────────

function RecordBreadcrumb({ label, onClick }: { label: string | null | undefined; onClick?: () => void }) {
  if (!label) return null;
  if (onClick) {
    return (
      <button onClick={onClick} title={`Открыть ${label} в Loom`} style={{
        fontSize: '9px', color: 'var(--acc)',
        overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        marginBottom: 2, letterSpacing: '0.03em', textTransform: 'uppercase',
        background: 'none', border: 'none', padding: 0, cursor: 'pointer',
        textDecoration: 'underline', textDecorationStyle: 'dotted',
        textAlign: 'left', fontFamily: 'inherit', display: 'block',
      }}>
        ◈ {label}
      </button>
    );
  }
  return (
    <div style={{
      fontSize: '9px', color: 'var(--t3)', opacity: 0.7,
      overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
      marginBottom: 2, letterSpacing: '0.03em', textTransform: 'uppercase',
    }}>
      {label}
    </div>
  );
}

// ── Field row ─────────────────────────────────────────────────────────────────

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

// ── Main component ────────────────────────────────────────────────────────────

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

  const schema      = data.schema ?? (typeof data.metadata?.schema      === 'string' ? data.metadata.schema      as string : null);
  const packageName =                  typeof data.metadata?.packageName === 'string' ? data.metadata.packageName as string : null;
  const routineGeoid =                 typeof data.metadata?.routineGeoid === 'string' ? data.metadata.routineGeoid as string : null;
  const routineName = routineGeoid ? routineGeoid.split(':').slice(1).join(':') || routineGeoid : null;

  return (
    <>
      <RecordHeaderCard
        label={data.label}
        schema={schema}
        packageName={packageName}
        routineName={routineName}
        onSchemaClick={schema      ? () => navigate(`/knot?schema=${encodeURIComponent(schema)}`)           : undefined}
        onPackageClick={packageName ? () => navigate(`/knot?pkg=${encodeURIComponent(packageName)}`)         : undefined}
        onRoutineClick={packageName ? () => navigate(`/knot?pkg=${encodeURIComponent(packageName)}`)         : undefined}
      />

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
