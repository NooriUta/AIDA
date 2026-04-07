// ─── transformColumns.ts — Statement column enrichment (second-pass) ─────────
//
// Called after schema explore + ELK layout, with the result of fetchStmtColumns().
// Patches existing LoomNodes with column data and builds column-level flow edges.
// No re-layout needed: only node.data is updated.

import type { ExploreResult } from '../services/lineage';
import type { ColumnInfo } from '../types/domain';
import type { LoomNode, LoomEdge } from '../types/graph';

const L2_MAX_COLS = 5;

export function applyStmtColumns(
  nodes: LoomNode[],
  baseEdges: LoomEdge[],
  colResult: ExploreResult,
): { nodes: LoomNode[]; cfEdges: LoomEdge[] } {
  if (colResult.edges.length === 0) return { nodes, cfEdges: [] };

  const nodeById = new Map(colResult.nodes.map((n) => [n.id, n]));
  const colsByParent = new Map<string, ColumnInfo[]>();

  // tableId → UPPER(colName) → colId
  const tableColMap = new Map<string, Map<string, string>>();
  // stmtId  → UPPER(colName) → stmtColId
  const stmtColMap  = new Map<string, Map<string, string>>();

  for (const e of colResult.edges) {
    const col = nodeById.get(e.target);
    if (!col) continue;

    if (e.type === 'HAS_COLUMN') {
      if (!tableColMap.has(e.source)) tableColMap.set(e.source, new Map());
      tableColMap.get(e.source)!.set(col.label.toUpperCase(), col.id);
      if (!colsByParent.has(e.source)) colsByParent.set(e.source, []);
      const cols = colsByParent.get(e.source)!;
      if (cols.length < L2_MAX_COLS)
        cols.push({ id: col.id, name: col.label, type: '', isPrimaryKey: false, isForeignKey: false });
    } else if (e.type === 'HAS_OUTPUT_COL' || e.type === 'HAS_AFFECTED_COL') {
      if (!stmtColMap.has(e.source)) stmtColMap.set(e.source, new Map());
      stmtColMap.get(e.source)!.set(col.label.toUpperCase(), col.id);
      if (!colsByParent.has(e.source)) colsByParent.set(e.source, []);
      const cols = colsByParent.get(e.source)!;
      if (cols.length < L2_MAX_COLS)
        cols.push({ id: col.id, name: col.label, type: '', isPrimaryKey: false, isForeignKey: false });
    }
  }

  const enrichedNodes = colsByParent.size === 0
    ? nodes
    : nodes.map((n) => {
        const cols = colsByParent.get(n.id);
        return cols ? { ...n, data: { ...n.data, columns: cols } } : n;
      });

  // ── Column-level flow edges ────────────────────────────────────────────────
  // baseEdges has WRITES_TO  (source=stmtId,  target=tableId)
  //           and READS_FROM (source=tableId, target=stmtId) — flipped for display.
  const renderedIds = new Set(nodes.map((n) => n.id));
  const cfEdges: LoomEdge[] = [];
  const cfSeen  = new Set<string>();

  for (const e of baseEdges) {
    const edgeType = e.data?.edgeType as string | undefined;
    if (edgeType !== 'WRITES_TO' && edgeType !== 'READS_FROM') continue;

    const stmtId  = edgeType === 'WRITES_TO' ? e.source : e.target;
    const tableId = edgeType === 'WRITES_TO' ? e.target : e.source;
    if (!renderedIds.has(stmtId) || !renderedIds.has(tableId)) continue;

    const tableCols = tableColMap.get(tableId);
    const stmtCols  = stmtColMap.get(stmtId);
    if (!tableCols || !stmtCols) continue;

    for (const [name, sColId] of stmtCols) {
      const tColId = tableCols.get(name);
      if (!tColId) continue;

      if (edgeType === 'WRITES_TO') {
        // stmt.affectedCol (right) → table.col (left)
        const cfId = `cf-w-${sColId}-${tColId}`;
        if (!cfSeen.has(cfId)) {
          cfSeen.add(cfId);
          cfEdges.push({
            id:           cfId,
            source:       stmtId,
            target:       tableId,
            sourceHandle: `src-${sColId}`,
            targetHandle: `tgt-${tColId}`,
            type:         'default',
            animated:     false,
            style:        { stroke: '#D4922A', strokeWidth: 1, strokeDasharray: '3 2', opacity: 0.75 },
            data:         { edgeType: 'HAS_AFFECTED_COL', parentStmtId: stmtId },
          });
        }
      } else {
        // READS_FROM display: table (left) → stmt (right)
        const cfId = `cf-r-${tColId}-${sColId}`;
        if (!cfSeen.has(cfId)) {
          cfSeen.add(cfId);
          cfEdges.push({
            id:           cfId,
            source:       tableId,
            target:       stmtId,
            sourceHandle: `src-${tColId}`,
            targetHandle: `tgt-${sColId}`,
            type:         'default',
            animated:     false,
            style:        { stroke: '#88B8A8', strokeWidth: 1, strokeDasharray: '3 2', opacity: 0.75 },
            data:         { edgeType: 'HAS_OUTPUT_COL', parentStmtId: stmtId },
          });
        }
      }
    }
  }

  return { nodes: enrichedNodes, cfEdges };
}
