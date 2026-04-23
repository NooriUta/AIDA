import './types'; // load Fastify request augmentation

import Fastify          from 'fastify';
import cookie           from '@fastify/cookie';
import fastifyWebsocket from '@fastify/websocket';

import { config }         from './config';
import rbacPlugin         from './plugins/rbac';
import { authRoutes }     from './routes/auth';
import { queryRoutes }    from './routes/query';
import { graphqlRoutes }  from './routes/graphql';
import { heimdallRoutes } from './routes/heimdall';
import { databasesRoutes } from './routes/databases';
import { prefsRoutes }    from './routes/prefs';
import { meRoutes }       from './routes/me';
import { adminRoutes }    from './routes/admin';
import { tenantRoutes }   from './admin/tenantRoutes';
import { userLifecycleRoutes } from './admin/userLifecycleRoutes';
import { adminUsersRoutes }    from './admin/usersRoutes';
import { serviceAccountRoutes } from './admin/serviceAccountRoutes';

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
  const allowedOrigins = new Set(
    (typeof config.corsOrigin === 'string' ? config.corsOrigin : '')
      .split(',')
      .map((o) => o.trim())
      .filter(Boolean),
  );

  app.addHook('onRequest', async (request, reply) => {
    const origin = request.headers.origin;
    const allowed = origin ? allowedOrigins.has(origin) : false;
    reply.header('Access-Control-Allow-Origin',      allowed ? origin! : 'null');
    reply.header('Access-Control-Allow-Credentials', 'true');
    reply.header('Access-Control-Allow-Methods',     'GET, POST, PUT, OPTIONS');
    reply.header('Access-Control-Allow-Headers',     'Content-Type, Authorization');
    if (request.method === 'OPTIONS') {
      return reply.status(204).send();
    }
  });

  await app.register(cookie, {
    secret: config.cookieSecret, // sign session cookies
  });

  await app.register(fastifyWebsocket);
  await app.register(rbacPlugin);

  // ── Routes ───────────────────────────────────────────────────────────────────

  await app.register(authRoutes,    { prefix: '/auth'    });
  await app.register(queryRoutes,   { prefix: '/api'     });
  await app.register(graphqlRoutes, { prefix: '/graphql' });
  await app.register(heimdallRoutes);
  await app.register(databasesRoutes);
  await app.register(prefsRoutes,   { prefix: '/prefs'   });
  await app.register(meRoutes);   // MTN-63 /me/*
  await app.register(adminRoutes);
  await app.register(tenantRoutes);
  await app.register(userLifecycleRoutes);  // MTN-61
  await app.register(adminUsersRoutes);       // Round 5 Tier C
  await app.register(serviceAccountRoutes, { prefix: '/api/admin' }); // MTN-34

  // ── Health ───────────────────────────────────────────────────────────────────

  app.get('/health', async () => ({ ok: true, ts: new Date().toISOString() }));

  // ── Listen ───────────────────────────────────────────────────────────────────

  await app.listen({ port: config.port, host: '0.0.0.0' });
}

start().catch((err: unknown) => {
  console.error(err);
  process.exit(1);
});
