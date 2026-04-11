import { useEffect, useRef, useState } from 'react';
import type { Dispatch, SetStateAction } from 'react';
import { useReactFlow } from '@xyflow/react';

import { useLoomStore }              from '../../stores/loomStore';
import { applyELKLayout, cancelPendingLayouts } from '../../utils/layoutGraph';
import { applyL1Layout }             from '../../utils/layoutL1';
import { edgeTypeClass }             from '../../utils/displayPipeline';
import type { LoomNode, LoomEdge }   from '../../types/graph';
import type { ColumnInfo }           from '../../types/domain';

interface Graph {
  nodes: LoomNode[];
  edges: LoomEdge[];
}

// ── Module-level helpers (defined once, not recreated on every render) ─────────

function stripNodeDim(n: LoomNode): LoomNode {
  if (!n.style?.opacity && !n.style?.pointerEvents) return n;
  const s = { ...n.style };
  delete s.opacity;
  delete s.pointerEvents;
  return { ...n, style: s };
}

/** Build edge className preserving the mapping-mode type class (loom-cf / loom-flow). */
function buildEdgeCls(e: LoomEdge, dimCls?: string): string | undefined {
  const tc = edgeTypeClass(e.data?.edgeType as string);
  if (!tc && !dimCls) return undefined;
  if (!dimCls) return tc || undefined;
  return tc ? `${tc} ${dimCls}` : dimCls;
}

function stripEdgeDim(e: LoomEdge): LoomEdge {
  const cls = buildEdgeCls(e);
  if (e.className === cls) return e;
  return { ...e, className: cls };
}

type SetNodes = Dispatch<SetStateAction<LoomNode[]>>;
type SetEdges = Dispatch<SetStateAction<LoomEdge[]>>;

/**
 * Runs ELK / L1 layout whenever displayGraph changes, then applies
 * post-layout dimming (tableFilter / stmtFilter / fieldFilter — LOOM-031).
 * Post-layout dimming is intentionally separate from displayGraph so filter
 * changes don't trigger full ELK re-runs.
 *
 * Must be used inside ReactFlowProvider (calls useReactFlow).
 */
export function useLoomLayout(
  displayGraph: Graph | null,
  setNodes: SetNodes,
  setEdges: SetEdges,
  stmtColsReady: boolean,
) {
  const { fitView, getEdges, getNodes } = useReactFlow();

  const [layouting,   setLayouting]   = useState(false);
  const [layoutError, setLayoutError] = useState(false);

  // Track whether post-layout dimming is active to skip no-op cleanup cycles
  const isDimmedRef = useRef(false);

  const {
    viewLevel,
    expandedDbs,
    l1Filter,
    filter,
    pendingFocusNodeId,
    requestFocusNode,
    clearPendingFocus,
    pendingDeepExpand,
    activatePendingDeepExpand,
    setGraphStats,
    setHighlightedColumns,
  } = useLoomStore();

  // ── Layout: L1 = pre-computed + applyL1Layout; L2/L3 = ELK ─────────────────
  useEffect(() => {
    if (!displayGraph) return;
    // Wait until stmtColsQ has settled so cfEdges exist and node heights are
    // final — this prevents a double ELK run on L2 graphs with column data.
    if (!stmtColsReady) return;
    let cancelled = false;

    // L1 grouped layout: positions set by transformGqlOverview, dynamically
    // adjusted by applyL1Layout whenever expandedDbs changes.
    if (viewLevel === 'L1') {
      let laid = applyL1Layout(displayGraph.nodes, expandedDbs);

      // applyL1Layout always re-sets l1SchemaNode.hidden based on expandedDbs,
      // which undoes any depth filter applied earlier in displayGraph.
      // Re-apply depth/system filter here as a post-pass.
      const hideDb     = l1Filter.systemLevel || l1Filter.depth === 1;
      const hideSchema = hideDb || l1Filter.depth === 2;
      if (hideDb || hideSchema) {
        laid = laid.map((n) => {
          if (hideDb     && n.type === 'databaseNode')   return { ...n, hidden: true };
          if (hideSchema && n.type === 'l1SchemaNode')   return { ...n, hidden: true };
          return n;
        });
      }

      setNodes(laid);
      setEdges(displayGraph.edges);
      setGraphStats(laid.length, displayGraph.edges.length);
      return;
    }

    setLayouting(true);
    setLayoutError(false);

    applyELKLayout(displayGraph.nodes, displayGraph.edges)
      .then((layoutedNodes) => {
        if (cancelled) return;
        setNodes(layoutedNodes);
        setEdges(displayGraph.edges);
        setGraphStats(layoutedNodes.length, displayGraph.edges.length);
      })
      .catch((err) => {
        console.error('[LOOM] ELK layout failed', err);
        if (!cancelled) setLayoutError(true);
      })
      .finally(() => {
        if (!cancelled) {
          setLayouting(false);
          // Back-nav zoom / search focus: fire AFTER layout so fitViewRequest
          // doesn't race against an empty/stale graph
          if (pendingFocusNodeId) {
            requestFocusNode(pendingFocusNodeId);
            clearPendingFocus();
          }
          // Search auto-expand: promote pending → active request now that graph exists
          if (pendingDeepExpand) {
            activatePendingDeepExpand();
          }
        }
      });

    return () => { cancelled = true; cancelPendingLayouts(); };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [displayGraph, viewLevel, expandedDbs, l1Filter.depth, l1Filter.systemLevel, stmtColsReady]);

  // ── Post-layout dimming — tableFilter, stmtFilter, fieldFilter + depth ───────
  // These phases are intentionally separate from displayGraph to avoid ELK re-runs.
  // Respects filter.depth: BFS expands N hops from the seed node(s).
  useEffect(() => {
    if (layouting || viewLevel !== 'L2') return;
    const { tableFilter, stmtFilter, fieldFilter, depth, upstream, downstream } = filter;
    const activeId = stmtFilter ?? tableFilter;
    const DIM_TABLE = 0.18;
    const DIM_FIELD = 0.08;

    if (!activeId && !fieldFilter) {
      if (!isDimmedRef.current) return;
      isDimmedRef.current = false;
      setNodes((ns) => ns.map(stripNodeDim));
      setEdges((es) => es.map(stripEdgeDim));
      setHighlightedColumns(null);
      return;
    }
    isDimmedRef.current = true;

    const currentEdges = getEdges();

    // ── Adjacency lists for directional BFS ──────────────────────────────
    const fwd = new Map<string, string[]>();  // source → targets (downstream)
    const rev = new Map<string, string[]>();  // target → sources (upstream)
    for (const e of currentEdges) {
      if (!fwd.has(e.source)) fwd.set(e.source, []);
      fwd.get(e.source)!.push(e.target);
      if (!rev.has(e.target)) rev.set(e.target, []);
      rev.get(e.target)!.push(e.source);
    }

    /** BFS from seeds up to `maxHops`, respecting upstream/downstream toggles. */
    function reachable(seeds: Iterable<string>, maxHops: number): Set<string> {
      const visited = new Set<string>(seeds);
      let frontier = [...visited];
      for (let hop = 0; hop < maxHops && frontier.length > 0; hop++) {
        const next: string[] = [];
        for (const id of frontier) {
          if (downstream) for (const nb of fwd.get(id) ?? []) {
            if (!visited.has(nb)) { visited.add(nb); next.push(nb); }
          }
          if (upstream) for (const nb of rev.get(id) ?? []) {
            if (!visited.has(nb)) { visited.add(nb); next.push(nb); }
          }
        }
        frontier = next;
      }
      return visited;
    }

    const hops = depth === Infinity ? 999 : depth;

    // ── Table / stmt filter: N-hop connected set ─────────────────────────
    let tableConnected: Set<string> | null = null;
    if (activeId) {
      tableConnected = reachable([activeId], hops);
    }

    // ── Field filter: label match + N-hop neighbours ─────────────────────
    let fieldRelevant: Set<string> | null = null;
    if (fieldFilter) {
      const fieldName = fieldFilter.toLowerCase();
      const seeds: string[] = [];
      for (const n of (getNodes() as unknown) as LoomNode[]) {
        const byLabel = n.data.label?.toLowerCase() === fieldName;
        const byCol   = (n.data.columns as ColumnInfo[] | undefined)?.some(
          (c) => c.name.toLowerCase() === fieldName,
        );
        if (byLabel || byCol) seeds.push(n.id);
      }
      fieldRelevant = reachable(seeds, hops);
    }

    // ── Column-level BFS: trace lineage through cf-edges ───────────────
    let highlightedCols: Set<string> | null = null;
    if (fieldFilter) {
      const fieldName = fieldFilter.toLowerCase();
      const colFwd = new Map<string, string[]>();
      const colRev = new Map<string, string[]>();
      for (const e of currentEdges) {
        if (!e.sourceHandle || !e.targetHandle) continue;
        const sCol = e.sourceHandle.replace(/^src-/, '');
        const tCol = e.targetHandle.replace(/^tgt-/, '');
        if (sCol === e.sourceHandle || tCol === e.targetHandle) continue;
        if (!colFwd.has(sCol)) colFwd.set(sCol, []);
        colFwd.get(sCol)!.push(tCol);
        if (!colRev.has(tCol)) colRev.set(tCol, []);
        colRev.get(tCol)!.push(sCol);
      }
      const colSeeds: string[] = [];
      for (const n of (getNodes() as unknown) as LoomNode[]) {
        for (const c of (n.data.columns as ColumnInfo[] | undefined) ?? []) {
          if (c.name.toLowerCase() === fieldName) colSeeds.push(c.id);
        }
      }
      const colHops = depth === Infinity ? 999 : depth;
      const colVisited = new Set<string>(colSeeds);
      let colFrontier = [...colSeeds];
      for (let h = 0; h < colHops && colFrontier.length > 0; h++) {
        const next: string[] = [];
        for (const cid of colFrontier) {
          if (downstream) for (const nb of colFwd.get(cid) ?? []) {
            if (!colVisited.has(nb)) { colVisited.add(nb); next.push(nb); }
          }
          if (upstream) for (const nb of colRev.get(cid) ?? []) {
            if (!colVisited.has(nb)) { colVisited.add(nb); next.push(nb); }
          }
        }
        colFrontier = next;
      }
      highlightedCols = colVisited.size > 0 ? colVisited : null;
    }
    setHighlightedColumns(highlightedCols);

    // ── Nodes that own at least one highlighted column ─────────────────
    let nodesWithHighlight: Set<string> | null = null;
    if (highlightedCols) {
      nodesWithHighlight = new Set<string>();
      for (const n of (getNodes() as unknown) as LoomNode[]) {
        for (const c of (n.data.columns as ColumnInfo[] | undefined) ?? []) {
          if (highlightedCols.has(c.id)) { nodesWithHighlight.add(n.id); break; }
        }
      }
    }

    // ── Apply combined dim ────────────────────────────────────────────────
    setNodes((ns) => ns.map((n) => {
      const inTable = !tableConnected || tableConnected.has(n.id);
      const inField = !fieldRelevant  || fieldRelevant.has(n.id);
      if (inTable && inField) return stripNodeDim(n);
      const opacity = inField ? DIM_TABLE : DIM_FIELD;
      return { ...n, style: { ...n.style, opacity, pointerEvents: 'none' as const } };
    }));
    setEdges((es) => es.map((e) => {
      const inTable = !tableConnected || (tableConnected.has(e.source) && tableConnected.has(e.target));
      const inField = !fieldRelevant  || (fieldRelevant.has(e.source) && fieldRelevant.has(e.target));
      const isCfEdge = e.sourceHandle?.startsWith('src-') && e.targetHandle?.startsWith('tgt-');
      // Column-flow edges: dim if endpoint columns aren't highlighted
      if (isCfEdge && highlightedCols) {
        const sCol = e.sourceHandle!.replace(/^src-/, '');
        const tCol = e.targetHandle!.replace(/^tgt-/, '');
        if (!highlightedCols.has(sCol) || !highlightedCols.has(tCol)) {
          return { ...e, className: buildEdgeCls(e, 'loom-edge-dim-field') };
        }
      }
      // Node-level edges: dim if neither endpoint has a highlighted column
      if (!isCfEdge && nodesWithHighlight) {
        const srcHas = nodesWithHighlight.has(e.source);
        const tgtHas = nodesWithHighlight.has(e.target);
        if (!srcHas || !tgtHas) {
          return { ...e, className: buildEdgeCls(e, 'loom-edge-dim-table') };
        }
      }
      if (inTable && inField) return stripEdgeDim(e);
      return { ...e, className: buildEdgeCls(e, inField ? 'loom-edge-dim-table' : 'loom-edge-dim-field') };
    }));

    // Fly to the focal node after style update settles.
    if (activeId) {
      let raf2: number;
      const raf1 = requestAnimationFrame(() => {
        raf2 = requestAnimationFrame(() => {
          fitView({
            nodes:   [{ id: activeId }],
            duration: 600,
            padding:  0.08,
            maxZoom:  1.8,
            minZoom:  0.15,
          });
        });
      });
      return () => { cancelAnimationFrame(raf1); cancelAnimationFrame(raf2); };
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filter.tableFilter, filter.stmtFilter, filter.fieldFilter, filter.depth,
      filter.upstream, filter.downstream, viewLevel, layouting, getEdges, getNodes, fitView]);

  return { layouting, layoutError };
}
