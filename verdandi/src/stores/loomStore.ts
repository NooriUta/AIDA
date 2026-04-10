import { create } from 'zustand';
import type { BreadcrumbItem, DaliNodeData, DaliNodeType, ViewLevel } from '../types/domain';

import { navigationActions } from './slices/navigationSlice';
import { l1Actions }         from './slices/l1Slice';
import { selectionActions }  from './slices/selectionSlice';
import { filterActions }     from './slices/filterSlice';
import { expansionActions }  from './slices/expansionSlice';
import { visibilityActions } from './slices/visibilitySlice';
import { viewportActions }   from './slices/viewportSlice';
import { themeActions }      from './slices/themeSlice';
import { undoActions, type UndoEntry } from './slices/undoSlice';
import { hydratePersistedState, subscribePersist } from './slices/persistSlice';

// ─── Exported types (kept here for backward-compat imports) ───────────────────

export interface ExpansionGqlNode {
  id: string; type: string; label: string; scope: string;
}
export interface ExpansionGqlEdge {
  id: string; source: string; target: string; type: string;
}

export interface FilterState {
  startObjectId:    string | null;
  startObjectType:  DaliNodeType | null;
  startObjectLabel: string | null;
  tableFilter:      string | null;
  stmtFilter:       string | null;
  fieldFilter:      string | null;
  depth:            number;
  upstream:         boolean;
  downstream:       boolean;
  tableLevelView:   boolean;
  showCfEdges:      boolean;
}

export interface L1ScopeItem {
  nodeId: string;
  label:  string;
  nodeType: DaliNodeType;
}

export interface L1FilterState {
  depth:       1 | 2 | 3 | 99;
  dirUp:       boolean;
  dirDown:     boolean;
  systemLevel: boolean;
}

export interface L1HierarchyFilter {
  dbId:     string | null;
  schemaId: string | null;
}

// ─── Store interface ──────────────────────────────────────────────────────────

export interface LoomStore {
  // View
  viewLevel:         ViewLevel;
  currentScope:      string | null;
  currentScopeLabel: string | null;
  navigationStack:   BreadcrumbItem[];

  // L1 scope & hierarchy
  l1ScopeStack:     L1ScopeItem[];
  expandedDbs:      Set<string>;
  l1Filter:         L1FilterState;
  l1HierarchyFilter: L1HierarchyFilter;
  availableApps:    { id: string; label: string }[];
  availableDbs:     { id: string; label: string; appId: string | null }[];
  availableSchemas: { id: string; label: string; dbId: string }[];

  // Selection / highlight
  selectedNodeId:   string | null;
  selectedNodeData: DaliNodeData | null;
  highlightedNodes: Set<string>;
  highlightedEdges: Set<string>;

  // Filter toolbar (L2/L3)
  filter:           FilterState;
  availableFields:  string[];
  availableTables:  { id: string; label: string }[];
  availableStmts:   { id: string; label: string; connectedTableIds: string[] }[];
  availableColumns: { id: string; name: string }[];

  // Theme & stats
  theme:      'dark' | 'light';
  palette:    string;
  nodeCount:       number;
  edgeCount:       number;
  zoom:            number;
  graphTruncated:  boolean;

  // Column-level dimming: column IDs on the lineage path (null = no dimming)
  highlightedColumns: Set<string> | null;

  // Node expansion / visibility (LOOM-026)
  nodeExpansionState: Record<string, 'collapsed' | 'partial' | 'expanded'>;
  hiddenNodeIds:      Set<string>;

  // Upstream / downstream expand (LOOM-027)
  expandRequest:          { nodeId: string; direction: 'upstream' | 'downstream' } | null;
  expandedUpstreamIds:    Set<string>;
  expandedDownstreamIds:  Set<string>;
  expansionGqlNodes:      ExpansionGqlNode[];
  expansionGqlEdges:      ExpansionGqlEdge[];

  // Undo / redo (LOOM-030)
  undoStack: UndoEntry[];
  redoStack: UndoEntry[];

  // Viewport
  fitViewRequest:     { type: 'full' } | { type: 'node'; nodeId: string } | null;
  pendingFocusNodeId: string | null;
  pendingDeepExpand:  { nodeId: string; depth: number } | null;
  deepExpandRequest:  { nodeId: string; depth: number } | null;

  // ── Actions (delegated to slice files) ────────────────────────────────────
  drillDown:         (nodeId: string, label: string, nodeType?: DaliNodeType) => void;
  jumpTo:            (level: ViewLevel, scope: string | null, label: string, nodeType?: DaliNodeType, opts?: { focusNodeId?: string; expandDepth?: number }) => void;
  navigateBack:      (index: number) => void;
  navigateToLevel:   (level: ViewLevel) => void;

  pushL1Scope:         (nodeId: string, label: string, nodeType: DaliNodeType) => void;
  popL1ScopeToIndex:   (index: number) => void;
  clearL1Scope:        () => void;
  setL1Scope:          (nodeId: string | null, label?: string) => void;
  toggleDbExpansion:   (dbId: string) => void;
  setL1Depth:          (depth: 1 | 2 | 3 | 99) => void;
  toggleL1DirUp:       () => void;
  toggleL1DirDown:     () => void;
  toggleL1SystemLevel: () => void;
  setL1HierarchyDb:       (dbId:     string | null) => void;
  setL1HierarchySchema:   (schemaId: string | null) => void;
  clearL1HierarchyFilter: () => void;
  setAvailableApps:    (apps:    { id: string; label: string }[]) => void;
  setAvailableDbs:     (dbs:     { id: string; label: string; appId: string | null }[]) => void;
  setAvailableSchemas: (schemas: { id: string; label: string; dbId: string }[]) => void;

  selectNode:    (nodeId: string | null, data?: DaliNodeData) => void;
  clearHighlight: () => void;

  setStartObject:       (nodeId: string, nodeType: DaliNodeType, label: string) => void;
  setTableFilter:       (tableId: string | null) => void;
  setStmtFilter:        (stmtId:  string | null) => void;
  setFieldFilter:       (columnName: string | null) => void;
  setDepth:             (depth: number) => void;
  setDirection:         (upstream: boolean, downstream: boolean) => void;
  toggleTableLevelView: () => void;
  toggleCfEdges:        () => void;
  clearFilter:          () => void;
  setAvailableFields:   (fields: string[]) => void;
  setAvailableTables:   (tables: { id: string; label: string }[]) => void;
  setAvailableStmts:    (stmts:  { id: string; label: string; connectedTableIds: string[] }[]) => void;
  setAvailableColumns:  (cols:   { id: string; name: string }[]) => void;

  setNodeExpansion: (nodeId: string, state: 'collapsed' | 'partial' | 'expanded') => void;
  hideNode:         (nodeId: string) => void;
  restoreNode:      (nodeId: string) => void;
  showAllNodes:     () => void;

  requestExpand:    (nodeId: string, direction: 'upstream' | 'downstream') => void;
  addExpansionData: (nodeId: string, direction: 'upstream' | 'downstream', nodes: ExpansionGqlNode[], edges: ExpansionGqlEdge[]) => void;
  clearExpandRequest: () => void;
  clearExpansion:     () => void;

  pushUndo:     (entry: UndoEntry) => void;
  undo:         () => void;
  redo:         () => void;
  clearHistory: () => void;

  toggleTheme:   () => void;
  setPalette:    (name: string) => void;
  setGraphStats:    (nodeCount: number, edgeCount: number) => void;
  setZoom:          (zoom: number) => void;
  setGraphTruncated: (truncated: boolean) => void;
  setHighlightedColumns: (cols: Set<string> | null) => void;

  requestFitView:            () => void;
  requestFocusNode:          (nodeId: string) => void;
  clearFitViewRequest:       () => void;
  clearPendingFocus:         () => void;
  clearPendingDeepExpand:    () => void;
  activatePendingDeepExpand: () => void;
  clearDeepExpandRequest:    () => void;
}

// ─── Initial filter defaults ──────────────────────────────────────────────────

export const FILTER_DEFAULTS: FilterState = {
  startObjectId:    null,
  startObjectType:  null,
  startObjectLabel: null,
  tableFilter:      null,
  stmtFilter:       null,
  fieldFilter:      null,
  depth:            5,
  upstream:         true,
  downstream:       true,
  tableLevelView:   false,
  showCfEdges:      true,
};

// ─── Store ────────────────────────────────────────────────────────────────────

// ─── Hydrate persisted filter state ───────────────────────────────────────────
const _persisted = hydratePersistedState();

export const useLoomStore = create<LoomStore>((set, get) => ({
  // ── Initial state ─────────────────────────────────────────────────────────
  viewLevel: (_persisted.viewLevel as LoomStore['viewLevel']) ?? 'L1',
  currentScope: _persisted.currentScope ?? null,
  currentScopeLabel: _persisted.currentScopeLabel ?? null,
  navigationStack: [],
  l1ScopeStack: [], expandedDbs: new Set(), l1HierarchyFilter: { dbId: null, schemaId: null },
  l1Filter: { depth: 99, dirUp: true, dirDown: true, systemLevel: false },
  availableApps: [], availableDbs: [], availableSchemas: [],
  selectedNodeId: null, selectedNodeData: null,
  highlightedNodes: new Set(), highlightedEdges: new Set(),
  filter: _persisted.filter ? { ...FILTER_DEFAULTS, ..._persisted.filter } : { ...FILTER_DEFAULTS },
  availableFields: [], availableTables: [], availableStmts: [], availableColumns: [],
  nodeExpansionState: {}, hiddenNodeIds: new Set(),
  undoStack: [], redoStack: [],
  expandRequest: null, expandedUpstreamIds: new Set(), expandedDownstreamIds: new Set(),
  expansionGqlNodes: [], expansionGqlEdges: [],
  fitViewRequest: null, pendingFocusNodeId: null, pendingDeepExpand: null, deepExpandRequest: null,
  theme: (localStorage.getItem('seer-theme') as 'dark' | 'light') ?? 'dark',
  palette: localStorage.getItem('seer-palette') ?? 'amber-forest',
  nodeCount: 0, edgeCount: 0, zoom: 1, graphTruncated: false, highlightedColumns: null,

  // ── Actions from slices ───────────────────────────────────────────────────
  ...navigationActions(set, get),
  ...l1Actions(set, get),
  ...selectionActions(set),
  ...filterActions(set),
  ...expansionActions(set),
  ...visibilityActions(set),
  ...viewportActions(set),
  ...themeActions(set, get),
  ...undoActions(set, get),
}));

// ── Subscribe to persist filter changes ──────────────────────────────────────
subscribePersist(useLoomStore);
