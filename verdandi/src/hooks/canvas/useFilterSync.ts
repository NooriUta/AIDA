import { useEffect } from 'react';

import { useLoomStore } from '../../stores/loomStore';
import type { LoomNode, LoomEdge } from '../../types/graph';
import type { ColumnInfo }          from '../../types/domain';

interface Graph {
  nodes: LoomNode[];
  edges: LoomEdge[];
}

/**
 * Syncs rawGraph contents into the store's filter-panel lists:
 * available apps, DBs, schemas, tables, stmts, columns.
 * Also handles auto-expansion side-effects (parent DB expand, table column expand).
 */
export function useFilterSync(rawGraph: Graph | null): void {
  const {
    viewLevel,
    filter,
    selectedNodeId,
    expandedDbs,
    toggleDbExpansion,
    nodeExpansionState,
    setNodeExpansion,
    setAvailableApps,
    setAvailableDbs,
    setAvailableSchemas,
    setAvailableTables,
    setAvailableStmts,
    setAvailableFields,
    setAvailableColumns,
  } = useLoomStore();

  // ── Populate App/DB/Schema lists for L1 filter panel ──────────────────────
  // NOTE: intentionally NOT cleared on L2/L3 so FilterToolbar can use them
  // as a quick-navigation switcher without returning to L1 first.
  useEffect(() => {
    if (viewLevel !== 'L1' || !rawGraph) return;
    setAvailableApps(
      rawGraph.nodes
        .filter((n) => n.type === 'applicationNode')
        .map((n) => ({ id: n.id, label: n.data.label })),
    );
    setAvailableDbs(
      rawGraph.nodes
        .filter((n) => n.type === 'databaseNode')
        .map((n) => ({ id: n.id, label: n.data.label, appId: n.parentId ?? null })),
    );
    setAvailableSchemas(
      rawGraph.nodes
        .filter((n) => n.type === 'l1SchemaNode')
        .map((n) => ({ id: n.id, label: n.data.label, dbId: n.parentId ?? '' })),
    );
  }, [viewLevel, rawGraph, setAvailableApps, setAvailableDbs, setAvailableSchemas]);

  // ── Populate Table/Stmt lists for L2 filter dropdowns ────────────────────────
  useEffect(() => {
    if (viewLevel !== 'L2' || !rawGraph) {
      setAvailableTables([]);
      setAvailableStmts([]);
      return;
    }
    const tables = rawGraph.nodes
      .filter((n) => n.type === 'tableNode')
      .map((n) => ({ id: n.id, label: n.data.label }))
      .sort((a, b) => a.label.localeCompare(b.label));
    setAvailableTables(tables);

    const tableIds = new Set(tables.map((t) => t.id));
    // Build map: stmtId → Set of connected tableIds (via READS_FROM / WRITES_TO)
    const stmtToTables = new Map<string, Set<string>>();
    for (const e of rawGraph.edges) {
      const et = e.data?.edgeType as string;
      if (et !== 'READS_FROM' && et !== 'WRITES_TO') continue;
      const [stmtId, tableId] = tableIds.has(e.target)
        ? [e.source, e.target]
        : tableIds.has(e.source)
        ? [e.target, e.source]
        : [null, null];
      if (!stmtId || !tableId) continue;
      if (!stmtToTables.has(stmtId)) stmtToTables.set(stmtId, new Set());
      stmtToTables.get(stmtId)!.add(tableId);
    }
    const stmts = rawGraph.nodes
      .filter((n) => n.type === 'statementNode')
      .map((n) => ({
        id:                n.id,
        label:             n.data.label,
        connectedTableIds: Array.from(stmtToTables.get(n.id) ?? []),
      }))
      .sort((a, b) => a.label.localeCompare(b.label));
    setAvailableStmts(stmts);
  }, [viewLevel, rawGraph, setAvailableTables, setAvailableStmts]);

  // ── Populate all-columns list (availableFields) for the column dropdown ─────
  // Collects unique column names across ALL table/statement nodes so the
  // dropdown is visible even when no specific table/stmt is selected.
  useEffect(() => {
    if (viewLevel !== 'L2' || !rawGraph) { setAvailableFields([]); return; }
    const names = new Set<string>();
    for (const n of rawGraph.nodes) {
      const cols = (n.data.columns as ColumnInfo[] | undefined) ?? [];
      for (const c of cols) names.add(c.name);
    }
    setAvailableFields([...names].sort());
  }, [viewLevel, rawGraph, setAvailableFields]);

  // ── Populate Column cascade: columns of the selected table or stmt ─────────
  // Re-runs when stmtColsQ.data arrives so stmt cols appear after second-pass fetch.
  useEffect(() => {
    if (viewLevel !== 'L2' || !rawGraph) { setAvailableColumns([]); return; }
    const activeId = filter.stmtFilter ?? filter.tableFilter;
    if (!activeId) { setAvailableColumns([]); return; }
    const node = rawGraph.nodes.find((n) => n.id === activeId);
    const cols = (node?.data.columns as ColumnInfo[] | undefined) ?? [];
    setAvailableColumns(cols.map((c) => ({ id: c.id, name: c.name })));
  }, [viewLevel, rawGraph, filter.tableFilter, filter.stmtFilter, setAvailableColumns]);

  // ── Auto-expand parent DB when navigating to a schema chip from search ───────
  // rawGraph always has l1SchemaNode.hidden=true (initial state), so we check
  // expandedDbs instead of node.hidden to avoid toggling an already-open DB
  // when the user simply clicks a visible schema chip.
  useEffect(() => {
    if (viewLevel !== 'L1' || !selectedNodeId || !rawGraph) return;
    const node = rawGraph.nodes.find((n) => n.id === selectedNodeId);
    if (node?.type === 'l1SchemaNode' && node.parentId && !expandedDbs.has(node.parentId)) {
      toggleDbExpansion(node.parentId);
    }
  }, [selectedNodeId, viewLevel, rawGraph, expandedDbs, toggleDbExpansion]);

  // ── Auto-expand table columns on L2 load ─────────────────────────────────────
  // Column-flow edges need column handles in the DOM — ensure tables are at least partial.
  useEffect(() => {
    if (viewLevel !== 'L2' || !rawGraph) return;
    for (const n of rawGraph.nodes) {
      if (n.data.nodeType !== 'DaliTable') continue;
      if ((nodeExpansionState[n.id] ?? 'partial') === 'collapsed') {
        setNodeExpansion(n.id, 'partial');
      }
    }
  }, [viewLevel, rawGraph, nodeExpansionState, setNodeExpansion]);
}
