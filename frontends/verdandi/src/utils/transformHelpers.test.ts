import { describe, it, expect } from 'vitest';
import {
  extractStatementType,
  extractRoutineKind,
  parseStmtLabel,
  sessionLabel,
  getEdgeStyle,
  NODE_TYPE_MAP,
  DRILLABLE_TYPES,
  SCOPE_FILTER_TYPES,
} from './transformHelpers';

// ─── extractStatementType ────────────────────────────────────────────────────

describe('extractStatementType', () => {
  it('returns the first SQL keyword found', () => {
    expect(extractStatementType('SELECT:42')).toBe('SELECT');
    expect(extractStatementType('INSERT:100')).toBe('INSERT');
    expect(extractStatementType('DELETE:687')).toBe('DELETE');
    expect(extractStatementType('MERGE INTO target')).toBe('MERGE');
  });

  it('prioritises SQ over other keywords', () => {
    expect(extractStatementType('SQ SELECT:12')).toBe('SQ');
  });

  it('normalises all CURSOR variants to CURSOR', () => {
    expect(extractStatementType('CURSOR:5')).toBe('CURSOR');
    expect(extractStatementType('DYNAMIC_CURSOR:10')).toBe('CURSOR');
    expect(extractStatementType('DINAMIC_CURSOR:3')).toBe('CURSOR');
  });

  it('is case-insensitive', () => {
    expect(extractStatementType('select:12')).toBe('SELECT');
    expect(extractStatementType('cursor:1')).toBe('CURSOR');
  });

  it('returns undefined for unrecognised labels', () => {
    expect(extractStatementType('unknown_label')).toBeUndefined();
    expect(extractStatementType('')).toBeUndefined();
  });
});

// ─── extractRoutineKind ──────────────────────────────────────────────────────

describe('extractRoutineKind', () => {
  it('returns PKG for DaliPackage regardless of label', () => {
    expect(extractRoutineKind('anything', 'DaliPackage')).toBe('PKG');
  });

  it('returns SESSION for DaliSession', () => {
    expect(extractRoutineKind('path/to/file.sql', 'DaliSession')).toBe('SESSION');
  });

  it('maps PROCEDURE to PROC', () => {
    expect(extractRoutineKind('SCHEMA.PROCEDURE:MY_PROC', 'DaliRoutine')).toBe('PROC');
  });

  it('maps FUNCTION to FUNC', () => {
    expect(extractRoutineKind('SCHEMA.FUNCTION:MY_FUNC', 'DaliRoutine')).toBe('FUNC');
  });

  it('returns ROUTINE when no keyword matches', () => {
    expect(extractRoutineKind('some_random_label', 'DaliRoutine')).toBe('ROUTINE');
  });

  it('is case-insensitive for keyword matching', () => {
    expect(extractRoutineKind('schema.procedure:name', 'DaliRoutine')).toBe('PROC');
  });
});

// ─── parseStmtLabel ──────────────────────────────────────────────────────────

describe('parseStmtLabel', () => {
  it('handles labels without colons — returns as-is', () => {
    const r = parseStmtLabel('simple_label');
    expect(r.shortLabel).toBe('simple_label');
    expect(r.groupPath).toEqual([]);
  });

  it('extracts shortLabel and groupPath from full PROCEDURE path', () => {
    const label = 'BUDM_RMS_TMD.DM_LOADER:PROCEDURE:LOAD_REG:DELETE:687';
    const r = parseStmtLabel(label);
    expect(r.shortLabel).toBe('DELETE:687');
    expect(r.groupPath).toEqual(['BUDM_RMS_TMD', 'DM_LOADER', 'LOAD_REG']);
  });

  it('extracts shortLabel and groupPath from FUNCTION path', () => {
    const label = 'SCHEMA.PKG:FUNCTION:MY_FUNC:SELECT:12';
    const r = parseStmtLabel(label);
    expect(r.shortLabel).toBe('SELECT:12');
    expect(r.groupPath).toEqual(['SCHEMA', 'PKG', 'MY_FUNC']);
  });

  it('falls back to last two parts when no PROCEDURE/FUNCTION keyword', () => {
    const label = 'A:B:C:D';
    const r = parseStmtLabel(label);
    expect(r.shortLabel).toBe('C:D');
    expect(r.groupPath).toEqual([]);
  });

  it('handles label where routine name is the last part', () => {
    const label = 'SCH.PKG:PROCEDURE:ROUTINE';
    const r = parseStmtLabel(label);
    // stmtParts is empty → falls back to full label
    expect(r.shortLabel).toBe(label);
    expect(r.groupPath).toEqual(['SCH', 'PKG', 'ROUTINE']);
  });
});

// ─── sessionLabel ────────────────────────────────────────────────────────────

describe('sessionLabel', () => {
  it('extracts filename without extension from Unix path', () => {
    expect(sessionLabel('/home/user/project/etl_load.sql')).toBe('etl_load');
  });

  it('extracts filename without extension from Windows path', () => {
    expect(sessionLabel('C:\\scripts\\daily_sync.sql')).toBe('daily_sync');
  });

  it('handles filename with no extension', () => {
    expect(sessionLabel('/scripts/my_script')).toBe('my_script');
  });

  it('returns the input as-is when there are no path separators', () => {
    expect(sessionLabel('load.sql')).toBe('load');
  });

  it('handles empty string gracefully', () => {
    expect(sessionLabel('')).toBe('');
  });
});

// ─── getEdgeStyle ────────────────────────────────────────────────────────────

describe('getEdgeStyle', () => {
  it('returns amber stroke for WRITES_TO (dashed)', () => {
    const s = getEdgeStyle('WRITES_TO');
    expect(s.stroke).toBe('#D4922A');
    expect(s.strokeDasharray).toBeTruthy();
  });

  it('returns teal stroke for READS_FROM (solid)', () => {
    const s = getEdgeStyle('READS_FROM');
    expect(s.stroke).toBe('#88B8A8');
    expect(s.strokeDasharray).toBeUndefined();
  });

  it('returns a fallback style for unknown edge types', () => {
    // @ts-expect-error — testing unknown type
    const s = getEdgeStyle('UNKNOWN_EDGE');
    expect(s.stroke).toBeDefined();
    expect(s.strokeDasharray).toBeTruthy();
  });
});

// ─── Constant shape checks ────────────────────────────────────────────────────

describe('NODE_TYPE_MAP', () => {
  it('maps every DaliNodeType to a non-empty RF node type string', () => {
    for (const [, rfType] of Object.entries(NODE_TYPE_MAP)) {
      expect(typeof rfType).toBe('string');
      expect(rfType.length).toBeGreaterThan(0);
    }
  });

  it('maps DaliDatabase to databaseNode', () => {
    expect(NODE_TYPE_MAP.DaliDatabase).toBe('databaseNode');
  });

  it('maps DaliStatement to statementNode', () => {
    expect(NODE_TYPE_MAP.DaliStatement).toBe('statementNode');
  });
});

describe('DRILLABLE_TYPES', () => {
  it('contains DaliSchema and DaliTable', () => {
    expect(DRILLABLE_TYPES.has('DaliSchema')).toBe(true);
    expect(DRILLABLE_TYPES.has('DaliTable')).toBe(true);
  });

  it('does not contain DaliApplication (uses scope filter instead)', () => {
    expect(DRILLABLE_TYPES.has('DaliApplication')).toBe(false);
  });
});

describe('SCOPE_FILTER_TYPES', () => {
  it('contains only DaliApplication', () => {
    expect(SCOPE_FILTER_TYPES.has('DaliApplication')).toBe(true);
    expect(SCOPE_FILTER_TYPES.size).toBe(1);
  });
});
