import { memo, useState, useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import type { KnotTable, KnotStatement, KnotTableDetail, KnotColumn } from '../../services/lineage';
import { fetchKnotTableDetail } from '../../services/lineage';
import { useIsMobile } from '../../hooks/useIsMobile';

interface Props {
  sessionId: string;
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

export const KnotStructure = memo(({ sessionId, tables, statements }: Props) => {
  const { t } = useTranslation();
  const isMobile = useIsMobile();
  const [filter, setFilter] = useState('');
  const [expanded, setExpanded] = useState<Set<string>>(new Set());
  // Lazy detail: geoid → KnotTableDetail | 'loading' | 'error'
  const [details, setDetails] = useState<Map<string, KnotTableDetail | 'loading' | 'error'>>(new Map());

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
        st.sourceTables?.forEach(ref => {
          const key = ref.geoid;
          if (!srcMap.has(key)) srcMap.set(key, []);
          srcMap.get(key)!.push(label);
        });
        st.targetTables?.forEach(ref => {
          const key = ref.geoid;
          if (!tgtMap.has(key)) tgtMap.set(key, []);
          tgtMap.get(key)!.push(label);
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

  const loadDetail = useCallback((geoid: string) => {
    setDetails(prev => {
      if (prev.has(geoid)) return prev;
      const next = new Map(prev);
      next.set(geoid, 'loading');
      return next;
    });
    fetchKnotTableDetail(sessionId, geoid)
      .then(detail => {
        setDetails(prev => {
          const next = new Map(prev);
          next.set(geoid, detail);
          return next;
        });
      })
      .catch(() => {
        setDetails(prev => {
          const next = new Map(prev);
          next.set(geoid, 'error');
          return next;
        });
      });
  }, [sessionId]);

  const toggle = useCallback((tb: KnotTable) => {
    const isOpen = expanded.has(tb.id);
    setExpanded(prev => {
      const next = new Set(prev);
      next.has(tb.id) ? next.delete(tb.id) : next.add(tb.id);
      return next;
    });
    // Trigger lazy load on first open (outside the updater — no side-effects in updater)
    if (!isOpen && !details.has(tb.geoid)) {
      loadDetail(tb.geoid);
    }
  }, [expanded, details, loadDetail]);

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
              const srcStmts = tableUsage.srcMap.get(tb.geoid) || [];
              const tgtStmts = tableUsage.tgtMap.get(tb.geoid) || [];
              const isMaster = tb.dataSource === 'master';
              const detail   = details.get(tb.geoid);
              return [
                <tr
                  key={tb.id}
                  onClick={() => toggle(tb)}
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
                  <td style={{ padding: '8px 12px', borderBottom: '1px solid var(--bd)', fontWeight: 500 }}>
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 5 }}>
                      <span>{tb.name}</span>
                      {/* master / reconstructed badge — right-aligned */}
                      {tb.dataSource && (
                        <span style={{
                          fontSize: 7, padding: '2px 5px', borderRadius: 2, flexShrink: 0,
                          fontWeight: 600, fontFamily: 'var(--mono)',
                          background: isMaster
                            ? 'color-mix(in srgb, var(--suc) 15%, transparent)'
                            : 'color-mix(in srgb, var(--wrn) 15%, transparent)',
                          border: `0.5px solid ${isMaster ? 'var(--suc)' : 'var(--wrn)'}`,
                          color: isMaster ? 'var(--suc)' : 'var(--wrn)',
                        }}>
                          {tb.dataSource}
                        </span>
                      )}
                    </div>
                  </td>
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
                      <div style={{ padding: '14px 16px', display: 'grid', gridTemplateColumns: isMobile ? '1fr' : '1fr 1fr', gap: isMobile ? 12 : 20 }}>

                        {/* Left: Columns table */}
                        <div>
                          {detail === 'loading' && (
                            <div style={{ fontSize: 11, color: 'var(--t3)', padding: '8px 0' }}>
                              Loading columns…
                            </div>
                          )}
                          {detail === 'error' && (
                            <div style={{ fontSize: 11, color: 'var(--err)', padding: '8px 0' }}>
                              Failed to load columns.
                            </div>
                          )}
                          {detail && detail !== 'loading' && detail !== 'error' && (
                            <ColumnDetail
                              detail={detail as KnotTableDetail}
                              t={t}
                              aliases={tb.aliases}
                            />
                          )}
                        </div>

                        {/* Right: Usage + snippet */}
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
                              <div style={{ display: 'flex', flexDirection: 'column', gap: 2, marginBottom: 10 }}>
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
                            <div style={{ fontSize: 11, color: 'var(--t3)', marginBottom: 10 }}>
                              No direct statement references
                            </div>
                          )}

                          {/* SQL snippet */}
                          {detail && detail !== 'loading' && detail !== 'error' && (detail as KnotTableDetail).snippet && (
                            <>
                              <SectionLabel>SQL</SectionLabel>
                              <pre style={{
                                margin: 0, fontSize: 10, fontFamily: 'var(--mono)',
                                color: 'var(--t2)', background: 'var(--bg2)',
                                border: '1px solid var(--bd)', borderRadius: 4,
                                padding: '8px 10px', overflowX: 'auto',
                                whiteSpace: 'pre-wrap', wordBreak: 'break-all',
                                maxHeight: 200, overflowY: 'auto',
                              }}>
                                {(detail as KnotTableDetail).snippet}
                              </pre>
                            </>
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

// ── ColumnDetail sub-component ────────────────────────────────────────────────

function ColumnDetail({
  detail,
  t,
  aliases,
}: {
  detail: KnotTableDetail;
  t: (key: string, opts?: Record<string, unknown>) => string;
  aliases: string[];
}) {
  const cols = detail.columns;
  return (
    <>
      <SectionLabel>
        {t('knot.tabs.structure')} — {t('knot.session.columns')} ({cols.length})
      </SectionLabel>

      {/* Table-level aliases */}
      {aliases?.length > 0 && (
        <div style={{ display: 'flex', alignItems: 'center', gap: 4, marginBottom: 6, flexWrap: 'wrap' }}>
          <span style={{ fontSize: 9, color: 'var(--t3)', textTransform: 'uppercase', letterSpacing: '0.06em' }}>
            {t('knot.table.aliases')}:
          </span>
          {aliases.map(a => (
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

      {cols.length > 0 ? (
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 11 }}>
            <thead>
              <tr>
                <MiniTh center>#</MiniTh>
                <MiniTh center>PK</MiniTh>
                <MiniTh center>FK</MiniTh>
                <MiniTh>{t('knot.column.name')}</MiniTh>
                <MiniTh>{t('knot.column.dataType')}</MiniTh>
                <MiniTh center>N</MiniTh>
                <MiniTh>Default</MiniTh>
                <MiniTh>{t('knot.column.alias')}</MiniTh>
              </tr>
            </thead>
            <tbody>
              {cols.slice(0, 50).map(col => (
                <ColumnRow key={col.id} col={col} />
              ))}
            </tbody>
          </table>
          {cols.length > 50 && (
            <div style={{ padding: '4px 6px', fontSize: 10, color: 'var(--t3)' }}>
              +{cols.length - 50} more
            </div>
          )}
        </div>
      ) : (
        <div style={{ fontSize: 11, color: 'var(--t3)' }}>—</div>
      )}
    </>
  );
}

function ColumnRow({ col }: { col: KnotColumn }) {
  return (
    <tr>
      <MiniTd center muted>{col.position || '—'}</MiniTd>
      <MiniTd center>
        {col.isPk && (
          <span style={{
            fontSize: 8, padding: '1px 4px', borderRadius: 2, fontWeight: 700,
            fontFamily: 'var(--mono)', letterSpacing: '0.03em',
            background: 'color-mix(in srgb, var(--wrn) 15%, transparent)',
            border: '0.5px solid var(--wrn)', color: 'var(--wrn)',
          }}>PK</span>
        )}
      </MiniTd>
      <MiniTd center>
        {col.isFk && (
          <span
            title={col.fkRefTable ? `→ ${col.fkRefTable}` : undefined}
            style={{
              fontSize: 8, padding: '1px 4px', borderRadius: 2, fontWeight: 700,
              fontFamily: 'var(--mono)', letterSpacing: '0.03em',
              background: 'color-mix(in srgb, var(--inf) 15%, transparent)',
              border: '0.5px solid var(--inf)', color: 'var(--inf)',
              cursor: col.fkRefTable ? 'help' : 'default',
            }}>FK</span>
        )}
      </MiniTd>
      <MiniTd mono bold>{col.name}</MiniTd>
      <MiniTd mono muted>{col.dataType || '—'}</MiniTd>
      <MiniTd center>
        {col.isRequired && (
          <span style={{
            fontSize: 8, padding: '1px 4px', borderRadius: 2, fontWeight: 700,
            fontFamily: 'var(--mono)',
            background: 'color-mix(in srgb, var(--err) 12%, transparent)',
            border: '0.5px solid var(--err)', color: 'var(--err)',
          }}>N</span>
        )}
      </MiniTd>
      <MiniTd mono muted>{col.defaultValue || '—'}</MiniTd>
      <MiniTd mono muted>{col.alias || '—'}</MiniTd>
    </tr>
  );
}

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
