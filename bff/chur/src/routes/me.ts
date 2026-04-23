/**
 * MTN-63 — Self-service user profile / preferences / consents API.
 *
 * Routes (all require authenticated session via existing `/auth/me` chain):
 *   GET  /me/profile        → UserProfile vertex
 *   PUT  /me/profile        → upsert with configVersion CAS (MTN-27 pattern)
 *   GET  /me/preferences    → UserPreferences vertex
 *   PUT  /me/preferences
 *   GET  /me/notifications  → UserNotifications vertex
 *   PUT  /me/notifications
 *   POST /me/consents       → append to UserConsents (T&C / privacy)
 *   GET  /me/consents       → list user's consents (latest first)
 *   GET  /me/session-activity → recent UserSessionEvents (MTN-64)
 *
 * All PUT endpoints accept optional `expectedConfigVersion` — when omitted,
 * server reads current + issues CAS with that value (backward compat).
 *
 * MTN-58: quotas are NOT returned — per-tenant caps live in DaliTenantConfig
 * now, enforced via middleware/quotaCheck.ts.
 *
 * MTN-62 policy: email / firstName / lastName NOT read from FRIGG — SoT is KC.
 * FE reads those from /auth/me (which re-derives from JWT on each call, TTL=5m).
 */
import type { FastifyPluginAsync } from 'fastify';
import { randomUUID } from 'node:crypto';
import {
  upsertUserVertex,
  getUserVertex,
} from '../users/FriggUserRepository';
import { friggUsersSql, friggUsersQuery } from '../users/FriggUsersClient';
import { csrfGuard } from '../middleware/csrfGuard';
import { sanitizeForAudit } from '../middleware/sanitizeForAudit';

// ── Helpers ─────────────────────────────────────────────────────────────────

async function currentConfigVersion(
  type: 'UserProfile' | 'UserPreferences' | 'UserNotifications',
  userId: string,
): Promise<number> {
  const existing = await getUserVertex(type, userId);
  return existing?.configVersion ?? 0;
}

function conflictBody(
  expected: number,
  current: number,
): Record<string, unknown> {
  return {
    error: 'config_version_conflict',
    expectedConfigVersion: expected,
    currentConfigVersion:  current,
    hint: 'GET /me/<resource> to read current configVersion, then retry',
  };
}

// ── Plugin ──────────────────────────────────────────────────────────────────

export const meRoutes: FastifyPluginAsync = async (app) => {

  // Profile
  app.get('/me/profile', { preHandler: [app.authenticate] },
    async (request, reply) => {
      const row = await getUserVertex('UserProfile', request.user.sub);
      if (!row) return reply.status(404).send({ error: 'profile_not_found', userId: request.user.sub });
      return reply.send(row);
    },
  );

  app.put<{ Body: { data: Record<string, unknown>; expectedConfigVersion?: number } }>(
    '/me/profile',
    { preHandler: [app.authenticate, csrfGuard] },
    async (request, reply) => {
      const { data, expectedConfigVersion } = request.body ?? ({} as any);
      if (!data || typeof data !== 'object') return reply.status(400).send({ error: 'data required' });
      const clean = sanitizeForAudit(data);
      const expected = expectedConfigVersion ?? await currentConfigVersion('UserProfile', request.user.sub);
      const r = await upsertUserVertex('UserProfile', request.user.sub, clean, expected);
      if (!r.ok) return reply.status(409).send(conflictBody(expected, r.current));
      return reply.send({ ok: true, configVersion: r.configVersion });
    },
  );

  // Preferences
  app.get('/me/preferences', { preHandler: [app.authenticate] },
    async (request, reply) => {
      const row = await getUserVertex('UserPreferences', request.user.sub);
      return reply.send(row ?? { userId: request.user.sub, configVersion: 0, data: {} });
    },
  );

  app.put<{ Body: { data: Record<string, unknown>; expectedConfigVersion?: number } }>(
    '/me/preferences',
    { preHandler: [app.authenticate, csrfGuard] },
    async (request, reply) => {
      const { data, expectedConfigVersion } = request.body ?? ({} as any);
      if (!data || typeof data !== 'object') return reply.status(400).send({ error: 'data required' });
      const expected = expectedConfigVersion ?? await currentConfigVersion('UserPreferences', request.user.sub);
      const r = await upsertUserVertex('UserPreferences', request.user.sub, data as Record<string, unknown>, expected);
      if (!r.ok) return reply.status(409).send(conflictBody(expected, r.current));
      return reply.send({ ok: true, configVersion: r.configVersion });
    },
  );

  // Notifications
  app.get('/me/notifications', { preHandler: [app.authenticate] },
    async (request, reply) => {
      const row = await getUserVertex('UserNotifications', request.user.sub);
      return reply.send(row ?? { userId: request.user.sub, configVersion: 0, data: {} });
    },
  );

  app.put<{ Body: { data: Record<string, unknown>; expectedConfigVersion?: number } }>(
    '/me/notifications',
    { preHandler: [app.authenticate, csrfGuard] },
    async (request, reply) => {
      const { data, expectedConfigVersion } = request.body ?? ({} as any);
      if (!data || typeof data !== 'object') return reply.status(400).send({ error: 'data required' });
      const expected = expectedConfigVersion ?? await currentConfigVersion('UserNotifications', request.user.sub);
      const r = await upsertUserVertex('UserNotifications', request.user.sub, data as Record<string, unknown>, expected);
      if (!r.ok) return reply.status(409).send(conflictBody(expected, r.current));
      return reply.send({ ok: true, configVersion: r.configVersion });
    },
  );

  // Consents — append-only. POST adds a new consent row; GET lists recent.
  app.post<{ Body: { scope: string; version: string; acceptedAt?: number; payload?: Record<string, unknown> } }>(
    '/me/consents',
    { preHandler: [app.authenticate, csrfGuard] },
    async (request, reply) => {
      const { scope, version, payload } = request.body ?? ({} as any);
      if (!scope || !version) return reply.status(400).send({ error: 'scope and version required' });
      const now = Date.now();
      const id  = randomUUID();
      await friggUsersSql(
        `INSERT INTO UserConsents SET id = :id, userId = :userId, scope = :scope, ` +
        `version = :version, acceptedAt = :acceptedAt, payload = :payload, ` +
        `configVersion = 1, updatedAt = :acceptedAt`,
        {
          id,
          userId:     request.user.sub,
          scope,
          version,
          acceptedAt: now,
          payload:    payload ? JSON.stringify(payload) : null,
        },
      );
      return reply.status(201).send({ ok: true, id, acceptedAt: now });
    },
  );

  app.get('/me/consents', { preHandler: [app.authenticate] },
    async (request, reply) => {
      const rows = await friggUsersQuery(
        `SELECT id, scope, version, acceptedAt FROM UserConsents ` +
        `WHERE userId = :userId ORDER BY acceptedAt DESC LIMIT 100`,
        { userId: request.user.sub },
      );
      return reply.send({ consents: rows });
    },
  );

  // MTN-64 view — recent session events for current user
  app.get('/me/session-activity', { preHandler: [app.authenticate] },
    async (request, reply) => {
      const rows = await friggUsersQuery(
        `SELECT eventType, ts, ipAddress, userAgent, tenantAlias, result ` +
        `FROM UserSessionEvents WHERE userId = :userId ORDER BY ts DESC LIMIT 200`,
        { userId: request.user.sub },
      );
      return reply.send({ events: rows });
    },
  );

  // Application state (saved filters, favorites, dashboard layouts, recently viewed)
  app.get('/me/app-state', { preHandler: [app.authenticate] },
    async (request, reply) => {
      const row = await getUserVertex('UserApplicationState', request.user.sub);
      return reply.send(row ?? { userId: request.user.sub, configVersion: 0, data: {} });
    },
  );

  app.put<{ Body: { data: Record<string, unknown>; expectedConfigVersion?: number } }>(
    '/me/app-state',
    { preHandler: [app.authenticate, csrfGuard] },
    async (request, reply) => {
      const { data, expectedConfigVersion } = request.body ?? ({} as any);
      if (!data || typeof data !== 'object') return reply.status(400).send({ error: 'data required' });
      const expected = expectedConfigVersion ?? await currentConfigVersion('UserApplicationState', request.user.sub);
      const r = await upsertUserVertex('UserApplicationState', request.user.sub, data, expected);
      if (!r.ok) return reply.status(409).send(conflictBody(expected, r.current));
      return reply.send({ ok: true, configVersion: r.configVersion });
    },
  );

  // Source bindings — read-only for the user themselves (written by admins)
  app.get('/me/source-bindings', { preHandler: [app.authenticate] },
    async (request, reply) => {
      const tenantAlias = request.user.activeTenantAlias ?? 'default';
      const rows = await friggUsersQuery(
        `SELECT payload, updatedAt FROM UserSourceBindings ` +
        `WHERE userId = :userId AND tenantAlias = :tenant LIMIT 1`,
        { userId: request.user.sub, tenant: tenantAlias },
      );
      if (rows.length === 0) return reply.send({ sourceIds: [], allowAllSources: false });
      const row = rows[0] as Record<string, unknown>;
      const payload = typeof row.payload === 'string' ? JSON.parse(row.payload) : (row.payload ?? {});
      return reply.send(payload);
    },
  );

  // Lifecycle — read-only (firstLoginAt, lastLoginAt, createdAt, createdBy)
  app.get('/me/lifecycle', { preHandler: [app.authenticate] },
    async (request, reply) => {
      const row = await getUserVertex('UserLifecycle', request.user.sub);
      return reply.send(row ?? { userId: request.user.sub, configVersion: 0, data: {} });
    },
  );
};
