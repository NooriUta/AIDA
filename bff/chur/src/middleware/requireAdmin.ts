import type { FastifyRequest, FastifyReply } from 'fastify';

/**
 * Fastify preHandler factory: requires all listed scopes in the session.
 * Must be used AFTER app.authenticate in the preHandler chain.
 *
 * Replaces the M1 role === 'admin' pattern with JWT scope-based RBAC.
 * Scopes are extracted from the Keycloak JWT `scope` claim at login time
 * and stored in the server-side session.
 *
 * Usage:
 *   app.delete('/route', { preHandler: [app.authenticate, requireScope('aida:admin')] }, handler)
 *   app.post('/danger', { preHandler: [app.authenticate, requireDestructive] }, handler)
 */
export function requireScope(...scopes: string[]) {
  return async (request: FastifyRequest, reply: FastifyReply): Promise<void> => {
    const sessionScopes: string[] = request.user?.scopes ?? [];
    const hasAll = scopes.every((s) => sessionScopes.includes(s));
    if (!hasAll) {
      return reply.status(403).send({
        error:    'Forbidden',
        required: scopes,
        message:  `Missing required scope(s): ${scopes.join(', ')}`,
      });
    }
  };
}

// Backward-compatible aliases — existing routes using requireAdmin do not need to change.
export const requireAdmin       = requireScope('aida:admin');
export const requireDestructive = requireScope('aida:admin', 'aida:admin:destructive');
