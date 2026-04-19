import { memo } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';

interface Props {
  record: string;          // record label (from ?record=)
  pkg?: string;            // parent package name (from ?pkg=)
  schema?: string;         // schema name (from ?schema=)
  routineGeoid?: string;   // e.g. "PROCEDURE:BUILD_CUSTOMER" (from ?routine=)
}

const REC_COLOR = '#B87AA8';

// ── Header card ──────────────────────────────────────────────────────────────

function RecordPageHeader({
  record, pkg, schema, routineName,
  onSchemaClick, onPkgClick,
}: {
  record: string;
  pkg?: string;
  schema?: string;
  routineName?: string;
  onSchemaClick?: () => void;
  onPkgClick?: () => void;
}) {
  return (
    <div style={{
      borderLeft: `3px solid ${REC_COLOR}`,
      background: 'var(--bg0)',
      borderBottom: '1px solid var(--bd)',
      padding: '14px 20px',
      display: 'flex',
      alignItems: 'flex-start',
      gap: 10,
    }}>
      <div style={{ flex: 1, overflow: 'hidden' }}>
        {schema && (
          onSchemaClick ? (
            <button onClick={onSchemaClick} style={breadcrumbStyle('link')}>◈ {schema}</button>
          ) : (
            <div style={breadcrumbStyle('dim')}>{schema}</div>
          )
        )}
        {pkg && (
          onPkgClick ? (
            <button onClick={onPkgClick} style={breadcrumbStyle('link')}>◈ {pkg}</button>
          ) : (
            <div style={breadcrumbStyle('dim')}>{pkg}</div>
          )
        )}
        {routineName && (
          onPkgClick ? (
            <button onClick={onPkgClick} style={breadcrumbStyle('link')}>◈ {routineName}</button>
          ) : (
            <div style={breadcrumbStyle('dim')}>{routineName}</div>
          )
        )}
        <div style={{
          fontSize: 16, fontWeight: 700, color: 'var(--t1)',
          letterSpacing: '0.02em', overflow: 'hidden',
          textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        }}>
          {record}
        </div>
      </div>
      <span style={{
        fontSize: '11px', fontWeight: 700, letterSpacing: '0.06em',
        padding: '2px 8px', borderRadius: 4, flexShrink: 0, marginTop: 2,
        background: `color-mix(in srgb, ${REC_COLOR} 18%, transparent)`,
        color: REC_COLOR,
        border: `1px solid color-mix(in srgb, ${REC_COLOR} 40%, transparent)`,
      }}>
        RECORD
      </span>
    </div>
  );
}

function breadcrumbStyle(variant: 'link' | 'dim'): React.CSSProperties {
  return variant === 'link'
    ? {
        fontSize: '10px', color: 'var(--acc)',
        overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        marginBottom: 3, letterSpacing: '0.03em', textTransform: 'uppercase',
        background: 'none', border: 'none', padding: 0, cursor: 'pointer',
        textDecoration: 'underline', textDecorationStyle: 'dotted',
        textAlign: 'left', fontFamily: 'inherit', display: 'block',
      }
    : {
        fontSize: '10px', color: 'var(--t3)', opacity: 0.7,
        overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        marginBottom: 3, letterSpacing: '0.03em', textTransform: 'uppercase',
      };
}

// ── Property row ──────────────────────────────────────────────────────────────

function PropRow({ label, value }: { label: string; value: string }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'baseline', gap: 8,
      padding: '5px 0', borderBottom: '1px solid var(--bd)',
      fontSize: 12,
    }}>
      <span style={{ color: 'var(--t3)', minWidth: 100, flexShrink: 0 }}>{label}</span>
      <span style={{ color: 'var(--t1)', fontFamily: 'var(--mono)', wordBreak: 'break-all' }}>{value}</span>
    </div>
  );
}

// ── Main component ────────────────────────────────────────────────────────────

export const KnotRecord = memo(({ record, pkg, schema, routineGeoid }: Props) => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const routineName = routineGeoid
    ? routineGeoid.split(':').slice(1).join(':') || routineGeoid
    : undefined;

  const openPkg = pkg
    ? () => navigate(`/knot?pkg=${encodeURIComponent(pkg)}`)
    : undefined;

  const openSchema = schema
    ? () => navigate(`/knot?schema=${encodeURIComponent(schema)}`)
    : undefined;

  const openInLoom = pkg
    ? () => navigate(`/?pkg=${encodeURIComponent(pkg)}`)
    : schema
    ? () => navigate(`/?schema=${encodeURIComponent(schema)}`)
    : undefined;

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', overflow: 'hidden' }}>
      <RecordPageHeader
        record={record}
        pkg={pkg}
        schema={schema}
        routineName={routineName}
        onSchemaClick={openSchema}
        onPkgClick={openPkg}
      />

      <div style={{ flex: 1, overflowY: 'auto', padding: 24 }}>

        {/* Properties section */}
        <section style={{ marginBottom: 24 }}>
          <div style={{
            fontSize: 10, fontWeight: 500, letterSpacing: '0.1em',
            color: 'var(--t3)', textTransform: 'uppercase', marginBottom: 10,
          }}>
            {t('inspector.properties')}
          </div>
          <div style={{
            background: 'var(--bg1)', border: '1px solid var(--bd)',
            borderRadius: 6, padding: '0 16px',
          }}>
            <PropRow label="Record" value={record} />
            {pkg        && <PropRow label={t('inspector.package', { defaultValue: 'Package' })}  value={pkg} />}
            {schema     && <PropRow label={t('inspector.schema',  { defaultValue: 'Schema' })}   value={schema} />}
            {routineGeoid && <PropRow label="routineGeoid" value={routineGeoid} />}
          </div>
        </section>

        {/* Navigation hint */}
        <section style={{
          background: 'var(--bg1)', border: '1px solid var(--bd)',
          borderRadius: 6, padding: '14px 16px',
          fontSize: 12, color: 'var(--t3)',
          lineHeight: 1.6,
          marginBottom: 24,
        }}>
          {t('knot.record.hint', {
            defaultValue: 'Детальные данные полей (тип, %ROWTYPE) доступны в инспекторе при выборе узла на канвасе.',
          })}
        </section>

        {/* Action buttons */}
        <div style={{ display: 'flex', gap: 10 }}>
          {openPkg && (
            <button
              onClick={openPkg}
              style={actionButtonStyle}
              onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.borderColor = 'var(--acc)'; }}
              onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.borderColor = 'var(--bd)'; }}
            >
              ◈ {t('knot.record.openPkg', { defaultValue: 'Открыть пакет в KNOT' })}
            </button>
          )}
          {openInLoom && (
            <button
              onClick={openInLoom}
              style={{ ...actionButtonStyle, color: 'var(--t2)' }}
              onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.borderColor = 'var(--t2)'; }}
              onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.borderColor = 'var(--bd)'; }}
            >
              ◈ {t('knot.openInLoom', { defaultValue: 'Открыть в Loom' })}
            </button>
          )}
        </div>
      </div>
    </div>
  );
});

KnotRecord.displayName = 'KnotRecord';

const actionButtonStyle: React.CSSProperties = {
  display: 'inline-flex', alignItems: 'center', gap: 5,
  padding: '6px 14px', fontSize: 12, fontWeight: 500, fontFamily: 'inherit',
  background: 'var(--bg2)', border: '1px solid var(--bd)', borderRadius: 5,
  color: 'var(--acc)', cursor: 'pointer', transition: 'border-color 0.1s',
};
