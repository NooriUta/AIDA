import { create } from 'zustand';
import type { BreadcrumbItem, DaliNodeType, ViewLevel } from '../types/domain';

// ─── Filter Toolbar state (LOOM-023b) ────────────────────────────────────────
export interface FilterState {
  /** NodeId of the "start object" shown in the pill */
  startObjectId: string | null;
  startObjectType: DaliNodeType | null;
  startObjectLabel: string | null;
  /** Selected column name (null = all columns) */
  fieldFilter: string | null;
  /** Traversal depth: 1–5, or Infinity */
  depth: number;
  /** Direction flags */
  upstream: boolean;
  downstream: boolean;
  /** Show only table-level graph (hide column edges) */
  tableLevelView: boolean;
}

// ─── L1 scope filter item (LOOM-024) ─────────────────────────────────────────
export interface L1ScopeItem {
  nodeId: string;
  label: string;
  nodeType: DaliNodeType;
}

// ─── L1 toolbar display params (LOOM-024b) ───────────────────────────────────
export interface L1FilterState {
  depth:       1 | 2 | 3 | 99;  // 99 = ∞
  dirUp:       boolean;
  dirDown:     boolean;
  systemLevel: boolean;          // hide DB/Schema nodes, show only App nodes
}

interface LoomStore {
  // ── View state ────────────────────────────────────────────────────────────
  viewLevel: ViewLevel;
  currentScope: string | null;
  /** Human-readable label of the current scope (set when drilling down) */
  currentScopeLabel: string | null;
  navigationStack: BreadcrumbItem[];

  // ── L1 scope filter (LOOM-024) — double-click on App/Service narrows L1 ──
  /** Breadcrumb-like stack for L1 scope filter; does NOT change viewLevel */
  l1ScopeStack: L1ScopeItem[];

  // ── L1 expanded databases (LOOM-024 v3) ──────────────────────────────────
  /** Set of DB node IDs whose schema children are currently expanded */
  expandedDbs: Set<string>;

  // ── L1 toolbar display params (LOOM-024b) ────────────────────────────────
  l1Filter: L1FilterState;

  // ── Selection / highlight ─────────────────────────────────────────────────
  selectedNodeId: string | null;
  highlightedNodes: Set<string>;
  highlightedEdges: Set<string>;

  // ── Filter toolbar (LOOM-023b) ────────────────────────────────────────────
  filter: FilterState;
  /** Column names available for the field dropdown — populated by LoomCanvas after layout */
  availableFields: string[];

  // ── Theme / Palette ───────────────────────────────────────────────────────
  theme: 'dark' | 'light';
  palette: string;

  // ── Graph stats (updated by LoomCanvas after layout) ─────────────────────
  nodeCount: number;
  edgeCount: number;
  zoom: number;

  // ── Actions ───────────────────────────────────────────────────────────────
  drillDown: (nodeId: string, label: string, nodeType?: DaliNodeType) => void;
  navigateBack: (index: number) => void;
  navigateToLevel: (level: ViewLevel) => void;
  selectNode: (nodeId: string | null) => void;
  clearHighlight: () => void;
  setGraphStats: (nodeCount: number, edgeCount: number) => void;
  setZoom: (zoom: number) => void;
  toggleTheme: () => void;
  setPalette: (name: string) => void;
  setAvailableFields: (fields: string[]) => void;

  // ── L1 scope filter actions (LOOM-024) ───────────────────────────────────
  /** Push Application or Service onto the L1 scope stack (stays on L1) */
  pushL1Scope: (nodeId: string, label: string, nodeType: DaliNodeType) => void;
  /** Pop back to a specific index (0 = full L1 Overview) */
  popL1ScopeToIndex: (index: number) => void;
  /** Clear L1 scope (back to full Overview) */
  clearL1Scope: () => void;
  /** Toggle expanded state of a DatabaseNode's schema children */
  toggleDbExpansion: (dbId: string) => void;

  // ── L1 toolbar actions (LOOM-024b) ───────────────────────────────────────
  setL1Depth:          (depth: 1 | 2 | 3 | 99) => void;
  toggleL1DirUp:       () => void;
  toggleL1DirDown:     () => void;
  toggleL1SystemLevel: () => void;

  // ── Available app list for L1 scope selector (LOOM-024b) ─────────────────────
  availableApps: { id: string; label: string }[];
  setAvailableApps: (apps: { id: string; label: string }[]) => void;
  /** Replace the entire L1 scope stack with a single entry, or clear it (null). */
  setL1Scope: (nodeId: string | null, label?: string) => void;

  // ── Node expansion / visibility (LOOM-026) ───────────────────────────────
  nodeExpansionState: Record<string, 'collapsed' | 'partial' | 'expanded'>;
  hiddenNodeIds: Set<string>;
  setNodeExpansion: (nodeId: string, state: 'collapsed' | 'partial' | 'expanded') => void;
  hideNode: (nodeId: string) => void;
  restoreNode: (nodeId: string) => void;
  showAllNodes: () => void;

  // ── Filter toolbar actions (LOOM-023b) ────────────────────────────────────
  setStartObject: (nodeId: string, nodeType: DaliNodeType, label: string) => void;
  setFieldFilter: (columnName: string | null) => void;
  setDepth: (depth: number) => void;
  setDirection: (upstream: boolean, downstream: boolean) => void;
  toggleTableLevelView: () => void;
  clearFilter: () => void;
}

const FILTER_DEFAULTS: FilterState = {
  startObjectId:    null,
  startObjectType:  null,
  startObjectLabel: null,
  fieldFilter:      null,
  depth:            Infinity,
  upstream:         true,
  downstream:       true,
  tableLevelView:   false,
};

export const useLoomStore = create<LoomStore>((set, get) => ({
  // ── Initial state ─────────────────────────────────────────────────────────
  viewLevel: 'L1',
  currentScope: null,
  currentScopeLabel: null,
  navigationStack: [],
  l1ScopeStack: [],
  expandedDbs: new Set<string>(),
  l1Filter: { depth: 2, dirUp: true, dirDown: true, systemLevel: false },
  availableApps: [],
  selectedNodeId: null,
  highlightedNodes: new Set<string>(),
  highlightedEdges: new Set<string>(),
  filter: { ...FILTER_DEFAULTS },
  availableFields: [],
  nodeExpansionState: {},
  hiddenNodeIds: new Set<string>(),
  theme: (localStorage.getItem('seer-theme') as 'dark' | 'light') ?? 'dark',
  palette: localStorage.getItem('seer-palette') ?? 'amber-forest',
  nodeCount: 0,
  edgeCount: 0,
  zoom: 1,

  // ── drillDown: push current level onto stack, advance ────────────────────
  drillDown: (nodeId, label, nodeType) => {
    const { viewLevel, currentScope, navigationStack } = get();
    const nextLevel: ViewLevel = viewLevel === 'L1' ? 'L2' : 'L3';
    console.log(`[LOOM] drillDown → ${nextLevel}, scope=${nodeId}, label=${label}`);
    set({
      viewLevel: nextLevel,
      currentScope: nodeId,
      currentScopeLabel: label,
      // L1 → L2: the root "Overview" button already represents L1, no stack push needed.
      // L2 → L3: push the current L2 scope so the breadcrumb shows the intermediate step.
      navigationStack:
        viewLevel === 'L1'
          ? []
          : [...navigationStack, { level: viewLevel, scope: currentScope, label }],
      selectedNodeId: null,
      availableFields: [],
      // Reset filter, but set new start object
      filter: {
        ...FILTER_DEFAULTS,
        startObjectId:    nodeId,
        startObjectType:  nodeType ?? null,
        startObjectLabel: label,
      },
    });
  },

  // ── navigateBack: pop stack back to given breadcrumb index ───────────────
  navigateBack: (index) => {
    const { navigationStack } = get();
    const item = navigationStack[index];
    if (!item) return;
    console.log(`[LOOM] navigateBack → level=${item.level}, scope=${item.scope}`);
    set({
      viewLevel: item.level,
      currentScope: item.scope,
      currentScopeLabel: item.label,
      navigationStack: navigationStack.slice(0, index),
      selectedNodeId: null,
      availableFields: [],
      filter: {
        ...FILTER_DEFAULTS,
        startObjectId:    item.scope,
        startObjectLabel: item.label,
      },
    });
  },

  // ── navigateToLevel: jump directly to a level (resets scope) ─────────────
  navigateToLevel: (level) => {
    set({
      viewLevel: level,
      currentScope: null,
      currentScopeLabel: null,
      navigationStack: [],
      l1ScopeStack: [],
      expandedDbs: new Set<string>(),
      l1Filter: { depth: 2, dirUp: true, dirDown: true, systemLevel: false },
      selectedNodeId: null,
      availableFields: [],
      filter: { ...FILTER_DEFAULTS },
    });
  },

  // ── selectNode ────────────────────────────────────────────────────────────
  selectNode: (nodeId) => {
    console.log(`[LOOM] selectNode → ${nodeId}`);
    set({ selectedNodeId: nodeId });
  },

  // ── clearHighlight ────────────────────────────────────────────────────────
  clearHighlight: () => {
    set({ highlightedNodes: new Set<string>(), highlightedEdges: new Set<string>() });
  },

  // ── setGraphStats (called by canvas after render) ─────────────────────────
  setGraphStats: (nodeCount, edgeCount) => set({ nodeCount, edgeCount }),

  // ── setAvailableFields (called by canvas when column nodes are loaded) ────
  setAvailableFields: (fields) => set({ availableFields: fields }),

  // ── L1 scope filter actions (LOOM-024) ───────────────────────────────────
  pushL1Scope: (nodeId, label, nodeType) => {
    set((s) => ({
      l1ScopeStack: [...s.l1ScopeStack, { nodeId, label, nodeType }],
      selectedNodeId: null,
    }));
  },

  popL1ScopeToIndex: (index) => {
    set((s) => ({
      l1ScopeStack: s.l1ScopeStack.slice(0, index),
      selectedNodeId: null,
    }));
  },

  clearL1Scope: () => set({ l1ScopeStack: [], selectedNodeId: null }),

  setL1Scope: (nodeId, label) => {
    if (!nodeId) {
      set({ l1ScopeStack: [], selectedNodeId: null });
    } else {
      set({
        l1ScopeStack: [{ nodeId, label: label ?? nodeId, nodeType: 'DaliApplication' }],
        selectedNodeId: null,
      });
    }
  },

  toggleDbExpansion: (dbId) => {
    set((s) => {
      const next = new Set(s.expandedDbs);
      if (next.has(dbId)) next.delete(dbId);
      else next.add(dbId);
      return { expandedDbs: next };
    });
  },

  // ── setZoom (called by canvas onMoveEnd) ─────────────────────────────────
  setZoom: (zoom) => set({ zoom }),

  // ── toggleTheme ───────────────────────────────────────────────────────────
  toggleTheme: () => {
    const next = get().theme === 'dark' ? 'light' : 'dark';
    localStorage.setItem('seer-theme', next);
    document.documentElement.setAttribute('data-theme', next);
    set({ theme: next });
  },

  // ── setPalette ────────────────────────────────────────────────────────────
  setPalette: (name) => {
    localStorage.setItem('seer-palette', name);
    if (name === 'amber-forest') {
      document.documentElement.removeAttribute('data-palette');
    } else {
      document.documentElement.setAttribute('data-palette', name);
    }
    set({ palette: name });
  },

  // ── Filter toolbar actions (LOOM-023b) ────────────────────────────────────
  setStartObject: (nodeId, nodeType, label) => {
    set((s) => ({
      filter: {
        ...s.filter,
        startObjectId:    nodeId,
        startObjectType:  nodeType,
        startObjectLabel: label,
        fieldFilter:      null,   // reset field when start object changes
        depth:            Infinity,
      },
    }));
  },

  setFieldFilter: (columnName) => {
    set((s) => ({ filter: { ...s.filter, fieldFilter: columnName } }));
  },

  setDepth: (depth) => {
    set((s) => ({ filter: { ...s.filter, depth } }));
  },

  setDirection: (upstream, downstream) => {
    set((s) => ({ filter: { ...s.filter, upstream, downstream } }));
  },

  toggleTableLevelView: () => {
    set((s) => ({
      filter: { ...s.filter, tableLevelView: !s.filter.tableLevelView },
    }));
  },

  clearFilter: () => {
    set((s) => ({
      filter: {
        ...FILTER_DEFAULTS,
        // preserve start object — only reset field/depth/direction
        startObjectId:    s.filter.startObjectId,
        startObjectType:  s.filter.startObjectType,
        startObjectLabel: s.filter.startObjectLabel,
      },
    }));
  },

  setAvailableApps: (apps) => set({ availableApps: apps }),

  // ── Node expansion / visibility (LOOM-026) ───────────────────────────────
  setNodeExpansion: (nodeId, state) => {
    set((s) => ({ nodeExpansionState: { ...s.nodeExpansionState, [nodeId]: state } }));
  },

  hideNode: (nodeId) => {
    set((s) => {
      const next = new Set(s.hiddenNodeIds);
      next.add(nodeId);
      return { hiddenNodeIds: next };
    });
  },

  restoreNode: (nodeId) => {
    set((s) => {
      const next = new Set(s.hiddenNodeIds);
      next.delete(nodeId);
      return { hiddenNodeIds: next };
    });
  },

  showAllNodes: () => set({ hiddenNodeIds: new Set<string>() }),

  // ── L1 toolbar actions (LOOM-024b) ────────────────────────────────────────
  setL1Depth:          (depth)  => set((s) => ({ l1Filter: { ...s.l1Filter, depth } })),
  toggleL1DirUp:       ()       => set((s) => ({ l1Filter: { ...s.l1Filter, dirUp: !s.l1Filter.dirUp } })),
  toggleL1DirDown:     ()       => set((s) => ({ l1Filter: { ...s.l1Filter, dirDown: !s.l1Filter.dirDown } })),
  toggleL1SystemLevel: ()       => set((s) => ({ l1Filter: { ...s.l1Filter, systemLevel: !s.l1Filter.systemLevel } })),
}));
