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

export const L1_SCH_AREA_PAD  = 5;   // top padding of schema area inside DB
export const L1_SCH_HEIGHT    = 20;  // height of each L1SchemaNode
export const L1_SCH_GAP       = 2;   // gap between schema chips

/** Height of a DatabaseNode expanded with `n` schema children. */
export function dbExpandedH(schemaCount: number): number {
  if (schemaCount === 0) return L1_DB_BASE_H;
  return (
    L1_DB_BASE_H +
    L1_SCH_AREA_PAD +
    schemaCount * (L1_SCH_HEIGHT + L1_SCH_GAP) -
    L1_SCH_GAP +
    L1_SCH_AREA_PAD
  );
}

/**
 * Y position of schema node at `index` within its parent DatabaseNode
 * (relative to DB's top-left corner).
 */
export function schemaChipY(index: number): number {
  return L1_DB_BASE_H + L1_SCH_AREA_PAD + index * (L1_SCH_HEIGHT + L1_SCH_GAP);
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
 *   l1SchemaNode  → hidden = !expandedDbs.has(parentDbId)
 *   databaseNode  → position.y recalculated; style.height set explicitly
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

  // Grouped DBs: y positions are relative to their App parent
  for (const [appId, dbs] of dbsByApp) {
    let y = L1_APP_HEADER;

    for (const db of dbs) {
      targetDbY.set(db.id, y);

      const schemas = schemasByDb.get(db.id) ?? [];
      const dbH     = expandedDbs.has(db.id)
        ? dbExpandedH(schemas.length)
        : L1_DB_BASE_H;

      targetDbH.set(db.id, dbH);
      y += dbH + L1_DB_GAP;
    }

    targetAppH.set(appId, y - L1_DB_GAP + L1_APP_PAD_BOT);
  }

  // Standalone DBs: only their height changes (position is absolute, never moves)
  for (const db of standaloneDbs) {
    const schemas = schemasByDb.get(db.id) ?? [];
    targetDbH.set(
      db.id,
      expandedDbs.has(db.id) ? dbExpandedH(schemas.length) : L1_DB_BASE_H,
    );
  }

  // ── Produce updated nodes ─────────────────────────────────────────────────
  // Always return new objects — no early-return optimisations that compare
  // against stale RF state. ReactFlow reconciles efficiently on its end.
  return nodes.map((node): LoomNode => {

    // Schema nodes: flip hidden flag only
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
        position: { x: node.position.x, y },
        style:    { ...node.style, height: h },
      };
    }

    // Standalone DB nodes (no parentId): only height changes
    if (node.type === 'databaseNode' && !node.parentId) {
      const h = targetDbH.get(node.id);
      if (h === undefined) return node;
      return { ...node, style: { ...node.style, height: h } };
    }

    // App group nodes: update height to contain all expanded children
    if (node.type === 'applicationNode') {
      const h = targetAppH.get(node.id);
      if (h === undefined) return node;
      return { ...node, style: { ...node.style, height: h } };
    }

    return node;
  });
}
