import { create } from 'zustand';
import type { BreadcrumbItem, DaliNodeType, ViewLevel } from '../types/domain';

// ─── Expansion types (LOOM-027) ───────────────────────────────────────────────
export interface ExpansionGqlNode {
  id: string; type: string; label: string; scope: string;
}
export interface ExpansionGqlEdge {
  id: string; source: string; target: string; type: string;
}

// ─── Filter Toolbar state (LOOM-023b) ────────────────────────────────────────
export interface FilterState {
  /** NodeId of the "start object" shown in the pill */
  startObjectId: string | null;
  startObjectType: DaliNodeType | null;
  startObjectLabel: string | null;
  /** Selected table node ID (null = all tables). Dims stmts not connected to this table. */
  tableFilter: string | null;
  /** Selected statement node ID (null = all stmts). Dims unrelated nodes. */
  stmtFilter: string | null;
  /** Selected column name (null = all columns) */
  fieldFilter: string | null;
  /** Traversal depth: 1–5, or Infinity */
  depth: number;
  /** Direction flags */
  upstream: boolean;
  downstream: boolean;
  /** Show only table-level graph (hide column rows + column-level edges) */
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

// ─── L1 hierarchy filter (DB → Schema cascading) ─────────────────────────────
// App-level filtering is handled by the Scope selector (l1ScopeStack).
export interface L1HierarchyFilter {
  dbId:     string | null;
  schemaId: string | null;
}

const L1_HIERARCHY_DEFAULTS: L1HierarchyFilter = {
  dbId: null, schemaId: null,
};

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
  /** Table nodes in the current L2 graph (for Table filter dropdown) */
  availableTables: { id: string; label: string }[];
  /** Statement nodes in the current L2 graph with their connected table IDs (for Stmt filter dropdown) */
  availableStmts: { id: string; label: string; connectedTableIds: string[] }[];
  /** Columns of the currently selected table or stmt — shown in the Column cascade dropdown */
  availableColumns: { id: string; name: string }[];

  // ── Theme / Palette ───────────────────────────────────────────────────────
  theme: 'dark' | 'light';
  palette: string;

  // ── Graph stats (updated by LoomCanvas after layout) ─────────────────────
  nodeCount: number;
  edgeCount: number;
  zoom: number;

  // ── Actions ───────────────────────────────────────────────────────────────
  drillDown: (nodeId: string, label: string, nodeType?: DaliNodeType) => void;
  /** Jump directly to a specific level + scope, regardless of current level (used by SearchPanel). */
  jumpTo: (level: ViewLevel, scope: string | null, label: string, nodeType?: DaliNodeType) => void;
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

  // ── Available App/DB/Schema lists for L1 filter panel ────────────────────────
  availableApps:    { id: string; label: string }[];
  availableDbs:     { id: string; label: string; appId: string | null }[];
  availableSchemas: { id: string; label: string; dbId: string }[];
  setAvailableApps:    (apps: { id: string; label: string }[]) => void;
  setAvailableDbs:     (dbs: { id: string; label: string; appId: string | null }[]) => void;
  setAvailableSchemas: (schemas: { id: string; label: string; dbId: string }[]) => void;
  /** Replace the entire L1 scope stack with a single entry, or clear it (null). */
  setL1Scope: (nodeId: string | null, label?: string) => void;

  // ── L1 hierarchy filter (DB → Schema) ────────────────────────────────────────
  l1HierarchyFilter: L1HierarchyFilter;
  setL1HierarchyDb:       (dbId:     string | null) => void;
  setL1HierarchySchema:   (schemaId: string | null) => void;
  clearL1HierarchyFilter: () => void;

  // ── Node expansion / visibility (LOOM-026) ───────────────────────────────
  nodeExpansionState: Record<string, 'collapsed' | 'partial' | 'expanded'>;
  hiddenNodeIds: Set<string>;
  setNodeExpansion: (nodeId: string, state: 'collapsed' | 'partial' | 'expanded') => void;
  hideNode: (nodeId: string) => void;
  restoreNode: (nodeId: string) => void;
  showAllNodes: () => void;

  // ── Upstream / downstream expand (LOOM-027) ───────────────────────────────
  /** Pending expand request — canvas watches and fires the query */
  expandRequest: { nodeId: string; direction: 'upstream' | 'downstream' } | null;
  expandedUpstreamIds: Set<string>;
  expandedDownstreamIds: Set<string>;
  /** Accumulated raw GQL nodes/edges from all expand operations */
  expansionGqlNodes: ExpansionGqlNode[];
  expansionGqlEdges: ExpansionGqlEdge[];
  requestExpand: (nodeId: string, direction: 'upstream' | 'downstream') => void;
  addExpansionData: (
    nodeId: string,
    direction: 'upstream' | 'downstream',
    nodes: ExpansionGqlNode[],
    edges: ExpansionGqlEdge[],
  ) => void;
  clearExpandRequest: () => void;
  clearExpansion: () => void;

  // ── Filter toolbar actions (LOOM-023b) ────────────────────────────────────
  setStartObject: (nodeId: string, nodeType: DaliNodeType, label: string) => void;
  setTableFilter: (tableId: string | null) => void;
  setStmtFilter:  (stmtId:  string | null) => void;
  setFieldFilter: (columnName: string | null) => void;
  setDepth: (depth: number) => void;
  setDirection: (upstream: boolean, downstream: boolean) => void;
  toggleTableLevelView: () => void;
  clearFilter: () => void;
  setAvailableTables:  (tables: { id: string; label: string }[]) => void;
  setAvailableStmts:   (stmts: { id: string; label: string; connectedTableIds: string[] }[]) => void;
  setAvailableColumns: (cols: { id: string; name: string }[]) => void;

  // ── Viewport focus requests (consumed by LoomCanvas after layout) ─────────
  /** Pending fitView request: full = fit all nodes; node = centre on specific node */
  fitViewRequest: { type: 'full' } | { type: 'node'; nodeId: string } | null;
  /** Trigger a full fitView (e.g. after returning to L1) */
  requestFitView: () => void;
  /** Trigger fitView centred on a specific node (e.g. after search) */
  requestFocusNode: (nodeId: string) => void;
  /** Consume (clear) the pending request after it has been executed */
  clearFitViewRequest: () => void;
}

const FILTER_DEFAULTS: FilterState = {
  startObjectId:    null,
  startObjectType:  null,
  startObjectLabel: null,
  tableFilter:      null,
  stmtFilter:       null,
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
  l1Filter: { depth: 99, dirUp: true, dirDown: true, systemLevel: false },
  l1HierarchyFilter: { ...L1_HIERARCHY_DEFAULTS },
  availableApps: [],
  availableDbs: [],
  availableSchemas: [],
  selectedNodeId: null,
  highlightedNodes: new Set<string>(),
  highlightedEdges: new Set<string>(),
  filter: { ...FILTER_DEFAULTS },
  availableFields: [],
  availableTables: [],
  availableStmts:  [],
  availableColumns: [],
  nodeExpansionState: {},
  hiddenNodeIds: new Set<string>(),
  expandRequest: null,
  expandedUpstreamIds: new Set<string>(),
  expandedDownstreamIds: new Set<string>(),
  expansionGqlNodes: [],
  expansionGqlEdges: [],
  fitViewRequest: null,
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
      navigationStack:
        viewLevel === 'L1'
          ? []
          : [...navigationStack, { level: viewLevel, scope: currentScope, label }],
      selectedNodeId: null,
      availableFields: [],
      filter: {
        ...FILTER_DEFAULTS,
        startObjectId:    nodeId,
        startObjectType:  nodeType ?? null,
        startObjectLabel: label,
      },
      // Clear expansion on navigation (LOOM-027)
      expandRequest: null,
      expandedUpstreamIds: new Set<string>(),
      expandedDownstreamIds: new Set<string>(),
      expansionGqlNodes: [],
      expansionGqlEdges: [],
    });
  },

  // ── jumpTo: direct navigation from search results (no level dependency) ───
  jumpTo: (level, scope, label, nodeType) => {
    console.log(`[LOOM] jumpTo → ${level}, scope=${scope}, label=${label}`);
    set({
      viewLevel:          level,
      currentScope:       scope,
      currentScopeLabel:  label,
      navigationStack:    [],
      l1ScopeStack:       [],
      expandedDbs:        new Set<string>(),
      l1HierarchyFilter:  { ...L1_HIERARCHY_DEFAULTS },
      selectedNodeId:     null,
      availableFields:    [],
      filter: {
        ...FILTER_DEFAULTS,
        startObjectId:    scope,
        startObjectType:  nodeType ?? null,
        startObjectLabel: label,
      },
      expandRequest: null,
      expandedUpstreamIds: new Set<string>(),
      expandedDownstreamIds: new Set<string>(),
      expansionGqlNodes: [],
      expansionGqlEdges: [],
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
      expandRequest: null,
      expandedUpstreamIds: new Set<string>(),
      expandedDownstreamIds: new Set<string>(),
      expansionGqlNodes: [],
      expansionGqlEdges: [],
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
      l1Filter: { depth: 99, dirUp: true, dirDown: true, systemLevel: false },
      l1HierarchyFilter: { ...L1_HIERARCHY_DEFAULTS },
      selectedNodeId: null,
      availableFields: [],
      filter: { ...FILTER_DEFAULTS },
      expandRequest: null,
      expandedUpstreamIds: new Set<string>(),
      expandedDownstreamIds: new Set<string>(),
      expansionGqlNodes: [],
      expansionGqlEdges: [],
      // Trigger full fitView when returning to L1
      fitViewRequest: level === 'L1' ? { type: 'full' } : null,
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

  setTableFilter: (tableId) => {
    set((s) => ({
      filter: {
        ...s.filter,
        tableFilter: tableId,
        stmtFilter:  null,   // reset stmt cascade when table changes
        fieldFilter: null,   // reset column cascade
      },
      availableColumns: [],
    }));
  },

  setStmtFilter: (stmtId) => {
    set((s) => ({
      filter: {
        ...s.filter,
        stmtFilter:  stmtId,
        fieldFilter: null,   // reset column cascade
      },
      availableColumns: [],
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

  setAvailableTables:  (tables) => set({ availableTables: tables }),
  setAvailableStmts:   (stmts)  => set({ availableStmts: stmts }),
  setAvailableColumns: (cols)   => set({ availableColumns: cols }),

  setAvailableApps:    (apps)    => set({ availableApps: apps }),
  setAvailableDbs:     (dbs)     => set({ availableDbs: dbs }),
  setAvailableSchemas: (schemas) => set({ availableSchemas: schemas }),

  // ── L1 hierarchy filter actions ───────────────────────────────────────────
  // Selecting DB resets Schema; App-level scope is handled by l1ScopeStack.
  setL1HierarchyDb: (dbId) =>
    set({ l1HierarchyFilter: { dbId, schemaId: null } }),
  setL1HierarchySchema: (schemaId) =>
    set((s) => ({ l1HierarchyFilter: { ...s.l1HierarchyFilter, schemaId } })),
  clearL1HierarchyFilter: () =>
    set({ l1HierarchyFilter: { ...L1_HIERARCHY_DEFAULTS } }),

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

  // ── Upstream / downstream expand actions (LOOM-027) ──────────────────────
  requestExpand: (nodeId, direction) => {
    set({ expandRequest: { nodeId, direction } });
  },

  addExpansionData: (nodeId, direction, nodes, edges) => {
    set((s) => {
      const nextUpIds   = new Set(s.expandedUpstreamIds);
      const nextDownIds = new Set(s.expandedDownstreamIds);
      if (direction === 'upstream')   nextUpIds.add(nodeId);
      else                            nextDownIds.add(nodeId);

      // De-duplicate by id
      const existingNodeIds = new Set(s.expansionGqlNodes.map((n) => n.id));
      const existingEdgeIds = new Set(s.expansionGqlEdges.map((e) => e.id));
      const newNodes = nodes.filter((n) => !existingNodeIds.has(n.id));
      const newEdges = edges.filter((e) => !existingEdgeIds.has(e.id));

      return {
        expandedUpstreamIds:   nextUpIds,
        expandedDownstreamIds: nextDownIds,
        expansionGqlNodes: [...s.expansionGqlNodes, ...newNodes],
        expansionGqlEdges: [...s.expansionGqlEdges, ...newEdges],
      };
    });
  },

  clearExpandRequest: () => set({ expandRequest: null }),

  clearExpansion: () => set({
    expandRequest: null,
    expandedUpstreamIds: new Set<string>(),
    expandedDownstreamIds: new Set<string>(),
    expansionGqlNodes: [],
    expansionGqlEdges: [],
  }),

  // ── Viewport focus / fitView actions ─────────────────────────────────────
  requestFitView:     ()         => set({ fitViewRequest: { type: 'full' } }),
  requestFocusNode:   (nodeId)   => set({ fitViewRequest: { type: 'node', nodeId } }),
  clearFitViewRequest: ()        => set({ fitViewRequest: null }),
}));
