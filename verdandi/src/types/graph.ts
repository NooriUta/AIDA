import type { Node, Edge } from '@xyflow/react';
import type { DaliNodeData, DaliEdgeType } from './domain';

export type LoomNode = Node<DaliNodeData>;
export type LoomEdge = Edge<{ edgeType: DaliEdgeType; parentStmtId?: string }>;
