import type { LoomStore } from '../loomStore';

type Set = (partial: Partial<LoomStore> | ((s: LoomStore) => Partial<LoomStore>)) => void;

export function viewportActions(set: Set) {
  return {
    requestFitView:    ()         => set({ fitViewRequest: { type: 'full' } }),
    requestFocusNode:  (nodeId: string) => set({ fitViewRequest: { type: 'node', nodeId } }),
    clearFitViewRequest:       () => set({ fitViewRequest: null }),
    clearPendingFocus:         () => set({ pendingFocusNodeId: null }),
    clearPendingDeepExpand:    () => set({ pendingDeepExpand: null }),
    activatePendingDeepExpand: () =>
      set((s) => ({ deepExpandRequest: s.pendingDeepExpand, pendingDeepExpand: null })),
    clearDeepExpandRequest:    () => set({ deepExpandRequest: null }),
  };
}
