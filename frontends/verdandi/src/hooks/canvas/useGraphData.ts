import { useMemo } from 'react';

import { useLoomStore }                                                                        from '../../stores/loomStore';
import { transformGqlOverview, transformGqlExplore, applyStmtColumns }                        from '../../utils/transformGraph';
import { transformGqlStatementTree }                                                           from '../../utils/transformStatementTree';
import { useOverview, useExplore, useRoutineAggregate, useLineage, useStmtColumns, useStatementTree } from '../../services/hooks';
import type { LoomNode, LoomEdge }                                                             from '../../types/graph';

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
    filter,
  } = useLoomStore();

  // Always call every hook (Rules of Hooks); enabled flags prevent firing.
  // Phase S2.3: L2 now has two modes depending on filter.routineAggregate.
  //   routineAggregate=true  → useRoutineAggregate (new routines+tables view)
  //   routineAggregate=false → useExplore (legacy detailed exploration)
  // Phase S2.5: L4 → useStatementTree (single-statement subquery drill)
  // Only one of the group is enabled at a time so React Query doesn't
  // pay for multiple simultaneous network calls per level switch.
  const overviewQ    = useOverview();
  const aggQ         = useRoutineAggregate(
    viewLevel === 'L2' && filter.routineAggregate ? currentScope : null,
  );
  const exploreQ     = useExplore(
    viewLevel === 'L2' && !filter.routineAggregate ? currentScope : null,
    filter.includeExternal,
  );
  const lineageQ     = useLineage(viewLevel === 'L3' ? currentScope : null);
  const stmtTreeQ    = useStatementTree(viewLevel === 'L4' ? currentScope : null);

  const activeQuery = viewLevel === 'L1' ? overviewQ
                    : viewLevel === 'L2' ? (filter.routineAggregate ? aggQ : exploreQ)
                    : viewLevel === 'L4' ? stmtTreeQ
                    : lineageQ;

  // Extract stmt/table/routine IDs for second-pass column enrichment.
  // Include expanded nodes so their columns are fetched too.
  // Phase S2.3: also pull IDs from the routine-aggregate response so any
  // DaliTable it returns gets its columns enriched the same way.
  const stmtIds = useMemo(() => {
    if (viewLevel !== 'L2') return [] as string[];
    const ENRICHABLE = new Set(['DaliStatement', 'DaliTable']);
    const ids = new Set<string>();
    const srcData = filter.routineAggregate ? aggQ.data : exploreQ.data;
    if (srcData) {
      for (const n of srcData.nodes) {
        if (ENRICHABLE.has(n.type)) ids.add(n.id);
      }
    }
    for (const n of expansionGqlNodes) {
      if (ENRICHABLE.has(n.type)) ids.add(n.id);
    }
    return [...ids];
  }, [viewLevel, filter.routineAggregate, aggQ.data, exploreQ.data, expansionGqlNodes]);

  const stmtColsQ = useStmtColumns(stmtIds);

  // Transform raw GQL → RF nodes/edges, merge expansion data, apply column enrichment.
  const rawGraph = useMemo(() => {
    let base: { nodes: LoomNode[]; edges: LoomEdge[] } | null = null;
    if (viewLevel === 'L1' && overviewQ.data) base = transformGqlOverview(overviewQ.data);
    else if (viewLevel === 'L2') {
      const l2data = filter.routineAggregate ? aggQ.data : exploreQ.data;
      if (l2data) base = transformGqlExplore(l2data);
    }
    else if (viewLevel === 'L3' && lineageQ.data)  base = transformGqlExplore(lineageQ.data);
    else if (viewLevel === 'L4' && stmtTreeQ.data) base = transformGqlStatementTree(stmtTreeQ.data);
    if (!base) return null;

    // LOOM-027: merge expansion nodes/edges (de-duplicated by id)
    if (expansionGqlNodes.length > 0 || expansionGqlEdges.length > 0) {
      // Compute existingNodeIds BEFORE transformGqlExplore so we can pass them
      // as externalNodeIds — edges connecting new expansion nodes to the starting
      // node (which the backend does NOT return) are otherwise dropped inside
      // transformGqlExplore's nodeIds filter.
      const existingNodeIds = new Set(base.nodes.map((n) => n.id));
      const existingEdgeIds = new Set(base.edges.map((e) => e.id));
      const expansionGraph = transformGqlExplore(
        { nodes: expansionGqlNodes, edges: expansionGqlEdges },
        existingNodeIds,
      );
      // L2: only allow table/statement nodes from expansion — suppress routines, packages, etc.
      const L2_ALLOWED = new Set(['tableNode', 'statementNode']);
      const allowedExpNodes = viewLevel === 'L2'
        ? expansionGraph.nodes.filter((n) => L2_ALLOWED.has(n.type))
        : expansionGraph.nodes;
      const allowedExpIds = new Set(allowedExpNodes.map((n) => n.id));
      const allowedExpEdges = viewLevel === 'L2'
        ? expansionGraph.edges.filter((e) => {
            // An edge is allowed when both endpoints are "reachable" — either a new expansion
            // node (allowedExpIds) or a node already present in the current graph (existingNodeIds).
            // The starting node of an upstream/downstream expand is NOT returned by the backend
            // but its edges (e.g. READS_FROM source=INSERT:4343) must still be admitted so the
            // newly fetched upstream tables connect to their consuming statement.
            const srcOk = allowedExpIds.has(e.source) || existingNodeIds.has(e.source);
            const tgtOk = allowedExpIds.has(e.target) || existingNodeIds.has(e.target);
            return srcOk && tgtOk;
          })
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
  }, [viewLevel, filter.routineAggregate, overviewQ.data, aggQ.data, exploreQ.data, lineageQ.data, stmtTreeQ.data, expansionGqlNodes, expansionGqlEdges, stmtColsQ.data]);

  // True once column enrichment has settled (single ELK run prerequisite).
  // L4 is unaffected: stmtIds is [] for L4, stmtColsQ never fires.
  const stmtColsReady =
    viewLevel !== 'L2' ||
    stmtIds.length === 0 ||
    (!stmtColsQ.isLoading && !stmtColsQ.isFetching);

  return { rawGraph, activeQuery, stmtColsReady };
}
