import { useQuery } from '@tanstack/react-query';
import {
  fetchOverview,
  fetchExplore,
  fetchLineage,
  fetchUpstream,
  fetchDownstream,
  fetchStmtColumns,
  fetchSearch,
  isUnauthorized,
} from './lineage';
import { useAuthStore } from '../stores/authStore';

// ── Query keys ────────────────────────────────────────────────────────────────

export const qk = {
  overview:   ()           => ['overview']          as const,
  explore:    (scope: string) => ['explore', scope] as const,
  lineage:    (nodeId: string) => ['lineage', nodeId] as const,
  upstream:   (nodeId: string) => ['upstream', nodeId] as const,
  downstream: (nodeId: string) => ['downstream', nodeId] as const,
  search:     (q: string)  => ['search', q]         as const,
};

// ── 401 handler — auto-logout when session expires ────────────────────────────

function useOnUnauthorized() {
  const logout = useAuthStore((s) => s.logout);
  return (err: unknown) => {
    if (isUnauthorized(err)) logout();
  };
}

// ── L1: Overview ──────────────────────────────────────────────────────────────

export function useOverview() {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.overview(),
    queryFn:  fetchOverview,
    throwOnError: false,
    meta: { onError },
  });
}

// ── L2: Explore ───────────────────────────────────────────────────────────────

export function useExplore(scope: string | null) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.explore(scope ?? ''),
    queryFn:  () => fetchExplore(scope!),
    enabled:  !!scope,
    throwOnError: false,
    meta: { onError },
  });
}

// ── L3: Lineage ───────────────────────────────────────────────────────────────

export function useLineage(nodeId: string | null) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.lineage(nodeId ?? ''),
    queryFn:  () => fetchLineage(nodeId!),
    enabled:  !!nodeId,
    throwOnError: false,
    meta: { onError },
  });
}

export function useUpstream(nodeId: string | null) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.upstream(nodeId ?? ''),
    queryFn:  () => fetchUpstream(nodeId!),
    enabled:  !!nodeId,
    throwOnError: false,
    meta: { onError },
  });
}

export function useDownstream(nodeId: string | null) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.downstream(nodeId ?? ''),
    queryFn:  () => fetchDownstream(nodeId!),
    enabled:  !!nodeId,
    throwOnError: false,
    meta: { onError },
  });
}

// ── L2+: Statement column enrichment ─────────────────────────────────────────

export function useStmtColumns(ids: string[]) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: ['stmtColumns', ids],
    queryFn:  () => fetchStmtColumns(ids),
    enabled:  ids.length > 0,
    staleTime: 60_000,
    throwOnError: false,
    meta: { onError },
  });
}

// ── Search ────────────────────────────────────────────────────────────────────

export function useSearch(query: string, limit = 20) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.search(query),
    queryFn:  () => fetchSearch(query, limit),
    enabled:  query.trim().length >= 2,   // don't fire on empty/single char
    staleTime: 60_000,                    // search results stay fresh 60s
    throwOnError: false,
    meta: { onError },
  });
}
