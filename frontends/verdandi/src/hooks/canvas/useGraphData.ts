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
  // Level → query routing:
  //   L1                              → useOverview
  //   L2                              → useRoutineAggregate (AGG mode, always)
  //   L2 with routineAggregate=false  → useExplore (manual EXP toggle from L2)
  //   L3 with routineAggregate=false  → useExplore (EXP, arrived via Routine drill-down)
  //   L3 with routineAggregate=true   → useLineage  (column lineage, direct navigation)
  //   L4                              → useStatementTree (single-statement drill)
  // Only one of the group is enabled at a time so React Query doesn't
  // pay for multiple simultaneous network calls per level switch.
  const overviewQ    = useOverview();
  const aggQ         = useRoutineAggregate(
    viewLevel === 'L2' && filter.routineAggregate ? currentScope : null,
  );
  const exploreQ     = useExplore(
    (viewLevel === 'L2' && !filter.routineAggregate) ||
    (viewLevel === 'L3' && !filter.routineAggregate)
      ? currentScope : null,
    filter.includeExternal,
  );
  const lineageQ     = useLineage(
    viewLevel === 'L3' && filter.routineAggregate ? currentScope : null,
  );
  const stmtTreeQ    = useStatementTree(viewLevel === 'L4' ? currentScope : null);

  const activeQuery = viewLevel === 'L1' ? overviewQ
                    : viewLevel === 'L2' ? (filter.routineAggregate ? aggQ : exploreQ)
                    : viewLevel === 'L3' ? (filter.routineAggregate ? lineageQ : exploreQ)
                    : viewLevel === 'L4' ? stmtTreeQ
                    : lineageQ;

  // Extract stmt/table/routine IDs for second-pass column enrichment.
  // Include expanded nodes so their columns are fetched too.
  // Phase S2.3: also pull IDs from the routine-aggregate response so any
  // DaliTable it returns gets its columns enriched the same way.
  // Phase S2.3+: L3 EXP (routineAggregate=false) also needs column enrichment.
  const needsEnrichment =
    viewLevel === 'L2' ||
    (viewLevel === 'L3' && !filter.routineAggregate);  // L3 EXP mode

  const stmtIds = useMemo(() => {
    if (!needsEnrichment) return [] as string[];
    const ENRICHABLE = new Set(['DaliStatement', 'DaliTable']);
    const ids = new Set<string>();
    // L2 AGG → aggQ, L2 EXP or L3 EXP → exploreQ
    const srcData = (viewLevel === 'L2' && filter.routineAggregate) ? aggQ.data : exploreQ.data;
    if (srcData) {
      for (const n of srcData.nodes) {
        if (ENRICHABLE.has(n.type)) ids.add(n.id);
      }
    }
    for (const n of expansionGqlNodes) {
      if (ENRICHABLE.has(n.type)) ids.add(n.id);
    }
    return [...ids];
  }, [needsEnrichment, viewLevel, filter.routineAggregate, aggQ.data, exploreQ.data, expansionGqlNodes]);

  const stmtColsQ = useStmtColumns(stmtIds);

  // Transform raw GQL → RF nodes/edges, merge expansion data, apply column enrichment.
  const rawGraph = useMemo(() => {
    let base: { nodes: LoomNode[]; edges: LoomEdge[] } | null = null;
    if (viewLevel === 'L1' && overviewQ.data) base = transformGqlOverview(overviewQ.data);
    else if (viewLevel === 'L2') {
      const l2data = filter.routineAggregate ? aggQ.data : exploreQ.data;
      if (l2data) base = transformGqlExplore(l2data);
    }
    else if (viewLevel === 'L3') {
      // L3 EXP (routineAggregate=false) → arrived by drilling from L2 AGG into a Routine
      // L3 lineage (routineAggregate=true) → arrived via direct column-lineage navigation
      const l3data = filter.routineAggregate ? lineageQ.data : exploreQ.data;
      if (l3data) base = transformGqlExplore(l3data);
    }
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
        { nodes: expansionGqlNodes, edges: expansionGqlEdges, hasMore: false },
        existingNodeIds,
      );
      // L2: only allow table/statement nodes from expansion — suppress routines, packages, etc.
      const L2_ALLOWED = new Set(['tableNode', 'statementNode']);
      const allowedExpNodes = viewLevel === 'L2'
        ? expansionGraph.nodes.filter((n) => L2_ALLOWED.has(n.type ?? ''))
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
    // Applies at L2 (both AGG and EXP) and L3 EXP (routineAggregate=false).
    if (needsEnrichment && stmtColsQ.data && stmtColsQ.data.edges.length > 0) {
      const { nodes: enrichedNodes, cfEdges } = applyStmtColumns(base.nodes, base.edges, stmtColsQ.data);
      base = { nodes: enrichedNodes, edges: [...base.edges, ...cfEdges] };
    }

    return base;
  }, [needsEnrichment, viewLevel, filter.routineAggregate, overviewQ.data, aggQ.data, exploreQ.data, lineageQ.data, stmtTreeQ.data, expansionGqlNodes, expansionGqlEdges, stmtColsQ.data]);

  // True once column enrichment has settled (single ELK run prerequisite).
  // L4 is unaffected: stmtIds is [] for L4, stmtColsQ never fires.
  // L3 lineage (routineAggregate=true) has no enrichment either.
  const stmtColsReady =
    !needsEnrichment ||
    stmtIds.length === 0 ||
    (!stmtColsQ.isLoading && !stmtColsQ.isFetching);

  return { rawGraph, activeQuery, stmtColsReady };
}
