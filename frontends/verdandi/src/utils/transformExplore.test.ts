import { describe, it, expect } from 'vitest';
import { transformGqlExplore } from './transformExplore';
import type { ExploreResult } from '../services/lineage';

// ── Minimal builders ──────────────────────────────────────────────────────────

function gNode(id: string, type: string, label = id, scope = ''): ExploreResult['nodes'][0] {
  return { id, type, label, scope };
}

function gEdge(id: string, source: string, target: string, type: string): ExploreResult['edges'][0] {
  return { id, source, target, type };
}

function mkResult(nodes: ExploreResult['nodes'], edges: ExploreResult['edges']): ExploreResult {
  return { nodes, edges, hasMore: false };
}

// ── transformGqlExplore — flat (pkg / lineage) path ───────────────────────────

describe('transformGqlExplore — flat path', () => {
  it('returns empty nodes/edges for empty result', () => {
    const r = transformGqlExplore(mkResult([], []));
    expect(r.nodes).toHaveLength(0);
    expect(r.edges).toHaveLength(0);
  });

  it('maps DaliTable → tableNode with correct data', () => {
    const result = mkResult(
      [gNode('t1', 'DaliTable', 'ORDERS')],
      [],
    );
    const { nodes } = transformGqlExplore(result);
    expect(nodes).toHaveLength(1);
    expect(nodes[0].type).toBe('tableNode');
    expect(nodes[0].data.label).toBe('ORDERS');
    expect(nodes[0].data.nodeType).toBe('DaliTable');
  });

  it('maps DaliRoutine → routineNode', () => {
    const result = mkResult(
      [gNode('r1', 'DaliRoutine', 'MY_PROC')],
      [],
    );
    const { nodes } = transformGqlExplore(result);
    expect(nodes[0].type).toBe('routineNode');
  });

  it('maps DaliStatement → statementNode with childrenAvailable=true', () => {
    const result = mkResult(
      [gNode('s1', 'DaliStatement', 'SCHEMA.PKG:PROCEDURE:PROC:SELECT:12')],
      [],
    );
    const { nodes } = transformGqlExplore(result);
    expect(nodes[0].type).toBe('statementNode');
    expect(nodes[0].data.childrenAvailable).toBe(true);
  });

  it('strips DaliColumn nodes from the output', () => {
    const result = mkResult(
      [
        gNode('t1', 'DaliTable', 'ORDERS'),
        gNode('c1', 'DaliColumn', 'ID'),
      ],
      [gEdge('e1', 't1', 'c1', 'HAS_COLUMN')],
    );
    const { nodes } = transformGqlExplore(result);
    expect(nodes.every((n) => n.data.nodeType !== 'DaliColumn')).toBe(true);
  });

  it('strips DaliOutputColumn nodes from the output', () => {
    const result = mkResult(
      [
        gNode('s1', 'DaliStatement', 'STMT'),
        gNode('oc1', 'DaliOutputColumn', 'COL_OUT'),
      ],
      [gEdge('e1', 's1', 'oc1', 'HAS_OUTPUT_COL')],
    );
    const { nodes } = transformGqlExplore(result);
    expect(nodes.every((n) => n.data.nodeType !== 'DaliOutputColumn')).toBe(true);
  });

  it('builds READS_FROM edge between table and statement', () => {
    const result = mkResult(
      [
        gNode('t1', 'DaliTable', 'ORDERS'),
        gNode('s1', 'DaliStatement', 'STMT'),
      ],
      [gEdge('e1', 't1', 's1', 'READS_FROM')],
    );
    const { edges } = transformGqlExplore(result);
    expect(edges.some((e) => e.data?.edgeType === 'READS_FROM')).toBe(true);
  });

  it('builds WRITES_TO edge between statement and table', () => {
    const result = mkResult(
      [
        gNode('s1', 'DaliStatement', 'STMT'),
        gNode('t1', 'DaliTable', 'ORDERS'),
      ],
      [gEdge('e1', 's1', 't1', 'WRITES_TO')],
    );
    const { edges } = transformGqlExplore(result);
    expect(edges.some((e) => e.data?.edgeType === 'WRITES_TO')).toBe(true);
  });

  it('suppresses CONTAINS_STMT edges (structural — not rendered)', () => {
    const result = mkResult(
      [
        gNode('r1', 'DaliRoutine', 'PROC'),
        gNode('s1', 'DaliStatement', 'STMT'),
      ],
      [gEdge('e1', 'r1', 's1', 'CONTAINS_STMT')],
    );
    const { edges } = transformGqlExplore(result);
    expect(edges.every((e) => e.data?.edgeType !== 'CONTAINS_STMT')).toBe(true);
  });

  it('inlines up to L2_MAX_COLS columns per table', () => {
    const cols = Array.from({ length: 8 }, (_, i) => gNode(`c${i}`, 'DaliColumn', `COL_${i}`));
    const colEdges = cols.map((c, i) =>
      gEdge(`e${i}`, 't1', c.id, 'HAS_COLUMN'),
    );
    const result = mkResult(
      [gNode('t1', 'DaliTable', 'ORDERS'), ...cols],
      colEdges,
    );
    const { nodes } = transformGqlExplore(result);
    const tableNode = nodes.find((n) => n.id === 't1')!;
    // Max inline = 5 (L2_MAX_COLS)
    expect((tableNode.data.columns as unknown[]).length).toBeLessThanOrEqual(5);
  });

  it('filters edges whose source or target is not in the node set', () => {
    // Only t1 is in the result, t2 is not
    const result = mkResult(
      [gNode('t1', 'DaliTable', 'A')],
      [gEdge('e1', 't1', 't2', 'READS_FROM')],
    );
    const { edges } = transformGqlExplore(result);
    // t2 is not in nodes — edge should be dropped
    expect(edges.every((e) => e.source !== 't2' && e.target !== 't2')).toBe(true);
  });

  it('externalNodeIds allows edges to external nodes through', () => {
    // t2 is external (not in result.nodes) but in externalNodeIds
    const result = mkResult(
      [gNode('t1', 'DaliTable', 'A')],
      [gEdge('e1', 't1', 't2', 'READS_FROM')],
    );
    const { edges } = transformGqlExplore(result, new Set(['t2']));
    // Sprint 1.2 inversion: READS_FROM direction is canonical (Table→Stmt) — no flip.
    // Fixture: source=t1 (Table), target=t2 (Stmt-like external) — passes through unchanged.
    expect(edges.some((e) => e.source === 't1' && e.target === 't2')).toBe(true);
  });
});

// ── transformGqlExplore — schema path ─────────────────────────────────────────

describe('transformGqlExplore — schema path', () => {
  it('detects schema explore and routes to schema transform', () => {
    const result = mkResult(
      [
        gNode('schema1', 'DaliSchema', 'MY_SCHEMA'),
        gNode('t1', 'DaliTable', 'ORDERS'),
      ],
      [gEdge('e1', 'schema1', 't1', 'CONTAINS_TABLE')],
    );
    const { nodes } = transformGqlExplore(result);
    // Schema transform groups tables under a schema group node
    expect(nodes.length).toBeGreaterThan(0);
  });

  it('includes tables connected via READS_FROM to schema statements', () => {
    const result = mkResult(
      [
        gNode('schema1', 'DaliSchema', 'MY_SCHEMA'),
        gNode('t1', 'DaliTable', 'ORDERS'),
        gNode('s1', 'DaliStatement', 'STMT'),
      ],
      [
        gEdge('e1', 'schema1', 't1', 'CONTAINS_TABLE'),
        gEdge('e2', 's1', 't1', 'WRITES_TO'),
      ],
    );
    const { nodes } = transformGqlExplore(result);
    expect(nodes.some((n) => n.id === 's1' || n.data.label === 'STMT')).toBe(true);
  });
});

// ── subquery hoisting ─────────────────────────────────────────────────────────

describe('transformGqlExplore — subquery hoisting', () => {
  it('removes subquery statement nodes from output', () => {
    const result = mkResult(
      [
        gNode('root', 'DaliStatement', 'ROOT_STMT'),
        gNode('sub1', 'DaliStatement', 'SUB_STMT'),
        gNode('t1', 'DaliTable', 'SRC'),
      ],
      [
        gEdge('e1', 'root', 'sub1', 'USES_SUBQUERY'),
        gEdge('e2', 'sub1', 't1', 'READS_FROM'),
      ],
    );
    const { nodes } = transformGqlExplore(result);
    // sub1 should be hidden (subquery) — root remains
    expect(nodes.some((n) => n.id === 'root')).toBe(true);
    expect(nodes.every((n) => n.id !== 'sub1')).toBe(true);
  });

  it('hoists subquery READS_FROM as synthetic edge from root statement', () => {
    const result = mkResult(
      [
        gNode('root', 'DaliStatement', 'ROOT'),
        gNode('sub1', 'DaliStatement', 'SUB'),
        gNode('t1', 'DaliTable', 'SRC'),
      ],
      [
        gEdge('e1', 'root', 'sub1', 'USES_SUBQUERY'),
        gEdge('e2', 'sub1', 't1', 'READS_FROM'),
      ],
    );
    const { edges } = transformGqlExplore(result);
    // A synthetic READS_FROM edge from t1 to root should be created
    const synthetic = edges.filter((e) => e.data?.edgeType === 'READS_FROM');
    expect(synthetic.length).toBeGreaterThan(0);
  });
});
