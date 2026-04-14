import { memo } from 'react';
import { KeyRound, Link2, Table2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import type { DaliNodeData, ColumnInfo } from '../../types/domain';
import { InspectorSection, InspectorRow } from './InspectorSection';

interface Props { data: DaliNodeData; nodeId: string }

// ── Header card ─────────────────────────────────────────────────────────────
// Mirrors the canvas TableNode header: Table2 icon, small schema label above,
// bold table name, column-count subline with optional data-source badge.
// Background uses var(--bg0) (darkest) + a left-accent border so the header
// reads as a distinct heading zone compared to the content area.

function TableHeaderCard({
  label, schema, columnCount, dataSource,
}: {
  label: string;
  schema: string | undefined;
  columnCount: number;
  dataSource: string | undefined;
}) {
  const { t } = useTranslation();
  return (
    <div
      role="heading"
      aria-level={2}
      style={{
        display:      'flex',
        alignItems:   'flex-start',
        gap:          'var(--seer-space-2)',
        padding:      '12px 14px',
        background:   'var(--bg0)',
        borderBottom: '1px solid var(--bd)',
        borderLeft:   '3px solid var(--acc)',
      }}
    >
      <Table2 size={14} color="var(--acc)" strokeWidth={1.5} style={{ flexShrink: 0, marginTop: 2 }} />
      <div style={{ flex: 1, overflow: 'hidden' }}>
        {schema && (
          <div style={{
            fontSize:     '9px',
            color:        'var(--t3)',
            opacity:      0.7,
            overflow:     'hidden',
            textOverflow: 'ellipsis',
            whiteSpace:   'nowrap',
            marginBottom: 2,
            letterSpacing: '0.03em',
            textTransform: 'uppercase',
          }}>
            {schema}
          </div>
        )}
        <div
          title={label}
          style={{
            fontWeight:   700,
            fontSize:     '13px',
            color:        'var(--t1)',
            overflow:     'hidden',
            textOverflow: 'ellipsis',
            whiteSpace:   'nowrap',
            letterSpacing: '0.02em',
          }}
        >
          {label}
        </div>
        <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: 2, display: 'flex', alignItems: 'center', gap: 5 }}>
          {columnCount} {t('nodes.columns')}
          {dataSource && (
            <span style={{
              fontSize: 8, padding: '1px 4px', borderRadius: 2, flexShrink: 0,
              fontWeight: 600, fontFamily: 'var(--mono)', letterSpacing: '0.03em',
              background: dataSource === 'master'
                ? 'color-mix(in srgb, var(--suc) 15%, transparent)'
                : 'color-mix(in srgb, var(--wrn) 15%, transparent)',
              border: `0.5px solid ${dataSource === 'master' ? 'var(--suc)' : 'var(--wrn)'}`,
              color: dataSource === 'master' ? 'var(--suc)' : 'var(--wrn)',
            }}>
              {dataSource}
            </span>
          )}
        </div>
      </div>
    </div>
  );
}

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
        fontFamily: 'var(--mono)',
      }}>
        {col.name}
      </span>
      {col.type && (
        <span style={{ color: 'var(--t3)', fontSize: '10px', flexShrink: 0 }}>{col.type}</span>
      )}
      {col.isPrimaryKey && (
        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 2 }}>
          <KeyRound size={9} color="var(--wrn)" strokeWidth={2} />
          <ColBadge label="PK" color="var(--wrn)" />
        </span>
      )}
      {col.isForeignKey && (
        <span style={{ display: 'inline-flex', alignItems: 'center', gap: 2 }}>
          <Link2 size={9} color="var(--inf)" strokeWidth={2} />
          <ColBadge label="FK" color="var(--inf)" />
        </span>
      )}
    </div>
  );
}

export const InspectorTable = memo(({ data, nodeId }: Props) => {
  const { t } = useTranslation();
  const columns = data.columns ?? [];
  const dataSource = typeof data.metadata?.dataSource === 'string' ? data.metadata.dataSource : undefined;

  return (
    <>
      <TableHeaderCard
        label={data.label}
        schema={data.schema}
        columnCount={columns.length}
        dataSource={dataSource}
      />

      <InspectorSection title={t('inspector.properties')}>
        {/* @rid row + standard ID row for fast copy-paste into ArcadeDB
            queries (`WHERE @rid = '#25:1234'`). Same value in both — kept
            for debugging ergonomics. Table label / schema / type are
            already shown in the header card above, so they're omitted here. */}
        <InspectorRow label="@rid" value={nodeId} />
        <InspectorRow label={t('inspector.id')} value={nodeId} />
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
