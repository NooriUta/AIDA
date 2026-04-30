import React, { memo, useState, useMemo, useCallback } from 'react';
import { useTranslation } from 'react-i18next';
import type { KnotStatement, KnotSnippet, KnotAtom, KnotOutputColumn, KnotAffectedColumn } from '../../services/lineage';
import { ToolbarSelect } from '../ui/ToolbarPrimitives';
import type { LookupMaps } from './knotStmtHelpers';
import {
  flattenAll, flattenChildren,
} from './knotStmtHelpers';
import { StmtTableRow } from './KnotStmtTableRow';
import { StmtDetailPanel } from './StmtDetailPanel';

interface Props {
  statements: KnotStatement[];
  snippets?: KnotSnippet[];
  atoms?: KnotAtom[];
  outputColumns?: KnotOutputColumn[];
  affectedColumns?: KnotAffectedColumn[];
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
