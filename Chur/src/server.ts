import './types'; // load JWT payload type augmentation

import Fastify from 'fastify';
import cookie  from '@fastify/cookie';
import jwt     from '@fastify/jwt';

import { config }         from './config';
import rbacPlugin         from './plugins/rbac';
import { authRoutes }     from './routes/auth';
import { queryRoutes }    from './routes/query';
import { graphqlRoutes }  from './routes/graphql';

async function start(): Promise<void> {
  const app = Fastify({
    logger: {
      level: process.env.LOG_LEVEL ?? 'info',
      transport:
        process.env.NODE_ENV !== 'production'
          ? { target: 'pino-pretty', options: { colorize: true } }
          : undefined,
    },
  });

  // ── Plugins ─────────────────────────────────────────────────────────────────

  // Manual CORS (avoids @fastify/cors Fastify-version mismatch in dev)
  // corsOrigin is a string (single origin) or comma-separated list in env.
  const allowedOrigins = new Set(
    (typeof config.corsOrigin === 'string' ? config.corsOrigin : '')
      .split(',')
      .map((o) => o.trim())
      .filter(Boolean),
  );

  app.addHook('onRequest', async (request, reply) => {
    const origin = request.headers.origin;
    // Only reflect the origin if it's in the allow-list; otherwise block.
    const allowed = origin ? allowedOrigins.has(origin) : false;
    reply.header('Access-Control-Allow-Origin',      allowed ? origin! : 'null');
    reply.header('Access-Control-Allow-Credentials', 'true');
    reply.header('Access-Control-Allow-Methods',     'GET, POST, OPTIONS');
    reply.header('Access-Control-Allow-Headers',     'Content-Type, Authorization');
    if (request.method === 'OPTIONS') {
      return reply.status(204).send();
    }
  });

  await app.register(cookie);

  await app.register(jwt, {
    secret: config.jwtSecret,
    cookie: { cookieName: 'token', signed: false },
  });

  await app.register(rbacPlugin);

  // ── Routes ───────────────────────────────────────────────────────────────────

  await app.register(authRoutes,    { prefix: '/auth'    });
  await app.register(queryRoutes,   { prefix: '/api'     });
  await app.register(graphqlRoutes, { prefix: '/graphql' });

  // ── Health ───────────────────────────────────────────────────────────────────

  app.get('/health', async () => ({ ok: true, ts: new Date().toISOString() }));

  // ── Listen ───────────────────────────────────────────────────────────────────

  await app.listen({ port: config.port, host: '0.0.0.0' });
}

start().catch((err: unknown) => {
  console.error(err);
  process.exit(1);
});
