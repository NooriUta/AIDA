// src/utils/filterGraph.ts
// LOOM-024: L1 scope filter — BFS по рёбрам от выбранного узла

import type { LoomNode, LoomEdge } from '../types/graph';

/**
 * Возвращает подграф, достижимый из `scopeNodeId` по любым рёбрам (обе стороны).
 * Включает сам scopeNodeId + все связанные с ним узлы и рёбра.
 *
 * Используется для L1 scope filter (LOOM-024):
 *   double-click на Application → показать только его Service/DB/Schema поддерево.
 */
export function filterGraphByScope(
  nodes: LoomNode[],
  edges: LoomEdge[],
  scopeNodeId: string,
): { nodes: LoomNode[]; edges: LoomEdge[] } {
  const nodeIndex = new Map(nodes.map((n) => [n.id, n]));

  // ── BFS ──────────────────────────────────────────────────────────────────
  const visited = new Set<string>([scopeNodeId]);
  const queue: string[] = [scopeNodeId];

  while (queue.length > 0) {
    const current = queue.shift()!;
    for (const edge of edges) {
      // traverse in both directions
      if (edge.source === current && !visited.has(edge.target)) {
        visited.add(edge.target);
        queue.push(edge.target);
      }
      if (edge.target === current && !visited.has(edge.source)) {
        visited.add(edge.source);
        queue.push(edge.source);
      }
    }
  }

  const filteredNodes = nodes.filter((n) => visited.has(n.id));
  const filteredEdges = edges.filter(
    (e) => visited.has(e.source) && visited.has(e.target),
  );

  // Preserve original positions (ELK will recalculate)
  return { nodes: filteredNodes, edges: filteredEdges };
}

/**
 * Возвращает те же узлы, но с `opacity` для dim-эффекта узлов вне scope.
 * Используется когда нужно видеть «серый фон» остальных нод.
 */
export function dimNodesOutsideScope(
  nodes: LoomNode[],
  scopeNodeIds: Set<string>,
): LoomNode[] {
  return nodes.map((n) => ({
    ...n,
    style: scopeNodeIds.has(n.id) ? n.style : { ...n.style, opacity: 0.2 },
  }));
}

/**
 * Собирает Set всех ID узлов, достижимых из scopeNodeId.
 */
export function reachableNodeIds(
  edges: LoomEdge[],
  scopeNodeId: string,
): Set<string> {
  const visited = new Set<string>([scopeNodeId]);
  const queue: string[] = [scopeNodeId];

  while (queue.length > 0) {
    const current = queue.shift()!;
    for (const edge of edges) {
      if (edge.source === current && !visited.has(edge.target)) {
        visited.add(edge.target);
        queue.push(edge.target);
      }
      if (edge.target === current && !visited.has(edge.source)) {
        visited.add(edge.source);
        queue.push(edge.source);
      }
    }
  }

  return visited;
}
