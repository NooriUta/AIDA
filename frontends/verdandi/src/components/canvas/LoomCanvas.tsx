import { memo, useCallback, useEffect, useRef, useState, useMemo } from 'react';
import { useTranslation } from 'react-i18next';
import {
  ReactFlow,
  ReactFlowProvider,
  Background,
  BackgroundVariant,
  Controls,
  MiniMap,
  Panel,
  useNodesState,
  useEdgesState,
  type NodeTypes,
  type OnMove,
} from '@xyflow/react';

import { ZoomLevelProvider, LOD_COMPACT_ZOOM } from './ZoomLevelContext';

import { SchemaNode }       from './nodes/SchemaNode';
import { SchemaGroupNode }  from './nodes/SchemaGroupNode';
import { PackageNode }      from './nodes/PackageNode';
import { TableNode }        from './nodes/TableNode';
import { RoutineNode }      from './nodes/RoutineNode';
import { RecordNode }       from './nodes/RecordNode';
import { ColumnNode }       from './nodes/ColumnNode';
import { StatementNode }    from './nodes/StatementNode';
import { RoutineGroupNode }  from './nodes/RoutineGroupNode';
import { PackageGroupNode }  from './nodes/PackageGroupNode';
import { ApplicationNode }  from './nodes/ApplicationNode';
import { DatabaseNode }     from './nodes/DatabaseNode';
import { L1SchemaNode }     from './nodes/L1SchemaNode';
import { Breadcrumb }       from './Breadcrumb';
import { NodeContextMenu }  from './NodeContextMenu';
import { ExportPanel }      from './ExportPanel';
import type { ContextMenuState } from './NodeContextMenu';

import { useLoomStore }            from '../../stores/loomStore';
import { clearLayoutCache }        from '../../utils/layoutGraph';
import { isUnauthorized }          from '../../services/lineage';
import { SCOPE_FILTER_TYPES }      from '../../utils/transformGraph';
import { CANVAS }                  from '../../utils/constants';
import type { LoomNode }           from '../../types/graph';

import { useGraphData }    from '../../hooks/canvas/useGraphData';
import { useExpansion }    from '../../hooks/canvas/useExpansion';
import { useDisplayGraph } from '../../hooks/canvas/useDisplayGraph';
import { useLoomLayout }   from '../../hooks/canvas/useLoomLayout';
import { useFitView }      from '../../hooks/canvas/useFitView';
import { useFilterSync }   from '../../hooks/canvas/useFilterSync';

// ─── nodeTypes must be defined OUTSIDE the render function ───────────────────
const NODE_TYPES: NodeTypes = {
  schemaNode:       SchemaNode       as NodeTypes[string],
  schemaGroupNode:  SchemaGroupNode  as NodeTypes[string],
  packageNode:      PackageNode      as NodeTypes[string],
  tableNode:        TableNode        as NodeTypes[string],
  routineNode:      RoutineNode      as NodeTypes[string],
  recordNode:       RecordNode       as NodeTypes[string],  // Phase S2.4 — PL/SQL records
  routineGroupNode:  RoutineGroupNode  as NodeTypes[string],
  packageGroupNode:  PackageGroupNode  as NodeTypes[string],
  statementNode:    StatementNode    as NodeTypes[string],
  columnNode:       ColumnNode       as NodeTypes[string],
  atomNode:         ColumnNode       as NodeTypes[string], // reuse ColumnNode for atoms
  applicationNode:  ApplicationNode  as NodeTypes[string],
  databaseNode:     DatabaseNode     as NodeTypes[string],
  l1SchemaNode:     L1SchemaNode     as NodeTypes[string],
};

// ─── Inner canvas (needs ReactFlowProvider to already be mounted) ─────────────
const LoomCanvasInner = memo(() => {
  const {
    viewLevel,
    currentScope,
    theme,
    selectNode,
    drillDown,
    pushL1Scope,
    setL1HierarchyDb,
    setL1HierarchySchema,
    clearL1HierarchyFilter,
    setTableFilter,
    setFieldFilter,
  } = useLoomStore();
  const { t } = useTranslation();

  const [nodes, setNodes, onNodesChange] = useNodesState<LoomNode>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState([]);
  const [contextMenu, setContextMenu] = useState<ContextMenuState>(null);

  // ── LOD: track zoom level for compact rendering ─────────────────────────────
  // Only re-render when crossing the LOD threshold, not on every pixel of zoom.
  const [zoomLevel, setZoomLevel] = useState(1);
  const onMove: OnMove = useCallback((_: unknown, viewport) => {
    setZoomLevel((prev) => {
      const isCompactNow  = prev < LOD_COMPACT_ZOOM;
      const isCompactNext = viewport.zoom < LOD_COMPACT_ZOOM;
      // Only update state when crossing the LOD boundary
      return isCompactNow !== isCompactNext ? viewport.zoom : prev;
    });
  }, []);

  /** Points to the outer wrapper div — passed to ExportPanel for PNG/SVG capture. */
  const containerRef = useRef<HTMLDivElement>(null);

  // ── Hooks ────────────────────────────────────────────────────────────────────
  const { rawGraph, activeQuery, stmtColsReady } = useGraphData();
  useExpansion();
  const { displayGraph }           = useDisplayGraph(rawGraph);
  const { layouting, layoutError, layoutWarning, triggerFullLayout } = useLoomLayout(displayGraph, setNodes, setEdges, stmtColsReady);
  const { onMoveEnd }              = useFitView(layouting);
  useFilterSync(rawGraph);

  // ── Close context menu on level change; clear ELK cache on scope change ──────
  // eslint-disable-next-line react-hooks/set-state-in-effect -- sync with external viewLevel
  useEffect(() => { setContextMenu(null); }, [viewLevel]);
  useEffect(() => { clearLayoutCache(); }, [currentScope]);

  // ── Status message key for error / empty states ─────────────────────────────
  const statusKey: string | null = (() => {
    if (activeQuery.isError) {
      return isUnauthorized(activeQuery.error) ? 'status.unauthorized' : 'status.error';
    }
    if (layoutError) return 'status.error';
    if (!activeQuery.isLoading && !layouting && displayGraph?.nodes.length === 0) {
      return 'status.empty';
    }
    return null;
  })();

  // Show loading while: primary query fetches, column enrichment settles, or ELK runs.
  // The stmtColsReady guard prevents a blank canvas between data arrival and ELK start.
  const isLoading = activeQuery.isLoading || !stmtColsReady || layouting;

  // Rough node count estimate for the large-graph loading hint (visible nodes in display graph).
  const pendingNodeCount = displayGraph?.nodes.filter((n) => !n.hidden).length ?? 0;
  const isLargeGraph = pendingNodeCount > 100;

  // hasMore: backend signalled that the graph was truncated at NODE_LIMIT.
  const hasMore = (activeQuery.data as { hasMore?: boolean } | undefined)?.hasMore ?? false;
  const setGraphTruncated = useLoomStore((s) => s.setGraphTruncated);
  useEffect(() => { setGraphTruncated(hasMore); }, [hasMore, setGraphTruncated]);

  // ── Node interactions ────────────────────────────────────────────────────────
  // Single click: select node (show in inspector) + apply filter.
  //   L1 — hierarchy highlight (DB, Schema)
  //   L2/L3 — Table / Statement / Column filter is handled INSIDE the node
  //           components (TableNode header/column, StatementNode header/column)
  //           to avoid React Flow onNodeClick overwriting internal onClick.
  //           Only standalone React Flow column/atom nodes are filtered here.
  // NEVER drill-down — that's double-click only.
  const onNodeClick = useCallback((_: React.MouseEvent, node: LoomNode) => {
    selectNode(node.id, node.data);

    if (viewLevel === 'L1') {
      if (node.type === 'databaseNode') {
        setL1HierarchyDb(node.id);
      } else if (node.type === 'l1SchemaNode' && node.parentId) {
        setL1HierarchyDb(node.parentId);
        setL1HierarchySchema(node.id);
      }
      return;
    }

    // ── L2 / L3: filter only standalone column/atom nodes ──────────────────
    // Table header/column clicks and Statement header/column clicks are
    // handled inside TableNode.tsx / StatementNode.tsx respectively.
    const nt = node.data.nodeType;
    if (
      nt === 'DaliColumn' || nt === 'DaliOutputColumn' ||
      nt === 'DaliAtom'   || nt === 'DaliAffectedColumn'
    ) {
      const f = useLoomStore.getState().filter;
      setFieldFilter(f.fieldFilter === node.data.label ? null : node.data.label);
    }
  }, [selectNode, viewLevel, setL1HierarchyDb, setL1HierarchySchema, setFieldFilter]);

  // Double-click = DrillDown (architecture rule: single=filter, double=drill):
  //   L1: Application        → scope filter (stays on L1, dims other apps)
  //   L1: Database / Schema  → drillDown to L2
  //   L2: Table / Statement / Schema / Package / Database / Routine → drillDown to L3
  //   L3: DaliStatement → drillDown to L4 (statement tree); all other types = leaf level
  //   Column / Atom / Join → no drill-down (leaf nodes)
  const onNodeDoubleClick = useCallback((_: React.MouseEvent, node: LoomNode) => {
    // L1: Application scope-filter
    if (viewLevel === 'L1' && SCOPE_FILTER_TYPES.has(node.data.nodeType)) {
      pushL1Scope(node.id, node.data.label, node.data.nodeType);
      return;
    }

    // L3: only DaliStatement can drill down to L4; all other types are leaf-level at L3.
    if (viewLevel === 'L3' && node.data.nodeType !== 'DaliStatement') return;

    // Only drillable types (Table, Statement, Schema, Package, Database, Routine)
    const nt = node.data.nodeType;
    if (
      nt !== 'DaliTable'    && nt !== 'DaliStatement' &&
      nt !== 'DaliSchema'   && nt !== 'DaliPackage'   &&
      nt !== 'DaliDatabase' && nt !== 'DaliRoutine'
    ) return;

    // Build scope string
    let scope: string;
    if (nt === 'DaliSchema') {
      const dbName = node.data.metadata?.databaseName as string | null | undefined;
      scope = dbName ? `schema-${node.data.label}|${dbName}` : `schema-${node.data.label}`;
    } else if (nt === 'DaliPackage') {
      scope = `pkg-${node.data.label}`;
    } else if (nt === 'DaliDatabase') {
      scope = `db-${node.data.label}`;
    } else if (nt === 'DaliRoutine') {
      // "routine-<@rid>" routes to exploreRoutineScope on the backend:
      // focused L3 view — root stmts + tables + records only, no subqueries or params.
      scope = `routine-${node.id}`;
    } else {
      scope = node.id;
    }

    // Clear any active filter before drilling (avoid stale filter on L3)
    setTableFilter(null);
    drillDown(scope, node.data.label, nt);
  }, [viewLevel, pushL1Scope, drillDown, setTableFilter]);

  // ── Context menu (LOOM-029) ──────────────────────────────────────────────────
  const onNodeContextMenu = useCallback((e: React.MouseEvent, node: LoomNode) => {
    e.preventDefault();
    setContextMenu({ nodeId: node.id, data: node.data, x: e.clientX, y: e.clientY });
  }, []);

  // ── Minimap node colour ──────────────────────────────────────────────────────
  const minimapNodeColor = useCallback((node: LoomNode) => {
    switch (node.type) {
      case 'applicationNode':  return '#A8B860';
      case 'databaseNode':     return '#a89a7a';
      case 'schemaGroupNode':  return '#88B8A8';
      case 'tableNode':        return '#A8B860';
      case 'routineNode':      return '#7DBF78';
      case 'routineGroupNode': return '#7DBF78';
      case 'statementNode':    return '#7DBF78';
      case 'columnNode':       return '#88B8A8';
      case 'atomNode':         return '#D4922A';
      case 'packageNode':
      default:                 return '#665c48';
    }
  }, []);

  const rfTheme = useMemo(
    () => (theme === 'dark' ? 'dark' : 'light') as 'dark' | 'light',
    [theme],
  );

  // ── Mapping-mode + zoom CSS classes for edge visibility ─────────────────────
  const tableLevelView = useLoomStore((s) => s.filter.tableLevelView);
  const isCompact = zoomLevel < LOD_COMPACT_ZOOM;
  const wrapperCls = [
    !tableLevelView && 'loom-column-mode',
    isCompact       && 'loom-compact',
  ].filter(Boolean).join(' ') || undefined;

  return (
    <ZoomLevelProvider value={zoomLevel}>
    <div ref={containerRef} className={wrapperCls} style={{ width: '100%', height: '100%', position: 'relative' }}>
      {/* Loading overlay */}
      {isLoading && (
        <div style={{
          position: 'absolute', inset: 0, zIndex: 200,
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          background: 'color-mix(in srgb, var(--bg0) 85%, transparent)', backdropFilter: 'blur(3px)',
          pointerEvents: 'none',
        }}>
          <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px' }}>
            <SpinnerSVG size={isLargeGraph ? 36 : 28} />
            <span style={{ fontSize: '11px', color: 'var(--t3)', letterSpacing: '0.07em' }}>
              {t(activeQuery.isLoading ? 'status.loading' : 'canvas.computingLayout')}
            </span>
            {isLargeGraph && layouting && (
              <>
                <span style={{ fontSize: '10px', color: 'var(--t3)', letterSpacing: '0.05em' }}>
                  {t('canvas.nodeCount', { count: pendingNodeCount })}
                </span>
                <LoadingDots />
              </>
            )}
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

      {/* M-3: grid layout warning banner */}
      {layoutWarning && !isLoading && (
        <div style={{
          position: 'absolute', top: 8, left: '50%', transform: 'translateX(-50%)',
          background: 'color-mix(in srgb, var(--wrn) 12%, transparent)',
          border: '1px solid color-mix(in srgb, var(--wrn) 35%, transparent)',
          borderRadius: 6, padding: '5px 12px',
          fontSize: 12, color: 'var(--wrn)',
          display: 'flex', alignItems: 'center', gap: 8, zIndex: 10,
          whiteSpace: 'nowrap',
        }}>
          ⚠ {layoutWarning}
          <button
            onClick={triggerFullLayout}
            style={{ fontSize: 11, color: 'var(--acc)', background: 'none',
                     border: 'none', cursor: 'pointer', padding: 0 }}
          >
            {t('canvas.computeFullLayout')}
          </button>
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
        onNodeContextMenu={onNodeContextMenu}
        onMove={onMove}
        onMoveEnd={onMoveEnd}
        onPaneClick={() => {
          selectNode(null);
          setContextMenu(null);
          if (viewLevel === 'L1') clearL1HierarchyFilter();
        }}
        colorMode={rfTheme}
        fitView
        fitViewOptions={{ padding: CANVAS.FIT_VIEW_PADDING }}
        minZoom={0.1}
        maxZoom={3}
        proOptions={{ hideAttribution: true }}
        /* ── LOOM-023: read-only canvas ── */
        nodesDraggable={false}
        nodesConnectable={false}
        elementsSelectable={true}
        panOnDrag={true}
        zoomOnScroll={true}
        zoomOnDoubleClick={false}
        onError={(code, message) => {
          // Suppress edge-handle warnings (#008) — fire for column-flow edges
          // whose handles haven't mounted yet; harmless but floods the console.
          if (code === '008') return;
          console.warn(`[React Flow] (${code})`, message);
        }}
      >
        <Background
          variant={BackgroundVariant.Dots}
          gap={24}
          size={1.5}
          color="color-mix(in srgb, var(--bd) 30%, transparent)"
        />
        <Controls position="bottom-right" />
        <MiniMap
          position="bottom-left"
          nodeColor={minimapNodeColor as (node: unknown) => string}
          maskColor={theme === 'dark' ? 'rgba(20,17,8,0.72)' : 'rgba(245,243,238,0.72)'}
          style={{ border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)' }}
          pannable
          zoomable
        />
        <Panel position="top-right" style={{ margin: '8px' }}>
          <ExportPanel containerRef={containerRef} />
        </Panel>
      </ReactFlow>

      {/* Context menu (LOOM-029) — rendered outside ReactFlow to avoid RF click capture */}
      <NodeContextMenu menu={contextMenu} onClose={() => setContextMenu(null)} />
    </div>
    </ZoomLevelProvider>
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
function SpinnerSVG({ size = 28 }: { size?: number }) {
  const half = size / 2;
  const r    = half - 3;
  return (
    <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`} fill="none"
      style={{ animation: 'spin 0.8s linear infinite' }}>
      <style>{`@keyframes spin { to { transform: rotate(360deg) } }`}</style>
      <circle cx={half} cy={half} r={r} stroke="var(--seer-border-2)" strokeWidth="2.5" />
      <path d={`M${half} ${half - r} A${r} ${r} 0 0 1 ${half + r} ${half}`}
        stroke="var(--seer-accent)" strokeWidth="2.5" strokeLinecap="round" />
    </svg>
  );
}

// ─── Pulsing dots for large-graph wait hint ───────────────────────────────────
function LoadingDots() {
  return (
    <div style={{ display: 'flex', gap: '5px', alignItems: 'center' }}>
      <style>{`
        @keyframes dotPulse {
          0%, 80%, 100% { opacity: 0.2; transform: scale(0.75); }
          40%            { opacity: 1;   transform: scale(1);    }
        }
      `}</style>
      {[0, 1, 2].map((i) => (
        <div key={i} style={{
          width: 5, height: 5, borderRadius: '50%',
          background: 'var(--seer-accent)',
          animation: `dotPulse 1.4s ease-in-out ${i * 0.22}s infinite`,
        }} />
      ))}
    </div>
  );
}
