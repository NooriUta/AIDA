import { describe, it, expect } from 'vitest';
import { applyStmtColumns } from './transformColumns';
import type { LoomNode, LoomEdge } from '../types/graph';
import type { ExploreResult } from '../services/lineage';

// ─── Helpers ─────────────────────────────────────────────────────────────────

function makeNode(id: string, type: string): LoomNode {
  return {
    id,
    type,
    position: { x: 0, y: 0 },
    data: { label: id, nodeType: type, columns: [] },
  } as unknown as LoomNode;
}

function makeEdge(id: string, source: string, target: string, edgeType: string): LoomEdge {
  return {
    id,
    source,
    target,
    data: { edgeType },
  } as unknown as LoomEdge;
}

function makeColResult(
  nodes: { id: string; label: string }[],
  edges: { id: string; source: string; target: string; type: string }[],
): ExploreResult {
  return {
    nodes: nodes.map((n) => ({ id: n.id, label: n.label, type: 'DaliColumn' })),
    edges,
  } as unknown as ExploreResult;
}

// ─── applyStmtColumns ────────────────────────────────────────────────────────

describe('applyStmtColumns', () => {
  it('returns nodes unchanged and no cfEdges when colResult has no edges', () => {
    const nodes  = [makeNode('t1', 'tableNode')];
    const edges: LoomEdge[] = [];
    const result = applyStmtColumns(nodes, edges, { nodes: [], edges: [] } as unknown as ExploreResult);
    expect(result.nodes).toBe(nodes);         // same reference — not copied
    expect(result.cfEdges).toHaveLength(0);
  });

  it('patches table node with HAS_COLUMN columns', () => {
    const tableNode = makeNode('table-1', 'tableNode');
    const colResult = makeColResult(
      [{ id: 'col-a', label: 'NAME' }, { id: 'col-b', label: 'AGE' }],
      [
        { id: 'e1', source: 'table-1', target: 'col-a', type: 'HAS_COLUMN' },
        { id: 'e2', source: 'table-1', target: 'col-b', type: 'HAS_COLUMN' },
      ],
    );
    const { nodes } = applyStmtColumns([tableNode], [], colResult);
    const enriched = nodes.find((n) => n.id === 'table-1')!;
    expect(enriched.data.columns).toHaveLength(2);
    expect((enriched.data.columns as { name: string }[])[0].name).toBe('NAME');
  });

  it('patches stmt node with HAS_OUTPUT_COL columns', () => {
    const stmtNode = makeNode('stmt-1', 'statementNode');
    const colResult = makeColResult(
      [{ id: 'sc-1', label: 'ORDER_ID' }],
      [{ id: 'e1', source: 'stmt-1', target: 'sc-1', type: 'HAS_OUTPUT_COL' }],
    );
    const { nodes } = applyStmtColumns([stmtNode], [], colResult);
    const enriched = nodes.find((n) => n.id === 'stmt-1')!;
    expect((enriched.data.columns as { name: string }[])[0].name).toBe('ORDER_ID');
  });

  it('builds WRITES_TO column-flow edges when stmt writes to table', () => {
    const tableNode = makeNode('table-1', 'tableNode');
    const stmtNode  = makeNode('stmt-1',  'statementNode');

    // colResult: table has col NAME, stmt has affected col NAME
    const colResult = makeColResult(
      [{ id: 'tc-name', label: 'NAME' }, { id: 'sc-name', label: 'NAME' }],
      [
        { id: 'e1', source: 'table-1', target: 'tc-name', type: 'HAS_COLUMN' },
        { id: 'e2', source: 'stmt-1',  target: 'sc-name', type: 'HAS_AFFECTED_COL' },
      ],
    );

    // baseEdges: WRITES_TO (stmt → table)
    const baseEdges = [makeEdge('w1', 'stmt-1', 'table-1', 'WRITES_TO')];

    const { cfEdges } = applyStmtColumns([tableNode, stmtNode], baseEdges, colResult);
    expect(cfEdges.length).toBeGreaterThan(0);
    const edge = cfEdges[0];
    expect(edge.source).toBe('stmt-1');
    expect(edge.target).toBe('table-1');
    expect(edge.data?.edgeType).toBe('HAS_AFFECTED_COL');
  });

  it('builds READS_FROM column-flow edges when stmt reads from table', () => {
    const tableNode = makeNode('table-1', 'tableNode');
    const stmtNode  = makeNode('stmt-1',  'statementNode');

    const colResult = makeColResult(
      [{ id: 'tc-id', label: 'ID' }, { id: 'sc-id', label: 'ID' }],
      [
        { id: 'e1', source: 'table-1', target: 'tc-id', type: 'HAS_COLUMN' },
        { id: 'e2', source: 'stmt-1',  target: 'sc-id', type: 'HAS_OUTPUT_COL' },
      ],
    );

    // READS_FROM: table → stmt
    const baseEdges = [makeEdge('r1', 'table-1', 'stmt-1', 'READS_FROM')];

    const { cfEdges } = applyStmtColumns([tableNode, stmtNode], baseEdges, colResult);
    expect(cfEdges.length).toBeGreaterThan(0);
    const edge = cfEdges[0];
    expect(edge.source).toBe('table-1');
    expect(edge.target).toBe('stmt-1');
    expect(edge.data?.edgeType).toBe('HAS_OUTPUT_COL');
  });

  it('does not create duplicate cfEdges for the same column pair', () => {
    const tableNode = makeNode('table-1', 'tableNode');
    const stmtNode  = makeNode('stmt-1',  'statementNode');

    const colResult = makeColResult(
      [{ id: 'tc-x', label: 'X' }, { id: 'sc-x', label: 'X' }],
      [
        { id: 'e1', source: 'table-1', target: 'tc-x', type: 'HAS_COLUMN' },
        { id: 'e2', source: 'stmt-1',  target: 'sc-x', type: 'HAS_AFFECTED_COL' },
      ],
    );

    const baseEdges = [
      makeEdge('w1', 'stmt-1', 'table-1', 'WRITES_TO'),
      makeEdge('w2', 'stmt-1', 'table-1', 'WRITES_TO'), // duplicate base edge
    ];

    const { cfEdges } = applyStmtColumns([tableNode, stmtNode], baseEdges, colResult);
    // Deduplication: only one cf edge per column pair
    const ids = cfEdges.map((e) => e.id);
    expect(new Set(ids).size).toBe(ids.length);
  });

  it('ignores column pairs where column names do not match', () => {
    const tableNode = makeNode('table-1', 'tableNode');
    const stmtNode  = makeNode('stmt-1',  'statementNode');

    const colResult = makeColResult(
      [{ id: 'tc-a', label: 'ALPHA' }, { id: 'sc-b', label: 'BETA' }],
      [
        { id: 'e1', source: 'table-1', target: 'tc-a', type: 'HAS_COLUMN' },
        { id: 'e2', source: 'stmt-1',  target: 'sc-b', type: 'HAS_AFFECTED_COL' },
      ],
    );

    const baseEdges = [makeEdge('w1', 'stmt-1', 'table-1', 'WRITES_TO')];
    const { cfEdges } = applyStmtColumns([tableNode, stmtNode], baseEdges, colResult);
    expect(cfEdges).toHaveLength(0);
  });

  it('skips cf edges for nodes not in the rendered graph', () => {
    // stmtNode is NOT in the nodes array — should be skipped
    const tableNode = makeNode('table-1', 'tableNode');

    const colResult = makeColResult(
      [{ id: 'tc-a', label: 'X' }, { id: 'sc-a', label: 'X' }],
      [
        { id: 'e1', source: 'table-1', target: 'tc-a', type: 'HAS_COLUMN' },
        { id: 'e2', source: 'stmt-1',  target: 'sc-a', type: 'HAS_AFFECTED_COL' },
      ],
    );
    const baseEdges = [makeEdge('w1', 'stmt-1', 'table-1', 'WRITES_TO')];

    const { cfEdges } = applyStmtColumns([tableNode], baseEdges, colResult);
    expect(cfEdges).toHaveLength(0);
  });
});
