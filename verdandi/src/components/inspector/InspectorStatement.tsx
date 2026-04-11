import { memo, useState, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import type { DaliNodeData, ColumnInfo } from '../../types/domain';
import { InspectorSection, InspectorRow } from './InspectorSection';

interface Props { data: DaliNodeData; nodeId: string }

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
        fontFamily: 'var(--seer-font-mono, monospace)',
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

export const InspectorStatement = memo(({ data, nodeId }: Props) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
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
      <InspectorSection title={t('inspector.properties')}>
        <InspectorRow label={t('inspector.type')}      value={<OpBadge op={operation || data.nodeType} />} />
        <InspectorRow label={t('inspector.label')}     value={fullLabel} />
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

      {/* ── SQL preview ──────────────────────────────────────────────────────── */}
      <SqlSection data={data} />

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
    </>
  );
});

// ── SQL preview section ──────────────────────────────────────────────────────

function SqlSection({ data }: { data: DaliNodeData }) {
  const { t } = useTranslation();
  const [copied, setCopied] = useState(false);

  const sqlText = typeof data.metadata?.sqlText === 'string' ? data.metadata.sqlText
    : typeof data.metadata?.snippet === 'string' ? data.metadata.snippet
    : '';

  const handleCopy = useCallback(() => {
    if (!sqlText) return;
    navigator.clipboard.writeText(sqlText).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    });
  }, [sqlText]);

  return (
    <InspectorSection title={t('inspector.sql')} defaultOpen={!!sqlText}>
      {sqlText ? (
        <div style={{ position: 'relative' }}>
          <pre style={{
            padding: '6px 10px', margin: 0,
            fontSize: '10px', lineHeight: '1.5',
            color: 'var(--t1)',
            background: 'var(--bg0)',
            border: '1px solid var(--bd)',
            borderRadius: 4,
            maxHeight: 200, overflowY: 'auto',
            whiteSpace: 'pre-wrap', wordBreak: 'break-word',
            fontFamily: 'var(--seer-font-mono, monospace)',
          }}>
            {sqlText}
          </pre>
          <button
            onClick={handleCopy}
            style={{
              position: 'absolute', top: 4, right: 4,
              fontSize: '9px', fontWeight: 500,
              padding: '2px 6px', borderRadius: 3,
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
      ) : (
        <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
          {t('knot.stmt.noSql')}
        </div>
      )}
    </InspectorSection>
  );
}

InspectorStatement.displayName = 'InspectorStatement';
