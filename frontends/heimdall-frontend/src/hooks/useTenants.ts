import { useState, useEffect, useRef } from 'react';
import { listTenants, type TenantSummary } from '../api/admin';

/**
 * Two-phase tenant loading:
 *
 * Phase 1  — fast: `withStats=false` → returns immediately from FRIGG
 *            (or the `[default]` fallback when FRIGG is down).
 *            UI shows the tenant list right away.
 *
 * Phase 2  — background: `withStats=true` enriches with member/atom/source
 *            counts. In DEV without YGG/FRIGG/KC this can take up to ~10 s,
 *            so it runs silently after the list is already visible.
 *            Errors in phase 2 are swallowed (stats are decorative).
 */
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

    // Phase 1: instant list (no external service calls)
    listTenants(ctrl.signal, false)
      .then(basic => {
        if (ctrl.signal.aborted) return;
        setTenants(basic);
        setLoading(false);

        // Phase 2: enrich with stats in background — never blocks the UI
        listTenants(ctrl.signal, true)
          .then(enriched => {
            if (!ctrl.signal.aborted) setTenants(enriched);
          })
          .catch(() => { /* stats are optional — YGG/FRIGG may not be running in DEV */ });
      })
      .catch(err => {
        if (ctrl.signal.aborted) return;
        setError(err instanceof Error ? err.message : 'Failed to load tenants');
        setLoading(false);
      });
  };

  useEffect(() => {
    refresh();
    return () => abortRef.current?.abort();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  return { tenants, loading, error, refresh };
}
