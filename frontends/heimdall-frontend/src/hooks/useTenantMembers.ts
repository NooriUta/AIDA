import { useState, useEffect, useRef } from 'react';
import { listMembers, type TenantMember } from '../api/admin';

export function useTenantMembers(alias: string | undefined) {
  const [members, setMembers] = useState<TenantMember[]>([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState<string | null>(null);
  const abortRef              = useRef<AbortController | null>(null);

  const refresh = () => {
    if (!alias) return;
    abortRef.current?.abort();
    const ctrl = new AbortController();
    abortRef.current = ctrl;
    setLoading(true);
    setError(null);
    listMembers(alias, ctrl.signal)
      .then(setMembers)
      .catch(err => {
        if (!(err instanceof DOMException && err.name === 'AbortError')) {
          setError(err instanceof Error ? err.message : 'Failed to load members');
        }
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    refresh();
    return () => abortRef.current?.abort();
  }, [alias]);

  return { members, loading, error, refresh };
}
