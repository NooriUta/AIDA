import { useMemo } from 'react';

import { useLoomStore } from '../../stores/loomStore';
import type { LoomNode, LoomEdge } from '../../types/graph';

interface Graph {
  nodes: LoomNode[];
  edges: LoomEdge[];
}

/**
 * 9-phase display pipeline: takes rawGraph, applies scope filters, visibility
 * rules, direction filters, and dimming. Does NOT trigger ELK re-layout for
 * table/field filter changes — those are handled by useLoomLayout post-layout.
 *
 * Returns displayGraph ready to be passed to the layout hook.
 */
export function useDisplayGraph(rawGraph: Graph | null) {
  const {
    viewLevel,
    l1ScopeStack,
    expandedDbs,
    l1Filter,
    l1HierarchyFilter,
    filter,
    hiddenNodeIds,
    selectedNodeId,
  } = useLoomStore();

  // ── L1 scope filter: dim nodes outside selected app (3-level parentId-aware) ─
  const scopedGraph = useMemo(() => {
    if (!rawGraph) return null;
    if (viewLevel !== 'L1' || l1ScopeStack.length === 0) return rawGraph;

    const scopeId = l1ScopeStack[l1ScopeStack.length - 1].nodeId;

    // L1 has 3 levels: App (group) → DB (child) → Schema (grandchild).
    const scopedDbIds = new Set(
      rawGraph.nodes
        .filter((n) => n.type === 'databaseNode' && n.parentId === scopeId)
        .map((n) => n.id),
    );

    const nodes = rawGraph.nodes.map((node) => {
      const inScope =
        node.id === scopeId ||
        node.parentId === scopeId ||
        (node.parentId != null && scopedDbIds.has(node.parentId));

      return inScope
        ? node
        : { ...node, style: { ...node.style, opacity: 0.15, pointerEvents: 'none' as const } };
    });

    return { nodes, edges: rawGraph.edges };
  }, [rawGraph, viewLevel, l1ScopeStack]);

  const COLUMN_EDGE_TYPES = useMemo(
    () => new Set(['HAS_COLUMN', 'ATOM_REF_COLUMN', 'HAS_ATOM', 'ATOM_PRODUCES',
                   'HAS_AFFECTED_COL', 'HAS_OUTPUT_COL']),
    [],
  );

  // ── displayGraph: phases 1-3c + 5-6 ──────────────────────────────────────────
  // Phase 4 (fieldFilter) is intentionally absent — handled in post-layout effect (LOOM-031)
  // so field selection changes don't trigger ELK re-layout.
  const displayGraph = useMemo(() => {
    if (!scopedGraph) return null;

    // Phase 1 — L1 system-level + depth filter
    let base = scopedGraph;
    if (viewLevel === 'L1') {
      const hideDb     = l1Filter.systemLevel || l1Filter.depth === 1;
      const hideSchema = hideDb || l1Filter.depth === 2;
      if (hideDb || hideSchema) {
        base = {
          nodes: base.nodes.map((n) => {
            if (hideDb && n.type === 'databaseNode')     return { ...n, hidden: true };
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

    // Phase 3 — Table-level view: strip column rows + column-level edges (LOOM-025)
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
        if (!filter.upstream   && et === 'READS_FROM') return false;
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
      // Keep a node visible if it still has any data-flow connection OR had none to begin with
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

    // Phase 3c — cf edge toggle: hide HAS_AFFECTED_COL / HAS_OUTPUT_COL when showCfEdges=false
    // tableLevelView (phase 3) already hides them via COLUMN_EDGE_TYPES, so this
    // phase only acts when tableLevelView is off.
    if (!filter.showCfEdges && !filter.tableLevelView && viewLevel !== 'L1') {
      base = {
        nodes: base.nodes,
        edges: base.edges.filter((e) => {
          const et = e.data?.edgeType as string;
          return et !== 'HAS_AFFECTED_COL' && et !== 'HAS_OUTPUT_COL';
        }),
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
          inScope.add(schemaId);
          const sn = base.nodes.find((n) => n.id === schemaId);
          if (sn?.parentId) {
            inScope.add(sn.parentId);
            const dn = base.nodes.find((n) => n.id === sn.parentId);
            if (dn?.parentId) inScope.add(dn.parentId);
          }
        } else {
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
  // filter.fieldFilter intentionally omitted — handled in the post-layout effect (LOOM-031)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [scopedGraph, viewLevel, l1Filter.systemLevel, l1Filter.depth, hiddenNodeIds, filter.tableLevelView, filter.showCfEdges, filter.upstream, filter.downstream, COLUMN_EDGE_TYPES, l1HierarchyFilter, selectedNodeId, expandedDbs]);

  return { displayGraph };
}
