import { Navigate } from 'react-router-dom';
import { useTenantContext } from '../hooks/useTenantContext';
import type { ReactNode } from 'react';

interface Props {
  /** Minimum role tier required to view this route. */
  require: 'super-admin' | 'admin' | 'local-admin' | 'viewer';
  children: ReactNode;
  /** Where to redirect if the check fails. Defaults to /overview/services. */
  redirectTo?: string;
}

/**
 * Navigation guard that redirects unauthorised users.
 * Must be used inside a ProtectedRoute (i.e. after session check).
 *
 * Role tiers:
 *   'viewer'      — any authenticated user
 *   'local-admin' — local-admin, tenant-owner, admin, super-admin
 *   'admin'       — admin, super-admin
 *   'super-admin' — super-admin only
 *
 * @example
 *   <Route path="users" element={
 *     <RoleGuard require="local-admin"><UsersPage /></RoleGuard>
 *   } />
 */
export function RoleGuard({ require, children, redirectTo = '/overview/services' }: Props) {
  const ctx = useTenantContext();

  const allowed =
    require === 'super-admin' ? ctx.isSuperAdmin :
    require === 'admin'       ? ctx.isAdmin :
    require === 'local-admin' ? ctx.isLocalAdmin :
    ctx.isViewer;

  if (!allowed) return <Navigate to={redirectTo} replace />;
  return <>{children}</>;
}
