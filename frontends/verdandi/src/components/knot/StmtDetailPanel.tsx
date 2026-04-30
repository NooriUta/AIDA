import { useState, type ReactNode } from 'react';
import { useTranslation } from 'react-i18next';
import type { KnotStatement } from '../../services/lineage';
import { useKnotSnippet } from '../../services/hooks';
import type { LookupMaps, FlatRow } from './knotStmtHelpers';
import {
  typeFromGeoid, lineFromGeoid, shortName, truncGeoid,
} from './knotStmtHelpers';
import { StmtTableRow } from './KnotStmtTableRow';
import {
  pTblTdStyle,
  atomColor, atomStatusBg, atomStatusColor, atomDisplayText,
  SectionHeader, CollapsibleSectionHeader, InfoRow, PTblTh,
  AtomFlag, AtomsBreakdown, SqlBlock,
} from './KnotStmtPrimitives';

// ── Flat subqueries (all descendants in one table) ──────────────────────────

function FlatSubqueries({ rows, expanded, toggle, maps }: {
  rows: FlatRow[]; expanded: Set<string>; toggle: (id: string) => void;
  maps: LookupMaps;
}) {
  const { t } = useTranslation();

  return (
    <div style={{
      borderLeft: '3px solid var(--bdh)',
      margin: '0 14px 12px 8px',
      background: 'var(--bg2)',
      borderRadius: '0 5px 5px 0',
    }}>
      <div style={{
        padding: '8px 12px', borderBottom: '1px solid var(--bd)',
        fontSize: 10, fontWeight: 500, letterSpacing: '0.06em',
        textTransform: 'uppercase', color: 'var(--t3)',
      }}>
        {t('knot.stmt.subqueries')} ({rows.length})
      </div>
      <table style={{ width: '100%', borderCollapse: 'collapse' }}>
        <thead>
          <tr>
            {[
              { key: '', w: 22 },
              { key: 'knot.stmt.name' },
              { key: 'knot.stmt.shortName' },
              { key: 'knot.stmt.aliases' },
              { key: 'knot.stmt.type' },
              { key: 'knot.stmt.level', center: true },
              { key: 'knot.stmt.line', center: true },
              { key: 'knot.stmt.sources', center: true },
              { key: 'knot.stmt.targets', center: true },
              { key: 'knot.stmt.subqueriesCount', center: true },
              { key: 'knot.stmt.atoms', center: true },
            ].map((h, i) => (
              <th key={i} style={{
                padding: '5px 8px',
                textAlign: h.center ? 'center' : 'left',
                fontSize: 10, fontWeight: 500, letterSpacing: '0.06em',
                textTransform: 'uppercase', color: 'var(--t3)',
                background: 'var(--bg0)', borderBottom: '1px solid var(--bd)',
                whiteSpace: 'nowrap',
                ...(h.w ? { width: h.w } : {}),
              }}>
                {h.key ? t(h.key) : ''}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map(({ stmt, depth }) => (
            <SubStmtRow key={stmt.id} stmt={stmt} depth={depth} expanded={expanded} toggle={toggle} maps={maps} />
          ))}
        </tbody>
      </table>
    </div>
  );
}

// ── Sub-statement row (in flat list) ────────────────────────────────────────

function SubStmtRow({ stmt, depth, expanded, toggle, maps }: {
  stmt: KnotStatement; depth: number;
  expanded: Set<string>; toggle: (id: string) => void;
  maps: LookupMaps;
}) {
  const { t } = useTranslation();
  const isOpen = expanded.has(stmt.id);

  return (
    <>
      <StmtTableRow
        stmt={stmt}
        depth={depth}
        isOpen={isOpen}
        toggle={toggle}
        levelLabel={String(depth)}
        indent
      />
      {isOpen && (
        <tr>
          <td colSpan={11} style={{
            padding: 0,
            background: 'var(--bg2)',
            borderBottom: '2px solid color-mix(in srgb, var(--acc) 40%, transparent)',
          }}>
            <StmtDetailPanel stmt={stmt} t={t} maps={maps} />
          </td>
        </tr>
      )}
    </>
  );
}

// ── Detail panel (shared by root and sub rows) ──────────────────────────────

export function StmtDetailPanel({ stmt, t, maps, flatChildren, expanded, onToggle }: {
  stmt:          KnotStatement;
  t:             (k: string, opts?: Record<string, unknown>) => string;
  maps:          LookupMaps;
  flatChildren?: FlatRow[];
  expanded?:     Set<string>;
  onToggle?:     (id: string) => void;
}) {
  const [atomsOpen,    setAtomsOpen]    = useState(false);
  const [sqlOpen,      setSqlOpen]      = useState(false);
  const [mainInfoOpen, setMainInfoOpen] = useState(false);

  const sqlFromMap  = stmt.geoid ? maps.snippetMap.get(stmt.geoid) : undefined;
  const stmtAtoms   = stmt.geoid ? maps.atomMap.get(stmt.geoid)    : undefined;
  const stmtOutCols = stmt.geoid ? maps.outColMap.get(stmt.geoid)  : undefined;

  // Lazy fetch: only fires when SQL section is open AND snippet wasn't in the pre-loaded map.
  const { data: snippetFromDb, isFetching: snippetLoading } = useKnotSnippet(
    stmt.geoid,
    sqlOpen && !sqlFromMap,
  );
  const sql = sqlFromMap ?? snippetFromDb ?? undefined;

  return (
    <div style={{
      padding: '0 14px 14px',
      borderLeft: '3px solid var(--acc)',
      margin: '0 0 0 6px',
    }}>

      {/* 1. SQL — collapsible, collapsed by default */}
      <CollapsibleSectionHeader
        open={sqlOpen}
        onToggle={() => setSqlOpen(o => !o)}
      >
        {t('knot.stmt.sqlTitle')}
      </CollapsibleSectionHeader>
      {sqlOpen && (
        snippetLoading ? (
          <div style={{ padding: '6px 0', fontSize: 11, color: 'var(--t3)' }}>…</div>
        ) : sql ? (
          <SqlBlock sql={sql} />
        ) : (
          <div style={{ padding: '6px 0', fontSize: 11, color: 'var(--t3)' }}>
            {t('knot.stmt.noSql')}
          </div>
        )
      )}

      {/* 2. Main info — collapsible, collapsed by default */}
      <CollapsibleSectionHeader
        open={mainInfoOpen}
        onToggle={() => setMainInfoOpen(o => !o)}
      >
        {t('knot.stmt.mainInfo')}
      </CollapsibleSectionHeader>
      {mainInfoOpen && (
        <table style={{ width: '100%', borderCollapse: 'collapse' }}>
          <tbody>
            <InfoRow label="Geoid" value={stmt.geoid || '—'} mono />
            <InfoRow label={t('knot.stmt.routine')} value={
              [stmt.packageName, stmt.routineName].filter(Boolean).join(':') || '—'
            } mono />
            {stmt.routineType && (
              <InfoRow label={t('knot.stmt.routineType')} value={stmt.routineType} />
            )}
            <InfoRow label={t('knot.stmt.shortName')} value={shortName(stmt)} />
            <InfoRow label={t('knot.stmt.type')} value={(typeFromGeoid(stmt.geoid) || stmt.stmtType) || '—'} />
            <InfoRow label={t('knot.stmt.line')} value={String((lineFromGeoid(stmt.geoid) ?? stmt.lineNumber) || '—')} mono />
            {stmt.stmtAliases && stmt.stmtAliases.length > 0 && (
              <InfoRow label={t('knot.stmt.aliases')} value={stmt.stmtAliases.join(', ')} mono />
            )}
            {stmt.children && stmt.children.length > 0 && (
              <InfoRow
                label={t('knot.stmt.childStmts')}
                value={stmt.children.map(c => truncGeoid(c.geoid)).join(', ')}
                mono
              />
            )}
          </tbody>
        </table>
      )}

      {/* 3. Target tables */}
      {(stmt.targetTables?.length || 0) > 0 && (
        <>
          <SectionHeader count={stmt.targetTables.length}>{t('knot.stmt.targetTables')}</SectionHeader>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <PTblTh>Тип</PTblTh>
                <PTblTh>{t('knot.table.name')}</PTblTh>
                <PTblTh>{t('knot.stmt.aliases')}</PTblTh>
                <PTblTh>Geoid</PTblTh>
              </tr>
            </thead>
            <tbody>
              {stmt.targetTables.map((ref, i) => (
                <tr key={i}>
                  <td style={{ ...pTblTdStyle, fontSize: 9, color: ref.nodeType === 'STMT' ? 'var(--wrn)' : 'var(--t3)', whiteSpace: 'nowrap' }}>
                    {ref.nodeType}
                  </td>
                  <td style={{
                    ...pTblTdStyle,
                    borderLeft: `3px solid ${ref.nodeType === 'STMT' ? 'var(--wrn)' : 'var(--suc)'}`,
                    fontFamily: 'var(--mono)', fontSize: 11,
                  }}>
                    {ref.name}
                  </td>
                  <td style={{ ...pTblTdStyle, fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--acc)' }}>
                    {ref.aliases?.length ? ref.aliases.join(', ') : '—'}
                  </td>
                  <td style={{ ...pTblTdStyle, fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t3)' }}>
                    {ref.geoid || '—'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}

      {/* 4. Affected columns — DaliAffectedColumn via HAS_AFFECTED_COL */}
      {(() => {
        const affCols = stmt.geoid ? maps.affColMap.get(stmt.geoid) : undefined;
        if (!affCols?.length) return null;
        return (
          <>
            <SectionHeader count={affCols.length}>{t('knot.stmt.targetCols')}</SectionHeader>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  <PTblTh>#</PTblTh>
                  <PTblTh>{t('knot.column.name')}</PTblTh>
                  <PTblTh>{t('knot.stmt.tableRef')}</PTblTh>
                </tr>
              </thead>
              <tbody>
                {affCols.map((col, i) => (
                  <tr key={i}>
                    <td style={{ ...pTblTdStyle, textAlign: 'center', width: 32, color: 'var(--t3)' }}>
                      {col.position || i + 1}
                    </td>
                    <td style={{
                      ...pTblTdStyle,
                      fontFamily: 'var(--mono)', fontSize: 11, fontWeight: 500,
                      borderLeft: '3px solid var(--suc)', color: 'var(--t1)',
                    }}>
                      {col.columnName || '—'}
                    </td>
                    <td style={{ ...pTblTdStyle, fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t3)' }}>
                      {col.tableName || '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </>
        );
      })()}

      {/* 5. Source sets (tables & queries) */}
      {(stmt.sourceTables?.length || 0) > 0 && (
        <>
          <SectionHeader count={stmt.sourceTables.length}>{t('knot.stmt.sourceSets')}</SectionHeader>
          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
            <thead>
              <tr>
                <PTblTh>Тип</PTblTh>
                <PTblTh>{t('knot.table.name')}</PTblTh>
                <PTblTh>{t('knot.stmt.aliases')}</PTblTh>
                <PTblTh>Geoid</PTblTh>
              </tr>
            </thead>
            <tbody>
              {stmt.sourceTables.map((ref, i) => (
                <tr key={i}>
                  <td style={{ ...pTblTdStyle, fontSize: 9, color: ref.nodeType === 'STMT' ? 'var(--wrn)' : 'var(--t3)', whiteSpace: 'nowrap' }}>
                    {ref.nodeType}
                  </td>
                  <td style={{
                    ...pTblTdStyle,
                    borderLeft: `3px solid ${ref.nodeType === 'STMT' ? 'var(--wrn)' : 'var(--inf)'}`,
                    fontFamily: 'var(--mono)', fontSize: 11,
                  }}>
                    {ref.name}
                  </td>
                  <td style={{ ...pTblTdStyle, fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--acc)' }}>
                    {ref.aliases?.length ? ref.aliases.join(', ') : '—'}
                  </td>
                  <td style={{ ...pTblTdStyle, fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t3)', maxWidth: 200, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
                      title={ref.geoid || undefined}>
                    {ref.geoid || '—'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </>
      )}

      {/* 6. Output columns (full detail with expressions and source atoms) */}
      {stmtOutCols && stmtOutCols.length > 0 && (
        <>
          <SectionHeader count={stmtOutCols.length}>{t('knot.stmt.outputCols')}</SectionHeader>
          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse' }}>
              <thead>
                <tr>
                  <PTblTh>#</PTblTh>
                  <PTblTh>{t('knot.column.name')}</PTblTh>
                  <PTblTh>{t('knot.stmt.expression')}</PTblTh>
                  <PTblTh>{t('knot.stmt.alias')}</PTblTh>
                  <PTblTh>{t('knot.stmt.sourceAtoms')}</PTblTh>
                </tr>
              </thead>
              <tbody>
                {stmtOutCols.map((col, i) => {
                  const srcAtoms = col.atoms ?? [];
                  return (
                    <tr key={i}>
                      <td style={{ ...pTblTdStyle, textAlign: 'center', width: 32, color: 'var(--t3)' }}>
                        {col.colOrder || i + 1}
                      </td>
                      <td style={{ ...pTblTdStyle, fontFamily: 'var(--mono)', fontSize: 11, fontWeight: 500 }}>
                        {col.alias || col.name}
                      </td>
                      <td style={{ ...pTblTdStyle, fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t3)' }}>
                        {col.expression}
                      </td>
                      <td style={{ ...pTblTdStyle, fontSize: 11 }}>
                        {col.alias || '—'}
                      </td>
                      {/* Source atoms via ATOM_PRODUCES edge (pre-grouped by backend) */}
                      <td style={{ ...pTblTdStyle, padding: 0 }}>
                        {srcAtoms.length === 0 ? (
                          <span style={{ padding: '5px 8px', display: 'block', color: 'var(--t3)', fontSize: 10 }}>—</span>
                        ) : (
                          <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                            <tbody>
                              {srcAtoms.slice(0, 10).map((a, ai) => (
                                <tr key={ai} style={{ borderBottom: ai < srcAtoms.length - 1 ? '1px solid var(--bd)' : 'none' }}>
                                  <td style={{
                                    padding: '3px 6px', fontSize: 10,
                                    fontFamily: 'var(--mono)',
                                    borderLeft: `3px solid ${atomStatusColor(a.status)}`,
                                    maxWidth: 140, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                                    color: 'var(--t1)',
                                  }} title={a.text}>
                                    {atomDisplayText(a.text)}
                                  </td>
                                  <td style={{ padding: '3px 6px', fontSize: 9, color: 'var(--inf)', fontFamily: 'var(--mono)', whiteSpace: 'nowrap' }}>
                                    {a.col || a.tbl || '—'}
                                  </td>
                                </tr>
                              ))}
                              {srcAtoms.length > 10 && (
                                <tr>
                                  <td colSpan={2} style={{ padding: '3px 6px', fontSize: 9, color: 'var(--t3)' }}>
                                    +{srcAtoms.length - 10} more
                                  </td>
                                </tr>
                              )}
                            </tbody>
                          </table>
                        )}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </>
      )}

      {/* 7. Atoms — collapsible, collapsed by default */}
      {stmtAtoms && stmtAtoms.length > 0 ? (
        <>
          <CollapsibleSectionHeader
            open={atomsOpen}
            onToggle={() => setAtomsOpen(o => !o)}
            count={stmtAtoms.length}
          >
            {t('knot.stmt.atoms')}
          </CollapsibleSectionHeader>
          {atomsOpen && (
            <>
              <AtomsBreakdown stmt={stmt} t={t} />
              <div style={{ overflowX: 'auto', marginTop: 8 }}>
                <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                  <thead>
                    <tr>
                      <PTblTh>Pos</PTblTh>
                      <PTblTh>{t('knot.stmt.atomText')}</PTblTh>
                      <PTblTh>{t('knot.column.name')}</PTblTh>
                      <PTblTh>{t('knot.stmt.affectedCol')}</PTblTh>
                      <PTblTh>{t('knot.stmt.refSource')}</PTblTh>
                      <PTblTh>{t('knot.stmt.tableGeoid')}</PTblTh>
                      <PTblTh>{t('knot.stmt.status')}</PTblTh>
                      <PTblTh>{t('knot.stmt.context')}</PTblTh>
                      <PTblTh title="s_complex / nested atoms">∑</PTblTh>
                    </tr>
                  </thead>
                  <tbody>
                    {stmtAtoms.slice(0, 50).map((a, i) => (
                      <tr key={i}>
                        {/* Pos: line:col extracted from atom_text */}
                        <td style={{
                          ...pTblTdStyle,
                          fontFamily: 'var(--mono)', fontSize: 10,
                          color: 'var(--t3)', textAlign: 'center', whiteSpace: 'nowrap',
                        }}>
                          {a.atomLine > 0 ? `${a.atomLine}:${a.atomPos}` : '—'}
                        </td>
                        <td style={{
                          ...pTblTdStyle,
                          fontFamily: 'var(--mono)', fontSize: 10,
                          borderLeft: `3px solid ${atomColor(a)}`,
                          maxWidth: 260, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap',
                        }} title={a.atomText}>
                          {atomDisplayText(a.atomText)}
                        </td>
                        <td style={{ ...pTblTdStyle, fontFamily: 'var(--mono)', fontSize: 11 }}>
                          {a.columnName || '—'}
                        </td>
                        <td style={{ ...pTblTdStyle, fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--inf)' }}>
                          {a.outputColName || '—'}
                        </td>
                        <td style={{ ...pTblTdStyle, fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--acc)' }}>
                          {a.refColEdge || a.refSourceName || '—'}
                        </td>
                        <td style={{ ...pTblTdStyle, fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--inf)' }}>
                          {a.refTblEdge || a.tableName || '—'}
                        </td>
                        <td style={{ ...pTblTdStyle, fontSize: 11 }}>
                          <span style={{
                            padding: '1px 5px', borderRadius: 3, fontSize: 10,
                            background: atomStatusBg(a.status), color: atomStatusColor(a.status),
                          }}>
                            {a.status || '—'}
                          </span>
                        </td>
                        <td style={{ ...pTblTdStyle, fontSize: 10, color: 'var(--t3)' }}>
                          {a.atomContext || '—'}
                        </td>
                        {/* Flags: s_complex / nestedAtomsCount / routineParam / routineVar */}
                        <td style={{ ...pTblTdStyle, textAlign: 'center', whiteSpace: 'nowrap' }}>
                          {(() => {
                            const badges: ReactNode[] = [];
                            if (a.complex)       badges.push(<AtomFlag key="c" bg="color-mix(in srgb, var(--t2) 18%, transparent)" color="var(--wrn)" title="s_complex">∑</AtomFlag>);
                            if (a.routineParam)  badges.push(<AtomFlag key="p" bg="color-mix(in srgb, var(--inf) 15%, transparent)" color="var(--inf)" title="routine param">P</AtomFlag>);
                            if (a.routineVar)    badges.push(<AtomFlag key="v" bg="color-mix(in srgb, var(--wrn) 12%, transparent)"  color="var(--wrn)" title="routine var">V</AtomFlag>);
                            if ((a.nestedAtomsCount || 0) > 0) badges.push(
                              <AtomFlag key="n" bg="color-mix(in srgb, var(--suc) 12%, transparent)" color="var(--suc)" title="nested atoms count">{a.nestedAtomsCount}</AtomFlag>
                            );
                            return badges.length > 0
                              ? <div style={{ display: 'flex', gap: 2, justifyContent: 'center', flexWrap: 'nowrap' }}>{badges}</div>
                              : <span style={{ color: 'var(--t3)', fontSize: 10 }}>—</span>;
                          })()}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {stmtAtoms.length > 50 && (
                  <div style={{ padding: '6px 8px', fontSize: 10, color: 'var(--t3)' }}>
                    +{stmtAtoms.length - 50} {t('knot.stmt.moreAtoms')}
                  </div>
                )}
              </div>
            </>
          )}
        </>
      ) : stmt.atomTotal > 0 ? (
        <>
          <SectionHeader count={stmt.atomTotal}>{t('knot.stmt.atoms')}</SectionHeader>
          <AtomsBreakdown stmt={stmt} t={t} />
        </>
      ) : (
        <>
          <SectionHeader>{t('knot.stmt.atoms')}</SectionHeader>
          <div style={{ padding: '6px 0', fontSize: 11, color: 'var(--t3)' }}>
            {stmt.children?.length ? t('knot.stmt.wrapperNoAtoms') : t('knot.stmt.noAtoms')}
          </div>
        </>
      )}

      {/* 8. Subqueries — only for root statements */}
      {flatChildren && flatChildren.length > 0 && expanded && onToggle && (
        <div style={{ margin: '14px 0 0', borderTop: '1px solid var(--bd)' }}>
          <FlatSubqueries rows={flatChildren} expanded={expanded} toggle={onToggle} maps={maps} />
        </div>
      )}
    </div>
  );
}
