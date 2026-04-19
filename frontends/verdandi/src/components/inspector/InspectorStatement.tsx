import { memo, useMemo, useState, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import { FileCode } from 'lucide-react';
import type { DaliNodeData, ColumnInfo } from '../../types/domain';
import type { SubqueryInfo, SourceTableRef } from '../../services/lineage';
import { useKnotSnippet, useStatementExtras } from '../../services/hooks';
import { InspectorSection, InspectorRow } from './InspectorSection';

interface Props { data: DaliNodeData; nodeId: string }

type InspectorTab = 'main' | 'extra' | 'stats' | 'sql';

const OP_COLORS: Record<string, string> = {
  INSERT: '#D4922A', UPDATE: '#D4922A', MERGE: '#D4922A', DELETE: '#c85c5c',
  SELECT: '#88B8A8', CTE: '#A8B860',   WITH: '#A8B860',  CREATE: '#7DBF78',
  DROP:   '#c85c5c', TRUNCATE: '#c85c5c', SQ: '#88B8A8', CURSOR: '#88B8A8',
};

// ── Header card ──────────────────────────────────────────────────────────────

function StatementHeaderCard({
  label, schema, groupPath, operation, columnCount, onSchemaClick, onPackageClick,
}: {
  label: string;
  schema?: string;
  groupPath: string[];
  operation: string;
  columnCount: number;
  onSchemaClick?: () => void;
  onPackageClick?: (pkg: string) => void;
}) {
  const { t } = useTranslation();
  const typeColor = OP_COLORS[operation] ?? 'var(--t3)';
  return (
    <div
      role="heading"
      aria-level={2}
      style={{
        display: 'flex', alignItems: 'flex-start', gap: 'var(--seer-space-2)',
        padding: '12px 14px',
        background: 'var(--bg0)', borderBottom: '1px solid var(--bd)',
        borderLeft: `3px solid ${typeColor}`,
      }}
    >
      <FileCode size={14} color={typeColor} strokeWidth={1.5} style={{ flexShrink: 0, marginTop: 2 }} />
      <div style={{ flex: 1, overflow: 'hidden' }}>
        {schema && (
          onSchemaClick ? (
            <button onClick={onSchemaClick} title={`Открыть ${schema} в Loom`} style={{
              fontSize: '9px', color: 'var(--acc)',
              overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
              marginBottom: 1, letterSpacing: '0.03em', textTransform: 'uppercase',
              background: 'none', border: 'none', padding: 0, cursor: 'pointer',
              textDecoration: 'underline', textDecorationStyle: 'dotted',
              textAlign: 'left', fontFamily: 'inherit', display: 'block',
            }}>
              ◈ {schema}
            </button>
          ) : (
            <div style={{
              fontSize: '9px', color: 'var(--t3)', opacity: 0.6,
              overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
              marginBottom: 1, letterSpacing: '0.03em', textTransform: 'uppercase',
            }}>
              {schema}
            </div>
          )
        )}
        {groupPath.length > 0 && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 0, marginBottom: 4 }}>
            {groupPath.map((seg, i) => {
              if (i === 0 && onPackageClick) {
                return (
                  <button key={i} onClick={() => onPackageClick(seg)} title={`Открыть ${seg} в Loom`} style={{
                    fontSize: '9px', color: 'var(--acc)',
                    overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                    lineHeight: '13px', letterSpacing: '0.03em', textTransform: 'uppercase',
                    background: 'none', border: 'none', padding: 0, cursor: 'pointer',
                    textDecoration: 'underline', textDecorationStyle: 'dotted',
                    textAlign: 'left', fontFamily: 'inherit',
                  }}>
                    ◈ {seg}
                  </button>
                );
              }
              return (
                <div key={i} title={seg} style={{
                  fontSize: '9px', color: 'var(--t3)',
                  opacity: 0.75,
                  overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                  lineHeight: '13px', letterSpacing: '0.03em',
                }}>
                  {seg}
                </div>
              );
            })}
          </div>
        )}
        <div title={label} style={{
          fontWeight: 700, fontSize: '13px', color: 'var(--t1)',
          overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
          letterSpacing: '0.02em',
        }}>
          {label}
        </div>
        {columnCount > 0 && (
          <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: 2 }}>
            {t('nodes.outputColumns', { count: columnCount })}
          </div>
        )}
      </div>
      {operation && (
        <span style={{
          fontSize: '9px', padding: '2px 7px', borderRadius: 3,
          fontFamily: 'var(--mono)', border: `0.5px solid ${typeColor}`,
          color: typeColor, opacity: 0.9, flexShrink: 0,
          letterSpacing: '0.04em', fontWeight: 700, marginTop: 2,
        }}>
          {operation}
        </span>
      )}
    </div>
  );
}

function OutputColRow({ col }: { col: ColumnInfo }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center',
      padding: '3px 10px', borderTop: '1px solid var(--bd)', fontSize: '11px',
    }}>
      <span style={{
        flex: 1, color: 'var(--t1)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        fontFamily: 'var(--mono)',
      }}>
        {col.name}
      </span>
      {col.type && (
        <span style={{ color: 'var(--t3)', fontSize: '10px', marginLeft: 4 }}>{col.type}</span>
      )}
    </div>
  );
}

function pkgFromLabel(fullLabel: string): string | null {
  const firstSeg = fullLabel.split(':')[0];
  const parts = firstSeg.split('.');
  return parts[parts.length - 1] || null;
}

// ── Tab bar ──────────────────────────────────────────────────────────────────

function TabBar({ active, onChange, labels }: {
  active: InspectorTab;
  onChange: (t: InspectorTab) => void;
  labels: Record<InspectorTab, string>;
}) {
  return (
    <div role="tablist" aria-label="inspector-statement-tabs" style={{
      display: 'flex', borderBottom: '1px solid var(--bd)',
      background: 'var(--bg1)', position: 'sticky', top: 0, zIndex: 1,
    }}>
      <TabButton active={active === 'main'}  onClick={() => onChange('main')}  label={labels.main} />
      <TabButton active={active === 'extra'} onClick={() => onChange('extra')} label={labels.extra} />
      <TabButton active={active === 'stats'} onClick={() => onChange('stats')} label={labels.stats} />
      <TabButton active={active === 'sql'}   onClick={() => onChange('sql')}   label={labels.sql} />
    </div>
  );
}

function TabButton({ active, onClick, label }: { active: boolean; onClick: () => void; label: string }) {
  return (
    <button role="tab" aria-selected={active} onClick={onClick} style={{
      flex: 1, padding: '8px 6px', background: 'transparent', border: 'none',
      borderBottom: `2px solid ${active ? 'var(--acc)' : 'transparent'}`,
      color: active ? 'var(--acc)' : 'var(--t2)',
      fontSize: '9px', fontWeight: active ? 700 : 500, letterSpacing: '0.06em',
      textTransform: 'uppercase', cursor: 'pointer',
      transition: 'color 0.12s, border-color 0.12s, background 0.08s',
      fontFamily: 'inherit', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
    }}
      onMouseEnter={(e) => { if (!active) (e.currentTarget as HTMLElement).style.background = 'var(--bg2)'; }}
      onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
    >
      {label}
    </button>
  );
}

// ── Main component ────────────────────────────────────────────────────────────

export const InspectorStatement = memo(({ data, nodeId }: Props) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [tab, setTab] = useState<InspectorTab>('main');

  const columns   = data.columns ?? [];
  const groupPath = Array.isArray(data.metadata?.groupPath) ? (data.metadata.groupPath as string[]) : [];
  const operation = typeof data.operation === 'string' ? data.operation : (data.metadata?.stmtType as string) ?? '';
  const fullLabel = typeof data.metadata?.fullLabel === 'string' ? data.metadata.fullLabel : data.label;
  const pkgName   = pkgFromLabel(fullLabel);

  const openInKnot = () => {
    const params = new URLSearchParams();
    if (pkgName) params.set('pkg', pkgName);
    params.set('stmt', data.label);
    navigate(`/knot?${params.toString()}`);
  };

  const openPkgInKnot = useCallback((pkg: string) => {
    navigate(`/knot?pkg=${encodeURIComponent(pkg)}`);
  }, [navigate]);

  const schema = data.schema ?? (typeof data.metadata?.schema === 'string' ? data.metadata.schema as string : undefined);
  const openSchemaInLoom = schema ? () => navigate(`/knot?schema=${encodeURIComponent(schema)}`) : undefined;

  const shortLabel = (() => {
    const l = data.label || 'Statement';
    if (l === fullLabel && l.includes(':')) {
      const tail = l.split(':').slice(-2).join(':');
      return tail || l;
    }
    return l;
  })();

  return (
    <>
      <StatementHeaderCard
        label={shortLabel}
        schema={schema}
        groupPath={groupPath}
        operation={operation || data.nodeType}
        columnCount={columns.length}
        onSchemaClick={openSchemaInLoom}
        onPackageClick={groupPath.length > 0 ? openPkgInKnot : undefined}
      />

      <TabBar
        active={tab}
        onChange={setTab}
        labels={{
          main:  t('inspector.tabMain'),
          extra: t('inspector.tabExtra'),
          stats: t('inspector.tabStats'),
          sql:   t('inspector.sql'),
        }}
      />

      {tab === 'main' && (
        <div role="tabpanel" aria-label={t('inspector.tabMain')}>
          <InspectorSection title={t('inspector.properties')}>
            <InspectorRow label="@rid" value={nodeId} />
            <div style={{ padding: '6px 10px 4px' }}>
              <button
                onClick={openInKnot}
                style={{
                  display: 'inline-flex', alignItems: 'center', gap: 5,
                  padding: '4px 10px', fontSize: 11, fontWeight: 500, fontFamily: 'inherit',
                  background: 'var(--bg3)', border: '1px solid var(--bd)', borderRadius: 4,
                  color: 'var(--acc)', cursor: 'pointer', transition: 'border-color 0.1s',
                }}
                onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.borderColor = 'var(--acc)'; }}
                onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.borderColor = 'var(--bd)'; }}
              >
                ◈ {t('contextMenu.openInKnot')}
              </button>
            </div>
          </InspectorSection>
          <InspectorSection
            title={`${t('inspector.outputColumns')} (${columns.length})`}
            defaultOpen={columns.length > 0}
          >
            {columns.length === 0 ? (
              <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
                {t('inspector.noColumns')}
              </div>
            ) : (
              <div style={{ marginTop: 2 }}>
                {columns.map((col) => <OutputColRow key={col.id} col={col} />)}
              </div>
            )}
          </InspectorSection>
        </div>
      )}

      {tab === 'extra' && (
        <div role="tabpanel" aria-label={t('inspector.tabExtra')}>
          <ExtraPanel stmtGeoid={nodeId} />
          <InspectorSection title={t('inspector.metadata')}>
            <InspectorRow label={t('inspector.fullLabel')} value={fullLabel} />
            {data.schema && <InspectorRow label={t('inspector.schema')} value={data.schema} />}
            {groupPath.length > 0 && (
              <InspectorRow label={t('inspector.path')} value={groupPath.join(' › ')} />
            )}
            <AllMetaRows meta={data.metadata as Record<string, unknown>} skip={['fullLabel']} />
          </InspectorSection>
        </div>
      )}

      {tab === 'stats' && (
        <div role="tabpanel" aria-label={t('inspector.tabStats')}>
          <StatsPanel data={data} columns={columns} groupPath={groupPath} stmtGeoid={nodeId} />
        </div>
      )}

      {tab === 'sql' && (
        <div role="tabpanel" aria-label={t('inspector.sql')}>
          <SqlPanel data={data} stmtGeoid={nodeId} />
        </div>
      )}
    </>
  );
});

InspectorStatement.displayName = 'InspectorStatement';

// ── Extra panel ───────────────────────────────────────────────────────────────
//
// Source tables (READS_FROM + future WRITES_TO) and subquery tree.
// Atom filters and stats have moved to the Stats tab.

function ExtraPanel({ stmtGeoid }: { stmtGeoid: string }) {
  const { t } = useTranslation();
  const { data, isFetching, isError } = useStatementExtras(stmtGeoid, !!stmtGeoid);

  const tree = useMemo(() => {
    if (!data?.descendants) return { items: [] as { info: SubqueryInfo; depth: number }[] };
    const byGeoid = new Map<string, SubqueryInfo>();
    for (const d of data.descendants) byGeoid.set(d.stmtGeoid, d);
    const depthOf = (info: SubqueryInfo, guard = 0): number => {
      if (guard > 30) return 0;
      const parentGeoid = info.parentStmtGeoid ?? '';
      const parent = byGeoid.get(parentGeoid);
      if (!parent) return 1;
      return 1 + depthOf(parent, guard + 1);
    };
    const items = data.descendants.map((info) => ({ info, depth: depthOf(info) }));
    return { items };
  }, [data?.descendants]);

  if (isFetching) {
    return (
      <div style={{ padding: '12px 12px', fontSize: '11px', color: 'var(--t3)' }}>
        {t('inspector.extraLoading')}
      </div>
    );
  }

  if (isError) {
    return (
      <div style={{ padding: '12px 12px', fontSize: '11px', color: 'var(--err)' }}>
        {t('inspector.extraError')}
      </div>
    );
  }

  const sources        = data?.sourceTables ?? [];
  const directSources  = sources.filter((s) => s.sourceKind === 'DIRECT');
  const subquerySources = sources.filter((s) => s.sourceKind === 'SUBQUERY');

  return (
    <>
      {/* ── Читаемые таблицы (READS_FROM) ──────────────────────────────────── */}
      <InspectorSection
        title={`${t('inspector.readTables')} (${sources.length})`}
        defaultOpen={sources.length > 0}
      >
        {sources.length === 0 ? (
          <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
            {t('inspector.noSourceTables')}
          </div>
        ) : (
          <div style={{ marginTop: 2 }}>
            {directSources.length > 0 && (
              <div style={{
                padding: '4px 10px 2px', fontSize: '9px',
                color: 'var(--t3)', letterSpacing: '0.06em', textTransform: 'uppercase',
              }}>
                {t('inspector.sourceDirect')} · {directSources.length}
              </div>
            )}
            {directSources.map((s) => <SourceRow key={s.rid} src={s} />)}
            {subquerySources.length > 0 && (
              <div style={{
                padding: '6px 10px 2px', marginTop: directSources.length > 0 ? 4 : 0,
                fontSize: '9px', color: 'var(--t3)', letterSpacing: '0.06em', textTransform: 'uppercase',
                borderTop: directSources.length > 0 ? '1px solid var(--bd)' : 'none',
              }}>
                {t('inspector.sourceViaSubquery')} · {subquerySources.length}
              </div>
            )}
            {subquerySources.map((s) => <SourceRow key={s.rid} src={s} />)}
          </div>
        )}
      </InspectorSection>

      {/* ── Изменяемые таблицы (WRITES_TO) — TODO: поле в StatementExtras ─── */}
      <InspectorSection title={`${t('inspector.writeTables')} (0)`} defaultOpen={false}>
        <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)', fontStyle: 'italic' }}>
          {t('inspector.noWriteTables')}
        </div>
      </InspectorSection>

      {/* ── Подзапросы (рекурсивно) ─────────────────────────────────────────── */}
      <InspectorSection title={`${t('inspector.subqueries')} (${tree.items.length})`}>
        {tree.items.length === 0 ? (
          <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
            {t('inspector.noSubqueries')}
          </div>
        ) : (
          <div style={{ marginTop: 2 }}>
            {tree.items.map(({ info, depth }) => (
              <div
                key={info.rid}
                title={info.stmtGeoid}
                style={{
                  display: 'flex', alignItems: 'center', gap: 6,
                  padding: '3px 10px', paddingLeft: 10 + (depth - 1) * 12,
                  borderTop: '1px solid var(--bd)', fontSize: '11px',
                }}
              >
                <SubqueryTypeBadge type={info.stmtType} />
                <span style={{
                  flex: 1, color: 'var(--t1)',
                  overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                  fontFamily: 'var(--mono)', fontSize: '10px',
                }}>
                  {shortenSubLabel(info.stmtGeoid, info.parentStmtGeoid)}
                </span>
                <span style={{ color: 'var(--t3)', fontSize: '9px', fontFamily: 'var(--mono)', flexShrink: 0 }}>
                  {info.rid}
                </span>
              </div>
            ))}
          </div>
        )}
      </InspectorSection>
    </>
  );
}

// ── Source row ────────────────────────────────────────────────────────────────

function SourceRow({ src }: { src: SourceTableRef }) {
  return (
    <div
      title={`${src.tableGeoid}${src.viaStmtGeoid ? `\nvia ${src.viaStmtGeoid}` : ''}`}
      style={{
        display: 'flex', alignItems: 'center', gap: 6,
        padding: '3px 10px', borderTop: '1px solid var(--bd)', fontSize: '11px',
      }}
    >
      <span style={{
        flex: 1, color: 'var(--t1)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        fontFamily: 'var(--mono)',
      }}>
        {src.tableName || src.tableGeoid}
      </span>
      {src.schemaGeoid && (
        <span style={{
          fontSize: '9px', color: 'var(--t3)', fontFamily: 'var(--mono)',
          padding: '1px 5px', borderRadius: 2, border: '0.5px solid var(--bd)', flexShrink: 0,
        }}>
          {src.schemaGeoid}
        </span>
      )}
    </div>
  );
}

// ── Subquery helpers ──────────────────────────────────────────────────────────

function SubqueryTypeBadge({ type }: { type: string }) {
  const color = OP_COLORS[type] ?? 'var(--t3)';
  return (
    <span style={{
      fontSize: '8px', padding: '1px 5px', borderRadius: 2,
      fontFamily: 'var(--mono)', border: `0.5px solid ${color}`,
      color, opacity: 0.85, flexShrink: 0, letterSpacing: '0.03em', fontWeight: 600,
    }}>
      {type}
    </span>
  );
}

function shortenSubLabel(geoid: string, parentGeoid: string | null): string {
  if (parentGeoid && geoid.startsWith(parentGeoid + ':')) {
    return geoid.slice(parentGeoid.length + 1);
  }
  const parts = geoid.split(':');
  return parts.slice(-2).join(':') || geoid;
}

// ── Generic metadata rows ─────────────────────────────────────────────────────

const META_SKIP_DEFAULT = new Set(['sqlText', 'snippet', 'ddlText', 'groupPath', 'id']);

function AllMetaRows({
  meta, skip = [],
}: {
  meta: Record<string, unknown> | undefined;
  skip?: string[];
}) {
  if (!meta) return null;
  const skipSet = new Set([...META_SKIP_DEFAULT, ...skip]);
  const rows = Object.entries(meta).filter(([k, v]) =>
    !skipSet.has(k) && v !== null && v !== undefined && typeof v !== 'object',
  );
  if (rows.length === 0) return null;
  return <>{rows.map(([k, v]) => <InspectorRow key={k} label={k} value={String(v)} />)}</>;
}

// ── Stats panel ───────────────────────────────────────────────────────────────
//
// Output column stats (ГРАФ) + atom filter breakdown (LAZY via useStatementExtras).
// The lazy query fires when this panel mounts (tab === 'stats'). React Query caches
// the result so switching between Extra and Stats tabs doesn't re-fetch.

function StatsPanel({
  data, columns, groupPath, stmtGeoid,
}: {
  data: DaliNodeData;
  columns: ColumnInfo[];
  groupPath: string[];
  stmtGeoid: string;
}) {
  const { t } = useTranslation();
  const pkCount    = columns.filter((c) => c.isPrimaryKey).length;
  const fkCount    = columns.filter((c) => c.isForeignKey).length;
  const typedCount = columns.filter((c) => !!c.type).length;
  const lineStart  = typeof data.metadata?.line_start === 'number' ? data.metadata.line_start : null;
  const lineEnd    = typeof data.metadata?.line_end   === 'number' ? data.metadata.line_end   : null;

  const { data: extras } = useStatementExtras(stmtGeoid, !!stmtGeoid);
  const filterWhere    = extras?.atomContexts.find((c) => c.context === 'WHERE')?.count    ?? 0;
  const filterHaving   = extras?.atomContexts.find((c) => c.context === 'HAVING')?.count   ?? 0;
  const filterJoin     = extras?.atomContexts.find((c) => c.context === 'JOIN')?.count      ?? 0;
  const filterSubquery =
    (extras?.atomContexts.find((c) => c.context === 'SUBQUERY')?.count  ?? 0)
    + (extras?.atomContexts.find((c) => c.context === 'USUBQUERY')?.count ?? 0)
    + (extras?.atomContexts.find((c) => c.context === 'CTE')?.count       ?? 0);

  return (
    <>
      <InspectorSection title={t('inspector.statistics')}>
        <InspectorRow label={t('inspector.statsOutputCount')} value={String(columns.length)} />
        {pkCount > 0 && <InspectorRow label={t('inspector.statsPkCount')} value={String(pkCount)} />}
        {fkCount > 0 && <InspectorRow label={t('inspector.statsFkCount')} value={String(fkCount)} />}
        {typedCount > 0 && (
          <InspectorRow label={t('inspector.statsTypedCount')} value={`${typedCount} / ${columns.length}`} />
        )}
        <InspectorRow label={t('inspector.statsScopeDepth')} value={String(groupPath.length)} />
        {(lineStart !== null || lineEnd !== null) && (
          <InspectorRow
            label={t('inspector.statsLineRange')}
            value={`${lineStart ?? '?'}–${lineEnd ?? '?'}`}
          />
        )}
      </InspectorSection>

      <InspectorSection title="Фильтры · JOIN · подзапросы">
        <InspectorRow label="WHERE атомы"    value={String(filterWhere)} />
        <InspectorRow label="HAVING атомы"   value={String(filterHaving)} />
        <InspectorRow label="JOIN атомы"     value={String(filterJoin)} />
        <InspectorRow label="Подзапросные атомы (CTE+SQ)" value={String(filterSubquery)} />
      </InspectorSection>

      <InspectorSection
        title={`${t('inspector.atomStats')} (${extras?.totalAtomCount ?? 0})`}
        defaultOpen={(extras?.totalAtomCount ?? 0) > 0}
      >
        {(extras?.atomContexts ?? []).length === 0 ? (
          <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
            {t('inspector.noAtoms')}
          </div>
        ) : (
          extras!.atomContexts.map((c) => (
            <InspectorRow key={c.context} label={c.context} value={String(c.count)} />
          ))
        )}
      </InspectorSection>
    </>
  );
}

// ── SQL panel ─────────────────────────────────────────────────────────────────

function SqlPanel({ data, stmtGeoid }: { data: DaliNodeData; stmtGeoid: string }) {
  const { t } = useTranslation();
  const [copied, setCopied] = useState(false);

  const preloaded =
    typeof data.metadata?.sqlText  === 'string' ? data.metadata.sqlText
    : typeof data.metadata?.snippet === 'string' ? data.metadata.snippet
    : '';

  const { data: fetched, isFetching, isError } = useKnotSnippet(
    stmtGeoid,
    !preloaded && !!stmtGeoid,
  );

  const sqlText = preloaded || fetched || '';

  const handleCopy = useCallback(() => {
    if (!sqlText) return;
    navigator.clipboard.writeText(sqlText).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    });
  }, [sqlText]);

  if (sqlText) {
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
            {copied ? t('inspector.copied') : t('inspector.copySql')}
          </button>
        </div>
        <pre style={{
          padding: '8px 10px', margin: 0,
          fontSize: '11px', lineHeight: '1.5',
          color: 'var(--t1)',
          background: 'var(--bg0)',
          border: '1px solid var(--bd)',
          borderRadius: 4,
          overflow: 'auto',
          whiteSpace: 'pre',
          fontFamily: 'var(--mono)',
        }}>
          {sqlText}
        </pre>
      </div>
    );
  }

  if (isFetching) {
    return (
      <div style={{ padding: '16px 12px', fontSize: '11px', color: 'var(--t3)' }}>
        {t('inspector.sqlLoading')}
      </div>
    );
  }

  if (isError) {
    return (
      <div style={{ padding: '16px 12px', fontSize: '11px', color: 'var(--err)' }}>
        {t('inspector.sqlError')}
      </div>
    );
  }

  return (
    <div style={{ padding: '16px 12px', fontSize: '11px', color: 'var(--t3)' }}>
      {t('knot.stmt.noSql')}
    </div>
  );
}
