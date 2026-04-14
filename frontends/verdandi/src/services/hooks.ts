import { useQuery } from '@tanstack/react-query';
import {
  fetchOverview,
  fetchExplore,
  fetchLineage,
  fetchUpstream,
  fetchDownstream,
  fetchStmtColumns,
  fetchSearch,
  fetchKnotSessions,
  fetchKnotReport,
  fetchKnotSnippet,
  fetchStatementExtras,
  fetchExpandDeep,
  isUnauthorized,
} from './lineage';
import { useAuthStore } from '../stores/authStore';

// ── Query keys ────────────────────────────────────────────────────────────────

export const qk = {
  overview:     ()               => ['overview']               as const,
  explore:      (scope: string)  => ['explore', scope]         as const,
  lineage:      (nodeId: string) => ['lineage', nodeId]        as const,
  upstream:     (nodeId: string) => ['upstream', nodeId]       as const,
  downstream:   (nodeId: string) => ['downstream', nodeId]     as const,
  expandDeep:   (nodeId: string, depth: number) => ['expandDeep', nodeId, depth] as const,
  search:       (q: string)      => ['search', q]              as const,
  knotSessions: ()               => ['knotSessions']           as const,
  knotReport:   (sid: string)    => ['knotReport', sid]        as const,
  knotSnippet:  (geoid: string)  => ['knotSnippet', geoid]     as const,
  stmtExtras:   (geoid: string)  => ['statementExtras', geoid] as const,
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
    staleTime: 30_000,
    retry: 2,
    throwOnError: false,
    meta: { onError },
  });
}

// ── L2: Explore ───────────────────────────────────────────────────────────────

export function useExplore(scope: string | null, includeExternal = false) {
  const onError = useOnUnauthorized();
  return useQuery({
    // includeExternal goes into the query key so enabling the toggle triggers
    // a refetch with a separate cache entry — otherwise React Query would
    // return the non-external result from cache.
    queryKey: [...qk.explore(scope ?? ''), includeExternal] as const,
    queryFn:  () => fetchExplore(scope!, includeExternal),
    enabled:  !!scope,
    staleTime: 30_000,
    retry: 2,
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
    staleTime: 30_000,
    retry: 2,
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
    staleTime: 30_000,
    retry: 2,
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
    staleTime: 30_000,
    retry: 2,
    throwOnError: false,
    meta: { onError },
  });
}

export function useExpandDeep(nodeId: string | null, depth: number) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.expandDeep(nodeId ?? '', depth),
    queryFn:  () => fetchExpandDeep(nodeId!, depth),
    enabled:  !!nodeId,
    staleTime: 30_000,
    retry: 2,
    throwOnError: false,
    meta: { onError },
  });
}

// ── L2+: Statement column enrichment ─────────────────────────────────────────

export function useStmtColumns(ids: string[]) {
  const onError = useOnUnauthorized();
  // Stable string key: sorted IDs joined so cache is invalidated whenever the set changes
  // (array reference changes each render, React Query deep-equals but a string is more robust)
  const key = [...ids].sort().join(',');
  return useQuery({
    queryKey: ['stmtColumns', key],
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

// ── KNOT: Session list + full report ──────────────────────────────────────────

export function useKnotSessions() {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.knotSessions(),
    queryFn:  fetchKnotSessions,
    staleTime: 30_000,
    throwOnError: false,
    meta: { onError },
  });
}

export function useKnotReport(sessionId: string | null) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.knotReport(sessionId ?? ''),
    queryFn:  () => fetchKnotReport(sessionId!),
    enabled:  !!sessionId,
    staleTime: 60_000,
    throwOnError: false,
    meta: { onError },
  });
}

/** Lazy snippet fetch — enabled only when the SQL section is open and snippet is missing from the map. */
export function useKnotSnippet(stmtGeoid: string | null | undefined, enabled: boolean) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.knotSnippet(stmtGeoid ?? ''),
    queryFn:  () => fetchKnotSnippet(stmtGeoid!),
    enabled:  enabled && !!stmtGeoid,
    staleTime: 300_000,  // snippets don't change during a session — cache 5 min
    throwOnError: false,
    meta: { onError },
  });
}

/**
 * Lazy fetch of subquery tree + atom stats for one DaliStatement.
 * Fires only while the LOOM Inspector "Дополнительно" tab is mounted — the
 * parent component mounts ExtraPanel conditionally on tab === 'extra', so
 * this hook naturally stays inactive for stmts the user never inspects.
 * 5-min React Query staleTime makes re-opening the tab instant for the same
 * statement within a session.
 */
export function useStatementExtras(stmtGeoid: string | null | undefined, enabled: boolean) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.stmtExtras(stmtGeoid ?? ''),
    queryFn:  () => fetchStatementExtras(stmtGeoid!),
    enabled:  enabled && !!stmtGeoid,
    staleTime: 300_000,
    throwOnError: false,
    meta: { onError },
  });
}
