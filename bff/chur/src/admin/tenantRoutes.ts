/**
 * CAP-06/07/08/09: Tenant lifecycle, member management, and reconnect endpoints.
 *
 * All routes under /api/admin/tenants/:alias require aida:superadmin or aida:admin.
 * Member-management routes require aida:tenant:admin + same-tenant check.
 */
import type { FastifyPluginAsync } from 'fastify';
import {
  validateAlias,
  provisionTenant,
  forceCleanupTenant,
} from './provisioning';
import {
  requireScope,
  requireSameTenant,
} from '../middleware/requireAdmin';
import { emitTenantAudit } from '../middleware/auditEmit';
import { adminRateLimit, provisioningRateLimit } from '../middleware/rateLimit';
import {
  listUsers,
  inviteUser,
  setUserRole,
  setUserEnabled,
} from '../keycloakAdmin';
import { randomUUID } from 'node:crypto';

// ── FRIGG helpers ─────────────────────────────────────────────────────────────

const FRIGG_URL  = (process.env.FRIGG_URL  ?? 'http://localhost:2481').replace(/\/$/, '');
const FRIGG_USER = process.env.FRIGG_USER  ?? 'root';
const FRIGG_PASS = process.env.FRIGG_PASS  ?? 'playwithdata';
const FRIGG_BASIC = Buffer.from(`${FRIGG_USER}:${FRIGG_PASS}`).toString('base64');
const HEIMDALL_URL = (process.env.HEIMDALL_URL ?? 'http://localhost:9093').replace(/\/$/, '');

async function friggSql(db: string, command: string, params?: Record<string, unknown>): Promise<unknown[]> {
  const res = await fetch(`${FRIGG_URL}/api/v1/command/${encodeURIComponent(db)}`, {
    method:  'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Basic ${FRIGG_BASIC}` },
    body:    JSON.stringify({ language: 'sql', command, params: params ?? {} }),
    signal:  AbortSignal.timeout(10_000),
  });
  if (!res.ok) throw new Error(`FRIGG ${res.status}: ${await res.text().catch(() => '')}`);
  const data = await res.json() as { result: unknown[] };
  return data.result ?? [];
}

async function getTenantConfig(alias: string): Promise<Record<string, unknown> | null> {
  const rows = await friggSql('frigg-tenants',
    `SELECT * FROM DaliTenantConfig WHERE tenantAlias = :alias LIMIT 1`,
    { alias },
  );
  return rows.length ? rows[0] as Record<string, unknown> : null;
}

// ── Routes ────────────────────────────────────────────────────────────────────

export const tenantRoutes: FastifyPluginAsync = async (app) => {

  // ── GET /api/admin/tenants ──────────────────────────────────────────────────
  app.get('/api/admin/tenants',
    { preHandler: [app.authenticate, requireScope('aida:admin'), adminRateLimit] },
    async (_req, reply) => {
      const rows = await friggSql('frigg-tenants',
        `SELECT tenantAlias, status, configVersion FROM DaliTenantConfig`,
      ).catch(() => [{ tenantAlias: 'default', status: 'ACTIVE', configVersion: 1 }]);
      return reply.send(rows);
    },
  );

  // ── POST /api/admin/tenants — provision ─────────────────────────────────────
  app.post<{ Body: { alias: string } }>('/api/admin/tenants',
    { preHandler: [app.authenticate, requireScope('aida:admin'), provisioningRateLimit] },
    async (request, reply) => {
      const { alias } = request.body ?? {};
      const validationError = validateAlias(alias);
      if (validationError) return reply.status(400).send({ error: validationError });

      const correlationId = randomUUID();
      try {
        const result = await provisionTenant(alias, correlationId, request.user.username);
        emitTenantAudit('seer.audit.tenant_created', request.user.username, alias,
          { keycloakOrgId: result.keycloakOrgId, correlationId });
        return reply.status(201).send(result);
      } catch (e) {
        return reply.status(500).send(e);
      }
    },
  );

  // ── GET /api/admin/tenants/:alias ───────────────────────────────────────────
  app.get<{ Params: { alias: string } }>('/api/admin/tenants/:alias',
    { preHandler: [app.authenticate, requireScope('aida:admin'), adminRateLimit] },
    async (request, reply) => {
      const cfg = await getTenantConfig(request.params.alias).catch(() => null);
      if (!cfg) return reply.status(404).send({ error: 'Tenant not found' });
      return reply.send(cfg);
    },
  );

  // ── DELETE /api/admin/tenants/:alias — suspend ──────────────────────────────
  app.delete<{ Params: { alias: string } }>('/api/admin/tenants/:alias',
    { preHandler: [app.authenticate, requireScope('aida:superadmin'), adminRateLimit] },
    async (request, reply) => {
      const { alias } = request.params;
      await friggSql('frigg-tenants',
        `UPDATE DaliTenantConfig SET status = 'SUSPENDED', updatedAt = :ts WHERE tenantAlias = :alias`,
        { alias, ts: Date.now() },
      );
      emitTenantAudit('seer.audit.tenant_suspended', request.user.username, alias);
      return reply.send({ ok: true, status: 'SUSPENDED' });
    },
  );

  // ── POST /api/admin/tenants/:alias/unsuspend ────────────────────────────────
  app.post<{ Params: { alias: string } }>('/api/admin/tenants/:alias/unsuspend',
    { preHandler: [app.authenticate, requireScope('aida:superadmin'), adminRateLimit] },
    async (request, reply) => {
      const { alias } = request.params;
      await friggSql('frigg-tenants',
        `UPDATE DaliTenantConfig SET status = 'ACTIVE', updatedAt = :ts WHERE tenantAlias = :alias`,
        { alias, ts: Date.now() },
      );
      return reply.send({ ok: true, status: 'ACTIVE' });
    },
  );

  // ── POST /api/admin/tenants/:alias/archive-now ──────────────────────────────
  app.post<{ Params: { alias: string } }>('/api/admin/tenants/:alias/archive-now',
    { preHandler: [app.authenticate, requireScope('aida:superadmin'), adminRateLimit] },
    async (request, reply) => {
      const { alias } = request.params;
      await friggSql('frigg-tenants',
        `UPDATE DaliTenantConfig SET status = 'ARCHIVED', updatedAt = :ts, archivedAt = :ts WHERE tenantAlias = :alias`,
        { alias, ts: Date.now() },
      );
      emitTenantAudit('seer.audit.tenant_archived', request.user.username, alias);
      return reply.send({ ok: true, status: 'ARCHIVED' });
    },
  );

  // ── POST /api/admin/tenants/:alias/restore ──────────────────────────────────
  app.post<{ Params: { alias: string } }>('/api/admin/tenants/:alias/restore',
    { preHandler: [app.authenticate, requireScope('aida:superadmin'), adminRateLimit] },
    async (request, reply) => {
      const { alias } = request.params;
      await friggSql('frigg-tenants',
        `UPDATE DaliTenantConfig SET status = 'ACTIVE', updatedAt = :ts WHERE tenantAlias = :alias`,
        { alias, ts: Date.now() },
      );
      emitTenantAudit('seer.audit.tenant_restored', request.user.username, alias);
      return reply.send({ ok: true, status: 'ACTIVE' });
    },
  );

  // ── PUT /api/admin/tenants/:alias/retention ─────────────────────────────────
  app.put<{ Params: { alias: string }; Body: { retainUntil: number } }>(
    '/api/admin/tenants/:alias/retention',
    { preHandler: [app.authenticate, requireScope('aida:superadmin'), adminRateLimit] },
    async (request, reply) => {
      const { alias }       = request.params;
      const { retainUntil } = request.body ?? {};
      if (!retainUntil || typeof retainUntil !== 'number') {
        return reply.status(400).send({ error: 'retainUntil (epoch ms) required' });
      }
      await friggSql('frigg-tenants',
        `UPDATE DaliTenantConfig SET archiveRetentionUntil = :until, updatedAt = :ts WHERE tenantAlias = :alias`,
        { alias, until: retainUntil, ts: Date.now() },
      );
      return reply.send({ ok: true });
    },
  );

  // ── POST /api/admin/tenants/:alias/force-cleanup — CAP-06 ───────────────────
  app.post<{ Params: { alias: string } }>('/api/admin/tenants/:alias/force-cleanup',
    { preHandler: [app.authenticate, requireScope('aida:admin', 'aida:admin:destructive'), provisioningRateLimit] },
    async (request, reply) => {
      const { alias } = request.params;
      await forceCleanupTenant(alias);
      emitTenantAudit('seer.audit.tenant_purged', request.user.username, alias);
      return reply.send({ ok: true });
    },
  );

  // ── POST /api/admin/tenants/:alias/reconnect — CAP-09 ──────────────────────
  app.post<{ Params: { alias: string } }>('/api/admin/tenants/:alias/reconnect',
    { preHandler: [app.authenticate, requireScope('aida:admin'), adminRateLimit] },
    async (request, reply) => {
      const { alias } = request.params;
      // Broadcast registry invalidation to all JVM services via Heimdall
      fetch(`${HEIMDALL_URL}/api/control/registry-invalidated`, {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify({ tenantAlias: alias }),
        signal:  AbortSignal.timeout(3_000),
      }).catch(() => {});
      return reply.send({ ok: true, message: `Registry invalidation broadcast for ${alias}` });
    },
  );

  // ── Member management (CAP-08) ──────────────────────────────────────────────

  app.get<{ Params: { alias: string } }>('/api/admin/tenants/:alias/members',
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant(), adminRateLimit] },
    async (_req, reply) => {
      const users = await listUsers().catch(() => []);
      return reply.send(users);
    },
  );

  app.post<{ Params: { alias: string }; Body: { email: string; name?: string; role: string } }>(
    '/api/admin/tenants/:alias/members',
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant(), adminRateLimit] },
    async (request, reply) => {
      const { email, name, role } = request.body ?? {};
      if (!email || !role) return reply.status(400).send({ error: 'email and role are required' });
      await inviteUser(email, name ?? email.split('@')[0], role as any);
      emitTenantAudit('seer.audit.member_added', request.user.username, request.params.alias, { email });
      return reply.status(202).send({ ok: true });
    },
  );

  app.put<{ Params: { alias: string; userId: string }; Body: { role?: string; enabled?: boolean } }>(
    '/api/admin/tenants/:alias/members/:userId',
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant(), adminRateLimit] },
    async (request, reply) => {
      const { userId } = request.params;
      const { role, enabled } = request.body ?? {};
      if (role !== undefined) {
        await setUserRole(userId, role as any);
        emitTenantAudit('seer.audit.member_role_changed', request.user.username, request.params.alias, { userId, role });
      }
      if (enabled !== undefined) await setUserEnabled(userId, enabled);
      return reply.send({ ok: true });
    },
  );

  app.delete<{ Params: { alias: string; userId: string } }>(
    '/api/admin/tenants/:alias/members/:userId',
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant(), adminRateLimit] },
    async (request, reply) => {
      const { userId } = request.params;
      await setUserEnabled(userId, false); // disable as proxy for remove (KC org member removal)
      emitTenantAudit('seer.audit.member_removed', request.user.username, request.params.alias, { userId });
      return reply.send({ ok: true });
    },
  );
};
