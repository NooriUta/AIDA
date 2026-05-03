// ─── transformColumns.ts — Statement column enrichment (second-pass) ─────────
//
// Called after schema explore + ELK layout, with the result of fetchStmtColumns().
// Patches existing LoomNodes with column data and builds column-level flow edges.
// No re-layout needed: only node.data is updated.

import type { ExploreResult } from '../services/lineage';
import type { ColumnInfo } from '../types/domain';
import type { LoomNode, LoomEdge } from '../types/graph';
import { TRANSFORM } from './constants';
import { getEdgeStyle } from './transformHelpers';

/** Extract just the stroke colour for the given edge type from the 4-way
 *  palette so cfEdges (fine-grain column-level flow lines) use the same
 *  colour as their aggregated table-level counterparts in the legend.
 *  cfEdges keep their own thinner stroke / dash / opacity so they read
 *  as a secondary "detail" overlay on top of the main edges. */
function cfStroke(edgeType: 'READS_FROM' | 'WRITES_TO'): string {
  const s = getEdgeStyle(edgeType);
  return (typeof s.stroke === 'string' ? s.stroke : '') || (edgeType === 'WRITES_TO' ? '#D4922A' : '#88B8A8');
}

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
      // Cap columns per node so ELK height stays manageable (MAX_PARTIAL_COLS).
      // tableColMap keeps ALL columns for edge-matching; colsByParent controls rendering.
      const tableCols = colsByParent.get(e.source)!;
      if (tableCols.length < TRANSFORM.MAX_PARTIAL_COLS) {
        // Read PK/FK/dataType from meta — populated by stmtColumns hasColQ RETURN clause.
        // SmallRye GraphQL serialises Map<String,String> as [{key,value}].
        const metaMap = Object.fromEntries((col.meta ?? []).map((e) => [e.key, e.value]));
        tableCols.push({
          id:           col.id,
          name:         col.label,
          type:         metaMap['dataType'] ?? '',
          isPrimaryKey: metaMap['isPk']       === 'true',
          isForeignKey: metaMap['isFk']       === 'true',
          isRequired:   metaMap['isRequired'] === 'true',
        });
      }
    } else if (e.type === 'HAS_OUTPUT_COL' || e.type === 'HAS_AFFECTED_COL') {
      if (!stmtColMap.has(e.source)) stmtColMap.set(e.source, new Map());
      stmtColMap.get(e.source)!.set(col.label.toUpperCase(), col.id);
      if (!colsByParent.has(e.source)) colsByParent.set(e.source, []);
      // Same cap for statement nodes — prevents multi-thousand-pixel statement nodes.
      const stmtCols = colsByParent.get(e.source)!;
      if (stmtCols.length < TRANSFORM.MAX_PARTIAL_COLS) {
        stmtCols.push({ id: col.id, name: col.label, type: '', isPrimaryKey: false, isForeignKey: false });
      }
    }
  }

  const enrichedNodes = colsByParent.size === 0
    ? nodes
    : nodes.map((n) => {
        const cols = colsByParent.get(n.id);
        return cols ? { ...n, data: { ...n.data, columns: cols } } : n;
      });

  // ── Column-level flow edges ────────────────────────────────────────────────
  // Sprint 1.2 inversion: edges are now in canonical DB direction, no flip needed.
  // baseEdges has WRITES_TO  (source=stmtId,  target=tableId)
  //           and READS_FROM (source=tableId, target=stmtId) — natural after inversion.
  const renderedIds = new Set(nodes.map((n) => n.id));
  const cfEdges: LoomEdge[] = [];
  const cfSeen  = new Set<string>();

  // Build per-node sets of column IDs that will have <Handle> elements in the DOM.
  // All columns in colsByParent get <Handle id="src-*" / "tgt-*"> rendered by
  // StatementNode / TableNode.  Guard here so every cfEdge we emit is guaranteed renderable.
  const visibleColIds = new Map<string, Set<string>>();
  for (const [nodeId, cols] of colsByParent) {
    visibleColIds.set(nodeId, new Set(cols.map((c) => c.id)));
  }

  for (const e of baseEdges) {
    const edgeType = e.data?.edgeType as string | undefined;
    if (edgeType !== 'WRITES_TO' && edgeType !== 'READS_FROM') continue;

    // Both edges now natural direction — WRITES_TO stmt→table, READS_FROM table→stmt.
    const stmtId  = edgeType === 'WRITES_TO' ? e.source : e.target;
    const tableId = edgeType === 'WRITES_TO' ? e.target : e.source;
    if (!renderedIds.has(stmtId) || !renderedIds.has(tableId)) continue;

    const tableCols = tableColMap.get(tableId);
    const stmtCols  = stmtColMap.get(stmtId);
    if (!tableCols || !stmtCols) continue;

    const visibleStmt  = visibleColIds.get(stmtId);
    const visibleTable = visibleColIds.get(tableId);
    // If either side has no visible-column data, no handles exist at all → skip
    if (!visibleStmt || !visibleTable) continue;

    for (const [name, sColId] of stmtCols) {
      const tColId = tableCols.get(name);
      if (!tColId) continue;
      // Skip if either column handle is absent from the DOM
      if (!visibleStmt.has(sColId) || !visibleTable.has(tColId)) continue;

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
            // Colour pulled from the 4-way legend palette (getEdgeStyle) so
            // cfEdges and their aggregated table-level counterparts use the
            // exact same stroke. Thinner width + lighter dash + reduced
            // opacity keep them visually "secondary" to the aggregated lines.
            style:        { stroke: cfStroke('WRITES_TO'), strokeWidth: 1, strokeDasharray: '3 2', opacity: 0.75 },
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
            style:        { stroke: cfStroke('READS_FROM'), strokeWidth: 1, strokeDasharray: '3 2', opacity: 0.75 },
            data:         { edgeType: 'HAS_OUTPUT_COL', parentStmtId: stmtId },
          });
        }
      }
    }
  }

  return { nodes: enrichedNodes, cfEdges };
}
