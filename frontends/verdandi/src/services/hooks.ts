import { useQuery } from '@tanstack/react-query';
import {
  fetchOverview,
  fetchExplore,
  fetchRoutineAggregate,
  fetchRoutineDetail,
  fetchStatementTree,
  fetchLineage,
  fetchUpstream,
  fetchDownstream,
  fetchStmtColumns,
  fetchSearch,
  fetchKnotSessions,
  fetchKnotReport,
  fetchKnotSnippet,
  fetchKnotScript,
  fetchKnotSourceFile,
  fetchKnotTableRoutines,
  fetchKnotColumnStatements,
  fetchStatementExtras,
  fetchExpandDeep,
  isUnauthorized,
} from './lineage';
import { useAuthStore } from '../stores/authStore';

// ── Query keys ────────────────────────────────────────────────────────────────

export const qk = {
  overview:      ()               => ['overview']               as const,
  explore:       (scope: string)  => ['explore', scope]         as const,
  lineage:       (nodeId: string) => ['lineage', nodeId]        as const,
  upstream:      (nodeId: string) => ['upstream', nodeId]       as const,
  downstream:    (nodeId: string) => ['downstream', nodeId]     as const,
  expandDeep:    (nodeId: string, depth: number) => ['expandDeep', nodeId, depth] as const,
  search:        (q: string)      => ['search', q]              as const,
  knotSessions:  ()               => ['knotSessions']           as const,
  knotReport:    (sid: string, sf?: string | null) => ['knotReport', sid, sf ?? ''] as const,
  knotSnippet:   (geoid: string)  => ['knotSnippet', geoid]     as const,
  knotScript:      (sid: string)    => ['knotScript', sid]        as const,
  knotSourceFile:  (sid: string)    => ['knotSourceFile', sid]    as const,
  stmtExtras:      (geoid: string)  => ['statementExtras', geoid] as const,
  routineDetail:    (nodeId: string) => ['routineDetail', nodeId]       as const,
  tableRoutines:    (rid: string)    => ['tableRoutines', rid]           as const,
  columnStatements: (geoid: string)  => ['columnStatements', geoid]      as const,
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

/**
 * Phase S2.3 — new L2 view. Fetches the routines+tables aggregated result
 * from exploreRoutineAggregate. Activated when viewLevel === 'L2' and
 * filter.routineAggregate is true (the default).
 */
export function useRoutineAggregate(scope: string | null) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: ['routineAggregate', scope ?? ''] as const,
    queryFn:  () => fetchRoutineAggregate(scope!),
    enabled:  !!scope,
    staleTime: 30_000,
    retry: 2,
    throwOnError: false,
    meta: { onError },
  });
}

// ── L4: Statement-tree drill ──────────────────────────────────────────────────

/**
 * Phase S2.5 — fetches the subquery tree for a single DaliStatement.
 * Activated when viewLevel === 'L4' and currentScope is a statement @rid.
 * Returns root stmt + all child sub-statements + their READS_FROM tables
 * + HAS_OUTPUT_COL + DATA_FLOW edges.
 */
export function useStatementTree(stmtId: string | null) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: ['statementTree', stmtId ?? ''] as const,
    queryFn:  () => fetchStatementTree(stmtId!),
    enabled:  !!stmtId,
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

export function useKnotReport(sessionId: string | null, sourceFile?: string | null) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.knotReport(sessionId ?? '', sourceFile),
    queryFn:  () => fetchKnotReport(sessionId!, sourceFile),
    enabled:  !!sessionId,
    staleTime: 60_000,
    throwOnError: false,
    meta: { onError },
  });
}

/**
 * Inspector detail for a DaliRoutine node.
 * Returns parameters (DaliParameter), variables (DaliVariable), root statements
 * (DaliStatement), and CALLS edges in both directions.
 * Fires whenever a routine node is selected in the inspector panel.
 */
export function useRoutineDetail(nodeId: string | null) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.routineDetail(nodeId ?? ''),
    queryFn:  () => fetchRoutineDetail(nodeId!),
    enabled:  !!nodeId,
    staleTime: 60_000,
    throwOnError: false,
    meta: { onError },
  });
}

/** Lazy full-source fetch from DaliSnippetScript — kept for backward compat. */
export function useKnotScript(sessionId: string | null | undefined, enabled: boolean) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.knotScript(sessionId ?? ''),
    queryFn:  () => fetchKnotScript(sessionId!),
    enabled:  enabled && !!sessionId,
    staleTime: 600_000,
    throwOnError: false,
    meta: { onError },
  });
}

/** Lazy full source file from hound_src_{tenant} archive — looked up by sessionId (two-step via DaliSession.file_path). */
export function useKnotSourceFile(sessionId: string | null | undefined, enabled: boolean) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.knotSourceFile(sessionId ?? ''),
    queryFn:  () => fetchKnotSourceFile(sessionId!),
    enabled:  enabled && !!sessionId,
    staleTime: 600_000,
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

/** Lazy statements that reference a given column (by columnGeoid). Enabled on expand. */
export function useKnotColumnStatements(columnGeoid: string | null | undefined, enabled: boolean) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.columnStatements(columnGeoid ?? ''),
    queryFn:  () => fetchKnotColumnStatements(columnGeoid!),
    enabled:  enabled && !!columnGeoid,
    staleTime: 120_000,
    throwOnError: false,
    meta: { onError },
  });
}

/** Lazy routines + statements that use a given table. Enabled when analytics section is opened. */
export function useKnotTableRoutines(tableRid: string | null | undefined, enabled: boolean) {
  const onError = useOnUnauthorized();
  return useQuery({
    queryKey: qk.tableRoutines(tableRid ?? ''),
    queryFn:  () => fetchKnotTableRoutines(tableRid!),
    enabled:  enabled && !!tableRid,
    staleTime: 120_000,
    throwOnError: false,
    meta: { onError },
  });
}
