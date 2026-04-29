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
  listUsers:       vi.fn().mockResolvedValue([]),
  inviteUser:      vi.fn().mockResolvedValue(undefined),
  setUserRole:     vi.fn().mockResolvedValue(undefined),
  setUserEnabled:  vi.fn().mockResolvedValue(undefined),
  // KC-ORG-04 wire
  listOrgMembers:   vi.fn().mockResolvedValue([]),
  inviteUserToOrg:  vi.fn().mockResolvedValue(undefined),
  removeOrgMember:  vi.fn().mockResolvedValue(undefined),
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

async function makeSid(scopes: string[], tenantAlias?: string): Promise<string> {
  const cookie = await sessions.createSession('tok', 'rt', 3600, 'uid', 'admin', 'admin', scopes);
  if (tenantAlias) await sessions.updateSession(cookie, { activeTenantAlias: tenantAlias });
  return cookie;
}

// MTN-44: csrfGuard requires Origin/Referer matching config.corsOrigin on
// mutating requests. Default corsOrigin='http://localhost:5173'.
const CSRF_HEADERS = { origin: 'http://localhost:5173' } as const;

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
      cookies: { sid }, headers: CSRF_HEADERS,
      payload: { alias: 'ab' }, // too short → mock returns error
    });
    expect(res.statusCode).toBe(400);
  });

  it('201 — valid alias provisions tenant', async () => {
    const sid = await makeSid(['aida:admin']);
    const res = await app.inject({
      method:  'POST', url: '/api/admin/tenants',
      cookies: { sid }, headers: CSRF_HEADERS,
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
    const res = await app.inject({ method: 'DELETE', url: '/api/admin/tenants/acme', cookies: { sid }, headers: CSRF_HEADERS });
    expect(res.statusCode).toBe(403);
  });

  it('200 — superadmin can suspend', async () => {
    // MTN-27: casUpdateTenant makes 3 SELECT configVersion calls per operation:
    // 1. readCurrentVersion (handler) → returns 1
    // 2. casUpdateTenant pre-check    → returns 1 (must equal expected)
    // 3. casUpdateTenant post-check   → returns 2 (expected + 1)
    let selectCount = 0;
    vi.stubGlobal('fetch', vi.fn((_url: string, init?: { body?: string }) => {
      const body = init?.body ?? '';
      if (body.includes('SELECT configVersion')) {
        selectCount++;
        return Promise.resolve({
          ok: true, status: 200,
          text: async () => '', headers: { get: () => null },
          json: async () => ({ result: [{ configVersion: selectCount <= 2 ? 1 : 2 }] }),
        });
      }
      return Promise.resolve({
        ok: true, status: 200,
        text: async () => '', headers: { get: () => null },
        json: async () => ({ result: [{ tenantAlias: 'acme', status: 'ACTIVE', configVersion: 1 }] }),
      });
    }));

    const sid = await makeSid(['aida:superadmin']);
    const res = await app.inject({ method: 'DELETE', url: '/api/admin/tenants/acme', cookies: { sid }, headers: CSRF_HEADERS });
    expect(res.statusCode).toBe(200);
    expect(res.json().status).toBe('SUSPENDED');
    expect(res.json().configVersion).toBe(2);
  });

  it('409 — stale expectedConfigVersion returns conflict (MTN-27 ext)', async () => {
    // Body explicitly sends stale version; mock returns higher stored version.
    vi.stubGlobal('fetch', vi.fn((_url: string, init?: { body?: string }) => {
      const body = init?.body ?? '';
      if (body.includes('SELECT configVersion')) {
        return Promise.resolve({
          ok: true, status: 200,
          text: async () => '', headers: { get: () => null },
          json: async () => ({ result: [{ configVersion: 7 }] }),  // stale expected=3 vs current=7
        });
      }
      return Promise.resolve({
        ok: true, status: 200,
        text: async () => '', headers: { get: () => null },
        json: async () => ({ result: [] }),
      });
    }));

    const sid = await makeSid(['aida:superadmin']);
    const res = await app.inject({
      method:  'DELETE', url: '/api/admin/tenants/acme',
      cookies: { sid }, headers: CSRF_HEADERS,
      payload: { expectedConfigVersion: 3 },
    });
    expect(res.statusCode).toBe(409);
    expect(res.json().error).toBe('config_version_conflict');
    expect(res.json().currentConfigVersion).toBe(7);
  });
});

describe('PUT /api/admin/tenants/:alias/retention — MTN-27 optimistic lock', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); });

  it('400 — missing expectedConfigVersion is rejected', async () => {
    const sid = await makeSid(['aida:superadmin']);
    const res = await app.inject({
      method:  'PUT',
      url:     '/api/admin/tenants/acme/retention',
      cookies: { sid }, headers: CSRF_HEADERS,
      payload: { retainUntil: Date.now() + 86_400_000 },  // no expectedConfigVersion
    });
    expect(res.statusCode).toBe(400);
    expect(res.json().error).toMatch(/expectedConfigVersion/);
  });

  it('200 — concurrent write with matching configVersion succeeds', async () => {
    const sid = await makeSid(['aida:superadmin']);
    // Stub sequence: (a) UPDATE returns ok, (b) SELECT returns new configVersion = 2.
    const fetch = vi.fn(async (_url: string, init?: { body?: string }) => {
      if (init?.body?.includes('SELECT configVersion')) {
        return { ok: true, status: 200,
          text: async () => '', headers: { get: () => null },
          json: async () => ({ result: [{ configVersion: 2 }] }) };
      }
      return { ok: true, status: 200,
        text: async () => '', headers: { get: () => null },
        json: async () => ({ result: [] }) };
    });
    vi.stubGlobal('fetch', fetch);

    const res = await app.inject({
      method:  'PUT',
      url:     '/api/admin/tenants/acme/retention',
      cookies: { sid }, headers: CSRF_HEADERS,
      payload: { retainUntil: Date.now() + 86_400_000, expectedConfigVersion: 1 },
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().configVersion).toBe(2);
  });

  it('409 — stale expectedConfigVersion returns conflict with current value', async () => {
    const sid = await makeSid(['aida:superadmin']);
    // Stub: UPDATE does nothing (CAS mismatch); SELECT returns stored configVersion = 7.
    const fetch = vi.fn(async (_url: string, init?: { body?: string }) => {
      if (init?.body?.includes('SELECT configVersion')) {
        return { ok: true, status: 200,
          text: async () => '', headers: { get: () => null },
          json: async () => ({ result: [{ configVersion: 7 }] }) };
      }
      return { ok: true, status: 200,
        text: async () => '', headers: { get: () => null },
        json: async () => ({ result: [] }) };
    });
    vi.stubGlobal('fetch', fetch);

    const res = await app.inject({
      method:  'PUT',
      url:     '/api/admin/tenants/acme/retention',
      cookies: { sid }, headers: CSRF_HEADERS,
      payload: { retainUntil: Date.now() + 86_400_000, expectedConfigVersion: 3 },
    });
    expect(res.statusCode).toBe(409);
    const body = res.json();
    expect(body.error).toBe('config_version_conflict');
    expect(body.expectedConfigVersion).toBe(3);
    expect(body.currentConfigVersion).toBe(7);
  });
});

describe('POST /api/admin/tenants/:alias/force-cleanup', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); });

  it('403 — admin without destructive scope', async () => {
    const sid = await makeSid(['aida:admin']);
    const res = await app.inject({
      method:  'POST', url: '/api/admin/tenants/acme/force-cleanup',
      cookies: { sid }, headers: CSRF_HEADERS,
    });
    expect(res.statusCode).toBe(403);
  });

  it('200 — admin with destructive scope purges tenant', async () => {
    const sid = await makeSid(['aida:admin', 'aida:admin:destructive']);
    const res = await app.inject({
      method:  'POST', url: '/api/admin/tenants/acme/force-cleanup',
      cookies: { sid }, headers: CSRF_HEADERS,
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
      cookies: { sid }, headers: CSRF_HEADERS,
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().ok).toBe(true);
  });
});

// ── KC-ORG-05: Two-tenant members isolation ─────────────────────────────────
import * as kc from '../keycloakAdmin';

describe('KC-ORG — member isolation via keycloakOrgId', () => {
  let app: App;

  beforeEach(async () => {
    // Per-tenant FRIGG stub: tenantAlias → keycloakOrgId mapping
    vi.stubGlobal('fetch', vi.fn().mockImplementation((url: string, init: any) => {
      const body = typeof init?.body === 'string' ? init.body : '';
      if (body.includes("'default'") || body.includes('"default"')) {
        return Promise.resolve({
          ok: true, status: 200, text: () => Promise.resolve(''),
          json: () => Promise.resolve({ result: [{
            tenantAlias: 'default', status: 'ACTIVE', configVersion: 1,
            keycloakOrgId: 'org-default',
          }]}),
          headers: { get: () => null },
        });
      }
      if (body.includes("'acme'") || body.includes('"acme"')) {
        return Promise.resolve({
          ok: true, status: 200, text: () => Promise.resolve(''),
          json: () => Promise.resolve({ result: [{
            tenantAlias: 'acme', status: 'ACTIVE', configVersion: 1,
            keycloakOrgId: 'org-acme',
          }]}),
          headers: { get: () => null },
        });
      }
      return Promise.resolve({
        ok: true, status: 200, text: () => Promise.resolve(''),
        json: () => Promise.resolve({ result: [] }),
        headers: { get: () => null },
      });
    }));

    // Per-orgId member mocks
    vi.mocked(kc.listOrgMembers).mockImplementation(async (orgId: string) => {
      if (orgId === 'org-default') {
        return [
          { id: 'u1', name: 'admin',    email: 'admin@seer.io',   firstName: '', lastName: '', role: 'admin' as const,        active: true, title:'', dept:'', phone:'', avatarColor:'', lang:'', tz:'', dateFmt:'', startPage:'', notifyEmail:false, notifyBrowser:false, notifyHarvest:false, notifyErrors:false, notifyDigest:false, quotas:{mimir:0,sessions:0,atoms:0,workers:0,anvil:0}, sources:[] },
          { id: 'u2', name: 'editor',   email: 'editor@seer.io',  firstName: '', lastName: '', role: 'editor' as const,       active: true, title:'', dept:'', phone:'', avatarColor:'', lang:'', tz:'', dateFmt:'', startPage:'', notifyEmail:false, notifyBrowser:false, notifyHarvest:false, notifyErrors:false, notifyDigest:false, quotas:{mimir:0,sessions:0,atoms:0,workers:0,anvil:0}, sources:[] },
        ];
      }
      if (orgId === 'org-acme') {
        return [
          { id: 'a1', name: 'acme-owner', email: 'owner@acme.com', firstName: '', lastName: '', role: 'tenant-owner' as const, active: true, title:'', dept:'', phone:'', avatarColor:'', lang:'', tz:'', dateFmt:'', startPage:'', notifyEmail:false, notifyBrowser:false, notifyHarvest:false, notifyErrors:false, notifyDigest:false, quotas:{mimir:0,sessions:0,atoms:0,workers:0,anvil:0}, sources:[] },
        ];
      }
      return [];
    });
    vi.mocked(kc.inviteUserToOrg).mockResolvedValue(undefined);
    vi.mocked(kc.removeOrgMember).mockResolvedValue(undefined);

    app = await buildApp();
  });

  it('GET /tenants/default/members — returns only default-org members (not realm-wide)', async () => {
    const sid = await makeSid(['aida:tenant:admin'], 'default');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/default/members', cookies: { sid },
    });
    expect(res.statusCode).toBe(200);
    const members = res.json() as Array<{ name: string }>;
    expect(members.map(m => m.name).sort()).toEqual(['admin', 'editor']);
    expect(kc.listOrgMembers).toHaveBeenCalledWith('org-default');
    expect(kc.listUsers).not.toHaveBeenCalled();
  });

  it('GET /tenants/acme/members — returns only acme-org members (1 user)', async () => {
    const sid = await makeSid(['aida:tenant:admin'], 'acme');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme/members', cookies: { sid },
    });
    expect(res.statusCode).toBe(200);
    const members = res.json() as Array<{ name: string }>;
    expect(members.map(m => m.name)).toEqual(['acme-owner']);
    expect(kc.listOrgMembers).toHaveBeenCalledWith('org-acme');
  });

  it('default members and acme members are DISJOINT (no cross-tenant leak)', async () => {
    // aida:admin bypasses requireSameTenant() — test focuses on data isolation, not RBAC
    const sid = await makeSid(['aida:admin']);
    const d = await app.inject({ method: 'GET', url: '/api/admin/tenants/default/members', cookies: { sid } });
    const a = await app.inject({ method: 'GET', url: '/api/admin/tenants/acme/members',    cookies: { sid } });
    const dIds = new Set((d.json() as Array<{ id: string }>).map(u => u.id));
    const aIds = new Set((a.json() as Array<{ id: string }>).map(u => u.id));
    const intersection = [...dIds].filter(id => aIds.has(id));
    expect(intersection).toEqual([]);
  });

  it('POST /tenants/default/members invite → inviteUserToOrg("org-default", "default", ...)', async () => {
    const sid = await makeSid(['aida:tenant:admin'], 'default');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/default/members',
      cookies: { sid }, headers: CSRF_HEADERS,
      payload: { email: 'new@seer.io', name: 'New User', role: 'viewer' },
    });
    expect(res.statusCode).toBe(202);
    expect(kc.inviteUserToOrg).toHaveBeenCalledWith(
      'org-default', 'default', 'new@seer.io', 'New User', 'viewer',
    );
    expect(kc.inviteUser).not.toHaveBeenCalled();
  });

  it('DELETE /tenants/default/members/:userId → removeOrgMember (not setUserEnabled)', async () => {
    const sid = await makeSid(['aida:tenant:admin'], 'default');
    const res = await app.inject({
      method: 'DELETE', url: '/api/admin/tenants/default/members/u1',
      cookies: { sid }, headers: CSRF_HEADERS,
    });
    expect(res.statusCode).toBe(200);
    expect(kc.removeOrgMember).toHaveBeenCalledWith('org-default', 'u1');
    expect(kc.setUserEnabled).not.toHaveBeenCalled();
  });
});

// ── L2 Multi-tenant isolation + lifecycle coverage (HTA-STAB) ───────────────
describe('Multi-tenant lifecycle isolation', () => {
  let app: App;
  beforeEach(async () => {
    // MTN-27: casUpdateTenant issues 3 SELECT configVersion calls per operation:
    //   call 1 — readCurrentVersion (handler)  → stable current
    //   call 2 — casUpdateTenant pre-check      → stable current (must equal expected)
    //   call 3 — casUpdateTenant post-check     → current + 1 (confirms bump)
    // Group-of-3 tracks this across chained operations (suspend → archive → restore).
    let callsInOp = 0;
    let currentVersion = 1;
    vi.stubGlobal('fetch', vi.fn((_url: string, init?: { body?: string }) => {
      const body = init?.body ?? '';
      if (body.includes('SELECT configVersion')) {
        callsInOp++;
        if (callsInOp === 3) {
          callsInOp = 0;
          currentVersion++;
        }
        const cv = currentVersion;
        return Promise.resolve({
          ok: true, status: 200,
          text: async () => '', headers: { get: () => null },
          json: async () => ({ result: [{ configVersion: cv }] }),
        });
      }
      return Promise.resolve({
        ok: true, status: 200,
        text: async () => '', headers: { get: () => null },
        json: async () => ({ result: [{ tenantAlias: 'acme', status: 'ACTIVE', configVersion: 1 }] }),
      });
    }));
    app = await buildApp();
  });

  // ── Missing endpoints ────────────────────────────────────────────────────

  it('unsuspend: 403 non-superadmin', async () => {
    const sid = await makeSid(['aida:admin']);
    const res = await app.inject({ method: 'POST', url: '/api/admin/tenants/acme/unsuspend', cookies: { sid } });
    expect(res.statusCode).toBe(403);
  });

  it('unsuspend: 200 superadmin', async () => {
    const sid = await makeSid(['aida:superadmin']);
    const res = await app.inject({ method: 'POST', url: '/api/admin/tenants/acme/unsuspend', cookies: { sid }, headers: CSRF_HEADERS });
    expect(res.statusCode).toBe(200);
    expect(res.json().ok).toBe(true);
  });

  it('archive-now: 200 superadmin', async () => {
    const sid = await makeSid(['aida:superadmin']);
    const res = await app.inject({ method: 'POST', url: '/api/admin/tenants/acme/archive-now', cookies: { sid }, headers: CSRF_HEADERS });
    expect(res.statusCode).toBe(200);
  });

  it('restore: 200 superadmin', async () => {
    const sid = await makeSid(['aida:superadmin']);
    const res = await app.inject({ method: 'POST', url: '/api/admin/tenants/acme/restore', cookies: { sid }, headers: CSRF_HEADERS });
    expect(res.statusCode).toBe(200);
    expect(res.json().ok).toBe(true);
  });

  it('retention: 400 missing retainUntil', async () => {
    const sid = await makeSid(['aida:superadmin']);
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/retention',
      cookies: { sid }, headers: CSRF_HEADERS, payload: {},
    });
    expect(res.statusCode).toBe(400);
  });

  it('retention: 200 superadmin +30d', async () => {
    // retention handler requires expectedConfigVersion in body (no readCurrentVersion call).
    // Override fetch so post-CAS SELECT returns expectedConfigVersion + 1 = 2.
    vi.stubGlobal('fetch', vi.fn((_url: string, init?: { body?: string }) => {
      const body = init?.body ?? '';
      if (body.includes('SELECT configVersion')) {
        return Promise.resolve({ ok: true, status: 200, text: async () => '', headers: { get: () => null }, json: async () => ({ result: [{ configVersion: 2 }] }) });
      }
      return Promise.resolve({ ok: true, status: 200, text: async () => '', headers: { get: () => null }, json: async () => ({ result: [] }) });
    }));
    const sid = await makeSid(['aida:superadmin']);
    const ts  = Date.now() + 30 * 24 * 3600 * 1000;
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/retention',
      cookies: { sid }, headers: CSRF_HEADERS, payload: { retainUntil: ts, expectedConfigVersion: 1 },
    });
    expect(res.statusCode).toBe(200);
  });

  // ── HTA-08: config update ───────────────────────────────────────────────

  it('config PUT: 403 admin (not superadmin)', async () => {
    const sid = await makeSid(['aida:admin']);
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme',
      cookies: { sid }, payload: { maxParseSessions: 50 },
    });
    expect(res.statusCode).toBe(403);
  });

  it('config PUT: 400 empty body', async () => {
    const sid = await makeSid(['aida:superadmin']);
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme',
      cookies: { sid }, headers: CSRF_HEADERS, payload: {},
    });
    expect(res.statusCode).toBe(400);
  });

  it('config PUT: 200 superadmin with editable fields', async () => {
    const sid = await makeSid(['aida:superadmin']);
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme',
      cookies: { sid }, headers: CSRF_HEADERS, payload: { maxParseSessions: 100, harvestCron: '0 0 * * *' },
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().ok).toBe(true);
  });

  // ── Full lifecycle ──────────────────────────────────────────────────────

  it('suspend → archive → restore (acme)', async () => {
    const sid = await makeSid(['aida:superadmin']);
    const susp = await app.inject({ method: 'DELETE', url: '/api/admin/tenants/acme', cookies: { sid }, headers: CSRF_HEADERS });
    expect(susp.json().status).toBe('SUSPENDED');
    const arch = await app.inject({ method: 'POST', url: '/api/admin/tenants/acme/archive-now', cookies: { sid }, headers: CSRF_HEADERS });
    expect(arch.statusCode).toBe(200);
    const rest = await app.inject({ method: 'POST', url: '/api/admin/tenants/acme/restore', cookies: { sid }, headers: CSRF_HEADERS });
    expect(rest.json().ok).toBe(true);
  });

  // ── Role isolation across multiple tenants ─────────────────────────────

  it('admin blocked on ALL lifecycle verbs for acme + beta + gamma', async () => {
    const sid = await makeSid(['aida:admin']);
    for (const t of ['acme', 'beta', 'gamma']) {
      const cases: Array<[string, string]> = [
        ['DELETE', `/api/admin/tenants/${t}`],
        ['POST',   `/api/admin/tenants/${t}/unsuspend`],
        ['POST',   `/api/admin/tenants/${t}/archive-now`],
        ['POST',   `/api/admin/tenants/${t}/restore`],
      ];
      for (const [method, path] of cases) {
        const res = await app.inject({ method: method as 'POST' | 'DELETE', url: path, cookies: { sid } });
        expect(res.statusCode, `${method} ${path}`).toBe(403);
      }
    }
  });
});
