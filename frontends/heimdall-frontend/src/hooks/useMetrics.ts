import { useEffect, useRef, useState } from 'react';
import type { MetricsSnapshot } from 'aida-shared';
import { HEIMDALL_API } from '../api';

const POLL_INTERVAL_MS = 2000;

export function useMetrics() {
  const [metrics, setMetrics] = useState<MetricsSnapshot | null>(null);
  const [error, setError]     = useState<string | null>(null);
  const abortRef              = useRef<AbortController | null>(null);
  const timerRef              = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    let cancelled = false;

    async function fetchMetrics() {
      const ctrl = new AbortController();
      abortRef.current = ctrl;
      try {
        const res = await fetch(`${HEIMDALL_API}/metrics/snapshot`, { signal: ctrl.signal });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = (await res.json()) as MetricsSnapshot;
        if (!cancelled) {
          setMetrics(data);
          setError(null);
        }
      } catch (err) {
        if (!cancelled && !(err instanceof DOMException && err.name === 'AbortError')) {
          setError(err instanceof Error ? err.message : 'Unknown error');
        }
      }
    }

    fetchMetrics();
    timerRef.current = setInterval(fetchMetrics, POLL_INTERVAL_MS);

    return () => {
      cancelled = true;
      if (timerRef.current) clearInterval(timerRef.current);
      abortRef.current?.abort();
    };
  }, []);

  return { metrics, error };
}
