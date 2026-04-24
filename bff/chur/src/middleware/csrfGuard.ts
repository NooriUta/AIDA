/**
 * MTN-44 — CSRF defense-in-depth for state-changing admin endpoints.
 *
 * Chur already sets cookie `sid` with {@code SameSite=strict} in production, which
 * is the primary CSRF defense — a cross-site form POST cannot attach `sid`. This
 * middleware adds a belt on top of the SameSite suspender: every mutating request
 * (POST / PUT / PATCH / DELETE) must carry an {@code Origin} or {@code Referer}
 * header whose origin matches one of the configured allow-list entries. Missing
 * or mismatching = 403 `csrf_origin_mismatch` before the route handler runs.
 *
 * <p>Why both? SameSite=strict can be downgraded by some embedded webviews and
 * by proxies that strip cookies & re-attach them for SSO use-cases. An explicit
 * Origin check catches that class of mistake without relying on browser behavior.
 *
 * <p>GET/HEAD/OPTIONS are exempt (non-mutating). Bearer-auth callers (service
 * accounts, M2M) are exempt too — CSRF only matters for ambient cookie auth.
 */
import type { FastifyReply, FastifyRequest } from 'fastify';
import { config } from '../config';

/** Parse `Origin` or `Referer` header to its scheme+host+port origin. */
function originOf(raw: string | undefined): string | null {
  if (!raw) return null;
  try {
    const u = new URL(raw);
    return `${u.protocol}//${u.host}`;
  } catch {
    return null;
  }
}

function allowedOrigins(): string[] {
  const raw = config.corsOrigin ?? '';
  return raw.split(',').map(s => s.trim()).filter(Boolean);
}

const SAFE_METHODS = new Set(['GET', 'HEAD', 'OPTIONS']);

export async function csrfGuard(request: FastifyRequest, reply: FastifyReply): Promise<void> {
  const method = request.method?.toUpperCase() ?? '';
  if (SAFE_METHODS.has(method)) return;

  // Bearer-auth callers (M2M / service accounts) skip the CSRF check. Cookie auth
  // is the only vector exposed to forged cross-site submissions.
  const auth = request.headers.authorization;
  if (auth && auth.startsWith('Bearer ')) return;

  const origin =
    originOf(request.headers.origin as string | undefined) ??
    originOf(request.headers.referer as string | undefined);

  if (!origin) {
    return reply.status(403).send({
      error: 'csrf_origin_missing',
      hint:  'mutating requests require Origin or Referer',
    });
  }
  const allowed = allowedOrigins();
  if (!allowed.some(a => a === origin)) {
    return reply.status(403).send({
      error:    'csrf_origin_mismatch',
      origin,
      allowed,
    });
  }
}
