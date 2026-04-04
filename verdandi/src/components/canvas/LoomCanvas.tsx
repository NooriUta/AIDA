import { memo, useCallback, useEffect, useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import {
  ReactFlow,
  ReactFlowProvider,
  Background,
  BackgroundVariant,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
  type NodeTypes,
  type OnMoveEnd,
} from '@xyflow/react';

import { SchemaNode }      from './nodes/SchemaNode';
import { PackageNode }     from './nodes/PackageNode';
import { TableNode }       from './nodes/TableNode';
import { RoutineNode }     from './nodes/RoutineNode';
import { ColumnNode }      from './nodes/ColumnNode';
import { ApplicationNode } from './nodes/ApplicationNode';
import { DatabaseNode }    from './nodes/DatabaseNode';
import { L1SchemaNode }    from './nodes/L1SchemaNode';
import { Breadcrumb }      from './Breadcrumb';

import { useLoomStore }                              from '../../stores/loomStore';
import { transformGqlOverview, transformGqlExplore } from '../../utils/transformGraph';
import { applyELKLayout }                            from '../../utils/layoutGraph';
import { applyL1Layout }                             from '../../utils/layoutL1';
import { useOverview, useExplore, useLineage }       from '../../services/hooks';
import { isUnauthorized }                            from '../../services/lineage';
import { SCOPE_FILTER_TYPES }                        from '../../utils/transformGraph';
import type { LoomNode, LoomEdge }                   from '../../types/graph';

// ─── nodeTypes must be defined OUTSIDE the render function ───────────────────
const NODE_TYPES: NodeTypes = {
  schemaNode:       SchemaNode       as NodeTypes[string],
  packageNode:      PackageNode      as NodeTypes[string],
  tableNode:        TableNode        as NodeTypes[string],
  routineNode:      RoutineNode      as NodeTypes[string],
  columnNode:       ColumnNode       as NodeTypes[string],
  atomNode:         ColumnNode       as NodeTypes[string], // reuse ColumnNode for atoms
  applicationNode:  ApplicationNode  as NodeTypes[string],
  databaseNode:     DatabaseNode     as NodeTypes[string],
  l1SchemaNode:     L1SchemaNode     as NodeTypes[string], // compact schema chip for L1
};

// ─── Inner canvas (needs ReactFlowProvider to already be mounted) ─────────────
const LoomCanvasInner = memo(() => {
  const {
    viewLevel,
    currentScope,
    l1ScopeStack,
    expandedDbs,
    l1Filter,
    theme,
    setGraphStats,
    setZoom,
    selectNode,
    drillDown,
    pushL1Scope,
    setAvailableFields,
  } = useLoomStore();
  const { t } = useTranslation();

  const [nodes, setNodes, onNodesChange] = useNodesState<LoomNode>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState<LoomEdge>([]);
  const [layouting, setLayouting] = useState(false);

  // ── Real data queries ───────────────────────────────────────────────────
  // All three are always called (Rules of Hooks); enabled flags prevent firing.
  const overviewQ = useOverview();
  const exploreQ  = useExplore(viewLevel === 'L2' ? currentScope : null);
  const lineageQ  = useLineage(viewLevel === 'L3' ? currentScope : null);

  const activeQuery = viewLevel === 'L1' ? overviewQ
                    : viewLevel === 'L2' ? exploreQ
                    : lineageQ;

  // ── Transform raw GraphQL data → RF nodes / edges ───────────────────────
  const rawGraph = useMemo(() => {
    if (viewLevel === 'L1' && overviewQ.data) return transformGqlOverview(overviewQ.data);
    if (viewLevel === 'L2' && exploreQ.data)  return transformGqlExplore(exploreQ.data);
    if (viewLevel === 'L3' && lineageQ.data)  return transformGqlExplore(lineageQ.data);
    return null;
  }, [viewLevel, overviewQ.data, exploreQ.data, lineageQ.data]);

  // ── L1 scope filter: dim nodes outside selected app (3-level parentId-aware) ─
  const scopedGraph = useMemo(() => {
    if (!rawGraph) return null;
    if (viewLevel !== 'L1' || l1ScopeStack.length === 0) return rawGraph;

    const scopeId = l1ScopeStack[l1ScopeStack.length - 1].nodeId;

    // L1 has 3 levels: App (group) → DB (child) → Schema (grandchild).
    // Build a set of all DB IDs that are direct children of the selected App,
    // so we can also include their L1SchemaNode grandchildren in scope.
    const scopedDbIds = new Set(
      rawGraph.nodes
        .filter((n) => n.type === 'databaseNode' && n.parentId === scopeId)
        .map((n) => n.id),
    );

    const nodes = rawGraph.nodes.map((node) => {
      const inScope =
        node.id === scopeId ||                                                    // the App itself
        (node.parentId === scopeId) ||                                            // DB children
        (node.parentId != null && scopedDbIds.has(node.parentId));               // Schema grandchildren

      return inScope
        ? node
        : {
            ...node,
            style: {
              ...node.style,
              opacity:       0.15,
              pointerEvents: 'none' as const,
            },
          };
    });

    return { nodes, edges: rawGraph.edges };
  }, [rawGraph, viewLevel, l1ScopeStack]);

  // ── L1 system-level view: hide DB and Schema nodes, show only App nodes ─────
  const displayGraph = useMemo(() => {
    if (!scopedGraph || viewLevel !== 'L1' || !l1Filter.systemLevel) return scopedGraph;
    const nodes = scopedGraph.nodes.map((n) =>
      (n.type === 'databaseNode' || n.type === 'l1SchemaNode')
        ? { ...n, hidden: true }
        : n,
    );
    return { nodes, edges: scopedGraph.edges };
  }, [scopedGraph, viewLevel, l1Filter.systemLevel]);

  // ── Layout: L1 = pre-computed + applyL1Layout; L2/L3 = ELK ─────────────────
  useEffect(() => {
    if (!displayGraph) return;
    let cancelled = false;

    // L1 grouped layout: positions set by transformGqlOverview, dynamically
    // adjusted by applyL1Layout whenever expandedDbs changes.
    if (viewLevel === 'L1') {
      const laid = applyL1Layout(displayGraph.nodes, expandedDbs);
      setNodes(laid);
      setEdges(displayGraph.edges);
      setGraphStats(laid.length, displayGraph.edges.length);
      setAvailableFields([]);
      return;
    }

    setLayouting(true);

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
      })
      .finally(() => {
        if (!cancelled) setLayouting(false);
      });

    return () => { cancelled = true; };
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [displayGraph, viewLevel, expandedDbs]);

  // ── Status message key for error / empty states ─────────────────────────
  const statusKey: string | null = (() => {
    if (activeQuery.isError) {
      return isUnauthorized(activeQuery.error) ? 'status.unauthorized' : 'status.error';
    }
    if (!activeQuery.isLoading && !layouting && displayGraph?.nodes.length === 0) {
      return 'status.empty';
    }
    return null;
  })();

  const isLoading = activeQuery.isLoading || layouting;

  // ── Node interactions ───────────────────────────────────────────────────
  const onNodeClick = useCallback((_: React.MouseEvent, node: LoomNode) => {
    selectNode(node.id);
  }, [selectNode]);

  // Double-click:
  //   Application / Service on L1 → scope filter (stays on L1)
  //   Database / Schema / Package  → drillDown (L1 → L2)
  //   Table                        → drillDown (L2 → L3)
  const onNodeDoubleClick = useCallback((_: React.MouseEvent, node: LoomNode) => {
    if (viewLevel === 'L1' && SCOPE_FILTER_TYPES.has(node.data.nodeType)) {
      pushL1Scope(node.id, node.data.label, node.data.nodeType);
      return;
    }
    if (node.data.childrenAvailable && viewLevel !== 'L3') {
      drillDown(node.id, node.data.label, node.data.nodeType);
    }
  }, [viewLevel, pushL1Scope, drillDown]);

  // ── Zoom tracking for status bar ────────────────────────────────────────
  const onMoveEnd: OnMoveEnd = useCallback((_: unknown, viewport) => {
    setZoom(viewport.zoom);
  }, [setZoom]);

  // ── Minimap node colour ─────────────────────────────────────────────────
  const minimapNodeColor = useCallback((node: LoomNode) => {
    switch (node.type) {
      case 'applicationNode': return '#A8B860';
      case 'databaseNode':    return '#a89a7a';
      case 'tableNode':       return '#A8B860';
      case 'routineNode':     return '#7DBF78';
      case 'columnNode':      return '#88B8A8';
      case 'atomNode':        return '#D4922A';
      case 'packageNode':
      default:                return '#665c48';
    }
  }, []);

  const rfTheme = useMemo(
    () => (theme === 'dark' ? 'dark' : 'light') as 'dark' | 'light',
    [theme],
  );

  return (
    <div style={{ width: '100%', height: '100%', position: 'relative' }}>
      {/* Loading overlay */}
      {isLoading && (
        <div style={{
          position: 'absolute', inset: 0, zIndex: 200,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          background: 'rgba(20,17,8,0.75)', backdropFilter: 'blur(3px)',
          pointerEvents: 'none',
        }}>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px' }}>
            <SpinnerSVG />
            <span style={{ fontSize: '11px', color: 'var(--t3)', letterSpacing: '0.07em' }}>
              {t(activeQuery.isLoading ? 'status.loading' : 'canvas.computingLayout')}
            </span>
          </div>
        </div>
      )}

      {/* Error / empty status message */}
      {statusKey && !isLoading && (
        <div style={{
          position: 'absolute', inset: 0, zIndex: 100,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          pointerEvents: 'none',
        }}>
          <span style={{ fontSize: '13px', color: 'var(--t3)', letterSpacing: '0.04em' }}>
            {t(statusKey)}
          </span>
        </div>
      )}

      {/* Breadcrumb */}
      <Breadcrumb />

      <ReactFlow
        nodes={nodes}
        edges={edges}
        onNodesChange={onNodesChange}
        onEdgesChange={onEdgesChange}
        nodeTypes={NODE_TYPES}
        onNodeClick={onNodeClick}
        onNodeDoubleClick={onNodeDoubleClick}
        onMoveEnd={onMoveEnd}
        onPaneClick={() => selectNode(null)}
        colorMode={rfTheme}
        fitView
        fitViewOptions={{ padding: 0.15 }}
        minZoom={0.1}
        maxZoom={3}
        proOptions={{ hideAttribution: true }}
        /* ── LOOM-023: read-only canvas ── */
        nodesDraggable={false}
        nodesConnectable={false}
        elementsSelectable={true}
        panOnDrag={true}
        zoomOnScroll={true}
      >
        {/* Dot-grid: 24px gap, Amber Forest --bd at opacity 0.3 */}
        <Background
          variant={BackgroundVariant.Dots}
          gap={24}
          size={1.5}
          color={theme === 'dark' ? '#42382a4d' : '#d4ccb84d'}
        />
        <Controls position="bottom-right" />
        <MiniMap
          position="bottom-left"
          nodeColor={minimapNodeColor as (node: unknown) => string}
          maskColor={theme === 'dark' ? 'rgba(20,17,8,0.72)' : 'rgba(245,243,238,0.72)'}
          style={{ border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)' }}
        />
      </ReactFlow>
    </div>
  );
});

LoomCanvasInner.displayName = 'LoomCanvasInner';

// ─── Public export wraps with provider ───────────────────────────────────────
export const LoomCanvas = memo(() => (
  <ReactFlowProvider>
    <LoomCanvasInner />
  </ReactFlowProvider>
));

LoomCanvas.displayName = 'LoomCanvas';

// ─── Spinner SVG (no dependency) ─────────────────────────────────────────────
function SpinnerSVG() {
  return (
    <svg width="28" height="28" viewBox="0 0 28 28" fill="none"
      style={{ animation: 'spin 0.8s linear infinite' }}>
      <style>{`@keyframes spin { to { transform: rotate(360deg) } }`}</style>
      <circle cx="14" cy="14" r="11" stroke="var(--seer-border-2)" strokeWidth="2.5" />
      <path d="M14 3 A11 11 0 0 1 25 14" stroke="var(--seer-accent)" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
  );
}
