import { lazy, Suspense, useEffect } from 'react';
import { Routes, Route, useLocation } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
// i18n must be initialised before any component renders — importing it here
// ensures it runs when App is loaded as an MF remote (main.tsx is not executed
// in that case, so we can't rely on main.tsx's import).  ES module singletons
// guarantee this runs exactly once even when both main.tsx and App.tsx import it.
import './i18n/config';
// React Flow CSS must also be imported here for MF-remote mode — main.tsx is
// not executed when verdandi is loaded as a remote by Shell.
import '@xyflow/react/dist/style.css';
import { Shell } from './components/layout/Shell';
import { LoginPage } from './components/auth/LoginPage';
import { ProtectedRoute } from './components/auth/ProtectedRoute';
import { ErrorBoundary } from './components/ErrorBoundary';
import { ToastContainer } from './components/Toast';
import { UnderConstructionPage } from './components/stubs/UnderConstructionPage';
import { useAuthStore }   from './stores/authStore';
import { usePrefsSync }  from './hooks/usePrefsSync';
import { applyDom }      from './stores/prefsStore';
import type { ServerPrefs } from './stores/prefsStore';
import { useLoomStore }  from './stores/loomStore';

const KnotPage = lazy(() =>
  import('./components/knot/KnotPage').then((m) => ({ default: m.KnotPage })),
);

const InspectorProto = lazy(() =>
  import('./components/inspector/InspectorProto').then((m) => ({ default: m.InspectorProto })),
);

// Module-level QueryClient singleton — shared across standalone and MF-remote usage.
// When verdandi runs inside the shell, the shell does not provide a QueryClient,
// so verdandi creates and owns its own.
const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30_000,
      retry: 1,
      refetchOnWindowFocus: false,
      networkMode: 'always',
    },
  },
});

queryClient.getQueryCache().subscribe((event) => {
  if (event.type === 'updated' && event.action.type === 'error') {
    const onError = event.query.meta?.onError as ((e: unknown) => void) | undefined;
    onError?.(event.action.error);
  }
});

/**
 * Verdandi root component — exported as MF remote (verdandi/App).
 *
 * Does NOT include BrowserRouter — the Router context is provided by the
 * host (Shell) when running as a remote, or by main.tsx when running standalone.
 * This prevents the "You cannot render a <Router> inside another <Router>" error.
 */
export default function App() {
  const checkSession = useAuthStore((s) => s.checkSession);

  // Verify the httpOnly cookie is still valid after page reload.
  // If the 8h token expired, checkSession clears state → ProtectedRoute redirects.
  useEffect(() => { checkSession(); }, []); // eslint-disable-line react-hooks/exhaustive-deps

  // Cross-MF prefs broadcast: when HEIMDALL changes palette/theme, apply here too
  useEffect(() => {
    const h = (e: Event) => applyDom((e as CustomEvent<Partial<ServerPrefs>>).detail);
    window.addEventListener('aida:prefs', h);
    return () => window.removeEventListener('aida:prefs', h);
  }, []);

  // Tenant switch: navigate to L1 and invalidate all cached queries so data
  // re-fetches from the newly selected tenant's ArcadeDB.
  useEffect(() => {
    const h = () => {
      useLoomStore.getState().navigateToLevel('L1');
      queryClient.invalidateQueries();
    };
    window.addEventListener('seer-tenant-changed', h);
    return () => window.removeEventListener('seer-tenant-changed', h);
  }, []);

  // Sync Verdandi preferences to/from Keycloak (R4.14)
  usePrefsSync();

  return (
    <QueryClientProvider client={queryClient}>
      <AppRoutes />
      <ToastContainer />
    </QueryClientProvider>
  );
}

/**
 * Route declarations use RELATIVE paths (no leading "/") so they work correctly
 * both in standalone mode (BrowserRouter at /) and when loaded as an MF remote
 * inside Shell (mounted at /verdandi/*).
 *
 * React Router v6 resolves relative routes against the matched parent segment,
 * so "login" resolves to "/login" standalone and "/verdandi/login" inside Shell.
 */
function AppRoutes() {
  const { pathname } = useLocation();

  return (
    <ErrorBoundary resetKey={pathname}>
      <Routes>
        <Route path="login" element={<LoginPage />} />

        {/* dev-only prototype routes — no auth guard */}
        <Route
          path="proto/inspector"
          element={<Suspense fallback={null}><InspectorProto /></Suspense>}
        />

        <Route
          path="knot"
          element={
            <ProtectedRoute>
              <Suspense fallback={null}>
                <KnotPage />
              </Suspense>
            </ProtectedRoute>
          }
        />

        <Route
          path="urd"
          element={
            <ProtectedRoute>
              <UnderConstructionPage module="URD" horizon="H3" descriptionKey="stub.urdDescription" />
            </ProtectedRoute>
          }
        />
        <Route
          path="skuld"
          element={
            <ProtectedRoute>
              <UnderConstructionPage module="SKULD" horizon="H3" descriptionKey="stub.skuldDescription" />
            </ProtectedRoute>
          }
        />

        {/* Catch-all: protected shell — matches "/" standalone and "/verdandi/*" in shell */}
        <Route
          path="*"
          element={
            <ProtectedRoute>
              <Shell />
            </ProtectedRoute>
          }
        />
      </Routes>
    </ErrorBoundary>
  );
}
