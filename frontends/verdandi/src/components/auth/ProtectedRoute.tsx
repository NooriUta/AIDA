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

  // Absolute "/login" — react-router v6's <Navigate> auto-prepends the
  // BrowserRouter `basename`, so this resolves to "/login" in standalone mode
  // and "/verdandi/login" when verdandi is mounted inside Shell.
  //
  // We deliberately do NOT use a relative path: the catch-all `path="*"` route
  // matches at *the current pathname*, so relative `to="login"` resolves into
  // an unbounded loop `/foo/login → /foo/login/login → /foo/login/login/login…`
  // when an unauthenticated user lands on any unknown URL.
  if (!isAuthenticated) return <Navigate to="/login" replace />;

  return <>{children}</>;
}
