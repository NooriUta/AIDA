import type { FastifyPluginAsync } from 'fastify';
import { config }                   from '../config';

const HEIMDALL_URL = process.env.HEIMDALL_URL ?? 'http://127.0.0.1:9093';

/**
 * SKULD proxy routes — pipeline management + HiL session state.
 *
 * Routes:
 *   GET/POST/PUT/DELETE /skuld/pipelines       → FRIGG :2481 (SavedPipeline CRUD)
 *   GET/POST/PUT/DELETE /skuld/pipelines/:id   → FRIGG :2481 (single pipeline)
 *   GET  /skuld/sessions/:id/state             → HEIMDALL /trace/sessions/:id/state
 *   POST /skuld/sessions/:id/decision          → 501 (upstream TBD — Decision pending)
 *
 * Ref: INTEGRATIONS_MATRIX I46-I48, SPRINT_MIMIR_PREP MP-06.
 */
export const skuldRoutes: FastifyPluginAsync = async (app) => {

  // ── SavedPipeline CRUD collection → FRIGG ─────────────────────────────────
  for (const method of ['GET', 'POST', 'PUT', 'DELETE'] as const) {
    app.route({
      method,
      url:         '/skuld/pipelines',
      preHandler:  [app.authenticate],
      handler: async (request, reply) => {
        const { activeTenantAlias } = request.user;
        try {
          const upstream = await fetch(`${config.friggUrl}/api/skuld/pipelines`, {
            method,
            headers: {
              'Content-Type':        'application/json',
              'X-Seer-Tenant-Alias': activeTenantAlias ?? 'default',
            },
            body: method !== 'GET' && method !== 'DELETE'
              ? JSON.stringify(request.body)
              : undefined,
          });
          const text = await upstream.text();
          return reply.status(upstream.status).send(text ? JSON.parse(text) : null);
        } catch (err) {
          request.log.error({ err }, 'FRIGG pipelines upstream unreachable');
          return reply.status(503).send({ error: 'pipeline store unavailable' });
        }
      },
    });
  }

  // ── SavedPipeline CRUD single → FRIGG ─────────────────────────────────────
  for (const method of ['GET', 'PUT', 'DELETE'] as const) {
    app.route({
      method,
      url:        '/skuld/pipelines/:id',
      preHandler: [app.authenticate],
      handler: async (request, reply) => {
        const { id } = request.params as { id: string };
        const { activeTenantAlias } = request.user;
        try {
          const upstream = await fetch(
            `${config.friggUrl}/api/skuld/pipelines/${encodeURIComponent(id)}`,
            {
              method,
              headers: {
                'Content-Type':        'application/json',
                'X-Seer-Tenant-Alias': activeTenantAlias ?? 'default',
              },
              body: method === 'PUT' ? JSON.stringify(request.body) : undefined,
            },
          );
          const text = await upstream.text();
          return reply.status(upstream.status).send(text ? JSON.parse(text) : null);
        } catch {
          return reply.status(503).send({ error: 'pipeline store unavailable' });
        }
      },
    });
  }

  // ── GET /skuld/sessions/:id/state → HEIMDALL /trace/sessions/:id/state ────
  app.get(
    '/skuld/sessions/:id/state',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const { id } = request.params as { id: string };
      const { role } = request.user;
      try {
        const upstream = await fetch(
          `${HEIMDALL_URL}/trace/sessions/${encodeURIComponent(id)}/state`,
          { headers: { 'X-Seer-Role': role } },
        );
        const text = await upstream.text();
        return reply.status(upstream.status).send(text ? JSON.parse(text) : null);
      } catch {
        return reply.status(503).send({ error: 'HEIMDALL service unavailable' });
      }
    },
  );

  // ── POST /skuld/sessions/:id/decision — upstream TBD ──────────────────────
  // TODO: wire to correct upstream once SKULD decision-endpoint is decided.
  // Tracked: SPRINT_MIMIR_PREP MP-06 — Decision pending as of 2026-05-02.
  app.post(
    '/skuld/sessions/:id/decision',
    { preHandler: [app.authenticate] },
    async (_request, reply) => reply.status(501).send({
      error:  'not implemented',
      detail: 'SKULD decision upstream not yet decided. Tracked in MP-06 backlog.',
    }),
  );
};
