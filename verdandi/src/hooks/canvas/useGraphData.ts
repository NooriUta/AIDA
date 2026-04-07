import { useMemo } from 'react';

import { useLoomStore }                                                      from '../../stores/loomStore';
import { transformGqlOverview, transformGqlExplore, applyStmtColumns }      from '../../utils/transformGraph';
import { useOverview, useExplore, useLineage, useStmtColumns }               from '../../services/hooks';
import type { LoomNode, LoomEdge }                                            from '../../types/graph';

/**
 * Fetches all GQL data for the active view level (L1/L2/L3),
 * merges expansion data from the store, applies stmt column enrichment,
 * and returns the raw node/edge graph ready for display pipeline.
 */
export function useGraphData() {
  const {
    viewLevel,
    currentScope,
    expansionGqlNodes,
    expansionGqlEdges,
  } = useLoomStore();

  // All three are always called (Rules of Hooks); enabled flags prevent firing.
  const overviewQ = useOverview();
  const exploreQ  = useExplore(viewLevel === 'L2' ? currentScope : null);
  const lineageQ  = useLineage(viewLevel === 'L3' ? currentScope : null);

  const activeQuery = viewLevel === 'L1' ? overviewQ
                    : viewLevel === 'L2' ? exploreQ
                    : lineageQ;

  // Extract stmt/table IDs for second-pass column enrichment.
  // Include expanded nodes so their columns are fetched too.
  const stmtIds = useMemo(() => {
    if (viewLevel !== 'L2') return [] as string[];
    const ENRICHABLE = new Set(['DaliStatement', 'DaliTable']);
    const ids = new Set<string>();
    if (exploreQ.data) {
      for (const n of exploreQ.data.nodes) {
        if (ENRICHABLE.has(n.type)) ids.add(n.id);
      }
    }
    for (const n of expansionGqlNodes) {
      if (ENRICHABLE.has(n.type)) ids.add(n.id);
    }
    return [...ids];
  }, [viewLevel, exploreQ.data, expansionGqlNodes]);

  const stmtColsQ = useStmtColumns(stmtIds);

  // Transform raw GQL → RF nodes/edges, merge expansion data, apply column enrichment.
  const rawGraph = useMemo(() => {
    let base: { nodes: LoomNode[]; edges: LoomEdge[] } | null = null;
    if (viewLevel === 'L1' && overviewQ.data) base = transformGqlOverview(overviewQ.data);
    else if (viewLevel === 'L2' && exploreQ.data)  base = transformGqlExplore(exploreQ.data);
    else if (viewLevel === 'L3' && lineageQ.data)  base = transformGqlExplore(lineageQ.data);
    if (!base) return null;

    // LOOM-027: merge expansion nodes/edges (de-duplicated by id)
    if (expansionGqlNodes.length > 0 || expansionGqlEdges.length > 0) {
      const expansionGraph = transformGqlExplore({ nodes: expansionGqlNodes, edges: expansionGqlEdges });
      const existingNodeIds = new Set(base.nodes.map((n) => n.id));
      const existingEdgeIds = new Set(base.edges.map((e) => e.id));
      // L2: only allow table/statement nodes from expansion — suppress routines, packages, etc.
      const L2_ALLOWED = new Set(['tableNode', 'statementNode']);
      const allowedExpNodes = viewLevel === 'L2'
        ? expansionGraph.nodes.filter((n) => L2_ALLOWED.has(n.type))
        : expansionGraph.nodes;
      const allowedExpIds = new Set(allowedExpNodes.map((n) => n.id));
      const allowedExpEdges = viewLevel === 'L2'
        ? expansionGraph.edges.filter((e) => allowedExpIds.has(e.source) && allowedExpIds.has(e.target))
        : expansionGraph.edges;
      base = {
        nodes: [...base.nodes, ...allowedExpNodes.filter((n) => !existingNodeIds.has(n.id))],
        edges: [...base.edges, ...allowedExpEdges.filter((e) => !existingEdgeIds.has(e.id))],
      };
    }

    // Second-pass stmt column enrichment: apply before ELK so node heights are correct.
    if (viewLevel === 'L2' && stmtColsQ.data && stmtColsQ.data.edges.length > 0) {
      const { nodes: enrichedNodes, cfEdges } = applyStmtColumns(base.nodes, base.edges, stmtColsQ.data);
      base = { nodes: enrichedNodes, edges: [...base.edges, ...cfEdges] };
    }

    return base;
  }, [viewLevel, overviewQ.data, exploreQ.data, lineageQ.data, expansionGqlNodes, expansionGqlEdges, stmtColsQ.data]);

  return { rawGraph, activeQuery };
}
