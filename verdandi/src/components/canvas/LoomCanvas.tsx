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
import { ColumnNode }       from './nodes/ColumnNode';
import { StatementNode }    from './nodes/StatementNode';
import { RoutineGroupNode } from './nodes/RoutineGroupNode';
import { ApplicationNode }  from './nodes/ApplicationNode';
import { DatabaseNode }     from './nodes/DatabaseNode';
import { L1SchemaNode }     from './nodes/L1SchemaNode';
import { Breadcrumb }       from './Breadcrumb';
import { NodeContextMenu }  from './NodeContextMenu';
import { ExportPanel }      from './ExportPanel';
import type { ContextMenuState } from './NodeContextMenu';

import { ErrorBoundary }            from '../ErrorBoundary';
import { useLoomStore }            from '../../stores/loomStore';
import { clearLayoutCache }        from '../../utils/layoutGraph';
import { isUnauthorized }          from '../../services/lineage';
import { SCOPE_FILTER_TYPES }      from '../../utils/transformGraph';
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
  routineGroupNode: RoutineGroupNode as NodeTypes[string],
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
  const { layouting, layoutError } = useLoomLayout(displayGraph, setNodes, setEdges, stmtColsReady);
  const { onMoveEnd }              = useFitView(layouting);
  useFilterSync(rawGraph);

  // ── Close context menu on level change; clear ELK cache on scope change ──────
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

  // ── Node interactions ────────────────────────────────────────────────────────
  const onNodeClick = useCallback((_: React.MouseEvent, node: LoomNode) => {
    selectNode(node.id, node.data);

    if (viewLevel === 'L1') {
      if (node.type === 'databaseNode') {
        // Single-click on DB → drill down to L2 showing all schemas
        const scope = `db-${node.data.label}`;
        drillDown(scope, node.data.label, node.data.nodeType);
      } else if (node.type === 'l1SchemaNode' && node.parentId) {
        setL1HierarchyDb(node.parentId);
        setL1HierarchySchema(node.id);
      }
    }
  }, [selectNode, viewLevel, drillDown, setL1HierarchyDb, setL1HierarchySchema]);

  // Double-click:
  //   Application on L1      → scope filter (stays on L1, dims other apps)
  //   Database on L1         → drillDown to DB-scope L2
  //   Schema chip on L1      → drillDown to schema L2
  //   Schema / Package on L2 → drillDown (L2 → L3)
  //   Table on L2            → drillDown (L2 → L3)
  const onNodeDoubleClick = useCallback((_: React.MouseEvent, node: LoomNode) => {
    if (viewLevel === 'L1' && SCOPE_FILTER_TYPES.has(node.data.nodeType)) {
      pushL1Scope(node.id, node.data.label, node.data.nodeType);
      return;
    }
    if (!node.data.childrenAvailable || viewLevel === 'L3') return;
    let scope: string;
    if (node.data.nodeType === 'DaliSchema') {
      const dbName = node.data.metadata?.databaseName as string | null | undefined;
      scope = dbName ? `schema-${node.data.label}|${dbName}` : `schema-${node.data.label}`;
    } else if (node.data.nodeType === 'DaliPackage') {
      scope = `pkg-${node.data.label}`;
    } else if (node.data.nodeType === 'DaliDatabase') {
      scope = `db-${node.data.label}`;
    } else {
      scope = node.id;
    }
    drillDown(scope, node.data.label, node.data.nodeType);
  }, [viewLevel, pushL1Scope, drillDown]);

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

  return (
    <ZoomLevelProvider value={zoomLevel}>
    <div ref={containerRef} style={{ width: '100%', height: '100%', position: 'relative' }}>
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
                <span style={{ fontSize: '10px', color: 'var(--t4)', letterSpacing: '0.05em' }}>
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
        zoomOnDoubleClick={false}
      >
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
