import type { FastifyPluginAsync } from 'fastify';
import { verifyUser } from '../users';
import { config } from '../config';

const COOKIE_OPTS = {
  httpOnly: true,
  path:     '/',
  // 'lax' allows cross-port requests on localhost (5173 → 3000 in dev).
  // 'strict' blocks the cookie entirely when origin differs from request host.
  sameSite: (process.env.NODE_ENV === 'production' ? 'strict' : 'lax') as 'strict' | 'lax',
  secure:   process.env.NODE_ENV === 'production',
  maxAge:   8 * 60 * 60, // 8 h in seconds
};

export const authRoutes: FastifyPluginAsync = async (app) => {
  // ── POST /auth/login ────────────────────────────────────────────────────────
  app.post<{ Body: { username: string; password: string } }>(
    '/login',
    {
      schema: {
        body: {
          type: 'object',
          required: ['username', 'password'],
          properties: {
            username: { type: 'string', minLength: 1 },
            password: { type: 'string', minLength: 1 },
          },
        },
      },
    },
    async (request, reply) => {
      const { username, password } = request.body;

      const user = await verifyUser(username, password).catch(() => null);
      if (!user) {
        return reply.status(401).send({ error: 'Invalid credentials' });
      }

      const token = app.jwt.sign(
        { sub: user.id, username: user.username, role: user.role },
        { expiresIn: config.jwtExpiry },
      );

      reply.setCookie('token', token, COOKIE_OPTS);
      return { id: user.id, username: user.username, role: user.role };
    },
  );

  // ── GET /auth/me ────────────────────────────────────────────────────────────
  app.get(
    '/me',
    { preHandler: [app.authenticate] },
    async (request) => {
      const { sub, username, role } = request.user;
      return { id: sub, username, role };
    },
  );

  // ── POST /auth/logout ───────────────────────────────────────────────────────
  app.post('/logout', async (_request, reply) => {
    reply.clearCookie('token', { path: '/' });
    return { ok: true };
  });
};
