import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuthStore } from '../../stores/authStore';

interface Props {
  children: ReactNode;
}

export function ProtectedRoute({ children }: Props) {
  const isAuthenticated   = useAuthStore((s) => s.isAuthenticated);
  const isCheckingSession = useAuthStore((s) => s.isCheckingSession);

  // While checkSession is in-flight, render nothing — avoids premature redirect
  // both in standalone mode and when loaded as an MF remote inside Shell
  // (Shell's session was already validated, but Verdandi's store hasn't caught
  // up yet via its own /auth/me call).
  if (isCheckingSession) return null;

  // Use a RELATIVE path so the redirect is correct both in standalone mode
  // ("/login") and when verdandi is mounted inside Shell at "/verdandi/*"
  // ("/verdandi/login").
  if (!isAuthenticated) return <Navigate to="login" replace />;

  return <>{children}</>;
}
