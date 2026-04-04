import type { CSSProperties } from 'react';
import type { ApiGraphResponse } from '../types/api';
import type { DaliNodeType, DaliEdgeType } from '../types/domain';
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
  DaliStatement:    'routineNode',
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

export function transformGqlExplore(result: ExploreResult): {
  nodes: LoomNode[];
  edges: LoomEdge[];
} {
  const nodes: LoomNode[] = result.nodes.map((n) => {
    const nodeType = n.type as DaliNodeType;
    return {
      id: n.id,
      type: NODE_TYPE_MAP[nodeType] ?? 'schemaNode',
      position: { x: 0, y: 0 },
      data: {
        label: n.label,
        nodeType,
        childrenAvailable: DRILLABLE_TYPES.has(nodeType),
        metadata: { scope: n.scope },
        schema: n.scope || undefined,
      },
    };
  });

  const edges: LoomEdge[] = result.edges.map((e) => {
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
