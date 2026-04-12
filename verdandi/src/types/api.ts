import type { DaliNodeType, DaliEdgeType, ViewLevel, DaliNodeData } from './domain';

export interface ApiNode {
  id: string;
  type: DaliNodeType;
  data: DaliNodeData;
}

export interface ApiEdge {
  id: string;
  source: string;
  target: string;
  type: DaliEdgeType;
  data?: Record<string, unknown>;
}

export interface ApiGraphResponse {
  nodes: ApiNode[];
  edges: ApiEdge[];
  metadata: {
    level: ViewLevel;
    scope: string;
    totalNodes: number;
    totalEdges: number;
  };
}
