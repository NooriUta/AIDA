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
        filter: { ...s.filter, stmtFilter: stmtId, fieldFilter: null },
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
    toggleCfEdges:          () =>
      set((s) => ({ filter: { ...s.filter, showCfEdges: !s.filter.showCfEdges } })),

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
