import type { FastifyPluginAsync } from 'fastify';
import { verifyUser } from '../users';
import { config } from '../config';

// ── In-memory rate limiter for /auth/login ────────────────────────────────────
// 5 attempts per IP within a 15-minute window (production).
// In dev mode: 50 attempts / 1 min — lenient enough not to block development.
const IS_PROD      = process.env.NODE_ENV === 'production';
const RATE_MAX     = IS_PROD ? 5  : 50;
const RATE_WINDOW  = IS_PROD ? 15 * 60 * 1000 : 60 * 1000;
const loginAttempts = new Map<string, { count: number; resetAt: number }>();

function isRateLimited(ip: string): boolean {
  const now   = Date.now();
  const entry = loginAttempts.get(ip);
  if (!entry || now > entry.resetAt) {
    loginAttempts.set(ip, { count: 1, resetAt: now + RATE_WINDOW });
    return false;
  }
  entry.count += 1;
  return entry.count > RATE_MAX;
}

// Sweep stale entries every 5 minutes to prevent memory leaks.
setInterval(() => {
  const now = Date.now();
  for (const [ip, entry] of loginAttempts) {
    if (now > entry.resetAt) loginAttempts.delete(ip);
  }
}, 5 * 60 * 1000).unref();

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
      // ── Rate limit check ────────────────────────────────────────────────
      const ip = request.ip;
      if (isRateLimited(ip)) {
        return reply.status(429).send({
          error: 'Too many login attempts. Try again in 15 minutes.',
        });
      }

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

  // ── POST /auth/refresh ──────────────────────────────────────────────────────
  // Silent token renewal: if the current JWT is still valid, issue a fresh one.
  // The frontend calls this periodically before the 8h token expires.
  app.post(
    '/refresh',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const { sub, username, role } = request.user;
      const token = app.jwt.sign(
        { sub, username, role },
        { expiresIn: config.jwtExpiry },
      );
      reply.setCookie('token', token, COOKIE_OPTS);
      return { id: sub, username, role };
    },
  );

  // ── POST /auth/logout ───────────────────────────────────────────────────────
  app.post('/logout', async (_request, reply) => {
    reply.clearCookie('token', { path: '/' });
    return { ok: true };
  });
};
