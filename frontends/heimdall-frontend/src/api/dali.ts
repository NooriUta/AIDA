const BASE = '/dali'; // proxied by Vite dev server → http://localhost:9090

export type DaliDialect = 'plsql' | 'postgresql' | 'clickhouse';
export type SessionStatus = 'QUEUED' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'CANCELLED';

export interface ParseSessionInput {
  dialect: DaliDialect;
  source: string;
  preview: boolean;
  /** If true, all Dali YGG data is truncated before this session writes. Default: true. */
  clearBeforeWrite: boolean;
}

export interface FileResult {
  path: string;
  success: boolean;
  atomCount: number;
  vertexCount: number;
  edgeCount: number;
  resolutionRate: number;
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
  dialect: string;
  source: string;
  startedAt: string;
  updatedAt: string;
  atomCount: number | null;
  vertexCount: number | null;
  edgeCount: number | null;
  resolutionRate: number | null;
  durationMs: number | null;
  warnings: string[];
  errors: string[];
  fileResults: FileResult[];
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
