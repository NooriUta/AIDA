/**
 * CAP-12: Rate limiting middleware for admin and provisioning endpoints.
 *
 * Admin:        30 req/min per IP
 * Provisioning: 5 req/min per IP
 *
 * Rate limiting is disabled when DISABLE_RATE_LIMIT=true (dev / test environments)
 * or when the request originates from localhost (127.0.0.1 / ::1).
 */
import type { FastifyRequest, FastifyReply } from 'fastify';

const RATE_LIMIT_DISABLED =
  process.env.DISABLE_RATE_LIMIT === 'true' || process.env.NODE_ENV === 'test';

function isLocalhost(ip: string): boolean {
  return ip === '127.0.0.1' || ip === '::1' || ip === '::ffff:127.0.0.1';
}

interface Bucket {
  count:   number;
  resetAt: number;
}

function makeRateLimiter(maxPerMin: number) {
  const buckets = new Map<string, Bucket>();

  setInterval(() => {
    const now = Date.now();
    for (const [ip, b] of buckets) {
      if (now > b.resetAt) buckets.delete(ip);
    }
  }, 60_000).unref();

  function isLimited(ip: string): boolean {
    const now   = Date.now();
    const b     = buckets.get(ip);
    if (!b || now > b.resetAt) {
      buckets.set(ip, { count: 1, resetAt: now + 60_000 });
      return false;
    }
    b.count += 1;
    return b.count > maxPerMin;
  }

  return async (request: FastifyRequest, reply: FastifyReply): Promise<void> => {
    if (RATE_LIMIT_DISABLED || isLocalhost(request.ip)) return;
    if (isLimited(request.ip)) {
      return reply.status(429).send({
        error:       'Too Many Requests',
        retryAfter:  60,
      });
    }
  };
}

/** 30 req/min — for general admin endpoints. */
export const adminRateLimit        = makeRateLimiter(30);

/** 5 req/min — for provisioning/destructive endpoints. */
export const provisioningRateLimit = makeRateLimiter(5);
