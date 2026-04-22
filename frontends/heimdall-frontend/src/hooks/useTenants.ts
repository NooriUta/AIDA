import { useState, useEffect, useRef } from 'react';
import { listTenants, type TenantSummary } from '../api/admin';

export function useTenants() {
  const [tenants, setTenants]   = useState<TenantSummary[]>([]);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState<string | null>(null);
  const abortRef                = useRef<AbortController | null>(null);

  const refresh = () => {
    abortRef.current?.abort();
    const ctrl = new AbortController();
    abortRef.current = ctrl;
    setLoading(true);
    setError(null);
    listTenants(ctrl.signal)
      .then(setTenants)
      .catch(err => {
        if (!(err instanceof DOMException && err.name === 'AbortError')) {
          setError(err instanceof Error ? err.message : 'Failed to load tenants');
        }
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    refresh();
    return () => abortRef.current?.abort();
  }, []);

  return { tenants, loading, error, refresh };
}
