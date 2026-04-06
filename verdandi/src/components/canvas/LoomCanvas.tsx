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
  useReactFlow,
  type NodeTypes,
  type OnMoveEnd,
} from '@xyflow/react';

import { SchemaNode }      from './nodes/SchemaNode';
import { SchemaGroupNode } from './nodes/SchemaGroupNode';
import { PackageNode }     from './nodes/PackageNode';
import { TableNode }       from './nodes/TableNode';
import { RoutineNode }     from './nodes/RoutineNode';
import { ColumnNode }      from './nodes/ColumnNode';
import { StatementNode }     from './nodes/StatementNode';
import { RoutineGroupNode } from './nodes/RoutineGroupNode';
import { ApplicationNode }  from './nodes/ApplicationNode';
import { DatabaseNode }    from './nodes/DatabaseNode';
import { L1SchemaNode }    from './nodes/L1SchemaNode';
import { Breadcrumb }      from './Breadcrumb';

import { useLoomStore }                              from '../../stores/loomStore';
import { transformGqlOverview, transformGqlExplore, applyStmtColumns } from '../../utils/transformGraph';
import { applyELKLayout }                            from '../../utils/layoutGraph';
import { applyL1Layout }                             from '../../utils/layoutL1';
import { useOverview, useExplore, useLineage, useUpstream, useDownstream, useStmtColumns } from '../../services/hooks';
import { isUnauthorized }                            from '../../services/lineage';
import { SCOPE_FILTER_TYPES }                        from '../../utils/transformGraph';
import type { LoomNode, LoomEdge }                   from '../../types/graph';
import type { ColumnInfo }                            from '../../types/domain';

// ─── nodeTypes must be defined OUTSIDE the render function ───────────────────
const NODE_TYPES: NodeTypes = {
  schemaNode:       SchemaNode       as NodeTypes[string],
  schemaGroupNode:  SchemaGroupNode  as NodeTypes[string], // L2 schema group container
  packageNode:      PackageNode      as NodeTypes[string],
  tableNode:        TableNode        as NodeTypes[string],
  routineNode:      RoutineNode      as NodeTypes[string],
  routineGroupNode: RoutineGroupNode as NodeTypes[string], // L2 routine container (Schema → Routine → Stmt)
  statementNode:    StatementNode    as NodeTypes[string],
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
    l1HierarchyFilter,
    filter,
    hiddenNodeIds,
    theme,
    setGraphStats,
    setZoom,
    selectNode,
    selectedNodeId,
    drillDown,
    pushL1Scope,
    setAvailableFields,
    setAvailableTables,
    setAvailableStmts,
    setAvailableColumns,
    setAvailableApps,
    setAvailableDbs,
    setAvailableSchemas,
    toggleDbExpansion,
    // LOOM-027: expand
    expandRequest,
    expansionGqlNodes,
    expansionGqlEdges,
    addExpansionData,
    clearExpandRequest,
    // Viewport focus
    fitViewRequest,
    clearFitViewRequest,
    // Canvas → FilterToolbarL1 sync
    setL1HierarchyDb,
    setL1HierarchySchema,
    clearL1HierarchyFilter,
    // Node expansion (column rows)
    nodeExpansionState,
    setNodeExpansion,
  } = useLoomStore();
  const { t } = useTranslation();
  const { fitView, getEdges } = useReactFlow();

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

  // ── Statement column enrichment (second-pass after schema explore) ──────────
  // Extract DaliStatement @rids from the explore result so we can fetch their
  // HAS_OUTPUT_COL / HAS_AFFECTED_COL columns in a separate targeted query.
  // This avoids the LIMIT collision between CONTAINS_STMT and HAS_OUTPUT_COL.
  // Send both DaliTable and DaliStatement IDs — backend handles HAS_COLUMN,
  // HAS_OUTPUT_COL, HAS_AFFECTED_COL in one query based on node type matching.
  const stmtIds = useMemo(() => {
    if (viewLevel !== 'L2' || !exploreQ.data) return [] as string[];
    return exploreQ.data.nodes
      .filter((n) => n.type === 'DaliStatement' || n.type === 'DaliTable')
      .map((n) => n.id);
  }, [viewLevel, exploreQ.data]);

  const stmtColsQ = useStmtColumns(stmtIds);

  // ── LOOM-027: upstream / downstream expand queries ────────────────────────
  const upstreamExpandId   = expandRequest?.direction === 'upstream'   ? expandRequest.nodeId : null;
  const downstreamExpandId = expandRequest?.direction === 'downstream' ? expandRequest.nodeId : null;
  const { data: upstreamExpandData,   isSuccess: upstreamOk   } = useUpstream(upstreamExpandId);
  const { data: downstreamExpandData, isSuccess: downstreamOk } = useDownstream(downstreamExpandId);

  useEffect(() => {
    if (upstreamOk && upstreamExpandData && upstreamExpandId) {
      addExpansionData(upstreamExpandId, 'upstream', upstreamExpandData.nodes, upstreamExpandData.edges);
      clearExpandRequest();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [upstreamOk, upstreamExpandData, upstreamExpandId]);

  useEffect(() => {
    if (downstreamOk && downstreamExpandData && downstreamExpandId) {
      addExpansionData(downstreamExpandId, 'downstream', downstreamExpandData.nodes, downstreamExpandData.edges);
      clearExpandRequest();
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [downstreamOk, downstreamExpandData, downstreamExpandId]);

  // ── Transform raw GraphQL data → RF nodes / edges ───────────────────────
  // After transform, merge any expansion data fetched via LOOM-027 expand buttons.
  // stmtColsQ.data is applied here so ELK re-layouts with correct node heights.
  const rawGraph = useMemo(() => {
    let base: { nodes: LoomNode[]; edges: LoomEdge[] } | null = null;
    if (viewLevel === 'L1' && overviewQ.data) base = transformGqlOverview(overviewQ.data);
    else if (viewLevel === 'L2' && exploreQ.data)  base = transformGqlExplore(exploreQ.data);
    else if (viewLevel === 'L3' && lineageQ.data)  base = transformGqlExplore(lineageQ.data);
    if (!base) return null;

    // LOOM-027: merge expansion nodes/edges (de-duplicated by id)
    if (expansionGqlNodes.length > 0 || expansionGqlEdges.length > 0) {
      const expansionGraph = transformGqlExplore({ nodes: expansionGqlNodes, edges: expansionGqlEdges });
      const existingNodeIds = new Set(base.nodes.map((n) => n.id));
      const existingEdgeIds = new Set(base.edges.map((e) => e.id));
      base = {
        nodes: [...base.nodes, ...expansionGraph.nodes.filter((n) => !existingNodeIds.has(n.id))],
        edges: [...base.edges, ...expansionGraph.edges.filter((e) => !existingEdgeIds.has(e.id))],
      };
    }

    // Second-pass stmt column enrichment: apply before ELK so node heights are correct.
    if (viewLevel === 'L2' && stmtColsQ.data && stmtColsQ.data.edges.length > 0) {
      const { nodes: enrichedNodes, cfEdges } = applyStmtColumns(base.nodes, base.edges, stmtColsQ.data);
      base = { nodes: enrichedNodes, edges: [...base.edges, ...cfEdges] };
    }

    return base;
  }, [viewLevel, overviewQ.data, exploreQ.data, lineageQ.data, expansionGqlNodes, expansionGqlEdges, stmtColsQ.data]);

  // ── Populate App/DB/Schema lists for L1 filter panel ──────────────────────
  useEffect(() => {
    if (viewLevel !== 'L1' || !rawGraph) {
      setAvailableApps([]);
      setAvailableDbs([]);
      setAvailableSchemas([]);
      return;
    }
    setAvailableApps(
      rawGraph.nodes
        .filter((n) => n.type === 'applicationNode')
        .map((n) => ({ id: n.id, label: n.data.label })),
    );
    setAvailableDbs(
      rawGraph.nodes
        .filter((n) => n.type === 'databaseNode')
        .map((n) => ({ id: n.id, label: n.data.label, appId: n.parentId ?? null })),
    );
    setAvailableSchemas(
      rawGraph.nodes
        .filter((n) => n.type === 'l1SchemaNode')
        .map((n) => ({ id: n.id, label: n.data.label, dbId: n.parentId ?? '' })),
    );
  }, [viewLevel, rawGraph, setAvailableApps, setAvailableDbs, setAvailableSchemas]);

  // ── Populate Table/Stmt lists for L2 filter dropdowns ────────────────────────
  useEffect(() => {
    if (viewLevel !== 'L2' || !rawGraph) {
      setAvailableTables([]);
      setAvailableStmts([]);
      return;
    }
    const tables = rawGraph.nodes
      .filter((n) => n.type === 'tableNode')
      .map((n) => ({ id: n.id, label: n.data.label }))
      .sort((a, b) => a.label.localeCompare(b.label));
    setAvailableTables(tables);

    const tableIds = new Set(tables.map((t) => t.id));
    // Build map: stmtId → Set of connected tableIds (via READS_FROM / WRITES_TO)
    const stmtToTables = new Map<string, Set<string>>();
    for (const e of rawGraph.edges) {
      const et = e.data?.edgeType as string;
      if (et !== 'READS_FROM' && et !== 'WRITES_TO') continue;
      const [stmtId, tableId] = tableIds.has(e.target)
        ? [e.source, e.target]
        : tableIds.has(e.source)
        ? [e.target, e.source]
        : [null, null];
      if (!stmtId || !tableId) continue;
      if (!stmtToTables.has(stmtId)) stmtToTables.set(stmtId, new Set());
      stmtToTables.get(stmtId)!.add(tableId);
    }
    const stmts = rawGraph.nodes
      .filter((n) => n.type === 'statementNode')
      .map((n) => ({
        id:                n.id,
        label:             n.data.label,
        connectedTableIds: Array.from(stmtToTables.get(n.id) ?? []),
      }))
      .sort((a, b) => a.label.localeCompare(b.label));
    setAvailableStmts(stmts);
  }, [viewLevel, rawGraph, setAvailableTables, setAvailableStmts]);

  // ── Populate Column cascade: columns of the selected table or stmt ─────────
  // Cascades: table selected → show DaliColumn list; stmt selected → output cols list.
  // Re-runs when stmtColsQ.data arrives so stmt cols appear after second-pass fetch.
  useEffect(() => {
    if (viewLevel !== 'L2' || !rawGraph) { setAvailableColumns([]); return; }
    const activeId = filter.stmtFilter ?? filter.tableFilter;
    if (!activeId) { setAvailableColumns([]); return; }
    const node = rawGraph.nodes.find((n) => n.id === activeId);
    const cols = (node?.data.columns as ColumnInfo[] | undefined) ?? [];
    setAvailableColumns(cols.map((c) => ({ id: c.id, name: c.name })));
  }, [viewLevel, rawGraph, filter.tableFilter, filter.stmtFilter, setAvailableColumns]);

  // ── Auto-expand parent DB when navigating to a schema chip from search ───────
  // rawGraph always has l1SchemaNode.hidden=true (initial state), so we check
  // expandedDbs instead of node.hidden to avoid toggling an already-open DB
  // when the user simply clicks a visible schema chip.
  useEffect(() => {
    if (viewLevel !== 'L1' || !selectedNodeId || !rawGraph) return;
    const node = rawGraph.nodes.find((n) => n.id === selectedNodeId);
    if (node?.type === 'l1SchemaNode' && node.parentId && !expandedDbs.has(node.parentId)) {
      toggleDbExpansion(node.parentId);
    }
  }, [selectedNodeId, viewLevel, rawGraph, expandedDbs, toggleDbExpansion]);

  // ── Auto-expand table columns on L2 load ─────────────────────────────────────
  // Column-flow edges need column handles in the DOM — ensure tables are at least partial.
  useEffect(() => {
    if (viewLevel !== 'L2' || !rawGraph) return;
    for (const n of rawGraph.nodes) {
      if (n.data.nodeType !== 'DaliTable') continue;
      if ((nodeExpansionState[n.id] ?? 'partial') === 'collapsed') {
        setNodeExpansion(n.id, 'partial');
      }
    }
  }, [viewLevel, rawGraph, nodeExpansionState, setNodeExpansion]);

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

  // ── Column-level edge types suppressed by tableLevelView (LOOM-025) ─────────
  const COLUMN_EDGE_TYPES = useMemo(
    () => new Set(['HAS_COLUMN', 'ATOM_REF_COLUMN', 'HAS_ATOM', 'ATOM_PRODUCES']),
    [],
  );

  // ── displayGraph: 8-phase transform pipeline ─────────────────────────────────
  //   Phase 1:  L1 system-level / depth filter (hide DB/Schema nodes by depth)
  //   Phase 2:  Hide nodes marked via 🔴 button (LOOM-026)
  //   Phase 3:  tableLevelView — strip column rows + column-level edges (LOOM-025)
  //   Phase 3b: direction filter — hide READS_FROM / WRITES_TO edges by ↑↓ toggle
  //   Phase 3c: tableFilter — dim stmts not connected to selected table (L2)
  //   Phase 3d: stmtFilter — dim nodes not connected to selected stmt (L2)
  //   Phase 4:  fieldFilter — dim edges/nodes unrelated to selected field (LOOM-025)
  //   Phase 5:  L1 hierarchy filter (App → DB → Schema) — dim out-of-scope nodes
  //   Phase 6:  L1 schema chip selection — dim other visible schema chips
  const displayGraph = useMemo(() => {
    if (!scopedGraph) return null;

    // Phase 1 — L1 system-level + depth filter
    //   systemLevel / depth=1 → hide DB + Schema
    //   depth=2                → hide Schema chips only (DBs stay)
    //   depth=3 / ∞            → show all (default expand behaviour)
    let base = scopedGraph;
    if (viewLevel === 'L1') {
      const hideDb     = l1Filter.systemLevel || l1Filter.depth === 1;
      const hideSchema = hideDb || l1Filter.depth === 2;
      if (hideDb || hideSchema) {
        base = {
          nodes: base.nodes.map((n) => {
            if (hideDb && n.type === 'databaseNode')  return { ...n, hidden: true };
            if (hideSchema && n.type === 'l1SchemaNode') return { ...n, hidden: true };
            return n;
          }),
          edges: base.edges,
        };
      }
    }

    // Phase 2 — Hidden nodes (LOOM-026 🔴 button)
    if (hiddenNodeIds.size > 0) {
      base = {
        nodes: base.nodes.map((n) =>
          hiddenNodeIds.has(n.id) ? { ...n, hidden: true } : n,
        ),
        edges: base.edges,
      };
    }

    // Phase 3 — Table-level view: strip column rows from nodes + column-level edges (LOOM-025)
    if (filter.tableLevelView && viewLevel !== 'L1') {
      base = {
        nodes: base.nodes.map((n) => ({ ...n, data: { ...n.data, columns: [] } })),
        edges: base.edges.filter(
          (e) => !COLUMN_EDGE_TYPES.has(e.data?.edgeType as string),
        ),
      };
    }

    // Phase 3b — Direction filter: hide READS_FROM / WRITES_TO by ↑↓ toggle
    if (viewLevel !== 'L1' && (!filter.upstream || !filter.downstream)) {
      const filteredEdges = base.edges.filter((e) => {
        const et = e.data?.edgeType as string;
        if (!filter.upstream  && et === 'READS_FROM') return false;
        if (!filter.downstream && et === 'WRITES_TO')  return false;
        return true;
      });
      // Dim nodes that lost all data-flow edges (became isolated)
      const connectedIds = new Set<string>();
      for (const e of filteredEdges) {
        const et = e.data?.edgeType as string;
        if (et === 'READS_FROM' || et === 'WRITES_TO') {
          connectedIds.add(e.source);
          connectedIds.add(e.target);
        }
      }
      // Keep a node visible if it still has any data-flow connection OR if it had none to begin with
      const hadFlow = new Set<string>();
      for (const e of base.edges) {
        const et = e.data?.edgeType as string;
        if (et === 'READS_FROM' || et === 'WRITES_TO') {
          hadFlow.add(e.source);
          hadFlow.add(e.target);
        }
      }
      base = {
        nodes: base.nodes.map((n) =>
          hadFlow.has(n.id) && !connectedIds.has(n.id)
            ? { ...n, style: { ...n.style, opacity: 0.12, pointerEvents: 'none' as const } }
            : n,
        ),
        edges: filteredEdges,
      };
    }

    // Phase 4 — Field filter: dim edges/nodes unrelated to selected field (LOOM-025)
    if (filter.fieldFilter && viewLevel !== 'L1') {
      const fieldName = filter.fieldFilter.toLowerCase();
      // Nodes directly matching the field (by label or containing that column)
      const relevantIds = new Set<string>();
      for (const n of base.nodes) {
        const byLabel = n.data.label?.toLowerCase() === fieldName;
        const byCol   = (n.data.columns as ColumnInfo[] | undefined)?.some(
          (c) => c.name.toLowerCase() === fieldName,
        );
        if (byLabel || byCol) relevantIds.add(n.id);
      }
      // Expand to immediate neighbours (one hop)
      for (const e of base.edges) {
        if (relevantIds.has(e.source) || relevantIds.has(e.target)) {
          relevantIds.add(e.source);
          relevantIds.add(e.target);
        }
      }
      const DIM = 0.08;
      base = {
        nodes: base.nodes.map((n) =>
          relevantIds.has(n.id)
            ? n
            : { ...n, style: { ...n.style, opacity: DIM, pointerEvents: 'none' as const } },
        ),
        edges: base.edges.map((e) =>
          relevantIds.has(e.source) && relevantIds.has(e.target)
            ? e
            : { ...e, style: { ...e.style, opacity: DIM } },
        ),
      };
    }

    // Phase 5 — L1 hierarchy filter (DB → Schema): dim nodes outside selected DB/Schema.
    // App-level scope is already handled by scopedGraph (l1ScopeStack).
    if (viewLevel === 'L1') {
      const { dbId, schemaId } = l1HierarchyFilter;
      if (dbId || schemaId) {
        const DIM_H = 0.12;
        const inScope = new Set<string>();

        if (schemaId) {
          // Schema chip + parent DB + grandparent App (keep parents at full opacity)
          inScope.add(schemaId);
          const sn = base.nodes.find((n) => n.id === schemaId);
          if (sn?.parentId) {
            inScope.add(sn.parentId);
            const dn = base.nodes.find((n) => n.id === sn.parentId);
            if (dn?.parentId) inScope.add(dn.parentId);
          }
        } else {
          // DB + all its schema chips + parent App
          inScope.add(dbId!);
          for (const n of base.nodes) {
            if (n.type === 'l1SchemaNode' && n.parentId === dbId) inScope.add(n.id);
          }
          const dn = base.nodes.find((n) => n.id === dbId);
          if (dn?.parentId) inScope.add(dn.parentId);
        }

        base = {
          nodes: base.nodes.map((n) =>
            inScope.has(n.id)
              ? n
              : { ...n, style: { ...n.style, opacity: DIM_H, pointerEvents: 'none' as const } },
          ),
          edges: base.edges,
        };
      }
    }

    // Phase 6 — L1 schema chip selection: dim other visible schema chips
    if (viewLevel === 'L1' && selectedNodeId) {
      const selNode = base.nodes.find((n) => n.id === selectedNodeId);
      // Only dim when the selected node is a visible schema chip (its parent DB is open)
      if (
        selNode?.type === 'l1SchemaNode' &&
        selNode.parentId &&
        expandedDbs.has(selNode.parentId)
      ) {
        const DIM_CHIP = 0.2;
        base = {
          nodes: base.nodes.map((n) => {
            if (n.type !== 'l1SchemaNode' || n.id === selectedNodeId) return n;
            return { ...n, style: { ...n.style, opacity: DIM_CHIP } };
          }),
          edges: base.edges,
        };
      }
    }

    return base;
  }, [scopedGraph, viewLevel, l1Filter.systemLevel, l1Filter.depth, hiddenNodeIds, filter.tableLevelView, filter.fieldFilter, filter.upstream, filter.downstream, COLUMN_EDGE_TYPES, l1HierarchyFilter, selectedNodeId, expandedDbs]);

  // ── Layout: L1 = pre-computed + applyL1Layout; L2/L3 = ELK ─────────────────
  useEffect(() => {
    if (!displayGraph) return;
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
  }, [displayGraph, viewLevel, expandedDbs, l1Filter.depth, l1Filter.systemLevel]);

  // ── Table/stmt filter dimming — applied POST-layout, bypasses ELK ──────────
  // Phases 3c/3d are intentionally removed from displayGraph so the filter
  // selection does NOT trigger an ELK re-layout (only opacity changes needed).
  // This effect runs after layout completes and whenever the filter changes.
  useEffect(() => {
    if (layouting || viewLevel !== 'L2') return;
    const { tableFilter, stmtFilter } = filter;
    const activeId = stmtFilter ?? tableFilter;
    const DIM = 0.18;

    // Helper: strip only the dim-related overrides we may have added
    const stripNodeDim = (n: LoomNode): LoomNode => {
      if (!n.style?.opacity && !n.style?.pointerEvents) return n;
      const s = { ...n.style };
      delete s.opacity;
      delete s.pointerEvents;
      return { ...n, style: s };
    };
    const stripEdgeDim = (e: LoomEdge): LoomEdge => {
      if (!e.style?.opacity) return e;
      const s = { ...e.style };
      delete s.opacity;
      return { ...e, style: s };
    };

    if (!activeId) {
      setNodes((ns) => ns.map(stripNodeDim));
      setEdges((es) => es.map(stripEdgeDim));
      return;
    }

    const currentEdges = getEdges();
    const connectedIds = new Set<string>([activeId]);
    for (const e of currentEdges) {
      if (e.source === activeId) connectedIds.add(e.target);
      if (e.target === activeId) connectedIds.add(e.source);
    }

    setNodes((ns) => ns.map((n) =>
      connectedIds.has(n.id)
        ? stripNodeDim(n)
        : { ...n, style: { ...n.style, opacity: DIM, pointerEvents: 'none' as const } },
    ));
    setEdges((es) => es.map((e) =>
      connectedIds.has(e.source) && connectedIds.has(e.target)
        ? stripEdgeDim(e)
        : { ...e, style: { ...e.style, opacity: DIM } },
    ));

    // Fly to the selected node after the style update settles.
    // Two rAF: first flushes React's setState batch; second waits for RF to
    // apply the update to its internal node store before fitView reads bounds.
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
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [filter.tableFilter, filter.stmtFilter, viewLevel, layouting, getEdges, fitView]);

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

    // Canvas → FilterToolbarL1 sync (L1 only).
    // Clicking a DB node highlights it in the DB cascade dropdown.
    // Clicking a Schema chip highlights both DB and Schema dropdowns.
    if (viewLevel === 'L1') {
      if (node.type === 'databaseNode') {
        setL1HierarchyDb(node.id);
      } else if (node.type === 'l1SchemaNode' && node.parentId) {
        // Schema chip lives inside a DB → set parent DB first, then schema.
        setL1HierarchyDb(node.parentId);
        setL1HierarchySchema(node.id);
      }
    }
  }, [selectNode, viewLevel, setL1HierarchyDb, setL1HierarchySchema]);

  // Double-click:
  //   Application on L1      → scope filter (stays on L1, dims other apps)
  //   Database on L1         → drillDown to DB-scope L2 (expand/collapse is in header)
  //   Schema chip on L1      → drillDown to schema L2
  //   Schema / Package on L2 → drillDown (L2 → L3)
  //   Table on L2            → drillDown (L2 → L3)
  const onNodeDoubleClick = useCallback((_: React.MouseEvent, node: LoomNode) => {
    if (viewLevel === 'L1' && SCOPE_FILTER_TYPES.has(node.data.nodeType)) {
      pushL1Scope(node.id, node.data.label, node.data.nodeType);
      return;
    }
    if (!node.data.childrenAvailable || viewLevel === 'L3') return;
    // DaliSchema: name-based scope so SHUTTLE dispatches to exploreSchema().
    // Append |DB_NAME when available (l1SchemaNode chips carry it in metadata)
    // so ExploreService can filter by db_name and resolve multi-DB collisions.
    let scope: string;
    if (node.data.nodeType === 'DaliSchema') {
      const dbName = node.data.metadata?.databaseName as string | null | undefined;
      scope = dbName ? `schema-${node.data.label}|${dbName}` : `schema-${node.data.label}`;
    } else if (node.data.nodeType === 'DaliPackage') {
      scope = `pkg-${node.data.label}`;
    } else {
      scope = node.id;
    }
    drillDown(scope, node.data.label, node.data.nodeType);
  }, [viewLevel, pushL1Scope, drillDown]);

  // ── Zoom tracking for status bar ────────────────────────────────────────
  const onMoveEnd: OnMoveEnd = useCallback((_: unknown, viewport) => {
    setZoom(viewport.zoom);
  }, [setZoom]);

  // ── Programmatic fitView: triggered by search (focus node) or L1 return ──
  useEffect(() => {
    if (!fitViewRequest || layouting) return;
    const req = fitViewRequest;
    // Small delay: let React Flow finish painting the new nodes before fitting
    const timer = setTimeout(() => {
      if (req.type === 'full') {
        fitView({ duration: 500, padding: 0.15 });
      } else {
        // Fit the selected node tightly in the viewport so it "fills the window"
        fitView({
          nodes:   [{ id: req.nodeId }],
          duration: 600,
          padding:  0.08,
          maxZoom:  1.8,
          minZoom:  0.15,
        });
      }
      clearFitViewRequest();
    }, 200);
    return () => clearTimeout(timer);
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fitViewRequest, layouting]);

  // ── Minimap node colour ─────────────────────────────────────────────────
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
        onPaneClick={() => {
          selectNode(null);
          // Clear canvas-driven hierarchy filter so toolbar dropdowns reset to "all"
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
