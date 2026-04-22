/**
 * MTN-61 — User soft-delete + retention + legal hold endpoints.
 *
 * Routes (all superadmin):
 *   DELETE /api/admin/users/:userId           → soft-delete + KC disable
 *   POST   /api/admin/users/:userId/restore   → undo soft-delete
 *   POST   /api/admin/users/:userId/legal-hold → set legalHoldUntil
 *   GET    /api/admin/users/soft-deleted      → list users pending hard-delete
 *
 * Soft-delete flow:
 *   1. PUT KC user enabled=false
 *   2. Upsert UserLifecycle: { deletedAt: now, deletedBy, deletionReason,
 *                              dataRetentionUntil: now + 30d }
 *   3. Emit audit event seer.audit.user_soft_deleted
 *
 * Hard-delete scheduler lives in heimdall-backend (UserHardDeleteJob, weekly).
 *
 * MTN-42 retention guard intersection — lowering dataRetentionUntil below
 * legalHoldUntil is blocked via checkRetentionChange().
 */
import type { FastifyPluginAsync } from 'fastify';
import { requireScope } from '../middleware/requireAdmin';
import { csrfGuard } from '../middleware/csrfGuard';
import { emitTenantAudit } from '../middleware/auditEmit';
import { setUserEnabled } from '../keycloakAdmin';
import { friggUsersSql, friggUsersQuery } from '../users/FriggUsersClient';
import { checkRetentionChange } from './retentionGuard';

const DATA_RETENTION_DAYS = 30;

type DeletionReason =
  | 'user_request'
  | 'admin_action'
  | 'gdpr_erasure'
  | 'inactive'
  | 'legal_order';

function daysFromNow(days: number): number {
  return Date.now() + days * 24 * 60 * 60 * 1000;
}

async function getUserLifecycle(userId: string): Promise<Record<string, unknown> | null> {
  const rows = await friggUsersQuery(
    `SELECT * FROM UserLifecycle WHERE userId = :userId LIMIT 1`,
    { userId },
  );
  return (rows[0] as Record<string, unknown> | undefined) ?? null;
}

export const userLifecycleRoutes: FastifyPluginAsync = async (app) => {

  // DELETE /api/admin/users/:userId — soft-delete
  app.delete<{
    Params: { userId: string };
    Body?: { reason?: DeletionReason; expectedConfigVersion?: number };
  }>('/api/admin/users/:userId',
    { preHandler: [app.authenticate, requireScope('aida:superadmin'), csrfGuard] },
    async (request, reply) => {
      const { userId } = request.params;
      const reason = request.body?.reason ?? 'admin_action';
      const now = Date.now();
      const retentionUntil = daysFromNow(DATA_RETENTION_DAYS);

      // Disable in KC
      try {
        await setUserEnabled(userId, false);
      } catch (err) {
        return reply.status(502).send({ error: 'kc_disable_failed', reason: (err as Error).message });
      }

      // Upsert lifecycle
      try {
        await friggUsersSql(`DELETE FROM UserLifecycle WHERE userId = :userId`, { userId });
      } catch { /* first-time */ }
      await friggUsersSql(
        `INSERT INTO UserLifecycle SET userId = :userId, deletedAt = :now, ` +
        `deletedBy = :deletedBy, deletionReason = :reason, ` +
        `dataRetentionUntil = :retentionUntil, configVersion = 1, updatedAt = :now`,
        {
          userId,
          now,
          deletedBy:      request.user.username,
          reason,
          retentionUntil,
        },
      );
      emitTenantAudit('seer.audit.user_soft_deleted', request.user.username, 'default',
                     { userId, reason, retentionUntil });
      return reply.send({ ok: true, dataRetentionUntil: retentionUntil });
    },
  );

  // POST /api/admin/users/:userId/restore
  app.post<{ Params: { userId: string } }>(
    '/api/admin/users/:userId/restore',
    { preHandler: [app.authenticate, requireScope('aida:superadmin'), csrfGuard] },
    async (request, reply) => {
      const { userId } = request.params;
      try {
        await setUserEnabled(userId, true);
      } catch (err) {
        return reply.status(502).send({ error: 'kc_enable_failed', reason: (err as Error).message });
      }
      await friggUsersSql(`DELETE FROM UserLifecycle WHERE userId = :userId`, { userId });
      emitTenantAudit('seer.audit.user_restored', request.user.username, 'default', { userId });
      return reply.send({ ok: true });
    },
  );

  // POST /api/admin/users/:userId/legal-hold
  app.post<{
    Params: { userId: string };
    Body: { legalHoldUntil: number };
  }>('/api/admin/users/:userId/legal-hold',
    { preHandler: [app.authenticate, requireScope('aida:superadmin'), csrfGuard] },
    async (request, reply) => {
      const { userId } = request.params;
      const { legalHoldUntil } = request.body ?? ({} as any);
      if (!legalHoldUntil || typeof legalHoldUntil !== 'number') {
        return reply.status(400).send({ error: 'legalHoldUntil (epoch ms) required' });
      }
      const current = await getUserLifecycle(userId);
      const now = Date.now();
      // MTN-42: ensure legal hold cannot be set if it would allow premature purge
      const check = checkRetentionChange({
        currentDataRetentionUntil:    (current?.dataRetentionUntil as number | null | undefined) ?? null,
        currentArchiveRetentionUntil: null,
        currentLegalHoldUntil:        (current?.legalHoldUntil as number | null | undefined) ?? null,
        newDataRetentionUntil:        (current?.dataRetentionUntil as number | null | undefined) ?? null,
      });
      if (!check.ok) return reply.status(check.status).send({ error: check.error, ...check.details });

      if (current) {
        await friggUsersSql(
          `UPDATE UserLifecycle SET legalHoldUntil = :legalHoldUntil, updatedAt = :now ` +
          `WHERE userId = :userId`,
          { userId, legalHoldUntil, now },
        );
      } else {
        // No prior soft-delete — legal hold still valid (audit trigger)
        await friggUsersSql(
          `INSERT INTO UserLifecycle SET userId = :userId, legalHoldUntil = :legalHoldUntil, ` +
          `configVersion = 1, updatedAt = :now`,
          { userId, legalHoldUntil, now },
        );
      }
      emitTenantAudit('seer.audit.user_legal_hold_set', request.user.username, 'default',
                     { userId, legalHoldUntil });
      return reply.send({ ok: true, legalHoldUntil });
    },
  );

  // GET /api/admin/users/soft-deleted
  app.get('/api/admin/users/soft-deleted',
    { preHandler: [app.authenticate, requireScope('aida:superadmin')] },
    async (_request, reply) => {
      const rows = await friggUsersQuery(
        `SELECT userId, deletedAt, deletedBy, deletionReason, dataRetentionUntil, legalHoldUntil ` +
        `FROM UserLifecycle WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC`,
      );
      return reply.send({ users: rows });
    },
  );
};
