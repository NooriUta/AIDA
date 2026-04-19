import { memo, useState } from 'react';
import type { CSSProperties } from 'react';
import { useTranslation } from 'react-i18next';
import { useNavigate } from 'react-router-dom';
import type { DaliNodeData } from '../../types/domain';
import { InspectorSection, InspectorRow } from './InspectorSection';
import { useRoutineDetail } from '../../services/hooks';
import { extractStatementType } from '../../utils/transformHelpers';

interface Props { data: DaliNodeData; nodeId: string }

type RoutineTab = 'main' | 'stats' | 'sql';

// ── Badges ────────────────────────────────────────────────────────────────────

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
      flexShrink: 0,
    }}>
      {kind.toUpperCase()}
    </span>
  );
}

// ── Statement op colours (mirrors InspectorStatement OP_COLORS) ──────────────

const STMT_OP_COLORS: Record<string, string> = {
  INSERT: '#D4922A', UPDATE: '#D4922A', MERGE: '#D4922A', DELETE: '#c85c5c',
  SELECT: '#88B8A8', CTE: '#A8B860',   WITH: '#A8B860',  CREATE: '#7DBF78',
  DROP:   '#c85c5c', TRUNCATE: '#c85c5c', SQ: '#88B8A8', CURSOR: '#88B8A8',
};

// ── Breadcrumb link (schema / package / routine) ─────────────────────────────

function RoutineBreadcrumb({ label, onClick }: { label: string | null | undefined; onClick?: () => void }) {
  if (!label) return null;
  if (onClick) {
    return (
      <button onClick={onClick} title={`Открыть ${label} в Loom`} style={{
        fontSize: '9px', color: 'var(--acc)',
        overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
        marginBottom: 2, letterSpacing: '0.03em', textTransform: 'uppercase',
        background: 'none', border: 'none', padding: 0, cursor: 'pointer',
        textDecoration: 'underline', textDecorationStyle: 'dotted',
        textAlign: 'left', fontFamily: 'inherit', display: 'block',
      }}>
        ◈ {label}
      </button>
    );
  }
  return (
    <div style={{
      fontSize: '9px', color: 'var(--t3)', opacity: 0.7,
      overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
      marginBottom: 2, letterSpacing: '0.03em', textTransform: 'uppercase',
    }}>
      {label}
    </div>
  );
}

// ── Header card (always visible above tabs) ───────────────────────────────────

function RoutineHeaderCard({
  label, routineKind, schema, packageName, onSchemaClick, onPackageClick,
}: {
  label: string;
  routineKind: string;
  schema: string | null;
  packageName: string | null;
  onSchemaClick?: () => void;
  onPackageClick?: () => void;
}) {
  const kindColor = KIND_COLORS[routineKind.toUpperCase()] ?? 'var(--t3)';
  return (
    <div
      role="heading"
      aria-level={2}
      style={{
        display: 'flex', alignItems: 'flex-start', gap: 8,
        padding: '12px 14px',
        background: 'var(--bg0)', borderBottom: '1px solid var(--bd)',
        borderLeft: `3px solid ${kindColor}`,
      }}
    >
      <div style={{ flex: 1, overflow: 'hidden' }}>
        <RoutineBreadcrumb label={schema} onClick={onSchemaClick} />
        {packageName && (
          onPackageClick ? (
            <button onClick={onPackageClick} title={`Открыть ${packageName} в Loom`} style={{
              fontSize: '9px', color: 'var(--acc)',
              overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
              marginBottom: 2, letterSpacing: '0.03em', textTransform: 'uppercase',
              background: 'none', border: 'none', padding: 0, cursor: 'pointer',
              textDecoration: 'underline', textDecorationStyle: 'dotted',
              textAlign: 'left', fontFamily: 'inherit', display: 'block',
            }}>
              ◈ {packageName}
            </button>
          ) : (
            <div style={{
              fontSize: '9px', color: 'var(--t3)', opacity: 0.7,
              overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
              marginBottom: 2, letterSpacing: '0.03em', textTransform: 'uppercase',
            }}>
              {packageName}
            </div>
          )
        )}
        <div title={label} style={{
          fontWeight: 700, fontSize: '13px', color: 'var(--t1)',
          overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
          letterSpacing: '0.02em',
        }}>
          {label}
        </div>
      </div>
      {routineKind && <KindBadge kind={routineKind} />}
    </div>
  );
}

// ── Generic item row ──────────────────────────────────────────────────────────

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

// ── Statement type breakdown ──────────────────────────────────────────────────

function stmtBreakdown(labels: string[]): Map<string, number> {
  const counts = new Map<string, number>();
  for (const lbl of labels) {
    const t = extractStatementType(lbl) ?? '?';
    counts.set(t, (counts.get(t) ?? 0) + 1);
  }
  return counts;
}

// ── Tab bar ───────────────────────────────────────────────────────────────────

function TabBar({ active, onChange, labels }: {
  active: RoutineTab;
  onChange: (t: RoutineTab) => void;
  labels: Record<RoutineTab, string>;
}) {
  return (
    <div role="tablist" style={{
      display: 'flex', borderBottom: '1px solid var(--bd)',
      background: 'var(--bg1)', position: 'sticky', top: 0, zIndex: 1,
    }}>
      {(['main', 'stats', 'sql'] as RoutineTab[]).map((t) => (
        <TabButton key={t} active={active === t} onClick={() => onChange(t)} label={labels[t]} />
      ))}
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

// ── Generic metadata rows ─────────────────────────────────────────────────────

const ROUTINE_META_SKIP = new Set(['sqlText', 'snippet', 'ddlText', 'groupPath', 'routineKind', 'packageName']);

function AllMetaRows({ meta }: { meta: Record<string, unknown> | undefined }) {
  if (!meta) return null;
  const rows = Object.entries(meta).filter(([k, v]) =>
    !ROUTINE_META_SKIP.has(k) && v !== null && v !== undefined && typeof v !== 'object',
  );
  if (rows.length === 0) return null;
  return <>{rows.map(([k, v]) => <InspectorRow key={k} label={k} value={String(v)} />)}</>;
}

// ── Main component ────────────────────────────────────────────────────────────

export const InspectorRoutine = memo(({ data, nodeId }: Props) => {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [tab, setTab] = useState<RoutineTab>('main');

  const isPackage    = data.nodeType === 'DaliPackage';
  const routineKind  = typeof data.metadata?.routineKind === 'string'
    ? data.metadata.routineKind
    : (isPackage ? 'PACKAGE' : '');
  const routinesCount = typeof data.routinesCount === 'number' ? data.routinesCount : null;
  const packageName   = typeof data.metadata?.packageName === 'string' ? data.metadata.packageName : null;
  const sqlText       = typeof data.metadata?.sqlText === 'string' ? data.metadata.sqlText : '';
  const schema        = data.schema ?? (typeof data.metadata?.schema === 'string' ? data.metadata.schema as string : null);

  const { data: detail, isLoading } = useRoutineDetail(nodeId);

  const params      = detail?.nodes.filter(n => n.type === 'DaliParameter') ?? [];
  const vars        = detail?.nodes.filter(n => n.type === 'DaliVariable')  ?? [];
  const stmtNodes   = detail?.nodes.filter(n => n.type === 'DaliStatement') ?? [];
  const pkgRoutines = detail?.nodes.filter(n => n.type === 'DaliRoutine')   ?? [];

  const callsOutEdges = detail?.edges.filter(e => e.type === 'CALLS' && e.source === nodeId) ?? [];
  const callsInEdges  = detail?.edges.filter(e => e.type === 'CALLS' && e.target === nodeId) ?? [];

  const nodeMap    = new Map(detail?.nodes.map(n => [n.id, n]) ?? []);
  const calleesOut = callsOutEdges.map(e => nodeMap.get(e.target)?.label ?? e.target);
  const callersIn  = callsInEdges.map(e => nodeMap.get(e.source)?.label ?? e.source);

  const breakdown  = stmtBreakdown(stmtNodes.map(n => n.label));

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
      {/* Always-visible header: package › name + kind badge */}
      <RoutineHeaderCard
        label={data.label}
        routineKind={routineKind}
        schema={schema}
        packageName={packageName}
        onSchemaClick={schema ? () => navigate(`/knot?schema=${encodeURIComponent(schema)}`) : undefined}
        onPackageClick={!isPackage && packageName ? openInKnot : undefined}
      />

      <TabBar
        active={tab}
        onChange={setTab}
        labels={{
          main:  t('inspector.tabMain'),
          stats: t('inspector.tabStats'),
          sql:   t('inspector.sql'),
        }}
      />

      {/* ── Основное: properties + lazy detail sections ───────────────────── */}
      {tab === 'main' && (
        <div role="tabpanel">
          <InspectorSection title={t('inspector.properties')}>
            {data.schema     && <InspectorRow label={t('inspector.schema')}   value={data.schema} />}
            {data.language   && <InspectorRow label={t('inspector.language')} value={data.language} />}
            {routinesCount !== null && (
              <InspectorRow label={t('inspector.routines')} value={String(routinesCount)} />
            )}
            <AllMetaRows meta={data.metadata as Record<string, unknown>} />
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
                ◈ {t('inspector.openInKnot')}
              </button>
            </div>
          </InspectorSection>

          {/* Parameters (lazy) */}
          {!isPackage && (
            <InspectorSection
              title={`${t('inspector.parameters')} (${isLoading ? '…' : params.length})`}
              defaultOpen={params.length > 0}
            >
              {isLoading ? <div style={loadingStyle}>…</div>
                : params.length === 0
                  ? <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>{t('inspector.noParameters')}</div>
                  : <div style={{ marginTop: 2 }}>{params.map((p) => <ItemRow key={p.id} name={p.label} tag={getDataType(p.meta)} dimTag />)}</div>
              }
            </InspectorSection>
          )}

          {/* Variables (lazy) */}
          {!isPackage && (
            <InspectorSection
              title={`${t('inspector.variables')} (${isLoading ? '…' : vars.length})`}
              defaultOpen={false}
            >
              {isLoading ? <div style={loadingStyle}>…</div>
                : vars.length === 0
                  ? <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>{t('inspector.noVariables')}</div>
                  : <div style={{ marginTop: 2 }}>{vars.map((v) => <ItemRow key={v.id} name={v.label} tag={getDataType(v.meta)} dimTag />)}</div>
              }
            </InspectorSection>
          )}

          {/* Calls out (lazy) */}
          {!isPackage && (
            <InspectorSection
              title={`${t('inspector.callsTo')} (${isLoading ? '…' : calleesOut.length})`}
              defaultOpen={calleesOut.length > 0}
            >
              {isLoading ? <div style={loadingStyle}>…</div>
                : calleesOut.length === 0
                  ? <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>{t('inspector.noCalls')}</div>
                  : <div style={{ marginTop: 2 }}>{calleesOut.map((name, i) => <ItemRow key={i} name={name} />)}</div>
              }
            </InspectorSection>
          )}

          {/* Called by (lazy) */}
          {!isPackage && (
            <InspectorSection
              title={`${t('inspector.calledBy')} (${isLoading ? '…' : callersIn.length})`}
              defaultOpen={callersIn.length > 0}
            >
              {isLoading ? <div style={loadingStyle}>…</div>
                : callersIn.length === 0
                  ? <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>{t('inspector.noCalls')}</div>
                  : <div style={{ marginTop: 2 }}>{callersIn.map((name, i) => <ItemRow key={i} name={name} />)}</div>
              }
            </InspectorSection>
          )}

          {/* Package routines (I-02) */}
          {isPackage && (
            <InspectorSection
              title={`${t('inspector.routines')} (${isLoading ? '…' : pkgRoutines.length || (routinesCount ?? '…')})`}
              defaultOpen
            >
              {isLoading ? <div style={loadingStyle}>…</div>
                : pkgRoutines.length === 0
                  ? <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>{t('inspector.noRoutines', { defaultValue: 'Нет данных' })}</div>
                  : <div style={{ marginTop: 2 }}>
                      {pkgRoutines.map((r) => {
                        const kind = (r.meta?.find?.((m: { key: string }) => m.key === 'routineKind')?.value as string | undefined) ?? '';
                        return (
                          <div
                            key={r.id}
                            onClick={() => navigate(`/knot?pkg=${encodeURIComponent(data.label)}&routine=${encodeURIComponent(r.label)}`)}
                            style={{
                              display: 'flex', alignItems: 'center', gap: 6,
                              padding: '4px 10px', borderTop: '1px solid var(--bd)',
                              fontSize: '11px', cursor: 'pointer',
                            }}
                            onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'var(--bg2)'; }}
                            onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                          >
                            {kind && <KindBadge kind={kind} />}
                            <span style={{
                              flex: 1, color: 'var(--t1)',
                              overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                              fontFamily: 'var(--mono)', fontSize: '11px',
                            }}>
                              {r.label}
                            </span>
                          </div>
                        );
                      })}
                    </div>
              }
            </InspectorSection>
          )}
        </div>
      )}

      {/* ── Статистика: statements breakdown ─────────────────────────────── */}
      {tab === 'stats' && (
        <div role="tabpanel">
          {!isPackage && (
            <>
              {/* Breakdown summary chips */}
              {!isLoading && stmtNodes.length > 0 && (
                <div style={{ padding: '6px 10px', display: 'flex', flexWrap: 'wrap', gap: '4px 6px', borderBottom: '1px solid var(--bd)' }}>
                  {[...breakdown.entries()].sort((a, b) => b[1] - a[1]).map(([type, count]) => (
                    <span key={type} style={{
                      fontSize: '10px', color: 'var(--t2)',
                      background: 'var(--bg3)', border: '1px solid var(--bd)',
                      borderRadius: 3, padding: '1px 5px', fontFamily: 'var(--mono)',
                    }}>
                      {type}<span style={{ color: 'var(--t3)', marginLeft: 3 }}>{count}</span>
                    </span>
                  ))}
                </div>
              )}
              {/* Clickable statement list (I-01) */}
              <InspectorSection
                title={`${t('inspector.statements')} (${isLoading ? '…' : stmtNodes.length})`}
                defaultOpen
              >
                {isLoading ? <div style={loadingStyle}>…</div>
                  : stmtNodes.length === 0
                    ? <div style={{ padding: '4px 10px', fontSize: '11px', color: 'var(--t3)' }}>{t('inspector.noStatements')}</div>
                    : <div style={{ marginTop: 2 }}>
                        {stmtNodes.map((n) => {
                          const opType = extractStatementType(n.label) ?? '?';
                          const opColor = STMT_OP_COLORS[opType] ?? 'var(--t3)';
                          const pkg = packageName ?? data.label;
                          return (
                            <div
                              key={n.id}
                              onClick={() => navigate(`/knot?pkg=${encodeURIComponent(pkg)}&stmt=${encodeURIComponent(n.label)}`)}
                              style={{
                                display: 'flex', alignItems: 'center', gap: 6,
                                padding: '4px 10px', borderTop: '1px solid var(--bd)',
                                fontSize: '11px', cursor: 'pointer',
                              }}
                              onMouseEnter={(e) => { (e.currentTarget as HTMLElement).style.background = 'var(--bg2)'; }}
                              onMouseLeave={(e) => { (e.currentTarget as HTMLElement).style.background = 'transparent'; }}
                            >
                              <span style={{
                                fontSize: '8px', padding: '1px 4px', borderRadius: 2,
                                fontFamily: 'var(--mono)', fontWeight: 700, flexShrink: 0,
                                border: `0.5px solid ${opColor}`, color: opColor,
                                letterSpacing: '0.03em',
                              }}>
                                {opType}
                              </span>
                              <span style={{
                                flex: 1, color: 'var(--t1)',
                                overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                                fontFamily: 'var(--mono)',
                              }}>
                                {n.label}
                              </span>
                            </div>
                          );
                        })}
                      </div>
                }
              </InspectorSection>
            </>
          )}
          {isPackage && routinesCount !== null && (
            <InspectorSection title={t('inspector.properties')} defaultOpen>
              <InspectorRow label={t('inspector.routines')} value={String(routinesCount)} />
            </InspectorSection>
          )}
        </div>
      )}

      {/* ── SQL ──────────────────────────────────────────────────────────── */}
      {tab === 'sql' && (
        <div role="tabpanel">
          {sqlText ? (
            <div style={{ padding: '8px 10px' }}>
              <pre style={{
                padding: '8px 10px', margin: 0,
                fontSize: '11px', lineHeight: '1.5',
                color: 'var(--t1)', background: 'var(--bg0)',
                border: '1px solid var(--bd)', borderRadius: 4,
                maxHeight: 'calc(100vh - 200px)', overflow: 'auto',
                whiteSpace: 'pre', fontFamily: 'var(--mono)',
              }}>
                {sqlText}
              </pre>
            </div>
          ) : (
            <div style={{ padding: '16px 12px', fontSize: '11px', color: 'var(--t3)' }}>
              {t('knot.stmt.noSql', { defaultValue: 'SQL недоступен' })}
            </div>
          )}
        </div>
      )}
    </>
  );
});

InspectorRoutine.displayName = 'InspectorRoutine';
