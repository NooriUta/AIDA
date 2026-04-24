/**
 * MTN-34 — Service accounts and API keys.
 *
 * Routes:
 *   GET    /api/admin/tenants/:alias/service-accounts
 *   POST   /api/admin/tenants/:alias/service-accounts
 *   DELETE /api/admin/tenants/:alias/service-accounts/:saId
 *   POST   /api/admin/tenants/:alias/service-accounts/:saId/keys
 *   DELETE /api/admin/tenants/:alias/service-accounts/:saId/keys/:keyId
 */
import type { FastifyPluginAsync } from 'fastify';
import { randomBytes, createHash } from 'node:crypto';
import { requireScope } from '../middleware/requireAdmin';
import { csrfGuard } from '../middleware/csrfGuard';
import { config } from '../config';

const FRIGG_BASIC = Buffer.from(`${config.friggUser}:${config.friggPass}`).toString('base64');
const DB = config.friggTenantsDb;

async function sql(command: string, params: Record<string, unknown> = {}): Promise<unknown[]> {
  const res = await fetch(`${config.friggUrl}/api/v1/command/${encodeURIComponent(DB)}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Basic ${FRIGG_BASIC}` },
    body: JSON.stringify({ language: 'sql', command, params }),
    signal: AbortSignal.timeout(10_000),
  });
  if (!res.ok) {
    const text = await res.text().catch(() => '');
    throw new Error(`FRIGG ${res.status}: ${text}`);
  }
  return ((await res.json()) as { result?: unknown[] }).result ?? [];
}

interface ServiceAccountRow {
  id: string;
  tenantAlias: string;
  name: string;
  enabled: boolean;
  createdBy: string;
  createdAt: number;
}

interface ApiKeyRow {
  keyId: string;
  serviceAccountId: string;
  hashedSecret: string;
  expiresAt: number | null;
  scopes: string;
  createdAt: number;
}

function hashSecret(raw: string): string {
  return createHash('sha256').update(raw).digest('hex');
}

export const serviceAccountRoutes: FastifyPluginAsync = async (app) => {
  // ── GET /api/admin/tenants/:alias/service-accounts ─────────────────────────
  app.get<{ Params: { alias: string } }>(
    '/tenants/:alias/service-accounts',
    { preHandler: [app.authenticate, requireScope('aida:admin')] },
    async (request, reply) => {
      const { alias } = request.params;
      const rows = await sql(
        `SELECT id, tenantAlias, name, enabled, createdBy, createdAt
         FROM ServiceAccount WHERE tenantAlias = :alias ORDER BY createdAt DESC`,
        { alias },
      );
      return reply.send(rows);
    },
  );

  // ── POST /api/admin/tenants/:alias/service-accounts ─────────────────────────
  app.post<{ Params: { alias: string }; Body: { name: string } }>(
    '/tenants/:alias/service-accounts',
    { preHandler: [app.authenticate, requireScope('aida:admin'), csrfGuard] },
    async (request, reply) => {
      const { alias } = request.params;
      const { name } = request.body;
      if (!name || typeof name !== 'string' || name.trim().length === 0) {
        return reply.status(400).send({ error: 'name_required' });
      }
      const id = randomBytes(16).toString('hex');
      const now = Date.now();
      await sql(
        `INSERT INTO ServiceAccount SET id = :id, tenantAlias = :alias, name = :name,
         enabled = true, createdBy = :createdBy, createdAt = :now`,
        { id, alias, name: name.trim(), createdBy: request.user.username, now },
      );
      return reply.status(201).send({ id, tenantAlias: alias, name: name.trim(), enabled: true });
    },
  );

  // ── DELETE /api/admin/tenants/:alias/service-accounts/:saId ────────────────
  app.delete<{ Params: { alias: string; saId: string } }>(
    '/tenants/:alias/service-accounts/:saId',
    { preHandler: [app.authenticate, requireScope('aida:admin'), csrfGuard] },
    async (request, reply) => {
      const { alias, saId } = request.params;
      // Delete keys first (cascade)
      await sql(`DELETE FROM ApiKey WHERE serviceAccountId = :saId`, { saId });
      await sql(
        `DELETE FROM ServiceAccount WHERE id = :saId AND tenantAlias = :alias`,
        { saId, alias },
      );
      return reply.send({ ok: true });
    },
  );

  // ── POST /api/admin/tenants/:alias/service-accounts/:saId/keys ─────────────
  app.post<{
    Params: { alias: string; saId: string };
    Body: { expiresAt?: number; scopes?: string[] };
  }>(
    '/tenants/:alias/service-accounts/:saId/keys',
    { preHandler: [app.authenticate, requireScope('aida:admin'), csrfGuard] },
    async (request, reply) => {
      const { alias, saId } = request.params;
      const { expiresAt, scopes = [] } = request.body ?? {};

      // Verify service account belongs to tenant
      const saRows = await sql(
        `SELECT id FROM ServiceAccount WHERE id = :saId AND tenantAlias = :alias LIMIT 1`,
        { saId, alias },
      );
      if (!saRows.length) {
        return reply.status(404).send({ error: 'service_account_not_found' });
      }

      const rawSecret = `sk_live_${randomBytes(24).toString('base64url')}`;
      const keyId = randomBytes(12).toString('hex');
      const now = Date.now();

      await sql(
        `INSERT INTO ApiKey SET keyId = :keyId, serviceAccountId = :saId,
         hashedSecret = :hash, expiresAt = :exp, scopes = :scopes, createdAt = :now`,
        {
          keyId,
          saId,
          hash: hashSecret(rawSecret),
          exp: expiresAt ?? null,
          scopes: scopes.join(' '),
          now,
        },
      );

      return reply.status(201).send({ keyId, rawSecret });
    },
  );

  // ── DELETE /api/admin/tenants/:alias/service-accounts/:saId/keys/:keyId ────
  app.delete<{ Params: { alias: string; saId: string; keyId: string } }>(
    '/tenants/:alias/service-accounts/:saId/keys/:keyId',
    { preHandler: [app.authenticate, requireScope('aida:admin'), csrfGuard] },
    async (request, reply) => {
      const { saId, keyId } = request.params;
      await sql(
        `DELETE FROM ApiKey WHERE keyId = :keyId AND serviceAccountId = :saId`,
        { keyId, saId },
      );
      return reply.send({ ok: true });
    },
  );
};
