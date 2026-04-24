import { useAuthStore } from '../stores/authStore';
import type { UserRole } from 'aida-shared';

/**
 * Role-level mapping for hierarchical permission checks.
 * Higher = more privileged.
 */
const ROLE_LEVELS: Record<UserRole | string, number> = {
  'super-admin':  100,
  'admin':         80,
  'tenant-owner':  70,
  'local-admin':   60,
  'auditor':       50,
  'operator':      40,
  'editor':        30,
  'viewer':        20,
};

/**
 * Derives user access context from the session role stored in authStore.
 *
 * Used for:
 *   - Navigation guard decisions (RoleGuard, HeimdallHeader visibility)
 *   - Conditional UI rendering (invite/manage buttons in UsersPage)
 *
 * Scope-based fine-grained checks are enforced by Chur (backend); this hook
 * is for UI-layer affordances only.
 */
export function useTenantContext() {
  const user  = useAuthStore(s => s.user);
  const role  = (user?.role as UserRole | undefined) ?? 'viewer';
  const level = ROLE_LEVELS[role] ?? 0;

  return {
    role,
    /** true for admin and super-admin */
    isAdmin:        level >= ROLE_LEVELS['admin'],
    /** true for tenant-owner, admin, super-admin */
    isTenantOwner:  level >= ROLE_LEVELS['tenant-owner'],
    /** true for local-admin and above */
    isLocalAdmin:   level >= ROLE_LEVELS['local-admin'],
    /** true for every authenticated user */
    isViewer:       level >= ROLE_LEVELS['viewer'],
    /** true for super-admin only */
    isSuperAdmin:   level >= ROLE_LEVELS['super-admin'],
    /** true if user can manage other users (local-admin+) */
    canManageUsers: level >= ROLE_LEVELS['local-admin'],
  };
}
