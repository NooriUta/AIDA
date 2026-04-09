// eslint-disable-next-line @typescript-eslint/no-explicit-any
type S = (p: any) => void;

export function visibilityActions(set: S) {
  return {
    setNodeExpansion: (nodeId: string, state: 'collapsed' | 'partial' | 'expanded') =>
      set((s: any) => ({ nodeExpansionState: { ...s.nodeExpansionState, [nodeId]: state } })),

    hideNode: (nodeId: string) =>
      set((s: any) => {
        const next = new Set(s.hiddenNodeIds);
        next.add(nodeId);
        return { hiddenNodeIds: next };
      }),

    restoreNode: (nodeId: string) =>
      set((s: any) => {
        const next = new Set(s.hiddenNodeIds);
        next.delete(nodeId);
        return { hiddenNodeIds: next };
      }),

    showAllNodes: () => set({ hiddenNodeIds: new Set<string>() }),
  };
}
