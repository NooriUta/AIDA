import { memo, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { KnotSession, KnotAtom } from '../../services/lineage';
import { ToolbarFilterBar, ToolbarInput, ToolbarCount, PageButton } from '../ui/ToolbarPrimitives';

interface Props {
  session: KnotSession;
  atoms: KnotAtom[];
}

type StatusFilter = 'all' | 'resolved' | 'failed' | 'constant' | 'function_call';
type FlagFilter   = 'all' | 'complex' | 'param' | 'var' | 'unattached';

const PAGE_SIZE = 100;

// Strip ~line:pos suffix from atom_text for display
function atomDisplayText(atomText: string): string {
  const idx = atomText.lastIndexOf('~');
  return idx > 0 ? atomText.substring(0, idx) : atomText;
}

// Two mutually exclusive source types:
//   TABLE source: refTblEdge (table name)  + refColEdge  (column name)
//   STMT  source: refStmtGeoid (stmt geoid) + refSourceName (output_col_name)
function atomSourceType(a: KnotAtom): 'TABLE' | 'STMT' | '—' {
  if (a.refTblEdge || a.refColEdge)       return 'TABLE';
  if (a.refStmtGeoid || a.refSourceName)  return 'STMT';
  return '—';
}

function atomSourceColor(type: 'TABLE' | 'STMT' | '—'): string {
  if (type === 'TABLE') return 'var(--inf)';
  if (type === 'STMT')  return 'var(--wrn)';
  return 'var(--t3)';
}

export const KnotAtoms = memo(({ session: s, atoms }: Props) => {
  const { t } = useTranslation();

  const [statusFilter, setStatusFilter] = useState<StatusFilter>('all');
  const [flagFilter, setFlagFilter]     = useState<FlagFilter>('all');
  const [searchText, setSearchText]     = useState('');
  const [page, setPage]                 = useState(0);
  const [showTable, setShowTable]       = useState(false);

  // ── Computed from atoms array ──────────────────────────────────────────────
  const columnRefCount = useMemo(
    () => atoms.filter(a => a.columnReference).length,
    [atoms],
  );

  const unattachedAtoms = useMemo(
    () => atoms.filter(a => !a.tableGeoid || a.tableGeoid === ''),
    [atoms],
  );

  const failedAtoms = useMemo(() => {
    const goodStatuses = new Set(['обработано', 'constant', 'resolved', '']);
    return atoms.filter(a => !goodStatuses.has((a.status || '').toLowerCase()));
  }, [atoms]);

  const byContext = useMemo(() => {
    const map = new Map<string, number>();
    for (const a of atoms) {
      const ctx = a.atomContext || '—';
      map.set(ctx, (map.get(ctx) || 0) + 1);
    }
    return Array.from(map.entries()).sort((a, b) => b[1] - a[1]);
  }, [atoms]);

  const resolvedPct = s.atomTotal > 0
    ? ((s.atomResolved / s.atomTotal) * 100).toFixed(1) + '%'
    : '—';

  const maxBar = Math.max(s.atomResolved, s.atomFailed, s.atomConstant, s.atomFuncCall, 1);

  // ── Filtered atoms for the full table ────────────────────────────────────
  const filteredAtoms = useMemo(() => {
    let result = atoms;

    // Status filter
    if (statusFilter !== 'all') {
      result = result.filter(a => {
        const s = (a.status || '').toLowerCase();
        if (statusFilter === 'resolved')      return s === 'обработано' || s === 'resolved';
        if (statusFilter === 'failed')        return s === 'unresolved';
        if (statusFilter === 'constant')      return s === 'constant';
        if (statusFilter === 'function_call') return s === 'function_call';
        return true;
      });
    }

    // Flag filter
    if (flagFilter !== 'all') {
      result = result.filter(a => {
        if (flagFilter === 'complex')    return a.complex;
        if (flagFilter === 'param')      return a.routineParam;
        if (flagFilter === 'var')        return a.routineVar;
        if (flagFilter === 'unattached') return !a.tableGeoid || a.tableGeoid === '';
        return true;
      });
    }

    // Text search
    if (searchText.trim()) {
      const q = searchText.trim().toLowerCase();
      result = result.filter(a =>
        a.atomText?.toLowerCase().includes(q) ||
        a.columnName?.toLowerCase().includes(q) ||
        a.tableName?.toLowerCase().includes(q)
      );
    }

    return result;
  }, [atoms, statusFilter, flagFilter, searchText]);

  const totalPages = Math.ceil(filteredAtoms.length / PAGE_SIZE);
  const pageAtoms  = filteredAtoms.slice(page * PAGE_SIZE, (page + 1) * PAGE_SIZE);

  // Reset to page 0 when filters change
  useMemo(() => { setPage(0); }, [statusFilter, flagFilter, searchText]);

  return (
    <div style={{ padding: 24, overflowY: 'auto', height: '100%', boxSizing: 'border-box' }}>

      {/* ── Row 1: session-level counts ─────────────────────────────────────── */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12, marginBottom: 12 }}>
        <Metric value={s.atomTotal}    label={t('knot.stmt.total')} />
        <Metric value={s.atomResolved} label={t('knot.atoms.resolved')} color="var(--suc)" />
        <Metric value={s.atomFailed}   label={t('knot.atoms.failed')}   color="var(--dan, #C06060)" />
        <Metric value={resolvedPct}    label="% resolved"               color="var(--acc)" />
      </div>

      {/* ── Row 2: atom type breakdown ──────────────────────────────────────── */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: 12, marginBottom: 16 }}>
        <Metric value={columnRefCount}         label={t('knot.atoms.columnRef')} color="var(--inf)" />
        <Metric value={s.atomFuncCall}         label={t('knot.atoms.funcCall')} />
        <Metric value={s.atomConstant}         label={t('knot.atoms.constant')} />
        <Metric value={unattachedAtoms.length} label={t('knot.atoms.unattached')} color="var(--wrn)" />
      </div>

      {/* ── Charts ──────────────────────────────────────────────────────────── */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12, marginBottom: 16 }}>

        <StatSection title={t('knot.atoms.title')}>
          <BarRow label={t('knot.atoms.resolved')}   value={s.atomResolved}         max={maxBar} color="var(--suc)" />
          <BarRow label={t('knot.atoms.columnRef')}  value={columnRefCount}          max={maxBar} color="var(--inf)" />
          <BarRow label={t('knot.atoms.funcCall')}   value={s.atomFuncCall}          max={maxBar} color="var(--t2)" />
          <BarRow label={t('knot.atoms.constant')}   value={s.atomConstant}          max={maxBar} color="var(--t3)" />
          <BarRow label={t('knot.atoms.unattached')} value={unattachedAtoms.length}  max={maxBar} color="var(--wrn)" />
          <BarRow label={t('knot.atoms.failed')}     value={s.atomFailed}            max={maxBar} color="var(--dan, #C06060)" />
        </StatSection>

        <StatSection title="Data flow edges">
          <StatRow label={t('knot.edges.readsFrom')}     value={s.edgeReadsFrom} />
          <StatRow label={t('knot.edges.writesTo')}      value={s.edgeWritesTo} />
          <StatRow label={t('knot.edges.atomRefColumn')} value={s.edgeAtomRefColumn} />
          <StatRow label={t('knot.edges.dataFlow')}      value={s.edgeDataFlow} />
        </StatSection>
      </div>

      {/* ── By context ──────────────────────────────────────────────────────── */}
      {byContext.length > 0 && (
        <CollapsibleCard title={t('knot.atoms.byContext')} defaultOpen>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
            {byContext.map(([ctx, count]) => (
              <div key={ctx} style={{
                display: 'flex', alignItems: 'center', gap: 6,
                padding: '3px 10px', borderRadius: 4,
                background: 'var(--bg3)', border: '1px solid var(--bd)',
              }}>
                <span style={{ fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--t2)' }}>{ctx}</span>
                <span style={{
                  fontSize: 10, fontWeight: 600, color: 'var(--acc)',
                  fontFamily: 'var(--mono)',
                }}>{count}</span>
              </div>
            ))}
          </div>
        </CollapsibleCard>
      )}

      {/* ── Unattached atoms ────────────────────────────────────────────────── */}
      {unattachedAtoms.length > 0 ? (
        <CollapsibleCard
          title={`${t('knot.atoms.unattached')} (${unattachedAtoms.length})`}
          accentColor="var(--wrn)"
        >
          <AtomMiniTable atoms={unattachedAtoms.slice(0, 30)} t={t} />
          {unattachedAtoms.length > 30 && (
            <div style={{ padding: '4px 0', fontSize: 10, color: 'var(--t3)' }}>
              +{unattachedAtoms.length - 30} {t('knot.stmt.moreAtoms')}
            </div>
          )}
        </CollapsibleCard>
      ) : (
        <InfoCard title={t('knot.atoms.unattached')} ok>
          {t('knot.atoms.noUnattached')}
        </InfoCard>
      )}

      {/* ── Failed atoms ────────────────────────────────────────────────────── */}
      {failedAtoms.length > 0 ? (
        <CollapsibleCard
          title={`${t('knot.atoms.failed')} (${failedAtoms.length})`}
          accentColor="var(--dan, #C06060)"
        >
          <AtomMiniTable atoms={failedAtoms.slice(0, 30)} t={t} />
          {failedAtoms.length > 30 && (
            <div style={{ padding: '4px 0', fontSize: 10, color: 'var(--t3)' }}>
              +{failedAtoms.length - 30} {t('knot.stmt.moreAtoms')}
            </div>
          )}
        </CollapsibleCard>
      ) : (
        <InfoCard title={t('knot.atoms.failed')} ok>
          {t('knot.atoms.noFailed')}
        </InfoCard>
      )}

      {/* ── Full filterable atom table ───────────────────────────────────────── */}
      <div style={{
        background: 'var(--bg2)', border: '1px solid var(--bd)',
        borderRadius: 8, marginBottom: 12, overflow: 'hidden',
      }}>
        {/* Header + toggle */}
        <div
          onClick={() => setShowTable(o => !o)}
          style={{
            padding: '10px 16px', display: 'flex', alignItems: 'center', gap: 8,
            cursor: 'pointer', userSelect: 'none',
            borderBottom: showTable ? '1px solid var(--bd)' : 'none',
          }}
          onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
          onMouseLeave={e => { (e.currentTarget as HTMLElement).style.background = ''; }}
        >
          <span style={{
            fontSize: 8, color: 'var(--acc)',
            display: 'inline-block',
            transform: showTable ? 'rotate(90deg)' : 'none',
            transition: 'transform 0.15s',
          }}>▶</span>
          <span style={{ fontSize: 12, fontWeight: 500, color: 'var(--t1)' }}>
            {t('knot.atoms.allAtoms')} ({atoms.length.toLocaleString()})
          </span>
        </div>

        {showTable && (
          <div style={{ padding: 16 }}>

            {/* Filter toolbar */}
            <div style={{ display: 'flex', gap: 8, marginBottom: 12, flexWrap: 'wrap', alignItems: 'center' }}>
              <ToolbarInput
                value={searchText}
                onChange={setSearchText}
                placeholder={t('knot.atoms.searchPlaceholder')}
              />
              <ToolbarFilterBar
                options={[
                  { value: 'all',           label: 'All' },
                  { value: 'resolved',      label: 'Resolved' },
                  { value: 'failed',        label: 'Failed',   color: 'var(--dan, #C06060)' },
                  { value: 'constant',      label: 'Const' },
                  { value: 'function_call', label: 'Func' },
                ] as { value: StatusFilter; label: string; color?: string }[]}
                value={statusFilter}
                onChange={setStatusFilter}
              />
              <ToolbarFilterBar
                options={[
                  { value: 'all',        label: 'All' },
                  { value: 'complex',    label: '∑ Complex' },
                  { value: 'param',      label: 'P Param',   color: 'var(--inf)' },
                  { value: 'var',        label: 'V Var',     color: 'var(--wrn)' },
                  { value: 'unattached', label: 'Unattached',color: 'var(--wrn)' },
                ] as { value: FlagFilter; label: string; color?: string }[]}
                value={flagFilter}
                onChange={setFlagFilter}
              />
              <ToolbarCount filtered={filteredAtoms.length} total={atoms.length} />
            </div>

            {/* Table */}
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 11 }}>
                <thead>
                  <tr>
                    {[
                      '#',
                      t('knot.stmt.atomText'),
                      t('knot.stmt.sourceType'),
                      t('knot.stmt.sourceRef'),
                      t('knot.stmt.refCol'),
                      'Pos',
                      t('knot.stmt.status'),
                      t('knot.stmt.context'),
                      t('knot.stmt.outputCol'),
                      '∑ Flags',
                    ].map((h, i) => (
                      <th key={i} style={{
                        padding: '4px 8px', fontSize: 9, fontWeight: 500,
                        letterSpacing: '0.06em', textTransform: 'uppercase',
                        color: 'var(--t3)', textAlign: i === 0 ? 'right' : 'left',
                        background: 'var(--bg3)', borderBottom: '1px solid var(--bd)',
                        whiteSpace: 'nowrap',
                      }}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {pageAtoms.map((a, i) => {
                    const idx = page * PAGE_SIZE + i + 1;
                    const dispText = atomDisplayText(a.atomText || '');
                    const statusLow = (a.status || '').toLowerCase();
                    const statusColor =
                      statusLow === 'обработано' || statusLow === 'resolved' ? 'var(--suc)' :
                      statusLow === 'unresolved'  ? 'var(--dan, #C06060)' :
                      statusLow === 'constant'    ? 'var(--t3)' :
                      statusLow === 'function_call' ? 'var(--t2)' : 'var(--t3)';

                    return (
                      <tr key={i} style={{ transition: 'background 0.08s' }}
                        onMouseEnter={e => (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'}
                        onMouseLeave={e => (e.currentTarget as HTMLElement).style.background = ''}>
                        <td style={{
                          padding: '3px 8px', borderBottom: '1px solid var(--bd)',
                          fontSize: 9, color: 'var(--t3)', textAlign: 'right', flexShrink: 0,
                        }}>{idx}</td>
                        <td style={{
                          padding: '3px 8px', borderBottom: '1px solid var(--bd)',
                          fontFamily: 'var(--mono)', fontSize: 10,
                          maxWidth: 240, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                        }} title={a.atomText}>{dispText || '—'}</td>
                        {/* Тип источника: TABLE или STMT */}
                        {(() => {
                          const st = atomSourceType(a);
                          return (
                            <td style={{
                              padding: '3px 8px', borderBottom: '1px solid var(--bd)',
                              fontSize: 9, color: atomSourceColor(st), whiteSpace: 'nowrap',
                              fontFamily: 'var(--mono)',
                            }}>{st}</td>
                          );
                        })()}
                        {/* Источник: таблица (refTblEdge) ИЛИ запрос (refStmtGeoid) */}
                        {(() => {
                          const val  = a.refTblEdge || a.refStmtGeoid || '—';
                          const col  = a.refTblEdge ? 'var(--acc)' : a.refStmtGeoid ? 'var(--wrn)' : 'var(--t3)';
                          return (
                            <td style={{
                              padding: '3px 8px', borderBottom: '1px solid var(--bd)',
                              fontFamily: 'var(--mono)', fontSize: 10, color: col,
                              maxWidth: 160, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                            }} title={val !== '—' ? val : undefined}>{val}</td>
                          );
                        })()}
                        {/* Колонка: refColEdge (TABLE) ИЛИ refSourceName/output_col_name (STMT) */}
                        {(() => {
                          const val = a.refColEdge || a.refSourceName || '—';
                          return (
                            <td style={{
                              padding: '3px 8px', borderBottom: '1px solid var(--bd)',
                              fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--inf)',
                              maxWidth: 140, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                            }} title={val !== '—' ? val : undefined}>{val}</td>
                          );
                        })()}
                        <td style={{
                          padding: '3px 8px', borderBottom: '1px solid var(--bd)',
                          fontFamily: 'var(--mono)', fontSize: 9, color: 'var(--t3)',
                          whiteSpace: 'nowrap',
                        }}>
                          {a.atomLine > 0 ? `${a.atomLine}:${a.atomPos}` : '—'}
                        </td>
                        <td style={{ padding: '3px 8px', borderBottom: '1px solid var(--bd)' }}>
                          <span style={{
                            padding: '1px 5px', borderRadius: 3, fontSize: 9,
                            background: 'var(--bg3)', border: '1px solid var(--bd)',
                            color: statusColor, whiteSpace: 'nowrap',
                          }}>{a.status || '—'}</span>
                        </td>
                        <td style={{
                          padding: '3px 8px', borderBottom: '1px solid var(--bd)',
                          fontSize: 10, color: 'var(--t3)', whiteSpace: 'nowrap',
                        }}>{a.atomContext || '—'}</td>
                        {/* Вых. колонка */}
                        <td style={{
                          padding: '3px 8px', borderBottom: '1px solid var(--bd)',
                          fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t2)',
                          whiteSpace: 'nowrap',
                        }}>{a.outputColName || '—'}</td>
                        <td style={{ padding: '3px 8px', borderBottom: '1px solid var(--bd)' }}>
                          <div style={{ display: 'flex', gap: 2 }}>
                            {a.complex     && <FlagBadge label="∑" color="var(--wrn)"  title="Complex" />}
                            {a.routineParam && <FlagBadge label="P" color="var(--inf)"  title="Routine param" />}
                            {a.routineVar   && <FlagBadge label="V" color="var(--acc)"  title="Routine var" />}
                            {a.nestedAtomsCount != null && a.nestedAtomsCount > 0 && (
                              <FlagBadge label={`N${a.nestedAtomsCount}`} color="var(--t2)" title="Nested atoms" />
                            )}
                          </div>
                        </td>
                      </tr>
                    );
                  })}
                  {pageAtoms.length === 0 && (
                    <tr>
                      <td colSpan={10} style={{
                        padding: '16px', textAlign: 'center',
                        fontSize: 11, color: 'var(--t3)',
                      }}>No atoms match the current filter</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginTop: 10, justifyContent: 'center' }}>
                <PageButton disabled={page === 0} onClick={() => setPage(0)}>«</PageButton>
                <PageButton disabled={page === 0} onClick={() => setPage(p => p - 1)}>‹</PageButton>
                <span style={{ fontSize: 11, color: 'var(--t2)' }}>{page + 1} / {totalPages}</span>
                <PageButton disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>›</PageButton>
                <PageButton disabled={page >= totalPages - 1} onClick={() => setPage(totalPages - 1)}>»</PageButton>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
});

KnotAtoms.displayName = 'KnotAtoms';

// ── Sub-components ────────────────────────────────────────────────────────────


function FlagBadge({ label, color, title }: { label: string; color: string; title: string }) {
  return (
    <span title={title} style={{
      display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
      padding: '0 3px', borderRadius: 2, fontSize: 8, fontWeight: 700,
      fontFamily: 'var(--mono)',
      background: `color-mix(in srgb, ${color} 15%, transparent)`,
      color, minWidth: 14, height: 14,
    }}>
      {label}
    </span>
  );
}

function CollapsibleCard({
  title, children, defaultOpen = false, accentColor,
}: {
  title: string; children: React.ReactNode; defaultOpen?: boolean; accentColor?: string;
}) {
  const [open, setOpen] = useState(defaultOpen);
  const color = accentColor || 'var(--acc)';
  return (
    <div style={{
      background: 'var(--bg2)', border: '1px solid var(--bd)',
      borderRadius: 8, marginBottom: 12, overflow: 'hidden',
    }}>
      <div
        onClick={() => setOpen(o => !o)}
        style={{
          padding: '10px 16px',
          borderBottom: open ? '1px solid var(--bd)' : 'none',
          display: 'flex', alignItems: 'center', gap: 8,
          cursor: 'pointer', userSelect: 'none',
          transition: 'background 0.1s',
        }}
        onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = 'var(--bg3)'; }}
        onMouseLeave={e => { (e.currentTarget as HTMLElement).style.background = ''; }}
      >
        <span style={{
          fontSize: 8, display: 'inline-block', color,
          transform: open ? 'rotate(90deg)' : 'none',
          transition: 'transform 0.15s',
        }}>▶</span>
        <span style={{ fontSize: 12, fontWeight: 500, color: 'var(--t1)' }}>{title}</span>
      </div>
      {open && <div style={{ padding: 16 }}>{children}</div>}
    </div>
  );
}

function InfoCard({ title, children, ok }: { title: string; children: React.ReactNode; ok?: boolean }) {
  return (
    <div style={{
      background: 'var(--bg2)', border: '1px solid var(--bd)',
      borderRadius: 8, marginBottom: 12,
      display: 'flex', alignItems: 'center', gap: 10,
      padding: '10px 16px',
    }}>
      <span style={{ color: ok ? 'var(--suc)' : 'var(--t3)', fontSize: 12 }}>{ok ? '✓' : '–'}</span>
      <span style={{ fontSize: 11, fontWeight: 500, color: 'var(--t2)' }}>{title}:</span>
      <span style={{ fontSize: 11, color: 'var(--t3)' }}>{children}</span>
    </div>
  );
}

function AtomMiniTable({
  atoms, t,
}: {
  atoms: KnotAtom[];
  t: (k: string) => string;
}) {
  return (
    <div style={{ overflowX: 'auto' }}>
      <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: 11 }}>
        <thead>
          <tr>
            {[
              t('knot.stmt.atomText'),
              t('knot.column.name'),
              t('knot.stmt.tableGeoid'),
              t('knot.stmt.status'),
              t('knot.stmt.context'),
            ].map((h, i) => (
              <th key={i} style={{
                padding: '4px 8px', fontSize: 9, fontWeight: 500,
                letterSpacing: '0.06em', textTransform: 'uppercase',
                color: 'var(--t3)', textAlign: 'left',
                background: 'var(--bg3)', borderBottom: '1px solid var(--bd)',
                whiteSpace: 'nowrap',
              }}>{h}</th>
            ))}
          </tr>
        </thead>
        <tbody>
          {atoms.map((a, i) => (
            <tr key={i}>
              <td style={{
                padding: '4px 8px', borderBottom: '1px solid var(--bd)',
                fontFamily: 'var(--mono)', fontSize: 10,
                maxWidth: 220, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
              }}>{a.atomText}</td>
              <td style={{
                padding: '4px 8px', borderBottom: '1px solid var(--bd)',
                fontFamily: 'var(--mono)', fontSize: 10,
              }}>{a.columnName || '—'}</td>
              <td style={{
                padding: '4px 8px', borderBottom: '1px solid var(--bd)',
                fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t3)',
              }}>{a.tableName || a.tableGeoid || '—'}</td>
              <td style={{ padding: '4px 8px', borderBottom: '1px solid var(--bd)', fontSize: 10 }}>
                <span style={{
                  padding: '1px 5px', borderRadius: 3, fontSize: 9,
                  background: 'var(--bg3)', border: '1px solid var(--bd)',
                  color: 'var(--wrn)',
                }}>{a.status || '—'}</span>
              </td>
              <td style={{
                padding: '4px 8px', borderBottom: '1px solid var(--bd)',
                fontSize: 10, color: 'var(--t3)',
              }}>{a.atomContext || '—'}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

function Metric({ value, label, color }: { value: number | string; label: string; color?: string }) {
  return (
    <div style={{
      background: 'var(--bg2)', border: '1px solid var(--bd)',
      borderRadius: 8, padding: '12px 16px',
      display: 'flex', flexDirection: 'column', gap: 4,
    }}>
      <div style={{ fontSize: 22, fontWeight: 600, lineHeight: 1, color: color || 'var(--t1)' }}>
        {typeof value === 'number' ? value.toLocaleString() : value}
      </div>
      <div style={{ fontSize: 10, color: 'var(--t3)' }}>{label}</div>
    </div>
  );
}

function StatSection({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div style={{
      background: 'var(--bg2)', border: '1px solid var(--bd)',
      borderRadius: 8, padding: 16,
    }}>
      <div style={{
        fontSize: 10, fontWeight: 500, letterSpacing: '0.08em',
        textTransform: 'uppercase', color: 'var(--t3)',
        marginBottom: 12, paddingBottom: 8,
        borderBottom: '1px solid var(--bd)',
      }}>
        {title}
      </div>
      {children}
    </div>
  );
}

function BarRow({ label, value, max, color }: {
  label: string; value: number; max: number; color: string;
}) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 8, padding: '3px 0' }}>
      <div style={{ width: 90, fontSize: 10, color: 'var(--t2)', flexShrink: 0 }}>{label}</div>
      <div style={{ flex: 1, height: 4, background: 'var(--bg3)', borderRadius: 2, overflow: 'hidden' }}>
        <div style={{
          height: '100%', borderRadius: 2, background: color, opacity: 0.75,
          width: `${(value / max) * 100}%`,
        }} />
      </div>
      <div style={{
        width: 50, textAlign: 'right', fontSize: 10,
        fontFamily: 'var(--mono)', color: 'var(--t2)', flexShrink: 0,
      }}>
        {value.toLocaleString()}
      </div>
    </div>
  );
}

function StatRow({ label, value }: { label: string; value: number }) {
  return (
    <div style={{
      display: 'flex', justifyContent: 'space-between', alignItems: 'center',
      padding: '5px 0', borderBottom: '1px solid var(--bd)', fontSize: 12,
    }}>
      <span style={{ color: 'var(--t2)' }}>{label}</span>
      <span style={{ fontWeight: 500, color: 'var(--t1)', fontFamily: 'var(--mono)', fontSize: 11 }}>
        {value.toLocaleString()}
      </span>
    </div>
  );
}
