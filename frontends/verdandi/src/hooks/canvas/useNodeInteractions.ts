import { useCallback } from 'react';
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

  const onNodeClick = useCallback((_: React.MouseEvent, node: LoomNode) => {
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
      } else if (node.type === 'l1SchemaNode' && node.parentId) {
        setL1HierarchyDb(node.parentId);
        setL1HierarchySchema(node.id);
      }
      return;
    }

    const nt = node.data.nodeType;

    if (nt === 'DaliDatabase') {
      setTableFilter(null);
      jumpTo('L2', `db-${node.data.label}`, node.data.label, 'DaliDatabase');
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
    if (viewLevel === 'L1' && SCOPE_FILTER_TYPES.has(node.data.nodeType)) {
      pushL1Scope(node.id, node.data.label, node.data.nodeType);
      return;
    }

    if (viewLevel === 'L3' && node.data.nodeType !== 'DaliStatement') return;

    const nt = node.data.nodeType;
    if (
      nt !== 'DaliTable'    && nt !== 'DaliStatement' &&
      nt !== 'DaliSchema'   && nt !== 'DaliPackage'   &&
      nt !== 'DaliDatabase' && nt !== 'DaliRoutine'
    ) return;

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
  }, [viewLevel, pushL1Scope, drillDown, setTableFilter]);

  const onNodeContextMenu = useCallback((e: React.MouseEvent, node: LoomNode) => {
    e.preventDefault();
    setContextMenu({ nodeId: node.id, data: node.data, x: e.clientX, y: e.clientY });
  }, [setContextMenu]);

  return { onNodeClick, onNodeDoubleClick, onNodeContextMenu };
}
