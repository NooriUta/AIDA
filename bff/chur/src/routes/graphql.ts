import type { FastifyPluginAsync } from 'fastify';
import WebSocket from 'ws';

const LINEAGE_API_URL = process.env.LINEAGE_API_URL ?? 'http://127.0.0.1:8080';

function toWsUrl(httpUrl: string): string {
  return httpUrl.replace(/^http:/, 'ws:').replace(/^https:/, 'wss:');
}

/**
 * Proxy /graphql → lineage-api (Quarkus, port 8080).
 *
 * rbac-proxy verifies the JWT cookie, then forwards the request with
 * trusted X-Seer-Role and X-Seer-User headers. lineage-api reads those
 * headers via SeerIdentity and applies RLS.
 *
 * lineage-api should NOT be exposed directly to the browser —
 * only reachable from rbac-proxy (enforced by Docker network in prod).
 */
export const graphqlRoutes: FastifyPluginAsync = async (app) => {
  // ── POST /graphql ───────────────────────────────────────────────────────────
  app.post(
    '/',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const { username, role, scopes, activeTenantAlias } = request.user;
      const isSuperAdmin = role === 'super-admin' || scopes?.includes('aida:superadmin');
      const overrideTenant = isSuperAdmin
        ? (request.headers['x-seer-override-tenant'] as string | undefined)
        : undefined;
      const effectiveTenant = overrideTenant ?? activeTenantAlias ?? 'default';

      try {
        const upstream = await fetch(`${LINEAGE_API_URL}/graphql`, {
          method: 'POST',
          headers: {
            'Content-Type':          'application/json',
            'X-Seer-Role':           role,
            'X-Seer-User':           username,
            'X-Seer-Tenant-Alias':   effectiveTenant,
          },
          body: JSON.stringify(request.body),
        });

        const data = await upstream.json();

        return reply
          .status(upstream.status)
          .header('Content-Type', 'application/json')
          .send(data);

      } catch (err) {
        app.log.error(err, 'lineage-api unreachable');
        return reply.status(503).send({
          errors: [{ message: 'Lineage API unavailable' }],
        });
      }
    },
  );

  // ── GET /graphql — HTTP introspection + graphql-ws subscription proxy ────────
  // app.route with `wsHandler` lets Fastify/websocket share the same GET path:
  //  - HTTP GET  → passthrough to lineage-api (GraphiQL / introspection)
  //  - WS upgrade → graphql-transport-ws proxy with X-Seer-Role/User injected
  app.route({
    method: 'GET',
    url: '/',
    preHandler: [app.authenticate],

    // HTTP GET — introspection / GraphiQL passthrough
    handler: async (request, reply) => {
      const { username, role, scopes, activeTenantAlias } = request.user;
      const isSuperAdmin = role === 'super-admin' || scopes?.includes('aida:superadmin');
      const overrideTenant = isSuperAdmin
        ? (request.headers['x-seer-override-tenant'] as string | undefined)
        : undefined;
      const effectiveTenant = overrideTenant ?? activeTenantAlias ?? 'default';
      const qs = new URLSearchParams(request.query as Record<string, string>);

      try {
        const upstream = await fetch(`${LINEAGE_API_URL}/graphql?${qs}`, {
          headers: {
            'X-Seer-Role':         role,
            'X-Seer-User':         username,
            'X-Seer-Tenant-Alias': effectiveTenant,
          },
        });

        const data = await upstream.json();
        return reply.status(upstream.status).send(data);

      } catch {
        return reply.status(503).send({ errors: [{ message: 'Lineage API unavailable' }] });
      }
    },

    // WebSocket upgrade — graphql-transport-ws proxy
    wsHandler: (socket, request) => {
      const { username, role, scopes, activeTenantAlias } = request.user;
      const isSuperAdmin = role === 'super-admin' || scopes?.includes('aida:superadmin');
      const overrideTenant = isSuperAdmin
        ? (request.headers['x-seer-override-tenant'] as string | undefined)
        : undefined;
      const effectiveTenant = overrideTenant ?? activeTenantAlias ?? 'default';
      const wsUrl = `${toWsUrl(LINEAGE_API_URL)}/graphql`;

      const upstream = new WebSocket(wsUrl, ['graphql-transport-ws'], {
        headers: {
          'X-Seer-Role':         role,
          'X-Seer-User':         username,
          'X-Seer-Tenant-Alias': effectiveTenant,
        },
      });

      // browser → SHUTTLE
      socket.on('message', (data) => {
        if (upstream.readyState === WebSocket.OPEN) {
          upstream.send(data as Buffer);
        }
      });

      // SHUTTLE → browser
      upstream.on('message', (data) => {
        if (socket.readyState === WebSocket.OPEN) {
          socket.send(data as Buffer);
        }
      });

      // Lifecycle
      socket.on('close', (code, reason) => upstream.close(code, reason));
      upstream.on('close', (code, reason) => {
        if (socket.readyState === WebSocket.OPEN) socket.close(code, reason);
      });

      socket.on('error', (err) => {
        app.log.warn(err, 'graphql-ws: browser socket error');
        upstream.terminate();
      });
      upstream.on('error', (err) => {
        app.log.warn(err, 'graphql-ws: upstream error');
        if (socket.readyState === WebSocket.OPEN) {
          socket.send(JSON.stringify({
            type: 'connection_error',
            payload: { message: 'Upstream unavailable' },
          }));
          socket.close(1011, 'upstream error');
        }
      });
    },
  });
};
