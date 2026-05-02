// MIMIR Copilot HTTP client — talks to Chur (BFF) which forwards to MIMIR :9094.
// All endpoints are session-cookie authenticated via Chur; tenant alias is
// injected by Chur from the active tenant on the session.

const BASE = import.meta.env.VITE_CHUR_URL ?? `${location.origin}`;

export interface AskRequest {
  question:      string;
  sessionId:     string;
  dbName?:       string | null;
  model?:        string | null;
  agents?:       string[] | null;
  maxToolCalls?: number;
}

export interface QuotaInfo {
  reason:        string;
  currentTokens: number;
  limitTokens:   number;
  currentCost:   number;
  limitCost:     number;
  resetAt:       string | null;
}

export interface ApprovalInfo {
  approvalId:        string;
  reason:            string;
  requestedAt:       string;
  expiresAt:         string;
  estimatedCostUsd:  number;
}

export interface MimirAnswer {
  answer:           string;
  toolCallsUsed:    string[];
  highlightNodeIds: string[];
  confidence:       number;
  durationMs:       number;
  provider?:        string | null;
  model?:           string | null;
  promptTokens?:    number;
  completionTokens?: number;
  quota?:           QuotaInfo | null;
  awaitingApproval?: ApprovalInfo | null;
}

export interface DecisionRequest {
  approvalId: string;
  approve:    boolean;
  comment?:   string;
}

const tenantHeaders = (): Record<string, string> => {
  const alias = localStorage.getItem('seer-active-tenant');
  return alias
    ? { 'X-Seer-Override-Tenant': alias, 'X-Seer-Tenant-Alias': alias }
    : {};
};

export async function askMimir(req: AskRequest): Promise<MimirAnswer> {
  const r = await fetch(`${BASE}/mimir/ask`, {
    method:      'POST',
    credentials: 'include',
    headers:     { 'Content-Type': 'application/json', ...tenantHeaders() },
    body:        JSON.stringify(req),
  });
  if (!r.ok) {
    const text = await r.text();
    throw new Error(`MIMIR ask ${r.status}: ${text}`);
  }
  return r.json();
}

export async function getMimirHealth(): Promise<{ status: string }> {
  const r = await fetch(`${BASE}/mimir/health`, { credentials: 'include' });
  return r.json();
}

export async function deleteMimirSession(sessionId: string): Promise<void> {
  await fetch(`${BASE}/mimir/sessions/${encodeURIComponent(sessionId)}`, {
    method:      'DELETE',
    credentials: 'include',
    headers:     tenantHeaders(),
  });
}

/** Operator approves or rejects a HiL-paused session. */
export async function decideMimirSession(
  sessionId: string,
  body:      DecisionRequest,
): Promise<MimirAnswer | { sessionId: string; status: string; decidedBy: string }> {
  const r = await fetch(
    `${BASE}/mimir/sessions/${encodeURIComponent(sessionId)}/decision`,
    {
      method:      'POST',
      credentials: 'include',
      headers:     { 'Content-Type': 'application/json', ...tenantHeaders() },
      body:        JSON.stringify(body),
    },
  );
  return r.json();
}
