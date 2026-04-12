// src/utils/layoutL1.ts
// LOOM-024 v3: Dynamic L1 group layout — handles DB schema expansion
//
// Layout constants mirror the rendered sizes in ApplicationNode.tsx,
// DatabaseNode.tsx, and L1SchemaNode.tsx.

import type { LoomNode } from '../types/graph';

// ─── Sizing constants ─────────────────────────────────────────────────────────
// Keep in sync with component CSS/inline styles.

export const L1_APP_HEADER    = 56;  // ApplicationNode: header (38) + meta (18)
export const L1_APP_PAD_BOT   = 8;   // padding below last DB inside App group
export const L1_DB_GAP        = 6;   // vertical gap between stacked DB nodes
export const L1_DB_BASE_H     = 46;  // collapsed DatabaseNode height (header ~24px + footer ~22px)

export const L1_SCH_AREA_PAD  = 5;   // top/bottom padding of schema area inside DB
export const L1_SCH_HEIGHT    = 20;  // height of each L1SchemaNode chip
export const L1_SCH_GAP       = 2;   // gap between schema chips (horizontal + vertical)

// ─── Multi-column chip grid ───────────────────────────────────────────────────
// ≤ 3 schemas → 1 column, chip width = 196 px (DB interior = 204 − 2×4 margins)
// > 3 schemas → 2 columns, each chip = 97 px (2×97 + 2 gap + 8 margins = 204)
// Threshold and widths can be tuned without touching component code.

const L1_SCH_MARGIN      = 4;   // left margin of chip area inside DB (x offset of col-0)
const L1_SCH_COL_THRESHOLD = 3; // switch to 2 cols when schemaCount > this
const L1_SCH_COL1_W      = 196; // chip width in 1-column layout
const L1_SCH_COL2_W      = 97;  // chip width in 2-column layout

/** Number of chip columns based on schema count. */
export function schemaGridCols(schemaCount: number): number {
  return schemaCount > L1_SCH_COL_THRESHOLD ? 2 : 1;
}

/** Width of each schema chip for a given column count. */
export function schemaChipW(cols: number): number {
  return cols === 1 ? L1_SCH_COL1_W : L1_SCH_COL2_W;
}

/**
 * X position of schema chip at `index` within its parent DatabaseNode
 * (relative to DB top-left corner).
 */
export function schemaChipX(index: number, cols: number): number {
  const col = index % cols;
  return L1_SCH_MARGIN + col * (L1_SCH_COL2_W + L1_SCH_GAP);
}

/**
 * Y position of schema chip at `index` within its parent DatabaseNode
 * (relative to DB top-left corner).
 */
export function schemaChipY(index: number, cols: number = 1): number {
  const row = Math.floor(index / cols);
  return L1_DB_BASE_H + L1_SCH_AREA_PAD + row * (L1_SCH_HEIGHT + L1_SCH_GAP);
}

/** Height of a DatabaseNode expanded with `n` schema children laid out in `cols` columns. */
export function dbExpandedH(schemaCount: number, cols: number = 1): number {
  if (schemaCount === 0) return L1_DB_BASE_H;
  const rows = Math.ceil(schemaCount / cols);
  return (
    L1_DB_BASE_H +
    L1_SCH_AREA_PAD +
    rows * (L1_SCH_HEIGHT + L1_SCH_GAP) -
    L1_SCH_GAP +
    L1_SCH_AREA_PAD
  );
}

// ─── Main layout function ─────────────────────────────────────────────────────

/**
 * Recomputes L1 node positions and visibility based on which DBs are expanded.
 *
 * Called every time `expandedDbs` changes. Input `nodes` always comes from
 * `transformGqlOverview` (original positions) — never from current RF state —
 * so results are deterministic and idempotent.
 *
 * Changes per node type:
 *   l1SchemaNode    → hidden = !expandedDbs.has(parentDbId)
 *   databaseNode    → position.y recalculated; style.height set to fit schema grid
 *   applicationNode → style.height grows to contain all expanded children
 */
export function applyL1Layout(
  nodes:       LoomNode[],
  expandedDbs: Set<string>,
): LoomNode[] {
  // ── Index by parent ───────────────────────────────────────────────────────
  const dbsByApp      = new Map<string, LoomNode[]>();  // appId  → DB children
  const schemasByDb   = new Map<string, LoomNode[]>();  // dbId   → Schema children
  const standaloneDbs: LoomNode[] = [];                 // DB nodes with no parentId

  for (const node of nodes) {
    if (node.type === 'databaseNode') {
      if (node.parentId) {
        const arr = dbsByApp.get(node.parentId) ?? [];
        arr.push(node);
        dbsByApp.set(node.parentId, arr);
      } else {
        standaloneDbs.push(node);
      }
    }
    if (node.type === 'l1SchemaNode' && node.parentId) {
      const arr = schemasByDb.get(node.parentId) ?? [];
      arr.push(node);
      schemasByDb.set(node.parentId, arr);
    }
  }

  // Sort DBs by original y (stable stacking order from transform)
  for (const [k, v] of dbsByApp) {
    dbsByApp.set(k, [...v].sort((a, b) => a.position.y - b.position.y));
  }
  for (const [k, v] of schemasByDb) {
    schemasByDb.set(k, [...v].sort((a, b) => a.position.y - b.position.y));
  }

  // ── Compute target heights deterministically from scratch ──────────────────
  // We NEVER compare against current RF state — always recompute from originals.
  const targetDbY  = new Map<string, number>();
  const targetDbH  = new Map<string, number>();
  const targetAppH = new Map<string, number>();

  // Grouped DBs: y positions are relative to their App parent.
  // Hidden DBs (e.g. systemLevel=true) are excluded from height calculation
  // so the App group collapses to header-only height.
  for (const [appId, dbs] of dbsByApp) {
    let y = L1_APP_HEADER;
    let hasVisible = false;

    for (const db of dbs) {
      if (db.hidden) continue;          // skip hidden DBs
      hasVisible = true;
      targetDbY.set(db.id, y);

      const schemas = schemasByDb.get(db.id) ?? [];
      const cols    = schemaGridCols(schemas.length);
      const dbH     = expandedDbs.has(db.id)
        ? dbExpandedH(schemas.length, cols)
        : L1_DB_BASE_H;

      targetDbH.set(db.id, dbH);
      y += dbH + L1_DB_GAP;
    }

    targetAppH.set(
      appId,
      hasVisible ? y - L1_DB_GAP + L1_APP_PAD_BOT : L1_APP_HEADER + L1_APP_PAD_BOT,
    );
  }

  // Standalone DBs: only height changes (position is absolute, never moves)
  for (const db of standaloneDbs) {
    const schemas = schemasByDb.get(db.id) ?? [];
    const cols    = schemaGridCols(schemas.length);
    targetDbH.set(
      db.id,
      expandedDbs.has(db.id) ? dbExpandedH(schemas.length, cols) : L1_DB_BASE_H,
    );
  }

  // ── Produce updated nodes ─────────────────────────────────────────────────
  // Always return new objects — no early-return optimisations that compare
  // against stale RF state. ReactFlow reconciles efficiently on its end.
  return nodes.map((node): LoomNode => {

    // Schema nodes: flip hidden flag only — positions pre-computed in transformGraph
    if (node.type === 'l1SchemaNode' && node.parentId) {
      const hidden = !expandedDbs.has(node.parentId);
      return { ...node, hidden };
    }

    // Grouped DB nodes: update y position within parent + explicit height
    if (node.type === 'databaseNode' && node.parentId) {
      const y = targetDbY.get(node.id);
      const h = targetDbH.get(node.id);
      if (y === undefined || h === undefined) return node;
      return {
        ...node,
        height:   h,
        position: { x: node.position.x, y },
        style:    { ...node.style, height: h },
      };
    }

    // Standalone DB nodes (no parentId): only height changes
    if (node.type === 'databaseNode' && !node.parentId) {
      const h = targetDbH.get(node.id);
      if (h === undefined) return node;
      return { ...node, height: h, style: { ...node.style, height: h } };
    }

    // App group nodes: update height to contain all expanded children
    if (node.type === 'applicationNode') {
      const h = targetAppH.get(node.id);
      if (h === undefined) return node;
      return { ...node, height: h, style: { ...node.style, height: h } };
    }

    return node;
  });
}
