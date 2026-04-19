import type { LoomNode } from '../types/graph';

export function getMinimapNodeColor(node: LoomNode): string {
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
}
