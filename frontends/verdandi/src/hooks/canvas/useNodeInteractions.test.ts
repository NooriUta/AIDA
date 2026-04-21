// @vitest-environment jsdom
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useNodeInteractions } from './useNodeInteractions';

// ── Store mock ────────────────────────────────────────────────────────────────

const mockSelectNode    = vi.fn();
const mockDrillDown     = vi.fn();
const mockJumpTo        = vi.fn();
const mockPushL1Scope   = vi.fn();
const mockSetL1Db       = vi.fn();
const mockSetL1Schema   = vi.fn();
const mockSetTableFilter= vi.fn();
const mockSetFieldFilter= vi.fn();

let mockViewLevel = 'L1';

vi.mock('../../stores/loomStore', () => ({
  useLoomStore: Object.assign(
    () => ({
      viewLevel:           mockViewLevel,
      selectNode:          mockSelectNode,
      drillDown:           mockDrillDown,
      jumpTo:              mockJumpTo,
      pushL1Scope:         mockPushL1Scope,
      setL1HierarchyDb:    mockSetL1Db,
      setL1HierarchySchema:mockSetL1Schema,
      setTableFilter:      mockSetTableFilter,
      setFieldFilter:      mockSetFieldFilter,
    }),
    { getState: () => ({ filter: { fieldFilter: null } }) }
  ),
}));

// ── Helpers ───────────────────────────────────────────────────────────────────

function makeNode(nodeType: string, type = 'default', label = 'MY_DB') {
  return {
    id: 'node-1',
    type,
    data: { nodeType, label, metadata: {} },
  } as any;
}

const fakeEvent = {} as React.MouseEvent;
const mockSetCtx = vi.fn();

beforeEach(() => {
  vi.clearAllMocks();
  mockViewLevel = 'L1';
});

// ── Tests ─────────────────────────────────────────────────────────────────────

describe('useNodeInteractions — L1 DaliDatabase click', () => {
  it('calls drillDown with db-scope when clicking databaseNode on L1', () => {
    mockViewLevel = 'L1';
    const { result } = renderHook(() => useNodeInteractions(mockSetCtx));
    const node = makeNode('DaliDatabase', 'databaseNode', 'MYDB');

    act(() => { result.current.onNodeClick(fakeEvent, node); });

    expect(mockSetTableFilter).toHaveBeenCalledWith(null);
    expect(mockDrillDown).toHaveBeenCalledWith('db-MYDB', 'MYDB', 'DaliDatabase');
    expect(mockJumpTo).not.toHaveBeenCalled();
  });
});

describe('useNodeInteractions — L2 DaliDatabase click (BUG-EK-01)', () => {
  it('calls jumpTo L2 with db-scope when clicking a DB node on L2', () => {
    mockViewLevel = 'L2';
    const { result } = renderHook(() => useNodeInteractions(mockSetCtx));
    const node = makeNode('DaliDatabase', 'databaseNode', 'ANALYTICS');

    act(() => { result.current.onNodeClick(fakeEvent, node); });

    expect(mockSetTableFilter).toHaveBeenCalledWith(null);
    expect(mockJumpTo).toHaveBeenCalledWith('L2', 'db-ANALYTICS', 'ANALYTICS', 'DaliDatabase');
    expect(mockDrillDown).not.toHaveBeenCalled();
  });

  it('calls jumpTo L2 with db-scope when clicking a DB node on L3', () => {
    mockViewLevel = 'L3';
    const { result } = renderHook(() => useNodeInteractions(mockSetCtx));
    const node = makeNode('DaliDatabase', 'databaseNode', 'STAGING');

    act(() => { result.current.onNodeClick(fakeEvent, node); });

    expect(mockJumpTo).toHaveBeenCalledWith('L2', 'db-STAGING', 'STAGING', 'DaliDatabase');
  });
});

describe('useNodeInteractions — column click', () => {
  it('calls setFieldFilter when clicking a DaliColumn on L2', () => {
    mockViewLevel = 'L2';
    const { result } = renderHook(() => useNodeInteractions(mockSetCtx));
    const node = makeNode('DaliColumn', 'columnNode', 'USER_ID');

    act(() => { result.current.onNodeClick(fakeEvent, node); });

    expect(mockSetFieldFilter).toHaveBeenCalled();
    expect(mockJumpTo).not.toHaveBeenCalled();
  });
});
