import type { FastifyRequest, FastifyReply } from 'fastify';

/**
 * Fastify preHandler: requires a valid authenticated session with admin role.
 * Must be used AFTER app.authenticate in the preHandler chain.
 *
 * M1 mapping: aida:admin scope → role === 'admin'
 * Full scope-based RBAC deferred to Sprint 4.
 *
 * Usage:
 *   app.get('/some-admin-route', { preHandler: [app.authenticate, requireAdmin] }, handler)
 */
export async function requireAdmin(
  request: FastifyRequest,
  reply: FastifyReply,
): Promise<void> {
  if (request.user?.role !== 'admin') {
    return reply.status(403).send({ error: 'admin role required' });
  }
}
