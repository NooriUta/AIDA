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

// Max inline columns shown per table / statement at L2
const L2_MAX_COLS = 5;

// ─── Nesting edges: build visual hierarchy (Schema → Routine → Stmt) ─────────
// CONTAINS_ROUTINE is dual-purpose: Schema→Routine AND Routine→Routine.
// CHILD_OF / USES_SUBQUERY / NESTED_IN are NOT included — sub-statements are
// invisible at L2 (they can be explored by drilling into a statement at L3).
const NESTING_EDGES = new Set<string>([
  'CONTAINS_ROUTINE', 'CONTAINS_STMT', 'CONTAINS_PACKAGE',
  'CONTAINS_TABLE', 'BELONGS_TO_SESSION',
  'HAS_COLUMN', 'HAS_OUTPUT_COL', 'HAS_AFFECTED_COL', 'HAS_PARAMETER', 'HAS_VARIABLE',
]);

// ─── Suppressed edges: ALL structural/containment edges hidden from arrows ───
// Superset of NESTING_EDGES.  Includes sub-statement links that are NOT rendered
// at L2 but whose targets must still be excluded from the "external nodes" list.
const SUPPRESSED_EDGES = new Set<string>([
  ...NESTING_EDGES,
  'CONTAINS_PACKAGE',
  'CHILD_OF', 'USES_SUBQUERY', 'NESTED_IN',
  'CALLS',
  'ROUTINE_USES_TABLE',
  'ATOM_REF_TABLE',
  'ATOM_REF_COLUMN',
  'ATOM_PRODUCES',
  'HAS_JOIN',
  'HAS_DATABASE',
  'CONTAINS_SCHEMA',
  'HAS_SERVICE',
  'USES_DATABASE',
]);

// ─── L2 edge whitelist: only data-flow arrows on the canvas ─────────────────
const L2_FLOW_EDGES = new Set<string>(['READS_FROM', 'WRITES_TO', 'DATA_FLOW']);

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
    const colNode = result.nodes.find((nd) => nd.id === e.target && nd.type === 'DaliColumn');
    if (!colNode) continue;
    if (!columnsByParent.has(e.source)) columnsByParent.set(e.source, []);
    const cols = columnsByParent.get(e.source)!;
    if (cols.length < L2_MAX_COLS) {
      cols.push({ id: colNode.id, name: colNode.label, type: '', isPrimaryKey: false, isForeignKey: false });
    }
  }

  // Walk containment tree: collect tables and statements
  const schemaTables: GraphNode[] = [];
  const schemaTableIds = new Set<string>();
  const schemaStmtIds = new Set<string>();

  function walkTree(parentId: string) {
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
        metadata:          { scope: table.scope },
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
        metadata:          { scope: nd.scope },
        schema:            tableSchemaName.get(extId) ?? nd.scope ?? '',
        columns:           columnsByParent.get(extId),
      },
    });
    renderedIds.add(extId);
  }

  for (const stmtId of allStmtIds) {
    const nd = nodeById.get(stmtId);
    if (!nd) continue;
    const { shortLabel, groupPath } = parseStmtLabel(nd.label);
    rfNodes.push({
      id:       stmtId,
      type:     'statementNode',
      position: { x: 0, y: 0 },
      data: {
        label:             shortLabel,
        nodeType:          'DaliStatement' as DaliNodeType,
        childrenAvailable: true,
        metadata:          { scope: nd.scope, groupPath, fullLabel: nd.label },
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
      return {
        id:       e.id,
        source:   flip ? e.target : e.source,
        target:   flip ? e.source : e.target,
        type:     'smoothstep',
        animated: ANIMATED_EDGES.has(edgeType),
        style:    getEdgeStyle(edgeType),
        data:     { edgeType },
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
      type:     'smoothstep',
      animated: false,
      style:    getEdgeStyle('READS_FROM'),
      data:     { edgeType: 'READS_FROM' as DaliEdgeType },
    });
  }

  return { subqueryIds: allSubqIds, syntheticEdges };
}

// ─── Public: flat + schema explore dispatcher ────────────────────────────────

export function transformGqlExplore(result: ExploreResult): {
  nodes: LoomNode[];
  edges: LoomEdge[];
} {
  if (isSchemaExploreResult(result)) {
    return transformSchemaExplore(result);
  }

  const columnsByTable = new Map<string, ColumnInfo[]>();
  for (const e of result.edges) {
    if (e.type !== 'HAS_COLUMN') continue;
    const colNode = result.nodes.find((nd) => nd.id === e.target && nd.type === 'DaliColumn');
    if (!colNode) continue;
    if (!columnsByTable.has(e.source)) columnsByTable.set(e.source, []);
    const cols = columnsByTable.get(e.source)!;
    if (cols.length < L2_MAX_COLS) {
      cols.push({ id: colNode.id, name: colNode.label, type: '', isPrimaryKey: false, isForeignKey: false });
    }
  }

  const outputColsByStmt = new Map<string, ColumnInfo[]>();
  for (const e of result.edges) {
    if (e.type !== 'HAS_OUTPUT_COL' && e.type !== 'HAS_AFFECTED_COL') continue;
    const colNode = result.nodes.find((nd) => nd.id === e.target && (nd.type === 'DaliOutputColumn' || nd.type === 'DaliColumn' || nd.type === 'DaliAffectedColumn'));
    if (!colNode) continue;
    if (!outputColsByStmt.has(e.source)) outputColsByStmt.set(e.source, []);
    const cols = outputColsByStmt.get(e.source)!;
    if (cols.length < L2_MAX_COLS) {
      cols.push({ id: colNode.id, name: colNode.label, type: '', isPrimaryKey: false, isForeignKey: false });
    }
  }

  const { subqueryIds, syntheticEdges } = hoistSubqueryReads(result);

  const nodes: LoomNode[] = result.nodes
    .filter((n) =>
      n.type !== 'DaliOutputColumn' &&
      n.type !== 'DaliColumn'       &&
      n.type !== 'DaliAffectedColumn' &&
      !subqueryIds.has(n.id),
    )
    .map((n) => {
      const nodeType = n.type as DaliNodeType;
      const isFlatRoutine = nodeType === 'DaliRoutine' || nodeType === 'DaliSession' || nodeType === 'DaliPackage';
      const { shortLabel, groupPath } = nodeType === 'DaliStatement'
        ? parseStmtLabel(n.label)
        : { shortLabel: n.label, groupPath: [] as string[] };
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
            groupPath:   groupPath.length > 0 ? groupPath : undefined,
            fullLabel:   nodeType === 'DaliStatement' ? n.label : undefined,
            routineKind: isFlatRoutine ? extractRoutineKind(n.label, nodeType) : undefined,
          },
          schema:    n.scope || undefined,
          columns:   outputColsByStmt.get(n.id) ?? columnsByTable.get(n.id),
          operation: nodeType === 'DaliStatement' ? extractStatementType(n.label) : undefined,
        },
      };
    });

  const nodeIds = new Set(nodes.map((n) => n.id));

  const regularEdges: LoomEdge[] = result.edges
    .filter((e) =>
      !SUPPRESSED_EDGES.has(e.type) &&
      nodeIds.has(e.source) &&
      nodeIds.has(e.target),
    )
    .map((e) => {
      const edgeType = e.type as DaliEdgeType;
      const flip = edgeType === 'READS_FROM';
      return {
        id:       e.id,
        source:   flip ? e.target : e.source,
        target:   flip ? e.source : e.target,
        type:     'smoothstep',
        animated: ANIMATED_EDGES.has(edgeType),
        style:    getEdgeStyle(edgeType),
        data:     { edgeType },
      };
    });

  const edges: LoomEdge[] = [
    ...regularEdges,
    ...syntheticEdges.filter((e) => nodeIds.has(e.source) && nodeIds.has(e.target)),
  ];

  return { nodes, edges };
}

