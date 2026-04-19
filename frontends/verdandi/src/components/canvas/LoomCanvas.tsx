import { memo, useCallback, useEffect, useRef, useState, useMemo } from 'react';
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
  type OnMove,
} from '@xyflow/react';

import { ZoomLevelProvider, LOD_COMPACT_ZOOM } from './ZoomLevelContext';
import { NODE_TYPES }       from './nodeTypes';
import { CanvasOverlays }   from './CanvasOverlays';
import { Breadcrumb }       from './Breadcrumb';
import { NodeContextMenu }  from './NodeContextMenu';
import { ExportPanel }      from './ExportPanel';
import type { ContextMenuState } from './NodeContextMenu';

import { useLoomStore }            from '../../stores/loomStore';
import { clearLayoutCache }        from '../../utils/layoutGraph';
import { isUnauthorized }          from '../../services/lineage';
import { CANVAS }                  from '../../utils/constants';
import { getMinimapNodeColor }     from '../../utils/minimapColors';
import type { LoomNode, LoomEdge } from '../../types/graph';

import { useGraphData }         from '../../hooks/canvas/useGraphData';
import { useExpansion }         from '../../hooks/canvas/useExpansion';
import { useDisplayGraph }      from '../../hooks/canvas/useDisplayGraph';
import { useLoomLayout }        from '../../hooks/canvas/useLoomLayout';
import { useFitView }           from '../../hooks/canvas/useFitView';
import { useFilterSync }        from '../../hooks/canvas/useFilterSync';
import { useNodeInteractions }  from '../../hooks/canvas/useNodeInteractions';

// ─── Inner canvas ─────────────────────────────────────────────────────────────
const LoomCanvasInner = memo(() => {
  const { viewLevel, currentScope, theme, selectNode, clearL1HierarchyFilter } = useLoomStore();

  const [nodes, setNodes, onNodesChange] = useNodesState<LoomNode>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState<LoomEdge>([]);
  const [contextMenu, setContextMenu]    = useState<ContextMenuState>(null);

  const containerRef = useRef<HTMLDivElement>(null);

  // ── LOD: re-render only on threshold crossing ───────────────────────────────
  const [zoomLevel, setZoomLevel] = useState(1);
  const onMove: OnMove = useCallback((_: unknown, viewport) => {
    setZoomLevel((prev) => {
      const isCompactNow  = prev < LOD_COMPACT_ZOOM;
      const isCompactNext = viewport.zoom < LOD_COMPACT_ZOOM;
      return isCompactNow !== isCompactNext ? viewport.zoom : prev;
    });
  }, []);

  // ── Hooks ────────────────────────────────────────────────────────────────────
  const { rawGraph, activeQuery, stmtColsReady } = useGraphData();
  useExpansion();
  const { displayGraph }                                    = useDisplayGraph(rawGraph);
  const { layouting, layoutError, layoutWarning, triggerFullLayout } =
    useLoomLayout(displayGraph, setNodes, setEdges, stmtColsReady);
  const { onMoveEnd } = useFitView(layouting);
  useFilterSync(rawGraph);

  const { onNodeClick, onNodeDoubleClick, onNodeContextMenu } =
    useNodeInteractions(setContextMenu);

  // ── Side effects ─────────────────────────────────────────────────────────────
  // eslint-disable-next-line react-hooks/set-state-in-effect
  useEffect(() => { setContextMenu(null); }, [viewLevel]);
  useEffect(() => { clearLayoutCache(); },  [currentScope]);

  const hasMore         = (activeQuery.data as { hasMore?: boolean } | undefined)?.hasMore ?? false;
  const setGraphTruncated = useLoomStore((s) => s.setGraphTruncated);
  useEffect(() => { setGraphTruncated(hasMore); }, [hasMore, setGraphTruncated]);

  // ── Derived state ────────────────────────────────────────────────────────────
  const statusKey: string | null = (() => {
    if (activeQuery.isError)
      return isUnauthorized(activeQuery.error) ? 'status.unauthorized' : 'status.error';
    if (layoutError) return 'status.error';
    if (!activeQuery.isLoading && !layouting && displayGraph?.nodes.length === 0)
      return 'status.empty';
    return null;
  })();

  const isLoading      = activeQuery.isLoading || !stmtColsReady || layouting;
  const pendingNodeCount = displayGraph?.nodes.filter((n) => !n.hidden).length ?? 0;
  const isLargeGraph   = pendingNodeCount > 100;

  const tableLevelView = useLoomStore((s) => s.filter.tableLevelView);
  const isCompact      = zoomLevel < LOD_COMPACT_ZOOM;
  const wrapperCls     = [
    !tableLevelView && 'loom-column-mode',
    isCompact       && 'loom-compact',
  ].filter(Boolean).join(' ') || undefined;

  const rfTheme = useMemo(
    () => (theme === 'dark' ? 'dark' : 'light') as 'dark' | 'light',
    [theme],
  );

  return (
    <ZoomLevelProvider value={zoomLevel}>
      <div ref={containerRef} className={wrapperCls}
        style={{ width: '100%', height: '100%', position: 'relative' }}>

        <CanvasOverlays
          isLoading={isLoading}
          isLargeGraph={isLargeGraph}
          layouting={layouting}
          pendingNodeCount={pendingNodeCount}
          statusKey={statusKey}
          layoutWarning={layoutWarning}
          triggerFullLayout={triggerFullLayout}
          activeQueryLoading={activeQuery.isLoading}
        />

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
          nodesDraggable={false}
          nodesConnectable={false}
          elementsSelectable={true}
          panOnDrag={true}
          zoomOnScroll={true}
          zoomOnDoubleClick={false}
          onError={(code, message) => {
            if (code === '008') return;
            console.warn(`[React Flow] (${code})`, message);
          }}
        >
          <Background
            variant={BackgroundVariant.Dots}
            gap={24} size={1.5}
            color="color-mix(in srgb, var(--bd) 30%, transparent)"
          />
          <Controls position="bottom-right" />
          <MiniMap
            position="bottom-left"
            nodeColor={getMinimapNodeColor as (node: unknown) => string}
            maskColor={theme === 'dark' ? 'rgba(20,17,8,0.72)' : 'rgba(245,243,238,0.72)'}
            style={{ border: '1px solid var(--bd)', borderRadius: 'var(--seer-radius-md)' }}
            pannable
            zoomable
          />
          <Panel position="top-right" style={{ margin: '8px' }}>
            <ExportPanel containerRef={containerRef} />
          </Panel>
        </ReactFlow>

        <NodeContextMenu menu={contextMenu} onClose={() => setContextMenu(null)} />
      </div>
    </ZoomLevelProvider>
  );
});

LoomCanvasInner.displayName = 'LoomCanvasInner';

// ─── Public export ────────────────────────────────────────────────────────────
export const LoomCanvas = memo(() => (
  <ReactFlowProvider>
    <LoomCanvasInner />
  </ReactFlowProvider>
));

LoomCanvas.displayName = 'LoomCanvas';
