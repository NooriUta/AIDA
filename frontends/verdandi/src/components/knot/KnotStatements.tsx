import { memo, useState, useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import type { KnotStatement, KnotSnippet, KnotAtom, KnotOutputColumn, KnotAffectedColumn } from '../../services/lineage';
import { ToolbarSelect } from '../ui/ToolbarPrimitives';
import { useKnotSnippet } from '../../services/hooks';

/** All lookup maps passed through the component tree */
type LookupMaps = {
  snippetMap: Map<string, string>;
  atomMap: Map<string, KnotAtom[]>;
  outColMap: Map<string, KnotOutputColumn[]>;
  affColMap: Map<string, KnotAffectedColumn[]>;
};

interface Props {
  statements: KnotStatement[];
  snippets?: KnotSnippet[];
  atoms?: KnotAtom[];
  outputColumns?: KnotOutputColumn[];
  affectedColumns?: KnotAffectedColumn[];
}

const TYPE_BADGE: Record<string, { bg: string; color: string }> = {
  SELECT:   { bg: 'color-mix(in srgb, var(--inf) 15%, transparent)', color: 'var(--inf)' },
  INSERT:   { bg: 'color-mix(in srgb, var(--suc) 15%, transparent)', color: 'var(--suc)' },
  UPDATE:   { bg: 'color-mix(in srgb, var(--wrn) 15%, transparent)',  color: 'var(--wrn)' },
  DELETE:   { bg: 'color-mix(in srgb, var(--danger) 15%, transparent)',   color: 'var(--danger)' },
  MERGE:    { bg: 'color-mix(in srgb, var(--t2) 15%, transparent)', color: 'var(--t2)' },
  CURSOR:   { bg: 'color-mix(in srgb, var(--t3) 25%, transparent)',   color: 'var(--t3)' },
  DINAMIC_CURSOR: { bg: 'color-mix(in srgb, var(--t3) 25%, transparent)', color: 'var(--t3)' },
  DYNAMIC_CURSOR: { bg: 'color-mix(in srgb, var(--t3) 25%, transparent)', color: 'var(--t3)' },
  FOR_CURSOR:     { bg: 'color-mix(in srgb, var(--t3) 25%, transparent)', color: 'var(--t3)' },
  SUBQUERY: { bg: 'color-mix(in srgb, var(--t3) 25%, transparent)',   color: 'var(--t3)' },
  CTE:      { bg: 'color-mix(in srgb, var(--t3) 25%, transparent)',   color: 'var(--t3)' },
  UNKNOWN:  { bg: 'color-mix(in srgb, var(--t3) 25%, transparent)',   color: 'var(--t3)' },
};

// ── Helpers ─────────────────────────────────────────────────────────────────

type FlatRow = { stmt: KnotStatement; depth: number };

/**
 * Geoid format: ...SCHEMA:PROC_TYPE:PROC_NAME:STMT_TYPE:LINE[:CHILD_TYPE:CHILD_LINE...]
 * The statement type is always at index -2 (before the trailing line number).
 * The statement line is always at index -1.
 * Both are more reliable than stmtType/lineNumber for child statements
 * where the backend may return the root's values instead of the child's.
 */
function typeFromGeoid(geoid: string | undefined): string {
  if (!geoid) return '';
  const parts = geoid.split(':');
  if (parts.length < 2) return '';
  const candidate = parts[parts.length - 2];
  return /^[A-Z][A-Z0-9_]*$/.test(candidate) ? candidate : '';
}

function lineFromGeoid(geoid: string | undefined): number | null {
  if (!geoid) return null;
  const parts = geoid.split(':');
  const n = parseInt(parts[parts.length - 1], 10);
  return isNaN(n) ? null : n;
}

/** Flatten entire tree into a flat list with depth */
function flattenAll(stmts: KnotStatement[]): FlatRow[] {
  const result: FlatRow[] = [];
  const walk = (list: KnotStatement[], depth: number) => {
    for (const s of list) {
      result.push({ stmt: s, depth });
      if (s.children?.length) walk(s.children, depth + 1);
    }
  };
  walk(stmts, 0);
  return result;
}

/** Flatten only children (skip root), keeping depth relative */
function flattenChildren(stmt: KnotStatement): FlatRow[] {
  const result: FlatRow[] = [];
  const walk = (list: KnotStatement[], depth: number) => {
    for (const s of list) {
      result.push({ stmt: s, depth });
      if (s.children?.length) walk(s.children, depth + 1);
    }
  };
  if (stmt.children?.length) walk(stmt.children, 1);
  return result;
}

function shortName(stmt: KnotStatement): string {
  const parts: string[] = [];
  if (stmt.routineName) parts.push(stmt.routineName);
  const type = typeFromGeoid(stmt.geoid) || stmt.stmtType;
  if (type) parts.push(type);
  return parts.join(' ') || stmt.geoid?.split(':').slice(-2).join(':') || '—';
}

function truncGeoid(geoid: string | undefined): string {
  if (!geoid) return '—';
  const parts = geoid.split(':');
  if (parts.length <= 3) return geoid;
  return '…:' + parts.slice(-3).join(':');
}

// ── Main component ──────────────────────────────────────────────────────────

export const KnotStatements = memo(({ statements, snippets, atoms, outputColumns, affectedColumns }: Props) => {
  const { t } = useTranslation();
  const [expanded, setExpanded] = useState<Set<string>>(new Set());

  // Build lookup maps: geoid → data
  const maps: LookupMaps = useMemo(() => {
    const snippetMap = new Map<string, string>();
    if (snippets) for (const s of snippets) {
      if (s.stmtGeoid && s.snippet) snippetMap.set(s.stmtGeoid, s.snippet);
    }
    const atomMap = new Map<string, KnotAtom[]>();
    if (atoms) for (const a of atoms) {
      if (a.stmtGeoid) {
        const list = atomMap.get(a.stmtGeoid) || [];
        list.push(a);
        atomMap.set(a.stmtGeoid, list);
      }
    }
    const outColMap = new Map<string, KnotOutputColumn[]>();
    if (outputColumns) for (const c of outputColumns) {
      if (c.stmtGeoid) {
        const list = outColMap.get(c.stmtGeoid) || [];
        list.push(c);
        outColMap.set(c.stmtGeoid, list);
      }
    }
    // Sort output columns by colOrder within each statement
    for (const [key, cols] of outColMap) {
      outColMap.set(key, cols.slice().sort((a, b) => (a.colOrder ?? 0) - (b.colOrder ?? 0)));
    }
    const affColMap = new Map<string, KnotAffectedColumn[]>();
    if (affectedColumns) for (const c of affectedColumns) {
      if (c.stmtGeoid) {
        const list = affColMap.get(c.stmtGeoid) || [];
        list.push(c);
        affColMap.set(c.stmtGeoid, list);
      }
    }
    return { snippetMap, atomMap, outColMap, affColMap };
  }, [snippets, atoms, outputColumns, affectedColumns]);

  const routines = useMemo(() => {
    const set = new Set<string>();
    statements.forEach(s => {
      const r = [s.packageName, s.routineName].filter(Boolean).join(':');
      if (r) set.add(r);
    });
    return ['ALL', ...Array.from(set)];
  }, [statements]);

  const [selectedRoutine, setSelectedRoutine] = useState('ALL');

  const filtered = useMemo(() => {
    if (selectedRoutine === 'ALL') return statements;
    return statements.filter(s => {
      const r = [s.packageName, s.routineName].filter(Boolean).join(':');
      return r === selectedRoutine;
    });
  }, [statements, selectedRoutine]);

  const allRows = useMemo(() => flattenAll(filtered), [filtered]);
  const subCount = allRows.length - filtered.length;

  const toggle = useCallback((id: string) => {
    setExpanded(prev => {
      const next = new Set(prev);
      next.has(id) ? next.delete(id) : next.add(id);
      return next;
    });
  }, []);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', height: '100%', overflow: 'hidden' }}>

      {/* Toolbar */}
      <div style={{
        padding: '8px 16px',
        background: 'var(--bg0)',
        borderBottom: '1px solid var(--bd)',
        display: 'flex', alignItems: 'center', gap: 8, flexShrink: 0,
      }}>
        <span style={{ fontSize: 11, color: 'var(--t3)' }}>{t('knot.stmt.routine')}:</span>
        <ToolbarSelect
          value={selectedRoutine}
          onChange={setSelectedRoutine}
          options={routines.map(r => ({ value: r, label: r === 'ALL' ? t('knot.stmt.allRoutines') : r }))}
        />
        <span style={{
          padding: '3px 8px', borderRadius: 3,
          background: 'var(--bg3)', border: '1px solid var(--bd)',
          color: 'var(--t2)', fontSize: 10,
        }}>
          {t('knot.stmt.rootStmts', { count: filtered.length })}
        </span>
        <span style={{ marginLeft: 'auto', fontSize: 11, color: 'var(--t3)' }}>
          {t('knot.stmt.showingSubq', { count: subCount })}
        </span>
      </div>

      {/* Table */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '12px 16px' }}>
        <div style={{ background: 'var(--bg2)', border: '1px solid var(--bd)', borderRadius: 6, overflow: 'hidden' }}>
          <div style={{
            padding: '10px 14px', borderBottom: '1px solid var(--bd)',
            display: 'flex', alignItems: 'center', gap: 8,
          }}>
            <div style={{ fontSize: 12, fontWeight: 500, color: 'var(--t1)' }}>
              {t('knot.tabs.statements')}
            </div>
            <span style={{ fontSize: 10, color: 'var(--t3)' }}>
              {selectedRoutine !== 'ALL' ? selectedRoutine.split(':').pop() + ' — ' : ''}
              {t('knot.stmt.rootAndSubq', { root: filtered.length, sub: subCount })}
            </span>
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
                  { key: 'knot.stmt.level' },
                  { key: 'knot.stmt.line', center: true },
                  { key: 'knot.stmt.sources', center: true },
                  { key: 'knot.stmt.targets', center: true },
                  { key: 'knot.stmt.subqueriesCount', center: true },
                  { key: 'knot.stmt.atoms', center: true },
                ].map((h, i) => (
                  <th key={i} style={{
                    padding: '6px 8px',
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
              {filtered.map(stmt => (
                <RootStmtRow key={stmt.id} stmt={stmt} expanded={expanded} toggle={toggle} maps={maps} />
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
});

KnotStatements.displayName = 'KnotStatements';

// ── Root statement row ──────────────────────────────────────────────────────

function RootStmtRow({ stmt, expanded, toggle, maps }: {
  stmt: KnotStatement; expanded: Set<string>; toggle: (id: string) => void;
  maps: LookupMaps;
}) {
  const { t } = useTranslation();
  const isOpen = expanded.has(stmt.id);
  const flatChildren = useMemo(() => flattenChildren(stmt), [stmt]);

  return (
    <>
      <StmtTableRow
        stmt={stmt}
        depth={0}
        isOpen={isOpen}
        toggle={toggle}
        levelLabel={t('knot.stmt.root')}
      />
      {isOpen && (
        <tr>
          <td colSpan={11} style={{
            padding: 0,
            background: 'var(--bg2)',
            borderBottom: '3px solid var(--acc)',
          }}>
            <StmtDetailPanel
              stmt={stmt} t={t} maps={maps}
              flatChildren={flatChildren}
              expanded={expanded}
              onToggle={toggle}
            />
          </td>
        </tr>
      )}
    </>
  );
}

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

// ── Shared table row ────────────────────────────────────────────────────────

function StmtTableRow({ stmt, depth, isOpen, toggle, levelLabel, indent }: {
  stmt: KnotStatement; depth: number; isOpen: boolean;
  toggle: (id: string) => void; levelLabel: string; indent?: boolean;
}) {
  const effectiveType = typeFromGeoid(stmt.geoid) || stmt.stmtType;
  const effectiveLine = lineFromGeoid(stmt.geoid) ?? stmt.lineNumber;
  const badge = TYPE_BADGE[effectiveType] || TYPE_BADGE.UNKNOWN;
  const pad = indent ? depth * 12 : 0;
  // Root rows: slightly different background so they visually stand out from subqueries
  const isRoot = depth === 0;
  const baseBg  = isRoot ? 'var(--bg3)' : '';
  const hoverBg = isRoot ? 'color-mix(in srgb, var(--bg3) 70%, var(--acc) 30%)' : 'var(--bg3)';

  return (
    <tr
      onClick={() => toggle(stmt.id)}
      style={{
        cursor: 'pointer', transition: 'background 0.1s',
        background: isOpen ? (isRoot ? 'color-mix(in srgb, var(--bg3) 80%, var(--acc) 20%)' : 'var(--bg3)') : baseBg,
      }}
      onMouseEnter={e => { (e.currentTarget as HTMLElement).style.background = hoverBg; }}
      onMouseLeave={e => {
        (e.currentTarget as HTMLElement).style.background = isOpen
          ? (isRoot ? 'color-mix(in srgb, var(--bg3) 80%, var(--acc) 20%)' : 'var(--bg3)')
          : baseBg;
      }}
    >
      {/* Arrow */}
      <td style={{ textAlign: 'center', padding: '6px 4px', borderBottom: '1px solid var(--bd)' }}>
        <span style={{
          fontSize: 9, color: 'var(--t3)', display: 'inline-block',
          transition: 'transform 0.15s',
          transform: isOpen ? 'rotate(90deg)' : 'none',
        }}>
          {'\u25B6'}
        </span>
      </td>
      {/* Name (geoid) */}
      <td style={{
        padding: '6px 8px', paddingLeft: 8 + pad, borderBottom: '1px solid var(--bd)',
        fontFamily: 'var(--mono)', fontSize: 10, color: 'var(--t2)',
      }}>
        {indent && <span style={{ color: 'var(--t3)', fontSize: 10, marginRight: 4 }}>{'\u2514'}</span>}
        {truncGeoid(stmt.geoid)}
      </td>
      {/* Short name */}
      <td style={{
        padding: '6px 8px', borderBottom: '1px solid var(--bd)',
        fontWeight: depth === 0 ? 500 : 400, fontSize: depth === 0 ? 12 : 11,
      }}>
        {shortName(stmt)}
      </td>
      {/* Aliases */}
      <td style={{ padding: '6px 8px', borderBottom: '1px solid var(--bd)', maxWidth: 120 }}>
        {(stmt.stmtAliases?.length || 0) > 0 ? (
          <div style={{ display: 'flex', gap: 2, flexWrap: 'wrap' }}>
            {(stmt.stmtAliases || []).slice(0, 3).map(a => (
              <span key={a} style={{
                padding: '1px 5px', borderRadius: 3, fontSize: 9,
                fontFamily: 'var(--mono)',
                background: 'color-mix(in srgb, var(--acc) 10%, transparent)',
                color: 'var(--acc)',
              }}>{a}</span>
            ))}
            {(stmt.stmtAliases?.length || 0) > 3 && (
              <span style={{ fontSize: 9, color: 'var(--t3)' }}>+{(stmt.stmtAliases?.length || 0) - 3}</span>
            )}
          </div>
        ) : (
          <span style={{ color: 'var(--t3)', fontSize: 10 }}>—</span>
        )}
      </td>
      {/* Type */}
      <td style={{ padding: '6px 8px', borderBottom: '1px solid var(--bd)' }}>
        <span style={{
          padding: '2px 6px', borderRadius: 3,
          fontSize: 10, fontFamily: 'var(--mono)',
          background: badge.bg, color: badge.color,
        }}>
          {effectiveType}
        </span>
      </td>
      {/* Level */}
      <td style={{ padding: '6px 8px', borderBottom: '1px solid var(--bd)', color: 'var(--t3)', fontSize: 11, textAlign: 'center' }}>
        {levelLabel}
      </td>
      {/* Line */}
      <td style={{
        padding: '6px 8px', borderBottom: '1px solid var(--bd)', textAlign: 'center',
        fontFamily: 'var(--mono)', fontSize: 11, color: 'var(--t2)',
      }}>
        {effectiveLine || '—'}
      </td>
      {/* Src */}
      <td style={{
        padding: '6px 8px', borderBottom: '1px solid var(--bd)', textAlign: 'center',
        color: (stmt.sourceTables?.length || 0) > 0 ? 'var(--inf)' : 'var(--t3)',
        fontWeight: (stmt.sourceTables?.length || 0) > 0 ? 500 : 400,
      }}>
        {stmt.sourceTables?.length || 0}
      </td>
      {/* Tgt */}
      <td style={{
        padding: '6px 8px', borderBottom: '1px solid var(--bd)', textAlign: 'center',
        color: (stmt.targetTables?.length || 0) > 0 ? 'var(--suc)' : 'var(--t3)',
        fontWeight: (stmt.targetTables?.length || 0) > 0 ? 500 : 400,
      }}>
        {stmt.targetTables?.length || 0}
      </td>
      {/* Subqueries count */}
      <td style={{
        padding: '6px 8px', borderBottom: '1px solid var(--bd)', textAlign: 'center',
        color: (stmt.children?.length || 0) > 0 ? 'var(--t2)' : 'var(--t3)',
      }}>
        {stmt.children?.length || '—'}
      </td>
      {/* Atoms */}
      <td style={{
        padding: '6px 8px', borderBottom: '1px solid var(--bd)', textAlign: 'center',
        fontWeight: stmt.atomTotal > 0 ? 500 : 400,
        color: stmt.atomTotal > 0 ? 'var(--t1)' : 'var(--t3)',
      }}>
        {stmt.atomTotal || '—'}
      </td>
    </tr>
  );
}

// ── Detail panel (shared by root and sub rows) ──────────────────────────────

/** Strip `~line:pos` suffix from atom_text for display */
function atomDisplayText(atomText: string): string {
  const tilde = atomText.lastIndexOf('~');
  return tilde >= 0 ? atomText.substring(0, tilde) : atomText;
}

function StmtDetailPanel({ stmt, t, maps, flatChildren, expanded, onToggle }: {
  stmt: KnotStatement;
  t: (k: string, opts?: Record<string, unknown>) => string;
  maps: LookupMaps;
  flatChildren?: FlatRow[];
  expanded?: Set<string>;
  onToggle?: (id: string) => void;
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
                            const badges: React.ReactNode[] = [];
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

// ── Tiny shared components ──────────────────────────────────────────────────

const sectionHeaderStyle: React.CSSProperties = {
  fontSize: 10, fontWeight: 500, letterSpacing: '0.08em',
  textTransform: 'uppercase', color: 'var(--t3)',
  padding: '10px 0 6px', borderBottom: '1px solid var(--bd)',
  display: 'flex', alignItems: 'center', gap: 6,
  marginBottom: 8, marginTop: 14,
};

function SectionHeader({ children, count }: { children: React.ReactNode; count?: number }) {
  return (
    <div style={sectionHeaderStyle}>
      {children}
      {count != null && (
        <span style={{
          padding: '1px 5px', borderRadius: 8, fontSize: 10,
          background: 'var(--bg4, var(--bg3))', color: 'var(--t3)',
          border: '1px solid var(--bd)',
        }}>
          {count}
        </span>
      )}
    </div>
  );
}

function CollapsibleSectionHeader({ children, count, open, onToggle }: {
  children: React.ReactNode;
  count?: number;
  open: boolean;
  onToggle: () => void;
}) {
  return (
    <div
      onClick={onToggle}
      style={{ ...sectionHeaderStyle, cursor: 'pointer', userSelect: 'none' }}
    >
      <span style={{
        fontSize: 8, display: 'inline-block',
        transition: 'transform 0.15s',
        transform: open ? 'rotate(90deg)' : 'none',
        color: 'var(--acc)',
      }}>
        {'\u25B6'}
      </span>
      {children}
      {count != null && (
        <span style={{
          padding: '1px 5px', borderRadius: 8, fontSize: 10,
          background: 'var(--bg4, var(--bg3))', color: 'var(--t3)',
          border: '1px solid var(--bd)',
        }}>
          {count}
        </span>
      )}
    </div>
  );
}

function InfoRow({ label, value, mono }: { label: string; value: string; mono?: boolean }) {
  return (
    <tr>
      <th style={{
        padding: '5px 8px', fontSize: 11, color: 'var(--t3)',
        fontWeight: 500, width: 180, verticalAlign: 'top',
        borderBottom: '1px solid var(--bd)', textAlign: 'left',
      }}>
        {label}
      </th>
      <td style={{
        padding: '5px 8px', fontSize: 11, color: 'var(--t2)',
        borderBottom: '1px solid var(--bd)',
        ...(mono ? { fontFamily: 'var(--mono)' } : {}),
      }}>
        {value}
      </td>
    </tr>
  );
}

function PTblTh({ children }: { children: React.ReactNode }) {
  return (
    <th style={{
      padding: '5px 8px', fontSize: 10, fontWeight: 500,
      letterSpacing: '0.05em', textTransform: 'uppercase', color: 'var(--t3)',
      background: 'var(--bg3)', border: '1px solid var(--bd)', textAlign: 'left',
    }}>
      {children}
    </th>
  );
}

const pTblTdStyle: React.CSSProperties = {
  padding: '5px 8px', fontSize: 11, color: 'var(--t2)',
  border: '1px solid var(--bd)', verticalAlign: 'top',
};

function AtomFlag({ children, bg, color, title }: { children: React.ReactNode; bg: string; color: string; title?: string }) {
  return (
    <span style={{
      display: 'inline-block', padding: '1px 4px', borderRadius: 3,
      fontSize: 9, fontFamily: 'var(--mono)',
      background: bg, color,
    }} title={title}>
      {children}
    </span>
  );
}

function atomColor(a: KnotAtom): string {
  if (a.columnReference) return 'var(--inf)';
  if (a.constant) return 'var(--wrn)';
  if (a.functionCall) return 'var(--suc)';
  return 'var(--t3)';
}

function atomStatusBg(status: string): string {
  switch (status.toLowerCase()) {
    case 'обработано': return 'color-mix(in srgb, var(--suc) 15%, transparent)';
    case 'unresolved': return 'color-mix(in srgb, var(--danger) 15%, transparent)';
    case 'constant':   return 'color-mix(in srgb, var(--wrn) 12%, transparent)';
    case 'function_call': return 'color-mix(in srgb, var(--inf) 15%, transparent)';
    default: return 'var(--bg3)';
  }
}

function atomStatusColor(status: string): string {
  switch (status.toLowerCase()) {
    case 'обработано': return 'var(--suc)';
    case 'unresolved': return 'var(--danger)';
    case 'constant':   return 'var(--wrn)';
    case 'function_call': return 'var(--inf)';
    default: return 'var(--t3)';
  }
}

function AtomsBreakdown({ stmt, t }: { stmt: KnotStatement; t: (k: string) => string }) {
  const other = stmt.atomFuncCall ?? 0;
  return (
    <div style={{
      display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(120px, 1fr))',
      gap: '4px 16px', padding: '6px 0', fontSize: 11,
    }}>
      <AtomStat label={t('knot.stmt.total')} value={stmt.atomTotal} color="var(--t1)" />
      <AtomStat label={t('knot.atoms.resolved')} value={stmt.atomResolved} color="var(--suc)" />
      <AtomStat label={t('knot.atoms.failed')} value={stmt.atomFailed} color="var(--danger)" />
      <AtomStat label={t('knot.atoms.constant')} value={stmt.atomConstant} color="var(--t3)" />
      {other > 0 && <AtomStat label={t('knot.atoms.funcCall')} value={other} color="var(--inf)" />}
    </div>
  );
}

function AtomStat({ label, value, color }: { label: string; value: number; color: string }) {
  return (
    <span>
      <span style={{ color: 'var(--t3)', marginRight: 4 }}>{label}:</span>
      <span style={{ fontFamily: 'var(--mono)', fontWeight: 500, color }}>{value}</span>
    </span>
  );
}

function SqlBlock({ sql }: { sql: string }) {
  const [copied, setCopied] = useState(false);

  const handleCopy = useCallback(() => {
    navigator.clipboard.writeText(sql).then(() => {
      setCopied(true);
      setTimeout(() => setCopied(false), 1500);
    });
  }, [sql]);

  return (
    <div style={{
      position: 'relative',
      background: 'var(--bg0)',
      border: '1px solid var(--bd)',
      borderRadius: 6,
      overflow: 'hidden',
    }}>
      {/* Copy button */}
      <button
        onClick={handleCopy}
        style={{
          position: 'absolute', top: 6, right: 6,
          padding: '3px 8px', fontSize: 10,
          background: copied ? 'var(--suc)' : 'var(--bg3)',
          border: '1px solid var(--bd)', borderRadius: 4,
          color: copied ? 'var(--bg0)' : 'var(--t3)',
          cursor: 'pointer', fontFamily: 'inherit',
          transition: 'background 0.15s, color 0.15s',
          zIndex: 1,
        }}
      >
        {copied ? 'Copied' : 'Copy'}
      </button>

      {/* SQL code */}
      <pre style={{
        margin: 0,
        padding: '12px 14px',
        fontSize: 11,
        lineHeight: 1.55,
        fontFamily: 'var(--mono)',
        color: 'var(--t2)',
        overflowX: 'auto',
        overflowY: 'auto',
        maxHeight: 320,
        whiteSpace: 'pre',
        tabSize: 2,
      }}>
        {sql}
      </pre>
    </div>
  );
}
