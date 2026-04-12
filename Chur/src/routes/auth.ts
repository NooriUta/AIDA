import type { FastifyPluginAsync } from 'fastify';
import { exchangeCredentials, extractUserInfo, keycloakLogout } from '../keycloak';
import { createSession, deleteSession, ensureValidSession } from '../sessions';

// ── In-memory rate limiter for /auth/login ────────────────────────────────────
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

setInterval(() => {
  const now = Date.now();
  for (const [ip, entry] of loginAttempts) {
    if (now > entry.resetAt) loginAttempts.delete(ip);
  }
}, 5 * 60 * 1000).unref();

// ── Cookie config ─────────────────────────────────────────────────────────────
const COOKIE_OPTS = {
  httpOnly: true,
  path:     '/',
  sameSite: (IS_PROD ? 'strict' : 'lax') as 'strict' | 'lax',
  secure:   IS_PROD,
  maxAge:   8 * 60 * 60, // 8 h
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
      const ip = request.ip;
      if (isRateLimited(ip)) {
        return reply.status(429).send({
          error: 'Too many login attempts. Try again later.',
        });
      }

      const { username, password } = request.body;

      let tokens;
      try {
        tokens = await exchangeCredentials(username, password);
      } catch {
        return reply.status(401).send({ error: 'Invalid credentials' });
      }

      // Decode access token payload to extract user info
      const payload = JSON.parse(
        Buffer.from(tokens.access_token.split('.')[1], 'base64url').toString(),
      );
      const userInfo = extractUserInfo(payload);

      const sid = createSession(
        tokens.access_token,
        tokens.refresh_token,
        tokens.expires_in,
        userInfo.sub,
        userInfo.username,
        userInfo.role,
      );

      reply.setCookie('sid', sid, COOKIE_OPTS);
      return { id: userInfo.sub, username: userInfo.username, role: userInfo.role };
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
  // Kept for backward compatibility. With Keycloak, lazy refresh in
  // app.authenticate handles this transparently, but the frontend may still
  // call /refresh explicitly.
  app.post(
    '/refresh',
    { preHandler: [app.authenticate] },
    async (request, reply) => {
      const { sub, username, role } = request.user;
      // Session was already refreshed by authenticate if needed
      return { id: sub, username, role };
    },
  );

  // ── POST /auth/logout ───────────────────────────────────────────────────────
  app.post('/logout', async (request, reply) => {
    const sid = request.cookies.sid;
    if (sid) {
      const session = deleteSession(sid);
      // Invalidate refresh token in Keycloak (fire-and-forget)
      if (session) keycloakLogout(session.refreshToken);
    }
    reply.clearCookie('sid', { path: '/' });
    return { ok: true };
  });
};
