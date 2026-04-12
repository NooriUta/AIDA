import type { FastifyPluginAsync } from 'fastify';

const HEIMDALL_ORIGIN = process.env.HEIMDALL_URL ?? 'http://localhost:9093';

/**
 * Proxy routes: Chur → HEIMDALL backend.
 *
 * Authorization model:
 *   - Chur validates Keycloak JWT (session cookie) via app.authenticate
 *   - Only "admin" role may access control and metrics endpoints
 *   - X-Seer-Role is forwarded so HEIMDALL can enforce its own guard
 *
 * WebSocket proxy (/heimdall/ws/events) deferred to Sprint 3
 * (requires @fastify/websocket which is not yet installed).
 */
export const heimdallRoutes: FastifyPluginAsync = async (app) => {

  // ── GET /heimdall/health — unauthenticated health probe ───────────────────
  app.get('/heimdall/health', async (_req, reply) => {
    try {
      const res = await fetch(`${HEIMDALL_ORIGIN}/q/health`);
      const body = await res.json();
      return reply.status(res.status).send(body);
    } catch {
      return reply.status(503).send({ status: 'HEIMDALL_UNREACHABLE' });
    }
  });

  // ── GET /heimdall/metrics/snapshot — admin only ───────────────────────────
  app.get(
    '/heimdall/metrics/snapshot',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const { role } = request.user;
      if (role !== 'admin') {
        return reply.status(403).send({ error: 'admin role required' });
      }
      try {
        const res = await fetch(`${HEIMDALL_ORIGIN}/metrics/snapshot`, {
          headers: { 'X-Seer-Role': role },
        });
        return reply.status(res.status).send(await res.json());
      } catch {
        return reply.status(503).send({ error: 'HEIMDALL_UNREACHABLE' });
      }
    },
  );

  // ── POST /heimdall/control/:action — admin only (destructive) ─────────────
  app.post(
    '/heimdall/control/:action',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const { role } = request.user;
      if (role !== 'admin') {
        return reply.status(403).send({ error: 'admin role required' });
      }
      const { action } = request.params as { action: string };
      const url = new URL(`/control/${action}`, HEIMDALL_ORIGIN);

      // Forward query params (e.g. ?name=baseline for /control/snapshot)
      if (request.query && typeof request.query === 'object') {
        for (const [k, v] of Object.entries(request.query as Record<string, string>)) {
          url.searchParams.set(k, v);
        }
      }

      try {
        const res = await fetch(url.toString(), {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'X-Seer-Role':  role,
          },
          body: request.body ? JSON.stringify(request.body) : undefined,
        });
        return reply.status(res.status).send(await res.json());
      } catch {
        return reply.status(503).send({ error: 'HEIMDALL_UNREACHABLE' });
      }
    },
  );

  // ── GET /heimdall/control/snapshots — admin only ──────────────────────────
  app.get(
    '/heimdall/control/snapshots',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const { role } = request.user;
      if (role !== 'admin') {
        return reply.status(403).send({ error: 'admin role required' });
      }
      try {
        const res = await fetch(`${HEIMDALL_ORIGIN}/control/snapshots`, {
          headers: { 'X-Seer-Role': role },
        });
        return reply.status(res.status).send(await res.json());
      } catch {
        return reply.status(503).send({ error: 'HEIMDALL_UNREACHABLE' });
      }
    },
  );
};
