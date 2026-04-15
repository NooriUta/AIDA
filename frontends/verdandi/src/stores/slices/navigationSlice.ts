import type { DaliNodeType, ViewLevel } from '../../types/domain';
import type { LoomStore } from '../loomStore';

type Set = (partial: Partial<LoomStore> | ((s: LoomStore) => Partial<LoomStore>)) => void;
type Get = () => LoomStore;

/** FILTER_DEFAULTS inline copy — avoids circular import with loomStore.ts.
 *  Must stay in sync with FILTER_DEFAULTS in loomStore.ts. */
const FILTER_DEFAULTS = {
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
  includeExternal:  false,
  routineAggregate: true,   // Phase S2.3: default L2 view = routine-aggregate
};

const EMPTY_EXPAND = {
  expandRequest: null,
  expandedUpstreamIds: new Set<string>(),
  expandedDownstreamIds: new Set<string>(),
  expansionGqlNodes: [] as LoomStore['expansionGqlNodes'],
  expansionGqlEdges: [] as LoomStore['expansionGqlEdges'],
};

export function navigationActions(set: Set, get: Get) {
  return {
    drillDown: (nodeId: string, label: string, nodeType?: DaliNodeType) => {
      const { viewLevel, currentScope, currentScopeLabel, navigationStack, filter } = get();

      // ── Phase S2.3: L2 aggregate + routine/package → L3 (detailed explore) ──
      // Double-clicking a RoutineNode or PackageNode on the aggregate canvas
      // transitions to L3 (EXP mode) to show all statements within that routine /
      // package. routineAggregate=false selects the explore query at L3.
      if (viewLevel === 'L2' && filter.routineAggregate &&
          (nodeType === 'DaliRoutine' || nodeType === 'DaliPackage')) {
        const scopeStr = nodeType === 'DaliPackage' ? `pkg-${label}` : nodeId;
        set({
          viewLevel:         'L3',
          currentScope:      scopeStr,
          currentScopeLabel: label,
          navigationStack:   [...navigationStack, {
            level:      viewLevel,
            scope:      currentScope,
            label:      currentScopeLabel ?? currentScope ?? viewLevel,
            fromNodeId: nodeId,
          }],
          selectedNodeId: null,
          availableFields: [],
          filter: {
            ...FILTER_DEFAULTS,
            startObjectId:    nodeId,
            startObjectType:  nodeType ?? null,
            startObjectLabel: label,
            fieldFilter:      null,
            depth:            Infinity,
            routineAggregate: false,   // L3 EXP mode — uses exploreQ
          },
          ...EMPTY_EXPAND,
        });
        return;
      }

      // ── Phase S2.5: L2 explore (manual toggle) + statement → L4 ────────────
      if (viewLevel === 'L2' && !filter.routineAggregate && nodeType === 'DaliStatement') {
        set({
          viewLevel:         'L4',
          currentScope:      nodeId,
          currentScopeLabel: label,
          navigationStack:   [...navigationStack, {
            level:      viewLevel,
            scope:      currentScope,
            label:      currentScopeLabel ?? currentScope ?? viewLevel,
            fromNodeId: nodeId,
          }],
          selectedNodeId: null,
          availableFields: [],
          filter: {
            ...FILTER_DEFAULTS,
            startObjectId:    nodeId,
            startObjectType:  nodeType ?? null,
            startObjectLabel: label,
            fieldFilter:      null,
            depth:            Infinity,
            routineAggregate: false,
          },
          ...EMPTY_EXPAND,
        });
        return;
      }

      // ── Phase S2.6: L3 EXP + statement → L4 (statement drill) ──────────────
      if (viewLevel === 'L3' && !filter.routineAggregate && nodeType === 'DaliStatement') {
        set({
          viewLevel:         'L4',
          currentScope:      nodeId,
          currentScopeLabel: label,
          navigationStack:   [...navigationStack, {
            level:      viewLevel,
            scope:      currentScope,
            label:      currentScopeLabel ?? currentScope ?? viewLevel,
            fromNodeId: nodeId,
          }],
          selectedNodeId: null,
          availableFields: [],
          filter: {
            ...FILTER_DEFAULTS,
            startObjectId:    nodeId,
            startObjectType:  nodeType ?? null,
            startObjectLabel: label,
            fieldFilter:      null,
            depth:            Infinity,
            routineAggregate: false,
          },
          ...EMPTY_EXPAND,
        });
        return;
      }

      // ── Default: L1→L2, L2→L3, L3→L3 ──────────────────────────────────────
      const nextLevel: ViewLevel = viewLevel === 'L1' ? 'L2' : 'L3';
      set({
        viewLevel: nextLevel,
        currentScope: nodeId,
        currentScopeLabel: label,
        navigationStack:
          viewLevel === 'L1'
            ? []
            : [...navigationStack, {
                level: viewLevel,
                scope: currentScope,
                label: currentScopeLabel ?? currentScope ?? viewLevel,
                fromNodeId: nodeId,
              }],
        selectedNodeId: null,
        availableFields: [],
        filter: {
          ...FILTER_DEFAULTS,
          startObjectId:    nodeId,
          startObjectType:  nodeType ?? null,
          startObjectLabel: label,
          fieldFilter:      null,
          depth:            Infinity,
        },
        ...EMPTY_EXPAND,
      });
    },

    jumpTo: (
      level: ViewLevel,
      scope: string | null,
      label: string,
      nodeType?: DaliNodeType,
      opts?: { focusNodeId?: string; expandDepth?: number },
    ) => {
      set({
        viewLevel:         level,
        currentScope:      scope,
        currentScopeLabel: label,
        navigationStack:   [],
        l1ScopeStack:      [],
        expandedDbs:       new Set<string>(),
        l1HierarchyFilter: { dbId: null, schemaId: null },
        selectedNodeId:    null,
        availableFields:   [],
        filter: {
          ...FILTER_DEFAULTS,
          startObjectId:    scope,
          startObjectType:  nodeType ?? null,
          startObjectLabel: label,
        },
        ...EMPTY_EXPAND,
        pendingFocusNodeId: opts?.focusNodeId ?? null,
        pendingDeepExpand: opts?.focusNodeId && opts?.expandDepth
          ? { nodeId: opts.focusNodeId, depth: opts.expandDepth }
          : null,
        deepExpandRequest: null,
      });
    },

    navigateBack: (index: number) => {
      const { navigationStack } = get();
      const item = navigationStack[index];
      if (!item) return;
      set({
        viewLevel:         item.level,
        currentScope:      item.scope,
        currentScopeLabel: item.label,
        navigationStack:   navigationStack.slice(0, index),
        selectedNodeId:    null,
        availableFields:   [],
        filter: {
          ...FILTER_DEFAULTS,
          startObjectId:    item.scope,
          startObjectLabel: item.label,
        },
        ...EMPTY_EXPAND,
        pendingFocusNodeId: item.fromNodeId ?? null,
      });
    },

    navigateToLevel: (level: ViewLevel) => {
      set({
        viewLevel:         level,
        currentScope:      null,
        currentScopeLabel: null,
        navigationStack:   [],
        l1ScopeStack:      [],
        expandedDbs:       new Set<string>(),
        l1Filter:          { depth: 99, dirUp: true, dirDown: true, systemLevel: false },
        l1HierarchyFilter: { dbId: null, schemaId: null },
        selectedNodeId:    null,
        availableFields:   [],
        filter:            { ...FILTER_DEFAULTS },
        ...EMPTY_EXPAND,
        fitViewRequest: level === 'L1' ? { type: 'full' } : null,
      });
    },
  };
}
