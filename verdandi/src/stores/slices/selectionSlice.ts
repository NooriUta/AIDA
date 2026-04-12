import type { DaliNodeData } from '../../types/domain';
import type { LoomStore } from '../loomStore';

type Set = (partial: Partial<LoomStore> | ((s: LoomStore) => Partial<LoomStore>)) => void;

export function selectionActions(set: Set) {
  return {
    selectNode: (nodeId: string | null, data?: DaliNodeData) =>
      set({ selectedNodeId: nodeId, selectedNodeData: data ?? null }),

    clearHighlight: () =>
      set({ highlightedNodes: new Set<string>(), highlightedEdges: new Set<string>() }),
  };
}
