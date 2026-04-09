import { useEffect, useRef, useState } from 'react';
import type { Dispatch, SetStateAction } from 'react';
import { useReactFlow } from '@xyflow/react';

import { useLoomStore }              from '../../stores/loomStore';
import { applyELKLayout, cancelPendingLayouts } from '../../utils/layoutGraph';
import { applyL1Layout }             from '../../utils/layoutL1';
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

function stripEdgeDim(e: LoomEdge): LoomEdge {
  if (!e.style?.opacity) return e;
  const s = { ...e.style };
  delete s.opacity;
  return { ...e, style: s };
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
    setAvailableFields,
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
      setAvailableFields([]);
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
        // Populate field dropdown: collect unique column/output-column labels
        const fields = layoutedNodes
          .filter((n) => n.data.nodeType === 'DaliColumn' || n.data.nodeType === 'DaliOutputColumn')
          .map((n) => n.data.label)
          .filter((label, idx, arr) => arr.indexOf(label) === idx)
          .sort();
        setAvailableFields(fields);
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

  // ── Post-layout dimming — tableFilter, stmtFilter + fieldFilter (LOOM-031) ───
  // These phases are intentionally absent from displayGraph to avoid ELK re-runs.
  useEffect(() => {
    if (layouting || viewLevel !== 'L2') return;
    const { tableFilter, stmtFilter, fieldFilter } = filter;
    const activeId = stmtFilter ?? tableFilter;
    const DIM_TABLE = 0.18;
    const DIM_FIELD = 0.08;

    if (!activeId && !fieldFilter) {
      // Skip cleanup if no dimming was previously applied (avoid no-op setNodes/setEdges)
      if (!isDimmedRef.current) return;
      isDimmedRef.current = false;
      setNodes((ns) => ns.map(stripNodeDim));
      setEdges((es) => es.map(stripEdgeDim));
      return;
    }
    isDimmedRef.current = true;

    const currentEdges = getEdges();

    // ── Table / stmt filter: one-hop connected set ────────────────────────
    let tableConnected: Set<string> | null = null;
    if (activeId) {
      tableConnected = new Set([activeId]);
      for (const e of currentEdges) {
        if (e.source === activeId) tableConnected.add(e.target);
        if (e.target === activeId) tableConnected.add(e.source);
      }
    }

    // ── Field filter: label match + one-hop neighbours ────────────────────
    let fieldRelevant: Set<string> | null = null;
    if (fieldFilter) {
      const fieldName = fieldFilter.toLowerCase();
      const relevant  = new Set<string>();
      for (const n of (getNodes() as unknown) as LoomNode[]) {
        const byLabel = n.data.label?.toLowerCase() === fieldName;
        const byCol   = (n.data.columns as ColumnInfo[] | undefined)?.some(
          (c) => c.name.toLowerCase() === fieldName,
        );
        if (byLabel || byCol) relevant.add(n.id);
      }
      for (const e of currentEdges) {
        if (relevant.has(e.source) || relevant.has(e.target)) {
          relevant.add(e.source);
          relevant.add(e.target);
        }
      }
      fieldRelevant = relevant;
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
      if (inTable && inField) return stripEdgeDim(e);
      return { ...e, style: { ...e.style, opacity: inField ? DIM_TABLE : DIM_FIELD } };
    }));

    // Fly to the table/stmt focal node after style update settles.
    if (activeId) {
      let raf1: number;
      let raf2: number;
      raf1 = requestAnimationFrame(() => {
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
  }, [filter.tableFilter, filter.stmtFilter, filter.fieldFilter, viewLevel, layouting, getEdges, getNodes, fitView]);

  return { layouting, layoutError };
}
