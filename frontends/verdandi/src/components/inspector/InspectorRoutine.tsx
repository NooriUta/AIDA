import { memo } from 'react';
import type { CSSProperties } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import type { DaliNodeData } from '../../types/domain';
import { InspectorSection, InspectorRow } from './InspectorSection';
import { useRoutineDetail } from '../../services/hooks';
import { extractStatementType } from '../../utils/transformHelpers';

interface Props { data: DaliNodeData; nodeId: string }

// ── Routine kind badge (PROCEDURE / FUNCTION / PACKAGE) ──────────────────────
const KIND_COLORS: Record<string, string> = {
  PROCEDURE: '#D4922A',
  FUNCTION:  '#88B8A8',
  PACKAGE:   '#A8B860',
};

function KindBadge({ kind }: { kind: string }) {
  const color = KIND_COLORS[kind.toUpperCase()] ?? 'var(--t3)';
  return (
    <span style={{
      fontSize: '10px', fontWeight: 700, letterSpacing: '0.06em',
      padding: '1px 6px', borderRadius: 4,
      background: `color-mix(in srgb, ${color} 18%, transparent)`,
      color, border: `1px solid color-mix(in srgb, ${color} 40%, transparent)`,
    }}>
      {kind.toUpperCase()}
    </span>
  );
}

// ── Generic item row (name + optional type tag) ───────────────────────────────
function ItemRow({ name, tag, dimTag }: { name: string; tag?: string; dimTag?: boolean }) {
  return (
    <div style={{
      display: 'flex', alignItems: 'center',
      padding: '3px 10px', borderTop: '1px solid var(--bd)',
      fontSize: '11px', gap: 6,
    }}>
      <span style={{
        flex: 1, color: 'var(--t1)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        fontFamily: 'var(--mono)',
      }}>
        {name}
      </span>
      {tag && (
        <span style={{ color: dimTag ? 'var(--t3)' : 'var(--acc)', fontSize: '10px', flexShrink: 0 }}>
          {tag}
        </span>
      )}
    </div>
  );
}

// ── Statement type breakdown: group labels by SQL keyword ─────────────────────
function stmtBreakdown(labels: string[]): Map<string, number> {
  const counts = new Map<string, number>();
  for (const lbl of labels) {
    const t = extractStatementType(lbl) ?? '?';
    counts.set(t, (counts.get(t) ?? 0) + 1);
  }
  return counts;
}

export const InspectorRoutine = memo(({ data, nodeId }: Props) => {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const isPackage   = data.nodeType === 'DaliPackage';
  const routineKind = typeof data.metadata?.routineKind === 'string'
    ? data.metadata.routineKind
    : (isPackage ? 'PACKAGE' : '');
  const routinesCount = typeof data.routinesCount === 'number' ? data.routinesCount : null;
  const packageName   = typeof data.metadata?.packageName === 'string' ? data.metadata.packageName : null;

  // ── Fetch detail data ─────────────────────────────────────────────────────
  const { data: detail, isLoading } = useRoutineDetail(isPackage ? null : nodeId);

  // Parse nodes by type
  const params    = detail?.nodes.filter(n => n.type === 'DaliParameter')  ?? [];
  const vars      = detail?.nodes.filter(n => n.type === 'DaliVariable')   ?? [];
  const stmtNodes = detail?.nodes.filter(n => n.type === 'DaliStatement')  ?? [];

  // Parse CALLS edges by direction
  const callsOutEdges = detail?.edges.filter(e => e.type === 'CALLS' && e.source === nodeId) ?? [];
  const callsInEdges  = detail?.edges.filter(e => e.type === 'CALLS' && e.target === nodeId) ?? [];

  // Map nodeId → label for callee/caller names
  const nodeMap = new Map(detail?.nodes.map(n => [n.id, n]) ?? []);
  const calleesOut = callsOutEdges.map(e => nodeMap.get(e.target)?.label ?? e.target);
  const callersIn  = callsInEdges.map(e => nodeMap.get(e.source)?.label ?? e.source);

  // Statement breakdown
  const breakdown = stmtBreakdown(stmtNodes.map(n => n.label));

  // Helper: dataType from meta array [{key,value}]
  const getDataType = (metaArr: Array<{ key: string; value: string }> | undefined): string | undefined =>
    metaArr?.find(m => m.key === 'dataType')?.value;

  const openInKnot = () => {
    const params = new URLSearchParams();
    if (isPackage) {
      params.set('pkg', data.label);
    } else {
      if (packageName) params.set('pkg', packageName);
    }
    navigate(`/knot?${params.toString()}`);
  };

  const loadingStyle: CSSProperties = {
    padding: '4px 10px', fontSize: '11px', color: 'var(--t3)', fontStyle: 'italic',
  };

  return (
    <>
      {/* ── Properties ─────────────────────────────────────────────────── */}
      <InspectorSection title={t('inspector.properties')}>
        <InspectorRow label={t('inspector.label')}   value={data.label} />
        <InspectorRow
          label={t('inspector.type')}
          value={routineKind ? <KindBadge kind={routineKind} /> : data.nodeType}
        />
        {data.language    && <InspectorRow label={t('inspector.language')} value={data.language} />}
        {data.schema      && <InspectorRow label={t('inspector.schema')}   value={data.schema} />}
        {packageName      && <InspectorRow label={t('inspector.package')}  value={packageName} />}
        {routinesCount !== null && (
          <InspectorRow label={t('inspector.routines')} value={String(routinesCount)} />
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
            ◈ {t('inspector.openInKnot')}
          </button>
        </div>
      </InspectorSection>

      {/* ── Parameters ─────────────────────────────────────────────────── */}
      {!isPackage && (
        <InspectorSection
          title={`${t('inspector.parameters')} (${isLoading ? '…' : params.length})`}
          defaultOpen={params.length > 0}
        >
          {isLoading ? (
            <div style={loadingStyle}>…</div>
          ) : params.length === 0 ? (
            <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
              {t('inspector.noParameters')}
            </div>
          ) : (
            <div style={{ marginTop: 2 }}>
              {params.map((p) => (
                <ItemRow key={p.id} name={p.label} tag={getDataType(p.meta)} dimTag />
              ))}
            </div>
          )}
        </InspectorSection>
      )}

      {/* ── Variables ──────────────────────────────────────────────────── */}
      {!isPackage && (
        <InspectorSection
          title={`${t('inspector.variables')} (${isLoading ? '…' : vars.length})`}
          defaultOpen={false}
        >
          {isLoading ? (
            <div style={loadingStyle}>…</div>
          ) : vars.length === 0 ? (
            <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
              {t('inspector.noVariables')}
            </div>
          ) : (
            <div style={{ marginTop: 2 }}>
              {vars.map((v) => (
                <ItemRow key={v.id} name={v.label} tag={getDataType(v.meta)} dimTag />
              ))}
            </div>
          )}
        </InspectorSection>
      )}

      {/* ── Statements ─────────────────────────────────────────────────── */}
      {!isPackage && (
        <InspectorSection
          title={`${t('inspector.statements')} (${isLoading ? '…' : stmtNodes.length})`}
          defaultOpen={stmtNodes.length > 0}
        >
          {isLoading ? (
            <div style={loadingStyle}>…</div>
          ) : stmtNodes.length === 0 ? (
            <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
              {t('inspector.noStatements')}
            </div>
          ) : (
            <div style={{ padding: '4px 10px', display: 'flex', flexWrap: 'wrap', gap: '4px 8px' }}>
              {[...breakdown.entries()].sort((a, b) => b[1] - a[1]).map(([type, count]) => (
                <span key={type} style={{
                  fontSize: '11px', color: 'var(--t1)',
                  background: 'var(--bg3)',
                  border: '1px solid var(--bd)',
                  borderRadius: 4,
                  padding: '1px 6px',
                  fontFamily: 'var(--mono)',
                }}>
                  {type}
                  <span style={{ color: 'var(--t3)', marginLeft: 4 }}>{count}</span>
                </span>
              ))}
            </div>
          )}
        </InspectorSection>
      )}

      {/* ── Calls (out) ────────────────────────────────────────────────── */}
      {!isPackage && (
        <InspectorSection
          title={`${t('inspector.callsTo')} (${isLoading ? '…' : calleesOut.length})`}
          defaultOpen={calleesOut.length > 0}
        >
          {isLoading ? (
            <div style={loadingStyle}>…</div>
          ) : calleesOut.length === 0 ? (
            <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
              {t('inspector.noCalls')}
            </div>
          ) : (
            <div style={{ marginTop: 2 }}>
              {calleesOut.map((name, i) => <ItemRow key={i} name={name} />)}
            </div>
          )}
        </InspectorSection>
      )}

      {/* ── Called by (in) ─────────────────────────────────────────────── */}
      {!isPackage && (
        <InspectorSection
          title={`${t('inspector.calledBy')} (${isLoading ? '…' : callersIn.length})`}
          defaultOpen={callersIn.length > 0}
        >
          {isLoading ? (
            <div style={loadingStyle}>…</div>
          ) : callersIn.length === 0 ? (
            <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
              {t('inspector.noCalls')}
            </div>
          ) : (
            <div style={{ marginTop: 2 }}>
              {callersIn.map((name, i) => <ItemRow key={i} name={name} />)}
            </div>
          )}
        </InspectorSection>
      )}
    </>
  );
});

InspectorRoutine.displayName = 'InspectorRoutine';
