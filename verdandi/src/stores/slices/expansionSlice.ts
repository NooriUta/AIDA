import type { ExpansionGqlEdge, ExpansionGqlNode } from '../loomStore';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type S = (p: any) => void;

export function expansionActions(set: S) {
  return {
    requestExpand: (nodeId: string, direction: 'upstream' | 'downstream') =>
      set({ expandRequest: { nodeId, direction } }),

    addExpansionData: (
      nodeId: string,
      direction: 'upstream' | 'downstream',
      nodes: ExpansionGqlNode[],
      edges: ExpansionGqlEdge[],
    ) =>
      set((s: any) => {
        const nextUpIds   = new Set(s.expandedUpstreamIds);
        const nextDownIds = new Set(s.expandedDownstreamIds);
        if (direction === 'upstream') nextUpIds.add(nodeId);
        else nextDownIds.add(nodeId);

        const existingNodeIds = new Set(s.expansionGqlNodes.map((n: any) => n.id));
        const existingEdgeIds = new Set(s.expansionGqlEdges.map((e: any) => e.id));
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
