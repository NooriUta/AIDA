const BASE = '/dali'; // proxied by Vite dev server → http://localhost:9090

function th(tenantAlias?: string): HeadersInit {
  return tenantAlias ? { 'X-Seer-Tenant-Alias': tenantAlias } : {};
}

export type DaliDialect = 'plsql' | 'postgresql' | 'clickhouse';
export type SessionStatus = 'QUEUED' | 'RUNNING' | 'CANCELLING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';

export interface ParseSessionInput {
  dialect: DaliDialect;
  source: string;
  preview: boolean;
  /** If true, all Dali YGG data is truncated before this session writes. Default: true. */
  clearBeforeWrite: boolean;
  /** Database name — when set, Hound creates a DaliDatabase vertex + CONTAINS_SCHEMA edges. */
  dbName?: string;
  /** Optional application name for BELONGS_TO_APP grouping. */
  appName?: string;
}

export interface VertexTypeStat {
  type: string;
  inserted: number;
  duplicate: number;
}

export interface FileResult {
  path: string;
  success: boolean;
  atomCount: number;
  vertexCount: number;
  edgeCount: number;
  droppedEdgeCount: number;
  vertexStats: VertexTypeStat[];
  resolutionRate: number;
  /** Column-reference atoms successfully resolved (present from server v2). */
  atomsResolved?: number;
  /** Column-reference atoms that failed resolution (present from server v2). */
  atomsUnresolved?: number;
  durationMs: number;
  warnings: string[];
  errors: string[];
}

export interface DaliSession {
  id: string;
  status: SessionStatus;
  progress: number;
  total: number;
  batch: boolean;
  clearBeforeWrite: boolean;
  dialect: string;
  source: string;
  startedAt: string;
  updatedAt: string;
  atomCount: number | null;
  vertexCount: number | null;
  edgeCount: number | null;
  droppedEdgeCount: number | null;
  vertexStats: VertexTypeStat[];
  resolutionRate: number | null;
  durationMs: number | null;
  warnings: string[];
  errors: string[];
  fileResults: FileResult[];
  friggPersisted: boolean;
  /** Dali instance tag — null/undefined for untagged (single-instance) sessions. */
  instanceId: string | null;
  /** Database name supplied at session creation — null/undefined for ad-hoc sessions. */
  dbName?: string | null;
  /** Tenant alias stored by the server at session creation time. "default" for single-tenant. */
  tenantAlias: string;
}

export interface DaliHealth {
  frigg: 'ok' | 'error';
  sessions: number;
}

export async function uploadAndParse(
  file: File,
  dialect: DaliDialect,
  preview: boolean,
  clearBeforeWrite: boolean,
  dbName?: string,
  appName?: string,
  tenantAlias?: string,
): Promise<DaliSession> {
  const form = new FormData();
  form.append('file', file);
  form.append('dialect', dialect);
  form.append('preview', String(preview));
  form.append('clearBeforeWrite', String(clearBeforeWrite));
  if (dbName)  form.append('dbName',  dbName);
  if (appName) form.append('appName', appName);
  const res = await fetch(`${BASE}/api/sessions/upload`, { method: 'POST', headers: th(tenantAlias), body: form });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error((body as { error?: string }).error ?? `HTTP ${res.status}`);
  }
  return res.json();
}

export async function postSession(input: ParseSessionInput, tenantAlias?: string): Promise<DaliSession> {
  const res = await fetch(`${BASE}/api/sessions`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...th(tenantAlias) },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error((body as { error?: string }).error ?? `HTTP ${res.status}`);
  }
  return res.json();
}

export async function getSession(id: string, signal?: AbortSignal, tenantAlias?: string): Promise<DaliSession> {
  const res = await fetch(`${BASE}/api/sessions/${id}`, { signal, headers: th(tenantAlias) });
  if (!res.ok) throw new Error(`Session ${id} not found`);
  return res.json();
}

export async function getSessions(limit = 50, tenantAlias?: string, allTenants = false): Promise<DaliSession[]> {
  const params = new URLSearchParams({ limit: String(limit) });
  if (allTenants) params.set('allTenants', 'true');
  const res = await fetch(`${BASE}/api/sessions?${params}`, { headers: th(tenantAlias) });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

export async function getDaliHealth(tenantAlias?: string): Promise<DaliHealth> {
  const res = await fetch(`${BASE}/api/sessions/health`, { headers: th(tenantAlias) });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

/** Returns sessions stored in FRIGG (the authoritative archive), bypassing in-memory cache. */
export async function getSessionsArchive(limit = 200, tenantAlias?: string, allTenants = false): Promise<DaliSession[]> {
  const params = new URLSearchParams({ limit: String(limit) });
  if (allTenants) params.set('allTenants', 'true');
  const res = await fetch(`${BASE}/api/sessions/archive?${params}`, { headers: th(tenantAlias) });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

export interface YggStats {
  tables:          number;
  columns:         number;
  sessions:        number;
  statements:      number;
  routines:        number;
  atomsTotal:      number;
  atomsResolved:   number;
  atomsConstant:   number;
  atomsUnresolved: number;
  atomsPending:    number;
}

export async function cancelSession(id: string, tenantAlias?: string): Promise<void> {
  const res = await fetch(`${BASE}/api/sessions/${id}/cancel`, { method: 'POST', headers: th(tenantAlias) });
  if (!res.ok && res.status !== 409) throw new Error(`HTTP ${res.status}`);
}

export async function getYggStats(tenantAlias?: string): Promise<YggStats> {
  const res = await fetch(`${BASE}/api/stats`, { headers: th(tenantAlias) });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

// ── JobRunr stats ─────────────────────────────────────────────────────────────

export interface JobRunrStats {
  enqueued:   number;
  processing: number;
  failed:     number;
  succeeded:  number;
  scheduled:  number;
}

/** GET /api/jobs/stats — live JobRunr queue counters from FRIGG jobrunr_jobs table. */
export async function getJobRunrStats(): Promise<JobRunrStats> {
  const res = await fetch(`${BASE}/api/jobs/stats`);
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

/** POST /api/jobs/reset-stuck — reset all PROCESSING jobs to FAILED (operator action). */
export async function resetStuckJobs(): Promise<{ reset: string; processing: number; failed: number }> {
  const res = await fetch(`${BASE}/api/jobs/reset-stuck`, { method: 'POST' });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

// ── JDBC Sources ─────────────────────────────────────────────────────────────

export interface SchemaFilter {
  include: string[];
  exclude: string[];
}

export interface DaliSource {
  id: string;
  name: string;
  dialect: string;
  jdbcUrl: string;
  username: string;
  lastHarvest: string | null;
  atomCount: number;
  schemaFilter: SchemaFilter;
}

export interface CreateSourceInput {
  name: string;
  dialect: string;
  jdbcUrl: string;
  username: string;
  password: string;
  schemaFilter: SchemaFilter;
}

export interface TestConnectionResult {
  ok: boolean;
  latencyMs?: number;
  error?: string;
}

export async function getSources(tenantAlias?: string): Promise<DaliSource[]> {
  const res = await fetch(`${BASE}/api/sources`, { headers: th(tenantAlias) });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

export async function createSource(input: CreateSourceInput, tenantAlias?: string): Promise<DaliSource> {
  const res = await fetch(`${BASE}/api/sources`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...th(tenantAlias) },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error((body as { error?: string }).error ?? `HTTP ${res.status}`);
  }
  return res.json();
}

export async function updateSource(id: string, input: Partial<CreateSourceInput>, tenantAlias?: string): Promise<DaliSource> {
  const res = await fetch(`${BASE}/api/sources/${id}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', ...th(tenantAlias) },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error((body as { error?: string }).error ?? `HTTP ${res.status}`);
  }
  return res.json();
}

export async function deleteSource(id: string, tenantAlias?: string): Promise<void> {
  const res = await fetch(`${BASE}/api/sources/${id}`, { method: 'DELETE', headers: th(tenantAlias) });
  if (!res.ok && res.status !== 404) throw new Error(`HTTP ${res.status}`);
}

export async function testConnection(
  jdbcUrl: string, username: string, password: string,
): Promise<TestConnectionResult> {
  const res = await fetch(`${BASE}/api/sources/test`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ jdbcUrl, username, password }),
  });
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}
