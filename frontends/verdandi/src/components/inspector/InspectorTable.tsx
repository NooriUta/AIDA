import { memo, useState, useCallback } from 'react';
import { KeyRound, Link2, Table2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import type { DaliNodeData, ColumnInfo } from '../../types/domain';
import { InspectorSection, InspectorRow } from './InspectorSection';

interface Props { data: DaliNodeData; nodeId: string }

type TableTab = 'overview' | 'sql';

// ── Header card ──────────────────────────────────────────────────────────────

function TableHeaderCard({
  label, schema, columnCount, dataSource, onSchemaClick,
}: {
  label: string;
  schema: string | undefined;
  columnCount: number;
  dataSource: string | undefined;
  onSchemaClick?: () => void;
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
          onSchemaClick ? (
            <button onClick={onSchemaClick} title={`Открыть ${schema} в Loom`} style={{
              fontSize: '9px', color: 'var(--acc)',
              overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
              marginBottom: 2, letterSpacing: '0.03em', textTransform: 'uppercase',
              background: 'none', border: 'none', padding: 0, cursor: 'pointer',
              textDecoration: 'underline', textDecorationStyle: 'dotted',
              textAlign: 'left', fontFamily: 'inherit', display: 'block',
            }}>
              ◈ {schema}
            </button>
          ) : (
            <div style={{
              fontSize: '9px', color: 'var(--t3)', opacity: 0.7,
              overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
              marginBottom: 2, letterSpacing: '0.03em', textTransform: 'uppercase',
            }}>
              {schema}
            </div>
          )
        )}
        <div
          title={label}
          style={{
            fontWeight: 700, fontSize: '13px', color: 'var(--t1)',
            overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
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

// ── Tab bar ──────────────────────────────────────────────────────────────────

function TabBar({ active, onChange }: { active: TableTab; onChange: (t: TableTab) => void }) {
  const { t } = useTranslation();
  return (
    <div role="tablist" style={{
      display: 'flex', borderBottom: '1px solid var(--bd)',
      background: 'var(--bg1)', position: 'sticky', top: 0, zIndex: 1,
    }}>
      <TabBtn active={active === 'overview'} onClick={() => onChange('overview')} label={t('inspector.tabMain')} />
      <TabBtn active={active === 'sql'}      onClick={() => onChange('sql')}      label="DDL" />
    </div>
  );
}

function TabBtn({ active, onClick, label }: { active: boolean; onClick: () => void; label: string }) {
  return (
    <button role="tab" aria-selected={active} onClick={onClick} style={{
      flex: 1, padding: '8px 6px', background: 'transparent', border: 'none',
      borderBottom: `2px solid ${active ? 'var(--acc)' : 'transparent'}`,
      color: active ? 'var(--acc)' : 'var(--t2)',
      fontSize: '9px', fontWeight: active ? 700 : 500, letterSpacing: '0.06em',
      textTransform: 'uppercase', cursor: 'pointer',
      transition: 'color 0.12s, border-color 0.12s, background 0.08s',
      fontFamily: 'inherit',
    }}
      onMouseEnter={(e) => { if (!active) (e.currentTarget as HTMLElement).style.background = 'var(--bg2)'; }}
      onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
    >
      {label}
    </button>
  );
}

// ── Column row ───────────────────────────────────────────────────────────────

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
      padding: '3px 10px', borderTop: '1px solid var(--bd)',
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

// ── DDL panel ────────────────────────────────────────────────────────────────

function DdlPanel({ ddlText }: { ddlText: string }) {
  const { t } = useTranslation();
  const [copied, setCopied] = useState(false);

  const handleCopy = useCallback(() => {
    if (!ddlText) return;
    navigator.clipboard.writeText(ddlText).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    });
  }, [ddlText]);

  if (!ddlText) {
    return (
      <div style={{ padding: '16px 12px', fontSize: '11px', color: 'var(--t3)' }}>
        {t('inspector.noDdl', { defaultValue: 'DDL недоступен' })}
      </div>
    );
  }

  return (
    <div style={{ padding: '8px 10px' }}>
      <div style={{ display: 'flex', justifyContent: 'flex-end', paddingBottom: 6 }}>
        <button
          onClick={handleCopy}
          style={{
            fontSize: '9px', fontWeight: 600, padding: '3px 10px', borderRadius: 3,
            background: copied ? 'var(--suc)' : 'var(--bg3)',
            border: '1px solid var(--bd)',
            color: copied ? 'var(--bg0)' : 'var(--t2)',
            cursor: 'pointer', letterSpacing: '0.04em', textTransform: 'uppercase',
            transition: 'background 0.15s, color 0.15s',
          }}
        >
          {copied ? t('inspector.copied', { defaultValue: 'Скопировано' }) : t('inspector.copySql', { defaultValue: 'Копировать' })}
        </button>
      </div>
      <pre style={{
        padding: '8px 10px', margin: 0,
        fontSize: '11px', lineHeight: '1.5',
        color: 'var(--t1)', background: 'var(--bg0)',
        border: '1px solid var(--bd)', borderRadius: 4,
        maxHeight: 'calc(100vh - 280px)', overflow: 'auto',
        whiteSpace: 'pre', fontFamily: 'var(--mono)',
      }}>
        {ddlText}
      </pre>
    </div>
  );
}

// ── Main component ────────────────────────────────────────────────────────────

export const InspectorTable = memo(({ data, nodeId }: Props) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [tab, setTab] = useState<TableTab>('overview');
  const columns   = data.columns ?? [];
  const dataSource = typeof data.metadata?.dataSource === 'string' ? data.metadata.dataSource : undefined;
  const ddlText    = typeof data.metadata?.ddlText    === 'string' ? data.metadata.ddlText    : '';
  const schema     = data.schema ?? (typeof data.metadata?.schema === 'string' ? data.metadata.schema : undefined);

  const openSchemaInLoom = schema
    ? () => navigate(`/knot?schema=${encodeURIComponent(schema)}`)
    : undefined;

  return (
    <>
      <TableHeaderCard
        label={data.label}
        schema={schema}
        columnCount={columns.length}
        dataSource={dataSource}
        onSchemaClick={openSchemaInLoom}
      />

      <TabBar active={tab} onChange={setTab} />

      {tab === 'overview' && (
        <div role="tabpanel">
          <InspectorSection title={t('inspector.properties')}>
            <InspectorRow label="@rid" value={nodeId} />
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
        </div>
      )}

      {tab === 'sql' && (
        <div role="tabpanel">
          <DdlPanel ddlText={ddlText} />
        </div>
      )}
    </>
  );
});

InspectorTable.displayName = 'InspectorTable';
