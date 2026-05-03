// src/utils/transformStatementTree.ts
// Phase S2.5 — L4 single-statement drill view.
//
// Transforms the ExploreResult from exploreStatementTree() into React Flow nodes + edges.
// The result contains:
//   - Root DaliStatement (self-row with NODE_ONLY edge)
//   - Child sub-statements linked via CHILD_OF (parent→child direction from backend)
//   - DaliTable nodes from READS_FROM edges
//   - DaliOutputColumn nodes from HAS_OUTPUT_COL
//   - DATA_FLOW edges between DaliOutputColumn nodes

import type { ExploreResult } from '../services/lineage';
import type { DaliNodeType, DaliEdgeType } from '../types/domain';
import type { LoomNode, LoomEdge } from '../types/graph';
import {
  NODE_TYPE_MAP,
  ANIMATED_EDGES,
  getEdgeStyle,
  extractStatementType,
  parseStmtLabel,
} from './transformHelpers';
import { TRANSFORM } from './constants';

const { EDGE_CURVATURE } = TRANSFORM;

// Edge types visible as arrows on the L4 canvas
const L4_FLOW_EDGES = new Set<string>([
  'READS_FROM',   // statement → table (flipped for display)
  'DATA_FLOW',    // outputCol → outputCol (animated)
  'FILTER_FLOW',  // column-level filter predicate
]);

// Structural edges shown as hierarchy arrows (not animated, muted)
const L4_STRUCT_EDGES = new Set<string>([
  'CHILD_OF',     // parent → child (already in display direction from backend)
]);

// Types to suppress (they are rendered as inline column rows, not standalone nodes)
const SUPPRESS_NODE_TYPES = new Set<string>([
  'DaliOutputColumn',
  'DaliAffectedColumn',
  'DaliAtom',
  'DaliColumn',
]);

export function transformGqlStatementTree(result: ExploreResult): {
  nodes: LoomNode[];
  edges: LoomEdge[];
} {
  const nodeById = new Map(result.nodes.map((n) => [n.id, n]));

  // Collect output columns per statement (rendered as inline rows)
  const outputColsByStmt = new Map<string, Array<{ id: string; name: string; type: string; isPrimaryKey: boolean; isForeignKey: boolean }>>();
  for (const e of result.edges) {
    if (e.type !== 'HAS_OUTPUT_COL' && e.type !== 'HAS_AFFECTED_COL') continue;
    const colNode = nodeById.get(e.target);
    if (!colNode) continue;
    if (!outputColsByStmt.has(e.source)) outputColsByStmt.set(e.source, []);
    const cols = outputColsByStmt.get(e.source)!;
    cols.push({ id: colNode.id, name: colNode.label, type: '', isPrimaryKey: false, isForeignKey: false });
  }

  // Build RF nodes — show statements + tables (suppress raw column nodes)
  const nodes: LoomNode[] = result.nodes
    .filter((n) => !SUPPRESS_NODE_TYPES.has(n.type))
    .map((n) => {
      const nodeType = n.type as DaliNodeType;
      const { shortLabel, groupPath } = nodeType === 'DaliStatement'
        ? parseStmtLabel(n.label)
        : { shortLabel: n.label, groupPath: [] as string[] };
      return {
        id:       n.id,
        type:     NODE_TYPE_MAP[nodeType] ?? 'statementNode',
        position: { x: 0, y: 0 },
        data: {
          label:             shortLabel,
          nodeType,
          childrenAvailable: false,
          metadata: {
            scope:     n.scope,
            groupPath: groupPath.length > 0 ? groupPath : undefined,
            fullLabel: nodeType === 'DaliStatement' ? n.label : undefined,
          },
          schema:    n.scope || undefined,
          columns:   outputColsByStmt.get(n.id),
          operation: nodeType === 'DaliStatement' ? extractStatementType(n.label) : undefined,
        },
      };
    });

  const nodeIds = new Set(nodes.map((n) => n.id));

  // Also track output column IDs for DATA_FLOW edge filtering
  const allColIds = new Set(result.nodes
    .filter((n) => n.type === 'DaliOutputColumn' || n.type === 'DaliAffectedColumn')
    .map((n) => n.id));

  const edges: LoomEdge[] = result.edges
    .filter((e) => {
      if (L4_FLOW_EDGES.has(e.type)) {
        if (e.type === 'READS_FROM') {
          return nodeIds.has(e.source) && nodeIds.has(e.target);
        }
        if (e.type === 'DATA_FLOW' || e.type === 'FILTER_FLOW') {
          // DATA_FLOW: DaliOutputColumn → DaliOutputColumn (both might be virtual)
          // Show if either endpoint is a known col or a rendered node
          return (nodeIds.has(e.source) || allColIds.has(e.source)) &&
                 (nodeIds.has(e.target) || allColIds.has(e.target));
        }
        return nodeIds.has(e.source) && nodeIds.has(e.target);
      }
      if (L4_STRUCT_EDGES.has(e.type)) {
        // CHILD_OF: backend emits as parent→child (root→sub)
        return nodeIds.has(e.source) && nodeIds.has(e.target);
      }
      return false;
    })
    .map((e) => {
      const edgeType = e.type as DaliEdgeType;
      // Sprint 1.2 inversion: READS_FROM теперь Table→Stmt в DB — flip-логика удалена.
      // Frontend использует direction из DB as-is.
      const srcH = e.sourceHandle && e.sourceHandle.length > 0 ? e.sourceHandle : undefined;
      const tgtH = e.targetHandle && e.targetHandle.length > 0 ? e.targetHandle : undefined;
      return {
        id:           e.id,
        source:       e.source,
        target:       e.target,
        sourceHandle: srcH,
        targetHandle: tgtH,
        type:         'default',
        pathOptions:  { curvature: EDGE_CURVATURE },
        animated:     ANIMATED_EDGES.has(edgeType),
        style:        L4_STRUCT_EDGES.has(e.type)
          ? { stroke: '#665c48', strokeWidth: 1, strokeDasharray: '4 2' }
          : getEdgeStyle(edgeType),
        data:         { edgeType },
      };
    });

  return { nodes, edges };
}
