const BASE = '/dali'; // proxied by Vite dev server → http://localhost:9090

export type DaliDialect = 'plsql' | 'postgresql' | 'clickhouse';
export type SessionStatus = 'QUEUED' | 'RUNNING' | 'CANCELLING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';

export interface ParseSessionInput {
  dialect: DaliDialect;
  source: string;
  preview: boolean;
  /** If true, all Dali YGG data is truncated before this session writes. Default: true. */
  clearBeforeWrite: boolean;
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
): Promise<DaliSession> {
  const form = new FormData();
  form.append('file', file);
  form.append('dialect', dialect);
  form.append('preview', String(preview));
  form.append('clearBeforeWrite', String(clearBeforeWrite));
  const res = await fetch(`${BASE}/api/sessions/upload`, { method: 'POST', body: form });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error((body as { error?: string }).error ?? `HTTP ${res.status}`);
  }
  return res.json();
}

export async function postSession(input: ParseSessionInput): Promise<DaliSession> {
  const res = await fetch(`${BASE}/api/sessions`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(input),
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error((body as { error?: string }).error ?? `HTTP ${res.status}`);
  }
  return res.json();
}

export async function getSession(id: string, signal?: AbortSignal): Promise<DaliSession> {
  const res = await fetch(`${BASE}/api/sessions/${id}`, { signal });
  if (!res.ok) throw new Error(`Session ${id} not found`);
  return res.json();
}

export async function getSessions(limit = 50): Promise<DaliSession[]> {
  const res = await fetch(`${BASE}/api/sessions?limit=${limit}`);
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

export async function getDaliHealth(): Promise<DaliHealth> {
  const res = await fetch(`${BASE}/api/sessions/health`);
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}

/** Returns sessions stored in FRIGG (the authoritative archive), bypassing in-memory cache. */
export async function getSessionsArchive(limit = 200): Promise<DaliSession[]> {
  const res = await fetch(`${BASE}/api/sessions/archive?limit=${limit}`);
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

export async function cancelSession(id: string): Promise<void> {
  const res = await fetch(`${BASE}/api/sessions/${id}/cancel`, { method: 'POST' });
  if (!res.ok && res.status !== 409) throw new Error(`HTTP ${res.status}`);
}

export async function getYggStats(): Promise<YggStats> {
  const res = await fetch(`${BASE}/api/stats`);
  if (!res.ok) throw new Error(`HTTP ${res.status}`);
  return res.json();
}
