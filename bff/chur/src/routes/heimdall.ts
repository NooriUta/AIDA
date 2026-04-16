import type { FastifyPluginAsync }      from 'fastify';
import type { SocketStream }            from '@fastify/websocket';
import { WebSocket }                    from 'ws';
import { requireAdmin }                 from '../middleware/requireAdmin';
import { ensureValidSession }           from '../sessions';
import { listUsers, setUserEnabled }    from '../keycloakAdmin';

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

  // ── GET /heimdall/users — list users from Keycloak + KC attributes ────────
  // Returns KcUserView[] (profile + prefs attrs stored in KC).
  // Quotas / source bindings come from FRIGG (R4.3 — not yet wired).
  app.get(
    '/heimdall/users',
    { preHandler: [app.authenticate, requireAdmin] },
    async (_request, reply) => {
      try {
        const users = await listUsers();
        return reply.send(users);
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'KC_ADMIN_ERROR';
        return reply.status(502).send({ error: msg });
      }
    },
  );

  // ── PUT /heimdall/users/:userId/enabled — block/unblock user ──────────────
  app.put(
    '/heimdall/users/:userId/enabled',
    { preHandler: [app.authenticate, requireAdmin] },
    async (request, reply) => {
      const { userId } = request.params as { userId: string };
      const { enabled } = request.body as { enabled: boolean };
      try {
        await setUserEnabled(userId, enabled);
        return reply.send({ ok: true });
      } catch (err) {
        const msg = err instanceof Error ? err.message : 'KC_ADMIN_ERROR';
        return reply.status(502).send({ error: msg });
      }
    },
  );

  // ── GET /heimdall/services/health — all-services health poll ────────────────
  // ServicesPage polls this every 10 s. Admin-only.
  app.get(
    '/heimdall/services/health',
    { preHandler: [app.authenticate, requireAdmin] },
    async (request, reply) => {
      try {
        const res = await fetch(`${HEIMDALL_ORIGIN}/services/health`, {
          headers: { 'X-Seer-Role': request.user.role },
        });
        return reply.status(res.status).send(await res.json());
      } catch {
        return reply.status(503).send({ error: 'HEIMDALL_UNREACHABLE' });
      }
    },
  );

  // ── POST /heimdall/services/:name/restart — restart a service ────────────
  // Admin-only. Delegates to heimdall-backend which shells out to docker.
  app.post(
    '/heimdall/services/:name/restart',
    { preHandler: [app.authenticate, requireAdmin] },
    async (request, reply) => {
      const { name } = request.params as { name: string };
      const query    = (request.query as Record<string, string>).mode
        ? `?mode=${(request.query as Record<string, string>).mode}`
        : '';
      try {
        const res = await fetch(`${HEIMDALL_ORIGIN}/services/${name}/restart${query}`, {
          method: 'POST',
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
    async (connection: SocketStream, request) => {
      // connection = SocketStream (Duplex); connection.socket = ws.WebSocket
      const ws = connection.socket;

      // Auth: validate session cookie
      const sid = (request.cookies as Record<string, string | undefined>)?.sid;
      if (!sid) {
        ws.close(1008, 'Unauthorized');
        return;
      }

      const sessionUser = await ensureValidSession(sid).catch(() => null);
      const hasAdminScope = sessionUser?.scopes?.includes('aida:admin') ?? false;
      if (!sessionUser || !hasAdminScope) {
        ws.close(1008, 'Forbidden');
        return;
      }

      // Build upstream WS URL, forward filter param
      const filterParam = (request.query as Record<string, string>).filter ?? '';
      const wsUrl = `${HEIMDALL_WS}/ws/events${filterParam ? `?filter=${encodeURIComponent(filterParam)}` : ''}`;

      const upstream = new WebSocket(wsUrl);

      upstream.on('message', (data) => {
        if (ws.readyState === WebSocket.OPEN) {
          // Convert Buffer → string so the browser receives a text frame, not binary.
          // JSON.parse(msg.data) in useEventStream requires a string, not a Blob.
          ws.send(data.toString());
        }
      });

      upstream.on('error', () => ws.close(1011, 'Upstream error'));
      upstream.on('close', () => { if (ws.readyState === WebSocket.OPEN) ws.close(); });

      ws.on('close', () => {
        if (upstream.readyState === WebSocket.OPEN) upstream.close();
      });

      // Client is read-only — ignore messages from browser
      ws.on('message', () => {});
    },
  );
};
