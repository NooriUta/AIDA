import type { LoomNode, LoomEdge } from '../types/graph';

// ─── Node dimension hints for ELK ────────────────────────────────────────────
const NODE_WIDTH  = 240;
const NODE_HEIGHT_BASE = 80;
const COLUMN_ROW_HEIGHT = 22;

function getNodeHeight(node: LoomNode): number {
  if (node.type === 'tableNode') {
    const cols = node.data.columns?.length ?? 0;
    return NODE_HEIGHT_BASE + Math.min(cols, 7) * COLUMN_ROW_HEIGHT + 24;
  }
  if (node.type === 'statementNode') {
    const cols = node.data.columns?.length ?? 0;
    return NODE_HEIGHT_BASE + Math.min(cols, 5) * COLUMN_ROW_HEIGHT + (cols > 0 ? 24 : 0);
  }
  return NODE_HEIGHT_BASE;
}

// ─── Fallback grid layout (used if ELK fails) ────────────────────────────────
function applyGridLayout(nodes: LoomNode[]): LoomNode[] {
  const cols = Math.max(1, Math.ceil(Math.sqrt(nodes.length)));
  return nodes.map((node, i) => ({
    ...node,
    position: {
      x: (i % cols) * (NODE_WIDTH + 60),
      y: Math.floor(i / cols) * (NODE_HEIGHT_BASE + 60),
    },
  }));
}

// ─── ELK types (minimal, avoids dependency on @types/elkjs) ──────────────────
interface ElkNode {
  id: string;
  width?: number;
  height?: number;
  x?: number;
  y?: number;
}
interface ElkEdge {
  id: string;
  sources: string[];
  targets: string[];
}
interface ElkGraph {
  id: string;
  layoutOptions?: Record<string, string>;
  children: ElkNode[];
  edges: ElkEdge[];
}
interface ElkApi {
  layout: (graph: ElkGraph) => Promise<ElkGraph & { children: ElkNode[] }>;
}

// ─── Shared layout options (flat layered, LEFT → RIGHT) ──────────────────────
const FLAT_LAYOUT_OPTIONS: Record<string, string> = {
  'elk.algorithm':                             'layered',
  'elk.direction':                             'RIGHT',
  'elk.layered.spacing.nodeNodeBetweenLayers': '100',
  'elk.spacing.nodeNode':                      '50',
  'elk.layered.crossingMinimization.strategy': 'LAYER_SWEEP',
  'elk.layered.nodePlacement.strategy':        'BRANDES_KOEPF',
};

// Singleton ELK instance (lazy-initialised)
let elkInstance: ElkApi | null = null;

async function getElk(): Promise<ElkApi | null> {
  if (elkInstance) return elkInstance;
  try {
    // elk.bundled.js = no web workers, runs in main thread
    const mod = await import('elkjs/lib/elk.bundled.js');
    const ELK = mod.default as new () => ElkApi;
    elkInstance = new ELK();
    return elkInstance;
  } catch (err) {
    console.warn('[LOOM] ELK load failed, using grid layout fallback.', err);
    return null;
  }
}

// ─── Main export ──────────────────────────────────────────────────────────────
//
// Compound mode (any node has parentId set):
//   Child nodes have pre-computed relative positions (set by transformSchemaExplore).
//   Only top-level nodes are passed to ELK; compound nodes use their style dimensions.
//   Cross-edges (extRoutine → child table) are reversed in ELK so ELK places
//   external routines to the RIGHT of the schema group (ELK direction = RIGHT).
//
export async function applyELKLayout(
  nodes: LoomNode[],
  edges: LoomEdge[],
): Promise<LoomNode[]> {
  if (nodes.length === 0) return nodes;

  const elk = await getElk();
  if (!elk) return applyGridLayout(nodes);

  // ── Flat layout (no compound nodes) ──────────────────────────────────────
  const childNodes = nodes.filter((n) => n.parentId);
  if (childNodes.length === 0) {
    const graph: ElkGraph = {
      id: 'root',
      layoutOptions: { ...FLAT_LAYOUT_OPTIONS },
      children: nodes.map((n) => ({ id: n.id, width: NODE_WIDTH, height: getNodeHeight(n) })),
      edges:    edges.map((e) => ({ id: e.id, sources: [e.source], targets: [e.target] })),
    };
    try {
      const result = await elk.layout(graph);
      return nodes.map((node) => {
        const laid = result.children.find((c) => c.id === node.id);
        return laid ? { ...node, position: { x: laid.x ?? 0, y: laid.y ?? 0 } } : node;
      });
    } catch (err) {
      console.warn('[LOOM] ELK layout error, using grid layout fallback.', err);
      return applyGridLayout(nodes);
    }
  }

  // ── Compound layout ───────────────────────────────────────────────────────
  const childIds      = new Set(childNodes.map((n) => n.id));
  const parentOfChild = new Map(childNodes.map((n) => [n.id, n.parentId as string]));
  const topNodes      = nodes.filter((n) => !n.parentId);

  const graph: ElkGraph = {
    id: 'root',
    layoutOptions: { ...FLAT_LAYOUT_OPTIONS },
    children: topNodes.map((n) => ({
      id:     n.id,
      // Use pre-computed style dimensions for compound (group) nodes
      width:  typeof n.style?.width  === 'number' ? n.style.width  : NODE_WIDTH,
      height: typeof n.style?.height === 'number' ? n.style.height : getNodeHeight(n),
    })),
    // Edges: skip CONTAINS_TABLE (implicit via parentId) and edges from child nodes.
    // CONTAINS_ROUTINE (pkg→routine), CONTAINS_STMT (routine→stmt), BELONGS_TO_SESSION
    // are real structural edges — keep them so ELK can use them for positioning.
    // Cross-hierarchy edges (stmt → child table) are reversed so ELK places
    // external nodes to the LEFT of the schema group (ELK direction = RIGHT).
    edges: edges
      .filter((e) => {
        if (childIds.has(e.source)) return false;
        const type = e.data?.edgeType;
        return type !== 'CONTAINS_TABLE';
      })
      .map((e) => {
        const tgtIsChild = childIds.has(e.target);
        return {
          id:      e.id,
          // Reverse: schemaGroup → extRoutine so ELK puts extRoutine on the right
          sources: tgtIsChild ? [parentOfChild.get(e.target) ?? e.target] : [e.source],
          targets: tgtIsChild ? [e.source]                                : [e.target],
        };
      }),
  };

  try {
    const result = await elk.layout(graph);
    const posMap = new Map(result.children.map((c) => [c.id, { x: c.x ?? 0, y: c.y ?? 0 }]));
    return nodes.map((node) => {
      if (node.parentId) return node; // keep pre-computed relative positions
      const pos = posMap.get(node.id);
      return pos ? { ...node, position: pos } : node;
    });
  } catch (err) {
    console.warn('[LOOM] ELK compound layout error.', err);
    return nodes; // children keep relative positions; top-level nodes at (0,0)
  }
}
