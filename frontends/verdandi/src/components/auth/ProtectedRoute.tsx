import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';

interface Props {
  children: ReactNode;
}

export function ProtectedRoute({ children }: Props) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated);
  const isLoading       = useAuthStore((s) => s.isLoading);

  // While the session check is in-flight, render nothing to avoid a premature
  // redirect.  The App-level useEffect calls checkSession() immediately, so the
  // loading window is very short (one network round-trip).
  if (isLoading) return null;

  // Use a RELATIVE path so the redirect is correct both in standalone mode
  // ("/login") and when verdandi is mounted inside Shell at "/verdandi/*"
  // ("/verdandi/login").  React Router v6 resolves relative <Navigate to>
  // against the currently matched route segment.
  if (!isAuthenticated) return <Navigate to="login" replace />;

  return <>{children}</>;
}
