import type { FastifyPluginAsync } from 'fastify';

const MIMIR_URL = process.env.MIMIR_URL ?? 'http://127.0.0.1:9094';

/**
 * Proxy routes: Chur → MIMIR AI assistant (port 9094).
 *
 * Auth: app.authenticate validates Keycloak session cookie.
 * X-Seer-Tenant-Alias forwarded so MIMIR isolates chat memory per tenant.
 *
 * Routes:
 *   POST   /mimir/ask            — submit lineage question → MimirAnswer
 *   GET    /mimir/sessions       — list active sessions for tenant
 *   GET    /mimir/sessions/:id   — get session details
 *   DELETE /mimir/sessions/:id   — clear session chat memory
 *   GET    /mimir/health         — unauthenticated health probe
 */
export const mimirRoutes: FastifyPluginAsync = async (app) => {

  // ── POST /mimir/ask ────────────────────────────────────────────────────────
  app.post(
    '/mimir/ask',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const { activeTenantAlias, role } = request.user;
      try {
        const upstream = await fetch(`${MIMIR_URL}/api/ask`, {
          method:  'POST',
          headers: {
            'Content-Type':        'application/json',
            'X-Seer-Tenant-Alias': activeTenantAlias ?? 'default',
            'X-Seer-Role':         role,
          },
          body: JSON.stringify(request.body),
        });
        return reply.status(upstream.status).send(await upstream.json());
      } catch (err) {
        request.log.error({ err }, 'mimir upstream unreachable');
        return reply.status(503).send({ error: 'MIMIR service unavailable' });
      }
    },
  );

  // ── GET /mimir/sessions ────────────────────────────────────────────────────
  app.get(
    '/mimir/sessions',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const { activeTenantAlias } = request.user;
      try {
        const upstream = await fetch(`${MIMIR_URL}/api/sessions`, {
          headers: { 'X-Seer-Tenant-Alias': activeTenantAlias ?? 'default' },
        });
        return reply.status(upstream.status).send(await upstream.json());
      } catch {
        return reply.status(503).send({ error: 'MIMIR service unavailable' });
      }
    },
  );

  // ── GET /mimir/sessions/:id ────────────────────────────────────────────────
  app.get(
    '/mimir/sessions/:id',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const { id } = request.params as { id: string };
      const { activeTenantAlias } = request.user;
      try {
        const upstream = await fetch(
          `${MIMIR_URL}/api/sessions/${encodeURIComponent(id)}`,
          { headers: { 'X-Seer-Tenant-Alias': activeTenantAlias ?? 'default' } },
        );
        return reply.status(upstream.status).send(await upstream.json());
      } catch {
        return reply.status(503).send({ error: 'MIMIR service unavailable' });
      }
    },
  );

  // ── DELETE /mimir/sessions/:id ─────────────────────────────────────────────
  app.delete(
    '/mimir/sessions/:id',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const { id } = request.params as { id: string };
      const { activeTenantAlias } = request.user;
      try {
        const upstream = await fetch(
          `${MIMIR_URL}/api/sessions/${encodeURIComponent(id)}`,
          {
            method:  'DELETE',
            headers: { 'X-Seer-Tenant-Alias': activeTenantAlias ?? 'default' },
          },
        );
        return reply.status(upstream.status).send();
      } catch {
        return reply.status(503).send({ error: 'MIMIR service unavailable' });
      }
    },
  );

  // ── GET /mimir/health — unauthenticated ────────────────────────────────────
  app.get('/mimir/health', async (_request, reply) => {
    try {
      const upstream = await fetch(`${MIMIR_URL}/api/health`);
      return reply.status(upstream.status).send(await upstream.json());
    } catch {
      return reply.status(503).send({ status: 'DOWN', service: 'mimir' });
    }
  });
};
