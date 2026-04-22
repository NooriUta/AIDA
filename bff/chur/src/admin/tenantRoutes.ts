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
  resumeProvisioning,
  forceCleanupTenant,
} from './provisioning';
import {
  requireScope,
  requireSameTenant,
} from '../middleware/requireAdmin';
import { emitTenantAudit } from '../middleware/auditEmit';
import { adminRateLimit, provisioningRateLimit } from '../middleware/rateLimit';
import { csrfGuard } from '../middleware/csrfGuard';
import { casUpdateTenant, casConflictBody } from './casUpdate';
import {
  listUsers,
  inviteUser,
  setUserRole,
  setUserEnabled,
  listOrgMembers,
  inviteUserToOrg,
  removeOrgMember,
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
    { preHandler: [app.authenticate, requireScope('aida:admin'), csrfGuard, provisioningRateLimit] },
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
        // MTN-25: structured provisioning error — includes failedStep so caller
        // can call /resume-provisioning with the right context.
        const msg = e instanceof Error ? e.message : String(e);
        const failedStep        = (e as { failedStep?: number })?.failedStep;
        const lastSuccessfulStep = (e as { lastSuccessfulStep?: number })?.lastSuccessfulStep;
        const cause             = (e as { cause?: string })?.cause;
        return reply.status(500).send({
          error:              'provisioning_failed',
          tenantAlias:        alias,
          correlationId,
          failedStep,
          lastSuccessfulStep,
          cause:              cause ?? msg,
          message:            `Provisioning failed at step ${failedStep ?? '?'}: ${cause ?? msg}. ` +
                              `Use POST /tenants/${alias}/resume-provisioning (superadmin) to retry.`,
        });
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

  // MTN-27: status-mutating endpoints use CAS via casUpdateTenant(). Body
  // requires `expectedConfigVersion` (when provided) — backward-compat: if
  // absent we read current then issue CAS with that value, preserving
  // pre-MTN-27 semantics for callers that haven't adopted optimistic-lock
  // yet. Admin UI should send the field explicitly.

  async function readCurrentVersion(alias: string): Promise<number> {
    const rows = await friggSql('frigg-tenants',
      `SELECT configVersion FROM DaliTenantConfig WHERE tenantAlias = :alias LIMIT 1`,
      { alias },
    );
    return Number((rows[0] as { configVersion?: number } | undefined)?.configVersion ?? 0);
  }

  // ── DELETE /api/admin/tenants/:alias — suspend ──────────────────────────────
  app.delete<{ Params: { alias: string }; Body?: { expectedConfigVersion?: number } }>(
    '/api/admin/tenants/:alias',
    { preHandler: [app.authenticate, requireScope('aida:superadmin'), csrfGuard, adminRateLimit] },
    async (request, reply) => {
      const { alias } = request.params;
      const expected = request.body?.expectedConfigVersion ?? await readCurrentVersion(alias);
      const r = await casUpdateTenant('frigg-tenants', alias, expected, {
        setClause: `status = 'SUSPENDED', updatedAt = :ts`,
        params:    { ts: Date.now() },
      });
      if (!r.ok) return reply.status(409).send(casConflictBody(alias, expected, r.current));
      emitTenantAudit('seer.audit.tenant_suspended', request.user.username, alias);
      return reply.send({ ok: true, status: 'SUSPENDED', configVersion: r.configVersion });
    },
  );

  // ── POST /api/admin/tenants/:alias/unsuspend ────────────────────────────────
  app.post<{ Params: { alias: string }; Body?: { expectedConfigVersion?: number } }>(
    '/api/admin/tenants/:alias/unsuspend',
    { preHandler: [app.authenticate, requireScope('aida:superadmin'), csrfGuard, adminRateLimit] },
    async (request, reply) => {
      const { alias } = request.params;
      const expected = request.body?.expectedConfigVersion ?? await readCurrentVersion(alias);
      const r = await casUpdateTenant('frigg-tenants', alias, expected, {
        setClause: `status = 'ACTIVE', updatedAt = :ts`,
        params:    { ts: Date.now() },
      });
      if (!r.ok) return reply.status(409).send(casConflictBody(alias, expected, r.current));
      return reply.send({ ok: true, status: 'ACTIVE', configVersion: r.configVersion });
    },
  );

  // ── POST /api/admin/tenants/:alias/archive-now ──────────────────────────────
  app.post<{ Params: { alias: string }; Body?: { expectedConfigVersion?: number } }>(
    '/api/admin/tenants/:alias/archive-now',
    { preHandler: [app.authenticate, requireScope('aida:superadmin'), csrfGuard, adminRateLimit] },
    async (request, reply) => {
      const { alias } = request.params;
      const expected = request.body?.expectedConfigVersion ?? await readCurrentVersion(alias);
      const r = await casUpdateTenant('frigg-tenants', alias, expected, {
        setClause: `status = 'ARCHIVED', updatedAt = :ts, archivedAt = :ts`,
        params:    { ts: Date.now() },
      });
      if (!r.ok) return reply.status(409).send(casConflictBody(alias, expected, r.current));
      emitTenantAudit('seer.audit.tenant_archived', request.user.username, alias);
      return reply.send({ ok: true, status: 'ARCHIVED', configVersion: r.configVersion });
    },
  );

  // ── POST /api/admin/tenants/:alias/restore ──────────────────────────────────
  app.post<{ Params: { alias: string }; Body?: { expectedConfigVersion?: number } }>(
    '/api/admin/tenants/:alias/restore',
    { preHandler: [app.authenticate, requireScope('aida:superadmin'), csrfGuard, adminRateLimit] },
    async (request, reply) => {
      const { alias } = request.params;
      const expected = request.body?.expectedConfigVersion ?? await readCurrentVersion(alias);
      const r = await casUpdateTenant('frigg-tenants', alias, expected, {
        setClause: `status = 'ACTIVE', updatedAt = :ts`,
        params:    { ts: Date.now() },
      });
      if (!r.ok) return reply.status(409).send(casConflictBody(alias, expected, r.current));
      emitTenantAudit('seer.audit.tenant_restored', request.user.username, alias);
      return reply.send({ ok: true, status: 'ACTIVE', configVersion: r.configVersion });
    },
  );

  // ── PUT /api/admin/tenants/:alias/retention ─────────────────────────────────
  // MTN-27: body requires `expectedConfigVersion` for optimistic-lock CAS.
  // Two concurrent admin edits can no longer lose a write silently.
  app.put<{ Params: { alias: string }; Body: { retainUntil: number; expectedConfigVersion: number } }>(
    '/api/admin/tenants/:alias/retention',
    { preHandler: [app.authenticate, requireScope('aida:superadmin'), csrfGuard, adminRateLimit] },
    async (request, reply) => {
      const { alias }                           = request.params;
      const { retainUntil, expectedConfigVersion } = request.body ?? ({} as { retainUntil: number; expectedConfigVersion: number });
      if (!retainUntil || typeof retainUntil !== 'number') {
        return reply.status(400).send({ error: 'retainUntil (epoch ms) required' });
      }
      if (typeof expectedConfigVersion !== 'number' || expectedConfigVersion < 1) {
        return reply.status(400).send({ error: 'expectedConfigVersion (positive integer) required — MTN-27' });
      }

      // CAS: update only when stored configVersion matches; bump it on success.
      await friggSql('frigg-tenants',
        `UPDATE DaliTenantConfig SET archiveRetentionUntil = :until, updatedAt = :ts,
           configVersion = configVersion + 1
         WHERE tenantAlias = :alias AND configVersion = :expected`,
        { alias, until: retainUntil, ts: Date.now(), expected: expectedConfigVersion },
      );

      // Verify the update actually landed (ArcadeDB HTTP API does not return affected-rows count).
      const after = await friggSql('frigg-tenants',
        `SELECT configVersion FROM DaliTenantConfig WHERE tenantAlias = :alias LIMIT 1`,
        { alias },
      );
      const current = Number((after[0] as { configVersion?: number })?.configVersion ?? 0);
      if (current !== expectedConfigVersion + 1) {
        return reply.status(409).send({
          error: 'config_version_conflict',
          tenantAlias: alias,
          expectedConfigVersion,
          currentConfigVersion: current,
          hint: 'GET /api/admin/tenants/:alias to read current configVersion, then retry with the new value',
        });
      }
      return reply.send({ ok: true, configVersion: current });
    },
  );

  // ── POST /api/admin/tenants/:alias/force-cleanup — CAP-06 ───────────────────
  app.post<{ Params: { alias: string } }>('/api/admin/tenants/:alias/force-cleanup',
    { preHandler: [app.authenticate, requireScope('aida:admin', 'aida:admin:destructive'), csrfGuard, provisioningRateLimit] },
    async (request, reply) => {
      const { alias } = request.params;
      await forceCleanupTenant(alias);
      emitTenantAudit('seer.audit.tenant_purged', request.user.username, alias);
      return reply.send({ ok: true });
    },
  );

  // ── POST /api/admin/tenants/:alias/resume-provisioning — MTN-25 ────────────
  app.post<{ Params: { alias: string } }>('/api/admin/tenants/:alias/resume-provisioning',
    { preHandler: [app.authenticate, requireScope('aida:superadmin'), csrfGuard, provisioningRateLimit] },
    async (request, reply) => {
      const { alias } = request.params;
      const correlationId = randomUUID();
      try {
        const result = await resumeProvisioning(alias, correlationId, request.user.username);
        emitTenantAudit('seer.audit.tenant_created', request.user.username, alias,
          { keycloakOrgId: result.keycloakOrgId, correlationId, resumed: true });
        return reply.status(200).send({ ok: true, ...result });
      } catch (e) {
        const msg = e instanceof Error ? e.message : String(e);
        const failedStep = (e as { failedStep?: number })?.failedStep;
        const cause      = (e as { cause?: string })?.cause;
        return reply.status(500).send({
          error:         'resume_provisioning_failed',
          tenantAlias:   alias,
          correlationId,
          failedStep,
          cause:         cause ?? msg,
        });
      }
    },
  );

  // ── POST /api/admin/tenants/:alias/reconnect — CAP-09 / MTN-01 ─────────────
  app.post<{ Params: { alias: string } }>('/api/admin/tenants/:alias/reconnect',
    { preHandler: [app.authenticate, requireScope('aida:admin'), csrfGuard, adminRateLimit] },
    async (request, reply) => {
      const { alias } = request.params;

      // MTN-01: Broadcast registry invalidation directly to each JVM service's
      // /api/internal/tenant-invalidate endpoint. Fan-out is fire-and-forget —
      // a single service being down should not block the other invalidations.
      // Adding new services later: append to targets list.
      const internalSecret = process.env.AIDA_INTERNAL_SHARED_SECRET ?? 'aida-internal-dev-secret';
      const targets = [
        { name: 'shuttle',  url: process.env.SHUTTLE_URL  ?? 'http://localhost:8080' },
        // { name: 'dali',     url: process.env.DALI_URL     ?? 'http://localhost:9090' },
        // { name: 'anvil',    url: process.env.ANVIL_URL    ?? 'http://localhost:9095' },
      ];
      const results = await Promise.all(targets.map(async t => {
        try {
          const res = await fetch(`${t.url}/api/internal/tenant-invalidate`, {
            method:  'POST',
            headers: {
              'Content-Type':   'application/json',
              'X-Internal-Auth': internalSecret,
            },
            body:   JSON.stringify({ tenantAlias: alias }),
            signal: AbortSignal.timeout(3_000),
          });
          return { target: t.name, status: res.status, ok: res.ok };
        } catch (e) {
          return { target: t.name, status: 0, ok: false, error: (e as Error).message };
        }
      }));

      // Legacy heimdall broadcast — keep for now, future sprints can remove
      // once all services listen via /tenant-invalidate.
      fetch(`${HEIMDALL_URL}/api/control/registry-invalidated`, {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify({ tenantAlias: alias }),
        signal:  AbortSignal.timeout(3_000),
      }).catch(() => {});

      return reply.send({ ok: true, tenantAlias: alias, targets: results });
    },
  );

  // ── Member management (CAP-08) ──────────────────────────────────────────────

  // KC-ORG-04: member routes resolve keycloakOrgId from DaliTenantConfig and
  // call the Organization-scoped APIs (vs legacy listUsers = whole realm).
  // Fallback to legacy listUsers() only when keycloakOrgId is missing (upgrade
  // path for dev deployments before sync_default_keycloak_org_id was run).

  app.get<{ Params: { alias: string } }>('/api/admin/tenants/:alias/members',
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant(), csrfGuard, adminRateLimit] },
    async (request, reply) => {
      const cfg = await getTenantConfig(request.params.alias).catch(() => null);
      const orgId = cfg?.keycloakOrgId as string | undefined;
      if (orgId) {
        const users = await listOrgMembers(orgId);
        return reply.send(users);
      }
      console.warn(`[KC-ORG] /tenants/${request.params.alias}/members: no keycloakOrgId — falling back to listUsers (whole realm)`);
      const users = await listUsers().catch(() => []);
      return reply.send(users);
    },
  );

  app.post<{ Params: { alias: string }; Body: { email: string; name?: string; role: string } }>(
    '/api/admin/tenants/:alias/members',
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant(), csrfGuard, adminRateLimit] },
    async (request, reply) => {
      const { email, name, role } = request.body ?? {};
      if (!email || !role) return reply.status(400).send({ error: 'email and role are required' });
      const { alias } = request.params;
      const cfg = await getTenantConfig(alias).catch(() => null);
      const orgId = cfg?.keycloakOrgId as string | undefined;
      if (orgId) {
        await inviteUserToOrg(orgId, alias, email, name ?? email.split('@')[0], role as any);
      } else {
        console.warn(`[KC-ORG] invite to ${alias}: no keycloakOrgId — using legacy inviteUser (no org binding)`);
        await inviteUser(email, name ?? email.split('@')[0], role as any);
      }
      emitTenantAudit('seer.audit.member_added', request.user.username, alias, { email });
      return reply.status(202).send({ ok: true });
    },
  );

  app.put<{ Params: { alias: string; userId: string }; Body: { role?: string; enabled?: boolean } }>(
    '/api/admin/tenants/:alias/members/:userId',
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant(), csrfGuard, adminRateLimit] },
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
    { preHandler: [app.authenticate, requireScope('aida:tenant:admin'), requireSameTenant(), csrfGuard, adminRateLimit] },
    async (request, reply) => {
      const { userId, alias } = request.params;
      const cfg = await getTenantConfig(alias).catch(() => null);
      const orgId = cfg?.keycloakOrgId as string | undefined;
      if (orgId) {
        await removeOrgMember(orgId, userId);
      } else {
        console.warn(`[KC-ORG] remove from ${alias}: no keycloakOrgId — disabling user as fallback`);
        await setUserEnabled(userId, false);
      }
      emitTenantAudit('seer.audit.member_removed', request.user.username, alias, { userId });
      return reply.send({ ok: true });
    },
  );
};
