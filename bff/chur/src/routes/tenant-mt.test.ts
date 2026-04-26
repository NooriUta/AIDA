/**
 * MT-R1-R5 automated coverage: harvest, resume-provisioning, role-change-signal,
 * config PUT/GET, feature-flags, member role/enabled update, tenant isolation.
 *
 * All external calls (FRIGG, Dali, KC) are stubbed.
 */
import { describe, it, expect, vi, beforeAll, beforeEach, afterEach } from 'vitest';
import Fastify from 'fastify';
import cookie from '@fastify/cookie';
import rbacPlugin from '../plugins/rbac';
import { tenantRoutes } from '../admin/tenantRoutes';
import * as sessions from '../sessions';
import { InMemorySessionStore } from '../store/InMemorySessionStore';

beforeAll(() => { sessions._setStoreForTesting(new InMemorySessionStore()); });

// ── Mock provisioning (resume) ────────────────────────────────────────────────
vi.mock('../admin/provisioning', () => ({
  validateAlias:     vi.fn((a: string) => a.length >= 4 ? null : 'too short'),
  provisionTenant:   vi.fn().mockResolvedValue({ tenantAlias: 'acme', keycloakOrgId: 'kc-1', lastStep: 7 }),
  resumeProvisioning: vi.fn().mockResolvedValue({ tenantAlias: 'acme', keycloakOrgId: 'kc-1', lastStep: 7 }),
  forceCleanupTenant: vi.fn().mockResolvedValue(undefined),
}));

vi.mock('../keycloakAdmin', () => ({
  listUsers:        vi.fn().mockResolvedValue([]),
  inviteUser:       vi.fn().mockResolvedValue(undefined),
  setUserRole:      vi.fn().mockResolvedValue(undefined),
  setUserEnabled:   vi.fn().mockResolvedValue(undefined),
  listOrgMembers:   vi.fn().mockResolvedValue([
    { id: 'u1', username: 'alice', email: 'alice@acme.io', role: 'viewer' },
  ]),
  inviteUserToOrg:  vi.fn().mockResolvedValue(undefined),
  removeOrgMember:  vi.fn().mockResolvedValue(undefined),
}));

// ── CSRF header ───────────────────────────────────────────────────────────────
const CSRF = { origin: 'http://localhost:5173' } as const;

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

async function sid(role: 'admin' | 'super-admin' | 'viewer', extra: string[] = []): Promise<string> {
  const scopes: string[] = role === 'super-admin'
    ? ['aida:superadmin', 'aida:admin']
    : role === 'admin'
    ? ['aida:admin']
    : ['seer:read'];
  return sessions.createSession('tok', 'rt', 3600, 'uid', role, role, [...scopes, ...extra]);
}

// ── Stateful FRIGG fetch stub ─────────────────────────────────────────────────
// CAS pattern: UPDATE is call #1 (returns empty result), SELECT configVersion is
// call #2 and must return expectedConfigVersion+1 to signal success.
// We start configVersion at 2 so that for expectedConfigVersion=1 the check
// `current !== expected+1` → `2 !== 2` → false → success.
function makeFetchStub(opts: { orgId?: string; configVersion?: number; status?: string } = {}) {
  const cv      = opts.configVersion ?? 2;   // returned on SELECT configVersion (= expected+1)
  const orgId   = opts.orgId   ?? 'kc-org-default';
  const status  = opts.status  ?? 'ACTIVE';

  return vi.fn((_url: string, init?: RequestInit) => {
    const body = typeof init?.body === 'string' ? init.body : '';

    // Dali harvest proxy — match before generic /command/ check
    if (_url.includes('/api/sessions/harvest')) {
      return Promise.resolve({
        ok: true, status: 200, text: () => Promise.resolve(''),
        headers: { get: () => null },
        json: () => Promise.resolve({ harvestId: 'h-123', tenantAlias: 'acme' }),
      });
    }

    if (body.includes('SELECT configVersion')) {
      // Post-UPDATE verify: return cv (= expected+1) to signal CAS success
      return Promise.resolve({
        ok: true, status: 200, text: () => Promise.resolve(''),
        headers: { get: () => null },
        json: () => Promise.resolve({ result: [{ configVersion: cv }] }),
      });
    }

    // All other FRIGG commands (UPDATE, SELECT tenant, SELECT members…)
    return Promise.resolve({
      ok: true, status: 200, text: () => Promise.resolve(''),
      headers: { get: () => null },
      json: () => Promise.resolve({
        result: [{ tenantAlias: 'acme', status, configVersion: cv, keycloakOrgId: orgId,
                   featureFlags: '{"betaHarvest":true}', lastRoleChangeAt: 0 }],
      }),
    });
  });
}

// ── Helper to create a session with activeTenantAlias ────────────────────────
async function sidForTenant(alias: string): Promise<string> {
  const cookie = await sessions.createSession(
    'tok', 'rt', 3600, 'uid', 'admin', 'admin',
    ['aida:tenant:admin', `aida:tenant:${alias}`],
  );
  await sessions.updateSession(cookie, { activeTenantAlias: alias });
  return cookie;
}

// ─────────────────────────────────────────────────────────────────────────────

describe('POST /api/admin/tenants/:alias/harvest', () => {
  let app: App;
  beforeEach(async () => {
    app = await buildApp();
    vi.stubGlobal('fetch', makeFetchStub());
  });
  afterEach(() => vi.unstubAllGlobals());

  it('MT-07 · 403 — viewer cannot trigger harvest', async () => {
    const cookie = await sid('viewer');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/harvest',
      cookies: { sid: cookie }, headers: CSRF,
    });
    expect(res.statusCode).toBe(403);
  });

  it('MT-07 · 403 — missing CSRF header', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/harvest',
      cookies: { sid: cookie },
    });
    expect(res.statusCode).toBe(403);
  });

  it('MT-07 · 200 — admin triggers harvest, returns harvestId', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/harvest',
      cookies: { sid: cookie }, headers: CSRF,
    });
    expect(res.statusCode).toBe(200);
    const body = res.json<{ harvestId: string; tenantAlias: string }>();
    expect(body.harvestId).toBe('h-123');
    expect(body.tenantAlias).toBe('acme');
  });

  it('MT-07 · 503 — Dali unreachable returns dali_unreachable error', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new Error('ECONNREFUSED')));
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/harvest',
      cookies: { sid: cookie }, headers: CSRF,
    });
    expect(res.statusCode).toBe(503);
    expect(res.json<{ error: string }>().error).toBe('dali_unreachable');
  });

  it('MT-07 · 401 — unauthenticated', async () => {
    const res = await app.inject({ method: 'POST', url: '/api/admin/tenants/acme/harvest', headers: CSRF });
    expect(res.statusCode).toBe(401);
  });
});

// ─────────────────────────────────────────────────────────────────────────────

describe('POST /api/admin/tenants/:alias/resume-provisioning', () => {
  let app: App;
  beforeEach(async () => {
    app = await buildApp();
    vi.stubGlobal('fetch', makeFetchStub());
  });
  afterEach(() => vi.unstubAllGlobals());

  it('MT-08 · 403 — admin (not superadmin) cannot resume', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/resume-provisioning',
      cookies: { sid: cookie }, headers: CSRF,
    });
    expect(res.statusCode).toBe(403);
  });

  it('MT-08 · 200 — superadmin resumes provisioning', async () => {
    const cookie = await sid('super-admin');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/resume-provisioning',
      cookies: { sid: cookie }, headers: CSRF,
    });
    expect(res.statusCode).toBe(200);
    const body = res.json<{ ok: boolean; tenantAlias: string }>();
    expect(body.ok).toBe(true);
    expect(body.tenantAlias).toBe('acme');
  });

  it('MT-08 · 500 — provisioning failure returns structured error', async () => {
    const { resumeProvisioning } = await import('../admin/provisioning');
    vi.mocked(resumeProvisioning).mockRejectedValueOnce(
      Object.assign(new Error('KC timeout'), { failedStep: 3, cause: 'kc_unreachable' }),
    );
    const cookie = await sid('super-admin');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/fail-me/resume-provisioning',
      cookies: { sid: cookie }, headers: CSRF,
    });
    expect(res.statusCode).toBe(500);
    const body = res.json<{ error: string; failedStep: number }>();
    expect(body.error).toBe('resume_provisioning_failed');
    expect(body.failedStep).toBe(3);
  });

  it('MT-08 · 401 — unauthenticated', async () => {
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/resume-provisioning', headers: CSRF,
    });
    expect(res.statusCode).toBe(401);
  });
});

// ─────────────────────────────────────────────────────────────────────────────

describe('PUT /api/admin/tenants/:alias/config', () => {
  let app: App;
  beforeEach(async () => {
    app = await buildApp();
    vi.stubGlobal('fetch', makeFetchStub());
  });
  afterEach(() => vi.unstubAllGlobals());

  it('MT-14 · 403 — admin (not superadmin) cannot update config', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/config',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { expectedConfigVersion: 1, harvestCron: '0 */6 * * *' },
    });
    expect(res.statusCode).toBe(403);
  });

  it('MT-14 · 400 — missing expectedConfigVersion', async () => {
    const cookie = await sid('super-admin');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/config',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { harvestCron: '0 */6 * * *' },
    });
    expect(res.statusCode).toBe(400);
  });

  it('MT-14 · 400 — no editable fields in body', async () => {
    const cookie = await sid('super-admin');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/config',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { expectedConfigVersion: 1 },
    });
    expect(res.statusCode).toBe(400);
    expect(res.json<{ error: string }>().error).toMatch(/no editable fields/);
  });

  it('MT-14 · 400 — invalid harvestCron (too short)', async () => {
    const cookie = await sid('super-admin');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/config',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { expectedConfigVersion: 1, harvestCron: '* *' },
    });
    expect(res.statusCode).toBe(400);
    expect(res.json<{ error: string }>().error).toMatch(/harvestCron/);
  });

  it('MT-14 · 400 — invalid llmMode', async () => {
    const cookie = await sid('super-admin');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/config',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { expectedConfigVersion: 1, llmMode: 'gpt4' },
    });
    expect(res.statusCode).toBe(400);
  });

  it('MT-14 · 200 — superadmin updates harvestCron', async () => {
    const cookie = await sid('super-admin');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/config',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { expectedConfigVersion: 1, harvestCron: '0 */6 * * *' },
    });
    expect(res.statusCode).toBe(200);
    expect(res.json<{ ok: boolean }>().ok).toBe(true);
  });

  it('MT-14 · 200 — superadmin updates llmMode to local', async () => {
    const cookie = await sid('super-admin');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/config',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { expectedConfigVersion: 1, llmMode: 'local' },
    });
    expect(res.statusCode).toBe(200);
  });

  it('MT-15 · 409 — stale configVersion returns conflict', async () => {
    // Stub: SELECT returns cv=1, but UPDATE sets cv=2, re-SELECT returns cv=1
    // Simulate: cv doesn't bump (conflict scenario)
    let call = 0;
    vi.stubGlobal('fetch', vi.fn((_url: string, init?: RequestInit) => {
      const body = typeof init?.body === 'string' ? init.body : '';
      if (body.includes('SELECT configVersion')) {
        call++;
        // First SELECT (read expected) returns 1; second SELECT (verify) returns 1 (no bump = conflict)
        return Promise.resolve({
          ok: true, status: 200, text: () => Promise.resolve(''),
          headers: { get: () => null },
          json: () => Promise.resolve({ result: [{ configVersion: call === 1 ? 1 : 1 }] }),
        });
      }
      return Promise.resolve({
        ok: true, status: 200, text: () => Promise.resolve(''),
        headers: { get: () => null },
        json: () => Promise.resolve({ result: [] }),
      });
    }));

    const cookie = await sid('super-admin');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/config',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { expectedConfigVersion: 1, harvestCron: '0 12 * * *' },
    });
    expect(res.statusCode).toBe(409);
    const body = res.json<{ currentConfigVersion: number }>();
    expect(body.currentConfigVersion).toBeDefined();
  });
});

// ─────────────────────────────────────────────────────────────────────────────

describe('GET/PUT /api/admin/tenants/:alias/feature-flags', () => {
  let app: App;
  beforeEach(async () => {
    app = await buildApp();
    vi.stubGlobal('fetch', makeFetchStub());
  });
  afterEach(() => vi.unstubAllGlobals());

  it('GET · 200 — returns parsed feature flags', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme/feature-flags',
      cookies: { sid: cookie },
    });
    expect(res.statusCode).toBe(200);
    const body = res.json<{ flags: Record<string, boolean> }>();
    expect(body.flags).toBeDefined();
    expect(typeof body.flags).toBe('object');
  });

  it('GET · 403 — viewer cannot read feature flags', async () => {
    const cookie = await sid('viewer');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme/feature-flags',
      cookies: { sid: cookie },
    });
    expect(res.statusCode).toBe(403);
  });

  it('PUT · 200 — admin sets feature flag', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/feature-flags',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { flags: { betaHarvest: true }, expectedConfigVersion: 1 },
    });
    expect(res.statusCode).toBe(200);
    expect(res.json<{ ok: boolean }>().ok).toBe(true);
  });

  it('PUT · 400 — missing flags field', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/feature-flags',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { expectedConfigVersion: 1 },
    });
    expect(res.statusCode).toBe(400);
  });
});

// ─────────────────────────────────────────────────────────────────────────────

describe('POST /api/admin/tenants/:alias/role-change-signal (MTN-39)', () => {
  let app: App;
  beforeEach(async () => {
    app = await buildApp();
    vi.stubGlobal('fetch', makeFetchStub());
  });
  afterEach(() => vi.unstubAllGlobals());

  it('403 — viewer cannot send role-change signal', async () => {
    const cookie = await sid('viewer');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/role-change-signal',
      cookies: { sid: cookie }, headers: CSRF,
    });
    expect(res.statusCode).toBe(403);
  });

  it('200 — admin sends role-change signal', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/role-change-signal',
      cookies: { sid: cookie }, headers: CSRF,
    });
    expect(res.statusCode).toBe(200);
    const body = res.json<{ ok: boolean; lastRoleChangeAt: number }>();
    expect(body.ok).toBe(true);
    expect(typeof body.lastRoleChangeAt).toBe('number');
  });

  it('401 — unauthenticated', async () => {
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/role-change-signal', headers: CSRF,
    });
    expect(res.statusCode).toBe(401);
  });
});

// ─────────────────────────────────────────────────────────────────────────────

describe('PUT /api/admin/tenants/:alias/members/:userId', () => {
  let app: App;
  beforeEach(async () => {
    app = await buildApp();
    vi.stubGlobal('fetch', makeFetchStub({ orgId: 'kc-org-acme' }));
  });
  afterEach(() => vi.unstubAllGlobals());

  it('MT-12 · 200 — local-admin with same-tenant scope can update member role', async () => {
    const cookie = await sidForTenant('acme');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/members/u1',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { role: 'editor' },
    });
    expect(res.statusCode).toBe(200);
  });

  it('MT-18 · 403 — local-admin for tenant A cannot update tenant B members', async () => {
    const cookie = await sidForTenant('acme');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/beta/members/u1',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { role: 'editor' },
    });
    expect(res.statusCode).toBe(403);
  });

  it('401 — unauthenticated', async () => {
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/members/u1',
      headers: CSRF, payload: { role: 'editor' },
    });
    expect(res.statusCode).toBe(401);
  });
});

// ─────────────────────────────────────────────────────────────────────────────

describe('MT-18 Tenant isolation — cross-tenant data access blocked', () => {
  let app: App;
  beforeEach(async () => {
    app = await buildApp();
    vi.stubGlobal('fetch', makeFetchStub());
  });
  afterEach(() => vi.unstubAllGlobals());

  it('tenant-A local-admin cannot GET tenant-B members', async () => {
    const cookie = await sidForTenant('acme');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/beta/members',
      cookies: { sid: cookie },
    });
    expect(res.statusCode).toBe(403);
  });

  it('tenant-A local-admin cannot POST invite to tenant-B', async () => {
    const cookie = await sidForTenant('acme');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/beta/members',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { email: 'hacker@evil.io', name: 'Hacker', role: 'admin' },
    });
    expect(res.statusCode).toBe(403);
  });

  it('tenant-A local-admin cannot DELETE member from tenant-B', async () => {
    const cookie = await sidForTenant('acme');
    const res = await app.inject({
      method: 'DELETE', url: '/api/admin/tenants/beta/members/u-victim',
      cookies: { sid: cookie }, headers: CSRF,
    });
    expect(res.statusCode).toBe(403);
  });

  it('admin (aida:admin) can access any tenant members (cross-tenant admin)', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme/members',
      cookies: { sid: cookie },
    });
    expect(res.statusCode).toBe(200);
  });

  it('superadmin can suspend any tenant', async () => {
    const cookie = await sid('super-admin');
    const res = await app.inject({
      method: 'DELETE', url: '/api/admin/tenants/acme',
      cookies: { sid: cookie }, headers: CSRF,
      // cv=2 in stub = expectedConfigVersion+1 → CAS succeeds without readCurrentVersion call
      payload: { expectedConfigVersion: 1 },
    });
    expect(res.statusCode).toBe(200);
  });

  it('admin cannot suspend tenant (superadmin-only)', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'DELETE', url: '/api/admin/tenants/acme',
      cookies: { sid: cookie }, headers: CSRF,
    });
    expect(res.statusCode).toBe(403);
  });
});

// ─────────────────────────────────────────────────────────────────────────────

describe('POST /api/admin/tenants — provisioning CSRF + alias validation', () => {
  let app: App;
  beforeEach(async () => {
    app = await buildApp();
    vi.stubGlobal('fetch', makeFetchStub());
  });
  afterEach(() => vi.unstubAllGlobals());

  it('MT-XX · 403 — missing CSRF on provision', async () => {
    const cookie = await sid('super-admin');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants',
      cookies: { sid: cookie },
      payload: { alias: 'newco' },
    });
    expect(res.statusCode).toBe(403);
  });

  it('MT-10 · 400 — alias too short', async () => {
    const cookie = await sid('super-admin');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { alias: 'ab' },
    });
    expect(res.statusCode).toBe(400);
  });

  it('MT-10 · 201 — valid alias provisions successfully', async () => {
    const cookie = await sid('super-admin');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { alias: 'newco' },
    });
    expect(res.statusCode).toBe(201);
    expect(res.json<{ tenantAlias: string }>().tenantAlias).toBe('acme');
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// SEC-01..05  RBAC scope enforcement
// ─────────────────────────────────────────────────────────────────────────────

describe('SEC — RBAC scope enforcement', () => {
  let app: App;
  beforeEach(async () => {
    app = await buildApp();
    vi.stubGlobal('fetch', makeFetchStub());
  });
  afterEach(() => vi.unstubAllGlobals());

  // SEC-01: local-admin (aida:tenant:admin only) cannot reach aida:admin endpoints
  it('SEC-01 · 403 — local-admin cannot GET tenant detail (requires aida:admin)', async () => {
    const cookie = await sidForTenant('acme');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme',
      cookies: { sid: cookie },
    });
    expect(res.statusCode).toBe(403);
  });

  it('SEC-01 · 403 — local-admin cannot GET tenant list (requires aida:admin)', async () => {
    const cookie = await sidForTenant('acme');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants',
      cookies: { sid: cookie },
    });
    expect(res.statusCode).toBe(403);
  });

  it('SEC-01 · 403 — local-admin cannot GET feature-flags (requires aida:admin)', async () => {
    const cookie = await sidForTenant('acme');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme/feature-flags',
      cookies: { sid: cookie },
    });
    expect(res.statusCode).toBe(403);
  });

  it('SEC-01 · 403 — local-admin cannot send role-change-signal (requires aida:admin)', async () => {
    const cookie = await sidForTenant('acme');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/role-change-signal',
      cookies: { sid: cookie }, headers: CSRF,
    });
    expect(res.statusCode).toBe(403);
  });

  // SEC-02: aida:admin cannot reach aida:superadmin endpoints
  it('SEC-02 · 403 — admin cannot suspend tenant (requires aida:superadmin)', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'DELETE', url: '/api/admin/tenants/acme',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { expectedConfigVersion: 1 },
    });
    expect(res.statusCode).toBe(403);
  });

  it('SEC-02 · 403 — admin cannot archive tenant (requires aida:superadmin)', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/archive-now',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { expectedConfigVersion: 1 },
    });
    expect(res.statusCode).toBe(403);
  });

  it('SEC-02 · 403 — admin cannot unsuspend tenant (requires aida:superadmin)', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/unsuspend',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { expectedConfigVersion: 1 },
    });
    expect(res.statusCode).toBe(403);
  });

  it('SEC-02 · 403 — admin cannot PUT /config (requires aida:superadmin)', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/config',
      cookies: { sid: cookie }, headers: CSRF,
      payload: { expectedConfigVersion: 1, harvestCron: '0 6 * * *' },
    });
    expect(res.statusCode).toBe(403);
  });

  it('SEC-02 · 403 — admin cannot resume-provisioning (requires aida:superadmin)', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/resume-provisioning',
      cookies: { sid: cookie }, headers: CSRF,
    });
    expect(res.statusCode).toBe(403);
  });

  // SEC-03: unauthenticated requests always 401
  it('SEC-03 · 401 — unauthenticated cannot GET tenant list', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/admin/tenants' });
    expect(res.statusCode).toBe(401);
  });

  it('SEC-03 · 401 — unauthenticated cannot GET tenant detail', async () => {
    const res = await app.inject({ method: 'GET', url: '/api/admin/tenants/acme' });
    expect(res.statusCode).toBe(401);
  });

  it('SEC-03 · 401 — unauthenticated cannot suspend', async () => {
    const res = await app.inject({
      method: 'DELETE', url: '/api/admin/tenants/acme', headers: CSRF,
    });
    expect(res.statusCode).toBe(401);
  });

  // SEC-04: CSRF missing on mutating endpoints returns 403
  it('SEC-04 · 403 — missing CSRF on suspend', async () => {
    const cookie = await sid('super-admin');
    const res = await app.inject({
      method: 'DELETE', url: '/api/admin/tenants/acme',
      cookies: { sid: cookie },
      payload: { expectedConfigVersion: 1 },
    });
    expect(res.statusCode).toBe(403);
  });

  it('SEC-04 · 403 — missing CSRF on archive', async () => {
    const cookie = await sid('super-admin');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/archive-now',
      cookies: { sid: cookie },
      payload: { expectedConfigVersion: 1 },
    });
    expect(res.statusCode).toBe(403);
  });

  it('SEC-04 · 403 — missing CSRF on PUT feature-flags', async () => {
    const cookie = await sid('admin');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/feature-flags',
      cookies: { sid: cookie },
      payload: { flags: { betaHarvest: false }, expectedConfigVersion: 1 },
    });
    expect(res.statusCode).toBe(403);
  });

  // SEC-05: viewer (seer:read only) blocked everywhere
  it('SEC-05 · 403 — viewer cannot GET tenant list', async () => {
    const cookie = await sid('viewer');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants',
      cookies: { sid: cookie },
    });
    expect(res.statusCode).toBe(403);
  });

  it('SEC-05 · 403 — viewer cannot trigger harvest', async () => {
    const cookie = await sid('viewer');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/harvest',
      cookies: { sid: cookie }, headers: CSRF,
    });
    expect(res.statusCode).toBe(403);
  });

  it('SEC-05 · 403 — viewer cannot GET members', async () => {
    const cookie = await sid('viewer');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme/members',
      cookies: { sid: cookie },
    });
    expect(res.statusCode).toBe(403);
  });
});
