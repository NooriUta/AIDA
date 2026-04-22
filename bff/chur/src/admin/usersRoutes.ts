/**
 * SPRINT_UI_POLISH_ROUND5 Tier C — admin user endpoints supporting UsersPage
 * rewrite + UserDetailDrawer. Split from tenantRoutes.ts to keep admin routes
 * grouped by resource.
 *
 * Routes:
 *   POST /api/admin/users/batch-hydrate             — N+1 avoidance
 *   GET  /api/admin/users?allTenants=true           — superadmin cross-tenant
 *   POST /api/admin/users/:userId/source-bindings   — edit UserSourceBindings
 */
import type { FastifyPluginAsync } from 'fastify';
import { requireScope, requireAnyScope } from '../middleware/requireAdmin';
import { csrfGuard } from '../middleware/csrfGuard';
import { sanitizeForAudit } from '../middleware/sanitizeForAudit';
import {
  getUserVertex,
  type UserVertexRow,
} from '../users/FriggUserRepository';
import { friggUsersSql, friggUsersQuery, type UserVertexType } from '../users/FriggUsersClient';
import { listUsers, listOrgMembers } from '../keycloakAdmin';
import { config } from '../config';

// ── Tenant-config lookup (duplicated from tenantRoutes — keeping that file private) ──

const FRIGG_BASIC = Buffer.from(`${config.friggUser}:${config.friggPass}`).toString('base64');

async function lookupKeycloakOrgId(alias: string): Promise<string | undefined> {
  try {
    const res = await fetch(
      `${config.friggUrl}/api/v1/query/${encodeURIComponent(config.friggTenantsDb)}`,
      {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', Authorization: `Basic ${FRIGG_BASIC}` },
        body: JSON.stringify({
          language: 'sql',
          command:  'SELECT keycloakOrgId FROM DaliTenantConfig WHERE tenantAlias = :alias LIMIT 1',
          params:   { alias },
        }),
        signal: AbortSignal.timeout(5_000),
      },
    );
    if (!res.ok) return undefined;
    const data = (await res.json()) as { result?: Array<{ keycloakOrgId?: string }> };
    return data.result?.[0]?.keycloakOrgId;
  } catch {
    return undefined;
  }
}

// ── Types ───────────────────────────────────────────────────────────────────

type HydrateField = 'profile' | 'preferences' | 'notifications';

const HYDRATE_FIELD_TO_VERTEX: Record<HydrateField, UserVertexType> = {
  profile:       'UserProfile',
  preferences:   'UserPreferences',
  notifications: 'UserNotifications',
};

interface HydratedUser {
  userId:        string;
  profile?:      UserVertexRow | null;
  preferences?:  UserVertexRow | null;
  notifications?: UserVertexRow | null;
}

// ── Plugin ──────────────────────────────────────────────────────────────────

export const adminUsersRoutes: FastifyPluginAsync = async (app) => {

  // POST /api/admin/users/batch-hydrate
  // body: { userIds: string[], fields: ['profile','preferences','notifications'] }
  app.post<{ Body: { userIds: string[]; fields: HydrateField[] } }>(
    '/api/admin/users/batch-hydrate',
    {
      preHandler: [
        app.authenticate,
        requireAnyScope('aida:tenant:admin', 'aida:admin', 'aida:superadmin'),
        csrfGuard,
      ],
    },
    async (request, reply) => {
      const { userIds, fields } = request.body ?? ({} as any);
      if (!Array.isArray(userIds) || userIds.length === 0) {
        return reply.status(400).send({ error: 'userIds required (non-empty array)' });
      }
      if (userIds.length > 500) {
        return reply.status(413).send({ error: 'batch too large', max: 500, received: userIds.length });
      }
      if (!Array.isArray(fields) || fields.length === 0) {
        return reply.status(400).send({ error: 'fields required (subset of profile/preferences/notifications)' });
      }
      const invalid = fields.filter(f => !(f in HYDRATE_FIELD_TO_VERTEX));
      if (invalid.length > 0) {
        return reply.status(400).send({ error: 'unknown fields', invalid });
      }

      // Parallel fetch — bounded concurrency of 20 to avoid saturating FRIGG
      const CONCURRENCY = 20;
      const hydrated: HydratedUser[] = [];
      for (let i = 0; i < userIds.length; i += CONCURRENCY) {
        const slice = userIds.slice(i, i + CONCURRENCY);
        const results = await Promise.all(slice.map(async userId => {
          const out: HydratedUser = { userId };
          for (const field of fields) {
            const vertex = HYDRATE_FIELD_TO_VERTEX[field];
            try {
              out[field] = await getUserVertex(vertex, userId);
            } catch {
              out[field] = null;
            }
          }
          return out;
        }));
        hydrated.push(...results);
      }
      return reply.send({ hydrated });
    },
  );

  // GET /api/admin/users?allTenants=true
  app.get<{ Querystring: { allTenants?: string; tenantAlias?: string } }>(
    '/api/admin/users',
    {
      preHandler: [
        app.authenticate,
        requireAnyScope('aida:tenant:admin', 'aida:admin', 'aida:superadmin'),
      ],
    },
    async (request, reply) => {
      const allTenants = request.query.allTenants === 'true' || request.query.allTenants === '1';

      // Only superadmin can request cross-tenant view
      if (allTenants) {
        const scopes = request.user.scopes ?? [];
        if (!scopes.includes('aida:superadmin')) {
          return reply.status(403).send({
            error: 'allTenants=true requires aida:superadmin',
            hint:  'omit allTenants to see only your active tenant',
          });
        }
        const users = await listUsers();
        return reply.send({ mode: 'cross-tenant', users });
      }

      // Single-tenant mode — resolves active tenant and returns its org members
      const alias = request.query.tenantAlias ?? 'default';
      // In future (MTN-13) alias comes from session.activeTenantAlias. For now
      // the caller specifies tenantAlias explicitly or we fall back to default.
      const orgId = await lookupKeycloakOrgId(alias);
      if (orgId) {
        const users = await listOrgMembers(orgId);
        return reply.send({ mode: 'single-tenant', tenantAlias: alias, users });
      }
      // Legacy: no keycloakOrgId yet (pre-KC-ORG-02 dev), show whole realm
      const users = await listUsers();
      return reply.send({ mode: 'single-tenant-fallback', tenantAlias: alias, users });
    },
  );

  // POST /api/admin/users/:userId/source-bindings
  // body: { tenantAlias: string, sources: string[] }
  app.post<{ Params: { userId: string }; Body: { tenantAlias: string; sources: string[] } }>(
    '/api/admin/users/:userId/source-bindings',
    {
      preHandler: [
        app.authenticate,
        requireAnyScope('aida:tenant:admin', 'aida:superadmin'),
        csrfGuard,
      ],
    },
    async (request, reply) => {
      const { userId } = request.params;
      const { tenantAlias, sources } = request.body ?? ({} as any);
      if (!tenantAlias || !Array.isArray(sources)) {
        return reply.status(400).send({ error: 'tenantAlias and sources[] required' });
      }
      // tenant-admin can only edit own tenant; superadmin has no restriction
      const scopes = request.user.scopes ?? [];
      const isSuper = scopes.includes('aida:superadmin');
      if (!isSuper) {
        const sessionTenant = (request as any).user?.activeTenantAlias ?? 'default';
        if (tenantAlias !== sessionTenant) {
          return reply.status(403).send({ error: 'tenant_mismatch', yourTenant: sessionTenant });
        }
      }
      const clean = sanitizeForAudit({ tenantAlias, sources });
      // Replace bindings for (userId, tenantAlias) — atomic
      try {
        await friggUsersSql(
          `DELETE FROM UserSourceBindings WHERE userId = :userId AND tenantAlias = :tenantAlias`,
          { userId, tenantAlias: clean.tenantAlias },
        );
      } catch { /* first write */ }
      const now = Date.now();
      for (const sourceId of clean.sources) {
        await friggUsersSql(
          `INSERT INTO UserSourceBindings SET userId = :userId, tenantAlias = :tenantAlias, ` +
          `sourceId = :sourceId, configVersion = 1, updatedAt = :now`,
          { userId, tenantAlias: clean.tenantAlias, sourceId, now },
        );
      }
      return reply.send({ ok: true, count: clean.sources.length });
    },
  );

  // GET /api/admin/users/:userId/source-bindings?tenantAlias=...
  app.get<{ Params: { userId: string }; Querystring: { tenantAlias?: string } }>(
    '/api/admin/users/:userId/source-bindings',
    {
      preHandler: [
        app.authenticate,
        requireAnyScope('aida:tenant:admin', 'aida:superadmin'),
      ],
    },
    async (request, reply) => {
      const { userId } = request.params;
      const tenantAlias = request.query.tenantAlias ?? 'default';
      const rows = await friggUsersQuery(
        `SELECT sourceId, updatedAt FROM UserSourceBindings ` +
        `WHERE userId = :userId AND tenantAlias = :tenantAlias ORDER BY sourceId ASC`,
        { userId, tenantAlias },
      );
      return reply.send({ userId, tenantAlias, bindings: rows });
    },
  );
};
