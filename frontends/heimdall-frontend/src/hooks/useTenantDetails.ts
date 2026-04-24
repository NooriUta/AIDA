import { useState, useEffect, useRef } from 'react';
import { getTenant, type DaliTenantConfig } from '../api/admin';

export function useTenantDetails(alias: string | undefined) {
  const [tenant, setTenant]   = useState<DaliTenantConfig | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError]     = useState<string | null>(null);
  const abortRef              = useRef<AbortController | null>(null);

  const refresh = () => {
    if (!alias) return;
    abortRef.current?.abort();
    const ctrl = new AbortController();
    abortRef.current = ctrl;
    setLoading(true);
    setError(null);
    getTenant(alias, ctrl.signal)
      .then(setTenant)
      .catch(err => {
        if (!(err instanceof DOMException && err.name === 'AbortError')) {
          setError(err instanceof Error ? err.message : 'Failed to load tenant');
        }
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    refresh();
    return () => abortRef.current?.abort();
  }, [alias]);

  return { tenant, loading, error, refresh };
}
