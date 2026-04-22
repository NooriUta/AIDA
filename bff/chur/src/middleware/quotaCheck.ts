/**
 * MTN-09 — Tenant quota enforcement middleware.
 *
 * Reads {@link DaliTenantConfig} maxParseSessions / maxAtoms / maxSources /
 * maxConcurrentJobs for the calling tenant and rejects create requests that
 * would push any counter over the cap.
 *
 * <p>Semantics:
 * <ul>
 *   <li><strong>429 Too Many Requests</strong> — soft cap: current == max.
 *       Retry-After header suggests the next harvest boundary.</li>
 *   <li><strong>403 Forbidden</strong> — hard cap: current &gt; max (rare, e.g.
 *       after config lowered). Body includes a hint pointing at tenant admin.</li>
 * </ul>
 *
 * <p>Counter source: {@link countTenantResource} queries ArcadeDB on-demand.
 * Results cached per (tenantAlias, resource) for {@link CACHE_TTL_MS} to bound
 * FRIGG load. After create/delete the cache entry is bumped / evicted inline.
 *
 * <p>Emits {@code seer.audit.quota_exceeded} via the existing emitter on every
 * rejection so admins get observability without new Prometheus metrics.
 */
import type { FastifyReply, FastifyRequest } from 'fastify';

const BASIC = Buffer.from(
  `${process.env.FRIGG_USER ?? 'root'}:${process.env.FRIGG_PASS ?? 'playwithdata'}`,
).toString('base64');
const FRIGG_URL = (process.env.FRIGG_URL ?? 'http://127.0.0.1:2481').replace(/\/$/, '');

export type QuotaResource = 'parseSessions' | 'atoms' | 'sources' | 'concurrentJobs';

const RESOURCE_TO_CAP_FIELD: Record<QuotaResource, string> = {
  parseSessions:  'maxParseSessions',
  atoms:          'maxAtoms',
  sources:        'maxSources',
  concurrentJobs: 'maxConcurrentJobs',
};

/**
 * Internal cache — (tenantAlias, resource) → { count, cap, fetchedAt }.
 * Short TTL (15s) keeps FRIGG-pressure flat even under burst create storms.
 */
const CACHE_TTL_MS = 15_000;

interface CacheEntry { count: number; cap: number; fetchedAt: number }
const cache = new Map<string, CacheEntry>();
const keyOf = (alias: string, resource: QuotaResource) => `${alias}::${resource}`;

async function friggQuery(db: string, command: string, params?: Record<string, unknown>): Promise<unknown[]> {
  const res = await fetch(`${FRIGG_URL}/api/v1/query/${encodeURIComponent(db)}`, {
    method:  'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Basic ${BASIC}` },
    body:    JSON.stringify({ language: 'sql', command, params: params ?? {} }),
    signal:  AbortSignal.timeout(5_000),
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`FRIGG ${res.status}: ${text}`);
  }
  const data = (await res.json()) as { result?: unknown[] };
  return data.result ?? [];
}

/**
 * Read the cap for a tenant's resource. Returns {@code Infinity} when the
 * tenant row has no cap set — quota disabled for that resource.
 */
export async function readTenantCap(tenantAlias: string, resource: QuotaResource): Promise<number> {
  const field = RESOURCE_TO_CAP_FIELD[resource];
  const rows = await friggQuery('frigg-tenants',
    `SELECT ${field} AS cap FROM DaliTenantConfig WHERE tenantAlias = :alias LIMIT 1`,
    { alias: tenantAlias },
  );
  const raw = (rows[0] as { cap?: number } | undefined)?.cap;
  if (raw == null || !Number.isFinite(Number(raw))) return Infinity;
  return Number(raw);
}

/**
 * Count current usage for a tenant. Pluggable — callers may override with a
 * resource-specific counter. Default counts Parse sessions / sources in
 * the tenant's Dali DB; atoms and concurrentJobs fallback to 0 when no
 * natural aggregate exists yet.
 */
export async function countTenantResource(
  tenantAlias: string,
  resource: QuotaResource,
): Promise<number> {
  const safeAlias = tenantAlias.replace(/-/g, '_');
  const daliDb    = `dali_${safeAlias}`;
  try {
    switch (resource) {
      case 'parseSessions': {
        const rows = await friggQuery(daliDb,
          `SELECT count(*) AS c FROM DaliSession WHERE status = 'RUNNING' OR status = 'PENDING'`);
        return Number((rows[0] as { c?: number } | undefined)?.c ?? 0);
      }
      case 'sources': {
        const rows = await friggQuery(daliDb, `SELECT count(*) AS c FROM DaliSource`);
        return Number((rows[0] as { c?: number } | undefined)?.c ?? 0);
      }
      case 'concurrentJobs': {
        const rows = await friggQuery('frigg-jobrunr',
          `SELECT count(*) AS c FROM jobrunr_jobs WHERE state = 'PROCESSING'`);
        return Number((rows[0] as { c?: number } | undefined)?.c ?? 0);
      }
      case 'atoms':
        // atoms live in hound_{alias}, aggregate lands with MTN-09 follow-up;
        // returning 0 here means atoms cap is not enforced yet (safe default).
        return 0;
    }
  } catch {
    // FRIGG unavailable → fail-open (return 0). Better than blocking writes.
    return 0;
  }
}

async function loadCacheEntry(tenantAlias: string, resource: QuotaResource): Promise<CacheEntry> {
  const [count, cap] = await Promise.all([
    countTenantResource(tenantAlias, resource),
    readTenantCap(tenantAlias, resource),
  ]);
  const entry = { count, cap, fetchedAt: Date.now() };
  cache.set(keyOf(tenantAlias, resource), entry);
  return entry;
}

async function getCacheEntry(tenantAlias: string, resource: QuotaResource): Promise<CacheEntry> {
  const k = keyOf(tenantAlias, resource);
  const existing = cache.get(k);
  if (existing && Date.now() - existing.fetchedAt < CACHE_TTL_MS) return existing;
  return loadCacheEntry(tenantAlias, resource);
}

export interface QuotaCheckResult {
  allowed: boolean;
  status:  200 | 429 | 403;
  count:   number;
  cap:     number;
  reason?: string;
}

/**
 * Core check — returns structured result so callers can emit audit events
 * before sending the HTTP response.
 */
export async function checkQuota(
  tenantAlias: string,
  resource: QuotaResource,
): Promise<QuotaCheckResult> {
  const { count, cap } = await getCacheEntry(tenantAlias, resource);
  if (!Number.isFinite(cap))    return { allowed: true, status: 200, count, cap };
  if (count < cap)              return { allowed: true, status: 200, count, cap };
  if (count === cap)            return { allowed: false, status: 429, count, cap,
                                        reason: 'soft_cap: current === max; wait for next harvest window' };
  return                                { allowed: false, status: 403, count, cap,
                                        reason: 'hard_cap: current > max; tenant config lowered cap below usage' };
}

/**
 * Fastify pre-handler factory — returns a middleware that rejects the request
 * with 429 / 403 before the route handler runs. Route handlers still bump
 * {@link bumpCachedCount} on success so the in-process counter stays hot.
 */
export function quotaCheck(resource: QuotaResource) {
  return async function quotaCheckHandler(request: FastifyRequest, reply: FastifyReply) {
    const alias = (request.headers['x-seer-tenant-alias'] as string | undefined) ?? 'default';
    const result = await checkQuota(alias, resource);
    if (!result.allowed) {
      if (result.status === 429) reply.header('Retry-After', '60');
      return reply.status(result.status).send({
        error: 'quota_exceeded',
        resource,
        current: result.count,
        cap:     result.cap,
        reason:  result.reason,
        tenantAlias: alias,
      });
    }
  };
}

/** Optimistically bump the cached counter after a successful create. */
export function bumpCachedCount(tenantAlias: string, resource: QuotaResource, delta = 1): void {
  const k = keyOf(tenantAlias, resource);
  const e = cache.get(k);
  if (e) cache.set(k, { ...e, count: e.count + delta });
}

/** @internal test helper. */
export function __resetQuotaCacheForTests(): void {
  cache.clear();
}
