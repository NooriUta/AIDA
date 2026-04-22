const BASE = '/chur/api/admin';

export type TenantStatus = 'ACTIVE' | 'SUSPENDED' | 'ARCHIVED' | 'PROVISIONING' | 'PURGED';

export interface TenantSummary {
  tenantAlias: string;
  status: TenantStatus;
  configVersion: number;
}

export interface DaliTenantConfig extends TenantSummary {
  keycloakOrgId?: string;
  yggLineageDbName?: string;
  yggSourceArchiveDbName?: string;
  friggDaliDbName?: string;
  yggInstanceUrl?: string;
  harvestCron?: string;
  llmMode?: string;
  dataRetentionDays?: number;
  maxParseSessions?: number;
  maxAtoms?: number;
  maxSources?: number;
  maxConcurrentJobs?: number;
  archiveS3Key?: string;
  archiveRetentionUntil?: number;
  featureFlags?: Record<string, boolean>;  // MTN-12
  createdAt?: number;
  updatedAt?: number;
}

export interface TenantMember {
  id: string;
  username: string;
  email: string;
  role: string;
  enabled: boolean;
}

async function adminFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${BASE}${path}`, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      Origin: window.location.origin,
      ...(init?.headers ?? {}),
    },
    ...init,
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({})) as { error?: string };
    throw new Error(body.error ?? `HTTP ${res.status}`);
  }
  return res.json() as Promise<T>;
}

export function listTenants(signal?: AbortSignal): Promise<TenantSummary[]> {
  return adminFetch<TenantSummary[]>('/tenants', { signal });
}

export function provisionTenant(alias: string): Promise<{ ok: boolean; tenantAlias: string }> {
  return adminFetch('/tenants', { method: 'POST', body: JSON.stringify({ alias }) });
}

export function forceCleanupTenant(alias: string): Promise<{ ok: boolean }> {
  return adminFetch(`/tenants/${encodeURIComponent(alias)}/force-cleanup`, { method: 'POST' });
}

export function getTenant(alias: string, signal?: AbortSignal): Promise<DaliTenantConfig> {
  return adminFetch<DaliTenantConfig>(`/tenants/${encodeURIComponent(alias)}`, { signal });
}

export function suspendTenant(alias: string): Promise<{ ok: boolean; status: string }> {
  return adminFetch(`/tenants/${encodeURIComponent(alias)}`, { method: 'DELETE' });
}

export function unsuspendTenant(alias: string): Promise<{ ok: boolean }> {
  return adminFetch(`/tenants/${encodeURIComponent(alias)}/unsuspend`, { method: 'POST' });
}

export function archiveTenant(alias: string): Promise<{ ok: boolean }> {
  return adminFetch(`/tenants/${encodeURIComponent(alias)}/archive-now`, { method: 'POST' });
}

export function restoreTenant(alias: string): Promise<{ ok: boolean }> {
  return adminFetch(`/tenants/${encodeURIComponent(alias)}/restore`, { method: 'POST' });
}

export function extendRetention(alias: string, retainUntil: number): Promise<{ ok: boolean }> {
  return adminFetch(`/tenants/${encodeURIComponent(alias)}/retention`, {
    method: 'PUT',
    body: JSON.stringify({ retainUntil }),
  });
}

export function listMembers(alias: string, signal?: AbortSignal): Promise<TenantMember[]> {
  return adminFetch<TenantMember[]>(`/tenants/${encodeURIComponent(alias)}/members`, { signal });
}

export function addMember(alias: string, email: string, name: string, role: string): Promise<{ ok: boolean }> {
  return adminFetch(`/tenants/${encodeURIComponent(alias)}/members`, {
    method: 'POST',
    body: JSON.stringify({ email, name, role }),
  });
}

export function removeMember(alias: string, userId: string): Promise<{ ok: boolean }> {
  return adminFetch(`/tenants/${encodeURIComponent(alias)}/members/${userId}`, {
    method: 'DELETE',
  });
}
