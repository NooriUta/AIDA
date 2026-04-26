import type { FastifyRequest, FastifyReply } from 'fastify';
import { emitSpoofAttempt } from './auditEmit';

/**
 * Fastify preHandler factory: requires all listed scopes in the session.
 * Must be used AFTER app.authenticate in the preHandler chain.
 */
export function requireScope(...scopes: string[]) {
  return async (request: FastifyRequest, reply: FastifyReply): Promise<void> => {
    const sessionScopes: string[] = request.user?.scopes ?? [];
    const hasAll = scopes.every((s) => sessionScopes.includes(s));
    if (!hasAll) {
      const missing = scopes.filter((s) => !sessionScopes.includes(s));
      console.warn(
        `[RBAC] 403 — user=${request.user?.username ?? '?'} ` +
        `role=${request.user?.role ?? '?'} ` +
        `missing=${missing.join(',')} ` +
        `session_scopes=${sessionScopes.join(',') || '(empty)'}`,
      );
      return reply.status(403).send({
        error:    'Forbidden',
        required: scopes,
        missing,
        message:  `Missing required scope(s): ${missing.join(', ')}`,
      });
    }
  };
}

// Backward-compatible aliases.
export const requireAdmin       = requireScope('aida:admin');
export const requireDestructive = requireScope('aida:admin', 'aida:admin:destructive');

/**
 * Round 5 — requires at least ONE of the listed scopes (OR semantics).
 * Use when an endpoint accepts multiple admin levels (e.g. tenant-admin
 * OR platform-admin OR superadmin).
 */
export function requireAnyScope(...scopes: string[]) {
  return async (request: FastifyRequest, reply: FastifyReply): Promise<void> => {
    const sessionScopes: string[] = request.user?.scopes ?? [];
    const hasAny = scopes.some((s) => sessionScopes.includes(s));
    if (!hasAny) {
      console.warn(
        `[RBAC] 403 — user=${request.user?.username ?? '?'} ` +
        `role=${request.user?.role ?? '?'} ` +
        `needed_any_of=${scopes.join(',')} ` +
        `session_scopes=${sessionScopes.join(',') || '(empty)'}`,
      );
      return reply.status(403).send({
        error:    'Forbidden',
        requiredAnyOf: scopes,
        message:  `Missing required scope — at least one of: ${scopes.join(', ')}`,
      });
    }
  };
}

/**
 * CAP-10/15: Blocks cross-tenant access and enforces JWT ↔ header alias consistency.
 *
 * Phase 2 (multi-tenant): validates that the JWT organization.alias claim matches
 * the :alias route param. Superadmin bypasses the check.
 * Mismatch emits seer.audit.tenant_spoof_attempt.
 */
export function requireSameTenant() {
  return async (request: FastifyRequest, reply: FastifyReply): Promise<void> => {
    if (!request.user) {
      return reply.status(401).send({ error: 'Unauthorized' });
    }
    if (request.user.scopes?.includes('aida:superadmin')) return;

    const targetAlias = (request.params as Record<string, string>)?.alias
      ?? (request.params as Record<string, string>)?.tenantId;
    if (!targetAlias) return; // route has no tenant scoping

    // Extract JWT organization.alias claim (CAP-15 anti-spoofing)
    const jwtAlias = extractJwtOrgAlias(request);
    if (jwtAlias && jwtAlias !== targetAlias) {
      console.warn(
        `[RBAC] spoof attempt — user=${request.user.username} ` +
        `jwt_alias=${jwtAlias} header_alias=${targetAlias}`,
      );
      emitSpoofAttempt(request.user.username, targetAlias, jwtAlias);
      return reply.status(403).send({ error: 'Forbidden: tenant alias mismatch' });
    }

    // Phase 2: enforce session activeTenantAlias vs route param (CAP-16 fix)
    const sessionAlias = (request.user as any).activeTenantAlias as string | undefined;
    if (sessionAlias && sessionAlias !== targetAlias) {
      return reply.status(403).send({ error: 'Forbidden: cross-tenant access denied' });
    }
  };
}

// ── Internal: lightweight JWT claim extraction ────────────────────────────────

function extractJwtOrgAlias(request: FastifyRequest): string | null {
  const auth = request.headers.authorization ?? '';
  if (!auth.startsWith('Bearer ')) return null;
  const token = auth.slice(7);
  const parts = token.split('.');
  if (parts.length < 2) return null;
  try {
    const payload = JSON.parse(Buffer.from(parts[1]!, 'base64url').toString('utf8'));
    return (payload?.organization?.alias as string | undefined) ?? null;
  } catch {
    return null;
  }
}
