import type { FastifyPluginAsync } from 'fastify';
import { WebSocket }               from 'ws';
import { requireAdmin }            from '../middleware/requireAdmin';
import { ensureValidSession }      from '../sessions';

const HEIMDALL_ORIGIN = process.env.HEIMDALL_URL ?? 'http://localhost:9093';
const HEIMDALL_WS     = HEIMDALL_ORIGIN.replace(/^http/, 'ws');

/**
 * Proxy routes: Chur → HEIMDALL backend.
 *
 * Authorization model (M1):
 *   - app.authenticate validates Keycloak session cookie
 *   - requireAdmin enforces role === 'admin'
 *   - X-Seer-Role forwarded so HEIMDALL can enforce its own guard
 *
 * Routes:
 *   GET  /heimdall/health             — unauthenticated
 *   GET  /heimdall/metrics/snapshot   — admin
 *   POST /heimdall/control/:action    — admin (destructive)
 *   GET  /heimdall/control/snapshots  — admin
 *   GET  /heimdall/ws/events          — admin WebSocket proxy (T3.2)
 */
export const heimdallRoutes: FastifyPluginAsync = async (app) => {

  // ── GET /heimdall/health — unauthenticated health probe ───────────────────
  app.get('/heimdall/health', async (_req, reply) => {
    try {
      const res  = await fetch(`${HEIMDALL_ORIGIN}/q/health`);
      const body = await res.json();
      return reply.status(res.status).send(body);
    } catch {
      return reply.status(503).send({ status: 'HEIMDALL_UNREACHABLE' });
    }
  });

  // ── GET /heimdall/metrics/snapshot — admin only ───────────────────────────
  app.get(
    '/heimdall/metrics/snapshot',
    { preHandler: [app.authenticate, requireAdmin] },
    async (request, reply) => {
      try {
        const res = await fetch(`${HEIMDALL_ORIGIN}/metrics/snapshot`, {
          headers: { 'X-Seer-Role': request.user.role },
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
    { preHandler: [app.authenticate, requireAdmin] },
    async (request, reply) => {
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
            'X-Seer-Role':  request.user.role,
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
    { preHandler: [app.authenticate, requireAdmin] },
    async (request, reply) => {
      try {
        const res = await fetch(`${HEIMDALL_ORIGIN}/control/snapshots`, {
          headers: { 'X-Seer-Role': request.user.role },
        });
        return reply.status(res.status).send(await res.json());
      } catch {
        return reply.status(503).send({ error: 'HEIMDALL_UNREACHABLE' });
      }
    },
  );

  // ── GET /heimdall/ws/events — WebSocket proxy (T3.2) ─────────────────────
  // Authenticates via session cookie, then proxies to HEIMDALL native WS.
  // Forwards ?filter= query param if present.
  // Client is read-only (upstream → client only).
  app.get(
    '/heimdall/ws/events',
    { websocket: true },
    async (socket, request) => {
      // Auth: validate session cookie
      const sid = (request.cookies as Record<string, string | undefined>)?.sid;
      if (!sid) {
        socket.close(1008, 'Unauthorized');
        return;
      }

      const sessionUser = await ensureValidSession(sid).catch(() => null);
      if (!sessionUser || sessionUser.role !== 'admin') {
        socket.close(1008, 'Forbidden');
        return;
      }

      // Build upstream WS URL, forward filter param
      const filterParam = (request.query as Record<string, string>).filter ?? '';
      const wsUrl = `${HEIMDALL_WS}/ws/events${filterParam ? `?filter=${encodeURIComponent(filterParam)}` : ''}`;

      const upstream = new WebSocket(wsUrl);

      upstream.on('message', (data) => {
        if (socket.readyState === socket.OPEN) {
          socket.send(data as string);
        }
      });

      upstream.on('error', () => socket.close(1011, 'Upstream error'));
      upstream.on('close',  () => { if (socket.readyState === socket.OPEN) socket.close(); });

      socket.on('close', () => {
        if (upstream.readyState === WebSocket.OPEN) upstream.close();
      });

      // Client is read-only — ignore messages from browser
      socket.on('message', () => {});
    },
  );
};
