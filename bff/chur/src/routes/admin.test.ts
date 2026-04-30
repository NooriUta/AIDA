/**
 * Admin routes RBAC integration tests (R4.8).
 */
import { describe, it, expect, vi, beforeEach, beforeAll } from 'vitest';
import Fastify from 'fastify';
import cookie from '@fastify/cookie';
import { adminRoutes } from './admin';
import rbacPlugin from '../plugins/rbac';
import * as sessions from '../sessions';
import { InMemorySessionStore } from '../store/InMemorySessionStore';

// Use in-memory store so tests don't need a live FRIGG
beforeAll(() => { sessions._setStoreForTesting(new InMemorySessionStore()); });

// ── Mock keycloakAdmin.ts ─────────────────────────────────────────────────────
vi.mock('../keycloakAdmin', () => ({
  listUsers:              vi.fn().mockResolvedValue([{ id: 'u1', name: 'alice', role: 'viewer', active: true }]),
  getUser:                vi.fn().mockResolvedValue({ id: 'u1', name: 'alice', role: 'viewer' }),
  inviteUser:             vi.fn().mockResolvedValue(undefined),
  setUserRole:            vi.fn().mockResolvedValue(undefined),
  setUserEnabled:         vi.fn().mockResolvedValue(undefined),
  getUserAttributes:      vi.fn().mockResolvedValue({}),
  setUserAttributes:      vi.fn().mockResolvedValue(undefined),
  // MTN: tenant picker — KC Organizations API (returns empty → falls back to own tenant)
  listAllOrganizations:   vi.fn().mockResolvedValue([]),
  getUserOrganizations:   vi.fn().mockResolvedValue([]),
}));

// ── App factory ───────────────────────────────────────────────────────────────
async function buildApp() {
  const app = Fastify({ logger: false });
  await app.register(cookie);
  await app.register(rbacPlugin);
  await app.register(adminRoutes);
  await app.ready();
  return app;
}

type App = Awaited<ReturnType<typeof buildApp>>;

// ── Session helpers ───────────────────────────────────────────────────────────

async function makeSid(role: string, scopes: string[], tenantAlias = 'default'): Promise<string> {
  const cookie = await sessions.createSession('tok', 'rt', 3600, 'uid-1', 'testuser', role as any, scopes);
  await sessions.updateSession(cookie, { activeTenantAlias: tenantAlias });
  return cookie;
}

// ── GET /admin/tenants ────────────────────────────────────────────────────────
// MTN: tenant picker is open to ALL authenticated users (not just admins) so
// that the UI can show a switcher to any multi-org KC member.

describe('GET /admin/tenants', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); });

  it('401 — no session', async () => {
    const res = await app.inject({ method: 'GET', url: '/admin/tenants' });
    expect(res.statusCode).toBe(401);
  });

  it('200 — viewer gets own tenant (KC orgs empty → fallback)', async () => {
    const sid = await makeSid('viewer', ['seer:read']);
    const res = await app.inject({ method: 'GET', url: '/admin/tenants', cookies: { sid } });
    expect(res.statusCode).toBe(200);
    const body = res.json() as { id: string }[];
    expect(Array.isArray(body)).toBe(true);
    expect(body[0].id).toBe('default');
  });

  it('200 — local-admin gets own tenant (KC orgs empty → fallback)', async () => {
    const sid = await makeSid('local-admin', ['seer:read', 'aida:tenant:admin']);
    const res = await app.inject({ method: 'GET', url: '/admin/tenants', cookies: { sid } });
    expect(res.statusCode).toBe(200);
    const body = res.json() as { id: string }[];
    expect(Array.isArray(body)).toBe(true);
    expect(body[0].id).toBe('default');
  });

  it('200 — admin with aida:admin scope gets all tenants', async () => {
    const sid = await makeSid('admin', ['seer:read', 'aida:admin', 'aida:tenant:admin']);
    const res = await app.inject({ method: 'GET', url: '/admin/tenants', cookies: { sid } });
    expect(res.statusCode).toBe(200);
    const body = res.json() as { id: string }[];
    expect(Array.isArray(body)).toBe(true);
    // Admin sees all FRIGG tenants (or fallback 'default' if FRIGG unavailable in CI)
    expect(body.length).toBeGreaterThanOrEqual(1);
    const ids = body.map((t: { id: string }) => t.id);
    expect(ids).toContain('default');
  });
});

// ── GET /admin/tenants/:tenantId/users ────────────────────────────────────────

describe('GET /admin/tenants/:tenantId/users', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); });

  it('401 — no session', async () => {
    const res = await app.inject({ method: 'GET', url: '/admin/tenants/default/users' });
    expect(res.statusCode).toBe(401);
  });

  it('403 — viewer missing aida:tenant:admin', async () => {
    const sid = await makeSid('viewer', ['seer:read']);
    const res = await app.inject({ method: 'GET', url: '/admin/tenants/default/users', cookies: { sid } });
    expect(res.statusCode).toBe(403);
    expect(res.json().requiredAnyOf).toContain('aida:tenant:admin');
  });

  it('200 — local-admin with aida:tenant:admin scope', async () => {
    const sid = await makeSid('local-admin', ['seer:read', 'aida:tenant:admin']);
    const res = await app.inject({ method: 'GET', url: '/admin/tenants/default/users', cookies: { sid } });
    expect(res.statusCode).toBe(200);
    expect(Array.isArray(res.json())).toBe(true);
  });

  it('200 — admin also passes (has tenant:admin in scopes)', async () => {
    const sid = await makeSid('admin', ['seer:read', 'aida:admin', 'aida:tenant:admin']);
    const res = await app.inject({ method: 'GET', url: '/admin/tenants/default/users', cookies: { sid } });
    expect(res.statusCode).toBe(200);
  });
});

// ── POST /admin/tenants/:tenantId/users/invite ────────────────────────────────

describe('POST /admin/tenants/:tenantId/users/invite', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); });

  it('403 — local-admin cannot assign elevated role "admin"', async () => {
    const sid = await makeSid('local-admin', ['seer:read', 'aida:tenant:admin']);
    const res = await app.inject({
      method: 'POST', url: '/admin/tenants/default/users/invite',
      cookies: { sid },
      payload: { email: 'bob@example.com', role: 'admin' },
    });
    expect(res.statusCode).toBe(403);
    expect(res.json().error).toBe('cannot_assign_elevated_role');
  });

  it('403 — local-admin cannot assign "super-admin"', async () => {
    const sid = await makeSid('local-admin', ['seer:read', 'aida:tenant:admin']);
    const res = await app.inject({
      method: 'POST', url: '/admin/tenants/default/users/invite',
      cookies: { sid },
      payload: { email: 'bob@example.com', role: 'super-admin' },
    });
    expect(res.statusCode).toBe(403);
  });

  it('202 — local-admin invites with non-elevated role "viewer"', async () => {
    const sid = await makeSid('local-admin', ['seer:read', 'aida:tenant:admin']);
    const res = await app.inject({
      method: 'POST', url: '/admin/tenants/default/users/invite',
      cookies: { sid },
      payload: { email: 'carol@example.com', role: 'viewer' },
    });
    expect(res.statusCode).toBe(202);
    expect(res.json().ok).toBe(true);
  });

  it('202 — admin (aida:admin scope) can invite with elevated role', async () => {
    const sid = await makeSid('admin', ['seer:read', 'aida:admin', 'aida:tenant:admin']);
    const res = await app.inject({
      method: 'POST', url: '/admin/tenants/default/users/invite',
      cookies: { sid },
      payload: { email: 'dave@example.com', role: 'admin' },
    });
    expect(res.statusCode).toBe(202);
  });
});

// ── PUT /admin/tenants/:tenantId/users/:userId/role ───────────────────────────

describe('PUT /admin/tenants/:tenantId/users/:userId/role', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); });

  it('403 — local-admin cannot assign elevated role to existing user', async () => {
    const sid = await makeSid('local-admin', ['seer:read', 'aida:tenant:admin']);
    const res = await app.inject({
      method: 'PUT', url: '/admin/tenants/default/users/u1/role',
      cookies: { sid },
      payload: { role: 'admin' },
    });
    expect(res.statusCode).toBe(403);
    expect(res.json().error).toBe('insufficient_privileges');
  });

  it('200 — local-admin assigns non-elevated role', async () => {
    const sid = await makeSid('local-admin', ['seer:read', 'aida:tenant:admin']);
    const res = await app.inject({
      method: 'PUT', url: '/admin/tenants/default/users/u1/role',
      cookies: { sid },
      payload: { role: 'editor' },
    });
    expect(res.statusCode).toBe(200);
  });

  it('200 — tenant-owner can assign elevated role', async () => {
    const sid = await makeSid('tenant-owner', ['seer:read', 'aida:tenant:admin', 'aida:tenant:owner']);
    const res = await app.inject({
      method: 'PUT', url: '/admin/tenants/default/users/u1/role',
      cookies: { sid },
      payload: { role: 'admin' },
    });
    expect(res.statusCode).toBe(200);
  });
});

// ── Self-service /admin/me/* ──────────────────────────────────────────────────

describe('GET /admin/me/profile', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); });

  it('401 — no session', async () => {
    const res = await app.inject({ method: 'GET', url: '/admin/me/profile' });
    expect(res.statusCode).toBe(401);
  });

  it('200 — any authenticated user can read own profile', async () => {
    const sid = await makeSid('viewer', ['seer:read']);
    const res = await app.inject({ method: 'GET', url: '/admin/me/profile', cookies: { sid } });
    expect(res.statusCode).toBe(200);
    expect(res.json()).toMatchObject({ title: expect.any(String), dept: expect.any(String), phone: expect.any(String) });
  });
});

describe('GET /admin/me/prefs', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); });

  it('401 — no session', async () => {
    const res = await app.inject({ method: 'GET', url: '/admin/me/prefs' });
    expect(res.statusCode).toBe(401);
  });

  it('200 — returns prefs with defaults for empty KC attributes', async () => {
    const sid = await makeSid('viewer', ['seer:read']);
    const res = await app.inject({ method: 'GET', url: '/admin/me/prefs', cookies: { sid } });
    expect(res.statusCode).toBe(200);
    const body = res.json();
    expect(body.lang).toBe('ru');
    expect(body.theme).toBe('dark');
    expect(body.density).toBe('normal');
  });
});

describe('PUT /admin/me/profile', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); });

  it('200 — viewer can update own profile', async () => {
    const sid = await makeSid('viewer', ['seer:read']);
    const res = await app.inject({
      method: 'PUT', url: '/admin/me/profile',
      cookies: { sid },
      payload: { title: 'Senior Engineer', dept: 'Analytics', phone: '+7 900 000-00-00' },
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().ok).toBe(true);
  });
});

describe('PUT /admin/me/prefs', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); });

  it('200 — viewer can update own prefs', async () => {
    const sid = await makeSid('viewer', ['seer:read']);
    const res = await app.inject({
      method: 'PUT', url: '/admin/me/prefs',
      cookies: { sid },
      payload: { theme: 'light', density: 'compact', lang: 'en' },
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().ok).toBe(true);
  });
});
