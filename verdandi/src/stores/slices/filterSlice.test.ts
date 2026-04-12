import { describe, it, expect } from 'vitest';
import { filterActions } from './filterSlice';

// ── Helpers ──────────────────────────────────────────────────────────────────

const INITIAL_FILTER = {
  startObjectId: null,
  startObjectType: null,
  startObjectLabel: null,
  tableFilter: null,
  stmtFilter: null,
  fieldFilter: null,
  depth: 5,
  upstream: true,
  downstream: true,
  tableLevelView: false,
  showCfEdges: true,
};

function setup(filterOverrides = {}, stateOverrides = {}) {
  let state = {
    filter: { ...INITIAL_FILTER, ...filterOverrides },
    availableColumns: [{ id: 'c1', name: 'col1' }],
    ...stateOverrides,
  } as any;
  const set = (partial: unknown) => {
    const p = typeof partial === 'function' ? partial(state) : partial;
    state = { ...state, ...p };
  };
  const actions = filterActions(set);
  return { actions, getState: () => state };
}

// ── setTableFilter ──────────────────────────────────────────────────────────

describe('setTableFilter', () => {
  it('sets tableFilter', () => {
    const { actions, getState } = setup();
    actions.setTableFilter('tbl-1');
    expect(getState().filter.tableFilter).toBe('tbl-1');
  });

  it('clears stmtFilter (cross-clearing)', () => {
    const { actions, getState } = setup({ stmtFilter: 'stmt-1' });
    actions.setTableFilter('tbl-1');
    expect(getState().filter.stmtFilter).toBeNull();
  });

  it('clears fieldFilter', () => {
    const { actions, getState } = setup({ fieldFilter: 'col_x' });
    actions.setTableFilter('tbl-1');
    expect(getState().filter.fieldFilter).toBeNull();
  });

  it('resets availableColumns to []', () => {
    const { actions, getState } = setup();
    actions.setTableFilter('tbl-1');
    expect(getState().availableColumns).toEqual([]);
  });

  it('accepts null to clear', () => {
    const { actions, getState } = setup({ tableFilter: 'tbl-1' });
    actions.setTableFilter(null);
    expect(getState().filter.tableFilter).toBeNull();
  });
});

// ── setStmtFilter ───────────────────────────────────────────────────────────

describe('setStmtFilter', () => {
  it('sets stmtFilter', () => {
    const { actions, getState } = setup();
    actions.setStmtFilter('stmt-1');
    expect(getState().filter.stmtFilter).toBe('stmt-1');
  });

  it('clears tableFilter (cross-clearing)', () => {
    const { actions, getState } = setup({ tableFilter: 'tbl-1' });
    actions.setStmtFilter('stmt-1');
    expect(getState().filter.tableFilter).toBeNull();
  });

  it('clears fieldFilter', () => {
    const { actions, getState } = setup({ fieldFilter: 'col_x' });
    actions.setStmtFilter('stmt-1');
    expect(getState().filter.fieldFilter).toBeNull();
  });

  it('resets availableColumns to []', () => {
    const { actions, getState } = setup();
    actions.setStmtFilter('stmt-1');
    expect(getState().availableColumns).toEqual([]);
  });
});

// ── setStartObject ──────────────────────────────────────────────────────────

describe('setStartObject', () => {
  it('sets id, type, label and clears fieldFilter, depth=Infinity', () => {
    const { actions, getState } = setup({ fieldFilter: 'col_x', depth: 3 });
    actions.setStartObject('n1', 'DaliTable' as any, 'my_table');
    expect(getState().filter.startObjectId).toBe('n1');
    expect(getState().filter.startObjectType).toBe('DaliTable');
    expect(getState().filter.startObjectLabel).toBe('my_table');
    expect(getState().filter.fieldFilter).toBeNull();
    expect(getState().filter.depth).toBe(Infinity);
  });
});

// ── setFieldFilter ──────────────────────────────────────────────────────────

describe('setFieldFilter', () => {
  it('sets fieldFilter', () => {
    const { actions, getState } = setup();
    actions.setFieldFilter('col_name');
    expect(getState().filter.fieldFilter).toBe('col_name');
  });
});

// ── toggleTableLevelView ────────────────────────────────────────────────────

describe('toggleTableLevelView', () => {
  it('flips tableLevelView', () => {
    const { actions, getState } = setup({ tableLevelView: false });
    actions.toggleTableLevelView();
    expect(getState().filter.tableLevelView).toBe(true);
    actions.toggleTableLevelView();
    expect(getState().filter.tableLevelView).toBe(false);
  });
});

// ── toggleMappingMode ───────────────────────────────────────────────────────

describe('toggleMappingMode', () => {
  it('flips tableLevelView AND sets showCfEdges inversely', () => {
    const { actions, getState } = setup({ tableLevelView: false, showCfEdges: true });
    actions.toggleMappingMode();
    expect(getState().filter.tableLevelView).toBe(true);
    expect(getState().filter.showCfEdges).toBe(false);
    actions.toggleMappingMode();
    expect(getState().filter.tableLevelView).toBe(false);
    expect(getState().filter.showCfEdges).toBe(true);
  });
});

// ── clearFilter ─────────────────────────────────────────────────────────────

describe('clearFilter', () => {
  it('resets all except startObject fields', () => {
    const { actions, getState } = setup({
      startObjectId: 'n1',
      startObjectType: 'DaliTable',
      startObjectLabel: 'my_table',
      tableFilter: 'tbl-1',
      stmtFilter: 'stmt-1',
      fieldFilter: 'col_x',
      depth: 3,
      upstream: false,
      downstream: false,
      tableLevelView: true,
      showCfEdges: false,
    });
    actions.clearFilter();
    const f = getState().filter;
    // Preserved
    expect(f.startObjectId).toBe('n1');
    expect(f.startObjectType).toBe('DaliTable');
    expect(f.startObjectLabel).toBe('my_table');
    // Reset to defaults
    expect(f.tableFilter).toBeNull();
    expect(f.stmtFilter).toBeNull();
    expect(f.fieldFilter).toBeNull();
    expect(f.depth).toBe(5);
    expect(f.upstream).toBe(true);
    expect(f.downstream).toBe(true);
    expect(f.tableLevelView).toBe(false);
    expect(f.showCfEdges).toBe(true);
  });
});
