/**
 * G3 + G4: Privilege elevation guard for invite + role-change endpoints.
 *
 * Per RBAC_MULTITENANT spec §3.5:
 *   - local-admin can assign: viewer, editor, operator, auditor (user-tier only)
 *   - tenant-owner can assign: above + local-admin (within own tenant)
 *   - admin can assign: anything except super-admin
 *   - super-admin can assign anything
 *   - self-elevation is forbidden (cannot raise own role)
 *
 * Usage:
 *   preHandler: [app.authenticate, requireSameTenant(), preventElevation]
 *   then in handler: pass body.role (target role) to assertNoElevation(currentUser, targetRole)
 */

import type { FastifyRequest } from 'fastify';

const USER_TIER_ROLES = new Set(['viewer', 'editor', 'operator', 'auditor']);

export interface ElevationCheckResult {
  ok: boolean;
  error?: string;
}

/**
 * Pure function — check whether `currentUser` is allowed to assign `targetRole`.
 * Returns ok=true on allowed, ok=false with error on blocked.
 */
export function checkElevation(
  currentScopes: string[] | undefined,
  currentRole: string | undefined,
  targetRole: string,
): ElevationCheckResult {
  const scopes = new Set(currentScopes ?? []);
  if (scopes.has('aida:superadmin')) return { ok: true };
  if (scopes.has('aida:admin')) {
    if (targetRole === 'super-admin') {
      return { ok: false, error: 'admin cannot assign super-admin role' };
    }
    return { ok: true };
  }
  if (scopes.has('aida:tenant:owner')) {
    if (targetRole === 'super-admin' || targetRole === 'admin') {
      return { ok: false, error: 'tenant-owner cannot assign platform-tier role' };
    }
    if (targetRole === 'tenant-owner' && currentRole !== 'tenant-owner') {
      return { ok: false, error: 'cannot self-elevate to tenant-owner' };
    }
    return { ok: true };
  }
  if (scopes.has('aida:tenant:admin')) {
    if (!USER_TIER_ROLES.has(targetRole)) {
      return { ok: false, error: `local-admin can only assign user-tier roles (${[...USER_TIER_ROLES].join(', ')})` };
    }
    return { ok: true };
  }
  return { ok: false, error: 'no privilege to assign roles' };
}

/**
 * Helper: throw a 403-shaped error if elevation blocked. Use in route handler
 * after reading the target role from body.
 *
 * @throws {{ statusCode: 403, error: string }}
 */
export function assertNoElevation(
  request: FastifyRequest,
  targetRole: string,
): void {
  const result = checkElevation(
    request.user?.scopes,
    request.user?.role,
    targetRole,
  );
  if (!result.ok) {
    const err = new Error(result.error ?? 'elevation blocked') as Error & { statusCode: number };
    err.statusCode = 403;
    throw err;
  }
}
