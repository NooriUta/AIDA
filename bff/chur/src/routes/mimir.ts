import type { FastifyPluginAsync } from 'fastify';

const MIMIR_URL      = process.env.MIMIR_URL ?? 'http://127.0.0.1:9094';
const DEFAULT_TENANT = process.env.MIMIR_DEFAULT_TENANT_ALIAS ?? 'default';

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
            'X-Seer-Tenant-Alias': activeTenantAlias ?? DEFAULT_TENANT,
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
          headers: { 'X-Seer-Tenant-Alias': activeTenantAlias ?? DEFAULT_TENANT },
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
          { headers: { 'X-Seer-Tenant-Alias': activeTenantAlias ?? DEFAULT_TENANT } },
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
            headers: { 'X-Seer-Tenant-Alias': activeTenantAlias ?? DEFAULT_TENANT },
          },
        );
        return reply.status(upstream.status).send();
      } catch {
        return reply.status(503).send({ error: 'MIMIR service unavailable' });
      }
    },
  );

  // ── POST /mimir/sessions/:id/decision — TIER2 MT-08 HiL approve/reject ────
  app.post(
    '/mimir/sessions/:id/decision',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const { id } = request.params as { id: string };
      const { activeTenantAlias, role, sub } = request.user as { activeTenantAlias?: string; role?: string; sub?: string };
      try {
        const upstream = await fetch(
          `${MIMIR_URL}/api/sessions/${encodeURIComponent(id)}/decision`,
          {
            method:  'POST',
            headers: {
              'Content-Type':        'application/json',
              'X-Seer-Tenant-Alias': activeTenantAlias ?? DEFAULT_TENANT,
              'X-Seer-Role':         role ?? 'user',
              'X-Seer-User-Id':      sub ?? 'unknown',
            },
            body: JSON.stringify(request.body),
          },
        );
        return reply.status(upstream.status).send(await upstream.json());
      } catch {
        return reply.status(503).send({ error: 'MIMIR service unavailable' });
      }
    },
  );

  // ── Admin BYOK / quota proxies — TIER2 MT-06 + MT-07 ──────────────────────
  // POST   /mimir/admin/llm-config              — upsert tenant BYOK config
  // DELETE /mimir/admin/llm-config/:alias       — remove tenant BYOK config
  // GET    /mimir/admin/llm-config/:alias       — masked view
  // PUT    /mimir/admin/llm-config/quota/:alias — set tenant quota
  // GET    /mimir/admin/llm-config/quota/:alias — get tenant quota
  // GET    /mimir/admin/llm-config/usage/:alias — daily usage aggregates
  const proxyAdmin = (
    method: 'GET' | 'POST' | 'PUT' | 'DELETE',
    suffix: string,
  ) =>
    async (request: any, reply: any) => {
      const { role, sub } = request.user as { role?: string; sub?: string };
      if (role !== 'admin' && role !== 'superadmin') {
        return reply.status(403).send({ error: 'admin role required' });
      }
      const url = `${MIMIR_URL}/api/admin/llm-config${suffix.replace(/:alias/g, encodeURIComponent(request.params.alias ?? ''))}`;
      const headers: Record<string, string> = {
        'Content-Type':        'application/json',
        'X-Seer-Role':         role,
        'X-Seer-User-Id':      sub ?? 'unknown',
      };
      const body = method === 'GET' || method === 'DELETE' ? undefined : JSON.stringify(request.body);
      try {
        const upstream = await fetch(url, { method, headers, body });
        const text = await upstream.text();
        reply.status(upstream.status);
        return text ? reply.type('application/json').send(text) : reply.send();
      } catch {
        return reply.status(503).send({ error: 'MIMIR service unavailable' });
      }
    };

  app.post  ('/mimir/admin/llm-config',                { preHandler: [app.authenticate] }, proxyAdmin('POST',   ''));
  app.delete('/mimir/admin/llm-config/:alias',         { preHandler: [app.authenticate] }, proxyAdmin('DELETE', '/:alias'));
  app.get   ('/mimir/admin/llm-config/:alias',         { preHandler: [app.authenticate] }, proxyAdmin('GET',    '/:alias'));
  app.put   ('/mimir/admin/llm-config/quota/:alias',   { preHandler: [app.authenticate] }, proxyAdmin('PUT',    '/quota/:alias'));
  app.get   ('/mimir/admin/llm-config/quota/:alias',   { preHandler: [app.authenticate] }, proxyAdmin('GET',    '/quota/:alias'));
  app.get   ('/mimir/admin/llm-config/usage/:alias',   { preHandler: [app.authenticate] }, proxyAdmin('GET',    '/usage/:alias'));

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
