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

// ── Header card ─────────────────────────────────────────────────────────────
// Mirrors the canvas StatementNode header — FileCode icon, groupPath breadcrumb
// with a left-accent border, bold title, op-colour badge, column-count subline.
// Background uses var(--bg0) (darkest panel token) so the header reads as a
// distinct "heading" zone compared to the content below (var(--bg2)).

function StatementHeaderCard({
  label, groupPath, operation, columnCount,
}: {
  label: string;
  groupPath: string[];
  operation: string;
  columnCount: number;
}) {
  const { t } = useTranslation();
  const typeColor = OP_COLORS[operation] ?? 'var(--t3)';
  return (
    <div
      role="heading"
      aria-level={2}
      style={{
        display:        'flex',
        alignItems:     'flex-start',
        gap:            'var(--seer-space-2)',
        padding:        '12px 14px',
        background:     'var(--bg0)',
        borderBottom:   '1px solid var(--bd)',
        borderLeft:     `3px solid ${typeColor}`,
      }}
    >
      <FileCode size={14} color={typeColor} strokeWidth={1.5} style={{ flexShrink: 0, marginTop: 2 }} />
      <div style={{ flex: 1, overflow: 'hidden' }}>
        {groupPath.length > 0 && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 0, marginBottom: 4 }}>
            {groupPath.map((seg, i) => (
              <div
                key={i}
                title={seg}
                style={{
                  fontSize:     '9px',
                  color:        'var(--t3)',
                  opacity:      0.6 + i * 0.15,
                  overflow:     'hidden',
                  textOverflow: 'ellipsis',
                  whiteSpace:   'nowrap',
                  lineHeight:   '13px',
                  letterSpacing: '0.03em',
                  textTransform: i === 0 ? 'uppercase' : 'none',
                }}
              >
                {seg}
              </div>
            ))}
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
        {columnCount > 0 && (
          <div style={{ fontSize: '11px', color: 'var(--t3)', marginTop: 2 }}>
            {t('nodes.outputColumns', { count: columnCount })}
          </div>
        )}
      </div>
      {operation && (
        <span
          style={{
            fontSize:      '9px',
            padding:       '2px 7px',
            borderRadius:  3,
            fontFamily:    'var(--mono)',
            border:        `0.5px solid ${typeColor}`,
            color:         typeColor,
            opacity:       0.9,
            flexShrink:    0,
            letterSpacing: '0.04em',
            fontWeight:    700,
            marginTop:     2,
          }}
        >
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
      padding: '3px 10px', borderTop: '1px solid var(--bd)',
      fontSize: '11px',
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

/** Extract package name from a statement's fullLabel:
 *  "DWH.PKG_ETL_CRM_STAGING:PROCEDURE:..." → "PKG_ETL_CRM_STAGING"
 */
function pkgFromLabel(fullLabel: string): string | null {
  const firstSeg = fullLabel.split(':')[0];
  const parts = firstSeg.split('.');
  return parts[parts.length - 1] || null;
}

// ── Tab bar ─────────────────────────────────────────────────────────────────

function TabBar({
  active, onChange, labels,
}: {
  active: InspectorTab;
  onChange: (t: InspectorTab) => void;
  labels: Record<InspectorTab, string>;
}) {
  return (
    <div
      role="tablist"
      aria-label="inspector-statement-tabs"
      style={{
        display: 'flex',
        borderBottom: '1px solid var(--bd)',
        background: 'var(--bg1)',
        position: 'sticky',
        top: 0,
        zIndex: 1,
      }}
    >
      <TabButton active={active === 'main'}  onClick={() => onChange('main')}  label={labels.main} />
      <TabButton active={active === 'extra'} onClick={() => onChange('extra')} label={labels.extra} />
      <TabButton active={active === 'stats'} onClick={() => onChange('stats')} label={labels.stats} />
      <TabButton active={active === 'sql'}   onClick={() => onChange('sql')}   label={labels.sql} />
    </div>
  );
}

function TabButton({
  active, onClick, label,
}: {
  active: boolean;
  onClick: () => void;
  label: string;
}) {
  return (
    <button
      role="tab"
      aria-selected={active}
      onClick={onClick}
      style={{
        flex: 1,
        padding: '8px 6px',
        background: 'transparent',
        border: 'none',
        borderBottom: `2px solid ${active ? 'var(--acc)' : 'transparent'}`,
        color: active ? 'var(--acc)' : 'var(--t2)',
        fontSize: '9px',
        fontWeight: active ? 700 : 500,
        letterSpacing: '0.06em',
        textTransform: 'uppercase',
        cursor: 'pointer',
        transition: 'color 0.12s, border-color 0.12s, background 0.08s',
        fontFamily: 'inherit',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        textOverflow: 'ellipsis',
      }}
      onMouseEnter={(e) => {
        if (!active) (e.currentTarget as HTMLElement).style.background = 'var(--bg2)';
      }}
      onMouseLeave={(e) => {
        (e.currentTarget as HTMLElement).style.background = 'transparent';
      }}
    >
      {label}
    </button>
  );
}

// ── Main component ──────────────────────────────────────────────────────────

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

  // Short display label for the header card: if `data.label` is already the
  // short form (e.g. "MERGE:46") use it as-is, otherwise split the full
  // geoid-style label on the last ':' to keep it tight.
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
        groupPath={groupPath}
        operation={operation || data.nodeType}
        columnCount={columns.length}
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
            {/* @rid row — same value as the React Flow node id, labeled
                verbatim for fast copy-paste into ArcadeDB Cypher / SQL
                queries (`WHERE @rid = '#25:6150'`). Kept alongside the
                standard ID row so both styles of debugging work. */}
            <InspectorRow label="@rid" value={nodeId} />
            <InspectorRow label={t('inspector.id')} value={nodeId} />
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
            {typeof data.metadata?.dataSource === 'string' && (
              <InspectorRow label={t('inspector.dataSource')} value={data.metadata.dataSource} />
            )}
            {typeof data.metadata?.session_id === 'string' && (
              <InspectorRow label={t('inspector.sessionId')} value={data.metadata.session_id as string} />
            )}
          </InspectorSection>
        </div>
      )}

      {tab === 'stats' && (
        <div role="tabpanel" aria-label={t('inspector.tabStats')}>
          <StatsPanel data={data} columns={columns} groupPath={groupPath} />
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

// ── Extra panel ──────────────────────────────────────────────────────────────
//
// Surfaces data that requires a backend lookup beyond the L2 `explore` query:
//   * all recursive CHILD_OF descendants (sub-queries, CTEs, inline views)
//   * DaliAtom counts grouped by parent_context (JOIN / SELECT / WHERE / CTE / …)
//
// Wired to the `knotStatementExtras(stmtGeoid)` GraphQL resolver in
// KnotService.java. The hook fires only while the panel is mounted (which is
// only while tab === 'extra'), so the query stays naturally lazy.

function ExtraPanel({ stmtGeoid }: { stmtGeoid: string }) {
  const { t } = useTranslation();
  const { data, isFetching, isError } = useStatementExtras(stmtGeoid, !!stmtGeoid);

  // Build a depth map from parent_statement chain so we can indent each
  // descendant under its parent. The root statement is depth 0 (not shown);
  // its direct children are depth 1; CTEs inside a child SELECT are depth 2.
  const tree = useMemo(() => {
    if (!data?.descendants) return { items: [] as { info: SubqueryInfo; depth: number }[] };
    const byGeoid = new Map<string, SubqueryInfo>();
    for (const d of data.descendants) byGeoid.set(d.stmtGeoid, d);
    const depthOf = (info: SubqueryInfo, guard = 0): number => {
      if (guard > 30) return 0;
      const parentGeoid = info.parentStmtGeoid ?? '';
      const parent = byGeoid.get(parentGeoid);
      if (!parent) return 1;          // immediate child of the root stmt
      return 1 + depthOf(parent, guard + 1);
    };
    // Preserve backend ordering (parent_stmt_geoid, stmt_geoid) but add depth.
    const items = data.descendants.map((info) => ({ info, depth: depthOf(info) }));
    return { items };
  }, [data?.descendants]);

  const filterCtxCount =
    data?.atomContexts.find((c) => c.context === 'WHERE')?.count ?? 0;
  const havingCtxCount =
    data?.atomContexts.find((c) => c.context === 'HAVING')?.count ?? 0;
  const joinCtxCount =
    data?.atomContexts.find((c) => c.context === 'JOIN')?.count ?? 0;
  const subqueryCtxCount =
    (data?.atomContexts.find((c) => c.context === 'SUBQUERY')?.count ?? 0)
    + (data?.atomContexts.find((c) => c.context === 'USUBQUERY')?.count ?? 0)
    + (data?.atomContexts.find((c) => c.context === 'CTE')?.count ?? 0);

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

  const sources = data?.sourceTables ?? [];
  const directSources   = sources.filter((s) => s.sourceKind === 'DIRECT');
  const subquerySources = sources.filter((s) => s.sourceKind === 'SUBQUERY');

  return (
    <>
      {/* ── Source tables (DIRECT vs SUBQUERY) ──────────────────────────────── */}
      <InspectorSection
        title={`${t('inspector.sourceTables')} (${sources.length})`}
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

      {/* ── Subqueries (all descendants, recursive) ─────────────────────────── */}
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
                  display:     'flex',
                  alignItems:  'center',
                  gap:         6,
                  padding:     '3px 10px',
                  paddingLeft: 10 + (depth - 1) * 12,
                  borderTop:   '1px solid var(--bd)',
                  fontSize:    '11px',
                }}
              >
                <SubqueryTypeBadge type={info.stmtType} />
                <span
                  className="mono"
                  style={{
                    flex:         1,
                    color:        'var(--t1)',
                    overflow:     'hidden',
                    textOverflow: 'ellipsis',
                    whiteSpace:   'nowrap',
                    fontFamily:   'var(--mono)',
                    fontSize:     '10px',
                  }}
                >
                  {shortenSubLabel(info.stmtGeoid, info.parentStmtGeoid)}
                </span>
                <span
                  style={{
                    color:      'var(--t3)',
                    fontSize:   '9px',
                    fontFamily: 'var(--mono)',
                    flexShrink: 0,
                  }}
                >
                  {info.rid}
                </span>
              </div>
            ))}
          </div>
        )}
      </InspectorSection>

      {/* ── Filter / JOIN / SubQuery atom summary ───────────────────────────── */}
      <InspectorSection title={t('inspector.filters')}>
        <InspectorRow label={t('inspector.filterWhere')}    value={String(filterCtxCount)} />
        <InspectorRow label={t('inspector.filterHaving')}   value={String(havingCtxCount)} />
        <InspectorRow label={t('inspector.filterJoin')}     value={String(joinCtxCount)} />
        <InspectorRow label={t('inspector.filterSubquery')} value={String(subqueryCtxCount)} />
      </InspectorSection>

      {/* ── Raw atom breakdown (all parent_context buckets) ────────────────── */}
      <InspectorSection
        title={`${t('inspector.atomStats')} (${data?.totalAtomCount ?? 0})`}
      >
        {(data?.atomContexts ?? []).length === 0 ? (
          <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
            {t('inspector.noAtoms')}
          </div>
        ) : (
          data!.atomContexts.map((c) => (
            <InspectorRow key={c.context} label={c.context} value={String(c.count)} />
          ))
        )}
      </InspectorSection>
    </>
  );
}

// ── Source-row (used by ExtraPanel's source-tables section) ────────────────

function SourceRow({ src }: { src: SourceTableRef }) {
  return (
    <div
      title={`${src.tableGeoid}${src.viaStmtGeoid ? `\nvia ${src.viaStmtGeoid}` : ''}`}
      style={{
        display:     'flex',
        alignItems:  'center',
        gap:         6,
        padding:     '3px 10px',
        borderTop:   '1px solid var(--bd)',
        fontSize:    '11px',
      }}
    >
      <span
        className="mono"
        style={{
          flex:         1,
          color:        'var(--t1)',
          overflow:     'hidden',
          textOverflow: 'ellipsis',
          whiteSpace:   'nowrap',
          fontFamily:   'var(--mono)',
        }}
      >
        {src.tableName || src.tableGeoid}
      </span>
      {src.schemaGeoid && (
        <span
          style={{
            fontSize:    '9px',
            color:       'var(--t3)',
            fontFamily:  'var(--mono)',
            padding:     '1px 5px',
            borderRadius: 2,
            border:      '0.5px solid var(--bd)',
            flexShrink:  0,
          }}
        >
          {src.schemaGeoid}
        </span>
      )}
    </div>
  );
}

// ── Subquery helpers ────────────────────────────────────────────────────────

function SubqueryTypeBadge({ type }: { type: string }) {
  const color = OP_COLORS[type] ?? 'var(--t3)';
  return (
    <span
      style={{
        fontSize:      '8px',
        padding:       '1px 5px',
        borderRadius:  2,
        fontFamily:    'var(--mono)',
        border:        `0.5px solid ${color}`,
        color,
        opacity:       0.85,
        flexShrink:    0,
        letterSpacing: '0.03em',
        fontWeight:    600,
      }}
    >
      {type}
    </span>
  );
}

/**
 * Trim a descendant stmt_geoid for display by removing the parent prefix,
 * leaving just the trailing path segment(s). Falls back to the last two
 * colon-separated parts if the parent isn't found.
 */
function shortenSubLabel(geoid: string, parentGeoid: string | null): string {
  if (parentGeoid && geoid.startsWith(parentGeoid + ':')) {
    return geoid.slice(parentGeoid.length + 1);
  }
  const parts = geoid.split(':');
  return parts.slice(-2).join(':') || geoid;
}

// ── Stats panel ──────────────────────────────────────────────────────────────

function StatsPanel({
  data, columns, groupPath,
}: {
  data: DaliNodeData;
  columns: ColumnInfo[];
  groupPath: string[];
}) {
  const { t } = useTranslation();
  const pkCount = columns.filter((c) => c.isPrimaryKey).length;
  const fkCount = columns.filter((c) => c.isForeignKey).length;
  const typedCount = columns.filter((c) => !!c.type).length;
  const lineStart = typeof data.metadata?.line_start === 'number' ? data.metadata.line_start : null;
  const lineEnd   = typeof data.metadata?.line_end   === 'number' ? data.metadata.line_end   : null;

  return (
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
  );
}

// ── SQL panel ────────────────────────────────────────────────────────────────
//
// Source of the SQL text, in priority order:
//   1. data.metadata.sqlText   — pre-loaded from transformExplore (rare)
//   2. data.metadata.snippet   — same, alternate property name
//   3. useKnotSnippet(geoid)   — lazy GraphQL fetch from DaliSnippet via
//                                services/shuttle KnotService.knotSnippet.
//
// The DaliStatement's React Flow node id equals the ArcadeDB @rid, so we
// pass the inspector's nodeId directly to useKnotSnippet. The backend
// accepts either @rid (#25:1234) or stmt_geoid (ADR: see KnotService.java).
//
// Because the SQL panel is only mounted when tab === 'sql', the query hook
// stays lazy for free: it fires the moment the user clicks the tab, and
// React Query caches for staleTime (5 min) so re-opening the tab for the
// same statement is instant.

function SqlPanel({ data, stmtGeoid }: { data: DaliNodeData; stmtGeoid: string }) {
  const { t } = useTranslation();
  const [copied, setCopied] = useState(false);

  const preloaded =
    typeof data.metadata?.sqlText === 'string' ? data.metadata.sqlText
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
    // Two-row layout: a toolbar strip with the Copy button, then the <pre>.
    // Previously the button was position:absolute over the pre, which hid
    // the first ~20 chars of every line — user complained ("Скопировать SQL
    // закрывает часть кода"). Moving it into its own strip keeps the code
    // fully legible regardless of line length.
    return (
      <div style={{ padding: '8px 10px' }}>
        <div style={{
          display: 'flex',
          justifyContent: 'flex-end',
          padding: '0 0 6px',
        }}>
          <button
            onClick={handleCopy}
            aria-label={t('inspector.copySql')}
            style={{
              fontSize: '9px', fontWeight: 600,
              padding: '3px 10px', borderRadius: 3,
              background: copied ? 'var(--suc)' : 'var(--bg3)',
              border: '1px solid var(--bd)',
              color: copied ? 'var(--bg0)' : 'var(--t2)',
              cursor: 'pointer',
              letterSpacing: '0.04em',
              textTransform: 'uppercase',
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
          maxHeight: 'calc(100vh - 280px)',
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
