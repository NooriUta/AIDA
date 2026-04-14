import { memo, useState, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import type { DaliNodeData, ColumnInfo } from '../../types/domain';
import { useKnotSnippet } from '../../services/hooks';
import { InspectorSection, InspectorRow } from './InspectorSection';

interface Props { data: DaliNodeData; nodeId: string }

type InspectorTab = 'main' | 'sql';

const OP_COLORS: Record<string, string> = {
  INSERT: '#D4922A', UPDATE: '#D4922A', MERGE: '#D4922A', DELETE: '#c85c5c',
  SELECT: '#88B8A8', CTE: '#A8B860',   WITH: '#A8B860',  CREATE: '#7DBF78',
  DROP:   '#c85c5c', TRUNCATE: '#c85c5c', SQ: '#88B8A8', CURSOR: '#88B8A8',
};

function OpBadge({ op }: { op: string }) {
  const color = OP_COLORS[op] ?? 'var(--t3)';
  return (
    <span style={{
      fontSize: '10px', fontWeight: 700, letterSpacing: '0.06em',
      padding: '1px 6px', borderRadius: 4,
      background: `color-mix(in srgb, ${color} 18%, transparent)`,
      color, border: `1px solid color-mix(in srgb, ${color} 40%, transparent)`,
    }}>
      {op}
    </span>
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
  labels: { main: string; sql: string };
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
      <TabButton
        active={active === 'main'}
        onClick={() => onChange('main')}
        label={labels.main}
      />
      <TabButton
        active={active === 'sql'}
        onClick={() => onChange('sql')}
        label={labels.sql}
      />
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
        padding: '8px 12px',
        background: 'transparent',
        border: 'none',
        borderBottom: `2px solid ${active ? 'var(--acc)' : 'transparent'}`,
        color: active ? 'var(--acc)' : 'var(--t2)',
        fontSize: '10px',
        fontWeight: active ? 700 : 500,
        letterSpacing: '0.08em',
        textTransform: 'uppercase',
        cursor: 'pointer',
        transition: 'color 0.12s, border-color 0.12s, background 0.08s',
        fontFamily: 'inherit',
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

  return (
    <>
      <TabBar
        active={tab}
        onChange={setTab}
        labels={{ main: t('inspector.tabMain'), sql: t('inspector.sql') }}
      />

      {tab === 'main' ? (
        <div role="tabpanel" aria-label={t('inspector.tabMain')}>
          <InspectorSection title={t('inspector.properties')}>
            <InspectorRow label={t('inspector.type')}  value={<OpBadge op={operation || data.nodeType} />} />
            <InspectorRow label={t('inspector.label')} value={fullLabel} />
            {groupPath.length > 0 && (
              <InspectorRow label={t('inspector.path')} value={groupPath.join(' › ')} />
            )}
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
      ) : (
        <div role="tabpanel" aria-label={t('inspector.sql')}>
          <SqlPanel data={data} stmtGeoid={nodeId} />
        </div>
      )}
    </>
  );
});

InspectorStatement.displayName = 'InspectorStatement';

// ── SQL panel ────────────────────────────────────────────────────────────────
//
// Source of the SQL text, in priority order:
//   1. data.metadata.sqlText   — pre-loaded from transformExplore (rare)
//   2. data.metadata.snippet   — same, alternate property name
//   3. useKnotSnippet(geoid)   — lazy GraphQL fetch from DaliSnippet via
//                                services/shuttle KnotService.knotSnippet.
//
// The DaliStatement's React Flow node id equals the stmt_geoid (see
// transformExplore.ts where allStmtIds feeds rfNodes[].id), so we pass
// the inspector's nodeId directly to useKnotSnippet.
//
// Because the SQL panel is only mounted when tab === 'sql', the query hook
// stays lazy for free: it first fires the moment the user clicks the tab,
// and React Query caches for staleTime (5 min) so re-opening the tab for
// the same statement is instant.

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
    return (
      <div style={{ position: 'relative', padding: '8px 10px' }}>
        <pre style={{
          padding: '8px 10px', margin: 0,
          fontSize: '11px', lineHeight: '1.5',
          color: 'var(--t1)',
          background: 'var(--bg0)',
          border: '1px solid var(--bd)',
          borderRadius: 4,
          maxHeight: 'calc(100vh - 200px)',
          overflow: 'auto',
          whiteSpace: 'pre',
          fontFamily: 'var(--mono)',
        }}>
          {sqlText}
        </pre>
        <button
          onClick={handleCopy}
          aria-label={t('inspector.copySql')}
          style={{
            position: 'absolute', top: 12, right: 14,
            fontSize: '9px', fontWeight: 500,
            padding: '3px 8px', borderRadius: 3,
            background: copied ? 'var(--suc)' : 'var(--bg3)',
            border: '1px solid var(--bd)',
            color: copied ? 'var(--bg0)' : 'var(--t2)',
            cursor: 'pointer',
            transition: 'background 0.15s, color 0.15s',
          }}
        >
          {copied ? t('inspector.copied') : t('inspector.copySql')}
        </button>
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
