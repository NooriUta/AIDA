import type { FastifyPluginAsync } from 'fastify';

const HEIMDALL_ORIGIN = process.env.HEIMDALL_URL ?? 'http://127.0.0.1:9093';

/**
 * User UI preferences — proxied to heimdall-backend → FRIGG.
 * Accessible to any authenticated user (own sub only — enforced by using session sub).
 *
 *   GET /prefs     → GET /api/prefs/{session.sub}  (returns defaults if first login)
 *   PUT /prefs     → PUT /api/prefs/{session.sub}  (upsert)
 *
 * The sub comes from the Chur session, never from the request body/query,
 * so a user cannot read or overwrite another user's prefs.
 */
export const prefsRoutes: FastifyPluginAsync = async (app) => {

  // ── GET /prefs — load server prefs for the current user ─────────────────
  app.get(
    '/',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const sub = encodeURIComponent(request.user.sub);
      try {
        const res = await fetch(`${HEIMDALL_ORIGIN}/api/prefs/${sub}`);
        return reply.status(res.status).send(await res.json());
      } catch {
        return reply.status(503).send({ error: 'HEIMDALL_UNREACHABLE' });
      }
    },
  );

  // ── PUT /prefs — save server prefs for the current user ─────────────────
  app.put(
    '/',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const sub = encodeURIComponent(request.user.sub);
      try {
        const res = await fetch(`${HEIMDALL_ORIGIN}/api/prefs/${sub}`, {
          method:  'PUT',
          headers: { 'Content-Type': 'application/json' },
          body:    JSON.stringify(request.body),
        });
        return reply.status(res.status).send(await res.json());
      } catch {
        return reply.status(503).send({ error: 'HEIMDALL_UNREACHABLE' });
      }
    },
  );
};
