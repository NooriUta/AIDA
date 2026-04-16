import { describe, it, expect } from 'vitest';
import {
  COLUMN_EDGE_TYPES,
  CF_EDGE_TYPES,
  TABLE_FLOW_TYPES,
  edgeTypeClass,
  applyL1ScopeFilter,
  applyL1DepthFilter,
  applyHiddenNodes,
  applyTableLevelView,
  applyDirectionFilter,
  applyCfEdgeToggle,
  applyL1HierarchyFilter,
  applyL1SchemaChipDim,
} from './displayPipeline';
import type { Graph } from './displayPipeline';

// ── Helpers ───────────────────────────────────────────────────────────────────

function node(id: string, type = 'tableNode', parentId?: string): any {
  return { id, type, parentId, data: { columns: ['col1'] }, style: {} };
}

function edge(source: string, target: string, edgeType: string): any {
  return { id: `${source}->${target}`, source, target, data: { edgeType } };
}

// ── COLUMN_EDGE_TYPES ─────────────────────────────────────────────────────────

describe('COLUMN_EDGE_TYPES', () => {
  it('includes HAS_COLUMN', () => expect(COLUMN_EDGE_TYPES.has('HAS_COLUMN')).toBe(true));
  it('includes ATOM_PRODUCES', () => expect(COLUMN_EDGE_TYPES.has('ATOM_PRODUCES')).toBe(true));
  it('does not include READS_FROM', () => expect(COLUMN_EDGE_TYPES.has('READS_FROM')).toBe(false));
});

// ── applyL1ScopeFilter ────────────────────────────────────────────────────────

describe('applyL1ScopeFilter', () => {
  it('returns same graph when viewLevel != L1', () => {
    const g: Graph = { nodes: [node('n1')], edges: [] };
    expect(applyL1ScopeFilter(g, 'L2', [{ nodeId: 'app1' }])).toBe(g);
  });

  it('returns same graph when stack is empty', () => {
    const g: Graph = { nodes: [node('n1')], edges: [] };
    expect(applyL1ScopeFilter(g, 'L1', [])).toBe(g);
  });

  it('dims nodes outside scope', () => {
    const g: Graph = {
      nodes: [
        node('app1', 'applicationNode'),
        node('n_other', 'l1SchemaNode'),
      ],
      edges: [],
    };
    const result = applyL1ScopeFilter(g, 'L1', [{ nodeId: 'app1' }]);
    const other = result.nodes.find((n) => n.id === 'n_other')!;
    expect(other.style?.opacity).toBe(0.15);
  });

  it('keeps scope node and its direct children visible', () => {
    const g: Graph = {
      nodes: [
        node('app1', 'applicationNode'),
        node('db1', 'databaseNode', 'app1'),
        node('schema1', 'l1SchemaNode', 'db1'),
      ],
      edges: [],
    };
    const result = applyL1ScopeFilter(g, 'L1', [{ nodeId: 'app1' }]);
    for (const n of result.nodes) {
      expect(n.style?.opacity).not.toBe(0.15);
    }
  });
});

// ── applyL1DepthFilter ────────────────────────────────────────────────────────

describe('applyL1DepthFilter', () => {
  it('no-op when viewLevel != L1', () => {
    const g: Graph = { nodes: [node('db1', 'databaseNode')], edges: [] };
    expect(applyL1DepthFilter(g, 'L2', { systemLevel: true, depth: 1 })).toBe(g);
  });

  it('hides databaseNode when systemLevel=true', () => {
    const g: Graph = { nodes: [node('db1', 'databaseNode')], edges: [] };
    const result = applyL1DepthFilter(g, 'L1', { systemLevel: true, depth: 99 });
    expect(result.nodes[0].hidden).toBe(true);
  });

  it('hides both db and schema when depth=1', () => {
    const g: Graph = {
      nodes: [node('db1', 'databaseNode'), node('s1', 'l1SchemaNode')],
      edges: [],
    };
    const result = applyL1DepthFilter(g, 'L1', { systemLevel: false, depth: 1 });
    expect(result.nodes.every((n) => n.hidden)).toBe(true);
  });

  it('hides schema but not db when depth=2', () => {
    const g: Graph = {
      nodes: [node('db1', 'databaseNode'), node('s1', 'l1SchemaNode')],
      edges: [],
    };
    const result = applyL1DepthFilter(g, 'L1', { systemLevel: false, depth: 2 });
    expect(result.nodes.find((n) => n.id === 'db1')!.hidden).toBeFalsy();
    expect(result.nodes.find((n) => n.id === 's1')!.hidden).toBe(true);
  });
});

// ── applyHiddenNodes ──────────────────────────────────────────────────────────

describe('applyHiddenNodes', () => {
  it('returns same graph when set is empty', () => {
    const g: Graph = { nodes: [node('n1')], edges: [] };
    expect(applyHiddenNodes(g, new Set())).toBe(g);
  });

  it('marks listed nodes as hidden', () => {
    const g: Graph = { nodes: [node('n1'), node('n2')], edges: [] };
    const result = applyHiddenNodes(g, new Set(['n1']));
    expect(result.nodes.find((n) => n.id === 'n1')!.hidden).toBe(true);
    expect(result.nodes.find((n) => n.id === 'n2')!.hidden).toBeFalsy();
  });
});

// ── applyTableLevelView ───────────────────────────────────────────────────────

describe('applyTableLevelView', () => {
  it('no-op when disabled', () => {
    const g: Graph = { nodes: [node('t1')], edges: [edge('t1', 't2', 'HAS_COLUMN')] };
    expect(applyTableLevelView(g, 'L2', false)).toBe(g);
  });

  it('strips columns from node data', () => {
    const g: Graph = { nodes: [node('t1')], edges: [] };
    const result = applyTableLevelView(g, 'L2', true);
    expect((result.nodes[0].data as any).columns).toEqual([]);
  });

  it('removes column-type edges', () => {
    const g: Graph = {
      nodes: [],
      edges: [
        edge('a', 'b', 'HAS_COLUMN'),
        edge('a', 'b', 'READS_FROM'),
        edge('a', 'b', 'ATOM_PRODUCES'),
      ],
    };
    const result = applyTableLevelView(g, 'L2', true);
    expect(result.edges).toHaveLength(1);
    expect(result.edges[0].data?.edgeType).toBe('READS_FROM');
  });

  it('no-op on L1 even when enabled', () => {
    const g: Graph = { nodes: [node('t1')], edges: [] };
    expect(applyTableLevelView(g, 'L1', true)).toBe(g);
  });
});

// ── applyDirectionFilter ──────────────────────────────────────────────────────

describe('applyDirectionFilter', () => {
  it('no-op when both directions enabled', () => {
    const g: Graph = { nodes: [node('t1')], edges: [edge('s', 't', 'READS_FROM')] };
    expect(applyDirectionFilter(g, 'L2', true, true)).toBe(g);
  });

  it('removes READS_FROM edges when upstream=false', () => {
    const g: Graph = {
      nodes: [node('s'), node('t')],
      edges: [edge('s', 't', 'READS_FROM'), edge('s', 't', 'WRITES_TO')],
    };
    const result = applyDirectionFilter(g, 'L2', false, true);
    expect(result.edges.map((e) => e.data?.edgeType)).not.toContain('READS_FROM');
    expect(result.edges.map((e) => e.data?.edgeType)).toContain('WRITES_TO');
  });

  it('dims nodes that lose all data-flow edges', () => {
    const g: Graph = {
      nodes: [node('s'), node('t')],
      edges: [edge('s', 't', 'READS_FROM')],
    };
    const result = applyDirectionFilter(g, 'L2', false, true);
    const s = result.nodes.find((n) => n.id === 's')!;
    const t = result.nodes.find((n) => n.id === 't')!;
    expect(s.style?.opacity).toBe(0.12);
    expect(t.style?.opacity).toBe(0.12);
  });
});

// ── applyCfEdgeToggle ─────────────────────────────────────────────────────────

describe('applyCfEdgeToggle', () => {
  it('tags CF edges with loom-cf class on L2', () => {
    const g: Graph = {
      nodes: [],
      edges: [edge('a', 'b', 'HAS_AFFECTED_COL')],
    };
    const result = applyCfEdgeToggle(g, 'L2', true, false);
    expect(result.edges[0].className).toBe('loom-cf');
  });

  it('no-op when tableLevelView=true (handled by phase 3)', () => {
    const g: Graph = { nodes: [], edges: [edge('a', 'b', 'HAS_OUTPUT_COL')] };
    expect(applyCfEdgeToggle(g, 'L2', false, true)).toBe(g);
  });

  it('tags edges with CSS classes instead of filtering', () => {
    const g: Graph = {
      nodes: [],
      edges: [
        edge('a', 'b', 'HAS_AFFECTED_COL'),
        edge('a', 'b', 'HAS_OUTPUT_COL'),
        edge('a', 'b', 'READS_FROM'),
      ],
    };
    const result = applyCfEdgeToggle(g, 'L2', false, false);
    expect(result.edges).toHaveLength(3);
    expect(result.edges[0].className).toBe('loom-cf');
    expect(result.edges[1].className).toBe('loom-cf');
    expect(result.edges[2].className).toBe('loom-flow');
  });
});

// ── applyL1HierarchyFilter ────────────────────────────────────────────────────

describe('applyL1HierarchyFilter', () => {
  it('no-op when viewLevel != L1', () => {
    const g: Graph = { nodes: [node('db1')], edges: [] };
    expect(applyL1HierarchyFilter(g, 'L2', { dbId: 'db1', schemaId: null })).toBe(g);
  });

  it('no-op when no filter set', () => {
    const g: Graph = { nodes: [node('db1')], edges: [] };
    expect(applyL1HierarchyFilter(g, 'L1', { dbId: null, schemaId: null })).toBe(g);
  });

  it('keeps only selected db and its children visible', () => {
    const g: Graph = {
      nodes: [
        node('app1', 'applicationNode'),
        node('db1', 'databaseNode', 'app1'),
        node('db2', 'databaseNode', 'app1'),
        node('s1', 'l1SchemaNode', 'db1'),
      ],
      edges: [],
    };
    const result = applyL1HierarchyFilter(g, 'L1', { dbId: 'db1', schemaId: null });
    const db2 = result.nodes.find((n) => n.id === 'db2')!;
    const db1 = result.nodes.find((n) => n.id === 'db1')!;
    expect(db2.style?.opacity).toBe(0.12);
    expect(db1.style?.opacity).not.toBe(0.12);
  });
});

// ── applyL1SchemaChipDim ──────────────────────────────────────────────────────

describe('applyL1SchemaChipDim', () => {
  it('no-op when selectedNodeId is null', () => {
    const g: Graph = { nodes: [node('s1', 'l1SchemaNode', 'db1')], edges: [] };
    expect(applyL1SchemaChipDim(g, 'L1', null, new Set(['db1']))).toBe(g);
  });

  it('no-op when selected node is not a schema chip', () => {
    const g: Graph = { nodes: [node('db1', 'databaseNode')], edges: [] };
    expect(applyL1SchemaChipDim(g, 'L1', 'db1', new Set())).toBe(g);
  });

  it('dims other schema chips in the same expanded db', () => {
    const g: Graph = {
      nodes: [
        node('s1', 'l1SchemaNode', 'db1'),
        node('s2', 'l1SchemaNode', 'db1'),
      ],
      edges: [],
    };
    const result = applyL1SchemaChipDim(g, 'L1', 's1', new Set(['db1']));
    const s1 = result.nodes.find((n) => n.id === 's1')!;
    const s2 = result.nodes.find((n) => n.id === 's2')!;
    expect(s1.style?.opacity).not.toBe(0.2);
    expect(s2.style?.opacity).toBe(0.2);
  });
});

// ── edgeTypeClass ────────────────────────────────────────────────────────────

describe('edgeTypeClass', () => {
  it('returns loom-cf for HAS_AFFECTED_COL', () => {
    expect(edgeTypeClass('HAS_AFFECTED_COL')).toBe('loom-cf');
  });

  it('returns loom-cf for HAS_OUTPUT_COL', () => {
    expect(edgeTypeClass('HAS_OUTPUT_COL')).toBe('loom-cf');
  });

  it('returns loom-flow for READS_FROM', () => {
    expect(edgeTypeClass('READS_FROM')).toBe('loom-flow');
  });

  it('returns loom-flow for WRITES_TO', () => {
    expect(edgeTypeClass('WRITES_TO')).toBe('loom-flow');
  });

  it('returns empty string for structural edge types', () => {
    expect(edgeTypeClass('HAS_COLUMN')).toBe('');
  });

  it('returns empty string for undefined', () => {
    expect(edgeTypeClass(undefined)).toBe('');
  });
});

// ── CF_EDGE_TYPES / TABLE_FLOW_TYPES ─────────────────────────────────────────

describe('CF_EDGE_TYPES', () => {
  it('has exactly 2 entries', () => {
    expect(CF_EDGE_TYPES.size).toBe(2);
  });
});

describe('TABLE_FLOW_TYPES', () => {
  it('has exactly 2 entries', () => {
    expect(TABLE_FLOW_TYPES.size).toBe(2);
  });
});
