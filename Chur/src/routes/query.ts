import type { FastifyPluginAsync } from 'fastify';
import { arcadeCommand, ArcadeError } from '../arcade';

interface QueryBody {
  language: 'sql' | 'cypher' | 'gremlin';
  command:  string;
  params?:  Record<string, unknown>;
}

export const queryRoutes: FastifyPluginAsync = async (app) => {
  // ── POST /api/query ─────────────────────────────────────────────────────────
  app.post<{ Body: QueryBody }>(
    '/query',
    {
      preHandler: [app.authorizeQuery],
      schema: {
        body: {
          type: 'object',
          required: ['language', 'command'],
          properties: {
            language: { type: 'string', enum: ['sql', 'cypher', 'gremlin'] },
            command:  { type: 'string', minLength: 1 },
            params:   { type: 'object' },
          },
        },
      },
    },
    async (request, reply) => {
      const { language, command, params } = request.body;

      try {
        const data = await arcadeCommand(language, command, params);
        return data;
      } catch (err) {
        if (err instanceof ArcadeError) {
          // Pass ArcadeDB errors to the client with their original status
          return reply.status(err.status >= 500 ? 502 : err.status).send({
            error: err.message,
          });
        }
        // Network / timeout errors
        app.log.error(err, 'ArcadeDB unreachable');
        return reply.status(503).send({ error: 'Database unavailable' });
      }
    },
  );
};
