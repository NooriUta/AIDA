import { describe, it, expect, vi, beforeEach } from 'vitest';
import { LAYOUT } from './constants';

// ── Mock Vite ?url import and ELK bundled ──────────────────────────────────────
vi.mock('elkjs/lib/elk-worker.min.js?url', () => ({ default: 'mock-worker-url' }));

// Minimal ELK mock: returns nodes with x/y set to 0
vi.mock('elkjs/lib/elk.bundled.js', () => ({
  default: class MockELK {
    async layout(graph: { children: { id: string }[] }) {
      return { ...graph, children: graph.children.map((n) => ({ ...n, x: 0, y: 0 })) };
    }
  },
}));

// Mock fetch used by resolveWorkerBlobUrl
global.fetch = vi.fn().mockResolvedValue({
  ok: true,
  text: async () => 'mock-worker-script',
});

// Mock URL.createObjectURL
global.URL.createObjectURL = vi.fn(() => 'blob:mock-url');

import { applyELKLayout, clearLayoutCache } from './layoutGraph';
import type { LoomNode, LoomEdge } from '../types/graph';

// ── Helpers ───────────────────────────────────────────────────────────────────

function makeNodes(count: number): LoomNode[] {
  return Array.from({ length: count }, (_, i) => ({
    id:       `n${i}`,
    type:     'tableNode',
    position: { x: 0, y: 0 },
    data:     { label: `node${i}`, nodeType: 'DaliTable', childrenAvailable: false, metadata: {} } as any,
  }));
}

function makeEdges(count: number, nodes: LoomNode[]): LoomEdge[] {
  const n = nodes.length;
  return Array.from({ length: count }, (_, i) => ({
    id:     `e${i}`,
    source: nodes[i % n].id,
    target: nodes[(i + 1) % n].id,
    data:   { edgeType: 'READS_FROM' },
  }));
}

// ── Tests ─────────────────────────────────────────────────────────────────────

describe('applyELKLayout: empty graph', () => {
  it('returns empty nodes immediately with isGrid=false, isDense=false', async () => {
    const result = await applyELKLayout([], []);
    expect(result.nodes).toHaveLength(0);
    expect(result.isGrid).toBe(false);
    expect(result.isDense).toBe(false);
  });
});

describe('HOUND 2076N: grid path', () => {
  beforeEach(() => clearLayoutCache());

  it('uses grid layout when node count > AUTO_GRID_THRESHOLD', async () => {
    const nodes = makeNodes(LAYOUT.AUTO_GRID_THRESHOLD + 1);
    const result = await applyELKLayout(nodes, []);
    expect(result.isGrid).toBe(true);
  });

  it('grid path always returns isDense=false regardless of E/V ratio', async () => {
    const nodes = makeNodes(LAYOUT.AUTO_GRID_THRESHOLD + 1);
    const edges = makeEdges(nodes.length * 10, nodes); // E/V = 10 >> DENSE_GRAPH_RATIO
    const result = await applyELKLayout(nodes, edges);
    expect(result.isGrid).toBe(true);
    expect(result.isDense).toBe(false);
  });

  it('assigns positions to all nodes in grid layout', async () => {
    const nodes = makeNodes(LAYOUT.AUTO_GRID_THRESHOLD + 1);
    const result = await applyELKLayout(nodes, []);
    expect(result.nodes.every((n) => typeof n.position.x === 'number' && typeof n.position.y === 'number')).toBe(true);
  });
});

describe('EK-01: dense graph detection', () => {
  beforeEach(() => clearLayoutCache());

  it('isDense=true when E/V > DENSE_GRAPH_RATIO', async () => {
    const nodes = makeNodes(4);
    // 4 nodes × DENSE_GRAPH_RATIO(5) + 1 = 21 edges → E/V = 5.25 > 5
    const edges = makeEdges(LAYOUT.DENSE_GRAPH_RATIO * nodes.length + 1, nodes);
    const result = await applyELKLayout(nodes, edges, { forceELK: true });
    expect(result.isDense).toBe(true);
    expect(result.isGrid).toBe(false);
  });

  it('isDense=false when E/V <= DENSE_GRAPH_RATIO', async () => {
    const nodes = makeNodes(10);
    const edges = makeEdges(10, nodes); // E/V = 1
    const result = await applyELKLayout(nodes, edges, { forceELK: true });
    expect(result.isDense).toBe(false);
    expect(result.isGrid).toBe(false);
  });

  it('isDense=false when no edges', async () => {
    const nodes = makeNodes(5);
    const result = await applyELKLayout(nodes, [], { forceELK: true });
    expect(result.isDense).toBe(false);
  });
});

describe('applyELKLayout: layout cache', () => {
  beforeEach(() => clearLayoutCache());

  it('returns cached result for identical graph fingerprint', async () => {
    const nodes = makeNodes(3);
    const edges = makeEdges(2, nodes);
    const r1 = await applyELKLayout(nodes, edges, { forceELK: true });
    const r2 = await applyELKLayout(nodes, edges, { forceELK: true });
    expect(r2.nodes).toBe(r1.nodes); // same array reference from cache
  });
});
