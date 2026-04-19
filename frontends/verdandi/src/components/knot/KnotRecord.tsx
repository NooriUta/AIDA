import { memo } from 'react';
import { useTranslation } from 'react-i18next';

const REC_COLOR = '#B87AA8';

interface KnotRecordField {
  name:        string;
  dataType?:   string;
  isRowType?:  boolean;
}

interface KnotRecordEntry {
  geoid:       string;
  name:        string;
  packageName?: string;
  fields:      KnotRecordField[];
}

interface Props {
  records: KnotRecordEntry[];
}

function FieldRow({ field }: { field: KnotRecordField }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center', gap: 8,
      padding: '4px 16px', borderTop: '1px solid var(--bd)',
      fontSize: '12px',
    }}>
      <span style={{
        flex: 1, fontFamily: 'var(--mono)', color: 'var(--t1)',
        overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
      }}>
        {field.name}
      </span>
      {field.dataType && (
        <span style={{ color: 'var(--t3)', fontSize: '11px', flexShrink: 0 }}>
          {field.dataType}
        </span>
      )}
      {field.isRowType && (
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

function RecordCard({ record }: { record: KnotRecordEntry }) {
  return (
    <div style={{
      border: '1px solid var(--bd)', borderRadius: 6,
      overflow: 'hidden', marginBottom: 12,
    }}>
      <div style={{
        display: 'flex', alignItems: 'center', gap: 8,
        padding: '7px 12px',
        background: `color-mix(in srgb, ${REC_COLOR} 8%, var(--bg1))`,
        borderBottom: '1px solid var(--bd)',
        borderLeft: `3px solid ${REC_COLOR}`,
      }}>
        <span style={{
          fontSize: '11px', fontWeight: 700, color: 'var(--t1)',
          fontFamily: 'var(--mono)',
          overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        }}>
          {record.name}
        </span>
        <span style={{
          fontSize: '9px', padding: '1px 6px', borderRadius: 3,
          background: `color-mix(in srgb, ${REC_COLOR} 18%, transparent)`,
          color: REC_COLOR,
          border: `0.5px solid color-mix(in srgb, ${REC_COLOR} 40%, transparent)`,
          flexShrink: 0, fontWeight: 700, letterSpacing: '0.04em',
        }}>
          RECORD
        </span>
        <span style={{ flex: 1 }} />
        <span style={{ fontSize: '10px', color: 'var(--t3)', flexShrink: 0 }}>
          {record.fields.length} fields
        </span>
      </div>
      {record.fields.map((f, i) => <FieldRow key={i} field={f} />)}
    </div>
  );
}

// KnotRecord — shows PL/SQL record types used in the selected session.
// Integration into KnotPage requires backend: add `records` to KnotReport
// (SHUTTLE KnotService) and extend the KNOT_REPORT GraphQL query.
// Tracked in Sprint B: Backend Data Layer.
export const KnotRecord = memo(({ records }: Props) => {
  const { t } = useTranslation();

  if (records.length === 0) {
    return (
      <div style={{ padding: '32px 16px', textAlign: 'center', color: 'var(--t3)', fontSize: '13px' }}>
        {t('knot.noRecords', 'No PL/SQL records in this session')}
      </div>
    );
  }

  return (
    <div style={{ padding: '12px 16px' }}>
      {records.map(r => <RecordCard key={r.geoid} record={r} />)}
    </div>
  );
});

KnotRecord.displayName = 'KnotRecord';
