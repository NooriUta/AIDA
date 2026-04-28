import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import type { ReactNode } from 'react';
import { HEIMDALL_ALLOWED_ROLES } from './users/types';
import type { UserRole } from './users/types';

export function ProtectedRoute({ children }: { children: ReactNode }) {
  const isAuthenticated = useAuthStore(s => s.isAuthenticated);
  const user            = useAuthStore(s => s.user);

  if (!isAuthenticated) return <Navigate to="login" replace />;

  if (user && !HEIMDALL_ALLOWED_ROLES.includes(user.role as UserRole)) {
    return (
      <div style={{
        display: 'flex', flexDirection: 'column', alignItems: 'center',
        justifyContent: 'center', height: '100vh', gap: 12,
        background: 'var(--bg0)', color: 'var(--t1)',
      }}>
        <div style={{ fontSize: 48 }}>🚫</div>
        <div style={{ fontSize: 18, fontWeight: 700 }}>Доступ запрещён</div>
        <div style={{ fontSize: 13, color: 'var(--t3)' }}>
          Heimdall доступен только ролям: admin, super-admin, tenant-owner, local-admin, auditor
        </div>
        <div style={{ fontSize: 12, color: 'var(--t3)' }}>
          Ваша роль: <code>{user.role}</code>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}
