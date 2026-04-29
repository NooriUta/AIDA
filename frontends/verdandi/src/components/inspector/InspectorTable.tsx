import { memo, useState, useCallback, useMemo } from 'react';
import { KeyRound, Link2, Table2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import type { DaliNodeData, ColumnInfo } from '../../types/domain';
import { InspectorSection, InspectorRow } from './InspectorSection';
import { useLoomStore } from '../../stores/loomStore';
import { useKnotTableRoutines, useKnotColumnStatements } from '../../services/hooks';
import type { KnotTableUsage, KnotColumnUsage } from '../../services/lineage';

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

function ColumnRow({ col, tableGeoid }: { col: ColumnInfo; tableGeoid?: string }) {
  const { t } = useTranslation();
  const [expanded, setExpanded] = useState(false);
  const colGeoid = tableGeoid && col.name ? `${tableGeoid}.${col.name.toUpperCase()}` : undefined;
  const { data: usages, isFetching } = useKnotColumnStatements(colGeoid, expanded);

  return (
    <>
      <div style={{
        display: 'flex', alignItems: 'center',
        padding: '3px 10px', borderTop: '1px solid var(--bd)',
        fontSize: '11px', gap: 4,
      }}>
        <button
          onClick={() => setExpanded((e) => !e)}
          title={expanded ? 'Свернуть' : 'Показать использование'}
          style={{
            background: 'none', border: 'none', cursor: 'pointer', padding: 0,
            color: 'var(--t3)', fontSize: '10px', lineHeight: 1, flexShrink: 0,
            transition: 'color 0.1s',
          }}
          onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.color = 'var(--acc)'; }}
          onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.color = 'var(--t3)'; }}
        >
          {expanded ? '▾' : '▸'}
        </button>
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
      {expanded && (
        <div style={{ background: 'var(--bg1)', borderTop: '1px solid var(--bd)' }}>
          {isFetching && (
            <div style={{ padding: '4px 16px', fontSize: '10px', color: 'var(--t3)' }}>…</div>
          )}
          {!isFetching && usages && usages.length === 0 && (
            <div style={{ padding: '4px 16px', fontSize: '10px', color: 'var(--t3)' }}>
              {t('inspector.noUsage', { defaultValue: 'Нет использований' })}
            </div>
          )}
          {!isFetching && usages && usages.map((u: KnotColumnUsage, i: number) => (
            <div key={`${u.stmtGeoid}-${i}`} style={{
              padding: '2px 16px', fontSize: '10px', color: 'var(--t2)',
              fontFamily: 'var(--mono)', borderTop: '1px solid var(--bd)',
            }}>
              {u.routineName || u.routineGeoid} · <span style={{ color: 'var(--t3)' }}>{u.stmtType}</span>
            </div>
          ))}
        </div>
      )}
    </>
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
    }).catch(() => {});
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

// ── Routines analytics section ────────────────────────────────────────────────

const STMT_OP_COLORS: Record<string, string> = {
  SELECT: 'var(--suc)',
  INSERT: 'var(--inf)',
  UPDATE: 'var(--wrn)',
  DELETE: 'var(--danger)',
  MERGE:  'var(--acc)',
};

function StmtRow({ usage, onNavigate }: { usage: KnotTableUsage; onNavigate: () => void }) {
  const stmtType  = usage.stmtType || '?';
  const color     = STMT_OP_COLORS[stmtType] ?? 'var(--t3)';
  const stmtLabel = usage.stmtGeoid
    ? usage.stmtGeoid.split(':').pop() ?? usage.stmtGeoid
    : '—';
  return (
    <div
      onClick={onNavigate}
      style={{
        display: 'flex', alignItems: 'center', gap: 6,
        padding: '4px 10px', borderTop: '1px solid var(--bd)',
        fontSize: '11px', cursor: 'pointer',
      }}
      onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'var(--bg2)'; }}
      onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
    >
      <span style={{
        fontSize: '8px', fontWeight: 700, padding: '1px 4px', borderRadius: 2,
        flexShrink: 0, fontFamily: 'var(--mono)',
        border: `0.5px solid ${color}`, color,
      }}>{stmtType}</span>
      <span style={{
        flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        color: 'var(--t1)', fontFamily: 'var(--mono)',
      }}>{stmtLabel}</span>
      {usage.routineName && (
        <span style={{ fontSize: '9px', color: 'var(--t3)', flexShrink: 0 }}>
          {usage.routineName}
        </span>
      )}
    </div>
  );
}

function TableRoutinesSection({ nodeId }: { nodeId: string }) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { jumpTo } = useLoomStore();
  const [open, setOpen] = useState(false);
  const { data, isFetching } = useKnotTableRoutines(nodeId, open);

  const routines = useMemo(() => {
    if (!data) return [];
    const map = new Map<string, { geoid: string; name: string; edgeTypes: Set<string>; stmtCount: number }>();
    for (const u of data) {
      if (!u.routineGeoid) continue;
      const cur = map.get(u.routineGeoid) ?? { geoid: u.routineGeoid, name: u.routineName, edgeTypes: new Set<string>(), stmtCount: 0 };
      cur.edgeTypes.add(u.edgeType);
      cur.stmtCount++;
      map.set(u.routineGeoid, cur);
    }
    return [...map.values()];
  }, [data]);

  const handleNavigate = useCallback((geoid: string, name: string) => {
    jumpTo('L3', geoid, name || geoid, 'DaliRoutine', { focusNodeId: geoid });
    navigate('/');
  }, [jumpTo, navigate]);

  return (
    <InspectorSection
      title={`${t('inspector.routines', { defaultValue: 'Routines' })}${data ? ` (${routines.length})` : ''}`}
      defaultOpen={false}
      onToggle={setOpen}
    >
      {isFetching && (
        <div style={{ padding: '6px 10px', fontSize: '11px', color: 'var(--t3)' }}>
          {t('status.loading', { defaultValue: '…' })}
        </div>
      )}
      {!isFetching && open && routines.length === 0 && (
        <div style={{ padding: '6px 10px', fontSize: '11px', color: 'var(--t3)' }}>
          {t('inspector.noRoutines', { defaultValue: 'Нет routines' })}
        </div>
      )}
      {!isFetching && routines.map((r) => {
        const hasRead  = r.edgeTypes.has('READS_FROM');
        const hasWrite = r.edgeTypes.has('WRITES_TO');
        const badge    = hasRead && hasWrite ? 'RW' : hasWrite ? 'W' : 'R';
        const badgeColor = hasRead && hasWrite ? 'var(--wrn)' : hasWrite ? 'var(--danger)' : 'var(--suc)';
        return (
          <div
            key={r.geoid}
            onClick={() => handleNavigate(r.geoid, r.name)}
            style={{
              display: 'flex', alignItems: 'center', gap: 6,
              padding: '4px 10px', borderTop: '1px solid var(--bd)',
              fontSize: '11px', cursor: 'pointer',
            }}
            onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'var(--bg2)'; }}
            onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
          >
            <span style={{
              fontSize: '9px', fontWeight: 700, padding: '1px 4px', borderRadius: 3, flexShrink: 0,
              background: `color-mix(in srgb, ${badgeColor} 15%, transparent)`,
              color: badgeColor,
              border: `1px solid color-mix(in srgb, ${badgeColor} 40%, transparent)`,
            }}>
              {badge}
            </span>
            <span style={{ flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', color: 'var(--t1)', fontFamily: 'var(--mono)' }}>
              {r.name || r.geoid}
            </span>
            <span style={{ fontSize: '9px', color: 'var(--t3)', flexShrink: 0 }}>
              {r.stmtCount} stmt
            </span>
          </div>
        );
      })}
    </InspectorSection>
  );
}

// ── EK-02: Statements section ────────────────────────────────────────────────
// Derives unique DaliStatements from the existing knotTableRoutines response
// (each usage entry carries stmtGeoid + stmtType) — no extra backend query needed.

function TableStatementsSection({ nodeId }: { nodeId: string }) {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { jumpTo } = useLoomStore();
  const [open, setOpen] = useState(false);
  const { data, isFetching } = useKnotTableRoutines(nodeId, open);

  const stmts = useMemo(() => {
    if (!data) return [];
    const seen = new Map<string, KnotTableUsage>();
    for (const u of data) {
      if (u.stmtGeoid && !seen.has(u.stmtGeoid)) seen.set(u.stmtGeoid, u);
    }
    return [...seen.values()];
  }, [data]);

  const handleNavigate = useCallback((usage: KnotTableUsage) => {
    if (usage.stmtGeoid) {
      jumpTo('L3', usage.stmtGeoid, usage.stmtType || usage.stmtGeoid, 'DaliStatement', { focusNodeId: usage.stmtGeoid });
      navigate('/');
    }
  }, [jumpTo, navigate]);

  return (
    <InspectorSection
      title={`${t('inspector.statements', { defaultValue: 'Statements' })}${data ? ` (${stmts.length})` : ''}`}
      defaultOpen={false}
      onToggle={setOpen}
    >
      {isFetching && (
        <div style={{ padding: '6px 10px', fontSize: '11px', color: 'var(--t3)' }}>
          {t('status.loading', { defaultValue: '…' })}
        </div>
      )}
      {!isFetching && open && stmts.length === 0 && (
        <div style={{ padding: '6px 10px', fontSize: '11px', color: 'var(--t3)' }}>
          {t('inspector.noStatements', { defaultValue: 'Нет statements' })}
        </div>
      )}
      {!isFetching && stmts.map((u, i) => (
        <StmtRow key={`${u.stmtGeoid}-${i}`} usage={u} onNavigate={() => handleNavigate(u)} />
      ))}
    </InspectorSection>
  );
}

// ── Main component ────────────────────────────────────────────────────────────

export const InspectorTable = memo(({ data, nodeId }: Props) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const { jumpTo } = useLoomStore();
  const [tab, setTab] = useState<TableTab>('overview');
  const columns    = data.columns ?? [];
  const dataSource = typeof data.metadata?.dataSource === 'string' ? data.metadata.dataSource : undefined;
  const ddlText    = typeof data.metadata?.ddlText    === 'string' ? data.metadata.ddlText    : '';
  const schema     = data.schema ?? (typeof data.metadata?.schema === 'string' ? data.metadata.schema : undefined);
  const tableGeoid = typeof data.metadata?.tableGeoid === 'string' ? data.metadata.tableGeoid
    : schema && data.label ? `${schema}.${data.label}` : undefined;

  const openSchemaInLoom = schema
    ? () => { jumpTo('L1', null, schema); navigate('/'); }
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
                {columns.map((col) => <ColumnRow key={col.id} col={col} tableGeoid={tableGeoid} />)}
              </div>
            )}
          </InspectorSection>

          <TableRoutinesSection nodeId={nodeId} />
          <TableStatementsSection nodeId={nodeId} />
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
