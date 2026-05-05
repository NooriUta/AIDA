import { useMemo } from 'react';

import { useLoomStore } from '../../stores/loomStore';
import type { LoomNode, LoomEdge } from '../../types/graph';
import {
  applyL1ScopeFilter,
  applyL1DepthFilter,
  applyHiddenNodes,
  applyTableLevelView,
  applyDirectionFilter,
  applyCfEdgeToggle,
  applyL1HierarchyFilter,
  applyL1SchemaChipDim,
} from '../../utils/displayPipeline';
import { LAYOUT } from '../../utils/constants';

interface Graph {
  nodes: LoomNode[];
  edges: LoomEdge[];
}

/**
 * 9-phase display pipeline: takes rawGraph, applies scope filters, visibility
 * rules, direction filters, and dimming. Does NOT trigger ELK re-layout for
 * table/field filter changes — those are handled by useLoomLayout post-layout.
 *
 * Returns displayGraph ready to be passed to the layout hook.
 */
export function useDisplayGraph(rawGraph: Graph | null) {
  const {
    viewLevel,
    l1ScopeStack,
    expandedDbs,
    l1Filter,
    l1HierarchyFilter,
    filter,
    hiddenNodeIds,
    selectedNodeId,
  } = useLoomStore();

  // applyL1SchemaChipDim is L1-only: stabilise the dep to null on L2/L3 so that
  // clicking a node (selectNode → selectedNodeId change) does NOT produce a new
  // displayGraph reference, which would otherwise trigger a full ELK re-run and
  // the subsequent requestFitView() zoom-out (LOOM-viewport-fix).
  const selectedNodeIdForL1 = viewLevel === 'L1' ? selectedNodeId : null;

  // ── Phase 0 — L1 scope filter ─────────────────────────────────────────────
  const scopedGraph = useMemo(() => {
    if (!rawGraph) return null;
    return applyL1ScopeFilter(rawGraph, viewLevel, l1ScopeStack);
  }, [rawGraph, viewLevel, l1ScopeStack]);

  // ── Phases 1–6 ────────────────────────────────────────────────────────────
  // Phase 4 (fieldFilter) is intentionally absent — handled in post-layout
  // effect (LOOM-031) so field selection changes don't trigger ELK re-layout.
  const displayGraph = useMemo(() => {
    if (!scopedGraph) return null;

    let g = scopedGraph;
    g = applyL1DepthFilter(g, viewLevel, l1Filter);
    g = applyHiddenNodes(g, hiddenNodeIds);
    // M-5: auto-enable tableLevelView for large graphs (hides ~17K cf-edges → ~3.5K)
    const effectiveTLV = g.nodes.length > LAYOUT.TABLE_LEVEL_THRESHOLD ? true : filter.tableLevelView;
    if (effectiveTLV && viewLevel !== 'L1') {
      const withCols = g.nodes.filter((n) => n.data.columns && (n.data.columns as unknown[]).length > 0).length;
      console.info(`[LOOM] displayGraph TLV active — nodeCount=${g.nodes.length} threshold=${LAYOUT.TABLE_LEVEL_THRESHOLD} filter.tableLevelView=${filter.tableLevelView} nodesWithColumns=${withCols}`);
    }
    g = applyTableLevelView(g, viewLevel, effectiveTLV);
    g = applyDirectionFilter(g, viewLevel, filter.upstream, filter.downstream);
    g = applyCfEdgeToggle(g, viewLevel, filter.showCfEdges, effectiveTLV);
    g = applyL1HierarchyFilter(g, viewLevel, l1HierarchyFilter);
    g = applyL1SchemaChipDim(g, viewLevel, selectedNodeIdForL1, expandedDbs);
    return g;
  // filter.fieldFilter intentionally omitted — handled in the post-layout effect (LOOM-031)
  // selectedNodeId replaced by selectedNodeIdForL1 — null on L2/L3 so node clicks don't re-trigger ELK.
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [scopedGraph, viewLevel, l1Filter.systemLevel, l1Filter.depth, hiddenNodeIds, filter.tableLevelView, filter.showCfEdges, filter.upstream, filter.downstream, l1HierarchyFilter, selectedNodeIdForL1, expandedDbs]);

  return { displayGraph };
}
