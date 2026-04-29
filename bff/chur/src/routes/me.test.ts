/**
 * MTN-63 — Self-service FRIGG routes (/me/*) vitest coverage.
 *
 * Mocks FriggUserRepository and FriggUsersClient so tests run without a
 * live ArcadeDB instance. Verifies auth gates, happy paths, 400/404/409
 * error shapes, and CAS version-conflict handling for each resource.
 */
import { describe, it, expect, vi, beforeAll, beforeEach } from 'vitest';
import Fastify from 'fastify';
import cookie from '@fastify/cookie';
import rbacPlugin from '../plugins/rbac';
import * as sessions from '../sessions';
import { InMemorySessionStore } from '../store/InMemorySessionStore';

// ── In-memory session store (no live FRIGG needed) ───────────────────────────

beforeAll(() => { sessions._setStoreForTesting(new InMemorySessionStore()); });

// ── Mocks ────────────────────────────────────────────────────────────────────

vi.mock('../users/FriggUserRepository', () => ({
  getUserVertex:    vi.fn(),
  upsertUserVertex: vi.fn(),
  deleteUserVertex: vi.fn(),
}));

vi.mock('../users/FriggUsersClient', () => ({
  friggUsersQuery: vi.fn(),
  friggUsersSql:   vi.fn(),
  withSchema:      vi.fn((fn: () => unknown) => fn()),
}));

// Bypass CSRF origin check in test (no browser Origin header in inject())
vi.mock('../middleware/csrfGuard', () => ({
  csrfGuard: vi.fn((_req: unknown, _reply: unknown, done: () => void) => done()),
}));

import { getUserVertex, upsertUserVertex } from '../users/FriggUserRepository';
import { friggUsersQuery, friggUsersSql } from '../users/FriggUsersClient';
import { csrfGuard } from '../middleware/csrfGuard';

const mockGet    = vi.mocked(getUserVertex);
const mockUpsert = vi.mocked(upsertUserVertex);
const mockQuery  = vi.mocked(friggUsersQuery);
const mockSql    = vi.mocked(friggUsersSql);
const mockCsrf   = vi.mocked(csrfGuard);

// resetAllMocks clears both call history AND queued mockResolvedValueOnce values.
// clearAllMocks only clears history — leftover queued values bleed into later tests.
// After reset, restore implementations that must always be active.
function resetMocks() {
  vi.resetAllMocks();
  mockQuery.mockResolvedValue([]);
  mockSql.mockResolvedValue([]);
  // csrfGuard must call done() — without it Fastify preHandler hangs and tests timeout
  mockCsrf.mockImplementation((_req: unknown, _reply: unknown, done: () => void) => done());
}

// ── App factory ───────────────────────────────────────────────────────────────

async function buildApp() {
  const { meRoutes } = await import('./me');
  const app = Fastify({ logger: false });
  await app.register(cookie);
  await app.register(rbacPlugin);
  await app.register(meRoutes);
  await app.ready();
  return app;
}

type App = Awaited<ReturnType<typeof buildApp>>;

async function makeSid(): Promise<string> {
  return sessions.createSession('tok', 'rt', 3600, 'user-uuid-1', 'tester', 'viewer' as any, ['seer:read']);
}

// ── GET /me/profile ───────────────────────────────────────────────────────────

describe('GET /me/profile', () => {
  let app: App;
  beforeEach(async () => {
    resetMocks();
    app = await buildApp();
  });

  it('401 — no session', async () => {
    const res = await app.inject({ method: 'GET', url: '/me/profile' });
    expect(res.statusCode).toBe(401);
  });

  it('404 — no FRIGG row for user', async () => {
    mockGet.mockResolvedValueOnce(null);
    const sid = await makeSid();
    const res = await app.inject({ method: 'GET', url: '/me/profile', cookies: { sid } });
    expect(res.statusCode).toBe(404);
    expect(res.json().error).toBe('profile_not_found');
  });

  it('200 — returns existing FRIGG UserProfile row', async () => {
    const row = { userId: 'user-uuid-1', configVersion: 3, updatedAt: 1000, reserved_acl_v2: null, data: { title: 'Analyst' } };
    mockGet.mockResolvedValueOnce(row as any);
    const sid = await makeSid();
    const res = await app.inject({ method: 'GET', url: '/me/profile', cookies: { sid } });
    expect(res.statusCode).toBe(200);
    expect(res.json().configVersion).toBe(3);
    expect(res.json().data.title).toBe('Analyst');
  });
});

// ── PUT /me/profile ───────────────────────────────────────────────────────────

describe('PUT /me/profile', () => {
  let app: App;
  beforeEach(async () => {
    resetMocks();
    app = await buildApp();
  });

  it('401 — no session', async () => {
    const res = await app.inject({ method: 'PUT', url: '/me/profile', payload: { data: {} } });
    expect(res.statusCode).toBe(401);
  });

  it('400 — missing data field', async () => {
    const sid = await makeSid();
    const res = await app.inject({ method: 'PUT', url: '/me/profile', cookies: { sid }, payload: {} });
    expect(res.statusCode).toBe(400);
    expect(res.json().error).toBe('data required');
  });

  it('200 — upserts new profile (configVersion=1)', async () => {
    mockGet.mockResolvedValueOnce(null);                                 // currentConfigVersion fallback
    mockUpsert.mockResolvedValueOnce({ ok: true, configVersion: 1 });
    const sid = await makeSid();
    const res = await app.inject({
      method: 'PUT', url: '/me/profile', cookies: { sid },
      payload: { data: { title: 'Engineer', dept: 'Platform' } },
    });
    expect(res.statusCode).toBe(200);
    expect(res.json()).toMatchObject({ ok: true, configVersion: 1 });
    expect(mockUpsert).toHaveBeenCalledWith(
      'UserProfile', 'user-uuid-1',
      expect.objectContaining({ title: 'Engineer' }),
      0,
    );
  });

  it('409 — configVersion conflict', async () => {
    mockGet.mockResolvedValueOnce({ configVersion: 2 } as any);
    mockUpsert.mockResolvedValueOnce({ ok: false, conflict: true, current: 2 });
    const sid = await makeSid();
    const res = await app.inject({
      method: 'PUT', url: '/me/profile', cookies: { sid },
      payload: { data: { title: 'X' }, expectedConfigVersion: 1 },
    });
    expect(res.statusCode).toBe(409);
    expect(res.json().error).toBe('config_version_conflict');
    expect(res.json().currentConfigVersion).toBe(2);
  });
});

// ── GET /me/preferences ───────────────────────────────────────────────────────

describe('GET /me/preferences', () => {
  let app: App;
  beforeEach(async () => {
    resetMocks();
    app = await buildApp();
  });

  it('401 — no session', async () => {
    const res = await app.inject({ method: 'GET', url: '/me/preferences' });
    expect(res.statusCode).toBe(401);
  });

  it('200 — returns empty default when no FRIGG row', async () => {
    mockGet.mockResolvedValueOnce(null);
    const sid = await makeSid();
    const res = await app.inject({ method: 'GET', url: '/me/preferences', cookies: { sid } });
    expect(res.statusCode).toBe(200);
    expect(res.json()).toMatchObject({ userId: 'user-uuid-1', configVersion: 0, data: {} });
  });

  it('200 — returns existing preferences row', async () => {
    const row = { userId: 'user-uuid-1', configVersion: 1, updatedAt: 2000, reserved_acl_v2: null, data: { lang: 'en', theme: 'light' } };
    mockGet.mockResolvedValueOnce(row as any);
    const sid = await makeSid();
    const res = await app.inject({ method: 'GET', url: '/me/preferences', cookies: { sid } });
    expect(res.statusCode).toBe(200);
    expect(res.json().data.lang).toBe('en');
  });
});

// ── PUT /me/preferences ───────────────────────────────────────────────────────

describe('PUT /me/preferences', () => {
  let app: App;
  beforeEach(async () => {
    resetMocks();
    app = await buildApp();
  });

  it('200 — upserts preferences', async () => {
    mockGet.mockResolvedValueOnce(null);
    mockUpsert.mockResolvedValueOnce({ ok: true, configVersion: 1 });
    const sid = await makeSid();
    const res = await app.inject({
      method: 'PUT', url: '/me/preferences', cookies: { sid },
      payload: { data: { lang: 'ru', theme: 'dark' } },
    });
    expect(res.statusCode).toBe(200);
    expect(res.json().ok).toBe(true);
    expect(mockUpsert).toHaveBeenCalledWith(
      'UserPreferences', 'user-uuid-1',
      expect.objectContaining({ lang: 'ru', theme: 'dark' }),
      expect.any(Number),
    );
  });
});

// ── GET /me/notifications ─────────────────────────────────────────────────────

describe('GET /me/notifications', () => {
  let app: App;
  beforeEach(async () => {
    resetMocks();
    app = await buildApp();
  });

  it('401 — no session', async () => {
    const res = await app.inject({ method: 'GET', url: '/me/notifications' });
    expect(res.statusCode).toBe(401);
  });

  it('200 — returns empty default', async () => {
    mockGet.mockResolvedValueOnce(null);
    const sid = await makeSid();
    const res = await app.inject({ method: 'GET', url: '/me/notifications', cookies: { sid } });
    expect(res.statusCode).toBe(200);
    expect(res.json().configVersion).toBe(0);
  });
});

// ── POST /me/consents ─────────────────────────────────────────────────────────

describe('POST /me/consents', () => {
  let app: App;
  beforeEach(async () => {
    resetMocks();
    app = await buildApp();
  });

  it('401 — no session', async () => {
    const res = await app.inject({ method: 'POST', url: '/me/consents', payload: { scope: 'tos', version: '2.0' } });
    expect(res.statusCode).toBe(401);
  });

  it('400 — missing scope', async () => {
    const sid = await makeSid();
    const res = await app.inject({ method: 'POST', url: '/me/consents', cookies: { sid }, payload: { version: '2.0' } });
    expect(res.statusCode).toBe(400);
  });

  it('400 — missing version', async () => {
    const sid = await makeSid();
    const res = await app.inject({ method: 'POST', url: '/me/consents', cookies: { sid }, payload: { scope: 'tos' } });
    expect(res.statusCode).toBe(400);
  });

  it('201 — records consent with generated id + timestamp', async () => {
    mockSql.mockResolvedValueOnce([]);
    const sid = await makeSid();
    const res = await app.inject({
      method: 'POST', url: '/me/consents', cookies: { sid },
      payload: { scope: 'tos', version: '2.0' },
    });
    expect(res.statusCode).toBe(201);
    const body = res.json();
    expect(body.ok).toBe(true);
    expect(typeof body.id).toBe('string');
    expect(body.id.length).toBeGreaterThan(0);
    expect(typeof body.acceptedAt).toBe('number');
    expect(mockSql).toHaveBeenCalledOnce();
  });
});

// ── GET /me/session-activity ──────────────────────────────────────────────────

describe('GET /me/session-activity', () => {
  let app: App;
  beforeEach(async () => {
    resetMocks();
    app = await buildApp();
  });

  it('401 — no session', async () => {
    const res = await app.inject({ method: 'GET', url: '/me/session-activity' });
    expect(res.statusCode).toBe(401);
  });

  it('200 — returns events array', async () => {
    mockQuery.mockResolvedValueOnce([
      { eventType: 'login', ts: 1000, ipAddress: '1.2.3.4', userAgent: 'Mozilla', tenantAlias: 'default', result: 'success' },
    ]);
    const sid = await makeSid();
    const res = await app.inject({ method: 'GET', url: '/me/session-activity', cookies: { sid } });
    expect(res.statusCode).toBe(200);
    expect(res.json().events).toHaveLength(1);
    expect(res.json().events[0].eventType).toBe('login');
  });
});

// ── GET /me/app-state ─────────────────────────────────────────────────────────

describe('GET /me/app-state', () => {
  let app: App;
  beforeEach(async () => {
    resetMocks();
    app = await buildApp();
  });

  it('200 — returns empty default when no state', async () => {
    mockGet.mockResolvedValueOnce(null);
    const sid = await makeSid();
    const res = await app.inject({ method: 'GET', url: '/me/app-state', cookies: { sid } });
    expect(res.statusCode).toBe(200);
    expect(res.json().data).toEqual({});
  });
});
