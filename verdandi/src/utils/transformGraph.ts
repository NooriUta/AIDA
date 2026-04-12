// ─── transformGraph.ts — re-export barrel ────────────────────────────────────
//
// All logic has been split into focused modules:
//   transformHelpers.ts  — constants + pure helpers (NODE_TYPE_MAP, edge styles, label parsers)
//   transformExplore.ts  — L2/L3 explore transforms (transformGqlExplore, applyStmtColumns)
//   transformOverview.ts — L1 overview transform  (transformGqlOverview)
//
// Consumers can either import from this barrel or from the individual modules.

export { SCOPE_FILTER_TYPES } from './transformHelpers';
export { transformGqlExplore } from './transformExplore';
export { applyStmtColumns } from './transformColumns';
export { transformGqlOverview } from './transformOverview';

// ─── Legacy: ApiGraphResponse → RF nodes (unused, kept for reference) ────────
import type { ApiGraphResponse } from '../types/api';
import type { DaliNodeType, DaliEdgeType } from '../types/domain';
import type { LoomNode, LoomEdge } from '../types/graph';
import { NODE_TYPE_MAP, ANIMATED_EDGES, getEdgeStyle } from './transformHelpers';

export function transformGraph(response: ApiGraphResponse): {
  nodes: LoomNode[];
  edges: LoomEdge[];
} {
  const nodes: LoomNode[] = response.nodes.map((n) => ({
    id: n.id,
    type: NODE_TYPE_MAP[n.type] ?? 'schemaNode',
    position: { x: 0, y: 0 },
    data: n.data,
  }));

  const edges: LoomEdge[] = response.edges.map((e) => ({
    id: e.id,
    source: e.source,
    target: e.target,
    animated: ANIMATED_EDGES.has(e.type),
    style: getEdgeStyle(e.type),
    data: { edgeType: e.type },
  }));

  return { nodes, edges };
}
