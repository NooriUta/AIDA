/**
 * RBAC_MULTITENANT v1.4 — multi-tenant role isolation tests.
 *
 * Seed state (matches KC + FRIGG after env recreation 2026-04-26):
 *
 *  Tenant   | User               | Role          | Password
 *  ---------|--------------------|---------------|---------------
 *  default  | editor             | editor        | editor
 *  default  | local.admin        | local-admin   | localadmin
 *  default  | tenant.owner       | tenant-owner  | (set via API)
 *  default  | alexey.petrov      | operator      | operator
 *  default  | sergey.ivanov      | auditor       | (set via API)
 *  default  | admin              | admin (realm) | admin
 *  default  | superadmin         | super-admin   | superadmin
 *  acme     | acme.admin         | local-admin   | acme.admin
 *  acme     | acme.owner         | tenant-owner  | acme.owner
 *  acme     | acme.editor        | editor        | acme.editor
 *  acme     | acme.viewer        | viewer        | acme.viewer
 *  ghjjh    | ghjjh.admin        | local-admin   | ghjjh.admin
 *  ghjjh    | ghjjh.owner        | tenant-owner  | ghjjh.owner
 *  ghjjh    | ghjjh.operator     | operator      | ghjjh.oper
 *  ghjjh    | ghjjh.auditor      | auditor       | ghjjh.audit
 *  test-ci  | testci.admin       | local-admin   | testci.admin
 *  test-ci  | testci.owner       | tenant-owner  | testci.owner
 *  test-ci  | testci.viewer      | viewer        | testci.view
 *  test-ci  | testci.editor      | editor        | testci.edit
 *  tettt3   | tettt3.admin       | local-admin   | tettt3.admin
 *  tettt3   | tettt3.owner       | tenant-owner  | tettt3.owner
 *  tettt3   | tettt3.operator    | operator      | tettt3.oper
 *  tettt3   | tettt3.viewer      | viewer        | tettt3.view
 *
 * All external calls (FRIGG, KC Admin, Dali) are stubbed.
 * For live E2E tests see docs/qa/manual-test-checklist.html.
 */
import { describe, it, expect, vi, beforeAll, beforeEach, afterEach } from 'vitest';
import Fastify from 'fastify';
import cookie from '@fastify/cookie';
import rbacPlugin from '../plugins/rbac';
import { tenantRoutes } from '../admin/tenantRoutes';
import * as sessions from '../sessions';
import { InMemorySessionStore } from '../store/InMemorySessionStore';

beforeAll(() => { sessions._setStoreForTesting(new InMemorySessionStore()); });

// ── Mocks ─────────────────────────────────────────────────────────────────────

vi.mock('../admin/provisioning', () => ({
  validateAlias:      vi.fn((a: string) => a.length >= 4 ? null : 'too short'),
  provisionTenant:    vi.fn().mockResolvedValue({ tenantAlias: 'acme', keycloakOrgId: 'kc-acme', lastStep: 7 }),
  resumeProvisioning: vi.fn().mockResolvedValue({ tenantAlias: 'acme', keycloakOrgId: 'kc-acme', lastStep: 7 }),
  forceCleanupTenant: vi.fn().mockResolvedValue(undefined),
}));

vi.mock('../keycloakAdmin', () => ({
  listUsers:        vi.fn().mockResolvedValue([]),
  inviteUser:       vi.fn().mockResolvedValue(undefined),
  inviteUserToOrg:  vi.fn().mockResolvedValue(undefined),
  setUserRole:      vi.fn().mockResolvedValue(undefined),
  setUserEnabled:   vi.fn().mockResolvedValue(undefined),
  removeOrgMember:  vi.fn().mockResolvedValue(undefined),
  listOrgMembers:   vi.fn().mockResolvedValue([
    { id: 'u1', username: 'acme.editor', email: 'editor@acme.io', role: 'editor' },
    { id: 'u2', username: 'acme.viewer', email: 'viewer@acme.io', role: 'viewer' },
  ]),
}));

const CSRF = { origin: 'http://localhost:5173' } as const;

// ── Tenant configs returned by FRIGG stubs ────────────────────────────────────

const TENANT_CONFIGS: Record<string, {
  keycloakOrgId: string; status: string; configVersion: number
}> = {
  acme:    { keycloakOrgId: 'kc-acme',    status: 'ACTIVE', configVersion: 2 },
  ghjjh:   { keycloakOrgId: 'kc-ghjjh',   status: 'ACTIVE', configVersion: 2 },
  'test-ci': { keycloakOrgId: 'kc-testci', status: 'ACTIVE', configVersion: 2 },
  tettt3:  { keycloakOrgId: 'kc-tettt3',  status: 'ACTIVE', configVersion: 2 },
  default: { keycloakOrgId: 'kc-default', status: 'ACTIVE', configVersion: 1 },
};

function makeFetch(alias = 'acme') {
  const cfg = TENANT_CONFIGS[alias] ?? TENANT_CONFIGS.acme;
  return vi.fn((_url: string, init?: RequestInit) => {
    const body = typeof init?.body === 'string' ? init.body : '';
    if (_url.includes('/api/sessions/harvest')) {
      return Promise.resolve({ ok: true, status: 200, headers: { get: () => null },
        json: () => Promise.resolve({ harvestId: 'h-1', tenantAlias: alias }) });
    }
    if (body.includes('SELECT configVersion')) {
      return Promise.resolve({ ok: true, status: 200, headers: { get: () => null },
        json: () => Promise.resolve({ result: [{ configVersion: cfg.configVersion }] }) });
    }
    return Promise.resolve({ ok: true, status: 200, headers: { get: () => null },
      json: () => Promise.resolve({ result: [{ tenantAlias: alias, ...cfg,
        featureFlags: '{}', lastRoleChangeAt: 0 }] }) });
  });
}

// ── Session helpers ───────────────────────────────────────────────────────────

type Role = 'super-admin' | 'admin' | 'local-admin' | 'tenant-owner' |
            'operator' | 'auditor' | 'editor' | 'viewer';

const ROLE_SCOPES: Record<Role, string[]> = {
  'super-admin':  ['aida:admin', 'aida:superadmin', 'seer:read', 'seer:write', 'aida:harvest', 'aida:audit'],
  'admin':        ['aida:admin', 'seer:read', 'seer:write', 'aida:harvest', 'aida:audit'],
  'local-admin':  ['aida:tenant:admin', 'seer:read', 'seer:write', 'aida:harvest'],
  'tenant-owner': ['aida:tenant:admin', 'aida:tenant:owner', 'seer:read', 'seer:write', 'aida:harvest'],
  'operator':     ['seer:read', 'aida:harvest'],
  'auditor':      ['seer:read', 'aida:audit'],
  'editor':       ['seer:read', 'seer:write'],
  'viewer':       ['seer:read'],
};

async function makeSession(role: Role, tenantAlias: string, extra: string[] = []): Promise<string> {
  const sid = await sessions.createSession(
    'tok', 'rt', 3600, `uid-${role}-${tenantAlias}`, role, role,
    [...ROLE_SCOPES[role], ...extra],
  );
  await sessions.updateSession(sid, { activeTenantAlias: tenantAlias });
  return sid;
}

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

// ─────────────────────────────────────────────────────────────────────────────
// RBAC-ISO: Role scope isolation — each role gets exactly its scopes
// ─────────────────────────────────────────────────────────────────────────────

describe('RBAC-ISO: scope isolation per role', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); vi.stubGlobal('fetch', makeFetch('acme')); });
  afterEach(() => vi.unstubAllGlobals());

  it('viewer → GET /members 403 (no aida:tenant:admin)', async () => {
    const sid = await makeSession('viewer', 'acme');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme/members',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(403);
  });

  it('editor → GET /members 403 (no aida:tenant:admin)', async () => {
    const sid = await makeSession('editor', 'acme');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme/members',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(403);
  });

  it('operator → GET /members 403 (no aida:tenant:admin)', async () => {
    const sid = await makeSession('operator', 'ghjjh');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/ghjjh/members',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(403);
  });

  it('auditor → GET /members 403 (no aida:tenant:admin)', async () => {
    const sid = await makeSession('auditor', 'ghjjh');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/ghjjh/members',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(403);
  });

  it('local-admin → GET /members 200 (acme)', async () => {
    const sid = await makeSession('local-admin', 'acme');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme/members',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(200);
    const body = res.json<any[]>();
    expect(Array.isArray(body)).toBe(true);
  });

  it('tenant-owner → GET /members 200 (acme)', async () => {
    const sid = await makeSession('tenant-owner', 'acme');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme/members',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(200);
  });

  it('admin → GET /members 200 (any tenant)', async () => {
    const sid = await makeSession('admin', 'acme');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme/members',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(200);
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// RBAC-TI: Tenant isolation — local-admin cannot cross tenant boundary
// ─────────────────────────────────────────────────────────────────────────────

describe('RBAC-TI: tenant isolation', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); vi.stubGlobal('fetch', makeFetch('acme')); });
  afterEach(() => vi.unstubAllGlobals());

  it('acme local-admin → GET /ghjjh/members 403 (wrong tenant)', async () => {
    const sid = await makeSession('local-admin', 'acme');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/ghjjh/members',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(403);
  });

  it('testci local-admin → GET /acme/members 403', async () => {
    vi.stubGlobal('fetch', makeFetch('test-ci'));
    const sid = await makeSession('local-admin', 'test-ci');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme/members',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(403);
  });

  it('tettt3 tenant-owner → POST /acme/members 403', async () => {
    vi.stubGlobal('fetch', makeFetch('tettt3'));
    const sid = await makeSession('tenant-owner', 'tettt3');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/members',
      cookies: { sid }, headers: CSRF,
      payload: { email: 'x@y.com', role: 'editor' },
    });
    expect(res.statusCode).toBe(403);
  });

  it('super-admin → GET /ghjjh/members 200 (cross-tenant allowed)', async () => {
    vi.stubGlobal('fetch', makeFetch('ghjjh'));
    const sid = await makeSession('super-admin', 'default');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/ghjjh/members',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(200);
  });

  // admin has aida:admin scope but requireSameTenant() still enforces activeTenantAlias
  // — only aida:superadmin bypasses it. Admin must be in the same tenant session.
  it('admin → GET /test-ci/members 403 (activeTenantAlias=default ≠ test-ci)', async () => {
    vi.stubGlobal('fetch', makeFetch('test-ci'));
    const sid = await makeSession('admin', 'default');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/test-ci/members',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(403);
  });

  it('admin → GET /test-ci/members 200 when activeTenantAlias matches', async () => {
    vi.stubGlobal('fetch', makeFetch('test-ci'));
    const sid = await makeSession('admin', 'test-ci');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/test-ci/members',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(200);
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// RBAC-INVITE: Invite role restrictions per tenant
// ─────────────────────────────────────────────────────────────────────────────

describe('RBAC-INVITE: invite role restrictions', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); vi.stubGlobal('fetch', makeFetch('acme')); });
  afterEach(() => vi.unstubAllGlobals());

  it('acme local-admin can invite editor to acme', async () => {
    const sid = await makeSession('local-admin', 'acme');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/members',
      cookies: { sid }, headers: CSRF,
      payload: { email: 'new@acme.io', role: 'editor' },
    });
    expect(res.statusCode).toBe(202);
  });

  it('acme local-admin can invite operator to acme', async () => {
    const sid = await makeSession('local-admin', 'acme');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/members',
      cookies: { sid }, headers: CSRF,
      payload: { email: 'ops@acme.io', role: 'operator' },
    });
    expect(res.statusCode).toBe(202);
  });

  it('ghjjh tenant-owner can invite local-admin', async () => {
    vi.stubGlobal('fetch', makeFetch('ghjjh'));
    const sid = await makeSession('tenant-owner', 'ghjjh');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/ghjjh/members',
      cookies: { sid }, headers: CSRF,
      payload: { email: 'la@ghjjh.io', role: 'local-admin' },
    });
    expect(res.statusCode).toBe(202);
  });

  // NOTE: tenantRoutes.ts POST /members does NOT check role elevation —
  // the guard (local-admin cannot assign admin) lives only in legacy admin.ts route.
  // This is a SPEC GAP (RBAC_MULTITENANT §3.5) — tracked as backlog item.
  it('acme local-admin invite admin → 202 (elevation guard not yet in tenantRoutes)', async () => {
    const sid = await makeSession('local-admin', 'acme');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/members',
      cookies: { sid }, headers: CSRF,
      payload: { email: 'hacker@acme.io', role: 'admin' },
    });
    // TODO: should be 403 once elevation check is added to tenantRoutes.ts
    expect(res.statusCode).toBe(202);
  });

  it('viewer cannot invite anyone', async () => {
    const sid = await makeSession('viewer', 'acme');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/members',
      cookies: { sid }, headers: CSRF,
      payload: { email: 'x@acme.io', role: 'editor' },
    });
    expect(res.statusCode).toBe(403);
  });

  it('invite without CSRF → 403', async () => {
    const sid = await makeSession('local-admin', 'acme');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/members',
      cookies: { sid },
      payload: { email: 'x@acme.io', role: 'editor' },
    });
    expect(res.statusCode).toBe(403);
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// RBAC-ROLE-CHANGE: Only local-admin/tenant-owner/admin can change roles
// ─────────────────────────────────────────────────────────────────────────────

describe('RBAC-ROLE-CHANGE: role assignment restrictions', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); vi.stubGlobal('fetch', makeFetch('acme')); });
  afterEach(() => vi.unstubAllGlobals());

  it('acme local-admin can change member role to operator', async () => {
    const sid = await makeSession('local-admin', 'acme');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/members/u1',
      cookies: { sid }, headers: CSRF,
      payload: { role: 'operator' },
    });
    expect(res.statusCode).toBe(200);
  });

  it('acme tenant-owner can promote to local-admin', async () => {
    const sid = await makeSession('tenant-owner', 'acme');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/members/u1',
      cookies: { sid }, headers: CSRF,
      payload: { role: 'local-admin' },
    });
    expect(res.statusCode).toBe(200);
  });

  it('ghjjh operator cannot change roles', async () => {
    vi.stubGlobal('fetch', makeFetch('ghjjh'));
    const sid = await makeSession('operator', 'ghjjh');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/ghjjh/members/u1',
      cookies: { sid }, headers: CSRF,
      payload: { role: 'editor' },
    });
    expect(res.statusCode).toBe(403);
  });

  it('ghjjh auditor cannot change roles', async () => {
    vi.stubGlobal('fetch', makeFetch('ghjjh'));
    const sid = await makeSession('auditor', 'ghjjh');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/ghjjh/members/u1',
      cookies: { sid }, headers: CSRF,
      payload: { role: 'editor' },
    });
    expect(res.statusCode).toBe(403);
  });

  it('testci editor cannot change roles', async () => {
    vi.stubGlobal('fetch', makeFetch('test-ci'));
    const sid = await makeSession('editor', 'test-ci');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/test-ci/members/u1',
      cookies: { sid }, headers: CSRF,
      payload: { role: 'viewer' },
    });
    expect(res.statusCode).toBe(403);
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// RBAC-SUSPEND: Suspend/archive require aida:superadmin
// ─────────────────────────────────────────────────────────────────────────────

describe('RBAC-SUSPEND: suspend/archive tenant access control', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); vi.stubGlobal('fetch', makeFetch('acme')); });
  afterEach(() => vi.unstubAllGlobals());

  // Suspend = DELETE /api/admin/tenants/:alias  (not POST /suspend)
  // Requires aida:superadmin. Body: { expectedConfigVersion? }

  it('local-admin cannot suspend tenant (DELETE /acme)', async () => {
    const sid = await makeSession('local-admin', 'acme');
    const res = await app.inject({
      method: 'DELETE', url: '/api/admin/tenants/acme',
      cookies: { sid }, headers: CSRF,
    });
    expect(res.statusCode).toBe(403);
  });

  it('tenant-owner cannot suspend tenant', async () => {
    const sid = await makeSession('tenant-owner', 'acme');
    const res = await app.inject({
      method: 'DELETE', url: '/api/admin/tenants/acme',
      cookies: { sid }, headers: CSRF,
    });
    expect(res.statusCode).toBe(403);
  });

  it('admin cannot suspend tenant (superadmin-only)', async () => {
    const sid = await makeSession('admin', 'acme');
    const res = await app.inject({
      method: 'DELETE', url: '/api/admin/tenants/acme',
      cookies: { sid }, headers: CSRF,
    });
    expect(res.statusCode).toBe(403);
  });

  it('super-admin can suspend any tenant (acme)', async () => {
    const sid = await makeSession('super-admin', 'default');
    const res = await app.inject({
      method: 'DELETE', url: '/api/admin/tenants/acme',
      cookies: { sid }, headers: CSRF,
    });
    expect([200, 409]).toContain(res.statusCode);
  });

  it('super-admin can suspend ghjjh (cross-tenant)', async () => {
    vi.stubGlobal('fetch', makeFetch('ghjjh'));
    const sid = await makeSession('super-admin', 'default');
    const res = await app.inject({
      method: 'DELETE', url: '/api/admin/tenants/ghjjh',
      cookies: { sid }, headers: CSRF,
    });
    expect([200, 409]).toContain(res.statusCode);
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// RBAC-HARVEST: operator+ can trigger harvest, viewer/editor cannot
// ─────────────────────────────────────────────────────────────────────────────

describe('RBAC-HARVEST: harvest access per role', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); vi.stubGlobal('fetch', makeFetch('acme')); });
  afterEach(() => vi.unstubAllGlobals());

  it('viewer cannot trigger harvest', async () => {
    const sid = await makeSession('viewer', 'acme');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/harvest',
      cookies: { sid }, headers: CSRF,
    });
    expect(res.statusCode).toBe(403);
  });

  it('editor cannot trigger harvest', async () => {
    const sid = await makeSession('editor', 'acme');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/harvest',
      cookies: { sid }, headers: CSRF,
    });
    expect(res.statusCode).toBe(403);
  });

  it('auditor cannot trigger harvest (ghjjh)', async () => {
    vi.stubGlobal('fetch', makeFetch('ghjjh'));
    const sid = await makeSession('auditor', 'ghjjh');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/ghjjh/harvest',
      cookies: { sid }, headers: CSRF,
    });
    expect(res.statusCode).toBe(403);
  });

  // NOTE: RBAC_MULTITENANT §3.3 says operator/local-admin (aida:harvest) should trigger harvest.
  // tenantRoutes.ts POST /harvest uses requireScope('aida:admin') — spec gap tracked as backlog item.
  it('operator cannot trigger harvest (ghjjh) — route requires aida:admin (spec gap)', async () => {
    vi.stubGlobal('fetch', makeFetch('ghjjh'));
    const sid = await makeSession('operator', 'ghjjh');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/ghjjh/harvest',
      cookies: { sid }, headers: CSRF,
    });
    // TODO: should be 200 once route is updated to accept aida:harvest + requireSameTenant()
    expect(res.statusCode).toBe(403);
  });

  it('local-admin cannot trigger harvest (acme) — route requires aida:admin (spec gap)', async () => {
    const sid = await makeSession('local-admin', 'acme');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/harvest',
      cookies: { sid }, headers: CSRF,
    });
    // TODO: should be 200 once route is updated to accept aida:harvest + requireSameTenant()
    expect(res.statusCode).toBe(403);
  });

  it('operator cannot trigger harvest on another tenant (testci → acme)', async () => {
    const sid = await makeSession('operator', 'test-ci');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/acme/harvest',
      cookies: { sid }, headers: CSRF,
    });
    expect(res.statusCode).toBe(403);
  });

  it('tettt3 operator can trigger harvest on own tenant (aida:harvest + requireSameTenant)', async () => {
    vi.stubGlobal('fetch', makeFetch('tettt3'));
    const sid = await makeSession('operator', 'tettt3');
    const res = await app.inject({
      method: 'POST', url: '/api/admin/tenants/tettt3/harvest',
      cookies: { sid }, headers: CSRF,
    });
    expect(res.statusCode).toBe(200);
  });
});

// ─────────────────────────────────────────────────────────────────────────────
// RBAC-CONFIG: tenant config read/write — superadmin only for writes
// ─────────────────────────────────────────────────────────────────────────────

describe('RBAC-CONFIG: tenant config access', () => {
  let app: App;
  beforeEach(async () => { app = await buildApp(); vi.stubGlobal('fetch', makeFetch('acme')); });
  afterEach(() => vi.unstubAllGlobals());

  // G2 fix: local-admin/tenant-owner can now read their own tenant config (aida:tenant:admin scope).
  it('local-admin can read own tenant config (aida:tenant:admin + requireSameTenant)', async () => {
    const sid = await makeSession('local-admin', 'acme');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(200);
  });

  it('tenant-owner can read own tenant config (aida:tenant:admin + requireSameTenant)', async () => {
    const sid = await makeSession('tenant-owner', 'acme');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(200);
  });

  it('viewer cannot read tenant config', async () => {
    const sid = await makeSession('viewer', 'acme');
    const res = await app.inject({
      method: 'GET', url: '/api/admin/tenants/acme',
      cookies: { sid },
    });
    expect(res.statusCode).toBe(403);
  });

  it('local-admin cannot PUT tenant config (superadmin-only)', async () => {
    const sid = await makeSession('local-admin', 'acme');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/config',
      cookies: { sid }, headers: CSRF,
      payload: { expectedConfigVersion: 1, harvestCron: '0 2 * * *' },
    });
    expect(res.statusCode).toBe(403);
  });

  it('super-admin can PUT tenant config (acme)', async () => {
    const sid = await makeSession('super-admin', 'default');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/acme/config',
      cookies: { sid }, headers: CSRF,
      payload: { expectedConfigVersion: 1, harvestCron: '0 2 * * *' },
    });
    expect([200, 409]).toContain(res.statusCode);
  });

  it('super-admin can PUT ghjjh config', async () => {
    vi.stubGlobal('fetch', makeFetch('ghjjh'));
    const sid = await makeSession('super-admin', 'default');
    const res = await app.inject({
      method: 'PUT', url: '/api/admin/tenants/ghjjh/config',
      cookies: { sid }, headers: CSRF,
      payload: { expectedConfigVersion: 1, llmMode: 'local' },
    });
    expect([200, 409]).toContain(res.statusCode);
  });
});
