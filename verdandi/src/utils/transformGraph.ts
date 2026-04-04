import type { CSSProperties } from 'react';
import type { ApiGraphResponse } from '../types/api';
import type { DaliNodeType, DaliEdgeType, ColumnInfo } from '../types/domain';
import type { LoomNode, LoomEdge } from '../types/graph';
import type { ExploreResult, SchemaNode } from '../services/lineage';
import { L1_APP_HEADER, L1_APP_PAD_BOT, L1_DB_BASE_H, L1_DB_GAP, schemaChipY } from './layoutL1';

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
//   --inf #88B8A8  READS_FROM   solid 1.5px
//   --wrn #D4922A  WRITES_TO    dashed 5 3  1.5px
//   --acc #A8B860  DATA_FLOW    animated dashed
//   --t3  #665c48  HAS_COLUMN   dashed 1px

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
    case 'HAS_COLUMN':      return { stroke: '#665c48', strokeWidth: 1, strokeDasharray: '4 3' };
    default:                return { stroke: '#42382a', strokeWidth: 1, strokeDasharray: '4 3' };
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

// Grid layout constants for schema group children
const L2_CHILD_W     = 240;
const L2_CHILD_H_BASE= 80;   // table header height (no columns)
const L2_COL_ROW_H   = 22;   // height of one column row in TableNode
const L2_COL_EXTRA   = 24;   // TableNode bottom padding when columns are shown
const L2_MAX_COLS    = 5;    // max column rows shown per table in L2
const L2_GAP_X       = 16;
const L2_GAP_Y       = 12;
const L2_GROUP_HDR   = 44;   // schema group header height
const L2_GROUP_PAD_S = 20;   // left / right padding
const L2_GROUP_PAD_B = 20;   // bottom padding

function isSchemaExploreResult(result: ExploreResult): boolean {
  if (!result.nodes.some((n) => n.type === 'DaliSchema')) return false;
  return result.edges.some(
    (e) => e.type === 'CONTAINS_TABLE' || e.type === 'CONTAINS_ROUTINE',
  );
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

  // IDs of nodes directly owned by the schema (tables + own routines)
  const childIds = new Set(
    result.edges
      .filter((e) => e.source === schemaId && (e.type === 'CONTAINS_TABLE' || e.type === 'CONTAINS_ROUTINE'))
      .map((e) => e.target),
  );

  // ── Column data: group DaliColumn nodes by their parent table ─────────────
  // HAS_COLUMN edges: source = tableId, target = columnId
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

  // ── Per-table cell heights (variable: depends on column count) ───────────
  const childList = result.nodes.filter((nd) => childIds.has(nd.id));

  function cellHeight(nodeId: string, nodeType: string): number {
    if (nodeType !== 'DaliTable') return L2_CHILD_H_BASE;
    const colCount = Math.min(columnsByTable.get(nodeId)?.length ?? 0, L2_MAX_COLS);
    return L2_CHILD_H_BASE + colCount * L2_COL_ROW_H + (colCount > 0 ? L2_COL_EXTRA : 0);
  }

  // ── Grid layout ───────────────────────────────────────────────────────────
  const childCount = childIds.size;
  const gridCols   = childCount <= 3 ? 1 : childCount <= 8 ? 2 : childCount <= 15 ? 3 : 4;
  const gridRows   = Math.ceil(childCount / gridCols);
  const groupW     = L2_GROUP_PAD_S * 2 + gridCols * L2_CHILD_W + (gridCols - 1) * L2_GAP_X;

  // Per-row max heights (so each row accommodates its tallest cell)
  const rowMaxH: number[] = Array.from({ length: gridRows }, (_, row) => {
    let maxH = L2_CHILD_H_BASE;
    for (let gc = 0; gc < gridCols; gc++) {
      const idx = row * gridCols + gc;
      if (idx >= childList.length) break;
      maxH = Math.max(maxH, cellHeight(childList[idx].id, childList[idx].type));
    }
    return maxH;
  });

  // Cumulative Y offsets for each row
  const rowY: number[] = [];
  let yAcc = L2_GROUP_HDR;
  for (let row = 0; row < gridRows; row++) {
    rowY.push(yAcc);
    yAcc += rowMaxH[row] + L2_GAP_Y;
  }
  const groupH = yAcc - L2_GAP_Y + L2_GROUP_PAD_B;

  const rfNodes: LoomNode[] = [];

  // ── Schema group node ─────────────────────────────────────────────────────
  rfNodes.push({
    id:       schemaId,
    type:     'schemaGroupNode',
    position: { x: 0, y: 0 },
    style:    { width: groupW, height: groupH },
    data: {
      label:             schemaNode.label,
      nodeType:          'DaliSchema',
      childrenAvailable: false,
      metadata:          {},
    },
  });

  // ── Children: tables + own routines + sessions inside the group ─────────
  childList.forEach((nd, idx) => {
    const gc       = idx % gridCols;
    const gr       = Math.floor(idx / gridCols);
    const nodeType = nd.type as DaliNodeType;
    const ch       = cellHeight(nd.id, nd.type);
    // DaliSession labels are raw file paths — extract filename for display
    const label = nodeType === 'DaliSession' ? sessionLabel(nd.label) : nd.label;
    rfNodes.push({
      id:       nd.id,
      type:     NODE_TYPE_MAP[nodeType] ?? 'tableNode',
      position: {
        x: L2_GROUP_PAD_S + gc * (L2_CHILD_W + L2_GAP_X),
        y: rowY[gr],
      },
      parentId: schemaId,
      extent:   'parent' as const,
      style:    { width: L2_CHILD_W, height: ch },
      data: {
        label,
        nodeType,
        childrenAvailable: DRILLABLE_TYPES.has(nodeType),
        metadata:          { scope: nd.scope },
        schema:            nd.scope || undefined,
        columns:           columnsByTable.get(nd.id),
      },
    });
  });

  // ── External nodes (tables accessed from outside the schema group) ─────────
  // DaliColumn + DaliOutputColumn skipped — they're inline in table/statement cards.
  for (const nd of result.nodes) {
    if (nd.id === schemaId || childIds.has(nd.id) || nd.type === 'DaliColumn' || nd.type === 'DaliOutputColumn') continue;
    const nodeType = nd.type as DaliNodeType;
    // DaliSession labels are raw file paths — extract filename for display
    const label = nodeType === 'DaliSession' ? sessionLabel(nd.label) : nd.label;
    rfNodes.push({
      id:       nd.id,
      type:     NODE_TYPE_MAP[nodeType] ?? 'routineNode',
      position: { x: 0, y: 0 },
      data: {
        label,
        nodeType,
        childrenAvailable: DRILLABLE_TYPES.has(nodeType),
        metadata:          { scope: nd.scope },
        schema:            nd.scope || undefined,
      },
    });
  }

  // ── Edges: suppress containment + inline-column edges ────────────────────
  const rfEdges: LoomEdge[] = result.edges
    .filter((e) => e.type !== 'CONTAINS_TABLE' && e.type !== 'CONTAINS_ROUTINE' && e.type !== 'HAS_COLUMN' && e.type !== 'HAS_OUTPUT_COL')
    .map((e) => {
      const edgeType = e.type as DaliEdgeType;
      return {
        id:       e.id,
        source:   e.source,
        target:   e.target,
        animated: ANIMATED_EDGES.has(edgeType),
        style:    getEdgeStyle(edgeType),
        data:     { edgeType },
      };
    });

  return { nodes: rfNodes, edges: rfEdges };
}

export function transformGqlExplore(result: ExploreResult): {
  nodes: LoomNode[];
  edges: LoomEdge[];
} {
  // Schema explore: group layout with schema as container
  if (isSchemaExploreResult(result)) {
    return transformSchemaExplore(result);
  }

  // Build output-column map: DaliStatement id → ColumnInfo[] (from HAS_OUTPUT_COL edges)
  const outputColsByStmt = new Map<string, ColumnInfo[]>();
  for (const e of result.edges) {
    if (e.type !== 'HAS_OUTPUT_COL') continue;
    const colNode = result.nodes.find((nd) => nd.id === e.target && nd.type === 'DaliOutputColumn');
    if (!colNode) continue;
    if (!outputColsByStmt.has(e.source)) outputColsByStmt.set(e.source, []);
    const cols = outputColsByStmt.get(e.source)!;
    if (cols.length < L2_MAX_COLS) {
      cols.push({ id: colNode.id, name: colNode.label, type: '', isPrimaryKey: false, isForeignKey: false });
    }
  }

  // Flat explore (package, rid-based, lineage)
  // DaliOutputColumn nodes are skipped — they render inline inside StatementNode.
  const nodes: LoomNode[] = result.nodes
    .filter((n) => n.type !== 'DaliOutputColumn')
    .map((n) => {
      const nodeType = n.type as DaliNodeType;
      return {
        id: n.id,
        type: NODE_TYPE_MAP[nodeType] ?? 'schemaNode',
        position: { x: 0, y: 0 },
        data: {
          label:             n.label,
          nodeType,
          childrenAvailable: DRILLABLE_TYPES.has(nodeType),
          metadata:          { scope: n.scope },
          schema:            n.scope || undefined,
          columns:           outputColsByStmt.get(n.id),
        },
      };
    });

  // HAS_OUTPUT_COL edges are implicit (inline in StatementNode); suppress them.
  const edges: LoomEdge[] = result.edges
    .filter((e) => e.type !== 'HAS_OUTPUT_COL')
    .map((e) => {
      const edgeType = e.type as DaliEdgeType;
      return {
        id: e.id,
        source: e.source,
        target: e.target,
        animated: ANIMATED_EDGES.has(edgeType),
        style: getEdgeStyle(edgeType),
        data: { edgeType },
      };
    });

  return { nodes, edges };
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
const L1_SCH_MARGIN      = 4;
const L1_SCH_WIDTH       = L1_DB_WIDTH - L1_SCH_MARGIN * 2; // 196px
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
): void {
  nodes.push({
    id:       schema.id,
    type:     'l1SchemaNode',
    position: { x: L1_SCH_MARGIN, y: schemaChipY(idx) },
    parentId: dbId,
    extent:   'parent' as const,
    hidden:   true,
    style:    { width: L1_SCH_WIDTH, height: 20 },
    data: {
      label:             schema.name,
      nodeType:          'DaliSchema' as DaliNodeType,
      childrenAvailable: true,
      metadata:          { color },
      tablesCount:       schema.tableCount,
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
    style:    { width: L1_DB_WIDTH },
    data: {
      label:             dbLabel,
      nodeType:          'DaliDatabase' as DaliNodeType,
      childrenAvailable: drillable,
      metadata:          { color, engine: dbEngine, tableCount: totalTables, schemaCount: schemas.length },
      tablesCount:       totalTables,
    },
  });
  schemas.forEach((sch, i) => pushSchemaChip(nodes, sch, dbId, i, color));
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
    style:    { width: L1_DB_WIDTH },
    data: {
      label:             dbLabel,
      nodeType:          'DaliDatabase' as DaliNodeType,
      childrenAvailable: drillable,
      metadata:          { color, engine: dbEngine, tableCount: totalTables, schemaCount: schemas.length },
      tablesCount:       totalTables,
    },
  });
  schemas.forEach((sch, i) => pushSchemaChip(nodes, sch, dbId, i, color));
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
