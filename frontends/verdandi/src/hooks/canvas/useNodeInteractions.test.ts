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

describe('useNodeInteractions — L1 schema node click', () => {
  it('calls setL1HierarchyDb + setL1HierarchySchema for l1SchemaNode with parentId', () => {
    mockViewLevel = 'L1';
    const { result } = renderHook(() => useNodeInteractions(mockSetCtx));
    const node = {
      id: 'schema-1',
      type: 'l1SchemaNode',
      parentId: 'db-parent',
      data: { nodeType: 'DaliSchema', label: 'MY_SCHEMA', metadata: {} },
    } as any;

    act(() => { result.current.onNodeClick(fakeEvent, node); });

    expect(mockSetL1Db).toHaveBeenCalledWith('db-parent');
    expect(mockSetL1Schema).toHaveBeenCalledWith('schema-1');
    expect(mockDrillDown).not.toHaveBeenCalled();
  });
});

describe('useNodeInteractions — onNodeDoubleClick', () => {
  it('calls pushL1Scope for DaliApplication on L1', () => {
    mockViewLevel = 'L1';
    const { result } = renderHook(() => useNodeInteractions(mockSetCtx));
    const node = makeNode('DaliApplication', 'appNode', 'MY_APP');

    act(() => { result.current.onNodeDoubleClick(fakeEvent, node); });

    expect(mockPushL1Scope).toHaveBeenCalledWith('node-1', 'MY_APP', 'DaliApplication');
  });

  it('returns early on L3 when nodeType is not DaliStatement', () => {
    mockViewLevel = 'L3';
    const { result } = renderHook(() => useNodeInteractions(mockSetCtx));
    const node = makeNode('DaliColumn', 'columnNode', 'COL');

    act(() => { result.current.onNodeDoubleClick(fakeEvent, node); });

    expect(mockDrillDown).not.toHaveBeenCalled();
  });

  it('drills to table by nodeId on L2 for DaliTable', () => {
    mockViewLevel = 'L2';
    const { result } = renderHook(() => useNodeInteractions(mockSetCtx));
    const node = makeNode('DaliTable', 'tableNode', 'ORDERS');

    act(() => { result.current.onNodeDoubleClick(fakeEvent, node); });

    expect(mockSetTableFilter).toHaveBeenCalledWith(null);
    expect(mockDrillDown).toHaveBeenCalledWith('node-1', 'ORDERS', 'DaliTable');
  });

  it('drills to schema with db qualifier when metadata.databaseName present', () => {
    mockViewLevel = 'L2';
    const { result } = renderHook(() => useNodeInteractions(mockSetCtx));
    const node = {
      id: 'node-1',
      type: 'schemaNode',
      data: { nodeType: 'DaliSchema', label: 'PUBLIC', metadata: { databaseName: 'PROD' } },
    } as any;

    act(() => { result.current.onNodeDoubleClick(fakeEvent, node); });

    expect(mockDrillDown).toHaveBeenCalledWith('schema-PUBLIC|PROD', 'PUBLIC', 'DaliSchema');
  });

  it('drills to routine with routine-<id> scope', () => {
    mockViewLevel = 'L2';
    const { result } = renderHook(() => useNodeInteractions(mockSetCtx));
    const node = makeNode('DaliRoutine', 'routineNode', 'PKG.PROC');

    act(() => { result.current.onNodeDoubleClick(fakeEvent, node); });

    expect(mockDrillDown).toHaveBeenCalledWith('routine-node-1', 'PKG.PROC', 'DaliRoutine');
  });

  it('skips drillDown for unrecognised nodeType on L2', () => {
    mockViewLevel = 'L2';
    const { result } = renderHook(() => useNodeInteractions(mockSetCtx));
    const node = makeNode('DaliColumn', 'columnNode', 'IRRELEVANT');

    act(() => { result.current.onNodeDoubleClick(fakeEvent, node); });

    expect(mockDrillDown).not.toHaveBeenCalled();
  });
});

describe('useNodeInteractions — onNodeContextMenu', () => {
  it('calls setContextMenu with node info and pointer coords', () => {
    mockViewLevel = 'L2';
    const { result } = renderHook(() => useNodeInteractions(mockSetCtx));
    const node = makeNode('DaliTable', 'tableNode', 'T1');
    const evt = { preventDefault: vi.fn(), clientX: 100, clientY: 200 } as any;

    act(() => { result.current.onNodeContextMenu(evt, node); });

    expect(evt.preventDefault).toHaveBeenCalled();
    expect(mockSetCtx).toHaveBeenCalledWith({
      nodeId: 'node-1',
      data: node.data,
      x: 100,
      y: 200,
    });
  });
});
