import { memo, useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import type { KnotTable, KnotStatement } from '../../services/lineage';

interface Props {
  tables: KnotTable[];
  statements: KnotStatement[];
}

// ── Helpers ─────────────────────────────────────────────────────────────────

function stmtShortLabel(st: KnotStatement): string {
  const parts: string[] = [];
  if (st.routineName) parts.push(st.routineName);
  if (st.stmtType)    parts.push(st.stmtType);
  if (st.lineNumber)  parts.push(`:${st.lineNumber}`);
  return parts.join(' ') || st.geoid?.split(':').slice(-2).join(':') || '—';
}

// ── Component ────────────────────────────────────────────────────────────────

export const KnotStructure = memo(({ tables, statements }: Props) => {
  const { t } = useTranslation();
  const [filter, setFilter] = useState('');
  const [expanded, setExpanded] = useState<Set<string>>(new Set());

  const schemas = useMemo(() => {
    const s = new Set<string>();
    tables.forEach(tb => { if (tb.schema) s.add(tb.schema); });
    return s.size;
  }, [tables]);

  // Reverse-map: tableGeoid → statement labels
  const tableUsage = useMemo(() => {
    const srcMap = new Map<string, string[]>();
    const tgtMap = new Map<string, string[]>();
    const walk = (stmts: KnotStatement[]) => {
      for (const st of stmts) {
        const label = stmtShortLabel(st);
        st.sourceTables?.forEach(geoid => {
          if (!srcMap.has(geoid)) srcMap.set(geoid, []);
          srcMap.get(geoid)!.push(label);
        });
        st.targetTables?.forEach(geoid => {
          if (!tgtMap.has(geoid)) tgtMap.set(geoid, []);
          tgtMap.get(geoid)!.push(label);
        });
        if (st.children?.length) walk(st.children);
      }
    };
    walk(statements);
    return { srcMap, tgtMap };
  }, [statements]);

  const filtered = useMemo(() => {
    if (!filter) return tables;
    const q = filter.toLowerCase();
    return tables.filter(tb =>
      tb.name.toLowerCase().includes(q) ||
      tb.geoid.toLowerCase().includes(q) ||
      tb.schema.toLowerCase().includes(q)
    );
  }, [tables, filter]);

  const toggle = (id: string) => {
    setExpanded(prev => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  };

  return (
    <div style={{ padding: 24, overflowY: 'auto', height: '100%', boxSizing: 'border-box' }}>

      {/* Search bar */}
      <div style={{
        display: 'flex', alignItems: 'center', gap: 8,
        background: 'var(--bg3)', border: '1px solid var(--bd)',
        borderRadius: 6, padding: '5px 10px', fontSize: 12,
        color: 'var(--t2)', marginBottom: 12,
      }}>
        <span style={{ fontSize: 11, color: 'var(--t3)' }}>{'\u2315'}</span>
        <input
          value={filter}
          onChange={e => setFilter(e.target.value)}
          placeholder={t('knot.filterTables') || 'filter tables\u2026'}
          style={{
            background: 'transparent', border: 'none', outline: 'none',
            color: 'var(--t1)', fontSize: 12, flex: 1, fontFamily: 'inherit',
          }}
        />
      </div>

      {/* Card */}
      <div style={{
        background: 'var(--bg2)', border: '1px solid var(--bd)', borderRadius: 8,
      }}>
        <div style={{
          padding: '12px 16px',
          borderBottom: '1px solid var(--bd)',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between',
        }}>
          <div style={{ fontSize: 12, fontWeight: 500, color: 'var(--t1)' }}>
            {t('knot.tabs.structure')}
          </div>
          <div style={{ fontSize: 10, color: 'var(--t3)' }}>
            {filtered.length} {t('knot.session.tables').toLowerCase()}
            {schemas > 0 && ` · ${schemas} ${t('knot.session.schemas').toLowerCase()}`}
          </div>
        </div>

        <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 12 }}>
          <thead>
            <tr>
              {[
                { label: '',                          w: 28,  center: false },
                { label: t('knot.stmt.tableGeoid'),   w: null, center: false },
                { label: t('knot.table.name'),        w: null, center: false },
                { label: t('knot.table.schema'),      w: null, center: false },
                { label: t('knot.table.type'),        w: null, center: false },
                { label: t('knot.table.aliases'),     w: null, center: false },
                { label: t('knot.table.columns'),     w: 50,  center: true  },
                { label: 'S',                         w: 32,  center: true  },
                { label: 'T',                         w: 32,  center: true  },
              ].map((h, i) => (
                <th key={i} style={{
                  padding: '8px 12px',
                  textAlign: h.center ? 'center' : 'left',
                  fontSize: 10, fontWeight: 500, letterSpacing: '0.06em',
                  color: 'var(--t3)', textTransform: 'uppercase',
                  borderBottom: '1px solid var(--bd)', whiteSpace: 'nowrap',
                  ...(h.w ? { width: h.w } : {}),
                }}>
                  {h.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {filtered.map(tb => {
              const isOpen = expanded.has(tb.id);
              const srcStmts = tableUsage.srcMap.get(tb.name) || [];
              const tgtStmts = tableUsage.tgtMap.get(tb.name) || [];
              return [
                <tr
                  key={tb.id}
                  onClick={() => toggle(tb.id)}
                  style={{
                    cursor: 'pointer', transition: 'background 0.1s',
                    background: isOpen ? 'var(--bg3)' : '',
                  }}
                  onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
                  onMouseLeave={e => { (e.currentTarget as HTMLElement).style.background = isOpen ? 'var(--bg3)' : ''; }}
                >
                  <td style={{ textAlign: 'center', color: 'var(--t3)', fontSize: 10, padding: '8px 12px', borderBottom: '1px solid var(--bd)' }}>
                    {isOpen ? '\u25BC' : '\u25B6'}
                  </td>
                  <td style={{ padding: '8px 12px', borderBottom: '1px solid var(--bd)' }}>
                    <span style={{ fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t2)' }}>{tb.geoid}</span>
                  </td>
                  <td style={{ padding: '8px 12px', borderBottom: '1px solid var(--bd)', fontWeight: 500 }}>{tb.name}</td>
                  <td style={{ padding: '8px 12px', borderBottom: '1px solid var(--bd)' }}>
                    {tb.schema && <Tag>{tb.schema}</Tag>}
                  </td>
                  <td style={{ padding: '8px 12px', borderBottom: '1px solid var(--bd)' }}>
                    <Tag muted={tb.tableType === 'VIEW'}>{tb.tableType}</Tag>
                  </td>
                  <td style={{ padding: '8px 12px', borderBottom: '1px solid var(--bd)', maxWidth: 120 }}>
                    {tb.aliases?.length > 0 ? (
                      <div style={{ display: 'flex', gap: 3, flexWrap: 'wrap' }}>
                        {tb.aliases.slice(0, 3).map(a => (
                          <span key={a} style={{
                            display: 'inline-block', padding: '1px 5px', borderRadius: 3,
                            fontSize: 9, fontFamily: 'var(--mono)',
                            background: 'color-mix(in srgb, var(--acc) 10%, transparent)',
                            border: '1px solid color-mix(in srgb, var(--acc) 25%, transparent)',
                            color: 'var(--acc)',
                          }}>{a}</span>
                        ))}
                        {tb.aliases.length > 3 && (
                          <span style={{ fontSize: 9, color: 'var(--t3)' }}>+{tb.aliases.length - 3}</span>
                        )}
                      </div>
                    ) : (
                      <span style={{ color: 'var(--t3)', fontSize: 10 }}>—</span>
                    )}
                  </td>
                  <td style={{ padding: '8px 12px', borderBottom: '1px solid var(--bd)', textAlign: 'center', color: 'var(--t2)' }}>
                    {tb.columnCount}
                  </td>
                  <td style={{ padding: '8px 12px', borderBottom: '1px solid var(--bd)', textAlign: 'center' }}>
                    {tb.sourceCount > 0
                      ? <span style={{ color: 'var(--inf)', fontWeight: 600 }}>{tb.sourceCount}</span>
                      : <span style={{ color: 'var(--t3)' }}>{'\u2014'}</span>}
                  </td>
                  <td style={{ padding: '8px 12px', borderBottom: '1px solid var(--bd)', textAlign: 'center' }}>
                    {tb.targetCount > 0
                      ? <span style={{ color: 'var(--suc)', fontWeight: 600 }}>{tb.targetCount}</span>
                      : <span style={{ color: 'var(--t3)' }}>{'\u2014'}</span>}
                  </td>
                </tr>,

                isOpen && (
                  <tr key={`${tb.id}-exp`}>
                    <td colSpan={9} style={{ padding: 0, background: 'var(--bg3)', borderBottom: '2px solid var(--acc)' }}>
                      <div style={{ padding: '14px 16px', display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>

                        {/* Left: Columns table */}
                        <div>
                          <SectionLabel>{t('knot.tabs.structure')} — {t('knot.session.columns')} ({tb.columns.length})</SectionLabel>
                          {/* Table-level aliases */}
                          {tb.aliases?.length > 0 && (
                            <div style={{ display: 'flex', alignItems: 'center', gap: 4, marginBottom: 6, flexWrap: 'wrap' }}>
                              <span style={{ fontSize: 9, color: 'var(--t3)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
                                {t('knot.table.aliases')}:
                              </span>
                              {tb.aliases.map(a => (
                                <span key={a} style={{
                                  display: 'inline-block', padding: '1px 6px', borderRadius: 3,
                                  fontSize: 10, fontFamily: 'var(--mono)',
                                  background: 'color-mix(in srgb, var(--acc) 10%, transparent)',
                                  border: '1px solid color-mix(in srgb, var(--acc) 25%, transparent)',
                                  color: 'var(--acc)',
                                }}>{a}</span>
                              ))}
                            </div>
                          )}
                          {tb.columns.length > 0 ? (
                            <div style={{ overflowX: 'auto' }}>
                              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 11 }}>
                                <thead>
                                  <tr>
                                    <MiniTh center>#</MiniTh>
                                    <MiniTh>{t('knot.column.name')}</MiniTh>
                                    <MiniTh>{t('knot.column.alias')}</MiniTh>
                                    <MiniTh>{t('knot.column.dataType')}</MiniTh>
                                    <MiniTh center>{t('knot.column.refs')}</MiniTh>
                                  </tr>
                                </thead>
                                <tbody>
                                  {tb.columns.slice(0, 30).map(col => (
                                    <tr key={col.id}>
                                      <MiniTd center muted>{col.position}</MiniTd>
                                      <MiniTd mono bold>{col.name}</MiniTd>
                                      <MiniTd mono muted>{col.alias || '—'}</MiniTd>
                                      <MiniTd mono muted>{col.dataType || '—'}</MiniTd>
                                      <MiniTd center>
                                        {col.atomRefCount > 0
                                          ? <span style={{ color: 'var(--inf)' }}>{col.atomRefCount}</span>
                                          : <span style={{ color: 'var(--t3)' }}>—</span>}
                                      </MiniTd>
                                    </tr>
                                  ))}
                                </tbody>
                              </table>
                              {tb.columns.length > 30 && (
                                <div style={{ padding: '4px 6px', fontSize: 10, color: 'var(--t3)' }}>
                                  +{tb.columns.length - 30} {t('knot.nodes.moreColumns', { count: '' }).replace('+ more', '').trim()}
                                </div>
                              )}
                            </div>
                          ) : (
                            <div style={{ fontSize: 11, color: 'var(--t3)' }}>—</div>
                          )}
                        </div>

                        {/* Right: Usage (source/target in which statements) */}
                        <div>
                          <SectionLabel>Usage</SectionLabel>

                          {srcStmts.length > 0 && (
                            <>
                              <UsageLabel color="var(--inf)">
                                {t('knot.structure.usedAsSource')} ({srcStmts.length})
                              </UsageLabel>
                              <div style={{ display: 'flex', flexDirection: 'column', gap: 2, marginBottom: 10 }}>
                                {srcStmts.slice(0, 8).map((name, i) => (
                                  <StmtChip key={i} color="var(--inf)">{name}</StmtChip>
                                ))}
                                {srcStmts.length > 8 && (
                                  <span style={{ fontSize: 10, color: 'var(--t3)', paddingLeft: 4 }}>
                                    +{srcStmts.length - 8} more
                                  </span>
                                )}
                              </div>
                            </>
                          )}

                          {tgtStmts.length > 0 && (
                            <>
                              <UsageLabel color="var(--suc)">
                                {t('knot.structure.usedAsTarget')} ({tgtStmts.length})
                              </UsageLabel>
                              <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                                {tgtStmts.slice(0, 8).map((name, i) => (
                                  <StmtChip key={i} color="var(--suc)">{name}</StmtChip>
                                ))}
                                {tgtStmts.length > 8 && (
                                  <span style={{ fontSize: 10, color: 'var(--t3)', paddingLeft: 4 }}>
                                    +{tgtStmts.length - 8} more
                                  </span>
                                )}
                              </div>
                            </>
                          )}

                          {srcStmts.length === 0 && tgtStmts.length === 0 && (
                            <div style={{ fontSize: 11, color: 'var(--t3)' }}>No direct statement references</div>
                          )}
                        </div>
                      </div>
                    </td>
                  </tr>
                ),
              ];
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
});

KnotStructure.displayName = 'KnotStructure';

// ── Sub-components ────────────────────────────────────────────────────────────

function Tag({ children, muted }: { children: React.ReactNode; muted?: boolean }) {
  return (
    <span style={{
      display: 'inline-block', padding: '1px 6px', borderRadius: 3,
      fontSize: 10, background: 'var(--bg2)', border: '1px solid var(--bd)',
      color: muted ? 'var(--t3)' : 'var(--t2)',
    }}>
      {children}
    </span>
  );
}

function SectionLabel({ children }: { children: React.ReactNode }) {
  return (
    <div style={{
      fontSize: 10, fontWeight: 500, letterSpacing: '0.08em',
      textTransform: 'uppercase', color: 'var(--t3)',
      marginBottom: 8, paddingBottom: 4, borderBottom: '1px solid var(--bd)',
    }}>
      {children}
    </div>
  );
}

function UsageLabel({ children, color }: { children: React.ReactNode; color: string }) {
  return (
    <div style={{ fontSize: 10, fontWeight: 500, color, marginBottom: 4, display: 'flex', alignItems: 'center', gap: 4 }}>
      <span style={{ width: 6, height: 6, borderRadius: '50%', background: color, display: 'inline-block' }} />
      {children}
    </div>
  );
}

function StmtChip({ children, color }: { children: React.ReactNode; color: string }) {
  return (
    <span style={{
      display: 'inline-block', padding: '2px 7px', borderRadius: 3,
      fontSize: 10, fontFamily: 'var(--mono)',
      background: `color-mix(in srgb, ${color} 8%, transparent)`,
      border: `1px solid color-mix(in srgb, ${color} 20%, transparent)`,
      color: 'var(--t2)',
      overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
    }}>
      {children}
    </span>
  );
}

function MiniTh({ children, center }: { children: React.ReactNode; center?: boolean }) {
  return (
    <th style={{
      padding: '4px 6px', fontSize: 9, fontWeight: 500,
      letterSpacing: '0.06em', textTransform: 'uppercase',
      color: 'var(--t3)', textAlign: center ? 'center' : 'left',
      background: 'var(--bg2)', borderBottom: '1px solid var(--bd)',
      whiteSpace: 'nowrap',
    }}>
      {children}
    </th>
  );
}

function MiniTd({ children, center, mono, bold, muted }: {
  children: React.ReactNode; center?: boolean; mono?: boolean; bold?: boolean; muted?: boolean;
}) {
  return (
    <td style={{
      padding: '3px 6px', borderBottom: '1px solid var(--bd)',
      textAlign: center ? 'center' : 'left',
      fontFamily: mono ? 'var(--mono)' : 'inherit',
      fontWeight: bold ? 500 : 400,
      color: muted ? 'var(--t3)' : 'var(--t2)',
      fontSize: 11,
    }}>
      {children}
    </td>
  );
}
