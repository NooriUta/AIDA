import type { LoomNode, LoomEdge } from '../types/graph';
import { LAYOUT } from './constants';

// ─── Node dimension hints for ELK (sourced from constants.ts) ────────────────
const { NODE_WIDTH, NODE_HEIGHT_BASE, COL_ROW_HEIGHT, GRID_SPACING } = LAYOUT;

function getNodeHeight(node: LoomNode): number {
  if (node.type === 'tableNode') {
    const cols = node.data.columns?.length ?? 0;
    return NODE_HEIGHT_BASE + cols * COL_ROW_HEIGHT + 24;
  }
  if (node.type === 'statementNode') {
    const cols = node.data.columns?.length ?? 0;
    return NODE_HEIGHT_BASE + cols * COL_ROW_HEIGHT + (cols > 0 ? 24 : 0);
  }
  // Routine group: height is pre-computed and stored in style
  if (node.type === 'routineGroupNode') {
    return typeof node.style?.height === 'number' ? node.style.height : NODE_HEIGHT_BASE;
  }
  return NODE_HEIGHT_BASE;
}

// ─── Fallback grid layout (used if ELK fails) ────────────────────────────────
// Tracks per-column Y so tall nodes (table with many columns) don't overlap.
function applyGridLayout(nodes: LoomNode[]): LoomNode[] {
  const colCount = Math.max(1, Math.ceil(Math.sqrt(nodes.length)));
  const colY = new Array<number>(colCount).fill(0);
  return nodes.map((node, i) => {
    const col = i % colCount;
    const y = colY[col];
    colY[col] += getNodeHeight(node) + GRID_SPACING;
    return { ...node, position: { x: col * (NODE_WIDTH + GRID_SPACING), y } };
  });
}

// ─── Data-flow edge types used for ELK layout (flat path) ────────────────────
// Containment edges (CONTAINS_ROUTINE, CONTAINS_STMT, etc.) are kept for
// visual rendering but excluded from ELK: they create deep hierarchical chains
// that, combined with cyclic data-flow edges, confuse the layered algorithm
// and can produce degenerate (all-at-origin) layouts.
const DATA_FLOW_FOR_LAYOUT = new Set([
  'READS_FROM', 'WRITES_TO', 'DATA_FLOW',
  'FILTER_FLOW', 'JOIN_FLOW', 'UNION_FLOW', 'ATOM_PRODUCES',
]);

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
// Adaptive: BRANDES_KOEPF is compact but crashes the Worker on 800+ nodes;
// LINEAR_SEGMENTS is safer for large graphs.
function getLayeredOptions(nodeCount: number): Record<string, string> {
  return {
    'elk.algorithm':                             'layered',
    'elk.direction':                             'RIGHT',
    'elk.layered.spacing.nodeNodeBetweenLayers': String(LAYOUT.ELK_BETWEEN_LAYERS),
    'elk.spacing.nodeNode':                      String(LAYOUT.ELK_NODE_SPACING),
    'elk.separateConnectedComponents':           'true',
    'elk.spacing.componentComponent':            String(LAYOUT.ELK_COMPONENT_SPACING),
    'elk.layered.crossingMinimization.strategy': 'LAYER_SWEEP',
    'elk.layered.nodePlacement.strategy':
      nodeCount > LAYOUT.LARGE_GRAPH_THRESHOLD ? 'LINEAR_SEGMENTS' : 'BRANDES_KOEPF',
    // Do NOT add unnecessary bendpoints — they create Z-shaped edge routes.
    'elk.layered.unnecessaryBendpoints':         'false',
  };
}

// ─── Fingerprint-based 1-entry layout cache ───────────────────────────────
// Avoids re-running ELK when the same graph structure is requested again
// (e.g. toggling a post-layout filter and back).
interface LayoutCacheEntry {
  fingerprint: string;
  result: LoomNode[];
}
let layoutCache: LayoutCacheEntry | null = null;

function graphFingerprint(nodes: LoomNode[], edges: LoomEdge[]): string {
  // Include node ID + computed height so height changes (column count) invalidate cache.
  const nodeKey = nodes.map((n) => `${n.id}:${getNodeHeight(n)}`).sort().join(',');
  const edgeKey = edges.map((e) => e.id).sort().join(',');
  return `${nodeKey}|${edgeKey}`;
}

/** Invalidate the layout cache and abort any in-flight Worker requests. */
export function clearLayoutCache(): void {
  layoutCache = null;
  cancelPendingLayouts();
}

// ─── ELK engine ─────────────────────────────────────────────────────────────
// Uses elk.bundled.js (pure JS) on the main thread.
//
// NOTE: A Web Worker approach was attempted (see workers/elkWorker.ts) but
// Vite's CJS→ESM transform renames the `Worker` global to `_Worker` inside
// elk.bundled.js, causing "TypeError: _Worker is not a constructor" inside
// the Worker context.  This is a Vite dev-server limitation; a production-
// only Worker can be revisited once Vite 6+ ships native CJS Worker support.
//
// The main-thread approach is acceptable:
//   • < 1 s for typical schemas (< 500 nodes)
//   • 2–5 s for large schemas (500–1000 nodes) — loading spinner is shown
//   • 15 s timeout with grid fallback for degenerate cases

const LAYOUT_TIMEOUT = LAYOUT.TIMEOUT_MS;

/** Cancel pending layouts — no-op now but kept for API compat with useLoomLayout. */
export function cancelPendingLayouts(): void {
  // Reserved for future Worker re-enablement
}

// Singleton bundled-ELK
let _elkMain: ElkApi | null = null;

async function getElk(): Promise<ElkApi> {
  if (_elkMain) return _elkMain;
  const mod = await import('elkjs/lib/elk.bundled.js');
  const ELK = (mod.default as unknown) as new () => ElkApi;
  _elkMain = new ELK();
  return _elkMain;
}

async function runElkLayout(graph: ElkGraph): Promise<(ElkGraph & { children: ElkNode[] }) | null> {
  const t0 = performance.now();
  try {
    const elk = await getElk();
    const result = await Promise.race([
      elk.layout(graph) as Promise<ElkGraph & { children: ElkNode[] }>,
      new Promise<never>((_, reject) =>
        setTimeout(() => reject(new Error(`ELK layout timed out after ${LAYOUT_TIMEOUT / 1000}s`)), LAYOUT_TIMEOUT),
      ),
    ]);
    const ms = (performance.now() - t0).toFixed(0);
    console.info(`[LOOM] ELK layout (main-thread) — ${ms} ms  (${graph.children.length} nodes, ${graph.edges.length} edges)`);
    return result;
  } catch (err) {
    const ms = (performance.now() - t0).toFixed(0);
    console.warn(`[LOOM] ELK layout failed after ${ms} ms, using grid fallback`, err);
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

  // Check 1-entry cache before hitting ELK
  const fp = graphFingerprint(nodes, edges);
  if (layoutCache && layoutCache.fingerprint === fp) {
    return layoutCache.result;
  }

  // ── Flat layout (no compound nodes) ──────────────────────────────────────
  const childNodes = nodes.filter((n) => n.parentId);
  if (childNodes.length === 0) {
    const graph: ElkGraph = {
      id: 'root',
      layoutOptions: getLayeredOptions(nodes.length),
      children: nodes.map((n) => ({ id: n.id, width: NODE_WIDTH, height: getNodeHeight(n) })),
      // Only data-flow edges — containment edges are filtered out so ELK receives
      // a clean DAG (or near-DAG) without CONTAINS_ROUTINE/CONTAINS_STMT chains.
      edges: edges
        .filter((e) => {
          const et = (e.data as { edgeType?: string } | undefined)?.edgeType;
          return !et || DATA_FLOW_FOR_LAYOUT.has(et);
        })
        .map((e) => ({ id: e.id, sources: [e.source], targets: [e.target] })),
    };
    const result = await runElkLayout(graph);
    if (!result) return applyGridLayout(nodes);
    const laid = nodes.map((node) => {
      const c = result.children.find((r) => r.id === node.id);
      return c ? { ...node, position: { x: c.x ?? 0, y: c.y ?? 0 } } : node;
    });
    layoutCache = { fingerprint: fp, result: laid };
    return laid;
  }

  // ── Compound layout (supports multi-level nesting: Schema → Routine → Stmt) ─
  const childIds  = new Set(childNodes.map((n) => n.id));
  const parentOf  = new Map(childNodes.map((n) => [n.id, n.parentId as string]));
  const topNodes  = nodes.filter((n) => !n.parentId);

  // Map each child to its top-level ancestor (handles arbitrary nesting depth)
  const topAncestorOf = new Map<string, string>();
  for (const n of childNodes) {
    let cur = n.id;
    while (parentOf.has(cur)) cur = parentOf.get(cur)!;
    topAncestorOf.set(n.id, cur);
  }

  const topNodeIds = new Set(topNodes.map((n) => n.id));

  const graph: ElkGraph = {
    id: 'root',
    layoutOptions: getLayeredOptions(topNodes.length),
    children: topNodes.map((n) => ({
      id:     n.id,
      // Use pre-computed style dimensions for compound (group) nodes
      width:  typeof n.style?.width  === 'number' ? n.style.width  : NODE_WIDTH,
      height: typeof n.style?.height === 'number' ? n.style.height : getNodeHeight(n),
    })),
    // Cross-group edges only — internal edges and containment edges are skipped.
    // When one endpoint is inside a group, we remap it to the top-level ancestor
    // so ELK can position the group relative to external nodes.
    edges: (() => {
      const seen = new Set<string>();
      const elkEdges: ElkEdge[] = [];
      for (const e of edges) {
        const srcIsChild = childIds.has(e.source);
        const tgtIsChild = childIds.has(e.target);
        const elkSrc = srcIsChild ? topAncestorOf.get(e.source)! : e.source;
        const elkTgt = tgtIsChild ? topAncestorOf.get(e.target)! : e.target;
        if (elkSrc === elkTgt) continue;                        // internal edge
        if (!topNodeIds.has(elkSrc) || !topNodeIds.has(elkTgt)) continue;
        const key = `${elkSrc}→${elkTgt}`;
        if (seen.has(key)) continue;                            // deduplicate
        seen.add(key);
        elkEdges.push({ id: e.id, sources: [elkSrc], targets: [elkTgt] });
      }
      return elkEdges;
    })(),
  };

  const result = await runElkLayout(graph);
  if (!result) {
    // Grid-position top-level nodes; children keep pre-computed relative positions.
    const gridTop = applyGridLayout(topNodes);
    const gridIds = new Set(gridTop.map((n) => n.id));
    return [...gridTop, ...nodes.filter((n) => !gridIds.has(n.id))];
  }
  const posMap = new Map(result.children.map((c) => [c.id, { x: c.x ?? 0, y: c.y ?? 0 }]));
  const laid = nodes.map((node) => {
    if (node.parentId) return node; // keep pre-computed relative positions
    const pos = posMap.get(node.id);
    return pos ? { ...node, position: pos } : node;
  });
  layoutCache = { fingerprint: fp, result: laid };
  return laid;
}
