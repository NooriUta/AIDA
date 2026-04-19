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

const OP_COLORS: Record<string, string> = {
  INSERT: '#D4922A', UPDATE: '#D4922A', MERGE: '#D4922A', DELETE: '#c85c5c',
  SELECT: '#88B8A8', CTE: '#A8B860',   WITH: '#A8B860',  CREATE: '#7DBF78',
  DROP:   '#c85c5c', TRUNCATE: '#c85c5c', SQ: '#88B8A8', CURSOR: '#88B8A8',
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
  const { data: detail, isLoading } = useRoutineDetail(nodeId);

  // Parse nodes by type
  const params        = detail?.nodes.filter(n => n.type === 'DaliParameter')  ?? [];
  const vars          = detail?.nodes.filter(n => n.type === 'DaliVariable')   ?? [];
  const stmtNodes     = detail?.nodes.filter(n => n.type === 'DaliStatement')  ?? [];
  const routineNodes  = detail?.nodes.filter(n => n.type === 'DaliRoutine' || n.type === 'DaliPackage') ?? [];

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
            <>
              {/* Summary breakdown */}
              <div style={{ padding: '3px 10px 4px', display: 'flex', flexWrap: 'wrap', gap: '3px 6px' }}>
                {[...breakdown.entries()].sort((a, b) => b[1] - a[1]).map(([type, count]) => (
                  <span key={type} style={{
                    fontSize: '10px', color: 'var(--t3)',
                    fontFamily: 'var(--mono)',
                  }}>
                    {type}<span style={{ color: 'var(--t2)', marginLeft: 2 }}>×{count}</span>
                  </span>
                ))}
              </div>
              {/* Clickable rows */}
              {stmtNodes.map(n => {
                const op   = extractStatementType(n.label) ?? '?';
                const line = n.label.split(':').at(-1);
                const opColor = OP_COLORS[op] ?? 'var(--t3)';
                const knotUrl = `/knot?${new URLSearchParams({
                  ...(packageName ? { pkg: packageName } : {}),
                  stmt: n.id,
                }).toString()}`;
                return (
                  <div
                    key={n.id}
                    onClick={() => navigate(knotUrl)}
                    title={n.label}
                    style={{
                      display: 'flex', alignItems: 'center', gap: 6,
                      padding: '3px 10px', borderTop: '1px solid var(--bd)',
                      cursor: 'pointer', fontSize: '11px',
                    }}
                    onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = 'var(--bg2)'; }}
                    onMouseLeave={e => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                  >
                    <span style={{
                      fontSize: '9px', padding: '1px 5px', borderRadius: 2,
                      fontFamily: 'var(--mono)', fontWeight: 700,
                      border: `0.5px solid ${opColor}`, color: opColor,
                      flexShrink: 0, letterSpacing: '0.03em',
                    }}>
                      {op}
                    </span>
                    <span style={{
                      flex: 1, color: 'var(--t1)', overflow: 'hidden',
                      textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                      fontFamily: 'var(--mono)',
                    }}>
                      {n.label}
                    </span>
                    {line && (
                      <span style={{ fontSize: '10px', color: 'var(--t3)', flexShrink: 0 }}>
                        :{line}
                      </span>
                    )}
                  </div>
                );
              })}
            </>
          )}
        </InspectorSection>
      )}

      {/* ── Package: routines list ──────────────────────────────────────── */}
      {isPackage && (
        <InspectorSection
          title={`${t('inspector.routines')} (${isLoading ? '…' : routineNodes.length})`}
          defaultOpen={routineNodes.length > 0}
        >
          {isLoading ? (
            <div style={loadingStyle}>…</div>
          ) : routineNodes.length === 0 ? (
            <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>
              {t('inspector.noRoutines')}
            </div>
          ) : (
            routineNodes.map(r => {
              const kind = (r.meta?.find(m => m.key === 'routineKind')?.value ?? '').toUpperCase();
              return (
                <div
                  key={r.id}
                  onClick={() => navigate(`/knot?${new URLSearchParams({ pkg: data.label }).toString()}`)}
                  title={r.label}
                  style={{
                    display: 'flex', alignItems: 'center', gap: 6,
                    padding: '3px 10px', borderTop: '1px solid var(--bd)',
                    cursor: 'pointer', fontSize: '11px',
                  }}
                  onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = 'var(--bg2)'; }}
                  onMouseLeave={e => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                >
                  {kind && <KindBadge kind={kind} />}
                  <span style={{
                    flex: 1, color: 'var(--t1)', overflow: 'hidden',
                    textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                    fontFamily: 'var(--mono)',
                  }}>
                    {r.label}
                  </span>
                </div>
              );
            })
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
