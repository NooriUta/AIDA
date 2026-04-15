/**
 * Pure display-pipeline functions for useDisplayGraph.
 *
 * Each function takes a graph + relevant state params, returns a new graph
 * (nodes/edges mutated via map/filter — no side effects).
 *
 * Extracting these here allows unit testing without React, Zustand, or ReactFlow.
 */

import type { LoomNode, LoomEdge } from '../types/graph';
import type { L1FilterState, L1HierarchyFilter } from '../stores/loomStore';

export interface Graph {
  nodes: LoomNode[];
  edges: LoomEdge[];
}

// ── Module-level constants (not inside useMemo) ───────────────────────────────

export const COLUMN_EDGE_TYPES = new Set([
  'HAS_COLUMN', 'ATOM_REF_COLUMN', 'HAS_ATOM', 'ATOM_PRODUCES',
  'HAS_AFFECTED_COL', 'HAS_OUTPUT_COL',
]);

// ── Phase 0 — L1 scope filter ─────────────────────────────────────────────────

interface L1ScopeItem { nodeId: string }

export function applyL1ScopeFilter(
  graph: Graph,
  viewLevel: string,
  l1ScopeStack: L1ScopeItem[],
): Graph {
  if (viewLevel !== 'L1' || l1ScopeStack.length === 0) return graph;

  const scopeId = l1ScopeStack[l1ScopeStack.length - 1].nodeId;

  const scopedDbIds = new Set(
    graph.nodes
      .filter((n) => n.type === 'databaseNode' && n.parentId === scopeId)
      .map((n) => n.id),
  );

  const nodes = graph.nodes.map((node) => {
    const inScope =
      node.id === scopeId ||
      node.parentId === scopeId ||
      (node.parentId != null && scopedDbIds.has(node.parentId));

    return inScope
      ? node
      : { ...node, style: { ...node.style, opacity: 0.15, pointerEvents: 'none' as const } };
  });

  return { nodes, edges: graph.edges };
}

// ── Phase 1 — L1 system-level + depth filter ─────────────────────────────────

export function applyL1DepthFilter(
  graph: Graph,
  viewLevel: string,
  l1Filter: Pick<L1FilterState, 'systemLevel' | 'depth'>,
): Graph {
  if (viewLevel !== 'L1') return graph;

  const hideDb     = l1Filter.systemLevel || l1Filter.depth === 1;
  const hideSchema = hideDb || l1Filter.depth === 2;
  if (!hideDb && !hideSchema) return graph;

  return {
    nodes: graph.nodes.map((n) => {
      if (hideDb && n.type === 'databaseNode')     return { ...n, hidden: true };
      if (hideSchema && n.type === 'l1SchemaNode') return { ...n, hidden: true };
      return n;
    }),
    edges: graph.edges,
  };
}

// ── Phase 2 — Hidden nodes (🔴 button) ────────────────────────────────────────

export function applyHiddenNodes(graph: Graph, hiddenNodeIds: Set<string>): Graph {
  if (hiddenNodeIds.size === 0) return graph;
  return {
    nodes: graph.nodes.map((n) =>
      hiddenNodeIds.has(n.id) ? { ...n, hidden: true } : n,
    ),
    edges: graph.edges,
  };
}

// ── Phase 3 — Table-level view ─────────────────────────────────────────────────

export function applyTableLevelView(
  graph: Graph,
  viewLevel: string,
  tableLevelView: boolean,
): Graph {
  if (!tableLevelView || viewLevel === 'L1') return graph;
  return {
    nodes: graph.nodes.map((n) => ({ ...n, data: { ...n.data, columns: [] } })),
    edges: graph.edges.filter((e) => !COLUMN_EDGE_TYPES.has(e.data?.edgeType as string)),
  };
}

// ── Phase 3b — Direction filter ───────────────────────────────────────────────

export function applyDirectionFilter(
  graph: Graph,
  viewLevel: string,
  upstream: boolean,
  downstream: boolean,
): Graph {
  if (viewLevel === 'L1') return graph;
  if (upstream && downstream) return graph;

  // Filter both table-level (READS_FROM/WRITES_TO) and column-level
  // (HAS_OUTPUT_COL/HAS_AFFECTED_COL) edges by direction so the toggle
  // works consistently in both mapping modes.
  const filteredEdges = graph.edges.filter((e) => {
    const et = e.data?.edgeType as string;
    if (!upstream   && (et === 'READS_FROM' || et === 'HAS_OUTPUT_COL'))   return false;
    if (!downstream && (et === 'WRITES_TO'  || et === 'HAS_AFFECTED_COL')) return false;
    return true;
  });

  const FLOW_TYPES = new Set(['READS_FROM', 'WRITES_TO', 'HAS_OUTPUT_COL', 'HAS_AFFECTED_COL']);

  const connectedIds = new Set<string>();
  for (const e of filteredEdges) {
    if (FLOW_TYPES.has(e.data?.edgeType as string)) {
      connectedIds.add(e.source);
      connectedIds.add(e.target);
    }
  }

  const hadFlow = new Set<string>();
  for (const e of graph.edges) {
    if (FLOW_TYPES.has(e.data?.edgeType as string)) {
      hadFlow.add(e.source);
      hadFlow.add(e.target);
    }
  }

  return {
    nodes: graph.nodes.map((n) =>
      hadFlow.has(n.id) && !connectedIds.has(n.id)
        ? { ...n, style: { ...n.style, opacity: 0.12, pointerEvents: 'none' as const } }
        : n,
    ),
    edges: filteredEdges,
  };
}

// ── Phase 3c — Mapping-mode edge tagging ─────────────────────────────────────
// Instead of filtering edges, we TAG them with CSS classes so that:
//   • Column mode (zoomed in): CF edges visible, table-flow edges hidden via CSS
//   • Column mode (zoomed out / compact): table-flow visible, CF edges hidden via CSS
//   • Table mode: column edges already removed by applyTableLevelView
// Edges are NOT removed because ELK needs READS_FROM / WRITES_TO for layout.

export const CF_EDGE_TYPES    = new Set(['HAS_AFFECTED_COL', 'HAS_OUTPUT_COL']);
export const TABLE_FLOW_TYPES = new Set(['READS_FROM', 'WRITES_TO']);

/** Returns the mapping-mode CSS class for an edge based on its edgeType. */
export function edgeTypeClass(edgeType: string | undefined): string {
  if (!edgeType) return '';
  if (CF_EDGE_TYPES.has(edgeType))    return 'loom-cf';
  if (TABLE_FLOW_TYPES.has(edgeType)) return 'loom-flow';
  return '';
}

export function applyCfEdgeToggle(
  graph: Graph,
  viewLevel: string,
  _showCfEdges: boolean,
  tableLevelView: boolean,
): Graph {
  if (viewLevel === 'L1' || tableLevelView) return graph;

  // If there are no column-flow (CF) edges in the graph at all, there is nothing
  // to show at fine-grain zoom — do NOT hide the table-flow edges.
  // This keeps READS_FROM / WRITES_TO arrows visible in L2 AGG mode (routine→table)
  // where column enrichment produces no CF edges (no statement columns to match).
  const hasCfEdges = graph.edges.some((e) => CF_EDGE_TYPES.has(e.data?.edgeType as string));
  if (!hasCfEdges) return graph;

  // Column mode: tag CF and flow edges with CSS classes for visibility control.
  // Do NOT filter — ELK needs READS_FROM / WRITES_TO for layered layout.
  return {
    nodes: graph.nodes,
    edges: graph.edges.map((e) => {
      const tc = edgeTypeClass(e.data?.edgeType as string);
      if (!tc) return e;
      const cls = e.className ? `${e.className} ${tc}` : tc;
      return cls !== e.className ? { ...e, className: cls } : e;
    }),
  };
}

// ── Phase 5 — L1 hierarchy filter ─────────────────────────────────────────────

export function applyL1HierarchyFilter(
  graph: Graph,
  viewLevel: string,
  l1HierarchyFilter: Pick<L1HierarchyFilter, 'dbId' | 'schemaId'>,
): Graph {
  if (viewLevel !== 'L1') return graph;

  const { dbId, schemaId } = l1HierarchyFilter;
  if (!dbId && !schemaId) return graph;

  const DIM_H = 0.12;
  const inScope = new Set<string>();

  if (schemaId) {
    inScope.add(schemaId);
    const sn = graph.nodes.find((n) => n.id === schemaId);
    if (sn?.parentId) {
      inScope.add(sn.parentId);
      const dn = graph.nodes.find((n) => n.id === sn.parentId);
      if (dn?.parentId) inScope.add(dn.parentId);
    }
  } else {
    inScope.add(dbId!);
    for (const n of graph.nodes) {
      if (n.type === 'l1SchemaNode' && n.parentId === dbId) inScope.add(n.id);
    }
    const dn = graph.nodes.find((n) => n.id === dbId);
    if (dn?.parentId) inScope.add(dn.parentId);
  }

  return {
    nodes: graph.nodes.map((n) =>
      inScope.has(n.id)
        ? n
        : { ...n, style: { ...n.style, opacity: DIM_H, pointerEvents: 'none' as const } },
    ),
    edges: graph.edges,
  };
}

// ── Phase 6 — L1 schema chip selection ───────────────────────────────────────

export function applyL1SchemaChipDim(
  graph: Graph,
  viewLevel: string,
  selectedNodeId: string | null,
  expandedDbs: Set<string>,
): Graph {
  if (viewLevel !== 'L1' || !selectedNodeId) return graph;

  const selNode = graph.nodes.find((n) => n.id === selectedNodeId);
  if (
    selNode?.type !== 'l1SchemaNode' ||
    !selNode.parentId ||
    !expandedDbs.has(selNode.parentId)
  ) {
    return graph;
  }

  const DIM_CHIP = 0.2;
  return {
    nodes: graph.nodes.map((n) => {
      if (n.type !== 'l1SchemaNode' || n.id === selectedNodeId) return n;
      return { ...n, style: { ...n.style, opacity: DIM_CHIP } };
    }),
    edges: graph.edges,
  };
}
