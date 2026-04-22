import { useEffect, useState, useCallback } from 'react';
import { HEIMDALL_API } from '../api';

export interface ClusterMetrics {
  cacheHitPct:     number | null;
  queriesPerMin:   number | null;
  walBytesWritten: number | null;
  openFiles:       number | null;
}

export interface ClusterHealth {
  id:        'frigg' | 'ygg';
  port:      number;
  health:    'up' | 'down';
  latencyMs: number | null;
  version:   string | null;
  dbs:       string[];
  metrics:   ClusterMetrics | null;
  error?:    string;
}

interface Response { clusters: ClusterHealth[] }

const POLL_MS = 15_000;

/** Polls chur `GET /heimdall/databases` — live ArcadeDB inventory + profiler metrics. */
export function useDatabases(): {
  clusters: ClusterHealth[] | null;
  error:    string | null;
  refresh:  () => void;
} {
  const [clusters, setClusters] = useState<ClusterHealth[] | null>(null);
  const [error,    setError]    = useState<string | null>(null);

  const refresh = useCallback(() => {
    fetch(`${HEIMDALL_API}/databases`, { credentials: 'include' })
      .then(r => {
        if (!r.ok) throw new Error(`HTTP ${r.status}`);
        return r.json() as Promise<Response>;
      })
      .then(d => { setClusters(d.clusters); setError(null); })
      .catch(e => setError(e instanceof Error ? e.message : 'error'));
  }, []);

  useEffect(() => {
    refresh();
    const id = setInterval(refresh, POLL_MS);
    return () => clearInterval(id);
  }, [refresh]);

  return { clusters, error, refresh };
}
