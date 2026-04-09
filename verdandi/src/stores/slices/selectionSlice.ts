import type { DaliNodeData } from '../../types/domain';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type S = (p: any) => void;

export function selectionActions(set: S) {
  return {
    selectNode: (nodeId: string | null, data?: DaliNodeData) =>
      set({ selectedNodeId: nodeId, selectedNodeData: data ?? null }),

    clearHighlight: () =>
      set({ highlightedNodes: new Set<string>(), highlightedEdges: new Set<string>() }),
  };
}
