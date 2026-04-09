// eslint-disable-next-line @typescript-eslint/no-explicit-any
type S = (p: any) => void;

export function viewportActions(set: S) {
  return {
    requestFitView:    ()         => set({ fitViewRequest: { type: 'full' } }),
    requestFocusNode:  (nodeId: string) => set({ fitViewRequest: { type: 'node', nodeId } }),
    clearFitViewRequest:       () => set({ fitViewRequest: null }),
    clearPendingFocus:         () => set({ pendingFocusNodeId: null }),
    clearPendingDeepExpand:    () => set({ pendingDeepExpand: null }),
    activatePendingDeepExpand: () =>
      set((s: any) => ({ deepExpandRequest: s.pendingDeepExpand, pendingDeepExpand: null })),
    clearDeepExpandRequest:    () => set({ deepExpandRequest: null }),
  };
}
