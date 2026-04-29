/**
 * UA-06: Hot-nodes widget — shows top-N nodes by LOOM click count.
 * Bar chart: node label on Y-axis, click count on X-axis.
 */
import React from 'react';
import type { HotNode } from '../../api/analytics';

interface Props {
  nodes: HotNode[];
}

export function HotNodesChart({ nodes }: Props) {
  if (nodes.length === 0) {
    return (
      <div className="analytics-empty" data-testid="hot-nodes-empty">
        No node clicks recorded yet.
      </div>
    );
  }

  const max = Math.max(...nodes.map(n => n.clicks), 1);

  return (
    <div className="hot-nodes-chart" data-testid="hot-nodes-chart">
      {nodes.map((node) => (
        <div key={node.nodeId} className="hot-node-row" title={`${node.nodeType}: ${node.nodeId}`}>
          <div className="hot-node-label">{node.nodeType}</div>
          <div className="hot-node-bar-wrap">
            <div
              className="hot-node-bar"
              style={{ width: `${(node.clicks / max) * 100}%` }}
            />
            <span className="hot-node-count">{node.clicks}</span>
          </div>
        </div>
      ))}
    </div>
  );
}
