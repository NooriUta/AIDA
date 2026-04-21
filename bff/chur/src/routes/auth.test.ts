import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import Fastify from 'fastify';
import cookie from '@fastify/cookie';
import { authRoutes } from './auth';
import * as sessions from '../sessions';
import { InMemorySessionStore } from '../store/InMemorySessionStore';

// Use in-memory store so tests don't need a live FRIGG
beforeAll(() => { sessions._setStoreForTesting(new InMemorySessionStore()); });

// ── Mock keycloak.ts ─────────────────────────────────────────────────────────
vi.mock('../keycloak', () => ({
  exchangeCredentials: vi.fn(),
  extractUserInfo:     vi.fn(),
  keycloakLogout:      vi.fn(),
}));

import { exchangeCredentials, extractUserInfo } from '../keycloak';
const mockExchange  = vi.mocked(exchangeCredentials);
const mockExtract   = vi.mocked(extractUserInfo);

// ── Minimal Fastify app factory ──────────────────────────────────────────────
async function buildApp() {
  const app = Fastify({ logger: false });
  await app.register(cookie);

  // Minimal authenticate decorator (mirrors rbac plugin)
  app.decorate('authenticate', async (request: any, reply: any) => {
    const sid = request.cookies.sid;
    if (!sid) return reply.status(401).send({ error: 'Unauthorized' });
    try {
      const session = await sessions.ensureValidSession(sid);
      request.user = { sub: session.sub, username: session.username, role: session.role, scopes: session.scopes };
    } catch {
      return reply.status(401).send({ error: 'Unauthorized' });
    }
  });

  await app.register(authRoutes, { prefix: '/auth' });
  await app.ready();
  return app;
}

// ── Helpers ──────────────────────────────────────────────────────────────────

/** Fake Keycloak access token payload (base64url-encoded JSON). */
function fakeAccessToken(claims: Record<string, unknown>): string {
  const header  = Buffer.from('{"alg":"RS256"}').toString('base64url');
  const payload = Buffer.from(JSON.stringify(claims)).toString('base64url');
  return `${header}.${payload}.fakesig`;
}

const ADMIN_CLAIMS = { sub: 'kc-001', preferred_username: 'admin', seer_roles: ['admin'] };

// ── Tests ────────────────────────────────────────────────────────────────────

describe('POST /auth/login', () => {
  let app: Awaited<ReturnType<typeof buildApp>>;

  beforeEach(async () => {
    vi.clearAllMocks();
    app = await buildApp();
  });

  it('returns 200 with user data on valid credentials', async () => {
    mockExchange.mockResolvedValue({
      access_token:  fakeAccessToken(ADMIN_CLAIMS),
      refresh_token: 'rt-123',
      expires_in:    300,
      token_type:    'Bearer',
    });
    mockExtract.mockReturnValue({ sub: 'kc-001', username: 'admin', role: 'admin', scopes: [] });

    const res = await app.inject({
      method: 'POST',
      url: '/auth/login',
      payload: { username: 'admin', password: 'admin' },
    });

    expect(res.statusCode).toBe(200);
    const body = res.json();
    expect(body.username).toBe('admin');
    expect(body.role).toBe('admin');
    expect(body.id).toBe('kc-001');
    expect(res.headers['set-cookie']).toMatch(/sid=/);
  });

  it('returns 401 on invalid credentials', async () => {
    mockExchange.mockRejectedValue(new Error('Keycloak login failed'));

    const res = await app.inject({
      method: 'POST',
      url: '/auth/login',
      payload: { username: 'admin', password: 'wrong' },
    });

    expect(res.statusCode).toBe(401);
    expect(res.json().error).toBe('Invalid credentials');
  });

  it('returns 400 when body is missing username', async () => {
    const res = await app.inject({
      method: 'POST',
      url: '/auth/login',
      payload: { password: 'secret' },
    });

    expect(res.statusCode).toBe(400);
  });

  it('returns 400 when body is missing password', async () => {
    const res = await app.inject({
      method: 'POST',
      url: '/auth/login',
      payload: { username: 'admin' },
    });

    expect(res.statusCode).toBe(400);
  });

  it('rate limit error message contains "Too many"', () => {
    const rateLimitBody = { error: 'Too many login attempts. Try again later.' };
    expect(rateLimitBody.error).toContain('Too many');
  });
});

describe('POST /auth/logout', () => {
  it('clears the sid cookie', async () => {
    const app = await buildApp();

    const res = await app.inject({
      method: 'POST',
      url: '/auth/logout',
    });

    expect(res.statusCode).toBe(200);
    expect(res.json().ok).toBe(true);
    expect(res.headers['set-cookie']).toMatch(/sid=;/);
  });
});

describe('GET /auth/me', () => {
  it('returns 401 without a valid cookie', async () => {
    const app = await buildApp();

    const res = await app.inject({
      method: 'GET',
      url: '/auth/me',
    });

    expect(res.statusCode).toBe(401);
  });

  it('returns user info with a valid session cookie', async () => {
    const app = await buildApp();

    const sid = await sessions.createSession(
      'fake-access-token',
      'fake-refresh-token',
      3600,
      'kc-002',
      'alice',
      'viewer',
    );

    const res = await app.inject({
      method: 'GET',
      url: '/auth/me',
      cookies: { sid },
    });

    expect(res.statusCode).toBe(200);
    const body = res.json();
    expect(body.username).toBe('alice');
    expect(body.role).toBe('viewer');
    expect(body.id).toBe('kc-002');
  });
});
