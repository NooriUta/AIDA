import type { CSSProperties } from 'react';
import type { DaliNodeType, DaliEdgeType } from '../types/domain';

// ─── Map Dali node type → React Flow node type string ───────────────────────
export const NODE_TYPE_MAP: Record<DaliNodeType, string> = {
  DaliApplication:    'applicationNode',
  DaliService:        'applicationNode',  // зарезервировано, визуально как Application
  DaliDatabase:       'databaseNode',
  DaliSchema:         'schemaNode',
  DaliPackage:        'packageNode',
  DaliTable:          'tableNode',
  DaliColumn:         'columnNode',
  DaliOutputColumn:   'columnNode',
  DaliAffectedColumn: 'columnNode',
  DaliAtom:           'atomNode',
  DaliRoutine:        'routineNode',
  DaliStatement:      'statementNode',
  DaliSession:        'routineNode',
  DaliJoin:           'routineNode',
  DaliParameter:      'columnNode',
  DaliVariable:       'columnNode',
  // Phase S2.4 — PL/SQL records
  DaliRecord:         'recordNode',
  DaliRecordField:    'columnNode',  // reuse column row visually
};

// ─── Node types that support drilling down ───────────────────────────────────
// Application/Service use scope filter (not level transition) on L1 (LOOM-024)
// DaliRoutine: double-click at L2 AGG drills to L3 EXP (scoped to that routine/package).
export const DRILLABLE_TYPES = new Set<DaliNodeType>([
  'DaliDatabase', 'DaliSchema', 'DaliPackage', 'DaliTable', 'DaliStatement', 'DaliRoutine',
]);

// Scope-filter on L1: double-click Application — сужает граф до её СУБД и схем
// Database и Schema при double-click уходят на L2 (drill-down)
export const SCOPE_FILTER_TYPES = new Set<DaliNodeType>([
  'DaliApplication',
]);

// ─── SEER Design System v1.1 — Amber Forest edge colours ────────────────────
export const ANIMATED_EDGES = new Set<DaliEdgeType>([
  'DATA_FLOW', 'ATOM_PRODUCES', 'FILTER_FLOW',
  'JOIN_FLOW', 'UNION_FLOW',
]);

export function getEdgeStyle(type: DaliEdgeType): CSSProperties {
  switch (type) {
    // ── L1 Application graph (LOOM-024) ──────────────────────────────────────
    case 'HAS_DATABASE':    return { stroke: '#A8B860', strokeWidth: 2 };
    case 'CONTAINS_SCHEMA': return { stroke: '#88B8A8', strokeWidth: 1.5 };
    case 'HAS_SERVICE':     return { stroke: '#A8B860', strokeWidth: 1.5 };
    case 'USES_DATABASE':   return { stroke: '#665c48', strokeWidth: 1.5, strokeDasharray: '6 3' };
    // ── Data flow ────────────────────────────────────────────────────────────
    // Each of the four primary data-flow edge types gets a unique combination
    // of (colour, dash pattern, animated) so they stay distinguishable at any
    // zoom level and on dense canvases:
    //   READS_FROM  — teal  solid           (consumption, incoming, calm)
    //   WRITES_TO   — amber solid           (production, outgoing — solid matches READS_FROM weight)
    //   DATA_FLOW   — lime  medium-dash 5/3 (column→column, animated flow)
    //   FILTER_FLOW — mauve dots       1/4  (filter predicate, animated dots)
    case 'READS_FROM':      return { stroke: '#88B8A8', strokeWidth: 1.5 };
    case 'WRITES_TO':       return { stroke: '#D4922A', strokeWidth: 1.5 };
    case 'DATA_FLOW':       return { stroke: '#A8B860', strokeWidth: 1.5, strokeDasharray: '5 3' };
    case 'FILTER_FLOW':     return { stroke: '#B87AA8', strokeWidth: 1.5, strokeDasharray: '1 4' };
    case 'JOIN_FLOW':       return { stroke: '#88B8A8', strokeWidth: 1.5 };
    case 'UNION_FLOW':      return { stroke: '#A8B860', strokeWidth: 1.5 };
    case 'ATOM_PRODUCES':   return { stroke: '#A8B860', strokeWidth: 1.5 };
    case 'ATOM_REF_COLUMN': return { stroke: '#88B8A8', strokeWidth: 1 };
    case 'HAS_ATOM':        return { stroke: '#88B8A8', strokeWidth: 1 };
    case 'HAS_COLUMN':         return { stroke: '#665c48', strokeWidth: 1, strokeDasharray: '4 3' };
    case 'CONTAINS_ROUTINE':   return { stroke: '#665c48', strokeWidth: 1, strokeDasharray: '6 3' };
    case 'CONTAINS_STMT':      return { stroke: '#665c48', strokeWidth: 1, strokeDasharray: '4 2' };
    case 'BELONGS_TO_SESSION': return { stroke: '#665c48', strokeWidth: 1, strokeDasharray: '6 3' };
    // Routine-to-routine call flow (CALLS edge — procedure invocations)
    case 'CALLS':              return { stroke: '#D4922A', strokeWidth: 1.5, strokeDasharray: '5 4' };
    // Phase S2.4 — PL/SQL record edges
    // HAS_RECORD_FIELD is structural (fields render inside RecordNode) — suppressed as arrow, style unused
    case 'HAS_RECORD_FIELD':      return { stroke: '#665c48', strokeWidth: 1, strokeDasharray: '4 3' };
    // BULK_COLLECTS_INTO: cursor SELECT → Record (amber, same family as WRITES_TO)
    case 'BULK_COLLECTS_INTO':    return { stroke: '#D4922A', strokeWidth: 1.5, strokeDasharray: '5 3' };
    // RETURNS_INTO: Statement → Record/Field/Var (mauve, data flow)
    case 'RETURNS_INTO':          return { stroke: '#B87AA8', strokeWidth: 1.5 };
    // RECORD_USED_IN: Record → Statement (teal, consumption — inverse of BULK_COLLECTS_INTO)
    case 'RECORD_USED_IN':        return { stroke: '#88B8A8', strokeWidth: 1.5, strokeDasharray: '5 3' };
    default:                  return { stroke: '#42382a', strokeWidth: 1, strokeDasharray: '4 3' };
  }
}

// ─── Statement type extraction ───────────────────────────────────────────────
export const SQL_KEYWORDS = new Set([
  'INSERT', 'SELECT', 'UPDATE', 'DELETE', 'MERGE',
  'CREATE', 'DROP', 'ALTER', 'TRUNCATE', 'CALL',
  'OPEN', 'FETCH', 'CLOSE', 'CTE', 'WITH', 'SQ',
  'CURSOR', 'DINAMIC_CURSOR', 'DYNAMIC_CURSOR',
]);

export function extractStatementType(label: string): string | undefined {
  const parts = label.split(/[\s:]+/).map((p) => p.toUpperCase());
  // SQ marks a subquery container — inner SELECT/INSERT is content, not the node type
  if (parts.includes('SQ')) return 'SQ';
  // Cursor variants normalised to single badge
  if (parts.some((p) => p === 'CURSOR' || p === 'DINAMIC_CURSOR' || p === 'DYNAMIC_CURSOR')) return 'CURSOR';
  // Root statements: first keyword = outermost operation type
  for (const p of parts) {
    if (SQL_KEYWORDS.has(p)) return p;
  }
  return undefined;
}

// ─── Routine kind extraction ─────────────────────────────────────────────────
export const ROUTINE_KINDS: Record<string, string> = {
  FUNCTION:  'FUNC',
  PROCEDURE: 'PROC',
};

export function extractRoutineKind(label: string, nodeType: DaliNodeType): string {
  if (nodeType === 'DaliPackage') return 'PKG';
  if (nodeType === 'DaliSession') return 'SESSION';
  for (const part of label.split(/[\s:.]+/)) {
    const short = ROUTINE_KINDS[part.toUpperCase()];
    if (short) return short;
  }
  return 'ROUTINE';
}

// ─── Statement label parser ──────────────────────────────────────────────────
//
// Full format: "SCHEMA.PACKAGE:PROCEDURE:ROUTINE_NAME:STMT_TYPE:LINE[:STMT_TYPE:LINE...]"
//
// Example: "BUDM_RMS_TMD.DM_LOADER_...:PROCEDURE:LOAD_..._REG:DELETE:687"
//   → label: "DELETE:687"
//   → groupPath: ["BUDM_RMS_TMD", "DM_LOADER_...", "LOAD_..._REG"]
const ROUTINE_TYPE_KEYWORDS = new Set(['PROCEDURE', 'FUNCTION']);

export function parseStmtLabel(label: string): { shortLabel: string; groupPath: string[] } {
  if (!label.includes(':')) return { shortLabel: label, groupPath: [] };

  const parts = label.split(':');
  const groupPath: string[] = [];

  let routineIdx = -1;
  for (let i = 0; i < parts.length; i++) {
    if (ROUTINE_TYPE_KEYWORDS.has(parts[i].toUpperCase())) { routineIdx = i; break; }
  }

  if (routineIdx >= 0) {
    const schemaPkg = parts[0].split('.');
    groupPath.push(...schemaPkg);
    if (routineIdx + 1 < parts.length) {
      groupPath.push(parts[routineIdx + 1]);
    }
    const stmtParts = parts.slice(routineIdx + 2);
    return { shortLabel: stmtParts.join(':') || label, groupPath };
  }

  return { shortLabel: parts.slice(-2).join(':'), groupPath: [] };
}

/**
 * DaliSession labels come from SHUTTLE as the raw file_path string.
 * Extract just the filename (without extension) for display.
 */
export function sessionLabel(filePath: string): string {
  const parts = filePath.split(/[\\/]/);
  const filename = parts[parts.length - 1] ?? filePath;
  const dotIdx = filename.lastIndexOf('.');
  return dotIdx > 0 ? filename.slice(0, dotIdx) : filename;
}
