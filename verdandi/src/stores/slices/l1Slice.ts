import type { DaliNodeType } from '../../types/domain';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type S = (p: any) => void;
// eslint-disable-next-line @typescript-eslint/no-explicit-any
type G = () => any;

export function l1Actions(set: S, get: G) {
  void get; // available for future use
  return {
    // ── L1 scope stack ────────────────────────────────────────────────────────
    pushL1Scope: (nodeId: string, label: string, nodeType: DaliNodeType) =>
      set((s: any) => ({
        l1ScopeStack: [...s.l1ScopeStack, { nodeId, label, nodeType }],
        selectedNodeId: null,
      })),

    popL1ScopeToIndex: (index: number) =>
      set((s: any) => ({
        l1ScopeStack: s.l1ScopeStack.slice(0, index),
        selectedNodeId: null,
      })),

    clearL1Scope: () => set({ l1ScopeStack: [], selectedNodeId: null }),

    setL1Scope: (nodeId: string | null, label?: string) => {
      if (!nodeId) {
        set({ l1ScopeStack: [], selectedNodeId: null });
      } else {
        set({
          l1ScopeStack: [{ nodeId, label: label ?? nodeId, nodeType: 'DaliApplication' }],
          selectedNodeId: null,
        });
      }
    },

    toggleDbExpansion: (dbId: string) =>
      set((s: any) => {
        const next = new Set(s.expandedDbs);
        if (next.has(dbId)) next.delete(dbId); else next.add(dbId);
        return { expandedDbs: next };
      }),

    // ── L1 toolbar (LOOM-024b) ────────────────────────────────────────────────
    setL1Depth: (depth: 1 | 2 | 3 | 99) =>
      set((s: any) => ({ l1Filter: { ...s.l1Filter, depth } })),
    toggleL1DirUp: () =>
      set((s: any) => ({ l1Filter: { ...s.l1Filter, dirUp: !s.l1Filter.dirUp } })),
    toggleL1DirDown: () =>
      set((s: any) => ({ l1Filter: { ...s.l1Filter, dirDown: !s.l1Filter.dirDown } })),
    toggleL1SystemLevel: () =>
      set((s: any) => ({ l1Filter: { ...s.l1Filter, systemLevel: !s.l1Filter.systemLevel } })),

    // ── L1 hierarchy filter ───────────────────────────────────────────────────
    setL1HierarchyDb:     (dbId: string | null) =>
      set({ l1HierarchyFilter: { dbId, schemaId: null } }),
    setL1HierarchySchema: (schemaId: string | null) =>
      set((s: any) => ({ l1HierarchyFilter: { ...s.l1HierarchyFilter, schemaId } })),
    clearL1HierarchyFilter: () =>
      set({ l1HierarchyFilter: { dbId: null, schemaId: null } }),

    // ── Available App/DB/Schema lists ─────────────────────────────────────────
    setAvailableApps:    (apps: any[])     => set({ availableApps: apps }),
    setAvailableDbs:     (dbs: any[])      => set({ availableDbs: dbs }),
    setAvailableSchemas: (schemas: any[])  => set({ availableSchemas: schemas }),
  };
}
