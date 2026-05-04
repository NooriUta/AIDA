import { useCallback, useRef } from 'react';
import { useLoomStore } from '../../stores/loomStore';
import { useHeimdallEmitter } from '../useHeimdallEmitter';
import { SCOPE_FILTER_TYPES } from '../../utils/transformGraph';
import type { LoomNode } from '../../types/graph';
import type { ContextMenuState } from '../../components/canvas/NodeContextMenu';

interface UseNodeInteractionsReturn {
  onNodeClick:        (e: React.MouseEvent, node: LoomNode) => void;
  onNodeDoubleClick:  (e: React.MouseEvent, node: LoomNode) => void;
  onNodeContextMenu:  (e: React.MouseEvent, node: LoomNode) => void;
}

export function useNodeInteractions(
  setContextMenu: (s: ContextMenuState) => void,
): UseNodeInteractionsReturn {
  const {
    viewLevel,
    selectNode,
    drillDown,
    jumpTo,
    pushL1Scope,
    setL1HierarchyDb,
    setL1HierarchySchema,
    setTableFilter,
    setFieldFilter,
  } = useLoomStore();

  const { emit: emitHeimdall } = useHeimdallEmitter();

  // Drill-down core — shared by `onNodeDoubleClick` and the synthesised
  // double-tap path below. Returns true if it actually drilled.
  const drillFromNode = useCallback((node: LoomNode): boolean => {
    if (viewLevel === 'L1' && SCOPE_FILTER_TYPES.has(node.data.nodeType)) {
      pushL1Scope(node.id, node.data.label, node.data.nodeType);
      emitHeimdall('LOOM_DRILL_DOWN', 'INFO', {
        nodeId: node.id, nodeType: node.data.nodeType, nodeLabel: node.data.label, fromLevel: viewLevel,
      });
      return true;
    }
    if (viewLevel === 'L3' && node.data.nodeType !== 'DaliStatement') return false;

    const nt = node.data.nodeType;
    if (
      nt !== 'DaliTable'    && nt !== 'DaliStatement' &&
      nt !== 'DaliSchema'   && nt !== 'DaliPackage'   &&
      nt !== 'DaliDatabase' && nt !== 'DaliRoutine'
    ) return false;

    let scope: string;
    if (nt === 'DaliSchema') {
      const dbName = node.data.metadata?.databaseName as string | null | undefined;
      scope = dbName ? `schema-${node.data.label}|${dbName}` : `schema-${node.data.label}`;
    } else if (nt === 'DaliPackage') {
      scope = `pkg-${node.data.label}`;
    } else if (nt === 'DaliDatabase') {
      scope = `db-${node.data.label}`;
    } else if (nt === 'DaliRoutine') {
      scope = `routine-${node.id}`;
    } else {
      scope = node.id;
    }

    setTableFilter(null);
    drillDown(scope, node.data.label, nt);
    emitHeimdall('LOOM_DRILL_DOWN', 'INFO', {
      nodeId: node.id, nodeType: nt, nodeLabel: node.data.label, fromLevel: viewLevel, scope,
    });
    return true;
  }, [viewLevel, pushL1Scope, drillDown, setTableFilter, emitHeimdall]);

  // Touch double-tap detection — React Flow's `onNodeDoubleClick` fires from
  // the browser's `dblclick`, which iOS / Android Chrome sometimes swallow
  // (the OS treats two quick taps as a zoom gesture). We synthesise a
  // double-tap from two `onNodeClick` calls on the same node within 350 ms.
  const lastTap = useRef<{ id: string; t: number } | null>(null);

  const onNodeClick = useCallback((_: React.MouseEvent, node: LoomNode) => {
    const now  = Date.now();
    const prev = lastTap.current;
    lastTap.current = { id: node.id, t: now };
    if (prev && prev.id === node.id && now - prev.t < 350) {
      lastTap.current = null;
      if (drillFromNode(node)) return;
    }

    selectNode(node.id, node.data);
    emitHeimdall('LOOM_NODE_SELECTED', 'INFO', {
      nodeId:    node.id,
      nodeType:  node.data.nodeType ?? '',
      nodeLabel: node.data.label   ?? '',
      viewLevel,
    });

    if (viewLevel === 'L1') {
      if (node.type === 'databaseNode') {
        setL1HierarchyDb(node.id);
        const scope = `db-${node.data.label}`;
        setTableFilter(null);
        drillDown(scope, node.data.label, 'DaliDatabase');
        emitHeimdall('LOOM_DRILL_DOWN', 'INFO', {
          nodeId: node.id, nodeType: 'DaliDatabase', nodeLabel: node.data.label, fromLevel: 'L1', scope,
        });
      } else if (node.type === 'l1SchemaNode' && node.parentId) {
        setL1HierarchyDb(node.parentId);
        setL1HierarchySchema(node.id);
      }
      return;
    }

    const nt = node.data.nodeType;

    if (nt === 'DaliDatabase') {
      setTableFilter(null);
      const scope = `db-${node.data.label}`;
      jumpTo('L2', scope, node.data.label, 'DaliDatabase');
      emitHeimdall('LOOM_DRILL_DOWN', 'INFO', {
        nodeId: node.id, nodeType: 'DaliDatabase', nodeLabel: node.data.label, fromLevel: viewLevel, scope,
      });
      return;
    }

    if (
      nt === 'DaliColumn' || nt === 'DaliOutputColumn' ||
      nt === 'DaliAtom'   || nt === 'DaliAffectedColumn'
    ) {
      const f = useLoomStore.getState().filter;
      setFieldFilter(f.fieldFilter === node.data.label ? null : node.data.label);
    }
  }, [selectNode, viewLevel, setL1HierarchyDb, setL1HierarchySchema, setFieldFilter, drillDown, jumpTo, setTableFilter, emitHeimdall]);

  const onNodeDoubleClick = useCallback((_: React.MouseEvent, node: LoomNode) => {
    drillFromNode(node);
  }, [drillFromNode]);

  const onNodeContextMenu = useCallback((e: React.MouseEvent, node: LoomNode) => {
    e.preventDefault();
    setContextMenu({ nodeId: node.id, data: node.data, x: e.clientX, y: e.clientY });
  }, [setContextMenu]);

  return { onNodeClick, onNodeDoubleClick, onNodeContextMenu };
}
