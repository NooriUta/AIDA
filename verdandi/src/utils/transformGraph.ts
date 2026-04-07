import type { CSSProperties } from 'react';
import type { ApiGraphResponse } from '../types/api';
import type { DaliNodeType, DaliEdgeType, ColumnInfo } from '../types/domain';
import type { LoomNode, LoomEdge } from '../types/graph';
import type { ExploreResult, SchemaNode, GraphNode } from '../services/lineage';
import { L1_APP_HEADER, L1_APP_PAD_BOT, L1_DB_BASE_H, L1_DB_GAP, schemaChipY, schemaChipX, schemaChipW, schemaGridCols } from './layoutL1';

// ─── Map Dali node type → React Flow node type string ───────────────────────
const NODE_TYPE_MAP: Record<DaliNodeType, string> = {
  DaliApplication:  'applicationNode',
  DaliService:      'applicationNode',  // зарезервировано, визуально как Application
  DaliDatabase:     'databaseNode',
  DaliSchema:       'schemaNode',
  DaliPackage:      'packageNode',
  DaliTable:        'tableNode',
  DaliColumn:       'columnNode',
  DaliOutputColumn: 'columnNode',
  DaliAffectedColumn: 'columnNode',
  DaliAtom:         'atomNode',
  DaliRoutine:      'routineNode',
  DaliStatement:    'statementNode',
  DaliSession:      'routineNode',
  DaliJoin:         'routineNode',
  DaliParameter:    'columnNode',
  DaliVariable:     'columnNode',
};

// ─── Node types that support drilling down ───────────────────────────────────
// Application/Service use scope filter (not level transition) on L1 (LOOM-024)
const DRILLABLE_TYPES = new Set<DaliNodeType>([
  'DaliDatabase', 'DaliSchema', 'DaliPackage', 'DaliTable',
]);

// Scope-filter on L1: double-click Application — сужает граф до её СУБД и схем
// Database и Schema при double-click уходят на L2 (drill-down)
export const SCOPE_FILTER_TYPES = new Set<DaliNodeType>([
  'DaliApplication',
]);

// ─── SEER Design System v1.1 — Amber Forest edge colours ────────────────────
//   --inf #88B8A8  READS_FROM        solid 1.5px  smoothstep (node→node)
//   --wrn #D4922A  WRITES_TO         dashed 5 3   1.5px smoothstep (node→node)
//   --inf #88B8A8  HAS_OUTPUT_COL    bezier dashed 3 2  1px opacity 0.75 (col→col)
//   --wrn #D4922A  HAS_AFFECTED_COL  bezier dashed 3 2  1px opacity 0.75 (col→col)
//   --acc #A8B860  DATA_FLOW         animated dashed
//   --t3  #665c48  HAS_COLUMN        dashed 1px

const ANIMATED_EDGES = new Set<DaliEdgeType>([
  'DATA_FLOW', 'ATOM_PRODUCES', 'FILTER_FLOW',
  'JOIN_FLOW', 'UNION_FLOW',
]);

function getEdgeStyle(type: DaliEdgeType): CSSProperties {
  switch (type) {
    // ── L1 Application graph (LOOM-024) ──────────────────────────────────────
    case 'HAS_DATABASE':    return { stroke: '#A8B860', strokeWidth: 2 };            // Система → СУБД
    case 'CONTAINS_SCHEMA': return { stroke: '#88B8A8', strokeWidth: 1.5 };          // СУБД → Схема
    case 'HAS_SERVICE':     return { stroke: '#A8B860', strokeWidth: 1.5 };          // зарезерв.
    case 'USES_DATABASE':   return { stroke: '#665c48', strokeWidth: 1.5, strokeDasharray: '6 3' }; // зарезерв.
    // ── Data flow ────────────────────────────────────────────────────────────
    case 'READS_FROM':      return { stroke: '#88B8A8', strokeWidth: 1.5 };
    case 'WRITES_TO':       return { stroke: '#D4922A', strokeWidth: 1.5, strokeDasharray: '5 3' };
    case 'DATA_FLOW':       return { stroke: '#A8B860', strokeWidth: 1.5 };
    case 'FILTER_FLOW':     return { stroke: '#D4922A', strokeWidth: 1.5 };
    case 'JOIN_FLOW':       return { stroke: '#88B8A8', strokeWidth: 1.5 };
    case 'UNION_FLOW':      return { stroke: '#A8B860', strokeWidth: 1.5 };
    case 'ATOM_PRODUCES':   return { stroke: '#A8B860', strokeWidth: 1.5 };
    case 'ATOM_REF_COLUMN': return { stroke: '#88B8A8', strokeWidth: 1 };
    case 'HAS_ATOM':        return { stroke: '#88B8A8', strokeWidth: 1 };
    case 'HAS_COLUMN':        return { stroke: '#665c48', strokeWidth: 1, strokeDasharray: '4 3' };
    case 'CONTAINS_ROUTINE':  return { stroke: '#665c48', strokeWidth: 1, strokeDasharray: '6 3' };
    case 'CONTAINS_STMT':     return { stroke: '#665c48', strokeWidth: 1, strokeDasharray: '4 2' };
    case 'BELONGS_TO_SESSION':return { stroke: '#665c48', strokeWidth: 1, strokeDasharray: '6 3' };
    default:                  return { stroke: '#42382a', strokeWidth: 1, strokeDasharray: '4 3' };
  }
}

// ─── Main transform ──────────────────────────────────────────────────────────
export function transformGraph(response: ApiGraphResponse): {
  nodes: LoomNode[];
  edges: LoomEdge[];
} {
  const nodes: LoomNode[] = response.nodes.map((n) => ({
    id: n.id,
    type: NODE_TYPE_MAP[n.type] ?? 'schemaNode',
    position: { x: 0, y: 0 }, // overwritten by ELK layout
    data: n.data,
  }));

  const edges: LoomEdge[] = response.edges.map((e) => ({
    id: e.id,
    source: e.source,
    target: e.target,
    animated: ANIMATED_EDGES.has(e.type),
    style: getEdgeStyle(e.type),
    data: { edgeType: e.type },
  }));

  return { nodes, edges };
}

// ─── LOOM-020: GraphQL ExploreResult → LoomNode[] / LoomEdge[] ───────────────
// Used by all L2/L3 views: explore, lineage, upstream, downstream.
//
// Schema explore mode (DaliSchema + CONTAINS_TABLE/CONTAINS_ROUTINE edges):
//   — Schema becomes a React Flow group node (schemaGroupNode).
//   — Tables and own routines are children (parentId = schemaId) with pre-computed
//     grid positions relative to the group.
//   — External routines (source of READS_FROM/WRITES_TO) are top-level nodes.
//   — CONTAINS_TABLE/CONTAINS_ROUTINE edges suppressed (implicit via parentId).
//   See layoutGraph.ts → applyELKLayout compound mode for positioning logic.

// Max inline columns shown per table / statement at L2
const L2_MAX_COLS = 5;

// ─── Group layout constants ─────────────────────────────────────────────────
const GRP_W       = 420;   // inner width available for children
const GRP_HDR     = 36;    // group header height
const GRP_PAD     = 12;    // side + bottom padding inside group
const GRP_GAP     = 6;     // vertical gap between children
const NODE_W      = 400;   // leaf node width
const NODE_H_BASE = 80;    // base height (header only, no columns)
const COL_ROW_H   = 22;    // height per inline column row

// ─── Nesting edges: used to build visual hierarchy (Schema → Routine → Stmt) ─
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
  'CONTAINS_PACKAGE',                        // legacy — suppressed if present
  'CHILD_OF', 'USES_SUBQUERY', 'NESTED_IN', // sub-statement links
]);

/** Build parent → children[] map from edges matching the given type set. */
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

/** Collect all transitive descendants of rootId via containment tree. */
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

/** Extract SQL statement type (INSERT, SELECT, …) from label.
 *
 * Handles two label formats:
 *   1. Path: "PKG:PROCEDURE:LOAD:MERGE:159:SELECT:159" → first SQL keyword = MERGE
 *   2. SQL snippet: "INSERT INTO table ..." → first word = INSERT
 */
const SQL_KEYWORDS = new Set([
  'INSERT', 'SELECT', 'UPDATE', 'DELETE', 'MERGE',
  'CREATE', 'DROP', 'ALTER', 'TRUNCATE', 'CALL',
  'OPEN', 'FETCH', 'CLOSE', 'CTE', 'WITH', 'SQ',
  'CURSOR', 'DINAMIC_CURSOR', 'DYNAMIC_CURSOR',
]);

function extractStatementType(label: string): string | undefined {
  for (const part of label.split(/[\s:]+/)) {
    const upper = part.toUpperCase();
    if (SQL_KEYWORDS.has(upper)) return upper;
  }
  return undefined;
}

/** Extract short routine kind badge from label path segments.
 *  Label format: "SCHEMA.PKG:PROCEDURE:NAME:..." or "SCHEMA.PKG:FUNCTION:NAME:..."
 */
const ROUTINE_KINDS: Record<string, string> = {
  FUNCTION:  'FUNC',
  PROCEDURE: 'PROC',
};

function extractRoutineKind(label: string, nodeType: DaliNodeType): string {
  if (nodeType === 'DaliPackage') return 'PKG';
  if (nodeType === 'DaliSession') return 'SESSION';
  for (const part of label.split(/[\s:.]+/)) {
    const short = ROUTINE_KINDS[part.toUpperCase()];
    if (short) return short;
  }
  return 'ROUTINE';
}

/** Parse statement label path into hierarchy + short display label.
 *
 * Full format: "SCHEMA.PACKAGE:PROCEDURE:ROUTINE_NAME:STMT_TYPE:LINE[:STMT_TYPE:LINE...]"
 *
 * Example: "BUDM_RMS_TMD.DM_LOADER_...:PROCEDURE:LOAD_..._REG:DELETE:687"
 *   → label: "DELETE:687"
 *   → groupPath: ["BUDM_RMS_TMD", "DM_LOADER_...", "LOAD_..._REG"]
 */
const ROUTINE_TYPE_KEYWORDS = new Set(['PROCEDURE', 'FUNCTION']);

function parseStmtLabel(label: string): { shortLabel: string; groupPath: string[] } {
  if (!label.includes(':')) return { shortLabel: label, groupPath: [] };

  const parts = label.split(':');
  const groupPath: string[] = [];

  // Find the routine type keyword index
  let routineIdx = -1;
  for (let i = 0; i < parts.length; i++) {
    if (ROUTINE_TYPE_KEYWORDS.has(parts[i].toUpperCase())) { routineIdx = i; break; }
  }

  if (routineIdx >= 0) {
    // parts[0] = "SCHEMA.PACKAGE" → split by dot
    const schemaPkg = parts[0].split('.');
    groupPath.push(...schemaPkg);                      // schema, package
    if (routineIdx + 1 < parts.length) {
      groupPath.push(parts[routineIdx + 1]);           // routine name
    }
    // Statement part: everything after routine name (STMT_TYPE:LINE[:...])
    const stmtParts = parts.slice(routineIdx + 2);
    return { shortLabel: stmtParts.join(':') || label, groupPath };
  }

  // No routine type found — return last 2 segments as label
  return { shortLabel: parts.slice(-2).join(':'), groupPath: [] };
}

function isSchemaExploreResult(result: ExploreResult): boolean {
  if (!result.nodes.some((n) => n.type === 'DaliSchema')) return false;
  // Use table-group layout only when the result contains actual tables.
  // Pure-package schemas (ODS, ODS_TMD — tableCount=0) have no CONTAINS_TABLE
  // edges, so route them to the flat explore path where packages render as nodes.
  return result.edges.some((e) => e.type === 'CONTAINS_TABLE');
}

/**
 * DaliSession labels come from SHUTTLE as the raw file_path string
 * (e.g. "C:\AIDA\...\package_RMS\DM_LOADER_RMS_FCT_RWA_INPUTS.pck").
 * Extract just the filename (without extension) for display.
 */
function sessionLabel(filePath: string): string {
  // Split on both \ and / to handle Windows and Unix paths
  const parts = filePath.split(/[\\/]/);
  const filename = parts[parts.length - 1] ?? filePath;
  // Strip extension
  const dotIdx = filename.lastIndexOf('.');
  return dotIdx > 0 ? filename.slice(0, dotIdx) : filename;
}

function transformSchemaExplore(result: ExploreResult): {
  nodes: LoomNode[];
  edges: LoomEdge[];
} {
  const schemaNode = result.nodes.find((n) => n.type === 'DaliSchema')!;
  const schemaId   = schemaNode.id;
  const schemaName = schemaNode.label;

  const nestTree = buildContainmentChildren(result.edges, NESTING_EDGES);
  const nodeById = new Map(result.nodes.map((n) => [n.id, n]));


  // ── Collect columns: DaliColumn → table only (stmt columns arrive via applyStmtColumns) ────
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

  // ── Walk containment tree: collect tables and statements ─────────────────
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
          walkTree(child.id);   // recurse into containers
          break;
        default:
          walkTree(childId);
          break;
      }
    }
  }
  walkTree(schemaId);

  // ── Phase 1b: Statements connected to tables via READS_FROM / WRITES_TO ──
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

  // Merge: all schema statements + connected external statements
  const allStmtIds = new Set([...schemaStmtIds, ...connectedStmtIds]);

  // ── Build flat React Flow nodes (no groups) ──────────────────────────────
  const rfNodes: LoomNode[] = [];
  const renderedIds = new Set<string>();

  // Tables
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
        schema:            schemaName,
        columns:           columnsByParent.get(table.id),
      },
    });
    renderedIds.add(table.id);
  }

  // Statements
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
        childrenAvailable: false,
        metadata:          { scope: nd.scope, groupPath, fullLabel: nd.label },
        operation:         extractStatementType(nd.label),
        columns:           columnsByParent.get(stmtId),
      },
    });
    renderedIds.add(stmtId);
  }

  // ── Edges: data-flow between rendered nodes only ──────────────────────────
  const rfEdges: LoomEdge[] = result.edges
    .filter((e) =>
      !SUPPRESSED_EDGES.has(e.type) &&
      renderedIds.has(e.source) &&
      renderedIds.has(e.target),
    )
    .map((e) => {
      const edgeType = e.type as DaliEdgeType;
      // Flip READS_FROM so data flows Table → Statement (left → right).
      // In the DB both READS_FROM and WRITES_TO are stored as Statement → Table.
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
//   USES_SUBQUERY  source=rootStmt   → target=subStmt  (parent "uses" child)
//   CHILD_OF       source=subStmt    → target=parentStmt
//   NESTED_IN      source=subStmt    → target=parentStmt
//
// Sub-statements are not rendered on the canvas.  Their READS_FROM edges are
// hoisted to the nearest visible ancestor so the root statement exposes every
// table it — and its sub-queries — touch.
//
// Returns:
//   subqueryIds    — ids to exclude from the node list
//   syntheticEdges — new READS_FROM edges to append (root → table)
function hoistSubqueryReads(result: ExploreResult): {
  subqueryIds:    Set<string>;
  syntheticEdges: LoomEdge[];
} {
  const SUBQ_TYPES = new Set(['USES_SUBQUERY', 'CHILD_OF', 'NESTED_IN']);
  const stmtIds   = new Set(
    result.nodes.filter((n) => n.type === 'DaliStatement').map((n) => n.id),
  );

  // Build parent → [children] tree for sub-statement containment.
  const subqTree = new Map<string, string[]>();
  for (const e of result.edges) {
    if (!SUBQ_TYPES.has(e.type)) continue;
    // Normalise direction: parentId is always the "outer" statement.
    const parentId = e.type === 'USES_SUBQUERY' ? e.source : e.target;
    const childId  = e.type === 'USES_SUBQUERY' ? e.target : e.source;
    if (!stmtIds.has(parentId) || !stmtIds.has(childId)) continue;
    if (!subqTree.has(parentId)) subqTree.set(parentId, []);
    subqTree.get(parentId)!.push(childId);
  }

  if (subqTree.size === 0) return { subqueryIds: new Set(), syntheticEdges: [] };

  // Collect all sub-statement ids (direct + transitive).
  // A statement is a subquery if it appears as a child in the tree.
  const immediateSubqIds = new Set<string>();
  for (const children of subqTree.values()) {
    for (const c of children) immediateSubqIds.add(c);
  }
  const allSubqIds = new Set<string>(immediateSubqIds);
  for (const id of immediateSubqIds) {
    for (const d of collectAllDescendants(id, subqTree)) allSubqIds.add(d);
  }

  // Map every sub-query id → its root (top-level, non-subquery) ancestor.
  const subqToRoot = new Map<string, string>();
  for (const [parentId, children] of subqTree) {
    if (allSubqIds.has(parentId)) continue;  // parent is itself a child elsewhere
    for (const childId of children) {
      subqToRoot.set(childId, parentId);
      for (const d of collectAllDescendants(childId, subqTree)) {
        subqToRoot.set(d, parentId);
      }
    }
  }

  // Pre-populate seen with direct root-stmt → table READS_FROM so we never
  // create a duplicate synthetic edge.
  const seen = new Set<string>();
  for (const e of result.edges) {
    if (e.type !== 'READS_FROM') continue;
    if (stmtIds.has(e.source) && !allSubqIds.has(e.source)) {
      seen.add(`${e.source}\x00${e.target}`);
    }
  }

  // Build synthetic edges: root → table for each unique pair from subquery reads.
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
      source:   e.target,   // flip: Table → rootStmt (matches schema path display direction)
      target:   rootId,
      type:     'smoothstep',
      animated: false,
      style:    getEdgeStyle('READS_FROM'),
      data:     { edgeType: 'READS_FROM' as DaliEdgeType },
    });
  }

  return { subqueryIds: allSubqIds, syntheticEdges };
}

export function transformGqlExplore(result: ExploreResult): {
  nodes: LoomNode[];
  edges: LoomEdge[];
} {
  // Schema explore: group layout with schema as container
  if (isSchemaExploreResult(result)) {
    return transformSchemaExplore(result);
  }

  // Build column inline maps from containment edges
  // HAS_COLUMN: table → column (columns render inline in TableNode)
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

  // HAS_OUTPUT_COL / HAS_AFFECTED_COL: statement → columns (render inline in StatementNode)
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

  // ── Subquery hoisting: hide sub-statements, promote their READS_FROM ────────
  const { subqueryIds, syntheticEdges } = hoistSubqueryReads(result);

  // Flat explore (package, rid-based, lineage)
  // DaliColumn, DaliOutputColumn, DaliAffectedColumn render inline — skip as standalone nodes.
  // Sub-statement nodes (subqueryIds) are also excluded: their READS_FROM edges
  // have been promoted to the root statement by hoistSubqueryReads().
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

  // HAS_COLUMN, HAS_OUTPUT_COL, HAS_AFFECTED_COL are implicit (inline); drop dangling edges.
  // Subquery nodes were removed from nodeIds, so their outgoing/incoming edges
  // (including USES_SUBQUERY, CHILD_OF, NESTED_IN, and their own READS_FROM)
  // are automatically dropped by the nodeIds.has() guard below.
  const regularEdges: LoomEdge[] = result.edges
    .filter((e) =>
      e.type !== 'HAS_OUTPUT_COL' &&
      e.type !== 'HAS_AFFECTED_COL' &&
      e.type !== 'HAS_COLUMN' &&
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

  // Append promoted subquery READS_FROM edges (both endpoints must be rendered).
  const edges: LoomEdge[] = [
    ...regularEdges,
    ...syntheticEdges.filter((e) => nodeIds.has(e.source) && nodeIds.has(e.target)),
  ];

  return { nodes, edges };
}

// ─── Statement column enrichment (second-pass) ───────────────────────────────
//
// Called after schema explore + layout, with the result of fetchStmtColumns().
// Builds a stmtId → ColumnInfo[] map and patches existing LoomNodes in-place.
// No re-layout needed: only node.data is updated.

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
  //           and READS_FROM (source=tableId, target=stmtId)  — flipped for display.
  const renderedIds = new Set(nodes.map((n) => n.id));
  const cfEdges: LoomEdge[] = [];
  const cfSeen  = new Set<string>();

  for (const e of baseEdges) {
    const edgeType = e.data?.edgeType as string | undefined;
    if (edgeType !== 'WRITES_TO' && edgeType !== 'READS_FROM') continue;

    // Recover stmtId / tableId regardless of display orientation
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

// ─── LOOM-024 v3: L1 three-level grouped layout ───────────────────────────────
//
// ApplicationNode (group parent)
//   └── DatabaseNode   (parentId: appId,  extent:'parent')
//         └── L1SchemaNode (parentId: dbId, extent:'parent', hidden: true initially)
//
// Positions pre-computed; ELK skipped for L1 (see LoomCanvas + layoutL1.ts).
//
// Entry point: transformGqlOverview() — auto-detects data richness:
//   Real mode  (SHUTTLE provides databaseGeoid): groups by real Application/Database.
//   Synthetic  (flat schema list):              buckets into HoundDB / System-N stubs.

const L1_APP_COLORS      = ['#A8B860', '#88B8A8', '#D4922A', '#7DBF78', '#c87f3c'];
const L1_APP_WIDTH       = 220;
const L1_DB_WIDTH        = 204;  // L1_APP_WIDTH - 8*2 margins
const L1_APP_X_GAP       = 32;
const L1_SCHEMAS_PER_DB  = 5;
const L1_SCHEMAS_PER_APP = L1_SCHEMAS_PER_DB * 2; // 10

// ── Shared node-creation helpers ──────────────────────────────────────────────

/** Push one L1SchemaNode chip (hidden by default, shown when parent DB expands). */
function pushSchemaChip(
  nodes:  LoomNode[],
  schema: SchemaNode,
  dbId:   string,
  idx:    number,
  color:  string,
  cols:   number,
): void {
  nodes.push({
    id:       schema.id,
    type:     'l1SchemaNode',
    position: { x: schemaChipX(idx, cols), y: schemaChipY(idx, cols) },
    parentId: dbId,
    extent:   'parent' as const,
    hidden:   true,
    width:    schemaChipW(cols),
    height:   20,
    style:    { width: schemaChipW(cols), height: 20 },
    data: {
      label:             schema.name,
      nodeType:          'DaliSchema' as DaliNodeType,
      childrenAvailable: true,
      metadata:          { color, databaseName: schema.databaseName ?? null },
      tablesCount:       schema.tableCount,
      routinesCount:     schema.packageCount,  // CONTAINS_ROUTINE → DaliPackage count
    },
  });
}

/** Push a standalone DatabaseNode (no parentId, top-level) + its schema chips.
 *  Returns curX advanced past the node.
 *  drillable=true only when dbId is a real ArcadeDB @rid (real mode). */
function pushStandaloneDb(
  nodes:    LoomNode[],
  dbId:     string,
  dbLabel:  string,
  dbEngine: string,
  schemas:  SchemaNode[],
  color:    string,
  curX:     number,
  drillable = true,
): number {
  const totalTables = schemas.reduce((s, sch) => s + sch.tableCount, 0);
  nodes.push({
    id:       dbId,
    type:     'databaseNode',
    position: { x: curX, y: 20 },
    width:    L1_DB_WIDTH,
    height:   L1_DB_BASE_H,
    style:    { width: L1_DB_WIDTH },
    data: {
      label:             dbLabel,
      nodeType:          'DaliDatabase' as DaliNodeType,
      childrenAvailable: drillable,
      metadata:          { color, engine: dbEngine, tableCount: totalTables, schemaCount: schemas.length },
      tablesCount:       totalTables,
    },
  });
  const cols = schemaGridCols(schemas.length);
  schemas.forEach((sch, i) => pushSchemaChip(nodes, sch, dbId, i, color, cols));
  return curX + L1_DB_WIDTH + L1_APP_X_GAP;
}

/** Push a DatabaseNode as a child of an ApplicationNode group + its schema chips.
 *  drillable=true only when dbId is a real ArcadeDB @rid (real mode). */
function pushGroupedDb(
  nodes:    LoomNode[],
  dbId:     string,
  dbLabel:  string,
  dbEngine: string,
  schemas:  SchemaNode[],
  parentId: string,
  dbY:      number,
  color:    string,
  drillable = true,
): void {
  const totalTables = schemas.reduce((s, sch) => s + sch.tableCount, 0);
  nodes.push({
    id:       dbId,
    type:     'databaseNode',
    position: { x: 8, y: dbY },
    parentId,
    extent:   'parent' as const,
    width:    L1_DB_WIDTH,
    height:   L1_DB_BASE_H,
    style:    { width: L1_DB_WIDTH },
    data: {
      label:             dbLabel,
      nodeType:          'DaliDatabase' as DaliNodeType,
      childrenAvailable: drillable,
      metadata:          { color, engine: dbEngine, tableCount: totalTables, schemaCount: schemas.length },
      tablesCount:       totalTables,
    },
  });
  const cols = schemaGridCols(schemas.length);
  schemas.forEach((sch, i) => pushSchemaChip(nodes, sch, dbId, i, color, cols));
}

// ── Real L1 builder — uses databaseGeoid / applicationGeoid from SHUTTLE ───────

function buildRealL1(schemas: SchemaNode[]): { nodes: LoomNode[]; edges: LoomEdge[] } {
  const nodes: LoomNode[] = [];
  let curX     = 20;
  let colorIdx = 0;

  type DbEntry  = { name: string; engine: string; schemas: SchemaNode[] };
  type AppEntry = { name: string; dbs: Map<string, DbEntry> };

  const appMap    = new Map<string, AppEntry>(); // applicationGeoid → entry
  const orphanDbs = new Map<string, DbEntry>();  // databaseGeoid → entry (no application)
  const stubBucket: SchemaNode[] = [];           // no databaseGeoid, no applicationGeoid

  for (const s of schemas) {
    const dbKey  = s.databaseGeoid  ?? '__stub__';
    const appKey = s.applicationGeoid ?? null;

    if (appKey) {
      if (!appMap.has(appKey)) {
        appMap.set(appKey, { name: s.applicationName ?? appKey, dbs: new Map() });
      }
      const app = appMap.get(appKey)!;
      if (!app.dbs.has(dbKey)) {
        app.dbs.set(dbKey, {
          name:    dbKey === '__stub__' ? 'HoundDB' : (s.databaseName  ?? 'HoundDB'),
          engine:  s.databaseEngine ?? '',
          schemas: [],
        });
      }
      app.dbs.get(dbKey)!.schemas.push(s);
    } else if (s.databaseGeoid) {
      if (!orphanDbs.has(dbKey)) {
        orphanDbs.set(dbKey, {
          name:    s.databaseName  ?? 'HoundDB',
          engine:  s.databaseEngine ?? '',
          schemas: [],
        });
      }
      orphanDbs.get(dbKey)!.schemas.push(s);
    } else {
      stubBucket.push(s);
    }
  }

  // ── Application groups
  for (const [appGeoid, app] of appMap) {
    const color   = L1_APP_COLORS[colorIdx++ % L1_APP_COLORS.length];
    const dbList  = [...app.dbs.entries()];
    const dbCount = dbList.length;

    if (dbCount === 1) {
      // Single-DB app → render DB standalone (no App wrapper)
      const [dbGeoid, db] = dbList[0];
      curX = pushStandaloneDb(nodes, dbGeoid, db.name, db.engine, db.schemas, color, curX);
    } else {
      const appH = L1_APP_HEADER + dbCount * L1_DB_BASE_H + (dbCount - 1) * L1_DB_GAP + L1_APP_PAD_BOT;
      nodes.push({
        id:       appGeoid,
        type:     'applicationNode',
        position: { x: curX, y: 20 },
        width:    L1_APP_WIDTH,
        height:   appH,
        style:    { width: L1_APP_WIDTH, height: appH },
        data: {
          label:             app.name,
          nodeType:          'DaliApplication' as DaliNodeType,
          childrenAvailable: false,
          metadata:          { color, databaseCount: dbCount },
        },
      });
      dbList.forEach(([dbGeoid, db], idx) => {
        const dbY = L1_APP_HEADER + idx * (L1_DB_BASE_H + L1_DB_GAP);
        pushGroupedDb(nodes, dbGeoid, db.name, db.engine, db.schemas, appGeoid, dbY, color);
      });
      curX += L1_APP_WIDTH + L1_APP_X_GAP;
    }
  }

  // ── Orphan DBs (databaseGeoid set, applicationGeoid absent)
  for (const [dbGeoid, db] of orphanDbs) {
    const color = L1_APP_COLORS[colorIdx++ % L1_APP_COLORS.length];
    curX = pushStandaloneDb(nodes, dbGeoid, db.name, db.engine, db.schemas, color, curX);
  }

  // ── Stub bucket (schemas with neither databaseGeoid nor applicationGeoid)
  // l1-stub-hound is a synthetic placeholder — not drillable via SHUTTLE
  if (stubBucket.length > 0) {
    const color = L1_APP_COLORS[colorIdx++ % L1_APP_COLORS.length];
    curX = pushStandaloneDb(nodes, 'l1-stub-hound', 'HoundDB', '', stubBucket, color, curX, false);
  }

  return { nodes, edges: [] };
}

// ── Synthetic L1 builder — fallback when SHUTTLE provides flat schema list ─────

function buildSyntheticL1(schemas: SchemaNode[]): { nodes: LoomNode[]; edges: LoomEdge[] } {
  const nodes: LoomNode[] = [];
  let curX = 20;

  for (let i = 0; i < schemas.length; i += L1_SCHEMAS_PER_APP) {
    const appBucket = schemas.slice(i, i + L1_SCHEMAS_PER_APP);
    const appIndex  = Math.floor(i / L1_SCHEMAS_PER_APP);
    const color     = L1_APP_COLORS[appIndex % L1_APP_COLORS.length];

    const dbBuckets: SchemaNode[][] = [];
    for (let j = 0; j < appBucket.length; j += L1_SCHEMAS_PER_DB) {
      dbBuckets.push(appBucket.slice(j, j + L1_SCHEMAS_PER_DB));
    }
    const dbCount = dbBuckets.length;

    if (dbCount === 1) {
      // Synthetic stub ID — not drillable (SHUTTLE can't resolve l1-db-N)
      curX = pushStandaloneDb(nodes, `l1-db-${appIndex}-0`, 'HoundDB', '', dbBuckets[0], color, curX, false);
      continue;
    }

    const appId = `l1-app-${appIndex}`;
    const appH  = L1_APP_HEADER + dbCount * L1_DB_BASE_H + (dbCount - 1) * L1_DB_GAP + L1_APP_PAD_BOT;
    nodes.push({
      id:       appId,
      type:     'applicationNode',
      position: { x: curX, y: 20 },
      width:    L1_APP_WIDTH,
      height:   appH,
      style:    { width: L1_APP_WIDTH, height: appH },
      data: {
        label:             `System-${appIndex + 1}`,
        nodeType:          'DaliApplication' as DaliNodeType,
        childrenAvailable: false,
        metadata:          { color, databaseCount: dbCount },
      },
    });

    dbBuckets.forEach((dbSchemas, dbIdx) => {
      const dbLabel = dbCount > 1 ? `HoundDB-${dbIdx + 1}` : 'HoundDB';
      const dbY     = L1_APP_HEADER + dbIdx * (L1_DB_BASE_H + L1_DB_GAP);
      // Synthetic stub IDs — not drillable until SHUTTLE provides real databaseGeoid
      pushGroupedDb(nodes, `l1-db-${appIndex}-${dbIdx}`, dbLabel, '', dbSchemas, appId, dbY, color, false);
    });

    curX += L1_APP_WIDTH + L1_APP_X_GAP;
  }

  return { nodes, edges: [] };
}

// ── Public entry point ────────────────────────────────────────────────────────

/**
 * SHUTTLE overview → L1 RF node tree (App → DB → Schema).
 *
 * Real mode:      any SchemaNode has `databaseGeoid` set → real names + real grouping.
 * Synthetic mode: flat schema list → stub DBs (HoundDB) and stub Apps (System-N).
 */
export function transformGqlOverview(schemas: SchemaNode[]): {
  nodes: LoomNode[];
  edges: LoomEdge[];
} {
  if (schemas.length === 0) return { nodes: [], edges: [] };

  return schemas.some(s => s.databaseGeoid != null)
    ? buildRealL1(schemas)
    : buildSyntheticL1(schemas);
}
