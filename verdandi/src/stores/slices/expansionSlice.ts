import type { ExpansionGqlEdge, ExpansionGqlNode, LoomStore } from '../loomStore';

type Set = (partial: Partial<LoomStore> | ((s: LoomStore) => Partial<LoomStore>)) => void;

export function expansionActions(set: Set) {
  return {
    requestExpand: (nodeId: string, direction: 'upstream' | 'downstream') =>
      set({ expandRequest: { nodeId, direction } }),

    addExpansionData: (
      nodeId: string,
      direction: 'upstream' | 'downstream',
      nodes: ExpansionGqlNode[],
      edges: ExpansionGqlEdge[],
    ) =>
      set((s) => {
        const nextUpIds   = new Set(s.expandedUpstreamIds);
        const nextDownIds = new Set(s.expandedDownstreamIds);
        if (direction === 'upstream') nextUpIds.add(nodeId);
        else nextDownIds.add(nodeId);

        const existingNodeIds = new Set(s.expansionGqlNodes.map((n) => n.id));
        const existingEdgeIds = new Set(s.expansionGqlEdges.map((e) => e.id));
        return {
          expandedUpstreamIds:   nextUpIds,
          expandedDownstreamIds: nextDownIds,
          expansionGqlNodes: [...s.expansionGqlNodes, ...nodes.filter((n) => !existingNodeIds.has(n.id))],
          expansionGqlEdges: [...s.expansionGqlEdges, ...edges.filter((e) => !existingEdgeIds.has(e.id))],
        };
      }),

    clearExpandRequest: () => set({ expandRequest: null }),

    clearExpansion: () => set({
      expandRequest: null,
      expandedUpstreamIds:   new Set<string>(),
      expandedDownstreamIds: new Set<string>(),
      expansionGqlNodes: [],
      expansionGqlEdges: [],
    }),
  };
}
