import type { DaliNodeType, ViewLevel } from '../../types/domain';
import type { LoomStore } from '../loomStore';

type Set = (partial: Partial<LoomStore> | ((s: LoomStore) => Partial<LoomStore>)) => void;
type Get = () => LoomStore;

/** FILTER_DEFAULTS inline copy — avoids circular import with loomStore.ts */
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
      const { viewLevel, currentScope, currentScopeLabel, navigationStack } = get();
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
