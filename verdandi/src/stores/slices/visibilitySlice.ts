import type { LoomStore } from '../loomStore';

type Set = (partial: Partial<LoomStore> | ((s: LoomStore) => Partial<LoomStore>)) => void;

export function visibilityActions(set: Set) {
  return {
    setNodeExpansion: (nodeId: string, state: 'collapsed' | 'partial' | 'expanded') =>
      set((s) => ({ nodeExpansionState: { ...s.nodeExpansionState, [nodeId]: state } })),

    hideNode: (nodeId: string) =>
      set((s) => {
        const next = new Set(s.hiddenNodeIds);
        next.add(nodeId);
        return { hiddenNodeIds: next };
      }),

    restoreNode: (nodeId: string) =>
      set((s) => {
        const next = new Set(s.hiddenNodeIds);
        next.delete(nodeId);
        return { hiddenNodeIds: next };
      }),

    showAllNodes: () => set({ hiddenNodeIds: new Set<string>() }),
  };
}
