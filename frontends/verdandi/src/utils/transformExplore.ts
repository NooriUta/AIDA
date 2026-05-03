import type { ExploreResult, GraphNode } from '../services/lineage';
import type { DaliNodeType, DaliEdgeType, ColumnInfo } from '../types/domain';
import type { LoomNode, LoomEdge } from '../types/graph';
import {
  NODE_TYPE_MAP,
  DRILLABLE_TYPES,
  ANIMATED_EDGES,
  getEdgeStyle,
  extractStatementType,
  extractRoutineKind,
  parseStmtLabel,
} from './transformHelpers';
import { TRANSFORM, LAYOUT } from './constants';

const { L2_MAX_COLS, EDGE_CURVATURE } = TRANSFORM;
const { NODE_WIDTH, NODE_HEIGHT_BASE } = LAYOUT;

// ─── Nesting edges: build visual hierarchy (Schema → Routine → Stmt) ─────────
// CONTAINS_ROUTINE is dual-purpose: Schema→Routine AND Routine→Routine.
// CHILD_OF / USES_SUBQUERY / NESTED_IN are NOT included — sub-statements are
// invisible at L2 (they can be explored by drilling into a statement at L3).
const NESTING_EDGES = new Set<string>([
  'CONTAINS_ROUTINE', 'CONTAINS_STMT', 'CONTAINS_PACKAGE',
  'CONTAINS_TABLE', 'BELONGS_TO_SESSION',
  'HAS_COLUMN', 'HAS_OUTPUT_COL', 'HAS_AFFECTED_COL', 'HAS_PARAMETER', 'HAS_VARIABLE',
  // Phase S2.4 — PL/SQL record field containment: fields render as rows inside RecordNode,
  // never as separate canvas nodes. RETURNS_INTO (stmt→record) is NOT suppressed — it renders
  // as a visible edge. BULK_COLLECTS_INTO and RECORD_USED_IN are handled below in SUPPRESSED_EDGES.
  'HAS_RECORD_FIELD',
]);

// ─── Suppressed edges: ALL structural/containment edges hidden from arrows ───
// Superset of NESTING_EDGES.  Includes sub-statement links that are NOT rendered
// at L2 but whose targets must still be excluded from the "external nodes" list.
const SUPPRESSED_EDGES = new Set<string>([
  ...NESTING_EDGES,
  'CONTAINS_PACKAGE',
  'CHILD_OF', 'USES_SUBQUERY', 'NESTED_IN',
  // NOTE: 'CALLS' is intentionally NOT suppressed — routine→routine call edges
  // are rendered at L2 AGG to show inter-procedure call flow.
  // ROUTINE_USES_TABLE removed Sprint 0.1 SCHEMA_CLEANUP (§13.5).
  'ATOM_REF_TABLE',
  'ATOM_REF_COLUMN',
  'ATOM_REF_STMT',
  'ATOM_REF_OUTPUT_COL',
  'ATOM_PRODUCES',
  'HAS_ATOM',
  'HAS_JOIN',
  'HAS_DATABASE',
  'CONTAINS_SCHEMA',
  'HAS_SERVICE',
  'USES_DATABASE',
  // Phase S2.4 — PL/SQL record containment (virtual edge emitted by backend)
  'CONTAINS_RECORD',
  // NODE_ONLY self-edges (emitted for routine aggregate standalone nodes)
  'NODE_ONLY',
]);

// ─── L2 edge whitelist: only data-flow arrows on the canvas ─────────────────
// FILTER_FLOW is a column-level lineage edge (analogous to DATA_FLOW) emitted
// by the backend for WHERE / HAVING / JOIN-condition column references.
const L2_FLOW_EDGES = new Set<string>(['READS_FROM', 'WRITES_TO', 'DATA_FLOW', 'FILTER_FLOW']);

// ─── Internal helpers ────────────────────────────────────────────────────────

function buildContainmentChildren(
  edges: { source: string; target: string; type: string }[],
  edgeTypes: Set<string>,
): Map<string, string[]> {
  const map = new Map<string, string[]>();
  for (const e of edges) {
    if (!edgeTypes.has(e.type)) continue;
    if (!map.has(e.source)) map.set(e.source, []);
    map.get(e.source)!.push(e.target);
  }
  return map;
}

function collectAllDescendants(
  rootId: string,
  tree: Map<string, string[]>,
): Set<string> {
  const visited = new Set<string>();
  const stack = [rootId];
  while (stack.length > 0) {
    const id = stack.pop()!;
    const children = tree.get(id);
    if (!children) continue;
    for (const child of children) {
      if (!visited.has(child)) {
        visited.add(child);
        stack.push(child);
      }
    }
  }
  return visited;
}

function isSchemaExploreResult(result: ExploreResult): boolean {
  if (!result.nodes.some((n) => n.type === 'DaliSchema')) return false;
  return result.edges.some((e) => e.type === 'CONTAINS_TABLE');
}

// ─── Schema explore (group layout) ──────────────────────────────────────────

function transformSchemaExplore(result: ExploreResult): {
  nodes: LoomNode[];
  edges: LoomEdge[];
} {
  const allSchemaNodes = result.nodes.filter((n) => n.type === 'DaliSchema');

  const nestTree = buildContainmentChildren(result.edges, NESTING_EDGES);
  const nodeById = new Map(result.nodes.map((n) => [n.id, n]));

  // Build table → schema name mapping (needed for multi-schema / DB-level explore)
  const tableSchemaName = new Map<string, string>();
  for (const e of result.edges) {
    if (e.type !== 'CONTAINS_TABLE') continue;
    const schemaN = nodeById.get(e.source);
    if (schemaN?.type === 'DaliSchema') tableSchemaName.set(e.target, schemaN.label);
  }

  // Collect columns: DaliColumn → table only (stmt columns arrive via applyStmtColumns)
  const columnsByParent = new Map<string, ColumnInfo[]>();
  for (const e of result.edges) {
    if (e.type !== 'HAS_COLUMN') continue;
    const colNode = nodeById.get(e.target);
    if (!colNode || colNode.type !== 'DaliColumn') continue;
    if (!columnsByParent.has(e.source)) columnsByParent.set(e.source, []);
    const cols = columnsByParent.get(e.source)!;
    if (cols.length < L2_MAX_COLS) {
      const metaMap = Object.fromEntries((colNode.meta ?? []).map((m) => [m.key, m.value]));
      cols.push({
        id:           colNode.id,
        name:         colNode.label,
        type:         metaMap['dataType']   ?? '',
        isPrimaryKey: metaMap['isPk']       === 'true',
        isForeignKey: metaMap['isFk']       === 'true',
        isRequired:   metaMap['isRequired'] === 'true',
      });
    }
  }

  // Walk containment tree: collect tables and statements
  const schemaTables: GraphNode[] = [];
  const schemaTableIds = new Set<string>();
  const schemaStmtIds = new Set<string>();

  const _visited = new Set<string>();
  function walkTree(parentId: string) {
    if (_visited.has(parentId)) return;     // cycle guard (TD-14)
    _visited.add(parentId);
    const children = nestTree.get(parentId);
    if (!children) return;
    for (const childId of children) {
      const child = nodeById.get(childId);
      if (!child) continue;
      switch (child.type) {
        case 'DaliTable':
          schemaTables.push(child);
          schemaTableIds.add(child.id);
          break;
        case 'DaliStatement':
          schemaStmtIds.add(child.id);
          break;
        case 'DaliPackage':
        case 'DaliRoutine':
        case 'DaliSession':
          walkTree(child.id);
          break;
        default:
          walkTree(childId);
          break;
      }
    }
  }
  for (const sn of allSchemaNodes) walkTree(sn.id);

  // Statements connected to tables via READS_FROM / WRITES_TO
  const DATA_FLOW_TYPES = new Set(['READS_FROM', 'WRITES_TO']);
  const connectedStmtIds = new Set<string>();
  for (const e of result.edges) {
    if (!DATA_FLOW_TYPES.has(e.type)) continue;
    const stmtId = schemaTableIds.has(e.target) ? e.source
                 : schemaTableIds.has(e.source) ? e.target
                 : null;
    if (stmtId && nodeById.get(stmtId)?.type === 'DaliStatement') {
      connectedStmtIds.add(stmtId);
    }
  }

  const allStmtIds = new Set([...schemaStmtIds, ...connectedStmtIds]);

  // Cross-schema WRITES_TO targets
  const externalWriteTableIds = new Set<string>();
  for (const e of result.edges) {
    if (e.type !== 'WRITES_TO') continue;
    if (!allStmtIds.has(e.source)) continue;
    if (schemaTableIds.has(e.target)) continue;
    const nd = nodeById.get(e.target);
    if (nd?.type === 'DaliTable') externalWriteTableIds.add(e.target);
  }

  const rfNodes: LoomNode[] = [];
  const renderedIds = new Set<string>();

  for (const table of schemaTables) {
    rfNodes.push({
      id:       table.id,
      type:     'tableNode',
      position: { x: 0, y: 0 },
      data: {
        label:             table.label,
        nodeType:          'DaliTable' as DaliNodeType,
        childrenAvailable: true,
        metadata:          { scope: table.scope, dataSource: table.dataSource || undefined },
        schema:            tableSchemaName.get(table.id) ?? '',
        columns:           columnsByParent.get(table.id),
      },
    });
    renderedIds.add(table.id);
  }

  for (const extId of externalWriteTableIds) {
    const nd = nodeById.get(extId);
    if (!nd) continue;
    rfNodes.push({
      id:       extId,
      type:     'tableNode',
      position: { x: 0, y: 0 },
      data: {
        label:             nd.label,
        nodeType:          'DaliTable' as DaliNodeType,
        childrenAvailable: true,
        metadata:          { scope: nd.scope, dataSource: nd.dataSource || undefined },
        schema:            tableSchemaName.get(extId) ?? nd.scope ?? '',
        columns:           columnsByParent.get(extId),
      },
    });
    renderedIds.add(extId);
  }

  // Phase S2.6 — build stmt → owner lookup so StatementNode header segments
  // can be made clickable (navigates back to L2 scoped to that routine/package).
  const stmtToRoutine = new Map<string, string>();
  const routineToParent = new Map<string, string>();
  for (const e of result.edges) {
    if (e.type === 'CONTAINS_STMT') stmtToRoutine.set(e.target, e.source);
    if (e.type === 'CONTAINS_ROUTINE') {
      const src = nodeById.get(e.source);
      if (src?.type === 'DaliPackage' || src?.type === 'DaliSchema') {
        routineToParent.set(e.target, e.source);
      }
    }
  }

  for (const stmtId of allStmtIds) {
    const nd = nodeById.get(stmtId);
    if (!nd) continue;
    const { shortLabel, groupPath } = parseStmtLabel(nd.label);
    const ownerRoutineId  = stmtToRoutine.get(stmtId);
    const ownerPackageId  = ownerRoutineId ? routineToParent.get(ownerRoutineId) : undefined;
    rfNodes.push({
      id:       stmtId,
      type:     'statementNode',
      position: { x: 0, y: 0 },
      data: {
        label:             shortLabel,
        nodeType:          'DaliStatement' as DaliNodeType,
        childrenAvailable: true,
        metadata:          {
          scope:          nd.scope,
          groupPath,
          fullLabel:      nd.label,
          ownerRoutineId: ownerRoutineId  ?? undefined,
          ownerPackageId: ownerPackageId  ?? undefined,
        },
        operation:         extractStatementType(nd.label),
        columns:           columnsByParent.get(stmtId),
      },
    });
    renderedIds.add(stmtId);
  }

  const rfEdges: LoomEdge[] = result.edges
    .filter((e) =>
      L2_FLOW_EDGES.has(e.type) &&
      renderedIds.has(e.source) &&
      renderedIds.has(e.target),
    )
    .map((e) => {
      const edgeType = e.type as DaliEdgeType;
      const flip = edgeType === 'READS_FROM';
      // Backend sets sourceHandle / targetHandle for column-level
      // DATA_FLOW / FILTER_FLOW ('src-#13:X' / 'tgt-#31:Y'). Pass them
      // through to React Flow so the edge lands on the specific column
      // row inside the parent TableNode / StatementNode card.
      // Handles are also flipped together with source/target for READS_FROM.
      const srcH = e.sourceHandle && e.sourceHandle.length > 0 ? e.sourceHandle : undefined;
      const tgtH = e.targetHandle && e.targetHandle.length > 0 ? e.targetHandle : undefined;
      return {
        id:           e.id,
        source:       flip ? e.target : e.source,
        target:       flip ? e.source : e.target,
        sourceHandle: flip ? tgtH : srcH,
        targetHandle: flip ? srcH : tgtH,
        type:         'default',
        pathOptions:  { curvature: EDGE_CURVATURE },
        animated:     ANIMATED_EDGES.has(edgeType),
        style:        getEdgeStyle(edgeType),
        data:         { edgeType },
      };
    });

  return { nodes: rfNodes, edges: rfEdges };
}

// ─── Subquery READS_FROM hoisting ────────────────────────────────────────────
//
// Root DaliStatement nodes can own sub-statements linked via:
//   USES_SUBQUERY  source=rootStmt  → target=subStmt
//   CHILD_OF       source=subStmt   → target=parentStmt
//   NESTED_IN      source=subStmt   → target=parentStmt
//
// Sub-statements are not rendered on the canvas. Their READS_FROM edges are
// hoisted to the nearest visible ancestor so the root statement exposes every
// table it — and its sub-queries — touch.
function hoistSubqueryReads(result: ExploreResult): {
  subqueryIds:    Set<string>;
  syntheticEdges: LoomEdge[];
} {
  const SUBQ_TYPES = new Set(['USES_SUBQUERY', 'CHILD_OF', 'NESTED_IN']);
  const stmtIds   = new Set(
    result.nodes.filter((n) => n.type === 'DaliStatement').map((n) => n.id),
  );

  const subqTree = new Map<string, string[]>();
  for (const e of result.edges) {
    if (!SUBQ_TYPES.has(e.type)) continue;
    const parentId = e.type === 'USES_SUBQUERY' ? e.source : e.target;
    const childId  = e.type === 'USES_SUBQUERY' ? e.target : e.source;
    if (!stmtIds.has(parentId) || !stmtIds.has(childId)) continue;
    if (!subqTree.has(parentId)) subqTree.set(parentId, []);
    subqTree.get(parentId)!.push(childId);
  }

  if (subqTree.size === 0) return { subqueryIds: new Set(), syntheticEdges: [] };

  const immediateSubqIds = new Set<string>();
  for (const children of subqTree.values()) {
    for (const c of children) immediateSubqIds.add(c);
  }
  const allSubqIds = new Set<string>(immediateSubqIds);
  for (const id of immediateSubqIds) {
    for (const d of collectAllDescendants(id, subqTree)) allSubqIds.add(d);
  }

  const subqToRoot = new Map<string, string>();
  for (const [parentId, children] of subqTree) {
    if (allSubqIds.has(parentId)) continue;
    for (const childId of children) {
      subqToRoot.set(childId, parentId);
      for (const d of collectAllDescendants(childId, subqTree)) {
        subqToRoot.set(d, parentId);
      }
    }
  }

  const seen = new Set<string>();
  for (const e of result.edges) {
    if (e.type !== 'READS_FROM') continue;
    if (stmtIds.has(e.source) && !allSubqIds.has(e.source)) {
      seen.add(`${e.source}\x00${e.target}`);
    }
  }

  const syntheticEdges: LoomEdge[] = [];
  let idx = 0;
  for (const e of result.edges) {
    if (e.type !== 'READS_FROM') continue;
    const rootId = subqToRoot.get(e.source);
    if (!rootId) continue;
    const key = `${rootId}\x00${e.target}`;
    if (seen.has(key)) continue;
    seen.add(key);
    syntheticEdges.push({
      id:       `__sqrf_${idx++}`,
      source:   e.target,
      target:   rootId,
      type:     'default',
      animated: false,
      style:    getEdgeStyle('READS_FROM'),
      data:     { edgeType: 'READS_FROM' as DaliEdgeType },
    });
  }

  return { subqueryIds: allSubqIds, syntheticEdges };
}

// ─── Public: flat + schema explore dispatcher ────────────────────────────────

export function transformGqlExplore(
  result: ExploreResult,
  /**
   * Optional set of node IDs already present in the base graph.
   * Used by the expansion path: the starting node (e.g. INSERT:4343) is NOT
   * returned by the backend expansion query, so its edges would otherwise be
   * dropped by the nodeIds filter.  Passing externalNodeIds lets those edges
   * through — the allowedExpEdges guard in useGraphData.ts then decides
   * whether to actually include them in the merged graph.
   */
  externalNodeIds?: Set<string>,
): {
  nodes: LoomNode[];
  edges: LoomEdge[];
} {
  if (isSchemaExploreResult(result)) {
    return transformSchemaExplore(result);
  }

  const nodeById = new Map(result.nodes.map((n) => [n.id, n]));

  const columnsByTable = new Map<string, ColumnInfo[]>();
  for (const e of result.edges) {
    if (e.type !== 'HAS_COLUMN') continue;
    const colNode = nodeById.get(e.target);
    if (!colNode || colNode.type !== 'DaliColumn') continue;
    if (!columnsByTable.has(e.source)) columnsByTable.set(e.source, []);
    const cols = columnsByTable.get(e.source)!;
    if (cols.length < L2_MAX_COLS) {
      const metaMap = Object.fromEntries((colNode.meta ?? []).map((m) => [m.key, m.value]));
      cols.push({
        id:           colNode.id,
        name:         colNode.label,
        type:         metaMap['dataType']   ?? '',
        isPrimaryKey: metaMap['isPk']       === 'true',
        isForeignKey: metaMap['isFk']       === 'true',
        isRequired:   metaMap['isRequired'] === 'true',
      });
    }
  }

  const outputColsByStmt = new Map<string, ColumnInfo[]>();
  for (const e of result.edges) {
    if (e.type !== 'HAS_OUTPUT_COL' && e.type !== 'HAS_AFFECTED_COL') continue;
    const colNode = nodeById.get(e.target);
    if (!colNode || !['DaliOutputColumn', 'DaliColumn', 'DaliAffectedColumn'].includes(colNode.type)) continue;
    if (!outputColsByStmt.has(e.source)) outputColsByStmt.set(e.source, []);
    const cols = outputColsByStmt.get(e.source)!;
    if (cols.length < L2_MAX_COLS) {
      cols.push({ id: colNode.id, name: colNode.label, type: '', isPrimaryKey: false, isForeignKey: false });
    }
  }

  // Phase S2.4 — collect DaliRecordField rows into RecordNode's data.columns
  // (analogous to DaliColumn→TableNode and DaliOutputColumn→StatementNode)
  const fieldsByRecord = new Map<string, Array<{ id: string; name: string; type: string; isPrimaryKey: boolean; isForeignKey: boolean }>>();
  for (const e of result.edges) {
    if (e.type !== 'HAS_RECORD_FIELD') continue;
    const fieldNode = nodeById.get(e.target);
    if (!fieldNode || fieldNode.type !== 'DaliRecordField') continue;
    if (!fieldsByRecord.has(e.source)) fieldsByRecord.set(e.source, []);
    const fields = fieldsByRecord.get(e.source)!;
    const metaMap = Object.fromEntries((fieldNode.meta ?? []).map((m) => [m.key, m.value]));
    fields.push({
      id:           fieldNode.id,
      name:         fieldNode.label,
      type:         metaMap['dataType'] ?? metaMap['data_type'] ?? '',
      isPrimaryKey: false,
      // Use isForeignKey as a proxy for "has source_column_geoid" (%ROWTYPE origin badge)
      isForeignKey: !!(metaMap['source_column_geoid']),
    });
  }

  const { subqueryIds, syntheticEdges } = hoistSubqueryReads(result);

  const nodes: LoomNode[] = result.nodes
    .filter((n) =>
      n.type !== 'DaliOutputColumn'  &&
      n.type !== 'DaliColumn'        &&
      n.type !== 'DaliAffectedColumn' &&
      n.type !== 'DaliAtom'          &&
      n.type !== 'DaliRecordField'   &&  // Phase S2.4: fields render inside RecordNode
      !subqueryIds.has(n.id),
    )
    .map((n) => {
      const nodeType = n.type as DaliNodeType;
      const isFlatRoutine = nodeType === 'DaliRoutine' || nodeType === 'DaliSession' || nodeType === 'DaliPackage';
      const { shortLabel, groupPath } = nodeType === 'DaliStatement'
        ? parseStmtLabel(n.label)
        : { shortLabel: n.label, groupPath: [] as string[] };

      // For DaliRoutine nodes from exploreRoutineAggregate, the backend now populates
      // node.scope = schema_geoid and node.meta with packageName / routineType.
      const nodeMeta = Object.fromEntries((n.meta ?? []).map((m) => [m.key, m.value]));
      const schemaName  = isFlatRoutine ? (n.scope || undefined) : undefined;
      const packageName = isFlatRoutine ? (nodeMeta['packageName'] || undefined) : undefined;
      // Prefer backend-supplied routineType (e.g. "PROCEDURE") over label-parsing heuristic.
      const routineKindRaw = nodeMeta['routineType'];
      const routineKind = isFlatRoutine
        ? (routineKindRaw
            ? ({ PROCEDURE: 'PROC', FUNCTION: 'FUNC', TRIGGER: 'TRIG', PACKAGE: 'PKG' }[routineKindRaw.toUpperCase()] ?? routineKindRaw)
            : extractRoutineKind(n.label, nodeType))
        : undefined;

      return {
        id: n.id,
        type: NODE_TYPE_MAP[nodeType] ?? 'schemaNode',
        position: { x: 0, y: 0 },
        data: {
          label:             shortLabel,
          nodeType,
          childrenAvailable: DRILLABLE_TYPES.has(nodeType),
          metadata: {
            scope:       n.scope,
            dataSource:  n.dataSource || undefined,
            groupPath:   groupPath.length > 0 ? groupPath : undefined,
            fullLabel:   nodeType === 'DaliStatement' ? n.label : undefined,
            routineKind,
            schemaName,   // Phase S2.3+: schema_geoid from backend, shown in RoutineNode header
            packageName,  // Phase S2.3+: package_geoid from backend, shown in RoutineNode header
          },
          schema:    n.scope || undefined,
          columns:   outputColsByStmt.get(n.id)
                  ?? columnsByTable.get(n.id)
                  ?? fieldsByRecord.get(n.id),  // Phase S2.4 — DaliRecord fields
          operation: nodeType === 'DaliStatement' ? extractStatementType(n.label) : undefined,
        },
      };
    });

  // ── Package compound grouping (L2 AGG, package scope) ───────────────────────
  // When the backend returns a DaliPackage node + CONTAINS_ROUTINE edges,
  // group child DaliRoutine nodes as compound children of the Package container.
  // ELK positions the Package group relative to Table nodes; Routines inside
  // the group keep pre-computed relative positions (not moved by ELK).
  {
    const pkgNodeMap = new Map<string, LoomNode>();
    for (const n of nodes) {
      if (n.data.nodeType === 'DaliPackage') pkgNodeMap.set(n.id, n);
    }

    if (pkgNodeMap.size > 0) {
      // Build package → [routineId] mapping from CONTAINS_ROUTINE edges
      const routinesByPkg = new Map<string, string[]>();
      for (const e of result.edges) {
        if (e.type !== 'CONTAINS_ROUTINE') continue;
        if (!pkgNodeMap.has(e.source)) continue;
        if (!routinesByPkg.has(e.source)) routinesByPkg.set(e.source, []);
        routinesByPkg.get(e.source)!.push(e.target);
      }

      const ROUTINE_H = NODE_HEIGHT_BASE; // RoutineNode height — matches constants.ts LAYOUT.NODE_HEIGHT_BASE (80px)
      const HDR_H     = 40;              // PackageGroupNode header area
      const PAD_SIDE  = 16;              // left / right inner padding
      const PAD_INNER = 10;             // vertical gap between routines

      const nodeById = new Map(nodes.map((n) => [n.id, n]));
      for (const [pkgId, pkgNode] of pkgNodeMap) {
        const childIds = routinesByPkg.get(pkgId) ?? [];
        let yOffset = HDR_H + PAD_INNER;

        for (const rid of childIds) {
          const rNode = nodeById.get(rid);
          if (!rNode) continue;
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          (rNode as any).parentId = pkgId;
          rNode.position = { x: PAD_SIDE, y: yOffset };
          yOffset += ROUTINE_H + PAD_INNER;
        }

        // Override node type → compound container
        pkgNode.type = 'packageGroupNode';
        const childCount = childIds.filter((rid) => nodeById.has(rid)).length;
        const groupW     = NODE_WIDTH + PAD_SIDE * 2; // 400 + 32 = 432px — wide enough to contain RoutineNodes
        const groupH     = HDR_H + childCount * (ROUTINE_H + PAD_INNER) + PAD_INNER;
        pkgNode.style    = { ...(pkgNode.style ?? {}), width: groupW, height: groupH };
      }
    }
  }

  const nodeIds = new Set(nodes.map((n) => n.id));
  // Merge with external node IDs so edges connecting new expansion nodes to
  // existing graph nodes (the starting node) are not dropped prematurely.
  const allKnownIds: Set<string> =
    externalNodeIds && externalNodeIds.size > 0
      ? new Set([...nodeIds, ...externalNodeIds])
      : nodeIds;

  const regularEdges: LoomEdge[] = result.edges
    .filter((e) =>
      !SUPPRESSED_EDGES.has(e.type) &&
      allKnownIds.has(e.source) &&
      allKnownIds.has(e.target),
    )
    .map((e) => {
      const edgeType = e.type as DaliEdgeType;
      const flip = edgeType === 'READS_FROM';
      // Column-level handles (backend sets 'src-#13:X' / 'tgt-#31:Y' for
      // DATA_FLOW and FILTER_FLOW). Route them into the specific column
      // row inside the parent card; flip together with source/target for
      // READS_FROM so the swap keeps handles on the correct side.
      const srcH = e.sourceHandle && e.sourceHandle.length > 0 ? e.sourceHandle : undefined;
      const tgtH = e.targetHandle && e.targetHandle.length > 0 ? e.targetHandle : undefined;
      return {
        id:           e.id,
        source:       flip ? e.target : e.source,
        target:       flip ? e.source : e.target,
        sourceHandle: flip ? tgtH : srcH,
        targetHandle: flip ? srcH : tgtH,
        type:         'default',
        pathOptions:  { curvature: EDGE_CURVATURE },
        animated:     ANIMATED_EDGES.has(edgeType),
        style:        getEdgeStyle(edgeType),
        data:         { edgeType },
      };
    });

  const edges: LoomEdge[] = [
    ...regularEdges,
    // hoisted sub-query READS_FROM: synthetic edges already in display direction
    // (source=table, target=rootStmt) — rootStmt may be an external (existing) node.
    ...syntheticEdges.filter((e) => allKnownIds.has(e.source) && allKnownIds.has(e.target)),
  ];

  return { nodes, edges };
}

