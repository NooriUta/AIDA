import type { FastifyPluginAsync } from 'fastify';
import { exchangeCredentials, extractUserInfo, keycloakLogout } from '../keycloak';
import { getUser } from '../keycloakAdmin';
import { createSession, deleteSession, ensureValidSession, updateSession } from '../sessions';
import { emitSessionEvent } from '../users/UserSessionEventsEmitter';
import { emitToHeimdall } from '../middleware/heimdallEmit';
import { csrfGuard } from '../middleware/csrfGuard';

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
// COOKIE_SECURE=false lets HTTP deployments work before TLS is configured.
// Defaults to true in production so HTTPS deployments stay secure by default.
const COOKIE_OPTS = {
  httpOnly: true,
  path:     '/',
  sameSite: (IS_PROD ? 'strict' : 'lax') as 'strict' | 'lax',
  secure:   process.env.COOKIE_SECURE === 'true' || (IS_PROD && process.env.COOKIE_SECURE !== 'false'),
  maxAge:   8 * 60 * 60, // 8 h
  // Share session across seer.* and heimdall.* subdomains
  ...(process.env.COOKIE_DOMAIN ? { domain: process.env.COOKIE_DOMAIN } : {}),
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
        emitToHeimdall('AUTH_LOGIN_FAILED', 'WARN', { username, reason: 'invalid_credentials' });
        return reply.status(401).send({ error: 'Invalid credentials' });
      }

      // Decode access token payload to extract user info
      const payload = JSON.parse(
        Buffer.from(tokens.access_token.split('.')[1], 'base64url').toString(),
      );
      const userInfo = extractUserInfo(payload);

      const sid = await createSession(
        tokens.access_token,
        tokens.refresh_token,
        tokens.expires_in,
        userInfo.sub,
        userInfo.username,
        userInfo.role,
        userInfo.scopes,
      );
      // Persist KC identity claims in session for /auth/me enrichment
      await updateSession(sid, {
        email:         userInfo.email,
        firstName:     userInfo.firstName,
        lastName:      userInfo.lastName,
        emailVerified: userInfo.emailVerified,
        // Persist tenant from KC JWT so subsequent /auth/me calls see it.
        // Only set when KC actually emits the claim; undefined keeps session default.
        ...(userInfo.tenantAlias ? { activeTenantAlias: userInfo.tenantAlias } : {}),
      });

      const activeTenantAlias = userInfo.tenantAlias ?? 'default';
      reply.setCookie('sid', sid, COOKIE_OPTS);
      emitToHeimdall('AUTH_LOGIN_SUCCESS', 'INFO', { username: userInfo.username, role: userInfo.role }, sid);
      void emitSessionEvent({
        userId:     userInfo.sub,
        sessionId:  sid,
        eventType:  'login',
        ipAddress:  request.ip,
        userAgent:  String(request.headers['user-agent'] ?? ''),
        tenantAlias: activeTenantAlias,
        result:     'success',
      });
      return { id: userInfo.sub, username: userInfo.username, role: userInfo.role,
               scopes: userInfo.scopes, email: userInfo.email,
               firstName: userInfo.firstName, lastName: userInfo.lastName,
               emailVerified: userInfo.emailVerified,
               activeTenantAlias };
    },
  );

  // ── GET /auth/me ────────────────────────────────────────────────────────────
  app.get(
    '/me',
    { preHandler: [app.authenticate] },
    async (request) => {
      const { sub, username, role, scopes, email, firstName, lastName,
              emailVerified, activeTenantAlias } = request.user;
      // Fallback: firstName/lastName absent in session (JWT didn't carry given_name/family_name
      // because KC user has no name set, or scopes changed since last login).
      // Fetch fresh from KC Admin API so profile page always shows real data.
      let fn = firstName, ln = lastName;
      if (!fn && !ln) {
        const kcUser = await getUser(sub).catch(() => null);
        fn = kcUser?.firstName ?? undefined;
        ln = kcUser?.lastName ?? undefined;
      }
      return { id: sub, username, role, scopes, email, firstName: fn, lastName: ln,
               emailVerified, activeTenantAlias: activeTenantAlias ?? 'default' };
    },
  );

  // ── POST /auth/refresh ──────────────────────────────────────────────────────
  app.post(
    '/refresh',
    { preHandler: [app.authenticate] },
    async (request) => {
      const { sub, username, role, scopes, email, firstName, lastName,
              emailVerified, activeTenantAlias } = request.user;
      return { id: sub, username, role, scopes, email, firstName, lastName,
               emailVerified, activeTenantAlias: activeTenantAlias ?? 'default' };
    },
  );

  // ── POST /auth/logout ───────────────────────────────────────────────────────
  // ── PATCH /auth/me/tenant — MTN-13: active-tenant switch ───────────────────
  app.patch<{ Body: { tenantAlias: string } }>(
    '/me/tenant',
    { preHandler: [app.authenticate, csrfGuard] },
    async (request, reply) => {
      const { tenantAlias } = request.body ?? {};
      if (!tenantAlias || typeof tenantAlias !== 'string') {
        return reply.status(400).send({ error: 'tenantAlias required' });
      }
      const sid = request.cookies.sid;
      if (!sid) return reply.status(401).send({ error: 'no session' });
      try {
        await updateSession(sid, { activeTenantAlias: tenantAlias });
      } catch (e) {
        return reply.status(404).send({ error: (e as Error).message });
      }
      void emitSessionEvent({
        userId:      request.user.sub,
        sessionId:   sid,
        eventType:   'tenant_switch',
        tenantAlias,
        ipAddress:   request.ip,
        userAgent:   String(request.headers['user-agent'] ?? ''),
        result:      'success',
      });
      return { ok: true, activeTenantAlias: tenantAlias };
    },
  );

  app.post('/logout', async (request, reply) => {
    const sid = request.cookies.sid;
    if (sid) {
      const session = await deleteSession(sid);
      // Invalidate refresh token in Keycloak (fire-and-forget)
      if (session) {
        keycloakLogout(session.refreshToken);
        emitToHeimdall('AUTH_LOGOUT', 'INFO', { username: session.username }, sid);
        // MTN-64: emit logout event
        void emitSessionEvent({
          userId:    session.sub,
          sessionId: sid,
          eventType: 'logout',
          ipAddress: request.ip,
          userAgent: String(request.headers['user-agent'] ?? ''),
          result:    'success',
        });
      }
    }
    reply.clearCookie('sid', { path: '/' });
    return { ok: true };
  });
};
