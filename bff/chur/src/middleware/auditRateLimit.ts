/**
 * MTN-46 — Per-tenant audit-emit rate limit.
 *
 * Threat: an abusive tenant spams audit-triggering ops (login churn,
 * permission toggles, quota queries) and saturates the audit stream,
 * drowning out signal for other tenants in Heimdall / SIEM exports.
 *
 * Defense: per-tenant token bucket applied at the emit path. Soft cap
 * 1000/min — events still emitted but flag `sampled=true` added to
 * payload so downstream can adjust retention. Hard cap 10000/min —
 * events dropped + single `audit_flood_detected` event emitted per
 * minute window for that tenant.
 *
 * This middleware is intentionally lightweight (in-process counters,
 * per-replica). Coordinated distributed limiting lands with MTN-56
 * priority tiers.
 */

const SOFT_PER_MIN = 1_000;
const HARD_PER_MIN = 10_000;
const WINDOW_MS    = 60_000;

interface Bucket {
  count:     number;
  windowEnd: number;
  floodSignaled: boolean;
}

const buckets = new Map<string, Bucket>();

function getBucket(tenantAlias: string): Bucket {
  const now = Date.now();
  let b = buckets.get(tenantAlias);
  if (!b || now > b.windowEnd) {
    b = { count: 0, windowEnd: now + WINDOW_MS, floodSignaled: false };
    buckets.set(tenantAlias, b);
  }
  return b;
}

export type AuditRateDecision =
  | { action: 'EMIT' }
  | { action: 'EMIT_SAMPLED' }
  | { action: 'DROP' }
  | { action: 'DROP_WITH_FLOOD_SIGNAL' };

/**
 * Call before persisting an audit event. The decision tells the caller
 * whether to emit, mark sampled, drop silently, or drop-and-emit one
 * `audit_flood_detected` event for the current window.
 */
export function checkAuditRate(tenantAlias: string): AuditRateDecision {
  if (!tenantAlias || tenantAlias.length === 0) tenantAlias = 'default';
  const b = getBucket(tenantAlias);
  b.count += 1;

  if (b.count > HARD_PER_MIN) {
    if (!b.floodSignaled) {
      b.floodSignaled = true;
      return { action: 'DROP_WITH_FLOOD_SIGNAL' };
    }
    return { action: 'DROP' };
  }
  if (b.count > SOFT_PER_MIN) {
    return { action: 'EMIT_SAMPLED' };
  }
  return { action: 'EMIT' };
}

/** @internal test helper — reset state between tests. */
export function __resetAuditRateLimit(): void {
  buckets.clear();
}
