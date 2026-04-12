import type { LoomStore } from '../loomStore';

type Set = (partial: Partial<LoomStore> | ((s: LoomStore) => Partial<LoomStore>)) => void;
type Get = () => LoomStore;

export function visibilityActions(set: Set, _get?: Get) {
  return {
    setNodeExpansion: (nodeId: string, state: 'collapsed' | 'partial' | 'expanded') =>
      set((s) => ({ nodeExpansionState: { ...s.nodeExpansionState, [nodeId]: state } })),

    hideNode: (nodeId: string) =>
      set((s) => {
        // Push undo entry
        const undoStack = [...s.undoStack, { type: 'hide' as const, nodeId }];
        const next = new Set(s.hiddenNodeIds);
        next.add(nodeId);
        return { hiddenNodeIds: next, undoStack, redoStack: [] };
      }),

    restoreNode: (nodeId: string) =>
      set((s) => {
        // Push undo entry
        const undoStack = [...s.undoStack, { type: 'restore' as const, nodeId }];
        const next = new Set(s.hiddenNodeIds);
        next.delete(nodeId);
        return { hiddenNodeIds: next, undoStack, redoStack: [] };
      }),

    showAllNodes: () =>
      set((s) => {
        // Push undo entry with previous state
        const previousHidden = [...s.hiddenNodeIds];
        const undoStack = previousHidden.length > 0
          ? [...s.undoStack, { type: 'showAll' as const, previousHidden }]
          : s.undoStack;
        return { hiddenNodeIds: new Set<string>(), undoStack, redoStack: [] };
      }),
  };
}
