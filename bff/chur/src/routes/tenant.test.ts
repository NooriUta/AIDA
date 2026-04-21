/**
 * CAP-16: Tenant lifecycle integration tests.
 *
 * These tests stub out all external HTTP calls (Keycloak, FRIGG, YGG, Heimdall)
 * and verify the Chur RBAC / routing layer only.
 */
import { describe, it, expect, vi, beforeAll, beforeEach } from 'vitest';
import Fastify from 'fastify';
import cookie from '@fastify/cookie';
import rbacPlugin from '../plugins/rbac';
import { tenantRoutes } from '../admin/tenantRoutes';
import * as sessions from '../sessions';
import { InMemorySessionStore } from '../store/InMemorySessionStore';

// Use in-memory sessions
beforeAll(() => { sessions._setStoreForTesting(new InMemorySessionStore()); });

// ── Mock all external dependencies ────────────────────────────────────────────

vi.mock('../admin/provisioning', () => ({
  validateAlias:     vi.fn((a: string) => a.length >= 4 ? null : 'too short'),
  provisionTenant:   vi.fn().mockResolvedValue({ tenantAlias: 'acme', keycloakOrgId: 'kc-org-1', lastStep: 7 }),
  forceCleanupTenant: vi.fn().mockResolvedValue(undefined),
}));

vi.mock('../keycloakAdmin', () => ({
  listUsers:      vi.fn().mockResolvedValue([]),
  inviteUser:     vi.fn().mockResolvedValue(undefined),
  setUserRole:    vi.fn().mockResolvedValue(undefined),
  setUserEnabled: vi.fn().mockResolvedValue(undefined),
}));

// Stub FRIGG HTTP calls
vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
  ok:   true,
  status: 200,
  text: () => Promise.resolve(''),
  json: () => Promise.resolve({ result: [{ tenantAlias: 'acme', status: 'ACTIVE', configVersion: 1 }] }),
  headers: { get: () => null },
}));

// ── App factory ───────────────────────────────────────────────────────────────

async function buildApp() {
  const app = Fastify({ logger: false });
  await app.register(cookie);
  await app.register(rbacPlugin);
  await app.register(tenantRoutes);
  await app.ready();
  return app;
}

type App = Awaited<ReturnType<typeof buildApp>>;

async function makeSid(scopes: string[]): Promise<string> {
  return sessions.createSession('tok', 'rt', 3600, 'uid', 'admin', 'admin', scopes);
}

// ── Tests ─────────────────────────────────────────────────────────────────────

describe('GET /api/admin/tenants', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); });

  it('401 — no session', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/admin/tenants' });
    expect(res.statusCode).toBe(401);
  });

  it('403 — viewer without aida:admin', async () => {
    const sid = await makeSid(['seer:read']);
    const res = await app.inject({ method: 'GET', url: '/api/admin/tenants', cookies: { sid } });
    expect(res.statusCode).toBe(403);
  });

  it('200 — admin with aida:admin', async () => {
    const sid = await makeSid(['aida:admin']);
    const res = await app.inject({ method: 'GET', url: '/api/admin/tenants', cookies: { sid } });
    expect(res.statusCode).toBe(200);
  });
});

describe('POST /api/admin/tenants — provisioning', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); });

  it('400 — invalid alias', async () => {
    const sid = await makeSid(['aida:admin']);
    const res = await app.inject({
      method:  'POST', url: '/api/admin/tenants',
      cookies: { sid },
      payload: { alias: 'ab' }, // too short → mock returns error
    });
    expect(res.statusCode).toBe(400);
  });

  it('201 — valid alias provisions tenant', async () => {
    const sid = await makeSid(['aida:admin']);
    const res = await app.inject({
      method:  'POST', url: '/api/admin/tenants',
      cookies: { sid },
      payload: { alias: 'acme' },
    });
    expect(res.statusCode).toBe(201);
    expect(res.json().tenantAlias).toBe('acme');
  });
});

describe('DELETE /api/admin/tenants/:alias — suspend', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); });

  it('403 — admin (not superadmin) cannot suspend', async () => {
    const sid = await makeSid(['aida:admin']); // no superadmin
    const res = await app.inject({ method: 'DELETE', url: '/api/admin/tenants/acme', cookies: { sid } });
    expect(res.statusCode).toBe(403);
  });

  it('200 — superadmin can suspend', async () => {
    const sid = await makeSid(['aida:superadmin']);
    const res = await app.inject({ method: 'DELETE', url: '/api/admin/tenants/acme', cookies: { sid } });
    expect(res.statusCode).toBe(200);
    expect(res.json().status).toBe('SUSPENDED');
  });
});

describe('POST /api/admin/tenants/:alias/force-cleanup', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); });

  it('403 — admin without destructive scope', async () => {
    const sid = await makeSid(['aida:admin']);
    const res = await app.inject({
      method:  'POST', url: '/api/admin/tenants/acme/force-cleanup',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(403);
  });

  it('200 — admin with destructive scope purges tenant', async () => {
    const sid = await makeSid(['aida:admin', 'aida:admin:destructive']);
    const res = await app.inject({
      method:  'POST', url: '/api/admin/tenants/acme/force-cleanup',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().ok).toBe(true);
  });
});

describe('POST /api/admin/tenants/:alias/reconnect', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); });

  it('200 — admin broadcasts registry invalidation', async () => {
    const sid = await makeSid(['aida:admin']);
    const res = await app.inject({
      method:  'POST', url: '/api/admin/tenants/acme/reconnect',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().ok).toBe(true);
  });
});
