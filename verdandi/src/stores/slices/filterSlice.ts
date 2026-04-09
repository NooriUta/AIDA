import type { DaliNodeType } from '../../types/domain';

// eslint-disable-next-line @typescript-eslint/no-explicit-any
type S = (p: any) => void;

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

export function filterActions(set: S) {
  return {
    setStartObject: (nodeId: string, nodeType: DaliNodeType, label: string) =>
      set((s: any) => ({
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
      set((s: any) => ({
        filter: { ...s.filter, tableFilter: tableId, stmtFilter: null, fieldFilter: null },
        availableColumns: [],
      })),

    setStmtFilter: (stmtId: string | null) =>
      set((s: any) => ({
        filter: { ...s.filter, stmtFilter: stmtId, fieldFilter: null },
        availableColumns: [],
      })),

    setFieldFilter:         (columnName: string | null) =>
      set((s: any) => ({ filter: { ...s.filter, fieldFilter: columnName } })),
    setDepth:               (depth: number) =>
      set((s: any) => ({ filter: { ...s.filter, depth } })),
    setDirection:           (upstream: boolean, downstream: boolean) =>
      set((s: any) => ({ filter: { ...s.filter, upstream, downstream } })),
    toggleTableLevelView:   () =>
      set((s: any) => ({ filter: { ...s.filter, tableLevelView: !s.filter.tableLevelView } })),
    toggleCfEdges:          () =>
      set((s: any) => ({ filter: { ...s.filter, showCfEdges: !s.filter.showCfEdges } })),

    clearFilter: () =>
      set((s: any) => ({
        filter: {
          ...FILTER_DEFAULTS,
          startObjectId:    s.filter.startObjectId,
          startObjectType:  s.filter.startObjectType,
          startObjectLabel: s.filter.startObjectLabel,
        },
      })),

    setAvailableFields:  (fields: string[])  => set({ availableFields: fields }),
    setAvailableTables:  (tables: any[])     => set({ availableTables: tables }),
    setAvailableStmts:   (stmts: any[])      => set({ availableStmts: stmts }),
    setAvailableColumns: (cols: any[])       => set({ availableColumns: cols }),
  };
}
