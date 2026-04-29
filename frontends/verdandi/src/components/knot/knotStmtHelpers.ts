import type { KnotStatement, KnotAtom, KnotOutputColumn, KnotAffectedColumn } from '../../services/lineage';

export type LookupMaps = {
  snippetMap: Map<string, string>;
  atomMap: Map<string, KnotAtom[]>;
  outColMap: Map<string, KnotOutputColumn[]>;
  affColMap: Map<string, KnotAffectedColumn[]>;
};

export type FlatRow = { stmt: KnotStatement; depth: number };

export const TYPE_BADGE: Record<string, { bg: string; color: string }> = {
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

export function typeFromGeoid(geoid: string | undefined): string {
  if (!geoid) return '';
  const parts = geoid.split(':');
  if (parts.length < 2) return '';
  const candidate = parts[parts.length - 2];
  return /^[A-Z][A-Z0-9_]*$/.test(candidate) ? candidate : '';
}

export function lineFromGeoid(geoid: string | undefined): number | null {
  if (!geoid) return null;
  const parts = geoid.split(':');
  const n = parseInt(parts[parts.length - 1], 10);
  return isNaN(n) ? null : n;
}

export function flattenAll(stmts: KnotStatement[]): FlatRow[] {
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

export function flattenChildren(stmt: KnotStatement): FlatRow[] {
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

export function shortName(stmt: KnotStatement): string {
  const parts: string[] = [];
  if (stmt.routineName) parts.push(stmt.routineName);
  const type = typeFromGeoid(stmt.geoid) || stmt.stmtType;
  if (type) parts.push(type);
  return parts.join(' ') || stmt.geoid?.split(':').slice(-2).join(':') || '—';
}

export function truncGeoid(geoid: string | undefined): string {
  if (!geoid) return '—';
  const parts = geoid.split(':');
  if (parts.length <= 3) return geoid;
  return '…:' + parts.slice(-3).join(':');
}
