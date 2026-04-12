import type { FastifyPluginAsync } from 'fastify';

const LINEAGE_API_URL = process.env.LINEAGE_API_URL ?? 'http://localhost:8080';

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
      const { username, role } = request.user;

      try {
        const upstream = await fetch(`${LINEAGE_API_URL}/graphql`, {
          method: 'POST',
          headers: {
            'Content-Type':  'application/json',
            'X-Seer-Role':   role,
            'X-Seer-User':   username,
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

  // ── GET /graphql (introspection / GraphiQL passthrough) ────────────────────
  app.get(
    '/',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const { username, role } = request.user;
      const qs = new URLSearchParams(request.query as Record<string, string>);

      try {
        const upstream = await fetch(`${LINEAGE_API_URL}/graphql?${qs}`, {
          headers: {
            'X-Seer-Role': role,
            'X-Seer-User': username,
          },
        });

        const data = await upstream.json();
        return reply.status(upstream.status).send(data);

      } catch {
        return reply.status(503).send({ errors: [{ message: 'Lineage API unavailable' }] });
      }
    },
  );
};
