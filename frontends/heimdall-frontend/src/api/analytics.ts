/**
 * UA-04: HEIMDALL UX Analytics API client.
 * Fetches 24-hour sliding-window UX metrics from HEIMDALL via Chur proxy.
 */

export interface HotNode {
  nodeId:   string;
  nodeType: string;
  clicks:   number;
}

export interface LevelDistribution {
  info:  number;
  warn:  number;
  error: number;
}

export interface SlowRender {
  timestamp:   number;
  nodesCount:  number;
  renderMs:    number;
  sessionId:   string;
}

export interface EventTypeCount {
  eventType: string;
  count:     number;
}

export interface UxSummary {
  hotNodes:            HotNode[];
  levelDistribution:   LevelDistribution;
  slowRenders:         SlowRender[];
  eventTypeCounts:     EventTypeCount[];
  activeSessionCount:  number;
  totalEventsInWindow: number;
  windowMs:            number;
}

export async function fetchUxSummary(): Promise<UxSummary> {
  const res = await fetch('/heimdall/analytics/ux', { credentials: 'include' });
  if (!res.ok) {
    throw new Error(`UX analytics fetch failed: ${res.status}`);
  }
  return res.json() as Promise<UxSummary>;
}
