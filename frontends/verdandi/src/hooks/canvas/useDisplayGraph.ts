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
    g = applyTableLevelView(g, viewLevel, effectiveTLV);
    g = applyDirectionFilter(g, viewLevel, filter.upstream, filter.downstream);
    g = applyCfEdgeToggle(g, viewLevel, filter.showCfEdges, effectiveTLV);
    g = applyL1HierarchyFilter(g, viewLevel, l1HierarchyFilter);
    g = applyL1SchemaChipDim(g, viewLevel, selectedNodeId, expandedDbs);
    return g;
  // filter.fieldFilter intentionally omitted — handled in the post-layout effect (LOOM-031)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [scopedGraph, viewLevel, l1Filter.systemLevel, l1Filter.depth, hiddenNodeIds, filter.tableLevelView, filter.showCfEdges, filter.upstream, filter.downstream, l1HierarchyFilter, selectedNodeId, expandedDbs]);

  return { displayGraph };
}
