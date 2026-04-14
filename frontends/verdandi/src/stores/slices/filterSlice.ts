import type { DaliNodeType } from '../../types/domain';
import type { LoomStore } from '../loomStore';

type Set = (partial: Partial<LoomStore> | ((s: LoomStore) => Partial<LoomStore>)) => void;

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
  // When true, the L2 explore query also fetches READS_FROM / WRITES_TO
  // edges whose table endpoint is in a DIFFERENT schema (external sources).
  // Default off — the canvas stays scoped to the current schema unless the
  // user explicitly opts in to seeing cross-schema lineage.
  includeExternal:  false,
};

export function filterActions(set: Set) {
  return {
    setStartObject: (nodeId: string, nodeType: DaliNodeType, label: string) =>
      set((s) => ({
        filter: {
          ...s.filter,
          startObjectId:    nodeId,
          startObjectType:  nodeType,
          startObjectLabel: label,
          fieldFilter:      null,
          depth:            Infinity,
        },
      })),

    setTableFilter: (tableId: string | null) =>
      set((s) => ({
        filter: { ...s.filter, tableFilter: tableId, stmtFilter: null, fieldFilter: null },
        availableColumns: [],
      })),

    setStmtFilter: (stmtId: string | null) =>
      set((s) => ({
        filter: { ...s.filter, stmtFilter: stmtId, tableFilter: null, fieldFilter: null },
        availableColumns: [],
      })),

    setFieldFilter:         (columnName: string | null) =>
      set((s) => ({ filter: { ...s.filter, fieldFilter: columnName } })),
    setDepth:               (depth: number) =>
      set((s) => ({ filter: { ...s.filter, depth } })),
    setDirection:           (upstream: boolean, downstream: boolean) =>
      set((s) => ({ filter: { ...s.filter, upstream, downstream } })),
    toggleTableLevelView:   () =>
      set((s) => ({ filter: { ...s.filter, tableLevelView: !s.filter.tableLevelView } })),
    toggleIncludeExternal:  () =>
      set((s) => ({ filter: { ...s.filter, includeExternal: !s.filter.includeExternal } })),
    toggleCfEdges:          () =>
      set((s) => ({ filter: { ...s.filter, showCfEdges: !s.filter.showCfEdges } })),
    /** Switch between column mapping (cf edges + column level) and table mapping */
    toggleMappingMode:      () =>
      set((s) => {
        const toTable = !s.filter.tableLevelView;
        return { filter: { ...s.filter, tableLevelView: toTable, showCfEdges: !toTable } };
      }),

    clearFilter: () =>
      set((s) => ({
        filter: {
          ...FILTER_DEFAULTS,
          startObjectId:    s.filter.startObjectId,
          startObjectType:  s.filter.startObjectType,
          startObjectLabel: s.filter.startObjectLabel,
        },
      })),

    setAvailableFields:  (fields: string[])                                              => set({ availableFields: fields }),
    setAvailableTables:  (tables: { id: string; label: string }[])                       => set({ availableTables: tables }),
    setAvailableStmts:   (stmts:  { id: string; label: string; connectedTableIds: string[] }[]) => set({ availableStmts: stmts }),
    setAvailableColumns: (cols:   { id: string; name: string }[])                        => set({ availableColumns: cols }),
  };
}
