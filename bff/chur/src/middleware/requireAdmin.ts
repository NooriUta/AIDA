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
 * G6 + G7: Blocks cross-tenant access. Fail-closed for non-superadmin/non-admin
 * when session has no activeTenantAlias.
 *
 * Source of truth: `request.user.activeTenantAlias` (verified server-side, set
 * during Auth Code callback or ROPC login from JWT). The legacy unverified
 * Bearer-token decode is removed (G7) — session is authoritative.
 *
 * Mismatch emits seer.audit.tenant_spoof_attempt.
 */
export function requireSameTenant() {
  return async (request: FastifyRequest, reply: FastifyReply): Promise<void> => {
    if (!request.user) {
      return reply.status(401).send({ error: 'Unauthorized' });
    }
    // Platform-tier roles bypass tenant scoping (admin can act across tenants;
    // super-admin even more so). Per spec §3 cross-tenant access matrix.
    if (request.user.scopes?.includes('aida:superadmin')) return;
    if (request.user.scopes?.includes('aida:admin')) return;

    const targetAlias = (request.params as Record<string, string>)?.alias
      ?? (request.params as Record<string, string>)?.tenantId;
    if (!targetAlias) return; // route has no tenant scoping

    // G6: fail-closed — non-platform user MUST have an active tenant in session.
    const sessionAlias = (request.user as any).activeTenantAlias as string | undefined;
    if (!sessionAlias) {
      console.warn(
        `[RBAC] G6 fail-closed — user=${request.user.username} ` +
        `role=${request.user.role} session has no activeTenantAlias, target=${targetAlias}`,
      );
      return reply.status(403).send({
        error: 'Forbidden: session has no active tenant',
        message: 'Re-authenticate or switch tenant via PATCH /auth/me/tenant',
      });
    }

    if (sessionAlias !== targetAlias) {
      console.warn(
        `[RBAC] cross-tenant denied — user=${request.user.username} ` +
        `session_alias=${sessionAlias} target=${targetAlias}`,
      );
      emitSpoofAttempt(request.user.username, targetAlias, sessionAlias);
      return reply.status(403).send({ error: 'Forbidden: cross-tenant access denied' });
    }
  };
}
