import type { LoomNode, LoomEdge } from '../types/graph';
import { LAYOUT, TRANSFORM } from './constants';

// ─── Layout result type ───────────────────────────────────────────────────────
export interface LayoutResult {
  nodes:  LoomNode[];
  isGrid: boolean;   // true when grid was used (proactive M-3 or fallback)
}
import elkWorkerUrl from 'elkjs/lib/elk-worker.min.js?url';

// ─── Node dimension hints for ELK (sourced from constants.ts) ────────────────
const { NODE_WIDTH, NODE_HEIGHT_BASE, COL_ROW_HEIGHT, GRID_SPACING } = LAYOUT;

function getNodeHeight(node: LoomNode): number {
  // Cap column count used for ELK height so giant statement/table nodes
  // (e.g. INSERT with 600+ output columns) don't stretch the layout to 20,000 px.
  // applyStmtColumns already caps at MAX_PARTIAL_COLS; this is a safety net.
  const maxCols = TRANSFORM.MAX_PARTIAL_COLS;
  if (node.type === 'tableNode') {
    const cols = Math.min(node.data.columns?.length ?? 0, maxCols);
    return NODE_HEIGHT_BASE + cols * COL_ROW_HEIGHT + 24;
  }
  if (node.type === 'statementNode') {
    const cols = Math.min(node.data.columns?.length ?? 0, maxCols);
    return NODE_HEIGHT_BASE + cols * COL_ROW_HEIGHT + (cols > 0 ? 24 : 0);
  }
  // Routine/Package group: height is pre-computed and stored in style.height
  if (node.type === 'routineGroupNode' || node.type === 'packageGroupNode') {
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
// CALLS is included so routine→routine call relationships inform ELK positioning
// at L2 AGG (routines that only call each other, with no shared table, would
// otherwise appear as unconnected components and be placed far apart).
const DATA_FLOW_FOR_LAYOUT = new Set([
  'READS_FROM', 'WRITES_TO', 'DATA_FLOW',
  'FILTER_FLOW', 'JOIN_FLOW', 'UNION_FLOW', 'ATOM_PRODUCES', 'CALLS',
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
  result:      LoomNode[];
  isGrid:      boolean;
}
let layoutCache: LayoutCacheEntry | null = null;

// ─── M-6: O(E) rolling hash for edge fingerprint (replaces sort().join()) ────
// XOR-fold: commutative — insertion order does not matter.
function fnv1a(s: string): number {
  let h = 0x811c9dc5;
  for (let i = 0; i < s.length; i++) {
    h ^= s.charCodeAt(i);
    h = Math.imul(h, 0x01000193) >>> 0;
  }
  return h;
}

function hashEdges(edges: LoomEdge[]): number {
  let h = 0;
  for (const e of edges) {
    const s = fnv1a(e.source + e.target);
    h = (h ^ s ^ (s << 7) ^ (s >>> 25)) >>> 0;
  }
  return h;
}

function graphFingerprint(nodes: LoomNode[], edges: LoomEdge[]): string {
  // Include node ID + computed height so height changes (column count) invalidate cache.
  // Node array is small so sort() is fine; edge array can be 17K+ → rolling hash.
  const nodeKey = nodes.map((n) => `${n.id}:${getNodeHeight(n)}`).sort().join(',');
  return `${nodeKey}|${hashEdges(edges)}`;
}

// ─── M-1: Deduplicate edges before flat ELK call ─────────────────────────────
// Prevents duplicate source→target pairs from confusing the layered algorithm.
function deduplicateEdges<T extends { source: string; target: string }>(edges: T[]): T[] {
  const seen = new Set<string>();
  return edges.filter((e) => {
    const key = `${e.source}→${e.target}`;
    return seen.has(key) ? false : (seen.add(key), true);
  });
}

/** Invalidate the layout cache and abort any in-flight Worker requests. */
export function clearLayoutCache(): void {
  layoutCache = null;
  cancelPendingLayouts();
}

// ─── ELK engine ──────────────────────────────────────────────────────────────
// elk.bundled.js runs on the main thread, but uses elk-worker.min.js as a real
// Web Worker for its own computation, so elk.layout() is truly non-blocking.
//
// We provide an explicit workerFactory so ELKNode (elk.bundled.js) skips its
// internal `require('./elk-worker.min.js').Worker` path — that path breaks after
// Rollup/Vite's CJS→ESM transform renames the local `_Worker` variable to a
// value that is not a constructor.  By providing our own factory we get a real
// browser Worker pointing to elk-worker.min.js served as a classic script.
//
// vite.config.ts: optimizeDeps.include: ['elkjs/lib/elk.bundled.js']
// elkWorkerUrl: imported via ?url — Vite emits elk-worker.min.js as a static
// asset and returns its URL; loaded as classic script, no ESM transform applied.

const LAYOUT_TIMEOUT = LAYOUT.TIMEOUT_MS;

let _elk:              ElkApi | null = null;
let _workerBlobUrl:    string | null = null;

// ─── Cross-origin Worker fix (MF-remote mode) ────────────────────────────────
// When verdandi runs as a Module Federation remote inside Shell (different port /
// origin), elkWorkerUrl resolves to verdandi's Vite server (e.g. :5173) while
// the Shell page is at a different origin (e.g. :5175).
// Browsers block cross-origin `new Worker(url)` calls with a SecurityError.
// Fix: fetch the worker script bytes via CORS (Vite dev server sends
// Access-Control-Allow-Origin: *), create a same-origin Blob URL, then use
// that blob URL in the workerFactory — no cross-origin restriction applies.
// In production the worker asset is co-located with the page, so the fetch
// is always same-origin and the blob just adds negligible overhead.
async function resolveWorkerBlobUrl(): Promise<string> {
  if (_workerBlobUrl) return _workerBlobUrl;
  try {
    const resp = await fetch(elkWorkerUrl);
    if (!resp.ok) throw new Error(`HTTP ${resp.status}`);
    const text = await resp.text();
    const blob = new Blob([text], { type: 'application/javascript' });
    _workerBlobUrl = URL.createObjectURL(blob);
  } catch (err) {
    // Same-origin fallback: if fetch fails for some reason, use the raw URL.
    console.warn('[LOOM] Could not fetch elk-worker for blob URL — falling back to direct URL', err);
    _workerBlobUrl = elkWorkerUrl;
  }
  return _workerBlobUrl;
}

async function getElk(): Promise<ElkApi> {
  if (_elk) return _elk;
  // Resolve blob URL BEFORE constructing ELK so workerFactory can be synchronous.
  const blobUrl = await resolveWorkerBlobUrl();
  const mod = await import('elkjs/lib/elk.bundled.js');
  const ELK = (mod.default as unknown) as new (opts: {
    workerUrl?:    string;
    workerFactory: (url: string) => Worker;
  }) => ElkApi;
  // Pass factory without workerUrl to skip ELKNode's Node.js web-worker check
  // (which always warns in browser because the 'web-worker' npm package is absent).
  _elk = new ELK({
    workerFactory: (_url: string) => new Worker(blobUrl),
  });
  return _elk;
}

/** Terminate the ELK worker and reset singleton (cancels any in-flight layout). */
export function cancelPendingLayouts(): void {
  if (_elk) {
    try { (_elk as any).terminate?.(); } catch { /* ignore */ }
    _elk = null;
  }
}

async function runElkLayout(
  graph:   ElkGraph,
  timeout: number = LAYOUT_TIMEOUT,
): Promise<(ElkGraph & { children: ElkNode[] }) | null> {
  const t0 = performance.now();
  try {
    const elk    = await getElk();
    const result = await Promise.race([
      elk.layout(graph) as Promise<ElkGraph & { children: ElkNode[] }>,
      new Promise<never>((_, reject) =>
        setTimeout(() => reject(new Error(`ELK layout timed out after ${timeout / 1000}s`)), timeout),
      ),
    ]);
    const ms = (performance.now() - t0).toFixed(0);
    console.info(`[LOOM] ELK layout (worker) — ${ms} ms  (${graph.children.length} nodes, ${graph.edges.length} edges)`);
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
  nodes:   LoomNode[],
  edges:   LoomEdge[],
  options: { timeout?: number; forceELK?: boolean } = {},
): Promise<LayoutResult> {
  if (nodes.length === 0) return { nodes, isGrid: false };

  // Check 1-entry cache before hitting ELK
  const fp = graphFingerprint(nodes, edges);
  if (layoutCache && layoutCache.fingerprint === fp) {
    return { nodes: layoutCache.result, isGrid: layoutCache.isGrid };
  }

  // M-3: proactive grid for very large graphs — skip ELK entirely
  // (forceELK=true bypasses this guard when the user explicitly requests full layout)
  if (!options.forceELK && nodes.length > LAYOUT.AUTO_GRID_THRESHOLD) {
    console.info(`[LOOM] Graph has ${nodes.length} nodes (>${LAYOUT.AUTO_GRID_THRESHOLD}) — using grid layout (M-3)`);
    const laid = applyGridLayout(nodes);
    layoutCache = { fingerprint: fp, result: laid, isGrid: true };
    return { nodes: laid, isGrid: true };
  }

  // M-7: dynamic timeout — reduce for large graphs to fail fast
  const timeout = options.timeout ?? (nodes.length > 1000 ? LAYOUT.ELK_TIMEOUT_LARGE : LAYOUT.TIMEOUT_MS);

  // ── Flat layout (no compound nodes) ──────────────────────────────────────
  const childNodes = nodes.filter((n) => n.parentId);
  if (childNodes.length === 0) {
    // M-1: deduplicate before passing to ELK (duplicate src→tgt confuses layered algo)
    const flatEdges = deduplicateEdges(
      edges
        .filter((e) => {
          const et = (e.data as { edgeType?: string } | undefined)?.edgeType;
          return !et || DATA_FLOW_FOR_LAYOUT.has(et);
        })
        .map((e) => ({ id: e.id, source: e.source, target: e.target })),
    );
    const graph: ElkGraph = {
      id: 'root',
      layoutOptions: getLayeredOptions(nodes.length),
      children: nodes.map((n) => ({ id: n.id, width: NODE_WIDTH, height: getNodeHeight(n) })),
      // Only data-flow edges — containment edges are filtered out so ELK receives
      // a clean DAG (or near-DAG) without CONTAINS_ROUTINE/CONTAINS_STMT chains.
      edges: flatEdges.map((e) => ({ id: e.id, sources: [e.source], targets: [e.target] })),
    };
    const result = await runElkLayout(graph, timeout);
    if (!result) {
      const laid = applyGridLayout(nodes);
      layoutCache = { fingerprint: fp, result: laid, isGrid: true };
      return { nodes: laid, isGrid: true };
    }
    const laid = nodes.map((node) => {
      const c = result.children.find((r) => r.id === node.id);
      return c ? { ...node, position: { x: c.x ?? 0, y: c.y ?? 0 } } : node;
    });
    layoutCache = { fingerprint: fp, result: laid, isGrid: false };
    return { nodes: laid, isGrid: false };
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

  const result = await runElkLayout(graph, timeout);
  if (!result) {
    // Grid-position top-level nodes; children keep pre-computed relative positions.
    const gridTop = applyGridLayout(topNodes);
    const gridIds = new Set(gridTop.map((n) => n.id));
    const laid = [...gridTop, ...nodes.filter((n) => !gridIds.has(n.id))];
    layoutCache = { fingerprint: fp, result: laid, isGrid: true };
    return { nodes: laid, isGrid: true };
  }
  const posMap = new Map(result.children.map((c) => [c.id, { x: c.x ?? 0, y: c.y ?? 0 }]));
  const laid = nodes.map((node) => {
    if (node.parentId) return node; // keep pre-computed relative positions
    const pos = posMap.get(node.id);
    return pos ? { ...node, position: pos } : node;
  });
  layoutCache = { fingerprint: fp, result: laid, isGrid: false };
  return { nodes: laid, isGrid: false };
}
