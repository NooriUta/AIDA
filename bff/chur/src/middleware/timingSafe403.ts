/**
 * MTN-40 — Tenant-existence oracle defense via timing equalization.
 *
 * Problem: GET /api/admin/tenants/acme returns 403 when user is not a member,
 * and 404 when the tenant doesn't exist. The latter path is usually faster
 * because it short-circuits before permission checks. An attacker enumerates
 * tenants by timing: fast = doesn't exist, slow = exists-but-forbidden.
 *
 * Defense: `equalize403()` wraps a 403/404-returning handler — normalizes the
 * response to 403 (hiding existence) AND adds randomized delay 50-200ms so
 * timing leaks are bounded.
 *
 * Scope: admin tenant endpoints. 404 on completely wrong shape (invalid
 * alias format, missing path segments) stays — attacker cannot derive
 * existence from those anyway.
 */
import type { FastifyReply, FastifyRequest } from 'fastify';

const DELAY_MIN_MS = 50;
const DELAY_MAX_MS = 200;

function randomDelay(): number {
  return DELAY_MIN_MS + Math.floor(Math.random() * (DELAY_MAX_MS - DELAY_MIN_MS + 1));
}

function sleep(ms: number): Promise<void> {
  return new Promise(resolve => setTimeout(resolve, ms));
}

/**
 * Post-handler that equalizes any 403/404 into a uniform opaque 403
 * response with randomized delay. Call from inside route handlers when
 * returning access-control errors.
 */
export async function opaqueForbidden(reply: FastifyReply): Promise<FastifyReply> {
  await sleep(randomDelay());
  return reply.status(403).send({ error: 'forbidden' });
}

/**
 * Wrap any async handler so thrown errors / 404 results become opaque 403s
 * with randomized timing. Only use on admin endpoints that should hide
 * existence — NOT on self-service endpoints where 404 is semantically useful.
 */
export function withOpaqueForbidden<T>(
  handler: (request: FastifyRequest, reply: FastifyReply) => Promise<T>,
): (request: FastifyRequest, reply: FastifyReply) => Promise<T | FastifyReply> {
  return async function wrapped(request: FastifyRequest, reply: FastifyReply) {
    try {
      const result = await handler(request, reply);
      if (reply.statusCode === 404) {
        await sleep(randomDelay());
        return reply.status(403).send({ error: 'forbidden' });
      }
      return result;
    } catch (err) {
      await sleep(randomDelay());
      return reply.status(403).send({ error: 'forbidden' });
    }
  };
}

/** @internal test helper — bypasses randomness for deterministic assertions. */
export const __timingConfig = { min: DELAY_MIN_MS, max: DELAY_MAX_MS };
