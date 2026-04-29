/**
 * UA-06: Slow renders widget — lists LOOM_VIEW_SLOW events from last 24 h.
 * Empty state when no renders exceeded the threshold.
 * "Navigate" button calls navigateTo with the sessionId (opens KNOT in Verdandi).
 */
import React from 'react';
import type { SlowRender } from '../../api/analytics';

interface Props {
  renders:    SlowRender[];
  navigateTo: (target: string, opts?: Record<string, unknown>) => void;
}

function formatTs(ts: number): string {
  return new Date(ts).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit', second: '2-digit' });
}

export function SlowRendersList({ renders, navigateTo }: Props) {
  if (renders.length === 0) {
    return (
      <div className="analytics-empty" data-testid="slow-renders-empty">
        No slow renders detected in the last 24 h.
      </div>
    );
  }

  return (
    <div className="slow-renders" data-testid="slow-renders-list">
      <table className="seer-table slow-renders-table">
        <thead>
          <tr>
            <th>Time</th>
            <th>Nodes</th>
            <th>Render ms</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {renders.map((r, i) => (
            <tr key={`${r.timestamp}-${i}`} className={r.renderMs > 2000 ? 'row-warn' : ''}>
              <td className="mono">{formatTs(r.timestamp)}</td>
              <td>{r.nodesCount.toLocaleString()}</td>
              <td className={r.renderMs > 2000 ? 'text-warn' : ''}>{r.renderMs} ms</td>
              <td>
                {r.sessionId && (
                  <button
                    className="btn btn-xs btn-ghost"
                    onClick={() => navigateTo('verdandi', { sessionId: r.sessionId })}
                    title="Open session in LOOM"
                  >
                    Open LOOM
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
