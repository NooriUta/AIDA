import { memo, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { KnotSession, KnotStatement, KnotRoutine, KnotCall, KnotParameter, KnotVariable } from '../../services/lineage';

interface Props {
  session: KnotSession;
  routines: KnotRoutine[];
  statements: KnotStatement[];
  calls: KnotCall[];
  parameters: KnotParameter[];
  variables: KnotVariable[];
}

interface StmtSummary {
  geoid: string;
  stmtType: string;
  lineNumber: number;
}

interface RoutineInfo {
  key: string;
  name: string;
  packageName: string;
  routineType: string;
  stmtCount: number;
  tables: Set<string>;
  stmtList: StmtSummary[];
}

// ── Helpers ───────────────────────────────────────────────────────────────────

function truncGeoid(geoid: string | undefined): string {
  if (!geoid) return '—';
  const parts = geoid.split(':');
  if (parts.length <= 3) return geoid;
  return '\u2026:' + parts.slice(-3).join(':');
}

// ── Component ─────────────────────────────────────────────────────────────────

export const KnotRoutines = memo(({ session: s, routines, statements, calls, parameters, variables }: Props) => {
  const { t } = useTranslation();
  const [expandedRoutine, setExpandedRoutine] = useState<string | null>(null);

  // Build call map: routineName → callee list
  const callMap = useMemo(() => {
    const m = new Map<string, { calleeName: string; lineStart: number }[]>();
    for (const c of calls) {
      const list = m.get(c.callerName) || [];
      list.push({ calleeName: c.calleeName, lineStart: c.lineStart });
      m.set(c.callerName, list);
    }
    return m;
  }, [calls]);

  // Build param map: routineName → parameters
  const paramMap = useMemo(() => {
    const m = new Map<string, KnotParameter[]>();
    for (const p of parameters) {
      const list = m.get(p.routineName) || [];
      list.push(p);
      m.set(p.routineName, list);
    }
    return m;
  }, [parameters]);

  // Build var map: routineName → variables
  const varMap = useMemo(() => {
    const m = new Map<string, KnotVariable[]>();
    for (const v of variables) {
      const list = m.get(v.routineName) || [];
      list.push(v);
      m.set(v.routineName, list);
    }
    return m;
  }, [variables]);

  // Build routine list: base from routines[] (full DB list), enrich with statement data
  const { packages, standalone } = useMemo(() => {
    const routineMap = new Map<string, RoutineInfo>();

    // 1. Initialise from complete routines list (includes routines with 0 statements)
    for (const r of routines) {
      const key = `${r.packageGeoid}:${r.routineName}`;
      routineMap.set(key, {
        key,
        name: r.routineName,
        packageName: r.packageGeoid,
        routineType: r.routineType,
        stmtCount: 0,
        tables: new Set(),
        stmtList: [],
      });
    }

    // 2. Enrich with statement data (stmtCount, tables, stmtList)
    const collect = (stmts: KnotStatement[]) => {
      for (const st of stmts) {
        const rName = st.routineName || 'UNKNOWN';
        const pkg   = st.packageName || '';
        const key   = `${pkg}:${rName}`;
        // Try exact key first, then standalone (empty package) fallback
        const r = routineMap.get(key) ?? routineMap.get(`:${rName}`);
        if (r) {
          r.stmtCount++;
          r.stmtList.push({ geoid: st.geoid, stmtType: st.stmtType, lineNumber: st.lineNumber });
          st.sourceTables?.forEach(ref => r.tables.add(ref.name));
          st.targetTables?.forEach(ref => r.tables.add(ref.name));
        }
        if (st.children?.length) collect(st.children);
      }
    };
    collect(statements);

    const pkgMap = new Map<string, RoutineInfo[]>();
    const standaloneList: RoutineInfo[] = [];

    for (const r of routineMap.values()) {
      if (r.packageName) {
        if (!pkgMap.has(r.packageName)) pkgMap.set(r.packageName, []);
        pkgMap.get(r.packageName)!.push(r);
      } else {
        standaloneList.push(r);
      }
    }

    return {
      packages: Array.from(pkgMap.entries()),
      standalone: standaloneList,
    };
  }, [routines, statements]);

  const toggleRoutine = (key: string) => {
    setExpandedRoutine(prev => prev === key ? null : key);
  };

  return (
    <div style={{ padding: 24, overflowY: 'auto', height: '100%', boxSizing: 'border-box' }}>

      {/* Metric tiles */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12, marginBottom: 16 }}>
        <Metric value={s.routineCount}   label={t('knot.session.routines')} />
        <Metric value={s.packageCount}   label={t('knot.session.packages')} />
        <Metric value={s.parameterCount} label={t('knot.session.parameters')} />
        <Metric value={s.variableCount}  label={t('knot.session.variables')} />
      </div>

      {/* Package cards */}
      {packages.map(([pkgName, routines]) => (
        <div key={pkgName} style={{
          background: 'var(--bg2)', border: '1px solid var(--bd)',
          borderRadius: 8, marginBottom: 12, overflow: 'hidden',
        }}>
          <div style={{
            padding: '10px 16px', borderBottom: '1px solid var(--bd)',
            display: 'flex', alignItems: 'center', gap: 8,
          }}>
            <div style={{ fontSize: 12, fontWeight: 500, color: 'var(--t1)' }}>{pkgName}</div>
            <Badge bg="color-mix(in srgb, var(--acc) 12%, transparent)" color="var(--acc)">PACKAGE</Badge>
            <span style={{ marginLeft: 'auto', fontSize: 10, color: 'var(--t3)' }}>
              {routines.length} routine{routines.length !== 1 ? 's' : ''}
            </span>
          </div>
          <div style={{ padding: 0 }}>
            {routines.map(r => (
              <RoutineRow
                key={r.key}
                routine={r}
                expanded={expandedRoutine === r.key}
                onToggle={() => toggleRoutine(r.key)}
                callees={callMap.get(r.name) || []}
                params={paramMap.get(r.name) || []}
                vars={varMap.get(r.name) || []}
              />
            ))}
          </div>
        </div>
      ))}

      {/* Standalone routines */}
      {standalone.length > 0 && (
        <div style={{
          background: 'var(--bg2)', border: '1px solid var(--bd)',
          borderRadius: 8, overflow: 'hidden',
        }}>
          <div style={{
            padding: '10px 16px', borderBottom: '1px solid var(--bd)',
            fontSize: 12, fontWeight: 500, color: 'var(--t1)',
          }}>
            Standalone routines
          </div>
          <div style={{ padding: 0 }}>
            {standalone.map(r => (
              <RoutineRow
                key={r.key}
                routine={r}
                expanded={expandedRoutine === r.key}
                onToggle={() => toggleRoutine(r.key)}
                callees={callMap.get(r.name) || []}
                params={paramMap.get(r.name) || []}
                vars={varMap.get(r.name) || []}
              />
            ))}
          </div>
        </div>
      )}
    </div>
  );
});

KnotRoutines.displayName = 'KnotRoutines';

// ── RoutineRow ─────────────────────────────────────────────────────────────────

const TYPE_COLOR: Record<string, { bg: string; color: string }> = {
  SELECT: { bg: 'rgba(136,184,168,.15)', color: 'var(--inf)' },
  INSERT: { bg: 'rgba(125,191,120,.15)', color: 'var(--suc)' },
  UPDATE: { bg: 'rgba(212,146,42,.15)',  color: 'var(--wrn)' },
  DELETE: { bg: 'rgba(192,96,96,.15)',   color: 'var(--dan, #C06060)' },
  MERGE:  { bg: 'rgba(154,140,110,.15)', color: 'var(--t2)' },
};

const DIR_COLOR: Record<string, string> = {
  IN:     'var(--inf)',
  OUT:    'var(--suc)',
  'IN OUT': 'var(--wrn)',
};

function RoutineRow({ routine: r, expanded, onToggle, callees, params, vars }: {
  routine: RoutineInfo; expanded: boolean; onToggle: () => void;
  callees: { calleeName: string; lineStart: number }[];
  params: KnotParameter[];
  vars: KnotVariable[];
}) {
  const tables = Array.from(r.tables);
  const hasExtras = callees.length > 0 || params.length > 0 || vars.length > 0;

  // Dynamic grid: 2 always (stmts + tables) + extras columns
  const extraCols = (callees.length > 0 ? 1 : 0) + (params.length > 0 ? 1 : 0) + (vars.length > 0 ? 1 : 0);
  const gridCols = `1fr 1fr${extraCols > 0 ? ` repeat(${extraCols}, 1fr)` : ''}`;

  return (
    <>
      <div
        onClick={onToggle}
        style={{
          display: 'flex', alignItems: 'flex-start', gap: 8,
          padding: '8px 14px', cursor: 'pointer',
          transition: 'background 0.1s',
          borderBottom: '1px solid var(--bd)',
          ...(expanded ? { background: 'var(--bg3)', borderLeft: '2px solid var(--acc)' } : {}),
        }}
        onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
        onMouseLeave={e => { if (!expanded) (e.currentTarget as HTMLElement).style.background = ''; }}
      >
        <div style={{ fontSize: 10, color: 'var(--t3)', flexShrink: 0, marginTop: 3, width: 12 }}>
          {expanded ? '\u25BC' : '\u25B6'}
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
            {r.routineType.toUpperCase().includes('FUNCTION')
              ? <Badge bg="rgba(136,184,168,.15)" color="var(--inf)">FUNC</Badge>
              : <Badge bg="rgba(125,191,120,.12)" color="var(--suc)">PROC</Badge>
            }
            <span style={{ fontFamily: 'var(--mono)', fontSize: 12, fontWeight: 500, color: 'var(--t1)' }}>
              {r.name}
            </span>
          </div>
          <div style={{ display: 'flex', gap: 4, marginTop: 3, flexWrap: 'wrap' }}>
            <Chip>{r.stmtCount} stmts</Chip>
            <Chip>{tables.length} tables</Chip>
            {params.length > 0 && <Chip>{params.length} params</Chip>}
            {vars.length > 0 && <Chip>{vars.length} vars</Chip>}
          </div>
        </div>
      </div>

      {expanded && (
        <div style={{
          background: 'var(--bg3)', padding: '14px 14px 14px 36px',
          borderBottom: '1px solid var(--bd)',
          display: 'grid', gridTemplateColumns: gridCols, gap: 20,
        }}>

          {/* Statements */}
          <div>
            <SectionLabel>{r.stmtCount} {r.stmtCount === 1 ? 'Statement' : 'Statements'}</SectionLabel>
            {r.stmtList.length === 0
              ? <div style={{ fontSize: 11, color: 'var(--t3)' }}>No statements</div>
              : r.stmtList.slice(0, 15).map((st, i) => {
                  const tc = TYPE_COLOR[st.stmtType] || { bg: 'rgba(102,92,72,.15)', color: 'var(--t3)' };
                  return (
                    <div key={i} style={{
                      display: 'flex', alignItems: 'center', gap: 6,
                      padding: '2px 0', borderBottom: i < r.stmtList.length - 1 ? '1px solid var(--bd)' : 'none',
                    }}>
                      <span style={{
                        display: 'inline-block', padding: '1px 5px', borderRadius: 3,
                        fontSize: 9, fontFamily: 'var(--mono)',
                        background: tc.bg, color: tc.color, flexShrink: 0,
                      }}>
                        {st.stmtType}
                      </span>
                      <span style={{
                        fontFamily: 'var(--mono)', fontSize: 10,
                        color: 'var(--t2)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                      }}>
                        {truncGeoid(st.geoid)}
                      </span>
                      {st.lineNumber > 0 && (
                        <span style={{ fontSize: 9, color: 'var(--t3)', flexShrink: 0 }}>
                          :{st.lineNumber}
                        </span>
                      )}
                    </div>
                  );
                })
            }
            {r.stmtList.length > 15 && (
              <div style={{ fontSize: 10, color: 'var(--t3)', marginTop: 4 }}>
                +{r.stmtList.length - 15} more
              </div>
            )}
          </div>

          {/* Tables */}
          <div>
            <SectionLabel>{tables.length} Tables used</SectionLabel>
            {tables.length === 0
              ? <div style={{ fontSize: 11, color: 'var(--t3)' }}>No tables</div>
              : tables.slice(0, 15).map(tbl => (
                  <div key={tbl} style={{ marginBottom: 2 }}>
                    <span style={{
                      display: 'inline-block', padding: '1px 6px', borderRadius: 3,
                      fontSize: 10, fontFamily: 'var(--mono)',
                      background: 'var(--bg2)', border: '1px solid var(--bd)', color: 'var(--t2)',
                    }}>
                      {tbl}
                    </span>
                  </div>
                ))
            }
            {tables.length > 15 && (
              <div style={{ fontSize: 10, color: 'var(--t3)', marginTop: 4 }}>
                +{tables.length - 15} more
              </div>
            )}
          </div>

          {/* Parameters */}
          {params.length > 0 && (
            <div>
              <SectionLabel>{params.length} Parameter{params.length !== 1 ? 's' : ''}</SectionLabel>
              {params.slice(0, 20).map((p, i) => (
                <div key={i} style={{
                  display: 'flex', alignItems: 'center', gap: 6,
                  padding: '2px 0', borderBottom: i < params.length - 1 ? '1px solid var(--bd)' : 'none',
                }}>
                  {p.direction && (
                    <span style={{
                      fontSize: 8, fontFamily: 'var(--mono)', fontWeight: 600,
                      color: DIR_COLOR[p.direction.toUpperCase()] || 'var(--t3)',
                      flexShrink: 0, minWidth: 18,
                    }}>
                      {p.direction.toUpperCase() === 'IN OUT' ? 'IO' : p.direction.toUpperCase().charAt(0)}
                    </span>
                  )}
                  <span style={{
                    fontFamily: 'var(--mono)', fontSize: 10,
                    color: 'var(--t1)', flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                  }}>
                    {p.paramName || '—'}
                  </span>
                  {p.dataType && (
                    <span style={{ fontSize: 9, color: 'var(--t3)', flexShrink: 0, fontFamily: 'var(--mono)' }}>
                      {p.dataType}
                    </span>
                  )}
                </div>
              ))}
              {params.length > 20 && (
                <div style={{ fontSize: 10, color: 'var(--t3)', marginTop: 4 }}>
                  +{params.length - 20} more
                </div>
              )}
            </div>
          )}

          {/* Variables */}
          {vars.length > 0 && (
            <div>
              <SectionLabel>{vars.length} Variable{vars.length !== 1 ? 's' : ''}</SectionLabel>
              {vars.slice(0, 20).map((v, i) => (
                <div key={i} style={{
                  display: 'flex', alignItems: 'center', gap: 6,
                  padding: '2px 0', borderBottom: i < vars.length - 1 ? '1px solid var(--bd)' : 'none',
                }}>
                  <span style={{
                    fontFamily: 'var(--mono)', fontSize: 10,
                    color: 'var(--t1)', flex: 1, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                  }}>
                    {v.varName || '—'}
                  </span>
                  {v.dataType && (
                    <span style={{ fontSize: 9, color: 'var(--t3)', flexShrink: 0, fontFamily: 'var(--mono)' }}>
                      {v.dataType}
                    </span>
                  )}
                </div>
              ))}
              {vars.length > 20 && (
                <div style={{ fontSize: 10, color: 'var(--t3)', marginTop: 4 }}>
                  +{vars.length - 20} more
                </div>
              )}
            </div>
          )}

          {/* Calls (only rendered when there are callees) */}
          {callees.length > 0 && (
            <div>
              <SectionLabel>{callees.length} Routine{callees.length !== 1 ? 's' : ''} called</SectionLabel>
              {callees.slice(0, 15).map((c, i) => (
                <div key={i} style={{
                  display: 'flex', alignItems: 'center', gap: 6,
                  padding: '2px 0', borderBottom: i < callees.length - 1 ? '1px solid var(--bd)' : 'none',
                }}>
                  <span style={{
                    fontFamily: 'var(--mono)', fontSize: 10,
                    color: 'var(--t1)', flex: 1,
                  }}>{c.calleeName || '—'}</span>
                  {c.lineStart > 0 && (
                    <span style={{ fontSize: 9, color: 'var(--t3)', flexShrink: 0 }}>
                      :{c.lineStart}
                    </span>
                  )}
                </div>
              ))}
              {callees.length > 15 && (
                <div style={{ fontSize: 10, color: 'var(--t3)', marginTop: 4 }}>
                  +{callees.length - 15} more
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </>
  );
}

// ── Helpers ───────────────────────────────────────────────────────────────────

function SectionLabel({ children }: { children: React.ReactNode }) {
  return (
    <div style={{
      fontSize: 10, fontWeight: 500, letterSpacing: '0.08em',
      textTransform: 'uppercase', color: 'var(--t3)',
      marginBottom: 6, paddingBottom: 4, borderBottom: '1px solid var(--bd)',
    }}>
      {children}
    </div>
  );
}

function Metric({ value, label }: { value: number; label: string }) {
  return (
    <div style={{
      background: 'var(--bg2)', border: '1px solid var(--bd)',
      borderRadius: 8, padding: '12px 16px',
      display: 'flex', flexDirection: 'column', gap: 4,
    }}>
      <div style={{ fontSize: 22, fontWeight: 600, lineHeight: 1, color: 'var(--t1)' }}>
        {value}
      </div>
      <div style={{ fontSize: 10, color: 'var(--t3)' }}>{label}</div>
    </div>
  );
}

function Badge({ children, bg, color }: { children: string; bg: string; color: string }) {
  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center',
      padding: '2px 7px', borderRadius: 3,
      fontSize: 10, fontWeight: 500,
      fontFamily: 'var(--mono)',
      background: bg, color,
    }}>
      {children}
    </span>
  );
}

function Chip({ children }: { children: React.ReactNode }) {
  return (
    <span style={{
      padding: '1px 6px', borderRadius: 10, fontSize: 9,
      background: 'var(--bg3)', color: 'var(--t3)',
      border: '1px solid var(--bd)',
    }}>
      {children}
    </span>
  );
}
