import type { SchemaNode } from '../services/lineage';
import type { DaliNodeType } from '../types/domain';
import type { LoomNode, LoomEdge } from '../types/graph';
import {
  L1_APP_HEADER,
  L1_APP_PAD_BOT,
  L1_DB_BASE_H,
  L1_DB_GAP,
  schemaChipY,
  schemaChipX,
  schemaChipW,
  schemaGridCols,
} from './layoutL1';

// ─── L1 layout constants ─────────────────────────────────────────────────────
const L1_APP_COLORS      = ['#A8B860', '#88B8A8', '#D4922A', '#7DBF78', '#c87f3c'];
const L1_APP_WIDTH       = 220;
const L1_DB_WIDTH        = 204;  // L1_APP_WIDTH - 8*2 margins
const L1_APP_X_GAP       = 32;
const L1_SCHEMAS_PER_DB  = 5;
const L1_SCHEMAS_PER_APP = L1_SCHEMAS_PER_DB * 2; // 10

// ─── Internal node-creation helpers ──────────────────────────────────────────

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
      routinesCount:     schema.packageCount,
    },
  });
}

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
      schemas:           schemas.map(s => ({ id: s.id, name: s.name, tableCount: s.tableCount })),
    },
  });
  const cols = schemaGridCols(schemas.length);
  schemas.forEach((sch, i) => pushSchemaChip(nodes, sch, dbId, i, color, cols));
  return curX + L1_DB_WIDTH + L1_APP_X_GAP;
}

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
      schemas:           schemas.map(s => ({ id: s.id, name: s.name, tableCount: s.tableCount })),
    },
  });
  const cols = schemaGridCols(schemas.length);
  schemas.forEach((sch, i) => pushSchemaChip(nodes, sch, dbId, i, color, cols));
}

// ─── Real L1 builder — uses databaseGeoid / applicationGeoid from SHUTTLE ────

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

  // Application groups
  for (const [appGeoid, app] of appMap) {
    const color   = L1_APP_COLORS[colorIdx++ % L1_APP_COLORS.length];
    const dbList  = [...app.dbs.entries()];
    const dbCount = dbList.length;

    if (dbCount === 1) {
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

  // Orphan DBs (databaseGeoid set, applicationGeoid absent)
  for (const [dbGeoid, db] of orphanDbs) {
    const color = L1_APP_COLORS[colorIdx++ % L1_APP_COLORS.length];
    curX = pushStandaloneDb(nodes, dbGeoid, db.name, db.engine, db.schemas, color, curX);
  }

  // Stub bucket (schemas with neither databaseGeoid nor applicationGeoid)
  if (stubBucket.length > 0) {
    const color = L1_APP_COLORS[colorIdx++ % L1_APP_COLORS.length];
    curX = pushStandaloneDb(nodes, 'l1-stub-hound', 'HoundDB', '', stubBucket, color, curX, false);
  }

  return { nodes, edges: [] };
}

// ─── Synthetic L1 builder — fallback when SHUTTLE provides flat schema list ──

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
      pushGroupedDb(nodes, `l1-db-${appIndex}-${dbIdx}`, dbLabel, '', dbSchemas, appId, dbY, color, false);
    });

    curX += L1_APP_WIDTH + L1_APP_X_GAP;
  }

  return { nodes, edges: [] };
}

// ─── Public entry point ───────────────────────────────────────────────────────

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
